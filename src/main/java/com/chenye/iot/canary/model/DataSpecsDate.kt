package com.chenye.iot.canary.model

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class DataSpecsDate constructor() : DataSpecsSupportStruct<Long>, Serializable {
    override fun validateSchema(): Result {
        return Result.succeed()
    }

    override fun parseValue(value: String): Long {
        return value.toLong()
    }

    override fun validateValue(value: Long): Boolean {
        return true
    }

    override fun type(): DataType = DataType.DATE

    override fun desc(): String {
        return "${type()}（毫秒时间戳）"
    }

    override fun desc(value: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(value))
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other::class == this::class
    }

    override fun hashCode(): Int {
        return 0
    }
}

