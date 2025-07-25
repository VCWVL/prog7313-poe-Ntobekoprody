package com.example.spendly.viewmodel

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import com.example.spendly.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.max
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.app.Application
import androidx.lifecycle.AndroidViewModel



class AppViewModel(private val appContext: android.app.Application) : AndroidViewModel(appContext) {


    private val _xpEvents = MutableSharedFlow<Int>()
    val xpEvents = _xpEvents.asSharedFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()


    private val scope = CoroutineScope(Dispatchers.Default)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _userStats = MutableStateFlow(
        UserStats(level = 1, xp = 0, xpToNextLevel = 100, dailyStreak = 0, longestStreak = 0, goalsCompleted = 0)
    )
    val userStats: StateFlow<UserStats> = _userStats

    private val _isDarkMode = mutableStateOf(ThemePreferenceManager.isDarkMode(appContext))
    val isDarkMode: State<Boolean> get() = _isDarkMode

    fun toggleDarkMode() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        ThemePreferenceManager.setDarkMode(appContext, newValue)
    }

    val balance: StateFlow<Double> = transactions.map { txs ->
        txs.sumOf { if (it.type == "income") it.amount else -it.amount }
    }.stateIn(scope, SharingStarted.Eagerly, 0.0)

    val income: StateFlow<Double> = transactions.map { txs ->
        txs.filter { it.type == "income" }.sumOf { it.amount }
    }.stateIn(scope, SharingStarted.Eagerly, 0.0)

    val expenses: StateFlow<Double> = transactions.map { txs ->
        txs.filter { it.type == "expense" }.sumOf { it.amount }
    }.stateIn(scope, SharingStarted.Eagerly, 0.0)

    fun register(email: String, password: String, onResult: (Boolean) -> Unit) {
        FirebaseAuthUtil.register(email, password) { success, firebaseUser ->
            if (success && firebaseUser != null) {
                _currentUser.value = createUser(firebaseUser, email)
                ensureDefaultCategories(_currentUser.value!!.id)
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

                loadCategoriesFromFirebase()
                ensureDefaultCategories(_currentUser.value!!.id)

                _transactions.value = emptyList()
                _budgets.value = emptyList()

                ensureUserDocumentExists(user.id)

                fetchUserStatsFromFirebase(firebaseUser.uid) { stats ->
                    stats?.let {
                        _userStats.value = it
                        updateStatsOnLogin() // <- only update after loading previous stats
                    }
                }

                loadTransactionsFromFirebase()
                loadBudgetsFromFirebase()

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
        _currentUser.value?.let { user ->
            uploadUserStatsToFirebase(user.id, _userStats.value)
        }

        FirebaseAuthUtil.logout()
        _currentUser.value = null

        _transactions.value = emptyList()
        _budgets.value = emptyList()
        _userStats.value = UserStats(
            level = 1,
            xp = 0,
            xpToNextLevel = 100,
            dailyStreak = 0,
            longestStreak = 0,
            goalsCompleted = 0
        )
    }

    fun addCategory(category: Category) {
        val userId = _currentUser.value?.id ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("categories")
            .document(category.id)
            .set(category)
    }

    fun testManualTransactionUpload() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val dummyTx = hashMapOf(
            "id" to "test123",
            "amount" to 999,
            "type" to "expense",
            "categoryId" to "testCategory",
            "description" to "Test Upload",
            "date" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("transactions")
            .document("test123")
            .set(dummyTx)
            .addOnSuccessListener {
                Log.d("TEST_FIREBASE", "✅ Manual upload worked")
            }
            .addOnFailureListener {
                Log.e("TEST_FIREBASE", "❌ Manual upload failed", it)
            }
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
    fun deleteTransaction(transaction: Transaction) {
        val userId = auth.currentUser?.uid ?: return
        val transactionId = transaction.id

        viewModelScope.launch {
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("transactions")
                    .document(transactionId)
                    .delete()
                    .addOnSuccessListener {
                        // Remove it from local state
                        _transactions.value = _transactions.value.filterNot { it.id == transactionId }
                    }
            } catch (e: Exception) {
                // Log or handle error
            }
        }
    }


    fun loadTransactionsFromFirebase() {
        val userId = _currentUser.value?.id ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("transactions")
            .get()
            .addOnSuccessListener { result ->
                val txList = result.mapNotNull { it.toObject(Transaction::class.java) }
                _transactions.value = txList
            }
            .addOnFailureListener {
                Log.e("FETCH_TX", "Failed to fetch transactions", it)
            }
    }

    fun addBudget(budget: Budget) {
        _budgets.value = _budgets.value + budget
        gainXP(15)
        _userStats.value = _userStats.value.copy(goalsCompleted = _userStats.value.goalsCompleted + 1)
        uploadBudgetToFirebase(budget)
    }
    fun deleteBudget(budget: Budget) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("budgets")
                    .document(budget.id)
                    .delete()
                    .addOnSuccessListener {
                        _budgets.value = _budgets.value.filterNot { it.id == budget.id }
                    }
            } catch (e: Exception) {
                // Optional: log or show error
            }
        }
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
        val stats = _userStats.value
        val lastLoginTime = _currentUser.value?.lastLogin ?: 0L
        val lastLoginDate = Calendar.getInstance().apply { timeInMillis = lastLoginTime }
        val today = Calendar.getInstance()

        // Calculate the difference in days
        val daysBetween = (today.timeInMillis - lastLoginDate.timeInMillis) / (1000 * 60 * 60 * 24)

        val newStreak = when (daysBetween) {
            0L -> stats.dailyStreak // already logged in today
            1L -> stats.dailyStreak + 1 // continued streak
            else -> 1 // reset streak
        }

        val newStats = stats.copy(
            dailyStreak = newStreak,
            longestStreak = max(stats.longestStreak, newStreak)
        )

        _userStats.value = newStats
        gainXP(5)

        _currentUser.value?.let { user ->
            // update the user's last login
            user.lastLogin = System.currentTimeMillis()
            uploadUserStatsToFirebase(user.id, newStats)
        }
    }


    fun gainXP(amount: Int) {
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

        // Emit XP gain
        viewModelScope.launch {
            _xpEvents.emit(amount)
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
    private fun uploadBudgetToFirebase(budget: Budget) {
        _currentUser.value?.let { user ->
            val db = FirebaseFirestore.getInstance()
            db.collection("users")
                .document(user.id)
                .collection("budgets")
                .document(budget.id)
                .set(budget)
        }
    }
    private fun fetchBudgetsFromFirebase(onFetched: (List<Budget>) -> Unit) {
        _currentUser.value?.let { user ->
            val db = FirebaseFirestore.getInstance()
            db.collection("users")
                .document(user.id)
                .collection("budgets")
                .get()
                .addOnSuccessListener { result ->
                    val budgets = result.mapNotNull { it.toObject(Budget::class.java) }
                    onFetched(budgets)
                }
                .addOnFailureListener {
                    Log.e("FIREBASE_BUDGETS", "Error fetching budgets", it)
                    onFetched(emptyList())
                }
        }
    }

    fun loadCategoriesFromFirebase() {
        val userId = _currentUser.value?.id ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("LOAD_CATEGORIES", "Error fetching categories", error)
                    return@addSnapshotListener
                }

                val loaded = snapshot?.documents?.mapNotNull { it.toObject(Category::class.java) } ?: emptyList()
                _categories.value = loaded
            }
    }

    fun ensureDefaultCategories(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val categoriesRef = db.collection("users").document(userId).collection("categories")

        categoriesRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                val defaults = listOf(
                    Category(UUID.randomUUID().toString(), "Groceries", "🛒", "#FF9800"),
                    Category(UUID.randomUUID().toString(), "Transport", "🚌", "#03A9F4"),
                    Category(UUID.randomUUID().toString(), "Entertainment", "🎮", "#E91E63"),
                    Category(UUID.randomUUID().toString(), "Bills", "💡", "#9C27B0"),
                    Category(UUID.randomUUID().toString(), "Savings", "🏦", "#4CAF50")
                )

                for (category in defaults) {
                    categoriesRef.document(category.id).set(category)
                }
            }
        }
    }



}
