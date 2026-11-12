package com.example.hr_employeemanagementsystem.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.hr_employeemanagementsystem.data.repository.EmployeeRepository
import com.example.hr_employeemanagementsystem.data.repository.LeaveRepository
import com.example.hr_employeemanagementsystem.data.repository.WFHRepository
import com.example.hr_employeemanagementsystem.ui.components.MetricCard

@Composable
fun ManagerDashboardScreen(
    leaveRepository: LeaveRepository,
    wfhRepository: WFHRepository,
    employeeRepository: EmployeeRepository,
    onNavigateLeaves: () -> Unit,
    onNavigateWFH: () -> Unit,
    onNavigateEmployees: () -> Unit,
    onNavigateKiosk: () -> Unit
) {
    val pendingLeaves by leaveRepository.getPendingLeaveCount().collectAsState(initial = 0)
    val pendingWFH by wfhRepository.getPendingWFHCount().collectAsState(initial = 0)
    val activeEmployees by employeeRepository.getActiveEmployeeCount().collectAsState(initial = 0)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Manager Dashboard Overview", style = MaterialTheme.typography.titleLarge)
        }

        item {
            MetricCard(
                title = "Pending Leave Requests",
                count = "$pendingLeaves Requests",
                icon = Icons.Default.EventBusy,
                containerColor = Color(0xFFFCE4EC),
                onClick = onNavigateLeaves
            )
        }

        item {
            MetricCard(
                title = "Work From Home Requests",
                count = "$pendingWFH Requests",
                icon = Icons.Default.HomeWork,
                containerColor = Color(0xFFE1F5FE),
                onClick = onNavigateWFH
            )
        }

        item {
            MetricCard(
                title = "Active Staff Roster",
                count = "$activeEmployees Employees",
                icon = Icons.Default.Group,
                containerColor = Color(0xFFE8F5E9),
                onClick = onNavigateEmployees
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(Icons.Default.Cake, contentDescription = "Birthday", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Today's Birthdays", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🎂 Sarah Smith (UI/UX Designer) - Happy Birthday!", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(Icons.Default.CameraFront, contentDescription = "Kiosk Mode")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Attendance Kiosk Station", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Launch biometric camera kiosk mode for employees to clock in/out.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onNavigateKiosk) {
                        Text("Open Face Kiosk")
                    }
                }
            }
        }
    }
}
