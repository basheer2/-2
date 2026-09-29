package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProjectStatus(val titleAr: String) {
    IN_PROGRESS("قيد التنفيذ"),
    PENDING("معلّق"),
    COMPLETED("مكتمل"),
    CANCELED("ملغي")
}

data class ProjectMaterialItem(
    val materialId: Long? = null,
    val name: String,
    val quantity: Double,
    val unit: String,
    val unitCost: Double,
    val unitSellingPrice: Double
) {
    val totalCost: Double get() = quantity * unitCost
    val totalBilled: Double get() = quantity * unitSellingPrice
}

data class ProjectLaborItem(
    val description: String,
    val workerName: String = "",
    val daysOrHours: Double = 1.0,
    val rate: Double = 0.0,
    val totalCost: Double = daysOrHours * rate
)

data class ProjectExpenseItem(
    val description: String,
    val amount: Double,
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val clientName: String,
    val clientPhone: String = "",
    val siteLocation: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val dueDate: Long = 0L,
    val status: ProjectStatus = ProjectStatus.IN_PROGRESS,
    val notes: String = "",
    val agreedAmount: Double = 0.0, // المبلغ المتفق عليه مع العميل
    val materials: List<ProjectMaterialItem> = emptyList(),
    val labor: List<ProjectLaborItem> = emptyList(),
    val expenses: List<ProjectExpenseItem> = emptyList(),
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalMaterialsCost: Double
        get() = materials.sumOf { it.totalCost }

    val totalLaborCost: Double
        get() = labor.sumOf { it.totalCost }

    val totalExpensesCost: Double
        get() = expenses.sumOf { it.amount }

    val totalProjectCost: Double
        get() = totalMaterialsCost + totalLaborCost + totalExpensesCost

    val estimatedProfit: Double
        get() = agreedAmount - totalProjectCost

    val profitMarginPercent: Double
        get() = if (agreedAmount > 0) (estimatedProfit / agreedAmount) * 100.0 else 0.0
}
