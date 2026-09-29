package com.example.ui.screens.projects

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Project
import com.example.data.model.ProjectStatus
import com.example.ui.components.ProjectStatusBadge
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EnergyGreen
import com.example.ui.viewmodel.ElectricianViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: Long,
    viewModel: ElectricianViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val project = allProjects.firstOrNull { it.id == projectId }

    val context = LocalContext.current
    var showEditSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (project == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("المشروع غير موجود أو تم حذفه")
        }
        return
    }

    Scaffold(
        modifier = modifier.testTag("project_detail_screen"),
        topBar = {
            TopAppBar(
                title = { Text(project.name, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        shareProjectSummary(context, project, settings.currency)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة")
                    }
                    IconButton(onClick = { showEditSheet = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = DangerRed)
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
            // Client & Site info card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(project.clientName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            ProjectStatusBadge(status = project.status)
                        }

                        if (project.clientPhone.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(project.clientPhone, fontSize = 13.sp)
                                }
                                TextButton(onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, "tel:${project.clientPhone}".toUri())
                                    context.startActivity(dialIntent)
                                }) {
                                    Text("اتصال بالعميل")
                                }
                            }
                        }

                        if (project.siteLocation.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(project.siteLocation, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Financial Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("البيان المالي للمشروع", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("قيمة العقد المتفق عليها:", fontSize = 13.sp)
                            Text("${"%.1f".format(project.agreedAmount)} ${settings.currency}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("تكلفة المواد والتوريدات:", fontSize = 13.sp)
                            Text("${"%.1f".format(project.totalMaterialsCost)} ${settings.currency}", fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("أجور العمالة والأعمال:", fontSize = 13.sp)
                            Text("${"%.1f".format(project.totalLaborCost)} ${settings.currency}", fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("المصاريف الأخرى:", fontSize = 13.sp)
                            Text("${"%.1f".format(project.totalExpensesCost)} ${settings.currency}", fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إجمالي التكاليف:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${"%.1f".format(project.totalProjectCost)} ${settings.currency}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("صافي الربح التقديري:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EnergyGreen)
                            Text(
                                "${"%.1f".format(project.estimatedProfit)} ${settings.currency} (${"%.1f".format(project.profitMarginPercent)}%)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = EnergyGreen
                            )
                        }
                    }
                }
            }

            // Materials Items List
            item {
                Text("المواد والتجهيزات المستخدمة (${project.materials.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            if (project.materials.isEmpty()) {
                item {
                    Text("لم يتم تسجيل مواد لهذا المشروع بعد.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(project.materials) { mat ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(mat.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("${mat.quantity} ${mat.unit} × ${mat.unitCost} ${settings.currency}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("${mat.totalCost} ${settings.currency}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Labor Items List
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("أعمال وعمالة المشروع (${project.labor.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            if (project.labor.isEmpty()) {
                item {
                    Text("لم يتم تسجيل عمالة لهذا المشروع بعد.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(project.labor) { lab ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(lab.description, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("${lab.workerName} • ${lab.daysOrHours} يوم/ساعة × ${lab.rate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("${lab.totalCost} ${settings.currency}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            if (project.notes.isNotBlank()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("ملاحظات:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(project.notes, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showEditSheet) {
        ProjectEditorSheet(
            project = project,
            currency = settings.currency,
            onDismiss = { showEditSheet = false },
            onSave = { updated ->
                viewModel.saveProject(updated)
                showEditSheet = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف المشروع") },
            text = { Text("هل أنت متأكد من حذف هذا المشروع نهائياً؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProject(project.id)
                        showDeleteConfirm = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("إلغاء") }
            }
        )
    }
}

private fun shareProjectSummary(context: Context, project: Project, currency: String) {
    val text = buildString {
        appendLine("⚡ تقرير مشروع: ${project.name}")
        appendLine("👤 العميل: ${project.clientName} (${project.clientPhone})")
        if (project.siteLocation.isNotBlank()) appendLine("📍 الموقع: ${project.siteLocation}")
        appendLine("📌 الحالة: ${project.status.titleAr}")
        appendLine("-------------------------")
        appendLine("💰 قيمة العقد: ${project.agreedAmount} $currency")
        appendLine("📦 تكلفة المواد: ${project.totalMaterialsCost} $currency")
        appendLine("👷 أجور العمالة: ${project.totalLaborCost} $currency")
        appendLine("💼 إجمالي التكلفة: ${project.totalProjectCost} $currency")
        appendLine("📈 صافي الأرباح: ${project.estimatedProfit} $currency (${"%.1f".format(project.profitMarginPercent)}%)")
        appendLine("-------------------------")
        appendLine("تم الاستخراج عبر تطبيق محاسب الكهربائي")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "تقرير مشروع ${project.name}")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة تقرير المشروع"))
}
