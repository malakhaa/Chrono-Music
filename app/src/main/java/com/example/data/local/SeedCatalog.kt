package com.example.data.local

import com.example.data.model.EqPresetEntity
import com.example.data.model.ListeningZone
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistTrackCrossRef
import com.example.data.model.RoomCorrectionProfile
import com.example.data.model.TrackEntity

object SeedCatalog {

    val initialTracks: List<TrackEntity> = listOf(
        TrackEntity(
            id = "track_midnight_obsidian",
            title = "Midnight Obsidian",
            artist = "Kaelen Vance",
            album = "Monolith Acoustics",
            genre = "Ambient Electronic",
            durationMs = 28_000L,
            artworkResName = "img_album_obsidian",
            formatLabel = "FLAC",
            sampleRateHz = 192000,
            bitDepth = 24,
            bitrateKbps = 9216,
            isHiResCertified = true,
            isCachedOffline = true,
            isFavorite = true,
            playCount = 14,
            synthSeed = 0,
            lyricsLrc = """
                [00:00.50]Silence settles over midnight glass
                [00:04.20]Sub-bass ripples through the open dark
                [00:08.50]Every harmonic tuned to crystal clarity
                [00:13.00]Twenty-four bits breathing in the quiet room
                [00:17.50]Time slows down inside the Chrono pulse
                [00:22.00]Echoes fade into pure obsidian night
            """.trimIndent()
        ),
        TrackEntity(
            id = "track_neon_chronometer",
            title = "Neon Chronometer",
            artist = "Lyra Solis",
            album = "Refraction 120",
            genre = "Synthwave",
            durationMs = 28_000L,
            artworkResName = "img_album_neon_pulse",
            formatLabel = "FLAC",
            sampleRateHz = 96000,
            bitDepth = 24,
            bitrateKbps = 4608,
            isHiResCertified = true,
            isCachedOffline = true,
            isFavorite = true,
            playCount = 11,
            synthSeed = 1,
            lyricsLrc = """
                [00:00.80]Rings of cyan light spin around the dial
                [00:05.10]Analog warmth meets digital precision
                [00:09.60]Hold the rhythm steady across every room
                [00:14.20]Lossless signal locked without a single drop
                [00:19.00]Neon horizon glowing at midnight
                [00:23.40]Let the synthesizer carry us home
            """.trimIndent()
        ),
        TrackEntity(
            id = "track_vacuum_tube_reverie",
            title = "Vacuum Tube Reverie",
            artist = "Aurelius Ensemble",
            album = "Warm Filament Sessions",
            genre = "Analog Jazz",
            durationMs = 28_000L,
            artworkResName = "img_album_analog_warmth",
            formatLabel = "WAV",
            sampleRateHz = 96000,
            bitDepth = 24,
            bitrateKbps = 4608,
            isHiResCertified = true,
            isCachedOffline = true,
            isFavorite = false,
            playCount = 9,
            synthSeed = 2,
            lyricsLrc = """
                [00:01.00]Amber filaments glow behind smoked glass
                [00:05.40]Warm harmonic velvet fills the listening chair
                [00:10.00]Close your eyes and hear the brass breathe
                [00:15.20]Every subtle overtone preserved in full
                [00:20.40]Late night sessions on pure analog tape
            """.trimIndent()
        ),
        TrackEntity(
            id = "track_cyan_horizon",
            title = "Cyan Horizon",
            artist = "Mira Sato",
            album = "Tidal Dynamics",
            genre = "Ambient Electronic",
            durationMs = 28_000L,
            artworkResName = "img_album_obsidian",
            formatLabel = "DSD",
            sampleRateHz = 192000,
            bitDepth = 32,
            bitrateKbps = 5644,
            isHiResCertified = true,
            isCachedOffline = true,
            isFavorite = false,
            playCount = 7,
            synthSeed = 3,
            lyricsLrc = """
                [00:00.60]Wide stereo horizon across calm waters
                [00:05.00]Clean transients shimmering in high definition
                [00:10.10]Feel the air between every gentle chord
                [00:15.80]Zero compression, only open dynamic space
                [00:21.50]Drifting effortlessly past the shoreline
            """.trimIndent()
        ),
        TrackEntity(
            id = "track_studio_blueprint",
            title = "Studio Blueprint",
            artist = "Henrik Lindqvist",
            album = "Reference Room III",
            genre = "Neo-Classical",
            durationMs = 28_000L,
            artworkResName = "img_album_analog_warmth",
            formatLabel = "FLAC",
            sampleRateHz = 88200,
            bitDepth = 24,
            bitrateKbps = 4233,
            isHiResCertified = true,
            isCachedOffline = false,
            isFavorite = true,
            playCount = 6,
            synthSeed = 4,
            lyricsLrc = """
                [00:01.20]Architectural soundscapes measured to the millisecond
                [00:06.00]Acoustic panels hush the outside noise
                [00:11.30]Pure piano harmonics suspended in mid-air
                [00:16.90]Room correction balanced for true neutrality
                [00:22.10]A timeless blueprint written in resonance
            """.trimIndent()
        ),
        TrackEntity(
            id = "track_glass_orbit",
            title = "Glass Orbit",
            artist = "Lyra Solis",
            album = "Refraction 120",
            genre = "Synthwave",
            durationMs = 28_000L,
            artworkResName = "img_album_neon_pulse",
            formatLabel = "FLAC",
            sampleRateHz = 48000,
            bitDepth = 24,
            bitrateKbps = 2304,
            isHiResCertified = true,
            isCachedOffline = true,
            isFavorite = false,
            playCount = 5,
            synthSeed = 5,
            lyricsLrc = """
                [00:00.90]Floating in a translucent orbit of sound
                [00:05.80]Hundred-twenty frames of fluid motion
                [00:11.00]Wireless LDAC locked at nine-ninety kilobits
                [00:16.40]Golden bells chiming over deep velvet bass
                [00:21.80]Gravity loosens as the final chord blooms
            """.trimIndent()
        ),
        TrackEntity(
            id = "track_amber_cathode",
            title = "Amber Cathode",
            artist = "Aurelius Ensemble",
            album = "Warm Filament Sessions",
            genre = "Analog Jazz",
            durationMs = 28_000L,
            artworkResName = "img_album_analog_warmth",
            formatLabel = "ALAC",
            sampleRateHz = 44100,
            bitDepth = 16,
            bitrateKbps = 1411,
            isHiResCertified = false,
            isCachedOffline = true,
            isFavorite = false,
            playCount = 4,
            synthSeed = 6,
            lyricsLrc = """
                [00:01.00]Red-book CD warmth from a golden era
                [00:06.20]Unplugged acoustics under soft amber lamps
                [00:12.00]Swaying slowly to an unhurried tempo
                [00:17.80]Every breath and finger slide intact
                [00:22.50]Until the needle lifts at dawn
            """.trimIndent()
        ),
        TrackEntity(
            id = "track_subzero_phase",
            title = "Subzero Phase",
            artist = "Kaelen Vance",
            album = "Monolith Acoustics",
            genre = "Ambient Electronic",
            durationMs = 28_000L,
            artworkResName = "img_album_obsidian",
            formatLabel = "FLAC",
            sampleRateHz = 192000,
            bitDepth = 24,
            bitrateKbps = 9216,
            isHiResCertified = true,
            isCachedOffline = false,
            isFavorite = false,
            playCount = 3,
            synthSeed = 7,
            lyricsLrc = """
                [00:00.80]Phase-aligned bass waves below forty hertz
                [00:05.50]External USB DAC locked to master clock
                [00:11.10]Crystalline air rising through the upper register
                [00:16.60]Shared playback synced across every listener
                [00:22.20]Pure sonic stillness in subzero phase
            """.trimIndent()
        )
    )

