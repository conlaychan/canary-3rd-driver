package com.chenye.iot.canary.model

data class DataSpecsText constructor(
    /**
     * 最多允许的字符数量，null 表示不限制
     */
    val length: Int?
) : DataSpecsSupportStruct<String>, DataSpecsSupportArray<String> {
    override fun validateSchema(): Result {
        if (length != null && length <= 0) {
            return Result.fail("TEXT数据类型中长度必须大于0")
        }
        return Result.succeed()
    }

    override fun parseValue(value: String): String {
        return value
    }

    override fun validateValue(value: String): Boolean {
        return length == null || length >= value.length
    }

    override fun type(): DataType = DataType.TEXT

    override fun desc(): String {
        return "${type()}（字符串）"
    }

    override fun desc(value: String): String {
        return value
    }
}

