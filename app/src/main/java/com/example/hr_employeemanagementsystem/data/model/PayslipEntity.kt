package com.example.hr_employeemanagementsystem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payslips")
data class PayslipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val monthYear: String, // e.g. "October 2026"
    val basicSalary: Double,
    val allowances: Double,
    val deductions: Double,
    val netSalary: Double,
    val pdfUrl: String? = null
)
