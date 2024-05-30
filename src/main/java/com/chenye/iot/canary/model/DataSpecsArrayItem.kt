package com.chenye.iot.canary.model

import java.io.Serializable

data class DataSpecsArrayItem<V : Serializable> constructor(
    val specs: DataSpecsSupportArray<V>
) {
    val type: ArrayDataType = ArrayDataType.valueOf(specs.type().name)
}
