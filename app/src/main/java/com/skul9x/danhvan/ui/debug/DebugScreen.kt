package com.skul9x.danhvan.ui.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.skul9x.danhvan.ui.MainViewModel

@Composable
fun DebugScreen(viewModel: MainViewModel) {
    val debugLog by viewModel.debugLog.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    LaunchedEffect(debugLog.size) {
        if (debugLog.isNotEmpty()) {
            listState.animateScrollToItem(debugLog.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Debug Logs", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            
            Row {
                IconButton(onClick = { viewModel.clearLog() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.Red)
                }
                IconButton(onClick = { 
                    clipboardManager.setText(AnnotatedString(debugLog.joinToString("\n"))) 
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Copy", tint = Color.Green)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
        ) {
            SelectionContainer {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.padding(8.dp)
                ) {
                    items(debugLog.size) { index ->
                        Text(
                            text = debugLog[index],
                            color = Color.Green,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                        Divider(color = Color.Gray.copy(alpha = 0.5f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}
