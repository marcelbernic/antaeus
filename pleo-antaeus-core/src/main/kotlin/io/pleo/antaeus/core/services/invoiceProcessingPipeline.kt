
import io.pleo.antaeus.core.services.BillingService.Companion.log
import io.pleo.antaeus.core.services.Command
import io.pleo.antaeus.core.services.CommandResult
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
        invoices: ReceiveChannel<Command>,
        results: SendChannel<CommandResult>) {

    log.info("Started Invoice Processing Pipeline")
    val invoiceToAgentsChannel = Channel<Command>(100000)
    val invoiceFromAgentsChannel = Channel<CommandResult>(100000)

    repeat(2) {
        agent(it+1, invoiceToAgentsChannel, invoiceFromAgentsChannel)
    }
    invoiceProcessingOrchestrator(results, invoices, invoiceToAgentsChannel, invoiceFromAgentsChannel)
}

fun CoroutineScope.invoiceProcessingOrchestrator(
        invoiceBackToService: SendChannel<CommandResult>,
        invoiceFromProductorChannel: ReceiveChannel<Command>,
        invoiceToAgentsChannel: SendChannel<Command>,
        invoiceFromAgentsChannel: ReceiveChannel<CommandResult> ){

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
                invoiceFromAgentsChannel.onReceive { result ->
                        //React in function of result (success, timeout, error)
                        invoiceBackToService.send(result)
                    }
            }
        }
    }
}

fun CoroutineScope.agent(
        agentId: Int,
        invoicesInChannel: ReceiveChannel<Command>,
        invoicesOutChannel: SendChannel<CommandResult>) =

        launch {
            log.info("Spawn Agent($agentId)")
            // Fan-out strategy
            for (command in invoicesInChannel) {
                log.info("Agent($agentId) starts processing ${command}")
                val commandResult = command.execute()
                delay(500)
                log.info("Agent($agentId) >>> ${command}[${LocalDateTime.now()}]")
                invoicesOutChannel.send(commandResult)
            }
}