/*
 * SPDX-FileCopyrightText: 2023 DerpFest
 * SPDX-License-Identifier: Apache-2.0
 */

package org.derpfest.prospect

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.BatteryManager
import android.view.View
import android.widget.RemoteViews
import org.derpfest.prospect.config.WidgetConfigActivity
import org.derpfest.prospect.utils.NoblesseUpdateService

/**
 * Implementation of App Widget functionality.
 */
class Noblesse : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        context.startService(Intent(context, NoblesseUpdateService::class.java))
        // There may be multiple widgets active, so update all of them
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        context.startService(Intent(context, NoblesseUpdateService::class.java))
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        context.stopService(Intent(context, NoblesseUpdateService::class.java))
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val batteryPercentage = intent.getFloatExtra("BatteryPercentage", -1f)
            val batteryTemp = intent.getFloatExtra("BatteryTemperature", -1f)
            val batteryHealth = intent.getIntExtra("BatteryHealth", BatteryManager.BATTERY_HEALTH_UNKNOWN)
            val isCharging = intent.getBooleanExtra("BatteryCharging", false)
            val chargingType = intent.getStringExtra("ChargingType")

            val widgetManager = AppWidgetManager.getInstance(context)
            val widgetComponent = ComponentName(context, Noblesse::class.java)
            val widgetIds = widgetManager.getAppWidgetIds(widgetComponent)

            widgetIds.forEach { widgetId ->
                val prefs = context.getSharedPreferences(WidgetConfigActivity.PREFS_NAME, Context.MODE_PRIVATE)
                val showTemp = prefs.getBoolean(getPreferenceKey(WidgetConfigActivity.PREF_SHOW_TEMP, widgetId), false)
                val showHealth = prefs.getBoolean(getPreferenceKey(WidgetConfigActivity.PREF_SHOW_HEALTH, widgetId), false)
                val theme = prefs.getInt(getPreferenceKey(WidgetConfigActivity.PREF_THEME, widgetId), R.id.themeDefault)

                val remoteViews = RemoteViews(context.packageName, R.layout.noblesse)

                // Update battery percentage
                remoteViews.setTextViewText(
                    R.id.noblesse_percentage,
                    "${batteryPercentage.toInt()}%"
                )

                // Update battery level bar color based on percentage and theme
                val barColor = when {
                    batteryPercentage <= 15 -> context.getColor(R.color.battery_low)
                    batteryPercentage <= 50 -> context.getColor(R.color.battery_medium)
                    else -> context.getColor(R.color.battery_good)
                }
                remoteViews.setInt(R.id.battery_level_bar, "setBackgroundColor", barColor)

                // Update battery level bar width
                val levelWidth = (batteryPercentage / 100f * Resources.getSystem().displayMetrics.widthPixels).toInt()
                remoteViews.setViewLayoutWidth(R.id.battery_level_bar, levelWidth)

                // Show/hide charging indicator
                remoteViews.setViewVisibility(R.id.charging_indicator, if (isCharging) View.VISIBLE else View.GONE)

                // Update temperature if enabled
                if (showTemp && batteryTemp >= 0) {
                    remoteViews.setViewVisibility(R.id.battery_temp, View.VISIBLE)
                    remoteViews.setTextViewText(R.id.battery_temp, String.format("%.1f°C", batteryTemp))
                } else {
                    remoteViews.setViewVisibility(R.id.battery_temp, View.GONE)
                }

                // Update health status if enabled
                if (showHealth) {
                    remoteViews.setViewVisibility(R.id.battery_health, View.VISIBLE)
                    val healthText = when (batteryHealth) {
                        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated"
                        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failed"
                        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                        else -> "Unknown"
                    }
                    remoteViews.setTextViewText(R.id.battery_health, healthText)
                } else {
                    remoteViews.setViewVisibility(R.id.battery_health, View.GONE)
                }

                // Apply theme
                val textColor = when (theme) {
                    R.id.themeDark -> context.getColor(R.color.widget_text_dark)
                    R.id.themeLight -> context.getColor(R.color.widget_text_light)
                    else -> context.getColor(R.color.widget_text_default)
                }
                remoteViews.setTextColor(R.id.noblesse_percentage, textColor)
                remoteViews.setTextColor(R.id.battery_temp, textColor)
                remoteViews.setTextColor(R.id.battery_health, textColor)

                widgetManager.updateAppWidget(widgetId, remoteViews)
            }
        }
        super.onReceive(context, intent)
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val remoteViews = RemoteViews(context.packageName, R.layout.noblesse)
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }

    private fun getPreferenceKey(key: String, widgetId: Int): String {
        return "${key}_$widgetId"
    }
}
