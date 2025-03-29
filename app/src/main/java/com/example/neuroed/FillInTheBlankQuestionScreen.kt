package com.example.neuroed

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.text.LocalTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay


data class FillInTheBlankQuestion(
    val questionText: String,
    val correctAnswer: String,
    val currentQuestion: Int,
    val totalQuestions: Int,
    val brainXpReward: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillInTheBlankQuestionScreen(navController: NavController) {
    // Sample question
    val question = FillInTheBlankQuestion(
        questionText = "The process by which plants convert sunlight into energy is called ____. ",
        correctAnswer = "Photosynthesis",
        currentQuestion = 2,
        totalQuestions = 10,
        brainXpReward = 0
    )

    // State for the user's answer and its status (unchecked, correct, wrong)
    var userAnswer by remember { mutableStateOf("") }
    var answerStatus by remember { mutableStateOf(AnswerStatus.UNCHECKED) }

    // Stats tracking
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

    // If the answer is correct, clear the input after a delay (simulate moving to the next question)
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
                // TopAppBar with a back arrow
                SmallTopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Question progress
                            Text(
                                text = "${question.currentQuestion} to ${question.totalQuestions}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            // Right: Countdown timer
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
            containerColor = Color.Black // Entire background is black
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black)
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Card for question text
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
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

                // OutlinedTextField for user input.
                // If the answer is wrong, the border color changes to red.
                val borderColor = if (answerStatus == AnswerStatus.WRONG) Color.Red else MaterialTheme.colorScheme.primary
                OutlinedTextField(
                    value = userAnswer,
                    onValueChange = {
                        userAnswer = it
                        // Remove error state as user begins correcting their answer
                        if (answerStatus == AnswerStatus.WRONG) {
                            answerStatus = AnswerStatus.UNCHECKED
                        }
                    },
                    label = { Text("Your answers", color = Color.White) },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = borderColor,
                        unfocusedBorderColor = if (answerStatus == AnswerStatus.WRONG) Color.Red else Color.Gray,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        containerColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Next button for moving to the next question.
                // On click, the answer is validated, and submission stats are recorded.
                Button(
                    onClick = {
                        // Calculate submission time (in seconds) for this attempt
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Next",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display submission statistics below the Next button
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Correct Submissions: $correctCount",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                    Text(
                        text = "Wrong Submissions: $wrongCount",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                    // Only show submission time if a submission has been made
                    if (submissionTimeSec > 0) {
                        Text(
                            text = "Time Taken: ${submissionTimeSec}s",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // If the answer is correct, display a green checkmark animation.
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
