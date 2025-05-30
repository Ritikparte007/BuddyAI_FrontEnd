package com.example.neuroed

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import okhttp3.*
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit
import okhttp3.Request

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MeditationGenerateScreen(navController: NavController) {
    val context = LocalContext.current

    // Input states.
    var problemText by remember { mutableStateOf(TextFieldValue("")) }
    var timeText by remember { mutableStateOf(TextFieldValue("12:00")) }

    // Session JSON received from the WebSocket.
    var sessionText by remember { mutableStateOf("") }

    // WebSocket reference.
    val webSocketRef = remember { mutableStateOf<WebSocket?>(null) }

    // Initialize the WebSocket connection.
    DisposableEffect(Unit) {
        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()
        // Use 10.0.2.2 for emulator instead of localhost.
        val request = Request.Builder()
            .url("ws://localhost:8000/api/meditation/generate/")
            .build()
        val webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connection opened")
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Received text: $text")
                Handler(Looper.getMainLooper()).post {
                    sessionText = text
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
        onDispose { webSocket.close(1000, "Screen disposed") }
    }

    // Play background music using MediaPlayer.
    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.music) }
    DisposableEffect(Unit) {
        mediaPlayer.isLooping = true
        mediaPlayer.start()
        onDispose {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    // Initialize TextToSpeech.
    var ttsInitialized by remember { mutableStateOf(false) }
    val ttsState = remember { mutableStateOf<TextToSpeech?>(null) }
    LaunchedEffect(context) {
        ttsState.value = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = ttsState.value?.setLanguage(Locale.US)
                    ?: TextToSpeech.LANG_NOT_SUPPORTED
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported")
                } else {
                    customizeVoice(ttsState.value!!)
                    ttsInitialized = true
                    // Test the TTS engine.
                    speak(ttsState.value!!, "Hello, I'm your personal assistant. How can I help you today? " +
                            "I’m here to make your day easier by assisting you with various tasks.")
                }
            } else {
                Log.e("TTS", "Initialization failed")
            }
        }, "com.google.android.tts")
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF121212), Color(0xFF1E1E1E))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate Session", color = Color.White, fontSize = 28.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.background(brush = backgroundGradient)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = problemText,
                    onValueChange = { problemText = it },
                    label = { Text("Describe Problem", color = Color.White) },
                    placeholder = { Text("Do I work hard? I feel tired", color = Color.LightGray) },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = "Problem", tint = Color(0xFF1ABC9C))
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color(0xFF2C2C2C),
                        focusedBorderColor = Color(0xFF1ABC9C),
                        unfocusedBorderColor = Color(0xFF555555),
                        cursorColor = Color(0xFF1ABC9C)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = timeText,
                    onValueChange = { timeText = it },
                    label = { Text("Select Time", color = Color.White) },
                    placeholder = { Text("hh:mm", color = Color.LightGray) },
                    trailingIcon = {
                        Icon(Icons.Default.Create, contentDescription = "Time", tint = Color(0xFF1ABC9C))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color(0xFF2C2C2C),
                        focusedBorderColor = Color(0xFF1ABC9C),
                        unfocusedBorderColor = Color(0xFF555555),
                        cursorColor = Color(0xFF1ABC9C)
                    ),
                    modifier = Modifier.width(150.dp)
                )

                Button(
                    onClick = {
                        val promptText = problemText.text
                        val timeValue = if (timeText.text.isNotBlank()) timeText.text else "12:00"
                        val json = JSONObject().apply {
                            put("userId", 1)
                            put("Prompt", promptText)
                            put("Time", timeValue)
                        }
                        Log.d("WebSocket", "Sending message: $json")
                        webSocketRef.value?.send(json.toString())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1ABC9C)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("Generate", color = Color.White)
                }

                if (sessionText.isNotEmpty()) {
                    ttsState.value?.let { tts ->
                        SessionTextAndAudio(sessionText, tts, ttsInitialized)
                    }
                }

                Button(
                    onClick = {
                        if (ttsInitialized) {
                            speak(ttsState.value!!, "This is a test of the TTS engine.")
                        } else {
                            Log.d("TTS", "TTS not initialized!")
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Test TTS", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SessionTextAndAudio(sessionText: String, tts: TextToSpeech, ttsInitialized: Boolean) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(scrollState)
                .padding(8.dp)
        ) {
            Text(sessionText, color = Color.White, style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (!ttsInitialized) {
                    Log.d("TTS", "TTS not initialized!")
                    return@Button
                }
                try {
                    // Parse outer JSON.
                    val outerJson = JSONObject(sessionText)
                    val sessionJson = if (outerJson.has("session")) {
                        outerJson.getJSONObject("session")
                    } else {
                        outerJson
                    }
                    val keys = sessionJson.keys().asSequence().toList()
                    Log.d("TTS", "Session JSON keys: $keys")
                    if (!sessionJson.has("response")) {
                        Log.e("TTS", "The JSON does not contain the expected 'response' key. Full JSON: $sessionJson")
                        return@Button
                    }
                    // Get the response array.
                    var responseArray = sessionJson.getJSONArray("response")
                    // If the response is a code block, clean it.
                    if (responseArray.length() == 1) {
                        val first = responseArray.getString(0)
                        if (first.trim().startsWith("```") && first.trim().endsWith("```")) {
                            val cleaned = first.trim()
                                .removePrefix("```json")
                                .removePrefix("```")
                                .removeSuffix("```")
                                .trim()
                            val innerJson = JSONObject(cleaned)
                            responseArray = innerJson.getJSONArray("response")
                            // Also update other arrays.
                            val pitchArray = innerJson.getJSONArray("setPitch")
                            val speechRateArray = innerJson.getJSONArray("setSpeechRate")
                            val pauseArray = innerJson.getJSONArray("pauseSecond")
                            // Convert arrays to lists.
                            val sentences = (0 until responseArray.length()).map { responseArray.getString(it) }
                            val pitchList = (0 until pitchArray.length()).map { pitchArray.getDouble(it).toFloat() }
                            val speechRateList = (0 until speechRateArray.length()).map { speechRateArray.getDouble(it).toFloat() }
                            val pauseList = (0 until pauseArray.length()).map { pauseArray.getInt(it).toLong() }
                            speakSessionWithPause(tts, sentences, pitchList, speechRateList, pauseList)
                            return@Button
                        }
                    }
                    // Otherwise, assume arrays are directly available.
                    val pitchArray = sessionJson.getJSONArray("setPitch")
                    val speechRateArray = sessionJson.getJSONArray("setSpeechRate")
                    val pauseArray = sessionJson.getJSONArray("pauseSecond")
                    val sentences = (0 until responseArray.length()).map { responseArray.getString(it) }
                    val pitchList = (0 until pitchArray.length()).map { pitchArray.getDouble(it).toFloat() }
                    val speechRateList = (0 until speechRateArray.length()).map { speechRateArray.getDouble(it).toFloat() }
                    val pauseList = (0 until pauseArray.length()).map { pauseArray.getInt(it).toLong() }
                    speakSessionWithPause(tts, sentences, pitchList, speechRateList, pauseList)
                } catch (e: Exception) {
                    Log.e("TTS", "Error parsing session JSON: ${e.localizedMessage}")
                }
            },
            modifier = Modifier.padding(8.dp).fillMaxWidth()
        ) {
            Text("Speak Session", color = Color.White)
        }
    }
}

