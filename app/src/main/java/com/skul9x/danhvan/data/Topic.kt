package com.skul9x.danhvan.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Topic(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val imagePath: String? = null
)

object TopicData {
    val topics = listOf(
        Topic("all", "Tất cả", Icons.Default.Star, Color(0xFFE91E63), null)
    )
    
    fun getTopic(id: String): Topic? = topics.find { it.id == id }

    // Palette for dynamic topics
    private val colors = listOf(
        Color(0xFFE91E63), // Pink
        Color(0xFF4CAF50), // Green
        Color(0xFFFF9800), // Orange
        Color(0xFF2196F3), // Blue
        Color(0xFF9C27B0), // Purple
        Color(0xFF00BCD4), // Cyan
        Color(0xFFFFC107), // Amber
        Color(0xFF795548)  // Brown
    )

    fun getColorForCategory(category: String): Color {
        val index = kotlin.math.abs(category.hashCode()) % colors.size
        return colors[index]
    }
}
