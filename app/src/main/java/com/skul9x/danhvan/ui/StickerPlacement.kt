package com.skul9x.danhvan.ui

data class StickerPlacement(
    val id: String, // Unique ID for this placement
    val stickerId: String, // ID of the sticker item (e.g., "sticker_lion")
    val x: Float,
    val y: Float,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val page: Int = 0 // Page index
)
