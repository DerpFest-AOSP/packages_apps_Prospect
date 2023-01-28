package org.derpfest.prospect

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.util.Log
import android.widget.RemoteViews
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
            val batteryPercentage: Float = intent.getFloatExtra("BatteryPercentage", -1f)

            val remoteViews = RemoteViews(context.packageName, R.layout.noblesse)
            remoteViews.setTextViewText(
                R.id.noblesse_percentage,
                batteryPercentage.toInt().toString() + "%"
            )
            val mPaddingSize = 165 - 0.01 * batteryPercentage * 165
            val pxPaddingSize = (mPaddingSize * Resources.getSystem().displayMetrics.density).toInt()
            remoteViews.setViewPadding(R.id.noblesse_status_block, 0, 0, pxPaddingSize, 0)
            AppWidgetManager.getInstance(context).updateAppWidget(
                ComponentName(context, Noblesse::class.java), remoteViews
            )
        }
        super.onReceive(context, intent)
    }
}