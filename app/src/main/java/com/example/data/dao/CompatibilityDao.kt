package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CompatibilityModel
import kotlinx.coroutines.flow.Flow

@Dao
interface CompatibilityDao {
    @Query("SELECT * FROM compatibility_models ORDER BY partCategory ASC, groupTitle ASC")
    fun getAllCompatibilities(): Flow<List<CompatibilityModel>>

    @Query("SELECT * FROM compatibility_models WHERE compatibleDevices LIKE '%' || :query || '%' OR groupTitle LIKE '%' || :query || '%' OR partCodeOrType LIKE '%' || :query || '%'")
    fun searchCompatibility(query: String): Flow<List<CompatibilityModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompatibility(item: CompatibilityModel): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CompatibilityModel>)

    @Delete
    suspend fun deleteCompatibility(item: CompatibilityModel)

    @Query("SELECT COUNT(*) FROM compatibility_models")
    suspend fun getCount(): Int
}
