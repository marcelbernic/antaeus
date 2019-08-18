/*
    Implements endpoints related to events.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Event
import io.pleo.antaeus.models.EventType
import io.pleo.antaeus.models.Invoice
import javax.inject.Inject

class EventService @Inject constructor(private val dal: AntaeusDal) {
    fun fetchAll(): List<Event> {
       return dal.fetchEvents()
    }

    fun fetchByClient(clientId: Int): List<Event> {
        return dal.fetchEvents(clientId)
    }

    fun createEvent(invoice: Invoice, type: EventType, msg: String): Event? {
        return dal.createEvent(invoice.customerId, invoice.id, type, msg)
    }
}
