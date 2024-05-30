package com.chenye.iot.canary.model

/**
 * 全部（最外层）的数据类型
 */
enum class DataType(
    val isNumber: Boolean = false
) {

    INT(true),
    DECIMAL(true),
    ENUM,
    BOOL,
    TEXT,
    DATE, // 毫秒格式的时间戳
    DAY, // 只有年月日的字符串格式日期，如：1970-01-02
    STRUCT,
    ARRAY;

}
