package com.example.hr_employeemanagementsystem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_logs")
data class LocationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val timestamp: String,
    val latitude: Double,
    val longitude: Double,
    val isSynced: Boolean = false
)
