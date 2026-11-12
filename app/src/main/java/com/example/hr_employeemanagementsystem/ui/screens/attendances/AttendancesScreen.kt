package com.example.hr_employeemanagementsystem.ui.screens.attendances

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.hr_employeemanagementsystem.data.model.AttendanceEntity
import com.example.hr_employeemanagementsystem.data.repository.AttendanceRepository

@Composable
fun AttendancesScreen(
    attendanceRepository: AttendanceRepository,
    isAdmin: Boolean,
    currentUserId: Long
) {
    val allAttendances by attendanceRepository.getAllAttendances().collectAsState(initial = emptyList())
    val myAttendances by attendanceRepository.getAttendancesForEmployee(currentUserId).collectAsState(initial = emptyList())

    var showAll by remember { mutableStateOf(isAdmin) }
    var selectedAttendance by remember { mutableStateOf<AttendanceEntity?>(null) }

    val displayList = if (showAll && isAdmin) allAttendances else myAttendances

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (isAdmin) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (showAll) "Company All Attendances" else "My Personal Attendances", style = MaterialTheme.typography.titleLarge)
                FilterChip(
                    selected = showAll,
                    onClick = { showAll = !showAll },
                    label = { Text(if (showAll) "Viewing: All" else "Viewing: My Logs") }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Text("My Attendance History", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (displayList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No attendance logs recorded yet.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(displayList, key = { it.id }) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { selectedAttendance = log },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(log.employeeName, style = MaterialTheme.typography.titleMedium)
                                Badge(containerColor = Color(0xFF4CAF50)) {
                                    Text(log.status, color = Color.White, modifier = Modifier.padding(4.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Check-In: ${log.checkIn}", style = MaterialTheme.typography.bodyMedium)
                            Text("Check-Out: ${log.checkOut ?: "Active Shift"}", style = MaterialTheme.typography.bodySmall)
                            Text("Worked Hours: ${log.workedHours} hrs", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    selectedAttendance?.let { log ->
        AlertDialog(
            onDismissRequest = { selectedAttendance = null },
            title = { Text("Attendance Detail: ${log.employeeName}") },
            text = {
                Column {
                    Text("Check-In: ${log.checkIn}")
                    Text("Check-Out: ${log.checkOut ?: "In Progress"}")
                    Text("Worked Hours: ${log.workedHours} hours")
                    Text("Face Verified: Yes")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedAttendance = null }) { Text("Close") }
            }
        )
    }
}
