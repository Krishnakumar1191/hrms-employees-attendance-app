package com.example.hr_employeemanagementsystem.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.hr_employeemanagementsystem.data.local.db.AppDatabase
import com.example.hr_employeemanagementsystem.data.model.CompanyEntity
import com.example.hr_employeemanagementsystem.data.model.DepartmentEntity
import com.example.hr_employeemanagementsystem.data.model.EmployeeEntity
import com.example.hr_employeemanagementsystem.data.local.preferences.UserPreferencesRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyRegisterScreen(
    context: android.content.Context,
    prefs: UserPreferencesRepository,
    onRegisterSuccess: () -> Unit,
    onNavigateLogin: () -> Unit
) {
    var companyName by remember { mutableStateOf("") }
    var companyAddress by remember { mutableStateOf("") }
    var headquarters by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var adminUsername by remember { mutableStateOf("") }
    var adminEmail by remember { mutableStateOf("") }
    var adminPhone by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    var adminConfirmPassword by remember { mutableStateOf("") }
    var jobRole by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }

    var departmentList by remember { mutableStateOf(mutableListOf("Executive", "Engineering", "Human Resources", "Sales", "Finance", "Marketing")) }
    var deptExpanded by remember { mutableStateOf(false) }
    var showAddDeptDialog by remember { mutableStateOf(false) }
    var newDeptName by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Register Organization", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Create your company workspace & admin account", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = companyName,
            onValueChange = { companyName = it },
            label = { Text("Company Name (e.g. Acme Corp)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = companyAddress,
            onValueChange = { companyAddress = it },
            label = { Text("Company Address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = headquarters,
            onValueChange = { headquarters = it },
            label = { Text("Headquarters Location") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = website,
            onValueChange = { website = it },
            label = { Text("Company Website") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = adminUsername,
            onValueChange = { adminUsername = it },
            label = { Text("Main Admin Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = adminEmail,
            onValueChange = { adminEmail = it },
            label = { Text("Main Admin Email Address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = adminPhone,
            onValueChange = { adminPhone = it },
            label = { Text("Main Admin Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = adminPassword,
            onValueChange = { adminPassword = it },
            label = { Text("Main Admin Password (min 6 chars)") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = adminConfirmPassword,
            onValueChange = { adminConfirmPassword = it },
            label = { Text("Confirm Admin Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = jobRole,
            onValueChange = { jobRole = it },
            label = { Text("Admin Job Role (e.g. IT Director)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Dynamic Department Selection Dropdown
        ExposedDropdownMenuBox(
            expanded = deptExpanded,
            onExpandedChange = { deptExpanded = !deptExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = department,
                onValueChange = {},
                readOnly = true,
                label = { Text("Admin Department") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = deptExpanded,
                onDismissRequest = { deptExpanded = false }
            ) {
                departmentList.forEach { dept ->
                    DropdownMenuItem(
                        text = { Text(dept) },
                        onClick = {
                            department = dept
                            deptExpanded = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("+ Add New Department...", color = MaterialTheme.colorScheme.primary) },
                    onClick = {
                        deptExpanded = false
                        showAddDeptDialog = true
                    }
                )
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (companyName.isBlank() || adminUsername.isBlank() || adminEmail.isBlank() || adminPassword.isBlank() || jobRole.isBlank() || department.isBlank()) {
                    errorMessage = "Please fill in all required fields."
                    return@Button
                }
                if (adminPassword.length < 6) {
                    errorMessage = "Password must be at least 6 characters."
                    return@Button
                }
                if (adminPassword != adminConfirmPassword) {
                    errorMessage = "Passwords do not match."
                    return@Button
                }
                isLoading = true
                errorMessage = null
                scope.launch {
                    try {
                        val db = AppDatabase.getDatabase(context, companyName.trim())
                        db.companyDao().insertCompany(
                            CompanyEntity(
                                companyName = companyName.trim(),
                                address = companyAddress.trim(),
                                headquarters = headquarters.trim(),
                                website = website.trim(),
                                adminUsername = adminUsername.trim()
                            )
                        )
                        // Seed dynamic departments
                        departmentList.forEach { dept ->
                            db.departmentDao().insertDepartment(DepartmentEntity(dept))
                        }

                        val adminEmp = EmployeeEntity(
                            displayName = adminUsername.trim(),
                            email = adminEmail.trim(),
                            phone = adminPhone.trim(),
                            companyId = 1L,
                            companyName = companyName.trim(),
                            jobTitle = jobRole.trim(),
                            department = department.trim(),
                            password = adminPassword,
                            isAdmin = true,
                            isEnrolled = true
                        )
                        db.employeeDao().insertEmployee(adminEmp)

                        prefs.saveCompany(companyName.trim())

                        isLoading = false
                        onRegisterSuccess()
                    } catch (e: Exception) {
                        isLoading = false
                        errorMessage = e.message ?: "Registration failed."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Register Company Workspace")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateLogin) {
            Text("Already registered? Back to Login")
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
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newDeptName.isNotBlank() && !departmentList.contains(newDeptName.trim())) {
                        departmentList.add(newDeptName.trim())
                        department = newDeptName.trim()
                        newDeptName = ""
                        showAddDeptDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDeptDialog = false }) { Text("Cancel") }
            }
        )
    }
}
