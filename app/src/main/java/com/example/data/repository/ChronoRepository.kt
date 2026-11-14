package com.example.data.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.example.audio.WaveGenerator
import com.example.data.local.ChronoDatabase
import com.example.data.local.SeedCatalog
import com.example.data.local.SettingsDataStore
import com.example.data.model.ChronoSettings
import com.example.data.model.EqPresetEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistTrackCrossRef
import com.example.data.model.PlaylistWithTracks
import com.example.data.model.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class ChronoRepository(private val context: Context) {

    private val dao = ChronoDatabase.getInstance(context).chronoDao()
    private val settingsStore = SettingsDataStore(context)
    private val audioCacheDir = File(context.filesDir, "chrono_tracks")

    val allTracks: Flow<List<TrackEntity>> = dao.observeAllTracks()
    val playlistsWithTracks: Flow<List<PlaylistWithTracks>> = dao.observePlaylistsWithTracks()
    val eqPresets: Flow<List<EqPresetEntity>> = dao.observeEqPresets()
    val settings: Flow<ChronoSettings> = settingsStore.settingsFlow
    val activeEqPresetId: Flow<String> = settingsStore.activeEqPresetIdFlow
    val eqEnabled: Flow<Boolean> = settingsStore.eqEnabledFlow

    suspend fun ensureSeededAndCached() = withContext(Dispatchers.IO) {
        if (dao.getTrackCount() == 0) {
            dao.insertTracks(SeedCatalog.initialTracks)
            dao.insertPlaylists(SeedCatalog.initialPlaylists)
            dao.insertPlaylistTrackRefs(SeedCatalog.initialPlaylistRefs)
        }
        if (dao.getEqPresetCount() == 0) {
            dao.insertEqPresets(SeedCatalog.initialEqPresets)
        }
        // Pre-generate offline WAV files for built-in tracks so playback starts instantaneously
        SeedCatalog.initialTracks.forEach { track ->
            WaveGenerator.ensureTrackWav(audioCacheDir, track.id, track.synthSeed)
        }
    }

    suspend fun resolvePlayableFileOrUri(track: TrackEntity): String = withContext(Dispatchers.IO) {
        if (track.audioUri.isNotBlank()) {
            track.audioUri
        } else {
            WaveGenerator.ensureTrackWav(audioCacheDir, track.id, track.synthSeed).absolutePath
        }
    }

    suspend fun importLocalAudioUri(uri: Uri): TrackEntity = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        val displayName = queryDisplayName(uri) ?: "Imported Audio"
        val retriever = MediaMetadataRetriever()
        var title = displayName.substringBeforeLast('.')
        var artist = "Local Library"
        var album = "Device Storage"
        var genre = "Imported"
        var durationMs = 180_000L
        var bitrateKbps = 1411
        var sampleRateHz = 44100

        runCatching {
            retriever.setDataSource(context, uri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() }?.let { title = it }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.takeIf { it.isNotBlank() }?.let { artist = it }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?.takeIf { it.isNotBlank() }?.let { album = it }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)?.takeIf { it.isNotBlank() }?.let { genre = it }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?.let { durationMs = it }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()?.let { bitrateKbps = (it / 1000).coerceAtLeast(128) }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull()?.let { sampleRateHz = it }
            }
        }
        runCatching { retriever.release() }

        val ext = displayName.substringAfterLast('.', "AUDIO").uppercase()
        val formatLabel = when (ext) {
            "FLAC", "WAV", "ALAC", "AIFF", "DSF", "DFF", "MP3", "OGG", "OPUS", "M4A", "AAC" -> ext
            else -> "PCM"
        }
        val isHiRes = sampleRateHz > 44100 || bitrateKbps > 1411 || formatLabel in listOf("FLAC", "WAV", "ALAC", "DSF", "DFF")
        val bitDepth = if (isHiRes) 24 else 16

        val track = TrackEntity(
            id = "local_${UUID.randomUUID().toString().take(8)}",
            title = title,
            artist = artist,
            album = album,
            genre = genre,
            durationMs = durationMs.coerceAtLeast(1000L),
            artworkResName = "img_album_neon_pulse",
            audioUri = uri.toString(),
            formatLabel = formatLabel,
            sampleRateHz = sampleRateHz,
            bitDepth = bitDepth,
            bitrateKbps = bitrateKbps,
            isHiResCertified = isHiRes,
            isCachedOffline = true,
            isFavorite = false,
            playCount = 0,
            lyricsLrc = "[00:01.00]Playing local device file: $title\n[00:06.00]Format: $formatLabel • ${sampleRateHz / 1000.0}kHz / $bitDepth-bit",
            isBuiltIn = false
        )
        dao.insertTrack(track)
        track
    }

    private fun queryDisplayName(uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        }.getOrNull()
    }

    suspend fun toggleFavorite(track: TrackEntity) {
        dao.setTrackFavorite(track.id, !track.isFavorite)
    }

    suspend fun toggleOfflineCache(track: TrackEntity) {
        val nextCached = !track.isCachedOffline
        if (nextCached && track.isBuiltIn) {
            withContext(Dispatchers.IO) {
                WaveGenerator.ensureTrackWav(audioCacheDir, track.id, track.synthSeed)
            }
        }
        dao.setTrackCachedOffline(track.id, nextCached)
    }

    suspend fun incrementPlayCount(trackId: String) {
        dao.incrementPlayCount(trackId)
    }

    suspend fun createPlaylist(name: String, description: String): PlaylistEntity {
        val playlist = PlaylistEntity(
            id = "pl_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            description = description.trim().ifBlank { "Custom Chrono Music collection" },
            accentHex = listOf(0xFF00E5FF, 0xFFFFB300, 0xFF9D7BFF, 0xFF2CE69B).random(),
            isCurated = false
        )
        dao.insertPlaylist(playlist)
        return playlist
    }

    suspend fun deletePlaylist(playlistId: String) {
        dao.clearPlaylistTracks(playlistId)
        dao.deletePlaylist(playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: String, trackId: String, position: Int) {
        dao.insertPlaylistTrackRef(PlaylistTrackCrossRef(playlistId, trackId, position))
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        dao.removeTrackFromPlaylist(playlistId, trackId)
    }

    suspend fun saveEqPreset(preset: EqPresetEntity) {
        dao.insertEqPreset(preset)
        settingsStore.setActiveEqPresetId(preset.id)
    }

    suspend fun deleteEqPreset(presetId: String) {
        dao.deleteEqPreset(presetId)
    }

    suspend fun setActiveEqPresetId(presetId: String) {
        settingsStore.setActiveEqPresetId(presetId)
    }

    suspend fun setEqEnabled(enabled: Boolean) {
        settingsStore.setEqEnabled(enabled)
    }

    suspend fun updateSettings(transform: (ChronoSettings) -> ChronoSettings) {
        settingsStore.updateSettings(transform)
    }

    suspend fun exportLibraryBackupJson(
        tracks: List<TrackEntity>,
        playlists: List<PlaylistWithTracks>,
        presets: List<EqPresetEntity>,
        settings: ChronoSettings
    ): String = withContext(Dispatchers.Default) {
        val root = JSONObject()
        root.put("app", "Chrono Music")
        root.put("exportedAt", System.currentTimeMillis())

        val settingsJson = JSONObject().apply {
            put("themeMode", settings.themeMode.name)
            put("highContrastMode", settings.highContrastMode)
            put("refreshRate120Hz", settings.refreshRate120Hz)
            put("losslessStreamingMode", settings.losslessStreamingMode)
            put("usbExclusiveDacMode", settings.usbExclusiveDacMode)
            put("btHiResMode", settings.btHiResMode)
            put("btPreferredCodec", settings.btPreferredCodec)
        }
        root.put("settings", settingsJson)

        val favoritesArr = JSONArray()
        tracks.filter { it.isFavorite }.forEach { favoritesArr.put(it.id) }
        root.put("favoriteTrackIds", favoritesArr)

        val playlistsArr = JSONArray()
        playlists.forEach { pw ->
            val pObj = JSONObject().apply {
                put("id", pw.playlist.id)
                put("name", pw.playlist.name)
                put("description", pw.playlist.description)
                put("accentHex", pw.playlist.accentHex)
                put("trackIds", JSONArray(pw.tracks.map { it.id }))
            }
            playlistsArr.put(pObj)
        }
        root.put("playlists", playlistsArr)

        val customPresetsArr = JSONArray()
        presets.filter { !it.isBuiltIn }.forEach { eq ->
            customPresetsArr.put(
                JSONObject().apply {
                    put("id", eq.id)
                    put("name", eq.name)
                    put("b60", eq.band60Hz.toDouble())
                    put("b230", eq.band230Hz.toDouble())
                    put("b910", eq.band910Hz.toDouble())
                    put("b3600", eq.band3600Hz.toDouble())
                    put("b14000", eq.band14000Hz.toDouble())
                    put("bassBoost", eq.bassBoostStrength)
                    put("virtualizer", eq.virtualizerStrength)
                    put("tubeWarmth", eq.tubeWarmth.toDouble())
                    put("crossfeed", eq.crossfeedEnabled)
                    put("roomCorrection", eq.roomCorrection)
                }
            )
        }
        root.put("customEqPresets", customPresetsArr)
        root.toString(2)
    }

    suspend fun importLibraryBackupJson(rawJson: String): Int = withContext(Dispatchers.IO) {
        val root = JSONObject(rawJson)
        var restoredCount = 0

        val favs = root.optJSONArray("favoriteTrackIds")
        if (favs != null) {
            for (i in 0 until favs.length()) {
                val id = favs.optString(i)
                if (id.isNotBlank()) {
                    dao.setTrackFavorite(id, true)
                    restoredCount++
                }
            }
        }

        val playlists = root.optJSONArray("playlists")
        if (playlists != null) {
            for (i in 0 until playlists.length()) {
                val obj = playlists.getJSONObject(i)
                val pid = obj.getString("id")
                dao.insertPlaylist(
                    PlaylistEntity(
                        id = pid,
                        name = obj.getString("name"),
                        description = obj.optString("description", "Restored playlist"),
                        accentHex = obj.optLong("accentHex", 0xFF00E5FF),
                        isCurated = false
                    )
                )
                val tids = obj.optJSONArray("trackIds")
                if (tids != null) {
                    for (j in 0 until tids.length()) {
                        dao.insertPlaylistTrackRef(PlaylistTrackCrossRef(pid, tids.getString(j), j))
                    }
                }
                restoredCount++
            }
        }

        val presets = root.optJSONArray("customEqPresets")
        if (presets != null) {
            for (i in 0 until presets.length()) {
                val obj = presets.getJSONObject(i)
                dao.insertEqPreset(
                    EqPresetEntity(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        band60Hz = obj.optDouble("b60", 0.0).toFloat(),
                        band230Hz = obj.optDouble("b230", 0.0).toFloat(),
                        band910Hz = obj.optDouble("b910", 0.0).toFloat(),
                        band3600Hz = obj.optDouble("b3600", 0.0).toFloat(),
                        band14000Hz = obj.optDouble("b14000", 0.0).toFloat(),
                        bassBoostStrength = obj.optInt("bassBoost", 0),
                        virtualizerStrength = obj.optInt("virtualizer", 0),
                        tubeWarmth = obj.optDouble("tubeWarmth", 0.25).toFloat(),
                        crossfeedEnabled = obj.optBoolean("crossfeed", false),
                        roomCorrection = obj.optString("roomCorrection", "BYPASS"),
                        isBuiltIn = false
                    )
                )
                restoredCount++
            }
        }
        restoredCount
    }
}
