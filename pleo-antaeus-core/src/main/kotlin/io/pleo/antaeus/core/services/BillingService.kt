package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import startInvoiceProcessingPipeline
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingService @Inject constructor(
        private val paymentProvider: PaymentProvider,
        private val invoiceService: InvoiceService
)  {
    companion object {
        val log: Logger = LoggerFactory.getLogger("BillingService")
    }

    suspend fun sendInvoicesToPipeline(channel: Channel<Invoice>) {
        val unpaidInvoices = invoiceService.fetchByStatus(InvoiceStatus.PENDING)

        for (invoice in unpaidInvoices) {
            log.info("Sending invoice ${invoice.id}")
            channel.send(invoice)
        }
    }

    fun runTask(): () -> Unit {
        val chargeInvoices: () -> Unit = {
            GlobalScope.launch {
                log.info("START BILLING at -> ${LocalDateTime.now()}")
                val channel = Channel<Invoice>(1000000)
                startInvoiceProcessingPipeline(channel)
                sendInvoicesToPipeline(channel)
            }
        }
        return chargeInvoices
    }

}