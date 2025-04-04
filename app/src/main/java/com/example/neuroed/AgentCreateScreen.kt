package com.example.neuroed // Change to your actual package name

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
// Import NavHostController if you use that type specifically
// import androidx.navigation.NavHostController
// import com.example.neuroed.ui.theme.NeuroEdTheme // Import your actual theme if defined separately
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check



// ... other imports ...
import androidx.compose.material3.AlertDialog // Add if missing
import androidx.compose.material3.TextButton // Add if missing
import androidx.compose.material3.ButtonDefaults // Add if missing
import androidx.compose.material3.CircularProgressIndicator // Add if missing
import androidx.compose.material3.LocalContentColor // Add if missing
import androidx.compose.material3.PrimaryTabRow // Add if missing
import androidx.compose.material3.Tab // Add if missing
import androidx.compose.runtime.DisposableEffect // NEW Import
import okhttp3.* // NEW Import for OkHttp
import okio.ByteString // NEW Import for OkHttp
import java.util.concurrent.TimeUnit

// --- Screen Route Definitions --- (Keep as is)
sealed class Screen(val route: String) {
    object CreateAgent : Screen("create_agent_screen")
    object AgentProcessing : Screen("agent_processing_screen")
}

// --- WebSocket Constants ---
private const val WEBSOCKET_URL = "api/AgentCreate/" // <-- IMPORTANT: REPLACE WITH YOUR SERVER URL
private const val NORMAL_CLOSURE_STATUS = 1000

