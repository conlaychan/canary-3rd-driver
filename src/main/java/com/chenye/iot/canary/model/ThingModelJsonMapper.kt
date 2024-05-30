package com.chenye.iot.canary.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.YearMonthSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.YearSerializer
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.text.SimpleDateFormat
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

object ThingModelJsonMapper : ObjectMapper() {

    init {
        this.registerKotlinModule()
        this.setSerializationInclusion(JsonInclude.Include.ALWAYS)
        this.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        this.setTimeZone(TimeZone.getDefault())
        this.dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        this.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
        val javaTimeModule = JavaTimeModule()
        javaTimeModule.addSerializer(Year::class.java, YearSerializer(DateTimeFormatter.ofPattern("uuuu")))
        javaTimeModule.addSerializer(YearMonth::class.java, YearMonthSerializer(DateTimeFormatter.ofPattern("uuuu-MM")))
        this.registerModule(javaTimeModule)
        this.registerModule(Jdk8Module())
    }

    fun <T> fromJson(json: String, clazz: Class<T>): T {
        return super.readValue(json, clazz)
    }

    fun <T> fromJson(jsonString: String, javaType: JavaType): T {
        return super.readValue(jsonString, javaType)
    }

    fun <T> fromJsonToList(json: String, elementClass: Class<T>): ArrayList<T?> {
        return fromJson(json, constructParametricType(ArrayList::class.java, elementClass))
    }

    fun constructParametricType(parametrized: Class<*>, vararg elementClasses: Class<*>): JavaType {
        return typeFactory.constructParametricType(parametrized, *elementClasses)
    }

    fun toJson(obj: Any): String {
        return super.writeValueAsString(obj)
    }

    /**
     * 反序列化 json 到 物模型对象，抛出任何异常都说明 json 结构不正确
     */
    fun fromJson(json: String): ThingModel {
        try {
            return this.fromJson0(json)
        } catch (th: Throwable) {
            throw IllegalArgumentException("反序列化 json 到物模型对象失败", th)
        }
    }

    private fun fromJson0(json: String): ThingModel {
        val root: JsonNode = this.readTree(json)
        val properties = mutableListOf<ThingModelProperty>()
        val services = mutableListOf<ThingModelService>()
        val events = mutableListOf<ThingModelEvent>()

        val pathProperties = root.path("properties")
        if (pathProperties.isArray) {
            val propertiesNode: ArrayNode = pathProperties.requireNonNull()
            for (propertyNode in propertiesNode) {
                properties.add(ThingModelProperty.fromJsonNode(propertyNode))
            }
        }

        val pathServices = root.path("services")
        if (pathServices.isArray) {
            val servicesNode: ArrayNode = pathServices.requireNonNull()
            for (serviceNode in servicesNode) {
                services.add(ThingModelService.fromJsonNode(serviceNode))
            }
        }

        val pathEvents = root.path("events")
        if (pathEvents.isArray) {
            val eventsNode: ArrayNode = pathEvents.requireNonNull()
            for (eventNode in eventsNode) {
                events.add(ThingModelEvent.fromJsonNode(eventNode))
            }
        }

        return ThingModel(properties, services, events)
    }

    fun fromJsonToListIOData(arrayNode: ArrayNode): List<IOData> {
        val ioDatas = mutableListOf<IOData>()
        for (ioDataNode in arrayNode) {
            val identifier: String = ioDataNode.path("identifier").requireNonNull<TextNode>().asText()
            val name: String = ioDataNode.path("name").requireNonNull<TextNode>().asText()
            val dataType: DataTypeSpecs = DataTypeSpecs.fromJsonNode(ioDataNode.path("dataType").requireNonNull())
            ioDatas.add(IOData(identifier, name, dataType))
        }
        return ioDatas
    }

    /**
     * 将阿里云 iot 平台的 tsl 物模型 json 描述转为我们自己的物模型对象
     */
    fun fromAliyunIotTsl(aliyunIotTsl: String): ThingModel {
        val replace: String = aliyunIotTsl
            .replace(dataTypeInt_regex, "\"type\": \"${DataType.INT}\"")
            .replace(dataTypeFloat_regex, "\"type\": \"${DataType.DECIMAL}\"")
            .replace(dataTypeDouble_regex, "\"type\": \"${DataType.DECIMAL}\"")
            .replace(dataTypeEnum_regex, "\"type\": \"${DataType.ENUM}\"")
            .replace(dataTypeBool_regex, "\"type\": \"${DataType.BOOL}\"")
            .replace(dataTypeText_regex, "\"type\": \"${DataType.TEXT}\"")
            .replace(dataTypeDate_regex, "\"type\": \"${DataType.DATE}\"")
            .replace(dataTypeStruct_regex, "\"type\": \"${DataType.STRUCT}\"")
            .replace(dataTypeArray_regex, "\"type\": \"${DataType.ARRAY}\"")
            .replace(accessModeRead_regex, "\"accessMode\": \"${AccessMode.READ_ONLY}\"")
            .replace(accessModeReadWrite_regex, "\"accessMode\": \"${AccessMode.READ_WRITE}\"")
            .replace(keyBool0_regex, """"false":""")
            .replace(keyBool1_regex, """"true":""")
        return this.fromJson(replace)
    }

    private val dataTypeInt_regex = """"type"\s*:\s*"int"""".toRegex()
    private val dataTypeFloat_regex = """"type"\s*:\s*"float"""".toRegex()
    private val dataTypeDouble_regex = """"type"\s*:\s*"double"""".toRegex()
    private val dataTypeEnum_regex = """"type"\s*:\s*"enum"""".toRegex()
    private val dataTypeBool_regex = """"type"\s*:\s*"bool"""".toRegex()
    private val dataTypeText_regex = """"type"\s*:\s*"text"""".toRegex()
    private val dataTypeDate_regex = """"type"\s*:\s*"date"""".toRegex()
    private val dataTypeStruct_regex = """"type"\s*:\s*"struct"""".toRegex()
    private val dataTypeArray_regex = """"type"\s*:\s*"array"""".toRegex()
    private val accessModeRead_regex = """"accessMode"\s*:\s*"r"""".toRegex()
    private val accessModeReadWrite_regex = """"accessMode"\s*:\s*"rw"""".toRegex()
    private val keyBool0_regex = """"0"\s*:""".toRegex()
    private val keyBool1_regex = """"1"\s*:""".toRegex()
}
