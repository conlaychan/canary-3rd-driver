package com.chenye.iot.canary.model

import com.fasterxml.jackson.databind.JavaType
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger

/**
 * 可嵌入到数组中的数据类型
 */
enum class ArrayDataType {
    INT {
        /**
         * @return ArrayList<BigInteger>
         */
        override fun parseArrayValues(json: String, specs: DataSpecsSupportArray<Serializable>): ArrayList<Serializable> {
            return try {
                ThingModelJsonMapper.readValue(json, listBigInt)
            } catch (e: Exception) {
                tryBooleanList(json, { BigInteger.ONE }, { BigInteger.ZERO }) ?: throw e
            }
        }
    },
    DECIMAL {
        /**
         * @return ArrayList<BigDecimal>
         */
        override fun parseArrayValues(json: String, specs: DataSpecsSupportArray<Serializable>): ArrayList<Serializable> {
            return try {
                ThingModelJsonMapper.readValue(json, listDecimal)
            } catch (e: Exception) {
                tryBooleanList(json, { BigDecimal.ONE }, { BigDecimal.ZERO }) ?: throw e
            }
        }
    },
    TEXT {
        /**
         * @return ArrayList<String>
         */
        override fun parseArrayValues(json: String, specs: DataSpecsSupportArray<Serializable>): ArrayList<Serializable> {
            return ThingModelJsonMapper.readValue(json, listString)
        }
    },
    STRUCT {
        /**
         * @return ArrayList<HashMap<String, Serializable>>
         */
        override fun parseArrayValues(json: String, specs: DataSpecsSupportArray<Serializable>): ArrayList<Serializable> {
            val converted = specs as DataSpecsStruct
            val listMapString: List<Map<String, String>> = ThingModelJsonMapper.readValue(json, structType)
            return ArrayList(listMapString.map { converted.parseValue(ThingModelJsonMapper.writeValueAsString(it)) })
        }
    };

    companion object {
        val listBigInt: JavaType =
            ThingModelJsonMapper.typeFactory.constructCollectionType(ArrayList::class.java, BigInteger::class.java)
        val listDecimal: JavaType =
            ThingModelJsonMapper.typeFactory.constructCollectionType(ArrayList::class.java, BigDecimal::class.java)
        val listString: JavaType =
            ThingModelJsonMapper.typeFactory.constructCollectionType(ArrayList::class.java, String::class.java)
        val structType: JavaType =
            ThingModelJsonMapper.typeFactory.constructParametricType(ArrayList::class.java, DataSpecsStruct.mapType)

        private fun tryBooleanList(json: String, ifTrue: () -> Serializable, ifFalse: () -> Serializable): ArrayList<Serializable>? {
            val ss: List<String> = try {
                ThingModelJsonMapper.readValue(json, listString)
            } catch (e: Exception) {
                return null
            }
            val res = ArrayList<Serializable>()
            for (s in ss) {
                val v = when {
                    "true".equals(s, true) -> ifTrue()
                    "false".equals(s, true) -> ifFalse()
                    else -> return null
                }
                res.add(v)
            }
            return res
        }
    }

    /**
     * 反序列化数组 json 值到 Java 类型
     */
    abstract fun parseArrayValues(json: String, specs: DataSpecsSupportArray<Serializable>): ArrayList<Serializable>
}
