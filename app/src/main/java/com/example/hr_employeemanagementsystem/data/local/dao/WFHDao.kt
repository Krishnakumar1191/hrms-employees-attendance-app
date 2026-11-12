package com.example.hr_employeemanagementsystem.data.local.dao

import androidx.room.*
import com.example.hr_employeemanagementsystem.data.model.WFHEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WFHDao {
    @Query("SELECT * FROM wfh_requests ORDER BY id DESC")
    fun getAllWFHRequests(): Flow<List<WFHEntity>>

    @Query("SELECT * FROM wfh_requests WHERE status = :status ORDER BY id DESC")
    fun getWFHByStatus(status: String): Flow<List<WFHEntity>>

    @Query("SELECT COUNT(*) FROM wfh_requests WHERE status = 'Requested'")
    fun getPendingWFHCount(): Flow<Int>

    @Query("SELECT * FROM wfh_requests WHERE employeeId = :employeeId ORDER BY id DESC")
    fun getWFHForEmployee(employeeId: Long): Flow<List<WFHEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWFH(wfh: WFHEntity): Long

    @Query("UPDATE wfh_requests SET status = :status WHERE id = :id")
    suspend fun updateWFHStatus(id: Long, status: String): Int
}
