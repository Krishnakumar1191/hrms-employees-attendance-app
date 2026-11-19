package com.example.hr_employeemanagementsystem.ui.screens.employees

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.hr_employeemanagementsystem.data.model.EmployeeEntity
import com.example.hr_employeemanagementsystem.data.repository.EmployeeRepository
import kotlinx.coroutines.launch

@Composable
fun EmployeeListScreen(
    employeeRepository: EmployeeRepository,
    onSelectEmployee: (employeeId: Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Enrolled

    val allEmployees by employeeRepository.getAllEmployees().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingEmployee by remember { mutableStateOf<EmployeeEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<EmployeeEntity?>(null) }

    val filteredList = allEmployees.filter { emp ->
        val matchesQuery = emp.displayName.contains(searchQuery, ignoreCase = true) ||
                emp.department.contains(searchQuery, ignoreCase = true) ||
                emp.jobTitle.contains(searchQuery, ignoreCase = true)
        val matchesTab = if (selectedTab == 1) emp.isEnrolled else true
        matchesQuery && matchesTab
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Employee")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Employees...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp)
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
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (emp.photoUri != null) {
                                        coil.compose.AsyncImage(
                                            model = emp.photoUri,
                                            contentDescription = "Profile Photo",
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(androidx.compose.foundation.shape.CircleShape),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = emp.displayName,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
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

                                    // Actions
                                    Column {
                                        IconButton(onClick = { editingEmployee = emp }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = { showDeleteConfirm = emp }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
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

    if (showAddDialog || editingEmployee != null) {
        val isEdit = editingEmployee != null
        AddEditEmployeeDialog(
            initialEmployee = editingEmployee,
            onDismiss = {
                showAddDialog = false
                editingEmployee = null
            },
            onSave = { updatedEmp ->
                scope.launch {
                    if (isEdit) {
                        employeeRepository.updateEmployee(updatedEmp)
                    } else {
                        employeeRepository.addEmployee(updatedEmp)
                    }
                    showAddDialog = false
                    editingEmployee = null
                }
            }
        )
    }

    showDeleteConfirm?.let { emp ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Employee") },
            text = { Text("Are you sure you want to delete ${emp.displayName}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            employeeRepository.deleteEmployee(emp)
                            showDeleteConfirm = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AddEditEmployeeDialog(
    initialEmployee: EmployeeEntity?,
    onDismiss: () -> Unit,
    onSave: (EmployeeEntity) -> Unit
) {
    var displayName by remember { mutableStateOf(initialEmployee?.displayName ?: "") }
    var email by remember { mutableStateOf(initialEmployee?.email ?: "") }
    var phone by remember { mutableStateOf(initialEmployee?.phone ?: "") }
    var jobTitle by remember { mutableStateOf(initialEmployee?.jobTitle ?: "") }
    var department by remember { mutableStateOf(initialEmployee?.department ?: "") }
    var trackEmployee by remember { mutableStateOf(initialEmployee?.trackEmployee ?: false) }
    var isAdmin by remember { mutableStateOf(initialEmployee?.isAdmin ?: false) }
    
    var password by remember { mutableStateOf(initialEmployee?.password ?: "") }
    var confirmPassword by remember { mutableStateOf(initialEmployee?.password ?: "") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialEmployee == null) "Add Employee" else "Edit Employee") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Full Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; emailError = null },
                    label = { Text("Email Address") },
                    singleLine = true,
                    isError = emailError != null,
                    supportingText = { emailError?.let { Text(it) } }
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; phoneError = null },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    isError = phoneError != null,
                    supportingText = { phoneError?.let { Text(it) } }
                )
                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = { Text("Job Title") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; passwordError = null },
                    label = { Text(if (initialEmployee == null) "Password" else "New Password (optional)") },
                    singleLine = true,
                    isError = passwordError != null,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; passwordError = null },
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    isError = passwordError != null,
                    supportingText = { passwordError?.let { Text(it) } },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = trackEmployee, onCheckedChange = { trackEmployee = it })
                    Text("Enable GPS Tracking")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isAdmin, onCheckedChange = { isAdmin = it })
                    Text("Has Admin Privileges")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var hasError = false
                    
                    val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)\$".toRegex()
                    if (!email.matches(emailRegex)) {
                        emailError = "Invalid email format"
                        hasError = true
                    }
                    if (phone.length < 10) {
                        phoneError = "Phone number too short"
                        hasError = true
                    }
                    
                    if (initialEmployee == null || password.isNotEmpty()) {
                        if (password.length < 6) {
                            passwordError = "Password must be at least 6 chars"
                            hasError = true
                        } else if (password != confirmPassword) {
                            passwordError = "Passwords do not match"
                            hasError = true
                        }
                    }

                    if (!hasError && displayName.isNotBlank() && email.isNotBlank()) {
                        val finalPassword = if (password.isNotBlank()) password else initialEmployee?.password ?: ""
                        val emp = initialEmployee?.copy(
                            displayName = displayName,
                            email = email,
                            phone = phone,
                            password = finalPassword,
                            jobTitle = jobTitle,
                            department = department,
                            trackEmployee = trackEmployee,
                            isAdmin = isAdmin
                        ) ?: EmployeeEntity(
                            displayName = displayName,
                            email = email,
                            phone = phone,
                            password = finalPassword,
                            jobTitle = jobTitle,
                            department = department,
                            companyId = 1L, // Default for now
                            companyName = "Headquarters", // Default
                            trackEmployee = trackEmployee,
                            isAdmin = isAdmin
                        )
                        onSave(emp)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
