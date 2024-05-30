package com.chenye.iot.canary.model

import java.io.Serializable

interface DataSpecs<V : Serializable> {

    /**
     * 验证自身的结构是否正确
     */
    fun validateSchema(): Result

    /**
     * 尝试转换数值，但不验证是否符合数据定义
     *
     * 抛出任何异常都意味着数值不符合约定的格式
     */
    fun parseValue(value: String): V

    /**
     * 验证数值是否符合数据定义
     */
    fun validateValue(value: V): Boolean

    fun validateStringValue(value: String): Result {
        val parsed = try {
            parseValue(value)
        } catch (e: Throwable) {
            return Result.fail("数据格式不符合物模型定义")
        }
        val validated = validateValue(parsed)
        if (!validated) {
            return Result.fail("数值不符合物模型定义")
        }
        return Result.succeed()
    }

    fun type(): DataType

    fun desc(): String

    /**
     * 把值翻译成业务语言
     */
    fun desc(value: V): String?

    /**
     * 把值翻译成业务语言
     */
    fun descStringValue(stringValue: String): String? {
        return try {
            desc(parseValue(stringValue))
        } catch (e: Exception) {
            null
        }
    }

    fun dataTypeSpecs(): DataTypeSpecs {
        return DataTypeSpecs(type(), this)
    }
}
