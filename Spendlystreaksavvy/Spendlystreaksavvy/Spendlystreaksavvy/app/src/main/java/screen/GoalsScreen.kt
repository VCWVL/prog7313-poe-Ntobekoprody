package com.example.spendly.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spendly.viewmodel.AppViewModel

@Composable
fun GoalsScreen(navController: NavController, viewModel: AppViewModel = viewModel()) {
    val stats by viewModel.userStats.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Progress & Goals") }) },
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Your Progress", style = MaterialTheme.typography.h6)

            ProgressItem(label = "Level", value = stats.level.toString())
            ProgressItem(label = "XP", value = "${stats.xp} / ${stats.xpToNextLevel}")
            ProgressItem(label = "Daily Streak", value = "${stats.dailyStreak} days")
            ProgressItem(label = "Longest Streak", value = "${stats.longestStreak} days")
            ProgressItem(label = "Goals Completed", value = stats.goalsCompleted.toString())

            LinearProgressIndicator(
                progress = (stats.xp.toFloat() / stats.xpToNextLevel).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
        }
    }
}

@Composable
fun ProgressItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(value)
    }
}
