package com.example.hr_employeemanagementsystem.data.local.dao

import androidx.room.*
import com.example.hr_employeemanagementsystem.data.model.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendances ORDER BY id DESC")
    fun getAllAttendances(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendances WHERE employeeId = :employeeId ORDER BY id DESC")
    fun getAttendancesForEmployee(employeeId: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendances WHERE employeeId = :employeeId AND checkOut IS NULL ORDER BY id DESC LIMIT 1")
    suspend fun getActiveAttendance(employeeId: Long): AttendanceEntity?

    @Query("SELECT COUNT(*) FROM attendances WHERE checkIn LIKE :datePrefix || '%'")
    fun getTodayAttendanceCount(datePrefix: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity): Int
}
