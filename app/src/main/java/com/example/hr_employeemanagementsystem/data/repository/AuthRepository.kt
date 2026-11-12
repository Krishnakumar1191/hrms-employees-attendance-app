package com.example.hr_employeemanagementsystem.data.repository

import com.example.hr_employeemanagementsystem.data.local.dao.EmployeeDao
import com.example.hr_employeemanagementsystem.data.local.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    val isAdmin: Flow<Boolean>
    val username: Flow<String>
    val userId: Flow<Long>
    val storedPin: Flow<String?>
    val selectedCompany: Flow<String>

    suspend fun loginWithCredentials(username: String, password: String): Result<Boolean>
    suspend fun verifyOtp(identifier: String, otp: String): Result<Boolean>
    suspend fun setupPin(pin: String)
    suspend fun verifyPin(pin: String): Boolean
    suspend fun selectCompany(company: String)
    suspend fun logout()
}

class AuthRepositoryImpl(
    private val prefs: UserPreferencesRepository,
    private val employeeDao: EmployeeDao
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> = prefs.isLoggedInFlow
    override val isAdmin: Flow<Boolean> = prefs.isAdminFlow
    override val username: Flow<String> = prefs.usernameFlow
    override val userId: Flow<Long> = prefs.userIdFlow
    override val storedPin: Flow<String?> = prefs.pinFlow
    override val selectedCompany: Flow<String> = prefs.selectedCompanyFlow

    override suspend fun loginWithCredentials(username: String, password: String): Result<Boolean> {
        return if (username.equals("admin", ignoreCase = true) && password == "admin") {
            prefs.saveLoginSession(username = "Admin User", userId = 1L, isAdmin = true)
            Result.success(true)
        } else if (username.equalsIgnoreCase("john") || username.contains("employee")) {
            prefs.saveLoginSession(username = "John Doe", userId = 2L, isAdmin = false)
            Result.success(true)
        } else {
            // Check in local database
            val employees = employeeDao.getAllEmployees().first()
            val matched = employees.find { it.email.contains(username, ignoreCase = true) || it.displayName.contains(username, ignoreCase = true) }
            if (matched != null) {
                prefs.saveLoginSession(username = matched.displayName, userId = matched.id, isAdmin = matched.isAdmin)
                Result.success(true)
            } else {
                Result.failure(Exception("Invalid credentials. Try 'admin' / 'admin' or 'john' / 'john'"))
            }
        }
    }

    private fun String.equalsIgnoreCase(other: String) = this.equals(other, ignoreCase = true)

    override suspend fun verifyOtp(identifier: String, otp: String): Result<Boolean> {
        return if (otp == "123456" || otp.length == 6) {
            prefs.saveLoginSession(username = "User ($identifier)", userId = 2L, isAdmin = false)
            Result.success(true)
        } else {
            Result.failure(Exception("Invalid OTP code. Use '123456' for test."))
        }
    }

    override suspend fun setupPin(pin: String) {
        prefs.savePin(pin)
    }

    override suspend fun verifyPin(pin: String): Boolean {
        val currentPin = storedPin.first()
        return currentPin != null && currentPin == pin
    }

    override suspend fun selectCompany(company: String) {
        prefs.saveCompany(company)
    }

    override suspend fun logout() {
        prefs.clearSession()
    }
}
