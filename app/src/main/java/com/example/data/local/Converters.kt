package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.InvoiceItem
import com.example.data.model.InvoiceStatus
import com.example.data.model.InvoiceType
import com.example.data.model.ProjectExpenseItem
import com.example.data.model.ProjectLaborItem
import com.example.data.model.ProjectMaterialItem
import com.example.data.model.ProjectStatus
import org.json.JSONArray
import org.json.JSONObject

class Converters {

    // ProjectStatus
    @TypeConverter
    fun fromProjectStatus(status: ProjectStatus): String = status.name

    @TypeConverter
    fun toProjectStatus(value: String): ProjectStatus =
        try { ProjectStatus.valueOf(value) } catch (e: Exception) { ProjectStatus.IN_PROGRESS }

    // InvoiceType
    @TypeConverter
    fun fromInvoiceType(type: InvoiceType): String = type.name

    @TypeConverter
    fun toInvoiceType(value: String): InvoiceType =
        try { InvoiceType.valueOf(value) } catch (e: Exception) { InvoiceType.INVOICE }

    // InvoiceStatus
    @TypeConverter
    fun fromInvoiceStatus(status: InvoiceStatus): String = status.name

    @TypeConverter
    fun toInvoiceStatus(value: String): InvoiceStatus =
        try { InvoiceStatus.valueOf(value) } catch (e: Exception) { InvoiceStatus.UNPAID }

    // ProjectMaterialItem List
    @TypeConverter
    fun fromProjectMaterials(list: List<ProjectMaterialItem>?): String {
        if (list == null) return "[]"
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("materialId", item.materialId ?: -1L)
            obj.put("name", item.name)
            obj.put("quantity", item.quantity)
            obj.put("unit", item.unit)
            obj.put("unitCost", item.unitCost)
            obj.put("unitSellingPrice", item.unitSellingPrice)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toProjectMaterials(json: String?): List<ProjectMaterialItem> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<ProjectMaterialItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val matId = obj.optLong("materialId", -1L)
                list.add(
                    ProjectMaterialItem(
                        materialId = if (matId != -1L) matId else null,
                        name = obj.optString("name", ""),
                        quantity = obj.optDouble("quantity", 0.0),
                        unit = obj.optString("unit", ""),
                        unitCost = obj.optDouble("unitCost", 0.0),
                        unitSellingPrice = obj.optDouble("unitSellingPrice", 0.0)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    // ProjectLaborItem List
    @TypeConverter
    fun fromProjectLabor(list: List<ProjectLaborItem>?): String {
        if (list == null) return "[]"
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("description", item.description)
            obj.put("workerName", item.workerName)
            obj.put("daysOrHours", item.daysOrHours)
            obj.put("rate", item.rate)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toProjectLabor(json: String?): List<ProjectLaborItem> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<ProjectLaborItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ProjectLaborItem(
                        description = obj.optString("description", ""),
                        workerName = obj.optString("workerName", ""),
                        daysOrHours = obj.optDouble("daysOrHours", 1.0),
                        rate = obj.optDouble("rate", 0.0)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    // ProjectExpenseItem List
    @TypeConverter
    fun fromProjectExpenses(list: List<ProjectExpenseItem>?): String {
        if (list == null) return "[]"
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("description", item.description)
            obj.put("amount", item.amount)
            obj.put("date", item.date)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toProjectExpenses(json: String?): List<ProjectExpenseItem> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<ProjectExpenseItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ProjectExpenseItem(
                        description = obj.optString("description", ""),
                        amount = obj.optDouble("amount", 0.0),
                        date = obj.optLong("date", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    // InvoiceItem List
    @TypeConverter
    fun fromInvoiceItems(list: List<InvoiceItem>?): String {
        if (list == null) return "[]"
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("description", item.description)
            obj.put("quantity", item.quantity)
            obj.put("unitPrice", item.unitPrice)
            obj.put("discount", item.discount)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toInvoiceItems(json: String?): List<InvoiceItem> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<InvoiceItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    InvoiceItem(
                        description = obj.optString("description", ""),
                        quantity = obj.optDouble("quantity", 1.0),
                        unitPrice = obj.optDouble("unitPrice", 0.0),
                        discount = obj.optDouble("discount", 0.0)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
