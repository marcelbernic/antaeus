package io.pleo.antaeus.core.scheduler

interface EventNotifier {
    fun register(observer: EventObserver)

    fun unregister(observer: EventObserver)

    fun notifyObservers(): () -> Unit
}