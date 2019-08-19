package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.command.ChargeInvoiceCommand
import io.pleo.antaeus.core.command.Command
import io.pleo.antaeus.core.command.CommandResult
import io.pleo.antaeus.core.command.CommandStatus
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.scheduler.EventObserver
import io.pleo.antaeus.models.EventType
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import startInvoiceProcessingPipeline
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class BillingService @Inject constructor(
        private val paymentProvider: PaymentProvider,
        private val eventService: EventService,
        private val invoiceService: InvoiceService,
        private val customerService: CustomerService
)  : CoroutineScope, EventObserver {

    companion object {
        val log: Logger = LoggerFactory.getLogger("BillingService")
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job

    private val channelToPipeline = Channel<Command>(1000)
    private val channelFromPipeline = Channel<CommandResult>(1000)
    // To keep track how many times we retried for a specific invoice <Key=invoiceId, Value=numberOfRetries>
    private val numberOFRetries = ConcurrentHashMap<Int, Int>()
    private val retryStrategyDelays = arrayOf(0L, Const.ONE_SEC, Const.THREE_SEC, Const.FIVE_SEC)

    override fun runTask() {
        launch {
            log.info("Starting billing cycle at [${LocalDateTime.now()}]")
            startInvoiceProcessingPipeline(channelToPipeline, channelFromPipeline)
            sendInvoicesToPipeline()
            processPaymentResults()
        }
    }

    suspend fun sendInvoicesToPipeline() {
        val unpaidInvoices = invoiceService.fetchByStatus(InvoiceStatus.PENDING)

        for (invoice in unpaidInvoices) {
            if (isValid(invoice)) {
                log.info("Locking invoice #${invoice.id}")
                invoiceService.updateInvoice(invoice.id, InvoiceStatus.IN_PROGRES)
                log.info("Sending invoice #${invoice.id} to the processing pipeline")
                channelToPipeline.send(ChargeInvoiceCommand(paymentProvider, invoice))
            } else {
                log.info("Found Invalid invoice #${invoice.id}. Required manual attention by an Operator")
                invoiceService.updateInvoice(invoice.id, InvoiceStatus.INVALID)
            }
        }
    }

    private fun isValid(invoice: Invoice) : Boolean {
        return validateCustomer(invoice) && validateCurrency(invoice)
    }

    private fun validateCurrency(invoice: Invoice): Boolean {
        if (invoice.amount.currency != customerService.fetch(invoice.customerId).currency) {
            eventService.createEvent(invoice, EventType.CURRENCY_MISMATCH, "Mismatch currency for the client")
            return false
        }
        return true
    }

    private fun validateCustomer(invoice: Invoice): Boolean {
        try {
            customerService.fetch(invoice.customerId)
        } catch (ex: CustomerNotFoundException) {
            eventService.createEvent(invoice, EventType.INEXISTENT_CLIENT, "Trying to charge an inexistent client")
            return false
        }
        return true
    }

    suspend fun processPaymentResults() {
        for (commandResult in channelFromPipeline) {
            val invoice = commandResult.getObject() as Invoice
            when(commandResult.status()) {
                CommandStatus.SUCCESS -> invoiceService.updateInvoice(invoice.id, InvoiceStatus.PAID)
                CommandStatus.TIMEOUT -> retryBilling(invoice)
                CommandStatus.NETWORK_ERROR -> retryBilling(invoice)
                CommandStatus.UNKNOWN_ERROR -> retryBilling(invoice)
            }
        }
    }

    suspend fun retryBilling(invoice: Invoice) {
        val retriedTimes = numberOFRetries.get(invoice.id) ?: 1
        if (retriedTimes < 4) {
            log.info("Retry mechanism Triggered for ${invoice.id}, attempt #$retriedTimes")
            log.info("Waiting for ${retryStrategyDelays[retriedTimes]/1000} Seconds")
            eventService.createEvent(invoice, EventType.PAYMENT_RETRY, "Attempt = $retriedTimes")
            launch {
                delay(retryStrategyDelays[retriedTimes])
                val chargeInvoiceCommand = ChargeInvoiceCommand(paymentProvider, invoice)
                numberOFRetries.set(invoice.id, retriedTimes+1)
                channelToPipeline.send(chargeInvoiceCommand)
            }
        } else {
            // All attempts had failed
            log.info("All retry attempts had failed")
            // mark this invoice as ERROR (so he can take individual attention)
            // Generate an event in the database
            // Notify (client/administration)
            eventService.createEvent(invoice, EventType.PAYMENT_ERROR, "The payment did not succeeded (in $retriedTimes Attempts)")
            invoiceService.updateInvoice(invoice.id, InvoiceStatus.FAILED)
            numberOFRetries.remove(invoice.id)
        }

    }
}