package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.LyricLine
import com.example.data.model.RepeatMode
import com.example.data.model.TrackEntity
import com.example.data.model.formatDurationMs
import com.example.ui.ChronoUiState
import com.example.ui.components.AlbumArtwork
import com.example.ui.components.GlassCard
import com.example.ui.components.HiResBadge
import com.example.ui.components.SpectrumCanvas
import com.example.ui.theme.CertifiedEmerald

@Composable
fun NowPlayingScreen(
    state: ChronoUiState,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: (TrackEntity) -> Unit,
    onToggleOfflineCache: (TrackEntity) -> Unit,
    onShareTrack: (TrackEntity) -> Unit,
    onOpenEqualizer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = state.currentTrack
    if (track == null) {
        Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Select a track from your Library to start listening.", style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Player, 1 = Synced Lyrics

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("now_playing_screen")
    ) {
        val isWide = maxWidth >= 720.dp

        if (isWide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PlayerControlsPane(
                    state = state,
                    track = track,
                    onTogglePlayPause = onTogglePlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onSeekTo = onSeekTo,
                    onToggleShuffle = onToggleShuffle,
                    onCycleRepeat = onCycleRepeat,
                    onToggleFavorite = { onToggleFavorite(track) },
                    onToggleOfflineCache = { onToggleOfflineCache(track) },
                    onShareTrack = { onShareTrack(track) },
                    onOpenEqualizer = onOpenEqualizer,
                    modifier = Modifier.weight(1f)
                )
                SyncedLyricsPane(
                    lyrics = state.currentLyrics,
                    activeIndex = state.activeLyricIndex,
                    onSeekTo = onSeekTo,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                        icon = { Icon(Icons.Filled.GraphicEq, contentDescription = null) }
                    ) {
                        Text("Studio Deck")
                    }
                    SegmentedButton(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                        icon = { Icon(Icons.Filled.Lyrics, contentDescription = null) }
                    ) {
                        Text("Synced Lyrics")
                    }
                }

                if (selectedTab == 0) {
                    PlayerControlsPane(
                        state = state,
                        track = track,
                        onTogglePlayPause = onTogglePlayPause,
                        onNext = onNext,
                        onPrevious = onPrevious,
                        onSeekTo = onSeekTo,
                        onToggleShuffle = onToggleShuffle,
                        onCycleRepeat = onCycleRepeat,
                        onToggleFavorite = { onToggleFavorite(track) },
                        onToggleOfflineCache = { onToggleOfflineCache(track) },
                        onShareTrack = { onShareTrack(track) },
                        onOpenEqualizer = onOpenEqualizer,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    SyncedLyricsPane(
                        lyrics = state.currentLyrics,
                        activeIndex = state.activeLyricIndex,
                        onSeekTo = onSeekTo,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerControlsPane(
    state: ChronoUiState,
    track: TrackEntity,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleOfflineCache: () -> Unit,
    onShareTrack: () -> Unit,
    onOpenEqualizer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            AlbumArtwork(
                resName = track.artworkResName,
                contentDescription = "${track.album} full cover artwork",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
                cornerRadius = 18.dp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = "${track.artist} — ${track.album}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HiResBadge(
                    label = "${track.formatLabel} ${track.bitDepth}-bit / ${track.sampleRateHz / 1000.0}kHz",
                    isCertified = track.isHiResCertified
                )
                HiResBadge(
                    label = "${track.bitrateKbps} kbps",
                    isCertified = false,
                    accentColor = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            SpectrumCanvas(
                levels = state.spectrumLevels,
                isPlaying = state.isPlaying,
                reducedMotion = state.settings.reducedMotion
            )
        }

        // Scrubbable Timeline + Transport
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            val dur = state.durationMs.coerceAtLeast(1000L)
            val pos = state.currentPositionMs.coerceIn(0L, dur)
            Slider(
                value = pos.toFloat(),
                onValueChange = { onSeekTo(it.toLong()) },
                valueRange = 0f..dur.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Playback position ${formatDurationMs(pos)} of ${formatDurationMs(dur)}"
                    }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatDurationMs(pos), style = MaterialTheme.typography.labelMedium)
                Text(formatDurationMs(dur), style = MaterialTheme.typography.labelMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shuffle,
                        contentDescription = stringResource(R.string.cd_shuffle),
                        tint = if (state.shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = stringResource(R.string.cd_previous),
                        modifier = Modifier.size(32.dp)
                    )
                }
                FilledIconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("now_playing_play_pause"),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (state.isPlaying) stringResource(R.string.cd_pause) else stringResource(R.string.cd_play),
                        modifier = Modifier.size(34.dp)
                    )
                }
                IconButton(
                    onClick = onNext,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = stringResource(R.string.cd_next),
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(
                    onClick = onCycleRepeat,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = if (state.repeatMode == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                        contentDescription = state.repeatMode.label,
                        tint = if (state.repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    onClick = onOpenEqualizer,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.Usb, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "${state.hardwareStatus.activeOutputCategory} • ${state.activeEqPreset.name}",
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row {
                    IconButton(
                        onClick = onToggleOfflineCache,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = if (track.isCachedOffline) Icons.Filled.DownloadDone else Icons.Outlined.CloudDownload,
                            contentDescription = if (track.isCachedOffline) "Cached offline" else "Save for offline listening",
                            tint = if (track.isCachedOffline) CertifiedEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onShareTrack,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = stringResource(R.string.cd_share)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(84.dp))
    }
}

@Composable
private fun SyncedLyricsPane(
    lyrics: List<LyricLine>,
    activeIndex: Int,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && activeIndex < lyrics.size) {
            listState.animateScrollToItem((activeIndex - 1).coerceAtLeast(0))
        }
    }

    GlassCard(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Synced Lyrics",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                text = "Tap any line to seek",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (lyrics.isEmpty()) {
            Text(
                text = "No timestamped LRC lyrics embedded for this track.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                itemsIndexed(lyrics) { index, line ->
                    val isActive = index == activeIndex
                    val textColor by animateColorAsState(
                        targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "lyricColor"
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.25f),
                        border = if (isActive) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .clickable { onSeekTo(line.timestampMs) }
                            .semantics {
                                contentDescription = "${formatDurationMs(line.timestampMs)}: ${line.text}"
                                if (isActive) liveRegion = LiveRegionMode.Polite
                            }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                            Text(
                                text = formatDurationMs(line.timestampMs),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = line.text,
                                style = if (isActive) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}
