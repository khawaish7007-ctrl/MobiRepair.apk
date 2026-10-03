package com.example.util

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.NumberFormat
import java.util.Locale

object UpiHelper {
    /**
     * Builds standard NPCI UPI URI string for dynamic QR code generation
     */
    fun buildUpiUri(
        upiId: String,
        shopName: String,
        amount: Double,
        transactionNote: String,
        referenceId: String = ""
    ): String {
        val cleanUpi = upiId.trim()
        val cleanShop = if (shopName.isNotBlank()) shopName.trim() else "Mobile Shop"
        val formattedAmount = String.format(Locale.US, "%.2f", amount)

        val encodedShop = URLEncoder.encode(cleanShop, StandardCharsets.UTF_8.name())
        val encodedNote = URLEncoder.encode(transactionNote.trim(), StandardCharsets.UTF_8.name())

        var uri = "upi://pay?pa=$cleanUpi&pn=$encodedShop&am=$formattedAmount&cu=INR&tn=$encodedNote"
        if (referenceId.isNotBlank()) {
            val encodedRef = URLEncoder.encode(referenceId.trim(), StandardCharsets.UTF_8.name())
            uri += "&tr=$encodedRef"
        }
        return uri
    }

    fun isValidUpiId(upiId: String): Boolean {
        val trimmed = upiId.trim()
        return trimmed.contains("@") && trimmed.length >= 5 && !trimmed.contains(" ")
    }

    fun formatInr(amount: Double): String {
        return "₹" + String.format(Locale.US, "%,.0f", amount)
    }
}
