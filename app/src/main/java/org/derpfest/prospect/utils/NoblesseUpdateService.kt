/*
 * SPDX-FileCopyrightText: 2023 DerpFest
 * SPDX-License-Identifier: Apache-2.0
 */

package org.derpfest.prospect.utils

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.*
import android.os.BatteryManager
import android.os.IBinder
import android.util.Log
import org.derpfest.prospect.Noblesse


class NoblesseUpdateService : Service() {

    private var mReceiverTag = false
    private var batteryChangedReceiver = BatteryChangedReceiver()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!mReceiverTag) {
            val batteryChangedReceiverFilter = IntentFilter()
            batteryChangedReceiverFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
            registerReceiver(batteryChangedReceiver, batteryChangedReceiverFilter)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        unregisterReceiver(batteryChangedReceiver)
        super.onDestroy()
    }

    inner class BatteryChangedReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val reportIntent = Intent(context, Noblesse::class.java)
            reportIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(application)
                .getAppWidgetIds(ComponentName(application, Noblesse::class.java))
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

            val batteryStatus: Intent? =
                IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { intentFilter ->
                    context.registerReceiver(null, intentFilter)
                }
            val batteryPct: Float? = batteryStatus?.let { mIntent ->
                val level: Int = mIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale: Int = mIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                level * 100 / scale.toFloat()
            }
            reportIntent.putExtra("BatteryPercentage", batteryPct)
            sendBroadcast(reportIntent)
        }
    }
}
