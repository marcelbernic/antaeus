package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice

class ChargeInvoiceCommand (
        private val paymentProvider: PaymentProvider,
        private val invoice: Invoice) : Command{

    override fun execute() {
        println("Inside command PAYING invoice ${invoice.id}")
    }

    override fun toString(): String {
        return "Command(inv=${invoice.id})"
    }
}