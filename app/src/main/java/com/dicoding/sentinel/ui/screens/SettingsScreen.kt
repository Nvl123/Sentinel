package com.dicoding.sentinel.ui.screens

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip

@Composable
fun SettingsScreen(
    context: Context,
    isAppLockEnabled: Boolean,
    lockedApps: Set<String>,
    streakStartTime: Long?,
    onToggleAppLock: (Boolean) -> Unit,
    onUpdateLockedApps: (Set<String>) -> Unit,
    onViewLevelProgression: () -> Unit,
    onClearData: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showAppSelectionDialog by remember { mutableStateOf(false) }

    var hasUsageStats by remember { mutableStateOf(checkUsageStatsPermission(context)) }
    var hasOverlay by remember { mutableStateOf(checkOverlayPermission(context)) }

    // Refresh permissions when screen is resumed (e.g. from settings)
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasUsageStats = checkUsageStatsPermission(context)
                hasOverlay = checkOverlayPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 40.dp, bottom = 40.dp)
    ) {
        item {
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        // LEVEL INFO SECTION
        if (streakStartTime != null) {
            val days = (System.currentTimeMillis() - streakStartTime) / (24 * 3600 * 1000)
            val level = com.dicoding.sentinel.util.GamificationUtils.getLevel(days)
            
            item {
                val currentBadge = remember(level.badgeResId) {
                    val cachedFile = com.dicoding.sentinel.util.GamificationUtils.getCompressedBadgeFile(context, level.badgeResId)
                    if (cachedFile != null) {
                        android.graphics.BitmapFactory.decodeFile(cachedFile.absolutePath)?.asImageBitmap()
                            ?: com.dicoding.sentinel.util.GamificationUtils.decodeSampledBitmapFromResource(
                                context.resources,
                                level.badgeResId,
                                256,
                                256
                            ).asImageBitmap()
                    } else {
                        com.dicoding.sentinel.util.GamificationUtils.decodeSampledBitmapFromResource(
                            context.resources,
                            level.badgeResId,
                            256,
                            256
                        ).asImageBitmap()
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = currentBadge,
                            contentDescription = "Level Badge",
                            modifier = Modifier
                                .size(64.dp)
                                .padding(4.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "CURRENT LEVEL",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = level.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            
            item {
                Text(
                    text = "GAMIFICATION",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onViewLevelProgression,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Lihat Semua Level & Emblem",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // SECURITY & APP LOCK SECTION
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "SECURITY & APP LOCK",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable App Lock",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Lock selected apps with a verification code.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = isAppLockEnabled,
                            onCheckedChange = { 
                                if (it && (!hasUsageStats || !hasOverlay)) {
                                    // Request permissions if not granted
                                    if (!hasUsageStats) {
                                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                                    } else if (!hasOverlay) {
                                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                                        context.startActivity(intent.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                                    }
                                } else {
                                    onToggleAppLock(it)
                                }
                            }
                        )
                    }

                    if (isAppLockEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAppSelectionDialog = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Locked Applications",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (lockedApps.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    LockedAppsIconsRow(context, lockedApps)
                                } else {
                                    Text(
                                        text = "No apps selected",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // DATA MANAGEMENT SECTION
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "DATA MANAGEMENT",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Hapus Semua Laporan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ini akan menghapus semua riwayat relapse dan data urge yang berhasil dilewati, serta mereset statistik.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { showConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("RESET SEMUA DATA", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showConfirmDialog) {
        DataResetDialog(
            onDismiss = { showConfirmDialog = false },
            onConfirm = { 
                onClearData()
                showConfirmDialog = false
            }
        )
    }

    if (showAppSelectionDialog) {
        AppSelectionDialog(
            context = context,
            selectedApps = lockedApps,
            onSelectionChanged = onUpdateLockedApps,
            onDismiss = { showAppSelectionDialog = false }
        )
    }
}

@Composable
fun LockedAppsIconsRow(context: Context, lockedApps: Set<String>) {
    val packageManager = context.packageManager
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(lockedApps.toList()) { packageName ->
            val icon = remember(packageName) { getAppIcon(packageManager, packageName) }
            if (icon != null) {
                Image(
                    bitmap = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                )
            }
        }
    }
}

@Composable
fun DataResetDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    var verificationText by remember { mutableStateOf("") }
    val randomString = remember { 
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        (1..6).map { chars.random() }.joinToString("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Konfirmasi Reset") },
        text = { 
            Column {
                Text("Apakah Anda yakin ingin menghapus SEMUA data laporan? Tindakan ini tidak dapat dibatalkan.")
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Ketik kode berikut untuk konfirmasi: $randomString",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = verificationText,
                    onValueChange = { verificationText = it.uppercase() },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ketik kode di sini") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = verificationText == randomString
            ) {
                Text(
                    "YA, HAPUS", 
                    color = if (verificationText == randomString) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("BATAL")
            }
        }
    )
}

@Composable
fun AppSelectionDialog(
    context: Context,
    selectedApps: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val packageManager = context.packageManager
    val apps = remember {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        packageManager.queryIntentActivities(intent, 0)
            .map { 
                AppInfo(
                    name = it.loadLabel(packageManager).toString(),
                    packageName = it.activityInfo.packageName
                )
            }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.name }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Aplikasi untuk di-Lock") },
        text = {
            Box(modifier = Modifier.height(400.dp)) {
                LazyColumn {
                    items(apps) { app ->
                        val icon = remember(app.packageName) { getAppIcon(packageManager, app.packageName) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newSelection = if (selectedApps.contains(app.packageName)) {
                                        selectedApps - app.packageName
                                    } else {
                                        selectedApps + app.packageName
                                    }
                                    onSelectionChanged(newSelection)
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedApps.contains(app.packageName),
                                onCheckedChange = {
                                    val newSelection = if (it) {
                                        selectedApps + app.packageName
                                    } else {
                                        selectedApps - app.packageName
                                    }
                                    onSelectionChanged(newSelection)
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            if (icon != null) {
                                Image(
                                    bitmap = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp).clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                            Text(text = app.name, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("SELESAI")
            }
        }
    )
}

data class AppInfo(val name: String, val packageName: String)

fun getAppIcon(packageManager: PackageManager, packageName: String): ImageBitmap? {
    return try {
        val drawable = packageManager.getApplicationIcon(packageName)
        val bitmap = android.graphics.Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            android.graphics.Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}



fun checkUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
    } else {
        @Suppress("DEPRECATION")
        appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

fun checkOverlayPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(context)
    } else {
        true
    }
}

