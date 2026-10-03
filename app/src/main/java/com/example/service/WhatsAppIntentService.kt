package com.example.service

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.example.data.model.RepairJob
import com.example.data.model.ShopProfile
import com.example.localization.AppLanguage
import com.example.util.UpiHelper
import com.example.util.WhatsAppHelper
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class WhatsAppDispatchResult {
    data class Success(val clientName: String, val message: String) : WhatsAppDispatchResult()
    data class Failed(val reason: String) : WhatsAppDispatchResult()
}

object WhatsAppIntentService {

    private const val PACKAGE_WHATSAPP = "com.whatsapp"
    private const val PACKAGE_WHATSAPP_BUSINESS = "com.whatsapp.w4b"

    /**
     * Dispatches a repair status update intent to the customer's WhatsApp
     */
    fun sendRepairStatusUpdate(
        context: Context,
        job: RepairJob,
        shop: ShopProfile,
        language: AppLanguage
    ): WhatsAppDispatchResult {
        val message = buildRepairStatusMessage(job, shop, language)
        return triggerWhatsAppIntent(context, job.customerPhone, message)
    }

    /**
     * Dispatches a formal payment receipt & invoice to the customer's WhatsApp
     */
    fun sendPaymentReceipt(
        context: Context,
        job: RepairJob,
        shop: ShopProfile,
        language: AppLanguage,
        customTxnId: String? = null
    ): WhatsAppDispatchResult {
        val ref = customTxnId?.ifBlank { null } ?: job.paymentUpiRef.ifBlank { "UPI-${System.currentTimeMillis() % 100000}" }
        val message = buildPaymentReceiptMessage(job, shop, language, ref)
        return triggerWhatsAppIntent(context, job.customerPhone, message)
    }

    /**
     * Dispatches a quick billing receipt for on-the-spot walk-in customer repairs
     */
    fun sendQuickBillReceipt(
        context: Context,
        customerPhone: String,
        customerName: String,
        deviceModel: String,
        serviceNote: String,
        amount: Double,
        upiRef: String?,
        shop: ShopProfile,
        language: AppLanguage
    ): WhatsAppDispatchResult {
        val message = buildQuickReceiptMessage(
            customerName = customerName,
            deviceModel = deviceModel,
            serviceNote = serviceNote,
            amount = amount,
            upiRef = upiRef,
            shop = shop,
            language = language
        )
        return triggerWhatsAppIntent(context, customerPhone, message)
    }

    /**
     * Low-level intent dispatcher:
     * 1. Checks if regular WhatsApp is installed -> launches with package
     * 2. Checks if WhatsApp Business is installed -> launches with package
     * 3. Falls back to generic Intent.ACTION_VIEW browser deep link
     * 4. Falls back to SMS intent if no web handler is available
     */
    fun triggerWhatsAppIntent(
        context: Context,
        rawPhone: String,
        messageText: String
    ): WhatsAppDispatchResult {
        if (rawPhone.isBlank()) {
            val err = "Customer mobile number is empty"
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            return WhatsAppDispatchResult.Failed(err)
        }

        val cleanPhone = WhatsAppHelper.normalizeIndianPhone(rawPhone)
        val encodedText = try {
            URLEncoder.encode(messageText, StandardCharsets.UTF_8.name())
        } catch (e: Exception) {
            messageText
        }

        val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedText"
        val pm = context.packageManager

        // Attempt 1: Regular WhatsApp
        if (isPackageInstalled(pm, PACKAGE_WHATSAPP)) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage(PACKAGE_WHATSAPP)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return try {
                context.startActivity(intent)
                WhatsAppDispatchResult.Success("WhatsApp", messageText)
            } catch (e: Exception) {
                fallbackToGeneric(context, cleanPhone, url, messageText)
            }
        }

