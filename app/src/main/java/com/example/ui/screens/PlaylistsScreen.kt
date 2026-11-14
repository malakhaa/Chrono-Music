package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.MultiListeningMode
import com.example.data.model.PlaylistWithTracks
import com.example.data.model.TrackEntity
import com.example.ui.ChronoUiState
import com.example.ui.components.GlassCard
import com.example.ui.components.HiResBadge
import com.example.ui.theme.CertifiedEmerald
import kotlin.math.roundToInt

@Composable
fun PlaylistsScreen(
    state: ChronoUiState,
    onCreatePlaylist: (String, String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onPlayPlaylist: (PlaylistWithTracks) -> Unit,
    onPlayTrack: (TrackEntity) -> Unit,
    onRemoveTrackFromPlaylist: (String, String) -> Unit,
    onSharePlaylist: (PlaylistWithTracks) -> Unit,
    onSetListeningMode: (MultiListeningMode) -> Unit,
    onRegenerateSessionCode: () -> Unit,
    onJoinSessionCode: (String) -> Unit,
    onShareSessionInvite: (String) -> Unit,
    onToggleZoneActive: (String) -> Unit,
    onUpdateZoneLatency: (String, Int) -> Unit,
    onUpdateZoneVolume: (String, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableIntStateOf(0) } // 0 = Playlists, 1 = Multi-Listening & Multi-Room
    var showCreateDialog by remember { mutableStateOf(false) }
    var joinCodeInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("playlists_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                shape = SegmentedButtonDefaults.itemShape(0, 2),
                icon = { Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null) }
            ) {
                Text("Playlists")
            }
            SegmentedButton(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                shape = SegmentedButtonDefaults.itemShape(1, 2),
                icon = { Icon(Icons.Filled.SpeakerGroup, contentDescription = null) }
            ) {
                Text("Multi-Listening")
            }
        }

        if (selectedSection == 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Custom Playlists (${state.playlists.size})",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.semantics { heading() }
                )
                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("create_playlist_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("New Playlist")
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(state.playlists, key = { it.playlist.id }) { pw ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pw.playlist.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = pw.playlist.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row {
                                IconButton(
                                    onClick = { onPlayPlaylist(pw) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play ${pw.playlist.name}", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(
                                    onClick = { onSharePlaylist(pw) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = "Share ${pw.playlist.name}")
                                }
                                if (!pw.playlist.isCurated) {
                                    IconButton(
                                        onClick = { onDeletePlaylist(pw.playlist.id) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete ${pw.playlist.name}", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }

                        if (pw.tracks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            pw.tracks.forEach { track ->
                                Surface(
                                    onClick = { onPlayTrack(track) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .minimumInteractiveComponentSize()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(track.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("${track.artist} • ${track.formatLabel}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(
                                            onClick = { onRemoveTrackFromPlaylist(pw.playlist.id, track.id) },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Icon(Icons.Filled.RemoveCircleOutline, contentDescription = "Remove ${track.title} from playlist")
                                        }
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Empty playlist — tap '+' on any Library track to add songs.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Multi-Listening Modes",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.semantics { heading() }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        MultiListeningMode.entries.forEach { mode ->
                            val selected = state.listeningMode == mode
                            Surface(
                                onClick = { onSetListeningMode(mode) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .minimumInteractiveComponentSize()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = mode.subtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Shared Playback Session Card
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("Shared Playback Room", style = MaterialTheme.typography.titleLarge)
                            }
                            HiResBadge(label = "${state.sharedPeersCount} SYNCED", isCertified = false, accentColor = CertifiedEmerald)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Session Code: ${state.sharedSessionCode}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = { onShareSessionInvite(state.sharedSessionCode) }) {
                                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Invite Friends")
                            }
                            OutlinedButton(onClick = onRegenerateSessionCode) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("New Code")
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = joinCodeInput,
                                onValueChange = { joinCodeInput = it },
                                label = { Text("Join room code") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    onJoinSessionCode(joinCodeInput)
                                    joinCodeInput = ""
                                },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Text("Sync")
                            }
                        }
                    }
                }

                // Synchronized Multi-Room Zones
                item {
                    Text(
                        text = "Synchronized Multi-Room Zones",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() }
                    )
                }
                items(state.listeningZones, key = { it.id }) { zone ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(zone.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "${zone.hardwareType} • ${zone.signalDbm} dBm",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = zone.isActive,
                                onCheckedChange = { onToggleZoneActive(zone.id) }
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HiResBadge(label = zone.codecBadge, isCertified = true)
                        if (zone.isActive) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Clock Latency Offset: ${zone.latencyOffsetMs} ms",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Slider(
                                value = zone.latencyOffsetMs.toFloat(),
                                onValueChange = { onUpdateZoneLatency(zone.id, it.roundToInt()) },
                                valueRange = -50f..150f
                            )
                            Text(
                                text = "Zone Gain Trim: ${(zone.volumeTrim * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Slider(
                                value = zone.volumeTrim,
                                onValueChange = { onUpdateZoneVolume(zone.id, it) },
                                valueRange = 0f..1f
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Custom Playlist") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Playlist name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreatePlaylist(name, desc)
                        showCreateDialog = false
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }
}
