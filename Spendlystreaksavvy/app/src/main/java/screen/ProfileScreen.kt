package com.example.spendly.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.spendly.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: AppViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val stats by viewModel.userStats.collectAsState()
    val isDark by viewModel.isDarkMode

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Welcome, ${user?.username ?: "Guest"}",
                style = MaterialTheme.typography.headlineSmall
            )

            Divider()

            ProfileStat(label = "Level", value = stats.level.toString())
            ProfileStat(label = "XP", value = "${stats.xp} / ${stats.xpToNextLevel}")
            ProfileStat(label = "Daily Streak", value = "${stats.dailyStreak} days")
            ProfileStat(label = "Goals Completed", value = stats.goalsCompleted.toString())

            Divider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dark Mode", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = isDark,
                    onCheckedChange = { viewModel.toggleDarkMode() }
                )
            }

            Button(
                onClick = {
                    viewModel.logout()
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text("Logout")
            }
        }
    }
}

@Composable
fun ProfileStat(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, fontWeight = FontWeight.Medium)
        Text(value)
    }
}
