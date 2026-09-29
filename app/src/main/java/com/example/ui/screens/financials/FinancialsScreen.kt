package com.example.ui.screens.financials

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EnergyGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.ElectricianViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialsScreen(
    viewModel: ElectricianViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val financial by viewModel.financialOverview.collectAsStateWithLifecycle()
    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    val allMaterials by viewModel.allMaterials.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val totalMaterialCostsInProjects = allProjects.sumOf { it.totalMaterialsCost }
    val totalLaborCostsInProjects = allProjects.sumOf { it.totalLaborCost }
    val totalExpensesInProjects = allProjects.sumOf { it.totalExpensesCost }
    val totalAgreedInProjects = allProjects.sumOf { it.agreedAmount }
    val netProfit = totalAgreedInProjects - (totalMaterialCostsInProjects + totalLaborCostsInProjects + totalExpensesInProjects)

    Scaffold(
        modifier = modifier.testTag("financials_screen"),
        topBar = {
            TopAppBar(
                title = { Text("التقارير المالية والأرباح") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Main Profit Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (netProfit >= 0) MaterialTheme.colorScheme.primaryContainer else DangerRed.copy(alpha = 0.15f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (netProfit >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (netProfit >= 0) EnergyGreen else DangerRed,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "صافي أرباح المشاريع الكلية",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${"%.1f".format(netProfit)} ${settings.currency}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (netProfit >= 0) EnergyGreen else DangerRed
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        val profitPercent = if (totalAgreedInProjects > 0) (netProfit / totalAgreedInProjects) * 100.0 else 0.0
                        Text(
                            text = "هامش الربح الإجمالي: ${"%.1f".format(profitPercent)} % من إجمالي العقود",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Cashflow Cards
            item {
                Text("المقبوضات والمستحقات:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FinancialMetricCard(
                        title = "المحصّل في الخزينة",
                        value = "${"%.1f".format(financial.totalRevenueCollected)} ${settings.currency}",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconColor = EnergyGreen,
                        modifier = Modifier.weight(1f)
                    )
                    FinancialMetricCard(
                        title = "المستحقات المعلقة",
                        value = "${"%.1f".format(financial.totalOutstandingDue)} ${settings.currency}",
                        icon = Icons.Default.ReceiptLong,
                        iconColor = WarningOrange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Costs Breakdown
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text("تفاصيل التكاليف والمصروفات:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CostProgressBarItem(
                            label = "تكاليف المواد المستخدمة",
                            amount = totalMaterialCostsInProjects,
                            total = financial.totalProjectCosts,
                            currency = settings.currency,
                            color = ElectricBlue
                        )
                        CostProgressBarItem(
                            label = "أجور العمالة والفنيين",
                            amount = totalLaborCostsInProjects,
                            total = financial.totalProjectCosts,
                            currency = settings.currency,
                            color = AmberGoldPrimary
                        )
                        CostProgressBarItem(
                            label = "المصاريف التشغيلية والإضافية",
                            amount = totalExpensesInProjects,
                            total = financial.totalProjectCosts,
                            currency = settings.currency,
                            color = WarningOrange
                        )

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إجمالي تكاليف المشاريع المنفذة:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${"%.1f".format(financial.totalProjectCosts)} ${settings.currency}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Assets and Stock Value
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("القيمة الإجمالية للمخزون الحالي", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${"%.1f".format(financial.totalInventoryValue)} ${settings.currency}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("رأس مال مجمد في البضائع والمستودع", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun CostProgressBarItem(
    label: String,
    amount: Double,
    total: Double,
    currency: String,
    color: Color
) {
    val progress = if (total > 0) (amount / total).toFloat().coerceIn(0f, 1f) else 0f
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("${"%.1f".format(amount)} $currency (${(progress * 100).toInt()}%)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
