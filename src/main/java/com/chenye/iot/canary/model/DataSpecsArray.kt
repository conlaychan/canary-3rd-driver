package com.chenye.iot.canary.model

import java.io.Serializable

data class DataSpecsArray constructor(
    val size: Int,
    val item: DataSpecsArrayItem<Serializable>
) : DataSpecs<ArrayList<Serializable>> {

    override fun validateSchema(): Result {
        if (size < 1) {
            return Result.fail("ARRAY数据类型中数组长度不能小于1")
        }
        val validateSchema = item.specs.validateSchema()
        if (validateSchema.failed) {
            return Result.fail("ARRAY数据类型中的参数定义错误：${validateSchema.failError()}")
        }
        return Result.succeed()
    }

    override fun parseValue(value: String): ArrayList<Serializable> {
        return item.type.parseArrayValues(value, item.specs)
    }

    override fun validateValue(value: ArrayList<Serializable>): Boolean {
        // 数组中内嵌结构体时要做更多校验
        if (item.type == ArrayDataType.STRUCT) {
            @Suppress("UNCHECKED_CAST")
            val objects: ArrayList<HashMap<String, Serializable>> = value as ArrayList<HashMap<String, Serializable>>
            for (obj in objects) {
                if (!item.specs.validateValue(obj)) {
                    return false
                }
            }
        }
        return true
    }

    override fun type(): DataType = DataType.ARRAY

    override fun desc(): String {
        if (item.type != ArrayDataType.STRUCT) {
            return "${type()}（${item.type}）"
        }

        val sb = StringBuilder(type().name).append("（")
        sb.append(item.specs.desc())
        return sb.append("）").toString()
    }

    override fun desc(value: ArrayList<Serializable>): String {
        return if (item.type != ArrayDataType.STRUCT) {
            // 数组里面放简单类型，直接拼接
            value.joinToString("，") { it.toString() }
        } else {
            // 数组里面放结构体，卧槽！
            value.joinToString("；") { (item.specs as DataSpecsStruct).desc(it as HashMap<String, Serializable>) }
        }
    }
}


