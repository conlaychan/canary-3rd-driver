package com.chenye.iot.canary.utils

import com.chenye.iot.canary.model.ThingModel
import com.chenye.iot.canary.model.ThingModelJsonMapper
import org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets

object DriverUtil {

    /**
     * 从json文件反序列化物模型
     */
    fun loadThingModelJsonFile(driverClass: Class<*>): ThingModel {
        val inputStream = driverClass.getResourceAsStream("/物模型/${driverClass.simpleName}.json")!!
        val json = inputStream.use {
            IOUtils.toString(it, StandardCharsets.UTF_8)
        }
        return ThingModelJsonMapper.fromJson(json)
    }

    fun javaBeanToStringMap(javaBean: Any): MutableMap<String, String> {
        val output = mutableMapOf<String, String>()
        for ((key, value) in ThingModelJsonMapper.convertValue(javaBean, Map::class.java)) {
            if (key !is CharSequence) {
                continue
            }
            if (value is CharSequence || value is Number || value is Boolean) {
                output[key.toString()] = value.toString()
            }
        }
        return output
    }

}
