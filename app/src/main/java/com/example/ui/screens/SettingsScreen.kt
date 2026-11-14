package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BluetoothAudio
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.data.model.ChronoSettings
import com.example.data.model.ThemeMode
import com.example.ui.ChronoUiState
import com.example.ui.components.GlassCard
import com.example.ui.components.HiResBadge
import com.example.ui.theme.CertifiedEmerald
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    state: ChronoUiState,
    onUpdateSettings: ((ChronoSettings) -> ChronoSettings) -> Unit,
    onRefreshHardware: () -> Unit,
    onRequestBluetoothPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = state.settings
    val hw = state.hardwareStatus

    val btCodecs = listOf("LDAC 990kbps", "aptX Adaptive", "aptX HD", "LHDC V5", "AAC", "SBC")
    val sampleRates = listOf(0 to "Auto Bit-Perfect", 48000 to "48 kHz", 96000 to "96 kHz", 192000 to "192 kHz")
    val bufferSizes = listOf(256, 1024, 4096)
    val bitDepths = listOf(16, 24, 32)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Display & Themes
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionTitle(Icons.Filled.Palette, "Display & Themes")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Theme Appearance", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ThemeMode.entries) { mode ->
                        FilterChip(
                            selected = s.themeMode == mode,
                            onClick = { onUpdateSettings { it.copy(themeMode = mode) } },
                            label = { Text(mode.label) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                SettingSwitchRow(
                    title = "WCAG AAA High-Contrast Mode",
                    subtitle = "Pure black surfaces, stark white text, and high-visibility focus borders",
                    checked = s.highContrastMode,
                    testTag = "setting_high_contrast_switch",
                    onCheckedChange = { checked -> onUpdateSettings { it.copy(highContrastMode = checked) } }
                )
                SettingSwitchRow(
                    title = "Material You Dynamic Color",
                    subtitle = "Adapt accent palette to Android wallpaper (disabled in High-Contrast)",
                    checked = s.dynamicColor,
                    onCheckedChange = { checked -> onUpdateSettings { it.copy(dynamicColor = checked) } }
                )
                SettingSwitchRow(
                    title = "Reduced Motion",
                    subtitle = "Minimize spectrum animations and spring transitions",
                    checked = s.reducedMotion,
                    onCheckedChange = { checked -> onUpdateSettings { it.copy(reducedMotion = checked) } }
                )
                Text(
                    text = "Glassmorphic Depth Intensity: ${(s.glassIntensity * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = s.glassIntensity,
                    onValueChange = { v -> onUpdateSettings { it.copy(glassIntensity = v) } },
                    valueRange = 0.25f..1f
                )
            }
        }

        // 2. Performance Settings
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionTitle(Icons.Filled.Speed, "Performance Settings")
                Spacer(modifier = Modifier.height(8.dp))
                SettingSwitchRow(
                    title = "120Hz Ultra-Smooth Refresh Rate",
                    subtitle = "Requests highest native display refresh rate for fluid scrolling & UI",
                    checked = s.refreshRate120Hz,
                    testTag = "setting_120hz_switch",
                    onCheckedChange = { checked -> onUpdateSettings { it.copy(refreshRate120Hz = checked) } }
                )
                SettingSwitchRow(
                    title = "Pre-Cache Next Queue Track",
                    subtitle = "Eliminates decode stalls between high-bitrate lossless tracks",
                    checked = s.precacheNextTrack,
                    onCheckedChange = { checked -> onUpdateSettings { it.copy(precacheNextTrack = checked) } }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("Audio Engine Buffer Size", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    bufferSizes.forEach { samples ->
                        FilterChip(
                            selected = s.audioBufferSamples == samples,
                            onClick = { onUpdateSettings { it.copy(audioBufferSamples = samples) } },
                            label = { Text("$samples smp") },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        )
                    }
                }
            }
        }

        // 3. Audio Quality Options
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle(Icons.Filled.HighQuality, "Audio Quality Options")
                    HiResBadge(label = "HI-RES AUDIO", isCertified = true)
                }
                Spacer(modifier = Modifier.height(8.dp))
                SettingSwitchRow(
                    title = "Lossless Streaming Priority",
                    subtitle = "Prioritizes uncompressed bandwidth and disables lossy downsampling",
                    checked = s.losslessStreamingMode,
                    testTag = "setting_lossless_switch",
                    onCheckedChange = { checked -> onUpdateSettings { it.copy(losslessStreamingMode = checked) } }
                )
                SettingSwitchRow(
                    title = "Offline Mode Only",
                    subtitle = "Restrict catalog strictly to locally cached bit-perfect tracks",
                    checked = s.offlineModeOnly,
                    onCheckedChange = { checked -> onUpdateSettings { it.copy(offlineModeOnly = checked) } }
                )
                SettingSwitchRow(
                    title = "Gapless Playback & ReplayGain",
                    subtitle = "Zero-gap album transitions with studio loudness protection",
                    checked = s.gaplessPlayback,
                    onCheckedChange = { checked -> onUpdateSettings { it.copy(gaplessPlayback = checked) } }
                )
                SettingSwitchRow(
                    title = "New Release Alerts",
                    subtitle = "Local release notification channel (requests notification permission)",
                    checked = s.releaseAlertsEnabled,
                    onCheckedChange = { checked ->
                        if (checked) onRequestNotificationPermission()
                        onUpdateSettings { it.copy(releaseAlertsEnabled = checked) }
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("Internal Pipeline Bit Depth", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    bitDepths.forEach { bits ->
                        FilterChip(
                            selected = s.outputBitDepth == bits,
                            onClick = { onUpdateSettings { it.copy(outputBitDepth = bits) } },
                            label = { Text(if (bits == 32) "32-bit Float" else "$bits-bit") },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        )
                    }
                }
            }
        }

        // 4. Special: USB Audio Interface & External DAC
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setting_usb_dac_section")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle(Icons.Filled.Usb, "USB Audio Interface & DAC")
                    IconButton(
                        onClick = onRefreshHardware,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Rescan USB audio interfaces")
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HiResBadge(
                        label = if (hw.usbDacConnected) "USB DAC CONNECTED" else "NO EXTERNAL DAC",
                        isCertified = hw.usbDacConnected,
                        accentColor = if (hw.usbDacConnected) CertifiedEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HiResBadge(label = "24-BIT / 192kHz READY", isCertified = true)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (hw.usbDacConnected) {
                        "Hardware: ${hw.usbDacName} (${hw.usbMaxChannels} ch • ${hw.usbSupportedRatesHz.joinToString { "${it / 1000}kHz" }})"
                    } else {
                        "Connect a USB-C DAC or external audio interface to unlock bit-perfect direct hardware routing."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingSwitchRow(
                    title = "Exclusive USB DAC Direct Mode",
                    subtitle = "Bypass Android mixer resampling when an external DAC is attached",
                    checked = s.usbExclusiveDacMode,
                    onCheckedChange = { checked -> onUpdateSettings { it.copy(usbExclusiveDacMode = checked) } }
                )
                SettingSwitchRow(
                    title = "USB Hardware Volume Control",
                    subtitle = "Control DAC analog gain stage directly via device volume keys",
                    checked = s.usbHardwareVolumeControl,
                    onCheckedChange = { checked -> onUpdateSettings { it.copy(usbHardwareVolumeControl = checked) } }
                )
                SettingSwitchRow(
                    title = "DSD-over-PCM (DoP) Native Tunnel",
                    subtitle = "Wrap DSD64/DSD128 streams into 24-bit PCM frames for compatible DACs",
                    checked = s.usbDsdOverPcm,
                    onCheckedChange = { checked -> onUpdateSettings { it.copy(usbDsdOverPcm = checked) } }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("USB Target Sample Rate", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sampleRates) { (hz, label) ->
                        FilterChip(
                            selected = s.usbTargetSampleRateHz == hz,
                            onClick = { onUpdateSettings { it.copy(usbTargetSampleRateHz = hz) } },
                            label = { Text(label) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        )
                    }
                }
            }
        }

        // 5. Special: Bluetooth Hi-Res Mode
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setting_bluetooth_hires_section")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle(Icons.Filled.BluetoothAudio, "Bluetooth Hi-Res Mode")
                    HiResBadge(label = "HI-RES WIRELESS", isCertified = s.btHiResMode)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (hw.bluetoothConnected) {
                        "Connected: ${hw.bluetoothDeviceName} • ${hw.bluetoothCodecDetected}"
                    } else {
                        "Status: Waiting for Bluetooth A2DP / BLE Audio headphones or speakers"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onRequestBluetoothPermission,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Verify Bluetooth Audio Permission & Scan")
                }
                Spacer(modifier = Modifier.height(8.dp))
                SettingSwitchRow(
                    title = "Bluetooth Hi-Res Mode",
                    subtitle = "Lock 24-bit / 96kHz wireless pipeline for LDAC & aptX Adaptive sinks",
                    checked = s.btHiResMode,
                    testTag = "setting_bt_hires_switch",
                    onCheckedChange = { checked -> onUpdateSettings { it.copy(btHiResMode = checked) } }
                )
                SettingSwitchRow(
                    title = "Low-Latency Multi-Device Pairing",
                    subtitle = "Reduces wireless jitter buffer for dual-headphone & shared playback",
                    checked = s.btLowLatencyMultipoint,
                    onCheckedChange = { checked -> onUpdateSettings { it.copy(btLowLatencyMultipoint = checked) } }
                )
                SettingSwitchRow(
                    title = "Range & Signal Stability Optimization",
                    subtitle = "Dynamic packet-retry protection to prevent dropouts across rooms",
                    checked = s.btRangeStabilityBoost,
                    onCheckedChange = { checked -> onUpdateSettings { it.copy(btRangeStabilityBoost = checked) } }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("Preferred Wireless Codec", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(btCodecs) { codec ->
                        FilterChip(
                            selected = s.btPreferredCodec == codec,
                            onClick = { onUpdateSettings { it.copy(btPreferredCodec = codec) } },
                            label = { Text(codec) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        )
                    }
                }
            }
        }

        // 6. Accessibility & Local JSON Backup
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionTitle(Icons.Filled.AccessibilityNew, "Accessibility & Library Backup")
                Spacer(modifier = Modifier.height(8.dp))
                SettingSwitchRow(
                    title = "Screen Reader Live Announcements",
                    subtitle = "Announce track transitions and voice results via TalkBack live regions",
                    checked = s.talkBackAnnouncements,
                    onCheckedChange = { checked -> onUpdateSettings { it.copy(talkBackAnnouncements = checked) } }
                )
                Text(
                    text = "Keyboard Navigation Shortcuts: Space = Play/Pause • N = Next Track • P = Previous Track",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onExportBackup,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Filled.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Export Backup")
                    }
                    OutlinedButton(
                        onClick = onImportBackup,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text("Restore Backup")
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(92.dp)) }
    }
}

@Composable
private fun SectionTitle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() }
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
        )
    }
}
