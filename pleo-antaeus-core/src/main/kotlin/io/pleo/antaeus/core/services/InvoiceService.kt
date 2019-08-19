/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import javax.inject.Inject

class InvoiceService @Inject constructor(private val dal: AntaeusDal) {

    fun fetchAll(): List<Invoice> {
       return dal.fetchInvoices()
    }

    fun fetchByStatus(invoiceStatus: InvoiceStatus): List<Invoice> {
        return dal.fetchInvoices(invoiceStatus)
    }

    fun fetch(id: Int): Invoice {
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }

    fun updateInvoiceStatus(id: Int, status: InvoiceStatus) {
        return dal.updateInvoice(id, status)
    }
}
