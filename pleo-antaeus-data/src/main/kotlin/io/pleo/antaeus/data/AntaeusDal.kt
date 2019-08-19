/*
    Implements the data access layer (DAL).
    This file implements the database queries used to fetch and insert rows in our database tables.

    See the `mappings` module for the conversions between database rows and Kotlin objects.
 */

package io.pleo.antaeus.data

import io.pleo.antaeus.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AntaeusDal @Inject constructor(private val db: Database) {
    fun fetchInvoice(id: Int): Invoice? {
        // transaction(db) runs the internal query as a new database transaction.
        return transaction(db) {
            // Returns the first invoice with matching id.
            InvoiceTable
                .select { InvoiceTable.id.eq(id) }
                .firstOrNull()
                ?.toInvoice()
        }
    }

    fun updateInvoice(id: Int, status: InvoiceStatus) {
        transaction(db) {
            InvoiceTable
                    .update({ InvoiceTable.id eq id }) {
                        it[this.status] = status.toString()
                    }
        }
    }

    fun fetchInvoices(): List<Invoice> {
        return transaction(db) {
            InvoiceTable
                .selectAll()
                .map { it.toInvoice() }
        }
    }

    fun fetchInvoices(invoiceStatus: InvoiceStatus): List<Invoice> {
        return transaction(db) {
            InvoiceTable
                    .selectAll()
                    .map { it.toInvoice() }
                    .filter { invoice -> invoice.status == invoiceStatus }
        }
    }

    fun createInvoice(amount: Money, customer: Customer, status: InvoiceStatus = InvoiceStatus.PENDING): Invoice? {
        val id = transaction(db) {
            // Insert the invoice and returns its new id.
            InvoiceTable
                .insert {
                    it[this.value] = amount.value
                    it[this.currency] = amount.currency.toString()
                    it[this.status] = status.toString()
                    it[this.customerId] = customer.id
                } get InvoiceTable.id
        }

        return fetchInvoice(id!!)
    }

    fun fetchCustomer(id: Int): Customer? {
        return transaction(db) {
            CustomerTable
                .select { CustomerTable.id.eq(id) }
                .firstOrNull()
                ?.toCustomer()
        }
    }

    fun fetchCustomers(): List<Customer> {
        return transaction(db) {
            CustomerTable
                .selectAll()
                .map { it.toCustomer() }
        }
    }

    fun createCustomer(currency: Currency): Customer? {
        val id = transaction(db) {
            // Insert the customer and return its new id.
            CustomerTable.insert {
                it[this.currency] = currency.toString()
            } get CustomerTable.id
        }

        return fetchCustomer(id!!)
    }

    fun fetchEvents(): List<Event> {
        return transaction(db) {
            EventTable
                .selectAll()
                .map { it.toEvent() }
        }
    }

    fun fetchEvents(customerId: Int): List<Event> {
        return transaction(db) {
            EventTable
                    .selectAll()
                    .map { it.toEvent() }
                    .filter {event -> event.customerId == customerId}
        }
    }

    fun createEvent(customerId: Int, invoiceId: Int, type: EventType, msg: String): Event? {
        val id = transaction(db) {
            // Insert the event and returns its new id.
            EventTable
                    .insert {
                        it[this.customerId] = customerId
                        it[this.invoiceId] = invoiceId
                        it[this.date] = DateTime.now()
                        it[this.type] = type.toString()
                        it[this.message] = msg
                    } get EventTable.id
        }


        return fetchEvent(id!!)
    }

    private fun fetchEvent(id: Int): Event? {
        return transaction(db) {
            EventTable
                    .select { EventTable.id.eq(id) }
                    .firstOrNull()
                    ?.toEvent()
        }
    }
}
