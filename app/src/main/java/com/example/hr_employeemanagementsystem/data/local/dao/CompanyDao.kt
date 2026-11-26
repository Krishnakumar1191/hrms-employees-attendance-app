package com.example.hr_employeemanagementsystem.data.local.dao

import androidx.room.*
import com.example.hr_employeemanagementsystem.data.model.CompanyEntity

@Dao
interface CompanyDao {
    @Query("SELECT * FROM companies WHERE companyName = :name LIMIT 1")
    suspend fun getCompany(name: String): CompanyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: CompanyEntity): Long
}
