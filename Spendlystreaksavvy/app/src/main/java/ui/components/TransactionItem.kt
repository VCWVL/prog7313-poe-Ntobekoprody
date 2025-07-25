package com.example.spendly.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.spendly.data.Transaction
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionItem(transaction: Transaction, categoryLabel: String) {
    val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        .format(Date(transaction.date))

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = categoryLabel, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = transaction.description, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = formattedDate, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = (if (transaction.type == "income") "+ " else "- ") + "R${"%.2f".format(transaction.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (transaction.type == "income") Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}
