package com.example.hr_employeemanagementsystem.data.local.dao

import androidx.room.*
import com.example.hr_employeemanagementsystem.data.model.LocationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationLogDao {
    @Query("SELECT * FROM location_logs WHERE employeeId = :employeeId ORDER BY timestamp ASC")
    fun getLocationLogsForEmployee(employeeId: Long): Flow<List<LocationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationLog(log: LocationLogEntity): Long
}
