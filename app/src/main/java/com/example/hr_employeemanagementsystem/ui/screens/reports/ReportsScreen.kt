package com.example.hr_employeemanagementsystem.ui.screens.reports

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hr_employeemanagementsystem.data.repository.AttendanceRepository
import com.example.hr_employeemanagementsystem.data.repository.LeaveRepository
import com.example.hr_employeemanagementsystem.data.repository.WFHRepository
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    attendanceRepository: AttendanceRepository,
    leaveRepository: LeaveRepository,
    wfhRepository: WFHRepository
) {
    val context = LocalContext.current
    val allAttendances by attendanceRepository.getAllAttendances().collectAsState(initial = emptyList())
    val allLeaves by leaveRepository.getAllLeaves().collectAsState(initial = emptyList())
    val allWfh by wfhRepository.getAllWFHRequests().collectAsState(initial = emptyList())

    var filterType by remember { mutableStateOf("This Month") }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var startDate by remember { mutableStateOf(todayStr) }
    var endDate by remember { mutableStateOf(todayStr) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val startDateState = rememberDatePickerState()
    val endDateState = rememberDatePickerState()

    val filterOptions = listOf("Today", "This Week", "This Month", "Custom Range")
    var filterExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Attendance & Activity Reports", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.Assessment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }

        // Filter Dropdown
        ExposedDropdownMenuBox(
            expanded = filterExpanded,
            onExpandedChange = { filterExpanded = !filterExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = filterType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Report Period") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = filterExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = filterExpanded,
                onDismissRequest = { filterExpanded = false }
            ) {
                filterOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            filterType = option
                            filterExpanded = false
                        }
                    )
                }
            }
        }

        if (filterType == "Custom Range") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showStartPicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Start: $startDate")
                }
                OutlinedButton(
                    onClick = { showEndPicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("End: $endDate")
                }
            }
        }

        // Export Actions
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    exportToCsv(context, allAttendances, allLeaves, allWfh)
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export Excel (CSV)")
            }
            Button(
                onClick = {
                    exportToPdf(context, allAttendances, allLeaves, allWfh)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Download PDF")
            }
        }

        HorizontalDivider()

        Text("Report Preview Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Attendance Logs: ${allAttendances.size}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Total Leave Requests: ${allLeaves.size}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Total WFH Requests: ${allWfh.size}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            items(allAttendances, key = { it.id }) { log ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(log.employeeName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Check-In: ${log.checkIn} | Check-Out: ${log.checkOut ?: "In Progress"}", style = MaterialTheme.typography.bodySmall)
                        Text("Status: ${log.status} • Worked: ${log.workedHours} hrs", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    if (showStartPicker) {
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDateState.selectedDateMillis?.let { millis ->
                        startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                    }
                    showStartPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartPicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = startDateState)
        }
    }

    if (showEndPicker) {
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDateState.selectedDateMillis?.let { millis ->
                        endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                    }
                    showEndPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndPicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = endDateState)
        }
    }
}

fun exportToCsv(
    context: Context,
    attendances: List<com.example.hr_employeemanagementsystem.data.model.AttendanceEntity>,
    leaves: List<com.example.hr_employeemanagementsystem.data.model.LeaveEntity>,
    wfh: List<com.example.hr_employeemanagementsystem.data.model.WFHEntity>
) {
    try {
        val csvHeader = "EmployeeName,Type,CheckIn/StartDate,CheckOut/EndDate,Status,Details\n"
        val csvRows = StringBuilder(csvHeader)

        attendances.forEach {
            csvRows.append("\"${it.employeeName}\",\"Attendance\",\"${it.checkIn}\",\"${it.checkOut ?: "In Progress"}\",\"${it.status}\",\"Worked: ${it.workedHours} hrs\"\n")
        }
        leaves.forEach {
            csvRows.append("\"${it.employeeName}\",\"Leave (${it.leaveType})\",\"${it.startDate}\",\"${it.endDate}\",\"${it.status}\",\"Reason: ${it.reason}\"\n")
        }
        wfh.forEach {
            csvRows.append("\"${it.employeeName}\",\"WFH\",\"${it.startDate}\",\"${it.endDate}\",\"${it.status}\",\"Reason: ${it.reason}\"\n")
        }

        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "HRMS_Attendance_Report_${System.currentTimeMillis()}.csv")
        file.writeText(csvRows.toString())
        Toast.makeText(context, "Excel (CSV) Exported to Downloads!", Toast.LENGTH_LONG).show()
    } catch (_: Exception) {
        try {
            val file = File(context.cacheDir, "HRMS_Attendance_Report.csv")
            val csvHeader = "EmployeeName,Type,CheckIn/StartDate,CheckOut/EndDate,Status,Details\n"
            val csvRows = StringBuilder(csvHeader)
            attendances.forEach {
                csvRows.append("\"${it.employeeName}\",\"Attendance\",\"${it.checkIn}\",\"${it.checkOut ?: "In Progress"}\",\"${it.status}\",\"Worked: ${it.workedHours} hrs\"\n")
            }
            file.writeText(csvRows.toString())
            Toast.makeText(context, "Exported to Cache: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (ex: Exception) {
            Toast.makeText(context, "Export failed: ${ex.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

fun exportToPdf(
    context: Context,
    attendances: List<com.example.hr_employeemanagementsystem.data.model.AttendanceEntity>,
    leaves: List<com.example.hr_employeemanagementsystem.data.model.LeaveEntity>,
    wfh: List<com.example.hr_employeemanagementsystem.data.model.WFHEntity>
) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint().apply {
            textSize = 14f
            color = android.graphics.Color.BLACK
        }

        var y = 40f
        canvas.drawText("HRMS Official Attendance & Activity Report", 40f, y, paint.apply { textSize = 18f; isFakeBoldText = true })
        y += 30f
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}", 40f, y, paint)
        y += 40f

        canvas.drawText("--- ATTENDANCE LOGS ---", 40f, y, paint.apply { isFakeBoldText = true })
        y += 20f
        paint.isFakeBoldText = false

        attendances.take(15).forEach {
            if (y > 800f) return@forEach
            canvas.drawText("• ${it.employeeName} | In: ${it.checkIn} | Out: ${it.checkOut ?: "Active"} | ${it.status}", 40f, y, paint)
            y += 20f
        }

        y += 20f
        canvas.drawText("--- LEAVE REQUESTS ---", 40f, y, paint.apply { isFakeBoldText = true })
        y += 20f
        paint.isFakeBoldText = false

        leaves.take(10).forEach {
            if (y > 800f) return@forEach
            canvas.drawText("• ${it.employeeName} | ${it.leaveType} | ${it.startDate} to ${it.endDate} | ${it.status}", 40f, y, paint)
            y += 20f
        }

        pdfDocument.finishPage(page)

        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "HRMS_Report_${System.currentTimeMillis()}.pdf")
        val outputStream = FileOutputStream(file)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        outputStream.close()

        Toast.makeText(context, "PDF Report Downloaded to Downloads!", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "PDF Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
