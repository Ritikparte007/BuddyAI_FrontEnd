package com.example.neuroed

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import java.time.LocalDate
import android.os.Looper
import java.time.format.TextStyle
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
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
import androidx.lifecycle.viewmodel.compose.viewModel


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
// ----------------------------------------------------------------------------------------
class VoiceMessageViewModel : androidx.lifecycle.ViewModel() {

    private val _recognizedText = MutableStateFlow("")
    val recognizedText = _recognizedText.asStateFlow()

    private val _receivedMessage = MutableStateFlow("")
    val receivedMessage = _receivedMessage.asStateFlow()

    private var webSocket: WebSocket? = null
    private val client: OkHttpClient = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    fun updateRecognizedText(text: String) {
        _recognizedText.value = text
    }

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
                    Log.i(VM_DEBUG_TAG, "WebSocket Opened: ${response.message}")
                }
                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.i(VM_DEBUG_TAG, "WebSocket Received Text: $text")
                    _receivedMessage.value = text
                }
                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    Log.i(VM_DEBUG_TAG, "WebSocket Received Bytes: ${bytes.hex()}")
                    _receivedMessage.value = bytes.utf8()
                }
                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.i(VM_DEBUG_TAG, "WebSocket Closing: Code=$code, Reason='$reason'")
                }
                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.i(VM_DEBUG_TAG, "WebSocket Closed: Code=$code, Reason='$reason'")
                    this@VoiceMessageViewModel.webSocket = null
                }
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e(VM_DEBUG_TAG, "WebSocket Failure: ${t.message}", t)
                    response?.let {
                        Log.e(VM_DEBUG_TAG, "Failure Response: Code=${it.code}, Message=${it.message}")
                    }
                    this@VoiceMessageViewModel.webSocket = null
                }
            })
        }
    }

    fun sendVoiceMessage(text: String) {
        if (text.isBlank()) {
            Log.w(VM_DEBUG_TAG, "Attempted to send blank message. Skipping.")
            return
        }
        val currentWebSocket = webSocket
        if (currentWebSocket == null) {
            Log.e(VM_DEBUG_TAG, "Cannot send message, WebSocket is null or closed. Attempting to reconnect...")
            connectWebSocket()
            return
        }
        val escapedText = text.replace("\\", "\\\\").replace("\"", "\\\"")
        val jsonPayload = "{\"message\": \"$escapedText\"}"
        Log.d(VM_DEBUG_TAG, "Sending JSON payload via WebSocket: $jsonPayload")
        val sent = currentWebSocket.send(jsonPayload)
        if (sent) {
            Log.d(VM_DEBUG_TAG, "JSON Message successfully queued for sending.")
        } else {
            Log.e(VM_DEBUG_TAG, "Failed to queue JSON message (WebSocket closing or buffer full?).")
            connectWebSocket()
        }
    }

    fun clearReceivedMessage() {
        if (_receivedMessage.value.isNotEmpty()) {
            Log.d(VM_DEBUG_TAG, "Clearing received message state.")
            _receivedMessage.value = ""
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(VM_DEBUG_TAG, "ViewModel cleared. Closing WebSocket.")
        webSocket?.close(1000, "ViewModel cleared")
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
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
        }
        recognizer.setRecognitionListener(listener)
        recognizer.startListening(intent)
        Log.d(MIC_DEBUG_TAG, "SpeechRecognizer.startListening() called.")
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
private suspend fun synthesizeSpeech(synthesizer: SpeechSynthesizer, text: String) =
    withContext(Dispatchers.IO) {
        if (text.isBlank()) {
            Log.d(TTS_DEBUG_TAG, "Skipping synthesis for blank/empty text.")
            return@withContext
        }
        try {
            Log.d(TTS_DEBUG_TAG, "Attempting to synthesize: '$text'")
            val result = synthesizer.SpeakTextAsync(text).get()
            when (result.reason) {
                ResultReason.SynthesizingAudioCompleted -> {
                    Log.d(
                        TTS_DEBUG_TAG,
                        "Speech synthesis completed successfully for '$text'. Audio duration: ${result.audioDuration}"
                    )
                }
                ResultReason.Canceled -> {
                    val cancellation = SpeechSynthesisCancellationDetails.fromResult(result)
                    Log.e(TTS_DEBUG_TAG, "Speech synthesis CANCELED for '$text'")
                    Log.e(TTS_DEBUG_TAG, ">> Reason: ${cancellation.reason}")
                    if (cancellation.reason == com.microsoft.cognitiveservices.speech.CancellationReason.Error) {
                        Log.e(TTS_DEBUG_TAG, ">> ErrorCode: ${cancellation.errorCode}")
                        Log.e(TTS_DEBUG_TAG, ">> ErrorDetails: ${cancellation.errorDetails}")
                    }
                }
                else -> {
                    Log.w(TTS_DEBUG_TAG, "Speech synthesis result for '$text': ${result.reason}")
                }
            }
            result.close()
        } catch (e: Exception) {
            Log.e(TTS_DEBUG_TAG, "Speech synthesis FAILED for '$text': ${e.message}", e)
        }
    }

// ----------------------------------------------------------------------------------------
// HomeScreen
// ----------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    voiceMessageViewModel: VoiceMessageViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val systemUiController = rememberSystemUiController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    SideEffect {
        systemUiController.setSystemBarsColor(color = Color.Transparent, darkIcons = true)
    }

    // Connect WebSocket when screen enters composition
    LaunchedEffect(Unit) {
        Log.d("HomeScreen", "Requesting ViewModel to connect to Assistant WebSocket.")
        voiceMessageViewModel.connectWebSocket()
    }

    // Initialize Azure TTS objects
    val speechSynthesizer = remember { mutableStateOf<SpeechSynthesizer?>(null) }
    val speechConfig = remember { mutableStateOf<SpeechConfig?>(null) }
    val audioConfig = remember { mutableStateOf<AudioConfig?>(null) }



    val userId = 1
    val api    = RetrofitClient.apiService
    val repo   = remember { AttendanceRepository(api) }
    val vm: AttendanceViewModel = viewModel(
        factory = AttendanceViewModelFactory(repo)
    )

    val attendance by vm.attendance.collectAsState()
    val loading    by vm.isLoading.collectAsState()
    val error      by vm.errorMessage.collectAsState()

    // when screen appears, mark attendance with client time
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

    // Whenever we get a message from the server, try to TTS
    val assistantReply = voiceMessageViewModel.receivedMessage.collectAsState().value
    LaunchedEffect(assistantReply) {
        val synthesizer = speechSynthesizer.value
        if (assistantReply.isNotEmpty() && synthesizer != null) {
            try {
                val jsonObject = org.json.JSONObject(assistantReply)
                val replyText = jsonObject.optString("message", assistantReply)
                Log.d(TTS_DEBUG_TAG, "Assistant reply received, attempting synthesis for: '$replyText'")
                synthesizeSpeech(synthesizer, replyText)
            } catch (e: org.json.JSONException) {
                Log.w(TTS_DEBUG_TAG, "Received message is not valid JSON ('$assistantReply'). Synthesizing raw text.")
                synthesizeSpeech(synthesizer, assistantReply)
            } finally {
                voiceMessageViewModel.clearReceivedMessage()
            }
        } else if (assistantReply.isNotEmpty() && synthesizer == null) {
            Log.w(TTS_DEBUG_TAG, "Received message but synthesizer is not ready.")
            voiceMessageViewModel.clearReceivedMessage()
        }
    }

    // Clean up TTS resources on dispose
    DisposableEffect(Unit) {
        onDispose {
            Log.d(TTS_DEBUG_TAG, "HomeScreen disposing. Cleaning up TTS resources.")
            scope.launch(Dispatchers.IO) {
                try {
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
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.75f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Menu", style = MaterialTheme.typography.titleMedium)
                }
                Divider()
                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
                )
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        navController.navigate("settings")
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") }
                )
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        Log.d("HomeScreen", "Logout Action - Implement")
                    },
                    icon = { Icon(Icons.Filled.ExitToApp, contentDescription = "Logout") }
                )
            }
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
                BottomNavigationBar(
                    navController = navController,
                    voiceMessageViewModel = voiceMessageViewModel
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
                    // Large top area with the emotions diagram
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    ) {
                        EmotionsDiagramAnimatedUI()
                    }
                }

                item { SectionHeader(title = "SyllabusHub") }
                item { SubjectList(navController) }


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

                // Enhanced user profile card (matching AIProfile color)
                item { EnhancedUserProfile(navController) }

                item { GridScreen(navController) }

                // Updated AgentList (same color as AIProfile)
                item { EnhancedAgentList(navController) }

                // Reduced space before the bottom Animation (16.dp now)
                item { Spacer(modifier = Modifier.height(16.dp)) }

                // … in your LazyColumn:
                item {


                    LibrarySection(navController) }


                // The Enhanced Animation at the bottom
                item { Animation(navController) }


            }
        }
    }
}

