package com.example.neuroed

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.neuroed.network.RetrofitClient.apiService
import com.example.neuroed.repository.TestListRepository
import com.example.neuroed.viewmodel.TestListViewModel
import com.example.neuroed.viewmodel.TestListViewModelFactory
import kotlinx.coroutines.delay

// Define the different test types.
enum class TestType {
    MCQ,
    TRUE_FALSE,
    FILL_IN_BLANK
}

// Updated UI model for each test item with all required fields.
data class TestItem(
    val subject: String,       // The subject name from API (Subject)
    val difficulty: String,    // Difficulty from API (Difficulty)
    val questionCount: Int,    // Question count from API (TotalQuestion)
    val coins: Int,            // Not provided by API; default value
    val timeLeft: String,      // Time countdown from API (TimeCountDown)
    val note: String,          // We build note using Topic, Subtopic, and Quotes
    val testType: TestType,    // Default value if API doesn't supply test type
    val topic: String,         // Topic from API
    val subtopic: String,      // Subtopic from API
    val totalQuestion: Int     // Total question count (could be same as questionCount)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestsScreen(
    navController: NavController,
    viewModel: TestListViewModel = viewModel(
        factory = TestListViewModelFactory(
            // Provide your repository instance with your Retrofit API service.
            TestListRepository(apiService)
        )
    )
) {
    // Theme colors - pure dark mode
    val backgroundColor = Color(0xFF121212)
    val cardColor = Color(0xFF1E1E1E)
    val accentColor = Color(0xFF808080) // Neutral gray instead of blue/green
    val textColor = Color.White
    val secondaryTextColor = Color(0xFFB0B0B0)

    // Launch API call when the composable is first composed (using userId = 1).
    LaunchedEffect(Unit) {
        viewModel.fetchTestList(userId = 1)
    }

    // Collect the test list state from the ViewModel.
    // Here, viewModel.testList is assumed to be State<List<TestList>?> from your API model.
    val apiTestList = viewModel.testList.value

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            // Left-aligned title in TopAppBar
            TopAppBar(
                title = { Text("Tests", color = textColor, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(colors = listOf(Color(0xFF121212), Color(0xFF0D0D0D)))
                )
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (apiTestList.isNullOrEmpty()) {
                    // Show an empty state with an icon and descriptive text when API data is null or empty.
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "No Data",
                                tint = secondaryTextColor,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No tests available",
                                color = secondaryTextColor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    // Map API data to our UI model.
                    val testItems = apiTestList.map { apiItem ->
                        TestItem(
                            subject = apiItem.Subject,
                            difficulty = apiItem.Difficulty,
                            questionCount = apiItem.TotalQuestion,
                            coins = 0, // Default value
                            timeLeft = apiItem.TimeCountDown,
                            note = "${apiItem.Topic} - ${apiItem.Subtopic}\n${apiItem.Quotes}",
                            testType = TestType.MCQ, // Default value (update if necessary)
                            topic = apiItem.Topic,
                            subtopic = apiItem.Subtopic,
                            totalQuestion = apiItem.TotalQuestion
                        )
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(testItems) { item ->
                            ModernTestCard(
                                item = item,
                                cardColor = cardColor,
                                accentColor = accentColor,
                                textColor = textColor,
                                secondaryTextColor = secondaryTextColor,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipRow(
    selectedFilter: TestType,
    onFilterSelected: (TestType) -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilterChip(
            selected = selectedFilter == TestType.MCQ,
            onClick = { onFilterSelected(TestType.MCQ) },
            label = { Text("MCQ") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF333333),
                selectedLabelColor = Color.White,
                containerColor = Color(0xFF1E1E1E),
                labelColor = Color.LightGray
            )
        )
        FilterChip(
            selected = selectedFilter == TestType.TRUE_FALSE,
            onClick = { onFilterSelected(TestType.TRUE_FALSE) },
            label = { Text("True/False") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF333333),
                selectedLabelColor = Color.White,
                containerColor = Color(0xFF1E1E1E),
                labelColor = Color.LightGray
            )
        )
        FilterChip(
            selected = selectedFilter == TestType.FILL_IN_BLANK,
            onClick = { onFilterSelected(TestType.FILL_IN_BLANK) },
            label = { Text("Fill") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF333333),
                selectedLabelColor = Color.White,
                containerColor = Color(0xFF1E1E1E),
                labelColor = Color.LightGray
            )
        )
    }
}

@Composable
fun CountdownTimer(
    initialSeconds: Int,
    textColor: Color,
    fontWeight: FontWeight
) {
    var secondsLeft by remember { mutableStateOf(initialSeconds) }
    LaunchedEffect(key1 = initialSeconds) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        }
    }
    val hours = secondsLeft / 3600
    val minutes = (secondsLeft % 3600) / 60
    val seconds = secondsLeft % 60
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2D2D2D))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = fontWeight),
            color = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTestCard(
    item: TestItem,
    cardColor: Color,
    accentColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    navController: NavController
) {
    val targetProgress = 0.6f  // Dummy progress value (60%)
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1000)
    )
    val progressPercentage = (animatedProgress * 100).toInt()
    var expanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .border(
                width = 1.dp,
                color = Color(0xFF333333),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(4.dp))
                ) {
                    // Ensure R.drawable.biology exists in your resources.
                    Image(
                        painter = painterResource(id = R.drawable.biology),
                        contentDescription = "Test Icon",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.subject,
                            color = textColor,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { /* handle per-card analysis click */ },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Analysis",
                                tint = secondaryTextColor
                            )
                        }
                    }
                    Text(
                        text = "Difficulty: ${item.difficulty}",
                        color = secondaryTextColor,
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Questions: 00/${item.questionCount}",
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF555555),
                    trackColor = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$progressPercentage% - ${when {
                        animatedProgress < 0.4f -> "Low"
                        animatedProgress < 0.7f -> "Medium"
                        else -> "Good"
                    }}",
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    CountdownTimer(
                        initialSeconds = 3600,
                        textColor = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.note,
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        // Build the navigation route with all parameters.
                        val route = "TestQuestionScreen/" +
                                "${item.testType.name}/" +
                                "1/" +  // Dummy id value; replace if available.
                                "${item.topic}/" +
                                "${item.subtopic}/" +
                                "${item.questionCount}/" +
                                "${item.subject}/" +
                                "${item.difficulty}/" +
                                "${item.totalQuestion}"
                        navController.navigate(route)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Start Test",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color(0xFF333333), thickness = 1.dp)
            TextButton(onClick = { expanded = !expanded }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (expanded) "Show Less" else "Show More",
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier
                            .rotate(rotationAngle)
                            .size(16.dp),
                        tint = Color.White
                    )
                }
            }
            if (expanded) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Coins: ${item.coins}",
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Time Left: ${item.timeLeft}",
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}