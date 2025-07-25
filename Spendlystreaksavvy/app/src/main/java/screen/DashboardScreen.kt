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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.spendly.data.Budget
import com.example.spendly.data.Transaction
import com.example.spendly.navigation.Routes
import com.example.spendly.ui.components.SpendingGroupedChart
import com.example.spendly.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavHostController, viewModel: AppViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val budgets by viewModel.budgets.collectAsState()

    var selectedPeriod by remember { mutableStateOf(PeriodOption.LAST_30_DAYS) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val selectedCategories = remember { mutableStateMapOf<String, Boolean>() }

    val now = Calendar.getInstance()
    val currentMonth = now.get(Calendar.MONTH)
    val currentYear = now.get(Calendar.YEAR)

    val currentMonthBudgets = budgets.filter { it.month == currentMonth && it.year == currentYear }

    LaunchedEffect(categories) {
        categories.forEach {
            if (selectedCategories[it.id] == null) {
                selectedCategories[it.id] = true
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadBudgetsFromFirebase()
        viewModel.loadCategoriesFromFirebase()
    }

    val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
    val expenses = transactions.filter { it.type == "expense" }.sumOf { it.amount }
    val balance = income - expenses

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Routes.NEW_TRANSACTION)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        },
        bottomBar = {
            BottomNavBar(navController)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Dashboard", style = MaterialTheme.typography.headlineSmall)
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("Balance", "R%.2f".format(balance), Color(0xFF4CAF50), Modifier.weight(1f))
                    StatCard("Income", "R%.2f".format(income), Color(0xFF2196F3), Modifier.weight(1f))
                    StatCard("Expenses", "R%.2f".format(expenses), Color(0xFFF44336), Modifier.weight(1f))
                }
            }

            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filter Categories", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { isDropdownExpanded = true }) {
                        Text(selectedPeriod.label)
                    }
                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        PeriodOption.values().forEach {
                            DropdownMenuItem(
                                onClick = {
                                    selectedPeriod = it
                                    isDropdownExpanded = false
                                },
                                text = { Text(it.label) }
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategories[category.id] == true,
                            onClick = {
                                selectedCategories[category.id] = !(selectedCategories[category.id] ?: true)
                            },
                            label = { Text("${category.icon} ${category.name}") },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (selectedCategories[category.id] == true)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }

                    AssistChip(
                        onClick = { navController.navigate(Routes.ADD_CATEGORY) },
                        label = { Text("Add") },
                        leadingIcon = {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    )
                }
            }

            item {
                Text("Spending Chart", style = MaterialTheme.typography.titleMedium)
            }

            item {
                val periodStart = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -selectedPeriod.daysBack)
                }.timeInMillis

                val filteredTx = transactions.filter {
                    it.type == "expense" &&
                            it.date >= periodStart &&
                            selectedCategories[it.categoryId] == true
                }

                val filteredCategories = categories.filter { selectedCategories[it.id] == true }

                val actuals = filteredCategories.map { cat ->
                    filteredTx.filter { it.categoryId == cat.id }.sumOf { it.amount }.toFloat()
                }

                val mins = filteredCategories.map { cat ->
                    currentMonthBudgets.find { it.categoryId == cat.id }?.minimum?.toFloat() ?: 0f
                }

                val maxs = filteredCategories.map { cat ->
                    currentMonthBudgets.find { it.categoryId == cat.id }?.maximum?.toFloat() ?: 0f
                }

                SpendingGroupedChart(
                    labels = filteredCategories.map { "${it.icon} ${it.name}" },
                    actuals = actuals,
                    mins = mins,
                    maxs = maxs
                )
            }

            item {
                Text("Your Budgets", style = MaterialTheme.typography.titleMedium)
            }

            items(currentMonthBudgets) { budget ->
                val category = categories.find { it.id == budget.categoryId }
                DashboardBudgetCard(
                    budget,
                    categoryName = category?.name ?: "Unknown",
                    icon = category?.icon ?: "📁",
                    colorHex = category?.color ?: ""
                )
            }

            item {
                Text("Recent Transactions", style = MaterialTheme.typography.titleMedium)
            }

            items(transactions.take(5)) { transaction ->
                val category = categories.find { it.id == transaction.categoryId }
                TransactionCard(
                    transaction = transaction,
                    icon = category?.icon ?: "💸",
                    colorHex = category?.color ?: ""
                )
            }
        }
    }
}

@Composable
fun StatCard(title: String, amount: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = MaterialTheme.colorScheme.onPrimary)
            Text(amount, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun DashboardBudgetCard(
    budget: Budget,
    categoryName: String,
    icon: String,
    colorHex: String
) {
    val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    val dateString = dateFormat.format(Date(budget.date))

    val containerColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Title: ${budget.title}", color = MaterialTheme.colorScheme.onSurface)
            Text("Category: $icon $categoryName", color = MaterialTheme.colorScheme.onSurface)
            Text("Limits: R%.2f - R%.2f".format(budget.minimum, budget.maximum), color = MaterialTheme.colorScheme.onSurface)
            Text("Spent: R%.2f".format(budget.spent), color = MaterialTheme.colorScheme.onSurface)
            Text("Month: $dateString", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun TransactionCard(
    transaction: Transaction,
    icon: String,
    colorHex: String
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateString = dateFormat.format(Date(transaction.date))

    val containerColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("$icon ${transaction.description}", color = MaterialTheme.colorScheme.onSurface)
            Text("R%.2f".format(transaction.amount), color = MaterialTheme.colorScheme.onSurface)
            Text(dateString, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

enum class PeriodOption(val label: String, val daysBack: Int) {
    LAST_7_DAYS("Last 7 Days", 7),
    LAST_30_DAYS("Last 30 Days", 30),
    LAST_90_DAYS("Last 90 Days", 90)
}
