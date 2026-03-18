package com.dicoding.sentinel.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Personal Habit Firewall",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(64.dp))

        // Streak Display (M3 Primary Container)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isLoaded) {
                        "${days}d ${hours}h ${minutes}m ${seconds}s"
                    } else {
                        "--d --h --m --s"
                    },
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "CURRENT CLEAN STREAK",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))

        // Firewall Button
        Button(
            onClick = onActivateFirewall,
            modifier = Modifier
                .size(200.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 2.dp
            )
        ) {
            Text(
                text = "ACTIVATE\nFIREWALL",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        TextButton(
            onClick = onReportRelapse,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(
                text = "REPORT RELAPSE",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                )
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
