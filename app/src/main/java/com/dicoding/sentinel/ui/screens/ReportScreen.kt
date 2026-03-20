package com.dicoding.sentinel.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dicoding.sentinel.domain.model.RelapseLog
import com.dicoding.sentinel.domain.model.UrgeLog
import com.dicoding.sentinel.ui.components.StreakCalendar
import com.dicoding.sentinel.util.StreakUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportScreen(
    relapses: List<RelapseLog>,
    urgeLogs: List<UrgeLog>,
    streakStartTime: Long,
    savedLongestStreak: Long
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val currentTime = System.currentTimeMillis()
    val diff = (currentTime - streakStartTime).coerceAtLeast(0)
    val days = diff / (24 * 3600 * 1000)
    val hours = (diff % (24 * 3600 * 1000)) / (3600 * 1000)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text(
                text = "REPORTS",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Streak Summary Section
        item {
            val (longestDays, longestHours) = StreakUtils.getLongestStreak(relapses, streakStartTime, savedLongestStreak)
            StreakSummarySection(days, hours, longestDays, longestHours)
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Streak Calendar Section
        item {
            Text(
                text = "STREAK VISUALIZATION",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            StreakCalendar(
                relapses = relapses,
                streakStartTime = streakStartTime,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Simple Visualization Section
        item {
            UrgeVisualizationSection(urgeLogs, relapses)
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Text(
                text = "RECENT ACTIVITY",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(relapses) { relapse ->
            ActivityCard(
                title = "Relapse: ${relapse.cause}",
                subtitle = dateFormat.format(Date(relapse.timestamp)),
                description = relapse.note,
                color = MaterialTheme.colorScheme.error
            )
        }

        items(urgeLogs) { urge ->
            val protocolNames = urge.protocolsUsed.split(",")
                .mapNotNull { idStr -> 
                    val id = idStr.toIntOrNull()
                    com.dicoding.sentinel.domain.model.Protocol.library.find { it.id == id }?.name 
                }
                .joinToString(", ")

            val description = if (protocolNames.isNotEmpty()) {
                "Metode: $protocolNames"
            } else {
                "Metode: Selesai tanpa Jurus"
            }

            ActivityCard(
                title = "Urge Defeated",
                subtitle = dateFormat.format(Date(urge.timestamp)),
                description = description,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun StreakSummarySection(days: Long, hours: Long, longestDays: Long, longestHours: Long) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "CURRENT CLEAN STREAK",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = days.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "d ",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = hours.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "h",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "LONGEST STREAK",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = longestDays.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "d ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = longestHours.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "h",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
fun UrgeVisualizationSection(urgeLogs: List<UrgeLog>, relapses: List<RelapseLog>) {
    val totalUrges = urgeLogs.size + relapses.size
    val successRate = if (totalUrges > 0) (urgeLogs.size.toFloat() / totalUrges) else 0f

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "FIREWALL EFFICIENCY", 
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${(successRate * 100).toInt()}%",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "${urgeLogs.size} Urges Defeated", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    Text(text = "${relapses.size} Broken Wall", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Progress Bar
            LinearProgressIndicator(
                progress = { successRate },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
            )
        }
    }
}

@Composable
fun ActivityCard(title: String, subtitle: String, description: String, color: Color) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(6.dp, 48.dp)
                    .background(color, RoundedCornerShape(3.dp))
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle, 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = description, 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
