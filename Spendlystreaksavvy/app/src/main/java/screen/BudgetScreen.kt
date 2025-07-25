package com.example.spendly.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.spendly.data.Budget
import com.example.spendly.data.Transaction
import com.example.spendly.navigation.Routes
import com.example.spendly.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(navController: NavHostController, viewModel: AppViewModel) {
    val budgets by viewModel.budgets.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBudgetsFromFirebase()
        viewModel.loadTransactionsFromFirebase()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budgets") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Routes.NEW_BUDGET_GOAL)
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget Goal")
            }
        },
        bottomBar = { BottomNavBar(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (budgets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No budget goals set.", color = MaterialTheme.colorScheme.onBackground)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(budgets) { budget ->
                        val category = categories.find { it.id == budget.categoryId }
                        val spentInCategory = transactions
                            .filter { it.categoryId == budget.categoryId && it.type == "expense" }
                            .sumOf { it.amount }

                        BudgetCard(
                            budget = budget.copy(spent = spentInCategory),
                            categoryName = category?.name ?: "Unknown",
                            icon = category?.icon ?: "💸",
                            colorHex = category?.color ?: "#2196F3",
                            onDelete = { viewModel.deleteBudget(budget) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetCard(
    budget: Budget,
    categoryName: String,
    icon: String,
    colorHex: String,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    val dateString = dateFormat.format(Date(budget.date))
    val progress = (budget.spent / budget.maximum).coerceIn(0.0, 1.0).toFloat()
    val animatedProgress by animateFloatAsState(targetValue = progress)

    val barColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Title: ${budget.title}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Budget", tint = MaterialTheme.colorScheme.error)
                }
            }
            Text("Category: $icon $categoryName", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text("Limits: R%.2f - R%.2f".format(budget.minimum, budget.maximum), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text("Spent: R%.2f".format(budget.spent), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            LinearProgressIndicator(
                progress = animatedProgress,
                color = barColor,
                trackColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(6.dp)
            )
            Text("Month: $dateString", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
