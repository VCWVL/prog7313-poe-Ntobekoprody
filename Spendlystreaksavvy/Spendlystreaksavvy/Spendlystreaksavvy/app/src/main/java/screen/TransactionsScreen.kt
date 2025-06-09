package com.example.spendly.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.spendly.viewmodel.AppViewModel
import com.example.spendly.navigation.Routes
import com.example.spendly.ui.components.TransactionItem

@Composable
fun TransactionsScreen(
    navController: NavController,
    viewModel: AppViewModel
) {
    val transactions by viewModel.transactions.collectAsState()

    // ✅ OPTIONAL: Ensure they’re loaded (if you haven't yet somewhere else)
    LaunchedEffect(Unit) {
        viewModel.loadTransactionsFromFirebase()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("All Transactions") })
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Routes.NEW_TRANSACTION)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions yet", style = MaterialTheme.typography.body1)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(transactions) { tx ->
                        TransactionItem(tx)
                    }
                }
            }
        }
    }
}
