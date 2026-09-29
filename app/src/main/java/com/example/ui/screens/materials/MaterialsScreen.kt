package com.example.ui.screens.materials

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MaterialItem
import com.example.ui.components.EmptyStateView
import com.example.ui.components.UnitNumberField
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EnergyGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.ElectricianViewModel

val MaterialCategories = listOf(
    "الكل",
    "كابلات وأسلاك",
    "قواطع ولوحات",
    "مفاتيح وأفياش",
    "إضاءة ولمبات",
    "مواسير وتمديدات",
    "أخرى"
)

val MaterialUnits = listOf(
    "حبة",
    "متر",
    "لفة",
    "طقم",
    "علبة",
    "كيلوجرام"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsScreen(
    viewModel: ElectricianViewModel,
    modifier: Modifier = Modifier
) {
    val allMaterials by viewModel.allMaterials.collectAsStateWithLifecycle()
    val lowStockMaterials by viewModel.lowStockMaterials.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var selectedCategory by remember { mutableStateOf("الكل") }
    var onlyLowStock by remember { mutableStateOf(false) }

    var showEditorSheet by remember { mutableStateOf(false) }
    var editingMaterial by remember { mutableStateOf<MaterialItem?>(null) }
    var materialToDelete by remember { mutableStateOf<MaterialItem?>(null) }

    val filteredMaterials = allMaterials.filter { mat ->
        val matchesCategory = (selectedCategory == "الكل" || mat.category == selectedCategory)
        val matchesLowStock = (!onlyLowStock || mat.isLowStock)
        matchesCategory && matchesLowStock
    }

    val totalInventoryValue = allMaterials.sumOf { it.totalPurchaseValue }

    Scaffold(
        modifier = modifier.testTag("materials_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingMaterial = null
                    showEditorSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("add_material_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة صنف")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Inventory Value & Low Stock Summary Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("إجمالي قيمة المخزون التقديرية", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${"%.1f".format(totalInventoryValue)} ${settings.currency}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("${allMaterials.size} أصناف مسجلة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    FilterChip(
                        selected = onlyLowStock,
                        onClick = { onlyLowStock = !onlyLowStock },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (lowStockMaterials.isNotEmpty()) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text("نقص (${lowStockMaterials.size})", fontSize = 12.sp)
                            }
                        },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DangerRed.copy(alpha = 0.15f),
                            selectedLabelColor = DangerRed
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Category Chips Carousel
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(MaterialCategories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredMaterials.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Inventory2,
                    title = "لا توجد أصناف تطابق التصفية",
                    message = "أضف المواد والأسلاك والقواطع والمستلزمات الكهربائية لتتبع الكميات والأسعار والتنبيه عند قرب النفاد.",
                    buttonTitle = "إضافة صنف جديد",
                    onButtonClick = {
                        editingMaterial = null
                        showEditorSheet = true
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMaterials) { material ->
                        MaterialItemCard(
                            material = material,
                            currency = settings.currency,
                            onIncrement = { viewModel.adjustMaterialStock(material.id, 1.0) },
                            onDecrement = { viewModel.adjustMaterialStock(material.id, -1.0) },
                            onEdit = {
                                editingMaterial = material
                                showEditorSheet = true
                            },
                            onDelete = { materialToDelete = material }
                        )
                    }
                }
            }
        }
    }

    if (showEditorSheet) {
        MaterialEditorSheet(
            material = editingMaterial,
            currency = settings.currency,
            onDismiss = { showEditorSheet = false },
            onSave = { updated ->
                viewModel.saveMaterial(updated)
                showEditorSheet = false
            }
        )
    }

    if (materialToDelete != null) {
        AlertDialog(
            onDismissRequest = { materialToDelete = null },
            title = { Text("حذف الصنف") },
            text = { Text("هل أنت متأكد من حذف الصنف «${materialToDelete!!.name}» من سجل المخزون؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMaterial(materialToDelete!!.id)
                        materialToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { materialToDelete = null }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
fun MaterialItemCard(
    material: MaterialItem,
    currency: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("material_card_${material.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(material.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("${material.category} • المورد: ${material.supplier.ifBlank { "غير محدد" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (material.isLowStock) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DangerRed.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("منخفض!", fontSize = 11.sp, color = DangerRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("سعر الشراء: ${material.purchasePrice} $currency", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("سعر البيع: ${material.sellingPrice} $currency", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = EnergyGreen)
                }

                // Quick Quantity Controls (+ and -)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    IconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = "إنقاص", modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "${material.quantity} ${material.unit}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    IconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "زيادة", modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = DangerRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialEditorSheet(
    material: MaterialItem?,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (MaterialItem) -> Unit
) {
    var name by remember { mutableStateOf(material?.name ?: "") }
    var category by remember { mutableStateOf(material?.category ?: "كابلات وأسلاك") }
    var unit by remember { mutableStateOf(material?.unit ?: "حبة") }
    var purchasePriceInput by remember { mutableStateOf(material?.purchasePrice?.toString() ?: "0.0") }
    var sellingPriceInput by remember { mutableStateOf(material?.sellingPrice?.toString() ?: "0.0") }
    var quantityInput by remember { mutableStateOf(material?.quantity?.toString() ?: "1.0") }
    var minStockInput by remember { mutableStateOf(material?.minStockAlert?.toString() ?: "5.0") }
    var supplier by remember { mutableStateOf(material?.supplier ?: "") }
    var notes by remember { mutableStateOf(material?.notes ?: "") }

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
                    text = if (material == null) "إضافة صنف جديد إلى المخزون" else "تعديل بيانات الصنف",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الصنف أو المادة *") },
                    placeholder = { Text("مثال: كابل 3×4 ملم² نحاس") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Text("التصنيف:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(MaterialCategories.filter { it != "الكل" }) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            item {
                Text("وحدة القياس:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(MaterialUnits) { u ->
                        FilterChip(
                            selected = unit == u,
                            onClick = { unit = u },
                            label = { Text(u, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    UnitNumberField(
                        value = purchasePriceInput,
                        onValueChange = { purchasePriceInput = it },
                        label = "سعر الشراء",
                        unit = currency,
                        modifier = Modifier.weight(1f)
                    )
                    UnitNumberField(
                        value = sellingPriceInput,
                        onValueChange = { sellingPriceInput = it },
                        label = "سعر البيع",
                        unit = currency,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    UnitNumberField(
                        value = quantityInput,
                        onValueChange = { quantityInput = it },
                        label = "الكمية الحالية",
                        unit = unit,
                        modifier = Modifier.weight(1f)
                    )
                    UnitNumberField(
                        value = minStockInput,
                        onValueChange = { minStockInput = it },
                        label = "الحد الأدنى للتنبيه",
                        unit = unit,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("المورد / المحل") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val pPrice = purchasePriceInput.toDoubleOrNull() ?: 0.0
                            val sPrice = sellingPriceInput.toDoubleOrNull() ?: 0.0
                            val qty = quantityInput.toDoubleOrNull() ?: 0.0
                            val minStock = minStockInput.toDoubleOrNull() ?: 5.0

                            val updated = (material ?: MaterialItem(
                                name = name,
                                category = category,
                                unit = unit,
                                purchasePrice = pPrice,
                                sellingPrice = sPrice,
                                quantity = qty
                            )).copy(
                                name = name,
                                category = category,
                                unit = unit,
                                purchasePrice = pPrice,
                                sellingPrice = sPrice,
                                quantity = qty,
                                minStockAlert = minStock,
                                supplier = supplier,
                                notes = notes
                            )
                            onSave(updated)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = name.isNotBlank()
                ) {
                    Text("حفظ المادة", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
