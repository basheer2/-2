package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AppSettings
import com.example.data.model.CalculationRecord
import com.example.data.model.Client
import com.example.data.model.Invoice
import com.example.data.model.MaterialItem
import com.example.data.model.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MaterialItem::class,
        Project::class,
        Client::class,
        Invoice::class,
        CalculationRecord::class,
        AppSettings::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun materialDao(): MaterialDao
    abstract fun projectDao(): ProjectDao
    abstract fun clientDao(): ClientDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun calculationHistoryDao(): CalculationHistoryDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope? = null): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "electrician_accountant.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope?
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    val coroutineScope = scope ?: CoroutineScope(Dispatchers.IO)
                    coroutineScope.launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            // Default Settings
            db.settingsDao().insertOrUpdateSettings(
                AppSettings(
                    id = 1,
                    businessName = "مؤسسة التيار الذهبي للمقاولات الكهربائية",
                    electricianName = "م/ أحمد الكهربائي",
                    phone = "0501234567",
                    currency = "ر.س",
                    defaultTaxEnabled = false,
                    defaultTaxRate = 15.0,
                    notificationsEnabled = true
                )
            )

            // Initial Materials Catalog (Common Electrical Supplies)
            val initialMaterials = listOf(
                MaterialItem(
                    name = "سلك نحاس معزول 2.5 ملم² (لفة 100م)",
                    category = "كابلات وأسلاك",
                    unit = "لفة",
                    purchasePrice = 145.0,
                    sellingPrice = 180.0,
                    quantity = 12.0,
                    minStockAlert = 4.0,
                    supplier = "شركة الكابلات المتحدة",
                    notes = "صناعة وطنية معتمدة"
                ),
                MaterialItem(
                    name = "سلك نحاس معزول 4 ملم² (لفة 100م)",
                    category = "كابلات وأسلاك",
                    unit = "لفة",
                    purchasePrice = 210.0,
                    sellingPrice = 260.0,
                    quantity = 8.0,
                    minStockAlert = 3.0,
                    supplier = "شركة الكابلات المتحدة"
                ),
                MaterialItem(
                    name = "كابل مسلح 4×16 ملم² نحاس",
                    category = "كابلات وأسلاك",
                    unit = "متر",
                    purchasePrice = 42.0,
                    sellingPrice = 55.0,
                    quantity = 3.0, // Low stock demo!
                    minStockAlert = 15.0,
                    supplier = "مؤسسة التوريدات الحديثة"
                ),
                MaterialItem(
                    name = "قاطع تفاضلي 20 أمبير أحادي (MCB 1P 20A)",
                    category = "قواطع ولوحات",
                    unit = "حبة",
                    purchasePrice = 18.0,
                    sellingPrice = 28.0,
                    quantity = 35.0,
                    minStockAlert = 10.0,
                    supplier = "وكيل شنايدر"
                ),
                MaterialItem(
                    name = "قاطع تفاضلي 32 أمبير أحادي (MCB 1P 32A)",
                    category = "قواطع ولوحات",
                    unit = "حبة",
                    purchasePrice = 22.0,
                    sellingPrice = 32.0,
                    quantity = 18.0,
                    minStockAlert = 5.0,
                    supplier = "وكيل شنايدر"
                ),
                MaterialItem(
                    name = "قاطع رئيسي 63 أمبير ثلاثي (MCB 3P 63A)",
                    category = "قواطع ولوحات",
                    unit = "حبة",
                    purchasePrice = 95.0,
                    sellingPrice = 135.0,
                    quantity = 6.0,
                    minStockAlert = 2.0,
                    supplier = "وكيل ABB"
                ),
                MaterialItem(
                    name = "مفتاح إنارة مفرد 10A ذكي ومودرن",
                    category = "مفاتيح وأفياش",
                    unit = "حبة",
                    purchasePrice = 14.0,
                    sellingPrice = 22.0,
                    quantity = 40.0,
                    minStockAlert = 10.0,
                    supplier = "مؤسسة الفنار"
                ),
                MaterialItem(
                    name = "فيش ثلاثي 13A مع مفتاح تأريض",
                    category = "مفاتيح وأفياش",
                    unit = "حبة",
                    purchasePrice = 12.0,
                    sellingPrice = 19.0,
                    quantity = 50.0,
                    minStockAlert = 15.0,
                    supplier = "مؤسسة الفنار"
                ),
                MaterialItem(
                    name = "سبوت لايت LED مقاس 7 سم 7W",
                    category = "إضاءة ولمبات",
                    unit = "حبة",
                    purchasePrice = 8.5,
                    sellingPrice = 15.0,
                    quantity = 4.0, // Low stock demo!
                    minStockAlert = 20.0,
                    supplier = "أضواء الجزيرة"
                ),
                MaterialItem(
                    name = "ماسورة PVC معزولة 20 ملم (طول 3م)",
                    category = "مواسير وتمديدات",
                    unit = "حبة",
                    purchasePrice = 5.0,
                    sellingPrice = 8.0,
                    quantity = 80.0,
                    minStockAlert = 25.0,
                    supplier = "المصنع الوطني"
                )
            )
            db.materialDao().insertAll(initialMaterials)

            // Initial Clients
            val initialClients = listOf(
                Client(
                    name = "المهندس خالد العمري",
                    phone = "0555123456",
                    address = "حي النرجس، الرياض",
                    notes = "مشروع تشطيب فيلا سكنية"
                ),
                Client(
                    name = "أبو فهد القحطاني",
                    phone = "0509876543",
                    address = "حي الملقا، الرياض",
                    notes = "تجديد لوحات ومفاتيح استراحة"
                ),
                Client(
                    name = "شركة الأفق للمقاولات",
                    phone = "0112345678",
                    address = "طريق الملك فهد",
                    notes = "عقد صيانة وتمديدات دورية"
                )
            )
            db.clientDao().insertAll(initialClients)
        }
    }
}
