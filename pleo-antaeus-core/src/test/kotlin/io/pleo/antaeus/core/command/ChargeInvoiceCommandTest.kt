package io.pleo.antaeus.core.command

import dagger.Component
import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.TestAntaeusModule
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.exceptions.NotEnoughFoundsException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.inject.Singleton

@Singleton
@Component(modules = [TestAntaeusModule::class])
interface ChargeInvoiceCommandTestContext {
    fun initialize(app: ChargeInvoiceCommandTest)
}

class ChargeInvoiceCommandTest {

    var paymentProvider = mockk<PaymentProvider>()
    var invoice = mockk<Invoice>()

    var chargeInvoiceCommand = ChargeInvoiceCommand(paymentProvider, invoice)


    @BeforeEach
    fun setup() {
        DaggerChargeInvoiceCommandTestContext.create().initialize(this)
    }

    @Test
    fun `will return ChargeInvoiceCommandResult (SUCCESS)`() {
        every { paymentProvider.charge(any()) } returns true
        val commandResult = chargeInvoiceCommand.execute()

        assertEquals(CommandStatus.SUCCESS, commandResult.status())
    }

    @Test
    fun `will return ChargeInvoiceCommandResult (NETWORK_ERROR)`() {
        every { paymentProvider.charge(any()) } throws mockk<NetworkException>()
        val commandResult = chargeInvoiceCommand.execute()

        assertEquals(CommandStatus.NETWORK_ERROR, commandResult.status())
    }

    @Test
    fun `will return ChargeInvoiceCommandResult (NOT_ENOUGH_FOUNDS)`() {
        every { paymentProvider.charge(any()) } throws mockk<NotEnoughFoundsException>()
        val commandResult = chargeInvoiceCommand.execute()

        assertEquals(CommandStatus.NOT_ENOUGH_FOUNDS, commandResult.status())
    }

    @Test
    fun `will return ChargeInvoiceCommandResult (UNKNOWN_ERROR)`() {
        every { paymentProvider.charge(any()) } throws mockk<Exception>()
        val commandResult = chargeInvoiceCommand.execute()

        assertEquals(CommandStatus.UNKNOWN_ERROR, commandResult.status())
    }

}