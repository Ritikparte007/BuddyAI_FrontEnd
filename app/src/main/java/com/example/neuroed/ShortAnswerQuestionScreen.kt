package com.example.neuroed

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// No internal weight imports needed.
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

data class ShortAnswerQuestion(
    val questionText: String,
    val correctAnswer: String,
    val currentQuestion: Int,
    val totalQuestions: Int,
    val brainXpReward: Int
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortAnswerQuestionScreen(navController: NavController) {
    // Sample Short Answer question
    val question = ShortAnswerQuestion(
        questionText = "What is the capital of France?",
        correctAnswer = "Paris",
        currentQuestion = 1,
        totalQuestions = 10,
        brainXpReward = 50
    )

    // State for the user's answer input and answer status
    var userAnswer by remember { mutableStateOf("") }
    var answerStatus by remember { mutableStateOf(AnswerStatus.UNCHECKED) }

    // Submission statistics tracking
    var correctCount by remember { mutableStateOf(0) }
    var wrongCount by remember { mutableStateOf(0) }
    var submissionTimeSec by remember { mutableStateOf(0L) }

    // Record the start time when the question appears
    val startTime = remember { System.currentTimeMillis() }

    // 5-minute countdown timer (300 seconds)
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

    // Dark color scheme for a modern look
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

    // When the answer is correct, reset the input after a delay (simulate moving to next question)
    LaunchedEffect(answerStatus) {
        if (answerStatus == AnswerStatus.CORRECT) {
            delay(2000L)
            answerStatus = AnswerStatus.UNCHECKED
            userAnswer = ""
        }
    }

    MaterialTheme(colorScheme = darkColorScheme) {
        Scaffold(
            topBar = {
                // TopAppBar: left shows progress; right shows countdown timer
                SmallTopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${question.currentQuestion} to ${question.totalQuestions}",
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
                    colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Black)
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                // Card for displaying the question text
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
                // OutlinedTextField for short answer input
                OutlinedTextField(
                    value = userAnswer,
                    onValueChange = {
                        userAnswer = it
                        if (answerStatus == AnswerStatus.WRONG) {
                            answerStatus = AnswerStatus.UNCHECKED
                        }
                    },
                    label = { Text("Your Answer", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = if (answerStatus == AnswerStatus.WRONG) Color.Red else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (answerStatus == AnswerStatus.WRONG) Color.Red else Color.Gray,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        containerColor = Color.Transparent,
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                // Submit button for checking the answer
                Button(
                    onClick = {
                        submissionTimeSec = (System.currentTimeMillis() - startTime) / 1000
                        if (userAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)) {
                            answerStatus = AnswerStatus.CORRECT
                            correctCount++
                        } else {
                            answerStatus = AnswerStatus.WRONG
                            wrongCount++
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Submit",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Display submission statistics
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
                // Overall green checkmark animation for correct answer feedback
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
                            .graphicsLayer(scaleX = scaleAnim, scaleY = scaleAnim)
                    )
                }
            }
        }
    }
}
