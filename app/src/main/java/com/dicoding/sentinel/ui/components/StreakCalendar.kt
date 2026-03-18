package com.dicoding.sentinel.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dicoding.sentinel.domain.model.RelapseLog
import com.dicoding.sentinel.util.StreakUtils
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@Composable
fun StreakCalendar(
    relapses: List<RelapseLog>,
    streakStartTime: Long?,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(LocalDate.now()) }
    val monthData = StreakUtils.getMonthData(
        relapses = relapses,
        streakStartTime = streakStartTime,
        year = currentMonth.year,
        month = currentMonth.monthValue
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Month and Year + Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
                }
                
                Text(
                    text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weekday Headers
            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days Grid
            val firstDayOfMonth = LocalDate.of(currentMonth.year, currentMonth.month, 1)
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 (Mon) to 7 (Sun)
            
            // padding days from previous month
            val totalCells = 42 // 6 weeks
            val days = mutableListOf<LocalDate?>()
            for (i in 1 until firstDayOfWeek) {
                days.add(null)
            }
            
            var date = firstDayOfMonth
            while (date.month == currentMonth.month) {
                days.add(date)
                date = date.plusDays(1)
            }
            
            while (days.size < totalCells) {
                days.add(null)
            }

            // Render 6 rows of 7 days
            days.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { day ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day != null) {
                                val status = monthData[day] ?: StreakUtils.DayStatus.NONE
                                DayCell(day.dayOfMonth.toString(), status)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(Color(0xFF4CAF50), "Clean")
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem(MaterialTheme.colorScheme.error, "Relapse")
            }
        }
    }
}

@Composable
fun DayCell(text: String, status: StreakUtils.DayStatus) {
    val backgroundColor = when (status) {
        StreakUtils.DayStatus.CLEAN -> Color(0xFF4CAF50).copy(alpha = 0.2f)
        StreakUtils.DayStatus.RELAPSE -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
        else -> Color.Transparent
    }
    
    val textColor = when (status) {
        StreakUtils.DayStatus.CLEAN -> Color(0xFF2E7D32)
        StreakUtils.DayStatus.RELAPSE -> MaterialTheme.colorScheme.error
        StreakUtils.DayStatus.FUTURE -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderColor = when (status) {
        StreakUtils.DayStatus.CLEAN -> Color(0xFF4CAF50)
        StreakUtils.DayStatus.RELAPSE -> MaterialTheme.colorScheme.error
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.padding(2.dp) // Subtle border effect or indicator
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (status != StreakUtils.DayStatus.NONE && status != StreakUtils.DayStatus.FUTURE) FontWeight.Bold else FontWeight.Normal
            ),
            color = textColor
        )
        
        if (status == StreakUtils.DayStatus.RELAPSE) {
             Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(4.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape)
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color.copy(alpha = 0.2f), CircleShape)
                .clip(CircleShape)
                .then(Modifier.padding(2.dp))
        ) {
            Box(modifier = Modifier.fillMaxSize().background(color, CircleShape))
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}
