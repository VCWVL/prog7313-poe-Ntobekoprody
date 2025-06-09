package com.example.spendly.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.spendly.data.Transaction
import com.example.spendly.navigation.Routes
import com.example.spendly.ui.components.SpendingBarChart
import com.example.spendly.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class PeriodOption(val label: String, val daysBack: Int) {
    LAST_7_DAYS("Last 7 Days", 7),
    LAST_30_DAYS("Last 30 Days", 30),
    LAST_90_DAYS("Last 90 Days", 90)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavHostController, viewModel: AppViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val categories = viewModel.categories.collectAsState().value

    var selectedPeriod by remember { mutableStateOf(PeriodOption.LAST_30_DAYS) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val selectedCategories = remember {
        mutableStateMapOf<String, Boolean>()
    }

    LaunchedEffect(categories) {
        categories.forEach { cat ->
            if (selectedCategories[cat.id] == null) {
                selectedCategories[cat.id] = true
            }
        }
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Dashboard", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard("Balance", "R%.2f".format(balance), Color(0xFF4CAF50), Modifier.weight(1f))
                StatCard("Income", "R%.2f".format(income), Color(0xFF2196F3), Modifier.weight(1f))
                StatCard("Expenses", "R%.2f".format(expenses), Color(0xFFF44336), Modifier.weight(1f))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(onClick = { isDropdownExpanded = true }) {
                    Text(selectedPeriod.label)
                }
                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    PeriodOption.values().forEach { period ->
                        DropdownMenuItem(
                            onClick = {
                                selectedPeriod = period
                                isDropdownExpanded = false
                            },
                            text = { Text(period.label) }
                        )
                    }
                }
            }

            Text("Filter Categories", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = selectedCategories[cat.id] ?: true
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategories[cat.id] = !isSelected
                        },
                        label = { Text(cat.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Spending Chart", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            val periodStart = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -selectedPeriod.daysBack)
            }.timeInMillis

            val filteredTx = transactions.filter {
                it.type == "expense" &&
                        it.date >= periodStart &&
                        selectedCategories[it.categoryId] == true
            }

            val spendingByCategory = filteredTx.groupBy { it.categoryId }
                .mapValues { (_, txs) -> txs.sumOf { it.amount }.toFloat() }

            val categoryIds = spendingByCategory.keys.toList()
            val categoryLabels = categoryIds.map { id ->
                categories.find { it.id == id }?.name ?: "Unknown"
            }

            val actuals = categoryIds.map { spendingByCategory[it] ?: 0f }
            val minGoals = List(actuals.size) { 100f }
            val maxGoals = List(actuals.size) { 1000f }

            SpendingBarChart(
                categoryLabels = categoryLabels,
                spendingValues = actuals,
                minGoals = minGoals,
                maxGoals = maxGoals,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Recent Transactions", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(transactions.take(5)) { transaction ->
                    TransactionCard(transaction)
                }
            }
        }
    }
}


@Composable
fun StatCard(title: String, amount: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text(amount, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun TransactionCard(transaction: Transaction) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(transaction.description, style = MaterialTheme.typography.titleSmall)
            Text("R%.2f".format(transaction.amount), style = MaterialTheme.typography.bodyLarge)
            Text(dateFormat.format(transaction.date), style = MaterialTheme.typography.bodySmall)
        }
    }
}