        // Attempt 2: WhatsApp Business
        if (isPackageInstalled(pm, PACKAGE_WHATSAPP_BUSINESS)) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage(PACKAGE_WHATSAPP_BUSINESS)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return try {
                context.startActivity(intent)
                WhatsAppDispatchResult.Success("WhatsApp Business", messageText)
            } catch (e: Exception) {
                fallbackToGeneric(context, cleanPhone, url, messageText)
            }
        }

        // Attempt 3: Generic browser URL deep link or SMS fallback
        return fallbackToGeneric(context, cleanPhone, url, messageText)
    }

    private fun fallbackToGeneric(
        context: Context,
        cleanPhone: String,
        url: String,
        messageText: String
    ): WhatsAppDispatchResult {
        val genericIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(genericIntent)
            WhatsAppDispatchResult.Success("Web Link", messageText)
        } catch (e: Exception) {
            // Attempt 4: SMS Fallback
            try {
                val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$cleanPhone")).apply {
                    putExtra("sms_body", messageText)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(smsIntent)
                WhatsAppDispatchResult.Success("SMS Fallback", messageText)
            } catch (smsEx: Exception) {
                val failMsg = "Unable to open messaging app: ${smsEx.message}"
                Toast.makeText(context, failMsg, Toast.LENGTH_SHORT).show()
                WhatsAppDispatchResult.Failed(failMsg)
            }
        }
    }

    private fun isPackageInstalled(pm: PackageManager, packageName: String): Boolean {
        return try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    // ==========================================
    // Message Formatters & Templates
    // ==========================================

    private fun buildRepairStatusMessage(
        job: RepairJob,
        shop: ShopProfile,
        language: AppLanguage
    ): String {
        val shopName = shop.shopName.ifBlank { "Mobile Repair Care" }
        val custName = job.customerName.ifBlank { "Customer" }
        val amountStr = UpiHelper.formatInr(job.estimatedAmount)
        val statusHeader = when (job.status) {
            RepairJob.STATUS_READY_FOR_PICKUP -> "✅ REPAIR COMPLETED (Ready for Pickup)"
            RepairJob.STATUS_IN_PROGRESS -> "⏳ WORK IN PROGRESS"
            RepairJob.STATUS_DELIVERED -> "🎉 DELIVERED & COMPLETED"
            else -> "📋 REPAIR TICKET REGISTERED"
        }

        return when (language) {
            AppLanguage.HI -> {
                val statusTextHi = when (job.status) {
                    RepairJob.STATUS_READY_FOR_PICKUP -> "आपके मोबाइल का रिपेयरिंग कार्य सफलतापूर्वक पूरा हो चुका है और फोन तैयार है।"
                    RepairJob.STATUS_IN_PROGRESS -> "आपके मोबाइल का काम चालू है, जल्द ही तैयार हो जाएगा।"
                    RepairJob.STATUS_DELIVERED -> "आपका मोबाइल सफलतापूर्वक डिलीवर कर दिया गया है। हमारे साथ जुड़ने के लिए धन्यवाद!"
                    else -> "आपका मोबाइल रिपेयरिंग के लिए दर्ज कर लिया गया है।"
                }
                """
                📱 *${shopName}*
                📍 *${statusHeader}*
                
                नमस्ते ${custName} जी,
                ${statusTextHi}
                
                🔖 *टोकन / जॉब आईडी:* ${job.tokenNumber}
                📲 *डिवाइस मॉडल:* ${job.deviceBrand} ${job.deviceModel}
                🔧 *समस्या / पार्ट्स:* ${job.issueDescription}
                💰 *बिल राशि:* ${amountStr}
                
                ${if (shop.address.isNotBlank()) "🏠 *दुकान का पता:* ${shop.address}\n" else ""}📞 *संपर्क:* ${shop.ownerPhone}
                
                धन्यवाद! 🙏
                *${shopName}*
                """.trimIndent()
            }
            AppLanguage.HINGLISH -> {
                val statusTextHing = when (job.status) {
                    RepairJob.STATUS_READY_FOR_PICKUP -> "Aapke mobile ki repairing complete ho chuki hai aur phone pick-up ke liye ready hai."
                    RepairJob.STATUS_IN_PROGRESS -> "Aapke mobile ka repairing work chal raha hai, jaldi hi ready ho jayega."
                    RepairJob.STATUS_DELIVERED -> "Aapka phone handover kar diya gaya hai. Thank you!"
                    else -> "Aapka phone repairing ke liye receive ho chuka hai."
                }
                """
                📱 *${shopName}*
                📍 *${statusHeader}*
                
                Namaste ${custName} ji,
                ${statusTextHing}
                
                🔖 *Job Token:* ${job.tokenNumber}
                📲 *Model:* ${job.deviceBrand} ${job.deviceModel}
                🔧 *Work / Problem:* ${job.issueDescription}
                💰 *Amount:* ${amountStr}
                
                ${if (shop.address.isNotBlank()) "🏠 *Shop Address:* ${shop.address}\n" else ""}📞 *Call/Help:* ${shop.ownerPhone}
                
                Thank you! 🙏
                *${shopName}*
                """.trimIndent()
            }
            AppLanguage.EN -> {
                """
                📱 *${shopName}*
                📍 *${statusHeader}*
                
                Dear ${custName},
                Your mobile device repair update:
                
                🔖 *Job Token:* ${job.tokenNumber}
                📲 *Device:* ${job.deviceBrand} ${job.deviceModel}
                🔧 *Service / Issue:* ${job.issueDescription}
                💰 *Total Bill:* ${amountStr}
                
                ${if (job.status == RepairJob.STATUS_READY_FOR_PICKUP) "✅ Your phone is tested and ready for pickup!\n" else ""}${if (shop.address.isNotBlank()) "🏠 *Shop:* ${shop.address}\n" else ""}📞 *Contact:* ${shop.ownerPhone}
                
                Thank you for your visit!
                *${shopName}*
                """.trimIndent()
            }
        }
    }

    private fun buildPaymentReceiptMessage(
        job: RepairJob,
        shop: ShopProfile,
        language: AppLanguage,
        upiRef: String
    ): String {
        val shopName = shop.shopName.ifBlank { "Mobile Repair Care" }
        val custName = job.customerName.ifBlank { "Customer" }
        val amountStr = UpiHelper.formatInr(job.estimatedAmount)
        val currentDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date())

        return when (language) {
            AppLanguage.HI -> {
                """
                🧾 *डिजिटल पेमेंट रसीद (PAYMENT RECEIPT)*
                🏪 *${shopName}*
                ───────────────────────
                👤 *ग्राहक:* ${custName}
                📱 *मोबाइल:* ${job.deviceBrand} ${job.deviceModel}
                🔖 *जॉब आईडी:* ${job.tokenNumber}
                🔧 *काम:* ${job.issueDescription}
                
                💵 *भुगतान राशि:* ${amountStr}
                ✅ *भुगतान स्थिति:* PAID (सफलतापूर्वक प्राप्त)
                💳 *UPI रेफरेंस / UTR:* ${upiRef}
                📅 *दिनांक व समय:* ${currentDate}
                ───────────────────────
                🙏 आपके विश्वास के लिए धन्यवाद!
                ${if (shop.address.isNotBlank()) "📍 ${shop.address}\n" else ""}📞 हेल्पलाइन: ${shop.ownerPhone}
                """.trimIndent()
            }
            AppLanguage.HINGLISH -> {
                """
                🧾 *PAYMENT RECEIPT / BILL*
                🏪 *${shopName}*
                ───────────────────────
                👤 *Customer:* ${custName}
                📱 *Device:* ${job.deviceBrand} ${job.deviceModel}
                🔖 *Job Token:* ${job.tokenNumber}
                🔧 *Work Done:* ${job.issueDescription}
                
                💵 *Total Paid:* ${amountStr}
                ✅ *Payment Mode:* Paid via UPI
                💳 *UPI Txn / UTR:* ${upiRef}
                📅 *Date & Time:* ${currentDate}
                ───────────────────────
                🙏 Shukriya! Phone collect karne ke liye shop visit karein.
                ${if (shop.address.isNotBlank()) "📍 ${shop.address}\n" else ""}📞 Contact: ${shop.ownerPhone}
                """.trimIndent()
            }
            AppLanguage.EN -> {
                """
                🧾 *OFFICIAL PAYMENT RECEIPT*
                🏪 *${shopName}*
                ───────────────────────
                👤 *Customer Name:* ${custName}
                📱 *Device:* ${job.deviceBrand} ${job.deviceModel}
                🔖 *Ticket #:* ${job.tokenNumber}
                🔧 *Service:* ${job.issueDescription}
                
                💵 *Amount Paid:* ${amountStr}
                ✅ *Payment Status:* PAID (Verified)
                💳 *UPI Ref / UTR:* ${upiRef}
                📅 *Date & Time:* ${currentDate}
                ───────────────────────
                Thank you for choosing ${shopName}!
                ${if (shop.address.isNotBlank()) "📍 ${shop.address}\n" else ""}📞 Support: ${shop.ownerPhone}
                """.trimIndent()
            }
        }
    }

    private fun buildQuickReceiptMessage(
        customerName: String,
        deviceModel: String,
        serviceNote: String,
        amount: Double,
        upiRef: String?,
        shop: ShopProfile,
        language: AppLanguage
    ): String {
        val shopName = shop.shopName.ifBlank { "Mobile Shop" }
        val cust = customerName.ifBlank { "Customer" }
        val amountStr = UpiHelper.formatInr(amount)
        val ref = upiRef?.ifBlank { null } ?: "UPI-${System.currentTimeMillis() % 100000}"
        val currentDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date())

        return """
        🧾 *${shopName} — PAYMENT RECEIPT*
        ───────────────────────
        👤 *Customer:* ${cust}
        📱 *Item / Service:* ${deviceModel.ifBlank { "Mobile Service" }}
        🔧 *Details:* ${serviceNote.ifBlank { "Service & Spares" }}
        
        💰 *Amount Paid:* ${amountStr}
        💳 *UPI Txn ID:* ${ref}
        📅 *Date:* ${currentDate}
        ───────────────────────
        Thank you for shopping with us! 🙏
        ${if (shop.address.isNotBlank()) "📍 ${shop.address}\n" else ""}📞 Contact: ${shop.ownerPhone}
        """.trimIndent()
    }
}
