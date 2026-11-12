package com.example.hr_employeemanagementsystem.data.local.mock

import com.example.hr_employeemanagementsystem.data.local.db.AppDatabase
import com.example.hr_employeemanagementsystem.data.model.*
import kotlinx.coroutines.flow.first

class MockDataInitializer(private val db: AppDatabase) {

    suspend fun seedInitialDataIfEmpty() {
        val existingEmployees = db.employeeDao().getAllEmployees().first()
        if (existingEmployees.isNotEmpty()) return // Already seeded

        // Seed Employees
        val mockEmployees = listOf(
            EmployeeEntity(
                id = 1L,
                displayName = "Admin User",
                email = "admin@company.com",
                phone = "+1 555-0100",
                jobTitle = "HR Director",
                department = "Human Resources",
                companyId = 1L,
                companyName = "Headquarters",
                isEnrolled = true,
                trackEmployee = false,
                isAdmin = true
            ),
            EmployeeEntity(
                id = 2L,
                displayName = "John Doe",
                email = "john.doe@company.com",
                phone = "+1 555-0101",
                jobTitle = "Senior Android Developer",
                department = "Engineering",
                companyId = 1L,
                companyName = "Headquarters",
                isEnrolled = true,
                trackEmployee = true,
                isAdmin = false
            ),
            EmployeeEntity(
                id = 3L,
                displayName = "Sarah Smith",
                email = "sarah.smith@company.com",
                phone = "+1 555-0102",
                jobTitle = "UI/UX Designer",
                department = "Design",
                companyId = 1L,
                companyName = "Headquarters",
                isEnrolled = false,
                trackEmployee = false,
                isAdmin = false
            ),
            EmployeeEntity(
                id = 4L,
                displayName = "Alex Johnson",
                email = "alex.j@company.com",
                phone = "+1 555-0103",
                jobTitle = "QA Lead",
                department = "Quality Assurance",
                companyId = 1L,
                companyName = "Headquarters",
                isEnrolled = true,
                trackEmployee = true,
                isAdmin = false
            )
        )
        db.employeeDao().insertEmployees(mockEmployees)

        // Seed Sample Leaves
        val mockLeaves = listOf(
            LeaveEntity(
                employeeId = 2L,
                employeeName = "John Doe",
                leaveType = "Casual Leave",
                startDate = "2026-11-01",
                endDate = "2026-11-02",
                totalDays = 2.0,
                status = "Requested",
                reason = "Family occasion",
                createdAt = "2026-10-24"
            ),
            LeaveEntity(
                employeeId = 3L,
                employeeName = "Sarah Smith",
                leaveType = "Sick Leave",
                startDate = "2026-10-25",
                endDate = "2026-10-25",
                totalDays = 1.0,
                status = "Approved",
                reason = "Doctor appointment",
                createdAt = "2026-10-23"
            )
        )
        mockLeaves.forEach { db.leaveDao().insertLeave(it) }

        // Seed Sample WFH Requests
        val mockWFH = listOf(
            WFHEntity(
                employeeId = 4L,
                employeeName = "Alex Johnson",
                startDate = "2026-10-28",
                endDate = "2026-10-29",
                totalDays = 2.0,
                status = "Requested",
                reason = "Home renovation",
                createdAt = "2026-10-24"
            )
        )
        mockWFH.forEach { db.wfhDao().insertWFH(it) }

        // Seed Events
        val mockEvents = listOf(
            EventEntity(
                title = "Diwali Holiday",
                date = "2026-11-01",
                description = "Company-wide official holiday",
                type = "Holiday"
            ),
            EventEntity(
                title = "Sarah's Birthday",
                date = "2026-10-29",
                description = "Celebrate Sarah's birthday in breakroom",
                type = "Birthday"
            ),
            EventEntity(
                title = "Q4 All-Hands Meeting",
                date = "2026-11-05",
                description = "Quarterly roadmap & achievements review",
                type = "Meeting"
            )
        )
        db.eventDao().insertEvents(mockEvents)

        // Seed Payslips
        val mockPayslips = listOf(
            PayslipEntity(
                employeeId = 2L,
                monthYear = "September 2026",
                basicSalary = 5000.0,
                allowances = 1200.0,
                deductions = 400.0,
                netSalary = 5800.0
            ),
            PayslipEntity(
                employeeId = 2L,
                monthYear = "August 2026",
                basicSalary = 5000.0,
                allowances = 1200.0,
                deductions = 400.0,
                netSalary = 5800.0
            )
        )
        db.payslipDao().insertPayslips(mockPayslips)
    }
}
