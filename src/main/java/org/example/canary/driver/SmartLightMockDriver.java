package org.example.canary.driver;

import com.chenye.iot.canary.model.*;
import com.chenye.iot.canary.utils.DriverUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SmartLightMockDriver extends IotDriver {

    @Override
    public String description() {
        return "模拟灯控驱动，与子系统平台进行通信";
    }

    @Override
    public ThingModel thingModel() {
        return DriverUtil.INSTANCE.loadThingModelJsonFile(SmartLightMockDriver.class);
    }

    private static final DataSpecsStruct INIT_PARAM_MODEL = new DataSpecsStruct(
            new ThingModelStructProperty("serverURI", "灯控系统http地址", new DataSpecsText(100).dataTypeSpecsStruct(), true),
            new ThingModelStructProperty("loginName", "登录用户名", new DataSpecsText(100).dataTypeSpecsStruct(), true),
            new ThingModelStructProperty("password", "登录密码", new DataSpecsText(100).dataTypeSpecsStruct(), true)
    );

    @Override
    public DataSpecsStruct initParamModel() {
        return INIT_PARAM_MODEL;
    }

    private static final DataSpecsStruct PROPERTY_BIND_RULE_MODEL = new DataSpecsStruct(
            new ThingModelStructProperty(
                    "lightId", "设备在子系统中的id",
                    new DataSpecsInt(BigInteger.valueOf(Integer.MIN_VALUE), BigInteger.valueOf(Integer.MAX_VALUE), BigInteger.ONE, null, null).dataTypeSpecsStruct(),
                    true
            )
    );

    @Override
    public DataSpecsStruct propertyBindRuleModel() {
        return PROPERTY_BIND_RULE_MODEL;
    }

    private String token;
    private ScheduledThreadPoolExecutor scheduler;
    /**
     * 缓存设备数据，key 即为 lightId
     */
    private ConcurrentHashMap<Integer, Device> deviceMap;
    /**
     * 定时开关的配置列表，key = lightId
     */
    private final ConcurrentHashMap<Integer, List<ScheduleSwitchConfig>> scheduleSwitchConfigs = new ConcurrentHashMap<>();

    @Override
    protected void start(String initParam, int cycleSeconds, Map<String, Set<String>> bindRules) {
        InitParam param = ThingModelJsonMapper.INSTANCE.fromJson(initParam, InitParam.class);
        this.token = "mock token";
        log.info("模拟登录：{}，获得token：{}", param, this.token);

        this.deviceMap = new ConcurrentHashMap<>();
        scheduler = new ScheduledThreadPoolExecutor(1);
        // 定时上报点位值
        scheduler.scheduleWithFixedDelay(() -> postPropertyValues(bindRules), cycleSeconds, cycleSeconds, TimeUnit.SECONDS);
        // 定时上报事件
        scheduler.scheduleWithFixedDelay(this::lowBatteryAlert, 10, 10, TimeUnit.MINUTES);
    }

    private void postPropertyValues(Map<String, Set<String>> bindRules) {
        bindRules.forEach((propertyIdentifier, rules) -> {
            for (String bindRule : rules) {
                BindRule rule = ThingModelJsonMapper.INSTANCE.fromJson(bindRule, BindRule.class);
                Device device = deviceMap.computeIfAbsent(rule.getLightId(), k -> new Device());
                Serializable value;
                switch (propertyIdentifier) {
                    case "color":
                        value = device.getColor();
                        break;
                    case "brightness":
                        value = device.getBrightness();
                        break;
                    case "switch":
                        value = device.getSwitch_();
                        break;
                    case "personalName":
                        value = device.getPersonalName();
                        break;
                    case "switchTime":
                        value = device.getSwitchTime();
                        break;
                    case "testDay":
                        value = device.getTestDay();
                        break;
                    case "gps":
                        value = ThingModelJsonMapper.INSTANCE.toJson(device.getGps());
                        break;
                    default:
                        log.error("未知属性（点位）类别：{}", propertyIdentifier);
                        value = null;
                }
                log.debug("模拟与子系统或设备通信，属性（点位）类别：{}，绑点规则反序列化：{}，采集到的实时值：{}", propertyIdentifier, rule, value);
                postPropertyValue(propertyIdentifier, bindRule, value);
            }
        });
    }

    @Override
    public void writeProperty(String propertyIdentifier, String bindRule, String value) {
        BindRule rule = ThingModelJsonMapper.INSTANCE.fromJson(bindRule, BindRule.class);
        Device device = deviceMap.computeIfAbsent(rule.getLightId(), k -> new Device());
        switch (propertyIdentifier) {
            case "color":
                device.setColor(Integer.valueOf(value));
                break;
            case "brightness":
                device.setBrightness(Integer.valueOf(value));
                break;
            case "switch":
                device.setSwitch_(Boolean.valueOf(value));
                break;
            case "personalName":
                device.setPersonalName(value);
                break;
            case "switchTime":
                log.error("属性（点位）类别：{} 不是可写的", propertyIdentifier);
                break;
            case "testDay":
                device.setTestDay(LocalDate.parse(value));
                break;
            case "gps":
                device.setGps(ThingModelJsonMapper.INSTANCE.fromJson(value, Gps.class));
                break;
            default:
                log.error("未知属性（点位）类别：{}", propertyIdentifier);
        }
    }

    @Override
    public void stop() {
        this.token = null;
        this.deviceMap = null;
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        this.scheduleSwitchConfigs.clear();
    }

    private void lowBatteryAlert() {
        Random random = new Random();
        for (Integer lightId : deviceMap.keySet()) {
            Map<String, String> outputData = new HashMap<>();
            outputData.put("lightId", lightId.toString());
            outputData.put("batteryPercent", Integer.toString(random.nextInt(21)));
            super.postEvent("lowBatteryAlert", outputData);
        }
    }

    @Override
    public Map<String, String> executeService(String serviceIdentifier, Map<String, String> inputData) {
        Map<String, String> outputData;
        switch (serviceIdentifier) {
            case "scheduleSwitch":
                outputData = scheduleSwitch(inputData);
                break;
            case "getScheduleList":
                outputData = getScheduleList(inputData);
                break;
            case "simpleService":
                outputData = simpleService(inputData);
                break;
            default:
                log.error("未知服务：{}", serviceIdentifier);
                outputData = new HashMap<>();
        }
        return outputData;
    }

    private Map<String, String> scheduleSwitch(Map<String, String> inputData) {
        Map<String, String> output = new LinkedHashMap<>();
        String lightId = inputData.get("lightId");
        String configListJson = inputData.get("configList");
        if (lightId == null || configListJson == null) {
            output.put("configResult", "false");
            return output;
        }
        List<ScheduleSwitchConfig> configList = ThingModelJsonMapper.INSTANCE.fromJsonToList(configListJson, ScheduleSwitchConfig.class);
        configList.sort(Comparator.comparing(ScheduleSwitchConfig::getSwitchTime));
        scheduleSwitchConfigs.put(Integer.valueOf(lightId), configList);
        output.put("configResult", "true");
        return output;
    }

    private Map<String, String> getScheduleList(Map<String, String> inputData) {
        Map<String, String> output = new LinkedHashMap<>();
        String lightId = inputData.get("lightId");
        if (lightId != null) {
            List<ScheduleSwitchConfig> configs = scheduleSwitchConfigs.get(Integer.valueOf(lightId));
            if (configs != null) {
                output.put("configList", ThingModelJsonMapper.INSTANCE.toJson(configs));
                return output;
            }
        }
        output.put("configList", "[]");
        return output;
    }

    private Map<String, String> simpleService(Map<String, String> inputData) {
        return inputData;
    }

    @Data
    private static class InitParam {
        private String serverURI;
        private String loginName;
        private String password;
    }

    @Data
    private static class BindRule {
        private Integer lightId;
    }

    @Data
    private static class Device {
        private Integer color = 1;
        private Integer brightness = 100;
        @JsonProperty("switch")
        private Boolean switch_ = false;
        private String personalName = "智能灯";
        private Long switchTime = System.currentTimeMillis();
        private LocalDate testDay = LocalDate.now();
        private Gps gps = new Gps();
    }

    @Data
    private static class Gps {
        private Integer coordinateSystem = 1;
        private BigDecimal longitude = BigDecimal.ZERO;
        private BigDecimal latitude = BigDecimal.ZERO;
    }

    @Data
    private static class ScheduleSwitchConfig {
        private Long switchTime;
        @JsonProperty("switch")
        private Boolean switch_;
    }
}
