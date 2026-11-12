package com.example.hr_employeemanagementsystem.data.repository

import com.example.hr_employeemanagementsystem.data.local.dao.WFHDao
import com.example.hr_employeemanagementsystem.data.model.WFHEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

interface WFHRepository {
    fun getAllWFHRequests(): Flow<List<WFHEntity>>
    fun getWFHByStatus(status: String): Flow<List<WFHEntity>>
    fun getWFHForEmployee(employeeId: Long): Flow<List<WFHEntity>>
    fun getPendingWFHCount(): Flow<Int>
    suspend fun applyWFH(employeeId: Long, employeeName: String, startDate: String, endDate: String, totalDays: Double, reason: String): Long
    suspend fun approveWFH(id: Long)
    suspend fun rejectWFH(id: Long)
    suspend fun revokeWFH(id: Long)
}

class WFHRepositoryImpl(
    private val wfhDao: WFHDao
) : WFHRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun getAllWFHRequests(): Flow<List<WFHEntity>> = wfhDao.getAllWFHRequests()

    override fun getWFHByStatus(status: String): Flow<List<WFHEntity>> = wfhDao.getWFHByStatus(status)

    override fun getWFHForEmployee(employeeId: Long): Flow<List<WFHEntity>> = wfhDao.getWFHForEmployee(employeeId)

    override fun getPendingWFHCount(): Flow<Int> = wfhDao.getPendingWFHCount()

    override suspend fun applyWFH(
        employeeId: Long,
        employeeName: String,
        startDate: String,
        endDate: String,
        totalDays: Double,
        reason: String
    ): Long {
        val wfh = WFHEntity(
            employeeId = employeeId,
            employeeName = employeeName,
            startDate = startDate,
            endDate = endDate,
            totalDays = totalDays,
            status = "Requested",
            reason = reason,
            createdAt = dateFormat.format(Date())
        )
        return wfhDao.insertWFH(wfh)
    }

    override suspend fun approveWFH(id: Long) { wfhDao.updateWFHStatus(id, "Approved") }

    override suspend fun rejectWFH(id: Long) { wfhDao.updateWFHStatus(id, "Rejected") }

    override suspend fun revokeWFH(id: Long) { wfhDao.updateWFHStatus(id, "Cancelled") }
}
