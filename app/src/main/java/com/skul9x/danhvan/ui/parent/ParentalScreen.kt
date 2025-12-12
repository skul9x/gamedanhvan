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
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importJson(context, it) }
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ... (Rest of UI) ...
        
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
            items(userWords) { word ->
                WordItem(word, onDelete = { viewModel.deleteWord(word) })
            }
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
