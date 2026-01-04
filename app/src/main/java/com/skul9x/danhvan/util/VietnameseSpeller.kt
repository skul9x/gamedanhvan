package com.skul9x.danhvan.util

import java.text.Normalizer
import java.util.regex.Pattern

object VietnameseSpeller {

    // Tone Map: Mark char -> Tone Name
    private val TONE_MARKS = mapOf(
        '\u0300' to "huyền", // Grave
        '\u0301' to "sắc",   // Acute
        '\u0309' to "hỏi",   // Hook
        '\u0303' to "ngã",   // Tilde
        '\u0323' to "nặng"   // Dot below
    )

    // Vowel normalization for decomposition
    // We need to handle "a", "á", "à"... to extract the base vowel and the tone.
    // Actually, standardizing to NFD (Normalization Form Decomposition) separates the tone mark.
    
    // Onset Map: "ngh" -> "ngờ", etc.
    private val ONSETS = mapOf(
        "ngh" to "ngờ",
        "ng" to "ngờ",
        "gh" to "gờ",
        "tr" to "trờ",
        "th" to "thờ",
        "ph" to "phờ",
        "nh" to "nhờ",
        "kh" to "khờ",
        "ch" to "chờ",
        "qu" to "quờ",
        "gi" to "di", // Pronunciation varies, but "di" is common for spelling
        "g" to "gờ",
        "đ" to "đờ",
        "d" to "dờ",
        "c" to "cờ",
        "b" to "bờ",
        "h" to "hờ",
        "k" to "ka",
        "l" to "lờ",
        "m" to "mờ",
        "n" to "nờ",
        "p" to "pờ",
        "r" to "rờ",
        "s" to "sờ",
        "t" to "tờ",
        "v" to "vờ",
        "x" to "xờ"
    )

    // Letter names for Rhyme spelling
    private val LETTER_NAMES = mapOf(
        'a' to "a",
        'ă' to "á", // "á" sound for letter ă
        'â' to "ớ", // "ớ" sound for letter â
        'b' to "bờ",
        'c' to "cờ",
        'd' to "dờ",
        'đ' to "đờ",
        'e' to "e",
        'ê' to "ê",
        'g' to "gờ",
        'h' to "hờ",
        'i' to "i",
        'k' to "ca",
        'l' to "lờ",
        'm' to "mờ",
        'n' to "nờ",
        'o' to "o",
        'ô' to "ô",
        'ơ' to "ơ",
        'p' to "pờ",
        'q' to "quy",
        'r' to "rờ",
        's' to "sờ",
        't' to "tờ",
        'u' to "u",
        'ư' to "ư",
        'v' to "vờ",
        'x' to "xờ",
        'y' to "y"
    )

