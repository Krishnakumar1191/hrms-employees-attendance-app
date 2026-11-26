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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.hr_employeemanagementsystem.data.local.db.AppDatabase
import com.example.hr_employeemanagementsystem.data.local.preferences.UserPreferencesRepository
import com.example.hr_employeemanagementsystem.data.repository.*
import com.example.hr_employeemanagementsystem.ui.navigation.NavGraph
import com.example.hr_employeemanagementsystem.ui.theme.HREmployeeManagementSystemTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        try {
            com.example.hr_employeemanagementsystem.ml.FaceNetModel(this)
            org.osmdroid.config.Configuration.getInstance().load(this, this.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE))
            org.osmdroid.config.Configuration.getInstance().userAgentValue = "HREmployeeManagementSystem/1.0"
        } catch(e: Exception) {}

        enableEdgeToEdge()

        setContent {
            HREmployeeManagementSystemTheme {
                val prefs = remember { UserPreferencesRepository(applicationContext) }
                val selectedCompany by prefs.selectedCompanyFlow.collectAsState(initial = "default")
                val db = remember(selectedCompany) { AppDatabase.getDatabase(applicationContext, selectedCompany) }

                val authRepository = remember { AuthRepositoryImpl(prefs, db.employeeDao()) }
                val employeeRepository = remember { EmployeeRepositoryImpl(db.employeeDao()) }
                val attendanceRepository = remember { AttendanceRepositoryImpl(db.attendanceDao()) }
                val leaveRepository = remember { LeaveRepositoryImpl(db.leaveDao()) }
                val wfhRepository = remember { WFHRepositoryImpl(db.wfhDao()) }
                val locationRepository = remember { LocationRepositoryImpl(db.locationLogDao()) }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { _ ->
                    // Optionally handle granted/denied states
                }

                LaunchedEffect(Unit) {
                    val permissionsToRequest = mutableListOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissionLauncher.launch(permissionsToRequest.toTypedArray())
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
