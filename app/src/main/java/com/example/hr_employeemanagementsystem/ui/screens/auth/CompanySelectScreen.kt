package com.example.hr_employeemanagementsystem.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hr_employeemanagementsystem.data.repository.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun CompanySelectScreen(
    authRepository: AuthRepository,
    onCompanySelected: () -> Unit
) {
    val companies = listOf("Main Headquarters", "Downtown Branch", "Tech Park Office", "Remote Division")
    var selectedCompany by remember { mutableStateOf(companies.first()) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Select Workspace / Branch", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        companies.forEach { company ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { selectedCompany = company },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedCompany == company) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = (selectedCompany == company),
                        onClick = { selectedCompany = company }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(company, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    authRepository.selectCompany(selectedCompany)
                    onCompanySelected()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue to Workspace")
        }
    }
}
