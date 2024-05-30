package com.chenye.iot.canary.utils

import java.io.Closeable
import java.time.LocalDateTime

interface DriverDataPoster : Closeable {

    /**
     * 向上推送设备属性实时值
     *
     * @param instanceId 驱动实例id
     * @param bindRule 绑定属性时的原始json
     * @param value 属性实时值，null表示没有采集到
     */
    fun postPropertyValue(instanceId: Long, propertyIdentifier: String, bindRule: String, value: String?, readAt: LocalDateTime)

    /**
     * 向上推送设备事件
     *
     * @param instanceId 驱动实例id
     * @param outputData 出参
     * @param readAt 事件的发生时间
     */
    fun postEvent(instanceId: Long, eventIdentifier: String, outputData: Map<String, String>, readAt: LocalDateTime)

    /**
     * 向上推送调用服务的结果（出参）
     *
     * @param instanceId 驱动实例id
     * @param serviceIdentifier 驱动层的服务id
     * @param outputData 出参
     * @param readAt 执行完服务的时间
     */
    fun postServiceOutputData(
        instanceId: Long,
        serviceIdentifier: String,
        outputData: Map<String, String>,
        readAt: LocalDateTime,
        sn: Int
    )

}
