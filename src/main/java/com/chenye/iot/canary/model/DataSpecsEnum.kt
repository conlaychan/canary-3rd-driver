package com.chenye.iot.canary.model

import java.util.*
import java.util.function.BiFunction
import java.util.function.Function

class DataSpecsEnum constructor(
    pair: Pair<Int, String>,
    vararg pairs: Pair<Int, String>
) : DataSpecsSupportStruct<Int>, TreeMap<Int, String>() {

    companion object {
        fun ofMap(map: Map<Int, String>): DataSpecsEnum {
            if (map.isEmpty()) {
                throw IllegalArgumentException()
            }
            var first: Pair<Int, String>? = null
            val rest: MutableList<Pair<Int, String>> = mutableListOf()
            for (entry in map) {
                if (first == null) {
                    first = entry.toPair()
                } else {
                    rest.add(entry.toPair())
                }
            }
            return DataSpecsEnum(first!!, *rest.toTypedArray())
        }
    }

    init {
        super.put(pair.first, pair.second)
        pairs.forEach { super.put(it.first, it.second) }
    }

    override fun validateSchema(): Result {
        if (this.values.toSet().size < this.size) {
            return Result.fail("ENUM数据类型中参数描述不可相同")
        }
        return Result.succeed()
    }

    override fun parseValue(value: String): Int {
        return value.toInt()
    }

    override fun validateValue(value: Int): Boolean {
        return this[value] != null
    }

    override fun type(): DataType = DataType.ENUM

    override fun desc(): String {
        val sb = StringBuilder(type().name).append("（")
        var index = 0
        this.forEach { (k, v) ->
            if (index != 0) {
                sb.append("，")
            }
            sb.append(k).append("：").append(v)
            index++
        }
        return sb.append("）").toString()
    }

    override fun desc(value: Int): String? {
        return this[value]
    }

    override fun clear() {
        throw UnsupportedOperationException()
    }

    override fun compute(key: Int, remappingFunction: BiFunction<in Int, in String?, out String?>): String? {
        throw UnsupportedOperationException()
    }

    override fun computeIfAbsent(key: Int, mappingFunction: Function<in Int, out String>): String {
        throw UnsupportedOperationException()
    }

    override fun computeIfPresent(key: Int, remappingFunction: BiFunction<in Int, in String, out String?>): String? {
        throw UnsupportedOperationException()
    }

    override fun merge(
        key: Int,
        value: String,
        remappingFunction: BiFunction<in String, in String, out String?>
    ): String? {
        throw UnsupportedOperationException()
    }

    override fun put(key: Int, value: String): String? {
        throw UnsupportedOperationException()
    }

    override fun putAll(from: Map<out Int, String>) {
        throw UnsupportedOperationException()
    }

    override fun putIfAbsent(key: Int, value: String): String? {
        throw UnsupportedOperationException()
    }

    override fun remove(key: Int): String? {
        throw UnsupportedOperationException()
    }

    override fun remove(key: Int?, value: String?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun replace(key: Int, oldValue: String, newValue: String): Boolean {
        throw UnsupportedOperationException()
    }

    override fun replace(key: Int, value: String): String? {
        throw UnsupportedOperationException()
    }

    override fun replaceAll(function: BiFunction<in Int, in String, out String>) {
        throw UnsupportedOperationException()
    }

}

