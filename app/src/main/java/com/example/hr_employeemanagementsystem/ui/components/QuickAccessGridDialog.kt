package com.example.hr_employeemanagementsystem.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class QuickAccessItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun QuickAccessGridDialog(
    onDismiss: () -> Unit,
    onNavigateLeaves: () -> Unit,
    onNavigateWFH: () -> Unit,
    onNavigateEmployees: () -> Unit,
    onNavigateKiosk: () -> Unit,
    onNavigateAttendances: () -> Unit,
    onNavigateEvents: () -> Unit,
    onNavigatePayslips: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val items = listOf(
        QuickAccessItem("Leaves", Icons.Default.EventBusy, onNavigateLeaves),
        QuickAccessItem("WFH", Icons.Default.HomeWork, onNavigateWFH),
        QuickAccessItem("Employees", Icons.Default.Group, onNavigateEmployees),
        QuickAccessItem("Kiosk Kiosk", Icons.Default.CameraFront, onNavigateKiosk),
        QuickAccessItem("Attendances", Icons.Default.ListAlt, onNavigateAttendances),
        QuickAccessItem("Events", Icons.Default.CalendarMonth, onNavigateEvents),
        QuickAccessItem("Payslips", Icons.Default.ReceiptLong, onNavigatePayslips),
        QuickAccessItem("Settings", Icons.Default.Settings, onNavigateSettings)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Access Menu") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().height(280.dp)
            ) {
                items(items.size) { index ->
                    val item = items[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                item.onClick()
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(item.icon, contentDescription = item.title, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(item.title, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
