package com.example.spendly.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spendly.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(navController: NavController, viewModel: AppViewModel = viewModel()) {
    val stats by viewModel.userStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress & Goals") },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Your Progress", style = MaterialTheme.typography.headlineSmall)

            ProgressItem(label = "Level", value = stats.level.toString())
            ProgressItem(label = "XP", value = "${stats.xp} / ${stats.xpToNextLevel}")
            ProgressItem(label = "Daily Streak", value = "${stats.dailyStreak} days")
            ProgressItem(label = "Longest Streak", value = "${stats.longestStreak} days")
            ProgressItem(label = "Goals Completed", value = stats.goalsCompleted.toString())

            LinearProgressIndicator(
                progress = (stats.xp.toFloat() / stats.xpToNextLevel).coerceIn(0f, 1f),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
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
        Text(label, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Text(value, color = MaterialTheme.colorScheme.onSurface)
    }
}
