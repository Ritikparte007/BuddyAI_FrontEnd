package com.example.neuroed

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.neuroed.model.TestQuestionList
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.TestQuestionCreateRepository
import com.example.neuroed.viewmodel.TestQuestionListViewModel
import com.example.neuroed.viewmodel.TestQuestionlistViewModelFactory
import com.google.gson.Gson
import kotlinx.coroutines.delay

// -----------------------------------------------------------------
// Dummy enum for answer status.
enum class AnswerStatus {
    UNCHECKED,
    CORRECT,
    WRONG
}

// -----------------------------------------------------------------
// UI model representing an MCQ question.
data class MCQQuestion(
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String,
    val currentQuestion: Int,
    val totalQuestions: Int,
    val brainXpReward: Int
)

// -----------------------------------------------------------------
// TestQuestionScreen: A wrapper to choose which question type to display.
@Composable
fun TestQuestionScreen(
    navController: NavController,
    questionType: TestType, // Test type enum defined in your project
    id: Int,
    topic: String,
    subtopic: String,
    questionCount: Int,
    subject: String,
    difficulty: String,
    totalQuestion: Int
) {
    when (questionType) {
        TestType.MCQ -> MCQQuestionScreen(
            navController = navController,
            subject = subject,
            difficulty = difficulty,
            topic = topic,
            subtopic = subtopic
        )
        TestType.TRUE_FALSE -> TrueFalseQuestionScreen(navController)
        TestType.FILL_IN_BLANK -> FillInBlankQuestionScreen(navController)
    }
}




