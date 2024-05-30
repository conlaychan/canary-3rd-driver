package com.chenye.iot.canary.model

data class IOData constructor(
    val identifier: String,
    val name: String,
    val dataType: DataTypeSpecs
)
