package com.dicoding.sentinel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dicoding.sentinel.data.local.SentinelDatabase
import com.dicoding.sentinel.data.local.SentinelPreference
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import com.dicoding.sentinel.ui.components.FirewallOverlay
import com.dicoding.sentinel.ui.components.ProtocolSelectionDialog
import com.dicoding.sentinel.ui.components.RelapseDialog
import com.dicoding.sentinel.ui.screens.DashboardScreen
import com.dicoding.sentinel.ui.screens.LoginScreen
import com.dicoding.sentinel.ui.screens.ReportScreen
import com.dicoding.sentinel.ui.theme.SentinelTheme
import com.dicoding.sentinel.ui.viewmodel.FirewallViewModel
import com.dicoding.sentinel.ui.viewmodel.RelapseViewModel
import kotlinx.coroutines.launch

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

import androidx.compose.material.icons.filled.Settings
import com.dicoding.sentinel.ui.screens.SettingsScreen
import com.dicoding.sentinel.widget.StreakWidgetProvider

enum class Screen { Dashboard, Reports, Settings }

class MainActivity : ComponentActivity() {
    override fun onStart() {
        super.onStart()
        StreakWidgetProvider.triggerUpdate(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val preferences = SentinelPreference(this)
        val database = SentinelDatabase.getDatabase(this)
        val relapseDao = database.relapseDao()

        setContent {
            val viewModel: FirewallViewModel = viewModel()
            val relapseViewModel: RelapseViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return RelapseViewModel(relapseDao) as T
                    }
                }
            )

            val firewallState by viewModel.uiState.collectAsState()
            val savedStreakStartTime by preferences.streakStartTime.collectAsState(initial = null)
            val savedLongestStreak by preferences.longestStreak.collectAsState(initial = 0L)
            val isLoggedIn by preferences.isLoggedIn.collectAsState(initial = false)
            val allRelapses by relapseViewModel.allRelapses.collectAsState()
            val allUrgeLogs by relapseViewModel.allUrgeLogs.collectAsState()
            
            // Firewall persistence flows
            val activeProtocolId by preferences.activeProtocolId.collectAsState(initial = null)
            val usedProtocolsStr by preferences.usedProtocolIdsStr.collectAsState(initial = "")
            
            val isAppLockEnabled by preferences.isAppLockEnabled.collectAsState(initial = false)
            val lockedAppsStr by preferences.lockedAppsStr.collectAsState(initial = "")
            val lockedApps = remember(lockedAppsStr) { 
                lockedAppsStr.split(",").filter { it.isNotBlank() }.toSet() 
            }

            val scope = rememberCoroutineScope()
            var showRelapseDialog by remember { mutableStateOf(false) }
            var currentScreen by remember { mutableStateOf(Screen.Dashboard) }

