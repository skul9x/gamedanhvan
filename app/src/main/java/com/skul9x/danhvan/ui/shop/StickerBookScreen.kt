package com.skul9x.danhvan.ui.shop

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.forEachGesture
// import androidx.compose.ui.input.pointer.util.move -- Removed invalid import
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.skul9x.danhvan.data.ShopData
import com.skul9x.danhvan.ui.MainViewModel
import com.skul9x.danhvan.ui.StickerPlacement
import com.skul9x.danhvan.ui.common.CandyButton
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun StickerBookScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val placedStickers by viewModel.placedStickers.collectAsState()
    val stickerInventory by viewModel.stickerInventory.collectAsState()
    val currentPage by viewModel.currentStickerPage.collectAsState()
    val shopItems by viewModel.shopItems.collectAsState()
    
    // Filter owned stickers (count > 0) - include both built-in and custom stickers
    val ownedStickers = shopItems.filter { 
        it.type == com.skul9x.danhvan.data.ItemType.STICKER && (stickerInventory[it.id] ?: 0) > 0
    }

    var selectedStickerId by remember { mutableStateOf<String?>(null) }
    
    // Animation State
    val scope = rememberCoroutineScope()
    var flyingStickerItem by remember { mutableStateOf<com.skul9x.danhvan.data.ShopItem?>(null) }
    val flyingStickerAnim = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var startPos by remember { mutableStateOf(Offset.Zero) }
    
    // Double-tap Prev button to recall all stickers (when on page 0)
    var lastPrevTapTime by remember { mutableStateOf(0L) }
    var showRecallConfirmation by remember { mutableStateOf(false) }
    
    // Canvas dimensions for centering
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Notebook Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Paper Color
            drawRect(color = Color(0xFFFFFDE7)) // Cream/Paper color

            val lineSpacing = 40.dp.toPx()
            val headerHeight = 80.dp.toPx()
            
            // Horizontal Lines (Blue)
            for (y in headerHeight.toInt() until size.height.toInt() step lineSpacing.toInt()) {
                drawLine(
                    color = Color(0xFFBBDEFB),
                    start = Offset(0f, y.toFloat()),
                    end = Offset(size.width, y.toFloat()),
                    strokeWidth = 2f
                )
            }
            
            // Vertical Margin Line (Red)
            drawLine(
                color = Color(0xFFFFCDD2),
                start = Offset(60.dp.toPx(), 0f),
                end = Offset(60.dp.toPx(), size.height),
                strokeWidth = 4f
            )
            
            // Holes (Binding)
            val holeRadius = 8.dp.toPx()
            val holeSpacing = 60.dp.toPx()
            for (y in 100 until size.height.toInt() step holeSpacing.toInt()) {
                drawCircle(
                    color = Color(0xFFEEEEEE),
                    radius = holeRadius,
                    center = Offset(20.dp.toPx(), y.toFloat())
                )
            }
        }

        // 2. Sticker Area (Canvas) - Filtered by Page
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 140.dp) // Space for bottom bar
                .onGloballyPositioned { canvasSize = it.size }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        // Consume drags on background to prevent weird scrolling if any
                    }
                }
                .clickable { selectedStickerId = null } // Deselect on background tap
        ) {
            placedStickers.filter { it.page == currentPage }.forEach { placed ->
                // Find sticker in shopItems (includes custom stickers)
                val shopItem = shopItems.find { it.id == placed.stickerId }
                if (shopItem != null) {
                    StickerItem(
                        item = shopItem,
                        placedSticker = placed,
                        isSelected = selectedStickerId == placed.id,
                        canvasBounds = canvasSize, // Pass canvas size for bounds checking
                        onSelect = { selectedStickerId = placed.id },
                        onUpdate = { x, y, scale, rot ->
                            viewModel.updateSticker(placed.id, x, y, scale, rot)
                        },
                        onInteractionEnd = {
                            viewModel.saveStickerPlacements()
                        }
                    )
                }
            }
        }
        
        // Flying Sticker Animation Overlay
        if (flyingStickerItem != null) {
            if (flyingStickerItem!!.imageUri != null) {
                coil.compose.AsyncImage(
                    model = flyingStickerItem!!.imageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .offset { IntOffset(flyingStickerAnim.value.x.toInt(), flyingStickerAnim.value.y.toInt()) },
                    contentScale = ContentScale.Fit
                )
            } else if (flyingStickerItem!!.resourceId != 0) {
                Image(
                    painter = painterResource(id = flyingStickerItem!!.resourceId),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .offset { IntOffset(flyingStickerAnim.value.x.toInt(), flyingStickerAnim.value.y.toInt()) }
                )
            }
        }

        // 3. Header with Pagination
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header + Pagination Merged
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                CandyButton(
                    onClick = onBack,
                    color = Color(0xFFE91E63),
                    modifier = Modifier.size(48.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Pagination Controls (Centered)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Arrow - Double tap on page 0 to recall all stickers
                    CandyButton(
                         onClick = { 
                             if (currentPage > 0) {
                                 viewModel.setStickerPage(currentPage - 1)
                                 lastPrevTapTime = 0L // Reset double-tap timer when navigating
                             } else {
                                 // On page 0: detect double-tap for recall
                                 val currentTime = System.currentTimeMillis()
                                 if (currentTime - lastPrevTapTime < 500) {
                                     // Double tap detected! Show confirmation
                                     showRecallConfirmation = true
                                     lastPrevTapTime = 0L
                                 } else {
                                     lastPrevTapTime = currentTime
                                 }
                             }
                         },
                         color = Color.Transparent,
                         contentPadding = PaddingValues(0.dp)
                    ) {
                        Image(
                            painter = painterResource(id = com.skul9x.danhvan.R.drawable.arrow_left),
                            contentDescription = "Prev",
                            modifier = Modifier
                                 .size(60.dp)
                                 .alpha(if (currentPage > 0) 1f else 0.5f)
                        )
                    }
                    
                    // Page Number
                    Text(
                        "${currentPage + 1}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF5D4037),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    
                    // Right Arrow
                    CandyButton(
                         onClick = { viewModel.setStickerPage(currentPage + 1) },
                         color = Color.Transparent,
                         contentPadding = PaddingValues(0.dp)
                    ) {
                        Image(
                            painter = painterResource(id = com.skul9x.danhvan.R.drawable.arrow_right),
                            contentDescription = "Next",
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))
                
                // Delete Button
                Box(modifier = Modifier.size(48.dp)) {
                    if (selectedStickerId != null) {
                        CandyButton(
                            onClick = { 
                                viewModel.removeSticker(selectedStickerId!!)
                                selectedStickerId = null
                            },
                            color = Color.Red,
                            modifier = Modifier.size(48.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    }
                }
            }
        }

        // 4. Bottom Bar (Sticker Picker)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(140.dp)
                .background(Color.White)
        ) {
            Divider(color = Color.LightGray, thickness = 2.dp)
            Text(
                "Sticker của bé (${ownedStickers.size})",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(8.dp),
                color = Color.Gray
            )
            
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(ownedStickers) { item ->
                    var itemPosition by remember { mutableStateOf(Offset.Zero) }
                    
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .onGloballyPositioned { coordinates ->
                                itemPosition = coordinates.positionInRoot()
                            }
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(item.color1).copy(alpha = 0.3f))
                            .clickable {
                                // Trigger Animation
                                scope.launch {
                                    // Validate canvasSize before calculating random position
                                    val minWidth = 300
                                    val minHeight = 500
                                    
                                    if (canvasSize.width < minWidth || canvasSize.height < minHeight) {
                                        // Canvas not ready or too small, place at safe default position
                                        val safeX = 150f
                                        val safeY = 200f
                                        viewModel.placeSticker(item.id, safeX, safeY)
                                        return@launch
                                    }
                                    
                                    flyingStickerItem = item
                                    startPos = itemPosition
                                    
                                    // Target: Random position on the page with safe margins
                                    // Ensure we have positive ranges for random calculation
                                    val horizontalMargin = 150
                                    val topMargin = 150
                                    val bottomMargin = 200
                                    
                                    val availableWidth = (canvasSize.width - horizontalMargin * 2).coerceAtLeast(100)
                                    val availableHeight = (canvasSize.height - topMargin - bottomMargin).coerceAtLeast(100)
                                    
                                    val targetX = Random.nextFloat() * availableWidth + horizontalMargin
                                    val targetY = Random.nextFloat() * availableHeight + topMargin
                                    
                                    flyingStickerAnim.snapTo(startPos)
                                    flyingStickerAnim.animateTo(
                                        targetValue = Offset(targetX, targetY),
                                        animationSpec = tween(durationMillis = 600)
                                    )
                                    
                                    // Place Sticker
                                    viewModel.placeSticker(item.id, targetX, targetY)
                                    flyingStickerItem = null
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Handle custom stickers with imageUri
                        if (item.imageUri != null) {
                            coil.compose.AsyncImage(
                                model = item.imageUri,
                                contentDescription = item.name,
                                modifier = Modifier.size(60.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else if (item.resourceId != 0) {
                            Image(
                                painter = painterResource(id = item.resourceId),
                                contentDescription = item.name,
                                modifier = Modifier.size(60.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        
                        // Quantity Badge
                        val count = stickerInventory[item.id] ?: 0
                        if (count > 1) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(20.dp)
                                    .background(Color.Red, androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$count",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                if (ownedStickers.isEmpty()) {
                    item {
                        Text(
                            "Chưa có sticker nào. Hãy vào Cửa Hàng để mua nhé!",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray
                        )
                    }
                }
            }
        }
        
        // Recall Confirmation Dialog
        if (showRecallConfirmation) {
            val totalStickers = placedStickers.size
            val totalPages = if (placedStickers.isEmpty()) 0 else placedStickers.maxOf { it.page } + 1
            
            AlertDialog(
                onDismissRequest = { showRecallConfirmation = false },
                title = { 
                    Text(
                        "Thu hồi tất cả sticker?",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = { 
                    Text("Bạn có muốn thu hồi tất cả $totalStickers sticker từ $totalPages trang về kho không?\n\nTất cả sticker sẽ được trả về \"Sticker của bé\" để bạn sắp xếp lại từ đầu.")
                },
                confirmButton = {
                    CandyButton(
                        onClick = {
                            viewModel.recallAllStickers() // Recall from ALL pages
                            selectedStickerId = null
                            showRecallConfirmation = false
                        },
                        color = Color(0xFF4CAF50)
                    ) {
                        Text("Thu hồi tất cả", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    CandyButton(
                        onClick = { showRecallConfirmation = false },
                        color = Color.Gray
                    ) {
                        Text("Hủy", color = Color.White)
                    }
                }
            )
        }
    }
}

@Composable
fun StickerItem(
    item: com.skul9x.danhvan.data.ShopItem,
    placedSticker: StickerPlacement,
    isSelected: Boolean,
    canvasBounds: androidx.compose.ui.unit.IntSize, // Canvas size for bounds checking
    onSelect: () -> Unit,
    onUpdate: (Float, Float, Float, Float) -> Unit,
    onInteractionEnd: () -> Unit
) {
    // Sticker visual size in pixels
    val density = LocalDensity.current
    val stickerSizePx = with(density) { 115.dp.toPx() }
    
    // Use key to force re-initialization when sticker ID changes
    // This ensures loading correct position from storage when app restarts
    var offsetX by remember(placedSticker.id) { mutableStateOf(placedSticker.x) }
    var offsetY by remember(placedSticker.id) { mutableStateOf(placedSticker.y) }
    var scale by remember(placedSticker.id) { mutableStateOf(placedSticker.scale) }
    var rotation by remember(placedSticker.id) { mutableStateOf(placedSticker.rotation) }

    // Helper function to clamp position within bounds
    fun clampPosition(x: Float, y: Float, currentScale: Float): Pair<Float, Float> {
        val scaledSize = stickerSizePx * currentScale
        val headerHeight = with(density) { 80.dp.toPx() } // Space for header
        
        val minX = 0f
        val maxX = (canvasBounds.width - scaledSize).coerceAtLeast(0f)
        val minY = headerHeight
        val maxY = (canvasBounds.height - scaledSize).coerceAtLeast(headerHeight)
        
        return Pair(
            x.coerceIn(minX, maxX),
            y.coerceIn(minY, maxY)
        )
    }

    // Sync with external state when placedSticker data changes (e.g., after restore)
    // Using individual properties as keys for more precise updates
    LaunchedEffect(placedSticker.x, placedSticker.y, placedSticker.scale, placedSticker.rotation) {
        offsetX = placedSticker.x
        offsetY = placedSticker.y
        scale = placedSticker.scale
        rotation = placedSticker.rotation
    }

    // Use rememberUpdatedState to avoid stale closures in pointerInput
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentOnUpdate by rememberUpdatedState(onUpdate)
    val currentOnInteractionEnd by rememberUpdatedState(onInteractionEnd)

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                rotationZ = rotation
            )
            .pointerInput(placedSticker.id, canvasBounds) { // Also key by canvasBounds
                // Combined Gesture Detector with Action Up detection for saving
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume() // IMPORTANT: Consume immediately to prevent propagation to parent
                        currentOnSelect() // Select on touch down
                        
                        var zoom = 1f
                        var pan = Offset.Zero
                        var rot = 0f
                        var hasInteracted = false
                        
                        do {
                            val event = awaitPointerEvent()
                            val canceled = event.changes.any { it.isConsumed }
                            if (!canceled) {
                                val zoomChange = event.calculateZoom()
                                val rotationChange = event.calculateRotation()
                                val panChange = event.calculatePan()
                                
                                if (zoomChange != 1f || rotationChange != 0f || panChange != Offset.Zero) {
                                    hasInteracted = true
                                    zoom *= zoomChange
                                    rot += rotationChange
                                    pan += panChange
                                    
                                    // Apply changes
                                    scale *= zoomChange
                                    rotation += rotationChange
                                    // Limit scale to prevent microscopic or giant stickers
                                    scale = scale.coerceIn(0.5f, 3.0f) // Reduced max to prevent overflow
                                    
                                    // Calculate new position with bounds checking
                                    val newX = offsetX + panChange.x
                                    val newY = offsetY + panChange.y
                                    val (clampedX, clampedY) = clampPosition(newX, newY, scale)
                                    offsetX = clampedX
                                    offsetY = clampedY
                                    
                                    currentOnUpdate(offsetX, offsetY, scale, rotation)
                                }
                                
                                // ALWAYS consume all events to prevent propagation to parent
                                event.changes.forEach { it.consume() }
                            }
                        } while (event.changes.any { it.pressed })
                        
                        // All pointers up - only save if there was actual interaction
                        if (hasInteracted) {
                            currentOnInteractionEnd()
                        }
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .size(115.dp)
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) Color.Blue else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Handle custom stickers with imageUri
            if (item.imageUri != null) {
                coil.compose.AsyncImage(
                    model = item.imageUri,
                    contentDescription = item.name,
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Fit
                )
            } else if (item.resourceId != 0) {
                val painter = painterResource(id = item.resourceId)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawIntoCanvas { canvas ->
                        val paint = androidx.compose.ui.graphics.Paint().apply {
                            blendMode = androidx.compose.ui.graphics.BlendMode.Multiply
                        }
                        canvas.saveLayer(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height), paint)
                        with(painter) {
                            draw(size)
                        }
                        canvas.restore()
                    }
                }
            }
        }
    }
}
