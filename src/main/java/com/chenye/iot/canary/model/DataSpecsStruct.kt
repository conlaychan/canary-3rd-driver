package com.chenye.iot.canary.model

import com.fasterxml.jackson.databind.type.MapType
import com.google.common.base.Joiner
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.util.function.Predicate
import java.util.function.UnaryOperator

class DataSpecsStruct constructor(
    property: ThingModelStructProperty,
    vararg properties: ThingModelStructProperty
) : DataSpecs<HashMap<String, Serializable>>, ArrayList<ThingModelStructProperty>(), DataSpecsSupportArray<HashMap<String, Serializable>> {

    init {
        super.add(property)
        super.addAll(properties.toList())
    }

    companion object {
        val mapType: MapType =
            ThingModelJsonMapper.typeFactory.constructMapType(
                HashMap::class.java,
                String::class.java,
                String::class.java
            )
    }

    override fun validateSchema(): Result {
        // 校验 id 和 name 的唯一性
        val ids = mutableSetOf<String>()
        val names = mutableSetOf<String>()
        for (structProperty in this) {
            val identifier = structProperty.identifier
            val name = structProperty.name
            if (ids.contains(identifier)) {
                return Result.fail("STRUCT数据类型中的参数id【$identifier】重复")
            } else {
                ids.add(identifier)
            }
            if (names.contains(name)) {
                return Result.fail("STRUCT数据类型中的参数名称【$name】重复")
            } else {
                names.add(name)
            }
            // 内嵌校验
            val validateSchema = structProperty.dataType.specs.validateSchema()
            if (validateSchema.failed) {
                return Result.fail("STRUCT数据类型中参数【$identifier】定义错误：${validateSchema.failError()}")
            }
        }

        // 正则校验 id 和 name
        for (structProperty in this) {
            val regexIdentifier = ThingModel.regexIdentifier(structProperty.identifier, "STRUCT数据类型中的参数")
            if (regexIdentifier.failed) {
                return regexIdentifier
            }
            val regexName = ThingModel.regexName(structProperty.name, "STRUCT数据类型中的参数")
            if (regexName.failed) {
                return regexName
            }
        }

        return Result.succeed()
    }

    /**
     * json转为hash对象
     */
    override fun parseValue(value: String): HashMap<String, Serializable> {
        val res: HashMap<String, Serializable> = hashMapOf()
        val mapString: Map<String, String> = ThingModelJsonMapper.readValue(value, mapType)
        for (property in this) {
            val itemValue: String? = mapString[property.identifier]
            if (itemValue != null) {
                res[property.identifier] = property.dataType.specs.parseValue(itemValue)
            }
        }
        return res
    }

    override fun validateValue(value: HashMap<String, Serializable>): Boolean {
        for (thingModelStructProperty in this) {
            val v: Serializable? = value[thingModelStructProperty.identifier]
            if (thingModelStructProperty.required && v == null) {
                return false
            }
            if (v != null) {
                val specsSupportStruct = thingModelStructProperty.dataType.specs
                when (thingModelStructProperty.dataType.type) {
                    StructDataType.INT -> {
                        val specs = specsSupportStruct as DataSpecsInt
                        if (!specs.validateValue(v as BigInteger)) {
                            return false
                        }
                    }
                    StructDataType.DECIMAL -> {
                        val specs = specsSupportStruct as DataSpecsDecimal
                        if (!specs.validateValue(v as BigDecimal)) {
                            return false
                        }
                    }
                    StructDataType.ENUM -> {
                        val specs = specsSupportStruct as DataSpecsEnum
                        if (!specs.validateValue(v as Int)) {
                            return false
                        }
                    }
                    StructDataType.BOOL -> {
                        val specs = specsSupportStruct as DataSpecsBool
                        if (!specs.validateValue(v as Boolean)) {
                            return false
                        }
                    }
                    StructDataType.TEXT -> {
                        val specs = specsSupportStruct as DataSpecsText
                        if (!specs.validateValue(v as String)) {
                            return false
                        }
                    }
                    StructDataType.DATE -> {
                        val specs = specsSupportStruct as DataSpecsDate
                        if (!specs.validateValue(v as Long)) {
                            return false
                        }
                    }
                    StructDataType.DAY -> {
                        val specs = specsSupportStruct as DataSpecsDay
                        if (!specs.validateValue(v as LocalDate)) {
                            return false
                        }
                    }
                }
            }
        }
        return true
    }

    override fun type(): DataType = DataType.STRUCT

    override fun desc(): String {
        val sb = StringBuilder(type().name).append("（")
        this.forEachIndexed { i, p ->
            if (i != 0) {
                sb.append("，")
            }
            sb.append(p.identifier).append("：").append(p.dataType.specs.desc())
        }
        return sb.append("）").toString()
    }

    override fun desc(value: HashMap<String, Serializable>): String {
        val descMap = mutableMapOf<String, String?>()
        for (structProperty in this) {
            descMap[structProperty.name] = value[structProperty.identifier]?.let { structProperty.dataType.specs.descStringValue(it.toString()) }
        }
        return Joiner.on("，").withKeyValueSeparator("：").join(descMap)
    }

    override fun add(element: ThingModelStructProperty): Boolean {
        throw UnsupportedOperationException()
    }

    override fun add(index: Int, element: ThingModelStructProperty) {
        throw UnsupportedOperationException()
    }

    override fun addAll(elements: Collection<ThingModelStructProperty>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun addAll(index: Int, elements: Collection<ThingModelStructProperty>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun clear() {
        throw UnsupportedOperationException()
    }

    override fun remove(element: ThingModelStructProperty): Boolean {
        throw UnsupportedOperationException()
    }

    override fun removeAll(elements: Collection<ThingModelStructProperty>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun removeAt(index: Int): ThingModelStructProperty {
        throw UnsupportedOperationException()
    }

    override fun removeIf(filter: Predicate<in ThingModelStructProperty>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        throw UnsupportedOperationException()
    }

    override fun replaceAll(operator: UnaryOperator<ThingModelStructProperty>) {
        throw UnsupportedOperationException()
    }

    override fun retainAll(elements: Collection<ThingModelStructProperty>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun set(index: Int, element: ThingModelStructProperty): ThingModelStructProperty {
        throw UnsupportedOperationException()
    }

    override fun sort(c: Comparator<in ThingModelStructProperty>?) {
        throw UnsupportedOperationException()
    }

}


