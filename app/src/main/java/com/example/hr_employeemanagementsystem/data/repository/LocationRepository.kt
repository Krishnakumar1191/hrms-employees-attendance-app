package com.example.hr_employeemanagementsystem.data.repository

import com.example.hr_employeemanagementsystem.data.local.dao.LocationLogDao
import com.example.hr_employeemanagementsystem.data.model.LocationLogEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

interface LocationRepository {
    fun getLocationLogsForEmployee(employeeId: Long): Flow<List<LocationLogEntity>>
    suspend fun saveLocationLog(employeeId: Long, latitude: Double, longitude: Double)
}

class LocationRepositoryImpl(
    private val locationLogDao: LocationLogDao
) : LocationRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun getLocationLogsForEmployee(employeeId: Long): Flow<List<LocationLogEntity>> =
        locationLogDao.getLocationLogsForEmployee(employeeId)

    override suspend fun saveLocationLog(employeeId: Long, latitude: Double, longitude: Double) {
        val log = LocationLogEntity(
            employeeId = employeeId,
            timestamp = dateFormat.format(Date()),
            latitude = latitude,
            longitude = longitude
        )
        locationLogDao.insertLocationLog(log)
    }
}
