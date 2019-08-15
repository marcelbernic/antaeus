package io.pleo.antaeus.core.cron

import io.pleo.antaeus.core.services.TaskExecutor

interface Cron {

    fun registerTaskExecutor(executor: TaskExecutor)
    fun removeTaskExecutor(executor: TaskExecutor)
    fun runTask()
}