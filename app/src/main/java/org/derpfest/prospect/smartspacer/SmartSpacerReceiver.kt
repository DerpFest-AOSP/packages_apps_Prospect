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
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, StayDerped::class.java)
        )

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.stay_derped)

            // Get the first target and card (adjust logic as needed)
            val target = data.targets.firstOrNull()
            val card = target?.pages?.firstOrNull()?.cards?.firstOrNull()

           // Update text views
           views.setTextViewText(R.id.smartspacer_title, card?.title ?: "")
            views.setTextViewText(R.id.smartspacer_subtitle, card?.subtitle ?: "")

            // Handle image loading (using setImageViewBitmap for API 31+)
            val iconBitmap = card?.icon
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && iconBitmap != null) {
                views.setImageViewBitmap(R.id.smartspacer_icon, iconBitmap)
            } else {
                // Handle image loading for older APIs (using file storage)
                if (iconBitmap != null) {
                    val iconFile = File(context.cacheDir, "icon_$appWidgetId.png")
                    val outputStream = FileOutputStream(iconFile)
                    iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.close()

                    views.setImageViewUri(R.id.smartspacer_icon, Uri.fromFile(iconFile))

                    iconFile.deleteOnExit() // Or delete it later
                }
            }

           // Set click intent if available
            card?.launchIntent?.let { launchIntent ->
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent) // Or apply to a specific view
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
