package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.InvoiceStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import startInvoiceProcessingPipeline
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class BillingService @Inject constructor(
        private val paymentProvider: PaymentProvider,
        private val invoiceService: InvoiceService
)  : CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job

    companion object {
        val log: Logger = LoggerFactory.getLogger("BillingService")
    }

    suspend fun sendInvoicesToPipeline(channel: Channel<Command>) {
        val unpaidInvoices = invoiceService.fetchByStatus(InvoiceStatus.PENDING)

        for (invoice in unpaidInvoices) {
            log.info("Locking invoice #${invoice.id}")
            invoiceService.updateInvoice(invoice.id, InvoiceStatus.IN_PROGRES)
            log.info("Sending invoice #${invoice.id} to the processing pipeline")
            val chargeInvoiceCommand = ChargeInvoiceCommand(paymentProvider, invoice)
            channel.send(chargeInvoiceCommand)
        }
    }

    fun runTask(): () -> Unit {
        val chargeInvoices: () -> Unit = {
            launch {
                log.info("Starting billing cycle at -> ${LocalDateTime.now()}")
                val channel = Channel<Command>(1000)
                startInvoiceProcessingPipeline(channel)
                sendInvoicesToPipeline(channel)
            }
        }
        return chargeInvoices
    }

}