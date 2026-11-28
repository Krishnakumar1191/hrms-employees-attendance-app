package com.example.hr_employeemanagementsystem.ui.screens.employees

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hr_employeemanagementsystem.data.local.dao.DepartmentDao
import com.example.hr_employeemanagementsystem.data.model.DepartmentEntity
import com.example.hr_employeemanagementsystem.data.model.EmployeeEntity
import com.example.hr_employeemanagementsystem.data.repository.EmployeeRepository
import kotlinx.coroutines.launch

@Composable
fun EmployeeListScreen(
    employeeRepository: EmployeeRepository,
    departmentDao: DepartmentDao,
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
                shape = RoundedCornerShape(14.dp)
            )

            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = (selectedTab == 0), onClick = { selectedTab = 0 }, text = { Text("All Staff (${allEmployees.size})") })
                Tab(selected = (selectedTab == 1), onClick = { selectedTab = 1 }, text = { Text("Face Enrolled (${allEmployees.count { it.isEnrolled }})") })
            }

            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No employees match search.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                        Text(emp.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("${emp.jobTitle} • ${emp.department}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

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
            departmentDao = departmentDao,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEmployeeDialog(
    initialEmployee: EmployeeEntity?,
    departmentDao: DepartmentDao,
    onDismiss: () -> Unit,
    onSave: (EmployeeEntity) -> Unit
) {
    val departments by departmentDao.getAllDepartments().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var displayName by remember { mutableStateOf(initialEmployee?.displayName ?: "") }
    var email by remember { mutableStateOf(initialEmployee?.email ?: "") }
    var phone by remember { mutableStateOf(initialEmployee?.phone ?: "") }
    var jobTitle by remember { mutableStateOf(initialEmployee?.jobTitle ?: "") }
    var department by remember { mutableStateOf(initialEmployee?.department ?: departments.firstOrNull()?.departmentName ?: "Executive") }
    var trackEmployee by remember { mutableStateOf(initialEmployee?.trackEmployee ?: false) }
    var isAdmin by remember { mutableStateOf(initialEmployee?.isAdmin ?: false) }
    
    var password by remember { mutableStateOf(initialEmployee?.password ?: "") }
    var confirmPassword by remember { mutableStateOf(initialEmployee?.password ?: "") }

    var deptExpanded by remember { mutableStateOf(false) }
    var showAddDeptDialog by remember { mutableStateOf(false) }
    var newDeptName by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var jobError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialEmployee == null) "Add New Employee" else "Edit Employee",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it; nameError = null },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        isError = nameError != null,
                        supportingText = { nameError?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; emailError = null },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        isError = emailError != null,
                        supportingText = { emailError?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it; phoneError = null },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        isError = phoneError != null,
                        supportingText = { phoneError?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = jobTitle,
                        onValueChange = { jobTitle = it; jobError = null },
                        label = { Text("Job Title") },
                        leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        isError = jobError != null,
                        supportingText = { jobError?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    ExposedDropdownMenuBox(
                        expanded = deptExpanded,
                        onExpandedChange = { deptExpanded = !deptExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = department,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Department") },
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptExpanded) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = deptExpanded,
                            onDismissRequest = { deptExpanded = false }
                        ) {
                            departments.forEach { dept ->
                                DropdownMenuItem(
                                    text = { Text(dept.departmentName) },
                                    onClick = {
                                        department = dept.departmentName
                                        deptExpanded = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("+ Add New Department...", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    deptExpanded = false
                                    showAddDeptDialog = true
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; passwordError = null },
                        label = { Text(if (initialEmployee == null) "Password" else "New Password (optional)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        isError = passwordError != null,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; passwordError = null },
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        isError = passwordError != null,
                        supportingText = { passwordError?.let { Text(it) } },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = trackEmployee, onCheckedChange = { trackEmployee = it })
                        Text("Enable GPS Tracking", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isAdmin, onCheckedChange = { isAdmin = it })
                        Text("Has Admin Privileges", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            var hasError = false

                            if (displayName.isBlank()) { nameError = "Full name is required"; hasError = true } else { nameError = null }
                            val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)\$".toRegex()
                            if (!email.matches(emailRegex)) { emailError = "Invalid email format"; hasError = true } else { emailError = null }
                            if (phone.length < 10) { phoneError = "Valid phone number required"; hasError = true } else { phoneError = null }
                            if (jobTitle.isBlank()) { jobError = "Job title is required"; hasError = true } else { jobError = null }

                            if (initialEmployee == null || password.isNotEmpty()) {
                                if (password.length < 6) {
                                    passwordError = "Password must be at least 6 characters"
                                    hasError = true
                                } else if (password != confirmPassword) {
                                    passwordError = "Passwords do not match"
                                    hasError = true
                                } else {
                                    passwordError = null
                                }
                            }

                            if (!hasError) {
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
                                    companyId = 1L,
                                    companyName = "Headquarters",
                                    trackEmployee = trackEmployee,
                                    isAdmin = isAdmin
                                )
                                onSave(emp)
                            }
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Save Employee", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showAddDeptDialog) {
        AlertDialog(
            onDismissRequest = { showAddDeptDialog = false },
            title = { Text("Add New Department") },
            text = {
                OutlinedTextField(
                    value = newDeptName,
                    onValueChange = { newDeptName = it },
                    label = { Text("Department Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newDeptName.isNotBlank()) {
                            scope.launch {
                                departmentDao.insertDepartment(DepartmentEntity(newDeptName.trim()))
                                department = newDeptName.trim()
                                newDeptName = ""
                                showAddDeptDialog = false
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDeptDialog = false }) { Text("Cancel") }
            }
        )
    }
}
