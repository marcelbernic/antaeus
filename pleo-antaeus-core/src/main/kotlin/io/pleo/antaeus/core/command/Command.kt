package io.pleo.antaeus.core.command

interface Command {
    fun execute() : CommandResult
}