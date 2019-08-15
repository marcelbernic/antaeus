package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.cron.FirstOfMonthCron
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject


class BillingService @Inject constructor(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService,
    private val firstOfMonthCron: FirstOfMonthCron
) : TaskExecutor {

    companion object {
        val log: Logger = LoggerFactory.getLogger("BillingService")
    }

    init {
        activateService()
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

    fun activateService() {
        log.info("Activating the service")

        firstOfMonthCron.registerTaskExecutor(this)
    }

    fun disableService() {
        log.info("Deactivating the service")

        firstOfMonthCron.removeTaskExecutor(this)
    }

    override fun execute() {
        chargeUnpaidInvoices()
    }
}