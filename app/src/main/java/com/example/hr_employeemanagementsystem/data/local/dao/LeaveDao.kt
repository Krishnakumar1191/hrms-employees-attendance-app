package com.example.hr_employeemanagementsystem.data.local.dao

import androidx.room.*
import com.example.hr_employeemanagementsystem.data.model.LeaveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaveDao {
    @Query("SELECT * FROM leaves ORDER BY id DESC")
    fun getAllLeaves(): Flow<List<LeaveEntity>>

    @Query("SELECT * FROM leaves WHERE status = :status ORDER BY id DESC")
    fun getLeavesByStatus(status: String): Flow<List<LeaveEntity>>

    @Query("SELECT COUNT(*) FROM leaves WHERE status = 'Requested'")
    fun getPendingLeaveCount(): Flow<Int>

    @Query("SELECT * FROM leaves WHERE employeeId = :employeeId ORDER BY id DESC")
    fun getLeavesForEmployee(employeeId: Long): Flow<List<LeaveEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeave(leave: LeaveEntity): Long

    @Query("UPDATE leaves SET status = :status WHERE id = :id")
    suspend fun updateLeaveStatus(id: Long, status: String): Int
}
