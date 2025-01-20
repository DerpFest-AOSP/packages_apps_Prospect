package org.derpfest.prospect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kieronquinn.app.smartspacer.sdk.SmartspacerData

class SmartSpacerReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "SmartSpacerReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: ${intent.action}")

        // Get the Smartspacer data from the intent
        if (intent.action == SmartspacerData.ACTION_UPDATE) {
            val data = SmartspacerData.fromIntent(intent)

            // Handle the received data and update the widget accordingly
            data?.let {
                updateWidget(context, it)
            }
        }
    }

    private fun updateWidget(context: Context, data: SmartspacerData) {
        // Use AppWidgetManager and RemoteViews to update your widget
        // based on the data received from Smartspacer.

        // TODO (properly adapt this to widget's layout):
        /*
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, StayDerped::class.java)
        )

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.stay_derped)

            // Get a target (e.g., the first one if there are multiple)
            val target = data.targets.firstOrNull()

            // Get a card (e.g., the first one if there are multiple)
            val card = target?.pages?.firstOrNull()?.cards?.firstOrNull()

            // Update text views
            views.setTextViewText(R.id.smartspacer_title, card?.title ?: "")
            views.setTextViewText(R.id.smartspacer_subtitle, card?.subtitle ?: "")

            // TODO (need to handle image loading)
            // ...

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        */

        Log.d(TAG, "Widget update logic not fully implemented yet.")
    }
}
