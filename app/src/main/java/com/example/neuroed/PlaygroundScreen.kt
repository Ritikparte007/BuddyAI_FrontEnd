package com.example.neuroed

import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.neuroed.ChatMessage
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.neuroed.R
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.SubjectlistRepository
import com.example.neuroed.viewmodel.SubjectSyllabusViewModel
import com.example.neuroed.viewmodel.SubjectSyllabusViewModelFactory
import com.example.neuroed.viewmodel.SubjectlistViewModelFactory
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.neuroed.viewmodel.SubjectlistViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import com.example.neuroed.model.SubjectSyllabusGetResponse
import com.example.neuroed.model.SubjectSyllabusHeadingTopic
import com.example.neuroed.model.SubjectSyllabusHeadingTopicSubtopic
import com.example.neuroed.model.SubjectlistResponse
import com.example.neuroed.repository.SubjectSyllabusGetRepository
import com.example.neuroed.repository.SubjectSyllabusHeadingRepository
import com.example.neuroed.repository.SubjectSyllabusHeadingSubtopicRepository
import com.example.neuroed.repository.SubjectSyllabusHeadingTopicRepository
import com.example.neuroed.viewmodel.SubjectSyllabusHeadingTopicSubtopicViewModel
import com.example.neuroed.viewmodel.SubjectSyllabusHeadingTopicSubtopicViewModelFactory
import com.example.neuroed.viewmodel.SubjectSyllabusHeadingTopicViewModel
import com.example.neuroed.viewmodel.SubjectSyllabusHeadingTopicViewModelFactory
import com.example.neuroed.viewmodel.SubjectSyllabusHeadingViewModel
import com.example.neuroed.viewmodel.SubjectSyllabusHeadingViewModelFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import org.json.JSONObject
import com.google.accompanist.permissions.rememberPermissionState
import android.Manifest
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.google.accompanist.permissions.shouldShowRationale
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import kotlinx.coroutines.delay
import android.content.Context
import com.example.neuroed.NeuroEdApp
import com.example.neuroed.model.UserInfoViewModel
import android.util.Base64
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import kotlinx.coroutines.isActive
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.neuroed.viewmodel.AzureTTSViewModel
import com.example.neuroed.viewmodel.TTSState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.IOException

// Import Global Voice Manager
import com.example.neuroed.voice.GlobalVoiceManager
import com.example.neuroed.voice.VoiceEnabledScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.awaitCancellation

//import android.util.Log

// --------------------
// Google Fonts Setup (unchanged)
// --------------------
private val certRes: Int = R.array.com_google_android_gms_fonts_certs

private val robotoFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = certRes
)

private val robotoFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Roboto"),
        fontProvider = robotoFontProvider,
        weight = FontWeight.Normal
    ),
    Font(
        googleFont = GoogleFont("Roboto"),
        fontProvider = robotoFontProvider,
        weight = FontWeight.Bold
    )
)

private val NeuroEdTypographys = Typography(
    bodyLarge = TextStyle(
        fontFamily = robotoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = Color.Black
    )
)

@Composable
fun NeuroEdTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        typography = NeuroEdTypographys,
        content = content
    )
}

// --------------------
// Compact Mode Switch (Radio Buttons)
// --------------------

