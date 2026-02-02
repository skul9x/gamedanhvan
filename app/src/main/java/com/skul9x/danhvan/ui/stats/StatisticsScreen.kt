package com.skul9x.danhvan.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skul9x.danhvan.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatisticsScreen(viewModel: MainViewModel) {
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Thống kê học tập",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Weekly Chart Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sao trong tuần",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Generate last 7 days placeholders if empty
                    val displayStats = if (weeklyStats.isEmpty()) {
                        (0..6).map { i ->
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.DAY_OF_YEAR, -i)
                            com.skul9x.danhvan.data.DailyStats(cal.timeInMillis, 0)
                        }.reversed()
                    } else {
                        // Fill in missing days logic could be here, but for simplicity just show what we have
                        // Better: Map last 7 days and find matching stats
                        (0..6).map { i ->
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.DAY_OF_YEAR, -(6-i))
                            cal.set(Calendar.HOUR_OF_DAY, 0)
                            cal.set(Calendar.MINUTE, 0)
                            cal.set(Calendar.SECOND, 0)
                            cal.set(Calendar.MILLISECOND, 0)
                            val date = cal.timeInMillis
                            weeklyStats.find { it.date == date } ?: com.skul9x.danhvan.data.DailyStats(date, 0)
                        }
                    }

                    val maxStars = displayStats.maxOfOrNull { it.starsEarned }?.coerceAtLeast(10) ?: 10
                    
                    displayStats.forEach { stat ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val barHeight = (stat.starsEarned.toFloat() / maxStars) * 150f
                            
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(barHeight.dp.coerceAtLeast(4.dp))
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(stat.date)),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Summary Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCard(
                title = "Hôm nay",
                value = "${weeklyStats.find { isToday(it.date) }?.starsEarned ?: 0} ⭐",
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.secondaryContainer
            )
            SummaryCard(
                title = "Tổng cộng",
                value = "${weeklyStats.sumOf { it.starsEarned }} ⭐",
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.tertiaryContainer
            )
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

fun isToday(date: Long): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal2.timeInMillis = date
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
