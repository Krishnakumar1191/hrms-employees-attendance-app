package com.example.hr_employeemanagementsystem.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HRSideDrawer(
    currentRoute: String,
    isAdmin: Boolean,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    gesturesEnabled: Boolean = true,
    onNavigate: (route: String) -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "HR Management",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isAdmin) "Manager Workspace" else "Employee Workspace",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                        label = { Text("Manager Dashboard") },
                        selected = (currentRoute == "manager_dashboard"),
                        onClick = { onNavigate("manager_dashboard") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.EventBusy, contentDescription = null) },
                        label = { Text("Leave Requests") },
                        selected = (currentRoute == "leaves"),
                        onClick = { onNavigate("leaves") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.HomeWork, contentDescription = null) },
                        label = { Text("Work From Home") },
                        selected = (currentRoute == "work_from_home"),
                        onClick = { onNavigate("work_from_home") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Group, contentDescription = null) },
                        label = { Text("Employee Directory") },
                        selected = (currentRoute == "employee_list"),
                        onClick = { onNavigate("employee_list") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.CameraFront, contentDescription = null) },
                        label = { Text("Attendance Kiosk") },
                        selected = (currentRoute == "kiosk_home"),
                        onClick = { onNavigate("kiosk_home") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = null) },
                        label = { Text("Attendance Logs") },
                        selected = (currentRoute == "attendances"),
                        onClick = { onNavigate("attendances") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        label = { Text("Events & Holidays") },
                        selected = (currentRoute == "events"),
                        onClick = { onNavigate("events") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null) },
                        label = { Text("Payslips") },
                        selected = (currentRoute == "payslips"),
                        onClick = { onNavigate("payslips") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Settings") },
                        selected = (currentRoute == "settings"),
                        onClick = { onNavigate("settings") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("My Profile") },
                        selected = (currentRoute == "profile"),
                        onClick = { onNavigate("profile") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) },
                        label = { Text("User Manual") },
                        selected = (currentRoute == "user_manual"),
                        onClick = { onNavigate("user_manual") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                        label = { Text("Logout") },
                        selected = false,
                        onClick = onLogout,
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        },
        content = content
    )
}
