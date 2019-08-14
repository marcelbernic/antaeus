package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger("BillingService")
    }

    fun chargeUnpaidInvoices(): List<Invoice>  {
        var unpaidInvoices = invoiceService.fetchByStatus(InvoiceStatus.PENDING)

        for (invoice in unpaidInvoices) {
            if (paymentProvider.charge(invoice)) {
                invoiceService.updateInvoice(invoice.id, InvoiceStatus.PAID)
            } else {
                log.error("Something really wrong just happened")
            }
        }
        return unpaidInvoices
    }
}