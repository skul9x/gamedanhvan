package com.skul9x.danhvan.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- 3D Candy Button ---
@Composable
fun CandyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = RoundedCornerShape(16.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animate press effect (squish down)
    val offsetY by animateDpAsState(if (isPressed) 4.dp else 0.dp)
    val shadowHeight by animateDpAsState(if (isPressed) 0.dp else 4.dp)

    Box(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .width(IntrinsicSize.Min)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        // Shadow Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 4.dp) // Static shadow offset
                .background(color.copy(alpha = 0.5f), shape)
        )
        
        // 3D Side Layer (Darker shade)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = shadowHeight)
                .background(color.copy(red = color.red * 0.8f, green = color.green * 0.8f, blue = color.blue * 0.8f), shape)
        )

        // Top Face Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = offsetY)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.8f), // Highlight
                            color
                        )
                    ),
                    shape = shape
                )
                .border(2.dp, Color.White.copy(alpha = 0.3f), shape)
                .padding(contentPadding),
            contentAlignment = Alignment.Center,
            content = content
        )
        
        // Shine effect
        Box(
            modifier = Modifier
                .offset(y = offsetY + 4.dp, x = 8.dp)
                .width(20.dp)
                .height(8.dp)
                .background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
        )
    }
}

// --- Puzzle Piece Syllable ---
@Composable
fun PuzzlePiece(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondaryContainer,
    textColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    // Simple puzzle shape using a Card with a custom shape or just visual cues
    // For simplicity, let's use a Card with a "connector" visual
    
    Box(modifier = modifier.clickable(onClick = onClick)) {
        // Main Body
        Card(
            colors = CardDefaults.cardColors(containerColor = color),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold, // Updated to ExtraBold
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                    ),
                    color = textColor
                )
            }
        }
        
        // Puzzle Connector (Visual only for now)
        // Right connector
        Canvas(modifier = Modifier.align(Alignment.CenterEnd).offset(x = 6.dp).size(12.dp)) {
            drawCircle(color = color)
        }
        // Left hole (Visual)
        Canvas(modifier = Modifier.align(Alignment.CenterStart).offset(x = (-6).dp).size(12.dp)) {
            drawCircle(color = Color.White.copy(alpha = 0.5f)) // "Hole"
        }
    }
}

// --- Kid Progress Bar ---
@Composable
fun KidProgressBar(
    progress: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .height(24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray.copy(alpha = 0.5f))
    ) {
        val width = maxWidth
        
        // Fill
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
        )
        
        // Star at the end of progress
        // Only show if progress > 0
        if (progress > 0) {
             Icon(
                 imageVector = androidx.compose.material.icons.Icons.Default.Star,
                 contentDescription = null,
                 tint = Color.Yellow,
                 modifier = Modifier
                     .align(Alignment.CenterStart)
                     .offset(x = (width * progress) - 12.dp) // Dynamic offset
                     .size(24.dp)
             )
        }
    }
}

// --- Mascot ---
@Composable
fun Mascot(
    state: MascotState,
    modifier: Modifier = Modifier
) {
    val emoji = when (state) {
        MascotState.IDLE -> "😊" // Happy
        MascotState.THINKING -> "🤔" // Thinking
        MascotState.CORRECT -> "🎉" // Party
        MascotState.WRONG -> "😅" // Sweat
    }
    
    // Bounce animation for Correct
    val infiniteTransition = rememberInfiniteTransition()
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (state == MascotState.CORRECT) -20f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Text(
        text = emoji,
        fontSize = 64.sp,
        modifier = modifier
            .offset(y = bounce.dp)
    )
}

enum class MascotState {
    IDLE, THINKING, CORRECT, WRONG
}
