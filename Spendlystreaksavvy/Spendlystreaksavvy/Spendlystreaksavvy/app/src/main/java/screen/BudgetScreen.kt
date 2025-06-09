package com.example.spendly.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.spendly.data.Budget
import com.example.spendly.navigation.Routes
import com.example.spendly.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(navController: NavHostController, viewModel: AppViewModel) {
    val budgets by viewModel.budgets.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBudgetsFromFirebase()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budgets") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Routes.NEW_BUDGET_GOAL)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget Goal")
            }
        },
        bottomBar = {
            BottomNavBar(navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (budgets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No budget goals set.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(budgets) { budget ->
                        BudgetCard(budget)
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetCard(budget: Budget) {
    val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(budget.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Min: R${budget.minimum}   Max: R${budget.maximum}", style = MaterialTheme.typography.bodyMedium)
            Text("Set for: ${dateFormat.format(Date(budget.date))}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
