package io.pleo.antaeus.core.scheduler

interface Stoppable {
    fun pause()
    fun resume()
    fun hasStopped(): Boolean
}