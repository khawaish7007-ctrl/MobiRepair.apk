package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.RepairJob
import com.example.localization.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRepairDialog(
    job: RepairJob?,
    inventoryItems: List<InventoryItem>,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onSave: (
        customerName: String,
        customerPhone: String,
        deviceBrand: String,
        deviceModel: String,
        issueDescription: String,
        estimatedAmount: Double,
        status: String,
        paymentStatus: String,
        paymentUpiRef: String,
        deductInventoryItemId: Long?
    ) -> Unit
) {
    val context = LocalContext.current
    val isEditing = job != null

    var customerName by remember { mutableStateOf(job?.customerName ?: "") }
    var customerPhone by remember { mutableStateOf(job?.customerPhone ?: "") }
    var deviceBrand by remember { mutableStateOf(job?.deviceBrand ?: "Realme") }
    var deviceModel by remember { mutableStateOf(job?.deviceModel ?: "") }
    var issueDescription by remember { mutableStateOf(job?.issueDescription ?: "") }
    var amountText by remember { mutableStateOf(job?.estimatedAmount?.toInt()?.toString() ?: "500") }
    var status by remember { mutableStateOf(job?.status ?: RepairJob.STATUS_PENDING) }
    var paymentStatus by remember { mutableStateOf(job?.paymentStatus ?: RepairJob.PAYMENT_UNPAID) }
    var paymentUpiRef by remember { mutableStateOf(job?.paymentUpiRef ?: "") }
    var selectedInventoryItemId by remember { mutableStateOf(job?.inventoryItemIdUsed) }

    var showPartDropdown by remember { mutableStateOf(false) }

    val brands = listOf("Realme", "Redmi", "Vivo", "Oppo", "Samsung", "OnePlus", "Poco", "Apple")
    val commonIssues = listOf(
        "Display / Combo",
        "Charging Port / CC Board",
        "Battery Drain",
        "Touch Glass",
        "Mic / Speaker",
        "Dead Solution"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("add_edit_repair_dialog"),
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
                        text = if (isEditing) strings.edit else strings.newJob,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = strings.close)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Customer Name & Phone
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text(strings.customerName) },
                    placeholder = { Text("e.g. Rahul Sharma") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("repair_customer_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text(strings.customerPhone) },
                    placeholder = { Text("e.g. 9876543210") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("repair_customer_phone_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Brand Selector Chips
                Text(
                    text = "Select Brand",
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
                    brands.forEach { brand ->
                        FilterChip(
                            selected = deviceBrand == brand,
                            onClick = { deviceBrand = brand },
                            label = { Text(brand) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deviceModel,
                    onValueChange = { deviceModel = it },
                    label = { Text(strings.deviceModel) },
                    placeholder = { Text("e.g. Realme C2 / Vivo Y12 / Redmi Note 9") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("repair_device_model_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Issue chips
                Text(
                    text = "Quick Issue Selection",
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
                    commonIssues.forEach { issue ->
                        FilterChip(
                            selected = issueDescription.contains(issue),
                            onClick = {
                                issueDescription = if (issueDescription.isBlank()) issue else "$issueDescription + $issue"
                            },
                            label = { Text(issue) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = issueDescription,
                    onValueChange = { issueDescription = it },
                    label = { Text(strings.issueDescription) },
                    placeholder = { Text("e.g. Display Broken, Folder Change OG") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Link to inventory part (Optional auto stock deduct)
                Text(
                    text = strings.partUsed,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    val selectedItem = inventoryItems.find { it.id == selectedInventoryItemId }
                    val buttonLabel = if (selectedItem != null) {
                        "${selectedItem.name} (${selectedItem.quantity} in stock)"
                    } else "None / Not from Inventory"

                    OutlinedButton(
                        onClick = { showPartDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = buttonLabel, maxLines = 1)
                    }

                    DropdownMenu(
                        expanded = showPartDropdown,
                        onDismissRequest = { showPartDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None (External or Labor only)") },
                            onClick = {
                                selectedInventoryItemId = null
                                showPartDropdown = false
                            }
                        )
                        inventoryItems.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(text = item.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = "Stock: ${item.quantity} | SP: ₹${item.sellingPrice.toInt()}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (item.quantity <= 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    selectedInventoryItemId = item.id
                                    // Pre-populate estimated amount with item's selling price if blank
                                    if (amountText.isBlank() || amountText == "500") {
                                        amountText = item.sellingPrice.toInt().toString()
                                    }
                                    showPartDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bill Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text(strings.estimatedAmount) },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("repair_bill_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Status Chips
                Text(
                    text = "Repair Status",
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
                    listOf(
                        RepairJob.STATUS_PENDING to strings.statusPending,
                        RepairJob.STATUS_IN_PROGRESS to strings.statusInProgress,
                        RepairJob.STATUS_READY_FOR_PICKUP to strings.statusReady,
                        RepairJob.STATUS_DELIVERED to strings.statusDelivered
                    ).forEach { (code, label) ->
                        FilterChip(
                            selected = status == code,
                            onClick = { status = code },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Status Chips
                Text(
                    text = strings.paymentStatus,
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
                    listOf(
                        RepairJob.PAYMENT_UNPAID to strings.unpaid,
                        RepairJob.PAYMENT_PAID_UPI to strings.paidUpi,
                        RepairJob.PAYMENT_PAID_CASH to strings.paidCash
                    ).forEach { (code, label) ->
                        FilterChip(
                            selected = paymentStatus == code,
                            onClick = { paymentStatus = code },
                            label = { Text(label) }
                        )
                    }
                }

                if (paymentStatus == RepairJob.PAYMENT_PAID_UPI) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = paymentUpiRef,
                        onValueChange = { paymentUpiRef = it },
                        label = { Text(strings.enterTxnId) },
                        placeholder = { Text(strings.txnIdPlaceholder) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (customerName.isBlank() || deviceModel.isBlank()) {
                            Toast.makeText(context, "Please enter customer name and device model", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        onSave(
                            customerName,
                            customerPhone,
                            deviceBrand,
                            deviceModel,
                            issueDescription,
                            amt,
                            status,
                            paymentStatus,
                            paymentUpiRef,
                            selectedInventoryItemId
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_repair_button")
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = strings.save, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
