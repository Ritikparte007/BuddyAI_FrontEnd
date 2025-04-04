package com.example.neuroed

import android.Manifest
import android.app.Activity
//import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
//import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraProvider
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
//import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.*
import java.util.Locale


import android.os.Handler
import android.os.Looper
import android.text.Layout
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
//import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureScope
//import androidx.compose.ui.layout.Placeable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.neuroed.model.TestCreate
import com.example.neuroed.network.MyWebSocketListener
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.SubjectSyllabusSaveRepository
import com.example.neuroed.repository.SubjectlistRepository
import com.example.neuroed.repository.TestCreateRepository
import com.example.neuroed.viewmodel.SubjectSyllabusSaveViewModel
import com.example.neuroed.viewmodel.SubjectSyllabusSaveViewModelFactory
import com.example.neuroed.viewmodel.SubjectlistViewModel
import com.example.neuroed.viewmodel.SubjectlistViewModelFactory
import com.example.neuroed.viewmodel.TestCreateViewModel
import com.example.neuroed.viewmodel.TestCreateViewModelFactory
import kotlinx.coroutines.launch


import okhttp3.WebSocket
import okhttp3.Request
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import kotlin.math.cos
import kotlin.math.sin

import androidx.compose.material3.Icon
import androidx.compose.material3.Text

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore



//import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.drawscope.DrawScope
//import androidx.compose.ui.layout.Placeable










import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
//import androidx.compose.ui.graphics.drawscope.drawLine
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue

import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel

import coil.compose.rememberAsyncImagePainter
import com.example.neuroed.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices





import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.microsoft.cognitiveservices.speech.ResultReason
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechSynthesisCancellationDetails
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response
import okhttp3.WebSocketListener
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.cos
import kotlin.math.sin




/** ---------------------------
 *       DATA CLASSES
 * --------------------------- */






import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.microsoft.cognitiveservices.speech.* // Import Azure Speech SDK
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext





import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.* // Add OkHttp dependency if using it for WebSocket
import okio.ByteString


















import androidx.compose.ui.res.vectorResource



import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.draw.clip

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.google.accompanist.permissions.*


import java.util.concurrent.TimeUnit // For OkHttpClient configuration



//=========================================================================================================================


import java.lang.Exception // Ensure Exception is imported


data class AIProfile(
    val image: Int,
    val name: String,
    val description: String,
    val rating: Double
)

val aiProfiles = listOf(
    AIProfile(R.drawable.biology, "DreamScapeAI", "Deep learning for stunning visuals", 4.6),
    AIProfile(R.drawable.biology, "NeuroVision",  "Next-gen AI-powered creativity",     4.7),
    AIProfile(R.drawable.biology, "MindGenix",    "Enhancing ideas with AI",            4.5)
)


const val TTS_DEBUG_TAG = "TTS_DEBUG"
const val MIC_DEBUG_TAG = "MicDebug"
const val VM_DEBUG_TAG = "ViewModelDebug"

class VoiceMessageViewModel : ViewModel() {

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _receivedMessage = MutableStateFlow("")
    val receivedMessage: StateFlow<String> = _receivedMessage.asStateFlow()

    private var webSocket: WebSocket? = null
    private val client: OkHttpClient = OkHttpClient.Builder()
        // Add longer timeouts if needed, especially for debugging
        .pingInterval(30, TimeUnit.SECONDS) // Keep connection alive
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    fun updateRecognizedText(text: String) {
        _recognizedText.value = text
        // Log sparingly if needed: Log.d(VM_DEBUG_TAG, "Recognized text updated in ViewModel: $text")
    }

    fun connectWebSocket() {
        if (webSocket != null) {
            Log.d(VM_DEBUG_TAG, "WebSocket already connected or connecting.")
            return
        }

        val actualUrlToUse = "ws://localhost:8000/api/Assistantconnection/" // <-- SET THE CORRECT URL HERE!

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
                    this@VoiceMessageViewModel.webSocket = null // Ensure cleared on close
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e(VM_DEBUG_TAG, "WebSocket Failure: ${t.message}", t)
                    response?.let {
                        Log.e(VM_DEBUG_TAG,"Failure Response: Code=${it.code}, Message=${it.message}")
                    }
                    this@VoiceMessageViewModel.webSocket = null // Ensure cleared on failure
                    // Optional: Add slight delay before potential auto-reconnect attempts
                }
            })
        }
    }

    /**
     * Sends the provided text message (recognized speech) over the WebSocket,
     * formatting it as a JSON object: {"message": "text content"}.
     */
    fun sendVoiceMessage(text: String) {
        if (text.isBlank()) {
            Log.w(VM_DEBUG_TAG, "Attempted to send blank message. Skipping.")
            return
        }

        val currentWebSocket = webSocket
        if (currentWebSocket == null) {
            Log.e(VM_DEBUG_TAG, "Cannot send message, WebSocket is null or closed. Attempting to reconnect...")
            connectWebSocket() // Attempt reconnect
            // Consider notifying the user or queuing the message
            // Toast.makeText(context, "Connection error. Please try again.", Toast.LENGTH_SHORT).show() // Cannot access context here directly
            return
        }

        // --- !!! IMPORTANT JSON FORMATTING !!! ---
        // Escape quotes within the text to create valid JSON
        val escapedText = text
            .replace("\\", "\\\\") // Escape backslashes first
            .replace("\"", "\\\"") // Escape double quotes

        // Construct the JSON payload string
        val jsonPayload = "{\"message\": \"$escapedText\"}"
        // --- End of JSON Formatting ---

        Log.d(VM_DEBUG_TAG, "Sending JSON payload via WebSocket: $jsonPayload")

        // Send the *JSON formatted string*
        val sent = currentWebSocket.send(jsonPayload)

        if (sent) {
            Log.d(VM_DEBUG_TAG, "JSON Message successfully queued for sending.")
            // Text clearing is handled in BottomNavBar onClick for immediate feedback
        } else {
            Log.e(VM_DEBUG_TAG, "Failed to queue JSON message (WebSocket closing or buffer full?).")
            // Maybe attempt reconnect or inform user
            connectWebSocket() // Attempt reconnect on send failure too
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
        // Clean up OkHttp client resources if it's not shared elsewhere
        // client.dispatcher.executorService.shutdown()
        // client.connectionPool.evictAll()
    }
}

