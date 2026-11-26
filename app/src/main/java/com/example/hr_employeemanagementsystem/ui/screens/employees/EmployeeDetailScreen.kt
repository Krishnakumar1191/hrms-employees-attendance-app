package com.example.hr_employeemanagementsystem.ui.screens.employees

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.hr_employeemanagementsystem.data.model.EmployeeEntity
import com.example.hr_employeemanagementsystem.data.repository.AttendanceRepository
import com.example.hr_employeemanagementsystem.data.repository.EmployeeRepository
import com.example.hr_employeemanagementsystem.data.repository.LocationRepository

@Composable
fun EmployeeDetailScreen(
    employeeId: Long,
    employeeRepository: EmployeeRepository,
    attendanceRepository: AttendanceRepository,
    locationRepository: LocationRepository,
    onNavigateEnroll: (employeeId: Long) -> Unit
) {
    val context = LocalContext.current
    var employee by remember { mutableStateOf<EmployeeEntity?>(null) }

    LaunchedEffect(employeeId) {
        employee = employeeRepository.getEmployeeById(employeeId)
    }

    val emp = employee
    if (emp == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (emp.photoUri != null) {
                        coil.compose.AsyncImage(
                            model = emp.photoUri,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    Column {
                        Text(emp.displayName, style = MaterialTheme.typography.headlineSmall)
                        Text("${emp.jobTitle} • ${emp.department}", style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("📧 ${emp.email}", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Text("📞 ${emp.phone}", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Text("🏢 ${emp.companyName}", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Biometric Face Registration", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (emp.isEnrolled) "Face Descriptor Enrolled in Local Database" else "Face Not Enrolled",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onNavigateEnroll(emp.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraFront, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (emp.isEnrolled) "Update Face Enrollment" else "Enroll Face Photo")
                }
            }
        }
    }
}
