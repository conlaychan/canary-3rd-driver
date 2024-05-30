package com.chenye.iot.canary.model

enum class AccessMode(
    val writeable: Boolean
) {

    READ_WRITE(true),
    READ_ONLY(false);

}