// -----------------------------------------------------------------
// MCQQuestionScreen: Display a question from API response (or fallback sample).
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MCQQuestionScreen(
    navController: NavController,
    subject: String,
    difficulty: String,
    topic: String,
    subtopic: String,
) {
    // Get the ApiService instance.
    val apiService = RetrofitClient.apiService
    // Create repository and ViewModel from your separate files.
    val repository = remember { TestQuestionCreateRepository(apiService) }
    val viewModel: TestQuestionListViewModel = viewModel(
        factory = TestQuestionlistViewModelFactory(repository)
    )

    // Observe the parsed response (QuestionResponse) from API.
    val parsedQuestions = viewModel.parsedQuestions.value

    // Trigger the API call when this screen is first composed.
    LaunchedEffect(Unit) {
        // Build the request data.
        val testQuestionData = TestQuestionList(
            subject = subject,
            difficulty = difficulty,
            totalQuestions = 10,  // or pass your totalQuestion value
            timeCountDown = "05:00",
            topic = topic,
            subtopic = subtopic
        )
        viewModel.fetchTestCreateTest(testQuestionData)
    }


    // If the API call is still in progress, show a loading indicator.
    if (parsedQuestions == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return  // Exit the UI function until data is available.
    }

    val question = if (parsedQuestions != null && parsedQuestions.isNotEmpty()) {
        MCQQuestion(
            questionText = parsedQuestions[0].question,
            options = parsedQuestions[0].options,
            correctAnswer = parsedQuestions[0].correct_answer,
            currentQuestion = 1,
            totalQuestions = parsedQuestions.size,
            brainXpReward = 50
        )
    } else {
        MCQQuestion(
            questionText = "Which planet is known as the Red Planet?",
            options = listOf("Earth", "Mars", "Jupiter", "Saturn"),
            correctAnswer = "Mars",
            currentQuestion = 1,
            totalQuestions = 10,
            brainXpReward = 50
        )
    }





    // UI states for interaction.
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var answerStatus by remember { mutableStateOf(AnswerStatus.UNCHECKED) }
    var correctCount by remember { mutableStateOf(0) }
    var wrongCount by remember { mutableStateOf(0) }
    var submissionTimeSec by remember { mutableStateOf(0L) }
    val startTime = remember { System.currentTimeMillis() }

    // Countdown timer (300 seconds = 5 minutes).
    var timeLeft by remember { mutableStateOf(300) }
    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
    }
    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeDisplay = String.format("%02d:%02d", minutes, seconds)

    // Reset answer state after a correct answer.
    LaunchedEffect(answerStatus) {
        if (answerStatus == AnswerStatus.CORRECT) {
            delay(2000L)
            answerStatus = AnswerStatus.UNCHECKED
            selectedOption = null
        }
    }

    // Define a dark color scheme.
    val darkColorScheme = darkColorScheme(
        primary = Color(0xFFBB86FC),
        onPrimary = Color.White,
        secondary = Color(0xFF03DAC6),
        onSecondary = Color.Black,
        background = Color.Black,
        onBackground = Color.White,
        surface = Color(0xFF1E1E1E),
        onSurface = Color.White
    )

    MaterialTheme(colorScheme = darkColorScheme) {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${question.currentQuestion} of ${question.totalQuestions}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Time Left: $timeDisplay",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
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
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = Color.Black
                    )
                )
            },
            containerColor = Color.Black
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                // Display the question.
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Text(
                        text = question.questionText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                // Display the options.
                Column(modifier = Modifier.fillMaxWidth()) {
                    question.options.forEach { option ->
                        val optionBorderColor = when {
                            answerStatus == AnswerStatus.WRONG && selectedOption == option -> Color.Red
                            answerStatus == AnswerStatus.UNCHECKED && selectedOption == option -> MaterialTheme.colorScheme.primary
                            else -> Color.Gray
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    if (answerStatus == AnswerStatus.UNCHECKED) {
                                        selectedOption = option
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(2.dp, optionBorderColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                if (answerStatus != AnswerStatus.UNCHECKED) {
                                    if (option.trim().equals(question.correctAnswer.trim(), ignoreCase = true)) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Correct",
                                            tint = Color.Green
                                        )
                                    } else if (selectedOption == option) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Wrong",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                // "Next" button: submits answer and triggers API call.
                Button(
                    onClick = {
                        submissionTimeSec = (System.currentTimeMillis() - startTime) / 1000
                        if (selectedOption != null &&
                            selectedOption!!.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
                        ) {
                            answerStatus = AnswerStatus.CORRECT
                            correctCount++
                        } else {
                            answerStatus = AnswerStatus.WRONG
                            wrongCount++
                        }
                        // Build the TestQuestionList data model.
                        val testQuestionData = TestQuestionList(
                            subject = subject,
                            difficulty = difficulty,
                            totalQuestions = question.totalQuestions,
                            timeCountDown = timeDisplay,
                            topic = topic,
                            subtopic = subtopic
                        )
                        // Trigger the API call via the ViewModel.
                        viewModel.fetchTestCreateTest(testQuestionData)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Next",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Display submission statistics.
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Correct Submissions: $correctCount",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                    Text(
                        text = "Wrong Submissions: $wrongCount",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                    if (submissionTimeSec > 0) {
                        Text(
                            text = "Time Taken: ${submissionTimeSec}s",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Animated icon on correct answer.
                if (answerStatus == AnswerStatus.CORRECT) {
                    val scaleAnim by animateFloatAsState(
                        targetValue = 1.5f,
                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                    )
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Correct Answer",
                        tint = Color.Green,
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.CenterHorizontally)
                            .graphicsLayer(scaleX = scaleAnim, scaleY = scaleAnim)
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// Placeholder: True/False & Fill in the Blank Screens
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrueFalseQuestionScreen(navController: NavController) {
    MaterialTheme {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = { Text("True/False Question", fontSize = 20.sp, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Black)
                )
            },
            containerColor = Color.Black
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "True/False UI Coming Soon", color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillInBlankQuestionScreen(navController: NavController) {
    MaterialTheme {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = { Text("Fill in the Blank Question", fontSize = 20.sp, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Black)
                )
            },
            containerColor = Color.Black
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Fill in the Blank UI Coming Soon", color = Color.White)
            }
        }
    }
}
