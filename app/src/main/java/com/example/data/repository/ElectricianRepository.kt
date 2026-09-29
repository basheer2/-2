package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AppSettings
import com.example.data.model.CalculationRecord
import com.example.data.model.Client
import com.example.data.model.Invoice
import com.example.data.model.InvoiceType
import com.example.data.model.MaterialItem
import com.example.data.model.Project
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class ElectricianRepository(private val db: AppDatabase) {

    // Materials
    val allMaterials: Flow<List<MaterialItem>> = db.materialDao().getAllMaterials()
    val lowStockMaterials: Flow<List<MaterialItem>> = db.materialDao().getLowStockMaterials()

    suspend fun getMaterialById(id: Long): MaterialItem? = db.materialDao().getMaterialById(id)
    suspend fun insertMaterial(material: MaterialItem): Long = db.materialDao().insertMaterial(material)
    suspend fun updateMaterial(material: MaterialItem) = db.materialDao().updateMaterial(material)
    suspend fun deleteMaterial(id: Long) = db.materialDao().deleteMaterialById(id)
    suspend fun updateMaterialStock(id: Long, delta: Double) = db.materialDao().updateStockQuantity(id, delta)

    // Projects
    val activeProjects: Flow<List<Project>> = db.projectDao().getActiveProjects()
    val archivedProjects: Flow<List<Project>> = db.projectDao().getArchivedProjects()
    val allProjects: Flow<List<Project>> = db.projectDao().getAllProjects()

    suspend fun getProjectById(id: Long): Project? = db.projectDao().getProjectById(id)
    suspend fun insertProject(project: Project): Long = db.projectDao().insertProject(project)
    suspend fun updateProject(project: Project) = db.projectDao().updateProject(project)
    suspend fun deleteProject(id: Long) = db.projectDao().deleteProjectById(id)

    // Clients
    val allClients: Flow<List<Client>> = db.clientDao().getAllClients()

    suspend fun getClientById(id: Long): Client? = db.clientDao().getClientById(id)
    suspend fun insertClient(client: Client): Long = db.clientDao().insertClient(client)
    suspend fun updateClient(client: Client) = db.clientDao().updateClient(client)
    suspend fun deleteClient(id: Long) = db.clientDao().deleteClientById(id)

    // Invoices
    val allInvoices: Flow<List<Invoice>> = db.invoiceDao().getAllInvoices()
    val standardInvoices: Flow<List<Invoice>> = db.invoiceDao().getInvoicesByType(InvoiceType.INVOICE)
    val quotations: Flow<List<Invoice>> = db.invoiceDao().getInvoicesByType(InvoiceType.QUOTATION)

    fun getInvoicesForProject(projectId: Long): Flow<List<Invoice>> = db.invoiceDao().getInvoicesByProject(projectId)
    fun getInvoicesForClient(clientId: Long): Flow<List<Invoice>> = db.invoiceDao().getInvoicesByClient(clientId)
    suspend fun getInvoiceById(id: Long): Invoice? = db.invoiceDao().getInvoiceById(id)
    suspend fun insertInvoice(invoice: Invoice): Long = db.invoiceDao().insertInvoice(invoice)
    suspend fun updateInvoice(invoice: Invoice) = db.invoiceDao().updateInvoice(invoice)
    suspend fun deleteInvoice(id: Long) = db.invoiceDao().deleteInvoiceById(id)

    // Calculation History
    val allHistory: Flow<List<CalculationRecord>> = db.calculationHistoryDao().getAllHistory()

    suspend fun insertCalculation(record: CalculationRecord): Long = db.calculationHistoryDao().insertRecord(record)
    suspend fun deleteCalculation(id: Long) = db.calculationHistoryDao().deleteRecordById(id)
    suspend fun clearHistory() = db.calculationHistoryDao().clearAllHistory()

    // Settings
    val settingsFlow: Flow<AppSettings?> = db.settingsDao().getSettings()

    suspend fun getSettings(): AppSettings {
        return db.settingsDao().getSettingsDirect() ?: AppSettings()
    }

    suspend fun saveSettings(settings: AppSettings) {
        db.settingsDao().insertOrUpdateSettings(settings)
    }

    // Backup to JSON string
    suspend fun exportBackupJson(
        materials: List<MaterialItem>,
        projects: List<Project>,
        clients: List<Client>,
        invoices: List<Invoice>,
        settings: AppSettings
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        // Settings
        val sObj = JSONObject()
        sObj.put("businessName", settings.businessName)
        sObj.put("electricianName", settings.electricianName)
        sObj.put("phone", settings.phone)
        sObj.put("currency", settings.currency)
        sObj.put("defaultTaxRate", settings.defaultTaxRate)
        sObj.put("defaultTaxEnabled", settings.defaultTaxEnabled)
        root.put("settings", sObj)

        // Materials
        val mArray = JSONArray()
        materials.forEach { m ->
            val obj = JSONObject()
            obj.put("name", m.name)
            obj.put("category", m.category)
            obj.put("unit", m.unit)
            obj.put("purchasePrice", m.purchasePrice)
            obj.put("sellingPrice", m.sellingPrice)
            obj.put("quantity", m.quantity)
            obj.put("minStockAlert", m.minStockAlert)
            obj.put("supplier", m.supplier)
            obj.put("notes", m.notes)
            mArray.put(obj)
        }
        root.put("materials", mArray)

        // Clients
        val cArray = JSONArray()
        clients.forEach { c ->
            val obj = JSONObject()
            obj.put("name", c.name)
            obj.put("phone", c.phone)
            obj.put("address", c.address)
            obj.put("notes", c.notes)
            cArray.put(obj)
        }
        root.put("clients", cArray)

        return root.toString(2)
    }

    // Restore from JSON string
    suspend fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            if (root.has("settings")) {
                val sObj = root.getJSONObject("settings")
                saveSettings(
                    AppSettings(
                        businessName = sObj.optString("businessName", "مؤسسة الأعمال الكهربائية"),
                        electricianName = sObj.optString("electricianName", "فني كهرباء"),
                        phone = sObj.optString("phone", ""),
                        currency = sObj.optString("currency", "ر.س"),
                        defaultTaxRate = sObj.optDouble("defaultTaxRate", 15.0),
                        defaultTaxEnabled = sObj.optBoolean("defaultTaxEnabled", false)
                    )
                )
            }
            if (root.has("materials")) {
                val mArray = root.getJSONArray("materials")
                for (i in 0 until mArray.length()) {
                    val m = mArray.getJSONObject(i)
                    insertMaterial(
                        MaterialItem(
                            name = m.getString("name"),
                            category = m.optString("category", "أخرى"),
                            unit = m.optString("unit", "حبة"),
                            purchasePrice = m.optDouble("purchasePrice", 0.0),
                            sellingPrice = m.optDouble("sellingPrice", 0.0),
                            quantity = m.optDouble("quantity", 0.0),
                            minStockAlert = m.optDouble("minStockAlert", 5.0),
                            supplier = m.optString("supplier", ""),
                            notes = m.optString("notes", "")
                        )
                    )
                }
            }
            if (root.has("clients")) {
                val cArray = root.getJSONArray("clients")
                for (i in 0 until cArray.length()) {
                    val c = cArray.getJSONObject(i)
                    insertClient(
                        Client(
                            name = c.getString("name"),
                            phone = c.optString("phone", ""),
                            address = c.optString("address", ""),
                            notes = c.optString("notes", "")
                        )
                    )
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
