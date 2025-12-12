package com.skul9x.danhvan.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val syllables: List<String>,
    val imageUri: String? = null,
    val audioUri: String? = null,
    val isUserAdded: Boolean = false,
    val category: String? = null
)
