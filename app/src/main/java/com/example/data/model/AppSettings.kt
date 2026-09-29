package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val businessName: String = "مؤسسة الأعمال الكهربائية",
    val electricianName: String = "فني كهرباء معتمد",
    val phone: String = "",
    val currency: String = "ر.س", // SAR, EGP, AED, KWD, $, etc.
    val defaultTaxEnabled: Boolean = false,
    val defaultTaxRate: Double = 15.0,
    val notificationsEnabled: Boolean = true,
    val lowStockThresholdDefault: Double = 5.0,
    val darkModePreference: String = "system" // "system", "dark", "light"
)
