package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class ThemeMode(val label: String) {
    DARK("Obsidian Dark"),
    LIGHT("Alabaster Light"),
    SYSTEM("System Default")
}

enum class RepeatMode(val label: String) {
    OFF("Repeat Off"),
    ALL("Repeat All"),
    ONE("Repeat One")
}

enum class MultiListeningMode(val label: String, val subtitle: String) {
    SOLO("Solo Audiophile", "Direct bit-perfect local output"),
    SHARED_SESSION("Shared Playback", "Live synchronized listening room"),
    MULTI_ROOM("Multi-Room Sync", "Time-aligned acoustic zones")
}

enum class RoomCorrectionProfile(val label: String, val description: String) {
    BYPASS("Bypass (Flat)", "Uncolored reference signal"),
    SMALL_STUDIO("Studio Nearfield", "Tames 120Hz desk boundary boom"),
    LIVING_ROOM("Living Room", "Compensates wide reflections & bass decay"),
    TREATED_BOOTH("Vocal & Acoustic", "Enhances upper-mid presence clarity")
}

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val durationMs: Long,
    val artworkResName: String = "img_album_obsidian",
    val artworkUri: String? = null,
    val audioUri: String = "",
    val formatLabel: String = "FLAC",
    val sampleRateHz: Int = 96000,
    val bitDepth: Int = 24,
    val bitrateKbps: Int = 4608,
    val isHiResCertified: Boolean = true,
    val isCachedOffline: Boolean = true,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lyricsLrc: String = "",
    val isBuiltIn: Boolean = true,
    val synthSeed: Int = 0
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val accentHex: Long = 0xFF00E5FF,
    val createdAt: Long = System.currentTimeMillis(),
    val isCurated: Boolean = false
)

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "trackId"],
    indices = [Index(value = ["trackId"])]
)
data class PlaylistTrackCrossRef(
    val playlistId: String,
    val trackId: String,
    val position: Int = 0
)

data class PlaylistWithTracks(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlaylistTrackCrossRef::class,
            parentColumn = "playlistId",
            entityColumn = "trackId"
        )
    )
    val tracks: List<TrackEntity>
)

@Entity(tableName = "eq_presets")
data class EqPresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val band60Hz: Float,
    val band230Hz: Float,
    val band910Hz: Float,
    val band3600Hz: Float,
    val band14000Hz: Float,
    val bassBoostStrength: Int = 0,
    val virtualizerStrength: Int = 0,
    val tubeWarmth: Float = 0.25f,
    val crossfeedEnabled: Boolean = false,
    val roomCorrection: String = RoomCorrectionProfile.BYPASS.name,
    val isBuiltIn: Boolean = false
)

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

data class ListeningZone(
    val id: String,
    val name: String,
    val hardwareType: String,
    val isActive: Boolean,
    val latencyOffsetMs: Int,
    val volumeTrim: Float,
    val codecBadge: String,
    val signalDbm: Int
)

data class AudioHardwareStatus(
    val activeOutputName: String = "Device Stereo Speaker",
    val activeOutputCategory: String = "Internal DAC",
    val isHiResOutputPath: Boolean = false,
    val usbDacConnected: Boolean = false,
    val usbDacName: String? = null,
    val usbSupportedRatesHz: List<Int> = emptyList(),
    val usbMaxChannels: Int = 2,
    val bluetoothConnected: Boolean = false,
    val bluetoothDeviceName: String? = null,
    val bluetoothCodecDetected: String = "No Wireless Sink"
)

data class ChronoSettings(
    // Display & Themes
    val themeMode: ThemeMode = ThemeMode.DARK,
    val highContrastMode: Boolean = false,
    val dynamicColor: Boolean = false,
    val glassIntensity: Float = 0.72f,
    val reducedMotion: Boolean = false,
    val talkBackAnnouncements: Boolean = true,

    // Performance Settings
    val refreshRate120Hz: Boolean = true,
    val audioBufferSamples: Int = 1024,
    val precacheNextTrack: Boolean = true,
    val hardwareVisualizer: Boolean = true,

    // Audio Quality Options
    val losslessStreamingMode: Boolean = true,
    val outputBitDepth: Int = 24,
    val resamplerQuality: String = "Sinc Best (Ultra-Linear)",
    val gaplessPlayback: Boolean = true,
    val replayGainEnabled: Boolean = false,
    val offlineModeOnly: Boolean = false,
    val releaseAlertsEnabled: Boolean = false,

    // USB Audio Interface & External DAC
    val usbExclusiveDacMode: Boolean = true,
    val usbHardwareVolumeControl: Boolean = true,
    val usbDsdOverPcm: Boolean = true,
    val usbTargetSampleRateHz: Int = 0, // 0 = Bit-Perfect Auto

    // Bluetooth Hi-Res Mode
    val btHiResMode: Boolean = true,
    val btPreferredCodec: String = "LDAC 990kbps",
    val btLowLatencyMultipoint: Boolean = true,
    val btRangeStabilityBoost: Boolean = true,
    val btMinBitrateFloorKbps: Int = 660
)

fun parseLrcLyrics(lrc: String): List<LyricLine> {
    if (lrc.isBlank()) return emptyList()
    val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})](.*)""")
    return lrc.lines()
        .mapNotNull { line ->
            val match = regex.find(line.trim()) ?: return@mapNotNull null
            val min = match.groupValues[1].toLongOrNull() ?: 0L
            val sec = match.groupValues[2].toLongOrNull() ?: 0L
            val fracRaw = match.groupValues[3]
            val millis = if (fracRaw.length == 2) (fracRaw.toLongOrNull() ?: 0L) * 10L else (fracRaw.toLongOrNull() ?: 0L)
            val text = match.groupValues[4].trim()
            if (text.isEmpty()) null else LyricLine((min * 60_000L) + (sec * 1000L) + millis, text)
        }
        .sortedBy { it.timestampMs }
}

fun formatDurationMs(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSec / 60L
    val seconds = totalSec % 60L
    return "%d:%02d".format(minutes, seconds)
}
