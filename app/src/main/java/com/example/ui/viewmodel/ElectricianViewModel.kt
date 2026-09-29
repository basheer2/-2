package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AppSettings
import com.example.data.model.CalculationRecord
import com.example.data.model.Client
import com.example.data.model.Invoice
import com.example.data.model.MaterialItem
import com.example.data.model.Project
import com.example.data.model.ProjectStatus
import com.example.data.repository.ElectricianRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchResults(
    val projects: List<Project> = emptyList(),
    val materials: List<MaterialItem> = emptyList(),
    val clients: List<Client> = emptyList(),
    val invoices: List<Invoice> = emptyList(),
    val query: String = ""
)

data class FinancialOverview(
    val totalRevenueCollected: Double = 0.0, // Total paid from invoices
    val totalOutstandingDue: Double = 0.0, // Remaining due from invoices
    val totalInvoiced: Double = 0.0,
    val totalInventoryValue: Double = 0.0,
    val totalProjectCosts: Double = 0.0,
    val totalEstimatedProfits: Double = 0.0
)

class ElectricianViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = ElectricianRepository(db)

    val activeProjects: StateFlow<List<Project>> = repository.activeProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedProjects: StateFlow<List<Project>> = repository.archivedProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProjects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMaterials: StateFlow<List<MaterialItem>> = repository.allMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockMaterials: StateFlow<List<MaterialItem>> = repository.lowStockMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClients: StateFlow<List<Client>> = repository.allClients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvoices: StateFlow<List<Invoice>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val standardInvoices: StateFlow<List<Invoice>> = repository.standardInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quotations: StateFlow<List<Invoice>> = repository.quotations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calculationHistory: StateFlow<List<CalculationRecord>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AppSettings> = repository.settingsFlow
        .map { it ?: AppSettings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Global Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<SearchResults> = combine(
        searchQuery,
        allProjects,
        allMaterials,
        allClients,
        allInvoices
    ) { q, projs, mats, cls, invs ->
        if (q.isBlank()) {
            SearchResults(query = q)
        } else {
            val queryLower = q.trim().lowercase()
            SearchResults(
                projects = projs.filter {
                    it.name.lowercase().contains(queryLower) ||
                    it.clientName.lowercase().contains(queryLower) ||
                    it.siteLocation.lowercase().contains(queryLower)
                },
                materials = mats.filter {
                    it.name.lowercase().contains(queryLower) ||
                    it.category.lowercase().contains(queryLower) ||
                    it.supplier.lowercase().contains(queryLower)
                },
                clients = cls.filter {
                    it.name.lowercase().contains(queryLower) ||
                    it.phone.lowercase().contains(queryLower)
                },
                invoices = invs.filter {
                    it.invoiceNumber.lowercase().contains(queryLower) ||
                    it.clientName.lowercase().contains(queryLower) ||
                    it.projectName.lowercase().contains(queryLower)
                },
                query = q
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResults())

    // Financial calculations summary
    val financialOverview: StateFlow<FinancialOverview> = combine(
        allInvoices,
        allMaterials,
        allProjects
    ) { invs, mats, projs ->
        val totalInvoiced = invs.sumOf { it.grandTotal }
        val totalPaid = invs.sumOf { it.paidAmount }
        val totalDue = invs.sumOf { it.remainingDue }
        val inventoryVal = mats.sumOf { it.totalPurchaseValue }
        val projCosts = projs.sumOf { it.totalProjectCost }
        val projProfits = projs.sumOf { it.estimatedProfit }

        FinancialOverview(
            totalRevenueCollected = totalPaid,
            totalOutstandingDue = totalDue,
            totalInvoiced = totalInvoiced,
            totalInventoryValue = inventoryVal,
            totalProjectCosts = projCosts,
            totalEstimatedProfits = projProfits
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialOverview())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Projects Operations ---
    fun saveProject(project: Project, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = if (project.id == 0L) {
                repository.insertProject(project)
            } else {
                repository.updateProject(project)
                project.id
            }
            _userMessage.emit("تم حفظ المشروع بنجاح")
            onComplete?.invoke(id)
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            _userMessage.emit("تم حذف المشروع")
        }
    }

    fun toggleProjectArchive(project: Project) {
        viewModelScope.launch {
            val updated = project.copy(isArchived = !project.isArchived)
            repository.updateProject(updated)
            _userMessage.emit(if (updated.isArchived) "تم أرشفة المشروع" else "تم إلغاء أرشفة المشروع")
        }
    }

    fun updateProjectStatus(project: Project, newStatus: ProjectStatus) {
        viewModelScope.launch {
            repository.updateProject(project.copy(status = newStatus))
            _userMessage.emit("تم تحديث حالة المشروع إلى ${newStatus.titleAr}")
        }
    }

    // --- Materials Operations ---
    fun saveMaterial(material: MaterialItem, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            if (material.id == 0L) {
                repository.insertMaterial(material)
            } else {
                repository.updateMaterial(material)
            }
            _userMessage.emit("تم حفظ المادة بنجاح")
            onComplete?.invoke()
        }
    }

    fun deleteMaterial(materialId: Long) {
        viewModelScope.launch {
            repository.deleteMaterial(materialId)
            _userMessage.emit("تم حذف الصنف من المخزون")
        }
    }

    fun adjustMaterialStock(materialId: Long, delta: Double) {
        viewModelScope.launch {
            repository.updateMaterialStock(materialId, delta)
        }
    }

    // --- Clients Operations ---
    fun saveClient(client: Client, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = if (client.id == 0L) {
                repository.insertClient(client)
            } else {
                repository.updateClient(client)
                client.id
            }
            _userMessage.emit("تم حفظ بيانات العميل بنجاح")
            onComplete?.invoke(id)
        }
    }

    fun deleteClient(clientId: Long) {
        viewModelScope.launch {
            repository.deleteClient(clientId)
            _userMessage.emit("تم حذف العميل")
        }
    }

    // --- Invoices Operations ---
    fun saveInvoice(invoice: Invoice, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = if (invoice.id == 0L) {
                repository.insertInvoice(invoice)
            } else {
                repository.updateInvoice(invoice)
                invoice.id
            }
            _userMessage.emit("تم حفظ ${invoice.type.titleAr} بنجاح")
            onComplete?.invoke(id)
        }
    }

    fun recordInvoicePayment(invoice: Invoice, amount: Double) {
        viewModelScope.launch {
            val newPaid = (invoice.paidAmount + amount).coerceAtMost(invoice.grandTotal)
            val newStatus = if (newPaid >= invoice.grandTotal) {
                com.example.data.model.InvoiceStatus.PAID
            } else if (newPaid > 0) {
                com.example.data.model.InvoiceStatus.PARTIAL
            } else {
                com.example.data.model.InvoiceStatus.UNPAID
            }
            repository.updateInvoice(invoice.copy(paidAmount = newPaid, status = newStatus))
            _userMessage.emit("تم تسجيل دفعة بقيمة $amount ${settings.value.currency}")
        }
    }

    fun deleteInvoice(invoiceId: Long) {
        viewModelScope.launch {
            repository.deleteInvoice(invoiceId)
            _userMessage.emit("تم حذف الفاتورة")
        }
    }

    // --- History Operations ---
    fun saveCalculationRecord(
        title: String,
        category: String,
        inputSummary: String,
        resultSummary: String,
        formula: String = ""
    ) {
        viewModelScope.launch {
            repository.insertCalculation(
                CalculationRecord(
                    title = title,
                    category = category,
                    inputSummary = inputSummary,
                    resultSummary = resultSummary,
                    detailedFormula = formula
                )
            )
            _userMessage.emit("تم حفظ الحسبة في سجل العمليات")
        }
    }

    fun deleteCalculationRecord(recordId: Long) {
        viewModelScope.launch {
            repository.deleteCalculation(recordId)
            _userMessage.emit("تم حذف السجل")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _userMessage.emit("تم مسح السجل بالكامل")
        }
    }

    // --- Settings Operations ---
    fun saveSettings(updated: AppSettings) {
        viewModelScope.launch {
            repository.saveSettings(updated)
            _userMessage.emit("تم حفظ الإعدادات بنجاح")
        }
    }

    suspend fun exportDataJson(): String {
        return repository.exportBackupJson(
            materials = allMaterials.value,
            projects = allProjects.value,
            clients = allClients.value,
            invoices = allInvoices.value,
            settings = settings.value
        )
    }

    fun importDataJson(jsonString: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.importBackupJson(jsonString)
            if (success) {
                _userMessage.emit("تمت استعادة البيانات بنجاح")
            } else {
                _userMessage.emit("فشلت استعادة البيانات! تحقق من صحة الملف")
            }
            onResult(success)
        }
    }

    fun resetToDemoData() {
        viewModelScope.launch {
            AppDatabase.populateInitialData(db)
            _userMessage.emit("تمت إعادة تحميل البيانات النموذجية بنجاح")
        }
    }
}
