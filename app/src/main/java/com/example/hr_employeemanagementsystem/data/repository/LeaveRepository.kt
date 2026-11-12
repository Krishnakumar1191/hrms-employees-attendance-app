package com.example.hr_employeemanagementsystem.data.repository

import com.example.hr_employeemanagementsystem.data.local.dao.LeaveDao
import com.example.hr_employeemanagementsystem.data.model.LeaveEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

interface LeaveRepository {
    fun getAllLeaves(): Flow<List<LeaveEntity>>
    fun getLeavesByStatus(status: String): Flow<List<LeaveEntity>>
    fun getLeavesForEmployee(employeeId: Long): Flow<List<LeaveEntity>>
    fun getPendingLeaveCount(): Flow<Int>
    suspend fun applyLeave(employeeId: Long, employeeName: String, leaveType: String, startDate: String, endDate: String, totalDays: Double, reason: String): Long
    suspend fun approveLeave(id: Long)
    suspend fun rejectLeave(id: Long)
    suspend fun revokeLeave(id: Long)
}

class LeaveRepositoryImpl(
    private val leaveDao: LeaveDao
) : LeaveRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun getAllLeaves(): Flow<List<LeaveEntity>> = leaveDao.getAllLeaves()

    override fun getLeavesByStatus(status: String): Flow<List<LeaveEntity>> = leaveDao.getLeavesByStatus(status)

    override fun getLeavesForEmployee(employeeId: Long): Flow<List<LeaveEntity>> = leaveDao.getLeavesForEmployee(employeeId)

    override fun getPendingLeaveCount(): Flow<Int> = leaveDao.getPendingLeaveCount()

    override suspend fun applyLeave(
        employeeId: Long,
        employeeName: String,
        leaveType: String,
        startDate: String,
        endDate: String,
        totalDays: Double,
        reason: String
    ): Long {
        val leave = LeaveEntity(
            employeeId = employeeId,
            employeeName = employeeName,
            leaveType = leaveType,
            startDate = startDate,
            endDate = endDate,
            totalDays = totalDays,
            status = "Requested",
            reason = reason,
            createdAt = dateFormat.format(Date())
        )
        return leaveDao.insertLeave(leave)
    }

    override suspend fun approveLeave(id: Long) { leaveDao.updateLeaveStatus(id, "Approved") }

    override suspend fun rejectLeave(id: Long) { leaveDao.updateLeaveStatus(id, "Rejected") }

    override suspend fun revokeLeave(id: Long) { leaveDao.updateLeaveStatus(id, "Revoked") }
}
