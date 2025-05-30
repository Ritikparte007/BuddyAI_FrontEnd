package com.example.neuroed

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.neuroed.model.TestItem
import com.example.neuroed.model.TestType
import com.example.neuroed.model.TestUpdateRequest
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

private object TS {
    val Purple       = Color(0xFF7F66D3)
    val BgLight      = Color(0xFFF8F9FB)
    val BgDark       = Color(0xFF121212)
    val SurfaceLight = Color.White
    val SurfaceDark  = Color(0xFF1E1E1E)
    val OutlineLight = Color(0xFFC9D2DA)
    val OutlineDark  = Color(0xFF2F3339)
    val TxtLight     = Color(0xFF1F2937)
    val TxtDark      = Color.White
    val TxtLightSub  = Color(0xFF535C64)
    val TxtDarkSub   = Color(0xFFB3B3B3)
}

private data class Question(
    val qText: String,
    val type: TestType,
    val options: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestStartScreen(navController: NavController) {
    val testItem = navController
        .previousBackStackEntry
        ?.savedStateHandle
        ?.get<TestItem>("test")
        ?: return

    // FIXED: Initialize from stored progress
    var currentQuestion by remember { mutableStateOf<Question?>(null) }
    var questionIndex by remember { mutableStateOf(testItem.SolveQuestion) }
    var questionsAnswered by remember { mutableStateOf(testItem.SolveQuestion) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var fillAnswer by remember { mutableStateOf(TextFieldValue("")) }
    var trueFalseAnswer by remember { mutableStateOf<Boolean?>(null) }
    var secsLeft by remember { mutableStateOf(300) }
    var isLoading by remember { mutableStateOf(false) }

    // State variables for test completion
    var isTestCompleted by remember { mutableStateOf(false) }
    var showCompletionAnimation by remember { mutableStateOf(false) }

    // Log the initial state for debugging
    LaunchedEffect(Unit) {
        Log.d("TestStart", "Initial state: SolveQuestion=${testItem.SolveQuestion}, totalQuestion=${testItem.totalQuestion}")
    }

    val gson = remember { Gson() }
    val wsReference = remember { mutableStateOf<WebSocket?>(null) }
    val scrollState = rememberScrollState()

    // Function to update test status
    fun updateTestStatus(solveQuestion: Int, completed: Boolean) {
        // Log what we're trying to update
        Log.d("TestUpdate", "Updating test ${testItem.id} with solveQuestion=$solveQuestion, completed=$completed")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get ApiService
                val apiService = RetrofitClient.apiService

                // Create request with snake_case field names for Django
                val request = TestUpdateRequest(
                    testId = testItem.id,
                    userId = testItem.user_id,
                    solveQuestion = solveQuestion,
                    completed = completed
                )

                // Log the request details before sending
                Log.d("TestUpdate", "Request: test_id=${request.testId}, user_id=${request.userId}, " +
                        "solve_question=${request.solveQuestion}, completed=${request.completed}")

                val response = ApiHelper.executeWithToken { token ->
                    apiService.updateTestStatus(testItem.id, request, token)
                }

                // Log the response
                if (response.success) {
                    Log.d("TestUpdate", "Success: ${response.message}")

                    // FIXED: Update testItem with the value from server if available
                    if (response.solve_question != null) {
                        Log.d("TestUpdate", "Server returned solve_question=${response.solve_question}")

                        withContext(Dispatchers.Main) {
                            // Update the TestItem object
                            testItem.SolveQuestion = response.solve_question

                            // Update state variables if needed
                            if (response.solve_question > questionsAnswered) {
                                questionsAnswered = response.solve_question
                            }
                        }
                    }
                } else {
                    Log.e("TestUpdate", "Failed: ${response.message}")
                }
            } catch (e: Exception) {
                // Log the exception with detailed info
                Log.e("TestUpdate", "Exception: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // Back handler to catch back button presses
    BackHandler {
        if (!isTestCompleted && questionsAnswered > 0) {
            updateTestStatus(questionsAnswered, false) // Not completed
        }
        navController.popBackStack()
    }

    // WebSocket setup
    DisposableEffect(Unit) {
        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()
        val request = Request.Builder()
            .url("ws://localhost:8000/api/TestStart/Question/Create/")
            .build()
        val listener = object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: okhttp3.Response) {
                // FIXED: Include resumeFromQuestion parameter
                val testMap = gson.fromJson(gson.toJson(testItem), Map::class.java) as Map<String, Any>
                val payload = HashMap<String, Any>(testMap)

                // Add resumeFromQuestion parameter
                payload["resumeFromQuestion"] = testItem.SolveQuestion

                Log.d("TestStart", "Opening WebSocket with resumeFromQuestion=${testItem.SolveQuestion}")

                // Send the modified payload
                ws.send(gson.toJson(payload))
            }

            override fun onMessage(ws: WebSocket, text: String) {
                val obj = JsonParser.parseString(text).asJsonObject

                // Log the received message
                Log.d("TestStart", "Received WebSocket message: ${text.take(100)}...")

                // Check if the server indicates test is already completed
                if (obj.has("completed") && obj.get("completed").asBoolean) {
                    Handler(Looper.getMainLooper()).post {
                        isTestCompleted = true
                        showCompletionAnimation = true
                    }
                    return
                }

                val qText = obj["Question"].asString
                val qType = TestType.valueOf(obj["type"].asString)
                val opts = if (obj.has("option")) obj["option"].asJsonArray.map { it.asString } else emptyList()

                // Check for question number from server
                val questionNumber = if (obj.has("questionNumber")) {
                    obj.get("questionNumber").asInt
                } else {
                    questionIndex + 1
                }

                Log.d("TestStart", "Server sent questionNumber=$questionNumber (or using questionIndex+1)")

                Handler(Looper.getMainLooper()).post {
                    // Use the question number from server or increment
                    questionIndex = questionNumber

                    Log.d("TestStart", "Updated questionIndex=$questionIndex")

                    // Only increment questionsAnswered if this is a new question
                    if (questionNumber > questionsAnswered) {
                        questionsAnswered = questionNumber
                        Log.d("TestStart", "Updated questionsAnswered=$questionsAnswered")
                    }

                    // Update current question
                    currentQuestion = Question(qText, qType, opts)
                    selected = null
                    fillAnswer = TextFieldValue("")
                    trueFalseAnswer = null
                    secsLeft = 300
                    isLoading = false

                    // Check if this is the last question
                    if (questionIndex >= testItem.totalQuestion) {
                        isTestCompleted = true
                        showCompletionAnimation = true
                        updateTestStatus(questionsAnswered, true) // Mark as completed
                    }
                }
            }
        }
        wsReference.value = client.newWebSocket(request, listener)

        // Return onDispose lambda
        onDispose {
            wsReference.value?.close(1000, "dispose")

            // Update test status when screen is closed
            if (!isTestCompleted && questionsAnswered > 0) {
                updateTestStatus(questionsAnswered, false) // Not completed
            }
        }
    }

    // Send answer logic
    fun sendAnswer() {
        isLoading = true

        // Check if all questions have been answered
        if (questionIndex >= testItem.totalQuestion) {
            // Show completion animation instead of requesting new question
            isTestCompleted = true
            showCompletionAnimation = true
            updateTestStatus(questionsAnswered, true) // Mark as completed
            return
        }

        val rawAnswer: Map<String, Any?> = when (currentQuestion?.type) {
            TestType.MCQ -> mapOf("answerIndex" to selected)
            TestType.FILL_BLANK, TestType.SHORT_ANSWER -> mapOf("answerText" to fillAnswer.text)
            TestType.TRUE_FALSE -> mapOf("answerBool" to trueFalseAnswer)
            else -> emptyMap()
        }
        val answerPart = rawAnswer.filterValues { it != null }
        val submitPart = mapOf("submit" to true)  // Indicate this is a submission
        val itemMap: Map<String, Any> = gson.fromJson(
            gson.toJson(testItem),
            Map::class.java
        ) as Map<String, Any>
        val payload = mutableMapOf<String, Any>().apply {
            putAll(itemMap)
            putAll(submitPart)
            putAll(answerPart as Map<String, Any>)

            // Add current progress
            put("currentQuestionIndex", questionIndex)
            put("questionsAnswered", questionsAnswered)
        }
        wsReference.value?.send(gson.toJson(payload))
    }

    // Timer
    LaunchedEffect(currentQuestion) {
        if (!isTestCompleted) {
            secsLeft = 300
            while (secsLeft > 0 && !isTestCompleted) {
                delay(1000L)
                secsLeft--
            }
        }
    }

    LaunchedEffect(secsLeft) {
        if (secsLeft == 0 && currentQuestion != null && !isLoading && !isTestCompleted) {
            sendAnswer()
        }
    }

    // Show completion animation after test is completed
    LaunchedEffect(isTestCompleted) {
        if (isTestCompleted) {
            showCompletionAnimation = true
        }
    }

    // Colors based on theme
    val dark = isSystemInDarkTheme()
    val bg = if (dark) TS.BgDark else TS.BgLight
    val surface = if (dark) TS.SurfaceDark else TS.SurfaceLight
    val txtPrimary = if (dark) TS.TxtDark else TS.TxtLight
    val txtSecondary = if (dark) TS.TxtDarkSub else TS.TxtLightSub
    val outline = if (dark) TS.OutlineDark else TS.OutlineLight

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        // Update test progress when navigating back
                        if (!isTestCompleted && questionsAnswered > 0) {
                            updateTestStatus(questionsAnswered, false)
                        }
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = txtPrimary
                        )
                    }
                },
                title = {
                    if (!isTestCompleted) {
                        // FIXED: Show the correct question number accounting for already answered questions
                        Text(
                            if (currentQuestion != null)
                                "Question ${questionIndex}/${testItem.totalQuestion}"
                            else
                                "Loading Question...",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = txtPrimary
                        )
                    } else {
                        Text(
                            "Test Completed",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = txtPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = surface,
                    titleContentColor = txtPrimary,
                    navigationIconContentColor = txtPrimary
                )
            )
        }
    ) { pad ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            // Test questions screen
            AnimatedVisibility(
                visible = !showCompletionAnimation,
                enter = fadeIn(),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Countdown
                    Box(
                        Modifier
                            .align(Alignment.End)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TS.Purple.copy(alpha = .1f))
                            .padding(8.dp)
                    ) {
                        Text("%02d:%02d".format(secsLeft / 60, secsLeft % 60), color = TS.Purple, fontWeight = FontWeight.Bold)
                    }

                    // Question card
                    currentQuestion?.let { q ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = surface),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .animateContentSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(q.qText, fontSize = 16.sp, color = txtPrimary)
                                when (q.type) {
                                    TestType.MCQ -> q.options.forEachIndexed { i, opt ->
                                        Row(
                                            Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.dp, if (selected == i) TS.Purple else outline, RoundedCornerShape(12.dp))
                                                .clickable { selected = i }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = selected == i,
                                                onClick = { selected = i },
                                                colors = RadioButtonDefaults.colors(selectedColor = TS.Purple, unselectedColor = outline)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(opt, fontSize = 14.sp, color = txtPrimary)
                                        }
                                    }
                                    TestType.FILL_BLANK -> OutlinedTextField(
                                        value = fillAnswer,
                                        onValueChange = { fillAnswer = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Type your answer", color = txtSecondary) }
                                    )
                                    TestType.SHORT_ANSWER -> OutlinedTextField(
                                        value = fillAnswer,
                                        onValueChange = { fillAnswer = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 100.dp),
                                        placeholder = { Text("Write a short answer...", color = txtSecondary) }
                                    )
                                    TestType.TRUE_FALSE -> Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Button(
                                            onClick = { trueFalseAnswer = true },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (trueFalseAnswer == true) TS.Purple else TS.Purple.copy(alpha = 0.6f)
                                            )
                                        ) {
                                            Text("True", fontSize = 14.sp)
                                        }
                                        Button(
                                            onClick = { trueFalseAnswer = false },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (trueFalseAnswer == false) TS.Purple else TS.Purple.copy(alpha = 0.6f)
                                            )
                                        ) {
                                            Text("False", fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    } ?: AnimatedVisibility(
                        visible = currentQuestion == null && !isTestCompleted,
                        enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(600)
                        ),
                        exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
                            targetOffsetY = { it / 2 },
                            animationSpec = tween(300)
                        )
                    ) {
                        Box(Modifier.fillMaxSize().height(300.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = TS.Purple)
                            Spacer(Modifier.height(16.dp))
                            Text("Loading question #${questionIndex + 1}...", color = txtSecondary)
                        }
                    }

                    // Next/Submit button
                    Button(
                        onClick = { sendAnswer() },
                        enabled = currentQuestion?.let { q ->
                            when (q.type) {
                                TestType.MCQ -> selected != null
                                TestType.FILL_BLANK -> fillAnswer.text.isNotBlank()
                                TestType.SHORT_ANSWER -> fillAnswer.text.isNotBlank()
                                TestType.TRUE_FALSE -> trueFalseAnswer != null
                            }
                        } == true && !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TS.Purple,
                            contentColor = Color.White,
                            disabledContainerColor = TS.Purple.copy(alpha = .40f),
                            disabledContentColor = Color.White.copy(alpha = .60f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Sending…")
                        } else {
                            if (questionIndex == testItem.totalQuestion - 1) {
                                Text("Finish Test", fontSize = 16.sp)
                            } else {
                                Text("Next", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            // Test completion screen with animation
            AnimatedVisibility(
                visible = showCompletionAnimation,
                enter = fadeIn(animationSpec = tween(800)) + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(800)
                )
            ) {
                TestCompletedScreen(
                    testItem = testItem,
                    questionsAnswered = questionsAnswered,
                    onFinish = {
                        // Make sure test is marked as completed before leaving
                        if (!isTestCompleted) {
                            updateTestStatus(questionsAnswered, true)
                        }
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun TestCompletedScreen(
    testItem: TestItem,
    questionsAnswered: Int,  // Added this parameter
    onFinish: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val txtPrimary = if (dark) TS.TxtDark else TS.TxtLight
    val txtSecondary = if (dark) TS.TxtDarkSub else TS.TxtLightSub

    // Animation states
    var showCheckmark by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    // Trigger animations in sequence
    LaunchedEffect(Unit) {
        showCheckmark = true
        delay(1000)
        showText = true
        delay(800)
        showButton = true
    }

    // Scale animation for checkmark
    val checkmarkScale by animateFloatAsState(
        targetValue = if (showCheckmark) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Checkmark icon with animation
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(60.dp))
                .background(TS.Purple.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = TS.Purple,
                modifier = Modifier
                    .size(80.dp)
                    .scale(checkmarkScale)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Congratulations text with animation
        AnimatedVisibility(
            visible = showText,
            enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                initialOffsetY = { 40 },
                animationSpec = tween(500)
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Test Completed!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = txtPrimary
                )

                Text(
                    "You've successfully completed the test",
                    fontSize = 16.sp,
                    color = txtSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Show actual questions answered vs total
                Text(
                    "Questions answered: $questionsAnswered/${testItem.totalQuestion}",
                    fontSize = 16.sp,
                    color = txtPrimary
                )

                // Show completion status
                val completionPercentage = (questionsAnswered.toFloat() / testItem.totalQuestion.toFloat()) * 100
                Text(
                    when {
                        completionPercentage >= 100 -> "Perfect! You completed the entire test."
                        completionPercentage >= 75 -> "Great job! You've completed most of the test."
                        completionPercentage >= 50 -> "Good progress! You've completed half of the test."
                        else -> "You've started working on this test."
                    },
                    fontSize = 14.sp,
                    color = txtSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Return to tests button with animation
        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                initialOffsetY = { 40 },
                animationSpec = tween(500)
            )
        ) {
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TS.Purple,
                    contentColor = Color.White
                )
            ) {
                Text("Return to Tests", fontSize = 16.sp)
            }
        }
    }
}