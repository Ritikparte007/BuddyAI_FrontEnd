// RecallingScreen.kt
package com.example.neuroed

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlinx.coroutines.launch
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState

// --- Data Model ---
data class TopicData(
    val topic: String,
    val unitNumber: String,
    val subjectName: String,
    val learningDate: String,
    val revisionDate: String
)

// --- Revision Filter Bar ---
@Composable
fun RevisionFilterBar(selectedRevision: String, onRevisionSelected: (String) -> Unit) {
    val revisionOptions = listOf("Upcoming", "Completed", "Pending")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        revisionOptions.forEach { option ->
            Button(
                onClick = { onRevisionSelected(option) },
                colors = if (option == selectedRevision)
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = option,
                    fontSize = 12.sp,
                    color = if (option == selectedRevision)
                        MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// --- Search Bar with Icon and Clear Button ---
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search Icon",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentSize(),
        placeholder = { Text("Search topics...") },
        singleLine = true
    )
}

// --- Time Filter Bar ---
@Composable
fun TimeFilterBar(selectedTimeFilter: String, onTimeFilterSelected: (String) -> Unit) {
    val timeOptions = listOf("Minute", "Hour", "Day", "Week", "Month")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        timeOptions.forEach { option ->
            Button(
                onClick = { onTimeFilterSelected(option) },
                colors = if (option == selectedTimeFilter)
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = option,
                    fontSize = 12.sp,
                    color = if (option == selectedTimeFilter)
                        MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface
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

fun generateDownChartData(data: List<Float>): List<Float> {
    return data.map { it * 0.9f + 5f }
}

// --- Performance Chart with Axis Labels ---
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
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
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
                color = Color.Gray
            )
            Text(
                text = "Time",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Mean Revision Time: N/A",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

// --- Performance Chart ---
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
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(false)
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
                            return "T${value.toInt() + 1}"
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
                    limitLine.lineColor = errorColor  // Use captured value here.
                    limitLine.lineWidth = 2f
                    limitLine.textColor = errorColor
                    limitLine.textSize = 12f
                    addLimitLine(limitLine)
                }
            }
        },
        update = { chart ->
            val entries = chartData.mapIndexed { index, value ->
                Entry(index.toFloat(), value)
            }
            val finalValue = chartData.last()
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



@Composable
fun TopicChart(
    chartData: List<Float>,
    filter: String,
    modifier: Modifier = Modifier
) {
    // Capture colors in the composable context.
    val errorColor = MaterialTheme.colorScheme.error.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
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
            val finalValue = chartData.last()
            // Use the captured colors instead of calling MaterialTheme directly.
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
            Color(lineColor).copy(alpha = 102F).toArgb(),
            Color.Transparent.toArgb()
        )
    )
}

// --- Enhanced Topic Card with Focused Color Design ---



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
    // Use an interaction source to detect press state
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f)

    androidx.compose.animation.AnimatedVisibility(
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
                    indication = rememberRipple(),
                    onClick = { onTitleClick(topic) }
                )
                .animateContentSize()
                // Use a thicker gradient border to emphasize the card.
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                if (filter == "Topic") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEEEEEE), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Unit: $unitNumber",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Subject: $subjectName",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
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
                        modifier = Modifier.weight(1f)
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
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Revision: $revisionDate",
                        style = MaterialTheme.typography.bodySmall
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
                color = Color.Gray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(2.dp)
            )
    )
}

// --- Main Recalling Screen with Focused Color Design ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecallingScreen(navController: NavController) {
    var selectedRevision by remember { mutableStateOf("Upcoming") }
    var searchQuery by remember { mutableStateOf("") }

    // Sample topics.
    val topics = listOf(
        "Cell Structure",
        "Mitosis",
        "Meiosis",
        "DNA Replication",
        "Protein Synthesis",
        "Photosynthesis",
        "Enzyme Kinetics",
        "Neural Networks",
        "Genetic Inheritance",
        "Evolution"
    )
    val topicDataList = topics.map { topic ->
        TopicData(
            topic = topic,
            unitNumber = "Unit 1",
            subjectName = "Biology",
            learningDate = "2023-03-01",
            revisionDate = "2023-03-05"
        )
    }
    val filteredTopics = when (selectedRevision) {
        "Upcoming" -> topicDataList.filter { it.topic.endsWith("e") }
        "Completed" -> topicDataList.filter { it.topic.endsWith("s") }
        "Pending" -> topicDataList.filter { it.topic.endsWith("n") }
        else -> topicDataList
    }.filter { it.topic.contains(searchQuery, ignoreCase = true) }

    var selectedItem by remember { mutableStateOf<TopicData?>(null) }
    val dummyChartData = generateDummyChartData("Day")
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
    ) {
        // Gradient background behind the top app bar using focused container colors.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
        )
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Recalling",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                RevisionFilterBar(selectedRevision = selectedRevision, onRevisionSelected = { selectedRevision = it })
                SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredTopics) { data ->
                        TopicCard(
                            topic = data.topic,
                            chartData = dummyChartData,
                            filter = "Topic",
                            unitNumber = data.unitNumber,
                            subjectName = data.subjectName,
                            learningDate = data.learningDate,
                            revisionDate = data.revisionDate,
                            onTitleClick = { selectedItem = data }
                        )
                    }
                }
            }

            if (selectedItem != null) {
                var bottomTimeFilter by remember { mutableStateOf("Day") }
                val bottomChartData = generateDummyChartData(bottomTimeFilter)
                ModalBottomSheet(
                    onDismissRequest = { selectedItem = null },
                    sheetState = sheetState,
                    dragHandle = { BottomSheetHandle() },
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .animateContentSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            // Bottom Sheet Header with Title, Subtitle, and dismiss cue.
                            Text(
                                text = "Topic Details",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Unit: ${selectedItem?.unitNumber} • ${selectedItem?.subjectName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                            Text(
                                text = "Swipe down to dismiss",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = selectedItem?.topic ?: "",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            TimeFilterBar(
                                selectedTimeFilter = bottomTimeFilter,
                                onTimeFilterSelected = { bottomTimeFilter = it }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            PerformanceChartWithAxisLabels(
                                chartData = bottomChartData,
                                filter = bottomTimeFilter,
                                twoRevision = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { selectedItem = null }) {
                                Text("Close")
                            }
                        }
                    }
                )
            }
        }
    }
}
