package com.skul9x.danhvan.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStatsDao {
    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT 7")
    fun getLast7DaysStats(): Flow<List<DailyStats>>

    @Query("SELECT * FROM daily_stats WHERE date = :date LIMIT 1")
    suspend fun getStatsForDate(date: Long): DailyStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: DailyStats)
    
    @Query("SELECT COUNT(*) FROM daily_stats WHERE starsEarned > 0")
    fun getDaysWithActivity(): Flow<Int>
}
