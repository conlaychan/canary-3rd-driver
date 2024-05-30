package com.chenye.iot.canary.model

import java.io.Serializable

/**
 * 支持内嵌在数组里的数据类型
 */
interface DataSpecsSupportArray<V : Serializable> : DataSpecs<V> {

    fun dataTypeSpecsArray(): DataSpecsArrayItem<Serializable> {
        return DataSpecsArrayItem(this) as DataSpecsArrayItem<Serializable>
    }

}
