package io.pleo.antaeus.core.command

import io.pleo.antaeus.core.services.CommandResult
import io.pleo.antaeus.models.Invoice

class ChargeInvoiceCommandResult (
        private val status: CommandStatus,
        private val invoice: Invoice) : CommandResult {

    override fun status() : CommandStatus {
        return status
    }

    override fun getObject(): Invoice {
        return invoice
    }
}