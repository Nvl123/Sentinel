package com.dicoding.sentinel.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    streakStartTime: Long?,
    onActivateFirewall: () -> Unit,
    onReportRelapse: () -> Unit,
    onLogout: () -> Unit
) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    Box(modifier = Modifier.fillMaxSize()) {

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val isLoaded = streakStartTime != null
    val diff = if (isLoaded) currentTime - streakStartTime!! else 0L
    val totalSeconds = (diff / 1000).coerceAtLeast(0)
    val days = totalSeconds / (24 * 3600)
    val hours = (totalSeconds % (24 * 3600)) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SENTINEL",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Personal Habit Firewall",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(64.dp))

        // Streak Display
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isLoaded) "${days}d ${hours}h ${minutes}m ${seconds}s" else "--d --h --m --s",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "CLEAN STREAK",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(80.dp))

        // Firewall Button
        Button(
            onClick = onActivateFirewall,
            modifier = Modifier
                .size(220.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF0055).copy(alpha = 0.8f),
                                Color(0xFF880022)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ACTIVATE\nFIREWALL",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        TextButton(onClick = onReportRelapse) {
            Text(
                text = "REPORT RELAPSE",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }

    // Logout Button in Top Right (Overlay) - Moved outside the Column to use BoxScope.align
    FilledIconButton(
        onClick = onLogout,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = Icons.Default.ExitToApp,
            contentDescription = "Logout"
        )
    }
}
}
