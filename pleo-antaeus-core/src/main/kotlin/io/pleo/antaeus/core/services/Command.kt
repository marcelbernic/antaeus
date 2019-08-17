package io.pleo.antaeus.core.services

interface Command {
    fun execute() : CommandResult
}