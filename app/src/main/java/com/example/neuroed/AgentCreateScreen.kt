package com.example.neuroed

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.TimeUnit
import okhttp3.Request

// --- Enhanced Screen Route Definitions ---
sealed class Screen(val route: String) {
    object CreateAgent : Screen("create_agent_screen")
    object AgentProcessing : Screen("agent_processing_screen")
    object AgentPlanning : Screen("agent_planning_screen")
    object AgentExecution : Screen("agent_execution_screen")
    object AgentSuccess : Screen("agent_success_screen")
}

// --- WebSocket Constants ---
private const val WEBSOCKET_URL = "ws://localhost:8000/api/AgentCreate/"
private const val NORMAL_CLOSURE_STATUS = 1000

// --- Enhanced Models ---
data class ToolInfo(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val description: String = ""
)

data class ExecutionStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val actionType: String,
    val toolNeeded: String,
    val inputsRequired: List<String>,
    val expectedOutput: String,
    val successCriteria: String,
    val errorHandling: String,
    val estimatedSeconds: Int,
    val status: StepStatus = StepStatus.PENDING
)

enum class StepStatus {
    PENDING, IN_PROGRESS, COMPLETED, FAILED
}

data class PlanningData(
    val taskType: String,
    val complexityLevel: String,
    val estimatedDurationMinutes: Int,
    val totalSteps: Int,
    val roadmap: List<ExecutionStep>,
    val executionPrompt: String,
    val requiredContext: List<String>,
    val planId: String,
    val createdAt: String
)

data class ExecutionContext(
    val contextItems: Map<String, String> = emptyMap()
)

fun getToolIcon(toolId: String): ImageVector {
    return when (toolId) {
        "email" -> Icons.Filled.Email
        "pdf_creator" -> Icons.Filled.Email
        "github_api" -> Icons.Filled.DateRange
        "weather" -> Icons.Filled.Build
        "scheduler" -> Icons.Filled.DateRange
        "web_browser" -> Icons.Filled.Email
        "selenium" -> Icons.Filled.Email
        "screenshot" -> Icons.Filled.Refresh
        else -> Icons.Filled.Build
    }
}

fun getActionTypeIcon(actionType: String): ImageVector {
    return when (actionType.lowercase()) {
        "navigate" -> Icons.Filled.Navigation
        "click" -> Icons.Filled.TouchApp
        "type" -> Icons.Filled.Keyboard
        "wait" -> Icons.Filled.Schedule
        "screenshot" -> Icons.Filled.Screenshot
        "api_call" -> Icons.Filled.Api
        "data_process" -> Icons.Filled.DataUsage
        "analyze" -> Icons.Filled.Analytics
        "execute" -> Icons.Filled.PlayArrow
        "verify" -> Icons.Filled.Verified
        else -> Icons.Filled.Build
    }
}

// --- Enhanced WebSocket State ---
class WebSocketState {
    var webSocket by mutableStateOf<WebSocket?>(null)
    var connectionStatus by mutableStateOf("Disconnected")
    var lastServerMessage by mutableStateOf<String?>(null)

    // Tool-related state
    var availableTools by mutableStateOf<Map<String, ToolInfo>>(emptyMap())
    var selectedTools by mutableStateOf<Set<String>>(emptySet())

    // Intent understanding state
    var intentUnderstood by mutableStateOf(false)
    var agentGoal by mutableStateOf<String?>(null)
    var toolRequirements by mutableStateOf<Map<String, String>>(emptyMap())
    var clarificationQuestions by mutableStateOf<List<String>>(emptyList())

    // Processing state
    var processingComplete by mutableStateOf(false)
    var currentStep by mutableStateOf(0)
    var currentPrompt by mutableStateOf("")
    var waitingForClarification by mutableStateOf(false)
    var isSubmittingClarification by mutableStateOf(false) // NEW: Added for better UX

