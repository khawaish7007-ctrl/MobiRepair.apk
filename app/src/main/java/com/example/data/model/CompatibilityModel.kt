package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "compatibility_models")
data class CompatibilityModel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partCategory: String, // "Display Combo", "Charging CC Board / Jack", "Battery", "Back Glass"
    val groupTitle: String, // e.g. "Realme C2 / Oppo A1k"
    val primaryBrand: String, // e.g. "Realme / Oppo"
    val compatibleDevices: String, // e.g. "Realme C2, Oppo A1k, Realme C1"
    val partCodeOrType: String, // e.g. "30-pin FHD / BLP673 / Type-C 16-pin"
    val technicianNotes: String, // e.g. "Display is 100% same. CC board requires mic test."
    val isCustomAdded: Boolean = false
)
