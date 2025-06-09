package com.example.spendly.repository

import com.example.spendly.data.*
import java.util.*
import kotlin.random.Random

object AppRepository {

    private val users = mutableListOf("demo" to "password")
    private var currentUser: User? = null

    private val categories = mutableListOf(
        Category("cat-1", "Food", "🍔", "#34A853"),
        Category("cat-2", "Transport", "🚗", "#4285F4"),
        Category("cat-3", "Shopping", "🛍️", "#EA4335"),
        Category("cat-4", "Bills", "📄", "#FBBC05"),
        Category("cat-5", "Entertainment", "🎬", "#9b87f5")
    )

    private val transactions = mutableListOf(
        Transaction(
            id = "trans-1",
            amount = 2500.0,
            description = "Salary",
            date = date(2025, 4, 1),
            type = "income",
            categoryId = "cat-4"
        ),
        Transaction(
            id = "trans-2",
            amount = 3330.2,
            description = "Freelance work",
            date = date(2025, 4, 15),
            type = "income",
            categoryId = "cat-4"
        ),
        Transaction(
            id = "trans-3",
            amount = 120.5,
            description = "Grocery shopping",
            date = date(2025, 4, 5),
            type = "expense",
            categoryId = "cat-1"
        ),
        Transaction(
            id = "trans-4",
            amount = 45.8,
            description = "Uber ride",
            date = date(2025, 4, 8),
            type = "expense",
            categoryId = "cat-2"
        ),
        Transaction(
            id = "trans-5",
            amount = 899.9,
            description = "New laptop",
            date = date(2025, 4, 12),
            type = "expense",
            categoryId = "cat-3"
        )
    )


    private val budgets = mutableListOf(
        Budget("budget-1", "cat-1", "Dinner", 200.0, 500.0, 300.0, createDate(4, 2025)),
        Budget("budget-2", "cat-2", "Takeout", 50.0, 200.0, 100.0, createDate(4, 2025)),
        Budget("budget-3", "cat-3", "Gifts", 0.0, 1000.0, 500.0, createDate(4, 2025)),
        Budget("budget-4", "cat-4", "School fees", 1000.0, 1500.0, 10000.0, createDate(4, 2025)),
        Budget("budget-5", "cat-5", "Holiday", 0.0, 300.0, 5000.0, createDate(4, 2025))
    )

    fun createDate(month: Int, year: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1) // 0-based (Jan = 0)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.timeInMillis
    }


    private fun date(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month, day, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun login(username: String, password: String): User? {
        val match = users.find { it.first == username && it.second == password } ?: return null
        val now = System.currentTimeMillis()
        val user = User(UUID.randomUUID().toString(), username, now, now, 0, 0)
        updateStreakAndPoints(user)
        currentUser = user
        return user
    }

    fun register(username: String, password: String): User? {
        if (users.any { it.first == username }) return null
        users.add(username to password)
        val now = System.currentTimeMillis()
        val user = User(UUID.randomUUID().toString(), username, now, now, 0, 0)
        updateStreakAndPoints(user)
        currentUser = user
        return user
    }

    fun logout() {
        currentUser = null
    }

    fun getCurrentUser(): User? = currentUser

    private fun updateStreakAndPoints(user: User) {
        val now = System.currentTimeMillis()
        val last = user.lastLogin ?: 0
        val calendarToday = Calendar.getInstance()
        val calendarLast = Calendar.getInstance().apply { timeInMillis = last }
        val calendarYest = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        when {
            last == 0L -> {
                user.streakCount = 1
                user.points = 10
            }
            calendarLast.get(Calendar.DAY_OF_YEAR) == calendarYest.get(Calendar.DAY_OF_YEAR) -> {
                user.streakCount += 1
                user.points += 10 + (user.streakCount / 5) * 5
            }
            calendarLast.get(Calendar.DAY_OF_YEAR) != calendarToday.get(Calendar.DAY_OF_YEAR) -> {
                user.streakCount = 1
                user.points += 10
            }
            else -> {
                user.points += 2
            }
        }

        user.lastLogin = now
    }

    fun getTransactions(): List<Transaction> = transactions.toList()

    fun addTransaction(new: Transaction) {
        transactions.add(0, new)
    }

    fun getTransactionsByCategory(categoryId: String): List<Transaction> {
        return transactions.filter { it.categoryId == categoryId }
    }

    fun getCategories(): List<Category> = categories.toList()

    fun addCategory(new: Category) {
        categories.add(new)
    }

    fun getBudgets(): List<Budget> = budgets.toList()

    fun addBudget(new: Budget) {
        budgets.add(new)
    }

    fun getMonthlyExpenses(): Double {
        val cal = Calendar.getInstance()
        return transactions.filter {
            it.type == "expense" && isSameMonth(it.date, cal)
        }.sumOf { it.amount }
    }

    fun getMonthlyIncome(): Double {
        val cal = Calendar.getInstance()
        return transactions.filter {
            it.type == "income" && isSameMonth(it.date, cal)
        }.sumOf { it.amount }
    }

    fun getCurrentBalance(): Double {
        return transactions.sumOf {
            if (it.type == "income") it.amount else -it.amount
        }
    }

    fun getBudgetForCategory(categoryId: String): Budget? {
        val now = Calendar.getInstance()
        return budgets.find {
            it.categoryId == categoryId &&
                    it.month == now.get(Calendar.MONTH) &&
                    it.year == now.get(Calendar.YEAR)
        }
    }

    private fun isSameMonth(timestamp: Long, now: Calendar): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return cal.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
    }
}
