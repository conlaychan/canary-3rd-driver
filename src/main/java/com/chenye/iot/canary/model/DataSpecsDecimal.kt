package com.chenye.iot.canary.model

import java.math.BigDecimal

data class DataSpecsDecimal constructor(
    val min: BigDecimal,
    val max: BigDecimal,
    val step: BigDecimal,

    val unit: String? = null,
    val unitName: String? = null
) : DataSpecsSupportStruct<BigDecimal>, DataSpecsSupportArray<BigDecimal> {

    override fun validateSchema(): Result {
        if (min >= max) {
            return Result.fail("DECIMAL数据类型中最小值必须小于最大值")
        }
        if (step <= BigDecimal.ZERO) {
            return Result.fail("DECIMAL数据类型中步长必须大于0")
        }
        if ((unit == null && unitName != null) || (unit != null && unitName == null)) {
            return Result.fail("DECIMAL数据类型中单位符号和单位名称必须同时指定或同时不指定")
        }
        return Result.succeed()
    }

    override fun parseValue(value: String): BigDecimal {
        return when {
            "true".equals(value, true) -> {
                BigDecimal.ONE
            }
            "false".equals(value, true) -> {
                BigDecimal.ZERO
            }
            else -> {
                value.toBigDecimal()
            }
        }
    }

    override fun validateValue(value: BigDecimal): Boolean {
        return value in min..max
    }

    override fun type(): DataType = DataType.DECIMAL

    override fun desc(): String {
        return "${type()}[${min}, ${max}]步长${step}"
    }

    override fun desc(value: BigDecimal): String {
        return value.toString() + (unit ?: "")
    }
}

