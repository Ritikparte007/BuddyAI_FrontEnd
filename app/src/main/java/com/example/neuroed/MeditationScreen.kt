package com.example.neuroed

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// Define colors directly to avoid redeclaration
private val backgroundDark = Color(0xFF121212)
private val surfaceDark = Color(0xFF1E1E1E)
private val primaryPurple = Color(0xFF9C27B0)
private val secondaryPurple = Color(0xFF673AB7)
private val accentPurple = Color(0xFFE040FB)
private val textPrimaryDark = Color.White
private val textSecondaryDark = Color(0xFFB3B3B3)
private val cardGradientStartDark = Color(0xFF202020)
private val cardGradientEndDark = Color(0xFF2D2D2D)
private val completedGreen = Color(0xFF4CAF50)
private val inProgressAmber = Color(0xFFFFC107)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeditationScreen(
    navController: NavController? = null
) {
    var selectedFilter by remember { mutableStateOf("In Progress") }
    var selectedDate by remember { mutableStateOf("Mon 1") }

    // More days for horizontal scrolling
    val dateLabels = listOf(
        "Mon 1", "Tue 2", "Wed 3", "Thu 4", "Fri 5", "Sat 6", "Sun 7",
        "Mon 8", "Tue 9", "Wed 10", "Thu 11", "Fri 12", "Sat 13", "Sun 14", "Mon 15"
    )

    // Filters with consistent naming
    val filters = listOf("All", "Completed", "In Progress")

    // Demo sessions
    val demoList = listOf(
        MeditationItemData(
            title = "Reduce Anxiety",
            subtitle = "Calm your brain health progress",
            time = "12:00",
            progress = 0.6f
        ),
        MeditationItemData(
            title = "Focus Training",
            subtitle = "Improve concentration and mental clarity",
            time = "10:00",
            progress = 0.3f
        ),
        MeditationItemData(
            title = "Sleep Preparation",
            subtitle = "Gentle relaxation for better sleep",
            time = "20:00",
            progress = 0.8f
        ),
        MeditationItemData(
            title = "Deep Meditation",
            subtitle = "Achieve deep calm and tranquility",
            time = "15:00",
            progress = 1f
        ),
        MeditationItemData(
            title = "Mindful Breathing",
            subtitle = "Connect with your breath for presence",
            time = "8:00",
            progress = 1f
        )
    )

    // Background gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            backgroundDark,
            backgroundDark.copy(alpha = 0.95f)
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Meditation",
                        color = textPrimaryDark,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundDark)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Monday, March 12, 2025",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondaryDark
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Scrollable Date Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    dateLabels.forEach { label ->
                        StaticDateCircle(
                            label = label,
                            isSelected = label == selectedDate,
                            onClick = { selectedDate = label }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Filter Tabs with sliding indicator
                LineIndicatorTabs(
                    tabs = filters,
                    selectedTab = selectedFilter,
                    onTabSelected = { selectedFilter = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Filter the sessions based on the selected filter
                val filteredSessions = when (selectedFilter) {
                    "Completed" -> demoList.filter { it.progress >= 1f }
                    "In Progress" -> demoList.filter { it.progress > 0f && it.progress < 1f }
                    else -> demoList
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredSessions) { item ->
                        MeditationItemCard(item = item)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Create Session Button
                Button(
                    onClick = { navController?.navigate("MeditationGenerateScreen") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
                ) {
                    Text(
                        text = "Create New Session",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun LineIndicatorTabs(
    tabs: List<String>,
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    // Simplified tab implementation to match the screenshot
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            tabs.forEach { tab ->
                val isSelected = tab == selectedTab
                val textColor = if (isSelected) primaryPurple else textSecondaryDark
                val fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = textColor,
                        fontWeight = fontWeight,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Indicator line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(textSecondaryDark.copy(alpha = 0.2f))
        ) {
            // Position the indicator correctly under the selected tab
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            ) {
                val startPercentage = when(selectedTab) {
                    "All" -> 0f
                    "Completed" -> 0.33f
                    "In Progress" -> 0.66f
                    else -> 0f
                }

                val endPercentage = when(selectedTab) {
                    "All" -> 0.33f
                    "Completed" -> 0.66f
                    "In Progress" -> 1f
                    else -> 0.33f
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(endPercentage)
                        .width(IntrinsicSize.Max)
                        .height(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .background(
                                if (selectedTab == "In Progress") primaryPurple else Color.Transparent
                            )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.66f)
                        .width(IntrinsicSize.Max)
                        .height(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .background(
                                if (selectedTab == "Completed") primaryPurple else Color.Transparent
                            )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.33f)
                        .width(IntrinsicSize.Max)
                        .height(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .background(
                                if (selectedTab == "All") primaryPurple else Color.Transparent
                            )
                    )
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

// Enhanced date circle with dark theme styling
@Composable
fun StaticDateCircle(
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val borderColor = if (isSelected) primaryPurple else textSecondaryDark.copy(alpha = 0.3f)
    val textColor = if (isSelected) primaryPurple else textSecondaryDark
    val backgroundColor = if (isSelected) primaryPurple.copy(alpha = 0.15f) else Color.Transparent

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .border(BorderStroke(1.5.dp, borderColor), CircleShape)
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// Simplified meditation card to match the screenshot
@Composable
fun MeditationItemCard(item: MeditationItemData) {
    // Determine status color
    val statusColor = when {
        item.progress >= 1f -> completedGreen
        item.progress > 0f -> inProgressAmber
        else -> textSecondaryDark
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardGradientStartDark),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Title and subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = textPrimaryDark,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondaryDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Action button and time
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            inProgressAmber.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = inProgressAmber,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondaryDark
                )
            }
        }
    }
}