    val initialPlaylists: List<PlaylistEntity> = listOf(
        PlaylistEntity(
            id = "pl_audiophile_master",
            name = "24-Bit Master Reference",
            description = "Ultra-high-resolution FLAC & DSD masters calibrated for USB DACs and LDAC headphones.",
            accentHex = 0xFF00E5FF,
            isCurated = true
        ),
        PlaylistEntity(
            id = "pl_late_night_tubes",
            name = "Late Night Vacuum Tubes",
            description = "Warm analog jazz and velvet electronics for dark-room listening.",
            accentHex = 0xFFFFB300,
            isCurated = true
        ),
        PlaylistEntity(
            id = "pl_offline_flight",
            name = "Offline Flight Vault",
            description = "Bit-perfect cached tracks ready without any internet connection.",
            accentHex = 0xFF2CE69B,
            isCurated = false
        )
    )

    val initialPlaylistRefs: List<PlaylistTrackCrossRef> = listOf(
        PlaylistTrackCrossRef("pl_audiophile_master", "track_midnight_obsidian", 0),
        PlaylistTrackCrossRef("pl_audiophile_master", "track_neon_chronometer", 1),
        PlaylistTrackCrossRef("pl_audiophile_master", "track_cyan_horizon", 2),
        PlaylistTrackCrossRef("pl_audiophile_master", "track_subzero_phase", 3),
        PlaylistTrackCrossRef("pl_late_night_tubes", "track_vacuum_tube_reverie", 0),
        PlaylistTrackCrossRef("pl_late_night_tubes", "track_studio_blueprint", 1),
        PlaylistTrackCrossRef("pl_late_night_tubes", "track_amber_cathode", 2),
        PlaylistTrackCrossRef("pl_offline_flight", "track_midnight_obsidian", 0),
        PlaylistTrackCrossRef("pl_offline_flight", "track_neon_chronometer", 1),
        PlaylistTrackCrossRef("pl_offline_flight", "track_glass_orbit", 2)
    )

