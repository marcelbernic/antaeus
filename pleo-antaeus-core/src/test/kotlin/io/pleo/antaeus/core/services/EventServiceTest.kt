package io.pleo.antaeus.core.services

import dagger.Component
import io.pleo.antaeus.core.TestAntaeusModule
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.EventType
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Component(modules = [TestAntaeusModule::class])
interface EventServiceTestContext {
    fun initialize(app: EventServiceTest)
}

class EventServiceTest {
    @Inject
    lateinit var eventService: EventService

    @Inject
    lateinit var dal: AntaeusDal

    @BeforeEach
    fun setup() {
        DaggerEventServiceTestContext.create().initialize(this)
    }

    @Test
    fun `will store event on createEvent`() {
        assertTrue(eventService.fetchAll().isEmpty())

        val a_customer = dal.createCustomer(Currency.EUR)
        val invoice1 = dal.createInvoice(Money(BigDecimal.valueOf(55), Currency.EUR), a_customer!!)
        val event1 = eventService.createEvent(invoice1!!, EventType.PAYMENT_START, "Test")

        assertEquals(event1, eventService.fetchAll()[0])
    }

    @Test
    fun `will return all events when fetch all`() {
        assertTrue(eventService.fetchAll().isEmpty())

        val a_customer = dal.createCustomer(Currency.EUR)
        val another_customer = dal.createCustomer(Currency.EUR)
        val invoice1 = dal.createInvoice(Money(BigDecimal.valueOf(55), Currency.EUR), a_customer!!)
        val invoice2 = dal.createInvoice(Money(BigDecimal.valueOf(70), Currency.EUR), another_customer!!)

        val event1 = eventService.createEvent(invoice1!!, EventType.PAYMENT_START, "Test")
        val event2 = eventService.createEvent(invoice2!!, EventType.PAYMENT_RETRY, "Test2")

        val allEvents = eventService.fetchAll()
        assertTrue(allEvents.contains(event1))
        assertTrue(allEvents.contains(event2))
        assertEquals(2, allEvents.size)
    }

    @Test
    fun `will fetch only events for customer`() {
        assertTrue(eventService.fetchAll().isEmpty())

        val a_customer = dal.createCustomer(Currency.EUR)
        val another_customer = dal.createCustomer(Currency.EUR)
        val invoice1 = dal.createInvoice(Money(BigDecimal.valueOf(55), Currency.EUR), a_customer!!)
        val invoice2 = dal.createInvoice(Money(BigDecimal.valueOf(70), Currency.EUR), another_customer!!)

        val event1 = eventService.createEvent(invoice1!!, EventType.PAYMENT_START, "Test")
        val event2 = eventService.createEvent(invoice2!!, EventType.PAYMENT_RETRY, "Test2")

        val eventsRelatedTo_a_customer = eventService.fetchByClient(a_customer.id)
        assertTrue(eventsRelatedTo_a_customer.contains(event1))
        assertEquals(1, eventsRelatedTo_a_customer.size)
    }
}