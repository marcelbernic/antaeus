/*
    Implements endpoints related to events.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Event
import javax.inject.Inject

class EventService @Inject constructor(private val dal: AntaeusDal) {
    fun fetchAll(): List<Event> {
       return dal.fetchEvents()
    }
}
