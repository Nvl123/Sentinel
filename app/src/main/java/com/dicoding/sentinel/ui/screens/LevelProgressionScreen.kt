package com.dicoding.sentinel.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dicoding.sentinel.util.GamificationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelProgressionScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sortedLevels = remember { GamificationUtils.levels.sortedBy { it.thresholdDays } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Level Progression", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            item {
                Text(
                    text = "Lalui setiap level dengan tetap bersih dan rawan relapse. Setiap pencapaian akan membuka emblem baru.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            items(
                items = sortedLevels,
                key = { it.name }
            ) { lv ->
                LevelDetailItem(context, lv)
            }
        }
    }
}

@Composable
fun LevelDetailItem(context: Context, level: GamificationUtils.Level) {
    // We try to load the _small version first if it exists, otherwise fallback
    // Actually, in code we refer to resource by ID. 
    // If the user runs the optimizer, they will have R.drawable.object_illustration_XX_small
    
    val badgeBitmap = remember(level.badgeResId) {
        val cachedFile = GamificationUtils.getCompressedBadgeFile(context, level.badgeResId)
        if (cachedFile != null) {
            android.graphics.BitmapFactory.decodeFile(cachedFile.absolutePath)?.asImageBitmap()
                ?: GamificationUtils.decodeSampledBitmapFromResource(context.resources, level.badgeResId, 128, 128).asImageBitmap()
        } else {
            GamificationUtils.decodeSampledBitmapFromResource(context.resources, level.badgeResId, 128, 128).asImageBitmap()
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Image(
                            bitmap = badgeBitmap,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Column {
                    Text(
                        text = level.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (level.thresholdDays == 0) "Mulai perjalanan" else "${level.thresholdDays} hari bersih",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            )
        }
    }
}
