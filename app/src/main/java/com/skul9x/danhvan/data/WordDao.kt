package com.skul9x.danhvan.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE isUserAdded = 1")
    fun getUserWords(): Flow<List<WordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity)

    @Update
    suspend fun updateWord(word: WordEntity)

    @Delete
    suspend fun deleteWord(word: WordEntity)

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Int): WordEntity?

    @Query("SELECT * FROM words WHERE isUserAdded = 1")
    suspend fun getUserWordsSync(): List<WordEntity>

    @Query("SELECT * FROM words WHERE text = :text LIMIT 1")
    suspend fun getWordByText(text: String): WordEntity?

    @Query("DELETE FROM words WHERE isUserAdded = 1")
    suspend fun deleteAllUserWords()
}
