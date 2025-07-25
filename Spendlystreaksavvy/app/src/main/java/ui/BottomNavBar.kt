package com.example.spendly.ui

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

data class BottomNavItem(val label: String, val route: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem("Dashboard", "dashboard", Icons.Default.Home),
    BottomNavItem("Transactions", "transactions", Icons.Default.List),
    BottomNavItem("Budgets", "budgets", Icons.Default.Lock),
    BottomNavItem("Goals", "goals", Icons.Default.Star),
    BottomNavItem("Profile", "profile", Icons.Default.Person)
)

@Composable
fun BottomNavBar(navController: NavController) {
    BottomNavigation {
        bottomNavItems.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = false, // Consider NavBackStackEntry for dynamic selection
                onClick = { navController.navigate(item.route) }
            )
        }
    }
}