// --- Screen Composable: CreateAgentScreen ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateAgentScreen(navController: NavController) {
    // State variables (Keep existing ones)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tools", "System Tools")
    var activeTools by remember { mutableStateOf(listOf<ToolInfo>()) }
    var promptText by remember { mutableStateOf(TextFieldValue("")) }
    var showEmptyPromptWarning by remember { mutableStateOf(false) }

    // --- WebSocket State ---
    var webSocket by remember { mutableStateOf<WebSocket?>(null) }
    var connectionStatus by remember { mutableStateOf("Disconnected") }
    var lastServerMessage by remember { mutableStateOf<String?>(null) } // To store received messages
    val okHttpClient = remember { OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build() } // Build client once

    // --- WebSocket Listener ---
    val webSocketListener = remember {
        object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                // Use LaunchedEffect to safely update state from background thread
                // Note: While mutableStateOf writes are thread-safe, it's good practice
                // especially for more complex updates or UI interactions.
                // However, for simple status updates, direct update is often okay.
                // Keeping direct update here for simplicity of the example.
                connectionStatus = "Connected"
                webSocket = ws // Store the active WebSocket session
                println("WebSocket: Opened")
                // Optionally send an initial message or auth token here if needed
                // ws.send("Client connected")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                lastServerMessage = "Received: $text"
                println("WebSocket: Received Text: $text")
                // --- HANDLE "ok" or other specific messages ---
                if (text.equals("ok", ignoreCase = true)) {
                    // Maybe update some UI state or trigger another action
                    println("WebSocket: Received 'ok' confirmation.")
                }
                // --- END HANDLING ---
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                lastServerMessage = "Received bytes: ${bytes.hex()}"
                println("WebSocket: Received Bytes: ${bytes.hex()}")
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                println("WebSocket: Closing: $code / $reason")
                connectionStatus = "Closing"
                ws.close(NORMAL_CLOSURE_STATUS, null) // Acknowledge closing
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                println("WebSocket: Closed: $code / $reason")
                connectionStatus = "Disconnected"
                webSocket = null // Clear the reference
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                println("WebSocket: Error: ${t.message}")
                t.printStackTrace()
                connectionStatus = "Error: ${t.message?.take(30) ?: "Unknown"}" // Show brief error
                webSocket = null // Clear reference on failure too
            }
        }
    }

    // --- WebSocket Connection Management ---
    DisposableEffect(WEBSOCKET_URL) { // Reconnect if URL changes (unlikely here)
        println("WebSocket: Attempting to connect...")
        connectionStatus = "Connecting..."
        val request = Request.Builder().url(WEBSOCKET_URL).build()
        val currentWebSocket = okHttpClient.newWebSocket(request, webSocketListener)

        // Cleanup function: Called when the composable leaves the composition
        onDispose {
            println("WebSocket: Disposing - Closing connection.")
            currentWebSocket.close(NORMAL_CLOSURE_STATUS, "Leaving screen")
            webSocket = null // Ensure reference is cleared
            connectionStatus = "Disconnected" // Reset status on dispose
        }
    }

    // Derived state for easy checking (Keep as is)
    val activeToolNames = remember(activeTools) { activeTools.map { it.name }.toSet() }

    // Warning Dialog (Keep as is)
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            // --- MODIFIED TopAppBar to show connection status ---
            TopAppBar(
                title = {
                    Column { // Use column for title and status
                        Text("Create Agent")
                        Text(
                            text = "WS: $connectionStatus",
                            style = MaterialTheme.typography.labelSmall, // Smaller font for status
                            color = when {
                                connectionStatus == "Connected" -> Color.Green.copy(alpha = 0.8f)
                                connectionStatus.startsWith("Error") -> MaterialTheme.colorScheme.error
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
            // --- END MODIFIED TopAppBar ---
        },
        bottomBar = {
            FilledTonalButton(
                onClick = {
                    val prompt = promptText.text.trim()
                    if (prompt.isEmpty()) {
                        showEmptyPromptWarning = true
                    } else {
                        // --- WEBSOCKET SEND & NAVIGATION TRIGGER ---
                        if (webSocket != null && connectionStatus == "Connected") {
                            println("WebSocket: Sending prompt: $prompt")
                            val sent = webSocket?.send(prompt) ?: false // Send the prompt text
                            if (sent) {
                                println("WebSocket: Send initiated.")
                                // Navigate immediately after sending
                                navController.navigate(Screen.AgentProcessing.route)
                            } else {
                                println("WebSocket: Failed to send prompt (queue full or socket closed).")
                                // Optionally show an error message to the user
                                lastServerMessage = "Error: Could not send prompt."
                                // Maybe don't navigate if send fails? Depends on requirements.
                            }
                        } else {
                            println("WebSocket: Cannot send, not connected.")
                            // Show a warning that connection isn't ready
                            showEmptyPromptWarning = true // Re-use warning or create specific one
                            // Modify AlertDialog text if needed to indicate connection issue
                        }
                        // --- END WEBSOCKET SEND & NAVIGATION TRIGGER ---
                    }
                },
                // Disable button if not connected? Optional.
                enabled = connectionStatus == "Connected",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(52.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(if (connectionStatus == "Connected") "Start Agent" else "Connecting...")
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
            // --- Prompt Input --- (Keep as is)
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

            // --- Active Tools Section --- (Keep as is)
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

            // --- Tool Tabs --- (Keep as is)
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

            // --- Scrollable Tool List --- (Keep as is)
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
                if (lastServerMessage != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Server: $lastServerMessage",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // --- End Optional Display ---


            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// --- Screen Composable: AgentProcessingScreen ---
// (Keep AgentProcessingScreen as it was, no changes needed there for this request)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentProcessingScreen(
    onStopClick: () -> Unit // Expects a lambda for the stop action
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val steps = listOf(
        "Agent process your Query....", // Index 0
        "Agent confusion track.....",   // Index 1 - CONFUSION STEP
        "Agent clear confusion..."      // Index 2
    )
    val confusionStepIndex = 1 // Define the index for the confusion step

    var processingComplete by remember { mutableStateOf(false) }
    var confusionInput by remember { mutableStateOf(TextFieldValue("")) }

    // --- Countdown Timer State ---
    val totalDurationSeconds = 90 // Example: 90 seconds total processing time
    var remainingTimeSeconds by remember { mutableLongStateOf(totalDurationSeconds.toLong()) }
    var timerRunning by remember { mutableStateOf(true) } // Control timer independently

    // Function to format seconds into MM:SS
    fun formatTime(seconds: Long): String {
        if (seconds < 0) return "00:00" // Avoid negative display
        val minutes = TimeUnit.SECONDS.toMinutes(seconds)
        val remainingSecs = seconds - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, remainingSecs)
    }

    // --- Stop Action ---
    // Encapsulate the stop logic
    val performStopAction = {
        if (!processingComplete) {
            processingComplete = true // Mark overall process as stopped/complete
            timerRunning = false      // Stop the timer explicitly
            onStopClick()             // Call the navigation lambda
        }
    }

    // --- Main UI with Scaffold ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agent Processing") },
                navigationIcon = {
                    IconButton(onClick = performStopAction) { // Use the unified stop action
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Stop and Go Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant, // Match card color
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        // Place button in bottom bar slot
        bottomBar = {
            // Optional: Add some elevation/surface for the bottom bar area
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp, // Add slight shadow
                color = MaterialTheme.colorScheme.scrim // Match background or use surface
            ) {
                Button(
                    onClick = performStopAction, // Use the unified stop action
                    enabled = !processingComplete, // Disable when stopped/complete
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        disabledContainerColor = Color.White.copy(alpha = 0.5f),
                        disabledContentColor = Color.Gray.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        // .align(Alignment.BottomCenter) // Alignment done by bottomBar placement
                        .fillMaxWidth(0.9f) // Control width relative to parent
                        .padding(horizontal = 16.dp, vertical = 16.dp) // Padding around the button
                        .height(48.dp)
                ) {
                    // Conditional Content for Button
                    if (!processingComplete) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                color = LocalContentColor.current, // Black
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
                            text = "Stopped", // Indicate final state
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

        },
        containerColor = MaterialTheme.colorScheme.scrim // Background for the main content area
    ) { innerPadding -> // Content area padding provided by Scaffold

        // Main content column (steps, optional input, timer)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Apply padding from Scaffold
                .padding(horizontal = 16.dp) // Add horizontal padding for content
                .verticalScroll(rememberScrollState()), // Make content scrollable
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp)) // Space from TopAppBar

            // Card with Steps
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                // Let card height be determined by content, remove fillMaxHeight
                ,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        // .fillMaxSize() // Remove fillMaxSize from inner column
                        .padding(20.dp),
                ) {
                    steps.forEachIndexed { index, step ->
                        // --- Row for Step Item (Indicator, Text, Line) ---
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            // Circle indicator
                            val circleColor = when {
                                index < currentStep || (index == currentStep && processingComplete) -> MaterialTheme.colorScheme.primary
                                index == currentStep && !processingComplete -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                            val iconColor = when {
                                index < currentStep || (index == currentStep && processingComplete) -> MaterialTheme.colorScheme.onPrimary
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(color = circleColor, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (index < currentStep || (index == currentStep && processingComplete)) {
                                    Icon(Icons.Filled.Check, "Completed", tint = iconColor, modifier = Modifier.size(16.dp))
                                }
                            }

                            // Step text
                            Text(
                                text = step,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp,
                                fontWeight = if (index == currentStep && !processingComplete) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        } // End Row for Step Item

                        // Line connecting circles
                        if (index < steps.size - 1) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 11.dp) // Align with center of circle
                                    .width(2.dp)
                                    .height(24.dp)
                                    .background(if (index < currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                            )
                        }
                        // --- End Line ---
                    } // End forEachIndexed
                } // End Column inside Card
            } // End Card

            Spacer(Modifier.height(24.dp)) // Space below card

            // --- Conditional Input Area for Confusion Step ---
            AnimatedVisibility(
                visible = currentStep == confusionStepIndex && !processingComplete,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Agent seems confused. Please provide clarification or keywords:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = confusionInput,
                        onValueChange = { confusionInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Your Clarification") },
                        placeholder = { Text("e.g., focus on the main points, simplify the language...") },
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )
                }
            }
            // --- End Conditional Input Area ---


            // --- Countdown Timer Display ---
            // Add space above timer, ensure it's visible even if input appears
            Spacer(Modifier.height( if (currentStep == confusionStepIndex && !processingComplete) 16.dp else 32.dp))

            Text(
                text = "Time Remaining: ${formatTime(remainingTimeSeconds)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface, // Use primary text color
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            // --- End Countdown Timer Display ---

            Spacer(Modifier.height(80.dp)) // Extra space at bottom to ensure content scrolls above bottom bar

        } // End Main Content Column
    } // End Scaffold

    // --- LaunchedEffect for Step Advancement ---
    LaunchedEffect(processingComplete) {
        if (!processingComplete) {
            while (currentStep < steps.size && !processingComplete) {
                // Don't advance automatically if waiting for input at confusion step
                if (currentStep == confusionStepIndex) {
                    // Optionally add logic here if you want to auto-proceed
                    // after a certain time even with no input, or wait indefinitely.
                    // For now, it will just wait until processingComplete becomes true.
                    delay(Long.MAX_VALUE) // Effectively waits until interrupted
                } else {
                    delay(1500) // Delay for other steps
                    // Check again after delay
                    if (!processingComplete) {
                        if (currentStep < steps.size - 1) {
                            currentStep++
                        } else { // Reached the last step (index 2)
                            // Keep showing the last step as current for a bit?
                            // Or mark complete immediately after last delay?
                            // Let's mark complete after the last delay completes.
                            currentStep = steps.size // Visually marks last step done
                            performStopAction() // Stop everything after completing last step
                        }
                    }
                }
            }
            // If the loop finishes because currentStep >= steps.size, ensure stopped
            if(currentStep >= steps.size && !processingComplete) {
                performStopAction()
            }
        }
    }
    // --- End LaunchedEffect for Step Advancement ---


    // --- LaunchedEffect for Countdown Timer ---
    LaunchedEffect(timerRunning, processingComplete) {
        if (timerRunning && !processingComplete) {
            while (remainingTimeSeconds > 0 && timerRunning && !processingComplete) {
                delay(1000)
                // Check flags again after delay
                if (timerRunning && !processingComplete) {
                    remainingTimeSeconds--
                    if (remainingTimeSeconds == 0L) {
                        // Time ran out, stop the process
                        performStopAction()
                    }
                }
            }
        }
        // Ensure timer stops if processing completes while effect is running
        if(processingComplete) {
            timerRunning = false
        }
    }
    // --- End LaunchedEffect for Countdown Timer ---
}

// --- Support Code (Data, Helpers, ListItems) ---
// (Keep ToolInfo, getStandardTools, getSystemTools, ToolIcon, ToolListItem as they were)
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


// --- IMPORTANT REMINDERS ---
// 1.  Replace `"ws://your_websocket_server_here"` with your actual WebSocket server address.
// 2.  Ensure your server is running and accessible from the device/emulator running the app.
// 3.  This implementation connects when the screen becomes visible and disconnects when it leaves.
// 4.  The prompt is sent when the "Start Agent" button is clicked, only if connected.
// 5.  Navigation happens *immediately* after the `send` call is initiated. It doesn't wait for a server confirmation like "ok" before navigating in this version.
// 6.  Error handling is basic; you might want more robust handling (e.g., retries, user feedback).
// 7.  Consider using a ViewModel for more complex state management and separation of concerns, especially if the WebSocket logic grows.