package com.skul9x.danhvan.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStats(
    @PrimaryKey val date: Long, // Start of day timestamp (midnight)
    val starsEarned: Int = 0,
    val wordsLearned: Int = 0
)
