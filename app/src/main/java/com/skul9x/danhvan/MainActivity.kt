package com.skul9x.danhvan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.skul9x.danhvan.ui.MainViewModel
import com.skul9x.danhvan.ui.game.GameScreen
import com.skul9x.danhvan.ui.parent.ParentalScreen
import com.skul9x.danhvan.ui.theme.DanhVanTheme

import androidx.compose.material.icons.filled.Info
import com.skul9x.danhvan.ui.debug.DebugScreen

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DanhVanTheme {
                val navController = rememberNavController()
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                
                // Child Lock State
                var showChildLock by remember { mutableStateOf(false) }
                var childLockAnswer by remember { mutableStateOf("") }
                val num1 = remember { (1..5).random() }
                val num2 = remember { (1..5).random() }
                
                if (showChildLock) {
                    AlertDialog(
                        onDismissRequest = { showChildLock = false },
                        title = { Text("Phụ huynh") },
                        text = {
                            Column {
                                Text("Vui lòng giải bài toán để tiếp tục:")
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("$num1 + $num2 = ?", style = MaterialTheme.typography.headlineSmall)
                                OutlinedTextField(
                                    value = childLockAnswer,
                                    onValueChange = { childLockAnswer = it },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                                )
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                if (childLockAnswer == (num1 + num2).toString()) {
                                    showChildLock = false
                                    navController.navigate("parent")
                                } else {
                                    childLockAnswer = ""
                                }
                            }) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showChildLock = false }) {
                                Text("Hủy")
                            }
                        }
                    )
                }

                Scaffold(
                    bottomBar = {
                        // Hide bottom bar if in game
                        if (currentRoute?.startsWith("game") != true) {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                    label = { Text("Chơi") },
                                    selected = currentRoute == "topics",
                                    onClick = { navController.navigate("topics") }
                                )
                                NavigationBarItem(
                                icon = { Icon(Icons.Default.Face, contentDescription = null) },
                                label = { Text("Phụ huynh") },
                                selected = currentRoute == "parent",
                                onClick = { 
                                    // Trigger Child Lock
                                    childLockAnswer = ""
                                    showChildLock = true 
                                }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                label = { Text("Thống kê") },
                                selected = currentRoute == "stats",
                                onClick = { navController.navigate("stats") }
                            )
                        }
                    }
                }) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "topics",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("topics") { 
                            com.skul9x.danhvan.ui.topic.TopicScreen(
                                viewModel = viewModel,
                                onTopicSelected = { topicId ->
                                    navController.navigate("game/$topicId")
                                },
                                onShopClick = { navController.navigate("shop") }
                            )
                        }
                        composable(
                            route = "game/{topicId}",
                            arguments = listOf(androidx.navigation.navArgument("topicId") { type = androidx.navigation.NavType.StringType })
                        ) { backStackEntry ->
                            val topicId = backStackEntry.arguments?.getString("topicId") ?: "all"
                            GameScreen(
                                viewModel = viewModel,
                                topicId = topicId,
                                onBack = { navController.popBackStack() },
                                onOpenStickerBook = { navController.navigate("sticker_book") }
                            )
                        }
                        // Keep old route for bottom nav compatibility (defaults to all)
                        composable("game") { 
                             GameScreen(
                                viewModel = viewModel,
                                topicId = "all",
                                onBack = { navController.navigate("topics") },
                                onOpenStickerBook = { navController.navigate("sticker_book") }
                            )
                        }
                        composable("parent") { 
                            com.skul9x.danhvan.ui.parent.ParentalScreen(
                                viewModel = viewModel,
                                onNavigateToDebug = { navController.navigate("debug") }
                            ) 
                        }
                        composable("stats") { com.skul9x.danhvan.ui.stats.StatisticsScreen(viewModel) }
                        composable("debug") { DebugScreen(viewModel) }
                        composable("shop") { 
                            com.skul9x.danhvan.ui.shop.ShopScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onOpenStickerBook = { navController.navigate("sticker_book") }
                            ) 
                        }
                        composable("sticker_book") {
                            com.skul9x.danhvan.ui.shop.StickerBookScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}