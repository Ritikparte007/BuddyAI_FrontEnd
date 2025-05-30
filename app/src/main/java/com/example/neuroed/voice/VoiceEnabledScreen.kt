// VoiceEnabledScreen.kt - FIXED VERSION
package com.example.neuroed.voice

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

@Composable
fun VoiceEnabledScreen(
    screenName: String,
    onVoiceResult: (String) -> Unit = {},
    onVoiceError: (String) -> Unit = {},
    onNetworkError: () -> Unit = {},
    onPermissionRequired: () -> Unit = {},
    silenceTimeoutMs: Long = 15000L,
    maxSessionDuration: Long = 300000L,
    autoRetryEnabled: Boolean = true,
    content: @Composable (
        isListening: Boolean,
        recognizedText: String,
        networkAvailable: Boolean,
        startVoice: () -> Unit,
        stopVoice: () -> Unit,
        restartSession: () -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // Global voice manager states
    val isListening by GlobalVoiceManager.isListening.collectAsState()
    val currentText by GlobalVoiceManager.currentText.collectAsState()
    val isInitialized by GlobalVoiceManager.isInitialized.collectAsState()
    val errorMessage by GlobalVoiceManager.errorMessage.collectAsState()
    val networkStatus by GlobalVoiceManager.networkStatus.collectAsState()

    // Local state
    var lastRecognizedText by remember { mutableStateOf("") }
    var sessionStartTime by remember { mutableLongStateOf(0L) }
    var isScreenActive by remember { mutableStateOf(true) }
    var retryCount by remember { mutableIntStateOf(0) }

    // Atomic flags
    val sessionTimeoutJob = remember { AtomicReference<Job?>(null) }
    val isProcessingError = remember { AtomicBoolean(false) }
    val lastErrorTime = remember { AtomicLong(0L) }

    // ✅ SIMPLE RETRY FUNCTION - NO COMPLEX TYPES
    fun performRetry() {
        scope.launch {
            if (!isProcessingError.compareAndSet(false, true)) {
                Log.w("VoiceEnabledScreen", "Already processing error")
                return@launch
            }

            try {
                if (!autoRetryEnabled || !isScreenActive || retryCount >= 3) {
                    retryCount = 0
                    return@launch
                }

                retryCount++
                val retryDelay = 1500L + (retryCount * 500L)
                Log.i("VoiceEnabledScreen", "🔄 Retry attempt $retryCount")
                delay(retryDelay)

                // ✅ REMOVED isHealthy() CHECK - JUST CHECK BASIC CONDITIONS
                if (isScreenActive && isInitialized && networkStatus) {
                    GlobalVoiceManager.clearCurrentText()

                    // Simple retry callback
                    val retryCallback = object : GlobalVoiceManager.VoiceCallback {
                        override fun onResult(text: String) {
                            if (!isScreenActive) return
                            lastRecognizedText = text
                            retryCount = 0
                            scope.launch { onVoiceResult(text) }
                        }

                        override fun onError(error: String) {
                            if (!isScreenActive) return
                            scope.launch {
                                onVoiceError(error)
                                if (retryCount < 3) performRetry()
                            }
                        }

                        override fun onStartListening() {
                            if (!isScreenActive) return
                            sessionStartTime = System.currentTimeMillis()
                        }

                        override fun onStopListening() {}
                        override fun onNetworkStatusChanged(isAvailable: Boolean) {
                            if (!isAvailable && isScreenActive) {
                                scope.launch { onNetworkError() }
                            }
                        }
                        override fun onPermissionRequired() {
                            if (!isScreenActive) return
                            scope.launch { onPermissionRequired() }
                        }
                    }

                    GlobalVoiceManager.startListening(context, retryCallback)
                } else {
                    retryCount = 0
                }
            } catch (e: Exception) {
                Log.e("VoiceEnabledScreen", "Error in retry", e)
                retryCount = 0
            } finally {
                isProcessingError.set(false)
            }
        }
    }

    // ✅ SIMPLE MAIN VOICE CALLBACK
    val voiceCallback = remember {
        object : GlobalVoiceManager.VoiceCallback {
            override fun onResult(text: String) {
                if (!isScreenActive) return
                lastRecognizedText = text
                retryCount = 0
                scope.launch { onVoiceResult(text) }
            }

            override fun onError(error: String) {
                if (!isScreenActive) return

                scope.launch {
                    onVoiceError(error)

                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastErrorTime.get() < 1000) return@launch
                    lastErrorTime.set(currentTime)

                    val shouldRetry = when {
                        error.contains("No speech", ignoreCase = true) && retryCount < 2 -> true
                        error.contains("timeout", ignoreCase = true) && retryCount < 1 -> true
                        error.contains("busy", ignoreCase = true) && retryCount < 3 -> true
                        error.contains("Network", ignoreCase = true) && networkStatus && retryCount < 2 -> true
                        else -> false
                    }

                    if (shouldRetry && autoRetryEnabled) {
                        performRetry()
                    } else {
                        retryCount = 0
                    }
                }
            }

            override fun onStartListening() {
                if (!isScreenActive) return
                sessionStartTime = System.currentTimeMillis()

                // Session timeout
                val timeoutJob = scope.launch {
                    try {
                        delay(maxSessionDuration)
                        if (isListening && isScreenActive) {
                            GlobalVoiceManager.stopListening()
                            onVoiceError("Session timeout")
                        }
                    } catch (e: CancellationException) {
                        // Timeout cancelled
                    }
                }
                sessionTimeoutJob.getAndSet(timeoutJob)?.cancel()
            }

            override fun onStopListening() {
                sessionTimeoutJob.getAndSet(null)?.cancel()
            }

            override fun onNetworkStatusChanged(isAvailable: Boolean) {
                if (!isAvailable && isScreenActive) {
                    scope.launch { onNetworkError() }
                }
            }

            override fun onPermissionRequired() {
                if (!isScreenActive) return
                scope.launch { onPermissionRequired() }
            }
        }
    }

    // ✅ FIXED VOICE CONTROLS - REMOVED isHealthy() CHECKS
    // VoiceEnabledScreen.kt mein startVoice function mein add kar
    val startVoice: () -> Unit = remember(voiceCallback) {
        {
            // ✅ REMOVE ALL LOGS - Performance issue

            if (!isScreenActive || !networkStatus || isListening) {
                return@remember
            }

            // ✅ NO COROUTINE - Direct call
            try {
                GlobalVoiceManager.stopListening()
                GlobalVoiceManager.clearCurrentText()

                // Reset local states
                lastRecognizedText = ""
                retryCount = 0

                // Start immediately
                GlobalVoiceManager.startListening(context, voiceCallback)

            } catch (e: Exception) {
                onVoiceError("Failed to start: ${e.message}")
            }
        }
    }

    val stopVoice: () -> Unit = remember {
        {
            try {
                GlobalVoiceManager.stopListening()
                sessionTimeoutJob.getAndSet(null)?.cancel()
                retryCount = 0
            } catch (e: Exception) {
                Log.e("VoiceEnabledScreen", "Error stopping voice", e)
            }
        }
    }

    val restartSession: () -> Unit = remember(startVoice, stopVoice) {
        {
            if (isScreenActive) {
                Log.d("VoiceEnabledScreen", "🔄 Restarting session...")
                stopVoice()
                scope.launch {
                    delay(750)
                    // ✅ SIMPLIFIED RESTART CONDITIONS
                    if (isScreenActive && networkStatus) {
                        if (!isInitialized) {
                            GlobalVoiceManager.initialize(context)
                            delay(500)
                        }
                        startVoice()
                    } else {
                        onVoiceError("Cannot restart - check network connection")
                    }
                }
            }
        }
    }

    // ✅ FIXED INITIALIZATION - ALWAYS TRY TO INITIALIZE
    LaunchedEffect(Unit) {
        try {
            Log.d("VoiceEnabledScreen", "🚀 Initializing voice manager...")
            GlobalVoiceManager.initialize(context)
            GlobalVoiceManager.updateSilenceTimeout(silenceTimeoutMs)
            delay(300) // Give more time for initialization
            Log.d("VoiceEnabledScreen", "✅ Voice manager initialization complete")
        } catch (e: Exception) {
            Log.e("VoiceEnabledScreen", "❌ Initialization failed", e)
            onVoiceError("Initialization failed: ${e.message}")
        }
    }

    // ✅ SIMPLE NETWORK MONITORING
    LaunchedEffect(networkStatus) {
        if (!networkStatus && isListening) {
            GlobalVoiceManager.stopListening()
            delay(100)
            onNetworkError()
        }
    }

    // ✅ SIMPLE LIFECYCLE
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    isScreenActive = false
                    if (isListening) GlobalVoiceManager.stopListening()
                    sessionTimeoutJob.getAndSet(null)?.cancel()
                }
                Lifecycle.Event.ON_RESUME -> {
                    isScreenActive = true
                    retryCount = 0
                    isProcessingError.set(false)
                }
                Lifecycle.Event.ON_DESTROY -> {
                    isScreenActive = false
                    if (isListening) GlobalVoiceManager.stopListening()
                    sessionTimeoutJob.getAndSet(null)?.cancel()
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            isScreenActive = false
            sessionTimeoutJob.getAndSet(null)?.cancel()
        }
    }

    // ✅ SIMPLE ERROR CLEARING
    LaunchedEffect(errorMessage) {
        errorMessage?.takeIf { it.isNotEmpty() }?.let {
            scope.launch {
                delay(2000)
                if (isScreenActive) {
                    try {
                        GlobalVoiceManager.clearError()
                    } catch (e: Exception) {
                        Log.w("VoiceEnabledScreen", "Error clearing error: ${e.message}")
                    }
                }
            }
        }
    }

    // ✅ SIMPLE TEXT DISPLAY
    val displayText = remember(currentText, lastRecognizedText) {
        when {
            currentText.isNotEmpty() -> currentText
            lastRecognizedText.isNotEmpty() -> lastRecognizedText
            else -> ""
        }
    }

    // ✅ SIMPLE CONTENT RENDERING
    content(
        isListening,
        displayText,
        networkStatus,
        startVoice,
        stopVoice,
        restartSession
    )
}