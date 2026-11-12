package com.example.hr_employeemanagementsystem.data.local.dao

import androidx.room.*
import com.example.hr_employeemanagementsystem.data.model.PayslipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PayslipDao {
    @Query("SELECT * FROM payslips WHERE employeeId = :employeeId ORDER BY id DESC")
    fun getPayslipsForEmployee(employeeId: Long): Flow<List<PayslipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayslips(payslips: List<PayslipEntity>): List<Long>
}
