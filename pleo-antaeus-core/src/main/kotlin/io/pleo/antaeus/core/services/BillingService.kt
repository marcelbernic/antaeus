package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.command.ChargeInvoiceCommand
import io.pleo.antaeus.core.command.CommandStatus
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
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

    companion object {
        val log: Logger = LoggerFactory.getLogger("BillingService")
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job

    fun runTask(): () -> Unit {
        val chargeInvoices: () -> Unit = {
            launch {
                log.info("Starting billing cycle at [${LocalDateTime.now()}]")

                val channelToPipeline = Channel<Command>(1000)
                val channelFromPipeline = Channel<CommandResult>(1000)
                startInvoiceProcessingPipeline(channelToPipeline, channelFromPipeline)

                sendInvoicesToPipeline(channelToPipeline)
                processPaymentResults(channelFromPipeline)
            }
        }
        return chargeInvoices
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

    suspend fun processPaymentResults(channel: Channel<CommandResult>) {
        for (commandResult in channel) {
            val invoice = commandResult.getObject() as Invoice
            when(commandResult.status()) {
                CommandStatus.SUCCESS -> invoiceService.updateInvoice(invoice.id, InvoiceStatus.PAID)
                CommandStatus.TIMEOUT -> log.info("Retry mechanism")
                CommandStatus.CURRENCY_MISMATCH -> log.info("Throw error")
                CommandStatus.NETWORK_ERROR -> log.info("Retry mechanism")
                CommandStatus.UNKNOWN_ERROR -> log.info("Retry mechanism")
            }
        }
    }
}