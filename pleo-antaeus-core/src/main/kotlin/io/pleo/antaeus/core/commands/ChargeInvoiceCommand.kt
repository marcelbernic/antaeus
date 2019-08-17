package io.pleo.antaeus.core.commands

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.Command
import io.pleo.antaeus.core.services.CommandResult
import io.pleo.antaeus.models.Invoice

class ChargeInvoiceCommand (
        private val paymentProvider: PaymentProvider,
        private val invoice: Invoice) : Command {

    override fun execute() : CommandResult {
        println("Inside command PAYING invoice ${invoice.id}")
        // Pay (Debit from client)
        val paymentResult = paymentProvider.charge(invoice)
        println("For Invoide(${invoice.id} the RESULT WAS $paymentResult")
        return ChargeInvoiceCommandResult(paymentResult, invoice)
        // Update isSuccessful in database
        // Set Event
    }

    override fun toString(): String {
        return "Command(inv=${invoice.id})"
    }
}