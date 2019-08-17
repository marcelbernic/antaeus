
import io.pleo.antaeus.core.services.BillingService.Companion.log
import io.pleo.antaeus.core.services.Command
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
        invoices: ReceiveChannel<Command>) {

    log.info("Started Invoice Processing Pipeline")
    val invoiceToAgentsChannel = Channel<Command>(100000)
    val invoiceFromAgentsChannel = Channel<String>(100000)

    repeat(2) {
        agent(it+1, invoiceToAgentsChannel, invoiceFromAgentsChannel)
    }
    invoiceProcessingOrchestrator(invoices, invoiceToAgentsChannel, invoiceFromAgentsChannel)
}

fun CoroutineScope.invoiceProcessingOrchestrator(
        invoiceFromProductorChannel: ReceiveChannel<Command>,
        invoiceToAgentsChannel: SendChannel<Command>,
        invoiceFromAgentsChannel: ReceiveChannel<String> ){

    log.info("Started Processing Queue")
    launch {
        // mechanism to send an invoice just ONE time
        // can be used with database LOCKING
        val requestedInvoices = mutableSetOf<Invoice>()
        while (true) {
            select<Unit> {
                invoiceFromProductorChannel.onReceive { ref ->
                        log.info("${ref} >>> Agent")
                        invoiceToAgentsChannel.send(ref)
                    }
                invoiceFromAgentsChannel.onReceive { msg ->
                        //React in function of result (success, timeout, error)
                    }
            }
        }
    }
}

fun CoroutineScope.agent(
        agentId: Int,
        invoicesInChannel: ReceiveChannel<Command>,
        invoicesOutChannel: SendChannel<String>) =

        launch {
            log.info("Spawn Agent($agentId)")
            // Fan-out strategy
            for (command in invoicesInChannel) {
                log.info("Agent($agentId) starts processing ${command}")
                command.execute()
                delay(500)
                log.info("Agent($agentId) >>> ${command}[${LocalDateTime.now()}]")
                invoicesOutChannel.send("Agent($agentId) >>> ${command}[${LocalDateTime.now()}]")
            }
}