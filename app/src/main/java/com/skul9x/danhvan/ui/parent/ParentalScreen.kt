package com.skul9x.danhvan.ui.parent

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.skul9x.danhvan.data.WordEntity
import com.skul9x.danhvan.ui.MainViewModel
import java.io.File
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun ParentalScreen(viewModel: MainViewModel, onNavigateToDebug: () -> Unit) {
    var newWordText by remember { mutableStateOf("") }
    val userWords by viewModel.userWords.collectAsState()

    var showJsonGuide by remember { mutableStateOf(false) }
    var showShopPriceDialog by remember { mutableStateOf(false) }
    var showDeleteAllConfirmDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importJson(it) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Khu vực Phụ huynh", style = MaterialTheme.typography.headlineMedium)
            Row {
                IconButton(onClick = { onNavigateToDebug() }) {
                    Icon(androidx.compose.material.icons.Icons.Default.Build, contentDescription = "Debug Logs", tint = MaterialTheme.colorScheme.error)
                }
                IconButton(onClick = { viewModel.reloadCategoryImages() }) {
                    Icon(androidx.compose.material.icons.Icons.Default.Refresh, contentDescription = "Reload Category Images", tint = androidx.compose.ui.graphics.Color.Blue)
                }
                IconButton(onClick = { showJsonGuide = true }) {
                    Icon(Icons.Default.Info, contentDescription = "Hướng dẫn JSON")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { launcher.launch("application/json") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Nhập từ JSON")
            }
            Button(
                onClick = { showShopPriceDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Quản lý Giá Shop")
            }
            Button(
                onClick = { showDeleteAllConfirmDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Xóa từ")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- BACKUP SECTION ---
        BackupSection(viewModel = viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        // Add New Word Section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Thêm từ mới", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = newWordText,
                    onValueChange = { newWordText = it },
                    label = { Text("Nhập từ (Ví dụ: Con Mèo)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (newWordText.isNotBlank()) {
                            viewModel.addWord(newWordText, null, null) // Image/Audio null for auto-gen
                            newWordText = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tạo từ mới")
                }
                Text(
                    "Lưu ý: Nếu không chọn ảnh/âm thanh, hệ thống sẽ tự tạo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Theme & Effect Settings
        Text("Cài đặt Giao diện & Hiệu ứng", style = MaterialTheme.typography.titleMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                val currentTheme by viewModel.currentTheme.collectAsState()
                val currentEffect by viewModel.currentEffect.collectAsState()
                
                Text("Giao diện (Themes)", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(com.skul9x.danhvan.data.ShopData.items.filter { it.type == com.skul9x.danhvan.data.ItemType.THEME }) { item ->
                        val isSelected = currentTheme == item.id
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(androidx.compose.ui.graphics.Color(item.color1), androidx.compose.ui.graphics.Color(item.color2))
                                    )
                                )
                                .border(if (isSelected) 3.dp else 0.dp, if (isSelected) androidx.compose.ui.graphics.Color.Green else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { viewModel.equipTheme(item.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                            }
                            Text(item.name, style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color.Black, maxLines = 1, modifier = Modifier.align(Alignment.BottomCenter).padding(2.dp))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Hiệu ứng (Effects)", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(com.skul9x.danhvan.data.ShopData.items.filter { it.type == com.skul9x.danhvan.data.ItemType.EFFECT }) { item ->
                        val isSelected = currentEffect == item.id
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(androidx.compose.ui.graphics.Color.LightGray)
                                .border(if (isSelected) 3.dp else 0.dp, if (isSelected) androidx.compose.ui.graphics.Color.Green else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { viewModel.equipEffect(item.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color.Green)
                            }
                            Text(item.name, style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color.Black, maxLines = 1, modifier = Modifier.align(Alignment.BottomCenter).padding(2.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Danh sách từ đã thêm", style = MaterialTheme.typography.titleMedium)
        
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(userWords, key = { it.id }) { word ->
                WordItem(word, onDelete = { viewModel.deleteWord(word) })
            }
        }
        
        // Delete All Confirmation Dialog
        if (showDeleteAllConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAllConfirmDialog = false },
                title = { Text("⚠️ Xác nhận xóa tất cả") },
                text = {
                    Column {
                        Text(
                            "Bạn có chắc chắn muốn xóa TẤT CẢ ${userWords.size} từ vựng đã thêm?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Hành động này không thể hoàn tác!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteAllUserWords()
                            showDeleteAllConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Xóa tất cả")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAllConfirmDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }

    if (showJsonGuide) {
        AlertDialog(
            onDismissRequest = { showJsonGuide = false },
            title = { Text("Định dạng file JSON") },
            text = {
                Column {
                    Text("File JSON cần có định dạng như sau:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text(
                            text = com.skul9x.danhvan.utils.JsonImportManager.getSampleJson(),
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Lưu ý: imageUri và audioUri là tùy chọn.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showJsonGuide = false }) {
                    Text("Đóng")
                }
            }
        )
    }
    
    if (showShopPriceDialog) {
        ShopPriceManagementDialog(
            viewModel = viewModel,
            onDismiss = { showShopPriceDialog = false }
        )
    }
}

@Composable
fun ShopPriceManagementDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val shopItems by viewModel.shopItems.collectAsState()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quản lý Giá Shop") },
        text = {
            LazyColumn(
                modifier = Modifier.height(400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Group by type
                val stickers = shopItems.filter { it.type == com.skul9x.danhvan.data.ItemType.STICKER }
                val themes = shopItems.filter { it.type == com.skul9x.danhvan.data.ItemType.THEME }
                val effects = shopItems.filter { it.type == com.skul9x.danhvan.data.ItemType.EFFECT }
                
                item { Text("Stickers", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary) }
                items(stickers) { item -> ShopPriceItem(item, viewModel) }
                
                item { Spacer(modifier = Modifier.height(8.dp)); Text("Themes", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary) }
                items(themes) { item -> ShopPriceItem(item, viewModel) }
                
                item { Spacer(modifier = Modifier.height(8.dp)); Text("Effects", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary) }
                items(effects) { item -> ShopPriceItem(item, viewModel) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun ShopPriceItem(item: com.skul9x.danhvan.data.ShopItem, viewModel: MainViewModel) {
    var priceText by remember(item.cost) { mutableStateOf(item.cost.toString()) }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text("ID: ${item.id}", style = MaterialTheme.typography.bodySmall)
            }
            
            OutlinedTextField(
                value = priceText,
                onValueChange = { 
                    priceText = it
                    val newPrice = it.toIntOrNull()
                    if (newPrice != null && newPrice >= 0) {
                        viewModel.updateItemPrice(item.id, newPrice)
                    }
                },
                label = { Text("Giá") },
                modifier = Modifier.width(100.dp),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )
        }
    }
}

@Composable
fun WordItem(word: WordEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (word.imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(File(word.imageUri)),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(word.text, style = MaterialTheme.typography.titleMedium)
                Text(
                    "(Từ do phụ huynh thêm)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Xóa")
            }
        }
    }
}

@Composable
fun BackupSection(viewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isBackupInProgress by viewModel.isBackupInProgress.collectAsState()
    val backupMessage by viewModel.backupMessage.collectAsState()
    val userWords by viewModel.userWords.collectAsState()
    val starCount by viewModel.starCount.collectAsState()
    val stickerInventory by viewModel.stickerInventory.collectAsState()
    val placedStickers by viewModel.placedStickers.collectAsState()
    
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var lastBackupInfo by remember { mutableStateOf(viewModel.getLastBackupInfo()) }
    
    // Launcher for restore file picker
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            pendingRestoreUri = it
            showRestoreConfirmDialog = true
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "📦",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Sao lưu & Khôi phục",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    if (lastBackupInfo != null) {
                        Text(
                            "Sao lưu lần cuối: $lastBackupInfo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Current data summary
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DataSummaryItem(
                        icon = "⭐",
                        value = starCount.toString(),
                        label = "Sao"
                    )
                    DataSummaryItem(
                        icon = "🎨",
                        value = stickerInventory.values.sum().toString(),
                        label = "Sticker"
                    )
                    DataSummaryItem(
                        icon = "📍",
                        value = placedStickers.size.toString(),
                        label = "Đã đặt"
                    )
                    DataSummaryItem(
                        icon = "📝",
                        value = userWords.size.toString(),
                        label = "Từ vựng"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.createBackup { result ->
                            if (result is com.skul9x.danhvan.utils.BackupResult.Success) {
                                lastBackupInfo = viewModel.getLastBackupInfo()
                                // Share the backup file using ViewModel (handles errors properly)
                                viewModel.shareBackupFile(result.uri)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isBackupInProgress,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isBackupInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("💾")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sao lưu")
                }
                
                OutlinedButton(
                    onClick = {
                        restoreLauncher.launch("application/zip")
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isBackupInProgress
                ) {
                    Text("📥 Khôi phục")
                }
            }
            
            // Message display
            backupMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("thành công")) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (message.contains("thành công")) "✅" else "❌",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            message,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearBackupMessage() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text("✕", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Help text
            Text(
                "💡 File sao lưu được lưu tại thư mục ứng dụng. Dùng nút Chia sẻ để gửi qua email hoặc lưu vào Drive.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    // Progress Dialog
    if (isBackupInProgress) {
        AlertDialog(
            onDismissRequest = { /* Cannot dismiss while in progress */ },
            title = { Text("Đang xử lý...") },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Vui lòng đợi trong giây lát...")
                }
            },
            confirmButton = {}
        )
    }
    
    // Restore Confirmation Dialog
    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { 
                showRestoreConfirmDialog = false 
                pendingRestoreUri = null
            },
            title = { 
                Text("⚠️ Xác nhận khôi phục") 
            },
            text = {
                Column {
                    Text(
                        "Khôi phục sẽ ghi đè dữ liệu hiện tại với dữ liệu từ file backup.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Dữ liệu hiện tại:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Text("• $starCount ⭐ sao")
                    Text("• ${stickerInventory.values.sum()} sticker trong kho")
                    Text("• ${placedStickers.size} sticker đã đặt")
                    Text("• ${userWords.size} từ vựng tự thêm")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Bạn có chắc muốn tiếp tục?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingRestoreUri?.let { uri ->
                            viewModel.restoreBackup(uri) { result ->
                                lastBackupInfo = viewModel.getLastBackupInfo()
                            }
                        }
                        showRestoreConfirmDialog = false
                        pendingRestoreUri = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Khôi phục")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirmDialog = false
                        pendingRestoreUri = null
                    }
                ) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun DataSummaryItem(
    icon: String,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, style = MaterialTheme.typography.titleLarge)
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

