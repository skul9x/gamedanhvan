package com.skul9x.danhvan.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "topic_images")
data class TopicImageEntity(
    @PrimaryKey val category: String,
    val imagePath: String
)