@Composable
fun CompactModeSwitch(
    selectedMode: String,
    onModeSelected: (String) -> Unit
) {
    // Using a Row with very small RadioButtons and labels.
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 20.dp)
    ) {
        RadioButton(
            selected = selectedMode == "Anim",
            onClick = { onModeSelected("Anim") },
            modifier = Modifier.size(3.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = "Animation", fontSize = 10.sp)

        Spacer(modifier = Modifier.width(10.dp))
        RadioButton(
            selected = selectedMode == "Canvas",
            onClick = { onModeSelected("Canvas") },
            modifier = Modifier.size(5.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = "Canvas", fontSize = 10.sp)

        Spacer(modifier = Modifier.width(10.dp))
        RadioButton(
            selected = selectedMode == "Camera",
            onClick = { onModeSelected("Camera") },
            modifier = Modifier.size(5.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = "GPT Vision", fontSize = 10.sp)

        Spacer(modifier = Modifier.width(10.dp))
        RadioButton(
            selected = selectedMode == "WebBrowser",
            onClick = { onModeSelected("WebBrowser") },
            modifier = Modifier.size(5.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = "WebBrowser", fontSize = 10.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PlaygroundScreen(
    navController: NavController
) {
    val context = LocalContext.current

    // ✅ Enhanced VoiceEnabledScreen with all callbacks
    VoiceEnabledScreen(
        screenName = "PlaygroundScreen",
        onVoiceResult = { text ->
            Log.d("PlaygroundVoice", "✅ Voice result received: $text")
            // Text will be handled in ChatInputBar
        },
        onVoiceError = { error ->
            Log.e("PlaygroundVoice", "❌ Voice error: $error")
            Toast.makeText(context, "Voice error: $error", Toast.LENGTH_SHORT).show()
        },
        onNetworkError = {
            Log.w("PlaygroundVoice", "📡 Network error")
            Toast.makeText(context, "Network connection required", Toast.LENGTH_SHORT).show()
        },
        onPermissionRequired = {
            Log.w("PlaygroundVoice", "🔐 Permission required")
            Toast.makeText(context, "Microphone permission required", Toast.LENGTH_SHORT).show()
        },
        silenceTimeoutMs = 20000L, // 20 seconds for playground
        maxSessionDuration = 600000L, // 10 minutes
        autoRetryEnabled = true
    ) { isListening, recognizedText, networkAvailable, startVoice, stopVoice, restartSession ->

        // ✅ Enhanced text clearing callback
        val clearText = remember {
            {
                Log.d("PlaygroundVoice", "🧹 Clearing recognized text")
                GlobalVoiceManager.clearCurrentText()
            }
        }

        // ✅ All your existing playground state management
        var topWeight by remember { mutableStateOf(0.5f) }
        var containerHeight by remember { mutableStateOf(0) }
        var mode by remember { mutableStateOf("Anim") }
        val density = LocalDensity.current

        var receivedData by remember { mutableStateOf("") }
        var simulationCode by remember { mutableStateOf("") }
        var explanationText by remember { mutableStateOf("") }

        // ✅ Azure TTS ViewModel
        val ttsViewModel: AzureTTSViewModel = viewModel()
        val ttsState by ttsViewModel.ttsState.collectAsState()

        // ✅ Initialize TTS when screen is first composed
        LaunchedEffect(Unit) {
            ttsViewModel.initializeTTS(context)
        }

        // ✅ AUTO-SPEECH: Start speaking when explanationText changes
        LaunchedEffect(explanationText) {
            if (explanationText.isNotEmpty() && explanationText != "Waiting for data...") {
                delay(500) // Small delay to ensure UI is updated
                ttsViewModel.speakText(explanationText)
            }
        }

        // ✅ WebSocket management with proper cleanup
        val webSocketRef = remember { mutableStateOf<WebSocket?>(null) }
        DisposableEffect(Unit) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("ws://localhost:8000/api/playground/")
                .build()

            val webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d("WebSocket", "Connection opened")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    try {
                        val jsonObject = JSONObject(text)
                        val messageObj = jsonObject.getJSONObject("message")
                        val explanation = messageObj.getString("Explanations")
                        val simulationCode = messageObj.getString("simulation_code")

                        Handler(Looper.getMainLooper()).post {
                            receivedData = simulationCode
                            explanationText = explanation
                            Log.d("WebSocket", "Simulation Code: $receivedData")
                        }
                    } catch (e: JSONException) {
                        Log.e("WebSocket", "Error parsing message", e)
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d("WebSocket", "Closing: $code / $reason")
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e("WebSocket", "Error: ${t.message}")
                }
            })

            webSocketRef.value = webSocket

            onDispose {
                webSocket.close(1000, "PlaygroundScreen disposed")
                webSocketRef.value = null
            }
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // ✅ Compact mode switch at the top
                    CompactModeSwitch(
                        selectedMode = mode,
                        onModeSelected = { mode = it }
                    )

                    // ✅ Container for top card and explanation card
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                containerHeight = coordinates.size.height
                            }
                    ) {
                        // ✅ Top card: Animation/Canvas/Camera mode
                        Card(
                            modifier = Modifier
                                .weight(topWeight)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            elevation = cardElevation(defaultElevation = 16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = cardColors(
                                containerColor = if (mode == "Anim") Color.Black else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            when (mode) {
                                "Anim" -> {
                                    // ✅ Animation mode with WebView
                                    AndroidView(
                                        modifier = Modifier.fillMaxSize(),
                                        factory = { context ->
                                            WebView(context).apply {
                                                settings.javaScriptEnabled = true
                                                settings.domStorageEnabled = true
                                                settings.allowFileAccess = true
                                                settings.allowContentAccess = true

                                                if (receivedData.isNotEmpty()) {
                                                    post {
                                                        loadDataWithBaseURL(
                                                            null,
                                                            receivedData,
                                                            "text/html",
                                                            "UTF-8",
                                                            null
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        update = { webView ->
                                            if (receivedData.isNotEmpty()) {
                                                webView.post {
                                                    webView.loadDataWithBaseURL(
                                                        null,
                                                        receivedData,
                                                        "text/html",
                                                        "UTF-8",
                                                        null
                                                    )
                                                }
                                            }
                                        }
                                    )
                                }
                                "Camera" -> {
                                    // ✅ Camera mode with global voice integration
                                    CameraPreview(
                                        onAnalysisResult = { result ->
                                            explanationText = result
                                        },
                                        isRecordingActive = isListening,
                                        voiceText = recognizedText,
                                        isSpeechActive = isListening
                                    )
                                }
                                "WebBrowser" -> {
                                    BrowserSearchScreen()
                                }
                                else -> {
                                    // ✅ Canvas mode
                                    DrawCanvas(modifier = Modifier.fillMaxSize())
                                }
                            }
                        }

                        // ✅ Draggable divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .pointerInput(containerHeight) {
                                    detectVerticalDragGestures { _, dragAmount ->
                                        if (containerHeight > 0) {
                                            val fractionChange = dragAmount / containerHeight
                                            topWeight = (topWeight + fractionChange).coerceIn(0.2f, 0.8f)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(64.dp)
                                    .height(6.dp)
                                    .background(
                                        color = Color.Gray.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(3.dp)
                                    )
                            )
                        }

                        // ✅ Bottom card with enhanced TTS integration
                        Card(
                            modifier = Modifier
                                .weight(1f - topWeight)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            elevation = cardElevation(defaultElevation = 16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // ✅ Enhanced auto-speech status indicator
                                AnimatedVisibility(
                                    visible = ttsState == TTSState.SPEAKING,
                                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            )
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // ✅ Animated speaker icon with pulsing effect
                                        val infiniteTransition = rememberInfiniteTransition(label = "speakerPulse")
                                        val speakerAlpha by infiniteTransition.animateFloat(
                                            initialValue = 0.5f,
                                            targetValue = 1f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(600),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "speakerAlpha"
                                        )

                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Speaking",
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = speakerAlpha),
                                            modifier = Modifier.size(16.dp)
                                        )

                                        Text(
                                            text = "🔊 Speaking explanation...",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Spacer(modifier = Modifier.weight(1f))

                                        // ✅ Stop speaking button
                                        IconButton(
                                            onClick = { ttsViewModel.stopSpeaking() },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Create,
                                                contentDescription = "Stop Speaking",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                // ✅ Enhanced text content area
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                        .verticalScroll(rememberScrollState()),
                                    contentAlignment = Alignment.TopStart
                                ) {
                                    if (explanationText.isNotEmpty()) {
                                        SelectionContainer {
                                            Text(
                                                text = explanationText,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    lineHeight = 24.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(32.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "Waiting for data...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                // ✅ Enhanced manual TTS controls
                                AnimatedVisibility(
                                    visible = explanationText.isNotEmpty() && ttsState != TTSState.SPEAKING,
                                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                                ) {
                                    ManualTTSControls(
                                        text = explanationText,
                                        ttsViewModel = ttsViewModel,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ✅ Enhanced ChatInputBar with all voice parameters
                    ChatInputBar(
                        webSocketRef = webSocketRef,
                        onModeToggle = {
                            mode = if (mode == "Anim") "Camera" else "Anim"
                        },
                        // ✅ Complete voice integration
                        isListening = isListening,
                        recognizedText = recognizedText,
                        networkAvailable = networkAvailable,
                        startVoice = startVoice,
                        stopVoice = stopVoice,
                        restartSession = restartSession,
                        onClearText = clearText,
                        isCameraMode = mode == "Camera"
                    )
                }
            }
        }
    }
}

//// ✅ Enhanced Manual TTS Controls Component
//@Composable
//private fun ManualTTSControls(
//    text: String,
//    ttsViewModel: AzureTTSViewModel,
//    modifier: Modifier = Modifier
//) {
//    val ttsState by ttsViewModel.ttsState.collectAsState()
//
//    Card(
//        modifier = modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
//        ),
//        shape = RoundedCornerShape(12.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(12.dp),
//            horizontalArrangement = Arrangement.SpaceEvenly,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // ✅ Play/Pause button
//            IconButton(
//                onClick = {
//                    when (ttsState) {
//                        TTSState.IDLE -> ttsViewModel.speakText(text)
//                        TTSState.SPEAKING -> ttsViewModel.pauseSpeaking()
//                        TTSState.PAUSED -> ttsViewModel.resumeSpeaking()
//                        else -> ttsViewModel.speakText(text)
//                    }
//                },
//                modifier = Modifier
//                    .size(40.dp)
//                    .background(
//                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
//                        CircleShape
//                    )
//            ) {
//                Icon(
//                    imageVector = when (ttsState) {
//                        TTSState.SPEAKING -> Icons.Default.Pause
//                        TTSState.PAUSED -> Icons.Default.PlayArrow
//                        else -> Icons.Default.PlayArrow
//                    },
//                    contentDescription = when (ttsState) {
//                        TTSState.SPEAKING -> "Pause"
//                        TTSState.PAUSED -> "Resume"
//                        else -> "Play"
//                    },
//                    tint = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier.size(20.dp)
//                )
//            }
//
//            // ✅ Stop button
//            IconButton(
//                onClick = { ttsViewModel.stopSpeaking() },
//                enabled = ttsState == TTSState.SPEAKING || ttsState == TTSState.PAUSED,
//                modifier = Modifier
//                    .size(40.dp)
//                    .background(
//                        if (ttsState == TTSState.SPEAKING || ttsState == TTSState.PAUSED)
//                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
//                        else
//                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
//                        CircleShape
//                    )
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Stop,
//                    contentDescription = "Stop",
//                    tint = if (ttsState == TTSState.SPEAKING || ttsState == TTSState.PAUSED)
//                        MaterialTheme.colorScheme.error
//                    else
//                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
//                    modifier = Modifier.size(20.dp)
//                )
//            }
//
//            // ✅ TTS status text
//            Text(
//                text = when (ttsState) {
//                    TTSState.IDLE -> "Ready to speak"
//                    TTSState.INITIALIZING -> "Initializing..."
//                    TTSState.SPEAKING -> "Speaking..."
//                    TTSState.PAUSED -> "Paused"
//                    TTSState.ERROR -> "Error occurred"
//                },
//                style = MaterialTheme.typography.labelMedium,
//                color = when (ttsState) {
//                    TTSState.ERROR -> MaterialTheme.colorScheme.error
//                    TTSState.SPEAKING -> MaterialTheme.colorScheme.primary
//                    else -> MaterialTheme.colorScheme.onSurfaceVariant
//                },
//                modifier = Modifier.weight(1f),
//                textAlign = TextAlign.Center
//            )
//        }
//    }
//}

// Manual TTS Controls Component
@Composable
fun ManualTTSControls(
    text: String,
    ttsViewModel: AzureTTSViewModel,
    modifier: Modifier = Modifier
) {
    val ttsState by ttsViewModel.ttsState.collectAsState()
    val errorMessage by ttsViewModel.errorMessage.collectAsState()

    var showSettings by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Main control row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Replay button
                Button(
                    onClick = { ttsViewModel.speakText(text) },
                    enabled = text.isNotBlank() && ttsState != TTSState.SPEAKING,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Replay",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Settings toggle
                TextButton(
                    onClick = { showSettings = !showSettings },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Error message
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(4.dp)
                )
            }

            // Settings panel
            if (showSettings) {
                val speechRate by ttsViewModel.speechRate.collectAsState()
                val volume by ttsViewModel.volume.collectAsState()

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    // Speech rate
                    Text(
                        text = "Speed: ${String.format("%.1fx", speechRate)}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(
                        value = speechRate,
                        onValueChange = { ttsViewModel.setSpeechRate(it) },
                        valueRange = 0.5f..2.0f,
                        steps = 15,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Volume
                    Text(
                        text = "Volume: ${(volume * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(
                        value = volume,
                        onValueChange = { ttsViewModel.setVolume(it) },
                        valueRange = 0.0f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserSearchScreen() {
    // State to hold the search query.
    var searchQuery by remember { mutableStateOf("") }
    // Reference to the WebView instance.
    var webViewInstance: WebView? by remember { mutableStateOf(null) }
    // Track the current URL loaded in the WebView.
    var currentUrl by remember { mutableStateOf("") }
    // Store the snapshot bitmap when captured.
    var snapshotBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search input field.
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        // Row for Search and Snapshot buttons.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    // Launch a search in the WebView using Google search.
                    val encodedQuery = Uri.encode(searchQuery)
                    val searchUrl = "https://www.google.com/search?q=$encodedQuery"
                    webViewInstance?.loadUrl(searchUrl)
                }
            ) {
                Text("Search")
            }
            Button(
                onClick = {
                    // Capture snapshot of the current WebView.
                    webViewInstance?.let { webView ->
                        snapshotBitmap = captureWebViewScreenshot(webView)
                        Log.d("WebView", "Snapshot captured of size: ${snapshotBitmap?.width}x${snapshotBitmap?.height}")
                    }
                }
            ) {
                Text("Take Snapshot")
            }
        }
        // WebView container.
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    // Override WebViewClient to track page loads.
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            currentUrl = url ?: ""
                            Log.d("WebView", "Page started loading: $currentUrl")
                        }
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            currentUrl = url ?: ""
                            Log.d("WebView", "Page finished loading: $currentUrl")
                        }
                    }
                    // Optionally, load a default page.
                    loadUrl("https://www.google.com")
                    // Save this instance so that we can access it later.
                    webViewInstance = this
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        // Display the captured snapshot (if any).
        snapshotBitmap?.let { bitmap ->
            Text(
                text = "Snapshot:",
                modifier = Modifier.padding(16.dp)
            )
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "WebView Snapshot",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp)
            )
        }
    }
}

/**
 * Captures a snapshot of the given WebView.
 */
fun captureWebViewScreenshot(webView: WebView): Bitmap? {
    if (webView.width == 0 || webView.height == 0) return null
    val bitmap = Bitmap.createBitmap(webView.width, webView.height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    webView.draw(canvas)
    return bitmap
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    onAnalysisResult: (String) -> Unit = {},
    isRecordingActive: Boolean = false, // From global voice manager
    voiceText: String = "", // From global voice manager
    isSpeechActive: Boolean = false // From global voice manager
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Enhanced state management for streaming analysis
    var isRecording by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Streaming analysis states
    var streamingResults by remember { mutableStateOf(mutableListOf<String>()) }
    var activeAnalysisCount by remember { mutableStateOf(0) }
    var totalFramesProcessed by remember { mutableStateOf(0) }
    var currentStreamingText by remember { mutableStateOf("") }

    // Speech session management - now using global voice manager
    var isSpeechSessionActive by remember { mutableStateOf(false) }

    // Multiple API endpoints for load balancing
    val apiConfigs = listOf(
        APIConfig("gpt-4o-mini", "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", false),
        APIConfig("gpt-4o", "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", false),
    )

    // Track API usage
    val apiUsageTracker = remember { mutableStateMapOf(*apiConfigs.map { it.model to false }.toTypedArray()) }

    // Enhanced ImageCapture for continuous streaming
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    // Streaming capture job
    val streamingCaptureJob = remember { mutableStateOf<Job?>(null) }

    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    // Enhanced speech monitoring with STREAMING analysis
    LaunchedEffect(isSpeechActive, isRecording, voiceText) {
        if (isRecording) {
            if (isSpeechActive && !isSpeechSessionActive) {
                // Speech started - begin streaming analysis
                Log.d("StreamingAnalysis", "Speech started - beginning streaming frame analysis")
                isSpeechSessionActive = true
                streamingResults.clear()
                totalFramesProcessed = 0
                activeAnalysisCount = 0
                currentStreamingText = "Starting live analysis..."

                // Start continuous frame capture and analysis
                streamingCaptureJob.value = MainScope().launch {
                    while (isActive && isSpeechSessionActive && isSpeechActive) {
                        captureAndAnalyzeFrame(
                            imageCapture = imageCapture,
                            context = context,
                            voiceText = voiceText,
                            apiConfigs = apiConfigs,
                            apiUsageTracker = apiUsageTracker,
                            totalFramesProcessed = totalFramesProcessed,
                            onAnalysisStart = { activeAnalysisCount++ },
                            onAnalysisComplete = { result ->
                                streamingResults.add(result)
                                activeAnalysisCount--
                                totalFramesProcessed++
                                currentStreamingText = "Frame ${streamingResults.size}: ${streamingResults.last().take(100)}${if (streamingResults.last().length > 100) "..." else ""}"
                            },
                            onAnalysisError = { error ->
                                activeAnalysisCount--
                                Log.e("StreamingAnalysis", "Frame analysis error: $error")
                            }
                        )
                        delay(800L) // Capture and analyze every 800ms
                    }
                }

            } else if (!isSpeechActive && isSpeechSessionActive) {
                // Speech ended - stop streaming and create final summary
                Log.d("StreamingAnalysis", "Speech ended - creating final summary from ${streamingResults.size} frame analyses")
                streamingCaptureJob.value?.cancel()
                isSpeechSessionActive = false

                if (streamingResults.isNotEmpty() && voiceText.isNotEmpty()) {
                    // Create comprehensive summary from all streaming results
                    currentStreamingText = "Creating final summary..."
                    performFinalSummaryAnalysis(
                        streamingResults = streamingResults.toList(),
                        voicePrompt = voiceText,
                        apiConfigs = apiConfigs,
                        onSummaryComplete = { summary ->
                            onAnalysisResult(summary)
                            currentStreamingText = "Analysis complete ✅"
                        },
                        onSummaryError = { error ->
                            errorMessage = "Summary error: $error"
                            currentStreamingText = "Summary failed ❌"
                        }
                    )
                }
            }
        }
    }

    // Monitor recording state
    LaunchedEffect(isRecordingActive) {
        if (isRecordingActive && !isRecording) {
            isRecording = true
            errorMessage = null
            streamingResults.clear()
            currentStreamingText = ""
            Log.d("StreamingAnalysis", "Streaming recording session started")
        } else if (!isRecordingActive && isRecording) {
            streamingCaptureJob.value?.cancel()
            isRecording = false
            isSpeechSessionActive = false
            totalFramesProcessed = 0
            activeAnalysisCount = 0
            Log.d("StreamingAnalysis", "Streaming recording session stopped")
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            streamingCaptureJob.value?.cancel()
        }
    }

    if (cameraPermissionState.status.isGranted) {
        Column(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            ) {
                // Enhanced PreviewView
                val previewView = remember { PreviewView(context) }

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        previewView.apply {
                            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder()
                                .setTargetResolution(android.util.Size(1280, 720))
                                .build()
                                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageCapture
                                )
                            } catch (exc: Exception) {
                                Log.e("CameraPreview", "Camera binding failed", exc)
                                errorMessage = "Failed to bind camera: ${exc.message}"
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    }
                )

                // Enhanced streaming indicators
                if (isRecording) {
                    // Main streaming status
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isSpeechSessionActive) {
                            // Pulsing indicator when streaming
                            val infiniteTransition = rememberInfiniteTransition()
                            val pulseAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.5f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(500),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color.White.copy(alpha = pulseAlpha), CircleShape)
                            )
                            Text(
                                text = if (activeAnalysisCount > 0) "STREAMING LIVE" else "READY TO STREAM",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color.White, CircleShape)
                            )
                            Text(
                                text = "WAITING",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // Streaming analysis status
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                activeAnalysisCount > 0 -> MaterialTheme.colorScheme.primary
                                isSpeechSessionActive -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = when {
                                    activeAnalysisCount > 0 -> "🔄 Analyzing frame ${totalFramesProcessed + 1}..."
                                    isSpeechSessionActive -> "🎤 Streaming analysis active"
                                    else -> "🎤 Say something for live analysis"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = if (activeAnalysisCount > 0 || isSpeechSessionActive) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (voiceText.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"$voiceText\"",
                                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                    color = if (activeAnalysisCount > 0 || isSpeechSessionActive)
                                        Color.White.copy(alpha = 0.9f)
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Live stats
                            if (isSpeechSessionActive || totalFramesProcessed > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📊 Frames: $totalFramesProcessed | Active: $activeAnalysisCount | Results: ${streamingResults.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (activeAnalysisCount > 0 || isSpeechSessionActive)
                                        Color.White.copy(alpha = 0.8f)
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Streaming results display
                if (currentStreamingText.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth(0.95f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "⚡ Live Analysis",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (activeAnalysisCount > 0) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "processing $activeAnalysisCount frames...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                currentStreamingText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Error display
                errorMessage?.let { message ->
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { errorMessage = null }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Grant Camera Permission")
            }
        }
    }
}

// Data class for API configuration
data class APIConfig(
    val model: String,
    val apiKey: String,
    var isBusy: Boolean = false
)

// STREAMING frame capture and analysis
suspend fun captureAndAnalyzeFrame(
    imageCapture: ImageCapture,
    context: Context,
    voiceText: String,
    apiConfigs: List<APIConfig>,
    apiUsageTracker: MutableMap<String, Boolean>,
    totalFramesProcessed: Int,
    onAnalysisStart: () -> Unit,
    onAnalysisComplete: (String) -> Unit,
    onAnalysisError: (String) -> Unit
) {
    try {
        suspendCancellableCoroutine<Unit> { continuation ->
            val mainExecutor = ContextCompat.getMainExecutor(context)
            imageCapture.takePicture(
                mainExecutor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        try {
                            val buffer = image.planes[0].buffer
                            val bytes = ByteArray(buffer.remaining())
                            buffer.get(bytes)
                            val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                            // Start analysis for this frame
                            performStreamingAnalysis(
                                frame = bitmap,
                                voicePrompt = voiceText,
                                apiConfigs = apiConfigs,
                                apiUsageTracker = apiUsageTracker,
                                frameNumber = totalFramesProcessed + 1,
                                onAnalysisStart = onAnalysisStart,
                                onAnalysisComplete = onAnalysisComplete,
                                onAnalysisError = onAnalysisError
                            )

                            image.close()
                            if (continuation.isActive) continuation.resume(Unit) {}
                        } catch (e: Exception) {
                            Log.e("CameraPreview", "Error processing streaming frame", e)
                            if (continuation.isActive) continuation.resume(Unit) {}
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CameraPreview", "Streaming capture failed", exception)
                        if (continuation.isActive) continuation.resume(Unit) {}
                    }
                }
            )
        }
    } catch (e: Exception) {
        Log.e("CameraPreview", "Error in streaming capture", e)
    }
}

// STREAMING Analysis function - analyzes individual frames in real-time
private fun performStreamingAnalysis(
    frame: Bitmap,
    voicePrompt: String,
    apiConfigs: List<APIConfig>,
    apiUsageTracker: MutableMap<String, Boolean>,
    frameNumber: Int,
    onAnalysisStart: () -> Unit,
    onAnalysisComplete: (String) -> Unit,
    onAnalysisError: (String) -> Unit
) {
    // Find available API endpoint
    val availableAPI = apiConfigs.find { !apiUsageTracker[it.model]!! }

    if (availableAPI == null) {
        onAnalysisError("All APIs busy, skipping frame $frameNumber")
        return
    }

    MainScope().launch {
        try {
            onAnalysisStart()
            apiUsageTracker[availableAPI.model] = true // Mark as busy

            // Quick resize for streaming (smaller for speed)
            val resizedBitmap = resizeBitmapForAnalysis(frame, maxDimension = 384)
            val byteArrayOutputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream) // Lower quality for speed
            val base64Image = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)

            val client = OkHttpClient.Builder()
                .connectTimeout(8, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(12, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(8, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val jsonObject = JSONObject().apply {
                put("model", availableAPI.model)
                val messagesArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        val contentArray = JSONArray().apply {
                            val promptText = """
                                Frame $frameNumber analysis for: "$voicePrompt"
                                
                                Provide a brief 1-sentence observation of what's most relevant in this frame. Focus on key educational elements, text, or demonstrations visible.
                            """.trimIndent()

                            put(JSONObject().apply {
                                put("type", "text")
                                put("text", promptText)
                            })

                            put(JSONObject().apply {
                                put("type", "image_url")
                                put("image_url", JSONObject().apply {
                                    put("url", "data:image/jpeg;base64,$base64Image")
                                })
                            })
                        }
                        put("content", contentArray)
                    })
                }
                put("messages", messagesArray)
                put("max_tokens", 100) // Very short responses for speed
                put("temperature", 0.1)
            }

            val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer ${availableAPI.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(responseBody)
                    val choices = jsonResponse.getJSONArray("choices")
                    val firstChoice = choices.getJSONObject(0)
                    val message = firstChoice.getJSONObject("message")
                    val content = message.getString("content")

                    onAnalysisComplete("Frame $frameNumber: $content")
                } else {
                    throw IOException("Frame $frameNumber analysis failed: ${response.code}")
                }
            }
        } catch (e: Exception) {
            Log.e("StreamingAnalysis", "Frame $frameNumber analysis error", e)
            onAnalysisError("Frame $frameNumber: ${e.message}")
        } finally {
            apiUsageTracker[availableAPI.model] = false // Mark as available
        }
    }
}

// FINAL Summary Analysis - combines all streaming results
private fun performFinalSummaryAnalysis(
    streamingResults: List<String>,
    voicePrompt: String,
    apiConfigs: List<APIConfig>,
    onSummaryComplete: (String) -> Unit,
    onSummaryError: (String) -> Unit
): Job {
    return MainScope().launch {
        try {
            // Use the most capable model for final summary
            val summaryAPI = apiConfigs.find { it.model == "gpt-4o" } ?: apiConfigs.first()

            val client = OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val combinedFrameData = streamingResults.joinToString("\n")

            val jsonObject = JSONObject().apply {
                put("model", summaryAPI.model)
                val messagesArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", """
                            User asked: "$voicePrompt"
                            
                            I captured ${streamingResults.size} frames during their speech and got these real-time analyses:
                            
                            $combinedFrameData
                            
                            Please provide a comprehensive, educational summary that:
                            1. Synthesizes all frame observations into a coherent explanation
                            2. Directly addresses their question: "$voicePrompt"
                            3. Provides detailed educational context and explanations
                            4. Highlights key concepts, formulas, or demonstrations that were visible
                            5. Gives step-by-step explanations where relevant
                            
                            Make it thorough and educational.
                        """.trimIndent())
                    })
                }
                put("messages", messagesArray)
                put("max_tokens", 1000)
                put("temperature", 0.3)
            }

            val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer ${summaryAPI.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(responseBody)
                    val choices = jsonResponse.getJSONArray("choices")
                    val firstChoice = choices.getJSONObject(0)
                    val message = firstChoice.getJSONObject("message")
                    val content = message.getString("content")

                    onSummaryComplete(content)
                } else {
                    throw IOException("Summary analysis failed: ${response.code}")
                }
            }
        } catch (e: Exception) {
            Log.e("SummaryAnalysis", "Final summary failed", e)
            onSummaryError(e.message ?: "Unknown summary error")
        }
    }
}

// Optimized bitmap resizing
private fun resizeBitmapForAnalysis(originalBitmap: Bitmap, maxDimension: Int = 768): Bitmap {
    val originalWidth = originalBitmap.width
    val originalHeight = originalBitmap.height

    val scale = when {
        originalWidth > originalHeight -> maxDimension.toFloat() / originalWidth
        else -> maxDimension.toFloat() / originalHeight
    }

    val newWidth = (originalWidth * scale).toInt()
    val newHeight = (originalHeight * scale).toInt()

    return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
}

@Composable
fun DrawCanvas(modifier: Modifier = Modifier) {
    var currentPath by remember { mutableStateOf(Path()) }
    var paths by remember { mutableStateOf(listOf<Path>()) }

    Canvas(
        modifier = modifier
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset: Offset ->
                        currentPath = Path().apply { moveTo(offset.x, offset.y) }
                    },
                    onDrag = { change: PointerInputChange, dragAmount: Offset ->
                        currentPath.lineTo(change.position.x, change.position.y)
                    },
                    onDragEnd = {
                        paths = paths + currentPath
                    }
                )
            }
    ) {
        // Draw all saved paths.
        paths.forEach { path ->
            drawPath(
                path = path,
                color = Color.Blue,
                style = Stroke(width = 4f)
            )
        }
        // Draw the current path.
        drawPath(
            path = currentPath,
            color = Color.Blue,
            style = Stroke(width = 4f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CascadingSubjectUnitTopicList(
    webSocketRef: MutableState<WebSocket?>,
    subjectListViewModel: SubjectlistViewModel = viewModel(
        factory = SubjectlistViewModelFactory(
            SubjectlistRepository(RetrofitClient.apiService)
        )
    ),
    onSelection: (selections: Map<String, Map<String, Map<String, List<String>>>>, Any?) -> Unit
) {
    val userInfoViewModel: UserInfoViewModel = viewModel()

    // Get the context
    val context = LocalContext.current

    // Observe the userId
    val userId by userInfoViewModel.userId.collectAsState()

    // Load the userId when the composable is first created
    LaunchedEffect(Unit) {
        userInfoViewModel.loadUserId(context)
    }

    // Fetch subject list when userId changes
    LaunchedEffect(userId) {
        if (userId != NeuroEdApp.INVALID_USER_ID) {
            subjectListViewModel.fetchSubjectList(userId) // Pass the userId
        }
    }
    // Observe subjects.
    val subjects by subjectListViewModel.subjectList.collectAsState(initial = emptyList())

    // Local state holders.
    val expandedSubjects = remember { mutableStateListOf<Int>() }
    val expandedUnitsMap = remember { mutableStateMapOf<Int, SnapshotStateList<Int>>() }

    // Instead of storing only IDs, we now store the full selected objects.
    // For the unit selection, we store one unit per subject.
    val selectedUnitDataMap: SnapshotStateMap<Int, SubjectSyllabusGetResponse> =
        remember { mutableStateMapOf() }
    // For topics, we allow multiple selections per unit.
    val selectedTopicDataMap: SnapshotStateMap<Int, SnapshotStateList<SubjectSyllabusHeadingTopic>> =
        remember { mutableStateMapOf() }
    // For subtopics, keyed by topic id, we allow multiple subtopics per topic.
    val selectedSubtopicsDataMap: SnapshotStateMap<Int, SnapshotStateList<SubjectSyllabusHeadingTopicSubtopic>> =
        remember { mutableStateMapOf() }

    val scrollState = rememberScrollState()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Select Subject",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        subjects.forEach { subject ->
            Card(
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        // When a new subject is selected, clear all previous selections.
                        expandedSubjects.clear()
                        expandedUnitsMap.clear()
                        selectedUnitDataMap.clear()
                        selectedTopicDataMap.clear()
                        selectedSubtopicsDataMap.clear()
                        expandedSubjects.add(subject.id)
                    }
                    .animateContentSize(animationSpec = tween(300))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = subject.subject,
                        style = MaterialTheme.typography.titleMedium
                    )
                    AnimatedVisibility(
                        visible = expandedSubjects.contains(subject.id),
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
                        // Each subject loads its units (syllabus).
                        val syllabusViewModel: SubjectSyllabusViewModel = viewModel(
                            key = "syllabus_${subject.id}",
                            factory = SubjectSyllabusViewModelFactory(
                                repository = SubjectSyllabusGetRepository(RetrofitClient.apiService),
                                subjectId = subject.id
                            )
                        )
                        val syllabusList by syllabusViewModel.subjectSyllabus.observeAsState(emptyList())

                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Select Unit", style = MaterialTheme.typography.titleSmall)

                            syllabusList.forEach { syllabus ->
                                // When selecting a unit, store the full Syllabus object.
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .clickable {
                                            // Toggle unit expansion.
                                            val subjectUnits = expandedUnitsMap.getOrPut(subject.id) {
                                                mutableStateListOf()
                                            }
                                            if (subjectUnits.contains(syllabus.id)) {
                                                subjectUnits.remove(syllabus.id)
                                                selectedUnitDataMap.remove(subject.id)
                                            } else {
                                                subjectUnits.add(syllabus.id)
                                                selectedUnitDataMap[subject.id] = syllabus
                                            }
                                        }
                                        .animateContentSize(animationSpec = tween(300))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(
                                            text = "Unit: ${syllabus.syllabusChapterName}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = if (expandedUnitsMap[subject.id]?.contains(syllabus.id) == true)
                                                "Collapse" else "Expand"
                                        )
                                    }
                                }
                                AnimatedVisibility(
                                    visible = expandedUnitsMap[subject.id]?.contains(syllabus.id) == true,
                                    enter = fadeIn(animationSpec = tween(300)),
                                    exit = fadeOut(animationSpec = tween(300))
                                ) {
                                    // Load headings for the selected unit.
                                    val headingViewModel: SubjectSyllabusHeadingViewModel = viewModel(
                                        key = "heading_${syllabus.id}",
                                        factory = SubjectSyllabusHeadingViewModelFactory(
                                            repository = SubjectSyllabusHeadingRepository(RetrofitClient.apiService),
                                            syllabus_id = syllabus.id
                                        )
                                    )
                                    val headings by headingViewModel.subjectSyllabusHeading.observeAsState(emptyList())
                                    Text(
                                        text = "Headings count: ${headings.size}",
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    // If there is at least one heading, automatically pick the first heading.
                                    val topics = remember { mutableStateListOf<SubjectSyllabusHeadingTopic>() }
                                    if (headings.isNotEmpty()) {
                                        val firstHeading = headings.first()
                                        val topicViewModel: SubjectSyllabusHeadingTopicViewModel = viewModel(
                                            key = "topic_${firstHeading.id}",
                                            factory = SubjectSyllabusHeadingTopicViewModelFactory(
                                                repository = SubjectSyllabusHeadingTopicRepository(RetrofitClient.apiService),
                                                title_id = firstHeading.id
                                            )
                                        )
                                        val fetchedTopics by topicViewModel.subjectsyllabusheadingTopic.observeAsState(emptyList())
                                        topics.clear()
                                        topics.addAll(fetchedTopics)
                                    }

                                    Column(modifier = Modifier.padding(start = 16.dp)) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Select Topic(s)", style = MaterialTheme.typography.bodySmall)
                                        topics.forEach { topic ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        // Toggle topic selection.
                                                        val topicsForUnit = selectedTopicDataMap.getOrPut(syllabus.id) {
                                                            mutableStateListOf()
                                                        }
                                                        if (topicsForUnit.contains(topic)) {
                                                            topicsForUnit.remove(topic)
                                                        } else {
                                                            topicsForUnit.add(topic)
                                                        }
                                                    }
                                                    .padding(vertical = 6.dp)
                                            ) {
                                                Checkbox(
                                                    checked = selectedTopicDataMap[syllabus.id]?.contains(topic)
                                                        ?: false,
                                                    onCheckedChange = { checked ->
                                                        val topicsForUnit = selectedTopicDataMap.getOrPut(syllabus.id) {
                                                            mutableStateListOf()
                                                        }
                                                        if (checked)
                                                            topicsForUnit.add(topic)
                                                        else
                                                            topicsForUnit.remove(topic)
                                                    }
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(topic.topic, style = MaterialTheme.typography.bodyLarge)
                                            }
                                            // If this topic is selected, show its subtopics.
                                            if (selectedTopicDataMap[syllabus.id]?.contains(topic) == true) {
                                                // Load subtopics for the selected topic.
                                                val subtopicViewModel: SubjectSyllabusHeadingTopicSubtopicViewModel = viewModel(
                                                    key = "subtopic_${topic.id}",
                                                    factory = SubjectSyllabusHeadingTopicSubtopicViewModelFactory(
                                                        repository = SubjectSyllabusHeadingSubtopicRepository(RetrofitClient.apiService),
                                                        topic_id = topic.id
                                                    )
                                                )
                                                val subtopics by subtopicViewModel.subjectsyllabusheadingTopicSubtopic.observeAsState(emptyList())
                                                AnimatedVisibility(
                                                    visible = subtopics.isNotEmpty(),
                                                    enter = fadeIn(animationSpec = tween(300)),
                                                    exit = fadeOut(animationSpec = tween(300))
                                                ) {
                                                    Column(modifier = Modifier.padding(start = 16.dp)) {
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text("Select Subtopic(s)", style = MaterialTheme.typography.bodySmall)
                                                        subtopics.forEach { subtopic ->
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .clickable {
                                                                        val selectedSubs = selectedSubtopicsDataMap.getOrPut(topic.id) {
                                                                            mutableStateListOf()
                                                                        }
                                                                        if (selectedSubs.contains(subtopic))
                                                                            selectedSubs.remove(subtopic)
                                                                        else
                                                                            selectedSubs.add(subtopic)
                                                                    }
                                                                    .padding(vertical = 4.dp)
                                                            ) {
                                                                Checkbox(
                                                                    checked = selectedSubtopicsDataMap[topic.id]?.contains(subtopic)
                                                                        ?: false,
                                                                    onCheckedChange = { checked ->
                                                                        val selectedSubs = selectedSubtopicsDataMap.getOrPut(topic.id) {
                                                                            mutableStateListOf()
                                                                        }
                                                                        if (checked)
                                                                            selectedSubs.add(subtopic)
                                                                        else
                                                                            selectedSubs.remove(subtopic)
                                                                    }
                                                                )
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                Text(subtopic.subtopic, style = MaterialTheme.typography.bodySmall)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                // Build a nested map using descriptive names.
                val finalSelections: MutableMap<String, Map<String, Map<String, List<String>>>> = mutableMapOf()
                subjects.forEach { subject ->
                    // Get the selected unit for the subject.
                    selectedUnitDataMap[subject.id]?.let { unit ->
                        val unitName = unit.syllabusChapterName
                        // Get the list of selected topics for the unit.
                        val topicsForUnit = selectedTopicDataMap[unit.id] ?: mutableStateListOf()
                        // Build a map for topics and their subtopics.
                        val topicMap: MutableMap<String, List<String>> = mutableMapOf()
                        topicsForUnit.forEach { topic ->
                            val topicName = topic.topic
                            // Get subtopics for each topic.
                            val subtopicNames = selectedSubtopicsDataMap[topic.id]?.map { it.subtopic } ?: emptyList()
                            topicMap["Topic: $topicName"] = subtopicNames
                        }
                        val unitMap = mapOf("Unit: $unitName" to topicMap)
                        finalSelections[subject.subject] = unitMap
                    }
                }

                val finalData = mapOf(
                    "selections" to finalSelections,
                    "screenWidth" to screenWidth,
                    "screenHeight" to screenHeight
                )

                if (finalSelections.isNotEmpty()) {
                    onSelection(finalSelections, null)
                    val gson = Gson()
                    val jsonSelections = gson.toJson(finalData)
                    Log.d("WebSocket", "Sending selection: $jsonSelections")
                    webSocketRef.value?.send(jsonSelections)
                }
            },
            enabled = selectedUnitDataMap.isNotEmpty() && selectedTopicDataMap.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirm Selection", style = MaterialTheme.typography.bodyLarge)
        }
    }
}









// ✅ FIXED ChatInputBar for PlaygroundScreen - exactly same pattern as BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ChatInputBar(
    webSocketRef: MutableState<WebSocket?>,
    onModeToggle: () -> Unit,
    // ✅ Enhanced parameters from VoiceEnabledScreen
    isListening: Boolean = false,
    recognizedText: String = "",
    networkAvailable: Boolean = true,
    startVoice: () -> Unit = {},
    stopVoice: () -> Unit = {},
    restartSession: () -> Unit = {},
    onClearText: () -> Unit = {}, // ✅ Proper text clearing callback
    isCameraMode: Boolean = false
) {
    var message by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // ✅ State management
    var showNetworkError by remember { mutableStateOf(false) }
    var lastVoiceText by remember { mutableStateOf("") }
    var sessionStartTime by remember { mutableLongStateOf(0L) }
    var sessionDuration by remember { mutableLongStateOf(0L) }

    // ✅ Permission handling
    val recordAudioPermission = rememberPermissionState(
        permission = Manifest.permission.RECORD_AUDIO
    )

    // ✅ File and UI state
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var showSubjectSheet by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var currentLearnOption by remember { mutableStateOf("Learn") }

    // ✅ Animation and UI calculations
    val borderColor by animateColorAsState(
        targetValue = if (message.text.isEmpty())
            MaterialTheme.colorScheme.outline
        else
            MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 300),
        label = "borderColor"
    )

    val density = LocalDensity.current
    val keyboardHeight = WindowInsets.ime.getBottom(density)
    val isKeyboardOpen = keyboardHeight > 0

    // ✅ File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedFileUri = it }
    }

    // ✅ Load vector resources safely
    val micOn = ImageVector.vectorResource(id = R.drawable.baseline_mic_24)
    val micOff = ImageVector.vectorResource(id = R.drawable.baseline_mic_off_24)
    val camera = ImageVector.vectorResource(id = R.drawable.baseline_photo_camera_24)
    val book = ImageVector.vectorResource(id = R.drawable.baseline_book_24)
    val file = ImageVector.vectorResource(id = R.drawable.baseline_attach_file_24)

    // ✅ Debug logging with throttling
    LaunchedEffect(isListening, recognizedText) {
        Log.d("ChatInputBar", "🔄 State update - isListening: $isListening, text: '${recognizedText.take(30)}${if(recognizedText.length > 30) "..." else ""}'")
    }

    // ✅ Network status monitoring
    LaunchedEffect(networkAvailable) {
        if (!networkAvailable && isListening) {
            showNetworkError = true
            stopVoice()
        } else if (networkAvailable) {
            showNetworkError = false
        }
    }

    // ✅ Enhanced voice text update with proper synchronization
    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotEmpty() &&
            recognizedText != lastVoiceText &&
            recognizedText != message.text) {

            Log.d("ChatInputBar", "🎤 Updating text field with voice: $recognizedText")
            lastVoiceText = recognizedText

            message = TextFieldValue(
                text = recognizedText,
                selection = androidx.compose.ui.text.TextRange(recognizedText.length)
            )
        }
    }

    // ✅ Keyboard interaction handling
    LaunchedEffect(isKeyboardOpen) {
        if (isKeyboardOpen && isListening) {
            Log.d("ChatInputBar", "⌨️ Keyboard opened, stopping voice")
            stopVoice()
        }
    }

    // ✅ Session duration tracking with proper cleanup
    LaunchedEffect(isListening) {
        if (isListening) {
            sessionStartTime = System.currentTimeMillis()
            val job = launch {
                while (isActive && isListening) {
                    delay(1000)
                    sessionDuration = System.currentTimeMillis() - sessionStartTime
                }
            }

            // Cleanup when listening stops
            try {
                awaitCancellation()
            } finally {
                job.cancel()
                sessionDuration = 0L
            }
        } else {
            sessionDuration = 0L
            sessionStartTime = 0L
        }
    }

    // ✅ Enhanced voice button click handler
    val handleVoiceButtonClick = remember {
        {
            Log.d("ChatInputBar", "🎤 Voice button clicked - State: listening=$isListening, permission=${recordAudioPermission.status.isGranted}, network=$networkAvailable")

            when {
                !recordAudioPermission.status.isGranted -> {
                    Log.d("ChatInputBar", "🔒 Requesting audio permission")
                    recordAudioPermission.launchPermissionRequest()
                }
                !networkAvailable -> {
                    Log.w("ChatInputBar", "📡 No network available")
                    showNetworkError = true
                }
                isListening -> {
                    Log.d("ChatInputBar", "🛑 Stopping voice recognition")
                    stopVoice()
                }
                else -> {
                    Log.d("ChatInputBar", "▶️ Starting voice session")
                    // Clear states before starting
                    message = TextFieldValue("")
                    lastVoiceText = ""
                    focusManager.clearFocus()
                    showNetworkError = false

                    // Clear through proper callback
                    onClearText()
                    startVoice()
                }
            }
        }
    }

    // ✅ Send message function with proper cleanup
    val handleSendMessage = remember {
        {
            if (message.text.isNotEmpty()) {
                val messageToSend = message.text.trim()
                Log.d("ChatInputBar", "📤 Sending message: $messageToSend")

                try {
                    webSocketRef.value?.send(messageToSend)

                    // Clear states after successful send
                    message = TextFieldValue("")
                    lastVoiceText = ""
                    onClearText()
                    focusManager.clearFocus()

                } catch (e: Exception) {
                    Log.e("ChatInputBar", "❌ Error sending message", e)
                    // Could show error toast here
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {

        // ✅ Network error banner
        AnimatedVisibility(
            visible = showNetworkError,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
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
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // ✅ Enhanced text field
        OutlinedTextField(
            value = message,
            onValueChange = { newValue ->
                Log.d("ChatInputBar", "✏️ Text changed: ${newValue.text}")
                message = newValue

                // Clear voice text if user types manually
                if (newValue.text != recognizedText && newValue.text != lastVoiceText) {
                    lastVoiceText = ""
                    if (newValue.text.isNotEmpty()) {
                        onClearText()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp, max = 120.dp)
                .verticalScroll(rememberScrollState())
                .focusRequester(focusRequester)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                ),
            leadingIcon = selectedFileUri?.let {
                {
                    BadgedBox(
                        badge = {
                            Badge(
                                modifier = Modifier.clickable { selectedFileUri = null }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Remove File",
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            },
            placeholder = {
                Text(
                    text = when {
                        !networkAvailable -> "🌐 Network required"
                        isListening -> "🎤 Listening..."
                        recognizedText.isNotEmpty() -> "Voice input received"
                        else -> "Message ChatGPT"
                    },
                    color = when {
                        !networkAvailable -> MaterialTheme.colorScheme.error
                        isListening -> MaterialTheme.colorScheme.primary
                        recognizedText.isNotEmpty() -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = handleSendMessage,
                    enabled = message.text.isNotEmpty() && networkAvailable
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send Message",
                        tint = if (message.text.isNotEmpty() && networkAvailable)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color.Transparent,
                unfocusedBorderColor = borderColor,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ Enhanced action buttons (only when keyboard is closed)
        if (!isKeyboardOpen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ✅ Enhanced Mic Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val micButtonColor by animateColorAsState(
                        targetValue = when {
                            !recordAudioPermission.status.isGranted ->
                                MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                            !networkAvailable ->
                                MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            isListening ->
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else -> Color.Transparent
                        },
                        animationSpec = tween(300),
                        label = "micButtonColor"
                    )

                    IconButton(
                        onClick = handleVoiceButtonClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = micButtonColor,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = when {
                                !recordAudioPermission.status.isGranted || !networkAvailable -> micOff
                                isListening -> micOn
                                else -> micOff
                            },
                            contentDescription = when {
                                !recordAudioPermission.status.isGranted -> "Permission Required"
                                !networkAvailable -> "Network Required"
                                isListening -> "Stop Recording"
                                else -> "Start Recording"
                            },
                            modifier = Modifier.size(20.dp),
                            tint = when {
                                !recordAudioPermission.status.isGranted || !networkAvailable ->
                                    MaterialTheme.colorScheme.error
                                isListening ->
                                    MaterialTheme.colorScheme.primary
                                else ->
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    Text(
                        text = when {
                            !recordAudioPermission.status.isGranted -> "No Perm"
                            !networkAvailable -> "No Net"
                            isListening -> "On"
                            else -> "Off"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            !recordAudioPermission.status.isGranted || !networkAvailable ->
                                MaterialTheme.colorScheme.error
                            isListening ->
                                MaterialTheme.colorScheme.primary
                            else ->
                                MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // ✅ Learn Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Learn",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = currentLearnOption,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // ✅ File Selection Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = file,
                            contentDescription = "Select File",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "File",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // ✅ Camera Toggle Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { onModeToggle() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = camera,
                            contentDescription = "Toggle Camera/Animation",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = if (isCameraMode) "Camera" else "Animation",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // ✅ Subject Selection Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { showSubjectSheet = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = book,
                            contentDescription = "Select Subject/Unit/Topic",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Subject",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // ✅ Enhanced voice status indicator
        AnimatedVisibility(
            visible = isListening,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
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
                    val infiniteTransition = rememberInfiniteTransition(label = "voicePulse")
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
                            .size(10.dp)
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

                        // ✅ Session duration and current text
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (sessionDuration > 0) {
                                val seconds = sessionDuration / 1000
                                Text(
                                    text = "(${seconds}s)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }

                            if (recognizedText.isNotEmpty()) {
                                Text(
                                    text = "\"${recognizedText.take(40)}${if (recognizedText.length > 40) "..." else ""}\"",
                                    style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // ✅ Control buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Restart button
                        IconButton(
                            onClick = {
                                Log.d("ChatInputBar", "🔄 Restart session")
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

                        // Stop button
                        IconButton(
                            onClick = {
                                Log.d("ChatInputBar", "🛑 Stop listening")
                                stopVoice()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Stop Listening",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // ✅ Permission denied message
        AnimatedVisibility(
            visible = !recordAudioPermission.status.isGranted &&
                    recordAudioPermission.status.shouldShowRationale,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
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

    // ✅ Modal Bottom Sheet for Subject Selection
    if (showSubjectSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSubjectSheet = false }
        ) {
            CascadingSubjectUnitTopicList(
                webSocketRef = webSocketRef,
                onSelection = { selections, _ ->
                    showSubjectSheet = false
                }
            )
        }
    }

    // ✅ Learn Option Dialog
    if (showDialog) {
        LearnOptionPopup(
            onDismiss = { showDialog = false },
            onSave = { selected ->
                currentLearnOption = selected
                showDialog = false
            },
            initialOption = if (currentLearnOption == "Learn") "Normal Learn" else currentLearnOption
        )
    }
}





@Composable
fun LearnOptionPopup(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    initialOption: String = "Normal Learn"
) {
    val options = listOf("Deep Learn", "Normal Learn", "Hard Learn")
    var selectedOption by remember { mutableStateOf(initialOption) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Learning Type",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selectedOption == option)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else Color.Transparent
                            )
                            .clickable { selectedOption = option }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedOption == option),
                            onClick = { selectedOption = option },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedOption) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Save", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}