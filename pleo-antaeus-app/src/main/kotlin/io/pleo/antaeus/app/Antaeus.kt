package io.pleo.antaeus.app

import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.rest.AntaeusRest
import javax.inject.Inject

class Antaeus {
    @Inject
    lateinit var dateInitializer: DataInitializer
    @Inject
    lateinit var billingService: BillingService
    @Inject
    lateinit var scheduler: Scheduler
    @Inject
    lateinit var rest: AntaeusRest

    init {
        DaggerAntaeusContext.create().initialize(this)
        dateInitializer.setupInitialData()
        scheduler.start(billingService.runTask())
        rest.run()
    }
}