package com.example.hr_employeemanagementsystem.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.hr_employeemanagementsystem.data.repository.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun LoginCredentialsScreen(
    authRepository: AuthRepository,
    onLoginSuccess: (isAdmin: Boolean) -> Unit,
    onNavigateOtp: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("HR Employee Management", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Sign in with credentials", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username or Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (username.isBlank() || password.isBlank()) {
                    errorMessage = "Please enter username and password"
                    return@Button
                }
                isLoading = true
                errorMessage = null
                scope.launch {
                    val result = authRepository.loginWithCredentials(username, password)
                    isLoading = false
                    result.onSuccess {
                        val isAdmin = username.equals("admin", ignoreCase = true)
                        onLoginSuccess(isAdmin)
                    }.onFailure { err ->
                        errorMessage = err.message ?: "Login failed"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Log In")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateOtp) {
            Text("Log in with OTP / Mobile")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Demo Accounts:", style = MaterialTheme.typography.labelLarge)
                Text("• Admin Role: admin / admin", style = MaterialTheme.typography.bodySmall)
                Text("• Employee Role: john / john", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