// ----------------------------------------------------------------------------------------
// Bottom NavigationBar (unchanged except for minor label updates)
// ----------------------------------------------------------------------------------------
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BottomNavigationBar(
    navController: NavController,
    voiceMessageViewModel: VoiceMessageViewModel
) {
    val context = LocalContext.current

    var isMicOn by remember { mutableStateOf(false) }
    var selectedRoute by remember { mutableStateOf("CreateSubjectScreen") }
    var recognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var isReadyForSpeech by remember { mutableStateOf(false) }

    val recognizedText by voiceMessageViewModel.recognizedText.collectAsState()
    val showSendButton = recognizedText.isNotEmpty()

    val recordAudioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val restartHandler = remember { Handler(Looper.getMainLooper()) }
    var restartRunnable by remember { mutableStateOf<Runnable?>(null) }

    fun cancelRestart() {
        restartRunnable?.let { restartHandler.removeCallbacks(it) }
        restartRunnable = null
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(MIC_DEBUG_TAG, "BottomNavigationBar disposing. Cancelling restart and destroying recognizer.")
            cancelRestart()
            recognizer?.stopListening()
            recognizer?.destroy()
        }
    }

    val regularListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(MIC_DEBUG_TAG, "onReadyForSpeech")
                isReadyForSpeech = true
            }
            override fun onBeginningOfSpeech() {
                Log.d(MIC_DEBUG_TAG, "onBeginningOfSpeech")
            }
            override fun onRmsChanged(rmsdB: Float) {
                Log.d(MIC_DEBUG_TAG, "Audio level (RMS): $rmsdB")
            }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                Log.d(MIC_DEBUG_TAG, "onEndOfSpeech")
                isReadyForSpeech = false
                if (isMicOn && recognizer != null) {
                    restartListening(
                        context = context,
                        recognizer = recognizer!!,
                        listener = this,
                        handler = restartHandler,
                        onRestart = { runnable -> restartRunnable = runnable },
                        isMicOn = { isMicOn }
                    )
                }
            }
            override fun onError(error: Int) {
                val errorText = getSpeechRecognizerErrorText(error)
                Log.e(MIC_DEBUG_TAG, "onError: $errorText ($error)")
                isReadyForSpeech = false
                if (!isMicOn && error == SpeechRecognizer.ERROR_CLIENT) {
                    Log.d(MIC_DEBUG_TAG, "Ignoring client error because mic is off.")
                    return
                }
                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        if (isMicOn && recognizer != null) {
                            restartListening(
                                context = context,
                                recognizer = recognizer!!,
                                listener = this,
                                handler = restartHandler,
                                onRestart = { runnable -> restartRunnable = runnable },
                                isMicOn = { isMicOn }
                            )
                        } else {
                            isMicOn = false
                        }
                    }
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS,
                    SpeechRecognizer.ERROR_CLIENT,
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
                    SpeechRecognizer.ERROR_NETWORK,
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
                    SpeechRecognizer.ERROR_SERVER -> {
                        isMicOn = false
                        voiceMessageViewModel.updateRecognizedText("")
                        Toast.makeText(context, "Voice input error: $errorText", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        isMicOn = false
                        voiceMessageViewModel.updateRecognizedText("")
                    }
                }
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val bestResult = matches?.firstOrNull()?.trim() ?: ""
                if (bestResult.isNotEmpty()) {
                    voiceMessageViewModel.updateRecognizedText(bestResult)
                }
                if (isMicOn && recognizer != null) {
                    restartListening(
                        context = context,
                        recognizer = recognizer!!,
                        listener = this,
                        handler = restartHandler,
                        onRestart = { runnable -> restartRunnable = runnable },
                        isMicOn = { isMicOn }
                    )
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partialResult = matches?.firstOrNull()?.trim() ?: ""
                if (partialResult.isNotEmpty()) {
                    voiceMessageViewModel.updateRecognizedText(partialResult)
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    // Effect to handle mic on/off
    LaunchedEffect(isMicOn, recordAudioPermission.status, recognizer) {
        if (recognizer == null) {
            Log.w(MIC_DEBUG_TAG, "Recognizer is null. Cannot start listening.")
            if (isMicOn) isMicOn = false
            return@LaunchedEffect
        }
        if (isMicOn) {
            if (recordAudioPermission.status.isGranted) {
                startRegularListening(context, recognizer!!, regularListener)
            } else {
                if (recordAudioPermission.status is PermissionStatus.Denied &&
                    !(recordAudioPermission.status as PermissionStatus.Denied).shouldShowRationale
                ) {
                    Toast.makeText(context, "Microphone permission is required.", Toast.LENGTH_LONG).show()
                    isMicOn = false
                    voiceMessageViewModel.updateRecognizedText("")
                }
            }
        } else {
            if (isReadyForSpeech) {
                recognizer?.stopListening()
            }
            cancelRestart()
        }
    }

    // Dispose effect to destroy recognizer
    DisposableEffect(Unit) {
        onDispose {
            recognizer?.destroy()
        }
    }

    // Bottom bar container
    Surface(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // If user has partially recognized text, show it above the bar
            AnimatedVisibility(visible = recognizedText.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = recognizedText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 3
                    )
                    if (showSendButton) {
                        IconButton(
                            onClick = {
                                voiceMessageViewModel.sendVoiceMessage(recognizedText)
                                voiceMessageViewModel.updateRecognizedText("")
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.menu),
                                contentDescription = "Send Message",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Actual navigation bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    CustomNavigationBarItem(
                        label = "Subject",
                        iconResId = R.drawable.curriculum,
                        isSelected = selectedRoute == "CreateSubjectScreen",
                        onClick = {
                            selectedRoute = "CreateSubjectScreen"
                            navController.navigate("CreateSubjectScreen") { launchSingleTop = true }
                        }
                    )
                    CustomNavigationBarItem(
                        label = "Feed",
                        iconResId = R.drawable.play,
                        isSelected = selectedRoute == "ReelsScreen",
                        onClick = {
                            selectedRoute = "ReelsScreen"
                            navController.navigate("ReelsScreen") { launchSingleTop = true }
                        }
                    )
                    // Mic button
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            if (!isMicOn && !recordAudioPermission.status.isGranted) {
                                recordAudioPermission.launchPermissionRequest()
                            } else {
                                if (isMicOn) {
                                    if (isReadyForSpeech) {
                                        recognizer?.stopListening()
                                    }
                                    cancelRestart()
                                    isMicOn = false
                                } else {
                                    recognizer?.destroy()
                                    recognizer = SpeechRecognizer.createSpeechRecognizer(context)
                                    isMicOn = true
                                }
                            }
                        },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isMicOn)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else
                                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id = if (isMicOn)
                                            R.drawable.baseline_mic_24
                                        else
                                            R.drawable.baseline_mic_off_24
                                    ),
                                    contentDescription = if (isMicOn) "Microphone On" else "Microphone Off",
                                    modifier = Modifier.size(26.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                if (isMicOn) {
                                    VoiceVisualizer(modifier = Modifier.matchParentSize())
                                }
                            }
                        },
                        label = {}
                    )
                    CustomNavigationBarItem(
                        label = "Matric",
                        iconResId = R.drawable.graph,
                        isSelected = selectedRoute == "library",
                        onClick = {
                            selectedRoute = "library"
                            navController.navigate("library") { launchSingleTop = true }
                        }
                    )
                    CustomNavigationBarItem(
                        label = "Character",
                        iconResId = R.drawable.bot,
                        isSelected = selectedRoute == "settings",
                        onClick = {
                            selectedRoute = "settings"
                            navController.navigate("CharacterCreateScreen") { launchSingleTop = true }
                        }
                    )
                }
            }
        }
    }
}

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
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
    subjectlistViewModel: SubjectlistViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = SubjectlistViewModelFactory(
            SubjectlistRepository(RetrofitClient.apiService)
        )
    )
) {
    val subjects by subjectlistViewModel.subjectList.collectAsState()
    LaunchedEffect(Unit) {
        subjectlistViewModel.fetchSubjectList()
    }
    if (subjects.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(subjects) { subject ->
                Card(
                    modifier = Modifier
                        .clickable {
                            navController.navigate("SyllabusScreen/${subject.id}/${subject.subjectDescription}/${subject.subject}")
                        }
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
        }
    }
}

// ----------------------------------------------------------------------------------------
// Enhanced UserProfile (now matches AIProfileCard color)
// ----------------------------------------------------------------------------------------
@Composable
fun EnhancedUserProfile(
    navController: NavController,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { navController.navigate("ProfileScreen") },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        // Use the same color as AIProfileCard => MaterialTheme.colorScheme.surface
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.biology),
                contentDescription = "User Profile",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "John Doe",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = 0.7f,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Level 10",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star",
                            tint = if (index < 4) Color(0xFFFFD700) else Color.Gray,
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
    barCount: Int = 5,
    barColor: Color = Color.White,
    minHeight: Dp = 4.dp,
    maxHeight: Dp = 16.dp,
    barWidth: Dp = 4.dp,
    spaceBetween: Dp = 2.dp
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animations = List(barCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 500,
                    easing = LinearEasing,
                    delayMillis = index * 100
                ),
                repeatMode = RepeatMode.Reverse
            )
        )
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(spaceBetween)
    ) {
        animations.forEach { anim ->
            val height = minHeight + (maxHeight - minHeight) * anim.value
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(height)
                    .background(barColor, shape = RoundedCornerShape(2.dp))
            )
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
        GridItemModel("Store", R.drawable.folders, "StoreScreen"),
        GridItemModel("Mindmap", R.drawable.mindmap, "DocumentsScreen"),
        GridItemModel("Assignents", R.drawable.contract, "AssignmentScreen"),
        GridItemModel("Playground", R.drawable.readingbook, "PlaygroundScreen"),
        GridItemModel("Meditation", R.drawable.meditation, "MeditationScreen"),
        GridItemModel("ClassRoom", R.drawable.training, "Meditation"),
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

@Composable
fun EmotionsDiagramAnimatedUI(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SoundReactiveGlowingRing(
                text = "BuddyAI",
                ringColor = Color(0xFF00E5FF),
                ringRadiusRatio = 0.38f
            )
        }
    }
}

