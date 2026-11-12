package com.example.hr_employeemanagementsystem.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hr_employeemanagementsystem.data.repository.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun LoginOtpScreen(
    authRepository: AuthRepository,
    onLoginSuccess: () -> Unit,
    onNavigateCredentials: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(1) } // 1: enter identifier, 2: enter OTP
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("OTP Authentication", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        if (step == 1) {
            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it },
                label = { Text("Mobile Number or Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (identifier.isNotBlank()) step = 2 else errorMessage = "Enter valid mobile/email"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send OTP Code")
            }
        } else {
            Text("OTP code sent to $identifier", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = otpCode,
                onValueChange = { otpCode = it },
                label = { Text("6-Digit OTP (Use '123456')") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    scope.launch {
                        val res = authRepository.verifyOtp(identifier, otpCode)
                        res.onSuccess { onLoginSuccess() }.onFailure { errorMessage = it.message }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Verify OTP")
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateCredentials) {
            Text("Back to Credential Login")
        }
    }
}
