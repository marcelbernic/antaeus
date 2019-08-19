package io.pleo.antaeus.models

enum class EventType {
    PAYMENT_SUCCESS,
    PAYMENT_START,
    PAYMENT_RETRY,
    PAYMENT_ERROR,
    INVALID_ENTITY,
    INEXISTENT_CLIENT,
    CURRENCY_MISMATCH
}
