package io.pleo.antaeus.app

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

// For instant the Scheduler can run one task at time...
// We can create multiple schedulers for different tasks
// With the observer pattern we can make it to have multiple listeners
class Scheduler @Inject constructor(private val scheduler: it.sauronsoftware.cron4j.Scheduler,
                                    private var schedulingPattern: String) : Stoppable {

    companion object {
        val log: Logger = LoggerFactory.getLogger("Scheduler")
    }

    init {
        // Here we need to validate the pattern
        // if schedulingPattern is not Valid -> throw Exception
        // if (!isValid(pattern) . I don't implement it just to save time
    }

    fun setPattern(pattern: String) {
        // if (isValid(pattern) else -> throw Exception
        schedulingPattern = pattern
    }

    fun start(task: () -> Unit) {
        GlobalScope.launch {
            println("Starting the Scheduler")
            scheduler.schedule(schedulingPattern, task)
            scheduler.start()
            while (true) { ; }
        }
    }

    // TODO: I'm not sure I'll use those functions
    // but the idea is that I want to be able to stop the scheduler
    // without stopping the application... there are multiple things to consider here..
    override fun pause() {
        if (scheduler.isStarted) {
            scheduler.stop()
        }
    }

    override fun resume() {
        if (!scheduler.isStarted) {
            scheduler.start()
        }
    }

    override fun hasStopped(): Boolean {
        return !scheduler.isStarted
    }
}