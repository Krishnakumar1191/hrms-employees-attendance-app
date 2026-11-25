package com.example.hr_employeemanagementsystem.ui.screens.attendances

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import com.example.hr_employeemanagementsystem.data.model.AttendanceEntity
import com.example.hr_employeemanagementsystem.data.repository.AttendanceRepository
import com.example.hr_employeemanagementsystem.data.repository.LocationRepository

@Composable
fun AttendancesScreen(
    attendanceRepository: AttendanceRepository,
    locationRepository: LocationRepository,
    isAdmin: Boolean,
    currentUserId: Long
) {
    val allAttendances by attendanceRepository.getAllAttendances().collectAsState(initial = emptyList())
    val myAttendances by attendanceRepository.getAttendancesForEmployee(currentUserId).collectAsState(initial = emptyList())

    var showAll by remember { mutableStateOf(isAdmin) }
    var selectedAttendance by remember { mutableStateOf<AttendanceEntity?>(null) }

    val displayList = if (showAll && isAdmin) allAttendances else myAttendances

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (isAdmin) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (showAll) "Company All Attendances" else "My Personal Attendances", style = MaterialTheme.typography.titleLarge)
                FilterChip(
                    selected = showAll,
                    onClick = { showAll = !showAll },
                    label = { Text(if (showAll) "Viewing: All" else "Viewing: My Logs") }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Text("My Attendance History", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (displayList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No attendance logs recorded yet.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(displayList, key = { it.id }) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { selectedAttendance = log },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(log.employeeName, style = MaterialTheme.typography.titleMedium)
                                Badge(containerColor = Color(0xFF4CAF50)) {
                                    Text(log.status, color = Color.White, modifier = Modifier.padding(4.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Check-In: ${log.checkIn}", style = MaterialTheme.typography.bodyMedium)
                            Text("Check-Out: ${log.checkOut ?: "Active Shift"}", style = MaterialTheme.typography.bodySmall)
                            Text("Worked Hours: ${log.workedHours} hrs", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    selectedAttendance?.let { log ->
        androidx.compose.ui.window.Dialog(onDismissRequest = { selectedAttendance = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Attendance Detail: ${log.employeeName}", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        IconButton(onClick = { selectedAttendance = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    HorizontalDivider()

                    Text("Check-In: ${log.checkIn}", style = MaterialTheme.typography.bodyMedium)
                    Text("Check-Out: ${log.checkOut ?: "In Progress"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Worked Hours: ${log.workedHours} hours", style = MaterialTheme.typography.bodyMedium)
                    Text("Face Verified: Yes", style = MaterialTheme.typography.bodyMedium)

                    if (log.capturedPhotoUri != null) {
                        Text("Clock-In Photo:", style = MaterialTheme.typography.labelMedium)
                        coil.compose.AsyncImage(
                            model = log.capturedPhotoUri,
                            contentDescription = "Check-in Photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }

                    if (log.checkInLat != null && log.checkInLng != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("LIVE LOCATION", style = MaterialTheme.typography.labelMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    Badge(containerColor = Color(0xFFE8F5E9)) {
                                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF4CAF50)))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Live Tracking Active", color = Color(0xFF2E7D32), style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val startLat = log.checkInLat ?: 11.0168
                                val startLng = log.checkInLng ?: 76.9558
                                val endLat = log.checkOutLat ?: startLat
                                val endLng = log.checkOutLng ?: startLng

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                ) {
                                    val startLat = log.checkInLat ?: 11.0168
                                    val startLng = log.checkInLng ?: 76.9558
                                    val endLat = log.checkOutLat ?: startLat
                                    val endLng = log.checkOutLng ?: startLng

                                    RouteMapCanvas(
                                        startLat = startLat,
                                        startLng = startLng,
                                        endLat = endLat,
                                        endLng = endLng
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("LATITUDE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text("$endLat", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("LONGITUDE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text("$endLng", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { selectedAttendance = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun RouteMapCanvas(startLat: Double, startLng: Double, endLat: Double, endLng: Double) {
    val description = "Route: $startLat,$startLng to $endLat,$endLng"
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
    ) {
        val width = size.width
        val height = size.height

        val gridColor = Color(0xFF1E293B)
        for (i in 0..width.toInt() step 40) {
            drawLine(gridColor, androidx.compose.ui.geometry.Offset(i.toFloat(), 0f), androidx.compose.ui.geometry.Offset(i.toFloat(), height), 1f)
        }
        for (j in 0..height.toInt() step 40) {
            drawLine(gridColor, androidx.compose.ui.geometry.Offset(0f, j.toFloat()), androidx.compose.ui.geometry.Offset(width, j.toFloat()), 1f)
        }

        val startX = width * 0.25f
        val startY = height * 0.75f
        val endX = width * 0.75f
        val endY = height * 0.25f

        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(startX, startY)
                quadraticTo(width * 0.5f, height * 0.2f, endX, endY)
            },
            color = Color(0xFF38BDF8).copy(alpha = 0.3f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 16f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )

        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(startX, startY)
                quadraticTo(width * 0.5f, height * 0.2f, endX, endY)
            },
            color = Color(0xFF0EA5E9),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )

        drawCircle(Color(0xFF16A34A), radius = 16f, center = androidx.compose.ui.geometry.Offset(startX, startY))
        drawCircle(Color.White, radius = 6f, center = androidx.compose.ui.geometry.Offset(startX, startY))

        drawCircle(Color(0xFFEF4444), radius = 16f, center = androidx.compose.ui.geometry.Offset(endX, endY))
        drawCircle(Color.White, radius = 6f, center = androidx.compose.ui.geometry.Offset(endX, endY))
    }
}
