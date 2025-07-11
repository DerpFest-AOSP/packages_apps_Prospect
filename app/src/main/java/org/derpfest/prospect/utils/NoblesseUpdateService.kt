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
            try {
                val batteryChangedReceiverFilter = IntentFilter()
                batteryChangedReceiverFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
                registerReceiver(batteryChangedReceiver, batteryChangedReceiverFilter)
                mReceiverTag = true // Fix memory leak: mark receiver as registered
                Log.d("NoblesseUpdateService", "Battery receiver registered")
            } catch (e: Exception) {
                Log.e("NoblesseUpdateService", "Failed to register battery receiver", e)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        if (mReceiverTag) {
            try {
                unregisterReceiver(batteryChangedReceiver)
                mReceiverTag = false // Reset flag
                Log.d("NoblesseUpdateService", "Battery receiver unregistered")
            } catch (e: Exception) {
                Log.e("NoblesseUpdateService", "Failed to unregister battery receiver", e)
            }
        }
        super.onDestroy()
    }

    inner class BatteryChangedReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            try {
                if (intent.action != Intent.ACTION_BATTERY_CHANGED) {
                    return
                }

                // Get battery info directly from the received intent (more efficient)
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                
                val batteryPct = if (level >= 0 && scale > 0) {
                    (level * 100 / scale.toFloat())
                } else {
                    Log.w("NoblesseUpdateService", "Invalid battery data: level=$level, scale=$scale")
                    return
                }

                // Create and send update intent
                val reportIntent = Intent(context, Noblesse::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra("BatteryPercentage", batteryPct)
                    
                    // Add widget IDs
                    val ids = AppWidgetManager.getInstance(application)
                        .getAppWidgetIds(ComponentName(application, Noblesse::class.java))
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                
                context.sendBroadcast(reportIntent)
                Log.d("NoblesseUpdateService", "Battery update sent: ${batteryPct.toInt()}%")
                
            } catch (e: Exception) {
                Log.e("NoblesseUpdateService", "Error processing battery change", e)
            }
        }
    }
}
