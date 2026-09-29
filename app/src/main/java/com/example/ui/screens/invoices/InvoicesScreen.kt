package com.example.ui.screens.invoices

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
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
import androidx.compose.material3.Switch
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
import com.example.data.model.Invoice
import com.example.data.model.InvoiceItem
import com.example.data.model.InvoiceStatus
import com.example.data.model.InvoiceType
import com.example.ui.components.EmptyStateView
import com.example.ui.components.InvoiceStatusBadge
import com.example.ui.components.UnitNumberField
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EnergyGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.ElectricianViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicesScreen(
    viewModel: ElectricianViewModel,
    onInvoiceClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val allInvoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var selectedType by remember { mutableStateOf(InvoiceType.INVOICE) }
    var selectedStatusFilter by remember { mutableStateOf("all") }

    var showEditorSheet by remember { mutableStateOf(false) }
    var editingInvoice by remember { mutableStateOf<Invoice?>(null) }
    var invoiceToDelete by remember { mutableStateOf<Invoice?>(null) }
    var invoiceToPay by remember { mutableStateOf<Invoice?>(null) }

    val filteredInvoices = allInvoices.filter { inv ->
        val matchesType = inv.type == selectedType
        val matchesStatus = when (selectedStatusFilter) {
            "all" -> true
            "paid" -> inv.status == InvoiceStatus.PAID
            "unpaid" -> inv.status == InvoiceStatus.UNPAID
            "partial" -> inv.status == InvoiceStatus.PARTIAL
            else -> true
        }
        matchesType && matchesStatus
    }

    val totalInvoiced = allInvoices.filter { it.type == InvoiceType.INVOICE }.sumOf { it.grandTotal }
    val totalPaid = allInvoices.filter { it.type == InvoiceType.INVOICE }.sumOf { it.paidAmount }
    val totalDue = allInvoices.filter { it.type == InvoiceType.INVOICE }.sumOf { it.remainingDue }

    Scaffold(
        modifier = modifier.testTag("invoices_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingInvoice = null
                    showEditorSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("add_invoice_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إنشاء فاتورة أو عرض سعر")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header Financial Summary
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
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("إجمالي الفواتير", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${"%.1f".format(totalInvoiced)} ${settings.currency}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column {
                        Text("المحصّل فعلياً", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${"%.1f".format(totalPaid)} ${settings.currency}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EnergyGreen)
                    }
                    Column {
                        Text("المتبقي غير محصل", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${"%.1f".format(totalDue)} ${settings.currency}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WarningOrange)
                    }
                }
            }

            // Tabs for Type: Invoices vs Quotations
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == InvoiceType.INVOICE,
                    onClick = { selectedType = InvoiceType.INVOICE },
                    label = { Text("فواتير المبيعات والأعمال") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedType == InvoiceType.QUOTATION,
                    onClick = { selectedType = InvoiceType.QUOTATION },
                    label = { Text("عروض الأسعار (Quotes)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            // Status Filter Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statuses = listOf(
                    Pair("all", "الكل (${allInvoices.count { it.type == selectedType }})"),
                    Pair("unpaid", "غير مدفوعة (${allInvoices.count { it.type == selectedType && it.status == InvoiceStatus.UNPAID }})"),
                    Pair("partial", "مدفوعة جزئياً (${allInvoices.count { it.type == selectedType && it.status == InvoiceStatus.PARTIAL }})"),
                    Pair("paid", "مدفوعة بالكامل (${allInvoices.count { it.type == selectedType && it.status == InvoiceStatus.PAID }})")
                )
                items(statuses) { (key, label) ->
                    FilterChip(
                        selected = selectedStatusFilter == key,
                        onClick = { selectedStatusFilter = key },
                        label = { Text(label, fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            if (filteredInvoices.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.ReceiptLong,
                    title = if (selectedType == InvoiceType.INVOICE) "لا توجد فواتير مسجلة" else "لا توجد عروض أسعار",
                    message = "يمكنك إنشاء فاتورة رسمية أو عرض سعر مع تفاصيل المواد والأعمال والخصم والضريبة وطباعتها للعميل.",
                    buttonTitle = if (selectedType == InvoiceType.INVOICE) "إنشاء فاتورة جديدة" else "إنشاء عرض سعر",
                    onButtonClick = {
                        editingInvoice = null
                        showEditorSheet = true
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredInvoices) { invoice ->
                        InvoiceItemCard(
                            invoice = invoice,
                            currency = settings.currency,
                            onClick = { onInvoiceClick(invoice.id) },
                            onRecordPayment = { invoiceToPay = invoice },
                            onDelete = { invoiceToDelete = invoice }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Invoice Sheet
    if (showEditorSheet) {
        InvoiceEditorSheet(
            invoice = editingInvoice,
            currency = settings.currency,
            defaultTaxEnabled = settings.defaultTaxEnabled,
            defaultTaxRate = settings.defaultTaxRate,
            initialType = selectedType,
            onDismiss = { showEditorSheet = false },
            onSave = { updated ->
                viewModel.saveInvoice(updated)
                showEditorSheet = false
            }
        )
    }

    // Payment Dialog
    if (invoiceToPay != null) {
        var payAmountInput by remember { mutableStateOf("${invoiceToPay!!.remainingDue}") }
        AlertDialog(
            onDismissRequest = { invoiceToPay = null },
            title = { Text("تسجيل دفعة للفاتورة ${invoiceToPay!!.invoiceNumber}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("المبلغ الإجمالي: ${invoiceToPay!!.grandTotal} ${settings.currency}")
                    Text("المبلغ المسدد مسبقاً: ${invoiceToPay!!.paidAmount} ${settings.currency}")
                    Text("المتبقي: ${invoiceToPay!!.remainingDue} ${settings.currency}", fontWeight = FontWeight.Bold, color = DangerRed)
                    Spacer(modifier = Modifier.height(4.dp))
                    UnitNumberField(
                        value = payAmountInput,
                        onValueChange = { payAmountInput = it },
                        label = "قيمة الدفعة المستلمة",
                        unit = settings.currency
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amount = payAmountInput.toDoubleOrNull() ?: 0.0
                    val currentInvoice = invoiceToPay
                    if (amount > 0 && currentInvoice != null) {
                        viewModel.recordInvoicePayment(currentInvoice, amount)
                        invoiceToPay = null
                    }
                }) {
                    Text("تأكيد الدفع")
                }
            },
            dismissButton = {
                TextButton(onClick = { invoiceToPay = null }) { Text("إلغاء") }
            }
        )
    }

    // Delete Confirm
    if (invoiceToDelete != null) {
        AlertDialog(
            onDismissRequest = { invoiceToDelete = null },
            title = { Text("حذف ${invoiceToDelete!!.type.titleAr}") },
            text = { Text("هل أنت متأكد من حذف ${invoiceToDelete!!.invoiceNumber} نهائياً؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteInvoice(invoiceToDelete!!.id)
                        invoiceToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { invoiceToDelete = null }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
fun InvoiceItemCard(
    invoice: Invoice,
    currency: String,
    onClick: () -> Unit,
    onRecordPayment: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("invoice_card_${invoice.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Text(invoice.clientName, fontSize = 13.sp)
                }
                InvoiceStatusBadge(status = invoice.status)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("الإجمالي المطلوب", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${"%.1f".format(invoice.grandTotal)} $currency", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column {
                    Text("المدفوع", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${"%.1f".format(invoice.paidAmount)} $currency", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EnergyGreen)
                }
                Column {
                    Text("المتبقي", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${"%.1f".format(invoice.remainingDue)} $currency",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (invoice.remainingDue > 0) DangerRed else EnergyGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (invoice.type == InvoiceType.INVOICE && invoice.remainingDue > 0) {
                    TextButton(onClick = onRecordPayment) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تسجيل دفعة", fontSize = 12.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
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
fun InvoiceEditorSheet(
    invoice: Invoice?,
    currency: String,
    defaultTaxEnabled: Boolean,
    defaultTaxRate: Double,
    initialType: InvoiceType,
    onDismiss: () -> Unit,
    onSave: (Invoice) -> Unit
) {
    var invoiceNumber by remember {
        mutableStateOf(
            invoice?.invoiceNumber ?: (if (initialType == InvoiceType.INVOICE) "INV-${System.currentTimeMillis() % 10000}" else "QUO-${System.currentTimeMillis() % 10000}")
        )
    }
    var type by remember { mutableStateOf(invoice?.type ?: initialType) }
    var clientName by remember { mutableStateOf(invoice?.clientName ?: "") }
    var clientPhone by remember { mutableStateOf(invoice?.clientPhone ?: "") }
    var projectName by remember { mutableStateOf(invoice?.projectName ?: "") }
    var overallDiscountInput by remember { mutableStateOf(invoice?.overallDiscount?.toString() ?: "0.0") }
    var isTaxEnabled by remember { mutableStateOf(invoice?.isTaxEnabled ?: defaultTaxEnabled) }
    var taxRateInput by remember { mutableStateOf(invoice?.taxRate?.toString() ?: "$defaultTaxRate") }
    var notes by remember { mutableStateOf(invoice?.notes ?: "") }

    val items = remember {
        mutableStateListOf<InvoiceItem>().apply {
            if (invoice != null) addAll(invoice.items)
            else add(InvoiceItem("أعمال تمديدات وتأسيس كهربائي", 1.0, 500.0))
        }
    }

    var showAddItemDialog by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val subtotal = items.sumOf { it.total }
    val discount = overallDiscountInput.toDoubleOrNull() ?: 0.0
    val taxable = maxOf(0.0, subtotal - discount)
    val taxRate = if (isTaxEnabled) (taxRateInput.toDoubleOrNull() ?: 15.0) else 0.0
    val taxAmount = taxable * (taxRate / 100.0)
    val grandTotal = taxable + taxAmount

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
                    text = if (invoice == null) "إنشاء ${type.titleAr} جديدة" else "تعديل ${type.titleAr}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == InvoiceType.INVOICE,
                        onClick = { type = InvoiceType.INVOICE },
                        label = { Text("فاتورة") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == InvoiceType.QUOTATION,
                        onClick = { type = InvoiceType.QUOTATION },
                        label = { Text("عرض سعر") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = invoiceNumber,
                    onValueChange = { invoiceNumber = it },
                    label = { Text("رقم المستند *") },
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
                        label = { Text("هاتف العميل") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = { projectName = it },
                        label = { Text("المشروع المرتبط") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Items List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("بنود المواد والخدمات (${items.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    TextButton(onClick = { showAddItemDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة بند")
                    }
                }
            }

            items(items) { itemLine ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(itemLine.description, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("${itemLine.quantity} × ${itemLine.unitPrice} = ${itemLine.total} $currency", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { items.remove(itemLine) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Discount & Tax
            item {
                UnitNumberField(
                    value = overallDiscountInput,
                    onValueChange = { overallDiscountInput = it },
                    label = "خصم إضافي",
                    unit = currency
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تطبيق ضريبة القيمة المضافة (VAT):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(checked = isTaxEnabled, onCheckedChange = { isTaxEnabled = it })
                }
                if (isTaxEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    UnitNumberField(
                        value = taxRateInput,
                        onValueChange = { taxRateInput = it },
                        label = "نسبة الضريبة",
                        unit = "%"
                    )
                }
            }

            // Totals Review Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("المجموع الفرعي:", fontSize = 12.sp)
                            Text("${"%.1f".format(subtotal)} $currency", fontSize = 12.sp)
                        }
                        if (discount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("الخصم:", fontSize = 12.sp)
                                Text("-${"%.1f".format(discount)} $currency", fontSize = 12.sp, color = DangerRed)
                            }
                        }
                        if (isTaxEnabled) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("الضريبة (${taxRate}%):", fontSize = 12.sp)
                                Text("+${"%.1f".format(taxAmount)} $currency", fontSize = 12.sp)
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("المبلغ الإجمالي النهائي:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("${"%.1f".format(grandTotal)} $currency", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات وشروط الفاتورة") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Button(
                    onClick = {
                        if (clientName.isNotBlank() && invoiceNumber.isNotBlank() && items.isNotEmpty()) {
                            val updated = (invoice ?: Invoice(invoiceNumber = invoiceNumber, clientName = clientName)).copy(
                                invoiceNumber = invoiceNumber,
                                type = type,
                                clientName = clientName,
                                clientPhone = clientPhone,
                                projectName = projectName,
                                items = items.toList(),
                                overallDiscount = discount,
                                isTaxEnabled = isTaxEnabled,
                                taxRate = taxRate,
                                notes = notes
                            )
                            onSave(updated)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = clientName.isNotBlank() && invoiceNumber.isNotBlank() && items.isNotEmpty()
                ) {
                    Text("حفظ ${type.titleAr}", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Add Item Dialog
    if (showAddItemDialog) {
        var desc by remember { mutableStateOf("") }
        var qty by remember { mutableStateOf("1") }
        var price by remember { mutableStateOf("50") }

        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text("إضافة بند للفاتورة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("وصف البند / المادة أو الخدمة") })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("الكمية") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("سعر الوحدة") }, modifier = Modifier.weight(1f))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (desc.isNotBlank()) {
                        val q = qty.toDoubleOrNull() ?: 1.0
                        val p = price.toDoubleOrNull() ?: 0.0
                        items.add(InvoiceItem(desc, q, p))
                        showAddItemDialog = false
                    }
                }) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) { Text("إلغاء") }
            }
        )
    }
}
