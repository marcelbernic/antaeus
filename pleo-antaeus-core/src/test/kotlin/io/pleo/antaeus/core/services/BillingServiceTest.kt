package io.pleo.antaeus.core.services

import dagger.Component
import io.mockk.every
import io.pleo.antaeus.core.TestAntaeusModule
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.EventType
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Component(modules = [TestAntaeusModule::class])
interface BillingServiceTestContext {
    fun initialize(app: BillingServiceTest)
}

class BillingServiceTest {
    @Inject
    lateinit var paymentProvider: PaymentProvider
    @Inject
    lateinit var customerService: CustomerService
    @Inject
    lateinit var eventService: EventService
    @Inject
    lateinit var invoiceService: InvoiceService
    @Inject
    lateinit var dal: AntaeusDal
    @Inject
    lateinit var billingService: BillingService

    @BeforeEach
    fun setup() {
        DaggerBillingServiceTestContext.create().initialize(this)

        val customer1 = dal.createCustomer(Currency.EUR)
        val customer2 = dal.createCustomer(Currency.USD)
        val invoice1 = dal.createInvoice(Money(BigDecimal.valueOf(100), Currency.EUR), customer1!!)
        val invoice2 = dal.createInvoice(Money(BigDecimal.valueOf(200), Currency.USD), customer2!!)
    }

    @Test
    fun `will charge and update successfully invoices`() {
        every { paymentProvider.charge(any())} returns true

        billingService.runTask()
        Thread.sleep(3000)

        val invoicesAfterBilling = invoiceService.fetchAll()
        for (invoice in invoicesAfterBilling) {
            assertEquals(InvoiceStatus.PAID, invoice.status)
        }
        Assertions.assertEquals(2, invoicesAfterBilling.size)
    }

    @Test
    fun `will generate Successful events`() {
        every { paymentProvider.charge(any())} returns true

        billingService.runTask()
        Thread.sleep(3000)

        val eventsAfterBilling = eventService.fetchAll()
        for (event in eventsAfterBilling) {
            assertEquals(EventType.PAYMENT_SUCCESS, event.type)
        }
    }
}
