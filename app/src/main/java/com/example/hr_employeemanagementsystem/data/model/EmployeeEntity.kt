package com.example.hr_employeemanagementsystem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val displayName: String,
    val email: String,
    val phone: String,
    val password: String = "",
    val jobTitle: String,
    val department: String,
    val companyId: Long,
    val companyName: String,
    val isEnrolled: Boolean = false,
    val faceDescriptor: String? = null, // Comma-separated 192 float embeddings
    val trackEmployee: Boolean = false,
    val isAdmin: Boolean = false,
    val active: Boolean = true,
    val photoUri: String? = null
)
