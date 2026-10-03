package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MobiRepairRepository
import com.example.data.model.CompatibilityModel
import com.example.data.model.InventoryItem
import com.example.data.model.RepairJob
import com.example.data.model.ShopProfile
import com.example.localization.AppLanguage
import com.example.localization.LocalizationManager
import com.example.util.WhatsAppHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MobiRepairRepository
    val firestoreSyncManager: com.example.data.sync.FirestoreSyncManager
    val cloudSyncState: StateFlow<com.example.data.sync.CloudSyncState>

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = MobiRepairRepository(
            database.shopDao(),
            database.inventoryDao(),
            database.repairJobDao(),
            database.compatibilityDao()
        )
        firestoreSyncManager = com.example.data.sync.FirestoreSyncManager(application, database.inventoryDao())
        cloudSyncState = firestoreSyncManager.syncState
    }

    fun migrateInventoryToFirestore(customShopId: String? = null) {
        val sId = customShopId ?: shopProfile.value.upiId.ifBlank { "main_shop" }
        viewModelScope.launch {
            firestoreSyncManager.migrateRoomToFirestore(sId, allInventory.value)
        }
    }

    fun pullInventoryFromFirestore(customShopId: String? = null) {
        val sId = customShopId ?: shopProfile.value.upiId.ifBlank { "main_shop" }
        viewModelScope.launch {
            firestoreSyncManager.pullFirestoreToRoom(sId)
        }
    }

    fun startRealtimeCloudSync(customShopId: String? = null) {
        val sId = customShopId ?: shopProfile.value.upiId.ifBlank { "main_shop" }
        firestoreSyncManager.startRealtimeCloudSync(sId)
    }

    fun stopRealtimeCloudSync() {
        firestoreSyncManager.stopRealtimeCloudSync()
    }

    // Shop Profile & Language
    val shopProfile: StateFlow<ShopProfile> = repository.shopProfile
        .combine(MutableStateFlow(Unit)) { profile, _ ->
            profile ?: ShopProfile()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ShopProfile()
        )

    private val _currentLanguage = MutableStateFlow(AppLanguage.EN)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Navigation & Tabs
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        viewModelScope.launch {
            val current = shopProfile.value
            repository.saveShopProfile(current.copy(selectedLanguage = language.name))
        }
    }

    // Repairs
    val allJobs: StateFlow<List<RepairJob>> = repository.allJobs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _repairSearchQuery = MutableStateFlow("")
    val repairSearchQuery: StateFlow<String> = _repairSearchQuery.asStateFlow()

    private val _repairStatusFilter = MutableStateFlow("ALL")
    val repairStatusFilter: StateFlow<String> = _repairStatusFilter.asStateFlow()

    fun setRepairSearchQuery(query: String) {
        _repairSearchQuery.value = query
    }

    fun setRepairStatusFilter(filter: String) {
        _repairStatusFilter.value = filter
    }

    val filteredJobs: StateFlow<List<RepairJob>> = combine(
        allJobs,
        _repairSearchQuery,
        _repairStatusFilter
    ) { jobs, query, statusFilter ->
        jobs.filter { job ->
            val matchesQuery = query.isBlank() ||
                    job.customerName.contains(query, ignoreCase = true) ||
                    job.customerPhone.contains(query, ignoreCase = true) ||
                    job.deviceModel.contains(query, ignoreCase = true) ||
                    job.tokenNumber.contains(query, ignoreCase = true) ||
                    job.issueDescription.contains(query, ignoreCase = true)

            val matchesStatus = statusFilter == "ALL" || job.status == statusFilter
            matchesQuery && matchesStatus
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Inventory
    val allInventory: StateFlow<List<InventoryItem>> = repository.allInventory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val lowStockItems: StateFlow<List<InventoryItem>> = repository.lowStockItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _inventorySearchQuery = MutableStateFlow("")
    val inventorySearchQuery: StateFlow<String> = _inventorySearchQuery.asStateFlow()

    private val _inventoryCategoryFilter = MutableStateFlow("ALL")
    val inventoryCategoryFilter: StateFlow<String> = _inventoryCategoryFilter.asStateFlow()

    private val _onlyLowStock = MutableStateFlow(false)
    val onlyLowStock: StateFlow<Boolean> = _onlyLowStock.asStateFlow()

    fun setInventorySearchQuery(query: String) {
        _inventorySearchQuery.value = query
    }

    fun setInventoryCategoryFilter(category: String) {
        _inventoryCategoryFilter.value = category
    }

    fun toggleOnlyLowStock() {
        _onlyLowStock.value = !_onlyLowStock.value
    }

    val filteredInventory: StateFlow<List<InventoryItem>> = combine(
        allInventory,
        _inventorySearchQuery,
        _inventoryCategoryFilter,
        _onlyLowStock
    ) { items, query, category, lowOnly ->
        items.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.compatibleModels.contains(query, ignoreCase = true) ||
                    item.partCode.contains(query, ignoreCase = true)

            val matchesCategory = category == "ALL" || item.category == category
            val matchesLow = !lowOnly || (item.isLowStock || item.isOutOfStock)

            matchesQuery && matchesCategory && matchesLow
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Compatibility
    val allCompatibilities: StateFlow<List<CompatibilityModel>> = repository.allCompatibilities
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _compatibilitySearchQuery = MutableStateFlow("")
    val compatibilitySearchQuery: StateFlow<String> = _compatibilitySearchQuery.asStateFlow()

    fun setCompatibilitySearchQuery(query: String) {
        _compatibilitySearchQuery.value = query
    }

    val filteredCompatibilities: StateFlow<List<CompatibilityModel>> = combine(
        allCompatibilities,
        _compatibilitySearchQuery
    ) { items, query ->
        if (query.isBlank()) {
            items
        } else {
            items.filter { item ->
                item.compatibleDevices.contains(query, ignoreCase = true) ||
                        item.groupTitle.contains(query, ignoreCase = true) ||
                        item.partCategory.contains(query, ignoreCase = true) ||
                        item.partCodeOrType.contains(query, ignoreCase = true) ||
                        item.technicianNotes.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dialog States
    private val _activeQrJob = MutableStateFlow<RepairJob?>(null)
    val activeQrJob: StateFlow<RepairJob?> = _activeQrJob.asStateFlow()

    private val _showQuickBillDialog = MutableStateFlow(false)
    val showQuickBillDialog: StateFlow<Boolean> = _showQuickBillDialog.asStateFlow()

    private val _showAddEditRepairDialog = MutableStateFlow(false)
    val showAddEditRepairDialog: StateFlow<Boolean> = _showAddEditRepairDialog.asStateFlow()
    val editingRepairJob = MutableStateFlow<RepairJob?>(null)

    private val _showAddEditInventoryDialog = MutableStateFlow(false)
    val showAddEditInventoryDialog: StateFlow<Boolean> = _showAddEditInventoryDialog.asStateFlow()
    val editingInventoryItem = MutableStateFlow<InventoryItem?>(null)

    fun openUpiQrForJob(job: RepairJob) {
        _activeQrJob.value = job
    }

    fun closeUpiQrDialog() {
        _activeQrJob.value = null
    }

    fun openQuickBillDialog() {
        _showQuickBillDialog.value = true
    }

    fun closeQuickBillDialog() {
        _showQuickBillDialog.value = false
    }

    fun openAddRepairDialog() {
        editingRepairJob.value = null
        _showAddEditRepairDialog.value = true
    }

    fun openEditRepairDialog(job: RepairJob) {
        editingRepairJob.value = job
        _showAddEditRepairDialog.value = true
    }

    fun closeAddEditRepairDialog() {
        _showAddEditRepairDialog.value = false
        editingRepairJob.value = null
    }

    fun openAddInventoryDialog() {
        editingInventoryItem.value = null
        _showAddEditInventoryDialog.value = true
    }

    fun openEditInventoryDialog(item: InventoryItem) {
        editingInventoryItem.value = item
        _showAddEditInventoryDialog.value = true
    }

    fun closeAddEditInventoryDialog() {
        _showAddEditInventoryDialog.value = false
        editingInventoryItem.value = null
    }

    // Actions: Shop Profile
    fun updateShopDetails(
        shopName: String,
        ownerName: String,
        upiId: String,
        ownerPhone: String,
        address: String
    ) {
        viewModelScope.launch {
            val current = shopProfile.value
            repository.saveShopProfile(
                current.copy(
                    shopName = shopName.trim(),
                    ownerName = ownerName.trim(),
                    upiId = upiId.trim(),
                    ownerPhone = ownerPhone.trim(),
                    address = address.trim()
                )
            )
        }
    }

    // Actions: Inventory
    fun saveInventoryItem(
        name: String,
        category: String,
        quantity: Int,
        costPrice: Double,
        sellingPrice: Double,
        minAlertQty: Int,
        compatibleModels: String,
        partCode: String
    ) {
        viewModelScope.launch {
            val current = editingInventoryItem.value
            if (current != null) {
                repository.updateInventoryItem(
                    current.copy(
                        name = name.trim(),
                        category = category.trim(),
                        quantity = quantity,
                        costPrice = costPrice,
                        sellingPrice = sellingPrice,
                        minAlertQuantity = minAlertQty,
                        compatibleModels = compatibleModels.trim(),
                        partCode = partCode.trim(),
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            } else {
                repository.saveInventoryItem(
                    InventoryItem(
                        name = name.trim(),
                        category = category.trim(),
                        quantity = quantity,
                        costPrice = costPrice,
                        sellingPrice = sellingPrice,
                        minAlertQuantity = minAlertQty,
                        compatibleModels = compatibleModels.trim(),
                        partCode = partCode.trim()
                    )
                )
            }
            closeAddEditInventoryDialog()
        }
    }

    fun adjustStock(id: Long, delta: Int) {
        viewModelScope.launch {
            repository.adjustInventoryQuantity(id, delta)
        }
    }

    fun deleteInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.deleteInventoryItem(item)
        }
    }

    // Actions: Repair Jobs
    fun saveRepairJob(
        customerName: String,
        customerPhone: String,
        deviceBrand: String,
        deviceModel: String,
        issueDescription: String,
        estimatedAmount: Double,
        status: String,
        paymentStatus: String,
        paymentUpiRef: String,
        deductInventoryItemId: Long? = null
    ) {
        viewModelScope.launch {
            val existing = editingRepairJob.value
            if (existing != null) {
                repository.updateRepairJob(
                    existing.copy(
                        customerName = customerName.trim(),
                        customerPhone = customerPhone.trim(),
                        deviceBrand = deviceBrand.trim(),
                        deviceModel = deviceModel.trim(),
                        issueDescription = issueDescription.trim(),
                        estimatedAmount = estimatedAmount,
                        status = status,
                        paymentStatus = paymentStatus,
                        paymentUpiRef = paymentUpiRef.trim(),
                        inventoryItemIdUsed = deductInventoryItemId ?: existing.inventoryItemIdUsed
                    )
                )
            } else {
                val nextTokenNum = "JOB-" + (100 + (allJobs.value.size + 1))
                val jobId = repository.saveRepairJob(
                    RepairJob(
                        tokenNumber = nextTokenNum,
                        customerName = customerName.trim(),
                        customerPhone = customerPhone.trim(),
                        deviceBrand = deviceBrand.trim(),
                        deviceModel = deviceModel.trim(),
                        issueDescription = issueDescription.trim(),
                        estimatedAmount = estimatedAmount,
                        status = status,
                        paymentStatus = paymentStatus,
                        paymentUpiRef = paymentUpiRef.trim(),
                        inventoryItemIdUsed = deductInventoryItemId
                    )
                )
                // Deduct inventory if selected
                if (deductInventoryItemId != null) {
                    repository.adjustInventoryQuantity(deductInventoryItemId, -1)
                }
            }
            closeAddEditRepairDialog()
        }
    }

    fun markJobReady(job: RepairJob, context: Context? = null) {
        viewModelScope.launch {
            repository.updateJobStatus(job.id, RepairJob.STATUS_READY_FOR_PICKUP)
            if (context != null) {
                sendJobWhatsAppNotification(context, job.copy(status = RepairJob.STATUS_READY_FOR_PICKUP))
            }
        }
    }

    fun markJobDelivered(job: RepairJob) {
        viewModelScope.launch {
            repository.updateJobStatus(job.id, RepairJob.STATUS_DELIVERED)
        }
    }

    fun recordJobPayment(
        job: RepairJob,
        paymentStatus: String,
        upiRef: String,
        context: Context? = null
    ) {
        viewModelScope.launch {
            repository.recordPayment(job.id, paymentStatus, upiRef.trim())
            // If marked paid, also auto-update active job if in dialog
            val updatedJob = job.copy(paymentStatus = paymentStatus, paymentUpiRef = upiRef.trim())
            _activeQrJob.value = updatedJob

            if (context != null) {
                sendJobWhatsAppNotification(context, updatedJob)
            }
        }
    }

    fun deleteRepairJob(job: RepairJob) {
        viewModelScope.launch {
            repository.deleteRepairJob(job)
        }
    }

    fun sendJobWhatsAppNotification(context: Context, job: RepairJob) {
        val shop = shopProfile.value
        if (job.isPaid) {
            com.example.service.WhatsAppIntentService.sendPaymentReceipt(
                context = context,
                job = job,
                shop = shop,
                language = _currentLanguage.value,
                customTxnId = job.paymentUpiRef
            )
        } else {
            com.example.service.WhatsAppIntentService.sendRepairStatusUpdate(
                context = context,
                job = job,
                shop = shop,
                language = _currentLanguage.value
            )
        }
    }

    fun sendJobPaymentReceipt(context: Context, job: RepairJob, upiRef: String) {
        val shop = shopProfile.value
        com.example.service.WhatsAppIntentService.sendPaymentReceipt(
            context = context,
            job = job,
            shop = shop,
            language = _currentLanguage.value,
            customTxnId = upiRef
        )
    }

    fun sendCustomWhatsAppNotification(
        context: Context,
        customerName: String,
        customerPhone: String,
        deviceModel: String,
        note: String,
        amount: Double,
        isPaid: Boolean,
        txnId: String?
    ) {
        val shop = shopProfile.value
        com.example.service.WhatsAppIntentService.sendQuickBillReceipt(
            context = context,
            customerPhone = customerPhone,
            customerName = customerName,
            deviceModel = deviceModel,
            serviceNote = note,
            amount = amount,
            upiRef = txnId,
            shop = shop,
            language = _currentLanguage.value
        )
    }
}
