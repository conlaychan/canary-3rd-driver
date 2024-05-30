package com.chenye.iot.canary.model

data class DataTypeSpecsStruct constructor(
    val type: StructDataType,
    val specs: DataSpecsSupportStruct<*>
)
