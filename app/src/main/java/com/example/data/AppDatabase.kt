package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CompatibilityDao
import com.example.data.dao.InventoryDao
import com.example.data.dao.RepairJobDao
import com.example.data.dao.ShopDao
import com.example.data.model.CompatibilityModel
import com.example.data.model.InventoryItem
import com.example.data.model.RepairJob
import com.example.data.model.ShopProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ShopProfile::class,
        InventoryItem::class,
        RepairJob::class,
        CompatibilityModel::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shopDao(): ShopDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun repairJobDao(): RepairJobDao
    abstract fun compatibilityDao(): CompatibilityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mobirepair_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        seedDatabase(database)
                    }
                }
            }
        }

        suspend fun seedDatabase(database: AppDatabase) {
            // Seed Shop Profile
            database.shopDao().insertOrUpdateProfile(
                ShopProfile(
                    id = 1,
                    shopName = "Bharat Mobile Care & Spares",
                    ownerName = "Rajesh Sharma",
                    upiId = "bharatmobile@okaxis",
                    ownerPhone = "9876543210",
                    address = "Shop 12, Main Mobile Market, Delhi Rd",
                    selectedLanguage = "EN",
                    enableWhatsAppAutoPrompt = true
                )
            )

            // Seed Inventory Items
            val initialInventory = listOf(
                InventoryItem(
                    name = "Realme C2 / Oppo A1k Display Combo OG",
                    category = "Folder (Display)",
                    quantity = 4,
                    costPrice = 650.0,
                    sellingPrice = 1250.0,
                    minAlertQuantity = 2,
                    compatibleModels = "Realme C2, Oppo A1k, Realme C1 (2019)",
                    partCode = "COMBO-A1K"
                ),
                InventoryItem(
                    name = "Vivo Y11 / Y12 / Y15 / Y17 Display Combo",
                    category = "Folder (Display)",
                    quantity = 3,
                    costPrice = 720.0,
                    sellingPrice = 1350.0,
                    minAlertQuantity = 2,
                    compatibleModels = "Vivo Y11, Vivo Y12, Vivo Y15, Vivo Y17, Vivo Y3, Vivo U10",
                    partCode = "COMBO-Y12"
                ),
                InventoryItem(
                    name = "Redmi 9 Power / Poco M3 CC Board Sub-board",
                    category = "Charging CC Board",
                    quantity = 1, // Alert! Low stock
                    costPrice = 130.0,
                    sellingPrice = 450.0,
                    minAlertQuantity = 2,
                    compatibleModels = "Redmi 9 Power, Poco M3, Redmi 9T",
                    partCode = "CC-R9P"
                ),
                InventoryItem(
                    name = "Type-C Universal 16-Pin Charging Jack (10 Pcs)",
                    category = "Charging Jack",
                    quantity = 6,
                    costPrice = 120.0,
                    sellingPrice = 350.0,
                    minAlertQuantity = 2,
                    compatibleModels = "Redmi 8, Redmi Note 8, Vivo Y20, Samsung M12",
                    partCode = "JACK-TC16"
                ),
                InventoryItem(
                    name = "BN59 Redmi Note 10 / 10S Battery 5000mAh",
                    category = "Battery",
                    quantity = 1, // Alert! Low stock
                    costPrice = 480.0,
                    sellingPrice = 950.0,
                    minAlertQuantity = 2,
                    compatibleModels = "Redmi Note 10, Redmi Note 10S, Poco M5s",
                    partCode = "BN-59"
                ),
                InventoryItem(
                    name = "BLP673 Battery 4230mAh OG",
                    category = "Battery",
                    quantity = 3,
                    costPrice = 410.0,
                    sellingPrice = 850.0,
                    minAlertQuantity = 2,
                    compatibleModels = "Realme C1, Realme 2, Oppo A3s, Oppo A5",
                    partCode = "BLP-673"
                ),
                InventoryItem(
                    name = "Samsung M21 / M30s / M31 OLED Display Folder",
                    category = "Folder (Display)",
                    quantity = 2,
                    costPrice = 1450.0,
                    sellingPrice = 2400.0,
                    minAlertQuantity = 2,
                    compatibleModels = "Samsung Galaxy M21, Samsung Galaxy M30s, Samsung Galaxy M31, Galaxy M21s",
                    partCode = "COMBO-M21"
                ),
                InventoryItem(
                    name = "Realme 5 / 5i / 5s / Oppo A5 2020 LCD Folder",
                    category = "Folder (Display)",
                    quantity = 0, // Alert! Out of stock
                    costPrice = 690.0,
                    sellingPrice = 1300.0,
                    minAlertQuantity = 2,
                    compatibleModels = "Realme 5, Realme 5i, Realme 5s, Oppo A5 2020, Oppo A9 2020",
                    partCode = "COMBO-R5"
                ),
                InventoryItem(
                    name = "Relife CP-0002 Liquid Glue 50ml",
                    category = "Repair Tools / Glue",
                    quantity = 4,
                    costPrice = 90.0,
                    sellingPrice = 200.0,
                    minAlertQuantity = 2,
                    compatibleModels = "Universal (All Phones)",
                    partCode = "GLUE-CP02"
                ),
                InventoryItem(
                    name = "Universal Earpiece Speaker 12x6mm (5 Pcs)",
                    category = "Speaker / Earpiece",
                    quantity = 5,
                    costPrice = 40.0,
                    sellingPrice = 150.0,
                    minAlertQuantity = 2,
                    compatibleModels = "Realme C2, Oppo A1k, Realme C1, Realme 2, Oppo A3s, Realme 3",
                    partCode = "SPK-1206"
                ),
                InventoryItem(
                    name = "Redmi 9 Power / Poco M3 Loudspeaker Buzzer Box",
                    category = "Speaker / Buzzer",
                    quantity = 2,
                    costPrice = 110.0,
                    sellingPrice = 350.0,
                    minAlertQuantity = 2,
                    compatibleModels = "Redmi 9 Power, Poco M3, Redmi 9T",
                    partCode = "RINGER-R9P"
                )
            )
            database.inventoryDao().insertAll(initialInventory)

            // Seed Compatibility Models (Shared phone parts)
            val compatibilities = listOf(
                CompatibilityModel(
                    partCategory = "Display (LCD Combo)",
                    groupTitle = "Realme C2 / Oppo A1k",
                    primaryBrand = "Realme / Oppo",
                    compatibleDevices = "Realme C2, Oppo A1k, Realme C1 (2019)",
                    partCodeOrType = "30-Pin IPS LCD Panel",
                    technicianNotes = "Display glass & flex connector are 100% identical. Frame molding has slight difference, clean bezel before pasting."
                ),
                CompatibilityModel(
                    partCategory = "Display (LCD Combo)",
                    groupTitle = "Vivo Y11 / Y12 / Y15 / Y17 / Y3",
                    primaryBrand = "Vivo",
                    compatibleDevices = "Vivo Y11 (2019), Vivo Y12, Vivo Y15, Vivo Y17, Vivo Y3, Vivo U10",
                    partCodeOrType = "Vivo Y-Series Universal Folder",
                    technicianNotes = "100% interchangeable drop-in replacement across all 6 models. Tested without issues."
                ),
                CompatibilityModel(
                    partCategory = "Display (LCD Combo)",
                    groupTitle = "Samsung Galaxy M21 / M30s / M31",
                    primaryBrand = "Samsung",
                    compatibleDevices = "Samsung Galaxy M21, Samsung Galaxy M30s, Samsung Galaxy M31, Galaxy M21s",
                    partCodeOrType = "FHD+ Infinity-U AMOLED Flex",
                    technicianNotes = "Same AMOLED ribbon & motherboard socket. Fits all 4 Samsung models without frame rework."
                ),
                CompatibilityModel(
                    partCategory = "Display (LCD Combo)",
                    groupTitle = "Realme 5 / 5i / 5s / Oppo A5 2020 / A9 2020",
                    primaryBrand = "Realme / Oppo",
                    compatibleDevices = "Realme 5, Realme 5i, Realme 5s, Oppo A5 2020, Oppo A9 2020",
                    partCodeOrType = "6.5\" Mini-drop HD+ Panel",
                    technicianNotes = "Identical display flex ribbon. Front glass and touch sensor match exactly."
                ),
                CompatibilityModel(
                    partCategory = "Display (LCD Combo)",
                    groupTitle = "Redmi Note 7 / Note 7 Pro / Note 7S",
                    primaryBrand = "Xiaomi",
                    compatibleDevices = "Redmi Note 7, Redmi Note 7 Pro, Redmi Note 7S",
                    partCodeOrType = "FHD+ Dot Notch Panel",
                    technicianNotes = "100% interchangeable. Both black and colored frame bezels fit seamlessly."
                ),
                CompatibilityModel(
                    partCategory = "Charging Jack / CC Board",
                    groupTitle = "Redmi 9 Power / Poco M3 CC Sub-Board",
                    primaryBrand = "Xiaomi / Poco",
                    compatibleDevices = "Redmi 9 Power, Poco M3, Redmi 9T",
                    partCodeOrType = "Type-C Sub-board w/ Fast Charge IC",
                    technicianNotes = "Direct board swap. Includes microphone, vibrator spring contacts, and Type-C port."
                ),
                CompatibilityModel(
                    partCategory = "Charging Jack / CC Board",
                    groupTitle = "Vivo Y20 / Y20i / Y12s Sub-Board",
                    primaryBrand = "Vivo",
                    compatibleDevices = "Vivo Y20, Vivo Y20i, Vivo Y20G, Vivo Y12s, Vivo Y20A",
                    partCodeOrType = "Micro-USB + 3.5mm Jack Board",
                    technicianNotes = "Full CC Board matches. Note: Test microphone on call after reassembly."
                ),
                CompatibilityModel(
                    partCategory = "Battery",
                    groupTitle = "BN59 Battery (Xiaomi)",
                    primaryBrand = "Xiaomi / Poco",
                    compatibleDevices = "Redmi Note 10, Redmi Note 10S, Poco M5s",
                    partCodeOrType = "BN59 (5000 mAh)",
                    technicianNotes = "4.45V Li-Polymer. Flex connector pin pitch and battery chamber dimensions identical."
                ),
                CompatibilityModel(
                    partCategory = "Battery",
                    groupTitle = "BLP673 Battery (Oppo / Realme)",
                    primaryBrand = "Oppo / Realme",
                    compatibleDevices = "Realme C1, Realme 2, Oppo A3s, Oppo A5",
                    partCodeOrType = "BLP673 (4230 mAh)",
                    technicianNotes = "High volume seller in India. Fits all listed budget phones."
                ),
                CompatibilityModel(
                    partCategory = "Battery",
                    groupTitle = "BLP729 Battery (Realme)",
                    primaryBrand = "Realme",
                    compatibleDevices = "Realme 5, Realme 5i, Realme 5s, Realme C3",
                    partCodeOrType = "BLP729 (5000 mAh)",
                    technicianNotes = "Matches Realme 5 series completely."
                ),
                CompatibilityModel(
                    partCategory = "Speaker / Buzzer",
                    groupTitle = "Universal Earpiece Speaker 12x6mm",
                    primaryBrand = "Realme / Oppo",
                    compatibleDevices = "Realme C2, Oppo A1k, Realme C1, Realme 2, Oppo A3s, Realme 3",
                    partCodeOrType = "SPK-1206 (12x6mm Ear Speaker)",
                    technicianNotes = "100% pin & contact compatible across Realme C-series and budget Oppo."
                ),
                CompatibilityModel(
                    partCategory = "Speaker / Buzzer",
                    groupTitle = "Loudspeaker Ringer Box (Redmi / Poco)",
                    primaryBrand = "Xiaomi / Poco",
                    compatibleDevices = "Redmi 9 Power, Poco M3, Redmi 9T",
                    partCodeOrType = "RINGER-R9P",
                    technicianNotes = "Complete bottom buzzer box with antenna contacts. Drop-in swap."
                ),
                CompatibilityModel(
                    partCategory = "Charging Jack / CC Board",
                    groupTitle = "Universal Micro-USB Jack (5-Pin SMD)",
                    primaryBrand = "Universal",
                    compatibleDevices = "Realme C2, Oppo A1k, Vivo Y11, Vivo Y12, Vivo Y15, Redmi 7A, Samsung A10",
                    partCodeOrType = "JACK-M5 (Reverse DIP)",
                    technicianNotes = "Universal 5-pin soldered port. Fits hundreds of budget Android phones."
                )
            )
            database.compatibilityDao().insertAll(compatibilities)

            // Seed Sample Repair Jobs
            val initialJobs = listOf(
                RepairJob(
                    tokenNumber = "JOB-101",
                    customerName = "Rahul Verma",
                    customerPhone = "9876543210",
                    deviceBrand = "Realme",
                    deviceModel = "Realme C2",
                    issueDescription = "Screen Display Broken (Folder Replacement)",
                    estimatedAmount = 1250.0,
                    status = RepairJob.STATUS_READY_FOR_PICKUP,
                    paymentStatus = RepairJob.PAYMENT_UNPAID
                ),
                RepairJob(
                    tokenNumber = "JOB-102",
                    customerName = "Amit Kumar",
                    customerPhone = "9812345678",
                    deviceBrand = "Redmi",
                    deviceModel = "Redmi 9 Power",
                    issueDescription = "Phone Not Charging (CC Sub-Board changed)",
                    estimatedAmount = 450.0,
                    status = RepairJob.STATUS_READY_FOR_PICKUP,
                    paymentStatus = RepairJob.PAYMENT_PAID_UPI,
                    paymentUpiRef = "429103829104"
                ),
                RepairJob(
                    tokenNumber = "JOB-103",
                    customerName = "Suman Patel",
                    customerPhone = "9988776655",
                    deviceBrand = "Vivo",
                    deviceModel = "Vivo Y12",
                    issueDescription = "Battery draining fast & Back glass loose",
                    estimatedAmount = 950.0,
                    status = RepairJob.STATUS_IN_PROGRESS,
                    paymentStatus = RepairJob.PAYMENT_UNPAID
                )
            )
            database.repairJobDao().insertAll(initialJobs)
        }
    }
}
