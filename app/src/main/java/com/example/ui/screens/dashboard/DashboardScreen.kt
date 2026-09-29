package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Project
import com.example.ui.components.MetricCard
import com.example.ui.components.ProjectStatusBadge
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EnergyGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.ElectricianViewModel

data class QuickCalcItem(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val calcCategory: String
)

@Composable
fun DashboardScreen(
    viewModel: ElectricianViewModel,
    onNavigateToCalculators: (String?) -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToInvoices: () -> Unit,
    onNavigateToMaterials: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onSelectProject: (Long) -> Unit,
    onNewProject: () -> Unit,
    onNewInvoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeProjects by viewModel.activeProjects.collectAsStateWithLifecycle()
    val lowStockMaterials by viewModel.lowStockMaterials.collectAsStateWithLifecycle()
    val financial by viewModel.financialOverview.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val quickCalcs = listOf(
        QuickCalcItem("قانون أوم", Icons.Default.Bolt, AmberGoldPrimary, "ohm"),
        QuickCalcItem("القدرة الكهربائية", Icons.Default.Power, ElectricBlue, "power"),
        QuickCalcItem("هبوط الجهد", Icons.Default.TrendingUp, WarningOrange, "voltage_drop"),
        QuickCalcItem("مقطع الكابل", Icons.Default.Rule, EnergyGreen, "cable_size"),
        QuickCalcItem("سعة القواطع", Icons.Default.Security, Color(0xFF8B5CF6), "breaker"),
        QuickCalcItem("استهلاك الطاقة", Icons.Default.ElectricMeter, Color(0xFFEC4899), "consumption"),
        QuickCalcItem("جدول الأحمال", Icons.Default.NetworkCheck, Color(0xFF14B8A6), "loads"),
        QuickCalcItem("تحويل الوحدات", Icons.Default.SwapHoriz, Color(0xFF64748B), "converter")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Welcome and Business Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "مرحبًا بك ⚡",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = settings.businessName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = settings.electricianName,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { onNavigateToSearch() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "بحث",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick action buttons inside banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onNewProject,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مشروع جديد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onNewInvoice,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("فاتورة جديدة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Metrics Grid (2 columns)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "المشاريع الجارية",
                        value = "${activeProjects.size}",
                        icon = Icons.Default.Folder,
                        iconColor = ElectricBlue,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToProjects
                    )
                    MetricCard(
                        title = "المستحقات عند العملاء",
                        value = "${"%.1f".format(financial.totalOutstandingDue)} ${settings.currency}",
                        icon = Icons.Default.ReceiptLong,
                        iconColor = WarningOrange,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToInvoices
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "نقص في المخزون",
                        value = "${lowStockMaterials.size} أصناف",
                        icon = Icons.Default.Inventory2,
                        iconColor = if (lowStockMaterials.isNotEmpty()) DangerRed else EnergyGreen,
                        subtitle = if (lowStockMaterials.isNotEmpty()) "يتطلب طلب كميات" else "المخزون كافٍ",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToMaterials
                    )
                    MetricCard(
                        title = "صافي أرباح المشاريع",
                        value = "${"%.1f".format(financial.totalEstimatedProfits)} ${settings.currency}",
                        icon = Icons.Default.TrendingUp,
                        iconColor = EnergyGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Low stock warning banner if any
        if (lowStockMaterials.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { onNavigateToMaterials() },
                    colors = CardDefaults.cardColors(
                        containerColor = DangerRed.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = DangerRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تنبيه نقص في المواد (${lowStockMaterials.size} أصناف)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DangerRed
                            )
                            Text(
                                text = lowStockMaterials.take(2).joinToString("، ") { it.name } +
                                    if (lowStockMaterials.size > 2) " وأخرى..." else "",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Electrical Calculators Quick Access
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(
                title = "الحاسبات الكهربائية السريعة",
                actionTitle = "عرض الكل",
                onActionClick = { onNavigateToCalculators(null) }
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(quickCalcs) { calc ->
                    Card(
                        modifier = Modifier
                            .width(115.dp)
                            .clickable { onNavigateToCalculators(calc.calcCategory) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(calc.color.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = calc.icon,
                                    contentDescription = calc.title,
                                    tint = calc.color,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = calc.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Recent Active Projects
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(
                title = "أحدث المشاريع",
                actionTitle = "جميع المشاريع",
                onActionClick = onNavigateToProjects
            )
        }

        if (activeProjects.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "لا توجد مشاريع جارية حالياً. ابدأ بإنشاء أول مشروع!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(activeProjects.take(3)) { project ->
                DashboardProjectItem(
                    project = project,
                    currency = settings.currency,
                    onClick = { onSelectProject(project.id) }
                )
            }
        }
    }
}

@Composable
private fun DashboardProjectItem(
    project: Project,
    currency: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "العميل: ${project.clientName}" + if (project.siteLocation.isNotBlank()) " • ${project.siteLocation}" else "",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "القيمة: ${"%.1f".format(project.agreedAmount)} $currency  |  الربح التقديري: ${"%.1f".format(project.estimatedProfit)} $currency",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EnergyGreen
                )
            }
            ProjectStatusBadge(status = project.status)
        }
    }
}
