package com.example.neuroed

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.Color as AndroidColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.neuroed.model.ForgettingItem
//import com.example.neuroed.model.RetentionPoint
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.ForgettingCurveRepository
import com.example.neuroed.viewmodel.ForgettingCurveViewModel
import com.example.neuroed.viewmodel.ForgettingCurveViewModelFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

// --- Data Models ---
data class TopicData(
    val topic: String,
    val unitNumber: String,
    val subjectName: String,
    val learningDate: String,
    val revisionDate: String
)

// This class might already exist in your project, but adding it here for completeness
data class ForgettingItem(
    val id: Int? = null,
    val kind: String? = null,
    val title: String? = null,
    val unit: String? = null,
    val subject: String? = null,
    val last_review_date: String? = null,
    val next_review_date: String? = null,
    val curve: List<RetentionPoint>? = null
)

// This class might already exist in your project, but adding it here for completeness
data class RetentionPoint(
    val time: Float,
    val retention: Float
)

// --- String Extension for Capitalization ---
fun String.capitalize(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault())
        else it.toString()
    }
}

// --- Time Filter Bar ---
@Composable
fun TimeFilterBar(selectedTimeFilter: String, onTimeFilterSelected: (String) -> Unit) {
    val timeOptions = listOf("Minute", "Hour", "Day", "Week", "Month")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        timeOptions.forEach { option ->
            Button(
                onClick = { onTimeFilterSelected(option) },
                colors = if (option == selectedTimeFilter)
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                else ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A)),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = option,
                    fontSize = 12.sp,
                    color = if (option == selectedTimeFilter)
                        MaterialTheme.colorScheme.onPrimary
                    else Color.White
                )
            }
        }
    }
}

// --- Chart Utilities ---
fun generateDummyChartData(timeFilter: String): List<Float> {
    // For demonstration, return a sample oscillating dataset.
    return listOf(50f, 65f, 40f, 70f, 55f, 80f, 60f)
}


// --- Enhanced Performance Chart with Time Labels ---
@Composable
fun PerformanceChartWithAxisLabels(
    chartData: List<Float>,
    filter: String,
    twoRevision: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.animateContentSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .background(
                    Color(0xFF121212),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(8.dp)
        ) {
            PerformanceChart(chartData, filter, twoRevision, Modifier.fillMaxSize())
            Text(
                text = "Memory Retention",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .rotate(-90f),
                fontSize = 12.sp,
                color = Color.LightGray
            )

            // Enhanced time label based on filter
            val timeLabel = when(filter.lowercase()) {
                "minute" -> "Time (Minutes)"
                "hour" -> "Time (Hours)"
                "day" -> "Time (Days)"
                "week" -> "Time (Weeks)"
                "month" -> "Time (Months)"
                else -> "Time"
            }

            Text(
                text = timeLabel,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                fontSize = 12.sp,
                color = Color.LightGray
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        // Display actual numerical time value based on the filter
        val meanRevisionTimeText = "Mean Revision Time: " + calculateMeanRevisionTime(chartData, filter)
        Text(
            text = meanRevisionTimeText,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 12.sp,
            color = Color.LightGray
        )
    }
}




// --- Topic Chart ---
@Composable
fun TopicChart(
    chartData: List<Float>,
    filter: String,
    modifier: Modifier = Modifier
) {
    val errorColor = MaterialTheme.colorScheme.error.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                // Set chart background to black for the dark theme.
                setBackgroundColor(AndroidColor.BLACK)
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(false)
                setScaleEnabled(false)
                axisRight.isEnabled = false
                xAxis.isEnabled = false
                axisLeft.isEnabled = false
            }
        },
        update = { chart ->
            val entries = chartData.mapIndexed { index, value ->
                Entry(index.toFloat(), value)
            }
            val finalValue = chartData.lastOrNull() ?: 0f
            val lineColor = if (finalValue < 50f) errorColor else onSurfaceColor
            val dataSet = LineDataSet(entries, "Item").apply {
                color = lineColor
                setDrawCircles(false)
                lineWidth = 1.5f
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawFilled(true)
                fillDrawable = getGradientDrawable(chart.context, lineColor)
                setDrawValues(false)
            }
            chart.data = LineData(dataSet)
            chart.invalidate()
        },
        modifier = modifier
    )
}

// --- Gradient Drawable Helper ---
fun getGradientDrawable(context: Context, lineColor: Int): GradientDrawable {
    return GradientDrawable(
        GradientDrawable.Orientation.TOP_BOTTOM,
        intArrayOf(
            Color(lineColor).copy(alpha = 0.4f).toArgb(),
            AndroidColor.TRANSPARENT
        )
    )
}

