package com.skul9x.danhvan.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.skul9x.danhvan.data.AppDatabase
import com.skul9x.danhvan.data.WordEntity
import com.skul9x.danhvan.data.DailyStats
import com.skul9x.danhvan.data.ShopItem
import com.skul9x.danhvan.utils.AssetManager
import com.skul9x.danhvan.utils.SyllableTokenizer
import com.skul9x.danhvan.utils.TTSManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val wordDao = AppDatabase.getDatabase(application).wordDao()
    private val assetManager = AssetManager(application)
    private val ttsManager = TTSManager(application)
    private val dailyStatsDao = AppDatabase.getDatabase(application).dailyStatsDao()

    val allWords: StateFlow<List<WordEntity>> = wordDao.getAllWords()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val userWords: StateFlow<List<WordEntity>> = wordDao.getUserWords()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val topicImageDao = AppDatabase.getDatabase(application).topicImageDao()

    // Dynamic Topics derived from User Words + Image Cache
    val topics: StateFlow<List<com.skul9x.danhvan.data.Topic>> = kotlinx.coroutines.flow.combine(
        userWords,
        topicImageDao.getAllTopicImages()
    ) { words, topicImages ->
        val defaultTopics = com.skul9x.danhvan.data.TopicData.topics
        val dynamicCategories = words.mapNotNull { it.category }
            .filter { it.isNotEmpty() }
            .distinct()
            .filter { category -> defaultTopics.none { it.name == category } } // Avoid duplicates with default

        val dynamicTopics = dynamicCategories.map { category ->
            val imageEntity = topicImages.find { it.category == category }
            com.skul9x.danhvan.data.Topic(
                id = category, // Use category name as ID for simplicity
                name = category,
                icon = androidx.compose.material.icons.Icons.Default.Star, // Default icon for dynamic topics
                color = com.skul9x.danhvan.data.TopicData.getColorForCategory(category),
                imagePath = imageEntity?.imagePath
            )
        }
        defaultTopics + dynamicTopics
    }.stateIn(viewModelScope, SharingStarted.Lazily, com.skul9x.danhvan.data.TopicData.topics)



    private fun checkAndFetchTopicImages() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val currentTopics = topics.value
            val topicImagesDir = java.io.File(getApplication<Application>().filesDir, "topic_images")
            if (!topicImagesDir.exists()) topicImagesDir.mkdirs()

            currentTopics.forEach { topic ->
                if (topic.id != "all" && topic.imagePath == null) {
                    fetchAndSaveTopicImage(topic, topicImagesDir)
                }
            }
        }
    }
    
    fun reloadCategoryImages() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            appendLog("--> Reloading all category images...\n")
            // 1. Clear DB
            topicImageDao.deleteAll()
            
            // 2. Clear Files
            val topicImagesDir = java.io.File(getApplication<Application>().filesDir, "topic_images")
            if (topicImagesDir.exists()) {
                topicImagesDir.deleteRecursively()
            }
            
            // 3. Re-fetch
            checkAndFetchTopicImages()
        }
    }

    fun updateTopicImage(topic: com.skul9x.danhvan.data.Topic) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            appendLog("--> Updating image for topic: ${topic.name}\n")
            
            // 1. Delete existing file
            if (topic.imagePath != null) {
                val file = java.io.File(topic.imagePath)
                if (file.exists()) file.delete()
            }
            
            val topicImagesDir = java.io.File(getApplication<Application>().filesDir, "topic_images")
            if (!topicImagesDir.exists()) topicImagesDir.mkdirs()
            
            fetchAndSaveTopicImage(topic, topicImagesDir)
        }
    }

    private suspend fun fetchAndSaveTopicImage(topic: com.skul9x.danhvan.data.Topic, topicImagesDir: java.io.File) {
        appendLog("--> Checking image for topic: ${topic.name}\n")
        
        // Search for image
        val searchResult = com.skul9x.danhvan.util.GoogleImageHelper.searchImage(topic.name + " cartoon")
        appendLog(searchResult.log)
        
        if (searchResult.urls.isNotEmpty()) {
            // Take top 10 (or less if fewer results) and shuffle them
            val candidates = searchResult.urls.take(10).shuffled()
            var success = false
            
            for (imageUrl in candidates) {
                val fileName = "topic_${topic.id.hashCode()}_${System.currentTimeMillis()}.jpg"
                val file = java.io.File(topicImagesDir, fileName)
                
                if (com.skul9x.danhvan.util.GoogleImageHelper.downloadImage(imageUrl, file)) {
                    topicImageDao.insertTopicImage(
                        com.skul9x.danhvan.data.TopicImageEntity(topic.id, file.absolutePath)
                    )
                    appendLog("--> Saved topic image: ${topic.name} (Source: $imageUrl)\n")
                    success = true
                    break // Stop after first success
                } else {
                    appendLog("--> Failed to download candidate: $imageUrl. Trying next...\n")
                }
            }
            
            if (!success) {
                appendLog("--> Failed to download ANY image for topic: ${topic.name}\n")
            }
        } else {
             appendLog("--> No images found for: ${topic.name}\n")
        }
    }

    fun addWord(text: String, imagePath: String?, audioPath: String?, category: String? = null) {
        viewModelScope.launch {
            val syllables = SyllableTokenizer.tokenize(text)
            
            // Auto-generate image if missing
            val finalImagePath = imagePath ?: assetManager.generatePlaceholderImage(text)
            
            // Audio is handled by TTS if missing (path is null)
            
            val word = WordEntity(
                text = text,
                syllables = syllables,
                imageUri = finalImagePath,
                audioUri = audioPath,
                isUserAdded = true,
                category = category
            )
            wordDao.insertWord(word)
        }
    }

    fun importJson(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val jsonWords = com.skul9x.danhvan.utils.JsonImportManager.parseJson(context, uri)
                jsonWords.forEach { jsonWord ->
                    addWord(jsonWord.text, jsonWord.imageUri, jsonWord.audioUri, jsonWord.category)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error (e.g., show toast via channel)
            }
        }
    }



    fun deleteWord(word: WordEntity) {
        viewModelScope.launch {
            assetManager.deleteAsset(word.imageUri)
            assetManager.deleteAsset(word.audioUri)
            wordDao.deleteWord(word)
        }
    }

    fun deleteAllUserWords() {
        viewModelScope.launch {
            // Delete associated assets first
            val currentWords = userWords.value
            currentWords.forEach { word ->
                assetManager.deleteAsset(word.imageUri)
                assetManager.deleteAsset(word.audioUri)
            }
            // Delete all from database
            wordDao.deleteAllUserWords()
        }
    }

    fun playTTS(text: String) {
        ttsManager.speak(text)
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
    
    // --- TOPIC SYSTEM ---
    fun getWordsByTopic(topicId: String): StateFlow<List<WordEntity>> {
        return if (topicId == "all") {
            // Combine all words (user + built-in if we had them)
            // For now just userWords as that's what we have
            userWords
        } else {
            // Filter by category
            // Note: Since we don't have a direct DB query for category yet, we'll filter in memory for now
            // Ideally this should be a DAO query
            userWords.map { words ->
                words.filter { it.category == topicId }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }
    
    // --- REWARDS & STATS SYSTEM ---
    
    private val _currentDayStars = kotlinx.coroutines.flow.MutableStateFlow(0)
    // starCount now reflects TOTAL stars for the shop context
    val starCount: StateFlow<Int> = _currentDayStars
    
    val weeklyStats = dailyStatsDao.getLast7DaysStats()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private fun checkDateAndReset() {
        viewModelScope.launch {
            val today = getTodayTimestamp()
            val stats = dailyStatsDao.getStatsForDate(today)
            loadTotalStars()
        }
    }
    
    private fun loadTotalStars() {
         val total = prefs.getInt("total_stars", 0)
         _currentDayStars.value = total
         checkDailyReward()
    }
    
    private fun getTodayTimestamp(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    // Override addStar to also increment total stars
    fun addStar() {
        viewModelScope.launch {
            val today = getTodayTimestamp()
            val currentStats = dailyStatsDao.getStatsForDate(today) ?: DailyStats(date = today)
            val newStats = currentStats.copy(starsEarned = currentStats.starsEarned + 1)
            dailyStatsDao.insertOrUpdate(newStats)
            
            // Update Total Stars
            val currentTotal = prefs.getInt("total_stars", 0)
            val newTotal = currentTotal + 1
            prefs.edit().putInt("total_stars", newTotal).apply()
            
            // Update UI
            _currentDayStars.value = newTotal
        }
    }

    // --- NEW FEATURES ---

    // Daily Reward
    private val _dailyRewardClaimed = kotlinx.coroutines.flow.MutableStateFlow(false)
    val dailyRewardClaimed: StateFlow<Boolean> = _dailyRewardClaimed

    fun checkDailyReward() {
        val today = getTodayTimestamp()
        val lastClaimed = prefs.getLong("last_daily_reward", 0)
        _dailyRewardClaimed.value = lastClaimed == today
    }

    fun claimDailyReward() {
        val today = getTodayTimestamp()
        val lastClaimed = prefs.getLong("last_daily_reward", 0)
        
        if (lastClaimed != today) {
            val reward = 10 // 10 stars daily
            val currentTotal = prefs.getInt("total_stars", 0)
            val newTotal = currentTotal + reward
            
            // Fix Race Condition: Use single editor for atomic operation
            prefs.edit().apply {
                putInt("total_stars", newTotal)
                putLong("last_daily_reward", today)
            }.apply()
            
            _currentDayStars.value = newTotal
            _dailyRewardClaimed.value = true
        }
    }

    // Lucky Spin
    fun spinWheel(onResult: (Int) -> Unit) {
        // Use synchronized block to prevent race condition from rapid clicks
        synchronized(this) {
            val cost = 5
            val currentTotal = prefs.getInt("total_stars", 0)
            
            if (currentTotal >= cost) {
                // Random reward
                val rewards = listOf(0, 5, 10, 20, 50)
                val weights = listOf(30, 40, 20, 8, 2) // Probabilities
                val reward = weightedRandom(rewards, weights)
                
                // Calculate final total: current - cost + reward
                val finalTotal = currentTotal - cost + reward
                
                // Fix Race Condition: Single atomic write
                prefs.edit().putInt("total_stars", finalTotal).apply()
                _currentDayStars.value = finalTotal
                
                onResult(reward)
            } else {
                onResult(-1) // Not enough stars
            }
        }
    }

    private fun weightedRandom(items: List<Int>, weights: List<Int>): Int {
        val totalWeight = weights.sum()
        var randomValue = (0 until totalWeight).random()
        for (i in items.indices) {
            randomValue -= weights[i]
            if (randomValue < 0) return items[i]
        }
        return items.last()
    }

    // --- SHOP & INVENTORY ---
    private val _shopItems = kotlinx.coroutines.flow.MutableStateFlow<List<ShopItem>>(emptyList())
    val shopItems: StateFlow<List<ShopItem>> = _shopItems

    private val _inventory = kotlinx.coroutines.flow.MutableStateFlow<List<String>>(listOf("theme_default"))
    val inventory: StateFlow<List<String>> = _inventory

    private val _currentTheme = kotlinx.coroutines.flow.MutableStateFlow("theme_default")
    val currentTheme: StateFlow<String> = _currentTheme

    private val _currentEffect = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val currentEffect: StateFlow<String?> = _currentEffect

    private val _effectIntensity = kotlinx.coroutines.flow.MutableStateFlow(1.0f)
    val effectIntensity: StateFlow<Float> = _effectIntensity

    // Simple persistence using SharedPreferences for now (to avoid DB migration complexity for this iteration)
    private val prefs by lazy { 
        application.getSharedPreferences("user_progress", Context.MODE_PRIVATE) 
    }

    // --- STICKER BOOK SYSTEM (declared before init to ensure proper initialization) ---
    private val _placedStickers = kotlinx.coroutines.flow.MutableStateFlow<List<StickerPlacement>>(emptyList())
    val placedStickers: StateFlow<List<StickerPlacement>> = _placedStickers

    private val _currentStickerPage = kotlinx.coroutines.flow.MutableStateFlow(0)
    val currentStickerPage: StateFlow<Int> = _currentStickerPage

    private val _stickerInventory = kotlinx.coroutines.flow.MutableStateFlow<Map<String, Int>>(emptyMap())
    val stickerInventory: StateFlow<Map<String, Int>> = _stickerInventory

    init {
        // Load Shop Data
        loadShopItems()
        
        // Load inventory & settings
        val savedInventory = prefs.getStringSet("inventory", setOf("theme_default")) ?: setOf("theme_default")
        _inventory.value = savedInventory.toList()
        _currentTheme.value = prefs.getString("current_theme", "theme_default") ?: "theme_default"
        _currentEffect.value = prefs.getString("current_effect", null)
        _effectIntensity.value = prefs.getFloat("effect_intensity", 1.0f)
        
        // Load sticker data
        try {
            loadStickerPlacements()
            loadStickerInventory()
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("MainViewModel", "Error loading sticker data", e)
        }
        
        // Initialize stars and daily reward check
        checkDateAndReset()
        
        // Check for missing topic images after a short delay to let things settle
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            checkAndFetchTopicImages()
        }
    }
    
    private fun loadShopItems() {
        val baseItems = com.skul9x.danhvan.data.ShopData.items
        
        // Load custom items
        val customConfigJson = prefs.getString("custom_shop_items", "[]") ?: "[]"
        val type = object : com.google.gson.reflect.TypeToken<List<ShopItem>>() {}.type
        val customItems: List<ShopItem> = try {
            com.google.gson.Gson().fromJson(customConfigJson, type)
        } catch (e: Exception) { emptyList() }
        
        // Load hidden items
        val hiddenIds = prefs.getStringSet("hidden_shop_items", emptySet()) ?: emptySet()
        
        val allItems = (baseItems + customItems).filter { !hiddenIds.contains(it.id) }
        
        val updatedItems = allItems.map { item ->
            val overridePrice = prefs.getInt("price_override_${item.id}", -1)
            if (overridePrice != -1) {
                item.copy(cost = overridePrice)
            } else {
                item
            }
        }
        _shopItems.value = updatedItems
    }
    
    fun addCustomSticker(name: String, cost: Int, imageUri: Uri, context: Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            // Copy image to internal storage
            val fileName = "custom_sticker_${System.currentTimeMillis()}.png"
            val file = java.io.File(context.filesDir, "custom_stickers")
            if (!file.exists()) file.mkdirs()
            val destFile = java.io.File(file, fileName)
            
            try {
                context.contentResolver.openInputStream(imageUri)?.use { input ->
                    java.io.FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                
                val newItem = ShopItem(
                    id = "custom_${System.currentTimeMillis()}",
                    name = name,
                    cost = cost,
                    color1 = 0xFFE3F2FD, // Default colors
                    color2 = 0xFF90CAF9,
                    type = com.skul9x.danhvan.data.ItemType.STICKER,
                    imageUri = destFile.absolutePath
                )
                
                // Save to custom list
                val customJson = prefs.getString("custom_shop_items", "[]") ?: "[]"
                val type = object : com.google.gson.reflect.TypeToken<List<ShopItem>>() {}.type
                val currentCustom: MutableList<ShopItem> = com.google.gson.Gson().fromJson(customJson, type)
                currentCustom.add(newItem)
                
                prefs.edit().putString("custom_shop_items", com.google.gson.Gson().toJson(currentCustom)).apply()
                loadShopItems()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun deleteShopItem(item: ShopItem) {
        if (item.id.startsWith("custom_")) {
            // Delete custom item
            val customJson = prefs.getString("custom_shop_items", "[]") ?: "[]"
            val type = object : com.google.gson.reflect.TypeToken<List<ShopItem>>() {}.type
            val currentCustom: MutableList<ShopItem> = com.google.gson.Gson().fromJson(customJson, type)
            
            val itemToDelete = currentCustom.find { it.id == item.id }
            if (itemToDelete != null) {
                // Delete file
                if (itemToDelete.imageUri != null) {
                    val file = java.io.File(itemToDelete.imageUri)
                    if (file.exists()) file.delete()
                }
                currentCustom.remove(itemToDelete)
                prefs.edit().putString("custom_shop_items", com.google.gson.Gson().toJson(currentCustom)).apply()
            }
        } else {
            // Hide built-in item
            val hiddenIds = prefs.getStringSet("hidden_shop_items", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            hiddenIds.add(item.id)
            prefs.edit().putStringSet("hidden_shop_items", hiddenIds).apply()
        }
        loadShopItems()
    }
    
    fun updateItemPrice(itemId: String, newPrice: Int) {
        prefs.edit().putInt("price_override_$itemId", newPrice).apply()
        loadShopItems() // Reload to update flow
    }
    
    fun equipTheme(itemId: String) {
        _currentTheme.value = itemId
        prefs.edit().putString("current_theme", itemId).apply()
    }

    fun equipEffect(itemId: String) {
        _currentEffect.value = itemId
        prefs.edit().putString("current_effect", itemId).apply()
    }
    
    fun updateEffectIntensity(value: Float) {
        _effectIntensity.value = value
        prefs.edit().putFloat("effect_intensity", value).apply()
    }

    // --- STICKER BOOK FUNCTIONS ---

    fun setStickerPage(page: Int) {
        if (page >= 0) {
            _currentStickerPage.value = page
        }
    }

    private fun loadStickerPlacements() {
        val json = prefs.getString("placed_stickers", "[]") ?: "[]"
        try {
            val type = object : com.google.gson.reflect.TypeToken<List<StickerPlacement>>() {}.type
            val list: List<StickerPlacement> = com.google.gson.Gson().fromJson(json, type)
            _placedStickers.value = list
        } catch (e: Exception) {
            e.printStackTrace()
            _placedStickers.value = emptyList()
        }
    }

    fun saveStickerPlacements() {
        val json = com.google.gson.Gson().toJson(_placedStickers.value)
        prefs.edit().putString("placed_stickers", json).apply()
    }

    private fun loadStickerInventory() {
        val json = prefs.getString("sticker_inventory_map", "{}") ?: "{}"
        try {
            val type = object : com.google.gson.reflect.TypeToken<Map<String, Int>>() {}.type
            val map: Map<String, Int> = com.google.gson.Gson().fromJson(json, type)
            _stickerInventory.value = map
        } catch (e: Exception) {
            e.printStackTrace()
            _stickerInventory.value = emptyMap()
        }
    }

    private fun saveStickerInventory() {
        val json = com.google.gson.Gson().toJson(_stickerInventory.value)
        prefs.edit().putString("sticker_inventory_map", json).apply()
    }

    fun placeSticker(stickerId: String, x: Float, y: Float) {
        // Check if we have this sticker in inventory
        val currentCount = _stickerInventory.value[stickerId] ?: 0
        if (currentCount > 0) {
            // Decrement inventory
            val newMap = _stickerInventory.value.toMutableMap()
            newMap[stickerId] = currentCount - 1
            if (newMap[stickerId] == 0) newMap.remove(stickerId)
            _stickerInventory.value = newMap
            saveStickerInventory()

            // Place sticker
            val newSticker = StickerPlacement(
                id = java.util.UUID.randomUUID().toString(),
                stickerId = stickerId,
                x = x,
                y = y,
                page = _currentStickerPage.value
            )
            _placedStickers.value = _placedStickers.value + newSticker
            saveStickerPlacements()
        } else {
            // Should not happen if UI is correct, but good safety
            appendLog("Attempted to place sticker $stickerId but count is 0")
        }
    }

    // Debounce job for auto-saving sticker positions
    private var stickerSaveJob: kotlinx.coroutines.Job? = null
    
    fun updateSticker(id: String, x: Float, y: Float, scale: Float, rotation: Float) {
        _placedStickers.value = _placedStickers.value.map { 
            if (it.id == id) it.copy(x = x, y = y, scale = scale, rotation = rotation) else it 
        }
        
        // Debounced auto-save: save after 500ms of no updates
        // This ensures data is saved even if app is killed, while avoiding excessive writes
        stickerSaveJob?.cancel()
        stickerSaveJob = viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            saveStickerPlacements()
        }
    }

    fun removeSticker(id: String) {
        val stickerToRemove = _placedStickers.value.find { it.id == id }
        if (stickerToRemove != null) {
            // Remove from placed
            _placedStickers.value = _placedStickers.value.filter { it.id != id }
            saveStickerPlacements()

            // Return to inventory
            val currentCount = _stickerInventory.value[stickerToRemove.stickerId] ?: 0
            val newMap = _stickerInventory.value.toMutableMap()
            newMap[stickerToRemove.stickerId] = currentCount + 1
            _stickerInventory.value = newMap
            saveStickerInventory()
        }
    }
    
    /**
     * Recall all stickers from a specific page back to inventory.
     * This allows users to reorganize all stickers at once.
     */
    fun recallAllStickersFromPage(page: Int) {
        val stickersOnPage = _placedStickers.value.filter { it.page == page }
        
        if (stickersOnPage.isEmpty()) return
        
        // Return all stickers to inventory
        val newInventory = _stickerInventory.value.toMutableMap()
        stickersOnPage.forEach { sticker ->
            val currentCount = newInventory[sticker.stickerId] ?: 0
            newInventory[sticker.stickerId] = currentCount + 1
        }
        _stickerInventory.value = newInventory
        saveStickerInventory()
        
        // Remove all stickers from this page
        _placedStickers.value = _placedStickers.value.filter { it.page != page }
        saveStickerPlacements()
        
        appendLog("Recalled ${stickersOnPage.size} stickers from page ${page + 1}")
    }
    
    /**
     * Recall ALL stickers from ALL pages back to inventory.
     * This allows users to completely reorganize their sticker book.
     */
    fun recallAllStickers() {
        val allPlacedStickers = _placedStickers.value
        
        if (allPlacedStickers.isEmpty()) return
        
        // Return all stickers to inventory
        val newInventory = _stickerInventory.value.toMutableMap()
        allPlacedStickers.forEach { sticker ->
            val currentCount = newInventory[sticker.stickerId] ?: 0
            newInventory[sticker.stickerId] = currentCount + 1
        }
        _stickerInventory.value = newInventory
        saveStickerInventory()
        
        // Remove all stickers from all pages
        _placedStickers.value = emptyList()
        saveStickerPlacements()
        
        // Reset to page 0
        _currentStickerPage.value = 0
        
        appendLog("Recalled ${allPlacedStickers.size} stickers from all pages")
    }
    
    // Override purchaseItem to handle stickers
    fun purchaseItem(item: com.skul9x.danhvan.data.ShopItem) {
        val currentTotal = prefs.getInt("total_stars", 0)
        if (currentTotal >= item.cost) {
            val newTotal = currentTotal - item.cost
            prefs.edit().putInt("total_stars", newTotal).apply()
            _currentDayStars.value = newTotal // Update UI stars

            if (item.type == com.skul9x.danhvan.data.ItemType.STICKER) {
                // Add to Sticker Inventory (Quantity)
                val currentCount = _stickerInventory.value[item.id] ?: 0
                val newMap = _stickerInventory.value.toMutableMap()
                newMap[item.id] = currentCount + 1
                _stickerInventory.value = newMap
                saveStickerInventory()
                
                // Also add to general inventory for "Owned" check compatibility if needed, 
                // but for stickers we rely on stickerInventory. 
                // Let's keep it in general inventory too so it shows up in "Sticker của bé" initially if we used that logic,
                // but we are switching to stickerInventory logic.
                // However, existing ShopScreen checks `inventory.contains(item.id)`.
                // We should probably leave it there or update ShopScreen.
                // Let's add it to general inventory just to mark "at least one owned" or "ever owned".
                val newInventory = _inventory.value.toMutableList().apply { 
                    if (!contains(item.id)) add(item.id) 
                }
                _inventory.value = newInventory
                prefs.edit().putStringSet("inventory", newInventory.toSet()).apply()

            } else {
                // Normal Theme/Effect
                val newInventory = _inventory.value.toMutableList().apply { add(item.id) }
                _inventory.value = newInventory
                prefs.edit().putStringSet("inventory", newInventory.toSet()).apply()
            }
        }
    }
    
    // --- DEBUG LOGGING ---
    private val _debugLog = kotlinx.coroutines.flow.MutableStateFlow<List<String>>(listOf("Debug Log Started..."))
    val debugLog: StateFlow<List<String>> = _debugLog
    
    fun appendLog(message: String) {
        val currentList = _debugLog.value.toMutableList()
        currentList.add(message)
        if (currentList.size > 1000) {
            currentList.removeAt(0)
        }
        _debugLog.value = currentList
    }
    
    fun clearLog() {
        _debugLog.value = listOf("Debug Log Cleared.")
    }
    
    // --- BACKUP SYSTEM ---
    private val backupManager by lazy { com.skul9x.danhvan.utils.BackupManager(getApplication()) }
    
    private val _isBackupInProgress = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isBackupInProgress: StateFlow<Boolean> = _isBackupInProgress
    
    private val _backupMessage = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val backupMessage: StateFlow<String?> = _backupMessage
    
    fun getLastBackupInfo(): String? = backupManager.getLastBackupInfo()
    
    fun createBackup(onComplete: (com.skul9x.danhvan.utils.BackupResult) -> Unit) {
        viewModelScope.launch {
            _isBackupInProgress.value = true
            appendLog("---> Starting backup...")
            
            val result = backupManager.createBackup()
            
            when (result) {
                is com.skul9x.danhvan.utils.BackupResult.Success -> {
                    appendLog("---> Backup completed: ${result.summary}")
                    _backupMessage.value = "Sao lưu thành công! ${result.summary.totalStars}⭐, ${result.summary.wordCount} từ"
                }
                is com.skul9x.danhvan.utils.BackupResult.Error -> {
                    appendLog("---> Backup failed: ${result.message}")
                    _backupMessage.value = result.message
                }
            }
            
            _isBackupInProgress.value = false
            onComplete(result)
        }
    }
    
    fun restoreBackup(uri: Uri, onComplete: (com.skul9x.danhvan.utils.RestoreResult) -> Unit) {
        viewModelScope.launch {
            _isBackupInProgress.value = true
            appendLog("---> Starting restore from: $uri")
            
            val result = backupManager.restoreBackup(uri)
            
            when (result) {
                is com.skul9x.danhvan.utils.RestoreResult.Success -> {
                    appendLog("---> Restore completed: ${result.summary}")
                    _backupMessage.value = "Khôi phục thành công! ${result.summary.totalStars}⭐, ${result.summary.wordCount} từ"
                    
                    // Reload all data
                    reloadAfterRestore()
                }
                is com.skul9x.danhvan.utils.RestoreResult.Error -> {
                    appendLog("---> Restore failed: ${result.message}")
                    _backupMessage.value = result.message
                }
            }
            
            _isBackupInProgress.value = false
            onComplete(result)
        }
    }
    
    private fun reloadAfterRestore() {
        // Reload SharedPreferences data
        val savedInventory = prefs.getStringSet("inventory", setOf("theme_default")) ?: setOf("theme_default")
        _inventory.value = savedInventory.toList()
        _currentTheme.value = prefs.getString("current_theme", "theme_default") ?: "theme_default"
        _currentEffect.value = prefs.getString("current_effect", null)
        _effectIntensity.value = prefs.getFloat("effect_intensity", 1.0f)
        
        // Reload stars
        loadTotalStars()
        
        // Reload sticker data
        loadStickerPlacements()
        loadStickerInventory()
        
        // Reload shop items
        loadShopItems()
    }
    
    fun clearBackupMessage() {
        _backupMessage.value = null
    }
}

