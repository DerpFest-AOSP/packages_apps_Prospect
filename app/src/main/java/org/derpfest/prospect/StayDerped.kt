package org.derpfest.prospect

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kieronquinn.app.smartspacer.sdk.SmartspacerData

class StayDerped : AppWidgetProvider() {

    companion object {
        private const val TAG = "StayDerped"
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "Widget enabled")
        requestSmartspacerUpdate(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d(TAG, "Widget onUpdate called")
        requestSmartspacerUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "Widget disabled")
    }

    override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
        Log.d(TAG, "Widget restored")
    }

    private fun requestSmartspacerUpdate(context: Context) {
        Log.d(TAG, "Requesting Smartspacer update")
        context.sendBroadcast(Intent(SmartspacerData.ACTION_REQUEST_UPDATE).apply {
            `package` = "com.kieronquinn.app.smartspacer"
        })
    }
}