// --- Modified Topic Card ---
@Composable
fun TopicCard(
    topic: String,
    chartData: List<Float>,
    filter: String,
    learningDate: String,
    revisionDate: String,
    unitNumber: String,
    subjectName: String,
    onTitleClick: (String) -> Unit = {}
) {
    // Use an interaction source to detect press state.
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f)

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .scale(scale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null, // Remove ripple for cleaner look
                    onClick = { onTitleClick(topic) }
                )
                .animateContentSize()
                // Simple dark border instead of gradient
                .border(
                    width = 1.dp,
                    color = Color(0xFF2A2A2A),
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)) // Darker black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Unit: $unitNumber",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray // Changed from blue to light gray
                        )
                        Text(
                            text = "Subject: $subjectName",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray // Changed from blue to light gray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = topic,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.weight(1f),
                        color = Color.White
                    )
                    TopicChart(
                        chartData = chartData,
                        filter = filter,
                        modifier = Modifier
                            .width(80.dp)
                            .height(50.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Learning: $learningDate",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFBDBDBD) // Light gray
                    )
                    Text(
                        text = "Revision: $revisionDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFBDBDBD)
                    )
                }
            }
        }
    }
}

// --- Draggable Handle for Bottom Sheet ---
@Composable
fun BottomSheetHandle() {
    Box(
        modifier = Modifier
            .width(40.dp)
            .height(4.dp)
            .background(
                color = Color.LightGray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(2.dp)
            )
    )
}


// Function to calculate mean revision time text based on filter
fun calculateMeanRevisionTime(chartData: List<Float>, filter: String): String {
    if (chartData.isEmpty()) return "N/A"

    // Get mean value (simplified example)
    val meanValue = chartData.sum() / chartData.size

    // Format based on filter
    return when(filter.lowercase()) {
        "minute" -> String.format("%.1f min", meanValue / 10) // Example scaling
        "hour" -> String.format("%.1f hrs", meanValue / 20)
        "day" -> String.format("%.1f days", meanValue / 30)
        "week" -> String.format("%.1f weeks", meanValue / 40)
        "month" -> String.format("%.1f months", meanValue / 50)
        else -> "N/A"
    }
}


@Composable
fun PerformanceChart(
    chartData: List<Float>,
    filter: String,
    twoRevision: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Capture composable color values here.
    val errorColor = MaterialTheme.colorScheme.error.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                // Set chart background to black for the dark theme.
                setBackgroundColor(AndroidColor.BLACK)
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)  // Enable touch for better interaction
                setScaleEnabled(false)
                axisRight.isEnabled = false
                xAxis.apply {
                    isEnabled = true
                    setDrawGridLines(false)
                    setDrawAxisLine(true)
                    granularity = 1f
                    position = XAxis.XAxisPosition.BOTTOM
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            // Customize X-axis labels based on filter
                            return when(filter.lowercase()) {
                                "minute" -> "${value.toInt() * 5}m" // 5 minute intervals
                                "hour" -> "${value.toInt()}h"
                                "day" -> "D${value.toInt() + 1}"
                                "week" -> "W${value.toInt() + 1}"
                                "month" -> "M${value.toInt() + 1}"
                                else -> "T${value.toInt() + 1}"
                            }
                        }
                    }
                }
                axisLeft.apply {
                    isEnabled = true
                    setDrawGridLines(true)
                    setDrawAxisLine(true)
                    granularity = 10f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "${value.toInt()}%"
                        }
                    }
                    removeAllLimitLines()
                    val limitLine = LimitLine(50f, "Threshold")
                    limitLine.lineColor = errorColor
                    limitLine.lineWidth = 2f
                    limitLine.textColor = errorColor
                    limitLine.textSize = 12f
                    addLimitLine(limitLine)
                }
            }
        },
        update = { chart ->
            // Generate appropriate entries based on filter
            val entries = generateFilterAdjustedEntries(chartData, filter)

            val finalValue = chartData.lastOrNull() ?: 0f
            val lineColor = if (finalValue < 50f) errorColor else onSurfaceColor
            val dataSet = LineDataSet(entries, "Performance").apply {
                color = lineColor
                setDrawCircles(true)
                circleRadius = 4f
                setCircleColor(lineColor)
                lineWidth = 2f
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawFilled(true)
                fillDrawable = getGradientDrawable(chart.context, lineColor)
                setDrawValues(false)
            }
            chart.data = LineData(dataSet)
            chart.invalidate()
        },
        modifier = modifier
    )
}

