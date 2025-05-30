package com.example.neuroed

import com.example.neuroed.IconSizes


import android.Manifest
import android.app.Activity
import android.content.ComponentName
import com.example.neuroed.ui.theme.AppColors
import android.content.ContentUris
import android.content.Context
import androidx.compose.foundation.layout.RowScope
import com.google.accompanist.permissions.PermissionState
import androidx.compose.material3.NavigationBarItem
import androidx.compose.ui.res.painterResource
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import java.time.LocalDate
import android.os.Looper
import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.neuroed.R
import com.example.neuroed.model.CharacterGetData
import com.example.neuroed.model.TestCreate
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.SubjectSyllabusSaveRepository
import com.example.neuroed.repository.SubjectlistRepository
import com.example.neuroed.repository.TestCreateRepository
import com.example.neuroed.repository.UserCharacterGet
import com.example.neuroed.viewmodel.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.microsoft.cognitiveservices.speech.ResultReason
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechSynthesisCancellationDetails
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import com.example.neuroed.LibrarySection
import com.example.neuroed.repository.AttendanceRepository




import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.neuroed.DSColors.darkSurface
import com.example.neuroed.HelpSupportColors.darkSurface
import com.example.neuroed.HelpSupportColors.darkText
import com.example.neuroed.model.UserInfoViewModel
import kotlin.math.sqrt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import com.example.neuroed.model.SubjectlistResponse
import com.example.neuroed.network.ApiHelper.getCurrentUserId
import com.example.neuroed.repository.UserProfileRepository
import com.example.neuroed.voice.GlobalVoiceManager
import com.example.neuroed.voice.VoiceEnabledScreen
//import org.json.JSONObject



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import okhttp3.*
//import okio.ByteString
import android.util.Base64
//import android.util.Log
import org.json.JSONObject
import java.util.UUID
//import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
//import kotlin.random.Random

//=====================================================

// Core Animation Imports
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.animateColor

// Coroutine Imports
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

// Canvas and Graphics Imports
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Math and Random Imports
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.abs




//======================================================


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos









const val TTS_DEBUG_TAG = "TTS_DEBUG"
const val MIC_DEBUG_TAG = "MicDebug"
const val VM_DEBUG_TAG = "ViewModelDebug"

data class AIProfile(
    val image: Int,
    val name: String,
    val description: String,
    val rating: Double
)

val aiProfiles = listOf(
    AIProfile(R.drawable.biology, "DreamScapeAI", "Deep learning...", 4.6),
    AIProfile(R.drawable.biology, "NeuroVision",  "Next-gen AI-powe...",     4.7),
    AIProfile(R.drawable.biology, "MindGenix",    "Enhancing ideas with..",            4.5)
)

// ----------------------------------------------------------------------------------------
// ViewModel for VoiceMessages



class VoiceMessageViewModel : ViewModel() {

    private val _receivedMessage = MutableStateFlow("")
    val receivedMessage = _receivedMessage.asStateFlow()

    // Enhanced voice pattern data
    data class VoicePattern(
        val averageFrequency: Float,
        val pitchVariation: Float,
        val speakingRate: Float,
        val voiceprint: String // Unique voice fingerprint
    )

    data class SpeakerInfo(
        val customUniqueId: String,
        val displayName: String,
        val azureSpeakerId: String,
        val lastActiveAt: Long = System.currentTimeMillis()
    )

    // Voice pattern storage
    private val registeredVoicePatterns = mutableMapOf<String, VoicePattern>()
    private val speakerMap = mutableMapOf<String, SpeakerInfo>() // Azure ID -> Speaker Info
    private var currentMainUserId: Int? = null

    // WebSocket connection
    private var webSocket: WebSocket? = null
    private val client: OkHttpClient = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val VM_DEBUG_TAG = "VoiceMessageVM"
    }

    // ========== VOICE PATTERN ANALYSIS ==========

    private fun analyzeVoicePattern(audioData: ShortArray): VoicePattern {
        val avgFreq = calculateAverageFrequency(audioData)
        val pitchVar = calculatePitchVariation(audioData)
        val speakingRate = calculateSpeakingRate(audioData)
        val voiceprint = generateVoiceFingerprint(audioData)

        return VoicePattern(avgFreq, pitchVar, speakingRate, voiceprint)
    }

    private fun calculateAverageFrequency(audioData: ShortArray): Float {
        // Simple frequency calculation (can be enhanced with FFT)
        var sum = 0.0
        for (sample in audioData) {
            sum += abs(sample.toInt().toDouble())  // ✅ Fixed: Short to Int to Double
        }
        return (sum / audioData.size).toFloat()
    }

    private fun calculatePitchVariation(audioData: ShortArray): Float {
        // Simple pitch variation calculation
        if (audioData.size <= 1) return 0f  // ✅ Added safety check

        var variations = 0f
        for (i in 1 until audioData.size) {
            val diff = abs(audioData[i].toInt() - audioData[i-1].toInt())  // ✅ Fixed: Convert to Int
            variations += diff
        }
        return variations / audioData.size
    }

    private fun calculateSpeakingRate(audioData: ShortArray): Float {
        // Simple speaking rate (words per minute estimate)
        val silenceThreshold = 1000
        var speechSamples = 0

        for (sample in audioData) {
            if (abs(sample.toInt()) > silenceThreshold) {  // ✅ Fixed: Convert to Int
                speechSamples++
            }
        }

        return if (audioData.isNotEmpty()) {  // ✅ Added safety check
            (speechSamples.toFloat() / audioData.size) * 100
        } else {
            0f
        }
    }

    private fun generateVoiceFingerprint(audioData: ShortArray): String {
        // Generate unique fingerprint (simplified version)
        val hashCode = audioData.contentHashCode()
        val avgFreq = calculateAverageFrequency(audioData)
        val fingerprint = "${hashCode}_${avgFreq.toInt()}_${audioData.size}"

        return Base64.encodeToString(fingerprint.toByteArray(), Base64.NO_WRAP)
    }

    private fun matchVoicePattern(currentPattern: VoicePattern): String? {
        registeredVoicePatterns.forEach { (speakerId, storedPattern) ->
            val similarity = calculateSimilarity(currentPattern, storedPattern)
            if (similarity > 0.85f) { // 85% match threshold
                Log.d(VM_DEBUG_TAG, "🎯 Voice matched: $speakerId (similarity: $similarity)")
                return speakerId
            }
        }
        return null
    }

    private fun calculateSimilarity(pattern1: VoicePattern, pattern2: VoicePattern): Float {
        val freqDiff = abs(pattern1.averageFrequency - pattern2.averageFrequency) / 1000f
        val pitchDiff = abs(pattern1.pitchVariation - pattern2.pitchVariation) / 1000f
        val rateDiff = abs(pattern1.speakingRate - pattern2.speakingRate) / 100f

        val totalDiff = (freqDiff + pitchDiff + rateDiff) / 3f
        return max(0f, 1f - totalDiff)  // ✅ Fixed: Using kotlin.math.max
    }

    // ========== USER REGISTRATION ==========

    fun registerMainUser(userId: Int, voiceSignature: String? = null) {
        currentMainUserId = userId

        val registrationPayload = """
        {
            "type": "user_registration",
            "user": {
                "id": $userId,
                "voice_signature": "${voiceSignature ?: "default_signature_$userId"}"
            },
            "timestamp": ${System.currentTimeMillis()}
        }
        """.trimIndent()

        sendWebSocketMessage(registrationPayload)
        Log.d(VM_DEBUG_TAG, "🔐 Main user registered: $userId")
    }

    // ========== SPEAKER MANAGEMENT ==========

    fun processVoiceInput(
        audioData: ShortArray,
        recognizedText: String,
        azureSpeakerId: String
    ) {
        if (recognizedText.isBlank()) return

        // Analyze voice pattern
        val currentVoicePattern = analyzeVoicePattern(audioData)

        // Try to match with existing voice patterns
        val matchedSpeakerId = matchVoicePattern(currentVoicePattern)

        val speakerInfo = if (matchedSpeakerId != null) {
            // Existing speaker recognized by voice
            getOrUpdateSpeakerInfo(matchedSpeakerId, azureSpeakerId)
        } else {
            // New speaker detected
            val newSpeakerId = generateUniqueSpeakerId()
            registeredVoicePatterns[newSpeakerId] = currentVoicePattern
            createNewSpeakerInfo(newSpeakerId, azureSpeakerId)
        }

        // Determine event type
        val eventType = determineEventType(speakerInfo.customUniqueId, azureSpeakerId)

        // Send conversation message
        sendConversationMessage(speakerInfo, recognizedText, eventType, azureSpeakerId)

        Log.d(VM_DEBUG_TAG, "🎤 Processed voice: ${speakerInfo.displayName} -> $recognizedText")
    }

    private fun generateUniqueSpeakerId(): String {
        val timestamp = System.currentTimeMillis()
        val random = Random.nextInt(1000, 9999)  // ✅ Fixed: Using kotlin.random.Random
        return "SPEAKER_${timestamp}_$random"
    }

    private fun getOrUpdateSpeakerInfo(speakerId: String, azureSpeakerId: String): SpeakerInfo {
        // Check if we have this speaker in current session
        speakerMap.values.find { it.customUniqueId == speakerId }?.let { existing ->
            // Update last active time
            val updated = existing.copy(
                lastActiveAt = System.currentTimeMillis(),
                azureSpeakerId = azureSpeakerId
            )
            speakerMap[azureSpeakerId] = updated
            return updated
        }

        // Create new session entry for returning speaker
        val speakerInfo = SpeakerInfo(
            customUniqueId = speakerId,
            displayName = getDisplayNameForSpeaker(speakerId),
            azureSpeakerId = azureSpeakerId,
            lastActiveAt = System.currentTimeMillis()
        )

        speakerMap[azureSpeakerId] = speakerInfo
        return speakerInfo
    }

    private fun createNewSpeakerInfo(speakerId: String, azureSpeakerId: String): SpeakerInfo {
        val displayName = generateDisplayName(speakerId)
        val speakerInfo = SpeakerInfo(
            customUniqueId = speakerId,
            displayName = displayName,
            azureSpeakerId = azureSpeakerId,
            lastActiveAt = System.currentTimeMillis()
        )

        speakerMap[azureSpeakerId] = speakerInfo
        Log.d(VM_DEBUG_TAG, "✨ New speaker created: $displayName ($speakerId)")

        return speakerInfo
    }

    private fun getDisplayNameForSpeaker(speakerId: String): String {
        // Try to get from stored data or generate new
        return when {
            speakerId.contains("MAIN") -> "Main User"
            speakerId.contains("USER") -> "User ${speakerId.takeLast(4)}"
            else -> "Speaker ${speakerId.takeLast(4)}"
        }
    }

    private fun generateDisplayName(speakerId: String): String {
        val speakerCount = speakerMap.size + 1
        return when (speakerCount) {
            1 -> "User A"
            2 -> "User B"
            3 -> "User C"
            else -> "User ${('A'.code + speakerCount - 1).toChar()}"
        }
    }

    private fun determineEventType(customUniqueId: String, azureSpeakerId: String): String {
        return when {
            !speakerMap.containsKey(azureSpeakerId) -> "new_speaker"
            speakerMap[azureSpeakerId]?.customUniqueId != customUniqueId -> "speaker_change"
            else -> "continued_speech"
        }
    }

    // ========== ENHANCED MESSAGE SENDING ==========

    private fun sendConversationMessage(
        speakerInfo: SpeakerInfo,
        message: String,
        eventType: String,
        azureSpeakerId: String
    ) {
        val messageId = UUID.randomUUID().toString()

        // ✅ Fixed: Proper JSON escaping
        val escapedMessage = message.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")

        val payload = """
        {
            "type": "conversation_message",
            "speaker": {
                "unique_id": "${speakerInfo.customUniqueId}",
                "display_name": "${speakerInfo.displayName}",
                "azure_speaker_id": "$azureSpeakerId"
            },
            "message": {
                "id": "$messageId",
                "text": "$escapedMessage",
                "timestamp": ${speakerInfo.lastActiveAt}
            },
            "event_type": "$eventType",
            "voice_pattern": {
                "average_frequency": 0.0,
                "pitch_variation": 0.0,
                "speaking_rate": 0.0,
                "voice_fingerprint": "temp_fingerprint"
            },
            "conversation_metadata": {
                "total_speakers": ${speakerMap.size},
                "current_active_speaker": "${speakerInfo.customUniqueId}",
                "main_user_id": $currentMainUserId
            }
        }
        """.trimIndent()

        sendWebSocketMessage(payload)
        Log.d(VM_DEBUG_TAG, "📤 Sent: $eventType from ${speakerInfo.displayName}")
    }

    // ========== WEBSOCKET CONNECTION ==========

    fun connectWebSocket() {
        if (webSocket != null) {
            Log.d(VM_DEBUG_TAG, "WebSocket already connected or connecting.")
            return
        }

        val actualUrlToUse = "ws://localhost:8000/api/Assistantconnection/"
        Log.d(VM_DEBUG_TAG, "Preparing to connect WebSocket to: $actualUrlToUse")
        val request = Request.Builder().url(actualUrlToUse).build()

        viewModelScope.launch(Dispatchers.IO) {
            Log.d(VM_DEBUG_TAG, "Attempting WebSocket connection...")
            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.i(VM_DEBUG_TAG, "✅ WebSocket Connected: ${response.message}")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.i(VM_DEBUG_TAG, "📥 WebSocket Received: $text")
                    handleWebSocketMessage(text)
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    Log.i(VM_DEBUG_TAG, "📥 WebSocket Received Bytes: ${bytes.hex()}")
                    _receivedMessage.value = bytes.utf8()
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.i(VM_DEBUG_TAG, "⚠️ WebSocket Closing: Code=$code, Reason='$reason'")
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.i(VM_DEBUG_TAG, "❌ WebSocket Closed: Code=$code, Reason='$reason'")
                    this@VoiceMessageViewModel.webSocket = null
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e(VM_DEBUG_TAG, "💥 WebSocket Failure: ${t.message}", t)
                    response?.let {
                        Log.e(VM_DEBUG_TAG, "Failure Response: Code=${it.code}, Message=${it.message}")
                    }
                    this@VoiceMessageViewModel.webSocket = null
                }
            })
        }
    }

    private fun handleWebSocketMessage(message: String) {
        try {
            // Parse and handle different message types
            val jsonObject = JSONObject(message)
            val messageType = jsonObject.optString("type", "unknown")

            when (messageType) {
                "ai_response" -> {
                    val aiMessage = jsonObject.optString("message", "")
                    val speakerInfo = jsonObject.optJSONObject("speaker")
                    val speakerName = speakerInfo?.optString("display_name", "Unknown")

                    Log.d(VM_DEBUG_TAG, "🤖 AI Response for $speakerName: $aiMessage")
                    _receivedMessage.value = message
                }
                "registration_success" -> {
                    Log.d(VM_DEBUG_TAG, "✅ User registration successful")
                    _receivedMessage.value = message
                }
                "error" -> {
                    val errorMessage = jsonObject.optString("message", "Unknown error")
                    Log.e(VM_DEBUG_TAG, "❌ Server Error: $errorMessage")
                    _receivedMessage.value = message
                }
                else -> {
                    Log.d(VM_DEBUG_TAG, "📥 Unknown message type: $messageType")
                    _receivedMessage.value = message
                }
            }
        } catch (e: Exception) {
            Log.e(VM_DEBUG_TAG, "Error parsing WebSocket message: ${e.message}")
            _receivedMessage.value = message
        }
    }

    private fun sendWebSocketMessage(jsonPayload: String) {
        val currentWebSocket = webSocket
        if (currentWebSocket == null) {
            Log.e(VM_DEBUG_TAG, "Cannot send message, WebSocket is null. Attempting to reconnect...")
            connectWebSocket()
            return
        }

        Log.d(VM_DEBUG_TAG, "📤 Sending: $jsonPayload")
        val sent = currentWebSocket.send(jsonPayload)

        if (sent) {
            Log.d(VM_DEBUG_TAG, "✅ Message queued successfully")
        } else {
            Log.e(VM_DEBUG_TAG, "❌ Failed to queue message")
            connectWebSocket()
        }
    }

    // ========== LEGACY SUPPORT ==========

    fun sendVoiceMessage(text: String) {
        if (text.isBlank()) {
            Log.w(VM_DEBUG_TAG, "Attempted to send blank message. Skipping.")
            return
        }

        val escapedText = text.replace("\\", "\\\\").replace("\"", "\\\"")
        val jsonPayload = "{\"message\": \"$escapedText\"}"
        sendWebSocketMessage(jsonPayload)
    }

    fun clearReceivedMessage() {
        if (_receivedMessage.value.isNotEmpty()) {
            Log.d(VM_DEBUG_TAG, "Clearing received message state.")
            _receivedMessage.value = ""
        }
    }

    // ========== CLEANUP ==========

    override fun onCleared() {
        super.onCleared()
        Log.d(VM_DEBUG_TAG, "🧹 ViewModel cleared. Closing WebSocket.")
        webSocket?.close(1000, "ViewModel cleared")
        registeredVoicePatterns.clear()
        speakerMap.clear()
    }
}


