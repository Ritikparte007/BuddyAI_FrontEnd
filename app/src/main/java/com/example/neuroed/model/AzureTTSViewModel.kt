package com.example.neuroed.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microsoft.cognitiveservices.speech.*
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Future

// TTS State enum
enum class TTSState {
    IDLE,
    SPEAKING,
    PAUSED,
    ERROR
}

// Azure TTS Configuration
object AzureTTSConfig {
    // Replace with your actual Azure Speech Service key and region
    const val SPEECH_SUBSCRIPTION_KEY = "4a5877d33caa49bcbc19eade2c0fc602"
    const val SPEECH_REGION = "centralindia" // e.g., "eastus", "westus2"

    // Available voices - you can expand this list
    val AVAILABLE_VOICES = mapOf(
        "English (US) - Female" to "en-US-JennyNeural",
        "English (US) - Male" to "en-US-GuyNeural",
        "English (UK) - Female" to "en-GB-SoniaNeural",
        "English (UK) - Male" to "en-GB-RyanNeural",
        "Spanish (Spain) - Female" to "es-ES-ElviraNeural",
        "French (France) - Female" to "fr-FR-DeniseNeural",
        "German (Germany) - Female" to "de-DE-KatjaNeural",
        "Japanese (Japan) - Female" to "ja-JP-NanamiNeural",
        "Chinese (Mandarin) - Female" to "zh-CN-XiaoxiaoNeural",
        "Italian (Italy) - Female" to "it-IT-ElsaNeural"
    )
}

class AzureTTSViewModel : ViewModel() {

