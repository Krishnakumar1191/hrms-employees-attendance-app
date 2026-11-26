package com.example.hr_employeemanagementsystem.ui.screens.leaves

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.hr_employeemanagementsystem.data.repository.LeaveRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeavesScreen(
    leaveRepository: LeaveRepository,
    currentUserId: Long = 1L,
    username: String = "User"
) {
    val tabs = listOf("Requested", "Approved", "Rejected", "Revoked")
    var selectedTab by remember { mutableIntStateOf(0) }
    var showApplyDialog by remember { mutableStateOf(false) }

    val currentStatus = tabs[selectedTab]
    val leavesList by leaveRepository.getLeavesByStatus(currentStatus).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showApplyDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Apply Leave")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = (selectedTab == index),
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            if (leavesList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No $currentStatus leave requests found.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(leavesList, key = { it.id }) { leave ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(leave.employeeName, style = MaterialTheme.typography.titleMedium)
                                    Badge(containerColor = when(leave.status) {
                                        "Approved" -> Color(0xFF4CAF50)
                                        "Rejected" -> Color(0xFFF44336)
                                        "Revoked" -> Color(0xFFFF9800)
                                        else -> Color(0xFF2196F3)
                                    }) {
                                        Text(leave.status, color = Color.White, modifier = Modifier.padding(4.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Type: ${leave.leaveType} (${leave.totalDays} Days)", style = MaterialTheme.typography.bodyMedium)
                                Text("Duration: ${leave.startDate} to ${leave.endDate}", style = MaterialTheme.typography.bodySmall)
                                Text("Reason: ${leave.reason}", style = MaterialTheme.typography.bodySmall)

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    if (leave.status == "Requested") {
                                        IconButton(onClick = { scope.launch { leaveRepository.rejectLeave(leave.id) } }) {
                                            Icon(Icons.Default.Close, contentDescription = "Reject", tint = Color.Red)
                                        }
                                        IconButton(onClick = { scope.launch { leaveRepository.approveLeave(leave.id) } }) {
                                            Icon(Icons.Default.Check, contentDescription = "Approve", tint = Color(0xFF4CAF50))
                                        }
                                    } else if (leave.status == "Approved") {
                                        OutlinedButton(onClick = { scope.launch { leaveRepository.revokeLeave(leave.id) } }) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Revoke")
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Revoke / Cancel")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showApplyDialog) {
        ApplyLeaveDialog(
            onDismiss = { showApplyDialog = false },
            onSubmit = { type, start, end, days, reason ->
                scope.launch {
                    leaveRepository.applyLeave(
                        employeeId = currentUserId,
                        employeeName = username,
                        leaveType = type,
                        startDate = start,
                        endDate = end,
                        totalDays = days,
                        reason = reason
                    )
                    showApplyDialog = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyLeaveDialog(
    onDismiss: () -> Unit,
    onSubmit: (type: String, start: String, end: String, days: Double, reason: String) -> Unit
) {
    val today = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) }
    var type by remember { mutableStateOf("Paid Leave") }
    var startDate by remember { mutableStateOf(today) }
    var endDate by remember { mutableStateOf(today) }
    var durationType by remember { mutableStateOf("Full Day") }
    var days by remember { mutableStateOf("1.0") }
    var reason by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val updateDays = { s: String, e: String, dtype: String ->
        if (dtype.contains("Half")) {
            days = "0.5"
            endDate = s
        } else {
            try {
                val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val d1 = format.parse(s)
                val d2 = format.parse(e)
                if (d1 != null && d2 != null) {
                    val diff = d2.time - d1.time
                    val calculated = (diff / (1000.0 * 60 * 60 * 24)).toLong() + 1L
                    if (calculated > 0) days = calculated.toDouble().toString() else days = "1.0"
                }
            } catch (_: Exception) {
                days = "1.0"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apply For Leave") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Leave Type") }, modifier = Modifier.fillMaxWidth())
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = durationType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Duration") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("Full Day", "First Half (0.5)", "Second Half (0.5)").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    durationType = option
                                    updateDays(startDate, endDate, option)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                DatePickerField(label = "Start Date", date = startDate, onDateSelected = { startDate = it; updateDays(it, endDate, durationType) })
                if (!durationType.contains("Half")) {
                    DatePickerField(label = "End Date", date = endDate, onDateSelected = { endDate = it; updateDays(startDate, it, durationType) })
                }
                OutlinedTextField(value = days, onValueChange = { days = it }, label = { Text("Total Days") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(type, startDate, endDate, days.toDoubleOrNull() ?: 1.0, reason) }) {
                Text("Submit Application")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    date: String,
    onDateSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    OutlinedTextField(
        value = date,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
    )

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val formattedDate = formatter.format(java.util.Date(millis))
                        onDateSelected(formattedDate)
                    }
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
