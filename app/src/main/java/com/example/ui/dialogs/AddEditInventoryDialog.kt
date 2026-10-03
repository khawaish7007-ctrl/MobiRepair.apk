package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.InventoryItem
import com.example.localization.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditInventoryDialog(
    item: InventoryItem?,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        category: String,
        quantity: Int,
        costPrice: Double,
        sellingPrice: Double,
        minAlertQty: Int,
        compatibleModels: String,
        partCode: String
    ) -> Unit
) {
    val context = LocalContext.current
    val isEditing = item != null

    var name by remember { mutableStateOf(item?.name ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "Folder (Display)") }
    var quantityText by remember { mutableStateOf(item?.quantity?.toString() ?: "5") }
    var costPriceText by remember { mutableStateOf(item?.costPrice?.toInt()?.toString() ?: "500") }
    var sellingPriceText by remember { mutableStateOf(item?.sellingPrice?.toInt()?.toString() ?: "1000") }
    var minAlertText by remember { mutableStateOf(item?.minAlertQuantity?.toString() ?: "2") }
    var compatibleModels by remember { mutableStateOf(item?.compatibleModels ?: "") }
    var partCode by remember { mutableStateOf(item?.partCode ?: "") }

    val categories = listOf(
        "Folder (Display)",
        "Charging CC Board",
        "Charging Jack",
        "Battery",
        "Touch Glass",
        "Back Panel",
        "Camera Glass",
        "Repair Tools / Glue"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("add_edit_inventory_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) strings.edit else strings.addItem,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = strings.close)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Item Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Part Name") },
                    placeholder = { Text("e.g. Realme C2 / Oppo A1k Combo OG") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inventory_item_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips
                Text(
                    text = strings.categoryFilter,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quantity & Alert Threshold
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it.filter { c -> c.isDigit() } },
                        label = { Text("Stock Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inventory_quantity_input")
                    )

                    OutlinedTextField(
                        value = minAlertText,
                        onValueChange = { minAlertText = it.filter { c -> c.isDigit() } },
                        label = { Text(strings.minAlertQty) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inventory_alert_qty_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cost & Selling Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = costPriceText,
                        onValueChange = { costPriceText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(strings.costPrice) },
                        prefix = { Text("₹ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inventory_cost_price_input")
                    )

                    OutlinedTextField(
                        value = sellingPriceText,
                        onValueChange = { sellingPriceText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(strings.sellingPrice) },
                        prefix = { Text("₹ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inventory_selling_price_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Compatible models (Essential for part identification)
                OutlinedTextField(
                    value = compatibleModels,
                    onValueChange = { compatibleModels = it },
                    label = { Text(strings.compatibleModels) },
                    placeholder = { Text("e.g. Realme C2, Oppo A1k, Realme C1") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inventory_compatible_models_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = partCode,
                    onValueChange = { partCode = it },
                    label = { Text("Part Code / Model (optional)") },
                    placeholder = { Text("e.g. BLP673, BN59, TC-16") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (name.isBlank()) {
                            Toast.makeText(context, "Please enter part name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val qty = quantityText.toIntOrNull() ?: 0
                        val cp = costPriceText.toDoubleOrNull() ?: 0.0
                        val sp = sellingPriceText.toDoubleOrNull() ?: 0.0
                        val minAlert = minAlertText.toIntOrNull() ?: 2

                        onSave(
                            name,
                            category,
                            qty,
                            cp,
                            sp,
                            minAlert,
                            compatibleModels,
                            partCode
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_inventory_button")
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = strings.save, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
