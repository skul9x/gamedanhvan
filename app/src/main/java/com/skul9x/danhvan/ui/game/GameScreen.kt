package com.skul9x.danhvan.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skul9x.danhvan.ui.MainViewModel
import com.skul9x.danhvan.ui.shop.ShopScreen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import coil.request.ImageRequest
import coil.imageLoader
import com.skul9x.danhvan.util.GoogleImageHelper
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import kotlin.coroutines.cancellation.CancellationException

import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas

@Composable
fun GameScreen(
    viewModel: MainViewModel,
    topicId: String = "all",
    onBack: () -> Unit,
    onOpenStickerBook: () -> Unit = {} // Default empty for compatibility or preview
) {
    // Fetch words based on topic
    val wordsFlow = remember(topicId) { viewModel.getWordsByTopic(topicId) }
    val gameWordsList by wordsFlow.collectAsState()
    
    // Shuffle words once when the list changes
    val gameWords = remember(gameWordsList) { gameWordsList.shuffled() }
    
    val starCount by viewModel.starCount.collectAsState()
    
    var currentWordIndex by remember { mutableStateOf(0) }
    var showShop by remember { mutableStateOf(false) }
    val currentThemeId by viewModel.currentTheme.collectAsState()
    
    // Theme Colors Map
    val themeColors = mapOf(
        "theme_default" to listOf(Color(0xFFE3F2FD), Color(0xFFE0F2F1)),
        "theme_pink" to listOf(Color(0xFFFCE4EC), Color(0xFFF8BBD0)),
        "theme_yellow" to listOf(Color(0xFFFFFDE7), Color(0xFFFFF59D)),
        "theme_dark" to listOf(Color(0xFF263238), Color(0xFF37474F)),
        "theme_forest" to listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
    )
    val currentColors = themeColors[currentThemeId] ?: themeColors["theme_default"]!!

    if (showShop) {
        com.skul9x.danhvan.ui.shop.ShopScreen(
            viewModel = viewModel, 
            onBack = { showShop = false },
            onOpenStickerBook = onOpenStickerBook
        )
        return
    }
    
    if (gameWords.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Chưa có từ nào trong chủ đề này.")
                Button(onClick = onBack) {
                    Text("Quay lại")
                }
            }
        }
        return
    }

    val currentWord = gameWords.getOrNull(currentWordIndex) ?: return

    // --- PRE-LOADING LOGIC ---
    val context = LocalContext.current
    LaunchedEffect(currentWordIndex) {
        val nextIndex = currentWordIndex + 1
        if (nextIndex < gameWords.size) {
            val nextWord = gameWords[nextIndex]
            withContext(Dispatchers.IO) {
                try {
                    val result = GoogleImageHelper.searchImage(nextWord.text)
                    val firstUrl = result.urls.firstOrNull()
                    if (firstUrl != null) {
                        val request = ImageRequest.Builder(context)
                            .data(firstUrl)
                            .build()
                        context.imageLoader.enqueue(request)
                        println("PRE-LOAD STARTED: ${nextWord.text} -> $firstUrl")
                    }
                } catch (e: CancellationException) {
                    throw e // Rethrow for structured concurrency
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = currentColors
                )
            )
    ) {
        // Background Pattern (Nano Banana)
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.skul9x.danhvan.R.drawable.nano_banana_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(0.15f), // Low opacity for subtle effect
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        // Decorative background circles
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.1f),
                radius = size.width * 0.8f,
                center = Offset(size.width * 0.9f, size.height * 0.1f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.1f),
                radius = size.width * 0.6f,
                center = Offset(size.width * 0.1f, size.height * 0.9f)
            )
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Header: Back Button + Star Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween // Push to edges
            ) {
                com.skul9x.danhvan.ui.common.CandyButton(
                    onClick = onBack,
                    color = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                
                // Star Count
                com.skul9x.danhvan.ui.common.CandyButton(
                    onClick = { showShop = true },
                    color = MaterialTheme.colorScheme.tertiary
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(androidx.compose.material.icons.Icons.Default.Star, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$starCount", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

    // Track rewarded words for this session
    val rewardedWordIds = remember { mutableStateListOf<Int>() }

    // Game Content
    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
        // Key the ExploreMode to the word ID to ensure fresh state/animations
        key(currentWord.id) {
            // Pass viewModel for logging
            ExploreMode(
                word = currentWord, 
                onPlayTTS = viewModel::playTTS, 
                initialSeed = currentWord.text.hashCode().toLong(),
                viewModel = viewModel,
                isRewarded = rewardedWordIds.contains(currentWord.id),
                onReward = {
                    if (!rewardedWordIds.contains(currentWord.id)) {
                        rewardedWordIds.add(currentWord.id)
                        viewModel.addStar()
                    }
                }
            )
        }
    }

            // Navigation Buttons (Previous / Next)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp), // Lift up from bottom
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                com.skul9x.danhvan.ui.common.CandyButton(
                    onClick = { if (currentWordIndex > 0) currentWordIndex-- },
                    modifier = Modifier.size(80.dp),
                    color = if (currentWordIndex > 0) Color(0xFFBDBDBD) else Color.LightGray
                ) {
                    Icon(
                        androidx.compose.material.icons.Icons.Default.ArrowBack,
                        contentDescription = "Trước",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                com.skul9x.danhvan.ui.common.CandyButton(
                    onClick = { 
                        if (currentWordIndex < gameWords.size - 1) {
                            // viewModel.addStar() // REMOVED: Only reward for correct voice input
                            currentWordIndex++ 
                        } else {
                            // End of list
                        }
                    },
                    modifier = Modifier.size(80.dp),
                    color = Color(0xFFFF5722) // Deep Orange
                ) {
                    Icon(
                        androidx.compose.material.icons.Icons.Default.ArrowForward,
                        contentDescription = "Tiếp theo",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}
