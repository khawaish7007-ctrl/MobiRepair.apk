package com.example.data

import com.example.data.dao.CompatibilityDao
import com.example.data.dao.InventoryDao
import com.example.data.dao.RepairJobDao
import com.example.data.dao.ShopDao
import com.example.data.model.CompatibilityModel
import com.example.data.model.InventoryItem
import com.example.data.model.RepairJob
import com.example.data.model.ShopProfile
import kotlinx.coroutines.flow.Flow

class MobiRepairRepository(
    private val shopDao: ShopDao,
    private val inventoryDao: InventoryDao,
    private val repairJobDao: RepairJobDao,
    private val compatibilityDao: CompatibilityDao
) {
    // Shop
    val shopProfile: Flow<ShopProfile?> = shopDao.getShopProfile()

    suspend fun getShopProfileOnce(): ShopProfile? = shopDao.getShopProfileOnce()

    suspend fun saveShopProfile(profile: ShopProfile) {
        shopDao.insertOrUpdateProfile(profile)
    }

    // Inventory
    val allInventory: Flow<List<InventoryItem>> = inventoryDao.getAllInventory()
    val lowStockItems: Flow<List<InventoryItem>> = inventoryDao.getLowStockItems()

    suspend fun saveInventoryItem(item: InventoryItem): Long {
        return inventoryDao.insertItem(item)
    }

    suspend fun updateInventoryItem(item: InventoryItem) {
        inventoryDao.updateItem(item)
    }

    suspend fun deleteInventoryItem(item: InventoryItem) {
        inventoryDao.deleteItem(item)
    }

    suspend fun adjustInventoryQuantity(id: Long, delta: Int) {
        inventoryDao.adjustQuantity(id, delta)
    }

    fun findItemsForModel(modelQuery: String): Flow<List<InventoryItem>> {
        return inventoryDao.findItemsForModel(modelQuery)
    }

    // Repair Jobs
    val allJobs: Flow<List<RepairJob>> = repairJobDao.getAllJobs()

    suspend fun saveRepairJob(job: RepairJob): Long {
        return repairJobDao.insertJob(job)
    }

    suspend fun updateRepairJob(job: RepairJob) {
        repairJobDao.updateJob(job)
    }

    suspend fun deleteRepairJob(job: RepairJob) {
        repairJobDao.deleteJob(job)
    }

    suspend fun updateJobStatus(id: Long, status: String) {
        repairJobDao.updateJobStatus(id, status)
    }

    suspend fun recordPayment(id: Long, paymentStatus: String, upiRef: String) {
        repairJobDao.updatePaymentStatus(id, paymentStatus, upiRef)
    }

    // Compatibility
    val allCompatibilities: Flow<List<CompatibilityModel>> = compatibilityDao.getAllCompatibilities()

    fun searchCompatibilities(query: String): Flow<List<CompatibilityModel>> {
        return if (query.isBlank()) {
            compatibilityDao.getAllCompatibilities()
        } else {
            compatibilityDao.searchCompatibility(query.trim())
        }
    }

    suspend fun saveCompatibility(item: CompatibilityModel): Long {
        return compatibilityDao.insertCompatibility(item)
    }

    suspend fun deleteCompatibility(item: CompatibilityModel) {
        compatibilityDao.deleteCompatibility(item)
    }
}
