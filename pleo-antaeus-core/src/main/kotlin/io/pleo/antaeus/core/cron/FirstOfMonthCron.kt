package io.pleo.antaeus.core.cron

import io.pleo.antaeus.core.services.TaskExecutor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import javax.inject.Inject


class FirstOfMonthCron @Inject constructor() : Cron {
    companion object {
        val log: Logger = LoggerFactory.getLogger("BillingScheduler")
    }

    init {
        log.info(" BillingScheduler was initialized")

        GlobalScope.launch {
            while (true) {
                if (getCurrentDay() == 1) {
                    FirstOfMonthCron.log.info("Notify all services")
                    runTask()
                }
                delay(ONE_DAY)
            }
        }
    }

    private val observers: MutableList<TaskExecutor> = mutableListOf()
    private val ONE_DAY: Long = 1000*60*60*24

    override fun registerTaskExecutor(executor: TaskExecutor) {
        observers.add(executor)

        log.info("A new observer was added to this Scheduler")
    }

    override fun removeTaskExecutor(executor: TaskExecutor) {
        observers.remove(executor)

        log.info("An observer was removed from this Scheduler")
    }

    override fun runTask() {
        for (observer in observers) {
            observer.execute()
        }
    }

    private fun getCurrentDay(): Int {
        return LocalDateTime.now().dayOfMonth
    }

}