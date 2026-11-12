package com.example.hr_employeemanagementsystem.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hr_employeemanagementsystem.data.local.db.AppDatabase
import com.example.hr_employeemanagementsystem.data.model.EmployeeEntity

@Composable
fun ProfileScreen(
    db: AppDatabase,
    currentUserId: Long,
    username: String,
    isAdmin: Boolean
) {
    var employee by remember { mutableStateOf<EmployeeEntity?>(null) }

    LaunchedEffect(currentUserId) {
        employee = db.employeeDao().getEmployeeById(currentUserId)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        Text(employee?.displayName ?: username, style = MaterialTheme.typography.headlineMedium)
        Text(if (isAdmin) "Role: HR Director / Administrator" else "Role: Employee", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Department: ${employee?.department ?: "Human Resources"}")
                Spacer(modifier = Modifier.height(6.dp))
                Text("Job Position: ${employee?.jobTitle ?: "HR Manager"}")
                Spacer(modifier = Modifier.height(6.dp))
                Text("Email: ${employee?.email ?: "admin@company.com"}")
                Spacer(modifier = Modifier.height(6.dp))
                Text("Branch: ${employee?.companyName ?: "Main Headquarters"}")
            }
        }
    }
}
