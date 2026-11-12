package com.example.hr_employeemanagementsystem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wfh_requests")
data class WFHEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val employeeName: String,
    val startDate: String,
    val endDate: String,
    val totalDays: Double,
    val status: String = "Requested", // Requested, Approved, Rejected
    val reason: String,
    val createdAt: String
)
