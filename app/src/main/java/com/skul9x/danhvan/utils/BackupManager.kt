package com.skul9x.danhvan.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.skul9x.danhvan.data.AppDatabase
import com.skul9x.danhvan.data.ShopItem
import com.skul9x.danhvan.data.WordEntity
import com.skul9x.danhvan.ui.StickerPlacement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Data class để backup từ vựng (không dùng Room annotations)
 */
data class WordBackup(
    val id: Int,
    val text: String,
    val syllables: List<String>,
    val imageUri: String?,
    val audioUri: String?,
    val isUserAdded: Boolean,
    val category: String?
)

/**
 * Data class chứa toàn bộ dữ liệu backup
 */
data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val totalStars: Int = 0,
    val inventory: List<String> = emptyList(),
    val stickerInventory: Map<String, Int> = emptyMap(),
    val placedStickers: List<StickerPlacement> = emptyList(),
    val currentTheme: String = "theme_default",
    val currentEffect: String? = null,
    val effectIntensity: Float = 1.0f,
    val customShopItems: List<ShopItem> = emptyList(),
    val priceOverrides: Map<String, Int> = emptyMap(),
    val hiddenShopItems: Set<String> = emptySet(),
    val userWords: List<WordBackup> = emptyList(),
    val lastDailyReward: Long = 0L
)

/**
 * Kết quả backup
 */
sealed class BackupResult {
    data class Success(val uri: Uri, val summary: BackupSummary) : BackupResult()
    data class Error(val message: String) : BackupResult()
}

/**
 * Kết quả restore
 */
sealed class RestoreResult {
    data class Success(val summary: BackupSummary) : RestoreResult()
    data class Error(val message: String) : RestoreResult()
}

/**
 * Tóm tắt nội dung backup
 */
data class BackupSummary(
    val totalStars: Int,
    val stickerCount: Int,
    val placedStickerCount: Int,
    val wordCount: Int,
    val customStickerCount: Int,
    val backupDate: String
)

/**
 * Manager class để xử lý backup và restore dữ liệu người dùng
 */
class BackupManager(private val context: Context) {
    
    private val TAG = "BackupManager"
    private val gson = Gson()
    private val prefs by lazy { 
        context.getSharedPreferences("user_progress", Context.MODE_PRIVATE) 
    }
    private val database by lazy { AppDatabase.getDatabase(context) }
    
    companion object {
        const val BACKUP_VERSION = 1
        const val BACKUP_DATA_FILE = "backup_data.json"
        const val IMAGES_FOLDER = "images"
    }
    
    /**
     * Tạo backup và lưu vào Downloads
     */
    suspend fun createBackup(): BackupResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting backup...")
            
            // 1. Thu thập tất cả dữ liệu
            val backupData = collectBackupData()
            
            // 2. Tạo file backup tạm
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val fileName = "DanhVan_Backup_${dateFormat.format(Date())}.zip"
            val backupDir = File(context.cacheDir, "backup_temp")
            if (backupDir.exists()) backupDir.deleteRecursively()
            backupDir.mkdirs()
            
            // 3. Lưu JSON data
            val jsonFile = File(backupDir, BACKUP_DATA_FILE)
            jsonFile.writeText(gson.toJson(backupData))
            
            // 4. Copy các file ảnh
            val imagesDir = File(backupDir, IMAGES_FOLDER)
            imagesDir.mkdirs()
            copyImageFiles(backupData, imagesDir)
            
            // 5. Zip tất cả
            val zipFile = File(context.getExternalFilesDir(null), fileName)
            zipDirectory(backupDir, zipFile)
            
            // 6. Cleanup
            backupDir.deleteRecursively()
            
            // 7. Lưu thông tin backup lần cuối
            prefs.edit().putLong("last_backup_time", System.currentTimeMillis()).apply()
            
            val summary = createSummary(backupData)
            Log.d(TAG, "Backup completed: ${zipFile.absolutePath}")
            
