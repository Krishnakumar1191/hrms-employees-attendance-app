package com.example.hr_employeemanagementsystem.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.hr_employeemanagementsystem.data.local.dao.*
import com.example.hr_employeemanagementsystem.data.model.*

@Database(
    entities = [
        EmployeeEntity::class,
        AttendanceEntity::class,
        LeaveEntity::class,
        WFHEntity::class,
        EventEntity::class,
        PayslipEntity::class,
        LocationLogEntity::class,
        CompanyEntity::class,
        DepartmentEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun leaveDao(): LeaveDao
    abstract fun wfhDao(): WFHDao
    abstract fun eventDao(): EventDao
    abstract fun payslipDao(): PayslipDao
    abstract fun locationLogDao(): LocationLogDao
    abstract fun companyDao(): CompanyDao
    abstract fun departmentDao(): DepartmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, companyName: String = "default"): AppDatabase {
            val dbName = if (companyName.isBlank() || companyName == "default") {
                "hr_management_db"
            } else {
                "${companyName.lowercase().replace(Regex("[^a-z0-9]"), "_")}_hr_db"
            }
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    dbName
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
