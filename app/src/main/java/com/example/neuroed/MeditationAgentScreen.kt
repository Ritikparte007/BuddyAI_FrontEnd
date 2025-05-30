package com.example.neuroed

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.example.neuroed.utils.speakSsmlSuspend
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
//import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.neuroed.model.TestType
import com.example.neuroed.utils.speakSsmlSuspend
import com.google.gson.JsonParser
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeditationsAgentScreen(navController: NavController) {
    var showCountdown by remember { mutableStateOf(true) }
    var countdownTime by remember { mutableStateOf(5) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(0) }
    val totalTime = remember { 300 } // 5 minutes in seconds

    // Theme colors
    val deepPurple = Color(0xFF4A148C)
    val lightPurple = Color(0xFF9C27B0)
    val teal = Color(0xFF009688)
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A1A2E),
            Color(0xFF16213E),
            Color(0xFF0F3460)
        )
    )


    val wsReference = remember { mutableStateOf<WebSocket?>(null) }

    DisposableEffect(Unit) {
        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()
        val request = Request.Builder()
            .url("ws://localhost:8000/api/AgentLivemeditation/")
            .build()
        val listener = object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: okhttp3.Response) {
                ws.send("""{"action":"start"}""")
            }
            override fun onMessage(ws: WebSocket, text: String) {
            }
        }
        wsReference.value = client.newWebSocket(request, listener)
        onDispose { wsReference.value?.close(1000, "dispose") }
    }


    // Countdown logic
    LaunchedEffect(Unit) {
        while (countdownTime > 0) {
            delay(1000L) // 1 second delay
            countdownTime--
        }
        showCountdown = false // After countdown finish, show real screen
    }

    // Timer logic when playing
    LaunchedEffect(isPlaying) {
        while (isPlaying && currentTime < totalTime) {
            delay(1000L)
            currentTime++
        }

        if (currentTime >= totalTime) {
            isPlaying = false
            // Could show a completion message here
        }
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pain Relief Meditation", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradientBackground)
                .padding(paddingValues)
        ) {
            if (showCountdown) {
                // 🕒 Countdown Screen
                CountdownScreen(countdownTime = countdownTime)
            } else {
                // 🌟 Main Meditation Screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Meditation visualization
                    EmotionsDiagramAnimatedUI(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Controls
                    MeditationControls(
                        isPlaying = isPlaying,
                        onPlayPause = { isPlaying = !isPlaying }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Meditation guide content
                    MeditationGuide()
                }
            }
        }
    }
}

@Composable
fun CountdownScreen(countdownTime: Int) {
    val animatedScale = remember { Animatable(0.8f) }

    LaunchedEffect(countdownTime) {
        animatedScale.animateTo(
            targetValue = 1.2f,
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        )
        animatedScale.animateTo(
            targetValue = 0.8f,
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Meditation Starting In...",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF7B1FA2),
                            Color(0xFF4A148C)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = countdownTime.toString(),
                color = Color.White,
                fontSize = (70 * animatedScale.value).sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Find a comfortable position",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp
        )
    }
}

@Composable
fun MeditationVisualizer(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()

    val outerCircleSize by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val middleCircleSize by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val innerCircleSize by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(250.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.minDimension / 2

            if (isPlaying) {
                // Outer circle
                drawCircle(
                    color = Color(0xFF7E57C2).copy(alpha = 0.4f),
                    radius = maxRadius * outerCircleSize,
                    center = center,
                    style = Stroke(width = 8f)
                )

                // Middle circle
                drawCircle(
                    color = Color(0xFF5E35B1).copy(alpha = 0.6f),
                    radius = maxRadius * middleCircleSize,
                    center = center,
                    style = Stroke(width = 5f)
                )

                // Inner circle
                drawCircle(
                    color = Color(0xFF3949AB).copy(alpha = 0.8f),
                    radius = maxRadius * innerCircleSize,
                    center = center,
                    style = Stroke(width = 3f)
                )

                // Draw sound waves when playing
                val waveCount = 8
                val radius = maxRadius * 0.9f

                for (i in 0 until waveCount) {
                    val angle = (i * (2 * PI / waveCount)).toFloat()
                    val amplitude = if (i % 2 == 0) 0.8f else 0.4f
                    val x = center.x + radius * cos(angle) * amplitude
                    val y = center.y + radius * sin(angle) * amplitude

                    drawLine(
                        color = Color(0xFF00BCD4),
                        start = center,
                        end = Offset(x, y),
                        strokeWidth = 1.5f,
                        cap = StrokeCap.Round
                    )
                }
            } else {
                // Static circle when paused
                drawCircle(
                    color = Color(0xFF7E57C2).copy(alpha = 0.5f),
                    radius = maxRadius * 0.8f,
                    center = center,
                    style = Stroke(width = 8f)
                )

                drawCircle(
                    color = Color(0xFF5E35B1).copy(alpha = 0.5f),
                    radius = maxRadius * 0.65f,
                    center = center,
                    style = Stroke(width = 5f)
                )

                drawCircle(
                    color = Color(0xFF3949AB).copy(alpha = 0.5f),
                    radius = maxRadius * 0.5f,
                    center = center,
                    style = Stroke(width = 3f)
                )
            }
        }
    }
}

