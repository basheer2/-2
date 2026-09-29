package com.example.ui.screens.invoices

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Invoice
import com.example.data.model.InvoiceType
import com.example.ui.components.InvoiceStatusBadge
import com.example.ui.components.UnitNumberField
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EnergyGreen
import com.example.ui.viewmodel.ElectricianViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    viewModel: ElectricianViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allInvoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val invoice = allInvoices.firstOrNull { it.id == invoiceId }

    val context = LocalContext.current
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (invoice == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("الفاتورة غير متوفرة أو تم حذفها")
        }
        return
    }

    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    Scaffold(
        modifier = modifier.testTag("invoice_detail_screen"),
        topBar = {
            TopAppBar(
                title = { Text("${invoice.type.titleAr} ${invoice.invoiceNumber}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        shareInvoiceReceipt(context, invoice, settings.businessName, settings.phone, settings.currency)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة")
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
            // Printable Arabic Invoice Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(settings.businessName, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.primary)
                                Text("فني كهربائي: ${settings.electricianName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (settings.phone.isNotBlank()) {
                                    Text("هاتف: ${settings.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            InvoiceStatusBadge(status = invoice.status)
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(14.dp))

                        // Invoice & Client Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("العميل: ${invoice.clientName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (invoice.clientPhone.isNotBlank()) Text("هاتف العميل: ${invoice.clientPhone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (invoice.projectName.isNotBlank()) Text("المشروع: ${invoice.projectName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("رقم: ${invoice.invoiceNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("التاريخ: ${dateFormat.format(Date(invoice.issueDate))}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("البند / الخدمة", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(2f))
                            Text("الكمية", fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            Text("السعر", fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            Text("الإجمالي", fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                        }

                        // Table Rows
                        invoice.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.description, fontSize = 12.sp, modifier = Modifier.weight(2f))
                                Text("${item.quantity}", fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                Text("${item.unitPrice}", fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                Text("${item.total} ${settings.currency}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                            }
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Summary Calculations
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(modifier = Modifier.width(220.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("المجموع الفرعي:", fontSize = 12.sp)
                                Text("${"%.1f".format(invoice.subtotal)} ${settings.currency}", fontSize = 12.sp)
                            }

                            if (invoice.overallDiscount > 0) {
                                Row(modifier = Modifier.width(220.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("الخصم:", fontSize = 12.sp)
                                    Text("-${"%.1f".format(invoice.overallDiscount)} ${settings.currency}", fontSize = 12.sp, color = DangerRed)
                                }
                            }

                            if (invoice.isTaxEnabled) {
                                Row(modifier = Modifier.width(220.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("ضريبة القيمة المضافة (${invoice.taxRate}%):", fontSize = 12.sp)
                                    Text("+${"%.1f".format(invoice.taxAmount)} ${settings.currency}", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Divider(modifier = Modifier.width(220.dp))
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(modifier = Modifier.width(220.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("الإجمالي المستحق:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${"%.1f".format(invoice.grandTotal)} ${settings.currency}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                            }

                            if (invoice.type == InvoiceType.INVOICE) {
                                Row(modifier = Modifier.width(220.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("المدفوع:", fontSize = 12.sp)
                                    Text("${"%.1f".format(invoice.paidAmount)} ${settings.currency}", fontSize = 12.sp, color = EnergyGreen)
                                }
                                Row(modifier = Modifier.width(220.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("المتبقي:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        "${"%.1f".format(invoice.remainingDue)} ${settings.currency}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (invoice.remainingDue > 0) DangerRed else EnergyGreen
                                    )
                                }
                            }
                        }

                        if (invoice.terms.isNotBlank() || invoice.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("الشروط والملاحظات:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(invoice.terms.ifBlank { invoice.notes }, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Quick Payment Action Card
            if (invoice.type == InvoiceType.INVOICE && invoice.remainingDue > 0) {
                item {
                    Button(
                        onClick = { showPaymentDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تسجيل سداد دفعة (${"%.1f".format(invoice.remainingDue)} ${settings.currency})")
                    }
                }
            }
        }
    }

    if (showPaymentDialog) {
        var amountText by remember { mutableStateOf("${invoice.remainingDue}") }
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("تسجيل دفعة سداد") },
            text = {
                Column {
                    Text("المبلغ المتبقي: ${invoice.remainingDue} ${settings.currency}")
                    Spacer(modifier = Modifier.height(8.dp))
                    UnitNumberField(value = amountText, onValueChange = { amountText = it }, label = "المبلغ المسدد", unit = settings.currency)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val a = amountText.toDoubleOrNull() ?: 0.0
                    if (a > 0) {
                        viewModel.recordInvoicePayment(invoice, a)
                        showPaymentDialog = false
                    }
                }) {
                    Text("تسجيل الدفع")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) { Text("إلغاء") }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف المستند") },
            text = { Text("هل أنت متأكد من حذف هذا المستند؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteInvoice(invoice.id)
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

private fun shareInvoiceReceipt(
    context: Context,
    invoice: Invoice,
    businessName: String,
    businessPhone: String,
    currency: String
) {
    val text = buildString {
        appendLine("⚡ $businessName")
        if (businessPhone.isNotBlank()) appendLine("هاتف: $businessPhone")
        appendLine("================================")
        appendLine("${invoice.type.titleAr} رقم: ${invoice.invoiceNumber}")
        appendLine("العميل: ${invoice.clientName} (${invoice.clientPhone})")
        if (invoice.projectName.isNotBlank()) appendLine("المشروع: ${invoice.projectName}")
        appendLine("الحالة: ${invoice.status.titleAr}")
        appendLine("--------------------------------")
        appendLine("البنود:")
        invoice.items.forEach { item ->
            appendLine("• ${item.description} - ${item.quantity} × ${item.unitPrice} = ${item.total} $currency")
        }
        appendLine("--------------------------------")
        appendLine("المجموع الفرعي: ${invoice.subtotal} $currency")
        if (invoice.overallDiscount > 0) appendLine("الخصم: -${invoice.overallDiscount} $currency")
        if (invoice.isTaxEnabled) appendLine("ضريبة القيمة المضافة: ${invoice.taxAmount} $currency")
        appendLine("الإجمالي النهائي: ${invoice.grandTotal} $currency")
        if (invoice.type == InvoiceType.INVOICE) {
            appendLine("المدفوع: ${invoice.paidAmount} $currency")
            appendLine("المتبقي: ${invoice.remainingDue} $currency")
        }
        appendLine("================================")
        appendLine("شكراً لتعاملكم معنا!")
        appendLine("تم الإصدار عبر تطبيق محاسب الكهربائي")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "${invoice.type.titleAr} ${invoice.invoiceNumber}")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة الفاتورة"))
}