// ----------------------------------------------------------------------------------------
// Helper functions for Speech
// ----------------------------------------------------------------------------------------
private fun getSpeechRecognizerErrorText(errorCode: Int): String {
    return when (errorCode) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
        SpeechRecognizer.ERROR_NETWORK -> "Network error"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
        SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
        SpeechRecognizer.ERROR_SERVER -> "Server error"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
        else -> "Unknown speech error ($errorCode)"
    }
}

private fun restartListening(
    context: Context,
    recognizer: SpeechRecognizer,
    listener: RecognitionListener,
    handler: Handler,
    delayMillis: Long = 500L,
    onRestart: (Runnable) -> Unit,
    isMicOn: () -> Boolean
) {
    recognizer.cancel()
    val runnable = Runnable {
        Log.d(MIC_DEBUG_TAG, "Restart runnable executing; isMicOn = ${isMicOn()}")
        if (isMicOn()) {
            startRegularListening(context, recognizer, listener)
        } else {
            Log.d(MIC_DEBUG_TAG, "Restart aborted; mic is off.")
        }
    }
    onRestart(runnable)
    handler.postDelayed(runnable, delayMillis)
}

private fun startRegularListening(
    context: Context,
    recognizer: SpeechRecognizer,
    listener: RecognitionListener
) {
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
        Log.e(MIC_DEBUG_TAG, "Speech recognition service is not available on this device.")
        Toast.makeText(context, "Speech recognition not available.", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1500)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
            // FIX 19: Add secure flag to prevent issues
            putExtra(RecognizerIntent.EXTRA_SECURE, false)
        }

        recognizer.setRecognitionListener(listener)
        recognizer.startListening(intent)
        Log.d(MIC_DEBUG_TAG, "SpeechRecognizer.startListening() called successfully.")

    } catch (e: SecurityException) {
        Log.e(MIC_DEBUG_TAG, "SecurityException starting listening: ${e.message}")
        Toast.makeText(context, "Audio recording permission denied.", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e(MIC_DEBUG_TAG, "Exception starting listening: ${e.message}")
        Toast.makeText(context, "Could not start voice input.", Toast.LENGTH_SHORT).show()
    }
}

// ----------------------------------------------------------------------------------------
// Azure TTS (Synthesizer)
// ----------------------------------------------------------------------------------------
//private suspend fun synthesizeSpeech(synthesizer: SpeechSynthesizer, text: String) =
//    withContext(Dispatchers.IO) {
//        if (text.isBlank()) {
//            Log.d(TTS_DEBUG_TAG, "Skipping synthesis for blank/empty text.")
//            return@withContext
//        }
//        try {
//            Log.d(TTS_DEBUG_TAG, "Attempting to synthesize: '$text'")
//            val result = synthesizer.SpeakTextAsync(text).get()
//            when (result.reason) {
//                ResultReason.SynthesizingAudioCompleted -> {
//                    Log.d(
//                        TTS_DEBUG_TAG,
//                        "Speech synthesis completed successfully for '$text'. Audio duration: ${result.audioDuration}"
//                    )
//                }
//                ResultReason.Canceled -> {
//                    val cancellation = SpeechSynthesisCancellationDetails.fromResult(result)
//                    Log.e(TTS_DEBUG_TAG, "Speech synthesis CANCELED for '$text'")
//                    Log.e(TTS_DEBUG_TAG, ">> Reason: ${cancellation.reason}")
//                    if (cancellation.reason == com.microsoft.cognitiveservices.speech.CancellationReason.Error) {
//                        Log.e(TTS_DEBUG_TAG, ">> ErrorCode: ${cancellation.errorCode}")
//                        Log.e(TTS_DEBUG_TAG, ">> ErrorDetails: ${cancellation.errorDetails}")
//                    }
//                }
//                else -> {
//                    Log.w(TTS_DEBUG_TAG, "Speech synthesis result for '$text': ${result.reason}")
//                }
//            }
//            result.close()
//        } catch (e: Exception) {
//            Log.e(TTS_DEBUG_TAG, "Speech synthesis FAILED for '$text': ${e.message}", e)
//        }
//    }

// ----------------------------------------------------------------------------------------
// HomeScreen
// ----------------------------------------------------------------------------------------





