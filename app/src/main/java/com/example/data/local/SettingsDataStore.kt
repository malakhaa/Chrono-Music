package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.ChronoSettings
import com.example.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.chronoDataStore: DataStore<Preferences> by preferencesDataStore(name = "chrono_music_settings")

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val GLASS_INTENSITY = floatPreferencesKey("glass_intensity")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val TALKBACK_ANNOUNCEMENTS = booleanPreferencesKey("talkback_announcements")

        val REFRESH_120HZ = booleanPreferencesKey("refresh_rate_120hz")
        val AUDIO_BUFFER_SAMPLES = intPreferencesKey("audio_buffer_samples")
        val PRECACHE_NEXT = booleanPreferencesKey("precache_next_track")
        val HARDWARE_VISUALIZER = booleanPreferencesKey("hardware_visualizer")

        val LOSSLESS_STREAMING = booleanPreferencesKey("lossless_streaming_mode")
        val OUTPUT_BIT_DEPTH = intPreferencesKey("output_bit_depth")
        val RESAMPLER_QUALITY = stringPreferencesKey("resampler_quality")
        val GAPLESS_PLAYBACK = booleanPreferencesKey("gapless_playback")
        val REPLAY_GAIN = booleanPreferencesKey("replay_gain")
        val OFFLINE_ONLY = booleanPreferencesKey("offline_mode_only")
        val RELEASE_ALERTS = booleanPreferencesKey("release_alerts_enabled")

        val USB_EXCLUSIVE_DAC = booleanPreferencesKey("usb_exclusive_dac_mode")
        val USB_HARDWARE_VOL = booleanPreferencesKey("usb_hardware_volume")
        val USB_DOP = booleanPreferencesKey("usb_dsd_over_pcm")
        val USB_TARGET_RATE = intPreferencesKey("usb_target_sample_rate")

        val BT_HIRES_MODE = booleanPreferencesKey("bt_hires_mode")
        val BT_PREFERRED_CODEC = stringPreferencesKey("bt_preferred_codec")
        val BT_LOW_LATENCY = booleanPreferencesKey("bt_low_latency_multipoint")
        val BT_RANGE_STABILITY = booleanPreferencesKey("bt_range_stability_boost")
        val BT_MIN_BITRATE = intPreferencesKey("bt_min_bitrate_floor")

        val ACTIVE_EQ_PRESET_ID = stringPreferencesKey("active_eq_preset_id")
        val EQ_ENABLED = booleanPreferencesKey("eq_enabled")
    }

    val settingsFlow: Flow<ChronoSettings> = context.chronoDataStore.data.map { prefs ->
        ChronoSettings(
            themeMode = runCatching {
                ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.DARK.name)
            }.getOrDefault(ThemeMode.DARK),
            highContrastMode = prefs[Keys.HIGH_CONTRAST] ?: false,
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: false,
            glassIntensity = prefs[Keys.GLASS_INTENSITY] ?: 0.72f,
            reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
            talkBackAnnouncements = prefs[Keys.TALKBACK_ANNOUNCEMENTS] ?: true,

            refreshRate120Hz = prefs[Keys.REFRESH_120HZ] ?: true,
            audioBufferSamples = prefs[Keys.AUDIO_BUFFER_SAMPLES] ?: 1024,
            precacheNextTrack = prefs[Keys.PRECACHE_NEXT] ?: true,
            hardwareVisualizer = prefs[Keys.HARDWARE_VISUALIZER] ?: true,

            losslessStreamingMode = prefs[Keys.LOSSLESS_STREAMING] ?: true,
            outputBitDepth = prefs[Keys.OUTPUT_BIT_DEPTH] ?: 24,
            resamplerQuality = prefs[Keys.RESAMPLER_QUALITY] ?: "Sinc Best (Ultra-Linear)",
            gaplessPlayback = prefs[Keys.GAPLESS_PLAYBACK] ?: true,
            replayGainEnabled = prefs[Keys.REPLAY_GAIN] ?: false,
            offlineModeOnly = prefs[Keys.OFFLINE_ONLY] ?: false,
            releaseAlertsEnabled = prefs[Keys.RELEASE_ALERTS] ?: false,

            usbExclusiveDacMode = prefs[Keys.USB_EXCLUSIVE_DAC] ?: true,
            usbHardwareVolumeControl = prefs[Keys.USB_HARDWARE_VOL] ?: true,
            usbDsdOverPcm = prefs[Keys.USB_DOP] ?: true,
            usbTargetSampleRateHz = prefs[Keys.USB_TARGET_RATE] ?: 0,

            btHiResMode = prefs[Keys.BT_HIRES_MODE] ?: true,
            btPreferredCodec = prefs[Keys.BT_PREFERRED_CODEC] ?: "LDAC 990kbps",
            btLowLatencyMultipoint = prefs[Keys.BT_LOW_LATENCY] ?: true,
            btRangeStabilityBoost = prefs[Keys.BT_RANGE_STABILITY] ?: true,
            btMinBitrateFloorKbps = prefs[Keys.BT_MIN_BITRATE] ?: 660
        )
    }

    val activeEqPresetIdFlow: Flow<String> = context.chronoDataStore.data.map { prefs ->
        prefs[Keys.ACTIVE_EQ_PRESET_ID] ?: "eq_reference"
    }

    val eqEnabledFlow: Flow<Boolean> = context.chronoDataStore.data.map { prefs ->
        prefs[Keys.EQ_ENABLED] ?: true
    }

    suspend fun updateSettings(transform: (ChronoSettings) -> ChronoSettings) {
        context.chronoDataStore.edit { prefs ->
            val current = ChronoSettings(
                themeMode = runCatching {
                    ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.DARK.name)
                }.getOrDefault(ThemeMode.DARK),
                highContrastMode = prefs[Keys.HIGH_CONTRAST] ?: false,
                dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: false,
                glassIntensity = prefs[Keys.GLASS_INTENSITY] ?: 0.72f,
                reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
                talkBackAnnouncements = prefs[Keys.TALKBACK_ANNOUNCEMENTS] ?: true,
                refreshRate120Hz = prefs[Keys.REFRESH_120HZ] ?: true,
                audioBufferSamples = prefs[Keys.AUDIO_BUFFER_SAMPLES] ?: 1024,
                precacheNextTrack = prefs[Keys.PRECACHE_NEXT] ?: true,
                hardwareVisualizer = prefs[Keys.HARDWARE_VISUALIZER] ?: true,
                losslessStreamingMode = prefs[Keys.LOSSLESS_STREAMING] ?: true,
                outputBitDepth = prefs[Keys.OUTPUT_BIT_DEPTH] ?: 24,
                resamplerQuality = prefs[Keys.RESAMPLER_QUALITY] ?: "Sinc Best (Ultra-Linear)",
                gaplessPlayback = prefs[Keys.GAPLESS_PLAYBACK] ?: true,
                replayGainEnabled = prefs[Keys.REPLAY_GAIN] ?: false,
                offlineModeOnly = prefs[Keys.OFFLINE_ONLY] ?: false,
                releaseAlertsEnabled = prefs[Keys.RELEASE_ALERTS] ?: false,
                usbExclusiveDacMode = prefs[Keys.USB_EXCLUSIVE_DAC] ?: true,
                usbHardwareVolumeControl = prefs[Keys.USB_HARDWARE_VOL] ?: true,
                usbDsdOverPcm = prefs[Keys.USB_DOP] ?: true,
                usbTargetSampleRateHz = prefs[Keys.USB_TARGET_RATE] ?: 0,
                btHiResMode = prefs[Keys.BT_HIRES_MODE] ?: true,
                btPreferredCodec = prefs[Keys.BT_PREFERRED_CODEC] ?: "LDAC 990kbps",
                btLowLatencyMultipoint = prefs[Keys.BT_LOW_LATENCY] ?: true,
                btRangeStabilityBoost = prefs[Keys.BT_RANGE_STABILITY] ?: true,
                btMinBitrateFloorKbps = prefs[Keys.BT_MIN_BITRATE] ?: 660
            )
            val updated = transform(current)
            prefs[Keys.THEME_MODE] = updated.themeMode.name
            prefs[Keys.HIGH_CONTRAST] = updated.highContrastMode
            prefs[Keys.DYNAMIC_COLOR] = updated.dynamicColor
            prefs[Keys.GLASS_INTENSITY] = updated.glassIntensity
            prefs[Keys.REDUCED_MOTION] = updated.reducedMotion
            prefs[Keys.TALKBACK_ANNOUNCEMENTS] = updated.talkBackAnnouncements
            prefs[Keys.REFRESH_120HZ] = updated.refreshRate120Hz
            prefs[Keys.AUDIO_BUFFER_SAMPLES] = updated.audioBufferSamples
            prefs[Keys.PRECACHE_NEXT] = updated.precacheNextTrack
            prefs[Keys.HARDWARE_VISUALIZER] = updated.hardwareVisualizer
            prefs[Keys.LOSSLESS_STREAMING] = updated.losslessStreamingMode
            prefs[Keys.OUTPUT_BIT_DEPTH] = updated.outputBitDepth
            prefs[Keys.RESAMPLER_QUALITY] = updated.resamplerQuality
            prefs[Keys.GAPLESS_PLAYBACK] = updated.gaplessPlayback
            prefs[Keys.REPLAY_GAIN] = updated.replayGainEnabled
            prefs[Keys.OFFLINE_ONLY] = updated.offlineModeOnly
            prefs[Keys.RELEASE_ALERTS] = updated.releaseAlertsEnabled
            prefs[Keys.USB_EXCLUSIVE_DAC] = updated.usbExclusiveDacMode
            prefs[Keys.USB_HARDWARE_VOL] = updated.usbHardwareVolumeControl
            prefs[Keys.USB_DOP] = updated.usbDsdOverPcm
            prefs[Keys.USB_TARGET_RATE] = updated.usbTargetSampleRateHz
            prefs[Keys.BT_HIRES_MODE] = updated.btHiResMode
            prefs[Keys.BT_PREFERRED_CODEC] = updated.btPreferredCodec
            prefs[Keys.BT_LOW_LATENCY] = updated.btLowLatencyMultipoint
            prefs[Keys.BT_RANGE_STABILITY] = updated.btRangeStabilityBoost
            prefs[Keys.BT_MIN_BITRATE] = updated.btMinBitrateFloorKbps
        }
    }

    suspend fun setActiveEqPresetId(presetId: String) {
        context.chronoDataStore.edit { it[Keys.ACTIVE_EQ_PRESET_ID] = presetId }
    }

    suspend fun setEqEnabled(enabled: Boolean) {
        context.chronoDataStore.edit { it[Keys.EQ_ENABLED] = enabled }
    }
}
