package com.example.hr_employeemanagementsystem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendances")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val employeeName: String,
    val checkIn: String, // ISO timestamp or formatted string
    val checkOut: String? = null,
    val workedHours: Double = 0.0,
    val status: String = "Present", // Present, Late, Half Day
    val recognizedFace: Boolean = true,
    val capturedPhotoUri: String? = null,
    val employeePhotoUri: String? = null,
    val checkOutPhotoUri: String? = null,
    val checkInLat: Double? = null,
    val checkInLng: Double? = null,
    val checkOutLat: Double? = null,
    val checkOutLng: Double? = null,
    val locationTrackLogs: String? = null // JSON array string of route coordinates
)
