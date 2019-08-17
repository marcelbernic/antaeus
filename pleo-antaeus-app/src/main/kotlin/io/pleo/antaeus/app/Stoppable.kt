package io.pleo.antaeus.app

interface Stoppable {
    fun pause()
    fun resume()
    fun hasStopped(): Boolean

}