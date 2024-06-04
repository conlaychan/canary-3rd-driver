package org.example.canary.driver;

import com.chenye.iot.canary.model.DataSpecsStruct;
import com.chenye.iot.canary.model.ThingModel;
import com.chenye.iot.canary.model.ThingModelJsonMapper;
import com.chenye.iot.canary.utils.DriverDataPoster;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class IotDriver {

    private DriverDataPoster driverDataPoster;
    private Long instanceId;

    /**
     * 关于驱动的使用说明
     *
     * @return maybe null
     */
    public abstract String description();

    /**
     * 声明物模型
     *
     * @return never null
     */
    public abstract ThingModel thingModel();

    /**
     * 声明初始化驱动实例时所需的参数
     *
     * @return maybe null
     */
    public abstract DataSpecsStruct initParamModel();

    /**
     * 声明绑定属性时所用的json（bind_rule）的结构
     *
     * @return maybe null
     */
    public abstract DataSpecsStruct propertyBindRuleModel();

    /**
     * 获取外部传入的驱动实例id，如果此实例从未启动过将会报错
     *
     * @return nevel null
     */
    private Long getInstanceId() {
        if (instanceId == null) {
            throw new IllegalStateException("驱动实例从未启动过，无法确定实例id");
        }
        return instanceId;
    }

    private DriverDataPoster getDriverDataPoster() {
        if (driverDataPoster == null) {
            throw new IllegalStateException("驱动实例从未启动过");
        }
        return driverDataPoster;
    }

    /**
     * 初始化驱动实例，建立通信连接，以确保后续的属性读写、服务调用、事件监听等工作可以正常进行。
     *
     * @param initParam    初始化驱动实例的参数json（nevel null）
     * @param cycleSeconds 采集点位值的间隔时间（秒）
     * @param bindRules    驱动实例所需承担的所有点位。
     *                     key：属性（点位）类别，例如：color、brightness、switchTime。
     *                     values：json字符串格式的绑点规则，例如：{"lightId": 123}。
     */
    protected abstract void start(String initParam, int cycleSeconds, Map<String, Set<String>> bindRules);

    public final void start(DriverDataPoster driverDataPoster, Long instanceId, String initParam, int cycleSeconds, Map<String, Set<String>> bindRules) {
        this.driverDataPoster = Objects.requireNonNull(driverDataPoster);
        this.instanceId = Objects.requireNonNull(instanceId);
        this.start(initParam == null || initParam.isEmpty() ? "{}" : initParam, cycleSeconds, bindRules);
    }

    /**
     * 写入属性值
     *
     * @param propertyIdentifier 属性（点位）类别，例如：color、brightness、switchTime。
     * @param bindRule           json字符串格式的绑点规则，例如：{"lightId": 123}。
     * @param value              属性值，never null。
     */
    public abstract void writeProperty(String propertyIdentifier, String bindRule, String value);

    /**
     * 执行服务
     *
     * @param serviceIdentifier 服务id，已经过物模型的校验，子类无需再校验
     * @param inputData         入参，已经过物模型的校验，子类无需再校验
     * @return 服务的出参
     */
    public abstract Map<String, String> executeService(String serviceIdentifier, Map<String, String> inputData);

    /**
     * 向平台的规则引擎发送点位实时值
     *
     * @param propertyIdentifier 属性（点位）类别，不可空
     * @param bindRule           json字符串格式的绑点规则，不可空
     * @param value              实时值，可为null
     */
    protected void postPropertyValue(String propertyIdentifier, String bindRule, Serializable value) {
        if (value instanceof List || value instanceof Map) {
            value = ThingModelJsonMapper.INSTANCE.toJson(value);
        }
        getDriverDataPoster().postPropertyValue(getInstanceId(), propertyIdentifier, bindRule, value == null ? null : value.toString(), LocalDateTime.now());
    }

    /**
     * 向平台的规则引擎推送设备事件
     *
     * @param outputData 出参
     */
    protected void postEvent(String eventIdentifier, Map<String, String> outputData) {
        getDriverDataPoster().postEvent(getInstanceId(), eventIdentifier, outputData, LocalDateTime.now());
    }

    /**
     * 销毁驱动实例，关闭通信连接，释放各种资源。
     */
    public abstract void stop();
}
