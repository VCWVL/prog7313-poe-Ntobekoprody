package com.example.spendly.data

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

fun fetchBudgetsFromFirebase(onResult: (List<Budget>) -> Unit) {
    val db = Firebase.firestore

    db.collection("budgets")
        .get()
        .addOnSuccessListener { result ->
            val budgets = result.mapNotNull { doc ->
                try {
                    Budget(
                        id = doc.id,
                        categoryId = doc.getString("categoryId") ?: "",
                        minimum = doc.getDouble("minimum") ?: 0.0,
                        maximum = doc.getDouble("maximum") ?: 0.0,
                        title = doc.getString("title") ?: "",
                        spent = 0.0, // This will be computed in the UI
                    )
                } catch (e: Exception) {
                    null
                }
            }
            onResult(budgets)
        }
        .addOnFailureListener {
            Log.e("FIREBASE_BUDGET", "Failed to fetch budgets", it)
            onResult(emptyList())
        }
}
