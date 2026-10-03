package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repair_jobs")
data class RepairJob(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tokenNumber: String,
    val customerName: String,
    val customerPhone: String,
    val deviceBrand: String,
    val deviceModel: String,
    val issueDescription: String,
    val estimatedAmount: Double,
    val status: String = STATUS_PENDING,
    val paymentStatus: String = PAYMENT_UNPAID,
    val paymentUpiRef: String = "",
    val inventoryItemIdUsed: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val deliveredAt: Long? = null
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_IN_PROGRESS = "IN_PROGRESS"
        const val STATUS_READY_FOR_PICKUP = "READY_FOR_PICKUP"
        const val STATUS_DELIVERED = "DELIVERED"

        const val PAYMENT_UNPAID = "UNPAID"
        const val PAYMENT_PAID_UPI = "PAID_UPI"
        const val PAYMENT_PAID_CASH = "PAID_CASH"
    }

    val isPaid: Boolean
        get() = paymentStatus == PAYMENT_PAID_UPI || paymentStatus == PAYMENT_PAID_CASH

    val isReadyOrDelivered: Boolean
        get() = status == STATUS_READY_FOR_PICKUP || status == STATUS_DELIVERED
}
