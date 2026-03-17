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
        
        val views = RemoteViews(context.packageName, R.layout.streak_widget)
        
        // Chronometer.setBase expects elapsedRealtime (time since boot)
        // We calculate the offset: current boot time - (current wall time - start wall time)
        val elapsedMillis = System.currentTimeMillis() - streakStartTime
        val baseTime = android.os.SystemClock.elapsedRealtime() - elapsedMillis

        views.setChronometer(R.id.widget_chronometer, baseTime, null, false)
        views.setChronometerCountDown(R.id.widget_chronometer, false)

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
