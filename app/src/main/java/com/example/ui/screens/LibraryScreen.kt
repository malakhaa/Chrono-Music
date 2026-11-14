package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.PlaylistWithTracks
import com.example.data.model.TrackEntity
import com.example.data.model.formatDurationMs
import com.example.ui.ChronoUiState
import com.example.ui.components.AlbumArtwork
import com.example.ui.components.GlassCard
import com.example.ui.components.HiResBadge
import com.example.ui.theme.CertifiedEmerald

@Composable
fun LibraryScreen(
    state: ChronoUiState,
    onPlayTrack: (TrackEntity) -> Unit,
    onToggleFavorite: (TrackEntity) -> Unit,
    onToggleOfflineCache: (TrackEntity) -> Unit,
    onShareTrack: (TrackEntity) -> Unit,
    onSelectGenreFilter: (String) -> Unit,
    onImportAudioClick: () -> Unit,
    onToggleOfflineOnly: () -> Unit,
    onToggleHighContrast: () -> Unit,
    onAddTrackToPlaylist: (String, TrackEntity) -> Unit,
    onOpenNowPlaying: () -> Unit,
    modifier: Modifier = Modifier
) {
    var playlistTargetTrack by remember { mutableStateOf<TrackEntity?>(null) }

    val genres = listOf(
        "All", "Hi-Res", "Favorites", "Offline",
        "Ambient Electronic", "Synthwave", "Analog Jazz", "Neo-Classical"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Audiophile Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_chrono),
                    contentDescription = "Chrono Music audiophile listening studio banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.25f),
                                    Color.Black.copy(alpha = 0.86f)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HiResBadge(
                                label = if (state.settings.losslessStreamingMode) "LOSSLESS PRIORITY" else "STANDARD STREAM",
                                isCertified = state.settings.losslessStreamingMode
                            )
                            HiResBadge(
                                label = if (state.hardwareStatus.usbDacConnected) "USB DAC ACTIVE" else state.hardwareStatus.activeOutputCategory.uppercase(),
                                isCertified = state.hardwareStatus.isHiResOutputPath,
                                accentColor = CertifiedEmerald
                            )
                        }
                        Text(
                            text = "Chrono Studio Master",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            modifier = Modifier.semantics { heading() }
                        )
                        Text(
                            text = "${state.hardwareStatus.activeOutputName} • ${state.settings.outputBitDepth}-bit • ${if (state.settings.refreshRate120Hz) "120Hz Motion" else "60Hz"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.86f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Quick Studio Controls
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    FilterChip(
                        selected = false,
                        onClick = onImportAudioClick,
                        label = { Text("Import Local Audio") },
                        leadingIcon = {
                            Icon(Icons.Filled.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("import_audio_chip")
                    )
                }
                item {
                    FilterChip(
                        selected = state.settings.offlineModeOnly,
                        onClick = onToggleOfflineOnly,
                        label = { Text(if (state.settings.offlineModeOnly) "Offline Mode: ON" else "Offline Mode") },
                        leadingIcon = {
                            Icon(Icons.Filled.CloudOff, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("offline_mode_chip")
                    )
                }
                item {
                    FilterChip(
                        selected = state.settings.highContrastMode,
                        onClick = onToggleHighContrast,
                        label = { Text(if (state.settings.highContrastMode) "WCAG High Contrast" else "High Contrast") },
                        leadingIcon = {
                            Icon(Icons.Filled.Contrast, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("high_contrast_chip")
                    )
                }
            }
        }

        // Personalized Recommendations Carousel
        if (state.recommendedTracks.isNotEmpty() && state.searchQuery.isBlank()) {
            item {
                Text(
                    text = "Curated For Your Acoustics",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() }
                )
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.recommendedTracks, key = { "rec_${it.id}" }) { track ->
                        GlassCard(
                            modifier = Modifier
                                .width(196.dp)
                                .testTag("recommended_card_${track.id}"),
                            onClick = {
                                onPlayTrack(track)
                                onOpenNowPlaying()
                            }
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                AlbumArtwork(
                                    resName = track.artworkResName,
                                    contentDescription = "${track.title} album art",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(118.dp),
                                    cornerRadius = 12.dp
                                )
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.PlayArrow,
                                            contentDescription = "Play ${track.title}",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track.artist,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            HiResBadge(
                                label = "${track.formatLabel} ${track.bitDepth}b/${track.sampleRateHz / 1000}k",
                                isCertified = track.isHiResCertified
                            )
                        }
                    }
                }
            }
        }

        // Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Master Library (${state.visibleTracks.size})",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() }
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (state.hardwareStatus.usbDacConnected) Icons.Filled.Usb else Icons.Filled.Headphones,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${state.tracks.count { it.isCachedOffline }} cached",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(genres) { genre ->
                        FilterChip(
                            selected = state.selectedGenreFilter == genre,
                            onClick = { onSelectGenreFilter(genre) },
                            label = { Text(genre) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        )
                    }
                }
            }
        }

        // Empty State or Track Rows
        if (state.visibleTracks.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "No matching tracks found",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Clear your search filter, turn off Offline Only mode, or import lossless audio files from your Android phone.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onImportAudioClick) {
                            Text("Import Audio From Phone")
                        }
                    }
                }
            }
        } else {
            items(state.visibleTracks, key = { it.id }) { track ->
                val isCurrent = state.currentTrack?.id == track.id
                TrackRowCard(
                    track = track,
                    isCurrent = isCurrent,
                    isPlaying = isCurrent && state.isPlaying,
                    onClick = { onPlayTrack(track) },
                    onToggleFavorite = { onToggleFavorite(track) },
                    onToggleOffline = { onToggleOfflineCache(track) },
                    onAddToPlaylist = { playlistTargetTrack = track },
                    onShare = { onShareTrack(track) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(92.dp)) }
    }

    playlistTargetTrack?.let { target ->
        AddToPlaylistDialog(
            track = target,
            playlists = state.playlists,
            onDismiss = { playlistTargetTrack = null },
            onSelectPlaylist = { playlistId ->
                onAddTrackToPlaylist(playlistId, target)
                playlistTargetTrack = null
            }
        )
    }
}