// --- Rest of your Kotlin file (HomeScreen, BottomNavigationBar, Helpers) ---
// --- No changes needed in HomeScreen or BottomNavigationBar for this specific fix ---
// --- Paste the rest of your single-file code here ---

// --- Placeholder Composables ---
// Replace these with your actual UI implementations




@Composable fun VoiceVisualizer(modifier: Modifier = Modifier) {
    // Replace with your actual visualizer implementation
    Box(modifier = modifier.size(width = 50.dp, height = 10.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)))
}


// --- Main Screen Composable ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    // Obtain the ViewModel instance scoped to the NavHost/Activity lifecycle
    voiceMessageViewModel: VoiceMessageViewModel = viewModel()
) {
    val systemUiController = rememberSystemUiController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // --- System UI Configuration ---
    SideEffect {
        systemUiController.setSystemBarsColor(color = Color.Transparent, darkIcons = true)
    }

    // --- WebSocket Connection Trigger ---
    // Ensure the ViewModel initiates its WebSocket connection when the screen enters composition.
    LaunchedEffect(Unit) {
        Log.d("HomeScreen", "Requesting ViewModel to connect to Assistant WebSocket.")
        voiceMessageViewModel.connectWebSocket()
    }

    // --- Text-to-Speech Setup ---
    val speechSynthesizer = remember { mutableStateOf<SpeechSynthesizer?>(null) }
    val speechConfig = remember { mutableStateOf<SpeechConfig?>(null) }
    val audioConfig = remember { mutableStateOf<AudioConfig?>(null) }

    LaunchedEffect(Unit) {
        // Initialize TTS only once
        try {
            // IMPORTANT: Replace with your actual Azure Speech Key and Region
            val speechKey = "4a5877d33caa49bcbc19eade2c0fc602" // Looks like you already put yours here
            val serviceRegion = "centralindia" // Looks like you already put yours here

            // Basic check if placeholders are still there
            if (speechKey.startsWith("YOUR_") || serviceRegion.startsWith("YOUR_")) {
                Log.e(TTS_DEBUG_TAG, "Azure Speech Key/Region not set! TTS will not work.")
                Toast.makeText(context, "TTS setup error: Credentials missing", Toast.LENGTH_LONG).show()
                return@LaunchedEffect // Stop initialization
            }

            Log.d(TTS_DEBUG_TAG, "Initializing Azure speech synthesizer...")
            speechConfig.value = SpeechConfig.fromSubscription(speechKey, serviceRegion).apply {
                speechSynthesisVoiceName = "en-US-AvaMultilingualNeural" // Choose desired voice
            }
            audioConfig.value = AudioConfig.fromDefaultSpeakerOutput() // Output to device speaker
            speechSynthesizer.value = SpeechSynthesizer(speechConfig.value, audioConfig.value)
            Log.d(TTS_DEBUG_TAG, "Speech synthesizer initialized successfully.")

        } catch (e: Exception) {
            Log.e(TTS_DEBUG_TAG, "Failed to initialize speech synthesizer: ${e.message}", e)
            Toast.makeText(context, "TTS Initialization Failed", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Receive Assistant Reply & Synthesize Speech ---
    val assistantReply = voiceMessageViewModel.receivedMessage.collectAsState().value

    LaunchedEffect(assistantReply) {
        // React to changes in the assistant's reply message
        val synthesizer = speechSynthesizer.value
        if (assistantReply.isNotEmpty() && synthesizer != null) {
            // Parse the JSON reply from the backend if needed
            try {
                // Assuming backend sends {"message": "reply text"}
                val jsonObject = org.json.JSONObject(assistantReply) // Basic JSON parsing
                val replyText = jsonObject.optString("message", assistantReply) // Fallback to raw if key missing

                Log.d(TTS_DEBUG_TAG, "Assistant reply received, attempting synthesis for: '$replyText'")
                synthesizeSpeech(synthesizer, replyText) // Synthesize the extracted message

            } catch (e: org.json.JSONException) {
                // If the received message wasn't JSON, synthesize the raw text
                Log.w(TTS_DEBUG_TAG, "Received message is not valid JSON ('$assistantReply'). Synthesizing raw text.")
                synthesizeSpeech(synthesizer, assistantReply)
            } finally {
                // Clear the message from ViewModel state AFTER processing to prevent re-synthesis
                voiceMessageViewModel.clearReceivedMessage()
            }

        } else if (assistantReply.isNotEmpty() && synthesizer == null) {
            Log.w(TTS_DEBUG_TAG, "Received message but synthesizer is not ready.")
            // Message might be cleared without synthesis if synthesizer isn't ready in time
            voiceMessageViewModel.clearReceivedMessage()
        }
    }

    // --- Cleanup TTS Resources ---
    DisposableEffect(Unit) {
        onDispose {
            Log.d(TTS_DEBUG_TAG, "HomeScreen disposing. Cleaning up TTS resources.")
            scope.launch(Dispatchers.IO) { // Perform cleanup off the main thread
                try {
                    speechSynthesizer.value?.close()
                    speechConfig.value?.close()
                    audioConfig.value?.close()
                    Log.d(TTS_DEBUG_TAG, "TTS resources closed.")
                } catch (e: Exception) {
                    Log.e(TTS_DEBUG_TAG, "Error cleaning up TTS resources: ${e.message}")
                }
            }
            // The ViewModel's onCleared will handle its WebSocket closure.
        }
    }


    // --- UI Structure ---
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.75f)) { // Drawer Content
                Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Text("Menu", style = MaterialTheme.typography.titleMedium)
                }
                Divider()
                NavigationDrawerItem(
                    label = { Text("Home") }, selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Home, "Home") }
                )
                NavigationDrawerItem(
                    label = { Text("Settings") }, selected = false,
                    onClick = { navController.navigate("settings"); scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Settings, "Settings") }
                )
                NavigationDrawerItem(
                    label = { Text("Logout") }, selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        Log.d("HomeScreen", "Logout Action - Implement")
                        // Add logout logic (e.g., clear prefs, navigate to login)
                    },
                    icon = { Icon(Icons.Filled.ExitToApp, "Logout") }
                )
            }
        }
    ) { // Main screen content area
        Scaffold(
            topBar = {
                TopAppBar( // Use CenterAlignedTopAppBar if title should be centered
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    ),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu")
                        }
                    },
                    title = { /* Optional: Text("Home") */ },
                    actions = {
                        IconButton(onClick = { /* Navigate Subscription */ }) {
                            Icon(
                                painter = painterResource(R.drawable.premium), // Use your drawable
                                contentDescription = "Premium Features",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified // Or specific color
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { navController.navigate("AutoOpenCameraScreen") }) {
                    Icon(
                        painter = painterResource(R.drawable.camera), // Use your drawable
                        contentDescription = "Open Camera",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            // Pass the shared ViewModel instance down to the BottomNavigationBar
            bottomBar = {
                BottomNavigationBar(
                    navController = navController,
                    voiceMessageViewModel = voiceMessageViewModel
                )
            }
        ) { innerPadding -> // Content area managed by Scaffold
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // Apply padding to avoid overlap with bars
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // --- Screen Content Sections ---
                item { Box(modifier = Modifier.fillMaxWidth().height(400.dp)) { EmotionsDiagramAnimatedUI() } }
                item { SectionHeader(title = "SyllabusHub") }
                item { SubjectList(navController) }
                item { SectionHeader(title = "VirtuBeings") }
                item { AIProfileScreen(navController) }
                item { UserProfile(navController) }
                item { GridScreen(navController) }
                item { AgentList(navController) }
                item { Spacer(modifier = Modifier.height(80.dp)) } // Space at bottom
            }
        }
    }
}

















@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BottomNavigationBar(
    navController: NavController,
    voiceMessageViewModel: VoiceMessageViewModel
) {
    val context = LocalContext.current

    // --- State Variables ---
    var isMicOn by remember { mutableStateOf(false) }
    var selectedRoute by remember { mutableStateOf("CreateSubjectScreen") }
    // Store the SpeechRecognizer in a mutable state so we can reinitialize it when needed.
    var recognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    // Initialize recognizer on first composition if available
    LaunchedEffect(context) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            Log.d(MIC_DEBUG_TAG, "SpeechRecognizer created.")
        } else {
            Log.e(MIC_DEBUG_TAG, "SpeechRecognizer not available on this device.")
        }
    }

    // Observe recognized text from the ViewModel
    val recognizedText by voiceMessageViewModel.recognizedText.collectAsState()
    val showSendButton = recognizedText.isNotEmpty()

    // --- Permissions Handling ---
    val recordAudioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    // --- Handler for scheduling restart callbacks ---
    val restartHandler = remember { Handler(Looper.getMainLooper()) }
    var restartRunnable by remember { mutableStateOf<Runnable?>(null) }

    // Helper function to cancel any pending restart callbacks
    fun cancelRestart() {
        restartRunnable?.let { restartHandler.removeCallbacks(it) }
        restartRunnable = null
    }

    // --- Speech Recognition Listener ---
    val regularListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(MIC_DEBUG_TAG, "onReadyForSpeech")
            }
            override fun onBeginningOfSpeech() {
                Log.d(MIC_DEBUG_TAG, "onBeginningOfSpeech")
            }
            override fun onRmsChanged(rmsdB: Float) {
                Log.d(MIC_DEBUG_TAG, "Audio level (RMS): $rmsdB")
            }
            override fun onBufferReceived(buffer: ByteArray?) { }
            override fun onEndOfSpeech() {
                Log.d(MIC_DEBUG_TAG, "onEndOfSpeech")
                if (isMicOn && recognizer != null) {
                    Log.d(MIC_DEBUG_TAG, "Scheduling restart after EndOfSpeech.")
                    restartListening(
                        context = context,
                        recognizer = recognizer!!,
                        listener = this,
                        handler = restartHandler,
                        onRestart = { runnable -> restartRunnable = runnable }
                    )
                }
            }
            override fun onError(error: Int) {
                val errorText = getSpeechRecognizerErrorText(error)
                Log.e(MIC_DEBUG_TAG, "onError: $errorText ($error)")
                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        if (isMicOn && recognizer != null) {
                            Log.d(MIC_DEBUG_TAG, "Scheduling restart after non-critical error: $errorText")
                            restartListening(
                                context = context,
                                recognizer = recognizer!!,
                                listener = this,
                                handler = restartHandler,
                                onRestart = { runnable -> restartRunnable = runnable }
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
                Log.d(MIC_DEBUG_TAG, "onResults: ${matches?.joinToString(" | ")}")
                val bestResult = matches?.firstOrNull()?.trim() ?: ""
                if (bestResult.isNotEmpty()) {
                    Log.d(MIC_DEBUG_TAG, "Final Result: '$bestResult'")
                    voiceMessageViewModel.updateRecognizedText(bestResult)
                } else {
                    Log.d(MIC_DEBUG_TAG, "onResults: No valid result.")
                }
                if (isMicOn && recognizer != null) {
                    Log.d(MIC_DEBUG_TAG, "Scheduling restart after onResults.")
                    restartListening(
                        context = context,
                        recognizer = recognizer!!,
                        listener = this,
                        handler = restartHandler,
                        onRestart = { runnable -> restartRunnable = runnable }
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
            override fun onEvent(eventType: Int, params: Bundle?) { }
        }
    }

    // --- Start/Stop Listening Effect ---
    LaunchedEffect(isMicOn, recordAudioPermission.status, recognizer) {
        if (recognizer == null) {
            Log.w(MIC_DEBUG_TAG, "Recognizer is null. Cannot start listening.")
            if (isMicOn) isMicOn = false
            return@LaunchedEffect
        }
        if (isMicOn) {
            if (recordAudioPermission.status.isGranted) {
                Log.d(MIC_DEBUG_TAG, "Mic ON & permission granted. Starting listening.")
                startRegularListening(context, recognizer!!, regularListener)
            } else {
                Log.w(MIC_DEBUG_TAG, "Mic ON but permission not granted.")
                if (recordAudioPermission.status is PermissionStatus.Denied &&
                    !recordAudioPermission.status.shouldShowRationale
                ) {
                    Log.e(MIC_DEBUG_TAG, "Permission permanently denied. Turning mic OFF.")
                    Toast.makeText(context, "Microphone permission is required.", Toast.LENGTH_LONG).show()
                    isMicOn = false
                    voiceMessageViewModel.updateRecognizedText("")
                }
            }
        } else {
            Log.d(MIC_DEBUG_TAG, "Mic OFF. Stopping listening.")
            recognizer?.stopListening()
            cancelRestart()
        }
    }

    // --- Cleanup: Destroy Recognizer When Composable Leaves ---
    DisposableEffect(Unit) {
        onDispose {
            Log.d(MIC_DEBUG_TAG, "Disposing recognizer.")
            recognizer?.destroy()
        }
    }

    // --- UI Layout ---
    Surface(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Recognized Text Display
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
                                Log.d(MIC_DEBUG_TAG, "Send button clicked. Sending: '$recognizedText'")
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
            // Navigation Bar Items
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
                    // Example Navigation Items: Subject, Feed, Mic, Library, Character
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
                    // --- Central Mic Button ---
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            if (recognizer == null) {
                                Toast.makeText(context, "Speech recognition unavailable", Toast.LENGTH_SHORT).show()
                                return@NavigationBarItem
                            }
                            Log.d(MIC_DEBUG_TAG, "Mic button clicked. Current state: ${if (isMicOn) "ON" else "OFF"}")
                            if (!isMicOn && !recordAudioPermission.status.isGranted) {
                                Log.d(MIC_DEBUG_TAG, "Requesting microphone permission.")
                                recordAudioPermission.launchPermissionRequest()
                            } else {
                                if (isMicOn) {
                                    // Stop listening and cancel pending restarts
                                    recognizer?.stopListening()
                                    cancelRestart()
                                    isMicOn = false
                                } else {
                                    // Reinitialize recognizer for a fresh start
                                    recognizer?.destroy()
                                    recognizer = SpeechRecognizer.createSpeechRecognizer(context)
                                    Log.d(MIC_DEBUG_TAG, "Reinitialized SpeechRecognizer for fresh start.")
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
                                    painter = painterResource(id = if (isMicOn)
                                        R.drawable.baseline_mic_24
                                    else
                                        R.drawable.baseline_mic_off_24),
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
                        label = "Library",
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
                            navController.navigate("settings") { launchSingleTop = true }
                        }
                    )
                }
            }
        }
    }
}

