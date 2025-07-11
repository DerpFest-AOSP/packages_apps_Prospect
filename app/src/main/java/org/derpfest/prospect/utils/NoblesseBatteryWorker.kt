/*
 * SPDX-FileCopyrightText: 2023 DerpFest
 * SPDX-License-Identifier: Apache-2.0
 */

package org.derpfest.prospect.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.work.*
import org.derpfest.prospect.Noblesse
import java.util.concurrent.TimeUnit

class NoblesseBatteryWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        return try {
            updateBatteryWidget()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun updateBatteryWidget() {
        val reportIntent = Intent(applicationContext, Noblesse::class.java)
        reportIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(applicationContext)
            .getAppWidgetIds(ComponentName(applicationContext, Noblesse::class.java))
        reportIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

        val batteryStatus: Intent? =
            IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { intentFilter ->
                applicationContext.registerReceiver(null, intentFilter)
            }
        val batteryPct: Float? = batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale.toFloat()
        }
        reportIntent.putExtra("BatteryPercentage", batteryPct)
        applicationContext.sendBroadcast(reportIntent)
    }

    companion object {
        private const val WORK_NAME = "NoblesseBatteryWork"

        fun startBatteryMonitoring(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()

            val batteryWorkRequest = PeriodicWorkRequestBuilder<NoblesseBatteryWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                batteryWorkRequest
            )
        }

        fun stopBatteryMonitoring(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}