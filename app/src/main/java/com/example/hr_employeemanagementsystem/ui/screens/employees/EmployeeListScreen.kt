package com.example.hr_employeemanagementsystem.ui.screens.employees

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.hr_employeemanagementsystem.data.repository.EmployeeRepository

@Composable
fun EmployeeListScreen(
    employeeRepository: EmployeeRepository,
    onSelectEmployee: (employeeId: Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Enrolled

    val allEmployees by employeeRepository.getAllEmployees().collectAsState(initial = emptyList())

    val filteredList = allEmployees.filter { emp ->
        val matchesQuery = emp.displayName.contains(searchQuery, ignoreCase = true) ||
                emp.department.contains(searchQuery, ignoreCase = true) ||
                emp.jobTitle.contains(searchQuery, ignoreCase = true)
        val matchesTab = if (selectedTab == 1) emp.isEnrolled else true
        matchesQuery && matchesTab
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Employees...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = (selectedTab == 0), onClick = { selectedTab = 0 }, text = { Text("All Staff (${allEmployees.size})") })
            Tab(selected = (selectedTab == 1), onClick = { selectedTab = 1 }, text = { Text("Face Enrolled (${allEmployees.count { it.isEnrolled }})") })
        }

        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No employees match search.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList, key = { it.id }) { emp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectEmployee(emp.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = emp.displayName,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(emp.displayName, style = MaterialTheme.typography.titleMedium)
                                Text("${emp.jobTitle} • ${emp.department}", style = MaterialTheme.typography.bodySmall)

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (emp.isEnrolled) {
                                        AssistChip(
                                            onClick = { },
                                            label = { Text("Enrolled") },
                                            colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFE8F5E9))
                                        )
                                    }
                                    if (emp.trackEmployee) {
                                        AssistChip(
                                            onClick = { },
                                            label = { Text("GPS Tracked") },
                                            leadingIcon = { Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                            colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFE1F5FE))
                                        )
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
