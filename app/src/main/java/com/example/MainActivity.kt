package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.ChronoApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        applyDisplayRefreshRate(prefer120Hz = true)
        setContent {
            ChronoApp(
                onRefreshRatePreferenceChanged = { prefer120Hz ->
                    applyDisplayRefreshRate(prefer120Hz)
                }
            )
        }
    }

    private fun applyDisplayRefreshRate(prefer120Hz: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val currentDisplay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay
            } ?: return

            val modes = currentDisplay.supportedModes
            val targetMode = if (prefer120Hz) {
                modes.maxByOrNull { it.refreshRate }
            } else {
                modes.minByOrNull { kotlin.math.abs(it.refreshRate - 60f) }
            }
            if (targetMode != null) {
                val params = window.attributes
                params.preferredDisplayModeId = targetMode.modeId
                window.attributes = params
            }
        }
    }
}
