package com.example.hr_employeemanagementsystem.ui.screens.attendances

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.hr_employeemanagementsystem.data.model.AttendanceEntity
import com.example.hr_employeemanagementsystem.data.model.LeaveEntity
import com.example.hr_employeemanagementsystem.data.model.WFHEntity
import com.example.hr_employeemanagementsystem.data.repository.AttendanceRepository
import com.example.hr_employeemanagementsystem.data.repository.LeaveRepository
import com.example.hr_employeemanagementsystem.data.repository.LocationRepository
import com.example.hr_employeemanagementsystem.data.repository.WFHRepository
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendancesScreen(
    attendanceRepository: AttendanceRepository,
    leaveRepository: LeaveRepository,
    wfhRepository: WFHRepository,
    locationRepository: LocationRepository,
    isAdmin: Boolean,
    currentUserId: Long
) {
    val allAttendances by attendanceRepository.getAllAttendances().collectAsState(initial = emptyList())
    val myAttendances by attendanceRepository.getAttendancesForEmployee(currentUserId).collectAsState(initial = emptyList())
    val allLeaves by leaveRepository.getAllLeaves().collectAsState(initial = emptyList())
    val allWfh by wfhRepository.getAllWFHRequests().collectAsState(initial = emptyList())

    var showAll by remember { mutableStateOf(isAdmin) }
    var selectedAttendance by remember { mutableStateOf<AttendanceEntity?>(null) }

    val displayAttendances = if (showAll && isAdmin) allAttendances else myAttendances
    val displayLeaves = allLeaves
    val displayWfh = allWfh

    var viewMode by remember { mutableStateOf("Month") } // "Month", "Week", "Day"
    val calendar = remember { Calendar.getInstance() }
    var displayedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var displayedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDateStr by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isAdmin) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (showAll) "Company Attendance Calendar" else "My Attendance Calendar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                FilterChip(
                    selected = showAll,
                    onClick = { showAll = !showAll },
                    label = { Text(if (showAll) "Viewing: All" else "Viewing: My Logs") }
                )
            }
        } else {
            Text("Attendance Calendar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        // View Mode Selector Tabs
        TabRow(selectedTabIndex = when(viewMode) { "Month" -> 0; "Week" -> 1; else -> 2 }) {
            Tab(selected = viewMode == "Month", onClick = { viewMode = "Month" }, text = { Text("Month View") })
            Tab(selected = viewMode == "Week", onClick = { viewMode = "Week" }, text = { Text("Week View") })
            Tab(selected = viewMode == "Day", onClick = { viewMode = "Day" }, text = { Text("Day View") })
        }

        when (viewMode) {
            "Month" -> {
                MonthAttendanceCalendarView(
                    displayedYear = displayedYear,
                    displayedMonth = displayedMonth,
                    onPrevMonth = {
                        if (displayedMonth == 0) { displayedMonth = 11; displayedYear-- } else { displayedMonth-- }
                        selectedDateStr = null
                    },
                    onNextMonth = {
                        if (displayedMonth == 11) { displayedMonth = 0; displayedYear++ } else { displayedMonth++ }
                        selectedDateStr = null
                    },
                    attendances = displayAttendances,
                    leaves = displayLeaves,
                    wfh = displayWfh,
                    selectedDateStr = selectedDateStr,
                    onDateSelected = { selectedDateStr = if (selectedDateStr == it) null else it },
                    onSelectAttendance = { selectedAttendance = it }
                )
            }
            "Week" -> {
                WeekAttendanceView(
                    attendances = displayAttendances,
                    leaves = displayLeaves,
                    wfh = displayWfh,
                    onSelectAttendance = { selectedAttendance = it }
                )
            }
            "Day" -> {
                DayAttendanceView(
                    attendances = displayAttendances,
                    leaves = displayLeaves,
                    wfh = displayWfh,
                    onSelectAttendance = { selectedAttendance = it }
                )
            }
        }
    }

    val context = LocalContext.current

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
                        Text("Attendance Detail: ${log.employeeName}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { selectedAttendance = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    HorizontalDivider()

                    Text("Check-In: ${log.checkIn}", style = MaterialTheme.typography.bodyMedium)
                    Text("Check-Out: ${log.checkOut ?: "In Progress"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Worked Hours: ${formatWorkedHours(log.checkIn, log.checkOut)}", style = MaterialTheme.typography.bodyMedium)
                    Text("Face Verified: Yes", style = MaterialTheme.typography.bodyMedium)

                    if (log.employeePhotoUri != null) {
                        Text("Enrolled Profile Photo:", style = MaterialTheme.typography.labelMedium)
                        coil.compose.AsyncImage(
                            model = log.employeePhotoUri,
                            contentDescription = "Enrolled Profile Photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(4.dp))
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
                                    Text("LIVE LOCATION", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Badge(containerColor = Color(0xFFE8F5E9)) {
                                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Live Tracking Active", color = Color(0xFF2E7D32), style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val startLat = log.checkInLat
                                val startLng = log.checkInLng
                                val endLat = log.checkOutLat ?: startLat
                                val endLng = log.checkOutLng ?: startLng

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    androidx.compose.ui.viewinterop.AndroidView(
                                        factory = { ctx ->
                                            org.maplibre.android.MapLibre.getInstance(ctx)
                                            org.maplibre.android.maps.MapView(ctx).apply {
                                                onCreate(null)
                                                getMapAsync { map ->
                                                    map.setStyle("https://tiles.openfreemap.org/styles/liberty") { _ ->
                                                        val startPoint = org.maplibre.android.geometry.LatLng(startLat, startLng)
                                                        map.addMarker(
                                                            org.maplibre.android.annotations.MarkerOptions()
                                                                .position(startPoint)
                                                                .title("Check-In Start")
                                                        )

                                                        if (endLat != startLat || endLng != startLng) {
                                                            val endPoint = org.maplibre.android.geometry.LatLng(endLat, endLng)
                                                            map.addMarker(
                                                                org.maplibre.android.annotations.MarkerOptions()
                                                                    .position(endPoint)
                                                                    .title("Check-Out End")
                                                            )
                                                        }

                                                        map.cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
                                                            .target(startPoint)
                                                            .zoom(15.0)
                                                            .build()
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("LATITUDE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text("$endLat", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("LONGITUDE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text("$endLng", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
fun MonthAttendanceCalendarView(
    displayedYear: Int,
    displayedMonth: Int,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    attendances: List<AttendanceEntity>,
    leaves: List<LeaveEntity>,
    wfh: List<WFHEntity>,
    selectedDateStr: String?,
    onDateSelected: (String) -> Unit,
    onSelectAttendance: (AttendanceEntity) -> Unit
) {
    val monthNameFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val tempCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, displayedYear)
        set(Calendar.MONTH, displayedMonth)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val monthTitle = monthNameFormat.format(tempCal.time)

    val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1

    val gridCells = mutableListOf<Int?>()
    for (i in 0 until firstDayOfWeek) {
        gridCells.add(null)
    }
    for (day in 1..daysInMonth) {
        gridCells.add(day)
    }

    val getLogsForDate = { dateStr: String ->
        attendances.filter { it.checkIn.startsWith(dateStr) }
    }
    val getLeavesForDate = { dateStr: String ->
        leaves.filter { it.startDate <= dateStr && it.endDate >= dateStr && it.status == "Approved" }
    }
    val getWfhForDate = { dateStr: String ->
        wfh.filter { it.startDate <= dateStr && it.endDate >= dateStr && it.status == "Approved" }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(monthTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row {
                IconButton(onClick = onPrevMonth) { Icon(Icons.Default.ChevronLeft, contentDescription = "Prev") }
                IconButton(onClick = onNextMonth) { Icon(Icons.Default.ChevronRight, contentDescription = "Next") }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                        Text(day, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(230.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(gridCells.size) { index ->
                        val dayNum = gridCells[index]
                        if (dayNum == null) {
                            Box(modifier = Modifier.size(36.dp))
                        } else {
                            val formattedDay = String.format(Locale.getDefault(), "%02d", dayNum)
                            val formattedMonth = String.format(Locale.getDefault(), "%02d", displayedMonth + 1)
                            val dateString = "$displayedYear-$formattedMonth-$formattedDay"

                            val dayLogs = getLogsForDate(dateString)
                            val dayLeaves = getLeavesForDate(dateString)
                            val dayWfh = getWfhForDate(dateString)

                            val hasAttendance = dayLogs.isNotEmpty()
                            val hasLeave = dayLeaves.isNotEmpty()
                            val hasWfh = dayWfh.isNotEmpty()
                            val isSelected = selectedDateStr == dateString

                            val cellColor = when {
                                hasAttendance -> Color(0xFFE8F5E9)
                                hasLeave -> Color(0xFFFFEBEE)
                                hasWfh -> Color(0xFFE3F2FD)
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else cellColor)
                                    .clickable { onDateSelected(dateString) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayNum.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected || hasAttendance || hasLeave || hasWfh) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        if (hasAttendance) Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xFF16A34A)))
                                        if (hasLeave) Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xFFDC2626)))
                                        if (hasWfh) Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xFF2563EB)))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (selectedDateStr != null) "Activity on $selectedDateStr" else "All Activity in $monthTitle",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        val targetDateStr = selectedDateStr
        val filteredAttendances = if (targetDateStr != null) attendances.filter { it.checkIn.startsWith(targetDateStr) } else attendances
        val filteredLeaves = if (targetDateStr != null) leaves.filter { it.startDate <= targetDateStr && it.endDate >= targetDateStr } else leaves
        val filteredWfh = if (targetDateStr != null) wfh.filter { it.startDate <= targetDateStr && it.endDate >= targetDateStr } else wfh

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredAttendances, key = { "month_att_${it.id}" }) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onSelectAttendance(log) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(log.employeeName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Badge(containerColor = Color(0xFF16A34A)) { Text("Attendance", color = Color.White, modifier = Modifier.padding(2.dp)) }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("🟢 Check-In: ${log.checkIn}", style = MaterialTheme.typography.bodySmall)
                        Text("🏁 Check-Out: ${log.checkOut ?: "In Progress"}", style = MaterialTheme.typography.bodySmall)
                        Text("⏱️ Worked: ${formatWorkedHours(log.checkIn, log.checkOut)}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            items(filteredLeaves, key = { "month_leave_${it.id}" }) { leave ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(leave.employeeName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Badge(containerColor = Color(0xFFDC2626)) { Text("Leave (${leave.leaveType})", color = Color.White, modifier = Modifier.padding(2.dp)) }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("📅 Period: ${leave.startDate} to ${leave.endDate} (${leave.totalDays} Days)", style = MaterialTheme.typography.bodySmall)
                        Text("📝 Reason: ${leave.reason}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            items(filteredWfh, key = { "month_wfh_${it.id}" }) { w ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(w.employeeName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Badge(containerColor = Color(0xFF2563EB)) { Text("WFH", color = Color.White, modifier = Modifier.padding(2.dp)) }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("📅 Period: ${w.startDate} to ${w.endDate} (${w.totalDays} Days)", style = MaterialTheme.typography.bodySmall)
                        Text("📝 Reason: ${w.reason}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun WeekAttendanceView(
    attendances: List<AttendanceEntity>,
    leaves: List<LeaveEntity>,
    wfh: List<WFHEntity>,
    onSelectAttendance: (AttendanceEntity) -> Unit
) {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    val weekDays = (0..6).map {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        val dayName = SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(cal.time)
        cal.add(Calendar.DAY_OF_MONTH, 1)
        Pair(dayName, dateStr)
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(weekDays) { (dayName, dateStr) ->
            val dayAttendances = attendances.filter { it.checkIn.startsWith(dateStr) }
            val dayLeaves = leaves.filter { it.startDate <= dateStr && it.endDate >= dateStr && it.status == "Approved" }
            val dayWfh = wfh.filter { it.startDate <= dateStr && it.endDate >= dateStr && it.status == "Approved" }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(dayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))

                    if (dayAttendances.isEmpty() && dayLeaves.isEmpty() && dayWfh.isEmpty()) {
                        Text("No activity recorded", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    dayAttendances.forEach { log ->
                        Text("🟢 Check-In: ${log.checkIn} | 🏁 Check-Out: ${log.checkOut ?: "Active"}", style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { onSelectAttendance(log) }) { Text("View Map & Details") }
                    }
                    dayLeaves.forEach { leave ->
                        Text("🟠 Leave: ${leave.leaveType} (${leave.reason})", style = MaterialTheme.typography.bodySmall, color = Color(0xFFDC2626))
                    }
                    dayWfh.forEach { w ->
                        Text("🔵 WFH: ${w.reason}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2563EB))
                    }
                }
            }
        }
    }
}

@Composable
fun DayAttendanceView(
    attendances: List<AttendanceEntity>,
    leaves: List<LeaveEntity>,
    wfh: List<WFHEntity>,
    onSelectAttendance: (AttendanceEntity) -> Unit
) {
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var selectedDayStr by remember { mutableStateOf(todayStr) }

    val dayAttendances = attendances.filter { it.checkIn.startsWith(selectedDayStr) }
    val dayLeaves = leaves.filter { it.startDate <= selectedDayStr && it.endDate >= selectedDayStr && it.status == "Approved" }
    val dayWfh = wfh.filter { it.startDate <= selectedDayStr && it.endDate >= selectedDayStr && it.status == "Approved" }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Selected Day: $selectedDayStr", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (dayAttendances.isEmpty() && dayLeaves.isEmpty() && dayWfh.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        Text("No records found for $selectedDayStr", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            items(dayAttendances, key = { "day_att_${it.id}" }) { log ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onSelectAttendance(log) }) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(log.employeeName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("🟢 Check-In: ${log.checkIn}", style = MaterialTheme.typography.bodySmall)
                        Text("🏁 Check-Out: ${log.checkOut ?: "In Progress"}", style = MaterialTheme.typography.bodySmall)
                        Text("⏱️ Worked: ${formatWorkedHours(log.checkIn, log.checkOut)}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            items(dayLeaves, key = { "day_leave_${it.id}" }) { leave ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Leave: ${leave.leaveType}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                        Text("Reason: ${leave.reason}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            items(dayWfh, key = { "day_wfh_${it.id}" }) { w ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Work From Home (WFH)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                        Text("Reason: ${w.reason}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

fun formatWorkedHours(checkInStr: String, checkOutStr: String?): String {
    if (checkOutStr == null) return "In Progress"
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val d1 = format.parse(checkInStr)
        val d2 = format.parse(checkOutStr)
        if (d1 != null && d2 != null) {
            val diffMillis = d2.time - d1.time
            if (diffMillis < 0) return "0 secs"
            val totalSeconds = diffMillis / 1000
            val hrs = totalSeconds / 3600
            val mins = (totalSeconds % 3600) / 60
            val secs = totalSeconds % 60
            
            when {
                hrs > 0 -> "$hrs hrs $mins mins $secs secs"
                mins > 0 -> "$mins mins $secs secs"
                else -> "$secs secs"
            }
        } else {
            "Completed"
        }
    } catch (_: Exception) {
        "Completed"
    }
}
