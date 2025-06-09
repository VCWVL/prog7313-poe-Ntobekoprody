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

    Log.d("FIREBASE_TX", "🔥 Starting upload for user $userId with transaction ${transaction.id}")

    db.collection("users")
        .document(userId)
        .collection("transactions")
        .document(transaction.id)
        .set(transaction)
        .addOnSuccessListener {
            Log.d("FIREBASE_TX", "✅ Uploaded transaction for user $userId")
        }
        .addOnFailureListener {
            Log.e("FIREBASE_TX", "❌ Failed to upload transaction", it)
        }
}




