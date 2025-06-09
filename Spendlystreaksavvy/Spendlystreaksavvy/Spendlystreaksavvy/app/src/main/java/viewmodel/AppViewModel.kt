package com.example.spendly.viewmodel

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.spendly.data.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.max

class AppViewModel : ViewModel() {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets

    private val _categories = MutableStateFlow(
        listOf(
            Category("1", "Groceries", "🛒", "#FF9800"),
            Category("2", "Transport", "🚌", "#03A9F4"),
            Category("3", "Entertainment", "🎮", "#E91E63"),
            Category("4", "Bills", "💡", "#9C27B0"),
            Category("5", "Savings", "🏦", "#4CAF50")
        )
    )
    val categories: StateFlow<List<Category>> = _categories

    private val _userStats = MutableStateFlow(
        UserStats(level = 1, xp = 0, xpToNextLevel = 100, dailyStreak = 0, longestStreak = 0, goalsCompleted = 0)
    )
    val userStats: StateFlow<UserStats> = _userStats

    var isDarkMode by mutableStateOf(false)
        private set

    val balance: StateFlow<Double> = transactions.map { txs ->
        txs.sumOf { if (it.type == "income") it.amount else -it.amount }
    }.stateIn(scope, SharingStarted.Eagerly, 0.0)

    val income: StateFlow<Double> = transactions.map { txs ->
        txs.filter { it.type == "income" }.sumOf { it.amount }
    }.stateIn(scope, SharingStarted.Eagerly, 0.0)

    val expenses: StateFlow<Double> = transactions.map { txs ->
        txs.filter { it.type == "expense" }.sumOf { it.amount }
    }.stateIn(scope, SharingStarted.Eagerly, 0.0)

    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
    }

    fun register(email: String, password: String, onResult: (Boolean) -> Unit) {
        FirebaseAuthUtil.register(email, password) { success, firebaseUser ->
            if (success && firebaseUser != null) {
                _currentUser.value = createUser(firebaseUser, email)
                updateStatsOnLogin()
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        FirebaseAuthUtil.login(email, password) { success, firebaseUser ->
            if (success && firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    username = email,
                    createdAt = System.currentTimeMillis(),
                    lastLogin = System.currentTimeMillis(),
                    points = 0,
                    streakCount = 0
                )
                _currentUser.value = user
                ensureUserDocumentExists(user.id)

                fetchUserStatsFromFirebase(firebaseUser.uid) { stats ->
                    stats?.let { _userStats.value = it }
                }

                updateStatsOnLogin()

                loadTransactionsFromFirebase()

                onResult(true)
            } else {
                onResult(false)
            }
        }
    }


    private fun createUser(firebaseUser: FirebaseUser, email: String): User {
        return User(
            id = firebaseUser.uid,
            username = email,
            createdAt = System.currentTimeMillis(),
            lastLogin = System.currentTimeMillis(),
            points = 0,
            streakCount = 0
        )
    }

    fun logout() {
        FirebaseAuthUtil.logout()
        _currentUser.value = null
    }

    fun addTransaction(tx: Transaction) {
        Log.d("TRANSACTION", "Trying to add Transaction")

        _transactions.value = _transactions.value + tx
        gainXP(10)

        _currentUser.value?.let { user ->
            uploadTransactionToFirestore(user.id, tx)
            Log.d("TRANSACTION", "Transaction uploading")
        }
        Log.d("TRANSACTION", "Transaction done")

    }

    fun loadTransactionsFromFirebase() {
        fetchTransactionsFromFirebase { fetched ->
            _transactions.value = fetched
        }
    }

    fun addBudget(budget: Budget) {
        _budgets.value = _budgets.value + budget
        gainXP(15)
        _userStats.value = _userStats.value.copy(goalsCompleted = _userStats.value.goalsCompleted + 1)
        uploadBudgetToFirebase(budget)
    }

    fun loadBudgetsFromFirebase() {
        fetchBudgetsFromFirebase { fetched ->
            _budgets.value = fetched
        }
    }

    fun getTransactionsByCategory(categoryId: String): List<Transaction> {
        return _transactions.value.filter { it.categoryId == categoryId }
    }

    private fun updateStatsOnLogin() {
        val newStats = _userStats.value.copy(
            dailyStreak = _userStats.value.dailyStreak + 1,
            longestStreak = max(_userStats.value.longestStreak, _userStats.value.dailyStreak + 1)
        )
        _userStats.value = newStats
        gainXP(5)
    }

    private fun gainXP(amount: Int) {
        val stats = _userStats.value
        var newXP = stats.xp + amount
        var newLevel = stats.level
        var xpToNext = stats.xpToNextLevel

        while (newXP >= xpToNext) {
            newXP -= xpToNext
            newLevel++
            xpToNext += 50
        }

        val updated = stats.copy(xp = newXP, level = newLevel, xpToNextLevel = xpToNext)
        _userStats.value = updated

        _currentUser.value?.let { user ->
            uploadUserStatsToFirebase(user.id, updated)
        }
    }
    fun ensureUserDocumentExists(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("users").document(userId)

        userDocRef.get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    val data = hashMapOf("createdAt" to System.currentTimeMillis())
                    userDocRef.set(data)
                        .addOnSuccessListener {
                            Log.d("FIREBASE_USER", "User document created: $userId")
                        }
                        .addOnFailureListener {
                            Log.e("FIREBASE_USER", "Failed to create user doc", it)
                        }
                }
            }
            .addOnFailureListener {
                Log.e("FIREBASE_USER", "Error checking user doc", it)
            }
    }

}
