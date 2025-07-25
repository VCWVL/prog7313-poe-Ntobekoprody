package com.example.spendly.screen

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spendly.data.Category
import com.example.spendly.data.Transaction
import com.example.spendly.viewmodel.AppViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.spendly.data.uploadTransactionToFirestore
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTransactionScreen(
    navController: NavController,
    viewModel: AppViewModel = viewModel()
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState(initial = emptyList())

    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("expense") }
    var categoryId by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(Calendar.getInstance().timeInMillis) }

    val calendar = Calendar.getInstance().apply { timeInMillis = date }
    val dateFormatted = remember(date) {
        android.text.format.DateFormat.format("dd MMM yyyy", Date(date)).toString()
    }

    LaunchedEffect(categories) {
        if (categoryId.isEmpty() && categories.isNotEmpty()) {
            categoryId = categories.first().id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors()
            )

            Row {
                RadioButton(selected = type == "income", onClick = { type = "income" })
                Text("Income", modifier = Modifier.padding(end = 16.dp))
                RadioButton(selected = type == "expense", onClick = { type = "expense" })
                Text("Expense")
            }

            Text("Category", style = MaterialTheme.typography.labelSmall)
            DropdownMenuWithSymbolCategories(
                items = categories,
                selectedId = categoryId,
                onItemSelected = { categoryId = it }
            )

            Text("Date: $dateFormatted")
            Button(onClick = {
                DatePickerDialog(
                    context,
                    { _, y, m, d ->
                        calendar.set(y, m, d)
                        date = calendar.timeInMillis
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }) {
                Text("Select Date")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue == null || description.isBlank() || categoryId.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val transaction = Transaction(
                        id = UUID.randomUUID().toString(),
                        amount = amountValue,
                        type = type,
                        date = date,
                        description = description,
                        categoryId = categoryId
                    )

                    viewModel.addTransaction(transaction)

                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "demo-user"
                    uploadTransactionToFirestore(userId, transaction)

                    Toast.makeText(context, "Transaction added!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Transaction")
            }
        }
    }
}

@Composable
fun DropdownMenuWithSymbolCategories(
    items: List<Category>,
    selectedId: String,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = items.find { it.id == selectedId }?.let { "${it.icon} ${it.name}" } ?: "Select Category"

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors()
        ) {
            Text(selected)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { category ->
                DropdownMenuItem(
                    onClick = {
                        onItemSelected(category.id)
                        expanded = false
                    },
                    text = { Text("${category.icon} ${category.name}") }
                )
            }
        }
    }
}
