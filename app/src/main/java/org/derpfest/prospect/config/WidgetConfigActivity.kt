/*
 * SPDX-FileCopyrightText: 2024 DerpFest
 * SPDX-License-Identifier: Apache-2.0
 */

package org.derpfest.prospect.config

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.*
import org.derpfest.prospect.R

class WidgetConfigActivity : Activity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var showBatteryTemp: CheckBox
    private lateinit var showBatteryHealth: CheckBox
    private lateinit var updateInterval: Spinner
    private lateinit var themeSelection: RadioGroup
    private lateinit var widgetPreview: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        setContentView(R.layout.widget_config_activity)

        // Find the widget ID from the intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Initialize views
        showBatteryTemp = findViewById(R.id.showBatteryTemp)
        showBatteryHealth = findViewById(R.id.showBatteryHealth)
        updateInterval = findViewById(R.id.updateInterval)
        themeSelection = findViewById(R.id.themeSelection)
        widgetPreview = findViewById(R.id.widget_preview)

        // Setup spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.update_intervals,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            updateInterval.adapter = adapter
        }

        // Setup theme selection listener
        themeSelection.setOnCheckedChangeListener { _, checkedId ->
            val previewDrawable = when (checkedId) {
                R.id.themeDark -> R.drawable.noblesse_preview_dark
                R.id.themeLight -> R.drawable.noblesse_preview_light
                else -> R.drawable.noblesse_preview_default
            }
            widgetPreview.setImageResource(previewDrawable)
        }

        // Load saved preferences if any
        loadPreferences()

        // Setup confirm button
        findViewById<Button>(R.id.confirmButton).setOnClickListener {
            savePreferences()
            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }

    private fun loadPreferences() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        showBatteryTemp.isChecked = prefs.getBoolean(getPreferenceKey(PREF_SHOW_TEMP), false)
        showBatteryHealth.isChecked = prefs.getBoolean(getPreferenceKey(PREF_SHOW_HEALTH), false)
        updateInterval.setSelection(prefs.getInt(getPreferenceKey(PREF_UPDATE_INTERVAL), 0))
        
        // Load theme and update preview
        val savedTheme = prefs.getInt(getPreferenceKey(PREF_THEME), R.id.themeDefault)
        themeSelection.check(savedTheme)
        val previewDrawable = when (savedTheme) {
            R.id.themeDark -> R.drawable.noblesse_preview_dark
            R.id.themeLight -> R.drawable.noblesse_preview_light
            else -> R.drawable.noblesse_preview_default
        }
        widgetPreview.setImageResource(previewDrawable)
    }

    private fun savePreferences() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().apply {
            putBoolean(getPreferenceKey(PREF_SHOW_TEMP), showBatteryTemp.isChecked)
            putBoolean(getPreferenceKey(PREF_SHOW_HEALTH), showBatteryHealth.isChecked)
            putInt(getPreferenceKey(PREF_UPDATE_INTERVAL), updateInterval.selectedItemPosition)
            putInt(getPreferenceKey(PREF_THEME), themeSelection.checkedRadioButtonId)
            apply()
        }
    }

    private fun getPreferenceKey(key: String): String {
        return "${key}_$appWidgetId"
    }

    companion object {
        const val PREFS_NAME = "org.derpfest.prospect.WidgetPrefs"
        const val PREF_SHOW_TEMP = "show_temp"
        const val PREF_SHOW_HEALTH = "show_health"
        const val PREF_UPDATE_INTERVAL = "update_interval"
        const val PREF_THEME = "theme"
    }
} 