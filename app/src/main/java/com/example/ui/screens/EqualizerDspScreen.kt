package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.data.model.EqPresetEntity
import com.example.data.model.RoomCorrectionProfile
import com.example.ui.ChronoUiState
import com.example.ui.components.GlassCard
import com.example.ui.components.HiResBadge
import kotlin.math.roundToInt

@Composable
fun EqualizerDspScreen(
    state: ChronoUiState,
    onToggleEqEnabled: (Boolean) -> Unit,
    onSelectPreset: (EqPresetEntity) -> Unit,
    onUpdateDraftEq: ((EqPresetEntity) -> EqPresetEntity) -> Unit,
    onSaveCustomPreset: (String) -> Unit,
    onDeletePreset: (EqPresetEntity) -> Unit,
    onSelectRoomCorrection: (RoomCorrectionProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    val active = state.activeEqPreset

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("equalizer_dsp_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Master Equalizer & Frequency Curve Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Modular Equalizer & DSP",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.semantics { heading() }
                        )
                        Text(
                            text = if (state.eqEnabled) "Active: ${active.name}" else "Bypassed (Bit-Direct)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.eqEnabled,
                        onCheckedChange = onToggleEqEnabled,
                        modifier = Modifier.testTag("eq_master_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                FrequencyCurveCanvas(
                    bands = listOf(
                        active.band60Hz,
                        active.band230Hz,
                        active.band910Hz,
                        active.band3600Hz,
                        active.band14000Hz
                    ),
                    enabled = state.eqEnabled
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Presets", style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Save Preset")
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.eqPresets, key = { it.id }) { preset ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilterChip(
                                selected = active.id == preset.id,
                                onClick = { onSelectPreset(preset) },
                                label = { Text(preset.name) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            )
                            if (!preset.isBuiltIn) {
                                IconButton(
                                    onClick = { onDeletePreset(preset) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete custom preset ${preset.name}",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5-Band Precision Sliders
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Parametric Bands (-12 dB to +12 dB)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(modifier = Modifier.height(8.dp))
                EqBandSlider("60 Hz Sub", active.band60Hz, state.eqEnabled) { db ->
                    onUpdateDraftEq { it.copy(band60Hz = db) }
                }
                EqBandSlider("230 Hz Warmth", active.band230Hz, state.eqEnabled) { db ->
                    onUpdateDraftEq { it.copy(band230Hz = db) }
                }
                EqBandSlider("910 Hz Presence", active.band910Hz, state.eqEnabled) { db ->
                    onUpdateDraftEq { it.copy(band910Hz = db) }
                }
                EqBandSlider("3.6 kHz Detail", active.band3600Hz, state.eqEnabled) { db ->
                    onUpdateDraftEq { it.copy(band3600Hz = db) }
                }
                EqBandSlider("14 kHz Air", active.band14000Hz, state.eqEnabled) { db ->
                    onUpdateDraftEq { it.copy(band14000Hz = db) }
                }
            }
        }

        // DSP Plugins Rack
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "DSP Plugins Rack",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.semantics { heading() }
                        )
                    }
                    HiResBadge(label = "64-BIT FLOAT DSP", isCertified = true)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Sub-Bass Harmonic Boost: ${active.bassBoostStrength / 10}%",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = active.bassBoostStrength.toFloat(),
                    onValueChange = { valInt -> onUpdateDraftEq { it.copy(bassBoostStrength = valInt.roundToInt()) } },
                    valueRange = 0f..1000f,
                    enabled = state.eqEnabled
                )

                Text(
                    text = "3D Soundstage Virtualizer: ${active.virtualizerStrength / 10}%",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = active.virtualizerStrength.toFloat(),
                    onValueChange = { valInt -> onUpdateDraftEq { it.copy(virtualizerStrength = valInt.roundToInt()) } },
                    valueRange = 0f..1000f,
                    enabled = state.eqEnabled
                )

                Text(
                    text = "Vacuum Tube Saturation Warmth: ${(active.tubeWarmth * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = active.tubeWarmth,
                    onValueChange = { warmth -> onUpdateDraftEq { it.copy(tubeWarmth = warmth) } },
                    valueRange = 0f..1f,
                    enabled = state.eqEnabled
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Binaural Headphone Crossfeed", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Reduces listening fatigue on hard-panned stereo mixes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = active.crossfeedEnabled,
                        onCheckedChange = { enabled -> onUpdateDraftEq { it.copy(crossfeedEnabled = enabled) } },
                        enabled = state.eqEnabled
                    )
                }
            }
        }

        // Room Correction Calibration
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Acoustic Room Correction",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(modifier = Modifier.height(6.dp))
                val currentProfile = runCatching { RoomCorrectionProfile.valueOf(active.roomCorrection) }
                    .getOrDefault(RoomCorrectionProfile.BYPASS)

                RoomCorrectionProfile.entries.forEach { profile ->
                    val selected = currentProfile == profile
                    Surface(
                        onClick = { onSelectRoomCorrection(profile) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .minimumInteractiveComponentSize()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = profile.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = profile.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(92.dp)) }
    }

    if (showSaveDialog) {
        var presetName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Custom EQ & DSP Preset") },
            text = {
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    label = { Text("Preset name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveCustomPreset(presetName)
                        showSaveDialog = false
                    },
                    enabled = presetName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun EqBandSlider(
    label: String,
    valueDb: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(
                text = "%+.1f dB".format(valueDb),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = valueDb,
            onValueChange = onValueChange,
            valueRange = -12f..12f,
            enabled = enabled,
            modifier = Modifier.semantics {
                contentDescription = "$label equalizer band set to ${valueDb.roundToInt()} decibels"
            }
        )
    }
}

@Composable
private fun FrequencyCurveCanvas(
    bands: List<Float>,
    enabled: Boolean
) {
    val lineColor = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val dotColor = MaterialTheme.colorScheme.secondary

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp)
            .semantics {
                contentDescription = "Equalizer frequency response curve across 5 bands"
            }
    ) {
        val midY = size.height / 2f
        // Zero dB baseline
        drawLine(
            color = gridColor,
            start = Offset(0f, midY),
            end = Offset(size.width, midY),
            strokeWidth = 2f
        )

        val points = bands.mapIndexed { idx, db ->
            val x = if (bands.size <= 1) 0f else (idx.toFloat() / (bands.size - 1)) * size.width
            val normY = ((-db / 12f) * (size.height * 0.42f)) + midY
            Offset(x, normY.coerceIn(8f, size.height - 8f))
        }

        if (points.size >= 2) {
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 0 until points.lastIndex) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val ctrlX = (p0.x + p1.x) / 2f
                    cubicTo(ctrlX, p0.y, ctrlX, p1.y, p1.x, p1.y)
                }
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )
        }

        points.forEach { pt ->
            drawCircle(color = dotColor, radius = 8f, center = pt)
        }
    }
}