// Function to generate entries based on time filter
fun generateFilterAdjustedEntries(chartData: List<Float>, filter: String): List<Entry> {
    return when(filter.lowercase()) {
        "minute" -> {
            // For minute filter, create more granular data points
            chartData.flatMapIndexed { index, value ->
                // Create more detailed points for minutes
                if (index < chartData.size - 1) {
                    val nextValue = chartData[index + 1]
                    val stepValue = (nextValue - value) / 5
                    (0..4).map { step ->
                        Entry((index * 5 + step).toFloat(), value + stepValue * step)
                    }
                } else {
                    listOf(Entry((index * 5).toFloat(), value))
                }
            }
        }
        else -> {
            // Default behavior for other filters
            chartData.mapIndexed { index, value ->
                Entry(index.toFloat(), value)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecallingScreen(
    navController: NavController
) {
    val userId = 1 // default userId to avoid missing argument

    // Initialize ViewModel via factory
    val repository = remember { ForgettingCurveRepository(RetrofitClient.apiService) }
    val factory = remember { ForgettingCurveViewModelFactory(repository, userId) }
    val viewModel: ForgettingCurveViewModel = viewModel(factory = factory)

    // Coroutine scope for potential async operations
    val coroutineScope = rememberCoroutineScope()

    // Observe LiveData as state
    val items by viewModel.curveItems.observeAsState(emptyList())
    val currentScale by viewModel.scale.observeAsState("day")
    val error by viewModel.error.observeAsState()

    // Track when minute filter is selected to show detailed time info
    var showDetailedTimeInfo by remember { mutableStateOf(false) }

    // Fetch data when screen is first composed
    LaunchedEffect(key1 = userId) {
        viewModel.fetchCurve()
    }

    // Update showDetailedTimeInfo whenever scale changes
    LaunchedEffect(currentScale) {
        showDetailedTimeInfo = currentScale?.lowercase() == "minute"
    }

    // Safe display scale conversion
    val displayScale = remember(currentScale) {
        currentScale?.capitalize() ?: "Day"
    }

    var selectedItem by remember { mutableStateOf<ForgettingItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Recalling",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = Color.White
                        )
                    },
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
                        containerColor = Color(0xFF121212)
                    ),
                    modifier = Modifier.height(48.dp)
                )
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                Spacer(modifier = Modifier.height(16.dp))

                // Time filter bar with proper display scale
                TimeFilterBar(
                    selectedTimeFilter = displayScale,
                    onTimeFilterSelected = {
                        viewModel.setScale(it.lowercase())
                        // Show detailed time info when "Minute" is selected
                        showDetailedTimeInfo = it.lowercase() == "minute"
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Display detailed time info when minute filter is selected
                AnimatedVisibility(visible = showDetailedTimeInfo) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Minute View",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "This view shows detailed minute-by-minute memory retention data. Each data point represents 5-minute intervals.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Error display
                error?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Rest of the code remains the same...
                // Item list or loading indicator
                if (items.isEmpty() && error == null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Loading data...",
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items) { item ->
                            // Default data for null safety
                            val curveData = item.curve?.map { it.retention }
                                ?: listOf(50f, 40f, 60f, 45f)

                            TopicCard(
                                topic = item.title ?: "Untitled Topic",
                                chartData = curveData,
                                filter = displayScale,
                                learningDate = item.last_review_date ?: "N/A",
                                revisionDate = item.next_review_date ?: "N/A",
                                unitNumber = item.unit ?: "N/A",
                                subjectName = item.subject ?: "N/A",
                                onTitleClick = { selectedItem = item }
                            )
                        }
                    }
                }
            }

            // Bottom sheet with enhanced chart display
            selectedItem?.let { itm ->
                ModalBottomSheet(
                    onDismissRequest = { selectedItem = null },
                    sheetState = sheetState,
                    dragHandle = { BottomSheetHandle() },
                    containerColor = Color(0xFF151515)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .padding(bottom = 32.dp) // Extra padding at bottom
                    ) {
                        Text("Topic Details", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text("Unit: ${itm.unit ?: "N/A"} • ${itm.subject ?: "N/A"}", color = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(itm.title ?: "Untitled Topic", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(16.dp))

                        TimeFilterBar(
                            selectedTimeFilter = displayScale,
                            onTimeFilterSelected = { viewModel.setScale(it.lowercase()) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Chart with default data for null safety
                        val curveData = itm.curve?.map { it.retention }
                            ?: listOf(50f, 40f, 60f, 45f)

                        PerformanceChartWithAxisLabels(
                            chartData = curveData,
                            filter = displayScale,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Time details shown when minute view is selected
                        AnimatedVisibility(visible = displayScale.lowercase() == "minute") {
                            Column {
                                Text(
                                    text = "Time Analysis",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Memory peak: ${calculatePeakTime(curveData)} min",
                                        color = Color.LightGray,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Decay rate: ${calculateDecayRate(curveData)}%/min",
                                        color = Color.LightGray,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        Button(
                            onClick = {
                                // Implementation code same as before
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("Start Learning", color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

// Helper functions for time analysis
fun calculatePeakTime(data: List<Float>): String {
    if (data.isEmpty()) return "N/A"
    val maxIndex = data.indices.maxByOrNull { data[it] } ?: 0
    return "${maxIndex * 5}"
}

fun calculateDecayRate(data: List<Float>): String {
    if (data.size < 2) return "N/A"
    val peakIndex = data.indices.maxByOrNull { data[it] } ?: 0
    if (peakIndex >= data.size - 1) return "0.0"

    // Calculate average decay rate after peak
    val totalDecay = data[peakIndex] - data.last()
    val timeSpan = (data.size - 1 - peakIndex) * 5 // 5 minutes per unit

    return if (timeSpan > 0)
        String.format("%.1f", (totalDecay / timeSpan))
    else "0.0"
}