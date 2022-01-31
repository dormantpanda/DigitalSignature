package com.example.digitalsignature.data.models

enum class SigningResult {
    AUTH_SUCCESS,
    AUTH_FAILED,
    TOO_MANY_ATTEMPTS,
    AUTH_ERROR,
    AUTH_CANCELED,
    EMPTY,
    COMPLETED,
    NO_HARDWARE,
    SENSOR_DISABLED
}