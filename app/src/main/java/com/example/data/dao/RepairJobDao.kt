package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RepairJob
import kotlinx.coroutines.flow.Flow

@Dao
interface RepairJobDao {
    @Query("SELECT * FROM repair_jobs ORDER BY createdAt DESC")
    fun getAllJobs(): Flow<List<RepairJob>>

    @Query("SELECT * FROM repair_jobs WHERE status = :status ORDER BY createdAt DESC")
    fun getJobsByStatus(status: String): Flow<List<RepairJob>>

    @Query("SELECT * FROM repair_jobs WHERE id = :id LIMIT 1")
    suspend fun getJobById(id: Long): RepairJob?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: RepairJob): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jobs: List<RepairJob>)

    @Update
    suspend fun updateJob(job: RepairJob)

    @Delete
    suspend fun deleteJob(job: RepairJob)

    @Query("UPDATE repair_jobs SET status = :status, completedAt = CASE WHEN :status = 'READY_FOR_PICKUP' THEN :timestamp ELSE completedAt END, deliveredAt = CASE WHEN :status = 'DELIVERED' THEN :timestamp ELSE deliveredAt END WHERE id = :id")
    suspend fun updateJobStatus(id: Long, status: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE repair_jobs SET paymentStatus = :paymentStatus, paymentUpiRef = :upiRef WHERE id = :id")
    suspend fun updatePaymentStatus(id: Long, paymentStatus: String, upiRef: String)

    @Query("SELECT COUNT(*) FROM repair_jobs")
    suspend fun getJobCount(): Int
}
