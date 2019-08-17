@file:JvmName("AntaeusApp")

package io.pleo.antaeus.app

import dagger.Component
import dagger.Module
import dagger.Provides
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.CustomerTable
import io.pleo.antaeus.data.InvoiceTable
import io.pleo.antaeus.models.Invoice
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
@Component(modules = [AntaeusModule::class])
interface AntaeusContext {
    fun initialize(app: Antaeus)
}

@Module
class AntaeusModule {
    @Singleton
    @Provides
    fun providePaymentProvider(): PaymentProvider {
        return object : PaymentProvider {
            override fun charge(invoice: Invoice): Boolean {
                return Random.nextBoolean()
            }
        }
    }

    @Singleton
    @Provides
    fun provideDatabase(): Database {
        val tables = arrayOf(InvoiceTable, CustomerTable)

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

    @Singleton
    @Provides
    fun provideScheduler(): Scheduler {
        return Scheduler(it.sauronsoftware.cron4j.Scheduler(), "* * * * *")
    }
}

fun main() {
    Antaeus()
}