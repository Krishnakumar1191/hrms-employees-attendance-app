package com.example.hr_employeemanagementsystem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class CompanyEntity(
    @PrimaryKey val companyName: String,
    val address: String,
    val headquarters: String,
    val website: String,
    val adminUsername: String
)
