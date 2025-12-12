package com.skul9x.danhvan.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object TranslationHelper {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Hardcoded dictionary for common words to ensure reliability
    private val dictionary = mapOf(
        "con bò" to "cow",
        "quả dâu" to "strawberry",
        "con mèo" to "cat",
        "con chó" to "dog",
        "quả táo" to "apple",
        "quả cam" to "orange",
        "cái ấm" to "kettle",
        "chị gái" to "sister",
        "em bé" to "baby",
        "bà" to "grandma",
        "ông" to "grandpa"
    )

    suspend fun translateToEnglish(vietnameseText: String): String = withContext(Dispatchers.IO) {
        // 1. Check dictionary first
        val lowerText = vietnameseText.lowercase().trim()
        dictionary[lowerText]?.let { return@withContext it }

        try {
            // 2. Use API if not in dictionary
            // Very strict prompt to avoid chatter
            val prompt = "Translate this Vietnamese word to English. Output ONLY the one English word. Do not explain. Do not list examples. Word: $vietnameseText"
            val encodedPrompt = URLEncoder.encode(prompt, "UTF-8")
            // Use model=openai for better reasoning if available, or default
            val url = "https://text.pollinations.ai/$encodedPrompt?model=openai"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "DanhVanApp/1.0")
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "ERROR_HTTP_${response.code}: $vietnameseText" 
                }
                val result = response.body?.string()?.trim() ?: ""
                if (result.isEmpty()) return@withContext "ERROR_EMPTY: $vietnameseText"
                
                // Aggressive cleanup:
                // 1. Take only the first line
                var cleanResult = result.lines().firstOrNull()?.trim() ?: result
                
                // 2. Remove "English:" prefix
                if (cleanResult.lowercase().startsWith("english:")) {
                    cleanResult = cleanResult.substring(8).trim()
                }
                
                // 3. Remove explanations in parentheses e.g. "Forest (noun)" -> "Forest"
                cleanResult = cleanResult.substringBefore("(").trim()
                
                // 4. Remove multiple meanings e.g. "Forest, Woods" -> "Forest"
                cleanResult = cleanResult.substringBefore(",").trim()
                
                // 5. Remove "Notes:" or similar if they somehow appeared on the first line
                cleanResult = cleanResult.substringBefore("Notes:").trim()
                
                // 6. Remove quotes
                cleanResult = cleanResult.replace("\"", "").replace(".", "")
                
                return@withContext cleanResult
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "ERROR_EXC_${e.message}: $vietnameseText"
        }
    }
}