// --- Helper Function: Restart Listening with Delay ---
private fun restartListening(
    context: Context,
    recognizer: SpeechRecognizer,
    listener: RecognitionListener,
    handler: Handler,
    delayMillis: Long = 500L,
    onRestart: (Runnable) -> Unit
) {
    recognizer.cancel()
    val runnable = Runnable {
        startRegularListening(context, recognizer, listener)
    }
    onRestart(runnable)
    handler.postDelayed(runnable, delayMillis)
}

// --- Helper Function: Start Regular Listening ---
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
























/**
 * Provides human-readable text for SpeechRecognizer error codes.
 */
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
                // Use theme colors for consistency
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        label = {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        // Customize colors further if needed
        // colors = NavigationBarItemDefaults.colors(...)
    )
}

/**
 * Performs text-to-speech synthesis using Azure SDK. Runs on IO Dispatcher.
 */
private suspend fun synthesizeSpeech(synthesizer: SpeechSynthesizer, text: String) = withContext(Dispatchers.IO) {
    if (text.isBlank()) {
        Log.d(TTS_DEBUG_TAG, "Skipping synthesis for blank/empty text.")
        return@withContext
    }
    try {
        Log.d(TTS_DEBUG_TAG, "Attempting to synthesize: '$text'")
        // This is a blocking call within the IO context
        val result: SpeechSynthesisResult = synthesizer.SpeakTextAsync(text).get()

        // Check the result
        when (result.reason) {
            ResultReason.SynthesizingAudioCompleted -> {
                Log.d(TTS_DEBUG_TAG, "Speech synthesis completed successfully for '$text'. Audio duration: ${result.audioDuration}")
            }
            ResultReason.Canceled -> {
                val cancellation = SpeechSynthesisCancellationDetails.fromResult(result)
                Log.e(TTS_DEBUG_TAG, "Speech synthesis CANCELED for '$text'")
                Log.e(TTS_DEBUG_TAG, ">> Reason: ${cancellation.reason}")
                if (cancellation.reason == CancellationReason.Error) {
                    Log.e(TTS_DEBUG_TAG, ">> ErrorCode: ${cancellation.errorCode}")
                    Log.e(TTS_DEBUG_TAG, ">> ErrorDetails: ${cancellation.errorDetails}")
                }
            }
            else -> {
                Log.w(TTS_DEBUG_TAG, "Speech synthesis result for '$text': ${result.reason}")
            }
        }
        // IMPORTANT: Close the result to release native resources
        result.close()

    } catch (e: Exception) {
        // Catch potential exceptions during synthesis (e.g., network issues, auth problems)
        Log.e(TTS_DEBUG_TAG, "Speech synthesis FAILED for '$text': ${e.message}", e)
        // Avoid showing toasts directly from background thread/suspend function if possible.
        // Propagate error state back to UI if user feedback is needed.
    }
}

