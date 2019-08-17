package io.pleo.antaeus.core.services

interface CommandResult {
    fun isSuccessful() : Boolean

    fun getObject() : Any
}