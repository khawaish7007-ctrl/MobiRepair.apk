package com.example.data.sync

import android.content.Context
import android.util.Log
import com.example.data.dao.InventoryDao
import com.example.data.model.InventoryItem
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class CloudSyncState {
    object Idle : CloudSyncState()
    object Syncing : CloudSyncState()
    data class Success(val message: String, val timestamp: Long = System.currentTimeMillis()) : CloudSyncState()
    data class Error(val error: String) : CloudSyncState()
    data class MissingConfig(val instructions: String) : CloudSyncState()
}

class FirestoreSyncManager(
    private val context: Context,
    private val inventoryDao: InventoryDao
) {
    private val TAG = "FirestoreSyncManager"

    private val _syncState = MutableStateFlow<CloudSyncState>(CloudSyncState.Idle)
    val syncState: StateFlow<CloudSyncState> = _syncState.asStateFlow()

    private var realtimeListener: ListenerRegistration? = null

    /**
     * Checks if Firebase is initialized in the current application context.
     * Prevents runtime crashes when google-services.json is not yet supplied.
     */
    fun isFirebaseAvailable(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private fun getFirestore(): FirebaseFirestore? {
        return if (isFirebaseAvailable()) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "Error obtaining Firestore instance: ${e.message}")
                null
            }
        } else {
            null
        }
    }

    /**
     * Sanitizes shop ID for Firestore path
     */
    fun sanitizeShopId(rawId: String): String {
        val sanitized = rawId.trim().lowercase()
            .replace(Regex("[^a-z0-9_]+"), "_")
            .trim('_')
        return if (sanitized.isNotBlank()) sanitized else "main_shop"
    }

    /**
     * MIGRATION UTILITY:
     * Migrates all local Room inventory items to Firebase Firestore.
     * Used for initial cloud seeding and multi-device setup.
     */
    suspend fun migrateRoomToFirestore(
        shopId: String,
        localItems: List<InventoryItem>
    ): Result<Int> = withContext(Dispatchers.IO) {
        if (!isFirebaseAvailable()) {
            val msg = "Firebase is not configured. Please place your google-services.json file in the /app folder to enable cloud sync."
            _syncState.value = CloudSyncState.MissingConfig(msg)
            return@withContext Result.failure(IllegalStateException(msg))
        }

        val db = getFirestore() ?: return@withContext Result.failure(IllegalStateException("Firestore unavailable"))
        val targetShopId = sanitizeShopId(shopId)

        _syncState.value = CloudSyncState.Syncing

        try {
            val batch = db.batch()
            val collectionRef = db.collection("shops").document(targetShopId).collection("inventory")

            for (item in localItems) {
                val docRef = collectionRef.document(item.id.toString())
                val data = hashMapOf<String, Any>(
                    "id" to item.id,
                    "name" to item.name,
                    "category" to item.category,
                    "quantity" to item.quantity,
                    "costPrice" to item.costPrice,
                    "sellingPrice" to item.sellingPrice,
                    "minAlertQuantity" to item.minAlertQuantity,
                    "compatibleModels" to item.compatibleModels,
                    "partCode" to item.partCode,
                    "lastUpdated" to item.lastUpdated,
                    "syncedAt" to System.currentTimeMillis()
                )
                batch.set(docRef, data, SetOptions.merge())
            }

            batch.commit().await()
            val count = localItems.size
            val successMsg = "Successfully migrated $count items to Firestore (Cloud: $targetShopId)"
            _syncState.value = CloudSyncState.Success(successMsg)
            Result.success(count)
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Failed to migrate inventory to cloud"
            Log.e(TAG, "Migration error", e)
            _syncState.value = CloudSyncState.Error(errorMsg)
            Result.failure(e)
        }
    }

    /**
     * PULL UTILITY:
     * Downloads cloud inventory from Firestore and updates/inserts into Room database.
     */
    suspend fun pullFirestoreToRoom(shopId: String): Result<Int> = withContext(Dispatchers.IO) {
        if (!isFirebaseAvailable()) {
            val msg = "Firebase is not configured. Place google-services.json in /app to enable cloud download."
            _syncState.value = CloudSyncState.MissingConfig(msg)
            return@withContext Result.failure(IllegalStateException(msg))
        }

        val db = getFirestore() ?: return@withContext Result.failure(IllegalStateException("Firestore unavailable"))
        val targetShopId = sanitizeShopId(shopId)

        _syncState.value = CloudSyncState.Syncing

        try {
            val snapshot = db.collection("shops")
                .document(targetShopId)
                .collection("inventory")
                .get()
                .await()

            val remoteItems = mutableListOf<InventoryItem>()
            for (doc in snapshot.documents) {
                val id = doc.getLong("id") ?: (doc.id.toLongOrNull() ?: 0L)
                val name = doc.getString("name") ?: ""
                val category = doc.getString("category") ?: "Folder (Display)"
                val quantity = doc.getLong("quantity")?.toInt() ?: 0
                val costPrice = doc.getDouble("costPrice") ?: 0.0
                val sellingPrice = doc.getDouble("sellingPrice") ?: 0.0
                val minAlertQuantity = doc.getLong("minAlertQuantity")?.toInt() ?: 2
                val compatibleModels = doc.getString("compatibleModels") ?: ""
                val partCode = doc.getString("partCode") ?: ""
                val lastUpdated = doc.getLong("lastUpdated") ?: System.currentTimeMillis()

                if (name.isNotBlank()) {
                    remoteItems.add(
                        InventoryItem(
                            id = id,
                            name = name,
                            category = category,
                            quantity = quantity,
                            costPrice = costPrice,
                            sellingPrice = sellingPrice,
                            minAlertQuantity = minAlertQuantity,
                            compatibleModels = compatibleModels,
                            partCode = partCode,
                            lastUpdated = lastUpdated
                        )
                    )
                }
            }

            if (remoteItems.isNotEmpty()) {
                inventoryDao.insertAll(remoteItems)
            }

            val successMsg = "Downloaded ${remoteItems.size} items from Cloud to local database"
            _syncState.value = CloudSyncState.Success(successMsg)
            Result.success(remoteItems.size)
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Failed to pull cloud inventory"
            Log.e(TAG, "Pull error", e)
            _syncState.value = CloudSyncState.Error(errorMsg)
            Result.failure(e)
        }
    }

    /**
     * MULTI-DEVICE REAL-TIME LISTENER:
     * Listens for remote changes in Firestore and automatically updates local Room storage in real time.
     */
    fun startRealtimeCloudSync(shopId: String, onUpdateReceived: (Int) -> Unit = {}) {
        if (!isFirebaseAvailable()) {
            _syncState.value = CloudSyncState.MissingConfig("Firebase not initialized")
            return
        }

        stopRealtimeCloudSync()
        val db = getFirestore() ?: return
        val targetShopId = sanitizeShopId(shopId)

        try {
            realtimeListener = db.collection("shops")
                .document(targetShopId)
                .collection("inventory")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Realtime sync error", error)
                        _syncState.value = CloudSyncState.Error(error.message ?: "Realtime sync error")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val updatedList = mutableListOf<InventoryItem>()
                        for (doc in snapshot.documents) {
                            val id = doc.getLong("id") ?: (doc.id.toLongOrNull() ?: 0L)
                            val name = doc.getString("name") ?: ""
                            val category = doc.getString("category") ?: "Folder (Display)"
                            val quantity = doc.getLong("quantity")?.toInt() ?: 0
                            val costPrice = doc.getDouble("costPrice") ?: 0.0
                            val sellingPrice = doc.getDouble("sellingPrice") ?: 0.0
                            val minAlertQuantity = doc.getLong("minAlertQuantity")?.toInt() ?: 2
                            val compatibleModels = doc.getString("compatibleModels") ?: ""
                            val partCode = doc.getString("partCode") ?: ""
                            val lastUpdated = doc.getLong("lastUpdated") ?: System.currentTimeMillis()

                            if (name.isNotBlank()) {
                                updatedList.add(
                                    InventoryItem(
                                        id = id,
                                        name = name,
                                        category = category,
                                        quantity = quantity,
                                        costPrice = costPrice,
                                        sellingPrice = sellingPrice,
                                        minAlertQuantity = minAlertQuantity,
                                        compatibleModels = compatibleModels,
                                        partCode = partCode,
                                        lastUpdated = lastUpdated
                                    )
                                )
                            }
                        }

                        // Persist to Room
                        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                            inventoryDao.insertAll(updatedList)
                            _syncState.value = CloudSyncState.Success("Real-time sync: ${updatedList.size} items updated")
                            onUpdateReceived(updatedList.size)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start realtime sync", e)
        }
    }

    fun stopRealtimeCloudSync() {
        realtimeListener?.remove()
        realtimeListener = null
    }

    fun resetState() {
        _syncState.value = CloudSyncState.Idle
    }
}