@Composable
private fun TrackRowCard(
    track: TrackEntity,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleOffline: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onShare: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("track_card_${track.id}"),
        accentGlow = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArtwork(
                resName = track.artworkResName,
                contentDescription = "${track.album} artwork",
                modifier = Modifier.size(58.dp),
                cornerRadius = 12.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} • ${formatDurationMs(track.durationMs)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HiResBadge(
                        label = "${track.formatLabel} ${track.bitDepth}-bit/${track.sampleRateHz / 1000.0}kHz",
                        isCertified = track.isHiResCertified
                    )
                    if (isPlaying) {
                        HiResBadge(
                            label = "PLAYING",
                            isCertified = false,
                            accentColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleOffline,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = if (track.isCachedOffline) Icons.Filled.DownloadDone else Icons.Outlined.CloudDownload,
                    contentDescription = if (track.isCachedOffline) "Cached offline for ${track.title}" else "Cache ${track.title} offline",
                    tint = if (track.isCachedOffline) CertifiedEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = stringResource(R.string.cd_favorite),
                    tint = if (track.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onAddToPlaylist,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                    contentDescription = "Add ${track.title} to playlist",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onShare,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = stringResource(R.string.cd_share),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AddToPlaylistDialog(
    track: TrackEntity,
    playlists: List<PlaylistWithTracks>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Playlist") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select a custom playlist for '${track.title}':",
                    style = MaterialTheme.typography.bodyMedium
                )
                playlists.forEach { pw ->
                    Surface(
                        onClick = { onSelectPlaylist(pw.playlist.id) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(pw.playlist.name, style = MaterialTheme.typography.titleMedium)
                            Text("${pw.tracks.size} tracks", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
