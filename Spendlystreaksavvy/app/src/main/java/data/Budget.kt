package com.example.spendly.data

import java.util.*

data class Budget(
    val id: String = UUID.randomUUID().toString(),
    val categoryId: String = "",
    val title: String = "",
    val minimum: Double = 0.0,
    val maximum: Double = 0.0,
    val spent: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val month: Int = Calendar.getInstance().get(Calendar.MONTH),
    val year: Int = Calendar.getInstance().get(Calendar.YEAR)
)
