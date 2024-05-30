package com.chenye.iot.canary.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode

/**
 * 物模型-事件
 */
data class ThingModelEvent constructor(

    /**
     * 标识符，在三元组中唯一
     */
    val identifier: String,

    /**
     * 名称，在三元组中唯一
     */
    val name: String,

    val type: EventType,

    val outputData: List<IOData>,

    val desc: String? = null
) {
    companion object {
        fun fromJson(json: String): ThingModelEvent {
            return this.fromJsonNode(ThingModelJsonMapper.readTree(json))
        }

        fun fromJsonNode(eventNode: JsonNode): ThingModelEvent {
            val identifier: String = eventNode.path("identifier").requireNonNull<TextNode>().asText()
            val name: String = eventNode.path("name").requireNonNull<TextNode>().asText()
            val type: EventType = EventType.valueOf(eventNode.path("type").requireNonNull<TextNode>().asText())
            val descNode: JsonNode = eventNode.path("desc")
            val desc: String? = if (descNode.isNull || descNode.isMissingNode) null else descNode.asText()
            val outputData = eventNode.path("outputData").takeUnless { it.isMissingNode || it.isNull }
                ?.let { ThingModelJsonMapper.fromJsonToListIOData(it.requireNonNull()) }
                ?: emptyList()
            return ThingModelEvent(identifier, name, type, outputData, desc)
        }
    }
}

