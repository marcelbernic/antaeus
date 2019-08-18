package io.pleo.antaeus.app

import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.random.Random

class DataInitializer @Inject constructor(private val dal: AntaeusDal) {

    companion object {
        val log: Logger = LoggerFactory.getLogger("DataInitializer")
    }

    fun setupInitialData() {
        log.info("Populate database with invoices")

        val customers = (1..10).mapNotNull {
            dal.createCustomer(
                    currency = Currency.values()[Random.nextInt(0, Currency.values().size)]
            )
        }

        customers.forEach { customer ->
            (1..2).forEach {
                dal.createInvoice(
                        amount = Money(
                                value = BigDecimal(Random.nextDouble(10.0, 500.0)),
                                currency = customer.currency
                        ),
                        customer = customer,
                        status = if (it == 1) InvoiceStatus.PENDING else InvoiceStatus.PAID
                )
            }
        }
    }
}