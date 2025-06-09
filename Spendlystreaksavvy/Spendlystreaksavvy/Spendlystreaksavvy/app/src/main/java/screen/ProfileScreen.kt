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
fun ProfileScreen(navController: NavController, viewModel: AppViewModel = viewModel()) {
    val user by viewModel.currentUser.collectAsState()
    val stats by viewModel.userStats.collectAsState()
    val isDark = viewModel.isDarkMode

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profile") }) },
        bottomBar = { BottomNavBar(navController = navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome, ${user?.username ?: "Guest"}", style = MaterialTheme.typography.h6)

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
                Text("Dark Mode")
                Switch(checked = isDark, onCheckedChange = { viewModel.toggleDarkMode() })
            }

            Button(
                onClick = {
                    viewModel.logout()
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
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
