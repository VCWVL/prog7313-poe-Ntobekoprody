package com.example.spendly.screen

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
import com.example.spendly.data.Budget
import com.example.spendly.data.Category
import com.example.spendly.ui.BottomNavBar
import com.example.spendly.viewmodel.AppViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBudgetGoalScreen(navController: NavController, viewModel: AppViewModel = viewModel()) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()

    var categoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }
    var minInput by remember { mutableStateOf("") }
    var maxInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Budget Goal") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Set New Budget Goal", style = MaterialTheme.typography.headlineSmall)

            Text("Category", style = MaterialTheme.typography.labelSmall)
            DropdownMenuWithEmojiCategories(
                items = categories,
                selectedId = categoryId,
                onItemSelected = { categoryId = it }
            )

            OutlinedTextField(
                value = minInput,
                onValueChange = { minInput = it },
                label = { Text("Minimum Amount (R)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors()
            )

            OutlinedTextField(
                value = maxInput,
                onValueChange = { maxInput = it },
                label = { Text("Maximum Amount (R)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val min = minInput.toDoubleOrNull()
                    val max = maxInput.toDoubleOrNull()

                    if (categoryId.isBlank() || min == null || max == null) {
                        Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (min > max) {
                        Toast.makeText(context, "Minimum cannot be greater than maximum", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.addBudget(
                        Budget(
                            categoryId = categoryId,
                            minimum = min,
                            maximum = max
                        )
                    )

                    Toast.makeText(context, "Budget goal added!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Budget Goal")
            }
        }
    }
}

@Composable
fun DropdownMenuWithEmojiCategories(
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