@Composable
fun MeditationControls(isPlaying: Boolean, onPlayPause: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous button (can be disabled or used for section navigation)
        IconButton(
            onClick = { /* Previous section or restart */ },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF303F9F).copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Play/Pause button
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF7B1FA2),
                            Color(0xFF4A148C)
                        )
                    )
                )
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Next button (can be disabled or used for section navigation)
        IconButton(
            onClick = { /* Next section */ },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF303F9F).copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Next",
                tint = Color.White,
                modifier = Modifier.rotate(180f)
            )
        }
    }
}

@Composable
fun MeditationProgress(currentTime: Int, totalTime: Int) {
    val progress = currentTime.toFloat() / totalTime.toFloat()
    val timeDisplay = formatTime(currentTime)
    val totalTimeDisplay = formatTime(totalTime)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF7B1FA2),
            trackColor = Color(0xFF424242)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = timeDisplay,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )

            Text(
                text = totalTimeDisplay,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}

val speechKey = "4a5877d33caa49bcbc19eade2c0fc602"
val speechRegion = "centralindia"





fun makeHumanSsml(
    text: String,
    voiceName: String,
    baseRate: String,
    basePitch: String,
    volume: String,
    expressStyle: String? = null // e.g., "whispering"
): String {
    val sentences = text.trim().split(". ").map { it.trim().removeSuffix(".") }
    return buildString {
        append("""
<speak version="1.0" xmlns="http://www.w3.org/2001/10/synthesis" xml:lang="en-IN"
       xmlns:mstts="https://www.w3.org/2001/mstts">
  <voice name="$voiceName">
""".trimIndent())
        expressStyle?.let {
            append("<mstts:express-as style=\"$it\">\n")
        }
        sentences.forEachIndexed { index, sentence ->
            val rateAdj = if (index % 2 == 0) "0.95" else "1.0"
            val pitchAdj = if (index % 2 == 0) "+2st" else "+0st"
            append("""
    <prosody rate="$rateAdj" pitch="$pitchAdj" volume="$volume">$sentence.</prosody>
    <break time="${if (index < sentences.lastIndex) "600ms" else "300ms"}"/>
""".trimIndent())
        }
        expressStyle?.let {
            append("</mstts:express-as>\n")
        }
        append("""
  </voice>
</speak>
""".trimIndent())
    }
}


@Composable
fun MeditationGuide() {
    // Prosody defaults
    val rate = "0.9"
    val pitch = "+2st"
    val volume = "medium"
    val voiceName = "en-US-AvaMultilingualNeural"
    val expressStyle = "whispering"

    // Azure TTS setup
    val speechKey = "4a5877d33caa49bcbc19eade2c0fc602"
    val speechRegion = "centralindia"
    val speechConfig = remember {
        SpeechConfig.fromSubscription(speechKey, speechRegion).apply {
            speechSynthesisVoiceName = voiceName
        }
    }
    val audioConfig = remember {
        AudioConfig.fromDefaultSpeakerOutput()
    }
    val synthesizer = remember {
        SpeechSynthesizer(speechConfig, audioConfig)
    }

    // Speak guide text human-like
    val guideText = "Pain Relief Meditation"
    LaunchedEffect(guideText) {
        val humanSsml = makeHumanSsml(
            text = guideText,
            voiceName = voiceName,
            baseRate = rate,
            basePitch = pitch,
            volume = volume,
            expressStyle = expressStyle
        )
        synthesizer.speakSsmlSuspend(humanSsml)
    }

    // Meditation steps
    val steps = listOf(
        "Beginning (Prepare)" to listOf(
            "Sit or lie down comfortably.",
            "Gently close your eyes.",
            "Take a deep breath in and slowly breathe out.",
            "Relax completely."
        ),
        "Focus (Awareness)" to listOf(
            "Bring attention to the area where you feel pain.",
            "Notice it calmly without resistance.",
            "Silently say: I see you, I accept you, I send you kindness."
        ),
        "Visualization (Healing Light)" to listOf(
            "Imagine a soft, golden healing light surrounding the painful area.",
            "With every breath, the light grows stronger and melts the pain."
        ),
        "Closing (Return)" to listOf(
            "Take a deep, peaceful breath.",
            "Move your fingers and toes gently.",
            "When ready, slowly open your eyes."
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E).copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = guideText,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )

            steps.forEach { (title, instructions) ->
                Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
                MeditationStep(title = title, instructions = instructions, emoji =
                when (title) {
                    "Beginning (Prepare)" -> "🌿"
                    "Focus (Awareness)" -> "🌟"
                    "Visualization (Healing Light)" -> "💫"
                    else -> "🌸"
                }
                )

                LaunchedEffect(title) {
                    val fullText = "$title. ${instructions.joinToString(" ")}"
                    val humanSsml = makeHumanSsml(
                        text = fullText,
                        voiceName = voiceName,
                        baseRate = rate,
                        basePitch = pitch,
                        volume = volume
                    )
                    synthesizer.speakSsmlSuspend(humanSsml)
                }
            }
        }
    }
}

@Composable
fun MeditationStep(title: String, instructions: List<String>, emoji: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        instructions.forEach { inst ->
            Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp), verticalAlignment = Alignment.Top) {
                Text(text = "•", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = inst, color = Color.White.copy(alpha = 0.9f), fontSize = 16.sp)
            }
        }
    }
}

// Helper function to format seconds into MM:SS
fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}

