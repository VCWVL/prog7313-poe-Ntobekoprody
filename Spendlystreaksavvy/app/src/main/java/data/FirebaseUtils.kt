package com.example.spendly.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

fun uploadUserStatsToFirebase(userId: String, stats: UserStats) {
    val db = Firebase.firestore
    val data = mapOf(
        "level" to stats.level,
        "xp" to stats.xp,
        "xpToNextLevel" to stats.xpToNextLevel,
        "dailyStreak" to stats.dailyStreak,
        "longestStreak" to stats.longestStreak,
        "goalsCompleted" to stats.goalsCompleted
    )

    db.collection("userStats").document(userId)
        .set(data)
        .addOnSuccessListener { Log.d("FIREBASE_STATS", "Stats uploaded for $userId") }
        .addOnFailureListener { Log.e("FIREBASE_STATS", "Failed to upload stats", it) }
}

fun uploadBudgetToFirebase(budget: Budget) {
    val db = Firebase.firestore
    val data = mapOf(
        "id" to budget.id,
        "categoryId" to budget.categoryId,
        "minimum" to budget.minimum,
        "maximum" to budget.maximum,
        "title" to budget.title,
        "spent" to budget.spent
    )

    db.collection("budgets").document(budget.id)
        .set(data)
        .addOnSuccessListener {
            Log.d("FIREBASE_BUDGET", "Budget uploaded: ${budget.title}")
        }
        .addOnFailureListener {
            Log.e("FIREBASE_BUDGET", "Failed to upload budget", it)
        }
}

fun fetchUserStatsFromFirebase(userId: String, onResult: (UserStats?) -> Unit) {
    val db = Firebase.firestore

    db.collection("userStats").document(userId)
        .get()
        .addOnSuccessListener { doc ->
            if (doc.exists()) {
                val stats = UserStats(
                    level = doc.getLong("level")?.toInt() ?: 1,
                    xp = doc.getLong("xp")?.toInt() ?: 0,
                    xpToNextLevel = doc.getLong("xpToNextLevel")?.toInt() ?: 100,
                    dailyStreak = doc.getLong("dailyStreak")?.toInt() ?: 0,
                    longestStreak = doc.getLong("longestStreak")?.toInt() ?: 0,
                    goalsCompleted = doc.getLong("goalsCompleted")?.toInt() ?: 0
                )
                onResult(stats)
            } else {
                onResult(null)
            }
        }
        .addOnFailureListener {
            Log.e("FIREBASE_STATS", "Failed to fetch stats", it)
            onResult(null)
        }
}