    fun getSpelling(syllable: String): String {
        val normalized = Normalizer.normalize(syllable, Normalizer.Form.NFD)
        
        // 1. Extract Tone
        var toneName = ""
        var baseChars = StringBuilder()
        
        for (char in normalized) {
            if (TONE_MARKS.containsKey(char)) {
                toneName = TONE_MARKS[char]!!
            } else {
                baseChars.append(char)
            }
        }
        
        // Re-normalize base to NFC for processing
        val baseSyllable = Normalizer.normalize(baseChars.toString(), Normalizer.Form.NFC).lowercase()
        
        // 2. Identify Onset
        var onset = ""
        var onsetSound = ""
        var rhyme = baseSyllable
        
        val sortedOnsets = ONSETS.keys.sortedByDescending { it.length }
        
        for (o in sortedOnsets) {
            if (baseSyllable.startsWith(o)) {
                onset = o
                onsetSound = ONSETS[o]!!
                rhyme = baseSyllable.substring(o.length)
                break
            }
        }
        
        // 3. Generate Spelling
        val sb = StringBuilder()
        
        // Determine "Combined Rhyme" (Rhyme with implied tone for closed syllables)
        // If rhyme ends in p, t, c, ch -> add Sắc tone (unless it's already there? No, rhyme is base)
        var combinedRhyme = rhyme
        val endsWithClosed = rhyme.endsWith("p") || rhyme.endsWith("t") || rhyme.endsWith("c") || rhyme.endsWith("ch")
        if (endsWithClosed) {
            combinedRhyme = addSacTone(rhyme)
        }
        
        // Special rhyme pronunciation mapping (e.g., "ia" -> "year")
        val RHYME_PRONUNCIATION = mapOf(
            "ia" to "year"
        )
        val rhymePronunciation = RHYME_PRONUNCIATION[rhyme] ?: combinedRhyme

        // A. Spell Rhyme (if complex)
        if (rhyme.isNotEmpty()) {
            if (rhyme.length > 1) {
                // Spell out letters of rhyme
                var rTemp = rhyme
                val rParts = ArrayList<String>()
                
                while (rTemp.isNotEmpty()) {
                    if (rTemp.startsWith("ng")) {
                        rParts.add("ngờ")
                        rTemp = rTemp.substring(2)
                    } else if (rTemp.startsWith("nh")) {
                        rParts.add("nhờ")
                        rTemp = rTemp.substring(2)
                    } else if (rTemp.startsWith("ch")) {
                        rParts.add("chờ")
                        rTemp = rTemp.substring(2)
                    } else if (rTemp.startsWith("tr")) { 
                        rParts.add("trờ")
                        rTemp = rTemp.substring(2)
                    } else if (rTemp.startsWith("ph")) {
                         rParts.add("phờ")
                         rTemp = rTemp.substring(2)
                    } else if (rTemp.startsWith("th")) {
                         rParts.add("thờ")
                         rTemp = rTemp.substring(2)
                    } else if (rTemp.startsWith("gh")) {
                         rParts.add("gờ")
                         rTemp = rTemp.substring(2)
                    } else if (rTemp.startsWith("kh")) {
                         rParts.add("khờ")
                         rTemp = rTemp.substring(2)
                    } else {
                        val c = rTemp[0]
                        rParts.add(LETTER_NAMES[c] ?: c.toString())
                        rTemp = rTemp.substring(1)
                    }
                }
                
                // Append parts
                rParts.forEach { sb.append(it).append(" ") }
                sb.append(rhymePronunciation).append(", ") // Use pronunciation (e.g., "year" for "ia")
            }
        }
        
        // B. Spell Full Syllable
        // Onset + Combined Rhyme -> Base with Combined Rhyme Tone
        // "tr" + "ức" -> "trức"
        // "đ" + "áp" -> "đáp"
        // "ng" + "a" -> "nga"
        
        // Construct the "Base with Combined Rhyme Tone" string for TTS
        // This is basically Onset + CombinedRhyme
        val baseWithTone = onset + combinedRhyme
        
        if (onset.isNotEmpty()) {
            sb.append(onsetSound).append(" ")
            if (rhyme.isNotEmpty()) {
                sb.append(rhymePronunciation).append(" ") // Use pronunciation (e.g., "tờ year")
            }
            // Base sound
            sb.append(baseWithTone) // "tia"
        } else {
            // No onset (e.g. "án")
            // "a nờ án, sắc án"
            // Rhyme spelling handled above: "a nờ án, "
            // Here just "án"
            sb.append(baseWithTone)
        }
        
        // C. Add Tone
        if (toneName.isNotEmpty()) {
            sb.append(" ").append(toneName).append(" ").append(syllable)
        }
        
        return sb.toString()
    }

    private fun addSacTone(text: String): String {
        // Add Sắc tone to the main vowel
        // Simple logic: Find first vowel (or specific priority) and add combining acute accent
        // Vowels: a, ă, â, e, ê, i, o, ô, ơ, u, ư, y
        // Priority: ê, ơ, â, ă, a, o, u, i, y (approximate)
        // Actually, let's just use a simple regex or replacement for common vowels
        // Or decompose, find vowel, add mark.
        
        val vowels = "aăâeêioôơuưy"
        var bestIndex = -1
        var bestVowelPriority = -1
        
        // Priority map (higher is better)
        val priority = mapOf(
            'ê' to 10, 'ơ' to 9, 'â' to 8, 'ă' to 7, 
            'ô' to 6, 'a' to 5, 'o' to 4, 'u' to 3, 'ư' to 3, 'i' to 2, 'y' to 1
        )

        for (i in text.indices) {
            val c = text[i]
            if (vowels.contains(c)) {
                val p = priority[c] ?: 0
                if (p > bestVowelPriority) {
                    bestVowelPriority = p
                    bestIndex = i
                }
            }
        }
        
        if (bestIndex != -1) {
            val sb = StringBuilder(text)
            // We need to combine the vowel with the tone.
            // Since we are in NFC, we might need to decompose or just map.
            // E.g. 'a' -> 'á', 'ê' -> 'ế'.
            val char = text[bestIndex]
            val withTone = when(char) {
                'a' -> 'á'
                'ă' -> 'ắ'
                'â' -> 'ấ'
                'e' -> 'é'
                'ê' -> 'ế'
                'i' -> 'í'
                'o' -> 'ó'
                'ô' -> 'ố'
                'ơ' -> 'ớ'
                'u' -> 'ú'
                'ư' -> 'ứ'
                'y' -> 'ý'
                else -> char
            }
            sb.setCharAt(bestIndex, withTone)
            return sb.toString()
        }
        
        return text
    }
}
