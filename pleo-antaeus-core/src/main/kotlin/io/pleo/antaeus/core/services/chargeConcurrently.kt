
import io.pleo.antaeus.models.Invoice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.time.LocalDateTime

fun CoroutineScope.startInvoiceProcessingPipeline(
        invoices: ReceiveChannel<Invoice>) {

    println("Started process Invoice")
    val invoicesToAgents = Channel<Invoice>(100000)
    val invoicesFromAgents = Channel<String>(100000)

    repeat(500) {
        worker(it+50, invoicesToAgents, invoicesFromAgents)
    }
    invoiceProcessingOrchestrator(invoices, invoicesToAgents, invoicesFromAgents)
}

fun CoroutineScope.invoiceProcessingOrchestrator(
        invoicesFromProductor: ReceiveChannel<Invoice>,
        invoicesToAgents: SendChannel<Invoice>,
        invoicesFromAgents: ReceiveChannel<String> ){

    println("Started Processing Queue")
    launch {
        // mechanism to send an invoice just ONE time
        // can be used with database LOCKING
        val requestedInvoices = mutableSetOf<Invoice>()
        while (true) {
            select<Unit> {
                invoicesFromProductor.onReceive { ref ->
                        println("ORCHESTRATOR: sending to worker invoice ${ref.id}")
                        invoicesToAgents.send(ref)
                    }
                invoicesFromAgents.onReceive { msg ->
                        println(msg)
                    }
            }
        }
    }
}

fun CoroutineScope.worker(
        workerId: Int,
        invoicesIn: ReceiveChannel<Invoice>,
        invoicesOut: SendChannel<String>) =

        launch {
            println("Started worker -> $workerId")
            // Fan-out strategy
            for (invoice in invoicesIn) {
                println("The Worker: [$workerId] starts processing Invoice: {${invoice.id}}")
                delay(500)
                println("Worker: [$workerId] JUST completed invoice ${invoice.id}")
                invoicesOut.send("[$workerId] ---> ${invoice.id}   now(${LocalDateTime.now()})")
            }
}