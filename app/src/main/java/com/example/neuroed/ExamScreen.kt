package com.example.neuroed

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.elevatedCardElevation
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay

// Dark mode color palette
private val DarkPrimarys = Color(0xFF6200EE)
private val DarkOnPrimary = Color.White
private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)
private val DarkTopBar = Color(0xFF1A1A1A)  // Dark topbar color
private val DarkError = Color(0xFFCF6679)
private val DarkCardBackground = Color(0xFF2C2C2C)
private val DarkOutline = Color(0xFF444444)
private val DarkText = Color.White
private val DarkTextSecondary = Color(0xFFB0B0B0)

// Dark theme wrapper
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = DarkPrimarys,
            onPrimary = DarkOnPrimary,
            background = DarkBackground,
            surface = DarkSurface,
            onSurface = DarkText,
            error = DarkError,
            outline = DarkOutline,
            surfaceVariant = Color(0xFF323232)
        ),
        typography = Typography(),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(navController: NavController) {
    // State for exam filtering tabs: Pending vs Completed
    var examTabIndex by remember { mutableStateOf(0) }
    val examTabs = listOf("Pending Exam", "Completed Exam")
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Text(
                        text = "Exam",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 24.sp,
                            color = DarkText
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }){
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkText
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = DarkTopBar,
                    titleContentColor = DarkText,
                    navigationIconContentColor = DarkText
                )
            )
        },
        containerColor = DarkBackground,
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .background(DarkBackground)
            ) {
                // TabRow for filtering exam items
                TabRow(
                    selectedTabIndex = examTabIndex,
                    containerColor = DarkSurface,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[examTabIndex]),
                            color = DarkPrimarys
                        )
                    }
                ) {
                    examTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = examTabIndex == index,
                            onClick = { examTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    color = if (examTabIndex == index) DarkPrimarys else DarkTextSecondary
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Wrapping exam items in a scrollable Column with system scroll indicator
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(scrollState)
                    ) {
                        // Adding more subjects for demonstration:
                        ExamItem(
                            subject = "Mathematics",
                            unitTitle = "Unit 1: Algebra Fundamentals",
                            goodProgress = 0.7f,
                            badProgress = 0.2f,
                            averageProgress = 0.5f,
                            excellenceProgress = 0.9f,
                            timeLabel = "2 days 12 hours"
                        )
                        ExamItem(
                            subject = "Physics",
                            unitTitle = "Unit 2: Mechanics",
                            goodProgress = 0.8f,
                            badProgress = 0.1f,
                            averageProgress = 0.4f,
                            excellenceProgress = 0.85f,
                            timeLabel = "3 days 8 hours"
                        )
                        ExamItem(
                            subject = "Chemistry",
                            unitTitle = "Unit 3: Organic Chemistry",
                            goodProgress = 0.6f,
                            badProgress = 0.3f,
                            averageProgress = 0.4f,
                            excellenceProgress = 0.95f,
                            timeLabel = "1 day 20 hours"
                        )
                        ExamItem(
                            subject = "Biology",
                            unitTitle = "Unit 4: Genetics",
                            goodProgress = 0.65f,
                            badProgress = 0.25f,
                            averageProgress = 0.55f,
                            excellenceProgress = 0.88f,
                            timeLabel = "2 days 6 hours"
                        )
                        ExamItem(
                            subject = "History",
                            unitTitle = "Unit 5: World War II",
                            goodProgress = 0.75f,
                            badProgress = 0.15f,
                            averageProgress = 0.45f,
                            excellenceProgress = 0.92f,
                            timeLabel = "3 days 4 hours"
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun ExamItem(
    subject: String,
    unitTitle: String,
    goodProgress: Float,
    badProgress: Float,
    averageProgress: Float,
    excellenceProgress: Float,
    timeLabel: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCardBackground,
            contentColor = DarkText
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Row with subject details on left and profile icon on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = subject,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Chip with a more subtle design
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, DarkPrimarys)
                        ) {
                            Text(
                                text = "100 Marks",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DarkPrimarys,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    // Unit title displayed below the subject
                    Text(
                        text = unitTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DarkTextSecondary
                        )
                    )
                }
                // Profile icon with a subtle glow effect
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(50))
                        .border(2.dp, DarkPrimarys, RoundedCornerShape(50))
                        .padding(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Profile Image",
                        tint = DarkPrimarys,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Enhanced progress visualization
            VerticalProgressWithLabels(
                badProgress = badProgress,
                averageProgress = averageProgress,
                goodProgress = goodProgress,
                excellenceProgress = excellenceProgress
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom row: display countdown timer with action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CountdownTimer(targetMillis = 216_000_000L) // 2 days 12 hours in ms
                Button(
                    onClick = { /* Handle start action */ },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkPrimarys,
                        contentColor = DarkOnPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Text(
                        text = "Start Exam",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VerticalProgressWithLabels(
    badProgress: Float,
    averageProgress: Float,
    goodProgress: Float,
    excellenceProgress: Float
) {
    // Row with four vertical progress items spaced evenly
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        VerticalProgressItem(label = "Bad", progress = badProgress, color = DarkError)
        VerticalProgressItem(label = "Average", progress = averageProgress, color = Color(0xFFE6B422))
        VerticalProgressItem(label = "Good", progress = goodProgress, color = Color(0xFF42A5F5))
        VerticalProgressItem(label = "Excellence", progress = excellenceProgress, color = Color(0xFF66BB6A))
    }
}

@Composable
fun VerticalProgressItem(label: String, progress: Float, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DarkTextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        VerticalProgressIndicator(
            progress = progress,
            modifier = Modifier.height(70.dp),
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                color = color
            )
        )
    }
}

@Composable
fun VerticalProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color,
    backgroundColor: Color = Color(0xFF333333)
) {
    Box(
        modifier = modifier
            .width(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(progress)
                .background(color)
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun CountdownTimer(targetMillis: Long) {
    var remainingTime by remember { mutableStateOf(targetMillis) }
    LaunchedEffect(key1 = targetMillis) {
        while (remainingTime > 0) {
            delay(1000L)
            remainingTime -= 1000L
        }
    }
    val days = remainingTime / (24 * 3600 * 1000)
    val hours = (remainingTime % (24 * 3600 * 1000)) / (3600 * 1000)
    val minutes = (remainingTime % (3600 * 1000)) / (60 * 1000)
    val seconds = (remainingTime % (60 * 1000)) / 1000

    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF333333)
        ) {
            Text(
                text = " ${days}d ${hours}h ${minutes}m ${seconds}s ",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = DarkPrimary
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "remaining",
            style = MaterialTheme.typography.bodySmall.copy(
                color = DarkTextSecondary
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExamScreenPreview() {
    AppTheme {
        ExamScreen(navController = rememberNavController())
    }
}