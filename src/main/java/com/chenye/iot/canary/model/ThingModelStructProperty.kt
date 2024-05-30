package com.chenye.iot.canary.model

/**
 * 物模型结构体属性
 */
data class ThingModelStructProperty constructor(
    /**
     * 标识符，在结构体中唯一
     */
    val identifier: String,

    /**
     * 名称，在结构体中唯一
     */
    val name: String,

    val dataType: DataTypeSpecsStruct,

    /**
     * 结构体中的该字段是否必填
     */
    val required: Boolean = true
)
