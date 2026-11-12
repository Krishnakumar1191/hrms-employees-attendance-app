package com.example.hr_employeemanagementsystem.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HRSideDrawer(
    currentRoute: String,
    isAdmin: Boolean,
    onNavigate: (route: String) -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "HR Management System",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                Divider()

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Manager Dashboard") },
                    selected = (currentRoute == "manager_dashboard"),
                    onClick = { onNavigate("manager_dashboard") }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.EventBusy, contentDescription = null) },
                    label = { Text("Leave Requests") },
                    selected = (currentRoute == "leaves"),
                    onClick = { onNavigate("leaves") }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.HomeWork, contentDescription = null) },
                    label = { Text("Work From Home") },
                    selected = (currentRoute == "work_from_home"),
                    onClick = { onNavigate("work_from_home") }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Group, contentDescription = null) },
                    label = { Text("Employee Directory") },
                    selected = (currentRoute == "employee_list"),
                    onClick = { onNavigate("employee_list") }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.CameraFront, contentDescription = null) },
                    label = { Text("Attendance Kiosk") },
                    selected = (currentRoute == "kiosk_home"),
                    onClick = { onNavigate("kiosk_home") }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ListAlt, contentDescription = null) },
                    label = { Text("Attendance Logs") },
                    selected = (currentRoute == "attendances"),
                    onClick = { onNavigate("attendances") }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text("Events & Holidays") },
                    selected = (currentRoute == "events"),
                    onClick = { onNavigate("events") }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                    label = { Text("Payslips") },
                    selected = (currentRoute == "payslips"),
                    onClick = { onNavigate("payslips") }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") },
                    selected = (currentRoute == "settings"),
                    onClick = { onNavigate("settings") }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("My Profile") },
                    selected = (currentRoute == "profile"),
                    onClick = { onNavigate("profile") }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    label = { Text("User Manual") },
                    selected = (currentRoute == "user_manual"),
                    onClick = { onNavigate("user_manual") }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Logout, contentDescription = null) },
                    label = { Text("Logout") },
                    selected = false,
                    onClick = onLogout
                )
            }
        },
        content = content
    )
}
