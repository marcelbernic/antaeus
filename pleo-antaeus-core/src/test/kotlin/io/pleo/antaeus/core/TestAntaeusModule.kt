package io.pleo.antaeus.core

import dagger.Module
import dagger.Provides
import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.CustomerTable
import io.pleo.antaeus.data.EventTable
import io.pleo.antaeus.data.InvoiceTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import javax.inject.Singleton

@Module
class TestAntaeusModule {
    @Singleton
    @Provides
    fun providePaymentProvider(): PaymentProvider {
        return mockk<PaymentProvider>() {
            every { charge(any()) } returns true
        }
    }

    @Singleton
    @Provides
    fun provideDatabase(): Database {
        val tables = arrayOf(InvoiceTable, CustomerTable, EventTable)

        return Database
                .connect("jdbc:sqlite:/tmp/data.db", "org.sqlite.JDBC")
                .also {
                    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
                    transaction(it) {
                        addLogger(StdOutSqlLogger)
                        SchemaUtils.drop(*tables)
                        SchemaUtils.create(*tables)
                    }
                }
    }
}