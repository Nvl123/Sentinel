package com.dicoding.sentinel.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.dicoding.sentinel.R
import com.dicoding.sentinel.data.local.SentinelPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StreakWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val preferences = SentinelPreference(context)
        val scope = CoroutineScope(Dispatchers.IO)

        for (appWidgetId in appWidgetIds) {
            scope.launch {
                updateWidgetData(context, appWidgetManager, appWidgetId, preferences)
            }
        }
    }

    private suspend fun updateWidgetData(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        preferences: SentinelPreference
    ) {
        val streakStartTime = preferences.streakStartTime.first() ?: System.currentTimeMillis()
        val database = com.dicoding.sentinel.data.local.SentinelDatabase.getDatabase(context)
        val relapses = database.relapseDao().getAllRelapses().first()
        
        val views = RemoteViews(context.packageName, R.layout.streak_widget)
        
        val currentTime = System.currentTimeMillis()
        val diffMillis = (currentTime - streakStartTime).coerceAtLeast(0)
        
        // Chronometer setup
        val dayRemainderMillis = diffMillis % (24 * 3600 * 1000)
        val hours = (dayRemainderMillis / (3600 * 1000)).toInt()
        val baseTime = android.os.SystemClock.elapsedRealtime() - dayRemainderMillis
        
        // Force HH:MM:SS format using Chronometer's format string
        val format = when {
            hours == 0 -> "00:%s"
            hours < 10 -> "0%s"
            else -> "%s"
        }
        
        views.setChronometer(R.id.widget_chronometer, baseTime, format, true)
        views.setChronometerCountDown(R.id.widget_chronometer, false)

        // Day Count in main timer
        val totalDays = diffMillis / (24 * 3600 * 1000)
        if (totalDays > 0) {
            views.setViewVisibility(R.id.widget_day_text, android.view.View.VISIBLE)
            views.setTextViewText(R.id.widget_day_text, "$totalDays.")
        } else {
            views.setViewVisibility(R.id.widget_day_text, android.view.View.GONE)
        }

        // Weekly Indicators (sn, sl, rb, km, jm, sb, mg)
        val today = java.util.Calendar.getInstance()
        val currentDayOfWeek = today.get(java.util.Calendar.DAY_OF_WEEK) 
        
        val indicatorIds = listOf(
            R.id.widget_indicator_mon, R.id.widget_indicator_tue, R.id.widget_indicator_wed,
            R.id.widget_indicator_thu, R.id.widget_indicator_fri, R.id.widget_indicator_sat,
            R.id.widget_indicator_sun
        )
        
        val todayIndexInWeek = when(currentDayOfWeek) {
            java.util.Calendar.MONDAY -> 0
            java.util.Calendar.TUESDAY -> 1
            java.util.Calendar.WEDNESDAY -> 2
            java.util.Calendar.THURSDAY -> 3
            java.util.Calendar.FRIDAY -> 4
            java.util.Calendar.SATURDAY -> 5
            java.util.Calendar.SUNDAY -> 6
            else -> 0
        }

        // Get status for each day of the current week (Monday to Sunday)
        val weekStatus = arrayOfNulls<StreakStatus>(7)
        val calendar = java.util.Calendar.getInstance()
        // Force Monday as the start of the week for calculation
        while (calendar.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.MONDAY) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)

        for (i in 0..6) {
            val dayStart = calendar.timeInMillis
            val dayEnd = dayStart + (24 * 3600 * 1000) - 1
            
            val hasRelapse = relapses.any { it.timestamp in dayStart..dayEnd }
            val isClean = dayStart >= streakStartTime && dayStart < currentTime && !hasRelapse
            
            weekStatus[i] = when {
                hasRelapse -> StreakStatus.RELAPSE
                isClean -> StreakStatus.CLEAN
                i > todayIndexInWeek -> StreakStatus.FUTURE
                else -> StreakStatus.NONE
            }
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        // Apply visual logic (Worm + ✓/✕)
        for (i in 0..6) {
            val status = weekStatus[i]
            val viewId = indicatorIds[i]
            
            when (status) {
                StreakStatus.CLEAN -> {
                    val isLeftClean = i > 0 && weekStatus[i-1] == StreakStatus.CLEAN
                    val isRightClean = i < 6 && weekStatus[i+1] == StreakStatus.CLEAN
                    
                    val bgRes = when {
                        isLeftClean && isRightClean -> R.drawable.widget_day_bg_mid
                        isLeftClean -> R.drawable.widget_day_bg_end
                        isRightClean -> R.drawable.widget_day_bg_start
                        else -> R.drawable.widget_day_bg_single
                    }
                    views.setInt(viewId, "setBackgroundResource", bgRes)
                    views.setTextViewText(viewId, "✓")
                    views.setFloat(viewId, "setAlpha", 1.0f)
                }
                StreakStatus.RELAPSE -> {
                    views.setInt(viewId, "setBackgroundResource", R.drawable.widget_day_bg_relapse)
                    views.setTextViewText(viewId, "✕")
                    views.setFloat(viewId, "setAlpha", 1.0f)
                }
                else -> {
                    views.setInt(viewId, "setBackgroundResource", 0)
                    views.setTextViewText(viewId, "")
                    views.setFloat(viewId, "setAlpha", 0.3f)
                }
            }
        }

        // Opening the app on click
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            android.content.Intent(context, com.dicoding.sentinel.MainActivity::class.java),
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_background_root, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    enum class StreakStatus { CLEAN, RELAPSE, FUTURE, NONE }

    companion object {
        fun triggerUpdate(context: Context) {
            val intent = android.content.Intent(context, StreakWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val ids = AppWidgetManager.getInstance(context)
                    .getAppWidgetIds(android.content.ComponentName(context, StreakWidgetProvider::class.java))
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }
}
