package com.example.neuroed

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.exp
import kotlin.math.ln

@Composable
fun ForgettingCurveScreen(navController: NavController) {
    // States for input fields.
    var timeDurationInput by remember { mutableStateOf("48") }
    var decayRateInput by remember { mutableStateOf("0.3") }
    var reviewThresholdInput by remember { mutableStateOf("0.6") }

    // State for time unit selection.
    val timeUnits = listOf("Minutes", "Hours", "Days", "Weeks")
    var selectedTimeUnit by remember { mutableStateOf("Hours") }
    var expanded by remember { mutableStateOf(false) }

    // Parse numeric inputs with fallbacks.
    val timeDurationValue = timeDurationInput.toFloatOrNull() ?: 48f
    val decayRate = decayRateInput.toFloatOrNull() ?: 0.3f
    val reviewThreshold = reviewThresholdInput.toFloatOrNull() ?: 0.6f

    // Convert timeDuration input (in selected unit) to base hours.
    val timeDurationInHours = when (selectedTimeUnit) {
        "Minutes" -> timeDurationValue / 60f
        "Hours" -> timeDurationValue
        "Days" -> timeDurationValue * 24f
        "Weeks" -> timeDurationValue * 168f
        else -> timeDurationValue
    }

    // Calculate optimal review time (in hours).
    val reviewTimeInHours = -ln(reviewThreshold) / decayRate

    // Convert optimal review time to selected unit.
    val reviewTimeDisplay = when (selectedTimeUnit) {
        "Minutes" -> reviewTimeInHours * 60f
        "Hours" -> reviewTimeInHours
        "Days" -> reviewTimeInHours / 24f
        "Weeks" -> reviewTimeInHours / 168f
        else -> reviewTimeInHours
    }

    // Animate the drawing progress of the curve.
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(timeDurationInHours, decayRate, reviewThreshold) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    // Generate sample data points for the curve using the converted time duration.
    val samplePoints = 100
    val timePoints = (0 until samplePoints).map { it * timeDurationInHours / (samplePoints - 1) }
    val retentionValues = timePoints.map { exp(-decayRate * it) }
    val currentPoints = (samplePoints * animatedProgress.value).toInt().coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Display Topic Name.
        Text(
            text = "Topic: Forgetting Curve",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Row for Time Duration input and time unit selection.
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = timeDurationInput,
                onValueChange = { timeDurationInput = it },
                label = { Text("Time Duration", color = Color.White) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Dropdown for time unit selection.
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = selectedTimeUnit,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Unit", color = Color.White) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    timeUnits.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit) },
                            onClick = {
                                selectedTimeUnit = unit
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Input for Decay Rate.
        OutlinedTextField(
            value = decayRateInput,
            onValueChange = { decayRateInput = it },
            label = { Text("Decay Rate", color = Color.White) },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Input for Review Threshold.
        OutlinedTextField(
            value = reviewThresholdInput,
            onValueChange = { reviewThresholdInput = it },
            label = { Text("Review Threshold", color = Color.White) },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Display review recommendation.
        Text(
            text = "Recommended review time: ${String.format("%.2f", reviewTimeDisplay)} $selectedTimeUnit after learning",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Card (Surface) for a modern look around the graph.
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw X and Y axes.
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, canvasHeight),
                    end = Offset(canvasWidth, canvasHeight),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, 0f),
                    end = Offset(0f, canvasHeight),
                    strokeWidth = 2f
                )

                // Create the path for the forgetting curve up to the current animated progress.
                val path = Path().apply {
                    if (timePoints.isNotEmpty()) {
                        val x0 = (timePoints[0] / timeDurationInHours) * canvasWidth
                        val y0 = canvasHeight - (retentionValues[0] * canvasHeight)
                        moveTo(x0, y0)
                    }
                    for (i in 1 until currentPoints) {
                        val x = (timePoints[i] / timeDurationInHours) * canvasWidth
                        val y = canvasHeight - (retentionValues[i] * canvasHeight)
                        lineTo(x, y)
                    }
                }
                // Draw the forgetting curve in blue.
                drawPath(
                    path = path,
                    color = Color.Blue,
                    style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Draw the horizontal threshold line (red dashed line).
                val thresholdY = canvasHeight - (reviewThreshold * canvasHeight)
                drawLine(
                    color = Color.Red,
                    start = Offset(0f, thresholdY),
                    end = Offset(canvasWidth, thresholdY),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Draw the vertical line indicating the optimal review time (green dashed line).
                if (reviewTimeInHours in 0f..timeDurationInHours) {
                    val reviewX = (reviewTimeInHours / timeDurationInHours) * canvasWidth
                    drawLine(
                        color = Color.Green,
                        start = Offset(reviewX, 0f),
                        end = Offset(reviewX, canvasHeight),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        // "Review" Button with modern styling.
        Button(
            onClick = {
                // Navigate to the review screen or perform a review action.
                navController.navigate("reviewScreen")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Review",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
