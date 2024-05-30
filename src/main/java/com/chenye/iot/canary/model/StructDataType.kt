package com.chenye.iot.canary.model

/**
 * 可嵌入到结构体中的数据类型
 */
enum class StructDataType(
    val type: DataType
) {
    INT(DataType.INT),
    DECIMAL(DataType.DECIMAL),
    ENUM(DataType.ENUM),
    BOOL(DataType.BOOL),
    TEXT(DataType.TEXT),
    DATE(DataType.DATE),
    DAY(DataType.DAY);

    companion object {
        private val VALUES: Array<StructDataType> = values()

        fun fromDataType(type: DataType): StructDataType {
            for (structDataType in VALUES) {
                if (structDataType.type == type) {
                    return structDataType
                }
            }
            throw IllegalArgumentException()
        }
    }
}
