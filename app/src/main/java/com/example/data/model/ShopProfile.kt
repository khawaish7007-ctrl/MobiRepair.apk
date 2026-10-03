package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_profile")
data class ShopProfile(
    @PrimaryKey val id: Int = 1,
    val shopName: String = "Bharat Mobile Care & Spares",
    val ownerName: String = "Rajesh Sharma",
    val upiId: String = "rajeshsharma@okaxis",
    val ownerPhone: String = "9876543210",
    val address: String = "Main Market, Near Clock Tower",
    val selectedLanguage: String = "EN",
    val enableWhatsAppAutoPrompt: Boolean = true
)