// --- End of File ---


















// Helper function to get error text



/** ---------------------------
 *       Websocket FUNCTION
 * --------------------------- */


private lateinit var webSocket: WebSocket






/** ---------------------------
 *       SECTION HEADER
 * --------------------------- */
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


/** ---------------------------
 *       SUBJECT LIST
 * --------------------------- */


@Composable
fun SubjectList(
    navController: NavController,
    subjectlistViewModel: SubjectlistViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = SubjectlistViewModelFactory(
            // Provide your repository instance using RetrofitClient.apiService
            SubjectlistRepository(RetrofitClient.apiService)
        )
    )
) {
    // Collect the subject list state from the ViewModel
    val subjects by subjectlistViewModel.subjectList.collectAsState()

    // Trigger the API call when the composable enters composition
    LaunchedEffect(Unit) {
        subjectlistViewModel.fetchSubjectList() // Defaults to userId = 1 if defined that way
    }

    if (subjects.isEmpty()) {
        // Show a loading indicator while waiting for data
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Display fetched subjects in a horizontal list.
        // Each subject card is clickable and navigates to a detail screen.
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
                            // Replace with your actual navigation route.
                            // Assumes subject has an 'id' property.
                            navController.navigate("SyllabusScreen/${subject.id}/${subject.subjectDescription}/${subject.subject}")
                        }
                        .width(130.dp)
                        .height(190.dp),
                    shape = RoundedCornerShape(13.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Use Coil's AsyncImage with placeholder and error fallback.
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
                        // Optional overlay (for loading/progress indication).
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(60.dp),
                                progress = 1.0f, // Replace with actual progress if available.
                                color = Color.White,
                                strokeWidth = 6.dp
                            )
                            Text(
                                text = "100%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        // Subject label overlay at top start.
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


/** ---------------------------
 *       USER PROFILE
 * --------------------------- */
@Composable
fun UserProfile(
    navController: NavController,
    backgroundColor: Color = Color.Black
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { navController.navigate("ProfileScreen") }
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
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
                    .border(2.dp, Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "John Doe",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
                        color = Color.Blue,
                        trackColor = Color.LightGray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "10",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star",
                            tint = if (index < 4) Color.Yellow else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/** ---------------------------
 *       VOICE VISUALIZER
 * --------------------------- */
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

/** ---------------------------
 *       BOTTOM NAV BAR
 * --------------------------- */
// 3. Updated Bottom Navigation Bar










/** ---------------------------
 *       GRID SCREEN
 * --------------------------- */

data class GridItemModel(
    val title: String,
    @DrawableRes val iconRes: Int,
    val route: String,
    val badgecount: Int = 0
)

@Composable
fun GridScreen(navController: NavController) {
    val items = listOf(
        GridItemModel("Test", R.drawable.test, "TestsScreen", badgecount = 2),
        GridItemModel("Task", R.drawable.taskuser, "TaskScreen",badgecount = 10),
        GridItemModel("Exam", R.drawable.exam, "ExamScreen", badgecount = 1),
        GridItemModel("Recall", R.drawable.recall, "RecallingScreen"),
        GridItemModel("Store", R.drawable.folders, "StoreScreen"),
        GridItemModel("Mindmap", R.drawable.mindmap, "DocumentsScreen"),
        GridItemModel("Assignents", R.drawable.contract, "PlaygroundScreen"),
        GridItemModel("Playground", R.drawable.readingbook, "PlaygroundScreen"),
        GridItemModel("Meditation",R.drawable.meditation," Meditation"),
        GridItemModel("Brainhealth",R.drawable.brainhealth," Meditation"),
        GridItemModel("Meditation",R.drawable.translation," Meditation"),
        GridItemModel("Meditation",R.drawable.question,"MeditationScreen")

        // Add more items as needed...
    )

    Box(
        modifier = Modifier
            .height(350.dp)
            .padding(16.dp)
//            .background(color = Color.Red)
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
    @DrawableRes iconRes: Int,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
//            .background(color = Color.Green)
            .width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Use BadgedBox to overlay a badge on the icon
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge {
                        Text(
                            text = badgeCount.toString(),
                            fontSize = 10.sp, // Adjust the font size if needed
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


@Composable
fun AgentList( navController: NavController) {
    // Root column

    val iconPainter: Painter = painterResource(id = R.drawable.baseline_add_24)
    Column(
        modifier = Modifier
            .fillMaxSize()               // Fill the parent container .background(Color(0xFF555555)) // Example background color from the screenshot
            .padding(16.dp)             // Outer padding
    ) {
        // Title
        Text(
            text = "Agent",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Row that holds the avatar/name and the plus icon
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Column for agent avatar + name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular avatar (replace with your image resource)
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                ) {
                    // For a local drawable resource
                    Image(
                        painter = painterResource(R.drawable.man), // Replace with your drawable
                        contentDescription = "Agent Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Agent name
                Text(
                    text = "Areax",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontSize = 12.sp,

                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            // Circular box for plus button
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .clickable(onClick = {
                        navController.navigate("AgentCreateScreen") // Replace with your destination route
                    })
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = iconPainter,
                    contentDescription = "Add Agent",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}


@Composable
fun EmotionsDiagramAnimatedUI(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize(), // Now using full size for proper fitting
        contentAlignment = Alignment.Center
    ) {
        // Center text and animation
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            SoundReactiveGlowingRing(
                text = "BuddyAI",
                ringColor = Color(0xFF00E5FF), // Bright cyan color
                ringRadiusRatio = 0.38f // Increased from 0.35f for better visibility
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

    // Sound level state
    var soundLevel by remember { mutableStateOf(0f) }
    var displayText by remember { mutableStateOf(text) }
    var isListening by remember { mutableStateOf(false) }

    // Emotion state
    var currentEmotion by remember { mutableStateOf("neutral") }

    // Animation for emotion changes
    val transitionSize = 200 // ms
    val colorTransition = updateTransition(currentEmotion, label = "colorTransition")
    val currentRingColor by colorTransition.animateColor(
        transitionSpec = { tween(transitionSize) },
        label = "ringColorAnimation"
    ) { emotion ->
        when (emotion) {
            "neutral" -> Color(0xFF00E5FF) // Cyan (original)
            "excited" -> Color(0xFFFF5722) // Deep Orange
            "thinking" -> Color(0xFF9C27B0) // Purple
            "agreeing" -> Color(0xFF4CAF50) // Green
            "curious" -> Color(0xFFFFEB3B) // Yellow
            "surprised" -> Color(0xFFFF9800) // Orange
            "concerned" -> Color(0xFFE91E63) // Pink
            else -> ringColor
        }
    }

    // Check audio permission
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Animation values influenced by sound
    val baseRotationSpeed = 15000 // Base rotation speed
    val rotationSpeed = remember { Animatable(baseRotationSpeed.toFloat()) }

    // Base animation values
    val infiniteTransition = rememberInfiniteTransition()

    // Rotation animation - speed changes with sound
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

    // Pulse animation - amplitude changes with sound
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

    // Dynamic pulse based on sound
    val dynamicPulse = remember { Animatable(1f) }

    // Glow intensity animation - increases with sound
    val baseGlowMin = 0.7f
    val baseGlowMax = 1f
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = baseGlowMin,
        targetValue = baseGlowMax,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Dynamic glow based on sound
    val dynamicGlow = remember { Animatable(1f) }

    // Wave distortion animation
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        )
    )

    // Dynamic wave intensity based on sound
    val waveIntensity = remember { Animatable(1f) }

    // Enhanced text responses for when sound is detected, grouped by emotion categories
    val responsesByEmotion = mapOf(
        "listening" to listOf(
            "Listening...", "I hear you", "I'm here", "Tell me"
        ),
        "thinking" to listOf(
            "Processing...", "Analyzing...", "Thinking...", "Computing...", "Learning..."
        ),
        "curious" to listOf(
            "Tell me more", "Go on...", "Interesting!", "And then?", "I'd like to know more"
        ),
        "agreeing" to listOf(
            "I understand", "Got it", "I follow", "Makes sense", "I see what you mean"
        ),
        "excited" to listOf(
            "That's great!", "Fascinating!", "Wonderful!", "Amazing!", "Brilliant!"
        ),
        "surprised" to listOf(
            "Oh!", "Really?", "Wow!", "Surprising!", "Is that so?"
        ),
        "concerned" to listOf(
            "Hmm...", "Let me check", "Are you sure?", "I'm not certain", "One moment..."
        )
    )

    // Remember the last time we changed the text
    var lastTextChangeTime by remember { mutableStateOf(0L) }

    // Sound pattern detection
    var soundPattern by remember { mutableStateOf(listOf<Float>()) }
    val patternSize = 5
    var consecutiveQuietFrames by remember { mutableStateOf(0) }
    var consecutiveLoudFrames by remember { mutableStateOf(0) }

    // More realistic voice detection parameters
    val quietThreshold = 0.03f
    val loudThreshold = 0.1f

    // Start audio recording if permission is granted
    LaunchedEffect(hasAudioPermission) {
        if (hasAudioPermission && !isListening) {
            isListening = true

            // Audio configuration
            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            audioRecord.startRecording()

            // Monitor sound levels
            coroutineScope.launch(Dispatchers.IO) {
                val buffer = ShortArray(bufferSize)
                var silenceCounter = 0
                val currentTime = System.currentTimeMillis()
                lastTextChangeTime = currentTime

                while (isListening) {
                    val readResult = audioRecord.read(buffer, 0, bufferSize)

                    if (readResult > 0) {
                        // Calculate sound level
                        var sum = 0.0
                        for (sample in buffer) {
                            sum += abs(sample.toDouble())
                        }
                        val average = sum / bufferSize
                        val normalized = (average / Short.MAX_VALUE).toFloat().coerceIn(0f, 1f)

                        soundLevel = normalized

                        // Update sound pattern for better speech detection
                        soundPattern = (soundPattern + normalized).takeLast(patternSize)

                        // Track consecutive quiet and loud frames for better speech pattern detection
                        if (normalized < quietThreshold) {
                            consecutiveQuietFrames++
                            consecutiveLoudFrames = 0
                        } else if (normalized > loudThreshold) {
                            consecutiveLoudFrames++
                            consecutiveQuietFrames = 0
                        }

                        // Update animations based on sound level with more realistic response
                        launch {
                            // Faster rotation when sound detected
                            rotationSpeed.animateTo(
                                targetValue = (baseRotationSpeed * (1f - soundLevel * 0.7f)).toFloat(),
                                animationSpec = spring(stiffness = 100f)
                            )

                            // More pulse when sound detected - more pronounced
                            dynamicPulse.animateTo(
                                targetValue = 1f + (soundLevel * 0.7f),
                                animationSpec = spring(stiffness = 300f)
                            )

                            // More glow when sound detected - more pronounced
                            dynamicGlow.animateTo(
                                targetValue = 1f + (soundLevel * 1.2f),
                                animationSpec = spring(stiffness = 300f)
                            )

                            // More wave distortion when sound detected
                            waveIntensity.animateTo(
                                targetValue = 1f + (soundLevel * 3f),
                                animationSpec = spring(stiffness = 300f)
                            )
                        }

                        // More nuanced text response based on sound patterns
                        val now = System.currentTimeMillis()
                        val timeSinceLastChange = now - lastTextChangeTime

                        // Detect speech patterns for better reaction
                        if (normalized > 0.05f) {
                            silenceCounter = 0

                            // Only change text and emotion if enough time has passed (prevents too frequent changes)
                            if ((displayText == text || timeSinceLastChange > 1500) && Math.random() < 0.25) {
                                // Choose emotion based on sound pattern
                                val newEmotion = when {
                                    // Detecting excited speech (loud with variations)
                                    normalized > 0.25f && soundPattern.maxOrNull()!! > 0.3f -> {
                                        "excited"
                                    }
                                    // Detecting questioning pattern (rising sound levels)
                                    soundPattern.size >= 3 &&
                                            soundPattern.takeLast(3).zipWithNext().all { it.second > it.first } -> {
                                        "curious"
                                    }
                                    // Detecting steady speech (consistent moderate levels)
                                    soundPattern.size >= 3 &&
                                            soundPattern.takeLast(3).all { it > 0.08f && it < 0.2f } -> {
                                        "agreeing"
                                    }
                                    // Detecting sudden peak
                                    soundPattern.size >= 2 &&
                                            soundPattern.last() > 0.2f && soundPattern[soundPattern.size - 2] < 0.1f -> {
                                        "surprised"
                                    }
                                    // Detecting inconsistent pattern
                                    soundPattern.size >= 3 &&
                                            soundPattern.takeLast(3).maxOrNull()!! - soundPattern.takeLast(3).minOrNull()!! > 0.15f -> {
                                        "concerned"
                                    }
                                    // Longer consistent speech pattern
                                    consecutiveLoudFrames > 5 -> {
                                        "thinking"
                                    }
                                    // Default listening state
                                    else -> "listening"
                                }

                                // Update emotion which will trigger color transition
                                currentEmotion = when (newEmotion) {
                                    "listening" -> "neutral"
                                    "thinking" -> "thinking"
                                    "curious" -> "curious"
                                    "agreeing" -> "agreeing"
                                    "excited" -> "excited"
                                    "surprised" -> "surprised"
                                    "concerned" -> "concerned"
                                    else -> "neutral"
                                }

                                // Select text based on the emotion
                                val responses = responsesByEmotion[newEmotion] ?: responsesByEmotion["listening"]!!
                                displayText = responses.random()
                                lastTextChangeTime = now
                            }
                        } else {
                            silenceCounter++
                            // Reset to default after longer silence
                            if (silenceCounter > 25 && displayText != text) {
                                displayText = text
                                currentEmotion = "neutral"
                            }
                        }
                    }

                    kotlinx.coroutines.delay(50)
                }

                // Cleanup
                audioRecord.stop()
                audioRecord.release()
            }
        }
    }

    // Cleanup when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            isListening = false
        }
    }

    // Create the composable
    Box(
        modifier = Modifier
            .fillMaxSize(0.9f) // Increased from 0.75f to 0.9f for better fit
            .wrapContentSize(Alignment.Center), // Added to ensure proper centering
        contentAlignment = Alignment.Center
    ) {
        // Render the glowing ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension * ringRadiusRatio * pulseSize * dynamicPulse.value

            // Outer glow effect (drawn first to be behind the main ring)
            drawCircle(
                color = currentRingColor.copy(alpha = 0.2f * glowIntensity * dynamicGlow.value),
                radius = radius + 30 * dynamicGlow.value,
                center = center,
                style = Stroke(width = 40f, cap = StrokeCap.Round)
            )

            // Medium glow layer
            drawCircle(
                color = currentRingColor.copy(alpha = 0.4f * glowIntensity * dynamicGlow.value),
                radius = radius + 15 * dynamicGlow.value,
                center = center,
                style = Stroke(width = 25f, cap = StrokeCap.Round)
            )

            // Apply rotation for the main ring
            rotate(rotation) {
                // Draw the main distorted ring
                val segments = 180
                val angleStep = (2 * PI / segments).toFloat()

                val path = Path()
                var firstPoint = true

                for (i in 0 until segments) {
                    val angle = i * angleStep

                    // Apply wave distortion - intensity increased with sound
                    val distortionAmplitude = 8 * waveIntensity.value * sin(angle * 3 + wavePhase)
                    val distortedRadius = radius + distortionAmplitude

                    val x = center.x + cos(angle) * distortedRadius
                    val y = center.y + sin(angle) * distortedRadius

                    if (firstPoint) {
                        path.moveTo(x, y)
                        firstPoint = false
                    } else {
                        path.lineTo(x, y)
                    }
                }

                path.close()

                // Draw the distorted ring with a gradient
                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            currentRingColor.copy(alpha = 0.8f * glowIntensity * dynamicGlow.value),
                            currentRingColor.copy(alpha = 1f * glowIntensity * dynamicGlow.value),
                            currentRingColor.copy(alpha = 0.8f * glowIntensity * dynamicGlow.value)
                        ),
                        start = Offset(center.x - radius, center.y - radius),
                        end = Offset(center.x + radius, center.y + radius)
                    ),
                    style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }

        // Text in the center with subtle animation based on emotion
        val textScale = remember { Animatable(1f) }

        // Scale text briefly when it changes
        LaunchedEffect(displayText) {
            textScale.animateTo(1.2f, animationSpec = tween(150))
            textScale.animateTo(1f, animationSpec = tween(150))
        }

        Text(
            text = displayText,
            color = Color.White,
            fontSize = 30.sp, // Increased from 28sp to 30sp for better visibility
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .scale(textScale.value)
                .blur(radius = 0.5.dp) // Subtle blur for text glow effect
        )

        // Emotion indicator text (for debugging, can be removed in production)
        /*
        Text(
            text = currentEmotion,
            color = currentRingColor,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
        */
    }
}


/** ---------------------------
 *       AI PROFILE SCREEN
 * --------------------------- */
@Composable
fun AIProfileScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(aiProfiles) { profile ->
                AIProfileCard(profile) { navController.navigate("ChatScreen") }
            }
        }
    }
}

@Composable
fun AIProfileCard(
    profile: AIProfile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .height(220.dp)
            .padding(8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = profile.image),
                    contentDescription = "AI Profile Image",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = profile.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = profile.description,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${profile.rating}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MailOutline,
                        contentDescription = "Chat count",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Chat",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = { onClick() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Text(
                    text = "Chat Now",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/** ---------------------------
 *   SPEECH RECOGNITION SETUP
 * --------------------------- */


private var speechRecognizer: SpeechRecognizer? = null
private var isListening = false

private fun startListening(context: Context) {
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
        Toast.makeText(context, "Speech Recognition not available", Toast.LENGTH_SHORT).show()
        return
    }
    // Create a persistent SpeechRecognizer instance if needed
    if (speechRecognizer == null) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    }

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    speechRecognizer?.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d("SpeechRecognizer", "Ready for speech")
        }
        override fun onBeginningOfSpeech() {
            Log.d("SpeechRecognizer", "Speech started")
        }
        override fun onRmsChanged(rmsdB: Float) { }
        override fun onBufferReceived(buffer: ByteArray?) { }
        override fun onEndOfSpeech() {
            Log.d("SpeechRecognizer", "Speech ended")
            // Add a slight delay before restarting listening
            Handler(Looper.getMainLooper()).postDelayed({
                if (isListening) {
                    speechRecognizer?.startListening(intent)
                }
            }, 500)
        }
        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                else -> "Unknown error"
            }
            Log.e("SpeechRecognizer", "Error occurred: $error - $errorMessage")
            // Add a slight delay before restarting listening (if appropriate)
            Handler(Looper.getMainLooper()).postDelayed({
                if (isListening && error != SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                    speechRecognizer?.startListening(intent)
                }
            }, 500)
        }
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.firstOrNull()?.let { finalText ->
                Log.d("SpeechRecognizer", "Final result: $finalText")
            }
        }
        override fun onPartialResults(partialResults: Bundle?) {
            val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            partialMatches?.firstOrNull()?.let { partialText ->
                Log.d("SpeechRecognizer", "Partial result: $partialText")
            }
        }
        override fun onEvent(eventType: Int, params: Bundle?) { }
    })

    // Set listening state and start listening
    isListening = true
    speechRecognizer?.startListening(intent)
}

