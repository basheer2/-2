package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materials")
data class MaterialItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // كابلات وأسلاك, قواطع ولوحات, مفاتيح وأفياش, إضاءة, مواسير وتمديدات, أخرى
    val unit: String, // متر, حبة, لفة, طقم, علبة
    val purchasePrice: Double,
    val sellingPrice: Double,
    val quantity: Double,
    val minStockAlert: Double = 5.0,
    val supplier: String = "",
    val notes: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = quantity <= minStockAlert

    val totalPurchaseValue: Double
        get() = purchasePrice * quantity

    val totalSellingValue: Double
        get() = sellingPrice * quantity
}
