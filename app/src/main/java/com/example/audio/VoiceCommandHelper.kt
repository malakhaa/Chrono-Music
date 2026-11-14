package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

sealed interface VoiceAction {
    data class SearchAndPlay(val query: String) : VoiceAction
    data class SearchOnly(val query: String) : VoiceAction
    data object Play : VoiceAction
    data object Pause : VoiceAction
    data object Next : VoiceAction
    data object Previous : VoiceAction
    data object ToggleShuffle : VoiceAction
    data object ToggleHighContrast : VoiceAction
    data object ToggleOfflineMode : VoiceAction
    data object OpenEqualizer : VoiceAction
    data object OpenSettings : VoiceAction
    data object OpenNowPlaying : VoiceAction
}

object VoiceCommandParser {
    fun parse(spokenText: String): VoiceAction {
        val clean = spokenText.trim().lowercase(Locale.getDefault())
        return when {
            clean == "pause" || clean == "stop" || clean == "stop music" -> VoiceAction.Pause
            clean == "play" || clean == "resume" || clean == "start" -> VoiceAction.Play
            clean.startsWith("play ") -> VoiceAction.SearchAndPlay(clean.removePrefix("play ").trim())
            clean.contains("next") || clean.contains("skip") -> VoiceAction.Next
            clean.contains("previous") || clean.contains("back") -> VoiceAction.Previous
            clean.contains("shuffle") -> VoiceAction.ToggleShuffle
            clean.contains("high contrast") || clean.contains("accessibility") -> VoiceAction.ToggleHighContrast
            clean.contains("offline") -> VoiceAction.ToggleOfflineMode
            clean.contains("equalizer") || clean.contains("dsp") || clean.contains("eq") -> VoiceAction.OpenEqualizer
            clean.contains("setting") || clean.contains("dac") || clean.contains("bluetooth") -> VoiceAction.OpenSettings
            clean.contains("lyrics") || clean.contains("now playing") -> VoiceAction.OpenNowPlaying
            else -> VoiceAction.SearchOnly(spokenText.trim())
        }
    }
}

class VoiceCommandController(
    private val context: Context,
    private val onListeningChanged: (Boolean) -> Unit,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening() {
        if (!isAvailable()) {
            onError("Speech recognition service unavailable on this device. Try quick voice commands below.")
            return
        }
        stopListening()
        runCatching {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        onListeningChanged(true)
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        onListeningChanged(false)
                    }

                    override fun onError(error: Int) {
                        onListeningChanged(false)
                        onError("Could not capture speech (error $error). Try again or use a quick command.")
                    }

                    override fun onResults(results: Bundle?) {
                        onListeningChanged(false)
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val best = matches?.firstOrNull()
                        if (!best.isNullOrBlank()) {
                            onResult(best)
                        } else {
                            onError("No voice command recognized.")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer?.startListening(intent)
        }.onFailure {
            onListeningChanged(false)
            onError("Voice recognition failed to start: ${it.localizedMessage ?: "Unknown error"}")
        }
    }

    fun stopListening() {
        runCatching {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        }
        speechRecognizer = null
        onListeningChanged(false)
    }
}
