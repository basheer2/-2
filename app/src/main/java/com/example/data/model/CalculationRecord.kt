package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String, // e.g. "حساب قانون أوم", "هبوط الجهد", "مقطع الكابل"
    val category: String, // "ohm", "power", "voltage_drop", "cable_size", "breaker", "consumption", "loads", "converter"
    val inputSummary: String, // e.g. "الجهد: 220V | التيار: 15A"
    val resultSummary: String, // e.g. "القدرة: 3300 W (3.3 kW) | المقاومة: 14.67 Ω"
    val detailedFormula: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
