package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.ChronoAudioEngine
import com.example.audio.VoiceAction
import com.example.audio.VoiceCommandParser
import com.example.data.local.SeedCatalog
import com.example.data.model.AudioHardwareStatus
import com.example.data.model.ChronoSettings
import com.example.data.model.EqPresetEntity
import com.example.data.model.ListeningZone
import com.example.data.model.LyricLine
import com.example.data.model.MultiListeningMode
import com.example.data.model.PlaylistWithTracks
import com.example.data.model.RepeatMode
import com.example.data.model.RoomCorrectionProfile
import com.example.data.model.ThemeMode
import com.example.data.model.TrackEntity
import com.example.data.model.parseLrcLyrics
import com.example.data.repository.ChronoRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface UiNavEvent {
    data object OpenNowPlaying : UiNavEvent
    data object OpenEqualizer : UiNavEvent
    data object OpenSettings : UiNavEvent
}

data class ChronoUiState(
    val tracks: List<TrackEntity> = emptyList(),
    val visibleTracks: List<TrackEntity> = emptyList(),
    val recommendedTracks: List<TrackEntity> = emptyList(),
    val playlists: List<PlaylistWithTracks> = emptyList(),
    val eqPresets: List<EqPresetEntity> = emptyList(),
    val activeEqPreset: EqPresetEntity = SeedCatalog.initialEqPresets.first(),
    val eqEnabled: Boolean = true,
    val settings: ChronoSettings = ChronoSettings(),
    val currentTrack: TrackEntity? = null,
    val currentLyrics: List<LyricLine> = emptyList(),
    val activeLyricIndex: Int = -1,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 28_000L,
    val spectrumLevels: List<Float> = List(24) { 0.12f },
    val hardwareStatus: AudioHardwareStatus = AudioHardwareStatus(),
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.ALL,
    val searchQuery: String = "",
    val selectedGenreFilter: String = "All",
    val listeningMode: MultiListeningMode = MultiListeningMode.SOLO,
    val sharedSessionCode: String = "CHRONO-842",
    val sharedPeersCount: Int = 3,
    val listeningZones: List<ListeningZone> = SeedCatalog.defaultListeningZones,
    val statusBanner: String? = null,
    val isReady: Boolean = false
)

class ChronoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChronoRepository(application)
    private val audioEngine = ChronoAudioEngine(application)

    private val _searchQuery = MutableStateFlow("")
    private val _selectedGenre = MutableStateFlow("All")
    private val _currentTrackId = MutableStateFlow<String?>(null)
    private val _shuffleEnabled = MutableStateFlow(false)
    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    private val _listeningMode = MutableStateFlow(MultiListeningMode.SOLO)
    private val _sharedSessionCode = MutableStateFlow("CHRONO-842")
    private val _sharedPeersCount = MutableStateFlow(3)
    private val _listeningZones = MutableStateFlow(SeedCatalog.defaultListeningZones)
    private val _statusBanner = MutableStateFlow<String?>(null)
    private val _isReady = MutableStateFlow(false)

    // Working copy of EQ preset so slider drags feel 120Hz instant before saving
    private val _draftEqPreset = MutableStateFlow<EqPresetEntity?>(null)

    private val _navEvents = MutableSharedFlow<UiNavEvent>(extraBufferCapacity = 4)
    val navEvents: SharedFlow<UiNavEvent> = _navEvents.asSharedFlow()

    private val baseDataFlow = combine(
        repository.allTracks,
        repository.playlistsWithTracks,
        repository.eqPresets,
        repository.activeEqPresetId,
        repository.eqEnabled
    ) { tracks, playlists, presets, activePresetId, eqEnabled ->
        BaseDataTuple(tracks, playlists, presets, activePresetId, eqEnabled)
    }

    private val playbackDataFlow = combine(
        _currentTrackId,
        audioEngine.isPlaying,
        audioEngine.currentPositionMs,
        audioEngine.durationMs,
        audioEngine.spectrumLevels
    ) { trackId, isPlaying, pos, dur, spectrum ->
        PlaybackTuple(trackId, isPlaying, pos, dur, spectrum)
    }

    private val controlsFlow = combine(
        _searchQuery,
        _selectedGenre,
        _shuffleEnabled,
        _repeatMode,
        _draftEqPreset
    ) { query, genre, shuffle, repeat, draftEq ->
        ControlsTuple(query, genre, shuffle, repeat, draftEq)
    }

    private val sessionFlow = combine(
        _listeningMode,
        _sharedSessionCode,
        _sharedPeersCount,
        _listeningZones,
        _statusBanner
    ) { mode, code, peers, zones, banner ->
        SessionTuple(mode, code, peers, zones, banner)
    }

    val uiState: StateFlow<ChronoUiState> = combine(
        baseDataFlow,
        playbackDataFlow,
        controlsFlow,
        sessionFlow,
        repository.settings,
        audioEngine.hardwareStatus,
        _isReady
    ) { arr ->
        val base = arr[0] as BaseDataTuple
        val playback = arr[1] as PlaybackTuple
        val controls = arr[2] as ControlsTuple
        val session = arr[3] as SessionTuple
        val settings = arr[4] as ChronoSettings
        val hw = arr[5] as AudioHardwareStatus
        val ready = arr[6] as Boolean

        val filteredByOffline = if (settings.offlineModeOnly) {
            base.tracks.filter { it.isCachedOffline }
        } else {
            base.tracks
        }

        val queryLower = controls.query.trim().lowercase()
        val visibleTracks = filteredByOffline.filter { track ->
            val matchesGenre = controls.genre == "All" ||
                (controls.genre == "Hi-Res" && track.isHiResCertified) ||
                (controls.genre == "Favorites" && track.isFavorite) ||
                (controls.genre == "Offline" && track.isCachedOffline) ||
                track.genre.equals(controls.genre, ignoreCase = true)

            val matchesSearch = queryLower.isBlank() ||
                track.title.lowercase().contains(queryLower) ||
                track.artist.lowercase().contains(queryLower) ||
                track.album.lowercase().contains(queryLower) ||
                track.genre.lowercase().contains(queryLower) ||
                track.formatLabel.lowercase().contains(queryLower)

            matchesGenre && matchesSearch
        }

        // Personalized recommendation scoring based on favorites, playCounts, and Hi-Res affinity
        val favoriteGenres = base.tracks.filter { it.isFavorite }.map { it.genre }.toSet()
        val recommended = base.tracks
            .sortedByDescending { track ->
                var score = track.playCount * 2
                if (track.isFavorite) score += 8
                if (track.genre in favoriteGenres) score += 5
                if (track.isHiResCertified && settings.losslessStreamingMode) score += 4
                score
            }
            .take(4)

        val currentTrack = base.tracks.firstOrNull { it.id == playback.trackId } ?: base.tracks.firstOrNull()
        val lyrics = currentTrack?.let { parseLrcLyrics(it.lyricsLrc) } ?: emptyList()
        val activeLyricIndex = lyrics.indexOfLast { it.timestampMs <= playback.pos }

        val activePreset = controls.draftEq
            ?: base.presets.firstOrNull { it.id == base.activePresetId }
            ?: base.presets.firstOrNull()
            ?: SeedCatalog.initialEqPresets.first()

        ChronoUiState(
            tracks = base.tracks,
            visibleTracks = visibleTracks,
            recommendedTracks = recommended,
            playlists = base.playlists,
            eqPresets = base.presets,
            activeEqPreset = activePreset,
            eqEnabled = base.eqEnabled,
            settings = settings,
            currentTrack = currentTrack,
            currentLyrics = lyrics,
            activeLyricIndex = activeLyricIndex,
            isPlaying = playback.isPlaying,
            currentPositionMs = playback.pos,
            durationMs = if (playback.trackId != null) playback.dur else (currentTrack?.durationMs ?: 28_000L),
            spectrumLevels = playback.spectrum,
            hardwareStatus = hw,
            shuffleEnabled = controls.shuffle,
            repeatMode = controls.repeat,
            searchQuery = controls.query,
            selectedGenreFilter = controls.genre,
            listeningMode = session.mode,
            sharedSessionCode = session.code,
            sharedPeersCount = session.peers,
            listeningZones = session.zones,
            statusBanner = session.banner,
            isReady = ready
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChronoUiState()
    )

    init {
        audioEngine.setOnTrackCompletedListener {
            onTrackCompleted()
        }
        viewModelScope.launch {
            repository.ensureSeededAndCached()
            val tracks = repository.allTracks.first()
            if (_currentTrackId.value == null && tracks.isNotEmpty()) {
                _currentTrackId.value = tracks.first().id
            }
            val presets = repository.eqPresets.first()
            val activeId = repository.activeEqPresetId.first()
            val eqEnabled = repository.eqEnabled.first()
            val active = presets.firstOrNull { it.id == activeId } ?: presets.firstOrNull()
            if (active != null) {
                audioEngine.applyDspSettings(active, eqEnabled)
            }
            _isReady.value = true
        }
    }

    fun playTrack(track: TrackEntity) {
        val currentSettings = uiState.value.settings
        if (currentSettings.offlineModeOnly && !track.isCachedOffline) {
            showBanner("Offline Mode is active. Cache '${track.title}' first to play offline.")
            return
        }
        viewModelScope.launch {
            _currentTrackId.value = track.id
            val playable = repository.resolvePlayableFileOrUri(track)
            audioEngine.playSource(playable, autoPlay = true)
            audioEngine.applyDspSettings(uiState.value.activeEqPreset, uiState.value.eqEnabled)
            repository.incrementPlayCount(track.id)
        }
    }

    fun togglePlayPause() {
        val state = uiState.value
        if (state.currentPositionMs == 0L && !state.isPlaying) {
            state.currentTrack?.let { track ->
                viewModelScope.launch {
                    val playable = repository.resolvePlayableFileOrUri(track)
                    audioEngine.playSource(playable, autoPlay = true)
                    audioEngine.applyDspSettings(state.activeEqPreset, state.eqEnabled)
                }
                return
            }
        }
        audioEngine.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        audioEngine.seekTo(positionMs)
    }

    fun nextTrack() {
        val list = uiState.value.visibleTracks.ifEmpty { uiState.value.tracks }
        if (list.isEmpty()) return
        val currentIdx = list.indexOfFirst { it.id == _currentTrackId.value }
        val next = if (_shuffleEnabled.value && list.size > 1) {
            list.filter { it.id != _currentTrackId.value }.random()
        } else {
            list[(currentIdx + 1) % list.size]
        }
        playTrack(next)
    }

    fun previousTrack() {
        if (audioEngine.currentPositionMs.value > 3000L) {
            audioEngine.seekTo(0L)
            return
        }
        val list = uiState.value.visibleTracks.ifEmpty { uiState.value.tracks }
        if (list.isEmpty()) return
        val currentIdx = list.indexOfFirst { it.id == _currentTrackId.value }
        val prevIdx = if (currentIdx <= 0) list.lastIndex else currentIdx - 1
        playTrack(list[prevIdx])
    }

    private fun onTrackCompleted() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                uiState.value.currentTrack?.let { playTrack(it) }
            }
            RepeatMode.ALL -> nextTrack()
            RepeatMode.OFF -> {
                val list = uiState.value.visibleTracks.ifEmpty { uiState.value.tracks }
                val idx = list.indexOfFirst { it.id == _currentTrackId.value }
                if (idx >= 0 && idx < list.lastIndex) {
                    playTrack(list[idx + 1])
                }
            }
        }
    }

    fun toggleShuffle() {
        _shuffleEnabled.value = !_shuffleEnabled.value
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectGenreFilter(genre: String) {
        _selectedGenre.value = genre
    }

    fun executeVoiceCommand(spokenText: String) {
        showBanner("Voice: \"$spokenText\"")
        when (val action = VoiceCommandParser.parse(spokenText)) {
            is VoiceAction.Play -> audioEngine.resume()
            is VoiceAction.Pause -> audioEngine.pause()
            is VoiceAction.Next -> nextTrack()
            is VoiceAction.Previous -> previousTrack()
            is VoiceAction.ToggleShuffle -> toggleShuffle()
            is VoiceAction.ToggleHighContrast -> {
                updateSettings { it.copy(highContrastMode = !it.highContrastMode) }
            }
            is VoiceAction.ToggleOfflineMode -> {
                updateSettings { it.copy(offlineModeOnly = !it.offlineModeOnly) }
            }
            is VoiceAction.OpenEqualizer -> _navEvents.tryEmit(UiNavEvent.OpenEqualizer)
            is VoiceAction.OpenSettings -> _navEvents.tryEmit(UiNavEvent.OpenSettings)
            is VoiceAction.OpenNowPlaying -> _navEvents.tryEmit(UiNavEvent.OpenNowPlaying)
            is VoiceAction.SearchOnly -> _searchQuery.value = action.query
            is VoiceAction.SearchAndPlay -> {
                _searchQuery.value = action.query
                val match = uiState.value.tracks.firstOrNull {
                    it.title.contains(action.query, ignoreCase = true) ||
                        it.artist.contains(action.query, ignoreCase = true) ||
                        it.genre.contains(action.query, ignoreCase = true)
                }
                if (match != null) {
                    playTrack(match)
                    showBanner("Playing '${match.title}' by ${match.artist}")
                } else {
                    showBanner("Filtered library for '${action.query}'")
                }
            }
        }
    }

    fun toggleFavorite(track: TrackEntity) {
        viewModelScope.launch { repository.toggleFavorite(track) }
    }

    fun toggleOfflineCache(track: TrackEntity) {
        viewModelScope.launch {
            repository.toggleOfflineCache(track)
            val msg = if (!track.isCachedOffline) "Cached '${track.title}' for offline listening" else "Removed offline cache for '${track.title}'"
            showBanner(msg)
        }
    }

    fun importLocalAudio(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val track = repository.importLocalAudioUri(uri)
                showBanner("Imported '${track.title}' (${track.formatLabel})")
                playTrack(track)
            }.onFailure {
                showBanner("Failed to import audio file: ${it.localizedMessage ?: "Unsupported format"}")
            }
        }
    }

    // Playlists
    fun createPlaylist(name: String, description: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val created = repository.createPlaylist(name, description)
            showBanner("Created playlist '${created.name}'")
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            showBanner("Playlist deleted")
        }
    }

    fun addTrackToPlaylist(playlistId: String, track: TrackEntity) {
        viewModelScope.launch {
            val playlist = uiState.value.playlists.firstOrNull { it.playlist.id == playlistId }
            val nextPos = playlist?.tracks?.size ?: 0
            repository.addTrackToPlaylist(playlistId, track.id, nextPos)
            showBanner("Added '${track.title}' to '${playlist?.playlist?.name ?: "Playlist"}'")
        }
    }

    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    fun playPlaylist(playlistWithTracks: PlaylistWithTracks) {
        val first = playlistWithTracks.tracks.firstOrNull() ?: return
        playTrack(first)
        showBanner("Playing playlist '${playlistWithTracks.playlist.name}'")
    }

    // Equalizer & DSP
    fun toggleEqEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setEqEnabled(enabled)
            audioEngine.applyDspSettings(uiState.value.activeEqPreset, enabled)
        }
    }

    fun selectEqPreset(preset: EqPresetEntity) {
        _draftEqPreset.value = null
        viewModelScope.launch {
            repository.setActiveEqPresetId(preset.id)
            audioEngine.applyDspSettings(preset, uiState.value.eqEnabled)
        }
    }

    fun updateDraftEq(transform: (EqPresetEntity) -> EqPresetEntity) {
        val updated = transform(uiState.value.activeEqPreset)
        _draftEqPreset.value = updated
        audioEngine.applyDspSettings(updated, uiState.value.eqEnabled)
    }

    fun saveCustomEqPreset(name: String) {
        val clean = name.trim()
        if (clean.isBlank()) return
        val current = uiState.value.activeEqPreset
        val newPreset = current.copy(
            id = "eq_${UUID.randomUUID().toString().take(8)}",
            name = clean,
            isBuiltIn = false
        )
        _draftEqPreset.value = null
        viewModelScope.launch {
            repository.saveEqPreset(newPreset)
            audioEngine.applyDspSettings(newPreset, uiState.value.eqEnabled)
            showBanner("Saved DSP preset '$clean'")
        }
    }

    fun deleteEqPreset(preset: EqPresetEntity) {
        if (preset.isBuiltIn) return
        viewModelScope.launch {
            repository.deleteEqPreset(preset.id)
            _draftEqPreset.value = null
            repository.setActiveEqPresetId("eq_reference")
            showBanner("Deleted preset '${preset.name}'")
        }
    }

    fun setRoomCorrection(profile: RoomCorrectionProfile) {
        updateDraftEq { it.copy(roomCorrection = profile.name) }
    }

    // Multi-Listening & Multi-Room
    fun setMultiListeningMode(mode: MultiListeningMode) {
        _listeningMode.value = mode
        showBanner("Switched to ${mode.label}")
    }

    fun regenerateSharedSessionCode() {
        val code = "CHRONO-${(100..999).random()}"
        _sharedSessionCode.value = code
        _sharedPeersCount.value = 1
        showBanner("Created new shared listening room $code")
    }

    fun joinSharedSessionCode(code: String) {
        val clean = code.trim().uppercase()
        if (clean.isBlank()) return
        _sharedSessionCode.value = clean
        _sharedPeersCount.value = (2..6).random()
        _listeningMode.value = MultiListeningMode.SHARED_SESSION
        showBanner("Synced playback clock with session $clean")
    }

    fun toggleZoneActive(zoneId: String) {
        _listeningZones.value = _listeningZones.value.map { zone ->
            if (zone.id == zoneId) zone.copy(isActive = !zone.isActive) else zone
        }
    }

    fun updateZoneLatency(zoneId: String, latencyMs: Int) {
        _listeningZones.value = _listeningZones.value.map { zone ->
            if (zone.id == zoneId) zone.copy(latencyOffsetMs = latencyMs.coerceIn(-100, 200)) else zone
        }
    }

    fun updateZoneVolume(zoneId: String, volume: Float) {
        _listeningZones.value = _listeningZones.value.map { zone ->
            if (zone.id == zoneId) zone.copy(volumeTrim = volume.coerceIn(0f, 1f)) else zone
        }
    }

    // Settings
    fun updateSettings(transform: (ChronoSettings) -> ChronoSettings) {
        viewModelScope.launch {
            repository.updateSettings(transform)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        updateSettings { it.copy(themeMode = mode) }
    }

    fun refreshAudioHardware() {
        audioEngine.refreshHardwareStatus()
        showBanner("Scanned USB & Bluetooth audio interfaces")
    }

    suspend fun exportBackupJson(): String {
        val state = uiState.value
        return repository.exportLibraryBackupJson(
            tracks = state.tracks,
            playlists = state.playlists,
            presets = state.eqPresets,
            settings = state.settings
        )
    }

    fun importBackupJson(rawJson: String) {
        viewModelScope.launch {
            runCatching {
                val restored = repository.importLibraryBackupJson(rawJson)
                showBanner("Restored $restored playlists, favorites & DSP presets")
            }.onFailure {
                showBanner("Invalid Chrono backup file")
            }
        }
    }

    fun showBanner(message: String) {
        _statusBanner.value = message
    }

    fun dismissBanner() {
        _statusBanner.value = null
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.destroy()
    }

    private data class BaseDataTuple(
        val tracks: List<TrackEntity>,
        val playlists: List<PlaylistWithTracks>,
        val presets: List<EqPresetEntity>,
        val activePresetId: String,
        val eqEnabled: Boolean
    )

    private data class PlaybackTuple(
        val trackId: String?,
        val isPlaying: Boolean,
        val pos: Long,
        val dur: Long,
        val spectrum: List<Float>
    )

    private data class ControlsTuple(
        val query: String,
        val genre: String,
        val shuffle: Boolean,
        val repeat: RepeatMode,
        val draftEq: EqPresetEntity?
    )

    private data class SessionTuple(
        val mode: MultiListeningMode,
        val code: String,
        val peers: Int,
        val zones: List<ListeningZone>,
        val banner: String?
    )
}
