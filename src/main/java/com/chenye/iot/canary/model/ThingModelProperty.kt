package com.chenye.iot.canary.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode

/**
 * 物模型-属性
 */
data class ThingModelProperty constructor(

    /**
     * 标识符，在三元组中唯一
     */
    val identifier: String,

    /**
     * 名称，在三元组中唯一
     */
    val name: String,
    val accessMode: AccessMode,

    val dataType: DataTypeSpecs,

    val desc: String? = null
) {
    companion object {
        fun fromJson(json: String): ThingModelProperty {
            return this.fromJsonNode(ThingModelJsonMapper.readTree(json))
        }

        fun fromJsonNode(propertyNode: JsonNode): ThingModelProperty {
            val identifier: String = propertyNode.path("identifier").requireNonNull<TextNode>().asText()
            val name: String = propertyNode.path("name").requireNonNull<TextNode>().asText()
            val accessMode: AccessMode =
                AccessMode.valueOf(propertyNode.path("accessMode").requireNonNull<TextNode>().asText())
            val descNode: JsonNode = propertyNode.path("desc")
            val desc: String? = if (descNode.isNull || descNode.isMissingNode) null else descNode.asText()
            val dataType: DataTypeSpecs = DataTypeSpecs.fromJsonNode(propertyNode.path("dataType").requireNonNull())
            return ThingModelProperty(identifier, name, accessMode, dataType, desc)
        }
    }
}

