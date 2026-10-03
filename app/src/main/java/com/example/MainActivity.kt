package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MobileScreenShare
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.RepairJob
import com.example.localization.AppLanguage
import com.example.localization.LocalizationManager
import com.example.ui.MainViewModel
import com.example.ui.components.AppTopBar
import com.example.ui.dialogs.AddEditInventoryDialog
import com.example.ui.dialogs.AddEditRepairDialog
import com.example.ui.dialogs.QuickBillDialog
import com.example.ui.dialogs.UpiQrDialog
import com.example.ui.screens.CompatibilityScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.RepairsScreen
import com.example.ui.screens.ShopSettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MobiRepairApp()
            }
        }
    }
}

@Composable
fun MobiRepairApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current

    val shopProfile by viewModel.shopProfile.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    val strings = LocalizationManager.getStrings(currentLanguage)

    // Sync saved language from profile once
    LaunchedEffect(shopProfile.selectedLanguage) {
        if (shopProfile.selectedLanguage.isNotBlank()) {
            val matchingLang = AppLanguage.values().find { it.name == shopProfile.selectedLanguage }
            if (matchingLang != null && matchingLang != currentLanguage) {
                viewModel.setLanguage(matchingLang)
            }
        }
    }

    // Repairs State
    val allJobs by viewModel.allJobs.collectAsStateWithLifecycle()
    val filteredJobs by viewModel.filteredJobs.collectAsStateWithLifecycle()
    val repairSearchQuery by viewModel.repairSearchQuery.collectAsStateWithLifecycle()
    val repairStatusFilter by viewModel.repairStatusFilter.collectAsStateWithLifecycle()

    // Inventory State
    val allInventory by viewModel.allInventory.collectAsStateWithLifecycle()
    val filteredInventory by viewModel.filteredInventory.collectAsStateWithLifecycle()
    val lowStockItems by viewModel.lowStockItems.collectAsStateWithLifecycle()
    val inventorySearchQuery by viewModel.inventorySearchQuery.collectAsStateWithLifecycle()
    val inventoryCategoryFilter by viewModel.inventoryCategoryFilter.collectAsStateWithLifecycle()
    val onlyLowStock by viewModel.onlyLowStock.collectAsStateWithLifecycle()

    // Compatibility State
    val filteredCompatibilities by viewModel.filteredCompatibilities.collectAsStateWithLifecycle()
    val compatibilitySearchQuery by viewModel.compatibilitySearchQuery.collectAsStateWithLifecycle()
    val cloudSyncState by viewModel.cloudSyncState.collectAsStateWithLifecycle()

    // Dialogs State
    val activeQrJob by viewModel.activeQrJob.collectAsStateWithLifecycle()
    val showQuickBillDialog by viewModel.showQuickBillDialog.collectAsStateWithLifecycle()
    val showAddEditRepairDialog by viewModel.showAddEditRepairDialog.collectAsStateWithLifecycle()
    val editingRepairJob by viewModel.editingRepairJob.collectAsStateWithLifecycle()
    val showAddEditInventoryDialog by viewModel.showAddEditInventoryDialog.collectAsStateWithLifecycle()
    val editingInventoryItem by viewModel.editingInventoryItem.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = strings.appTitle,
                strings = strings,
                currentLanguage = currentLanguage,
                onLanguageSelected = { viewModel.setLanguage(it) },
                onQuickBillClick = { viewModel.openQuickBillDialog() }
            )
        },
        bottomBar = {
            NavigationBar(modifier = Modifier.testTag("bottom_nav_bar")) {
                // Tab 0: Repairs
                val pendingCount = allJobs.count { it.status == RepairJob.STATUS_PENDING }
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = {
                        if (pendingCount > 0) {
                            BadgedBox(badge = { Badge { Text("$pendingCount") } }) {
                                Icon(imageVector = Icons.Default.Build, contentDescription = strings.tabRepairs)
                            }
                        } else {
                            Icon(imageVector = Icons.Default.Build, contentDescription = strings.tabRepairs)
                        }
                    },
                    label = { Text(strings.tabRepairs, maxLines = 1) },
                    modifier = Modifier.testTag("tab_repairs")
                )

                // Tab 1: Inventory
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = {
                        if (lowStockItems.isNotEmpty()) {
                            BadgedBox(badge = { Badge { Text("${lowStockItems.size}") } }) {
                                Icon(imageVector = Icons.Default.Inventory2, contentDescription = strings.tabInventory)
                            }
                        } else {
                            Icon(imageVector = Icons.Default.Inventory2, contentDescription = strings.tabInventory)
                        }
                    },
                    label = { Text(strings.tabInventory, maxLines = 1) },
                    modifier = Modifier.testTag("tab_inventory")
                )

                // Tab 2: Compatibility
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = { Icon(imageVector = Icons.AutoMirrored.Filled.MobileScreenShare, contentDescription = strings.tabCompatibility) },
                    label = { Text(strings.tabCompatibility, maxLines = 1) },
                    modifier = Modifier.testTag("tab_compatibility")
                )

                // Tab 3: Shop & UPI
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = { Icon(imageVector = Icons.Default.Store, contentDescription = strings.tabSettings) },
                    label = { Text(strings.tabSettings, maxLines = 1) },
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    RepairsScreen(
                        jobs = filteredJobs,
                        allJobs = allJobs,
                        searchQuery = repairSearchQuery,
                        statusFilter = repairStatusFilter,
                        strings = strings,
                        onSearchChange = { viewModel.setRepairSearchQuery(it) },
                        onStatusFilterChange = { viewModel.setRepairStatusFilter(it) },
                        onNewJobClick = { viewModel.openAddRepairDialog() },
                        onJobClick = { viewModel.openEditRepairDialog(it) },
                        onOpenQr = { viewModel.openUpiQrForJob(it) },
                        onSendWhatsApp = { viewModel.sendJobWhatsAppNotification(context, it) },
                        onMarkReady = { viewModel.markJobReady(it, context) },
                        onMarkDelivered = { viewModel.markJobDelivered(it) },
                        onDeleteJob = { viewModel.deleteRepairJob(it) }
                    )
                }
                1 -> {
                    InventoryScreen(
                        items = filteredInventory,
                        allItems = allInventory,
                        lowStockItems = lowStockItems,
                        searchQuery = inventorySearchQuery,
                        categoryFilter = inventoryCategoryFilter,
                        onlyLowStock = onlyLowStock,
                        strings = strings,
                        onSearchChange = { viewModel.setInventorySearchQuery(it) },
                        onCategoryFilterChange = { viewModel.setInventoryCategoryFilter(it) },
                        onToggleLowStockOnly = { viewModel.toggleOnlyLowStock() },
                        onAddItemClick = { viewModel.openAddInventoryDialog() },
                        onEditItemClick = { viewModel.openEditInventoryDialog(it) },
                        onAdjustStock = { id, delta -> viewModel.adjustStock(id, delta) },
                        onDeleteItem = { viewModel.deleteInventoryItem(it) }
                    )
                }
                2 -> {
                    CompatibilityScreen(
                        compatibilities = filteredCompatibilities,
                        inventoryItems = allInventory,
                        searchQuery = compatibilitySearchQuery,
                        strings = strings,
                        onSearchChange = { viewModel.setCompatibilitySearchQuery(it) }
                    )
                }
                3 -> {
                    ShopSettingsScreen(
                        shopProfile = shopProfile,
                        currentLanguage = currentLanguage,
                        strings = strings,
                        cloudSyncState = cloudSyncState,
                        onLanguageChange = { viewModel.setLanguage(it) },
                        onSaveProfile = { shopName, ownerName, upiId, ownerPhone, address ->
                            viewModel.updateShopDetails(shopName, ownerName, upiId, ownerPhone, address)
                        },
                        onMigrateToFirestore = { viewModel.migrateInventoryToFirestore() },
                        onPullFromFirestore = { viewModel.pullInventoryFromFirestore() },
                        onToggleRealtimeSync = { enabled ->
                            if (enabled) viewModel.startRealtimeCloudSync() else viewModel.stopRealtimeCloudSync()
                        }
                    )
                }
            }
        }
    }

    // Dynamic UPI QR Dialog for Job
    if (activeQrJob != null) {
        UpiQrDialog(
            job = activeQrJob!!,
            shopProfile = shopProfile,
            strings = strings,
            onDismiss = { viewModel.closeUpiQrDialog() },
            onPaymentConfirmed = { job, txnId ->
                viewModel.recordJobPayment(job, RepairJob.PAYMENT_PAID_UPI, txnId, context)
            },
            onSendWhatsApp = { job ->
                viewModel.sendJobWhatsAppNotification(context, job)
            }
        )
    }

    // Quick Standalone UPI Bill Dialog
    if (showQuickBillDialog) {
        QuickBillDialog(
            shopProfile = shopProfile,
            strings = strings,
            onDismiss = { viewModel.closeQuickBillDialog() },
            onSendWhatsApp = { custName, custPhone, model, note, amt, isPaid, txnId ->
                viewModel.sendCustomWhatsAppNotification(
                    context = context,
                    customerName = custName,
                    customerPhone = custPhone,
                    deviceModel = model,
                    note = note,
                    amount = amt,
                    isPaid = isPaid,
                    txnId = txnId
                )
            }
        )
    }

    // Add / Edit Repair Job Dialog
    if (showAddEditRepairDialog) {
        AddEditRepairDialog(
            job = editingRepairJob,
            inventoryItems = allInventory,
            strings = strings,
            onDismiss = { viewModel.closeAddEditRepairDialog() },
            onSave = { name, phone, brand, model, issue, amt, status, payStatus, upiRef, deductId ->
                viewModel.saveRepairJob(
                    customerName = name,
                    customerPhone = phone,
                    deviceBrand = brand,
                    deviceModel = model,
                    issueDescription = issue,
                    estimatedAmount = amt,
                    status = status,
                    paymentStatus = payStatus,
                    paymentUpiRef = upiRef,
                    deductInventoryItemId = deductId
                )
            }
        )
    }

    // Add / Edit Inventory Part Dialog
    if (showAddEditInventoryDialog) {
        AddEditInventoryDialog(
            item = editingInventoryItem,
            strings = strings,
            onDismiss = { viewModel.closeAddEditInventoryDialog() },
            onSave = { name, cat, qty, cp, sp, minAlert, models, code ->
                viewModel.saveInventoryItem(
                    name = name,
                    category = cat,
                    quantity = qty,
                    costPrice = cp,
                    sellingPrice = sp,
                    minAlertQty = minAlert,
                    compatibleModels = models,
                    partCode = code
                )
            }
        )
    }
}
