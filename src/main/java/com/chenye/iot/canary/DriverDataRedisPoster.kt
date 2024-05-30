package com.chenye.iot.canary

import com.chenye.iot.canary.model.ThingModelJsonMapper
import com.chenye.iot.canary.utils.BulkProcessor
import com.chenye.iot.canary.utils.DriverDataPoster
import com.chenye.iot.canary.utils.RedisChannel
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.LinkedBlockingQueue

@Component
class DriverDataRedisPoster(
    private val stringRedisTemplate: StringRedisTemplate
) : DriverDataPoster {

    override fun postPropertyValue(instanceId: Long, propertyIdentifier: String, bindRule: String, value: String?, readAt: LocalDateTime) {
        if (!working) {
            return
        }
        bufferedPropertyValues.offer(UpDevicePropertyValue(instanceId, propertyIdentifier, bindRule, value, readAt))
    }

    private val bufferedPropertyValues = LinkedBlockingQueue<UpDevicePropertyValue>()
    private var working = true
    private val bulkProcessor = BulkProcessor("redis批量广播")

    override fun close() {
        working = false
        bulkProcessor.stop()
    }

    init {
        bulkProcessor.consume(bufferedPropertyValues, 100, 100) { chunk ->
            stringRedisTemplate.convertAndSend(RedisChannel.UP_DEVICE_PROPERTY_VALUE, ThingModelJsonMapper.writeValueAsString(chunk))
        }
    }

    override fun postEvent(instanceId: Long, eventIdentifier: String, outputData: Map<String, String>, readAt: LocalDateTime) {
        if (!working) {
            return
        }
        stringRedisTemplate.convertAndSend(
            RedisChannel.UP_DEVICE_EVENT,
            ThingModelJsonMapper.writeValueAsString(UpDeviceOutput(instanceId, eventIdentifier, outputData, readAt))
        )
    }

    override fun postServiceOutputData(
        instanceId: Long,
        serviceIdentifier: String,
        outputData: Map<String, String>,
        readAt: LocalDateTime,
        sn: Int
    ) {
        if (!working) {
            return
        }
        stringRedisTemplate.convertAndSend(
            RedisChannel.UP_DEVICE_SERVICE_OUTPUT,
            ThingModelJsonMapper.writeValueAsString(UpDeviceOutput(instanceId, serviceIdentifier, outputData, readAt, sn))
        )
    }

    data class UpDevicePropertyValue(
        val driverInstanceId: Long,
        val propertyIdentifier: String,
        val bindRule: String,
        val value: String?,
        val readAt: LocalDateTime
    )

    data class UpDeviceOutput(
        val driverInstanceId: Long,
        /**
         * 服务或事件的identifier
         */
        val abilityIdentifier: String,
        /**
         * 服务或事件的出参
         */
        val outputData: Map<String, String>,

        /**
         * 执行完服务的时间 或 事件的发生时间
         */
        val readAt: LocalDateTime,

        val serialNumber: Int? = null
    )
}