@Composable
fun SoundReactiveGlowingRing(
    text: String,
    ringColor: Color,
    ringRadiusRatio: Float = 0.38f
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var soundLevel by remember { mutableStateOf(0f) }
    var displayText by remember { mutableStateOf(text) }
    var isListening by remember { mutableStateOf(false) }
    var currentEmotion by remember { mutableStateOf("neutral") }

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

    var pressing by remember { mutableStateOf(false) }
    var pressStartTime by remember { mutableStateOf<Long?>(null) }
    val painFactor by derivedStateOf {
        if (pressing && pressStartTime != null) {
            val pressDuration = System.currentTimeMillis() - pressStartTime!!
            (pressDuration / 3000f).coerceIn(0f, 1f)
        } else 0f
    }
    val painMessages = listOf(
        "Ouch...",
        "This really hurts...",
        "Aghh! Please stop!",
        "ARGHH!!!"
    )
    val currentPainMessage by derivedStateOf {
        when {
            painFactor < 0.25f -> painMessages[0]
            painFactor < 0.5f  -> painMessages[1]
            painFactor < 0.75f -> painMessages[2]
            else               -> painMessages[3]
        }
    }
    val painColor = Color.Red
    val finalRingColor = lerp(start = currentRingColor, stop = painColor, fraction = painFactor)
    val ringScaleX by derivedStateOf { 1f - 0.3f * painFactor }
    val ringScaleY by derivedStateOf { 1f - 0.4f * painFactor }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val baseRotationSpeed = 15000
    val rotationSpeed = remember { Animatable(baseRotationSpeed.toFloat()) }
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = rotationSpeed.value.toInt(), easing = LinearEasing))
    )
    val basePulseMin = 0.95f
    val basePulseMax = 1.05f
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = basePulseMin,
        targetValue = basePulseMax,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = EaseInOutQuad), repeatMode = RepeatMode.Reverse)
    )
    val dynamicPulse = remember { Animatable(1f) }
    val baseGlowMin = 0.7f
    val baseGlowMax = 1f
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = baseGlowMin,
        targetValue = baseGlowMax,
        animationSpec = infiniteRepeatable(animation = tween(3000, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse)
    )
    val dynamicGlow = remember { Animatable(1f) }
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(animation = tween(4000, easing = LinearEasing))
    )
    val waveIntensity = remember { Animatable(1f) }
    val polygonRotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(20000, easing = LinearEasing), repeatMode = RepeatMode.Restart)
    )
    val polygonRotation2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(25000, easing = LinearEasing), repeatMode = RepeatMode.Restart)
    )

    data class Star(val x: Float, val y: Float, val baseAlpha: Float, val twinkleOffset: Float, val twinkleSpeed: Float)
    val starCount = 60
    val randomSeed = remember { Random(System.currentTimeMillis()) }
    val stars = remember {
        List(starCount) {
            Star(
                x = randomSeed.nextFloat(),
                y = randomSeed.nextFloat(),
                baseAlpha = 0.2f + randomSeed.nextFloat() * 0.6f,
                twinkleOffset = randomSeed.nextFloat() * 2 * PI.toFloat(),
                twinkleSpeed = 0.5f + randomSeed.nextFloat() * 1.5f
            )
        }
    }
    val starTwinklePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(animation = tween(4000, easing = LinearEasing), repeatMode = RepeatMode.Restart)
    )

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
                        }
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

    var hasSpokenPain by remember { mutableStateOf(false) }
    LaunchedEffect(pressing, painFactor, currentPainMessage) {
        if (pressing && painFactor >= 0.5f && !hasSpokenPain) {
            tts.speak(currentPainMessage, TextToSpeech.QUEUE_FLUSH, null, null)
            hasSpokenPain = true
        }
        if (!pressing) {
            hasSpokenPain = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(0.9f)
            .wrapContentSize(Alignment.Center)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressing = true
                        pressStartTime = System.currentTimeMillis()
                        try {
                            awaitRelease()
                        } finally {
                            pressing = false
                            pressStartTime = null
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = size.minDimension * ringRadiusRatio * pulseSize * dynamicPulse.value
            stars.forEach { star ->
                val starX = star.x * size.width
                val starY = star.y * size.height
                val twinkle = sin(starTwinklePhase * star.twinkleSpeed + star.twinkleOffset).toFloat()
                val starAlpha = (star.baseAlpha + twinkle * 0.2f).coerceIn(0f, 1f)
                drawCircle(
                    color = finalRingColor.copy(alpha = starAlpha * 0.4f),
                    radius = 2f,
                    center = Offset(starX, starY)
                )
            }
            withTransform({
                scale(scaleX = ringScaleX, scaleY = ringScaleY, pivot = center)
            }) {
                fun drawPolygon(
                    sides: Int,
                    rotationAngle: Float,
                    polygonRadius: Float,
                    polygonColor: Color,
                    alphaValue: Float
                ) {
                    if (sides < 3) return
                    val path = Path().apply {
                        val angleStep = 360f / sides
                        var firstPoint = true
                        for (i in 0 until sides) {
                            val angle = (i * angleStep + rotationAngle) * (PI.toFloat() / 180f)
                            val x = center.x + cos(angle) * polygonRadius
                            val y = center.y + sin(angle) * polygonRadius
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
                        color = polygonColor.copy(alpha = alphaValue),
                        style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
                drawPolygon(
                    sides = 6,
                    rotationAngle = polygonRotation1,
                    polygonRadius = baseRadius * 1.7f,
                    polygonColor = finalRingColor,
                    alphaValue = 0.18f
                )
                drawPolygon(
                    sides = 8,
                    rotationAngle = polygonRotation2,
                    polygonRadius = baseRadius * 1.45f,
                    polygonColor = finalRingColor,
                    alphaValue = 0.16f
                )
                drawCircle(
                    color = finalRingColor.copy(alpha = 0.2f * glowIntensity * dynamicGlow.value),
                    radius = baseRadius + 30 * dynamicGlow.value,
                    center = center,
                    style = Stroke(width = 40f, cap = StrokeCap.Round)
                )
                drawCircle(
                    color = finalRingColor.copy(alpha = 0.4f * glowIntensity * dynamicGlow.value),
                    radius = baseRadius + 15 * dynamicGlow.value,
                    center = center,
                    style = Stroke(width = 25f, cap = StrokeCap.Round)
                )
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
                                finalRingColor.copy(alpha = 0.8f * glowIntensity * dynamicGlow.value),
                                finalRingColor.copy(alpha = 1f * glowIntensity * dynamicGlow.value),
                                finalRingColor.copy(alpha = 0.8f * glowIntensity * dynamicGlow.value)
                            ),
                            start = Offset(center.x - baseRadius, center.y - baseRadius),
                            end = Offset(center.x + baseRadius, center.y + baseRadius)
                        ),
                        style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        }
        val textScale = remember { Animatable(1f) }
        LaunchedEffect(displayText) {
            textScale.animateTo(1.2f, animationSpec = tween(150))
            textScale.animateTo(1f, animationSpec = tween(150))
        }
        val finalTextToShow = if (pressing) currentPainMessage else displayText
        Text(
            text = finalTextToShow,
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .scale(textScale.value)
                .blur(radius = 0.5.dp)
        )
    }
}


// ----------------------------------------------------------------------------------------
// AI Profile Screen (with some spacing around the row)
// ----------------------------------------------------------------------------------------


// 1. Fixed AIProfileScreen that properly handles the data
@Composable
fun AIProfileScreen(
    navController: NavController,
) {
    val userId = 1
    val repository = remember { UserCharacterGet(RetrofitClient.apiService) }
    val viewModel: UserCharacterListViewModel = viewModel(
        factory = CharacterGetViewModelFactory(repository, userId)
    )

    // observe the LiveData as Compose state
    val characters by viewModel.userCharacterList.observeAsState(initial = emptyList())

    // Add basic loading state
    var isLoading by remember { mutableStateOf(true) }

    // Update loading state when characters are loaded
    LaunchedEffect(characters) {
        if (characters.isNotEmpty()) {
            isLoading = false
        }
    }

    // Add a timeout to prevent infinite loading state
    LaunchedEffect(Unit) {
        delay(5000) // 5 seconds timeout
        isLoading = false
    }

    if (isLoading && characters.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (characters.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No characters available",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        // render a LazyRow of real CharacterGetData items
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(characters) { character ->
                // Use the null-safe version of AIProfileCard
                NullSafeAIProfileCard(character = character) {
                    navController.navigate("ChatScreen/${character.id}")
                }
            }
        }
    }
}

// 2. A null-safe wrapper for AIProfileCard
@Composable
fun NullSafeAIProfileCard(
    character: CharacterGetData,
    onClick: () -> Unit
) {
    // Create a safe version of the character with default values for null fields
    val safeCharacter = CharacterGetData(
        id = character.id,
        Character_name = character.Character_name ?: "Unknown",
        Description = character.Description ?: "No description available",
        ChatCount = character.ChatCount,
        StarCount = character.StarCount
    )

    // Use the original AIProfileCard with the safe character
    AIProfileCard(character = safeCharacter, onClick = onClick)
}

// 3. Original AIProfileCard that expects non-null values
@Composable
fun AIProfileCard(
    character: CharacterGetData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .height(220.dp)
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector     = Icons.Default.Person,
                    contentDescription = null,
                    modifier        = Modifier.size(40.dp),
                    tint            = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Name & description
            Text(
                text       = character.Character_name,
                fontWeight = FontWeight.Bold,
                fontSize   = 13.sp,
                color      = MaterialTheme.colorScheme.onSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text       = character.Description,
                fontSize   = 10.sp,
                textAlign  = TextAlign.Center,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier   = Modifier.padding(horizontal = 6.dp),
                maxLines   = 3,
                overflow   = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ChatCount & StarCount
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Chat count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Default.MailOutline,
                        contentDescription = "Chats",
                        modifier           = Modifier.size(14.dp),
                        tint               = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text       = "${character.ChatCount}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 12.sp,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Star count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Default.Star,
                        contentDescription = "Stars",
                        modifier           = Modifier.size(14.dp),
                        tint               = Color(0xFFFFD700)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text       = "${character.StarCount}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 12.sp,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onClick,
                shape   = RoundedCornerShape(12.dp),
                colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Text(
                    text       = "Chat Now",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold
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
    // List of learning messages to display
    val learningCards = listOf(
        "Learning Step 1",
        "Learning Step 2",
        "Learning Step 3",
        "Learning Step 4"
    )

    // Track the current index
    var currentIndex by remember { mutableStateOf(0) }

    // Cycle through cards every 3 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            currentIndex = (currentIndex + 1) % learningCards.size
        }
    }

    // Slide + fade with a small scale effect
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
        // A gradient brush background for the card
        val gradientBrush = Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
            )
        )

        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(gradientBrush)
                    .padding(16.dp)
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = learningCards[targetIndex],
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
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
