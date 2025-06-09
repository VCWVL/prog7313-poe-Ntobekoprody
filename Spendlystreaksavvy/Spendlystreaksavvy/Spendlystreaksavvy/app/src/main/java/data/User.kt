package com.example.spendly.data

data class User(
    val id: String,
    val username: String,
    val createdAt: Long,
    var lastLogin: Long,
    var points: Int = 0,
    var streakCount: Int = 0
)
