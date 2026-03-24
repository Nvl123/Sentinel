package com.dicoding.sentinel

import android.app.*
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.widget.Toast
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.*
import androidx.savedstate.*
import com.dicoding.sentinel.data.local.SentinelPreference
import com.dicoding.sentinel.ui.theme.SentinelTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.util.*



class AppMonitorService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var preferences: SentinelPreference
    private var isMonitoring = false

    companion object {
        var instance: AppMonitorService? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        preferences = SentinelPreference(this)
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isMonitoring) {
            isMonitoring = true
            startMonitoring()
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "app_monitor_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "App Lock Monitor",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Sentinel App Lock")
            .setContentText("Monitoring apps...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }
    }

    private var currentForegroundApp: String? = null
    private val unlockedPackages = mutableSetOf<String>()
    private var isMonitoringEnabled = false
    private var currentLockedApps = emptySet<String>()
    
    // Overlay components
    private var overlayView: android.view.View? = null
    private val windowManager by lazy { getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager }

    private fun startMonitoring() {
        android.util.Log.d("SENTINEL_DEBUG", "startMonitoring: Loop started")
        
        // Collect preferences in real-time
        serviceScope.launch {
            combine(preferences.isAppLockEnabled, preferences.lockedAppsStr) { enabled, appsStr ->
                enabled to appsStr
            }.collect { (enabled, appsStr) ->
                isMonitoringEnabled = enabled
                currentLockedApps = if (appsStr.isBlank()) emptySet() else appsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                android.util.Log.d("SENTINEL_DEBUG", "Preferences updated: enabled=$isMonitoringEnabled, lockedApps=$currentLockedApps")
                
                // If disabled, hide overlay immediately
                if (!enabled) hideOverlay()
            }
        }

        // Monitoring loop
        serviceScope.launch {
            while (isActive) {
                if (isMonitoringEnabled) {
                    val foregroundApp = getForegroundApp()
                    
                    // Update current app even if it's sentinel
                    if (foregroundApp != currentForegroundApp) {
                        unlockedPackages.clear() // Clear all unlocks on any app switch for maximum security
                        currentForegroundApp = foregroundApp
                        android.util.Log.d("SENTINEL_DEBUG", "App changed to: $foregroundApp")
                    }

                    if (foregroundApp != null && currentLockedApps.contains(foregroundApp)) {
                        // If it's locked and NOT unlocked yet, show overlay!
                        if (!unlockedPackages.contains(foregroundApp) && overlayView == null) {
                            android.util.Log.d("SENTINEL_DEBUG", "SHOWING OVERLAY LOCK for $foregroundApp")
                            withContext(Dispatchers.Main) {
                                showOverlay(foregroundApp)
                            }
                        }
                    } else {
                        // If we are no longer in a locked app, hide overlay
                        if (overlayView != null) {
                            withContext(Dispatchers.Main) { hideOverlay() }
                        }
                    }
                }
                delay(1000)
            }
        }
    }

    // Call this from LockActivity when unlocked
    fun markAppAsUnlocked(packageName: String) {
        unlockedPackages.add(packageName)
        android.util.Log.d("SENTINEL_DEBUG", "App marked as UNLOCKED via markAppAsUnlocked: $packageName")
    }

    private fun showOverlay(packageName: String) {

        if (overlayView != null) return
        
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            android.view.WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = android.view.WindowManager.LayoutParams(
            android.view.WindowManager.LayoutParams.MATCH_PARENT,
            android.view.WindowManager.LayoutParams.MATCH_PARENT,
            type,
            android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            android.graphics.PixelFormat.TRANSLUCENT
        )
        
        // Adjust params to be focusable so we can type
        params.flags = params.flags and android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()

        val composeView = ComposeView(this).apply {
            setContent {
                SentinelTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Black
                    ) {
                        LockScreenContent(
                            onUnlockSuccess = {
                                unlockedPackages.add(packageName)
                                hideOverlay()
                            }
                        )
                    }
                }
            }
        }
        
        // Fix for ComposeView in Service
        val lifecycleOwner = MyLifecycleOwner()
        lifecycleOwner.performRestore(null)
        lifecycleOwner.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_RESUME)
        
        composeView.setViewTreeLifecycleOwner(lifecycleOwner)
        composeView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        // setViewTreeComposeViewOwner(composeView) // Removed as it is unresolved

        try {
            windowManager.addView(composeView, params)
            overlayView = composeView
        } catch (e: Exception) {
            android.util.Log.e("SENTINEL_DEBUG", "Failed to add overlay view", e)
        }
    }

    private fun hideOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                // Ignore
            }
            overlayView = null
        }
    }

    // Helper class for Lifecycle in Service
    class MyLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val savedStateRegistryController = SavedStateRegistryController.create(this)

        override val lifecycle: androidx.lifecycle.Lifecycle = lifecycleRegistry
        override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry

        fun handleLifecycleEvent(event: androidx.lifecycle.Lifecycle.Event) = lifecycleRegistry.handleLifecycleEvent(event)
        fun performRestore(savedState: Bundle?) = savedStateRegistryController.performRestore(savedState)
    }

    private fun getForegroundApp(): String? {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        
        val usageEvents = usageStatsManager.queryEvents(time - 10000, time)
        val event = android.app.usage.UsageEvents.Event()
        var lastApp: String? = null
        
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == 1 || event.eventType == 31) {
                lastApp = event.packageName
            }
        }
        
        if (lastApp == null) {
            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 60, time)
            if (!stats.isNullOrEmpty()) {
                lastApp = stats.maxByOrNull { it.lastTimeUsed }?.packageName
            }
        }

        if (lastApp == packageName) return null
        return lastApp
    }





    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceScope.cancel()
        isMonitoring = false
    }
}