// Remember to add a stop function to properly release resources when needed
private fun stopListening() {
    isListening = false
    speechRecognizer?.stopListening()
    speechRecognizer?.cancel()
    speechRecognizer?.destroy()
    speechRecognizer = null
}


/** ---------------------------
 *    OPTIONAL: PREVIEWS
 * --------------------------- */
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        val navController = rememberNavController()
        HomeScreen(navController = navController)
    }
}


@Composable
fun LocationScreen() {
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    var locationText by remember { mutableStateOf("लोकेशन प्राप्त हो रही है...") }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    locationText = "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
                } else {
                    locationText = "लोकेशन नहीं मिली"
                }
            }
        } else {
            locationText = "परमिशन नहीं मिला"
        }
    }

    Text(text = locationText)
}


fun getAllImages(context: Context): List<Uri> {
    val imageUris = mutableListOf<Uri>()
    // Define the columns to retrieve (only the image ID here)
    val projection = arrayOf(MediaStore.Images.Media._ID)
    // Optional: Sort images by date added (descending order)
    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

    // Query the external storage for images
    val query = context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        sortOrder
    )

    // Use the cursor to iterate over the results
    query?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            // Build the content Uri for each image
            val contentUri: Uri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )
            imageUris.add(contentUri)
        }
    }
    return imageUris
}



