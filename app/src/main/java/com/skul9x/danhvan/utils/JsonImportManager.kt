package com.skul9x.danhvan.utils

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.skul9x.danhvan.data.WordEntity
import java.io.BufferedReader
import java.io.InputStreamReader

data class JsonWord(
    val text: String,
    val imageUri: String? = null,
    val audioUri: String? = null,
    val category: String? = null
)

object JsonImportManager {
    private val gson = Gson()

    fun parseJson(context: Context, uri: Uri): List<JsonWord> {
        val inputStream = context.contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val listType = object : TypeToken<List<JsonWord>>() {}.type
        return gson.fromJson(reader, listType)
    }

    fun getSampleJson(): String {
        val sample = listOf(
            JsonWord(text = "Con Mèo", category = "Động vật"),
            JsonWord(text = "Quả Táo", category = "Hoa quả")
        )
        return gson.toJson(sample)
    }


}
