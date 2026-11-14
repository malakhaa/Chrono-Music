package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.TrackEntity
import com.example.ui.theme.CertifiedEmerald
import com.example.ui.theme.HiResGold
import com.example.ui.theme.LocalChronoVisualTokens

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    accentGlow: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalChronoVisualTokens.current
    val shape = RoundedCornerShape(cornerRadius)
    val bgBrush = if (tokens.isHighContrast) {
        Brush.linearGradient(
            listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface)
        )
    } else {
        val alphaTop = (0.55f + tokens.glassIntensity * 0.35f).coerceIn(0.4f, 0.94f)
        val alphaBottom = (0.42f + tokens.glassIntensity * 0.30f).coerceIn(0.35f, 0.88f)
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface.copy(alpha = alphaTop),
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alphaBottom)
            )
        )
    }

    val borderStroke = if (tokens.isHighContrast) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
    } else {
        BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(
                    accentGlow.copy(alpha = 0.35f),
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )
            )
        )
    }

    val cardModifier = modifier
        .clip(shape)
        .background(bgBrush, shape)
        .border(borderStroke, shape)
        .let { base ->
            if (onClick != null) {
                base
                    .minimumInteractiveComponentSize()
                    .clickable(onClick = onClick)
            } else base
        }

    Column(
        modifier = cardModifier.padding(16.dp),
        content = content
    )
}

@Composable
fun HiResBadge(
    label: String,
    isCertified: Boolean = true,
    modifier: Modifier = Modifier,
    accentColor: Color = if (isCertified) HiResGold else CertifiedEmerald
) {
    val tokens = LocalChronoVisualTokens.current
    val bgColor = if (tokens.isHighContrast) MaterialTheme.colorScheme.secondary else accentColor.copy(alpha = 0.16f)
    val textColor = if (tokens.isHighContrast) MaterialTheme.colorScheme.onSecondary else accentColor
    val borderColor = if (tokens.isHighContrast) MaterialTheme.colorScheme.onSecondary else accentColor.copy(alpha = 0.55f)

    Surface(
        modifier = modifier.semantics {
            contentDescription = if (isCertified) "Hi-Res Audio Certified: $label" else label
        },
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isCertified) {
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = textColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun AlbumArtwork(
    resName: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp
) {
    val resId = when (resName) {
        "img_album_neon_pulse" -> R.drawable.img_album_neon_pulse
        "img_album_analog_warmth" -> R.drawable.img_album_analog_warmth
        "img_hero_chrono" -> R.drawable.img_hero_chrono
        else -> R.drawable.img_album_obsidian
    }
    Image(
        painter = painterResource(id = resId),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(cornerRadius))
    )
}

@Composable
fun PersistentSearchVoiceBar(
    query: String,
    isListeningVoice: Boolean,
    onQueryChange: (String) -> Unit,
    onVoiceClick: () -> Unit,
    onSearchSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalChronoVisualTokens.current
    val micBg by animateColorAsState(
        targetValue = if (isListeningVoice) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primaryContainer,
        label = "micBg"
    )
    val micTint = if (isListeningVoice) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimaryContainer

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .testTag("persistent_search_input")
                .semantics {
                    contentDescription = "Search music library by track, artist, or audio format"
                },
            placeholder = {
                Text(
                    text = stringResource(R.string.search_placeholder),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear search query"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (tokens.isHighContrast) 1f else 0.78f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (tokens.isHighContrast) 1f else 0.65f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Surface(
            modifier = Modifier
                .size(52.dp)
                .minimumInteractiveComponentSize()
                .testTag("voice_command_button")
                .semantics {
                    contentDescription = if (isListeningVoice) "Listening for voice command" else "Activate voice command search"
                    liveRegion = LiveRegionMode.Polite
                },
            shape = CircleShape,
            color = micBg,
            border = BorderStroke(if (tokens.isHighContrast) 2.dp else 1.dp, MaterialTheme.colorScheme.primary),
            onClick = onVoiceClick
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isListeningVoice) Icons.Filled.GraphicEq else Icons.Filled.Mic,
                    contentDescription = stringResource(R.string.cd_voice_search),
                    tint = micTint
                )
            }
        }
    }
}

@Composable
fun MiniPlayerBar(
    track: TrackEntity,
    isPlaying: Boolean,
    progressFraction: Float,
    outputLabel: String,
    onOpenNowPlaying: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalChronoVisualTokens.current
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        animationSpec = if (tokens.reducedMotion) spring(stiffness = Spring.StiffnessHigh) else spring(stiffness = Spring.StiffnessMediumLow),
        label = "miniProgress"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("mini_player_bar")
            .semantics {
                contentDescription = "Now playing ${track.title} by ${track.artist}, output $outputLabel. Double tap to expand player."
            },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = if (tokens.isHighContrast) 1f else 0.92f),
        border = BorderStroke(
            width = if (tokens.isHighContrast) 2.dp else 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = if (tokens.isHighContrast) 1f else 0.45f)
        ),
        tonalElevation = 8.dp,
        onClick = onOpenNowPlaying
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AlbumArtwork(
                    resName = track.artworkResName,
                    contentDescription = "${track.album} cover",
                    modifier = Modifier.size(46.dp),
                    cornerRadius = 10.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${track.artist} • $outputLabel",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("mini_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) stringResource(R.string.cd_pause) else stringResource(R.string.cd_play),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onNextTrack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("mini_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = stringResource(R.string.cd_next),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            // Thin progress indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
fun SpectrumCanvas(
    levels: List<Float>,
    isPlaying: Boolean,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val outline = MaterialTheme.colorScheme.outline

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .semantics {
                contentDescription = if (isPlaying) "Active live audio spectrum analyzer" else "Paused audio spectrum analyzer"
            }
    ) {
        val count = levels.size.coerceAtLeast(1)
        val gap = 4.dp.toPx()
        val barWidth = ((size.width - gap * (count - 1)) / count).coerceAtLeast(3f)

        levels.forEachIndexed { index, rawLevel ->
            val level = if (reducedMotion && isPlaying) 0.45f else rawLevel.coerceIn(0.08f, 1f)
            val barHeight = size.height * level
            val x = index * (barWidth + gap)
            val y = size.height - barHeight
            val barColor = when {
                !isPlaying -> outline.copy(alpha = 0.5f)
                index % 4 == 0 -> secondary
                else -> primary
            }
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
