package com.example.hr_employeemanagementsystem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leaves")
data class LeaveEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val employeeName: String,
    val leaveType: String, // Paid Leave, Sick Leave, Casual Leave
    val startDate: String,
    val endDate: String,
    val totalDays: Double,
    val status: String = "Requested", // Requested, Approved, Rejected, Revoked
    val reason: String,
    val createdAt: String
)