/* ----------  Design-System Tokens ---------- */
object DSColors {
    val primary         = Color(0xFF6A5ACD)
    val primaryLight    = Color(0xFF9370DB)
    val background      = Color(0xFFF9FAFB)
    val surface         = Color(0xFFFFFFFF)
    val surfaceHighlight= Color(0xFFE6E6FA)
    val textDark        = Color(0xFF1F2937)
    val textLight       = Color(0xFF6B7280)
    val divider         = Color(0xFFE5E7EB)
    val success         = Color(0xFF34D399)
    val info            = Color(0xFF3B82F6)
    val warning         = Color(0xFFFBBF24)

    /* dark tokens */
    val darkBg          = Color(0xFF121212)
    val darkSurface     = Color(0xFF1E1E1E)
    val darkText        = Color(0xFFE0E0E0)
}

object DSSpacing { val xs = 4.dp; val sm = 8.dp; val md = 16.dp; val lg = 24.dp }
object DSRadius  { val card = 12.dp; val pill = 24.dp }
object DSMotion  {
    const val fade = 500
    const val slideOffsetY = -40
    val easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
    const val stagger = 100
}
object DSTypography {
    val h1 = TextStyle(fontSize = 24.sp , fontWeight = FontWeight.W700)
    val h2 = TextStyle(fontSize = 18.sp , fontWeight = FontWeight.W700)
    val body = TextStyle(fontSize = 15.sp , fontWeight = FontWeight.W500)
    val caption = TextStyle(fontSize = 12.sp , fontWeight = FontWeight.W400)
}
object IconSizes { val headerLogo = 48.dp; val headerBox = 80.dp; val drawerIcon = 25.dp; val socialIcon = 20.dp }

/* ----------  Theme ---------- */
private fun lightScheme() = lightColorScheme(
    primary = DSColors.primary,
    onPrimary = Color.White,
    primaryContainer = DSColors.primaryLight,
    background = DSColors.background,
    surface = DSColors.surface,
    onSurface = DSColors.textDark
)
private fun darkScheme() = darkColorScheme(
    primary = DSColors.primaryLight,
    onPrimary = Color.Black,
    primaryContainer = DSColors.primary,
    background = DSColors.darkBg,
    surface = DSColors.darkSurface,
    onSurface = DSColors.darkText
)

@Composable
fun BuddyAITheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) darkScheme() else lightScheme()
    MaterialTheme(colorScheme = colors, typography = Typography(
        titleLarge = DSTypography.h1,
        titleMedium = DSTypography.h2,
        bodyMedium  = DSTypography.body,
        labelSmall  = DSTypography.caption
    ), shapes = Shapes(extraSmall = RoundedCornerShape(DSRadius.card))) {
        content()
    }
}

/* ----------  Drawer Item Model ---------- */
data class DrawerItem(
    val label: String,
    val icon: ImageVector? = null,
    val iconRes: Int? = null,
    val route: String,
    val selected: Boolean = false,
    val badge: String? = null
)


@Immutable
data class SocialItem(val name: String, @DrawableRes val iconRes: Int)

// Inside HomeDrawerContent composable, before the Row:
val socialItems = listOf(
    SocialItem("Twitter",   R.drawable.twitter),
    SocialItem("Instagram", R.drawable.instagram),
    SocialItem("LinkedIn",  R.drawable.linkedin)
)

/* ----------  Home Drawer Content ---------- */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class
)
@Composable
fun HomeDrawerContent(
    navController: NavController,
    drawerState: DrawerState
) {
    val scope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val colors   = MaterialTheme.colorScheme
    val scroll   = rememberScrollState()
    val isDark   = isSystemInDarkTheme()
    val drawerBg by animateColorAsState(
        if (isDark) DSColors.darkBg else colors.surface,
        animationSpec = tween(DSMotion.fade, easing = DSMotion.easing)
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = DSMotion.fade,
                easing = DSMotion.easing
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = DSMotion.fade,
                easing = DSMotion.easing
            ),
            initialOffsetY = { DSMotion.slideOffsetY }   // -40 ➜ slides up
        )
    ) {
        ModalDrawerSheet(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .background(drawerBg),
            drawerShape = RoundedCornerShape(
                topEnd = DSRadius.card,
                bottomEnd = DSRadius.card
            ),
            drawerContainerColor = drawerBg,
            drawerContentColor  = colors.onSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(scroll)
            ) {
                /* ---------- Header ---------- */
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(if (isDark) drawerBg else colors.primary)
                        .padding(DSSpacing.md),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(IconSizes.headerBox)
                            .clip(RoundedCornerShape(DSRadius.card))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        if (isDark) drawerBg else colors.primaryContainer,
                                        if (isDark) drawerBg else colors.primary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = colors.onSurface,
                            modifier = Modifier.size(IconSizes.headerLogo)
                        )
                    }
                    Text(
                        text = "BUDDYAI",
                        style = DSTypography.h2,
                        color = colors.onSurface,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
                Spacer(Modifier.height(DSSpacing.sm))

                /* ---------- Navigation Items ---------- */
                val items = listOf(
                    DrawerItem("Home", Icons.Default.Home, null, "home", selected = true, badge = "New"),
                    DrawerItem("Notifications", Icons.Default.Star, null, "NotificationScreen", badge = "3"),
                    DrawerItem("Profile", Icons.Default.Person, null, "profile"),
                    DrawerItem("Settings", Icons.Default.Settings, null, "settings"),
                    DrawerItem("Subscription", Icons.Default.CheckCircle, null, "SubscriptionScreen", badge = "PRO"),
                    DrawerItem("Share", Icons.Default.Share, null, "share"),
                    DrawerItem("Help & Support", Icons.Default.Home, null, "HelpSupportScreen"),
                    DrawerItem("Logout", Icons.Default.ExitToApp, null, "logout")
                )
                items.forEachIndexed { idx, item ->
                    val labelCol = if (item.selected) colors.primary else colors.onSurface
                    NavigationDrawerItem(
                        label = { Text(item.label, style = DSTypography.body, color = labelCol) },
                        selected = item.selected,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(item.route)
                            }
                        },
                        icon = {
                            when {
                                item.icon != null -> Icon(item.icon, null, Modifier.size(IconSizes.drawerIcon))
                                item.iconRes != null -> Icon(
                                    painterResource(item.iconRes), null, Modifier.size(IconSizes.drawerIcon)
                                )
                            }
                        },
                        badge = item.badge?.let { { Text(it, style = DSTypography.caption) } },
                        modifier = Modifier
                            .padding(horizontal = DSSpacing.md, vertical = DSSpacing.xs)
                            .animateEnterExit(
                                enter = fadeIn(
                                    animationSpec = tween(
                                        DSMotion.fade,
                                        delayMillis = DSMotion.stagger * idx,
                                        easing = DSMotion.easing
                                    )
                                ) +slideInVertically(
                                    animationSpec = tween(
                                        durationMillis = DSMotion.fade,
                                        delayMillis    = DSMotion.stagger * idx,   // omit delay for the header call
                                        easing         = DSMotion.easing
                                    ),
                                    initialOffsetY = { DSMotion.slideOffsetY }     // constant → -40
                                )
                            ),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color.Transparent,
                            unselectedContainerColor = Color.Transparent,
                            unselectedTextColor = colors.onSurface,
                            selectedTextColor = colors.primary
                        )
                    )
                }

                Spacer(Modifier.weight(1f))

                /* ---------- Social Links ---------- */
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DSSpacing.md),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    socialItems.forEachIndexed { index, item ->
                        IconButton(onClick = { /* TODO: Navigate to ${item.name} */ }) {
                            Icon(
                                painter = painterResource(id = item.iconRes),
                                contentDescription = item.name,
                                tint = Color.Unspecified,         // uses default content color
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        if (index != socialItems.lastIndex) {
                            Spacer(modifier = Modifier.width(DSSpacing.sm))
                        }
                    }
                }

                /* ---------- Version ---------- */
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(DSSpacing.md),
                    contentAlignment = Alignment.Center
                ) {
                    Text("BUDDYAI v1.0", style = DSTypography.caption, color = colors.onSurface)
                }
            }
        }
    }
}



private fun synthesizeSpeechWithAnimation(
    synthesizer: SpeechSynthesizer,
    text: String,
    onSpeakingStateChanged: (Boolean) -> Unit
) {
    try {
        Log.d(TTS_DEBUG_TAG, "Starting speech synthesis with animation for: '$text'")

        // Start speaking animation
        onSpeakingStateChanged(true)

        // Launch coroutine for TTS
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = synthesizer.SpeakTextAsync(text).get()

                when (result.reason) {
                    ResultReason.SynthesizingAudioCompleted -> {
                        Log.d(TTS_DEBUG_TAG, "✅ Speech synthesis completed successfully")
                        // Keep animation running for a bit after speech completes for better UX
                        delay(500) // 500ms delay after speech ends
                        withContext(Dispatchers.Main) {
                            onSpeakingStateChanged(false)
                        }
                    }
                    ResultReason.Canceled -> {
                        val cancellation = SpeechSynthesisCancellationDetails.fromResult(result)
                        Log.w(TTS_DEBUG_TAG, "⏹️ Speech synthesis canceled: ${cancellation.reason}")
                        if (cancellation.reason == com.microsoft.cognitiveservices.speech.CancellationReason.Error) {
                            Log.e(TTS_DEBUG_TAG, "❌ Speech synthesis error: ${cancellation.errorDetails}")
                        }
                        withContext(Dispatchers.Main) {
                            onSpeakingStateChanged(false)
                        }
                    }
                    else -> {
                        Log.w(TTS_DEBUG_TAG, "⚠️ Unexpected speech synthesis result: ${result.reason}")
                        withContext(Dispatchers.Main) {
                            onSpeakingStateChanged(false)
                        }
                    }
                }
                result.close()
            } catch (e: Exception) {
                Log.e(TTS_DEBUG_TAG, "❌ Exception during speech synthesis: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onSpeakingStateChanged(false)
                }
            }
        }
    } catch (e: Exception) {
        Log.e(TTS_DEBUG_TAG, "❌ Exception starting speech synthesis: ${e.message}", e)
        onSpeakingStateChanged(false)
    }
}

// ✅ FALLBACK: Original function for backward compatibility
private fun synthesizeSpeech(synthesizer: SpeechSynthesizer, text: String) {
    GlobalScope.launch(Dispatchers.IO) {
        synthesizeSpeech(synthesizer, text)
    }
}




