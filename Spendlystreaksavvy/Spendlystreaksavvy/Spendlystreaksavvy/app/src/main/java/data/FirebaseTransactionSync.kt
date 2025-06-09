package com.example.spendly.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

// Upload a single transaction to Firebase
fun uploadTransactionToFirebase(
    amount: Double,
    description: String,
    type: String,
    categoryId: String = ""
) {
    val db = Firebase.firestore
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val now = sdf.format(Date())

    val data = hashMapOf(
        "amount" to amount,
        "description" to description,
        "type" to type,
        "date" to now,
        "categoryId" to categoryId
    )

    db.collection("transactions")
        .add(data)
        .addOnSuccessListener {
            Log.d("FIREBASE_TX", "Transaction uploaded with ID: ${it.id}")
        }
        .addOnFailureListener {
            Log.e("FIREBASE_TX", "Failed to upload transaction", it)
        }
}

// Suspending version for coroutines (optional)
suspend fun fetchTransactionsFromFirebaseSuspend(): List<Transaction> {
    val db = Firebase.firestore
    val transactions = mutableListOf<Transaction>()

    return try {
        val snapshot = db.collection("transactions").get().await()
        for (doc in snapshot.documents) {
            val transaction = doc.toObject(Transaction::class.java)
            if (transaction != null) {
                transactions.add(transaction)
            }
        }
        transactions
    } catch (e: Exception) {
        Log.e("FIREBASE_FETCH", "Error fetching transactions", e)
        emptyList()
    }
}

// Callback-based version (used by ViewModel right now)
fun fetchTransactionsFromFirebase(onResult: (List<Transaction>) -> Unit) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    if (uid == null) {
        onResult(emptyList())
        return
    }

    FirebaseFirestore.getInstance()
        .collection("users")
        .document(uid)
        .collection("transactions")
        .get()
        .addOnSuccessListener { result ->
            val transactions = result.mapNotNull { it.toObject(Transaction::class.java) }
            onResult(transactions)
        }
        .addOnFailureListener { exception ->
            Log.e("FIREBASE_TX", "Error fetching transactions", exception)
            onResult(emptyList())
        }
}

