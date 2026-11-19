package com.example.hr_employeemanagementsystem.data.local.dao

import androidx.room.*
import com.example.hr_employeemanagementsystem.data.model.EmployeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY displayName ASC")
    fun getAllEmployees(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getEmployeeById(id: Long): EmployeeEntity?

    @Query("SELECT * FROM employees WHERE isEnrolled = 1")
    suspend fun getEnrolledEmployees(): List<EmployeeEntity>

    @Query("SELECT * FROM employees WHERE trackEmployee = 1")
    fun getTrackedEmployees(): Flow<List<EmployeeEntity>>

    @Query("SELECT COUNT(*) FROM employees WHERE active = 1")
    fun getActiveEmployeeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<EmployeeEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity): Long

    @Update
    suspend fun updateEmployee(employee: EmployeeEntity): Int

    @Delete
    suspend fun deleteEmployee(employee: EmployeeEntity): Int

    @Query("UPDATE employees SET isEnrolled = 1, faceDescriptor = :descriptor WHERE id = :employeeId")
    suspend fun updateFaceDescriptor(employeeId: Long, descriptor: String): Int
}
