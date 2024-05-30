package com.chenye.iot.canary.model

import java.io.Serializable

/**
 * 支持内嵌在结构体里的数据类型
 */
interface DataSpecsSupportStruct<V : Serializable> : DataSpecs<V> {

    fun dataTypeSpecsStruct(): DataTypeSpecsStruct {
        return DataTypeSpecsStruct(StructDataType.valueOf(type().name), this)
    }

}
