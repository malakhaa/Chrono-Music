package com.example.audio

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

object WaveGenerator {

    private val scaleChords = listOf(
        doubleArrayOf(130.81, 196.00, 246.94, 293.66), // Cmaj9
        doubleArrayOf(110.00, 164.81, 220.00, 261.63), // Am7
        doubleArrayOf(87.31, 130.81, 174.61, 220.00),  // Fmaj7
        doubleArrayOf(98.00, 146.83, 196.00, 246.94),  // G6
        doubleArrayOf(146.83, 220.00, 261.63, 329.63), // Dm9
        doubleArrayOf(116.54, 174.61, 220.00, 293.66), // Bbmaj7
        doubleArrayOf(123.47, 185.00, 246.94, 293.66), // Bm7
        doubleArrayOf(103.83, 155.56, 207.65, 246.94)  // Abmaj7
    )

    fun ensureTrackWav(
        cacheDir: File,
        trackId: String,
        seed: Int,
        durationSeconds: Int = 28
    ): File {
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val target = File(cacheDir, "${trackId}.wav")
        if (target.exists() && target.length() > 44L) {
            return target
        }

        val sampleRate = 44100
        val channels = 2
        val bitsPerSample = 16
        val totalFrames = sampleRate * durationSeconds
        val dataBytes = totalFrames * channels * (bitsPerSample / 8)

        val baseProgression = listOf(
            scaleChords[seed % scaleChords.size],
            scaleChords[(seed + 1) % scaleChords.size],
            scaleChords[(seed + 2) % scaleChords.size],
            scaleChords[(seed + 3) % scaleChords.size]
        )
        val tempoBpm = 76.0 + (seed % 4) * 8.0
        val beatSec = 60.0 / tempoBpm

        BufferedOutputStream(FileOutputStream(target), 65536).use { out ->
            out.write(buildWavHeader(dataBytes, sampleRate, channels, bitsPerSample))

            val chunkFrames = 2048
            val byteBuf = ByteBuffer.allocate(chunkFrames * channels * 2).order(ByteOrder.LITTLE_ENDIAN)

            var frame = 0
            while (frame < totalFrames) {
                byteBuf.clear()
                val batchEnd = minOf(frame + chunkFrames, totalFrames)
                for (i in frame until batchEnd) {
                    val t = i.toDouble() / sampleRate.toDouble()
                    val chordDuration = beatSec * 4.0
                    val chordIdx = ((t / chordDuration).toInt()) % baseProgression.size
                    val chord = baseProgression[chordIdx]
                    val chordLocalT = (t % chordDuration) / chordDuration

                    // Warm pad envelope
                    val padEnv = 0.65 + 0.35 * sin(PI * chordLocalT)

                    // Sub bass + warm fifths
                    val sub = sin(2.0 * PI * (chord[0] * 0.5) * t) * 0.24
                    val root = sin(2.0 * PI * chord[0] * t) * 0.17 * padEnv
                    val third = sin(2.0 * PI * chord[1] * t + 0.3) * 0.13 * padEnv
                    val seventh = sin(2.0 * PI * chord[2] * t + 0.7) * 0.11 * padEnv

                    // Gentle bell arpeggio every half beat
                    val arpStepSec = beatSec * 0.5
                    val arpPos = (t % arpStepSec) / arpStepSec
                    val arpNoteIdx = ((t / arpStepSec).toInt() + seed) % chord.size
                    val arpFreq = chord[arpNoteIdx] * 2.0
                    val arpEnv = kotlin.math.exp(-arpPos * 5.2)
                    val arp = (sin(2.0 * PI * arpFreq * t) + 0.25 * sin(4.0 * PI * arpFreq * t)) * 0.14 * arpEnv

                    // Stereo shimmer offset
                    val panWave = 0.25 * sin(2.0 * PI * 0.18 * t)
                    val fadeIn = (t / 0.8).coerceIn(0.0, 1.0)
                    val fadeOut = ((durationSeconds - t) / 1.2).coerceIn(0.0, 1.0)
                    val masterEnv = fadeIn * fadeOut

                    val leftSample = ((sub + root + third * (1.0 - panWave) + seventh * 0.8 + arp * (0.85 + panWave)) * masterEnv)
                        .coerceIn(-0.95, 0.95)
                    val rightSample = ((sub + root + third * (1.0 + panWave) + seventh + arp * (0.85 - panWave)) * masterEnv)
                        .coerceIn(-0.95, 0.95)

                    byteBuf.putShort((leftSample * Short.MAX_VALUE).toInt().toShort())
                    byteBuf.putShort((rightSample * Short.MAX_VALUE).toInt().toShort())
                }
                out.write(byteBuf.array(), 0, byteBuf.position())
                frame = batchEnd
            }
        }
        return target
    }

    private fun buildWavHeader(
        dataSize: Int,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ): ByteArray {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray())
        header.putInt(36 + dataSize)
        header.put("WAVE".toByteArray())
        header.put("fmt ".toByteArray())
        header.putInt(16) // PCM chunk size
        header.putShort(1.toShort()) // AudioFormat = 1 (PCM)
        header.putShort(channels.toShort())
        header.putInt(sampleRate)
        header.putInt(byteRate)
        header.putShort(blockAlign.toShort())
        header.putShort(bitsPerSample.toShort())
        header.put("data".toByteArray())
        header.putInt(dataSize)
        return header.array()
    }
}
