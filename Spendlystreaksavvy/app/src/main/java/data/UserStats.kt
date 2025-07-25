package com.example.spendly.data

data class UserStats(
    val level: Int,
    val xp: Int,
    val xpToNextLevel: Int,
    val dailyStreak: Int,
    val longestStreak: Int,
    val goalsCompleted: Int
)
