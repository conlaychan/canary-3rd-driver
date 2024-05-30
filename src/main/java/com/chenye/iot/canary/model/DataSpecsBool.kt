package com.chenye.iot.canary.model

data class DataSpecsBool constructor(
    val `false`: String,
    val `true`: String
) : DataSpecsSupportStruct<Boolean> {

    companion object {
        fun parseValue(value: String): Boolean {
            if ("1" == value || "true".equals(value, true)) {
                return true
            }
            if ("0" == value || "false".equals(value, true)) {
                return false
            }
            throw IllegalArgumentException("BOOL数据类型仅接受 true/false")
        }
    }

    override fun validateSchema(): Result {
        if (`false`.equals(`true`, true)) {
            return Result.fail("BOOL数据类型中参数描述不可相同")
        }
        return Result.succeed()
    }

    override fun parseValue(value: String): Boolean {
        return Companion.parseValue(value)
    }

    override fun validateValue(value: Boolean): Boolean {
        return true
    }

    override fun type(): DataType = DataType.BOOL

    override fun desc(): String {
        return "${type()}（false：${`false`}，true：${`true`}）"
    }

    override fun desc(value: Boolean): String {
        return if (value) `true` else `false`
    }
}