/**
 * Recursively speaks each sentence with a delay specified by pauseList.
 * Waits for each utterance to finish and then delays before speaking the next sentence.
 */
fun speakSessionWithPause(
    tts: TextToSpeech,
    sentences: List<String>,
    pitchList: List<Float>,
    speechRateList: List<Float>,
    pauseList: List<Long>,
    index: Int = 0
) {
    if (index >= sentences.size) return
    tts.setPitch(pitchList[index])
    tts.setSpeechRate(speechRateList[index])
    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            Log.d("TTS", "Utterance $utteranceId started")
        }
        override fun onDone(utteranceId: String?) {
            Log.d("TTS", "Utterance $utteranceId done. Pausing for ${pauseList[index]} seconds")
            Handler(Looper.getMainLooper()).postDelayed({
                speakSessionWithPause(tts, sentences, pitchList, speechRateList, pauseList, index + 1)
            }, pauseList[index] * 1000)
        }
        override fun onError(utteranceId: String?) {
            Log.e("TTS", "Error on utterance $utteranceId")
        }
    })
    Log.d("TTS", "Speaking sentence: ${sentences[index]}")
    tts.speak(sentences[index], TextToSpeech.QUEUE_FLUSH, null, "utterance_$index")
}

fun customizeVoice(tts: TextToSpeech) {
    tts.setPitch(1.0f)
    tts.setSpeechRate(0.9f)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
        val voices: Set<Voice> = tts.voices
        for (voice in voices) {
            if (voice.locale == Locale.US && voice.name.contains("google", ignoreCase = true)) {
                tts.voice = voice
                break
            }
        }
    }
}

fun speak(tts: TextToSpeech, text: String) {
    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "test")
}