            BackupResult.Success(Uri.fromFile(zipFile), summary)
            
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed", e)
            BackupResult.Error("Lỗi tạo backup: ${e.message}")
        }
    }
    
    /**
     * Khôi phục từ file backup
     */
    suspend fun restoreBackup(uri: Uri): RestoreResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting restore from: $uri")
            
            // 1. Giải nén file backup
            val extractDir = File(context.cacheDir, "restore_temp")
            if (extractDir.exists()) extractDir.deleteRecursively()
            extractDir.mkdirs()
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                unzipFile(inputStream, extractDir)
            } ?: return@withContext RestoreResult.Error("Không thể đọc file backup")
            
            // 2. Đọc dữ liệu JSON
            val jsonFile = File(extractDir, BACKUP_DATA_FILE)
            if (!jsonFile.exists()) {
                extractDir.deleteRecursively()
                return@withContext RestoreResult.Error("File backup không hợp lệ")
            }
            
            val backupData = gson.fromJson(jsonFile.readText(), BackupData::class.java)
            
            // 3. Khôi phục dữ liệu
            restoreData(backupData, extractDir)
            
            // 4. Cleanup
            extractDir.deleteRecursively()
            
            val summary = createSummary(backupData)
            Log.d(TAG, "Restore completed successfully")
            
            RestoreResult.Success(summary)
            
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            RestoreResult.Error("Lỗi khôi phục: ${e.message}")
        }
    }
    
    /**
     * Thu thập tất cả dữ liệu cần backup
     */
    private suspend fun collectBackupData(): BackupData {
        // SharedPreferences data
        val totalStars = prefs.getInt("total_stars", 0)
        val inventory = prefs.getStringSet("inventory", setOf("theme_default"))?.toList() ?: listOf("theme_default")
        val currentTheme = prefs.getString("current_theme", "theme_default") ?: "theme_default"
        val currentEffect = prefs.getString("current_effect", null)
        val effectIntensity = prefs.getFloat("effect_intensity", 1.0f)
        val lastDailyReward = prefs.getLong("last_daily_reward", 0L)
        
        // Sticker inventory
        val stickerInventoryJson = prefs.getString("sticker_inventory_map", "{}") ?: "{}"
        val stickerInventoryType = object : TypeToken<Map<String, Int>>() {}.type
        val stickerInventory: Map<String, Int> = try {
            gson.fromJson(stickerInventoryJson, stickerInventoryType)
        } catch (e: Exception) { emptyMap() }
        
        // Placed stickers
        val placedStickersJson = prefs.getString("placed_stickers", "[]") ?: "[]"
        val placedStickersType = object : TypeToken<List<StickerPlacement>>() {}.type
        val placedStickers: List<StickerPlacement> = try {
            gson.fromJson(placedStickersJson, placedStickersType)
        } catch (e: Exception) { emptyList() }
        
        // Custom shop items
        val customShopItemsJson = prefs.getString("custom_shop_items", "[]") ?: "[]"
        val customShopItemsType = object : TypeToken<List<ShopItem>>() {}.type
        val customShopItems: List<ShopItem> = try {
            gson.fromJson(customShopItemsJson, customShopItemsType)
        } catch (e: Exception) { emptyList() }
        
        // Price overrides
        val priceOverrides = mutableMapOf<String, Int>()
        prefs.all.forEach { (key, value) ->
            if (key.startsWith("price_override_") && value is Int) {
                val itemId = key.removePrefix("price_override_")
                priceOverrides[itemId] = value
            }
        }
        
        // Hidden shop items
        val hiddenShopItems = prefs.getStringSet("hidden_shop_items", emptySet()) ?: emptySet()
        
        // User words from database
        val wordEntities = database.wordDao().getUserWordsSync()
        val userWords = wordEntities.map { entity ->
            WordBackup(
                id = entity.id,
                text = entity.text,
                syllables = entity.syllables,
                imageUri = entity.imageUri,
                audioUri = entity.audioUri,
                isUserAdded = entity.isUserAdded,
                category = entity.category
            )
        }
        
        return BackupData(
            version = BACKUP_VERSION,
            timestamp = System.currentTimeMillis(),
            totalStars = totalStars,
            inventory = inventory,
            stickerInventory = stickerInventory,
            placedStickers = placedStickers,
            currentTheme = currentTheme,
            currentEffect = currentEffect,
            effectIntensity = effectIntensity,
            customShopItems = customShopItems,
            priceOverrides = priceOverrides,
            hiddenShopItems = hiddenShopItems,
            userWords = userWords,
            lastDailyReward = lastDailyReward
        )
    }
    
    /**
     * Copy các file ảnh vào thư mục backup
     */
    private fun copyImageFiles(backupData: BackupData, imagesDir: File) {
        // Custom sticker images
        backupData.customShopItems.forEach { item ->
            item.imageUri?.let { path ->
                val sourceFile = File(path)
                if (sourceFile.exists()) {
                    val destFile = File(imagesDir, "custom_sticker_${item.id}.png")
                    sourceFile.copyTo(destFile, overwrite = true)
                }
            }
        }
        
        // Word images
        backupData.userWords.forEach { word ->
            word.imageUri?.let { path ->
                val sourceFile = File(path)
                if (sourceFile.exists()) {
                    val destFile = File(imagesDir, "word_${word.id}.png")
                    sourceFile.copyTo(destFile, overwrite = true)
                }
            }
        }
    }
    
    /**
     * Khôi phục dữ liệu từ backup
     */
    private suspend fun restoreData(backupData: BackupData, extractDir: File) {
        val editor = prefs.edit()
        
        // Restore SharedPreferences
        editor.putInt("total_stars", backupData.totalStars)
        editor.putStringSet("inventory", backupData.inventory.toSet())
        editor.putString("current_theme", backupData.currentTheme)
        editor.putString("current_effect", backupData.currentEffect)
        editor.putFloat("effect_intensity", backupData.effectIntensity)
        editor.putLong("last_daily_reward", backupData.lastDailyReward)
        
        // Restore sticker inventory
        editor.putString("sticker_inventory_map", gson.toJson(backupData.stickerInventory))
        
        // Restore placed stickers
        editor.putString("placed_stickers", gson.toJson(backupData.placedStickers))
        
        // Restore price overrides
        backupData.priceOverrides.forEach { (itemId, price) ->
            editor.putInt("price_override_$itemId", price)
        }
        
        // Restore hidden shop items
        editor.putStringSet("hidden_shop_items", backupData.hiddenShopItems)
        
        // Restore custom sticker images and update paths
        val customStickersDir = File(context.filesDir, "custom_stickers")
        if (!customStickersDir.exists()) customStickersDir.mkdirs()
        
        val restoredCustomItems = backupData.customShopItems.map { item ->
            val backupImageFile = File(extractDir, "$IMAGES_FOLDER/custom_sticker_${item.id}.png")
            if (backupImageFile.exists()) {
                val destFile = File(customStickersDir, "custom_sticker_${item.id}.png")
                backupImageFile.copyTo(destFile, overwrite = true)
                item.copy(imageUri = destFile.absolutePath)
            } else {
                item
            }
        }
        editor.putString("custom_shop_items", gson.toJson(restoredCustomItems))
        
        editor.apply()
        
        // Restore user words to database
        val wordDao = database.wordDao()
        
        // Restore word images
        val wordsDir = File(context.filesDir, "word_images")
        if (!wordsDir.exists()) wordsDir.mkdirs()
        
        backupData.userWords.forEach { wordBackup ->
            var restoredImageUri = wordBackup.imageUri
            
            // Restore image file
            val backupImageFile = File(extractDir, "$IMAGES_FOLDER/word_${wordBackup.id}.png")
            if (backupImageFile.exists()) {
                val destFile = File(wordsDir, "word_${wordBackup.id}_${System.currentTimeMillis()}.png")
                backupImageFile.copyTo(destFile, overwrite = true)
                restoredImageUri = destFile.absolutePath
            }
            
            val wordEntity = WordEntity(
                id = 0, // Auto-generate new ID
                text = wordBackup.text,
                syllables = wordBackup.syllables,
                imageUri = restoredImageUri,
                audioUri = wordBackup.audioUri,
                isUserAdded = wordBackup.isUserAdded,
                category = wordBackup.category
            )
            
            // Check if word already exists
            val existingWord = wordDao.getWordByText(wordBackup.text)
            if (existingWord == null) {
                wordDao.insertWord(wordEntity)
            }
        }
    }
    
    /**
     * Tạo tóm tắt backup
     */
    private fun createSummary(backupData: BackupData): BackupSummary {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return BackupSummary(
            totalStars = backupData.totalStars,
            stickerCount = backupData.stickerInventory.values.sum(),
            placedStickerCount = backupData.placedStickers.size,
            wordCount = backupData.userWords.size,
            customStickerCount = backupData.customShopItems.size,
            backupDate = dateFormat.format(Date(backupData.timestamp))
        )
    }
    
    /**
     * Lấy thông tin backup lần cuối
     */
    fun getLastBackupInfo(): String? {
        val lastBackupTime = prefs.getLong("last_backup_time", 0L)
        if (lastBackupTime == 0L) return null
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date(lastBackupTime))
    }
    
    /**
     * Zip một thư mục
     */
    private fun zipDirectory(sourceDir: File, zipFile: File) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            sourceDir.walkTopDown().forEach { file ->
                val relativePath = file.relativeTo(sourceDir).path
                if (file.isFile) {
                    val entry = ZipEntry(relativePath)
                    zos.putNextEntry(entry)
                    file.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }
        }
    }
    
    /**
     * Giải nén file zip với bảo vệ Zip Slip vulnerability
     */
    private fun unzipFile(inputStream: InputStream, destDir: File) {
        val canonicalDestDir = destDir.canonicalPath
        
        ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
            var entry: ZipEntry?
            while (zis.nextEntry.also { entry = it } != null) {
                val entryName = entry?.name ?: continue
                val file = File(destDir, entryName)
                
                // SECURITY FIX: Check for Zip Slip vulnerability (path traversal attack)
                val canonicalFilePath = file.canonicalPath
                if (!canonicalFilePath.startsWith(canonicalDestDir + File.separator) && 
                    canonicalFilePath != canonicalDestDir) {
                    Log.e(TAG, "Security: Blocked zip entry with path traversal: $entryName")
                    throw SecurityException("Zip entry outside target directory: $entryName")
                }
                
                if (entry!!.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    FileOutputStream(file).use { fos ->
                        zis.copyTo(fos)
                    }
                }
                zis.closeEntry()
            }
        }
    }
}