@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    voiceMessageViewModel: VoiceMessageViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {

    val recordAudioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val context = LocalContext.current

    // ✅ CRITICAL: AI Speaking state for animation with AI response text
    var isAISpeaking by remember { mutableStateOf(false) }
    var currentAIResponse by remember { mutableStateOf("") } // ✅ NEW: Store AI response text
    var aiSpeakingJob by remember { mutableStateOf<Job?>(null) }

    // ✅ CRITICAL: Cleanup voice when leaving HomeScreen
    DisposableEffect(Unit) {
        Log.d("HomeScreen", "🎤 HomeScreen voice session started")

        onDispose {
            Log.d("HomeScreen", "🛑 HomeScreen disposing - stopping voice")
            try {
                // Stop any active voice session
                if (GlobalVoiceManager.isListening.value) {
                    GlobalVoiceManager.stopListening()
                }
                // Stop AI speaking animation
                isAISpeaking = false
                currentAIResponse = "" // ✅ Clear AI response
                aiSpeakingJob?.cancel()
                // Small delay for cleanup
                kotlinx.coroutines.runBlocking {
                    kotlinx.coroutines.delay(100)
                }
            } catch (e: Exception) {
                Log.e("HomeScreen", "Error disposing voice: ${e.message}")
            }
        }
    }

    // ✅ Monitor navigation changes and cleanup voice
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            // If navigating away from home, stop voice and AI animation
            if (destination.route != "home" && destination.route != null) {
                Log.d("HomeScreen", "🚀 Navigating away from HomeScreen to: ${destination.route}")
                try {
                    if (GlobalVoiceManager.isListening.value) {
                        GlobalVoiceManager.stopListening()
                        Log.d("HomeScreen", "✅ Stopped voice due to navigation")
                    }
                    isAISpeaking = false
                    currentAIResponse = "" // ✅ Clear AI response
                    aiSpeakingJob?.cancel()
                } catch (e: Exception) {
                    Log.e("HomeScreen", "Error stopping voice on navigation: ${e.message}")
                }
            }
        }

        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    // ✅ CRITICAL: Use VoiceEnabledScreen wrapper exactly like PlaygroundScreen
    VoiceEnabledScreen(
        screenName = "HomeScreen",
        onVoiceResult = { text ->
            Log.d("HomeVoice", "✅ Voice result received: $text")
            // Don't auto-send, let user choose
        },
        onVoiceError = { error ->
            Log.e("HomeVoice", "❌ Voice error: $error")
            Toast.makeText(context, "Voice error: $error", Toast.LENGTH_SHORT).show()
        },
        onNetworkError = {
            Log.w("HomeVoice", "📡 Network error")
            Toast.makeText(context, "Network connection required", Toast.LENGTH_SHORT).show()
        },
        onPermissionRequired = {
            Log.w("HomeVoice", "🔐 Permission required")
            recordAudioPermission.launchPermissionRequest()
        },
        silenceTimeoutMs = 15000L,
        maxSessionDuration = 300000L, // 5 minutes
        autoRetryEnabled = true
    ) { isListening, recognizedText, networkAvailable, startVoice, stopVoice, restartSession ->

        // ✅ All your existing HomeScreen state management
        val lifecycleOwner = LocalLifecycleOwner.current
        val needsRefreshSubjects = remember { mutableStateOf(false) }

        val createSubjectLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val shouldRefresh = result.data?.getBooleanExtra("REFRESH_SUBJECTS", false) ?: false
                if (shouldRefresh) {
                    Log.d("HomeScreen", "Received result to refresh subjects")
                    needsRefreshSubjects.value = true
                }
            }
        }

        LaunchedEffect(needsRefreshSubjects.value) {
            if (needsRefreshSubjects.value) {
                delay(100)
                needsRefreshSubjects.value = false
            }
        }

        DisposableEffect(navController) {
            val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
                if (destination.route == "home" || destination.route == null) {
                    Log.d("HomeScreen", "Detected return to HomeScreen - refreshing subjects")
                    needsRefreshSubjects.value = true
                }
            }

            navController.addOnDestinationChangedListener(listener)
            onDispose {
                navController.removeOnDestinationChangedListener(listener)
            }
        }

        val systemUiController = rememberSystemUiController()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        var currentSoundLevel by remember { mutableStateOf(0f) }

        SideEffect {
            systemUiController.setSystemBarsColor(
                color = Color.Black,
                darkIcons = false
            )
        }

        // Connect WebSocket when screen enters composition
        LaunchedEffect(Unit) {
            Log.d("HomeScreen", "Requesting ViewModel to connect to Assistant WebSocket.")
            voiceMessageViewModel.connectWebSocket()
        }

        // All your existing TTS setup...
        val speechSynthesizer = remember { mutableStateOf<SpeechSynthesizer?>(null) }
        val speechConfig = remember { mutableStateOf<SpeechConfig?>(null) }
        val audioConfig = remember { mutableStateOf<AudioConfig?>(null) }

        val userId = 1
        val api = RetrofitClient.apiService
        val repo = remember { AttendanceRepository(api) }
        val vm: AttendanceViewModel = viewModel(
            factory = AttendanceViewModelFactory(repo)
        )

        val attendance by vm.attendance.collectAsState()
        val loading by vm.isLoading.collectAsState()
        val error by vm.errorMessage.collectAsState()

        LaunchedEffect(Unit) {
            vm.markToday(userId)
        }

        LaunchedEffect(Unit) {
            try {
                val speechKey = "4a5877d33caa49bcbc19eade2c0fc602"
                val serviceRegion = "centralindia"
                Log.d(TTS_DEBUG_TAG, "Initializing Azure speech synthesizer...")
                speechConfig.value = SpeechConfig.fromSubscription(speechKey, serviceRegion).apply {
                    speechSynthesisVoiceName = "en-US-AvaMultilingualNeural"
                }
                audioConfig.value = AudioConfig.fromDefaultSpeakerOutput()
                speechSynthesizer.value = SpeechSynthesizer(speechConfig.value, audioConfig.value)
                Log.d(TTS_DEBUG_TAG, "Speech synthesizer initialized successfully.")
            } catch (e: Exception) {
                Log.e(TTS_DEBUG_TAG, "Failed to initialize speech synthesizer: ${e.message}", e)
                Toast.makeText(context, "TTS Initialization Failed", Toast.LENGTH_SHORT).show()
            }
        }

        val clearText = remember {
            {
                Log.d("HomeVoice", "🧹 Clearing recognized text")
                GlobalVoiceManager.clearCurrentText()
            }
        }

        // ✅ ENHANCED: Monitor assistant reply and trigger AI speaking animation with text
        val assistantReply = voiceMessageViewModel.receivedMessage.collectAsState().value
        LaunchedEffect(assistantReply) {
            val synthesizer = speechSynthesizer.value
            if (assistantReply.isNotEmpty() && synthesizer != null) {
                try {
                    val jsonObject = org.json.JSONObject(assistantReply)
                    val replyText = jsonObject.optString("message", assistantReply)
                    Log.d(TTS_DEBUG_TAG, "Assistant reply received, attempting synthesis for: '$replyText'")

                    // ✅ SET AI RESPONSE TEXT FOR TYPEWRITER
                    currentAIResponse = replyText
                    isAISpeaking = true
                    Log.d("HomeScreen", "🤖 AI Speaking animation started with text: '${replyText.take(50)}...'")

                    // Enhanced speech synthesis with animation control
                    synthesizeSpeechWithAnimation(synthesizer, replyText) { speaking ->
                        isAISpeaking = speaking
                        if (!speaking) {
                            Log.d("HomeScreen", "🤖 AI Speaking animation stopped")
                            // ✅ CLEAR AI RESPONSE TEXT WHEN SPEAKING STOPS
                            currentAIResponse = ""
                        }
                    }

                } catch (e: org.json.JSONException) {
                    Log.w(TTS_DEBUG_TAG, "Received message is not valid JSON ('$assistantReply'). Synthesizing raw text.")

                    // ✅ SET AI RESPONSE TEXT for raw text too
                    currentAIResponse = assistantReply
                    isAISpeaking = true
                    Log.d("HomeScreen", "🤖 AI Speaking animation started (raw text): '${assistantReply.take(50)}...'")

                    synthesizeSpeechWithAnimation(synthesizer, assistantReply) { speaking ->
                        isAISpeaking = speaking
                        if (!speaking) {
                            Log.d("HomeScreen", "🤖 AI Speaking animation stopped (raw text)")
                            // ✅ CLEAR AI RESPONSE TEXT WHEN SPEAKING STOPS
                            currentAIResponse = ""
                        }
                    }
                } finally {
                    voiceMessageViewModel.clearReceivedMessage()
                }
            } else if (assistantReply.isNotEmpty() && synthesizer == null) {
                Log.w(TTS_DEBUG_TAG, "Received message but synthesizer is not ready.")
                voiceMessageViewModel.clearReceivedMessage()
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                Log.d(TTS_DEBUG_TAG, "HomeScreen disposing. Cleaning up TTS resources.")
                scope.launch(Dispatchers.IO) {
                    try {
                        isAISpeaking = false
                        currentAIResponse = "" // ✅ Clear AI response
                        aiSpeakingJob?.cancel()
                        speechSynthesizer.value?.close()
                        speechConfig.value?.close()
                        audioConfig.value?.close()
                        Log.d(TTS_DEBUG_TAG, "TTS resources closed.")
                    } catch (e: Exception) {
                        Log.e(TTS_DEBUG_TAG, "Error cleaning up TTS resources: ${e.message}")
                    }
                }
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerContent = {
                HomeDrawerContent(navController, drawerState)
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent
                        ),
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        title = { },
                        actions = {
                            IconButton(onClick = { }) {
                                Icon(
                                    painter = painterResource(R.drawable.premium),
                                    contentDescription = "Premium Features",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.Unspecified
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    // ✅ CRITICAL: Pass the voice states from VoiceEnabledScreen
                    BottomNavigationBar(
                        navController = navController,
                        voiceMessageViewModel = voiceMessageViewModel,
                        onSoundLevelChanged = { level -> currentSoundLevel = level },
                        recordAudioPermission = recordAudioPermission,
                        // ✅ Enhanced voice parameters
                        isListening = isListening,
                        recognizedText = recognizedText,
                        networkAvailable = networkAvailable,
                        startVoice = startVoice,
                        stopVoice = stopVoice,
                        restartSession = restartSession,
                        onClearText = clearText
                    )
                }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                        ) {
                            // ✅ UPDATED: Pass AI response text to animation
                            EmotionsDiagramAnimatedUI(
                                soundLevel = currentSoundLevel,
                                isAISpeaking = isAISpeaking,
                                aiResponseText = currentAIResponse, // ✅ Pass current AI response
                                showFullResponse = false // ✅ Show summary for better UX
                            )
                        }
                    }

                    item { SectionHeader(title = "SyllabusHub") }
                    item {
                        SubjectList(
                            navController = navController,
                            forceRefresh = needsRefreshSubjects.value
                        )
                    }
                    item {
                        Text(
                            text = "VirtuBeings",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .padding(start = 16.dp, top = 8.dp, bottom = 6.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        AIProfileScreen(navController)
                    }
                    item { EnhancedUserProfile(navController) }
                    item { GridScreen(navController) }
                    item { EnhancedAgentList(navController) }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item { TrendingSection(navController) }
                    item { LibrarySection(navController) }
                    item { Animation(navController) }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------------------
// Bottom NavigationBar (unchanged except for minor label updates)
// ----------------------------------------------------------------------------------------

@Composable
fun RowScope.CustomNavigationBarItem(
    label: String,
    iconResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = isSelected,
        onClick = onClick,
        icon = {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = label,
                modifier = Modifier.size(22.dp),
                tint = Color.Unspecified
            )
        },
        label = {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BottomNavigationBar(
    navController: NavController,
    voiceMessageViewModel: VoiceMessageViewModel,
    onSoundLevelChanged: (Float) -> Unit = {},
    recordAudioPermission: PermissionState,
    // ✅ Enhanced parameters from VoiceEnabledScreen
    isListening: Boolean = false,
    recognizedText: String = "",
    networkAvailable: Boolean = true,
    startVoice: () -> Unit = {},
    stopVoice: () -> Unit = {},
    restartSession: () -> Unit = {},
    onClearText: () -> Unit = {} // ✅ NEW: Proper text clearing
) {
    var selectedRoute by remember { mutableStateOf("CreateSubjectScreen") }
    var showNetworkError by remember { mutableStateOf(false) }
    var lastSentText by remember { mutableStateOf("") }

    // ✅ Prevent duplicate message sends
    val canSendMessage = remember(recognizedText, lastSentText) {
        recognizedText.isNotEmpty() &&
                recognizedText != lastSentText &&
                recognizedText.length >= 3
    }

    // ✅ Debug logging with throttling
    LaunchedEffect(isListening, recognizedText) {
        Log.d("BottomNav", "🔄 State update - isListening: $isListening, text: '${recognizedText.take(30)}${if(recognizedText.length > 30) "..." else ""}'")
    }

    // ✅ Network status monitoring
    LaunchedEffect(networkAvailable) {
        if (!networkAvailable && isListening) {
            showNetworkError = true
            stopVoice()
        }
    }

    Surface(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {

            // ✅ Network error banner
            AnimatedVisibility(
                visible = showNetworkError,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Network connection required for voice input",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { showNetworkError = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // ✅ Enhanced voice input display
            AnimatedVisibility(
                visible = recognizedText.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = recognizedText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            // ✅ Character count indicator
                            if (recognizedText.isNotEmpty()) {
                                Text(
                                    text = "${recognizedText.length} characters",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // ✅ Action buttons row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Clear button
                            IconButton(
                                onClick = {
                                    Log.d("BottomNav", "🧹 Clearing text")
                                    onClearText()
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Clear Text",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Send button
                            AnimatedVisibility(visible = canSendMessage) {
                                IconButton(
                                    onClick = {
                                        Log.d("BottomNav", "📤 Sending voice message: $recognizedText")
                                        try {
                                            voiceMessageViewModel.sendVoiceMessage(recognizedText)
                                            lastSentText = recognizedText
                                            onClearText()
                                        } catch (e: Exception) {
                                            Log.e("BottomNav", "❌ Error sending message", e)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send Message",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ✅ Enhanced listening status indicator
            AnimatedVisibility(
                visible = isListening,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // ✅ Enhanced pulsing indicator
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val pulseAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, easing = EaseInOutCubic),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseAlpha"
                        )

                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 0.8f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = EaseInOutCubic),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseScale"
                        )

                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .scale(pulseScale)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha),
                                    CircleShape
                                )
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🎤 Listening for speech...",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            if (recognizedText.isNotEmpty()) {
                                Text(
                                    text = "\"${recognizedText.take(40)}${if (recognizedText.length > 40) "..." else ""}\"",
                                    style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // ✅ Stop and restart buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    Log.d("BottomNav", "🔄 Restart session clicked")
                                    restartSession()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Restart Session",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    Log.d("BottomNav", "🛑 Stop button clicked")
                                    stopVoice()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Stop Listening",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ✅ Navigation Bar
            NavigationBar(
                containerColor = Color.Transparent,
                modifier = Modifier.fillMaxWidth().height(70.dp)
            ) {

                CustomNavigationBarItem(
                    label = "Subject",
                    iconResId = R.drawable.homeicon,
                    isSelected = selectedRoute == "CreateSubjectScreen",
                    onClick = {
                        selectedRoute = "CreateSubjectScreen"
                        navController.navigate("CreateSubjectScreen") { launchSingleTop = true }
                    }
                )

                // ✅ ENHANCED: Production-ready mic button
                val handleVoiceButtonClick = remember {
                    {
                        Log.d("BottomNav", "🎤 Mic button clicked - State: listening=$isListening, permission=${recordAudioPermission.status.isGranted}, network=$networkAvailable")

                        when {
                            !recordAudioPermission.status.isGranted -> {
                                Log.d("BottomNav", "🔒 Requesting audio permission")
                                recordAudioPermission.launchPermissionRequest()
                            }
                            !networkAvailable -> {
                                Log.w("BottomNav", "📡 No network available")
                                showNetworkError = true
                            }
                            isListening -> {
                                Log.d("BottomNav", "🛑 Stopping voice recognition")
                                stopVoice()
                            }
                            else -> {
                                Log.d("BottomNav", "▶️ Starting voice session")
                                showNetworkError = false
                                onClearText() // Clear through proper callback
                                startVoice()
                            }
                        }
                    }
                }

                NavigationBarItem(
                    selected = false,
                    onClick = handleVoiceButtonClick,
                    icon = {
                        // ✅ Enhanced visual feedback with better animations
                        val animatedColor by animateColorAsState(
                            targetValue = when {
                                !recordAudioPermission.status.isGranted ->
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                !networkAvailable ->
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                isListening ->
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                else ->
                                    Color.Transparent
                            },
                            animationSpec = tween(300, easing = EaseInOutCubic),
                            label = "buttonColor"
                        )

                        val animatedSize by animateFloatAsState(
                            targetValue = if (isListening) 54f else 48f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "buttonSize"
                        )

                        Box(
                            modifier = Modifier
                                .size(animatedSize.dp)
                                .background(animatedColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            // ✅ Icon with proper states
                            Icon(
                                painter = painterResource(
                                    id = when {
                                        !recordAudioPermission.status.isGranted ->
                                            R.drawable.baseline_mic_off_24
                                        !networkAvailable ->
                                            R.drawable.baseline_mic_off_24
                                        isListening ->
                                            R.drawable.baseline_mic_24
                                        else ->
                                            R.drawable.baseline_mic_off_24
                                    }
                                ),
                                contentDescription = when {
                                    !recordAudioPermission.status.isGranted ->
                                        "Microphone Permission Required"
                                    !networkAvailable ->
                                        "Network Required"
                                    isListening ->
                                        "Stop Listening"
                                    else ->
                                        "Start Listening"
                                },
                                modifier = Modifier.size(26.dp),
                                tint = when {
                                    !recordAudioPermission.status.isGranted ||
                                            !networkAvailable ->
                                        MaterialTheme.colorScheme.error
                                    isListening ->
                                        MaterialTheme.colorScheme.primary
                                    else ->
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )

                            // ✅ Voice visualizer when listening
                            if (isListening) {
                                VoiceVisualizer(
                                    modifier = Modifier.matchParentSize(),
                                    barColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    soundLevel = 0.7f // Could be connected to actual audio levels
                                )
                            }
                        }
                    },
                    label = {
                        Text(
                            text = when {
                                !recordAudioPermission.status.isGranted -> "No Perm"
                                !networkAvailable -> "No Net"
                                isListening -> "Listening"
                                else -> "Voice"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                !recordAudioPermission.status.isGranted ||
                                        !networkAvailable ->
                                    MaterialTheme.colorScheme.error
                                isListening ->
                                    MaterialTheme.colorScheme.primary
                                else ->
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                )

                CustomNavigationBarItem(
                    label = "Matric",
                    iconResId = R.drawable.graph,
                    isSelected = selectedRoute == "MatricScreen",
                    onClick = {
                        selectedRoute = "MatricScreen"
                        navController.navigate("MatricScreen") { launchSingleTop = true }
                    }
                )

                CustomNavigationBarItem(
                    label = "Character",
                    iconResId = R.drawable.bot,
                    isSelected = selectedRoute == "CharacterCreateScreen",
                    onClick = {
                        selectedRoute = "CharacterCreateScreen"
                        navController.navigate("CharacterCreateScreen") {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ✅ Enhanced permission denied message
            AnimatedVisibility(
                visible = !recordAudioPermission.status.isGranted &&
                        recordAudioPermission.status is PermissionStatus.Denied &&
                        (recordAudioPermission.status as PermissionStatus.Denied).shouldShowRationale,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Microphone permission required for voice input",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { recordAudioPermission.launchPermissionRequest() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                "Grant",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}


// ----------------------------------------------------------------------------------------
// Section Header
// ----------------------------------------------------------------------------------------
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.W500,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.onBackground
    )
}

// ----------------------------------------------------------------------------------------
// Subject List
// ----------------------------------------------------------------------------------------




@Composable
fun SubjectList(
    navController: NavController,
    subjectlistViewModel: SubjectlistViewModel = viewModel(
        factory = SubjectlistViewModelFactory(
            SubjectlistRepository(RetrofitClient.apiService)
        )
    ),
    forceRefresh: Boolean = false
) {
    // Get the UserInfoViewModel
    val userInfoViewModel: UserInfoViewModel = viewModel()

    // Get the context
    val context = LocalContext.current

    // Observe the userId
    val userId by userInfoViewModel.userId.collectAsState()

    // Observe the preloaded subject list
    val subjects by subjectlistViewModel.subjectList.collectAsState()

    // Observe loading state (from the updated ViewModel)
    val isLoading by subjectlistViewModel.isLoading.collectAsState()

    // Consolidated refresh state - responds to both forceRefresh and ActivityResult
    val refreshRequested = remember { mutableStateOf(forceRefresh) }

    // Create activity result launcher
    val createSubjectLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val shouldRefresh = result.data?.getBooleanExtra("REFRESH_SUBJECTS", false) ?: false
            if (shouldRefresh) {
                Log.d("SubjectList", "Received result to refresh subjects")
                refreshRequested.value = true
            }
        }
    }

    // Load the userId when the composable is first created
    LaunchedEffect(Unit) {
        userInfoViewModel.loadUserId(context)
    }

    // Update based on forceRefresh changes from parent
    LaunchedEffect(forceRefresh) {
        if (forceRefresh) {
            refreshRequested.value = true
        }
    }

    // If the userId is valid, make sure the data is loaded
    // This is a safety mechanism in case the data wasn't preloaded
    LaunchedEffect(userId, refreshRequested.value) {
        if (userId != NeuroEdApp.INVALID_USER_ID) {
            if (refreshRequested.value) {
                // Force a refresh of the data
                Log.d("SubjectList", "Force refreshing subject list for user: $userId")
                subjectlistViewModel.refreshSubjectList(userId)
                // Reset the refresh flag after requesting refresh
                refreshRequested.value = false
            } else {
                // Normal load (will be a no-op if already loaded)
                Log.d("SubjectList", "Ensuring subject list is loaded for user: $userId")
                subjectlistViewModel.fetchSubjectList(userId)
            }
        }
    }

    // Track when the screen appears in composition using lifecycle
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Refresh the subject list when screen becomes visible again
                if (userId != NeuroEdApp.INVALID_USER_ID) {
                    Log.d("SubjectList", "Screen resumed - refreshing subject list")
                    subjectlistViewModel.refreshSubjectList(userId)
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Add a debug log to verify data availability
    LaunchedEffect(subjects) {
        Log.d("SubjectList", "Subjects available on render: ${subjects.size}")
    }

    // Main content
    if (isLoading && subjects.isEmpty()) {
        // Loading UI
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading subjects...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else if (subjects.isEmpty()) {
        // Empty state UI
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No subjects found",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        try {
                            val componentName = ComponentName(
                                context.packageName,
                                "com.example.neuroed.CreateSubjectScreen"
                            )
                            val intent = Intent().setComponent(componentName)
                            createSubjectLauncher.launch(intent)
                        } catch (e: Exception) {
                            Log.e("SubjectList", "Error launching CreateSubjectScreen", e)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Create Your First Subject")
                }
            }
        }
    } else {
        // Display the subjects in a row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Show only first 3 subjects
            val previewSubjects = subjects.take(3)
            items(previewSubjects) { subject ->
                SubjectCard(
                    subject = subject,
                    onClick = {
                        navController.navigate(
                            "SyllabusScreen/${subject.id}/${subject.subjectDescription}/${subject.subject}"
                        )
                    }
                )
            }

            // "See All" card
            item {
                SeeAllCard(onClick = { navController.navigate("FullSubjectListScreen") })
            }
        }
    }
}





// Subject Card component (extracted for reusability)
@Composable
fun SubjectCard(
    subject: SubjectlistResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .width(130.dp)
            .height(190.dp),
        shape = RoundedCornerShape(13.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = subject.subjectImage ?: R.drawable.biology,
                contentDescription = "Subject Image",
                placeholder = painterResource(id = R.drawable.mathssub),
                error = painterResource(id = R.drawable.biology),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(60.dp),
                    progress = 1.0f,
                    color = Color.White,
                    strokeWidth = 6.dp
                )
                Text(
                    text = "100%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subject.subject,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// SeeAll Card component (extracted for reusability)
@Composable
fun SeeAllCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .width(130.dp)
            .height(190.dp),
        shape = RoundedCornerShape(13.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_add_24),
                    contentDescription = "See All",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "See All",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


// ----------------------------------------------------------------------------------------
// Enhanced UserProfile (now matches AIProfileCard color)
// ----------------------------------------------------------------------------------------
@Composable
fun EnhancedUserProfile(
    navController: NavController
) {
    val context = LocalContext.current

    val userId by remember {
        derivedStateOf {
            val sharedPrefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            sharedPrefs.getInt("userInfoId", -1)
        }
    }

    if (userId == -1) {
        // Loading state
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val apiService = remember { RetrofitClient.apiService }
    val repository = remember { UserProfileRepository(apiService) }
    val viewModel: UserProfileViewModel = viewModel(
        factory = UserProfileViewModelFactory(repository, userId)
    )

    // Observe the profile data
    val profileState by viewModel.userProfile.observeAsState()

    // ✅ SOLUTION: Assign to local variable to enable smart cast
    val profile = profileState

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { navController.navigate("ProfileScreen") },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar
            if (profile?.avatarUrl?.isNotEmpty() == true) {
                AsyncImage(
                    model = profile.avatarUrl, // ✅ Now this works
                    contentDescription = "User Profile",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Profile",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                // Real user name
                Text(
                    text = profile?.user_name ?: "Loading...",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // XP Progress Bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val progressFraction = if (profile != null && profile.maxXpForLevel > 0) {
                        profile.currentXp.toFloat() / profile.maxXpForLevel.toFloat() // ✅ Works now
                    } else 0f

                    LinearProgressIndicator(
                        progress = progressFraction,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Level ${profile?.level ?: 0}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stars based on level
                Row {
                    val userLevel = profile?.level ?: 0
                    val filledStars = minOf(5, maxOf(0, userLevel / 2))

                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star",
                            tint = if (index < filledStars) Color(0xFFFFD700) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}


// ----------------------------------------------------------------------------------------
// VoiceVisualizer
// ----------------------------------------------------------------------------------------
@Composable
fun VoiceVisualizer(
    modifier: Modifier = Modifier,
    soundLevel: Float = 0.5f, // ✅ Added missing soundLevel parameter
    barCount: Int = 4,
    barColor: Color = Color.White,
    minHeight: Dp = 3.dp,
    maxHeight: Dp = 12.dp,
    barWidth: Dp = 2.dp,
    spaceBetween: Dp = 1.dp,
    animationSpeed: Int = 400, // ✅ Configurable animation speed
    isActive: Boolean = true // ✅ Control animation state
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voiceVisualizer")

    // ✅ Sound level affects animation intensity
    val adjustedSoundLevel = soundLevel.coerceIn(0f, 1f)
    val intensityMultiplier = if (isActive) (0.3f + adjustedSoundLevel * 0.7f) else 0f

    val animations = List(barCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = intensityMultiplier,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = animationSpeed + (index * 100),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    Row(
        modifier = modifier.wrapContentSize(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(spaceBetween)
    ) {
        animations.forEachIndexed { index, anim ->
            // ✅ Different heights based on sound level and index
            val baseHeight = minHeight + (maxHeight - minHeight) * anim.value
            val variationFactor = 0.8f + (index % 2) * 0.4f // Alternate bar heights
            val finalHeight = baseHeight * variationFactor

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(finalHeight.coerceAtLeast(minHeight))
                    .background(
                        color = barColor.copy(
                            alpha = if (isActive) 0.7f + anim.value * 0.3f else 0.3f
                        ),
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

// ✅ Enhanced Voice Visualizer with Real-time Sound Level
@Composable
fun EnhancedVoiceVisualizer(
    modifier: Modifier = Modifier,
    soundLevel: Float = 0.5f,
    isListening: Boolean = false,
    barCount: Int = 6,
    barColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    showBackground: Boolean = true
) {
    val containerModifier = if (showBackground) {
        modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    } else {
        modifier
    }

    Box(
        modifier = containerModifier,
        contentAlignment = Alignment.Center
    ) {
        if (isListening) {
            VoiceVisualizer(
                soundLevel = soundLevel,
                barCount = barCount,
                barColor = barColor,
                minHeight = 4.dp,
                maxHeight = 16.dp,
                barWidth = 3.dp,
                spaceBetween = 2.dp,
                animationSpeed = 300,
                isActive = true
            )
        } else {
            // ✅ Static bars when not listening
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(barCount) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(4.dp)
                            .background(
                                color = barColor.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(1.dp)
                            )
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------------------
// GridScreen
// ----------------------------------------------------------------------------------------
data class GridItemModel(
    val title: String,
    val iconRes: Int,
    val route: String,
    val badgecount: Int = 0
)

@Composable
fun GridScreen(navController: NavController) {
    val items = listOf(
        GridItemModel("Test", R.drawable.test, "TestsScreen", badgecount = 2),
        GridItemModel("Task", R.drawable.taskuser, "TaskScreen", badgecount = 10),
        GridItemModel("Exam", R.drawable.exam, "ExamScreen", badgecount = 1),
        GridItemModel("Recall", R.drawable.recall, "RecallingScreen"),
        GridItemModel("Cloud", R.drawable.clouddownload, "StoreScreen"),
        GridItemModel("Mindmap", R.drawable.mindmap, "MindmapScreen"),
        GridItemModel("Assignents", R.drawable.writing, "AssignmentScreen"),
        GridItemModel("Playground", R.drawable.student, "PlaygroundScreen"),
        GridItemModel("Meditation", R.drawable.meditation, "MeditationScreen"),
        GridItemModel("ClassRoom", R.drawable.training, "ChallengeScreen"),
        GridItemModel("MultLang", R.drawable.translation, "LanguagelearnScreen"),
        GridItemModel("Game", R.drawable.game, "GamesScreen")

    )

    Box(
        modifier = Modifier
            .height(350.dp)
            .padding(16.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            userScrollEnabled = false
        ) {
            items(items) { item ->
                GridItem(
                    text = item.title,
                    iconRes = item.iconRes,
                    badgeCount = item.badgecount,
                    onClick = { navController.navigate(item.route) }
                )
            }
        }
    }
}

@Composable
fun GridItem(
    text: String,
    iconRes: Int,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge {
                        Text(
                            text = badgeCount.toString(),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = text,
                    tint = Color.Unspecified
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// ----------------------------------------------------------------------------------------
// Enhanced AgentList (now same color as AIProfile: surface)
// ----------------------------------------------------------------------------------------
@Composable
fun EnhancedAgentList(navController: NavController) {
    // Example agent names + images
    val agents = listOf("Areax", "Jin", "Luna")
    val iconPainter = painterResource(id = R.drawable.baseline_add_24)

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        // Use the same color as AIProfile => MaterialTheme.colorScheme.surface
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Agent",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                // Display each agent in a circle
                agents.forEach { agentName ->
                    Column(
                        modifier = Modifier.widthIn(min = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .clickable { navController.navigate("agent_action") }
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.man),
                                contentDescription = "Agent Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = agentName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        )
                    }
                }
                // Add new agent icon
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .clickable { navController.navigate("AgentCreateScreen") }
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = iconPainter,
                            contentDescription = "Add Agent",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Add Agent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

            }
        }
    }
}

// ----------------------------------------------------------------------------------------
// Emotions Diagram (BuddyAI glow animation remains unchanged)
// ----------------------------------------------------------------------------------------














// ----------------------------------------------------------------------------------------
// AI Profile Screen (with some spacing around the row)
// ----------------------------------------------------------------------------------------

fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (start * (1 - fraction) + stop * fraction)
}

@Composable
fun EmotionsDiagramAnimatedUI(
    modifier: Modifier = Modifier,
    soundLevel: Float = 0f,
    isAISpeaking: Boolean = false,
    aiResponseText: String = "", // ✅ NEW: AI response from backend
    showFullResponse: Boolean = false // ✅ NEW: Control full text or summary
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            JarvisStyleRing(
                text = "BuddyAI",
                ringColor = Color(0xFF00E5FF),
                ringRadiusRatio = 0.38f,
                externalSoundLevel = soundLevel,
                isAISpeaking = isAISpeaking,
                aiResponseText = aiResponseText, // ✅ Pass AI response
                showFullResponse = showFullResponse // ✅ Pass display preference
            )
        }
    }
}




@Composable
fun JarvisStyleRing(
    text: String,
    ringColor: Color,
    ringRadiusRatio: Float = 0.38f,
    externalSoundLevel: Float = 0f,
    isAISpeaking: Boolean = false,
    aiResponseText: String = "",
    showFullResponse: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var soundLevel by remember { mutableStateOf(externalSoundLevel) }
    var displayText by remember { mutableStateOf(text) }
    var isListening by remember { mutableStateOf(false) }
    var currentEmotion by remember { mutableStateOf("neutral") }

    // Typewriter animation states
    var typewriterText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    var currentCharIndex by remember { mutableStateOf(0) }

    LaunchedEffect(externalSoundLevel) {
        soundLevel = externalSoundLevel
    }

    // Stars animation data
    data class Star(
        val initialX: Float,
        val initialY: Float,
        val baseAlpha: Float,
        val twinkleOffset: Float,
        val twinkleSpeed: Float,
        val moveSpeed: Float
    )
    val starCount = 60
    val randomSeed = remember { Random(System.currentTimeMillis()) }
    val stars = remember {
        List(starCount) {
            Star(
                initialX = randomSeed.nextFloat(),
                initialY = randomSeed.nextFloat(),
                baseAlpha = 0.2f + randomSeed.nextFloat() * 0.6f,
                twinkleOffset = randomSeed.nextFloat() * 2 * PI.toFloat(),
                twinkleSpeed = 0.5f + randomSeed.nextFloat() * 1.5f,
                moveSpeed = 0.3f + randomSeed.nextFloat() * 1.5f
            )
        }
    }

    // Color transition for emotions
    val transitionSize = 200
    val colorTransition = updateTransition(currentEmotion, label = "colorTransition")
    val currentRingColor by colorTransition.animateColor(
        transitionSpec = { tween(transitionSize) },
        label = "ringColorAnimation"
    ) { emotion ->
        when (emotion) {
            "neutral"    -> Color(0xFF00E5FF)
            "excited"    -> Color(0xFFFF5722)
            "thinking"   -> Color(0xFF9C27B0)
            "agreeing"   -> Color(0xFF4CAF50)
            "curious"    -> Color(0xFFFFEB3B)
            "surprised"  -> Color(0xFFFF9800)
            "concerned"  -> Color(0xFFE91E63)
            else         -> ringColor
        }
    }

    // TTS Integration
    val ttsHolder = remember { mutableStateOf<TextToSpeech?>(null) }
    val tts = remember {
        TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsHolder.value?.apply {
                    setLanguage(Locale.getDefault())
                    setPitch(0.6f)
                    setSpeechRate(0.8f)
                }
            }
        }).also { ttsInstance ->
            ttsHolder.value = ttsInstance
        }
    }
    DisposableEffect(Unit) {
        onDispose { tts.shutdown() }
    }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    // Animation variables from SoundReactiveGlowingRing
    val baseRotationSpeed = 15000
    val rotationSpeed = remember { Animatable(baseRotationSpeed.toFloat()) }
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = rotationSpeed.value.toInt(),
                easing = LinearEasing
            )
        )
    )

    val basePulseMin = 0.95f
    val basePulseMax = 1.05f
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = basePulseMin,
        targetValue = basePulseMax,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        )
    )
    val dynamicPulse = remember { Animatable(1f) }

    val dynamicGlow = remember { Animatable(1f) }
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        )
    )
    val waveIntensity = remember { Animatable(1f) }

    val starTwinklePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val starMovementPhase = remember { Animatable(0f) }

    // Response patterns
    val responsesByEmotion = mapOf(
        "listening"  to listOf("Listening...", "I hear you", "I'm here", "Tell me"),
        "thinking"   to listOf("Processing...", "Analyzing...", "Thinking...", "Computing...", "Learning..."),
        "curious"    to listOf("Tell me more", "Go on...", "Interesting!", "And then?", "I'd like to know more"),
        "agreeing"   to listOf("I understand", "Got it", "I follow", "Makes sense", "I see what you mean"),
        "excited"    to listOf("That's great!", "Fascinating!", "Wonderful!", "Amazing!", "Brilliant!"),
        "surprised"  to listOf("Oh!", "Really?", "Wow!", "Surprising!", "Is that so?"),
        "concerned"  to listOf("Hmm...", "Let me check", "Are you sure?", "I'm not certain", "One moment...")
    )

    var lastTextChangeTime by remember { mutableStateOf(0L) }
    var soundPattern by remember { mutableStateOf(listOf<Float>()) }
    val patternSize = 5
    var consecutiveQuietFrames by remember { mutableStateOf(0) }
    var consecutiveLoudFrames by remember { mutableStateOf(0) }
    val quietThreshold = 0.03f
    val loudThreshold = 0.1f

    // Azure speech duration estimation - More accurate
    fun estimateAzureSpeechDuration(text: String): Int {
        val wordsPerMinute = 150f // Slower for better sync
        val wordCount = text.split(" ").size
        val estimatedMinutes = wordCount / wordsPerMinute
        val estimatedMs = (estimatedMinutes * 60 * 1000).toInt()
        val punctuationPauses = text.count { it in ".,!?;:" } * 500 // Longer pauses
        return (estimatedMs + punctuationPauses).coerceAtLeast(3000) // Minimum 3 seconds
    }

    // Real Azure TTS event-driven typewriter animation
    LaunchedEffect(aiResponseText, isAISpeaking) {
        if (isAISpeaking && aiResponseText.isNotEmpty()) {
            launch {
                isTyping = true
                typewriterText = ""
                currentCharIndex = 0

                delay(300) // Small delay before starting

                val words = aiResponseText.split(" ")
                val windowSize = 4 // Show 4 words at a time
                var currentWordIndex = 0

                // Start typewriter immediately when Azure TTS starts
                while (currentWordIndex < words.size && isAISpeaking) {
                    // Calculate window
                    val windowStart = maxOf(0, currentWordIndex - windowSize + 1)
                    val windowEnd = minOf(words.size, currentWordIndex + 1)
                    val windowWords = words.subList(windowStart, windowEnd)

                    // Update display
                    typewriterText = windowWords.joinToString(" ")
                    currentWordIndex++

                    // Dynamic delay based on Azure TTS progress
                    // Faster at start, slower near end for better sync
                    val progressRatio = currentWordIndex.toFloat() / words.size
                    val dynamicDelay = when {
                        progressRatio < 0.3f -> 200L // Fast start
                        progressRatio < 0.7f -> 350L // Medium
                        else -> 500L // Slower end
                    }

                    delay(dynamicDelay)
                }

                isTyping = false
            }
        } else {
            launch {
                isTyping = false
                typewriterText = ""
                currentCharIndex = 0
                displayText = text
            }
        }
    }

    // Audio processing
    LaunchedEffect(hasAudioPermission) {
        if (hasAudioPermission && !isListening) {
            isListening = true
            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize)
            audioRecord.startRecording()

            coroutineScope.launch(Dispatchers.IO) {
                val buffer = ShortArray(bufferSize)
                var silenceCounter = 0
                lastTextChangeTime = System.currentTimeMillis()

                while (isListening) {
                    val readResult = audioRecord.read(buffer, 0, bufferSize)
                    if (readResult > 0) {
                        var sum = 0.0
                        for (sample in buffer) {
                            sum += abs(sample.toDouble())
                        }
                        val average = sum / bufferSize
                        val normalized = (average / Short.MAX_VALUE).toFloat().coerceIn(0f, 1f)
                        soundLevel = normalized
                        soundPattern = (soundPattern + normalized).takeLast(patternSize)

                        if (normalized < quietThreshold) {
                            consecutiveQuietFrames++
                            consecutiveLoudFrames = 0
                        } else if (normalized > loudThreshold) {
                            consecutiveLoudFrames++
                            consecutiveQuietFrames = 0
                        }

                        // Animation updates
                        launch {
                            rotationSpeed.animateTo(
                                targetValue = (baseRotationSpeed * (1f - soundLevel * 0.7f)).toFloat(),
                                animationSpec = spring(stiffness = 100f)
                            )
                            dynamicPulse.animateTo(
                                targetValue = 1f + (soundLevel * 0.7f),
                                animationSpec = spring(stiffness = 300f)
                            )
                            dynamicGlow.animateTo(
                                targetValue = 1f + (soundLevel * 1.2f),
                                animationSpec = spring(stiffness = 300f)
                            )
                            waveIntensity.animateTo(
                                targetValue = 1f + (soundLevel * 3f),
                                animationSpec = spring(stiffness = 300f)
                            )
                            starMovementPhase.animateTo(
                                targetValue = starMovementPhase.value + (soundLevel * 10f),
                                animationSpec = spring(stiffness = 50f)
                            )
                        }

                        // Text display logic
                        val now = System.currentTimeMillis()
                        val timeSinceLastChange = now - lastTextChangeTime
                        if (normalized > 0.05f) {
                            silenceCounter = 0
                            if ((displayText == text || timeSinceLastChange > 1500) && Math.random() < 0.25) {
                                val newEmotion = when {
                                    normalized > 0.25f && soundPattern.maxOrNull()!! > 0.3f -> "excited"
                                    soundPattern.size >= 3 &&
                                            soundPattern.takeLast(3).zipWithNext().all { it.second > it.first } -> "curious"
                                    soundPattern.size >= 3 &&
                                            soundPattern.takeLast(3).all { it > 0.08f && it < 0.2f } -> "agreeing"
                                    soundPattern.size >= 2 &&
                                            soundPattern.last() > 0.2f &&
                                            soundPattern[soundPattern.size - 2] < 0.1f -> "surprised"
                                    soundPattern.size >= 3 &&
                                            (soundPattern.takeLast(3).maxOrNull()!! - soundPattern.takeLast(3).minOrNull()!!) > 0.15f -> "concerned"
                                    consecutiveLoudFrames > 5 -> "thinking"
                                    else -> "listening"
                                }
                                currentEmotion = when (newEmotion) {
                                    "listening" -> "neutral"
                                    "thinking"  -> "thinking"
                                    "curious"   -> "curious"
                                    "agreeing"  -> "agreeing"
                                    "excited"   -> "excited"
                                    "surprised" -> "surprised"
                                    "concerned" -> "concerned"
                                    else        -> "neutral"
                                }
                                val responses = responsesByEmotion[newEmotion] ?: responsesByEmotion["listening"]!!
                                displayText = responses.random()
                                lastTextChangeTime = now
                            }
                        } else {
                            silenceCounter++
                            if (silenceCounter > 25 && displayText != text) {
                                displayText = text
                                currentEmotion = "neutral"
                            }
                        }
                    }
                    delay(50)
                }
                audioRecord.stop()
                audioRecord.release()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { isListening = false }
    }

    val finalRingColor = if (isAISpeaking) {
        Color(0xFF00E5FF)
    } else {
        currentRingColor
    }

    Box(
        modifier = Modifier
            .fillMaxSize(0.9f)
            .wrapContentSize(Alignment.Center),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = size.minDimension * ringRadiusRatio * pulseSize * dynamicPulse.value

            // Draw animated stars (no glow)
            stars.forEach { star ->
                val movementAngle = starMovementPhase.value * star.moveSpeed
                val moveX = cos(movementAngle) * soundLevel * 30
                val moveY = sin(movementAngle) * soundLevel * 30

                val starX = (star.initialX * size.width) + moveX
                val starY = (star.initialY * size.height) + moveY

                val twinkle = sin(starTwinklePhase * star.twinkleSpeed + star.twinkleOffset).toFloat()
                val starAlpha = (star.baseAlpha + twinkle * 0.2f).coerceIn(0f, 1f)
                val starSize = 2f + (soundLevel * 3f)

                drawCircle(
                    color = finalRingColor.copy(alpha = starAlpha * 0.4f),
                    radius = starSize,
                    center = Offset(starX, starY)
                )
            }

            // Proper glowing ring layers from SoundReactiveGlowingRing
            // Outer glow layer
            drawCircle(
                color = finalRingColor.copy(alpha = 0.2f * dynamicGlow.value),
                radius = baseRadius + 30 * dynamicGlow.value,
                center = center,
                style = Stroke(width = 40f, cap = StrokeCap.Round)
            )

            // Middle glow layer
            drawCircle(
                color = finalRingColor.copy(alpha = 0.4f * dynamicGlow.value),
                radius = baseRadius + 15 * dynamicGlow.value,
                center = center,
                style = Stroke(width = 25f, cap = StrokeCap.Round)
            )

            // Main ring with rotation and wave distortion
            rotate(rotation) {
                val segments = 180
                val angleStep = (2 * PI / segments).toFloat()
                val path = Path().apply {
                    var firstPoint = true
                    for (i in 0 until segments) {
                        val angle = i * angleStep
                        val distortionAmplitude = 8 * waveIntensity.value * sin(angle * 3 + wavePhase)
                        val distortedRadius = baseRadius + distortionAmplitude
                        val x = center.x + cos(angle) * distortedRadius
                        val y = center.y + sin(angle) * distortedRadius
                        if (firstPoint) {
                            moveTo(x, y)
                            firstPoint = false
                        } else {
                            lineTo(x, y)
                        }
                    }
                    close()
                }
                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            finalRingColor.copy(alpha = 0.8f * dynamicGlow.value),
                            finalRingColor.copy(alpha = 1f * dynamicGlow.value),
                            finalRingColor.copy(alpha = 0.8f * dynamicGlow.value)
                        ),
                        start = Offset(center.x - baseRadius, center.y - baseRadius),
                        end = Offset(center.x + baseRadius, center.y + baseRadius)
                    ),
                    style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }

        // Text display with smaller font and continuous flow
        val textScale = remember { Animatable(1f) }
        LaunchedEffect(displayText, typewriterText) {
            textScale.animateTo(1.1f, animationSpec = tween(100))
            textScale.animateTo(1f, animationSpec = tween(100))
        }

        val finalTextToShow = when {
            isAISpeaking && typewriterText.isNotEmpty() -> typewriterText
            isAISpeaking && typewriterText.isEmpty() -> "Processing..."
            else -> displayText
        }

        Text(
            text = finalTextToShow,
            color = Color.White,
            fontSize = 16.sp, // Much smaller font size
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .scale(textScale.value)
                .padding(horizontal = 60.dp) // More padding to keep text well inside ring
                .widthIn(max = 200.dp), // Maximum width constraint
            maxLines = 2, // Only 2 lines to stay within ring
            lineHeight = 18.sp, // Tighter line height
            overflow = TextOverflow.Clip // Clean cut, no ellipsis
        )
    }
}





// 1. Fixed AIProfileScreen - Shows only 3 characters + See All (like SubjectList)
@Composable
fun AIProfileScreen(
    navController: NavController,
) {
    val context = LocalContext.current

    // Get UserInfoViewModel to fetch current user ID
    val userInfoViewModel: UserInfoViewModel = viewModel()
    val currentUserId by userInfoViewModel.userId.collectAsState()

    // Load userId when composable starts
    LaunchedEffect(Unit) {
        userInfoViewModel.loadUserId(context)
    }

    // Determine actual user ID - NO DEFAULT FALLBACK
    val actualUserId = remember(currentUserId) {
        when {
            currentUserId != NeuroEdApp.INVALID_USER_ID -> currentUserId
            else -> {
                val sharedPrefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                val storedUserId = sharedPrefs.getInt("userInfoId", NeuroEdApp.INVALID_USER_ID)
                if (storedUserId != NeuroEdApp.INVALID_USER_ID) {
                    storedUserId
                } else {
                    null // NO DEFAULT
                }
            }
        }
    }

    // If no valid user ID, show empty state instead of login requirement
    val finalUserId = actualUserId ?: NeuroEdApp.INVALID_USER_ID

    // Create repository and ViewModel with fallback user ID
    val repository = remember(finalUserId) {
        UserCharacterGet(RetrofitClient.apiService)
    }

    val viewModel: UserCharacterListViewModel = viewModel(
        factory = CharacterGetViewModelFactory(repository, finalUserId)
    )

    val characters by viewModel.userCharacterList.observeAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.observeAsState(initial = true)
    val errorMessage by viewModel.errorMessage.observeAsState()

    // Loading state - Show only when actually loading
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Loading your characters...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    // Error state - Show error without user ID info
    else if (errorMessage != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Unable to load characters",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Button(
                    onClick = { viewModel.loadUserCharacters() }
                ) {
                    Text("Try Again")
                }
            }
        }
    }
    // Empty state
    else if (characters.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "No characters yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Create your first AI character to get started",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = { navController.navigate("CharacterCreateScreen") }
                ) {
                    Text("Create Character")
                }
            }
        }
    }
    // Characters display - Clean without user ID text
    else {
        Column {
            // Characters list without user info header
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Show first 3 characters for preview
                val previewCharacters = characters.take(3)
                items(previewCharacters) { character ->
                    NullSafeAIProfileCard(
                        character = character,
                        currentUserId = actualUserId.toString()
                    ) {
                        navController.navigate("ChatScreen/${character.id}/$actualUserId")
                    }
                }

                // Show "See All" if more than 3 characters
                if (characters.size > 3) {
                    item {
                        SeeAllCharactersCard(
                            onClick = {
                                navController.navigate("FullCharacterListScreen/$actualUserId")
                            }
                        )
                    }
                }
            }
        }
    }
}

// 2. See All Characters Card - Same as SubjectList's SeeAllCard
@Composable
fun SeeAllCharactersCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .height(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "See All",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Text
            Text(
                text = "See All",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Characters",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // View All Button
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Text(
                    text = "View All",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

// 3. A null-safe wrapper for AIProfileCard
@Composable
fun NullSafeAIProfileCard(
    character: CharacterGetData,
    currentUserId: String? = null, // Add currentUserId parameter
    onClick: () -> Unit
) {
    val safeCharacter = CharacterGetData(
        id = character.id,
        Character_name = character.Character_name ?: "Unknown",
        Description = character.Description ?: "No description available",
        ChatCount = character.ChatCount,
        StarCount = character.StarCount
    )

    AIProfileCard(
        character = safeCharacter,
        currentUserId = currentUserId, // Pass currentUserId
        onClick = onClick
    )
}


// 4. Original AIProfileCard that expects non-null values
@Composable
fun AIProfileCard(
    character: CharacterGetData,
    onClick: () -> Unit,
    currentUserId: String? = null
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .height(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Character Avatar",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Character Name
            Text(
                text = character.Character_name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description with weight to take available space
            Text(
                text = character.Description,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .weight(1f) // This makes it take available space
                    .padding(horizontal = 4.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chat Count
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.MailOutline,
                        contentDescription = "Chats",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${character.ChatCount}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Star Count
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Stars",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFFD700)
                    )
                    Text(
                        text = "${character.StarCount}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chat Now Button - Always at bottom
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Chat Now",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------------------
// Location Screen (optional)
// ----------------------------------------------------------------------------------------
@Composable
fun LocationScreen() {
    val context = LocalContext.current
    val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
    var locationText by remember { mutableStateOf("लोकेशन प्राप्त हो रही है...") }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                locationText = if (location != null) {
                    "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
                } else {
                    "लोकेशन नहीं मिली"
                }
            }
        } else {
            locationText = "परमिशन नहीं मिला"
        }
    }

    Text(text = locationText)
}

// ----------------------------------------------------------------------------------------
// Example for fetching images (optional utility)
// ----------------------------------------------------------------------------------------
fun getAllImages(context: Context): List<android.net.Uri> {
    val imageUris = mutableListOf<android.net.Uri>()
    val projection = arrayOf(MediaStore.Images.Media._ID)
    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
    val query = context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        sortOrder
    )
    query?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )
            imageUris.add(contentUri)
        }
    }
    return imageUris
}

// ----------------------------------------------------------------------------------------
// Animation Composable (Enhanced with gradient + scale + fade + slide)
// ----------------------------------------------------------------------------------------
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Animation(navController: NavController) {
    val learningCards = listOf(
        "🌟 Believe in yourself and your abilities.",
        "📚 Read for at least 10 minutes today.",
        "🎯 Set one clear goal for today.",
        "🧠 Brain exercise: Try to recall yesterday’s learnings.",
        "💡 Pomodoro tip: 25 minutes focus, 5 minutes break.",
        "🏆 Congratulations! You completed 3 learning steps!",
        "👥 Invite a friend to learn with you and stay motivated.",
        "🧘‍♂️ Relax your mind: Take 3 deep breaths before studying."
    )

    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            currentIndex = (currentIndex + 1) % learningCards.size
        }
    }

    // Slide + fade + scale animation
    AnimatedContent(
        targetState = currentIndex,
        transitionSpec = {
            (slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 600)
            ) + fadeIn(animationSpec = tween(600)) + scaleIn(initialScale = 0.95f)) with
                    (slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(durationMillis = 600)
                    ) + fadeOut(animationSpec = tween(600)) + scaleOut(targetScale = 0.95f))
        },
        label = "LearningStepsAnimation"
    ) { targetIndex ->

        // Fancy Gradient Brush
        val gradientBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF8E2DE2), // purple
                Color(0xFF4A00E0), // blue
                Color(0xFF00C9FF), // light blue
                Color(0xFF92FE9D)  // green
            ),
            start = Offset(0f, 0f),
            end = Offset(1000f, 1000f)
        )

        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(150.dp)
                .shadow(12.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.15f) // Glass effect
            )
        ) {
            Box(
                modifier = Modifier
                    .background(gradientBrush)
                    .padding(20.dp)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = learningCards[targetIndex],
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(2f, 2f),
                            blurRadius = 6f
                        )
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}




// ----------------------------------------------------------------------------------------
// Preview
// ----------------------------------------------------------------------------------------
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        val navController = rememberNavController()
        HomeScreen(navController = navController)
    }
}
