package com.example.hr_employeemanagementsystem.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.hr_employeemanagementsystem.data.local.db.AppDatabase
import com.example.hr_employeemanagementsystem.data.local.preferences.UserPreferencesRepository
import com.example.hr_employeemanagementsystem.data.repository.*
import com.example.hr_employeemanagementsystem.ui.components.HRSideDrawer
import com.example.hr_employeemanagementsystem.ui.components.HRTopBar
import com.example.hr_employeemanagementsystem.ui.components.QuickAccessGridDialog
import com.example.hr_employeemanagementsystem.ui.screens.attendances.AttendancesScreen
import com.example.hr_employeemanagementsystem.ui.screens.auth.*
import com.example.hr_employeemanagementsystem.ui.screens.dashboard.ManagerDashboardScreen
import com.example.hr_employeemanagementsystem.ui.screens.employees.EmployeeDetailScreen
import com.example.hr_employeemanagementsystem.ui.screens.employees.EmployeeListScreen
import com.example.hr_employeemanagementsystem.ui.screens.enroll.FaceEnrollScreen
import com.example.hr_employeemanagementsystem.ui.screens.events.EventsScreen
import com.example.hr_employeemanagementsystem.ui.screens.kiosk.KioskHomeScreen
import com.example.hr_employeemanagementsystem.ui.screens.leaves.LeavesScreen
import com.example.hr_employeemanagementsystem.ui.screens.payslip.PayslipsScreen
import com.example.hr_employeemanagementsystem.ui.screens.settings.ProfileScreen
import com.example.hr_employeemanagementsystem.ui.screens.settings.SettingsScreen
import com.example.hr_employeemanagementsystem.ui.screens.settings.UserManualScreen
import com.example.hr_employeemanagementsystem.ui.screens.wfh.WFHScreen
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    navController: NavHostController,
    db: AppDatabase,
    prefs: UserPreferencesRepository,
    authRepository: AuthRepository,
    employeeRepository: EmployeeRepository,
    attendanceRepository: AttendanceRepository,
    leaveRepository: LeaveRepository,
    wfhRepository: WFHRepository,
    locationRepository: LocationRepository
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.LoginCredentials.route

    val isLoggedIn by prefs.isLoggedInFlow.collectAsState(initial = false)
    val isAdmin by prefs.isAdminFlow.collectAsState(initial = false)
    val currentUserId by prefs.userIdFlow.collectAsState(initial = 1L)
    val username by prefs.usernameFlow.collectAsState(initial = "User")
    val storedPin by prefs.pinFlow.collectAsState(initial = null)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showQuickAccessDialog by remember { mutableStateOf(false) }

    val startDestination = remember(isLoggedIn, storedPin, isAdmin) {
        when {
            !isLoggedIn -> Screen.LoginCredentials.route
            storedPin.isNullOrBlank() -> Screen.PinSetup.route
            isAdmin -> Screen.ManagerDashboard.route
            else -> Screen.KioskHome.route
        }
    }

    val isAuthRoute = currentRoute in listOf(
        Screen.LoginCredentials.route,
        Screen.LoginOtp.route,
        Screen.PinSetup.route,
        Screen.PinLogin.route,
        Screen.CompanySelect.route
    )

    HRSideDrawer(
        currentRoute = currentRoute,
        isAdmin = isAdmin,
        drawerState = drawerState,
        gesturesEnabled = !isAuthRoute,
        onNavigate = { route ->
            scope.launch { drawerState.close() }
            navController.navigate(route)
        },
        onLogout = {
            scope.launch {
                authRepository.logout()
                drawerState.close()
                navController.navigate(Screen.LoginCredentials.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (!isAuthRoute) {
                    HRTopBar(
                        title = when (currentRoute) {
                            Screen.ManagerDashboard.route -> "Manager Dashboard"
                            Screen.Leaves.route -> "Leave Requests"
                            Screen.WorkFromHome.route -> "Work From Home"
                            Screen.EmployeeList.route -> "Employee Directory"
                            Screen.KioskHome.route -> "Face Attendance Kiosk"
                            Screen.Attendances.route -> "Attendance History"
                            Screen.Events.route -> "Events & Holidays"
                            Screen.Payslips.route -> "Payslips"
                            Screen.Settings.route -> "Settings"
                            Screen.Profile.route -> "User Profile"
                            Screen.UserManual.route -> "User Guide"
                            else -> "HR Management"
                        },
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onQuickAccessClick = { showQuickAccessDialog = true },
                        onNotificationClick = { },
                        onProfileClick = { navController.navigate(Screen.Profile.route) }
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(if (isAuthRoute) PaddingValues(0.dp) else padding)) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    // Auth screens
                    composable(Screen.LoginCredentials.route) {
                        LoginCredentialsScreen(
                            authRepository = authRepository,
                            onLoginSuccess = { admin ->
                                val target = if (storedPin.isNullOrBlank()) {
                                    Screen.PinSetup.route
                                } else if (admin) {
                                    Screen.ManagerDashboard.route
                                } else {
                                    Screen.KioskHome.route
                                }
                                navController.navigate(target) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigateOtp = { navController.navigate(Screen.LoginOtp.route) }
                        )
                    }
                    composable(Screen.LoginOtp.route) {
                        LoginOtpScreen(
                            authRepository = authRepository,
                            onLoginSuccess = {
                                if (storedPin.isNullOrBlank()) {
                                    navController.navigate(Screen.PinSetup.route)
                                } else {
                                    navController.navigate(Screen.CompanySelect.route)
                                }
                            },
                            onNavigateCredentials = { navController.navigate(Screen.LoginCredentials.route) }
                        )
                    }
                    composable(Screen.PinSetup.route) {
                        PinSetupScreen(
                            authRepository = authRepository,
                            onPinSet = {
                                navController.navigate(Screen.CompanySelect.route)
                            }
                        )
                    }
                    composable(Screen.PinLogin.route) {
                        PinLoginScreen(
                            authRepository = authRepository,
                            onPinSuccess = {
                                val target = if (isAdmin) Screen.ManagerDashboard.route else Screen.KioskHome.route
                                navController.navigate(target) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onForceReAuth = { navController.navigate(Screen.LoginCredentials.route) }
                        )
                    }
                    composable(Screen.CompanySelect.route) {
                        CompanySelectScreen(
                            authRepository = authRepository,
                            onCompanySelected = {
                                val target = if (isAdmin) Screen.ManagerDashboard.route else Screen.KioskHome.route
                                navController.navigate(target) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    // Main App screens
                    composable(Screen.ManagerDashboard.route) {
                        ManagerDashboardScreen(
                            leaveRepository = leaveRepository,
                            wfhRepository = wfhRepository,
                            employeeRepository = employeeRepository,
                            onNavigateLeaves = { navController.navigate(Screen.Leaves.route) },
                            onNavigateWFH = { navController.navigate(Screen.WorkFromHome.route) },
                            onNavigateEmployees = { navController.navigate(Screen.EmployeeList.route) },
                            onNavigateKiosk = { navController.navigate(Screen.KioskHome.route) }
                        )
                    }
                    composable(Screen.Leaves.route) {
                        LeavesScreen(
                            leaveRepository = leaveRepository,
                            currentUserId = currentUserId,
                            username = username
                        )
                    }
                    composable(Screen.WorkFromHome.route) {
                        WFHScreen(
                            wfhRepository = wfhRepository,
                            currentUserId = currentUserId,
                            username = username
                        )
                    }
                    composable(Screen.EmployeeList.route) {
                        EmployeeListScreen(
                            employeeRepository = employeeRepository,
                            onSelectEmployee = { id -> navController.navigate(Screen.EmployeeDetail.createRoute(id)) }
                        )
                    }
                    composable(
                        route = Screen.EmployeeDetail.route,
                        arguments = listOf(navArgument("employeeId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val empId = backStackEntry.arguments?.getLong("employeeId") ?: 1L
                        EmployeeDetailScreen(
                            employeeId = empId,
                            employeeRepository = employeeRepository,
                            locationRepository = locationRepository,
                            onNavigateEnroll = { id -> navController.navigate(Screen.FaceEnroll.createRoute(id)) }
                        )
                    }
                    composable(
                        route = Screen.FaceEnroll.route,
                        arguments = listOf(navArgument("employeeId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val empId = backStackEntry.arguments?.getLong("employeeId") ?: 1L
                        FaceEnrollScreen(
                            employeeId = empId,
                            employeeRepository = employeeRepository,
                            onEnrollSuccess = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.KioskHome.route) {
                        KioskHomeScreen(
                            attendanceRepository = attendanceRepository,
                            employeeRepository = employeeRepository
                        )
                    }
                    composable(Screen.Attendances.route) {
                        AttendancesScreen(
                            attendanceRepository = attendanceRepository,
                            isAdmin = isAdmin,
                            currentUserId = currentUserId
                        )
                    }
                    composable(Screen.Events.route) {
                        EventsScreen(db = db)
                    }
                    composable(Screen.Payslips.route) {
                        PayslipsScreen(db = db, currentUserId = currentUserId)
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            prefs = prefs,
                            authRepository = authRepository,
                            onNavigateChangePin = { navController.navigate(Screen.PinSetup.route) },
                            onLogout = {
                                scope.launch {
                                    authRepository.logout()
                                    navController.navigate(Screen.LoginCredentials.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }
                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            db = db,
                            currentUserId = currentUserId,
                            username = username,
                            isAdmin = isAdmin
                        )
                    }
                    composable(Screen.UserManual.route) {
                        UserManualScreen()
                    }
                }
            }
        }

        if (showQuickAccessDialog) {
            QuickAccessGridDialog(
                onDismiss = { showQuickAccessDialog = false },
                onNavigateLeaves = { navController.navigate(Screen.Leaves.route) },
                onNavigateWFH = { navController.navigate(Screen.WorkFromHome.route) },
                onNavigateEmployees = { navController.navigate(Screen.EmployeeList.route) },
                onNavigateKiosk = { navController.navigate(Screen.KioskHome.route) },
                onNavigateAttendances = { navController.navigate(Screen.Attendances.route) },
                onNavigateEvents = { navController.navigate(Screen.Events.route) },
                onNavigatePayslips = { navController.navigate(Screen.Payslips.route) },
                onNavigateSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
    }
}
