package com.example.digitalsignature.data.models

import java.util.*

data class VerificationResult (
    val state: ResultState,
    val signerName: String? = null,
    val location: String? = null,
    val reason: String? = null,
    val date: Calendar? = null
) {
    companion object {
        val EMPTY = VerificationResult(
            state = ResultState.RESULT_EMPTY,
            signerName = null,
            location = null,
            reason = null,
            date = null
        )
    }

    enum class ResultState {
        RESULT_OK,
        RESULT_FAIL,
        RESULT_EMPTY,
    }

    fun isOk(): Boolean {
        return state == ResultState.RESULT_OK
    }

    fun isFail(): Boolean {
        return state == ResultState.RESULT_FAIL
    }

    fun inNotEmpty(): Boolean {
        return state == ResultState.RESULT_OK || state == ResultState.RESULT_FAIL
    }

    fun isEmpty(): Boolean {
        return state == ResultState.RESULT_EMPTY
    }
}