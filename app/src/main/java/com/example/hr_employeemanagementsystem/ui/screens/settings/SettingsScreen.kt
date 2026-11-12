package com.example.hr_employeemanagementsystem.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hr_employeemanagementsystem.data.local.preferences.UserPreferencesRepository
import com.example.hr_employeemanagementsystem.data.repository.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    prefs: UserPreferencesRepository,
    authRepository: AuthRepository,
    onNavigateChangePin: () -> Unit,
    onLogout: () -> Unit
) {
    val soundEnabled by prefs.soundEnabledFlow.collectAsState(initial = true)
    val checkInDelay by prefs.checkInDelayFlow.collectAsState(initial = 5)
    val checkOutDelay by prefs.checkOutDelayFlow.collectAsState(initial = 5)
    val selectedCompany by prefs.selectedCompanyFlow.collectAsState(initial = "Main Headquarters")

    var sound by remember(soundEnabled) { mutableStateOf(soundEnabled) }
    var inDelay by remember(checkInDelay) { mutableFloatStateOf(checkInDelay.toFloat()) }
    var outDelay by remember(checkOutDelay) { mutableFloatStateOf(checkOutDelay.toFloat()) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Application Settings", style = MaterialTheme.typography.titleLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Current Workspace / Branch", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(selectedCompany, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Attendance Audio Feedback Sound", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = sound,
                        onCheckedChange = {
                            sound = it
                            scope.launch { prefs.updateSettings(sound, inDelay.toInt(), outDelay.toInt()) }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Check-In Cooldown Delay: ${inDelay.toInt()} seconds")
                Slider(
                    value = inDelay,
                    onValueChange = { inDelay = it },
                    valueRange = 1f..15f,
                    onValueChangeFinished = {
                        scope.launch { prefs.updateSettings(sound, inDelay.toInt(), outDelay.toInt()) }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Check-Out Cooldown Delay: ${outDelay.toInt()} seconds")
                Slider(
                    value = outDelay,
                    onValueChange = { outDelay = it },
                    valueRange = 1f..15f,
                    onValueChangeFinished = {
                        scope.launch { prefs.updateSettings(sound, inDelay.toInt(), outDelay.toInt()) }
                    }
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Security", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onNavigateChangePin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change 4-Digit Security PIN")
                }
            }
        }

        Button(
            onClick = {
                scope.launch {
                    authRepository.logout()
                    onLogout()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout Account")
        }
    }
}