    // State flows for UI
    private val _ttsState = MutableStateFlow(TTSState.IDLE)
    val ttsState: StateFlow<TTSState> = _ttsState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedVoice = MutableStateFlow("en-US-JennyNeural")
    val selectedVoice: StateFlow<String> = _selectedVoice.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f) // 0.5x to 2.0x speed
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _volume = MutableStateFlow(1.0f) // 0.0 to 1.0
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    // Azure Speech SDK objects
    private var speechSynthesizer: SpeechSynthesizer? = null
    private var currentSynthesisResult: Future<SpeechSynthesisResult>? = null

    /**
     * Initialize the Azure TTS service
     */
    fun initializeTTS(context: Context) {
        if (_isInitialized.value) {
            Log.d("AzureTTS", "TTS already initialized")
            return
        }

        viewModelScope.launch {
            try {
                _errorMessage.value = null

                withContext(Dispatchers.IO) {
                    // Validate configuration
                    if (AzureTTSConfig.SPEECH_SUBSCRIPTION_KEY == "YOUR_AZURE_SPEECH_KEY" ||
                        AzureTTSConfig.SPEECH_REGION == "YOUR_AZURE_REGION") {
                        throw Exception("Please configure your Azure Speech Service credentials in AzureTTSConfig")
                    }

                    // Create speech configuration
                    val speechConfig = SpeechConfig.fromSubscription(
                        AzureTTSConfig.SPEECH_SUBSCRIPTION_KEY,
                        AzureTTSConfig.SPEECH_REGION
                    )

                    // Configure audio output to default speaker
                    val audioConfig = AudioConfig.fromDefaultSpeakerOutput()

                    // Create speech synthesizer
                    speechSynthesizer = SpeechSynthesizer(speechConfig, audioConfig)

                    // Test the connection
                    speechSynthesizer?.let { synthesizer ->
                        val testResult = synthesizer.SpeakTextAsync("").get()
                        testResult?.close()
                    }

                    _isInitialized.value = true
                    Log.d("AzureTTS", "TTS initialized successfully")
                }
            } catch (e: Exception) {
                Log.e("AzureTTS", "Failed to initialize TTS", e)
                _errorMessage.value = "Failed to initialize TTS: ${e.message}"
                _ttsState.value = TTSState.ERROR
                _isInitialized.value = false
            }
        }
    }

    /**
     * Speak the given text using Azure TTS
     */
    fun speakText(text: String) {
        if (text.isBlank()) {
            Log.w("AzureTTS", "Attempted to speak empty text")
            return
        }

        if (!_isInitialized.value) {
            _errorMessage.value = "TTS not initialized. Please initialize first."
            return
        }

        viewModelScope.launch {
            try {
                _ttsState.value = TTSState.SPEAKING
                _errorMessage.value = null

                withContext(Dispatchers.IO) {
                    // Build SSML with current settings
                    val ssml = buildSSML(text, _selectedVoice.value, _speechRate.value, _volume.value)

                    Log.d("AzureTTS", "Speaking text with SSML: $ssml")

                    // Start speech synthesis
                    currentSynthesisResult = speechSynthesizer?.SpeakSsmlAsync(ssml)
                    val result = currentSynthesisResult?.get()

                    // Handle the result
                    when (result?.reason) {
                        ResultReason.SynthesizingAudioCompleted -> {
                            Log.d("AzureTTS", "Speech synthesis completed successfully")
                            _ttsState.value = TTSState.IDLE
                        }
                        ResultReason.Canceled -> {
                            val cancellation = SpeechSynthesisCancellationDetails.fromResult(result)
                            Log.w("AzureTTS", "Speech synthesis canceled: ${cancellation.reason}")

                            if (cancellation.reason != CancellationReason.Error) {
                                _ttsState.value = TTSState.IDLE
                            } else {
                                _errorMessage.value = "Speech synthesis error: ${cancellation.errorDetails}"
                                _ttsState.value = TTSState.ERROR
                                Log.e("AzureTTS", "Cancellation error details: ${cancellation.errorDetails}")
                            }
                        }
                        else -> {
                            _errorMessage.value = "Unexpected result: ${result?.reason}"
                            _ttsState.value = TTSState.ERROR
                            Log.e("AzureTTS", "Unexpected synthesis result: ${result?.reason}")
                        }
                    }

                    // Clean up result
                    result?.close()
                }
            } catch (e: Exception) {
                Log.e("AzureTTS", "Error during speech synthesis", e)
                _errorMessage.value = "Speech synthesis failed: ${e.message}"
                _ttsState.value = TTSState.ERROR
            }
        }
    }

    /**
     * Stop current speech synthesis
     */
    fun stopSpeaking() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Cancel current synthesis
                    currentSynthesisResult?.cancel(true)

                    // Stop the synthesizer
                    speechSynthesizer?.StopSpeakingAsync()?.get()
                }

                _ttsState.value = TTSState.IDLE
                Log.d("AzureTTS", "Speech synthesis stopped")
            } catch (e: Exception) {
                Log.e("AzureTTS", "Error stopping speech synthesis", e)
                _errorMessage.value = "Failed to stop speech: ${e.message}"
            }
        }
    }

    /**
     * Set the voice for speech synthesis
     */
    fun setVoice(voice: String) {
        if (AzureTTSConfig.AVAILABLE_VOICES.containsValue(voice)) {
            _selectedVoice.value = voice
            Log.d("AzureTTS", "Voice changed to: $voice")
        } else {
            Log.w("AzureTTS", "Invalid voice selected: $voice")
            _errorMessage.value = "Invalid voice selected: $voice"
        }
    }

    /**
     * Set the speech rate (0.5x to 2.0x)
     */
    fun setSpeechRate(rate: Float) {
        val clampedRate = rate.coerceIn(0.5f, 2.0f)
        _speechRate.value = clampedRate
        Log.d("AzureTTS", "Speech rate changed to: ${clampedRate}x")
    }

    /**
     * Set the volume (0.0 to 1.0)
     */
    fun setVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0.0f, 1.0f)
        _volume.value = clampedVolume
        Log.d("AzureTTS", "Volume changed to: ${(clampedVolume * 100).toInt()}%")
    }

    /**
     * Clear any error messages
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Get the display name for a voice
     */
    fun getVoiceDisplayName(voice: String): String {
        return AzureTTSConfig.AVAILABLE_VOICES.entries.find { it.value == voice }?.key ?: voice
    }

    /**
     * Check if currently speaking
     */
    fun isSpeaking(): Boolean {
        return _ttsState.value == TTSState.SPEAKING
    }

    /**
     * Build SSML (Speech Synthesis Markup Language) for the text
     */
    private fun buildSSML(text: String, voice: String, rate: Float, volume: Float): String {
        // Calculate rate percentage relative to normal speed
        val ratePercentage = ((rate - 1.0f) * 100).toInt()
        val rateString = if (ratePercentage >= 0) "+${ratePercentage}%" else "${ratePercentage}%"

        // Calculate volume percentage
        val volumePercentage = (volume * 100).toInt()

        // Determine language based on voice
        val language = when {
            voice.startsWith("en-US") -> "en-US"
            voice.startsWith("en-GB") -> "en-GB"
            voice.startsWith("es-ES") -> "es-ES"
            voice.startsWith("fr-FR") -> "fr-FR"
            voice.startsWith("de-DE") -> "de-DE"
            voice.startsWith("ja-JP") -> "ja-JP"
            voice.startsWith("zh-CN") -> "zh-CN"
            voice.startsWith("it-IT") -> "it-IT"
            else -> "en-US"
        }

        return """
            <speak version="1.0" xmlns="http://www.w3.org/2001/10/synthesis" xml:lang="$language">
                <voice name="$voice">
                    <prosody rate="$rateString" volume="$volumePercentage%">
                        $text
                    </prosody>
                </voice>
            </speak>
        """.trimIndent()
    }

    /**
     * Clean up resources when ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        try {
            // Stop any ongoing synthesis
            currentSynthesisResult?.cancel(true)

            // Close the synthesizer
            speechSynthesizer?.close()

            Log.d("AzureTTS", "TTS resources cleaned up")
        } catch (e: Exception) {
            Log.e("AzureTTS", "Error cleaning up TTS resources", e)
        }
    }
}