package com.example.neuroed // Change to your actual package name

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// --- Screen Route Definitions ---
sealed class Screen(val route: String) {
    object CreateAgent : Screen("create_agent_screen")
    object AgentProcessing : Screen("agent_processing_screen")
}

// --- WebSocket Constants ---
private const val WEBSOCKET_URL = "ws://localhost:8000/api/AgentCreate/" // <-- IMPORTANT: REPLACE WITH YOUR SERVER URL
private const val NORMAL_CLOSURE_STATUS = 1000

// --- Shared WebSocket State ---
class WebSocketState {
    var webSocket by mutableStateOf<WebSocket?>(null)
    var connectionStatus by mutableStateOf("Disconnected")
    var lastServerMessage by mutableStateOf<String?>(null)

    // OkHttp client is created once and reused
    val okHttpClient = OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build()

    // Flag to track if processing should be stopped
    var processingComplete by mutableStateOf(false)

    // Message content being processed
    var currentPrompt by mutableStateOf("")

    // WebSocket Listener implementation
    val webSocketListener = object : WebSocketListener() {
        override fun onOpen(ws: WebSocket, response: Response) {
            connectionStatus = "Connected"
            webSocket = ws
            println("WebSocket: Opened")
        }

        override fun onMessage(ws: WebSocket, text: String) {
            lastServerMessage = "Received: $text"
            println("WebSocket: Received Text: $text")
            // Handle specific messages
            if (text.equals("ok", ignoreCase = true)) {
                println("WebSocket: Received 'ok' confirmation.")
            }
        }

        override fun onMessage(ws: WebSocket, bytes: ByteString) {
            lastServerMessage = "Received bytes: ${bytes.hex()}"
            println("WebSocket: Received Bytes: ${bytes.hex()}")
        }

        override fun onClosing(ws: WebSocket, code: Int, reason: String) {
            println("WebSocket: Closing: $code / $reason")
            connectionStatus = "Closing"
            ws.close(NORMAL_CLOSURE_STATUS, null)
        }

        override fun onClosed(ws: WebSocket, code: Int, reason: String) {
            println("WebSocket: Closed: $code / $reason")
            connectionStatus = "Disconnected"
            webSocket = null
        }

        override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
            println("WebSocket: Error: ${t.message}")
            t.printStackTrace()
            connectionStatus = "Error: ${t.message?.take(30) ?: "Unknown"}"
            webSocket = null
        }
    }

    // Function to connect to WebSocket server
    fun connect() {
        if (webSocket == null || connectionStatus != "Connected") {
            println("WebSocket: Attempting to connect...")
            connectionStatus = "Connecting..."
            val request = Request.Builder().url(WEBSOCKET_URL).build()
            webSocket = okHttpClient.newWebSocket(request, webSocketListener)
        }
    }

    // Function to disconnect WebSocket
    fun disconnect() {
        webSocket?.close(NORMAL_CLOSURE_STATUS, "Closing connection")
        webSocket = null
        connectionStatus = "Disconnected"
    }

    // Function to send message through WebSocket
    fun sendMessage(message: String): Boolean {
        return if (webSocket != null && connectionStatus == "Connected") {
            println("WebSocket: Sending message: $message")
            currentPrompt = message
            webSocket?.send(message) ?: false
        } else {
            println("WebSocket: Cannot send, not connected.")
            false
        }
    }

    // Function to stop agent processing
    fun stopProcessing() {
        processingComplete = true
        // Optionally notify server that processing is stopped
        webSocket?.send("STOP_PROCESSING")
    }
}

// Create a composable to remember our WebSocket state
@Composable
fun rememberWebSocketState(): WebSocketState {
    return remember { WebSocketState() }
}