    // Planning state
    var planningCreated by mutableStateOf(false)
    var planningData by mutableStateOf<PlanningData?>(null)
    var contextNeeded by mutableStateOf<List<String>>(emptyList())
    var readyForExecution by mutableStateOf(false)
    var executionContext by mutableStateOf<ExecutionContext>(ExecutionContext())

    // Execution state
    var executionStarted by mutableStateOf(false)
    var currentExecutionStep by mutableStateOf(0)
    var executionSteps by mutableStateOf<List<ExecutionStep>>(emptyList())
    var executionCompleted by mutableStateOf(false)

    // OkHttp client is created once and reused
    val okHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    // Enhanced WebSocket Listener implementation
    val webSocketListener = object : WebSocketListener() {
        override fun onOpen(ws: WebSocket, response: Response) {
            connectionStatus = "Connected"
            webSocket = ws
            println("WebSocket: Opened")
        }

        override fun onMessage(ws: WebSocket, text: String) {
            lastServerMessage = text
            println("WebSocket: Received Text: $text")
            Log.d("WebSocketState", "Received message: $text")

            try {
                val jsonMessage = JSONObject(text)
                println("DEBUG: Parsed JSON successfully")

                // Handle different message types
                if (jsonMessage.has("type")) {
                    val messageType = jsonMessage.getString("type")
                    println("DEBUG: Message type: $messageType")

                    when (messageType) {
                        "available_tools" -> {
                            println("DEBUG: Handling available tools")
                            handleAvailableTools(jsonMessage)
                            return
                        }
                        "tools_updated" -> {
                            println("DEBUG: Handling tools updated")
                            handleToolsUpdated(jsonMessage)
                            return
                        }
                        "intent_response" -> {
                            println("DEBUG: Handling intent response")
                            handleIntentResponse(jsonMessage)
                            return
                        }
                        "execution_started" -> handleExecutionStarted(jsonMessage)
                        "step_started" -> handleStepStarted(jsonMessage)
                        "step_completed" -> handleStepCompleted(jsonMessage)
                        "execution_completed" -> handleExecutionCompleted(jsonMessage)
                        "context_received" -> handleContextReceived(jsonMessage)
                        "error" -> handleError(jsonMessage)
                    }
                    return
                }

                // Legacy intent understanding handling
                println("DEBUG: Handling legacy intent response")
                handleLegacyIntentResponse(jsonMessage)

            } catch (e: Exception) {
                Log.e("WebSocketState", "Error parsing message: ${e.message}")
                println("DEBUG: Error parsing JSON: ${e.message}")
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

    // Message handling functions
    private fun handleAvailableTools(jsonMessage: JSONObject) {
        val toolsObj = jsonMessage.getJSONObject("tools")
        val toolsMap = mutableMapOf<String, ToolInfo>()

        val keys = toolsObj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val toolData = toolsObj.getJSONObject(key)
            val name = toolData.getString("name")
            val description = toolData.getString("description")
            toolsMap[key] = ToolInfo(key, name, getToolIcon(key), description)
        }

        availableTools = toolsMap
    }

    private fun handleToolsUpdated(jsonMessage: JSONObject) {
        if (jsonMessage.has("selected_tools")) {
            val toolsArray = jsonMessage.getJSONArray("selected_tools")
            val tools = mutableSetOf<String>()
            for (i in 0 until toolsArray.length()) {
                tools.add(toolsArray.getString(i))
            }
            selectedTools = tools
        }
    }

    private fun handleIntentResponse(jsonMessage: JSONObject) {
        println("DEBUG: Handling intent response: $jsonMessage")

        // Reset clarification submission state
        isSubmittingClarification = false

        intentUnderstood = jsonMessage.getBoolean("intent_understood")

        if (intentUnderstood) {
            println("DEBUG: Intent understood!")
            agentGoal = jsonMessage.optString("goal", null)

            // Extract tool requirements
            if (jsonMessage.has("tool_requirements")) {
                val requirementsObj = jsonMessage.getJSONObject("tool_requirements")
                val requirementsMap = mutableMapOf<String, String>()
                val reqKeys = requirementsObj.keys()
                while (reqKeys.hasNext()) {
                    val key = reqKeys.next()
                    requirementsMap[key] = requirementsObj.getString(key)
                }
                toolRequirements = requirementsMap
            }

            // Handle planning data
            if (jsonMessage.has("planning_created") && jsonMessage.getBoolean("planning_created")) {
                planningCreated = true
                contextNeeded = extractStringList(jsonMessage, "context_needed")
                readyForExecution = jsonMessage.optBoolean("ready_for_execution", false)

                val planningData = PlanningData(
                    taskType = jsonMessage.optString("task_type", "general"),
                    complexityLevel = jsonMessage.optString("complexity_level", "medium"),
                    estimatedDurationMinutes = jsonMessage.optInt("estimated_duration_minutes", 5),
                    totalSteps = jsonMessage.optInt("total_steps", 1),
                    roadmap = extractRoadmap(jsonMessage),
                    executionPrompt = jsonMessage.optString("execution_prompt", ""),
                    requiredContext = contextNeeded,
                    planId = jsonMessage.optString("plan_id", ""),
                    createdAt = jsonMessage.optString("created_at", "")
                )

                this.planningData = planningData
                executionSteps = planningData.roadmap
            }

            processingComplete = true
            currentStep = if (planningCreated) 3 else 2
            waitingForClarification = false

        } else {
            // Intent नहीं समझा, clarification चाहिए
            println("DEBUG: Intent not understood, need clarification")

            // Clear previous clarification questions
            clarificationQuestions = emptyList()

            if (jsonMessage.has("clarification_questions")) {
                val questionsArray = jsonMessage.getJSONArray("clarification_questions")
                val questions = mutableListOf<String>()

                println("DEBUG: Found ${questionsArray.length()} clarification questions")

                for (i in 0 until questionsArray.length()) {
                    val question = questionsArray.getString(i)
                    questions.add(question)
                    println("DEBUG: Question $i: $question")
                }

                clarificationQuestions = questions
                waitingForClarification = true
                currentStep = 1
                processingComplete = false

                println("DEBUG: Set clarificationQuestions to: $clarificationQuestions")
                println("DEBUG: waitingForClarification: $waitingForClarification")
            } else {
                println("DEBUG: No clarification_questions field found in response")
            }
        }
    }

    private fun handleLegacyIntentResponse(jsonMessage: JSONObject) {
        println("DEBUG: Handling legacy intent response")

        if (jsonMessage.has("intent_understood")) {
            isSubmittingClarification = false
            intentUnderstood = jsonMessage.getBoolean("intent_understood")

            if (intentUnderstood) {
                agentGoal = jsonMessage.optString("goal", null)

                if (jsonMessage.has("tool_requirements")) {
                    val requirementsObj = jsonMessage.getJSONObject("tool_requirements")
                    val requirementsMap = mutableMapOf<String, String>()
                    val reqKeys = requirementsObj.keys()
                    while (reqKeys.hasNext()) {
                        val key = reqKeys.next()
                        requirementsMap[key] = requirementsObj.getString(key)
                    }
                    toolRequirements = requirementsMap
                }

                processingComplete = true
                currentStep = 2
                waitingForClarification = false
            } else {
                // Clarification needed
                clarificationQuestions = emptyList() // Clear previous

                if (jsonMessage.has("clarification_questions")) {
                    val questionsArray = jsonMessage.getJSONArray("clarification_questions")
                    val questions = mutableListOf<String>()

                    println("DEBUG: Legacy - Found ${questionsArray.length()} questions")

                    for (i in 0 until questionsArray.length()) {
                        val question = questionsArray.getString(i)
                        questions.add(question)
                        println("DEBUG: Legacy Question $i: $question")
                    }

                    clarificationQuestions = questions
                    waitingForClarification = true
                    currentStep = 1
                    processingComplete = false
                }
            }
        }
    }

    private fun handleExecutionStarted(jsonMessage: JSONObject) {
        executionStarted = true
        currentExecutionStep = 0
        if (jsonMessage.has("roadmap")) {
            executionSteps = extractRoadmap(jsonMessage)
        }
    }

    private fun handleStepStarted(jsonMessage: JSONObject) {
        val stepNumber = jsonMessage.optInt("step_number", 0)
        if (stepNumber > 0 && stepNumber <= executionSteps.size) {
            val updatedSteps = executionSteps.toMutableList()
            updatedSteps[stepNumber - 1] = updatedSteps[stepNumber - 1].copy(status = StepStatus.IN_PROGRESS)
            executionSteps = updatedSteps
            currentExecutionStep = stepNumber - 1
        }
    }

    private fun handleStepCompleted(jsonMessage: JSONObject) {
        val stepNumber = jsonMessage.optInt("step_number", 0)
        val success = jsonMessage.optBoolean("success", false)

        if (stepNumber > 0 && stepNumber <= executionSteps.size) {
            val updatedSteps = executionSteps.toMutableList()
            updatedSteps[stepNumber - 1] = updatedSteps[stepNumber - 1].copy(
                status = if (success) StepStatus.COMPLETED else StepStatus.FAILED
            )
            executionSteps = updatedSteps
        }
    }

    private fun handleExecutionCompleted(jsonMessage: JSONObject) {
        executionCompleted = true
        val success = jsonMessage.optBoolean("success", false)
        if (success) {
            val updatedSteps = executionSteps.map {
                if (it.status == StepStatus.PENDING || it.status == StepStatus.IN_PROGRESS) {
                    it.copy(status = StepStatus.COMPLETED)
                } else it
            }
            executionSteps = updatedSteps
        }
    }

    private fun handleContextReceived(jsonMessage: JSONObject) {
        readyForExecution = jsonMessage.optBoolean("ready_for_execution", false)
    }

    private fun handleError(jsonMessage: JSONObject) {
        val errorMessage = jsonMessage.optString("message", "Unknown error")
        Log.e("WebSocketState", "Server error: $errorMessage")
        isSubmittingClarification = false // Reset on error
    }

    private fun extractStringList(jsonObj: JSONObject, key: String): List<String> {
        return try {
            if (jsonObj.has(key)) {
                val array = jsonObj.getJSONArray(key)
                (0 until array.length()).map { array.getString(it) }
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun extractRoadmap(jsonObj: JSONObject): List<ExecutionStep> {
        return try {
            if (jsonObj.has("roadmap")) {
                val roadmapArray = jsonObj.getJSONArray("roadmap")
                (0 until roadmapArray.length()).map { i ->
                    val stepObj = roadmapArray.getJSONObject(i)
                    ExecutionStep(
                        stepNumber = stepObj.optInt("step_number", i + 1),
                        title = stepObj.optString("title", "Step ${i + 1}"),
                        description = stepObj.optString("description", ""),
                        actionType = stepObj.optString("action_type", "general"),
                        toolNeeded = stepObj.optString("tool_needed", "general"),
                        inputsRequired = extractStringList(stepObj, "inputs_required"),
                        expectedOutput = stepObj.optString("expected_output", ""),
                        successCriteria = stepObj.optString("success_criteria", ""),
                        errorHandling = stepObj.optString("error_handling", ""),
                        estimatedSeconds = stepObj.optInt("estimated_seconds", 30)
                    )
                }
            } else emptyList()
        } catch (e: Exception) {
            Log.e("WebSocketState", "Error extracting roadmap: ${e.message}")
            emptyList()
        }
    }

    // Connection functions
    fun connect() {
        if (webSocket == null || connectionStatus != "Connected") {
            println("WebSocket: Attempting to connect...")
            connectionStatus = "Connecting..."
            val request = Request.Builder().url(WEBSOCKET_URL).build()
            webSocket = okHttpClient.newWebSocket(request, webSocketListener)
        }
    }

    fun disconnect() {
        webSocket?.close(NORMAL_CLOSURE_STATUS, "Closing connection")
        webSocket = null
        connectionStatus = "Disconnected"
    }

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

    // FIXED: Send clarification answers in proper JSON format
    fun sendClarificationAnswers(answers: String): Boolean {
        return if (webSocket != null && connectionStatus == "Connected") {
            println("WebSocket: Sending clarification answers: $answers")

            // JSON format में message भेजें
            val clarificationMessage = JSONObject().apply {
                put("type", "clarification_response")
                put("answers", answers)
            }

            // State update करें
            isSubmittingClarification = true

            val success = webSocket?.send(clarificationMessage.toString()) ?: false

            if (!success) {
                isSubmittingClarification = false
            }

            success
        } else {
            println("WebSocket: Cannot send clarification, not connected.")
            false
        }
    }

    fun updateToolSelections(tools: Set<String>): Boolean {
        val toolsJson = JSONObject()
        toolsJson.put("selected_tools", tools.toList())

        return if (webSocket != null && connectionStatus == "Connected") {
            println("WebSocket: Updating tools: $tools")
            webSocket?.send(toolsJson.toString()) ?: false
        } else {
            println("WebSocket: Cannot update tools, not connected.")
            false
        }
    }

    fun provideContext(context: Map<String, String>): Boolean {
        val contextJson = JSONObject()
        contextJson.put("type", "provide_context")
        contextJson.put("context", JSONObject(context))

        return if (webSocket != null && connectionStatus == "Connected") {
            println("WebSocket: Providing context: $context")
            executionContext = ExecutionContext(context)
            webSocket?.send(contextJson.toString()) ?: false
        } else {
            println("WebSocket: Cannot provide context, not connected.")
            false
        }
    }

    fun startExecution(): Boolean {
        val executionJson = JSONObject()
        executionJson.put("type", "start_execution")

        return if (webSocket != null && connectionStatus == "Connected") {
            println("WebSocket: Starting execution")
            webSocket?.send(executionJson.toString()) ?: false
        } else {
            println("WebSocket: Cannot start execution, not connected.")
            false
        }
    }

    // Reset state for a new session
    fun resetState() {
        intentUnderstood = false
        agentGoal = null
        toolRequirements = emptyMap()
        clarificationQuestions = emptyList()
        processingComplete = false
        currentStep = 0
        waitingForClarification = false
        lastServerMessage = null
        isSubmittingClarification = false // ADDED: Reset submission state

        // Reset planning state
        planningCreated = false
        planningData = null
        contextNeeded = emptyList()
        readyForExecution = false
        executionContext = ExecutionContext()

        // Reset execution state
        executionStarted = false
        currentExecutionStep = 0
        executionSteps = emptyList()
        executionCompleted = false
    }
}

@Composable
fun rememberWebSocketState(): WebSocketState {
    return remember { WebSocketState() }
}

// --- CreateAgentScreen ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateAgentScreen(navController: NavController, webSocketState: WebSocketState) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tools")
    var promptText by remember { mutableStateOf(TextFieldValue("")) }
    var showEmptyPromptWarning by remember { mutableStateOf(false) }
    var showConnectionWarning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        webSocketState.connect()
        webSocketState.resetState()
    }

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

    if (showConnectionWarning) {
        AlertDialog(
            onDismissRequest = { showConnectionWarning = false },
            title = { Text("Connection Error") },
            text = { Text("WebSocket is not connected. Please wait for connection or try again.") },
            confirmButton = {
                TextButton(onClick = {
                    showConnectionWarning = false
                    webSocketState.connect()
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
                        webSocketState.processingComplete = false
                        webSocketState.currentStep = 0

                        if (webSocketState.sendMessage(prompt)) {
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
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(if (webSocketState.connectionStatus == "Connected") "Understand Intent" else "Connecting...")
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
            item {
                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Agent Prompt") },
                    placeholder = { Text("Describe what kind of agent you want to create...") },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 4
                )
            }

            if (webSocketState.selectedTools.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            "Selected Tools",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            webSocketState.selectedTools.forEach { toolId ->
                                val tool = webSocketState.availableTools[toolId] ?:
                                ToolInfo(toolId, toolId, getToolIcon(toolId))

                                InputChip(
                                    selected = true,
                                    onClick = { },
                                    label = { Text(tool.name) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = tool.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(InputChipDefaults.IconSize)
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Remove ${tool.name}",
                                            modifier = Modifier
                                                .size(InputChipDefaults.IconSize)
                                                .clickable {
                                                    val updatedTools = webSocketState.selectedTools.toMutableSet()
                                                    updatedTools.remove(toolId)
                                                    webSocketState.updateToolSelections(updatedTools)
                                                }
                                        )
                                    },
                                    shape = CircleShape
                                )
                            }
                        }
                    }
                }
            }

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

            val currentTools = webSocketState.availableTools.values.toList()

            items(currentTools, key = { it.id }) { tool ->
                val isActive = webSocketState.selectedTools.contains(tool.id)
                ToolListItem(
                    tool = tool,
                    isActive = isActive,
                    onActiveChange = { isChecked ->
                        val updatedTools = webSocketState.selectedTools.toMutableSet()
                        if (isChecked) {
                            updatedTools.add(tool.id)
                        } else {
                            updatedTools.remove(tool.id)
                        }
                        webSocketState.updateToolSelections(updatedTools)
                    }
                )
            }

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

// --- ToolListItem Composable ---
@Composable
fun ToolListItem(
    tool: ToolInfo,
    isActive: Boolean,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(tool.name, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = {
            if (tool.description.isNotEmpty()) {
                Text(tool.description, style = MaterialTheme.typography.bodyMedium)
            }
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp)
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

// --- FIXED AgentProcessingScreen with Better Clarification Handling ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentProcessingScreen(
    navController: NavController,
    webSocketState: WebSocketState
) {
    val steps = listOf(
        "Processing your query...",      // Index 0
        "Asking for clarification...",   // Index 1 - CLARIFICATION STEP
        "Intent understood!",            // Index 2 - UNDERSTOOD STEP
        "Creating execution plan..."     // Index 3 - PLANNING STEP
    )

    var clarificationInput by remember { mutableStateOf(TextFieldValue("")) }
    val currentStep = webSocketState.currentStep

    // Clarification submit करने का function
    val submitClarification = {
        val answer = clarificationInput.text.trim()
        if (answer.isNotEmpty() && !webSocketState.isSubmittingClarification) {
            if (webSocketState.sendClarificationAnswers(answer)) {
                clarificationInput = TextFieldValue("")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Intent Understanding") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, "Go back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress visualization
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    steps.forEachIndexed { index, step ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            val isCompleted = index < currentStep ||
                                    (index == currentStep && webSocketState.processingComplete)
                            val isActive = index == currentStep

                            val circleColor = when {
                                isCompleted -> MaterialTheme.colorScheme.primary
                                isActive -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(color = circleColor, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    isCompleted -> {
                                        Icon(
                                            Icons.Filled.Check,
                                            "Completed",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    isActive && !webSocketState.processingComplete -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }

                            Text(
                                text = step,
                                color = if (isActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }

                        if (index < steps.size - 1) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 11.dp)
                                    .width(2.dp)
                                    .height(24.dp)
                                    .background(
                                        if (index < currentStep)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outline
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content based on current step
            when (currentStep) {
                0 -> {
                    ProcessingStepContent()
                }

                1 -> {
                    ClarificationStepContent(
                        webSocketState = webSocketState,
                        clarificationInput = clarificationInput,
                        onInputChange = { clarificationInput = it },
                        onSubmit = submitClarification
                    )
                }

                2 -> {
                    if (!webSocketState.planningCreated) {
                        IntentUnderstoodContent(webSocketState, navController)
                    }
                }

                3 -> {
                    if (webSocketState.planningCreated && webSocketState.planningData != null) {
                        LaunchedEffect(Unit) {
                            navController.navigate(Screen.AgentPlanning.route)
                        }
                    } else {
                        PlanningStepContent()
                    }
                }
            }
        }
    }
}

@Composable
private fun ProcessingStepContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(60.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 5.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Analyzing your request...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ClarificationStepContent(
    webSocketState: WebSocketState,
    clarificationInput: TextFieldValue,
    onInputChange: (TextFieldValue) -> Unit,
    onSubmit: () -> Unit
) {
    if (webSocketState.waitingForClarification && webSocketState.clarificationQuestions.isNotEmpty()) {
        // Display clarification questions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "I need more information to understand your request:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // FIXED: Display all clarification questions properly
                webSocketState.clarificationQuestions.forEachIndexed { index, question ->
                    Text(
                        text = "${index + 1}. $question",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = clarificationInput,
            onValueChange = onInputChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Your Answer") },
            placeholder = { Text("Please answer the questions above...") },
            shape = RoundedCornerShape(12.dp),
            minLines = 4,
            enabled = !webSocketState.isSubmittingClarification
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = clarificationInput.text.trim().isNotEmpty() &&
                    !webSocketState.isSubmittingClarification,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (webSocketState.isSubmittingClarification) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submitting...")
            } else {
                Text("Submit Answers")
            }
        }
    } else {
        // Processing answers
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(60.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 5.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (webSocketState.isSubmittingClarification)
                    "Submitting your answers..."
                else "Processing your answers...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun IntentUnderstoodContent(
    webSocketState: WebSocketState,
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Success! I understand your intent.",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        BasicIntentDisplay(webSocketState)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate("agenttest") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Filled.Science, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Test Agent")
        }
    }
}

@Composable
private fun PlanningStepContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(60.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 5.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Creating detailed execution plan...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// --- Agent Planning Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentPlanningScreen(
    navController: NavController,
    webSocketState: WebSocketState
) {
    val planningData = webSocketState.planningData
    var contextInputs by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Execution Plan") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, "Go back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        bottomBar = {
            if (webSocketState.contextNeeded.isEmpty() || webSocketState.readyForExecution) {
                Button(
                    onClick = {
                        webSocketState.startExecution()
                        navController.navigate(Screen.AgentExecution.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Execution")
                }
            } else {
                Button(
                    onClick = {
                        webSocketState.provideContext(contextInputs)
                    },
                    enabled = contextInputs.size == webSocketState.contextNeeded.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Provide Context")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (planningData != null) {
                item {
                    PlanOverviewCard(planningData)
                }

                if (webSocketState.contextNeeded.isNotEmpty() && !webSocketState.readyForExecution) {
                    item {
                        ContextRequirementsCard(
                            contextNeeded = webSocketState.contextNeeded,
                            contextInputs = contextInputs,
                            onContextChange = { key, value ->
                                contextInputs = contextInputs.toMutableMap().apply {
                                    put(key, value)
                                }
                            }
                        )
                    }
                }

                item {
                    Text(
                        text = "Execution Steps",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                itemsIndexed(planningData.roadmap) { index, step ->
                    ExecutionStepCard(step = step, stepIndex = index)
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            } else {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No planning data available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// --- Agent Execution Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentExecutionScreen(
    navController: NavController,
    webSocketState: WebSocketState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agent Execution") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        enabled = webSocketState.executionCompleted
                    ) {
                        Icon(Icons.Filled.ArrowBack, "Go back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        bottomBar = {
            if (webSocketState.executionCompleted) {
                Button(
                    onClick = { navController.navigate(Screen.CreateAgent.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.Home, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create New Agent")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ExecutionStatusCard(webSocketState)
            }

            item {
                ExecutionProgressCard(webSocketState)
            }

            item {
                Text(
                    text = "Execution Steps",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            itemsIndexed(webSocketState.executionSteps) { index, step ->
                ExecutionStepProgressCard(
                    step = step,
                    stepIndex = index,
                    isCurrentStep = index == webSocketState.currentExecutionStep
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// --- Supporting Composables ---
@Composable
fun BasicIntentDisplay(webSocketState: WebSocketState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "AGENT GOAL",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = webSocketState.agentGoal ?: "Goal not specified",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (webSocketState.toolRequirements.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "REQUIRED TOOLS",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                webSocketState.toolRequirements.forEach { (toolId, reason) ->
                    val tool = webSocketState.availableTools[toolId] ?:
                    ToolInfo(toolId, toolId.capitalizeCustom(), getToolIcon(toolId))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tool.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 32.dp, top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlanOverviewCard(planning: PlanningData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "EXECUTION PLAN",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PlanMetric(
                    icon = Icons.Filled.Category,
                    label = "Type",
                    value = planning.taskType.capitalizeCustom()
                )
                PlanMetric(
                    icon = Icons.Filled.Speed,
                    label = "Complexity",
                    value = planning.complexityLevel.capitalizeCustom()
                )
                PlanMetric(
                    icon = Icons.Filled.Timer,
                    label = "Duration",
                    value = "${planning.estimatedDurationMinutes}m"
                )
                PlanMetric(
                    icon = Icons.Filled.List,
                    label = "Steps",
                    value = planning.totalSteps.toString()
                )
            }
        }
    }
}

@Composable
fun PlanMetric(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun ContextRequirementsCard(
    contextNeeded: List<String>,
    contextInputs: Map<String, String>,
    onContextChange: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "REQUIRED CONTEXT",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = "Please provide the following information to proceed:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            contextNeeded.forEach { contextKey ->
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contextInputs[contextKey] ?: "",
                    onValueChange = { onContextChange(contextKey, it) },
                    label = { Text(contextKey.replace("_", " ").capitalizeCustom()) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

@Composable
fun ExecutionStepCard(step: ExecutionStep, stepIndex: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${stepIndex + 1}",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${step.estimatedSeconds}s • ${step.toolNeeded}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (step.inputsRequired.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Inputs: ${step.inputsRequired.joinToString(", ")}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ExecutionStatusCard(webSocketState: WebSocketState) {
    val statusColor = if (webSocketState.executionCompleted) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }

    val statusText = when {
        webSocketState.executionCompleted -> "Execution Completed"
        webSocketState.executionStarted -> "Execution in Progress"
        else -> "Preparing for Execution"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (webSocketState.executionCompleted) Icons.Filled.CheckCircle else Icons.Filled.PlayCircleFilled,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}

@Composable
fun ExecutionStepProgressCard(
    step: ExecutionStep,
    stepIndex: Int,
    isCurrentStep: Boolean
) {
    val cardColor = when (step.status) {
        StepStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
        StepStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondaryContainer
        StepStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
        StepStatus.PENDING -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val iconColor = when (step.status) {
        StepStatus.COMPLETED -> MaterialTheme.colorScheme.primary
        StepStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
        StepStatus.FAILED -> MaterialTheme.colorScheme.error
        StepStatus.PENDING -> MaterialTheme.colorScheme.outline
    }

    val stepIcon = when (step.status) {
        StepStatus.COMPLETED -> Icons.Filled.CheckCircle
        StepStatus.IN_PROGRESS -> Icons.Filled.PlayCircleFilled
        StepStatus.FAILED -> Icons.Filled.Error
        StepStatus.PENDING -> Icons.Filled.Circle
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (isCurrentStep) it.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)) else it },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = stepIcon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = step.status.name.lowercase().capitalizeCustom(),
                    style = MaterialTheme.typography.labelMedium,
                    color = iconColor
                )
            }

            if (step.status == StepStatus.IN_PROGRESS) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

// FIXED: ExecutionProgressCard with proper layout
@Composable
fun ExecutionProgressCard(webSocketState: WebSocketState) {
    val completedSteps = webSocketState.executionSteps.count { it.status == StepStatus.COMPLETED }
    val totalSteps = webSocketState.executionSteps.size
    val progress = if (totalSteps > 0) completedSteps.toFloat() / totalSteps else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "$completedSteps / $totalSteps steps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }
    }
}

// String extension for capitalization
fun String.capitalizeCustom(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}