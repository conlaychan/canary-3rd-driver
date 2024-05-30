package com.chenye.iot.canary.model

import com.fasterxml.jackson.annotation.JsonIgnore

sealed class Result constructor(
    val successful: Boolean
) {
    @JsonIgnore
    val failed: Boolean = !successful

    class SuccessfulResult : Result(true)
    class FailedResult constructor(val error: String) : Result(false)

    companion object {
        fun succeed(): SuccessfulResult {
            return SuccessfulResult()
        }

        fun fail(error: String): FailedResult {
            return FailedResult(error)
        }
    }

    fun ifSucceed(block: () -> Unit) {
        if (successful) {
            block()
        }
    }

    fun ifFail(block: (String) -> Unit) {
        if (failed && this is FailedResult) {
            block(this.error)
        }
    }

    /**
     * 失败时获取错误信息
     */
    fun failError(): String = (this as FailedResult).error

    override fun toString(): String {
        if (this is FailedResult) {
            return "FailedResult(successful=$successful, error=$error)"
        }
        return "SuccessfulResult(successful=$successful)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Result

        if (successful != other.successful) return false
        if (this is FailedResult && other is FailedResult) {
            if (error != other.error) return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = successful.hashCode()
        if (this is FailedResult) {
            result = 31 * result + (error.hashCode())
        }
        return result
    }

}