// --- Screen Composable: CreateAgentScreen ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateAgentScreen(navController: NavController, webSocketState: WebSocketState) {
    // State variables
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tools", "System Tools")
    var activeTools by remember { mutableStateOf(listOf<ToolInfo>()) }
    var promptText by remember { mutableStateOf(TextFieldValue("")) }
    var showEmptyPromptWarning by remember { mutableStateOf(false) }
    var showConnectionWarning by remember { mutableStateOf(false) }

    // Connect to WebSocket when screen is shown
    LaunchedEffect(Unit) {
        webSocketState.connect()
    }

    // Derived state for easy checking
    val activeToolNames = remember(activeTools) { activeTools.map { it.name }.toSet() }

    // Warning Dialog
    if (showEmptyPromptWarning) {
        AlertDialog(
            onDismissRequest = { showEmptyPromptWarning = false },
            title = { Text("Warning") },
            text = { Text("Please write a prompt before starting the agent.") },
            confirmButton = {
                TextButton(onClick = { showEmptyPromptWarning = false }) {
                    Text("OK")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Connection Warning Dialog
    if (showConnectionWarning) {
        AlertDialog(
            onDismissRequest = { showConnectionWarning = false },
            title = { Text("Connection Error") },
            text = { Text("WebSocket is not connected. Please wait for connection or try again.") },
            confirmButton = {
                TextButton(onClick = {
                    showConnectionWarning = false
                    webSocketState.connect() // Try to reconnect
                }) {
                    Text("Reconnect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConnectionWarning = false }) {
                    Text("OK")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Create Agent")
                        Text(
                            text = "WS: ${webSocketState.connectionStatus}",
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                webSocketState.connectionStatus == "Connected" -> Color.Green.copy(alpha = 0.8f)
                                webSocketState.connectionStatus.startsWith("Error") -> MaterialTheme.colorScheme.error
                                else -> LocalContentColor.current.copy(alpha = 0.7f)
                            }
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        bottomBar = {
            FilledTonalButton(
                onClick = {
                    val prompt = promptText.text.trim()
                    if (prompt.isEmpty()) {
                        showEmptyPromptWarning = true
                    } else if (webSocketState.connectionStatus != "Connected") {
                        showConnectionWarning = true
                    } else {
                        // Reset processing state for new session
                        webSocketState.processingComplete = false

                        // Send prompt via WebSocket
                        if (webSocketState.sendMessage(prompt)) {
                            // Navigate to processing screen
                            navController.navigate(Screen.AgentProcessing.route)
                        } else {
                            showConnectionWarning = true
                        }
                    }
                },
                enabled = webSocketState.connectionStatus == "Connected",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(52.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(if (webSocketState.connectionStatus == "Connected") "Start Agent" else "Connecting...")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Prompt Input ---
            item {
                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Agent Prompt") },
                    placeholder = { Text("Describe what the agent should do...") },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 4
                )
            }

            // --- Active Tools Section ---
            if (activeTools.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            "Active Tools",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            activeTools.forEach { tool ->
                                InputChip(
                                    selected = true,
                                    onClick = { /* Removal via trailing icon */ },
                                    label = { Text(tool.name) },
                                    leadingIcon = { ToolIcon(tool.icon, size = InputChipDefaults.IconSize) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Remove ${tool.name}",
                                            modifier = Modifier
                                                .size(InputChipDefaults.IconSize)
                                                .clickable { activeTools = activeTools - tool }
                                        )
                                    },
                                    shape = CircleShape
                                )
                            }
                        }
                    }
                }
            }

            // --- Tool Tabs ---
            item {
                PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }

            // --- Scrollable Tool List ---
            val currentTools = when (selectedTabIndex) {
                0 -> getStandardTools()
                1 -> getSystemTools()
                else -> emptyList()
            }

            items(currentTools, key = { it.name }) { tool ->
                val isActive = activeToolNames.contains(tool.name)
                ToolListItem(
                    tool = tool,
                    isActive = isActive,
                    onActiveChange = { isChecked ->
                        activeTools = if (isChecked) {
                            (activeTools + tool).distinctBy { it.name }
                        } else {
                            activeTools.filterNot { it.name == tool.name }
                        }
                    }
                )
            }

            // --- Optional: Display last received message for debugging ---
            item {
                if (webSocketState.lastServerMessage != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Server: ${webSocketState.lastServerMessage}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentProcessingScreen(
    navController: NavController,
    webSocketState: WebSocketState,
    onStopClick: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val steps = listOf(
        "Agent process your Query....", // Index 0
        "Agent asking for clarification.....",   // Index 1 - CONFUSION STEP
        "Agent clear confusion..."      // Index 2
    )
    val confusionStepIndex = 1 // Define the index for the confusion step

    var confusionInput by remember { mutableStateOf(TextFieldValue("")) }

    // Track clarification questions from the server
    var clarificationQuestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var showProcessingPrompt by remember { mutableStateOf(false) }

    // Store all answers
    var questionAnswers by remember { mutableStateOf<MutableMap<Int, String>>(mutableMapOf()) }

    // Track if we're waiting for new questions after submitting answers
    var waitingForServerResponse by remember { mutableStateOf(false) }

    // --- Stop Action ---
    // Encapsulate the stop logic
    val performStopAction = {
        if (!webSocketState.processingComplete) {
            webSocketState.stopProcessing()
            // Send stop signal to server if needed
            webSocketState.webSocket?.send("STOP")
            // Don't navigate back automatically - let user decide
        }
    }

    // --- Handle individual question submission ---
    val submitCurrentAnswer = {
        val answer = confusionInput.text.trim()
        if (answer.isNotEmpty()) {
            // Store the answer for the current question
            questionAnswers[currentQuestionIndex] = answer

            // Move to next question
            if (currentQuestionIndex < clarificationQuestions.size - 1) {
                currentQuestionIndex++
                confusionInput = TextFieldValue("")
            } else {
                // All questions answered, show processing prompt
                showProcessingPrompt = true
                waitingForServerResponse = true

                // Prepare all answers to send
                val allAnswers = StringBuilder()
                for (i in 0 until clarificationQuestions.size) {
                    allAnswers.append("Q${i+1}: ${clarificationQuestions[i]}\n")
                    allAnswers.append("A${i+1}: ${questionAnswers[i] ?: ""}\n\n")
                }

                // Send all answers at once
                webSocketState.webSocket?.send("CLARIFY:${allAnswers.toString()}")

                // We don't progress to next step yet - we need to wait for server response
                // If the server sends more questions, we'll stay at the clarification step
            }
        }
    }

    // --- Main UI with Scaffold ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agent Processing") },
                navigationIcon = {
                    IconButton(onClick = {
                        performStopAction()
                        navController.navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Stop and Go Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.scrim
            ) {
                Button(
                    onClick = {
                        performStopAction()
                        navController.navigateUp()
                    },
                    enabled = !webSocketState.processingComplete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        disabledContainerColor = Color.White.copy(alpha = 0.5f),
                        disabledContentColor = Color.Gray.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .height(48.dp)
                ) {
                    if (!webSocketState.processingComplete) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                color = LocalContentColor.current,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(
                                text = "Processing...",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = "Stopped",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.scrim
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // Card with Steps
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    steps.forEachIndexed { index, step ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            val circleColor = when {
                                index < currentStep || (index == currentStep && webSocketState.processingComplete) -> MaterialTheme.colorScheme.primary
                                index == currentStep && !webSocketState.processingComplete -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                            val iconColor = when {
                                index < currentStep || (index == currentStep && webSocketState.processingComplete) -> MaterialTheme.colorScheme.onPrimary
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(color = circleColor, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (index < currentStep || (index == currentStep && webSocketState.processingComplete)) {
                                    Icon(Icons.Filled.Check, "Completed", tint = iconColor, modifier = Modifier.size(16.dp))
                                }
                            }

                            Text(
                                text = step,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp,
                                fontWeight = if (index == currentStep && !webSocketState.processingComplete) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }

                        if (index < steps.size - 1) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 11.dp)
                                    .width(2.dp)
                                    .height(24.dp)
                                    .background(if (index < currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Display progress for questions when questions are available
            if (clarificationQuestions.isNotEmpty() && currentStep == confusionStepIndex && !webSocketState.processingComplete && !showProcessingPrompt) {
                Text(
                    text = "Question ${currentQuestionIndex + 1} of ${clarificationQuestions.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LinearProgressIndicator(
                    progress = (currentQuestionIndex.toFloat() / clarificationQuestions.size),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(Modifier.height(16.dp))
            }

            // --- Conditional Input Area for Clarification Questions ---
            AnimatedVisibility(
                visible = currentStep == confusionStepIndex && !webSocketState.processingComplete && clarificationQuestions.isNotEmpty() && !showProcessingPrompt,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Show current question
                    if (clarificationQuestions.isNotEmpty() && currentQuestionIndex < clarificationQuestions.size) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = clarificationQuestions[currentQuestionIndex],
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = confusionInput,
                            onValueChange = { confusionInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Your Answer") },
                            placeholder = { Text("Type your answer here...") },
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 3
                        )

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = submitCurrentAnswer,
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(if (currentQuestionIndex < clarificationQuestions.size - 1) "Next Question" else "Submit Answers")
                        }
                    }
                }
            }

            // --- Processing Prompt after all questions are answered ---
            AnimatedVisibility(
                visible = currentStep == confusionStepIndex && !webSocketState.processingComplete && showProcessingPrompt,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (waitingForServerResponse)
                            "Processing your answers..."
                        else "Answers processed! Moving to next step...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    if (waitingForServerResponse) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Processed",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )

                        // Add a small delay before moving to next step
                        LaunchedEffect(Unit) {
                            delay(1500)
                            currentStep++
                        }
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }

    // --- LaunchedEffect for Step Advancement ---
    LaunchedEffect(webSocketState.processingComplete) {
        if (!webSocketState.processingComplete) {
            while (currentStep < steps.size && !webSocketState.processingComplete) {
                // Auto-advance logic depending on steps
                when (currentStep) {
                    confusionStepIndex -> {
                        // Wait for input at confusion step
                        delay(Long.MAX_VALUE) // Wait until interrupted
                    }
                    else -> {
                        delay(1500) // Normal step delay
                        if (!webSocketState.processingComplete) {
                            if (currentStep < steps.size - 1) {
                                currentStep++
                            } else {
                                currentStep = steps.size
                                webSocketState.stopProcessing()
                            }
                        }
                    }
                }
            }
            if (currentStep >= steps.size && !webSocketState.processingComplete) {
                webSocketState.stopProcessing()
            }
        }
    }

    // Listen for WebSocket messages that might affect our state
    LaunchedEffect(webSocketState.lastServerMessage) {
        // Parse messages from server to potentially advance steps
        val message = webSocketState.lastServerMessage ?: return@LaunchedEffect
        Log.d("WebSocketdata", "Received message: $message")

        when {
            message.contains("STEP:0", ignoreCase = true) -> currentStep = 0
            message.contains("STEP:1", ignoreCase = true) -> currentStep = 1
            message.contains("STEP:2", ignoreCase = true) -> currentStep = 2
            message.contains("COMPLETE", ignoreCase = true) -> webSocketState.stopProcessing()
        }

        // Parse JSON for clarification questions
        if (message.contains("\"clarification_questions\"")) {
            try {
                // Extract the JSON part from the message
                val jsonStartIndex = message.indexOf("{")
                val jsonEndIndex = message.lastIndexOf("}") + 1

                if (jsonStartIndex >= 0 && jsonEndIndex > jsonStartIndex) {
                    val jsonString = message.substring(jsonStartIndex, jsonEndIndex)

                    // Use a JSON parser to extract the clarification questions
                    val jsonObject = JSONObject(jsonString)

                    // Check if intent is now understood
                    val intentUnderstood = jsonObject.optBoolean("intent_understood", false)

                    if (intentUnderstood) {
                        // Intent is understood, move to next step
                        waitingForServerResponse = false
                        showProcessingPrompt = true
                        // Progress will happen after brief delay via the LaunchedEffect
                    } else {
                        // Get new clarification questions
                        val questionsArray = jsonObject.getJSONArray("clarification_questions")

                        val questions = mutableListOf<String>()
                        for (i in 0 until questionsArray.length()) {
                            questions.add(questionsArray.getString(i))
                        }

                        // Reset for new round of questions
                        clarificationQuestions = questions
                        currentQuestionIndex = 0
                        waitingForServerResponse = false
                        showProcessingPrompt = false

                        // Clear previous answers for the new set of questions
                        questionAnswers.clear()
                        confusionInput = TextFieldValue("")
                    }
                }
            } catch (e: Exception) {
                Log.e("WebSocketdata", "Error parsing clarification questions: ${e.message}")
            }
        }
    }
}
// --- Modified NavHost implementation (example) ---
// This would go in your main activity or navigation setup
/*
@Composable
fun AgentNavHost(navController: NavHostController) {
    // Create shared WebSocket state
    val webSocketState = rememberWebSocketState()

    // Clean up WebSocket when the app exits
    DisposableEffect(Unit) {
        onDispose {
            webSocketState.disconnect()
        }
    }

    NavHost(navController = navController, startDestination = Screen.CreateAgent.route) {
        composable(Screen.CreateAgent.route) {
            CreateAgentScreen(navController, webSocketState)
        }
        composable(Screen.AgentProcessing.route) {
            AgentProcessingScreen(navController, webSocketState)
        }
        // Other routes...
    }
}
*/

// --- Support Code (Data, Helpers, ListItems) ---
data class ToolInfo(val name: String, val icon: ImageVector)

fun getStandardTools(): List<ToolInfo> = listOf(
    ToolInfo("Web Browser", Icons.Filled.Email),
    ToolInfo("Gmail", Icons.Filled.Email),
    ToolInfo("Map Search", Icons.Filled.Email),
    ToolInfo("GitHub", Icons.Filled.Email),
    ToolInfo("PDF Creator", Icons.Filled.Email),
    ToolInfo("Social Post (X)", Icons.Filled.Share)
)

fun getSystemTools(): List<ToolInfo> = listOf(
    ToolInfo("Camera", Icons.Filled.Email),
    ToolInfo("File Manager", Icons.Filled.Email),
    ToolInfo("Notifications", Icons.Filled.Notifications),
    ToolInfo("Microphone", Icons.Filled.Email),
    ToolInfo("Calendar", Icons.Filled.Email),
    ToolInfo("Background Task", Icons.Filled.Settings)
)

@Composable
fun ToolIcon(iconVector: ImageVector, modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
    Icon(
        imageVector = iconVector,
        contentDescription = null,
        modifier = modifier.size(size),
        tint = tint
    )
}

@Composable
fun ToolListItem(
    tool: ToolInfo,
    isActive: Boolean,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(tool.name, style = MaterialTheme.typography.bodyLarge) },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                ToolIcon(
                    tool.icon,
                    size = 24.dp,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        },
        trailingContent = {
            Switch(
                checked = isActive,
                onCheckedChange = onActiveChange
            )
        },
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onActiveChange(!isActive) },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    )
}