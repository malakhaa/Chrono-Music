package com.example

import com.example.audio.VoiceAction
import com.example.audio.VoiceCommandParser
import com.example.audio.WaveGenerator
import com.example.data.model.formatDurationMs
import com.example.data.model.parseLrcLyrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class ExampleUnitTest {

    @Test
    fun voiceCommandParser_recognizesPlaybackAndAccessibilityCommands() {
        assertEquals(VoiceAction.Pause, VoiceCommandParser.parse("pause"))
        assertEquals(VoiceAction.Play, VoiceCommandParser.parse("Resume"))
        assertEquals(VoiceAction.Next, VoiceCommandParser.parse("next track"))
        assertEquals(VoiceAction.ToggleHighContrast, VoiceCommandParser.parse("Switch to high contrast"))
        assertEquals(VoiceAction.OpenEqualizer, VoiceCommandParser.parse("Open equalizer"))
        assertEquals(
            VoiceAction.SearchAndPlay("midnight obsidian"),
            VoiceCommandParser.parse("Play Midnight Obsidian")
        )
    }

    @Test
    fun parseLrcLyrics_extractsTimestampsInOrder() {
        val raw = """
            [00:10.50]Second line
            [00:02.00]First line
        """.trimIndent()
        val parsed = parseLrcLyrics(raw)
        assertEquals(2, parsed.size)
        assertEquals(2000L, parsed[0].timestampMs)
        assertEquals("First line", parsed[0].text)
        assertEquals(10500L, parsed[1].timestampMs)
        assertEquals("0:10", formatDurationMs(10500L))
    }

    @Test
    fun waveGenerator_createsValidRiffWavFile() {
        val tempDir = Files.createTempDirectory("chrono_wav_test").toFile()
        val wavFile = WaveGenerator.ensureTrackWav(tempDir, "test_track", seed = 1, durationSeconds = 2)
        assertTrue(wavFile.exists())
        val bytes = wavFile.readBytes()
        assertTrue(bytes.size > 44)
        assertEquals("RIFF", String(bytes.copyOfRange(0, 4)))
        assertEquals("WAVE", String(bytes.copyOfRange(8, 12)))
        tempDir.deleteRecursively()
    }
}
