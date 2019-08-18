package io.pleo.antaeus.core.scheduler

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

// For instant the Scheduler can run one task at time...
// We can create multiple schedulers for different tasks
// With the observer pattern we can make it to have multiple listeners
class Scheduler @Inject constructor(private val scheduler: it.sauronsoftware.cron4j.Scheduler,
                                    private var schedulingPattern: String) : EventNotifier,Stoppable {

    companion object {
        val log: Logger = LoggerFactory.getLogger("Scheduler")
    }

    private var observers = mutableListOf<EventObserver>()

    init {
        // Here we need to validate the pattern if schedulingPattern is not Valid -> throw Exception
        // I don't implement it just to save time
    }

    fun start() {
        log.info("Starting cron job ($schedulingPattern)")
        GlobalScope.launch {
            println("Starting the Scheduler")
            scheduler.schedule(schedulingPattern, notifyObservers())
            scheduler.start()
            while (true) { ; }
        }
    }

    override fun register(observer: EventObserver) {
        log.info("Adding eventObserver to the schedueler")
        observers.add(observer)
    }

    override fun unregister(observer: EventObserver) {
        log.info("Removing eventObserver from the scheduler")
        if (observers.contains(observer)) {
            observers.remove(observer)
        }
    }

    override fun notifyObservers(): () -> Unit {
        val notifyAllObservers: () -> Unit = {
            for (observer in observers) {
                observer.runTask()
            }
        }
        return notifyAllObservers
    }

    // TODO: I'm not sure I'll use these functions for the scope of the exercice
    // but the idea is that I want to be able to stop the scheduler (change pattern)
    // without stopping the application... there are multiple things to consider here..
    fun setPattern(pattern: String) {
        // if (isValid(pattern)) else -> throw Exception
        schedulingPattern = pattern
    }

    override fun pause() {
        if (scheduler.isStarted) {
            scheduler.stop()
        }
    }

    override fun resume() {
        if (hasStopped()) {
            scheduler.start()
        }
    }

    override fun hasStopped(): Boolean {
        return !scheduler.isStarted
    }
}