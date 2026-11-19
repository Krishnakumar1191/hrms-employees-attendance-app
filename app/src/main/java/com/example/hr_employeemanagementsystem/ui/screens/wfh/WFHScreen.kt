package com.example.hr_employeemanagementsystem.ui.screens.wfh

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.hr_employeemanagementsystem.data.repository.WFHRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WFHScreen(
    wfhRepository: WFHRepository,
    currentUserId: Long = 1L,
    username: String = "User"
) {
    val tabs = listOf("Requested", "Approved", "Rejected")
    var selectedTab by remember { mutableIntStateOf(0) }
    var showApplyDialog by remember { mutableStateOf(false) }

    val currentStatus = tabs[selectedTab]
    val wfhList by wfhRepository.getWFHByStatus(currentStatus).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showApplyDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Apply WFH")
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

            if (wfhList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No $currentStatus WFH requests found.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(wfhList, key = { it.id }) { wfh ->
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
                                    Text(wfh.employeeName, style = MaterialTheme.typography.titleMedium)
                                    Badge(containerColor = when(wfh.status) {
                                        "Approved" -> Color(0xFF4CAF50)
                                        "Rejected" -> Color(0xFFF44336)
                                        else -> Color(0xFF2196F3)
                                    }) {
                                        Text(wfh.status, color = Color.White, modifier = Modifier.padding(4.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Duration: ${wfh.startDate} to ${wfh.endDate} (${wfh.totalDays} Days)", style = MaterialTheme.typography.bodyMedium)
                                Text("Reason: ${wfh.reason}", style = MaterialTheme.typography.bodySmall)

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    if (wfh.status == "Requested") {
                                        IconButton(onClick = { scope.launch { wfhRepository.rejectWFH(wfh.id) } }) {
                                            Icon(Icons.Default.Close, contentDescription = "Reject", tint = Color.Red)
                                        }
                                        IconButton(onClick = { scope.launch { wfhRepository.approveWFH(wfh.id) } }) {
                                            Icon(Icons.Default.Check, contentDescription = "Approve", tint = Color(0xFF4CAF50))
                                        }
                                    } else if (wfh.status == "Approved") {
                                        OutlinedButton(onClick = { scope.launch { wfhRepository.revokeWFH(wfh.id) } }) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Cancel")
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Cancel Approval")
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
        ApplyWFHDialog(
            onDismiss = { showApplyDialog = false },
            onSubmit = { start, end, days, reason ->
                scope.launch {
                    wfhRepository.applyWFH(
                        employeeId = currentUserId,
                        employeeName = username,
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

@Composable
fun ApplyWFHDialog(
    onDismiss: () -> Unit,
    onSubmit: (start: String, end: String, days: Double, reason: String) -> Unit
) {
    var startDate by remember { mutableStateOf("2026-11-05") }
    var endDate by remember { mutableStateOf("2026-11-06") }
    var days by remember { mutableStateOf("2.0") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Work From Home") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Start Date (YYYY-MM-DD)") })
                OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("End Date (YYYY-MM-DD)") })
                OutlinedTextField(value = days, onValueChange = { days = it }, label = { Text("Total Days") })
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason / Task Summary") })
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(startDate, endDate, days.toDoubleOrNull() ?: 1.0, reason) }) {
                Text("Submit Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
