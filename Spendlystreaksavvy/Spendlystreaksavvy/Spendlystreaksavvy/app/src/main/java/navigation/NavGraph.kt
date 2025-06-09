package com.example.spendly.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.spendly.screen.NewBudgetGoalScreen
import com.example.spendly.ui.*
import com.example.spendly.viewmodel.AppViewModel

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val TRANSACTIONS = "transactions"
    const val NEW_TRANSACTION = "new-transaction"
    const val BUDGETS = "budgets"
    const val NEW_BUDGET_GOAL = "new-budget-goal"
    const val GOALS = "goals"
    const val PROFILE = "profile"
    const val NOT_FOUND = "not_found"
}

@Composable
fun AppNavGraph(navController: NavHostController, viewModel: AppViewModel) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) { LoginScreen(navController, viewModel) }
        composable(Routes.REGISTER) { RegisterScreen(navController, viewModel) }
        composable(Routes.DASHBOARD) { DashboardScreen(navController, viewModel) }
        composable(Routes.TRANSACTIONS) { TransactionsScreen(navController, viewModel) }
        composable(Routes.NEW_TRANSACTION) { NewTransactionScreen(navController, viewModel) }
        composable(Routes.BUDGETS) { BudgetScreen(navController, viewModel) }
        composable(Routes.NEW_BUDGET_GOAL) { NewBudgetGoalScreen(navController, viewModel) }
        composable(Routes.GOALS) { GoalsScreen(navController, viewModel) }
        composable(Routes.PROFILE) { ProfileScreen(navController, viewModel) }
        composable(Routes.NOT_FOUND) { NotFoundScreen(navController, viewModel) }
    }
}
