package com.example.spendly.ui

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
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
import java.util.*

@Composable
fun NewTransactionScreen(
    navController: NavController,
    viewModel: AppViewModel
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

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Add Transaction", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            RadioButton(selected = type == "income", onClick = { type = "income" })
            Text("Income", modifier = Modifier.padding(start = 4.dp, end = 16.dp))
            RadioButton(selected = type == "expense", onClick = { type = "expense" })
            Text("Expense", modifier = Modifier.padding(start = 4.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Category", style = MaterialTheme.typography.caption)

        DropdownMenuWithSymbolCategories(
            items = categories,
            selectedId = categoryId,
            onItemSelected = { categoryId = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Date: $dateFormatted")
        Button(onClick = {
            val dp = DatePickerDialog(
                context,
                { _, y, m, d ->
                    calendar.set(y, m, d)
                    date = calendar.timeInMillis
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dp.show()
        }) {
            Text("Select Date")
        }

        Spacer(modifier = Modifier.height(24.dp))

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

                viewModel.addTransaction(transaction) // ✅ This pushes to repository
                Toast.makeText(context, "Transaction added!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Transaction")
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
    val selected = items.find { it.id == selectedId }?.name ?: "Select Category"

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selected)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEachIndexed { index, category ->
                val symbol = when (index % 5) {
                    0 -> "💰"
                    1 -> "🍔"
                    2 -> "🏡"
                    3 -> "🎓"
                    else -> "🛒"
                }
                DropdownMenuItem(onClick = {
                    onItemSelected(category.id)
                    expanded = false
                }) {
                    Text("$symbol ${category.name}")
                }
            }
        }
    }
}
