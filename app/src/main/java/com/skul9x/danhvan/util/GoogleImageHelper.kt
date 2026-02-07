package com.skul9x.danhvan.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.coroutines.cancellation.CancellationException

object GoogleImageHelper {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Headers from the Python script
    private val HEADERS = mapOf(
        "accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
        "accept-language" to "en-US,en;q=0.9",
        "cache-control" to "max-age=0",
        "sec-ch-ua" to "\"Chromium\";v=\"143\", \"Not A(Brand\";v=\"24\", \"Google Chrome\";v=\"143\"",
        "sec-ch-ua-arch" to "\"x86\"",
        "sec-ch-ua-bitness" to "\"64\"",
        "sec-ch-ua-full-version" to "\"143.0.7499.41\"",
        "sec-ch-ua-mobile" to "?0",
        "sec-ch-ua-platform" to "\"Windows\"",
        "sec-fetch-dest" to "document",
        "sec-fetch-mode" to "navigate",
        "sec-fetch-site" to "none",
        "sec-fetch-user" to "?1",
        "upgrade-insecure-requests" to "1",
        "user-agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36"
    )

    // Patterns from the Python script
    private val PATTERN_STRINGS = listOf(
        "\"ou\":\"(http[^\"]+?)\"",
        "data-src=\"(http[^\"]+?)\"",
        "src=\"(http[^\"]+?)\"",
        "AF_initDataCallback\\((.*?)\\);",
        "\\[\"(http[^\"]+?)\",\\d+,\\d+\\]",
        "(https?://[^\"]+\\.(?:jpg|jpeg|png|webp))"
    )

    private val COMPILED_PATTERNS = PATTERN_STRINGS.map { Pattern.compile(it, Pattern.CASE_INSENSITIVE or Pattern.DOTALL) }

    data class SearchResult(
        val urls: List<String>,
        val log: String
    )

    suspend fun searchImage(query: String): SearchResult = withContext(Dispatchers.IO) {
        val logBuilder = StringBuilder()
        try {
            logBuilder.append("--> GOOGLE SEARCH (Ported Python Logic): '$query'\n")
            
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val searchUrl = "https://www.google.com/search?q=$encodedQuery&tbm=isch&ie=UTF-8&oe=UTF-8"
            logBuilder.append("    URL: $searchUrl\n")

            val requestBuilder = Request.Builder().url(searchUrl)
            HEADERS.forEach { (k, v) -> requestBuilder.header(k, v) }
            val request = requestBuilder.build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    logBuilder.append("<-- ERROR: HTTP ${response.code}\n")
                    return@withContext SearchResult(emptyList(), logBuilder.toString())
                }

                val html = response.body?.string() ?: ""
                logBuilder.append("    HTML Size: ${html.length} chars\n")

                val candidates = LinkedHashSet<String>() // Use Set to avoid duplicates, preserve order

                for (pattern in COMPILED_PATTERNS) {
                    val matcher = pattern.matcher(html)
                    while (matcher.find()) {
                        val rawUrl = matcher.group(1) ?: continue
                        
                        // Decode logic
                        var decoded = decodeUnicode(rawUrl)
                        // Simple HTML unescape (replace &amp; etc)
                        decoded = decoded.replace("&amp;", "&")
                            .replace("&lt;", "<")
                            .replace("&gt;", ">")
                            .replace("&quot;", "\"")
                            .replace("&#39;", "'")

                        if (decoded.startsWith("http")) {
                            val lower = decoded.lowercase()
                            // Filter for common image extensions
                            if (lower.contains(".jpg") || lower.contains(".jpeg") || 
                                lower.contains(".png") || lower.contains(".webp") || 
                                lower.contains("encrypted-tbn0")) {
                                
                                // Prioritize High-Res: Exclude thumbnails if possible, but keep them as backup
                                // The user wants "High Res", so let's try to filter out tbn0 if we have others
                                candidates.add(decoded)
                            }
                        }
                    }
                }

                logBuilder.append("    Total Unique URLs found: ${candidates.size}\n")
                
                // Filter out thumbnails for the "Best" result
                val highResCandidates = candidates.filter { !it.contains("encrypted-tbn0") && !it.contains("gstatic.com") }
                
                // Return all high-res candidates, or fallback to all candidates if none found
                val finalCandidates = if (highResCandidates.isNotEmpty()) {
                    highResCandidates
                } else {
                    candidates.toList()
                }

                if (finalCandidates.isNotEmpty()) {
                    logBuilder.append("<-- FOUND ${finalCandidates.size} URLs. Top: ${finalCandidates.first()}\n")
                    return@withContext SearchResult(finalCandidates, logBuilder.toString())
                } else {
                    logBuilder.append("<-- ERROR: No suitable image found.\n")
                    return@withContext SearchResult(emptyList(), logBuilder.toString())
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            logBuilder.append("<-- EXCEPTION: ${e.message}\n")
            return@withContext SearchResult(emptyList(), logBuilder.toString())
        }
    }

    suspend fun downloadImage(url: String, file: java.io.File): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext false
                
                response.body?.byteStream()?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                return@withContext true
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    private fun decodeUnicode(input: String): String {
        var res = input
        // Basic unicode unescape
        val rx = Pattern.compile("\\\\u([0-9A-Fa-f]{4})")
        val matcher = rx.matcher(res)
        val sb = StringBuffer()
        while (matcher.find()) {
            try {
                val ch = matcher.group(1).toInt(16).toChar()
                matcher.appendReplacement(sb, ch.toString())
            } catch (e: Exception) {
                // ignore
            }
        }
        matcher.appendTail(sb)
        res = sb.toString()
        
        // Also handle the \x escape if present (though less common in JSON for these)
        res = res.replace("\\u003d", "=")
            .replace("\\u0026", "&")
            .replace("\\u0025", "%")
            .replace("\\/", "/")
        return res
    }
}