            // Handle AppMonitorService Lifecycle
            LaunchedEffect(isAppLockEnabled) {
                val intent = android.content.Intent(this@MainActivity, AppMonitorService::class.java)
                if (isAppLockEnabled) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                    } else {
                        startService(intent)
                    }
                } else {
                    stopService(intent)
                }
            }

            // Restore firewall state on launch
            LaunchedEffect(activeProtocolId, usedProtocolsStr) {
                if (activeProtocolId != null) {
                    viewModel.restoreState(activeProtocolId, usedProtocolsStr)
                }
            }

            // Persist the initial streak start time if it's missing in DataStore
            LaunchedEffect(savedStreakStartTime) {
                if (savedStreakStartTime == null) {
                    preferences.initializeStreak()
                    StreakWidgetProvider.triggerUpdate(this@MainActivity)
                }
            }

            SentinelTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (isLoggedIn && !firewallState.isActive) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 3.dp
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == Screen.Dashboard,
                                onClick = { currentScreen = Screen.Dashboard },
                                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                label = { Text("Streak") }
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.Reports,
                                onClick = { currentScreen = Screen.Reports },
                                icon = { Icon(Icons.Default.Info, contentDescription = null) },
                                label = { Text("Reports") }
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.Settings,
                                onClick = { currentScreen = Screen.Settings },
                                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                label = { Text("Settings") }
                            )
                        }
                    }
                }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (!isLoggedIn) {
                            LoginScreen(onLoginSuccess = {
                                scope.launch { preferences.setLoggedIn(true) }
                            })
                        } else {
                            when (currentScreen) {
                                Screen.Dashboard -> {
                                    DashboardScreen(
                                        streakStartTime = savedStreakStartTime,
                                        onActivateFirewall = { viewModel.startFirewallFlow() },
                                        onReportRelapse = { showRelapseDialog = true },
                                        onLogout = {
                                            scope.launch { preferences.logout() }
                                        }
                                    )
                                }
                                Screen.Reports -> {
                                    ReportScreen(
                                        relapses = allRelapses,
                                        urgeLogs = allUrgeLogs,
                                        streakStartTime = savedStreakStartTime ?: System.currentTimeMillis(),
                                        savedLongestStreak = savedLongestStreak
                                    )
                                }
                                Screen.Settings -> {
                                    SettingsScreen(
                                        context = this@MainActivity,
                                        isAppLockEnabled = isAppLockEnabled,
                                        lockedApps = lockedApps,
                                        onToggleAppLock = { enabled ->
                                            scope.launch { preferences.setAppLockEnabled(enabled) }
                                        },
                                        onUpdateLockedApps = { apps ->
                                            scope.launch { preferences.setLockedApps(apps.joinToString(",")) }
                                        },
                                        onClearData = {
                                            relapseViewModel.clearAllData()
                                            scope.launch { 
                                                preferences.clearAllData() 
                                                StreakWidgetProvider.triggerUpdate(this@MainActivity)
                                            }
                                        }
                                    )
                                }
                            }

                            if (firewallState.isActive) {
                                FirewallOverlay(
                                    isActive = firewallState.isActive,
                                    currentProtocol = firewallState.currentProtocol,
                                    timerRemaining = firewallState.timerRemaining,
                                    showCheckIn = firewallState.showCheckIn,
                                    isVictorious = firewallState.isVictorious,
                                    onUrgeStillPresent = { viewModel.onUrgeStillPresent(it) },
                                    onProtocolDone = { viewModel.onProtocolFinished() },
                                    onProtocolAction = { protocol ->
                                        if (protocol.id == 2) { // Grayscale
                                            val intent = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                            startActivity(intent)
                                        }
                                    },
                                    onVictoryConfirmed = {
                                        relapseViewModel.logUrgeDefeated(firewallState.usedProtocolIds)
                                        scope.launch { 
                                            preferences.incrementUrgeDefeated() 
                                            preferences.clearFirewallState()
                                        }
                                        viewModel.dismissFirewall()
                                    },
                                    onDismiss = { 
                                        scope.launch { preferences.clearFirewallState() }
                                        viewModel.dismissFirewall() 
                                    }
                                )

                                if (firewallState.needsSelection) {
                                    ProtocolSelectionDialog(
                                        usedProtocols = firewallState.usedProtocols,
                                        onProtocolSelected = { 
                                            vibrateDevice()
                                            scope.launch {
                                                preferences.saveFirewallState(it.id, firewallState.usedProtocolIds.plus(it.id).joinToString(","))
                                            }
                                            viewModel.activateFirewall(it) 
                                        },
                                        onDismiss = { 
                                            scope.launch { preferences.clearFirewallState() }
                                            viewModel.dismissFirewall() 
                                        }
                                    )
                                }
                            }

                            if (showRelapseDialog) {
                                RelapseDialog(
                                    onDismiss = { showRelapseDialog = false },
                                    onConfirm = { cause, note ->
                                        scope.launch {
                                            relapseViewModel.logRelapse(cause, note)
                                            preferences.resetStreak()
                                            StreakWidgetProvider.triggerUpdate(this@MainActivity)
                                            showRelapseDialog = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun vibrateDevice() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }
}