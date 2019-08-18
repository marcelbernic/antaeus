package io.pleo.antaeus.models

import java.util.*

data class Event(
        val id: Int,
        val date: Date,
        val customerId: Int,
        val type: EventType,
        val message: String
)
