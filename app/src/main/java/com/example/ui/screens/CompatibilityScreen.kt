package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MobileScreenShare
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CompatibilityModel
import com.example.data.model.InventoryItem
import com.example.localization.AppStrings
import com.example.util.UpiHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CompatibilityScreen(
    compatibilities: List<CompatibilityModel>,
    inventoryItems: List<InventoryItem>,
    searchQuery: String,
    strings: AppStrings,
    onSearchChange: (String) -> Unit
) {
    var selectedModel by remember { mutableStateOf<String?>("Realme C2") }
    var selectedBrandFilter by remember { mutableStateOf("ALL") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    // Popular mobile models list extracted dynamically + common workshop models
    val allKnownModels = remember(compatibilities) {
        val modelsSet = linkedSetOf(
            "Realme C2",
            "Redmi 9 Power",
            "Vivo Y11",
            "Vivo Y12",
            "Vivo Y20",
            "Samsung Galaxy M21",
            "Redmi Note 10",
            "Realme 5",
            "Oppo A1k",
            "Poco M3",
            "Oppo A3s",
            "Redmi Note 7"
        )
        compatibilities.forEach { item ->
            item.compatibleDevices.split(",").forEach { dev ->
                val clean = dev.trim()
                if (clean.isNotBlank()) modelsSet.add(clean)
            }
        }
        modelsSet.toList()
    }

    val brands = listOf(
        "ALL" to "All Brands",
        "Realme" to "Realme",
        "Redmi" to "Xiaomi / Redmi",
        "Vivo" to "Vivo",
        "Samsung" to "Samsung",
        "Oppo" to "Oppo"
    )

    val partCategories = listOf(
        "ALL" to "All Parts",
        "Display" to "LCD Folders",
        "Charging" to "Charging Ports & CC",
        "Speaker" to "Speakers & Buzzers",
        "Battery" to "Batteries"
    )

    // Filter models by brand and search query
    val displayedModels = remember(allKnownModels, selectedBrandFilter, searchQuery) {
        allKnownModels.filter { model ->
            val matchesBrand = if (selectedBrandFilter == "ALL") true else model.contains(selectedBrandFilter, ignoreCase = true)
            val matchesSearch = if (searchQuery.isBlank()) true else model.contains(searchQuery, ignoreCase = true)
            matchesBrand && matchesSearch
        }
    }

    // Interchangeable parts for the selected model
    val selectedModelParts = remember(selectedModel, compatibilities, selectedCategoryFilter) {
        if (selectedModel == null) emptyList()
        else {
            compatibilities.filter { item ->
                val matchesModel = item.compatibleDevices.contains(selectedModel!!, ignoreCase = true) ||
                        item.groupTitle.contains(selectedModel!!, ignoreCase = true)
                val matchesCategory = if (selectedCategoryFilter == "ALL") true
                else item.partCategory.contains(selectedCategoryFilter, ignoreCase = true)
                matchesModel && matchesCategory
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("compatibility_matrix_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Compatibility Matrix",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Select any mobile model to see all interchangeable parts & live shop stock.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("compatibility_search_field"),
                placeholder = { Text("Search mobile model (e.g. C2, Note 9, Y20, M21)...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Brand Selector Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                brands.forEach { (brandKey, label) ->
                    FilterChip(
                        selected = selectedBrandFilter == brandKey,
                        onClick = { selectedBrandFilter = brandKey },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        // Mobile Model Quick-Select Carousel
        item {
            Column {
                Text(
                    text = "Select Mobile Model",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    displayedModels.forEach { modelName ->
                        val isSelected = selectedModel.equals(modelName, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedModel = if (isSelected) null else modelName
                                }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = modelName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category Filter Chips (Charging ports, LCDs, Speakers, Batteries)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                partCategories.forEach { (catKey, label) ->
                    FilterChip(
                        selected = selectedCategoryFilter == catKey,
                        onClick = { selectedCategoryFilter = catKey },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        // Selected Model Header & Stats Banner
        if (selectedModel != null) {
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = selectedModel!!,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Interchangeable Parts Matrix",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${selectedModelParts.size} Shared Groups",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // List of Interchangeable Parts for the Selected Model
            if (selectedModelParts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No compatible parts recorded for $selectedModel in this category.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(selectedModelParts, key = { it.id }) { compatModel ->
                    CompatibilityMatrixCard(
                        model = compatModel,
                        inventoryItems = inventoryItems,
                        currentSelectedPhone = selectedModel!!
                    )
                }
            }
        } else {
            // General Browse Mode when no single phone is selected
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Shared Part Groups (${compatibilities.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val generalList = compatibilities.filter { item ->
                if (selectedCategoryFilter == "ALL") true
                else item.partCategory.contains(selectedCategoryFilter, ignoreCase = true)
            }

            items(generalList, key = { it.id }) { compatModel ->
                CompatibilityMatrixCard(
                    model = compatModel,
                    inventoryItems = inventoryItems,
                    currentSelectedPhone = null
                )
            }
        }
    }
}

/**
 * A dedicated Card displaying:
 * - Part Category & Specifications
 * - Interchangeable Models (highlighting the selected phone)
 * - LIVE Room Database Inventory Status (Quantity in stock, Cost & Selling Price)
 * - Technician fitment notes
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompatibilityMatrixCard(
    model: CompatibilityModel,
    inventoryItems: List<InventoryItem>,
    currentSelectedPhone: String?
) {
    val categoryIcon = getCategoryIcon(model.partCategory)
    val categoryColor = getCategoryColor(model.partCategory)

    // Pull from existing Room inventory database matching partCode, name, or compatibleModels
    val matchingInventory = inventoryItems.filter { item ->
        val codeMatch = item.partCode.isNotBlank() && model.partCodeOrType.contains(item.partCode, ignoreCase = true)
        val nameMatch = model.groupTitle.contains(item.name.take(12), ignoreCase = true) ||
                item.name.contains(model.groupTitle.take(12), ignoreCase = true)
        val deviceOverlap = model.compatibleDevices.split(",").any { dev ->
            item.compatibleModels.contains(dev.trim(), ignoreCase = true)
        }
        codeMatch || nameMatch || deviceOverlap
    }

    val totalStock = matchingInventory.sumOf { it.quantity }
    val bestInventoryItem = matchingInventory.firstOrNull()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("matrix_part_card_${model.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Category Badge & Live Inventory Stock Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = categoryIcon, contentDescription = null, tint = categoryColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = model.partCategory,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                        Text(
                            text = model.groupTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Live Room Stock Badge
                if (matchingInventory.isNotEmpty()) {
                    val (badgeBg, badgeText, badgeColor) = when {
                        totalStock <= 0 -> Triple(Color(0xFFFEE2E2), "0 In Stock", Color(0xFFDC2626))
                        totalStock == 1 -> Triple(Color(0xFFFEF3C7), "1 Left (Low)", Color(0xFFB45309))
                        else -> Triple(Color(0xFFD1FAE5), "$totalStock In Stock", Color(0xFF047857))
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (totalStock > 0) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Not in Stock",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Part Code or IC Type
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Part Spec / Code: ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = model.partCodeOrType,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Interchangeable Phone Models FlowRow
            Text(
                text = "Interchangeable With Sibling Models:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val devices = model.compatibleDevices.split(",")
                devices.forEach { dev ->
                    val cleanDev = dev.trim()
                    val isTargetPhone = currentSelectedPhone != null && cleanDev.contains(currentSelectedPhone, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isTargetPhone) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isTargetPhone) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Text(
                            text = cleanDev,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isTargetPhone) FontWeight.Bold else FontWeight.Normal,
                            color = if (isTargetPhone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Live Inventory Price & Stock Details Box
            if (bestInventoryItem != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = Color(0xFF047857),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = bestInventoryItem.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "Code: ${bestInventoryItem.partCode} • Cost: ${UpiHelper.formatInr(bestInventoryItem.costPrice)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = UpiHelper.formatInr(bestInventoryItem.sellingPrice),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                            Text(
                                text = "Selling Price",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Technician Fitment Notes
            if (model.technicianNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFFBEB))
                        .padding(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = model.technicianNotes,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF92400E)
                    )
                }
            }
        }
    }
}

private fun getCategoryIcon(category: String): ImageVector {
    return when {
        category.contains("Display", ignoreCase = true) || category.contains("LCD", ignoreCase = true) ->
            Icons.AutoMirrored.Filled.MobileScreenShare
        category.contains("Charging", ignoreCase = true) || category.contains("Jack", ignoreCase = true) ->
            Icons.Default.Cable
        category.contains("Battery", ignoreCase = true) ->
            Icons.Default.BatteryChargingFull
        category.contains("Speaker", ignoreCase = true) || category.contains("Buzzer", ignoreCase = true) ->
            Icons.Default.VolumeUp
        else ->
            Icons.Default.PhoneAndroid
    }
}

private fun getCategoryColor(category: String): Color {
    return when {
        category.contains("Display", ignoreCase = true) || category.contains("LCD", ignoreCase = true) ->
            Color(0xFF1E40AF) // Deep Blue
        category.contains("Charging", ignoreCase = true) ->
            Color(0xFFD97706) // Amber
        category.contains("Battery", ignoreCase = true) ->
            Color(0xFF047857) // Green
        category.contains("Speaker", ignoreCase = true) || category.contains("Buzzer", ignoreCase = true) ->
            Color(0xFF7C3AED) // Purple
        else ->
            Color(0xFF475569) // Slate
    }
}
