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
        LocationLogEntity::class
    ],
    version = 4,
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

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hr_management_db"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
