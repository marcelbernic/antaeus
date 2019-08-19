package io.pleo.antaeus.core.services

import dagger.Component
import io.pleo.antaeus.core.TestAntaeusModule
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Component(modules = [TestAntaeusModule::class])
interface CustomerServiceTestContext {
    fun initialize(app: CustomerServiceTest)
}

class CustomerServiceTest {
    @Inject
    lateinit var customerService: CustomerService

    @Inject
    lateinit var dal: AntaeusDal

    @BeforeEach
    fun setup() {
        DaggerCustomerServiceTestContext.create().initialize(this)
    }

    @Test
    fun `will throw CustomerNotFoundException if customer is not found`() {
        assertThrows<CustomerNotFoundException> {
            customerService.fetch(1236543456)
        }
    }

    @Test
    fun `will find customer when fetch and it is database`() {
        val customer = dal.createCustomer(Currency.EUR)

        assertEquals(customer, customerService.fetch(customer?.id!!))
    }

    @Test
    fun `will return all customers when fetch all`() {
        assertTrue(customerService.fetchAll().isEmpty())

        val a_customer = dal.createCustomer(Currency.EUR)
        val another_customer = dal.createCustomer(Currency.EUR)

        val allCustomers = customerService.fetchAll()
        assertTrue(allCustomers.contains(a_customer))
        assertTrue(allCustomers.contains(another_customer))
        assertEquals(2, allCustomers.size)
    }
}