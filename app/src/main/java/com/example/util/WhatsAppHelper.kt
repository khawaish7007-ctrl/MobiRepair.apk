package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object WhatsAppHelper {

    fun normalizeIndianPhone(rawPhone: String): String {
        // Strip everything except digits
        var digits = rawPhone.replace(Regex("[^0-9]"), "")
        if (digits.startsWith("0")) {
            digits = digits.substring(1)
        }
        // If 10 digits (standard Indian mobile like 9876543210), prefix with 91
        if (digits.length == 10) {
            digits = "91$digits"
        }
        return digits
    }

    fun openWhatsAppMessage(context: Context, rawPhone: String, message: String): Boolean {
        val cleanPhone = normalizeIndianPhone(rawPhone)
        val encodedMsg = URLEncoder.encode(message, StandardCharsets.UTF_8.name())
        val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMsg"

        // Try launching WhatsApp explicitly first
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            // First attempt: regular WhatsApp
            intent.setPackage("com.whatsapp")
            context.startActivity(intent)
            true
        } catch (e1: Exception) {
            try {
                // Second attempt: WhatsApp Business
                intent.setPackage("com.whatsapp.w4b")
                context.startActivity(intent)
                true
            } catch (e2: Exception) {
                try {
                    // Third attempt: Generic browser / deep link
                    val genericIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(genericIntent)
                    true
                } catch (e3: Exception) {
                    // Fallback: SMS
                    openSmsFallback(context, cleanPhone, message)
                    false
                }
            }
        }
    }

    fun openSmsFallback(context: Context, rawPhone: String, message: String) {
        try {
            val cleanPhone = normalizeIndianPhone(rawPhone)
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$cleanPhone")
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(smsIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open messaging app: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
