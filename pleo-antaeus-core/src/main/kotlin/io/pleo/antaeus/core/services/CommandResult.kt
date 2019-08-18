package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.command.CommandStatus

interface CommandResult {
    fun status() : CommandStatus

    fun getObject() : Any
}