package com.example.spendly.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.example.spendly.data.Transaction

fun uploadTransactionToFirestore(userId: String, transaction: Transaction) {
    val db = FirebaseFirestore.getInstance()

    if (userId.isBlank()) {
        Log.e("FIREBASE_TX", "❌ userId is blank")
        return
    }
    if (transaction.id.isBlank()) {
        Log.e("FIREBASE_TX", "❌ transaction.id is blank")
        return
    }

    val txMap = mapOf(
        "id" to transaction.id,
        "amount" to transaction.amount,
        "description" to transaction.description,
        "date" to transaction.date,
        "type" to transaction.type,
        "categoryId" to transaction.categoryId
    )

    Log.d("FIREBASE_TX", "🔥 Uploading tx to /users/$userId/transactions/${transaction.id}")

    db.collection("users")
        .document(userId)
        .collection("transactions")
        .document(transaction.id)
        .set(txMap)
        .addOnSuccessListener {
            Log.d("FIREBASE_TX", "✅ Transaction uploaded!")
        }
        .addOnFailureListener {
            Log.e("FIREBASE_TX", "❌ Upload failed", it)
        }
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FIREBASE_TX", "✔️ Upload complete and successful")
            } else {
                Log.e("FIREBASE_TX", "❗ Upload complete but failed: ${task.exception?.message}")
            }
        }
}