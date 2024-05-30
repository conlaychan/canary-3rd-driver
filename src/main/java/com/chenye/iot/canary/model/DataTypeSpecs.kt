package com.chenye.iot.canary.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.module.kotlin.treeToValue

/**
 * 包装物模型中的 type 和 specs
 */
data class DataTypeSpecs constructor(
    val type: DataType,
    val specs: DataSpecs<*>
) {
    companion object {
        private val mapIntString: MapType =
            ThingModelJsonMapper.typeFactory.constructMapType(Map::class.java, Int::class.java, String::class.java)

        fun fromJson(json: String): DataTypeSpecs {
            val root: JsonNode = ThingModelJsonMapper.readTree(json)
            return fromJsonNode(root)
        }

        fun fromJsonNode(jsonNode: JsonNode): DataTypeSpecs {
            val type: DataType = DataType.valueOf(jsonNode.path("type").requireNonNull<TextNode>().asText())
            val specsNode: JsonNode = jsonNode.path("specs").requireNonNull()
            val specs: DataSpecs<*> =
                when (type) {
                    DataType.STRUCT -> {
                        fromJsonToDataSpecsStruct(specsNode.requireNonNull())
                    }
                    DataType.ARRAY -> {
                        val size: Int = specsNode.path("size").asInt()
                        val itemNode: ObjectNode = specsNode.path("item").requireNonNull()
                        val arrayDataType = ArrayDataType.valueOf(itemNode.path("type").requireNonNull<TextNode>().asText())
                        val specs =
                            when (arrayDataType) {
                                ArrayDataType.INT -> ThingModelJsonMapper.treeToValue<DataSpecsInt>(itemNode.path("specs"))
                                ArrayDataType.DECIMAL -> ThingModelJsonMapper.treeToValue<DataSpecsDecimal>(itemNode.path("specs"))
                                ArrayDataType.TEXT -> ThingModelJsonMapper.treeToValue<DataSpecsText>(itemNode.path("specs"))
                                ArrayDataType.STRUCT -> fromJsonToDataSpecsStruct(itemNode.path("specs").requireNonNull())
                            }
                        DataSpecsArray(size, specs!!.dataTypeSpecsArray())
                    }
                    DataType.INT -> ThingModelJsonMapper.treeToValue(specsNode, DataSpecsInt::class.java)
                    DataType.DECIMAL -> ThingModelJsonMapper.treeToValue(specsNode, DataSpecsDecimal::class.java)
                    DataType.ENUM -> {
                        val map: Map<Int, String> = ThingModelJsonMapper.readValue(specsNode.toString(), mapIntString)
                        if (map.isEmpty()) {
                            throw IllegalArgumentException("ENUM数据类型中至少要有一个参数定义")
                        }
                        var pair: Pair<Int, String>? = null
                        val pairs: MutableList<Pair<Int, String>> = mutableListOf()
                        for (entry in map) {
                            if (pair == null) {
                                pair = entry.key to entry.value
                            } else {
                                pairs.add(entry.key to entry.value)
                            }
                        }
                        DataSpecsEnum(pair!!, *pairs.toTypedArray())
                    }
                    DataType.BOOL -> ThingModelJsonMapper.treeToValue(specsNode, DataSpecsBool::class.java)
                    DataType.TEXT -> ThingModelJsonMapper.treeToValue(specsNode, DataSpecsText::class.java)
                    DataType.DATE -> ThingModelJsonMapper.treeToValue(specsNode, DataSpecsDate::class.java)
                    DataType.DAY -> ThingModelJsonMapper.treeToValue(specsNode, DataSpecsDay::class.java)
                }
            return DataTypeSpecs(type, specs)
        }

        fun fromJsonToDataSpecsStruct(structSpecs: ArrayNode): DataSpecsStruct {
            val list = mutableListOf<ThingModelStructProperty>()
            for (structSpecsNode in structSpecs) {
                val identifier: String = structSpecsNode.path("identifier").requireNonNull<TextNode>().asText()
                val name: String = structSpecsNode.path("name").requireNonNull<TextNode>().asText()
                val requiredText: String = structSpecsNode.path("required").asText().trim()
                val required: Boolean? = if ("1" == requiredText || "true".equals(requiredText, true)) {
                    true
                } else if ("0" == requiredText || "false".equals(requiredText, true)) {
                    false
                } else {
                    null
                }
                val dataTypeNode: ObjectNode = structSpecsNode.path("dataType").requireNonNull()
                val dataTypeSpecs: DataTypeSpecs = fromJsonNode(dataTypeNode)
                val structDataType = StructDataType.fromDataType(dataTypeSpecs.type)
                val specs: DataSpecsSupportStruct<*> = dataTypeSpecs.specs as DataSpecsSupportStruct<*>
                val property = if (required != null) {
                    ThingModelStructProperty(identifier, name, DataTypeSpecsStruct(structDataType, specs), required)
                } else {
                    ThingModelStructProperty(identifier, name, DataTypeSpecsStruct(structDataType, specs))
                }
                list.add(property)
            }
            if (list.isEmpty()) {
                throw IllegalArgumentException("STRUCT数据类型中至少要有一个参数定义")
            }
            var property: ThingModelStructProperty? = null
            val properties: MutableList<ThingModelStructProperty> = mutableListOf()
            for (p in list) {
                if (property == null) {
                    property = p
                } else {
                    properties.add(p)
                }
            }
            return DataSpecsStruct(property!!, *properties.toTypedArray())
        }
    }
}
