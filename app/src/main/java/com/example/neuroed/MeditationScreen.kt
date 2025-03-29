package com.example.neuroed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeditationScreen(
    navController: NavController? = null
) {
    var selectedFilter by remember { mutableStateOf("session") }
    var selectedDate by remember { mutableStateOf("Mon 1") }

    // More days for horizontal scrolling
    val dateLabels = listOf(
        "Mon 1", "Tue 2", "Wed 3", "Thu 4", "Fri 5", "Sat 6", "Sun 7",
        "Mon 8", "Tue 9", "Wed 10", "Thu 11", "Fri 12", "Sat 13", "Sun 14", "Mon 15"
    )
    // Filters: first filter now is "session"
    val filters = listOf("session", "Completed", "InProgress")

    // Demo sessions (one sample completed session added)
    val demoList = listOf(
        MeditationItemData(
            title = "Reduce Anxiety",
            subtitle = "calm your brain heath progress growths",
            time = "12:00",
            progress = 0.6f
        ),
        MeditationItemData(
            title = "Deep Meditation",
            subtitle = "Achieve deep calm",
            time = "15:00",
            progress = 1f
        ),
        MeditationItemData(
            title = "Reduce Anxiety",
            subtitle = "calm your brain heath progress growths",
            time = "12:00",
            progress = 0.6f
        )
    )

    // Background gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF121212), Color(0xFF1E1E1E))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Meditation",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Monday, March 12, 2025",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Date Row (custom scrollbar removed)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dateLabels.forEach { label ->
                        StaticDateCircle(
                            label = label,
                            isSelected = label == selectedDate,
                            onClick = { selectedDate = label }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Filter Chips Row (Clickable; now without animation, centered text)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    filters.forEach { filter ->
                        FilterChip(
                            label = filter,
                            isSelected = filter == selectedFilter,
                            onClick = { selectedFilter = filter }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Filter the sessions based on the selected filter and pass the filter to the card
                val filteredSessions = when (selectedFilter) {
                    "Completed" -> demoList.filter { it.progress >= 1f }
                    "InProgress" -> demoList.filter { it.progress < 1f }
                    else -> demoList
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSessions) { item ->
                        MeditationItemCard(item = item, currentFilter = selectedFilter)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Create Session Button (Rounded)
                Button(
                    onClick = { navController?.navigate("MeditationGenerateScreen") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text(text = "Create session", color = Color.White)
                }

            }
        }
    }
}

// Data model includes a progress field
data class MeditationItemData(
    val title: String,
    val subtitle: String,
    val time: String,
    val progress: Float = 0.6f
)

// Static version of DateCircle without animation or size increase
@Composable
fun StaticDateCircle(
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    // Always keep scale at 1f (no size increase)
    val scale = 1f
    val borderColor = if (isSelected) Color(0xFF4CAF50) else Color.White
    val textColor = if (isSelected) Color(0xFF4CAF50) else Color.White

    Box(
        modifier = Modifier
            .size(60.dp)
            .scale(scale)
            .clip(CircleShape)
            .border(BorderStroke(2.dp, borderColor), CircleShape)
            .background(if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

// FilterChip without animation and with centered text and fixed width
@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    // Use static values (no animation)
    val scale = if (isSelected) 1.1f else 1f
    val backgroundColor = if (isSelected) Color(0xFF4CAF50) else Color.Transparent
    val textColor = if (isSelected) Color.Black else Color.White

    Surface(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White),
        color = backgroundColor,
        modifier = Modifier
            .width(100.dp)  // Fixed width
            .height(40.dp)  // Fixed height for chip
            .scale(scale)
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            textAlign = TextAlign.Center
        )
    }
}

// Meditation Item Card now takes a currentFilter parameter to adjust progress display
@Composable
fun MeditationItemCard(item: MeditationItemData, currentFilter: String) {
    var expanded by remember { mutableStateOf(false) }

    // Determine display progress based on current filter:
    // - For "session": do not show progress (but show a static start/play icon)
    // - For "Completed": force progress to 1f (100%)
    // - For "InProgress": force progress to 0.5f (50%)
    val displayProgress = when (currentFilter) {
        "Completed" -> 1f
        "InProgress" -> 0.5f
        else -> item.progress
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (expanded) 1f else displayProgress,
        animationSpec = tween(durationMillis = 1000)
    )
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 360f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )
    val cardElevation by animateFloatAsState(
        targetValue = if (expanded) 8f else 4f,
        animationSpec = tween(durationMillis = 500)
    )
    val borderColor by animateColorAsState(
        targetValue = if (expanded) Color(0xFF4CAF50) else Color.Transparent,
        animationSpec = tween(durationMillis = 500)
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Brush.horizontalGradient(
                colors = if (expanded)
                    listOf(Color(0xFF1E1E1E), Color(0xFF2E2E2E))
                else listOf(Color(0xFF1E1E1E), Color(0xFF1E1E1E))
            ).toColor()
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation.dp),
        border = BorderStroke(width = 2.dp, color = borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (currentFilter != "session") {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
                            CircularProgressIndicator(
                                progress = animatedProgress,
                                strokeWidth = 4.dp,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.fillMaxSize()
                            )
                            val pulseScale by animateFloatAsState(
                                targetValue = if (expanded) 1.2f else 1f,
                                animationSpec = tween(durationMillis = 800)
                            )
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(rotation)
                                    .scale(pulseScale)
                            )
                        }
                    } else {
                        // For "session" filter, show a static play icon
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            // Removed the progress text block as requested.
        }
    }
}

// Extension function: Convert Brush to Color by sampling a middle color.
// (This is a quick workaround; in real apps consider a proper implementation.)
fun Brush.toColor(): Color = Color.Unspecified
