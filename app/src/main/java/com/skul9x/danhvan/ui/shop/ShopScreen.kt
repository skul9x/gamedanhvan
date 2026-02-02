package com.skul9x.danhvan.ui.shop

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skul9x.danhvan.R
import com.skul9x.danhvan.data.ShopData
import com.skul9x.danhvan.data.ShopItem
import com.skul9x.danhvan.ui.MainViewModel
import com.skul9x.danhvan.ui.common.CandyButton

@Composable
fun ShopScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOpenStickerBook: () -> Unit
) {
    val starCount by viewModel.starCount.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    
    // Feature 2: Purchase Celebration
    var showCelebration by remember { mutableStateOf(false) }
    var celebratedItem by remember { mutableStateOf<ShopItem?>(null) }

    // Feature: Parent Mode
    var isParentMode by remember { mutableStateOf(false) }
    var showGateDialog by remember { mutableStateOf(false) }
    var showAddStickerDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Magical Background
        Image(
            painter = painterResource(id = R.drawable.shop_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 2. Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (Candy Style)
                CandyButton(
                    onClick = onBack,
                    color = Color(0xFFE91E63), // Pink
                    modifier = Modifier.size(48.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                // Title Banner
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF673AB7), Color(0xFF9C27B0))
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .border(2.dp, Color.White, RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Cửa Hàng Sao",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }

                // Actions Row (Book + Stars + Parent Mode)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Parent Mode Toggle
                    CandyButton(
                        onClick = { showGateDialog = true },
                        color = if (isParentMode) Color(0xFF4CAF50) else Color.Gray,
                        modifier = Modifier.size(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            if (isParentMode) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = "Parent Mode",
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    // Book Icon (Open Sticker Book)
                    CandyButton(
                        onClick = {
                            if (isParentMode) {
                                // In parent mode, maybe disable book or allow? Let's allow.
                                onOpenStickerBook()
                            } else {
                                onOpenStickerBook()
                            }
                        },
                        color = Color(0xFF2196F3), // Blue
                        modifier = Modifier.size(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        // Using MenuBook icon for "Open Sticker Book"
                        Icon(Icons.Default.MenuBook, contentDescription = "Sticker Book", tint = Color.White) 
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    // Star Counter
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFFFC107), // Amber
                                shape = RoundedCornerShape(24.dp)
                            )
                            .border(2.dp, Color.White, RoundedCornerShape(24.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$starCount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // 3. Shop Grid (Stickers Only)
            val shopItems by viewModel.shopItems.collectAsState()
            val filteredItems = shopItems.filter { it.type == com.skul9x.danhvan.data.ItemType.STICKER }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredItems) { item ->
                    ShopItemCard(
                        item = item,
                        isOwned = inventory.contains(item.id), // Or check stickerInventory if needed, but inventory is fine for "owned" status
                        isEquipped = false,
                        canAfford = starCount >= item.cost,
                        isParentMode = isParentMode,
                        onBuy = { 
                            viewModel.purchaseItem(item)
                            if (starCount >= item.cost) {
                                celebratedItem = item
                                showCelebration = true
                            }
                        },
                        onEquip = { },
                        onDelete = { viewModel.deleteShopItem(item) }
                    )
                }
            }
        }
        
        // Add Sticker FAB
        if (isParentMode) {
            FloatingActionButton(
                onClick = { showAddStickerDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = Color(0xFFE91E63),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Sticker")
            }
        }
        
        // Dialogs
        if (showGateDialog) {
            MathGateDialog(
                onDismiss = { showGateDialog = false },
                onSuccess = {
                    isParentMode = !isParentMode
                    showGateDialog = false
                }
            )
        }
        
        if (showAddStickerDialog) {
            AddStickerDialog(
                onDismiss = { showAddStickerDialog = false },
                onAdd = { name, cost, uri ->
                    viewModel.addCustomSticker(name, cost, uri, context)
                    showAddStickerDialog = false
                }
            )
        }
        
        // Celebration Dialog
        if (showCelebration && celebratedItem != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable { showCelebration = false },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CHÚC MỪNG!", style = MaterialTheme.typography.headlineLarge, color = Color.Yellow, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Bạn đã sở hữu:", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text(celebratedItem!!.name, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Render the item preview
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(celebratedItem!!.color1), Color(celebratedItem!!.color2))
                                )
                            )
                            .border(4.dp, Color.White, RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Handle custom stickers with imageUri vs built-in stickers with resourceId
                        if (celebratedItem!!.imageUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(celebratedItem!!.imageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = celebratedItem!!.name,
                                modifier = Modifier.size(150.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else if (celebratedItem!!.resourceId != 0) {
                            Image(
                                painter = painterResource(id = celebratedItem!!.resourceId),
                                contentDescription = null,
                                modifier = Modifier.size(150.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = { showCelebration = false }) {
                        Text("Tuyệt vời!")
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFFFFC107) else Color.LightGray
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text, color = if (isSelected) Color.Black else Color.DarkGray, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ShopItemCard(
    item: ShopItem,
    isOwned: Boolean,
    isEquipped: Boolean,
    canAfford: Boolean,
    isParentMode: Boolean = false,
    onBuy: () -> Unit,
    onEquip: () -> Unit,
    onDelete: () -> Unit = {}
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f)
    
    // Feature 1: Interactive Preview
    var previewTrigger by remember { mutableStateOf(0L) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (isOwned) onEquip() else if (canAfford) onBuy()
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(item.color1), Color(item.color2))
                        )
                    )
                    .border(4.dp, if (isEquipped) Color(0xFF4CAF50) else Color.Transparent, RoundedCornerShape(16.dp))
                    .clickable { 
                        // Tap to preview effect
                        if (item.type == com.skul9x.danhvan.data.ItemType.EFFECT) {
                            previewTrigger = System.currentTimeMillis()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isEquipped) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Equipped",
                        tint = Color.White,
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF4CAF50), CircleShape)
                            .padding(8.dp)
                    )
                }
                
                // Icon for effects
                if (item.type == com.skul9x.danhvan.data.ItemType.EFFECT) {
                    Icon(
                        Icons.Default.Star, // Placeholder for effect icon
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(48.dp)
                    )
                    
                    // Render Particle System for Preview
                    val type = when (item.id) {
                        "effect_sparkle" -> com.skul9x.danhvan.ui.common.ParticleType.SPARKLE
                        "effect_bubble" -> com.skul9x.danhvan.ui.common.ParticleType.BUBBLE
                        "effect_heart" -> com.skul9x.danhvan.ui.common.ParticleType.HEART
                        else -> null
                    }
                    if (type != null) {
                        com.skul9x.danhvan.ui.common.ParticleSystem(
                            type = type,
                            trigger = previewTrigger,
                            intensity = 0.5f // Lower intensity for small card
                        )
                    }
                } else if (item.type == com.skul9x.danhvan.data.ItemType.STICKER) {
                    if (item.imageUri != null) {
                         AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.name,
                            modifier = Modifier.size(60.dp),
                            contentScale = ContentScale.Fit,
                            placeholder = painterResource(R.drawable.ic_launcher_foreground), // Placeholder
                            error = painterResource(R.drawable.ic_cross_wrong) // Error image
                        )
                    } else {
                        Image(
                            painter = painterResource(id = item.resourceId),
                            contentDescription = item.name,
                            modifier = Modifier.size(60.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            // Parent Mode Controls
            if (isParentMode) {
                 Row(
                     modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                     horizontalArrangement = Arrangement.Center
                 ) {
                     IconButton(onClick = onDelete) {
                         Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                     }
                 }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Price Display (Requested by User)
            if (!isOwned) {
                 Row(
                     verticalAlignment = Alignment.CenterVertically,
                     modifier = Modifier.padding(bottom = 4.dp)
                 ) {
                     Icon(
                         Icons.Default.Star, 
                         contentDescription = null, 
                         tint = Color(0xFFFFC107), // Amber
                         modifier = Modifier.size(20.dp)
                     )
                     Spacer(modifier = Modifier.width(4.dp))
                     Text(
                         text = "${item.cost}",
                         style = MaterialTheme.typography.titleMedium,
                         fontWeight = FontWeight.Bold,
                         color = Color(0xFFFFC107)
                     )
                 }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Button
            if (isOwned) {
                if (item.type == com.skul9x.danhvan.data.ItemType.STICKER) {
                     Button(
                        onClick = { /* Do nothing or show info */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Đã sở hữu")
                    }
                } else {
                    Button(
                        onClick = onEquip,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEquipped) Color(0xFF4CAF50) else Color(0xFF2196F3)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isEquipped) "Đang dùng" else "Sử dụng")
                    }
                }
            } else {
                Button(
                    onClick = onBuy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAfford) Color(0xFFFF9800) else Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = canAfford
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${item.cost}")
                }
            }
        }
    }
}

@Composable
fun MathGateDialog(onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var answer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    
    // Simple math problem: 12 + 5
    val correctAnswer = "17"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dành cho phụ huynh") },
        text = {
            Column {
                Text("Vui lòng giải bài toán sau để tiếp tục:")
                Spacer(modifier = Modifier.height(8.dp))
                Text("12 + 5 = ?", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it; error = false },
                    label = { Text("Kết quả") },
                    singleLine = true,
                    isError = error
                )
                if (error) {
                    Text("Sai rồi!", color = Color.Red)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (answer.trim() == correctAnswer) {
                    onSuccess()
                } else {
                    error = true
                }
            }) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun AddStickerDialog(onDismiss: () -> Unit, onAdd: (String, Int, android.net.Uri) -> Unit) {
    var name by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("25") }
    var selectedUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
        selectedUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm Sticker Mới") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên Sticker") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = cost,
                    onValueChange = { if (it.all { char -> char.isDigit() }) cost = it },
                    label = { Text("Giá (Sao)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Image Picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedUri != null) {
                        AsyncImage(
                            model = selectedUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Chọn Ảnh")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && cost.isNotBlank() && selectedUri != null) {
                        onAdd(name, cost.toIntOrNull() ?: 50, selectedUri!!)
                    }
                },
                enabled = name.isNotBlank() && cost.isNotBlank() && selectedUri != null
            ) {
                Text("Thêm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
