package com.chenye.iot.canary.model

import java.math.BigInteger

data class DataSpecsInt constructor(
    val min: BigInteger,
    val max: BigInteger,
    val step: BigInteger,

    val unit: String? = null,
    val unitName: String? = null
) : DataSpecsSupportStruct<BigInteger>, DataSpecsSupportArray<BigInteger> {

    override fun validateSchema(): Result {
        if (min >= max) {
            return Result.fail("${DataType.INT}数据类型中最小值必须小于最大值")
        }
        if (step <= BigInteger.ZERO) {
            return Result.fail("${DataType.INT}数据类型中步长必须大于0")
        }
        if ((unit == null && unitName != null) || (unit != null && unitName == null)) {
            return Result.fail("${DataType.INT}数据类型中单位符号和单位名称必须同时指定或同时不指定")
        }
        return Result.succeed()
    }

    override fun parseValue(value: String): BigInteger {
        return when {
            "true".equals(value, true) -> {
                BigInteger.ONE
            }
            "false".equals(value, true) -> {
                BigInteger.ZERO
            }
            else -> {
                value.toBigInteger()
            }
        }
    }

    override fun validateValue(value: BigInteger): Boolean {
        return value in min..max
    }

    override fun type(): DataType = DataType.INT

    override fun desc(): String {
        return "${type()}[${min}, ${max}]步长${step}"
    }

    override fun desc(value: BigInteger): String {
        return value.toString() + (unit ?: "")
    }
}

