package com.example.hr_employeemanagementsystem.data.repository

import com.example.hr_employeemanagementsystem.data.local.dao.AttendanceDao
import com.example.hr_employeemanagementsystem.data.model.AttendanceEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

interface AttendanceRepository {
    fun getAllAttendances(): Flow<List<AttendanceEntity>>
    fun getAttendancesForEmployee(employeeId: Long): Flow<List<AttendanceEntity>>
    suspend fun getActiveAttendance(employeeId: Long): AttendanceEntity?
    fun getTodayAttendanceCount(datePrefix: String): Flow<Int>
    suspend fun clockIn(employeeId: Long, employeeName: String, photoUri: String?, employeePhotoUri: String?, lat: Double?, lng: Double?): AttendanceEntity
    suspend fun clockOut(activeAttendance: AttendanceEntity, lat: Double?, lng: Double?, photoUri: String?): AttendanceEntity
}

class AttendanceRepositoryImpl(
    private val attendanceDao: AttendanceDao
) : AttendanceRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun getAllAttendances(): Flow<List<AttendanceEntity>> = attendanceDao.getAllAttendances()

    override fun getAttendancesForEmployee(employeeId: Long): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendancesForEmployee(employeeId)

    override suspend fun getActiveAttendance(employeeId: Long): AttendanceEntity? =
        attendanceDao.getActiveAttendance(employeeId)

    override fun getTodayAttendanceCount(datePrefix: String): Flow<Int> =
        attendanceDao.getTodayAttendanceCount(datePrefix)

    override suspend fun clockIn(employeeId: Long, employeeName: String, photoUri: String?, employeePhotoUri: String?, lat: Double?, lng: Double?): AttendanceEntity {
        val now = dateFormat.format(Date())
        val newAttendance = AttendanceEntity(
            employeeId = employeeId,
            employeeName = employeeName,
            checkIn = now,
            checkOut = null,
            workedHours = 0.0,
            status = "Present",
            recognizedFace = true,
            capturedPhotoUri = photoUri,
            employeePhotoUri = employeePhotoUri,
            checkInLat = lat,
            checkInLng = lng
        )
        val id = attendanceDao.insertAttendance(newAttendance)
        return newAttendance.copy(id = id)
    }

    override suspend fun clockOut(activeAttendance: AttendanceEntity, lat: Double?, lng: Double?, photoUri: String?): AttendanceEntity {
        val now = dateFormat.format(Date())
        val checkInDate = try { dateFormat.parse(activeAttendance.checkIn) } catch (e: Exception) { Date() }
        val checkOutDate = Date()
        val diffMillis = checkOutDate.time - (checkInDate?.time ?: checkOutDate.time)
        val workedHours = String.format(Locale.US, "%.2f", diffMillis / (1000.0 * 60 * 60)).toDouble()

        attendanceDao.clockOut(activeAttendance.id, now, lat, lng, workedHours, photoUri)
        return activeAttendance.copy(
            checkOut = now,
            checkOutLat = lat,
            checkOutLng = lng,
            workedHours = workedHours,
            checkOutPhotoUri = photoUri
        )
    }
}
