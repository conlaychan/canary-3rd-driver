package com.chenye.iot.canary.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode

/**
 * 物模型-服务
 */
data class ThingModelService constructor(

    /**
     * 标识符，在三元组中唯一
     */
    val identifier: String,

    /**
     * 名称，在三元组中唯一
     */
    val name: String,

    /**
     * 异步还是同步，即是否等待响应。
     *
     * 在驱动层，该字段仅作为一个参考值，具体是异步还是同步由设备层决定。
     */
    val callType: CallType,

    val inputData: List<IOData>,

    val outputData: List<IOData>,

    val desc: String? = null
) {
    companion object {
        fun fromJson(json: String): ThingModelService {
            return this.fromJsonNode(ThingModelJsonMapper.readTree(json))
        }

        fun fromJsonNode(serviceNode: JsonNode): ThingModelService {
            val identifier: String = serviceNode.path("identifier").requireNonNull<TextNode>().asText()
            val name: String = serviceNode.path("name").requireNonNull<TextNode>().asText()
            val callType: CallType = CallType.valueOf(serviceNode.path("callType").requireNonNull<TextNode>().asText())
            val descNode: JsonNode = serviceNode.path("desc")
            val desc: String? = if (descNode.isNull || descNode.isMissingNode) null else descNode.asText()
            val inputData = serviceNode.path("inputData").takeUnless { it.isMissingNode || it.isNull }
                ?.let { ThingModelJsonMapper.fromJsonToListIOData(it.requireNonNull()) }
                ?: emptyList()
            val outputData = serviceNode.path("outputData").takeUnless { it.isMissingNode || it.isNull }
                ?.let { ThingModelJsonMapper.fromJsonToListIOData(it.requireNonNull()) }
                ?: emptyList()
            return ThingModelService(identifier, name, callType, inputData, outputData, desc)
        }
    }
}

