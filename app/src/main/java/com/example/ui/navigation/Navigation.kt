package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val titleAr: String, val icon: ImageVector? = null) {
    object Dashboard : Screen("dashboard", "الرئيسية", Icons.Default.Dashboard)
    object Calculators : Screen("calculators", "الحاسبات", Icons.Default.Calculate)
    object Projects : Screen("projects", "المشاريع", Icons.Default.Folder)
    object Invoices : Screen("invoices", "الفواتير", Icons.Default.ReceiptLong)
    object Materials : Screen("materials", "المخزون", Icons.Default.Inventory2)

    // Secondary screens
    object Clients : Screen("clients", "العملاء")
    object Financials : Screen("financials", "المالية والأرباح")
    object History : Screen("history", "سجل العمليات")
    object Settings : Screen("settings", "الإعدادات")
    object Search : Screen("search", "البحث الشامل")
}

val BottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Calculators,
    Screen.Projects,
    Screen.Invoices,
    Screen.Materials
)
