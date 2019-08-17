package io.pleo.antaeus.core.commands

import io.pleo.antaeus.core.services.CommandResult
import io.pleo.antaeus.models.Invoice

class ChargeInvoiceCommandResult (
        private val commandSuccess: Boolean,
        private val invoice: Invoice) : CommandResult {

    override fun isSuccessful() : Boolean {
        return commandSuccess
    }

    override fun getObject(): Invoice {
        return invoice
    }
}