    val initialEqPresets: List<EqPresetEntity> = listOf(
        EqPresetEntity(
            id = "eq_reference",
            name = "Audiophile Reference",
            band60Hz = 0f,
            band230Hz = 0f,
            band910Hz = 0f,
            band3600Hz = 0f,
            band14000Hz = 0f,
            bassBoostStrength = 0,
            virtualizerStrength = 0,
            tubeWarmth = 0.15f,
            crossfeedEnabled = false,
            roomCorrection = RoomCorrectionProfile.BYPASS.name,
            isBuiltIn = true
        ),
        EqPresetEntity(
            id = "eq_warm_vinyl",
            name = "Warm Analog Vinyl",
            band60Hz = 3.5f,
            band230Hz = 2.0f,
            band910Hz = -0.5f,
            band3600Hz = 1.0f,
            band14000Hz = -1.5f,
            bassBoostStrength = 250,
            virtualizerStrength = 200,
            tubeWarmth = 0.72f,
            crossfeedEnabled = true,
            roomCorrection = RoomCorrectionProfile.LIVING_ROOM.name,
            isBuiltIn = true
        ),
        EqPresetEntity(
            id = "eq_sub_bass",
            name = "Sub-Bass Impact",
            band60Hz = 6.5f,
            band230Hz = 3.5f,
            band910Hz = -1.0f,
            band3600Hz = 1.5f,
            band14000Hz = 2.5f,
            bassBoostStrength = 620,
            virtualizerStrength = 350,
            tubeWarmth = 0.30f,
            crossfeedEnabled = false,
            roomCorrection = RoomCorrectionProfile.BYPASS.name,
            isBuiltIn = true
        ),
        EqPresetEntity(
            id = "eq_vocal_clarity",
            name = "Vocal & Acoustic",
            band60Hz = -2.0f,
            band230Hz = -1.0f,
            band910Hz = 3.5f,
            band3600Hz = 4.5f,
            band14000Hz = 2.0f,
            bassBoostStrength = 0,
            virtualizerStrength = 400,
            tubeWarmth = 0.20f,
            crossfeedEnabled = true,
            roomCorrection = RoomCorrectionProfile.TREATED_BOOTH.name,
            isBuiltIn = true
        ),
        EqPresetEntity(
            id = "eq_late_night",
            name = "Late Night Studio",
            band60Hz = 2.5f,
            band230Hz = 1.0f,
            band910Hz = 0.5f,
            band3600Hz = -1.5f,
            band14000Hz = -2.5f,
            bassBoostStrength = 180,
            virtualizerStrength = 500,
            tubeWarmth = 0.55f,
            crossfeedEnabled = true,
            roomCorrection = RoomCorrectionProfile.SMALL_STUDIO.name,
            isBuiltIn = true
        )
    )

    val defaultListeningZones: List<ListeningZone> = listOf(
        ListeningZone(
            id = "zone_phone",
            name = "This Android Device",
            hardwareType = "Primary Hi-Res Output",
            isActive = true,
            latencyOffsetMs = 0,
            volumeTrim = 1.0f,
            codecBadge = "24-bit / 96kHz",
            signalDbm = -32
        ),
        ListeningZone(
            id = "zone_studio",
            name = "Studio Desk DAC",
            hardwareType = "USB-C Balanced Amp",
            isActive = true,
            latencyOffsetMs = 12,
            volumeTrim = 0.88f,
            codecBadge = "PCM 192kHz",
            signalDbm = -28
        ),
        ListeningZone(
            id = "zone_living",
            name = "Living Room Hi-Fi",
            hardwareType = "LDAC Wireless Receiver",
            isActive = false,
            latencyOffsetMs = 45,
            volumeTrim = 0.80f,
            codecBadge = "LDAC 990kbps",
            signalDbm = -54
        ),
        ListeningZone(
            id = "zone_bedroom",
            name = "Bedroom Acoustic Dock",
            hardwareType = "aptX Adaptive Sink",
            isActive = false,
            latencyOffsetMs = 34,
            volumeTrim = 0.70f,
            codecBadge = "aptX HD",
            signalDbm = -61
        )
    )
}
