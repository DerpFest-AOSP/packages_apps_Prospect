package org.derpfest.prospect

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.kieronquinn.app.smartspacer.sdk.SmartspacerData

/**
 * Implementation of App Widget functionality.
 */
class StayDerped : AppWidgetProvider() {
    
    private fun requestSmartspacerUpdate(context: Context) {
        // Request an update from Smartspacer
        context.sendBroadcast(Intent(SmartspacerData.ACTION_REQUEST_UPDATE).apply {
            `package` = "com.kieronquinn.app.smartspacer" // Package name of Smartspacer
        })
    }

    override fun onEnabled(context: Context) {
        // Called when the first widget instance is added
        requestSmartspacerUpdate(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Update the widget
        requestSmartspacerUpdate(context)
    }

    override fun onDisabled(context: Context) {
        // Called when the last widget instance is removed
    }
}
