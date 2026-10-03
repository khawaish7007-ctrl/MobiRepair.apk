package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val quantity: Int,
    val costPrice: Double,
    val sellingPrice: Double,
    val minAlertQuantity: Int = 2,
    val compatibleModels: String = "",
    val partCode: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = quantity in 1..minAlertQuantity

    val isOutOfStock: Boolean
        get() = quantity <= 0

    val potentialProfit: Double
        get() = (sellingPrice - costPrice) * quantity.coerceAtLeast(0)
}
