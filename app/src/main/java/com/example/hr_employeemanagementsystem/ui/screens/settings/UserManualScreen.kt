package com.example.hr_employeemanagementsystem.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UserManualScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("User Manual & Guide", style = MaterialTheme.typography.titleLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("1. Biometric Attendance Kiosk", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Open the Face Kiosk from the Quick Access grid or Manager Dashboard. Position your face in front of the front camera. The system will detect your face, match your profile, and toggle your check-in/check-out status with audio cues.", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("2. Leave & WFH Management", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("HR Managers can review pending leave and work-from-home applications across Requested, Approved, Rejected, and Revoked tabs. Use the tick icon to approve or cross icon to reject requests.", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("3. Live GPS Location Tracking", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("For employees with GPS tracking enabled, checking in automatically launches a background GPS tracking service. The travel route is recorded and rendered on the Google Maps view in Employee Details.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
