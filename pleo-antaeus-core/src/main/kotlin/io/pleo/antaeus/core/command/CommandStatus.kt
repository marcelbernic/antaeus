package io.pleo.antaeus.core.command

enum class CommandStatus {
    SUCCESS,
    TIMEOUT,
    CURRENCY_MISMATCH,
    NETWORK_ERROR,
    UNKNOWN_ERROR
}