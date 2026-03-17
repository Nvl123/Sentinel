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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dicoding.sentinel.domain.model.RelapseLog
import com.dicoding.sentinel.domain.model.UrgeLog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportScreen(
    relapses: List<RelapseLog>,
    urgeLogs: List<UrgeLog>,
    streakStartTime: Long
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
            StreakSummarySection(days, hours)
            Spacer(modifier = Modifier.height(24.dp))
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
fun StreakSummarySection(days: Long, hours: Long) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "CURRENT STREAK",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = days.toString(),
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black)
                )
                Text(
                    text = "d ",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = hours.toString(),
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black)
                )
                Text(
                    text = "h",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
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
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = "FIREWALL EFFICIENCY", style = MaterialTheme.typography.labelLarge)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${(successRate * 100).toInt()}%",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "${urgeLogs.size} Urges Defeated", color = MaterialTheme.colorScheme.primary)
                    Text(text = "${relapses.size} Broken Wall", color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Simple Bar Chart Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(successRate)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
fun ActivityCard(title: String, subtitle: String, description: String, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(4.dp, 40.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                if (description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
