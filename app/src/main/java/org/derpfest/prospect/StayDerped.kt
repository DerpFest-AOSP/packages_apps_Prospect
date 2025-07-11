package org.derpfest.prospect

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.widget.RemoteViews

/**
 * Implementation of App Widget functionality for StayDerped widget.
 * Displays current date and time with DerpFest branding.
 */
class StayDerped : AppWidgetProvider() {
    
    companion object {
        private const val ACTION_WIDGET_CLICK = "org.derpfest.prospect.WIDGET_CLICK"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update all active widgets
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
        super.onDisabled(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_WIDGET_CLICK -> {
                // Open default clock app when widget is clicked
                try {
                    val clockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(clockIntent)
                } catch (e: Exception) {
                    // Fallback to system clock if AlarmClock intent fails
                    try {
                        val fallbackIntent = Intent().apply {
                            action = Intent.ACTION_MAIN
                            addCategory(Intent.CATEGORY_LAUNCHER)
                            setClassName("com.android.deskclock", "com.android.deskclock.DeskClock")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(fallbackIntent)
                    } catch (ex: Exception) {
                        // If all else fails, just ignore the click
                    }
                }
            }
            
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                // Handle manual updates
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, StayDerped::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                onUpdate(context, appWidgetManager, appWidgetIds)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Construct the RemoteViews object
        val remoteViews = RemoteViews(context.packageName, R.layout.stay_derped)
        
        // Set up click intent for the entire widget
        val clickIntent = Intent(context, StayDerped::class.java).apply {
            action = ACTION_WIDGET_CLICK
        }
        val clickPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews.setOnClickPendingIntent(R.id.widget_root, clickPendingIntent)
        
        // The TextClock views will automatically update themselves,
        // so no manual time setting is needed
        
        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }
}
