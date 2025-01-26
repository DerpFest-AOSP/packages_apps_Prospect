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
import org.derpfest.prospect.config.WidgetConfigActivity

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
            reportIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

            val batteryStatus: Intent? =
                IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { intentFilter ->
                    context.registerReceiver(null, intentFilter)
                }

            batteryStatus?.let { mIntent ->
                // Battery percentage
                val level: Int = mIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale: Int = mIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryPct = level * 100 / scale.toFloat()
                reportIntent.putExtra("BatteryPercentage", batteryPct)

                // Battery temperature
                val temp = mIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f
                reportIntent.putExtra("BatteryTemperature", temp)

                // Battery health
                val health = mIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
                reportIntent.putExtra("BatteryHealth", health)

                // Charging status
                val status = mIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                                status == BatteryManager.BATTERY_STATUS_FULL
                reportIntent.putExtra("BatteryCharging", isCharging)

                // Get charging type if charging
                if (isCharging) {
                    val chargingType = when (mIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
                        BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                        BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "WIRELESS"
                        else -> "UNKNOWN"
                    }
                    reportIntent.putExtra("ChargingType", chargingType)
                }
            }

            sendBroadcast(reportIntent)
        }
    }
}
