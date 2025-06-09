package com.example.spendly.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spendly.data.Budget

@Composable
fun BudgetItem(budget: Budget) {
    val spent = budget.spent
    val remaining = budget.maximum - spent
    val progress = (spent / budget.maximum).toFloat().coerceIn(0f, 1f)

    Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(budget.title, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = progress)
            Spacer(modifier = Modifier.height(4.dp))
            Text("R${"%.2f".format(spent)} spent out of R${"%.2f".format(budget.maximum)}")
            Text("Remaining: R${"%.2f".format(remaining)}")
        }
    }
}
