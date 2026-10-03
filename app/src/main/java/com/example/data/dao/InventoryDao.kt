package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.InventoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY lastUpdated DESC")
    fun getAllInventory(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE quantity <= minAlertQuantity ORDER BY quantity ASC")
    fun getLowStockItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE compatibleModels LIKE '%' || :modelQuery || '%' OR name LIKE '%' || :modelQuery || '%'")
    fun findItemsForModel(modelQuery: String): Flow<List<InventoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<InventoryItem>)

    @Update
    suspend fun updateItem(item: InventoryItem)

    @Delete
    suspend fun deleteItem(item: InventoryItem)

    @Query("UPDATE inventory_items SET quantity = MAX(0, quantity + :delta), lastUpdated = :now WHERE id = :id")
    suspend fun adjustQuantity(id: Long, delta: Int, now: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM inventory_items")
    suspend fun getItemCount(): Int
}
