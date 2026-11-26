package com.example.hr_employeemanagementsystem.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.hr_employeemanagementsystem.data.local.db.AppDatabase
import com.example.hr_employeemanagementsystem.data.model.EventEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(db: AppDatabase) {
    val events by db.eventDao().getAllEvents().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }

    // Calendar state
    val calendar = remember { Calendar.getInstance() }
    var displayedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var displayedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) } // 0-11
    var selectedDateStr by remember { mutableStateOf<String?>(null) }

    // Seed initial comprehensive calendar events & holidays if empty
    LaunchedEffect(Unit) {
        val currentEvents = events
        if (currentEvents.isEmpty()) {
            val initialEvents = listOf(
                EventEntity(title = "New Year's Day", date = "2026-01-01", description = "Public Holiday - Office Closed", type = "Holiday"),
                EventEntity(title = "Republic Day", date = "2026-01-26", description = "National Holiday - Flag Hoisting at Office", type = "Holiday"),
                EventEntity(title = "Q1 Strategy Town Hall", date = "2026-02-15", description = "Company-wide quarterly review meeting in Main Auditorium", type = "Meeting"),
                EventEntity(title = "Arun Kumar's Birthday", date = "2026-03-10", description = "Team celebration & cake cutting at 4:00 PM", type = "Birthday"),
                EventEntity(title = "Spring Team Outing", date = "2026-04-20", description = "Annual resort day out & team building activities", type = "Announcement"),
                EventEntity(title = "Independence Day", date = "2026-08-15", description = "National Holiday - Office Closed", type = "Holiday"),
                EventEntity(title = "Project Milestone Review", date = "2026-09-10", description = "Review meeting with client stakeholders", type = "Meeting"),
                EventEntity(title = "Gandhi Jayanti", date = "2026-10-02", description = "National Holiday - Office Closed", type = "Holiday"),
                EventEntity(title = "Diwali Festival", date = "2026-11-12", description = "Festival Holiday - Office Closed", type = "Holiday"),
                EventEntity(title = "Christmas Day", date = "2026-12-25", description = "Public Holiday - Office Closed", type = "Holiday")
            )
            db.eventDao().insertEvents(initialEvents)
        }
    }

    // Format month header title (e.g., "November 2026")
    val monthNameFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val tempCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, displayedYear)
        set(Calendar.MONTH, displayedMonth)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val monthTitle = monthNameFormat.format(tempCal.time)

    // Calculate days for the month grid
    val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sun, 1 = Mon, etc.

    val gridCells = mutableListOf<Int?>()
    for (i in 0 until firstDayOfWeek) {
        gridCells.add(null)
    }
    for (day in 1..daysInMonth) {
        gridCells.add(day)
    }

    val filters = listOf("All", "Holiday", "Meeting", "Birthday", "Announcement")

    // Filter events based on selected filter and month/selected date
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val filteredEvents = events.filter { event ->
        try {
            val eventDate = dateFormatter.parse(event.date)
            if (eventDate != null) {
                val cal = Calendar.getInstance().apply { time = eventDate }
                val matchesMonth = cal.get(Calendar.YEAR) == displayedYear && cal.get(Calendar.MONTH) == displayedMonth
                val matchesType = selectedFilter == "All" || event.type.equals(selectedFilter, ignoreCase = true)

                if (selectedDateStr != null) {
                    matchesMonth && matchesType && event.date == selectedDateStr
                } else {
                    matchesMonth && matchesType
                }
            } else false
        } catch (_: Exception) {
            false
        }
    }.distinctBy { "${it.title}_${it.date}_${it.type}" }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // Header with Month Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("My Calendar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if (displayedMonth == 0) {
                            displayedMonth = 11
                            displayedYear -= 1
                        } else {
                            displayedMonth -= 1
                        }
                        selectedDateStr = null
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                    }
                    Text(monthTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    IconButton(onClick = {
                        if (displayedMonth == 11) {
                            displayedMonth = 0
                            displayedYear += 1
                        } else {
                            displayedMonth += 1
                        }
                        selectedDateStr = null
                    }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            ScrollableTabRow(
                selectedTabIndex = filters.indexOf(selectedFilter),
                edgePadding = 0.dp,
                divider = {}
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = (selectedFilter == filter),
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Google Calendar Style Grid Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Day of week headers
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                            Text(day, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Month Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.height(240.dp),
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

                                val dayEvents = events.filter { it.date == dateString }
                                val hasEvents = dayEvents.isNotEmpty()
                                val isSelected = selectedDateStr == dateString

                                val dotColor = if (hasEvents) {
                                    when (dayEvents.first().type.lowercase()) {
                                        "holiday" -> Color(0xFF16A34A)
                                        "meeting" -> Color(0xFF2563EB)
                                        "birthday" -> Color(0xFF9333EA)
                                        else -> Color(0xFFEA580C)
                                    }
                                } else Color.Transparent

                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                hasEvents -> dotColor.copy(alpha = 0.15f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .clickable {
                                            selectedDateStr = if (selectedDateStr == dateString) null else dateString
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayNum.toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected || hasEvents) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                hasEvents -> dotColor
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                        if (hasEvents && !isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(dotColor)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Events List Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (selectedDateStr != null) "Events on $selectedDateStr" else "Events in $monthTitle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (selectedDateStr != null) {
                    TextButton(onClick = { selectedDateStr = null }) {
                        Text("Show All")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredEvents.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text("No events scheduled for this selection.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredEvents, key = { it.id }) { event ->
                        val accentColor = when (event.type.lowercase()) {
                            "holiday" -> Color(0xFF16A34A) // Green
                            "meeting" -> Color(0xFF2563EB) // Blue
                            "birthday" -> Color(0xFF9333EA) // Purple
                            else -> Color(0xFFEA580C) // Orange
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .height(80.dp)
                                        .background(accentColor)
                                )
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                        Badge(containerColor = accentColor) {
                                            Text(event.type, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("📅 ${event.date}", style = MaterialTheme.typography.bodyMedium, color = accentColor, fontWeight = FontWeight.Medium)
                                    Text(event.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEventDialog(
            onDismiss = { showAddDialog = false },
            onSubmit = { title, date, desc, type ->
                scope.launch {
                    db.eventDao().insertEvent(
                        EventEntity(title = title, date = date, description = desc, type = type)
                    )
                    showAddDialog = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, date: String, desc: String, type: String) -> Unit
) {
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today) }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Meeting") }
    var expanded by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Calendar Event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event Title") }, modifier = Modifier.fillMaxWidth())

                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Event Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("Holiday", "Meeting", "Birthday", "Announcement").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    type = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank()) onSubmit(title, date, description, type) }) {
                Text("Add to Calendar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
