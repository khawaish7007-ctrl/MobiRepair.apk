package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.InventoryItem
import com.example.data.model.RepairJob
import com.example.localization.AppLanguage
import com.example.localization.LocalizationManager
import com.example.util.QrCodeGenerator
import com.example.util.UpiHelper
import com.example.util.WhatsAppHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MobiRepair", appName)
    }

    @Test
    fun testUpiUriGeneration() {
        val uri = UpiHelper.buildUpiUri(
            upiId = "sharmamobile@okaxis",
            shopName = "Sharma Mobile Care",
            amount = 1250.0,
            transactionNote = "JOB-101 Display Repair",
            referenceId = "JOB-101"
        )
        assertTrue(uri.startsWith("upi://pay?pa=sharmamobile@okaxis"))
        assertTrue(uri.contains("am=1250.00"))
        assertTrue(uri.contains("cu=INR"))
    }

    @Test
    fun testUpiQrBitmapGeneration() {
        val uri = "upi://pay?pa=sharmamobile@okaxis&pn=Sharma+Mobile&am=500.00&cu=INR"
        val bitmap = QrCodeGenerator.generateQrBitmap(uri, 128)
        assertNotNull(bitmap)
        assertEquals(128, bitmap?.width)
        assertEquals(128, bitmap?.height)
    }

    @Test
    fun testWhatsAppNumberNormalization() {
        assertEquals("919876543210", WhatsAppHelper.normalizeIndianPhone("9876543210"))
        assertEquals("919876543210", WhatsAppHelper.normalizeIndianPhone("+91 98765-43210"))
        assertEquals("919876543210", WhatsAppHelper.normalizeIndianPhone("09876543210"))
    }

    @Test
    fun testLocalizationMessages() {
        val msgHi = LocalizationManager.buildWhatsAppMessage(
            language = AppLanguage.HI,
            shopName = "Sharma Mobile",
            customerName = "Rahul",
            deviceModel = "Realme C2",
            issue = "Display Folder",
            amount = 1250.0,
            isPaid = true,
            txnId = "UTR428190"
        )
        assertTrue(msgHi.contains("नमस्ते Rahul जी"))
        assertTrue(msgHi.contains("Realme C2"))
        assertTrue(msgHi.contains("UTR428190"))

        val msgEn = LocalizationManager.buildWhatsAppMessage(
            language = AppLanguage.EN,
            shopName = "Sharma Mobile",
            customerName = "Rahul",
            deviceModel = "Realme C2",
            issue = "Display Folder",
            amount = 1250.0,
            isPaid = false,
            txnId = null
        )
        assertTrue(msgEn.contains("Dear Rahul"))
        assertTrue(msgEn.contains("COMPLETED"))
    }

    @Test
    fun testInventoryStockAlerts() {
        val itemLow = InventoryItem(
            name = "Test Item",
            category = "Folder",
            quantity = 1,
            costPrice = 100.0,
            sellingPrice = 200.0,
            minAlertQuantity = 2
        )
        assertTrue(itemLow.isLowStock)

        val itemOut = itemLow.copy(quantity = 0)
        assertTrue(itemOut.isOutOfStock)
    }

    @Test
    fun testFirestoreSyncManagerSanitization() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(context, com.example.data.AppDatabase::class.java).build()
        val syncManager = com.example.data.sync.FirestoreSyncManager(context, db.inventoryDao())
        
        assertEquals("bharat_mobile_care", syncManager.sanitizeShopId("Bharat Mobile Care!"))
        assertEquals("sharma_upi", syncManager.sanitizeShopId("sharma@upi"))
        assertEquals("main_shop", syncManager.sanitizeShopId("   "))
        
        // Without google-services.json in test environment, isFirebaseAvailable returns false gracefully
        val available = syncManager.isFirebaseAvailable()
        // Graceful handling without throwing exceptions
        assertNotNull(available)
        db.close()
    }

    @Test
    fun testWhatsAppIntentServiceReceiptGeneration() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shop = com.example.data.model.ShopProfile(
            shopName = "Bharat Mobile Hub",
            upiId = "bharat@upi",
            ownerPhone = "9876543210"
        )
        val job = com.example.data.model.RepairJob(
            tokenNumber = "JOB-777",
            customerName = "Vikram Singh",
            customerPhone = "9876543210",
            deviceBrand = "Realme",
            deviceModel = "Realme C2",
            issueDescription = "Folder Screen Replacement",
            estimatedAmount = 1450.0,
            status = com.example.data.model.RepairJob.STATUS_READY_FOR_PICKUP,
            paymentStatus = com.example.data.model.RepairJob.PAYMENT_PAID_UPI,
            paymentUpiRef = "UTR99887766"
        )

        val resultReceipt = com.example.service.WhatsAppIntentService.sendPaymentReceipt(
            context = context,
            job = job,
            shop = shop,
            language = AppLanguage.EN,
            customTxnId = "UTR99887766"
        )
        assertTrue(resultReceipt is com.example.service.WhatsAppDispatchResult.Success)
        val msg = (resultReceipt as com.example.service.WhatsAppDispatchResult.Success).message
        assertTrue(msg.contains("OFFICIAL PAYMENT RECEIPT"))
        assertTrue(msg.contains("Vikram Singh"))
        assertTrue(msg.contains("₹1,450"))
        assertTrue(msg.contains("UTR99887766"))

        // Status update test
        val resultStatus = com.example.service.WhatsAppIntentService.sendRepairStatusUpdate(
            context = context,
            job = job,
            shop = shop,
            language = AppLanguage.HI
        )
        assertTrue(resultStatus is com.example.service.WhatsAppDispatchResult.Success)
        val msgHi = (resultStatus as com.example.service.WhatsAppDispatchResult.Success).message
        assertTrue(msgHi.contains("Vikram Singh"))
        assertTrue(msgHi.contains("JOB-777"))
        assertTrue(msgHi.contains("Realme C2"))
    }
}
