package org.derpfest.prospect

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import com.kieronquinn.app.smartspacer.sdk.SmartspacerData
import java.io.File
import java.io.FileOutputStream

class SmartSpacerReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "SmartSpacerReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: ${intent.action}")

        if (intent.action == SmartspacerData.ACTION_UPDATE) {
            val data = SmartspacerData.fromIntent(intent)
            data?.let {
                updateWidget(context, it)
            }
        }
    }

    private fun updateWidget(context: Context, data: SmartspacerData) {
        Log.d(TAG, "Updating widget...")
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, StayDerped::class.java)
        )

        appWidgetIds.forEach { appWidgetId ->
            val views = RemoteViews(context.packageName, R.layout.stay_derped)

            val target = data.targets.firstOrNull()
            val card = target?.pages?.firstOrNull()?.cards?.firstOrNull()

            Log.d(TAG, "Card data: title=${card?.title}, subtitle=${card?.subtitle}")

            views.setTextViewText(R.id.smartspacer_title, card?.title.orEmpty())
            views.setTextViewText(R.id.smartspacer_subtitle, card?.subtitle.orEmpty())

            card?.icon?.let { iconBitmap ->
                handleIconLoading(context, views, appWidgetId, iconBitmap)
            }

            card?.launchIntent?.let { launchIntent ->
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun handleIconLoading(context: Context, views: RemoteViews, appWidgetId: Int, iconBitmap: Bitmap) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Log.d(TAG, "Using setImageViewBitmap for API 31+")
                views.setImageViewBitmap(R.id.smartspacer_icon, iconBitmap)
            } else {
                Log.d(TAG, "Using file storage for image loading")
                val iconFile = File(context.cacheDir, "icon_$appWidgetId.png")

                FileOutputStream(iconFile).use { outputStream ->
                    iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }

                views.setImageViewUri(R.id.smartspacer_icon, Uri.fromFile(iconFile))
                iconFile.deleteOnExit()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling icon loading: ${e.message}", e)
        }
    }
}

