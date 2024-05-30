package com.chenye.iot.canary;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chenye.iot.canary.dao.DeviceAbility;
import com.chenye.iot.canary.dao.DeviceAbilityMapper;
import com.chenye.iot.canary.dao.DriverInstance;
import com.chenye.iot.canary.dao.DriverInstanceMapper;
import com.chenye.iot.canary.model.DataSpecs;
import com.chenye.iot.canary.model.IOData;
import com.chenye.iot.canary.model.ThingModelJsonMapper;
import com.chenye.iot.canary.model.ThingModelService;
import com.chenye.iot.canary.utils.ClassScanner;
import com.chenye.iot.canary.utils.DriverDataPoster;
import com.chenye.iot.canary.utils.RedisChannel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.canary.driver.IotDriver;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class Canary3rdDriverInstanceScheduler implements MessageListener {

    private final DriverDataPoster driverDataPoster;
    private final StringRedisTemplate stringRedisTemplate;
    private final DriverInstanceMapper driverInstanceMapper;
    private final DeviceAbilityMapper deviceAbilityMapper;
    private final ConcurrentHashMap<Long, IotDriver> instanceMap = new ConcurrentHashMap<>();

    @PostConstruct
    protected void init() {
        stringRedisTemplate.delete(RedisChannel.KEY_STANDALONE_DRIVER);
        Set<Class<?>> classes = ClassScanner.INSTANCE.scanByPackages(Collections.singleton(IotDriver.class.getPackage().getName()), Thread.currentThread().getContextClassLoader());
        for (Class<?> clazz : classes) {
            if (IotDriver.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    this.registerDriver(clazz);
                    log.info("驱动 {} 注册成功", clazz);
                } catch (Throwable t) {
                    log.error("驱动 {} 注册失败", clazz, t);
                }
            }
        }

        this.autoStartDriverInstances();
    }

    private void registerDriver(Class<?> clazz) throws Exception {
        IotDriver instance = (IotDriver) clazz.getDeclaredConstructor().newInstance();
        Map<String, Object> driverInfo = new LinkedHashMap<>();
        driverInfo.put("name", clazz.getName());
        driverInfo.put("thingModel", Objects.requireNonNull(instance.thingModel()));
        driverInfo.put("video", false);
        driverInfo.put("description", instance.description());
        driverInfo.put("initParamModel", instance.initParamModel());
        driverInfo.put("propertyBindRuleModel", instance.propertyBindRuleModel());
        stringRedisTemplate.opsForHash().put(RedisChannel.KEY_STANDALONE_DRIVER, clazz.getName(), ThingModelJsonMapper.INSTANCE.toJson(driverInfo));
    }

    private void autoStartDriverInstances() {
        LambdaQueryWrapper<DriverInstance> query = Wrappers.lambdaQuery(DriverInstance.class)
                .eq(DriverInstance::getAutoStartEnabled, true)
                .eq(DriverInstance::getStandalone, true);
        List<DriverInstance> instances = driverInstanceMapper.selectList(query);
        for (DriverInstance instance : instances) {
            try {
                this.startDriverInstance(instance);
                log.info("自动启动驱动实例 {} 成功", instance.getDriverInstanceName());
            } catch (Throwable t) {
                log.error("自动启动驱动实例 {} 失败", instance.getDriverInstanceName(), t);
            }
        }
    }

    public void startDriverInstance(DriverInstance driverInstance) throws Exception {
        Long id = driverInstance.getId();
        if (this.instanceMap.get(id) != null) {
            return;
        }
        List<DeviceAbility> abilities = deviceAbilityMapper.listByDriverInstanceIdAndAbilityType(id, "PROPERTY");
        Map<String, Set<String>> bindRules = new HashMap<>();
        for (DeviceAbility ability : abilities) {
            String propertyIdentifier = ability.getDriverModelAbilityIdentifier();
            String bindRule = ability.getBindRule();
            bindRules.computeIfAbsent(propertyIdentifier, k -> new HashSet<>()).add(bindRule);
        }
        Class<?> clazz = Class.forName(driverInstance.getDriverName());
        IotDriver instance = (IotDriver) clazz.getDeclaredConstructor().newInstance();
        String instanceName = driverInstance.getDriverInstanceName();
        this.instanceMap.put(id, instance);
        instance.start(driverDataPoster, id, instanceName, driverInstance.getInitParam(), driverInstance.getCycleSeconds(), bindRules);
    }

    public void stop(Long instanceId) {
        IotDriver instance = this.instanceMap.get(instanceId);
        if (instance != null) {
            instance.stop();
            this.instanceMap.remove(instanceId);
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String body = new String(message.getBody());
        if (RedisChannel.DOWN_DEVICE_PROPERTY_VALUE.equals(channel)) {
            this.writeProperty(body);
        } else if (RedisChannel.DOWN_DEVICE_SERVICE_INTPUT.equals(channel)) {
            this.executeService(body);
        }
    }

    private void writeProperty(String message) {
        DownDevicePropertyValue data;
        try {
            data = ThingModelJsonMapper.INSTANCE.fromJson(message, DownDevicePropertyValue.class);
        } catch (Throwable e) {
            log.error("反序列化{}失败，原文json：{}", DownDevicePropertyValue.class, message, e);
            return;
        }
        if (data.jsonPropertyValueWrappers == null) {
            return;
        }
        Long id = data.driverInstanceId;
        IotDriver instance = this.instanceMap.get(id);
        if (instance == null) {
            log.error("驱动实例id：{}，没有启动", id);
            return;
        }
        for (JsonPropertyValueWrapper propertyValueWrapper : data.jsonPropertyValueWrappers) {
            instance.writeProperty(propertyValueWrapper.propertyIdentifier, propertyValueWrapper.bindRule, propertyValueWrapper.value);
        }
    }

    private void executeService(String message) {
        DownDeviceServiceInput data;
        try {
            data = ThingModelJsonMapper.INSTANCE.fromJson(message, DownDeviceServiceInput.class);
        } catch (Throwable e) {
            log.error("反序列化{}失败，原文json：{}", DownDeviceServiceInput.class, message, e);
            return;
        }
        Long driverInstanceId = data.getDriverInstanceId();
        IotDriver instance = this.instanceMap.get(driverInstanceId);
        if (instance == null) {
            log.error("驱动实例id：{}，没有启动", driverInstanceId);
            return;
        }
        boolean check = this.checkServiceInputData(instance, data.serviceIdentifier, data.inputData);
        if (!check) {
            return;
        }
        Map<String, String> outputData;
        try {
            outputData = instance.executeService(data.serviceIdentifier, data.inputData);
        } catch (Throwable e) {
            log.error(
                    "执行驱动服务时捕获到异常，driverInstanceId：{}，serviceIdentifier：{}，inputData：{}",
                    driverInstanceId, data.serviceIdentifier, data.inputData, e
            );
            return;
        }
        if (data.serialNumber != null) {
            driverDataPoster.postServiceOutputData(
                    driverInstanceId, data.serviceIdentifier, outputData, LocalDateTime.now(), data.serialNumber
            );
        }
    }

    /**
     * 检查服务的入参是否符合物模型中的数据定义
     */
    private boolean checkServiceInputData(
            IotDriver instance, String serviceIdentifier, Map<String, String> inputData
    ) {
        String name = instance.getInstanceName();
        ThingModelService service = instance.thingModel().getServices().stream().filter(it -> it.getIdentifier().equals(serviceIdentifier)).findFirst().orElse(null);
        if (service == null) {
            log.error("驱动实例：{}，物模型中没有服务id：{}", name, serviceIdentifier);
            return false;
        }
        for (IOData ioData : service.getInputData()) {
            String value = inputData.get(ioData.getIdentifier());
            if (value == null) {
                log.error(
                        "驱动实例：{}，服务id：{}，入参中缺少字段：{}，入参json：{}",
                        name, serviceIdentifier, ioData.getIdentifier(), ThingModelJsonMapper.INSTANCE.toJson(inputData)
                );
                return false;
            }
            boolean validate;
            try {
                @SuppressWarnings("unchecked")
                DataSpecs<Serializable> specs = (DataSpecs<Serializable>) ioData.getDataType().getSpecs();
                validate = specs.validateValue(specs.parseValue(value));
            } catch (Throwable e) {
                log.error(
                        "服务入参不符合数据定义，instanceName：{}, serviceIdentifier：{}，入参json：{}",
                        name, serviceIdentifier, ThingModelJsonMapper.INSTANCE.toJson(inputData), e
                );
                return false;
            }
            if (!validate) {
                log.error(
                        "服务入参不符合数据定义，instanceName：{}, serviceIdentifier：{}，入参json：{}，字段id：{}，字段值：{}",
                        name,
                        serviceIdentifier,
                        ThingModelJsonMapper.INSTANCE.toJson(inputData),
                        ioData.getIdentifier(),
                        value
                );
                return false;
            }
        }
        return true;
    }

    public List<Map<String, Object>> list() {
        List<Map<String, Object>> list = new ArrayList<>();
        this.instanceMap.forEach((id, instance) -> {
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("id", id);
            obj.put("name", instance.getInstanceName());
            list.add(obj);
        });
        return list;
    }

    @Data
    private static class DownDevicePropertyValue {
        private Long driverInstanceId;
        private List<JsonPropertyValueWrapper> jsonPropertyValueWrappers;
    }

    @Data
    private static class JsonPropertyValueWrapper {
        private String propertyIdentifier;
        /**
         * 绑定属性时的json
         */
        private String bindRule;

        /**
         * 属性值，对于BOOL数据类型这里是true/false
         */
        private String value;
    }

    @Data
    private static class DownDeviceServiceInput {
        private Long driverInstanceId;
        private String serviceIdentifier;
        private Map<String, String> inputData;
        private Integer serialNumber;
    }
}
