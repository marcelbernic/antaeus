package io.pleo.antaeus.core.services

import dagger.Component
import io.pleo.antaeus.core.TestAntaeusModule
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Component(modules = [TestAntaeusModule::class])
interface InvoiceServiceTestContext {
    fun initialize(app: InvoiceServiceTest)
}

class InvoiceServiceTest {
    @Inject
    lateinit var invoiceService: InvoiceService

    @Inject
    lateinit var dal: AntaeusDal

    @BeforeEach
    fun setup() {
        DaggerInvoiceServiceTestContext.create().initialize(this)
    }

    @Test
    fun `will throw if customer is not found`() {
        assertThrows<InvoiceNotFoundException> {
            invoiceService.fetch(438916557)
        }
    }

    @Test
    fun `will find invoice when fetch and it is database`() {
        val customer = dal.createCustomer(Currency.EUR)
        val invoice = dal.createInvoice(Money(BigDecimal.valueOf(50), Currency.EUR), customer!!)

        assertEquals(invoice, invoiceService.fetch(invoice!!.id))
    }

    @Test
    fun `will return all invoices when fetch all`() {
        assertTrue(invoiceService.fetchAll().isEmpty())

        val a_customer = dal.createCustomer(Currency.EUR)
        val another_customer = dal.createCustomer(Currency.EUR)

        val invoice1 = dal.createInvoice(Money(BigDecimal.valueOf(55), Currency.EUR), a_customer!!)
        val invoice2 = dal.createInvoice(Money(BigDecimal.valueOf(70), Currency.EUR), another_customer!!)

        val allInvoces = invoiceService.fetchAll()
        assertTrue(allInvoces.contains(invoice1))
        assertTrue(allInvoces.contains(invoice2))
        assertEquals(2, allInvoces.size)
    }

    @Test
    fun `will fetch only invoice with specific status`() {
        assertTrue(invoiceService.fetchAll().isEmpty())

        val a_customer = dal.createCustomer(Currency.EUR)
        val another_customer = dal.createCustomer(Currency.EUR)

        val invoice1 = dal.createInvoice(Money(BigDecimal.valueOf(55), Currency.EUR), a_customer!!, InvoiceStatus.PAID)
        val invoice2 = dal.createInvoice(Money(BigDecimal.valueOf(70), Currency.EUR), another_customer!!)

        val onlyPaidInvoices = invoiceService.fetchByStatus(InvoiceStatus.PAID)
        assertTrue(onlyPaidInvoices.contains(invoice1))
        assertEquals(1, onlyPaidInvoices.size)
    }

    @Test
    fun `will update invoice`() {
        val a_customer = dal.createCustomer(Currency.EUR)
        val invoice1 = dal.createInvoice(Money(BigDecimal.valueOf(55), Currency.EUR), a_customer!!, InvoiceStatus.INVALID)

        invoiceService.updateInvoiceStatus(invoice1!!.id, InvoiceStatus.PAID)
        val invoiceAfterUpdate = dal.fetchInvoice(invoice1.id)

        assertEquals(InvoiceStatus.PAID, invoiceAfterUpdate!!.status)
    }
}
