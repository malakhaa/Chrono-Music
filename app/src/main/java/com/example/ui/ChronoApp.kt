package com.example.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.KeyEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.audio.VoiceCommandController
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.PersistentSearchVoiceBar
import com.example.ui.screens.EqualizerDspScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ChronoMusicTheme
import kotlinx.coroutines.launch

sealed class ChronoRoute(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Library : ChronoRoute("library", "Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic)
    data object Playlists : ChronoRoute("playlists", "Playlists", Icons.AutoMirrored.Filled.QueueMusic, Icons.AutoMirrored.Outlined.QueueMusic)
    data object NowPlaying : ChronoRoute("now_playing", "Player", Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq)
    data object Equalizer : ChronoRoute("equalizer", "EQ & DSP", Icons.Filled.Equalizer, Icons.Outlined.Equalizer)
    data object Settings : ChronoRoute("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun ChronoApp(
    onRefreshRatePreferenceChanged: (Boolean) -> Unit,
    viewModel: ChronoViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var isListeningVoice by remember { mutableStateOf(false) }
    var showVoiceQuickSheet by remember { mutableStateOf(false) }

    val voiceController = remember(context) {
        VoiceCommandController(
            context = context,
            onListeningChanged = { isListeningVoice = it },
            onResult = { spoken -> viewModel.executeVoiceCommand(spoken) },
            onError = { msg ->
                viewModel.showBanner(msg)
                showVoiceQuickSheet = true
            }
        )
    }

    DisposableEffect(voiceController) {
        onDispose { voiceController.stopListening() }
    }

    val recordAudioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (voiceController.isAvailable()) {
                voiceController.startListening()
            } else {
                showVoiceQuickSheet = true
            }
        } else {
            viewModel.showBanner("Microphone permission denied — use quick voice commands instead.")
            showVoiceQuickSheet = true
        }
    }

    val btPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.refreshAudioHardware()
        viewModel.showBanner(
            if (granted) "Bluetooth Hi-Res hardware scan complete"
            else "Using AudioManager output detection without Bluetooth Connect permission"
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.showBanner(
            if (granted) "New release notifications enabled"
            else "Notification permission declined"
        )
    }

    val importAudioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importLocalAudio(it) }
    }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { targetUri ->
            scope.launch {
                runCatching {
                    val json = viewModel.exportBackupJson()
                    context.contentResolver.openOutputStream(targetUri)?.use { out ->
                        out.write(json.toByteArray())
                    }
                    viewModel.showBanner("Library & DSP backup saved")
                }.onFailure {
                    viewModel.showBanner("Failed to export backup: ${it.localizedMessage}")
                }
            }
        }
    }

    val importBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { sourceUri ->
            scope.launch {
                runCatching {
                    val text = context.contentResolver.openInputStream(sourceUri)?.bufferedReader()?.readText().orEmpty()
                    viewModel.importBackupJson(text)
                }.onFailure {
                    viewModel.showBanner("Could not read backup file")
                }
            }
        }
    }

    LaunchedEffect(state.settings.refreshRate120Hz) {
        onRefreshRatePreferenceChanged(state.settings.refreshRate120Hz)
    }

    LaunchedEffect(state.statusBanner) {
        state.statusBanner?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissBanner()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            val targetRoute = when (event) {
                UiNavEvent.OpenNowPlaying -> ChronoRoute.NowPlaying.route
                UiNavEvent.OpenEqualizer -> ChronoRoute.Equalizer.route
                UiNavEvent.OpenSettings -> ChronoRoute.Settings.route
            }
            navController.navigate(targetRoute) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val routes = listOf(
        ChronoRoute.Library,
        ChronoRoute.Playlists,
        ChronoRoute.NowPlaying,
        ChronoRoute.Equalizer,
        ChronoRoute.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isNowPlayingRoute = currentDestination?.route == ChronoRoute.NowPlaying.route

    ChronoMusicTheme(
        themeMode = state.settings.themeMode,
        highContrastMode = state.settings.highContrastMode,
        dynamicColor = state.settings.dynamicColor,
        glassIntensity = state.settings.glassIntensity,
        reducedMotion = state.settings.reducedMotion
    ) {
        val bgBrush = if (state.settings.highContrastMode) {
            Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background))
        } else {
            Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.background,
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.background
                )
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.key) {
                            Key.Spacebar -> {
                                viewModel.togglePlayPause()
                                true
                            }
                            Key.MediaNext -> {
                                viewModel.nextTrack()
                                true
                            }
                            Key.MediaPrevious -> {
                                viewModel.previousTrack()
                                true
                            }
                            else -> false
                        }
                    } else false
                }
        ) {
            val useNavigationRail = maxWidth >= 600.dp

            Row(modifier = Modifier.fillMaxSize()) {
                if (useNavigationRail) {
                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .windowInsetsPadding(WindowInsets.safeDrawing),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        routes.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                modifier = Modifier.testTag("nav_${item.route}")
                            )
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.weight(1f),
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    bottomBar = {
                        if (!useNavigationRail) {
                            NavigationBar(
                                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                                containerColor = MaterialTheme.colorScheme.surface.copy(
                                    alpha = if (state.settings.highContrastMode) 1f else 0.94f
                                )
                            ) {
                                routes.forEach { item ->
                                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.label
                                            )
                                        },
                                        label = { Text(item.label, maxLines = 1) },
                                        modifier = Modifier.testTag("nav_${item.route}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 960.dp)
                                .align(Alignment.TopCenter)
                        ) {
                            // Persistent Search & Voice Control Bar
                            PersistentSearchVoiceBar(
                                query = state.searchQuery,
                                isListeningVoice = isListeningVoice,
                                onQueryChange = { query ->
                                    viewModel.updateSearchQuery(query)
                                    if (currentDestination?.route != ChronoRoute.Library.route && query.isNotBlank()) {
                                        navController.navigate(ChronoRoute.Library.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                onVoiceClick = {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (granted) {
                                        if (voiceController.isAvailable()) {
                                            voiceController.startListening()
                                        } else {
                                            showVoiceQuickSheet = true
                                        }
                                    } else {
                                        recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                onSearchSubmit = {
                                    if (state.searchQuery.isNotBlank()) {
                                        viewModel.executeVoiceCommand(state.searchQuery)
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )

                            NavHost(
                                navController = navController,
                                startDestination = ChronoRoute.Library.route,
                                modifier = Modifier.weight(1f)
                            ) {
                                composable(ChronoRoute.Library.route) {
                                    LibraryScreen(
                                        state = state,
                                        onPlayTrack = viewModel::playTrack,
                                        onToggleFavorite = viewModel::toggleFavorite,
                                        onToggleOfflineCache = viewModel::toggleOfflineCache,
                                        onShareTrack = { track ->
                                            shareText(
                                                context,
                                                "Listening to '${track.title}' by ${track.artist} in ${track.formatLabel} ${track.bitDepth}-bit/${track.sampleRateHz / 1000.0}kHz on Chrono Music!"
                                            )
                                        },
                                        onSelectGenreFilter = viewModel::selectGenreFilter,
                                        onImportAudioClick = {
                                            importAudioLauncher.launch(arrayOf("audio/*"))
                                        },
                                        onToggleOfflineOnly = {
                                            viewModel.updateSettings { it.copy(offlineModeOnly = !it.offlineModeOnly) }
                                        },
                                        onToggleHighContrast = {
                                            viewModel.updateSettings { it.copy(highContrastMode = !it.highContrastMode) }
                                        },
                                        onAddTrackToPlaylist = viewModel::addTrackToPlaylist,
                                        onOpenNowPlaying = {
                                            navController.navigate(ChronoRoute.NowPlaying.route) {
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }

                                composable(ChronoRoute.Playlists.route) {
                                    PlaylistsScreen(
                                        state = state,
                                        onCreatePlaylist = viewModel::createPlaylist,
                                        onDeletePlaylist = viewModel::deletePlaylist,
                                        onPlayPlaylist = viewModel::playPlaylist,
                                        onPlayTrack = viewModel::playTrack,
                                        onRemoveTrackFromPlaylist = viewModel::removeTrackFromPlaylist,
                                        onSharePlaylist = { pw ->
                                            val listNames = pw.tracks.joinToString(", ") { it.title }
                                            shareText(
                                                context,
                                                "Check out my Chrono Music playlist '${pw.playlist.name}' (${pw.tracks.size} Hi-Res tracks): $listNames"
                                            )
                                        },
                                        onSetListeningMode = viewModel::setMultiListeningMode,
                                        onRegenerateSessionCode = viewModel::regenerateSharedSessionCode,
                                        onJoinSessionCode = viewModel::joinSharedSessionCode,
                                        onShareSessionInvite = { code ->
                                            shareText(
                                                context,
                                                "Join my synchronized Chrono Music listening room! Session Code: $code"
                                            )
                                        },
                                        onToggleZoneActive = viewModel::toggleZoneActive,
                                        onUpdateZoneLatency = viewModel::updateZoneLatency,
                                        onUpdateZoneVolume = viewModel::updateZoneVolume
                                    )
                                }

                                composable(ChronoRoute.NowPlaying.route) {
                                    NowPlayingScreen(
                                        state = state,
                                        onTogglePlayPause = viewModel::togglePlayPause,
                                        onNext = viewModel::nextTrack,
                                        onPrevious = viewModel::previousTrack,
                                        onSeekTo = viewModel::seekTo,
                                        onToggleShuffle = viewModel::toggleShuffle,
                                        onCycleRepeat = viewModel::cycleRepeatMode,
                                        onToggleFavorite = viewModel::toggleFavorite,
                                        onToggleOfflineCache = viewModel::toggleOfflineCache,
                                        onShareTrack = { track ->
                                            shareText(
                                                context,
                                                "Now spinning '${track.title}' by ${track.artist} (${track.formatLabel} ${track.bitDepth}-bit/${track.sampleRateHz / 1000.0}kHz) on Chrono Music"
                                            )
                                        },
                                        onOpenEqualizer = {
                                            navController.navigate(ChronoRoute.Equalizer.route) {
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }

                                composable(ChronoRoute.Equalizer.route) {
                                    EqualizerDspScreen(
                                        state = state,
                                        onToggleEqEnabled = viewModel::toggleEqEnabled,
                                        onSelectPreset = viewModel::selectEqPreset,
                                        onUpdateDraftEq = viewModel::updateDraftEq,
                                        onSaveCustomPreset = viewModel::saveCustomEqPreset,
                                        onDeletePreset = viewModel::deleteEqPreset,
                                        onSelectRoomCorrection = viewModel::setRoomCorrection
                                    )
                                }

                                composable(ChronoRoute.Settings.route) {
                                    SettingsScreen(
                                        state = state,
                                        onUpdateSettings = viewModel::updateSettings,
                                        onRefreshHardware = viewModel::refreshAudioHardware,
                                        onRequestBluetoothPermission = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                btPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                                            } else {
                                                viewModel.refreshAudioHardware()
                                            }
                                        },
                                        onRequestNotificationPermission = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            }
                                        },
                                        onExportBackup = {
                                            exportBackupLauncher.launch("chrono_music_backup.json")
                                        },
                                        onImportBackup = {
                                            importBackupLauncher.launch(arrayOf("application/json", "text/plain"))
                                        }
                                    )
                                }
                            }
                        }

                        // Floating Glassmorphic Mini-Player on non-player screens
                        val activeTrack = state.currentTrack
                        androidx.compose.animation.AnimatedVisibility(
                            visible = activeTrack != null && !isNowPlayingRoute,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .widthIn(max = 640.dp)
                        ) {
                            if (activeTrack != null) {
                                val progress = if (state.durationMs > 0) {
                                    state.currentPositionMs.toFloat() / state.durationMs.toFloat()
                                } else 0f
                                MiniPlayerBar(
                                    track = activeTrack,
                                    isPlaying = state.isPlaying,
                                    progressFraction = progress,
                                    outputLabel = state.hardwareStatus.activeOutputCategory,
                                    onOpenNowPlaying = {
                                        navController.navigate(ChronoRoute.NowPlaying.route) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onTogglePlayPause = viewModel::togglePlayPause,
                                    onNextTrack = viewModel::nextTrack
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showVoiceQuickSheet) {
        val quickCommands = listOf(
            "Play Midnight Obsidian",
            "Play Neon Chronometer",
            "Pause",
            "Next",
            "Open Equalizer",
            "Toggle High Contrast",
            "Open Settings"
        )
        AlertDialog(
            onDismissRequest = { showVoiceQuickSheet = false },
            title = { Text("Voice & Hands-Free Commands") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Speak commands via the mic button or tap a hands-free command below:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    quickCommands.forEach { cmd ->
                        SuggestionChip(
                            onClick = {
                                viewModel.executeVoiceCommand(cmd)
                                showVoiceQuickSheet = false
                            },
                            label = { Text(cmd) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVoiceQuickSheet = false }) { Text("Close") }
            }
        )
    }
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Chrono Music")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share via"))
}
