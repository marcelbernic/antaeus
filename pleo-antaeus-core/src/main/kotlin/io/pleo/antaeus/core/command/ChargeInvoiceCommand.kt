package io.pleo.antaeus.core.command

import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice

class ChargeInvoiceCommand (
        private val paymentProvider: PaymentProvider,
        private val invoice: Invoice) : Command {

    override fun execute() : CommandResult {
        try {
            if (paymentProvider.charge(invoice)) {
                return ChargeInvoiceCommandResult(CommandStatus.SUCCESS, invoice)
            }
        } catch (e: Exception) {
            return ChargeInvoiceCommandResult(setCommandStatus(e), invoice)
        }
        return ChargeInvoiceCommandResult(CommandStatus.UNKNOWN_ERROR, invoice)
    }

    private fun setCommandStatus(exception: Exception) : CommandStatus {
        when(exception) {
            // We can easily add more exceptions types returned by external service
            // ex: The customer don't have enough founds
            is NetworkException -> return CommandStatus.NETWORK_ERROR
        }
        return CommandStatus.UNKNOWN_ERROR
    }

    override fun toString(): String {
        return "ChargeInvoiceCommand(invId=${invoice.id})"
    }
}