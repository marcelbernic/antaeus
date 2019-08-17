package io.pleo.antaeus.core.services

interface EventNotifier {
    fun register(observer: EventObserver)

    fun unregister(observer: EventObserver)

    fun notifyObservers()
}