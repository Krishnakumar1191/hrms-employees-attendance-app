package com.example.hr_employeemanagementsystem.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object LoginCredentials : Screen("login_credentials")
    object LoginOtp : Screen("login_otp")
    object PinSetup : Screen("pin_setup")
    object PinLogin : Screen("pin_login")
    object CompanySelect : Screen("company_select")
    object CompanyRegister : Screen("company_register")
    object ManagerDashboard : Screen("manager_dashboard")
    object Leaves : Screen("leaves")
    object WorkFromHome : Screen("work_from_home")
    object EmployeeList : Screen("employee_list")
    object EmployeeDetail : Screen("employee_detail/{employeeId}") {
        fun createRoute(employeeId: Long) = "employee_detail/$employeeId"
    }
    object FaceEnroll : Screen("face_enroll/{employeeId}") {
        fun createRoute(employeeId: Long) = "face_enroll/$employeeId"
    }
    object KioskHome : Screen("kiosk_home")
    object Attendances : Screen("attendances")
    object Events : Screen("events")
    object Payslips : Screen("payslips")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
    object Reports : Screen("reports")
    object UserManual : Screen("user_manual")
}
