package com.example.ui.screens.projects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Project
import com.example.data.model.ProjectExpenseItem
import com.example.data.model.ProjectLaborItem
import com.example.data.model.ProjectMaterialItem
import com.example.data.model.ProjectStatus
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ProjectStatusBadge
import com.example.ui.components.UnitNumberField
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EnergyGreen
import com.example.ui.viewmodel.ElectricianViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    viewModel: ElectricianViewModel,
    onProjectClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("all") } // "all", "in_progress", "completed", "pending", "archived"
    var showProjectDialog by remember { mutableStateOf(false) }
    var editingProject by remember { mutableStateOf<Project?>(null) }
    var projectToDelete by remember { mutableStateOf<Project?>(null) }

    val filteredProjects = allProjects.filter { project ->
        when (selectedFilter) {
            "all" -> !project.isArchived
            "in_progress" -> !project.isArchived && project.status == ProjectStatus.IN_PROGRESS
            "completed" -> !project.isArchived && project.status == ProjectStatus.COMPLETED
            "pending" -> !project.isArchived && project.status == ProjectStatus.PENDING
            "archived" -> project.isArchived
            else -> true
        }
    }

    Scaffold(
        modifier = modifier.testTag("projects_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingProject = null
                    showProjectDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("add_project_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "مشروع جديد")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    Pair("all", "النشطة (${allProjects.count { !it.isArchived }})"),
                    Pair("in_progress", "قيد التنفيذ (${allProjects.count { !it.isArchived && it.status == ProjectStatus.IN_PROGRESS }})"),
                    Pair("completed", "المكتملة (${allProjects.count { !it.isArchived && it.status == ProjectStatus.COMPLETED }})"),
                    Pair("pending", "المعلقة (${allProjects.count { !it.isArchived && it.status == ProjectStatus.PENDING }})"),
                    Pair("archived", "المؤرشفة (${allProjects.count { it.isArchived }})")
                )
                items(filters) { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            if (filteredProjects.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Folder,
                    title = "لا توجد مشاريع في هذا القسم",
                    message = "يمكنك إضافة مشروع كهربائي جديد، ومتابعة تكاليف المواد وأجور العمالة والأرباح بكل سهولة.",
                    buttonTitle = "إضافة مشروع جديد",
                    onButtonClick = {
                        editingProject = null
                        showProjectDialog = true
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProjects) { project ->
                        ProjectItemCard(
                            project = project,
                            currency = settings.currency,
                            onClick = { onProjectClick(project.id) },
                            onEdit = {
                                editingProject = project
                                showProjectDialog = true
                            },
                            onDelete = { projectToDelete = project },
                            onArchive = { viewModel.toggleProjectArchive(project) }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Project Dialog
    if (showProjectDialog) {
        ProjectEditorSheet(
            project = editingProject,
            currency = settings.currency,
            onDismiss = { showProjectDialog = false },
            onSave = { updated ->
                viewModel.saveProject(updated)
                showProjectDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (projectToDelete != null) {
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("تأكيد الحذف") },
            text = { Text("هل أنت متأكد من رغبتك في حذف مشروع «${projectToDelete!!.name}»؟ لا يمكن التراجع عن هذه العملية.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProject(projectToDelete!!.id)
                        projectToDelete = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun ProjectItemCard(
    project: Project,
    currency: String,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onArchive: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("project_item_${project.id}"),
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
                Text(
                    text = project.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                ProjectStatusBadge(status = project.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                Text(project.clientName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (project.siteLocation.isNotBlank()) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(project.siteLocation, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // Financials Summary for Project
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("قيمة العقد", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${"%.1f".format(project.agreedAmount)} $currency", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column {
                    Text("إجمالي التكلفة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${"%.1f".format(project.totalProjectCost)} $currency", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column {
                    Text("صافي الربح", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${"%.1f".format(project.estimatedProfit)} $currency",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (project.estimatedProfit >= 0) EnergyGreen else DangerRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onArchive, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (project.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                        contentDescription = "أرشفة",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = DangerRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectEditorSheet(
    project: Project?,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (Project) -> Unit
) {
    var name by remember { mutableStateOf(project?.name ?: "") }
    var clientName by remember { mutableStateOf(project?.clientName ?: "") }
    var clientPhone by remember { mutableStateOf(project?.clientPhone ?: "") }
    var siteLocation by remember { mutableStateOf(project?.siteLocation ?: "") }
    var agreedAmountInput by remember { mutableStateOf(project?.agreedAmount?.toString() ?: "0.0") }
    var status by remember { mutableStateOf(project?.status ?: ProjectStatus.IN_PROGRESS) }
    var notes by remember { mutableStateOf(project?.notes ?: "") }

    // Sub items
    val materials = remember { mutableStateListOf<ProjectMaterialItem>().apply { addAll(project?.materials ?: emptyList()) } }
    val labor = remember { mutableStateListOf<ProjectLaborItem>().apply { addAll(project?.labor ?: emptyList()) } }
    val expenses = remember { mutableStateListOf<ProjectExpenseItem>().apply { addAll(project?.expenses ?: emptyList()) } }

    var showAddMaterialDialog by remember { mutableStateOf(false) }
    var showAddLaborDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Text(
                    text = if (project == null) "إنشاء مشروع كهربائي جديد" else "تعديل المشروع",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المشروع *") },
                    placeholder = { Text("مثال: تشطيب فيلا حي النرجس") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("اسم العميل *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("رقم الهاتف") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = siteLocation,
                        onValueChange = { siteLocation = it },
                        label = { Text("الموقع / الحي") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item {
                UnitNumberField(
                    value = agreedAmountInput,
                    onValueChange = { agreedAmountInput = it },
                    label = "قيمة العقد المتفق عليها مع العميل",
                    unit = currency
                )
            }

            item {
                Text("حالة المشروع:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ProjectStatus.values().forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st.titleAr, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Materials in Project section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("المواد والتوريدات (${materials.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    TextButton(onClick = { showAddMaterialDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة مادة")
                    }
                }
                materials.forEachIndexed { index, mat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(mat.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("${mat.quantity} ${mat.unit} × ${mat.unitCost} = ${mat.totalCost} $currency", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { materials.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Labor in Project section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الأعمال والعمالة (${labor.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    TextButton(onClick = { showAddLaborDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة عمل/عامل")
                    }
                }
                labor.forEachIndexed { index, lab ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(lab.description, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("${lab.workerName} • ${lab.daysOrHours} يوم/ساعة × ${lab.rate} = ${lab.totalCost} $currency", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { labor.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات المشروع") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Button(
                    onClick = {
                        if (name.isNotBlank() && clientName.isNotBlank()) {
                            val agreed = agreedAmountInput.toDoubleOrNull() ?: 0.0
                            val updated = (project ?: Project(name = name, clientName = clientName)).copy(
                                name = name,
                                clientName = clientName,
                                clientPhone = clientPhone,
                                siteLocation = siteLocation,
                                agreedAmount = agreed,
                                status = status,
                                notes = notes,
                                materials = materials.toList(),
                                labor = labor.toList(),
                                expenses = expenses.toList()
                            )
                            onSave(updated)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = name.isNotBlank() && clientName.isNotBlank()
                ) {
                    Text("حفظ المشروع", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    // Dialog for adding material to project
    if (showAddMaterialDialog) {
        var matName by remember { mutableStateOf("") }
        var matQty by remember { mutableStateOf("1") }
        var matUnit by remember { mutableStateOf("حبة") }
        var matCost by remember { mutableStateOf("0") }

        AlertDialog(
            onDismissRequest = { showAddMaterialDialog = false },
            title = { Text("إضافة مادة للمشروع") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = matName, onValueChange = { matName = it }, label = { Text("اسم المادة") })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = matQty, onValueChange = { matQty = it }, label = { Text("الكمية") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = matUnit, onValueChange = { matUnit = it }, label = { Text("الوحدة") }, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = matCost, onValueChange = { matCost = it }, label = { Text("سعر التكلفة للوحدة") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (matName.isNotBlank()) {
                        val q = matQty.toDoubleOrNull() ?: 1.0
                        val c = matCost.toDoubleOrNull() ?: 0.0
                        materials.add(
                            ProjectMaterialItem(
                                name = matName,
                                quantity = q,
                                unit = matUnit,
                                unitCost = c,
                                unitSellingPrice = c
                            )
                        )
                        showAddMaterialDialog = false
                    }
                }) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMaterialDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Dialog for adding labor to project
    if (showAddLaborDialog) {
        var desc by remember { mutableStateOf("") }
        var worker by remember { mutableStateOf("") }
        var days by remember { mutableStateOf("1") }
        var rate by remember { mutableStateOf("150") }

        AlertDialog(
            onDismissRequest = { showAddLaborDialog = false },
            title = { Text("إضافة عمل / أجور عمالة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("طبيعة العمل (تأسيس، تمديد، تركيب...)") })
                    OutlinedTextField(value = worker, onValueChange = { worker = it }, label = { Text("اسم الفني / العامل") })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = days, onValueChange = { days = it }, label = { Text("الأيام / الساعات") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text("الأجر اليومي") }, modifier = Modifier.weight(1f))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (desc.isNotBlank()) {
                        val d = days.toDoubleOrNull() ?: 1.0
                        val r = rate.toDoubleOrNull() ?: 0.0
                        labor.add(
                            ProjectLaborItem(
                                description = desc,
                                workerName = worker,
                                daysOrHours = d,
                                rate = r
                            )
                        )
                        showAddLaborDialog = false
                    }
                }) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddLaborDialog = false }) { Text("إلغاء") }
            }
        )
    }
}
