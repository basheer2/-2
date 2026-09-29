package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class InvoiceType(val titleAr: String) {
    INVOICE("فاتورة"),
    QUOTATION("عرض سعر")
}

enum class InvoiceStatus(val titleAr: String) {
    UNPAID("غير مدفوعة"),
    PARTIAL("مدفوعة جزئياً"),
    PAID("مدفوعة بالكامل"),
    DRAFT("مسودة")
}

data class InvoiceItem(
    val description: String,
    val quantity: Double,
    val unitPrice: Double,
    val discount: Double = 0.0
) {
    val total: Double
        get() = (quantity * unitPrice) - discount
}

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val type: InvoiceType = InvoiceType.INVOICE,
    val clientId: Long? = null,
    val clientName: String,
    val clientPhone: String = "",
    val projectId: Long? = null,
    val projectName: String = "",
    val issueDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + (7L * 24 * 3600 * 1000), // Default 7 days
    val items: List<InvoiceItem> = emptyList(),
    val overallDiscount: Double = 0.0,
    val isTaxEnabled: Boolean = false,
    val taxRate: Double = 15.0, // Percentage
    val paidAmount: Double = 0.0,
    val status: InvoiceStatus = InvoiceStatus.UNPAID,
    val notes: String = "",
    val terms: String = "الضمان يشمل التمديدات والأعمال المنفذة وفق الكود المعتمد.",
    val createdAt: Long = System.currentTimeMillis()
) {
    val subtotal: Double
        get() = items.sumOf { it.total }

    val discountCalculated: Double
        get() = overallDiscount

    val taxableAmount: Double
        get() = maxOf(0.0, subtotal - discountCalculated)

    val taxAmount: Double
        get() = if (isTaxEnabled) taxableAmount * (taxRate / 100.0) else 0.0

    val grandTotal: Double
        get() = taxableAmount + taxAmount

    val remainingDue: Double
        get() = maxOf(0.0, grandTotal - paidAmount)
}
