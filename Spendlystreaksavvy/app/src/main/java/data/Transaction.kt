package com.example.spendly.data

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Transaction(
    val id: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val date: Long = 0L,
    val type: String = "",
    val categoryId: String = ""
)



