package com.skul9x.danhvan.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.skul9x.danhvan.data.WordEntity
import com.skul9x.danhvan.ui.MainViewModel
import com.skul9x.danhvan.ui.common.BouncingButton
import com.skul9x.danhvan.ui.common.EntranceAnimation
import kotlinx.coroutines.delay
import java.io.File

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.drawscope.Stroke

// --- Mode 1: Khám Phá (Explore) ---
enum class FeedbackType { NONE, CORRECT, WRONG }

// State Machine for animation (fixes merge animation stuck bug)
enum class AnimationPhase {
    IDLE,           // Show syllables
    MERGING,        // Animation 0→1
    SHOWING_RESULT  // Show merged word + feedback
}

data class GameAnimationState(
    val phase: AnimationPhase = AnimationPhase.IDLE,
    val feedbackType: FeedbackType = FeedbackType.NONE
)

@Composable
fun ExploreMode(
    word: WordEntity, 
    onPlayTTS: (String) -> Unit, 
    initialSeed: Long = System.currentTimeMillis(),
    viewModel: com.skul9x.danhvan.ui.MainViewModel,
    isRewarded: Boolean,
    onReward: () -> Unit
) {
    // Default to true for internet image
    var showInternetImage by remember { mutableStateOf(true) }
    
    // Seed for randomizing image (start with deterministic seed for cache hit)
    var imageSeed by remember { mutableStateOf(initialSeed) }
    
    // Progress state
    var isLoading by remember { mutableStateOf(false) }
    
    // Image URL state
    var googleImageUrls by remember(word.text) { mutableStateOf<List<String>>(emptyList()) }
    var currentUrlIndex by remember(word.text) { mutableStateOf(0) }
    var isSearching by remember(word.text) { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Use Coil singleton ImageLoader (prevents Resource Leak from creating new OkHttpClient per recomposition)
    val imageLoader = coil.Coil.imageLoader(context)
    
    // Trigger Google Search when word changes or refresh is clicked
    LaunchedEffect(word.text, imageSeed) {
        isSearching = true
        isLoading = true
        
        // Clear previous log for new word, append for refresh
        if (imageSeed == initialSeed) {
             viewModel.appendLog("\n--> NEW WORD: '${word.text}'\n")
        } else {
             viewModel.appendLog("\n--> REFRESH: '${word.text}'\n")
        }
        
        // Fix: Move network call to IO dispatcher to avoid blocking Main Thread
        val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            com.skul9x.danhvan.util.GoogleImageHelper.searchImage(word.text)
        }
        googleImageUrls = result.urls.orEmpty()  // Null-safe (defensive programming)
        viewModel.appendLog(result.log ?: "")    // Null-safe
        
        // Randomize start index (3 to 8)
        if (result.urls.isNotEmpty()) {
            val maxIndex = kotlin.math.min(8, result.urls.size - 1)
            currentUrlIndex = if (maxIndex >= 3) {
                (3..maxIndex).random()
            } else {
                0
            }
            viewModel.appendLog("--> Selected Random Start Index: $currentUrlIndex (Range: 3-$maxIndex)\n")
        } else {
            currentUrlIndex = 0
        }
        
        isSearching = false
        // Don't set isLoading = false here, wait for Coil to load or fail
        if (result.urls.isEmpty()) {
            isLoading = false
        }
    }
    
    val currentImageUrl = googleImageUrls.getOrNull(currentUrlIndex)
    
    // State for lifecycle-safe retry trigger (Bug fix: Improper Error Handling)
    var retryTrigger by remember { mutableStateOf(0) }
    
    // Handle retry in lifecycle-safe way via LaunchedEffect
    LaunchedEffect(retryTrigger) {
        if (retryTrigger > 0 && currentUrlIndex < googleImageUrls.size - 1) {
            currentUrlIndex++
            viewModel.appendLog("--> RETRYING with next URL (Index: $currentUrlIndex)...\n")
        }
    }

    val imageRequest = remember(currentImageUrl) {
        if (currentImageUrl.isNullOrEmpty()) null
        else {
            coil.request.ImageRequest.Builder(context)
                .data(currentImageUrl)
                .listener(
                    onStart = {
                        isLoading = true
                        viewModel.appendLog("--> COIL START: Loading $currentImageUrl\n")
                    },
                    onSuccess = { _, _ -> 
                        isLoading = false
                        viewModel.appendLog("<-- COIL SUCCESS: Image loaded!\n")
                    },
                    onError = { _, result ->
                        viewModel.appendLog("<-- COIL ERROR: ${result.throwable.message}\n")
                        result.throwable.printStackTrace()
                        
                        // Trigger retry via state (lifecycle-safe)
                        if (currentUrlIndex < googleImageUrls.size - 1) {
                            retryTrigger++
                        } else {
                            isLoading = false
                            viewModel.appendLog("--> ALL URLS FAILED.\n")
                        }
                    }
                )
                .crossfade(true)
                .build()
        }
    }
    
    // State for zoomed image
    var zoomedImage by remember { mutableStateOf<Any?>(null) } // Can be String (URL) or File

    // Auto-close popup after 2 seconds
    LaunchedEffect(zoomedImage) {
        if (zoomedImage != null) {
            kotlinx.coroutines.delay(2000)
            zoomedImage = null
        }
    }

    // --- STATE MACHINE (replaces feedbackState + isMerging) ---
    var animState by remember(word.text) { mutableStateOf(GameAnimationState()) }
    val mergeAnim = remember(word.text) { Animatable(0f) }
    
    // Race condition guard - locks result processing after match
    var isResultLocked by remember(word.text) { mutableStateOf(false) }
    
    // Double-reward prevention with rememberUpdatedState
    val currentIsRewarded by rememberUpdatedState(isRewarded)
    var hasLocallyRewarded by remember(word.text) { mutableStateOf(false) }
    
    // Unified Animation Controller (fixes merge animation stuck bug)
    LaunchedEffect(animState.phase) {
        when (animState.phase) {
            AnimationPhase.IDLE -> {
                mergeAnim.snapTo(0f)
            }
            AnimationPhase.MERGING -> {
                // Phase 1: Merge Animation
                mergeAnim.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
                
                // Phase 2: Play TTS
                onPlayTTS(word.text)
                
                // Phase 3: Transition to SHOWING_RESULT
                animState = animState.copy(phase = AnimationPhase.SHOWING_RESULT)
            }
            AnimationPhase.SHOWING_RESULT -> {
                // Show result for 1500ms then reset
                delay(1500)
                animState = GameAnimationState() // Full reset to IDLE
            }
        }
    }
    
    // Helper to play sound (uses SoundManager singleton to prevent resource leak)
    fun playSound(resId: Int) {
        com.skul9x.danhvan.util.SoundManager.play(context, resId)
    }
    
    // Safe reward function with double protection
    fun safeReward(): Boolean {
        if (currentIsRewarded || hasLocallyRewarded) {
            viewModel.appendLog("--> REWARD BLOCKED: Already rewarded\n")
            return false
        }
        hasLocallyRewarded = true
        onReward()
        viewModel.appendLog("--> REWARD SUCCESS!\n")
        return true
    }
    
    // Trigger functions for animation
    fun triggerSuccessAnimation() {
        if (animState.phase == AnimationPhase.IDLE) {
            animState = GameAnimationState(
                phase = AnimationPhase.MERGING,
                feedbackType = FeedbackType.CORRECT
            )
        }
    }
    
    fun triggerFailureAnimation() {
        if (animState.phase == AnimationPhase.IDLE) {
            animState = GameAnimationState(
                phase = AnimationPhase.SHOWING_RESULT,
                feedbackType = FeedbackType.WRONG
            )
        }
    }

    // --- VOICE RECOGNITION ---
    val speechRecognizerHelper = remember(context) { com.skul9x.danhvan.util.SpeechRecognizerHelper(context) }
    // Voice Recognition state
    val isListening by speechRecognizerHelper.isListening.collectAsState()
    val result by speechRecognizerHelper.result.collectAsState()
    val partialResult by speechRecognizerHelper.partialResult.collectAsState()
    val error by speechRecognizerHelper.error.collectAsState()
    
    // Check results (Final & Partial) with State Machine guards
    LaunchedEffect(result, partialResult) {
        // Guard 1: Already locked (processing previous match)
        if (isResultLocked) {
            viewModel.appendLog("--> IGNORED: Result locked\\n")
            return@LaunchedEffect
        }
        
        val currentResult = result ?: partialResult
        
        // Guard 2: No result
        if (currentResult.isNullOrEmpty()) return@LaunchedEffect
        
        // Guard 3: Animation in progress
        if (animState.phase != AnimationPhase.IDLE) {
            viewModel.appendLog("--> IGNORED: Animation in progress\\n")
            return@LaunchedEffect
        }
        
        val spoken = currentResult.lowercase()
        val target = word.text.lowercase()
        
        // Normalize: remove spaces, punctuation, BUT KEEP ACCENTS. Special handle 'đ' -> 'd'
        fun normalize(s: String): String {
            var temp = s.lowercase()
            
            // --- SPECIAL DICTIONARY (Handle Loanwords) ---
            temp = temp.replace("pi a nô", "piano")
                       .replace("ghi ta", "guitar")
                       .replace("vi rút", "virus")
                       
            // 1. Handle 'đ' manually 
            temp = temp.replace("đ", "d")
            
            // 2. Remove non-letter/digit chars but KEEPS ACCENTS
            return temp.filter { it.isLetterOrDigit() }
        }
        
        val spokenClean = normalize(spoken)
        val targetClean = normalize(target)
        
        // Check for exact match or contains
        val isMatch = (spokenClean.isNotEmpty() && targetClean.contains(spokenClean)) || 
                      (targetClean.isNotEmpty() && spokenClean.contains(targetClean))

        if (isMatch) {
            // LOCK IMMEDIATELY to prevent race condition
            isResultLocked = true
            speechRecognizerHelper.stopListening()
            
            viewModel.appendLog("--> SPEECH MATCH: '$spoken' ($spokenClean) ~ '${word.text}' ($targetClean)\\n")
            
            // Small delay for smooth UX
            delay(300)
            
            // Use safe reward with double protection
            val rewarded = safeReward()
            
            if (rewarded) {
                triggerSuccessAnimation()
                playSound(com.skul9x.danhvan.R.raw.correct)
            } else {
                android.widget.Toast.makeText(context, "Bạn đã nhận sao cho từ này rồi!", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else if (result != null && !isResultLocked) {
            // Only show wrong for FINAL result and not locked
            isResultLocked = true
            
            viewModel.appendLog("--> MISMATCH: Expected '${word.text}' ($targetClean), Heard '$spoken' ($spokenClean)\\n")
            
            triggerFailureAnimation()
            playSound(com.skul9x.danhvan.R.raw.wrong)
            
            // Unlock after animation completes
            delay(1500)
            isResultLocked = false
        }
    }
    
    // Permission State
    var hasMicPermission by remember { mutableStateOf(false) }
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasMicPermission = isGranted }
    )
    
    // Check permission on start
    LaunchedEffect(Unit) {
        hasMicPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    
    // Old Speech Result logic removed as it's now handled by the new block above.
    
    // Cleanup
    DisposableEffect(Unit) {
        onDispose { speechRecognizerHelper.destroy() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(24.dp), // Rounder shape
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.clickable {
                     if (showInternetImage) {
                         if (currentImageUrl != null) zoomedImage = currentImageUrl
                     } else {
                         if (word.imageUri != null) zoomedImage = File(word.imageUri)
                     }
                }
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    if (showInternetImage) {
                        if (isSearching) {
                             Box(modifier = Modifier.size(250.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                }
                            }
                        } else if (currentImageUrl == null) {
                            Box(modifier = Modifier.size(250.dp), contentAlignment = Alignment.Center) {
                                 Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Close, contentDescription = "Error", tint = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Không tìm thấy hình ảnh",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Nhấn nút làm mới để thử lại",
                                        color = Color.LightGray,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            coil.compose.SubcomposeAsyncImage(
                                model = imageRequest,
                                imageLoader = imageLoader,
                                contentDescription = word.text,
                                modifier = Modifier
                                    .size(250.dp)
                                    .clip(RoundedCornerShape(32.dp))
                                    .border(4.dp, Color.White, RoundedCornerShape(32.dp)),
                                contentScale = ContentScale.Crop,
                                loading = {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                },
                                error = {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Close, contentDescription = "Error", tint = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.size(48.dp))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Lỗi tải hình",
                                                color = Color.Gray,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    } else {
                        // Local image mode - null-safe check
                        val localImageFile = word.imageUri?.let { File(it) }
                        if (localImageFile != null && localImageFile.exists()) {
                            Image(
                                painter = rememberAsyncImagePainter(localImageFile),
                                contentDescription = word.text,
                                modifier = Modifier
                                    .size(250.dp)
                                    .clip(RoundedCornerShape(32.dp))
                                    .border(4.dp, Color.White, RoundedCornerShape(32.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Placeholder when no local image
                            Box(
                                modifier = Modifier
                                    .size(250.dp)
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Close, contentDescription = "No Image", tint = Color.Gray, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Chưa có ảnh",
                                        color = Color.DarkGray,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                    
                    // Refresh Button (Top Right)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(40.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color.White.copy(alpha = 0.7f))
                            .clickable {
                                if (showInternetImage) {
                                    imageSeed = System.currentTimeMillis()
                                } else {
                                    showInternetImage = true 
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Refresh, 
                            contentDescription = "Refresh Image",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // Full Size Image Popup
            if (zoomedImage != null) {
                androidx.compose.ui.window.Dialog(onDismissRequest = { zoomedImage = null }) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { zoomedImage = null }, // Tap anywhere to close early
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            coil.compose.AsyncImage(
                                model = zoomedImage,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f) // Square or adjust as needed
                                    .padding(4.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Syllables / Merged Word
            if (mergeAnim.value < 0.5f) {
                // Use FlowRow to handle long words
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    val syllableColors = listOf(
                        Color(0xFF29B6F6), // Light Blue
                        Color(0xFFFFCA28), // Amber
                        Color(0xFFEF5350)  // Red
                    )
                    
                    word.syllables.forEachIndexed { index, syllable ->
                        EntranceAnimation(delayMillis = index * 100L) {
                            com.skul9x.danhvan.ui.common.PuzzlePiece(
                                text = syllable,
                                onClick = { 
                                    val spelling = com.skul9x.danhvan.util.VietnameseSpeller.getSpelling(syllable)
                                    onPlayTTS(spelling) 
                                },
                                color = syllableColors[index % syllableColors.size],
                                textColor = Color.White,
                                modifier = Modifier.graphicsLayer {
                                    alpha = 1f - mergeAnim.value
                                    scaleX = 1f - (mergeAnim.value * 0.5f)
                                    scaleY = 1f - (mergeAnim.value * 0.5f)
                                }
                            )
                        }
                    }
                }
            } else {
                // Merged Result (Full Word Card)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = (mergeAnim.value - 0.5f) * 2f
                            scaleX = mergeAnim.value
                            scaleY = mergeAnim.value
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        ResizingStrokeText(
                            text = word.text,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp),
                            initialStyle = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                            ),
                            textColor = Color(0xFFE91E63),
                            strokeColor = Color.White,
                            strokeWidth = 8f
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons Row (Speaker + Mic)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                 // 1. Removed Read Whole Word Button
                 
                 // 2. Microphone Button (Voice Recognition)
                 val micColor by androidx.compose.animation.animateColorAsState(
                     if (isRewarded) Color.LightGray else if (isListening) Color.Red else Color(0xFF4CAF50) 
                 )
                 
                 com.skul9x.danhvan.ui.common.CandyButton(
                     onClick = { 
                         if (!isRewarded) {
                             if (hasMicPermission) {
                                 if (isListening) speechRecognizerHelper.stopListening() else speechRecognizerHelper.startListening()
                             } else {
                                 permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                             }
                         }
                     },
                     color = micColor,
                     shape = androidx.compose.foundation.shape.CircleShape,
                     modifier = Modifier.size(80.dp)
                 ) {
                     Icon(
                         if (isListening) androidx.compose.material.icons.Icons.Default.Close else androidx.compose.material.icons.Icons.Filled.Mic, // Need Mic icon
                         contentDescription = "Thu âm", 
                         tint = Color.White, 
                         modifier = Modifier.size(40.dp)
                     )
                 }
            }
            
            // Listening Indicator
            AnimatedVisibility(visible = isListening) {
                Text("Đang nghe...", color = Color.Red, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
            }
        }
        
        // FEEDBACK POPUP OVERLAY
        // FEEDBACK POPUP OVERLAY
        // 1. Correct Popup
        androidx.compose.animation.AnimatedVisibility(
            visible = animState.feedbackType == FeedbackType.CORRECT,
            modifier = Modifier.align(Alignment.Center),
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.skul9x.danhvan.R.drawable.ic_check_correct),
                contentDescription = null,
                modifier = Modifier.size(150.dp)
            )
        }

        // 2. Wrong Popup
        androidx.compose.animation.AnimatedVisibility(
            visible = animState.feedbackType == FeedbackType.WRONG,
            modifier = Modifier.align(Alignment.Center),
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.skul9x.danhvan.R.drawable.ic_cross_wrong),
                contentDescription = null,
                modifier = Modifier.size(150.dp)
            )
        }
    }
}

// --- Mode 2: Ghép Từ (Spelling) ---
@Composable
fun SpellingMode(word: WordEntity, onPlayTTS: (String) -> Unit, onCorrect: () -> Unit, isRewarded: Boolean = false, onReward: () -> Unit) {
    var shuffledSyllables by remember(word) { mutableStateOf(word.syllables.shuffled()) }
    var selectedSyllables by remember(word) { mutableStateOf(emptyList<String>()) }
    var isCorrect by remember(word) { mutableStateOf(false) }
    
    // Local reward lock to prevent reshuffle exploit (Bug 5 fix)
    var hasLocallyRewarded by remember(word) { mutableStateOf(isRewarded) }
    
    // Auto-show internet image
    val internetImageUrl = remember(word.text) { 
        "https://image.pollinations.ai/prompt/${java.net.URLEncoder.encode("cartoon " + word.text, "UTF-8")}?width=400&height=400&nologo=true" 
    }

    LaunchedEffect(selectedSyllables) {
        if (selectedSyllables == word.syllables && !isCorrect) {
            isCorrect = true
            onPlayTTS(word.text)
            onCorrect()
            
            // Only reward if not already rewarded
            if (!hasLocallyRewarded) {
                hasLocallyRewarded = true
                onReward()
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Display Image
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            coil.compose.SubcomposeAsyncImage(
                model = internetImageUrl,
                contentDescription = word.text,
                modifier = Modifier.size(150.dp),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                },
                error = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Close, contentDescription = "Error", tint = androidx.compose.ui.graphics.Color.Red)
                            Text("Lỗi tải hình", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            )
        }

        Text("Ghép các âm tiết thành từ đúng:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Target Area
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(80.dp)) {
            word.syllables.forEachIndexed { index, _ ->
                val syllable = selectedSyllables.getOrNull(index)
                Card(
                    modifier = Modifier.size(80.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (syllable != null) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.LightGray
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (syllable != null) {
                            Text(syllable, style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Source Area
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            shuffledSyllables.forEach { syllable ->
                // Filter out already selected instances
                val remainingCount = shuffledSyllables.count { it == syllable } - selectedSyllables.count { it == syllable }
                
                if (remainingCount > 0) {
                    BouncingButton(onClick = {
                        onPlayTTS(syllable)
                        selectedSyllables = selectedSyllables + syllable
                    }) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                            Text(
                                text = syllable,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { selectedSyllables = emptyList() }) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Text("Làm lại")
        }
        
        if (isCorrect) {
            Text("Chính xác!", color = androidx.compose.ui.graphics.Color.Green, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

// --- Mode 3: Trắc Nghiệm (Quiz) ---
@Composable
fun QuizMode(word: WordEntity, allWords: List<WordEntity>, onCorrect: () -> Unit) {
    // Edge case guard: Need at least 2 words for meaningful quiz
    if (allWords.size < 2) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Cần thêm từ vựng để chơi trắc nghiệm!",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
        return
    }
    
    val options = remember(word, allWords) {
        (allWords.filter { it.id != word.id }.shuffled().take(3) + word).shuffled()
    }
    var selectedWord by remember(word) { mutableStateOf<WordEntity?>(null) }
    
    val internetImageUrl = remember(word.text) { 
        "https://image.pollinations.ai/prompt/${java.net.URLEncoder.encode("cartoon " + word.text, "UTF-8")}" 
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Always show internet image for quiz
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Image(
                painter = rememberAsyncImagePainter(internetImageUrl),
                contentDescription = null,
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(options) { option ->
                val isSelected = selectedWord == option
                val isTarget = option.id == word.id
                val color = when {
                    isSelected && isTarget -> androidx.compose.ui.graphics.Color.Green
                    isSelected && !isTarget -> androidx.compose.ui.graphics.Color.Red
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = color),
                    modifier = Modifier
                        .height(80.dp)
                        .clickable {
                            selectedWord = option
                            if (isTarget) onCorrect()
                        }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(option.text, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

// --- Mode 4: Điền Khuyết (Fill-in) ---
@Composable
fun FillInMode(word: WordEntity, allWords: List<WordEntity>, onPlayTTS: (String) -> Unit, onCorrect: () -> Unit) {
    if (word.syllables.isEmpty()) return
    
    val missingIndex = remember(word) { word.syllables.indices.random() }
    val missingSyllable = word.syllables[missingIndex]
    
    // Generate distractors (use distinct to avoid duplicates)
    val distractors = remember(word, allWords) {
        val allSyllables = allWords.flatMap { it.syllables }
            .filter { it != missingSyllable }
            .distinct()  // Fix: Remove duplicate syllables
        (allSyllables.shuffled().take(2) + missingSyllable).shuffled()
    }
    
    var selectedOption by remember(word) { mutableStateOf<String?>(null) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Chọn âm tiết còn thiếu:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            word.syllables.forEachIndexed { index, syllable ->
                Card(
                    modifier = Modifier.size(80.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (index == missingIndex) {
                            Text(if (selectedOption == missingSyllable) syllable else "?", style = MaterialTheme.typography.headlineSmall)
                        } else {
                            Text(syllable, style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            distractors.forEach { option ->
                BouncingButton(onClick = {
                    onPlayTTS(option)
                    selectedOption = option
                    if (option == missingSyllable) onCorrect()
                }) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedOption == option) {
                                if (option == missingSyllable) androidx.compose.ui.graphics.Color.Green else androidx.compose.ui.graphics.Color.Red
                            } else MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- Mode 5: Lật Hình (Memory) ---
data class MemoryCard(
    val id: String,
    val content: String?, // Text or Image Path (nullable for missing images)
    val isImage: Boolean,
    val matchId: Int, // Word ID to match pairs
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

@Composable
fun MemoryMode(words: List<WordEntity>, onPlayTTS: (String) -> Unit) {
    // Take 4 random words for 8 cards
    val gameWords = remember(words) { words.shuffled().take(4) }
    
    var cards by remember(gameWords) {
        mutableStateOf(
            gameWords.flatMap { word ->
                listOf(
                    MemoryCard(id = "${word.id}_img", content = word.imageUri ?: "", isImage = true, matchId = word.id),
                    MemoryCard(id = "${word.id}_txt", content = word.text, isImage = false, matchId = word.id)
                )
            }.shuffled()
        )
    }
    
    var flippedIndices by remember { mutableStateOf(emptyList<Int>()) }

    LaunchedEffect(flippedIndices) {
        if (flippedIndices.size == 2) {
            val idx1 = flippedIndices[0]
            val idx2 = flippedIndices[1]
            if (cards[idx1].matchId == cards[idx2].matchId) {
                // Match - use copy() for proper state update
                cards = cards.mapIndexed { index, card ->
                    if (index == idx1 || index == idx2) card.copy(isMatched = true)
                    else card
                }
                if (!cards[idx1].isImage) cards[idx1].content?.let { onPlayTTS(it) }
                if (!cards[idx2].isImage) cards[idx2].content?.let { onPlayTTS(it) }
            }
            // Delay and flip back if not matched
            kotlinx.coroutines.delay(1000)
            cards = cards.map { if (!it.isMatched) it.copy(isFlipped = false) else it }
            flippedIndices = emptyList()
        }
    }

    LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.fillMaxSize()) {
        itemsIndexed(cards) { index, card ->
            val rotation by animateFloatAsState(targetValue = if (card.isFlipped || card.isMatched) 180f else 0f)
            
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .aspectRatio(1f)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clickable {
                        if (flippedIndices.size < 2 && !card.isFlipped && !card.isMatched) {
                            // Use copy() for proper state update
                            cards = cards.mapIndexed { i, c ->
                                if (i == index) c.copy(isFlipped = true) else c
                            }
                            flippedIndices = flippedIndices + index
                        }
                    }
            ) {
                if (rotation <= 90f) {
                    // Back of card
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {}
                } else {
                    // Front of card
                    Card(
                        modifier = Modifier.fillMaxSize().graphicsLayer { rotationY = 180f },
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (card.isImage) {
                                val imageFile = card.content?.let { File(it) }
                                if (imageFile != null && imageFile.exists()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(imageFile),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text("IMG")
                                }
                            } else {
                                Text(card.content ?: "", textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResizingStrokeText(
    text: String,
    modifier: Modifier = Modifier,
    initialStyle: TextStyle,
    textColor: Color,
    strokeColor: Color? = null,
    strokeWidth: Float = 0f,
    minFontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp),
    maxIterations: Int = 20
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        var textStyle by remember(text) { mutableStateOf(initialStyle) }
        var readyToDraw by remember(text) { mutableStateOf(false) }
        var iterations by remember(text) { mutableStateOf(0) }

        // Invisible measuring text
        Text(
            text = text,
            style = textStyle,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.alpha(0f),
            onTextLayout = { result ->
                if ((result.didOverflowWidth || result.didOverflowHeight) && 
                    textStyle.fontSize > minFontSize && 
                    iterations < maxIterations) {
                    // Shrink font by 10%
                    val newSize = textStyle.fontSize * 0.9f
                    textStyle = textStyle.copy(fontSize = newSize)
                    iterations++
                } else {
                    readyToDraw = true
                }
            }
        )

        if (readyToDraw) {
            // Stroke Layer
            if (strokeColor != null) {
                Text(
                    text = text,
                    style = textStyle.copy(
                        drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidth,
                            join = StrokeJoin.Round
                        )
                    ),
                    color = strokeColor,
                    maxLines = 1,
                    softWrap = false
                )
            }
            
            // Fill Layer
            Text(
                text = text,
                style = textStyle,
                color = textColor,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
