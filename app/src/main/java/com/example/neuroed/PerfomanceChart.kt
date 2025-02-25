package com.example.neuroed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/**
 * A simple line chart that draws a performance graph.
 *
 * @param data A list of Float values representing performance scores.
 * @param modifier Modifier to be applied to the chart.
 * @param lineColor Color of the line.
 * @param backgroundColor Background color behind the chart.
 * @param lineWidth Stroke width of the line.
 */
@Composable
fun PerformanceLineChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color.Blue,
    backgroundColor: Color = Color(0xFFEEEEEE),
    lineWidth: Float = 4f
) {
    if (data.isEmpty()) return

    // Determine min and max for scaling
    val minValue = data.minOrNull() ?: 0f
    val maxValue = data.maxOrNull() ?: 1f
    val valueRange = if (maxValue - minValue == 0f) 1f else maxValue - minValue

    // Draw on a canvas
    Canvas(modifier = modifier.background(backgroundColor)) {
        val width = size.width
        val height = size.height
        // Calculate the horizontal distance between data points.
        val xStep = width / (data.size - 1)

        // Convert data values to canvas points
        val points = data.mapIndexed { index, value ->
            val x = index * xStep
            // Normalize the value (0..1) and invert the y-axis so higher values are at the top.
            val normalized = (value - minValue) / valueRange
            val y = height - (normalized * height)
            Offset(x, y)
        }

        // Draw lines between each point
        for (i in 0 until points.size - 1) {
            drawLine(
                color = lineColor,
                start = points[i],
                end = points[i + 1],
                strokeWidth = lineWidth
            )
        }
    }
}

@Composable
fun SubjectPerformanceScreen() {
    // Sample performance data (e.g., performance score over time)
    val performanceData = listOf(0.5f, 0.8f, 1.2f, 0.9f, 1.5f, 1.8f, 1.6f, 2.0f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Subject Learning Performance",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        // The chart fills a container height of 250dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            PerformanceLineChart(
                data = performanceData,
                modifier = Modifier.fillMaxSize(),
                lineColor = Color.Blue,
                backgroundColor = Color(0xFFEEEEEE),
                lineWidth = 4f
            )
        }
    }
}
