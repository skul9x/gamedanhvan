package com.skul9x.danhvan.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicImageDao {
    @Query("SELECT * FROM topic_images")
    fun getAllTopicImages(): Flow<List<TopicImageEntity>>

    @Query("SELECT * FROM topic_images WHERE category = :category LIMIT 1")
    suspend fun getTopicImage(category: String): TopicImageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopicImage(topicImage: TopicImageEntity)

    @Query("DELETE FROM topic_images")
    suspend fun deleteAll()
}
