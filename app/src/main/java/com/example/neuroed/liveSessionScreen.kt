package com.example.neuroed

import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun liveSessionScreen(navController: NavController) {
    // Get the context.
    val context = LocalContext.current

    // State to control background music.
    var isMusicPlaying by remember { mutableStateOf(false) }
    // Elapsed time state in seconds.
    var elapsedSeconds by remember { mutableStateOf(0) }
    // Amplitude state from the Visualizer (range: 0..1).
    var amplitude by remember { mutableStateOf(0f) }

    // Create MediaPlayer once (ensure R.raw.music exists).
    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.music) }

    // Dispose the MediaPlayer only when the composable leaves the composition.
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    // Control media playback based on isMusicPlaying.
    LaunchedEffect(isMusicPlaying) {
        if (isMusicPlaying) {
            mediaPlayer.isLooping = true
            mediaPlayer.start()
        } else {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            }
        }
    }

    // Visualizer: capture waveform and compute average amplitude.
    DisposableEffect(mediaPlayer) {
        val visualizer = Visualizer(mediaPlayer.audioSessionId).apply {
            captureSize = Visualizer.getCaptureSizeRange()[1]
            setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(
                    visualizer: Visualizer?,
                    waveform: ByteArray?,
                    samplingRate: Int
                ) {
                    waveform?.let {
                        val avg = it.map { byte -> abs(byte.toInt()) }.average().toFloat() / 128f
                        amplitude = avg.coerceIn(0f, 1f)
                    }
                }
                override fun onFftDataCapture(
                    visualizer: Visualizer?,
                    fft: ByteArray?,
                    samplingRate: Int
                ) { /* Not used here */ }
            }, Visualizer.getMaxCaptureRate() / 2, true, false)
            enabled = true
        }
        onDispose {
            visualizer.release()
        }
    }

    // Count-up timer: while music is playing, delay 1 sec and increment elapsedSeconds.
    LaunchedEffect(isMusicPlaying, elapsedSeconds) {
        if (isMusicPlaying) {
            delay(1000)
            elapsedSeconds++
        }
    }

    // Format seconds into "mm:ss".
    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    // Background gradient.
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0D0D0D), Color(0xFF262626))
    )

    // Layout: Fixed timer area at top, scrollable content below.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Fixed timer display.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatTime(elapsedSeconds),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Scrollable content.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                // Updated row of bars: more bars and decreased max height.
                MirroredAnimatedBars(
                    barCount = 10,         // Increased number of bars
                    maxBarHeight = 100,      // Decreased maximum height per bar
                    barColor = Color.Gray,
                    barWidth = 10,
                    spaceBetweenBars = 4,    // Optionally, adjust spacing
                    animate = isMusicPlaying,
                    amplitude = amplitude
                )
                Spacer(modifier = Modifier.height(40.dp))
                // Guided meditation instructions.
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Find a comfortable position. Close your eyes and take a deep breath.",
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Focus on your breathing. Let go of your worries and allow calmness to fill your mind.",
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
                // Action buttons: Play/Pause toggles music & timer, and End Session.
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { isMusicPlaying = !isMusicPlaying },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1ABC9C)),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = if (isMusicPlaying) "Pause Music" else "Play Music",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Button(
                        onClick = { /* Handle session end/save */ },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1ABC9C)),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "End Session",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * MirroredAnimatedBars creates a row of bars where each bar is drawn as a single composable
 * that displays two parts—one at the top and one at the bottom. Each bar uses its own animation
 * value (with a staggered delay) so that the heights change independently, similar to a typical audio visualizer.
 *
 * @param barCount Number of bars.
 * @param maxBarHeight Maximum base height (in dp) used for calculating the animated height.
 * @param barColor Base color for the bars.
 * @param barWidth Width (in dp) for each bar.
 * @param spaceBetweenBars Horizontal spacing (in dp) between bars.
 * @param animate If true, animate the bars; if false, use a static animation value.
 * @param amplitude A float (0..1) representing the current audio amplitude.
 */
@Composable
fun MirroredAnimatedBars(
    modifier: Modifier = Modifier,
    barCount: Int = 10,
    maxBarHeight: Int = 100,
    barColor: Color = Color.Gray,
    barWidth: Int = 10,
    spaceBetweenBars: Int = 4,
    animate: Boolean = true,
    amplitude: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedValues = List(barCount) { index ->
        if (animate) {
            infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1000 + index * 150
                        0.3f at 0
                        0.8f at (300 + index * 50)
                        1f at (600 + index * 50)
                        0.8f at (800 + index * 50)
                        0.3f at (1000 + index * 150)
                    },
                    repeatMode = RepeatMode.Reverse
                )
            ).value
        } else {
            0.3f
        }
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        animatedValues.forEachIndexed { index, animValue ->
            SingleMirroredBar(
                animationFactor = animValue,
                barWidth = barWidth.dp,
                maxBarHeight = maxBarHeight,
                amplitude = amplitude,
                barColor = barColor
            )
            if (index < barCount - 1) {
                Spacer(modifier = Modifier.width(spaceBetweenBars.dp))
            }
        }
    }
}

/**
 * SingleMirroredBar draws one bar as a column with two identical parts. Each part uses the given animation
 * factor to compute its height, creating a mirrored effect.
 *
 * @param animationFactor A float (from the individual animation) that modulates the bar height.
 * @param barWidth The width of the bar (as Dp).
 * @param maxBarHeight The maximum base height (in dp) for calculating the animated height.
 * @param amplitude A float (0..1) representing the current audio amplitude.
 * @param barColor The base color of the bar.
 */
@Composable
fun SingleMirroredBar(
    animationFactor: Float,
    barWidth: Dp,
    maxBarHeight: Int,
    amplitude: Float,
    barColor: Color
) {
    // Compute the animated height for this bar.
    val animatedHeight = maxBarHeight * animationFactor * (1f + amplitude * 0.5f)
    Column(
        modifier = Modifier.width(barWidth),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top part.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(animatedHeight.dp)
                .background(barColor)
        )
        // Bottom part.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(animatedHeight.dp)
                .background(barColor)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLiveSessionScreen() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize()) {
            liveSessionScreen(navController = rememberNavController())
        }
    }
}
