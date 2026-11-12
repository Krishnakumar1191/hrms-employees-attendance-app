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
fun PinSetupScreen(
    authRepository: AuthRepository,
    onPinSet: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Setup 4-Digit Security PIN", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Used for fast local unlock", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = { if (it.length <= 4) pin = it },
            label = { Text("Enter 4-Digit PIN") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPin,
            onValueChange = { if (it.length <= 4) confirmPin = it },
            label = { Text("Confirm PIN") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        error?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (pin.length != 4) {
                    error = "PIN must be exactly 4 digits"
                } else if (pin != confirmPin) {
                    error = "PINs do not match"
                } else {
                    scope.launch {
                        authRepository.setupPin(pin)
                        onPinSet()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save PIN & Continue")
        }
    }
}
