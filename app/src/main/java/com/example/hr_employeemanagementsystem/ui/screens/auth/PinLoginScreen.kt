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
fun PinLoginScreen(
    authRepository: AuthRepository,
    onPinSuccess: () -> Unit,
    onForceReAuth: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Unlock App", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Enter your 4-Digit PIN", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = {
                if (it.length <= 4) {
                    pin = it
                    if (it.length == 4) {
                        scope.launch {
                            if (authRepository.verifyPin(it)) {
                                onPinSuccess()
                            } else {
                                error = "Incorrect PIN"
                            }
                        }
                    }
                }
            },
            label = { Text("4-Digit PIN") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        error?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onForceReAuth) {
            Text("Log in with password instead")
        }
    }
}
