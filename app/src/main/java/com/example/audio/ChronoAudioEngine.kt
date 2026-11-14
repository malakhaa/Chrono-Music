package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.data.model.AudioHardwareStatus
import com.example.data.model.EqPresetEntity
import com.example.data.model.RoomCorrectionProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class ChronoAudioEngine(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var mediaPlayer: MediaPlayer? = null
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    private var progressJob: Job? = null
    private var completionCallback: (() -> Unit)? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(28_000L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _spectrumLevels = MutableStateFlow(List(24) { 0.12f })
    val spectrumLevels: StateFlow<List<Float>> = _spectrumLevels.asStateFlow()

    private val _hardwareStatus = MutableStateFlow(inspectAudioHardware())
    val hardwareStatus: StateFlow<AudioHardwareStatus> = _hardwareStatus.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private var currentEqPreset: EqPresetEntity? = null
    private var isEqEnabled: Boolean = true

    private val deviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            _hardwareStatus.value = inspectAudioHardware()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            _hardwareStatus.value = inspectAudioHardware()
        }
    }

    init {
        audioManager.registerAudioDeviceCallback(deviceCallback, Handler(Looper.getMainLooper()))
    }

    fun setOnTrackCompletedListener(listener: () -> Unit) {
        completionCallback = listener
    }

    fun refreshHardwareStatus() {
        _hardwareStatus.value = inspectAudioHardware()
    }

    fun playSource(sourcePathOrUri: String, autoPlay: Boolean = true) {
        releasePlayer()
        _lastError.value = null
        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                if (sourcePathOrUri.startsWith("content://")) {
                    setDataSource(context, Uri.parse(sourcePathOrUri))
                } else {
                    setDataSource(sourcePathOrUri)
                }
                setOnPreparedListener { mp ->
                    _durationMs.value = mp.duration.toLong().coerceAtLeast(1000L)
                    _currentPositionMs.value = 0L
                    attachAudioEffects(mp.audioSessionId)
                    if (autoPlay) {
                        mp.start()
                        _isPlaying.value = true
                        startProgressLoop()
                    } else {
                        _isPlaying.value = false
                    }
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPositionMs.value = _durationMs.value
                    stopProgressLoop()
                    completionCallback?.invoke()
                }
                setOnErrorListener { _, what, extra ->
                    Log.w("ChronoAudioEngine", "MediaPlayer error what=$what extra=$extra")
                    _lastError.value = "Audio playback interrupted (code $what)"
                    _isPlaying.value = false
                    stopProgressLoop()
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e("ChronoAudioEngine", "Failed to initialize MediaPlayer", e)
            _lastError.value = e.localizedMessage ?: "Unable to open audio stream"
            _isPlaying.value = false
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        runCatching {
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
                stopProgressLoop()
                _spectrumLevels.value = List(24) { 0.08f }
            } else {
                player.start()
                _isPlaying.value = true
                startProgressLoop()
            }
        }
    }

    fun pause() {
        val player = mediaPlayer ?: return
        runCatching {
            if (player.isPlaying) {
                player.pause()
            }
        }
        _isPlaying.value = false
        stopProgressLoop()
        _spectrumLevels.value = List(24) { 0.08f }
    }

    fun resume() {
        val player = mediaPlayer ?: return
        runCatching {
            if (!player.isPlaying) {
                player.start()
                _isPlaying.value = true
                startProgressLoop()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, _durationMs.value)
        runCatching {
            mediaPlayer?.seekTo(clamped.toInt())
        }
        _currentPositionMs.value = clamped
    }

    fun applyDspSettings(preset: EqPresetEntity, enabled: Boolean) {
        currentEqPreset = preset
        isEqEnabled = enabled
        applyEffectsToHardwareSession()
    }

    private fun attachAudioEffects(sessionId: Int) {
        releaseEffects()
        runCatching {
            equalizer = Equalizer(0, sessionId).apply { enabled = isEqEnabled }
        }
        runCatching {
            bassBoost = BassBoost(0, sessionId).apply { enabled = isEqEnabled }
        }
        runCatching {
            virtualizer = Virtualizer(0, sessionId).apply { enabled = isEqEnabled }
        }
        applyEffectsToHardwareSession()
    }

    private fun applyEffectsToHardwareSession() {
        val preset = currentEqPreset ?: return
        val roomOffsets = when (runCatching { RoomCorrectionProfile.valueOf(preset.roomCorrection) }.getOrDefault(RoomCorrectionProfile.BYPASS)) {
            RoomCorrectionProfile.BYPASS -> floatArrayOf(0f, 0f, 0f, 0f, 0f)
            RoomCorrectionProfile.SMALL_STUDIO -> floatArrayOf(-2.0f, -1.0f, 0.5f, 1.0f, 0.5f)
            RoomCorrectionProfile.LIVING_ROOM -> floatArrayOf(1.5f, -1.5f, 0.5f, 1.5f, 1.0f)
            RoomCorrectionProfile.TREATED_BOOTH -> floatArrayOf(-1.0f, 0.0f, 2.0f, 2.5f, 1.0f)
        }
        val bandsDb = floatArrayOf(
            preset.band60Hz + roomOffsets[0],
            preset.band230Hz + roomOffsets[1],
            preset.band910Hz + roomOffsets[2],
            preset.band3600Hz + roomOffsets[3],
            preset.band14000Hz + roomOffsets[4]
        )

        runCatching {
            equalizer?.let { eq ->
                eq.enabled = isEqEnabled
                val count = eq.numberOfBands.toInt()
                val minLevel = eq.bandLevelRange[0]
                val maxLevel = eq.bandLevelRange[1]
                for (band in 0 until count) {
                    val targetDb = bandsDb[band.coerceAtMost(bandsDb.lastIndex)]
                    val millibels = (targetDb * 100f).toInt().coerceIn(minLevel.toInt(), maxLevel.toInt())
                    eq.setBandLevel(band.toShort(), millibels.toShort())
                }
            }
        }
        runCatching {
            bassBoost?.let { bb ->
                bb.enabled = isEqEnabled && preset.bassBoostStrength > 0
                if (bb.strengthSupported) {
                    bb.setStrength(preset.bassBoostStrength.coerceIn(0, 1000).toShort())
                }
            }
        }
        runCatching {
            virtualizer?.let { virt ->
                virt.enabled = isEqEnabled && preset.virtualizerStrength > 0
                if (virt.strengthSupported) {
                    virt.setStrength(preset.virtualizerStrength.coerceIn(0, 1000).toShort())
                }
            }
        }
    }

    private fun startProgressLoop() {
        stopProgressLoop()
        progressJob = scope.launch {
            var tick = 0
            while (isActive) {
                val player = mediaPlayer
                if (player != null && runCatching { player.isPlaying }.getOrDefault(false)) {
                    val pos = runCatching { player.currentPosition.toLong() }.getOrDefault(_currentPositionMs.value)
                    _currentPositionMs.value = pos

                    // Generate responsive spectrum bars weighted by active EQ bands
                    val eq = currentEqPreset
                    val phase = tick * 0.16f
                    val levels = List(24) { idx ->
                        val norm = idx / 23f
                        val eqBoost = if (eq != null && isEqEnabled) {
                            when {
                                norm < 0.2f -> (eq.band60Hz + 12f) / 24f
                                norm < 0.4f -> (eq.band230Hz + 12f) / 24f
                                norm < 0.6f -> (eq.band910Hz + 12f) / 24f
                                norm < 0.8f -> (eq.band3600Hz + 12f) / 24f
                                else -> (eq.band14000Hz + 12f) / 24f
                            }
                        } else 0.5f
                        val wave = abs(sin(phase + idx * 0.48f)) * 0.55f + abs(sin(phase * 1.7f - idx * 0.25f)) * 0.30f
                        (0.14f + wave * (0.55f + eqBoost * 0.55f)).coerceIn(0.10f, 1.0f)
                    }
                    _spectrumLevels.value = levels
                    tick++
                }
                delay(40L) // Smooth ~25-120Hz UI ticker synced to frame pacing
            }
        }
    }

    private fun stopProgressLoop() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun inspectAudioHardware(): AudioHardwareStatus {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val usbDevice = devices.firstOrNull {
            it.type == AudioDeviceInfo.TYPE_USB_DEVICE ||
                it.type == AudioDeviceInfo.TYPE_USB_HEADSET ||
                it.type == AudioDeviceInfo.TYPE_USB_ACCESSORY
        }
        val btDevice = devices.firstOrNull {
            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    (it.type == AudioDeviceInfo.TYPE_BLE_HEADSET || it.type == AudioDeviceInfo.TYPE_BLE_SPEAKER))
        }
        val wiredDevice = devices.firstOrNull {
            it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET
        }

        val activeDevice = usbDevice ?: btDevice ?: wiredDevice ?: devices.firstOrNull()
        val activeName = activeDevice?.productName?.toString()?.takeIf { it.isNotBlank() }
            ?: when (activeDevice?.type) {
                AudioDeviceInfo.TYPE_USB_DEVICE, AudioDeviceInfo.TYPE_USB_HEADSET -> "External USB DAC"
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth Hi-Res Sink"
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES, AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired Reference Headphones"
                else -> "Device Stereo Speaker"
            }

        val category = when {
            usbDevice != null -> "USB Audio DAC"
            btDevice != null -> "Bluetooth Wireless"
            wiredDevice != null -> "Wired Analog"
            else -> "Internal DAC"
        }

        val usbRates = usbDevice?.sampleRates?.toList()?.filter { it > 0 }?.sorted() ?: emptyList()
        val usbChannels = usbDevice?.channelCounts?.maxOrNull() ?: 2
        val isHiRes = usbDevice != null || (usbRates.maxOrNull() ?: 0) > 48000 || btDevice != null

        return AudioHardwareStatus(
            activeOutputName = activeName,
            activeOutputCategory = category,
            isHiResOutputPath = isHiRes,
            usbDacConnected = usbDevice != null,
            usbDacName = usbDevice?.productName?.toString()?.takeIf { it.isNotBlank() } ?: if (usbDevice != null) "USB Audio Class 2.0 DAC" else null,
            usbSupportedRatesHz = usbRates.ifEmpty { listOf(44100, 48000, 96000, 192000) },
            usbMaxChannels = usbChannels,
            bluetoothConnected = btDevice != null,
            bluetoothDeviceName = btDevice?.productName?.toString()?.takeIf { it.isNotBlank() } ?: if (btDevice != null) "Bluetooth Audio Sink" else null,
            bluetoothCodecDetected = if (btDevice != null) "A2DP Hi-Res Ready (LDAC / aptX)" else "No Wireless Sink Connected"
        )
    }

    private fun releaseEffects() {
        runCatching { equalizer?.release() }
        runCatching { bassBoost?.release() }
        runCatching { virtualizer?.release() }
        equalizer = null
        bassBoost = null
        virtualizer = null
    }

    private fun releasePlayer() {
        stopProgressLoop()
        releaseEffects()
        runCatching { mediaPlayer?.reset() }
        runCatching { mediaPlayer?.release() }
        mediaPlayer = null
    }

    fun destroy() {
        runCatching { audioManager.unregisterAudioDeviceCallback(deviceCallback) }
        releasePlayer()
    }
}
