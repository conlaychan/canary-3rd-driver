package com.chenye.iot.canary.model

import java.io.Serializable
import java.time.LocalDate

class DataSpecsDay constructor() : DataSpecsSupportStruct<LocalDate>, Serializable {
    override fun validateSchema(): Result {
        return Result.succeed()
    }

    override fun parseValue(value: String): LocalDate {
        return LocalDate.parse(value)
    }

    override fun validateValue(value: LocalDate): Boolean {
        return true
    }

    override fun type(): DataType = DataType.DAY

    override fun desc(): String {
        return "${type()}（日期）"
    }

    override fun desc(value: LocalDate): String {
        return value.toString()
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other::class == this::class
    }

    override fun hashCode(): Int {
        return 0
    }

}
