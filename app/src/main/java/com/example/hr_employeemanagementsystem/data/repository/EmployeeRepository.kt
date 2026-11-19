package com.example.hr_employeemanagementsystem.data.repository

import com.example.hr_employeemanagementsystem.data.local.dao.EmployeeDao
import com.example.hr_employeemanagementsystem.data.model.EmployeeEntity
import kotlinx.coroutines.flow.Flow

interface EmployeeRepository {
    fun getAllEmployees(): Flow<List<EmployeeEntity>>
    suspend fun getEmployeeById(id: Long): EmployeeEntity?
    suspend fun getEnrolledEmployees(): List<EmployeeEntity>
    fun getTrackedEmployees(): Flow<List<EmployeeEntity>>
    fun getActiveEmployeeCount(): Flow<Int>
    suspend fun saveFaceEnrollment(employeeId: Long, faceDescriptor: String)
    suspend fun addEmployee(employee: EmployeeEntity): Long
    suspend fun updateEmployee(employee: EmployeeEntity)
    suspend fun deleteEmployee(employee: EmployeeEntity)
}

class EmployeeRepositoryImpl(
    private val employeeDao: EmployeeDao
) : EmployeeRepository {

    override fun getAllEmployees(): Flow<List<EmployeeEntity>> = employeeDao.getAllEmployees()

    override suspend fun getEmployeeById(id: Long): EmployeeEntity? = employeeDao.getEmployeeById(id)

    override suspend fun getEnrolledEmployees(): List<EmployeeEntity> = employeeDao.getEnrolledEmployees()

    override fun getTrackedEmployees(): Flow<List<EmployeeEntity>> = employeeDao.getTrackedEmployees()

    override fun getActiveEmployeeCount(): Flow<Int> = employeeDao.getActiveEmployeeCount()

    override suspend fun saveFaceEnrollment(employeeId: Long, faceDescriptor: String) {
        employeeDao.updateFaceDescriptor(employeeId, faceDescriptor)
    }

    override suspend fun addEmployee(employee: EmployeeEntity): Long {
        return employeeDao.insertEmployee(employee)
    }

    override suspend fun updateEmployee(employee: EmployeeEntity) {
        employeeDao.updateEmployee(employee)
    }

    override suspend fun deleteEmployee(employee: EmployeeEntity) {
        employeeDao.deleteEmployee(employee)
    }
}
