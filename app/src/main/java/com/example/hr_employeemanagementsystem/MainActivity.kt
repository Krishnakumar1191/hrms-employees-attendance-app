package com.example.hr_employeemanagementsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.hr_employeemanagementsystem.data.local.db.AppDatabase
import com.example.hr_employeemanagementsystem.data.local.mock.MockDataInitializer
import com.example.hr_employeemanagementsystem.data.local.preferences.UserPreferencesRepository
import com.example.hr_employeemanagementsystem.data.repository.*
import com.example.hr_employeemanagementsystem.ui.navigation.NavGraph
import com.example.hr_employeemanagementsystem.ui.theme.HREmployeeManagementSystemTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HREmployeeManagementSystemTheme {
                val db = remember { AppDatabase.getDatabase(applicationContext) }
                val prefs = remember { UserPreferencesRepository(applicationContext) }

                val authRepository = remember { AuthRepositoryImpl(prefs, db.employeeDao()) }
                val employeeRepository = remember { EmployeeRepositoryImpl(db.employeeDao()) }
                val attendanceRepository = remember { AttendanceRepositoryImpl(db.attendanceDao()) }
                val leaveRepository = remember { LeaveRepositoryImpl(db.leaveDao()) }
                val wfhRepository = remember { WFHRepositoryImpl(db.wfhDao()) }
                val locationRepository = remember { LocationRepositoryImpl(db.locationLogDao()) }

                LaunchedEffect(Unit) {
                    MockDataInitializer(db).seedInitialDataIfEmpty()
                }

                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        navController = navController,
                        db = db,
                        prefs = prefs,
                        authRepository = authRepository,
                        employeeRepository = employeeRepository,
                        attendanceRepository = attendanceRepository,
                        leaveRepository = leaveRepository,
                        wfhRepository = wfhRepository,
                        locationRepository = locationRepository
                    )
                }
            }
        }
    }
}
