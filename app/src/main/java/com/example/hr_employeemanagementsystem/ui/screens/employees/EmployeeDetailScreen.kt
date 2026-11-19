package com.example.hr_employeemanagementsystem.ui.screens.employees

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.hr_employeemanagementsystem.data.model.EmployeeEntity
import com.example.hr_employeemanagementsystem.data.repository.EmployeeRepository
import com.example.hr_employeemanagementsystem.data.repository.LocationRepository
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun EmployeeDetailScreen(
    employeeId: Long,
    employeeRepository: EmployeeRepository,
    locationRepository: LocationRepository,
    onNavigateEnroll: (employeeId: Long) -> Unit
) {
    val context = LocalContext.current
    var employee by remember { mutableStateOf<EmployeeEntity?>(null) }
    val locationLogs by locationRepository.getLocationLogsForEmployee(employeeId).collectAsState(initial = emptyList())

    LaunchedEffect(employeeId) {
        employee = employeeRepository.getEmployeeById(employeeId)
    }

    val emp = employee
    if (emp == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val defaultPos = LatLng(12.9716, 77.5946) // Default LatLng (e.g. Bangalore)
    val routePoints = if (locationLogs.isNotEmpty()) {
        locationLogs.map { LatLng(it.latitude, it.longitude) }
    } else {
        listOf(defaultPos, LatLng(12.9750, 77.5990))
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(routePoints.last(), 14f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (emp.photoUri != null) {
                        coil.compose.AsyncImage(
                            model = emp.photoUri,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    Column {
                        Text(emp.displayName, style = MaterialTheme.typography.headlineSmall)
                        Text("${emp.jobTitle} • ${emp.department}", style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("📧 ${emp.email}", style = MaterialTheme.typography.bodyMedium)
                Text("📞 ${emp.phone}", style = MaterialTheme.typography.bodyMedium)
                Text("🏢 ${emp.companyName}", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Biometric Face Registration", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (emp.isEnrolled) "Face Descriptor Enrolled in Local Database" else "Face Not Enrolled",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onNavigateEnroll(emp.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraFront, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (emp.isEnrolled) "Update Face Enrollment" else "Enroll Face Photo")
                }
            }
        }

        if (emp.trackEmployee) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Live GPS Shift Tracking Map", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState
                        ) {
                            Polyline(points = routePoints, color = androidx.compose.ui.graphics.Color.Blue, width = 8f)
                            Marker(state = com.google.maps.android.compose.MarkerState(position = routePoints.first()), title = "Check-in Start")
                            Marker(state = com.google.maps.android.compose.MarkerState(position = routePoints.last()), title = "Current Position")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            val lastPos = routePoints.last()
                            val gmmIntentUri = Uri.parse("google.navigation:q=${lastPos.latitude},${lastPos.longitude}&mode=w")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            context.startActivity(mapIntent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Directions, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Route in Google Maps App")
                    }
                }
            }
        }
    }
}
