// AgentTestScreen.kt - Separate File
package com.example.neuroed

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Test-specific data classes
data class TestMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromAgent: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val messageType: String = "text", // "text", "status", "error", "step", "execution"
    val stepInfo: StepInfo? = null,
    val metadata: Map<String, Any> = emptyMap()
)

data class StepInfo(
    val stepNumber: Int,
    val stepTitle: String,
    val stepDescription: String,
    val isCompleted: Boolean = false,
    val isActive: Boolean = false,
    val hasError: Boolean = false,
    val result: String? = null
)

data class AgentTestStatus(
    val isConnected: Boolean = false,
    val isExecuting: Boolean = false,
    val currentStep: Int = 0,
    val totalSteps: Int = 0,
    val executionProgress: Float = 0f,
    val canSave: Boolean = false,
    val canModify: Boolean = true,
    val error: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentTestScreen(
    navController: NavController,
    webSocketState: WebSocketState
) {
    var testMessages by remember { mutableStateOf<List<TestMessage>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var testStatus by remember { mutableStateOf(AgentTestStatus(isConnected = true)) }
    var showActionDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showModifyDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // WebSocket message handler
    LaunchedEffect(webSocketState) {
        // Handle WebSocket messages here
        // You can collect messages from webSocketState if it has a messages flow
        // Or remove this if not needed for now
    }

    // Auto-scroll to bottom
    LaunchedEffect(testMessages.size) {
        if (testMessages.isNotEmpty()) {
            listState.animateScrollToItem(testMessages.size - 1)
        }
    }

    // Animations
    val connectionColor by animateColorAsState(
        targetValue = if (testStatus.isConnected) Color(0xFF4CAF50) else Color(0xFFF44336),
        animationSpec = tween(300)
    )

    val executionPulse = rememberInfiniteTransition()
    val pulseScale by executionPulse.animateFloat(
        initialValue = 1f,
        targetValue = if (testStatus.isExecuting) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Agent Status Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    if (testStatus.isExecuting)
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    else MaterialTheme.colorScheme.primaryContainer
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (testStatus.isExecuting) {
                                CircularProgressIndicator(
                                    progress = testStatus.executionProgress,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(text = "🧪", fontSize = 20.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text("Test Agent", style = MaterialTheme.typography.titleMedium)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(connectionColor)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when {
                                        !testStatus.isConnected -> "Disconnected"
                                        testStatus.isExecuting -> "Executing ${testStatus.currentStep}/${testStatus.totalSteps}"
                                        testStatus.canSave -> "Ready to Save"
                                        else -> "Ready for Testing"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, "Go back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showActionDialog = true },
                        enabled = !testStatus.isExecuting
                    ) {
                        Icon(Icons.Default.MoreVert, "Actions")
                    }
                }
            )
        },
        bottomBar = {
            TestScreenBottomBar(
                inputText = inputText,
                onInputChange = { inputText = it },
                testStatus = testStatus,
                onSendTest = { command ->
                    executeTestCommand(command, webSocketState) { status ->
                        testStatus = status
                    }
                    inputText = ""
                },
                onCancel = {
                    navController.navigate("agent_planning") {
                        popUpTo("agent_test") { inclusive = true }
                    }
                },
                onModify = { showModifyDialog = true },
                onSave = { showSaveDialog = true }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            state = listState,
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (testMessages.isEmpty()) {
                item { TestWelcomeMessage() }
            }

            items(testMessages) { message ->
                TestChatMessage(message = message)
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Dialogs
    if (showActionDialog) {
        TestActionDialog(
            onDismiss = { showActionDialog = false },
            onActionSelected = { action ->
                when (action) {
                    "save" -> showSaveDialog = true
                    "modify" -> showModifyDialog = true
                    "reset" -> testMessages = emptyList()
                    "view_plan" -> navController.navigate("agent_planning")
                }
                showActionDialog = false
            }
        )
    }

    if (showSaveDialog) {
        SaveAgentDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { agentName ->
                saveTestAgent(agentName, webSocketState)
                navController.navigate("create_agent") {
                    popUpTo("create_agent") { inclusive = true }
                }
            }
        )
    }

    if (showModifyDialog) {
        ModifyAgentDialog(
            onDismiss = { showModifyDialog = false },
            onModify = { type ->
                when (type) {
                    "replan" -> navController.navigate("agent_planning") {
                        popUpTo("agent_test") { inclusive = true }
                    }
                    "change_intent" -> navController.navigate("agent_processing") {
                        popUpTo("agent_test") { inclusive = true }
                    }
                    "new_agent" -> navController.navigate("create_agent") {
                        popUpTo("create_agent") { inclusive = true }
                    }
                }
            }
        )
    }
}

@Composable
fun TestScreenBottomBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    testStatus: AgentTestStatus,
    onSendTest: (String) -> Unit,
    onCancel: () -> Unit,
    onModify: () -> Unit,
    onSave: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 3.dp
    ) {
        Column {
            // Execution Progress
            AnimatedVisibility(
                visible = testStatus.isExecuting,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Executing Test",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "${testStatus.currentStep}/${testStatus.totalSteps}",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = testStatus.executionProgress,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Action Buttons - Enhanced visibility
            AnimatedVisibility(
                visible = !testStatus.isExecuting,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Action buttons title
                        Text(
                            text = "Test Actions",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Cancel Button - Red outline
                            OutlinedButton(
                                onClick = onCancel,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error,
                                    containerColor = Color.Transparent
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.error
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Cancel,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Cancel",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }

                            // Modify Button - Primary outline
                            OutlinedButton(
                                onClick = onModify,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    containerColor = Color.Transparent
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Modify",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }

                            // Save Button - Filled primary
                            Button(
                                onClick = onSave,
                                modifier = Modifier.weight(1f),
                                enabled = testStatus.canSave,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (testStatus.canSave)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (testStatus.canSave)
                                        MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        if (testStatus.canSave) Icons.Outlined.Save else Icons.Outlined.SaveAs,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        if (testStatus.canSave) "Save Agent" else "Test First",
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Status text
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when {
                                testStatus.canSave -> "✅ Agent tested successfully - Ready to save!"
                                testStatus.error != null -> "❌ ${testStatus.error}"
                                else -> "💡 Send test commands above to enable saving"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                testStatus.canSave -> Color(0xFF4CAF50)
                                testStatus.error != null -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Input Row
            AnimatedVisibility(visible = !testStatus.isExecuting) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = onInputChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Test: 'Go to Instagram and like recent post'") },
                        enabled = testStatus.isConnected,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    FloatingActionButton(
                        onClick = { if (inputText.isNotEmpty()) onSendTest(inputText) },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, "Execute Test")
                    }
                }
            }
        }
    }
}

@Composable
fun TestWelcomeMessage() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🧪 Test Your Agent",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your agent is ready for testing! Send commands to see how it performs:",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TestExamplePrompt("Go to Instagram and like the latest post")
                TestExamplePrompt("Navigate to YouTube and search for tutorials")
                TestExamplePrompt("Open Chess.com and start a quick game")
                TestExamplePrompt("Take a screenshot of the current page")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Test thoroughly before saving your agent",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TestExamplePrompt(text: String) {
    Text(
        text = "• $text",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
fun TestChatMessage(message: TestMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromAgent) Arrangement.Start else Arrangement.End
    ) {
        if (message.isFromAgent) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when (message.messageType) {
                            "error" -> MaterialTheme.colorScheme.errorContainer
                            "step" -> MaterialTheme.colorScheme.tertiaryContainer
                            "execution" -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (message.messageType) {
                        "error" -> "❌"
                        "step" -> "⚡"
                        "execution" -> "🔄"
                        else -> "🧪"
                    },
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.weight(0.85f),
            horizontalAlignment = if (message.isFromAgent) Alignment.Start else Alignment.End
        ) {
            Surface(
                color = when {
                    !message.isFromAgent -> MaterialTheme.colorScheme.primaryContainer
                    message.messageType == "error" -> MaterialTheme.colorScheme.errorContainer
                    message.messageType == "step" -> MaterialTheme.colorScheme.tertiaryContainer
                    message.messageType == "execution" -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                },
                shape = RoundedCornerShape(
                    topStart = if (message.isFromAgent) 4.dp else 16.dp,
                    topEnd = if (message.isFromAgent) 16.dp else 4.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    message.stepInfo?.let { stepInfo ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Step ${stepInfo.stepNumber}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            when {
                                stepInfo.isActive -> CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.dp
                                )
                                stepInfo.isCompleted -> Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = Color(0xFF4CAF50)
                                )
                                stepInfo.hasError -> Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Text(
                            text = stepInfo.stepTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
            )
        }

        if (!message.isFromAgent) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👤", fontSize = 16.sp)
            }
        }
    }
}

// Dialog Components
@Composable
fun TestActionDialog(
    onDismiss: () -> Unit,
    onActionSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Test Actions") },
        text = {
            Column {
                ActionDialogItem(
                    icon = Icons.Outlined.Save,
                    title = "Save Agent",
                    description = "Save this tested agent",
                    onClick = { onActionSelected("save") }
                )

                ActionDialogItem(
                    icon = Icons.Outlined.Edit,
                    title = "Modify Agent",
                    description = "Change configuration",
                    onClick = { onActionSelected("modify") }
                )

                ActionDialogItem(
                    icon = Icons.Outlined.Visibility,
                    title = "View Plan",
                    description = "Review execution plan",
                    onClick = { onActionSelected("view_plan") }
                )

                ActionDialogItem(
                    icon = Icons.Outlined.Refresh,
                    title = "Reset Test",
                    description = "Clear test messages",
                    onClick = { onActionSelected("reset") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun ActionDialogItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SaveAgentDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var agentName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Tested Agent") },
        text = {
            Column {
                Text("Give your tested agent a name:")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = agentName,
                    onValueChange = { agentName = it },
                    placeholder = { Text("e.g., Instagram Bot, Web Scraper") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(agentName) },
                enabled = agentName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ModifyAgentDialog(
    onDismiss: () -> Unit,
    onModify: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modify Agent") },
        text = {
            Column {
                Text("What would you like to modify?")
                Spacer(modifier = Modifier.height(12.dp))

                ModifyOption(
                    icon = Icons.Outlined.Edit,
                    title = "Change Plan",
                    description = "Modify execution steps",
                    onClick = { onModify("replan") }
                )

                ModifyOption(
                    icon = Icons.Outlined.Psychology,
                    title = "Change Intent",
                    description = "Redefine agent purpose",
                    onClick = { onModify("change_intent") }
                )

                ModifyOption(
                    icon = Icons.Outlined.Add,
                    title = "Create New",
                    description = "Start with new agent",
                    onClick = { onModify("new_agent") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ModifyOption(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// Helper Functions
fun handleTestWebSocketMessage(
    message: String,
    onMessageReceived: (TestMessage) -> Unit
) {
    try {
        // Parse JSON message (you'll need to implement based on your WebSocket format)
        // Example implementation:

        // For now, create a simple test message
        val testMessage = TestMessage(
            content = message,
            isFromAgent = true,
            messageType = "text"
        )
        onMessageReceived(testMessage)

    } catch (e: Exception) {
        // Handle parsing error
        val errorMessage = TestMessage(
            content = "Error parsing message: ${e.message}",
            isFromAgent = true,
            messageType = "error"
        )
        onMessageReceived(errorMessage)
    }
}

fun executeTestCommand(
    command: String,
    webSocketState: WebSocketState,
    onStatusUpdate: (AgentTestStatus) -> Unit
) {
    // Update status to executing
    onStatusUpdate(AgentTestStatus(
        isConnected = true,
        isExecuting = true,
        currentStep = 1,
        totalSteps = 3,
        executionProgress = 0.33f
    ))

    // Send test command to WebSocket
    try {
        webSocketState.sendMessage("""
            {
                "type": "test_execution",
                "command": "$command"
            }
        """.trimIndent())
    } catch (e: Exception) {
        // Handle error
        onStatusUpdate(AgentTestStatus(
            isConnected = false,
            error = "Failed to send command: ${e.message}"
        ))
    }
}

fun saveTestAgent(agentName: String, webSocketState: WebSocketState) {
    try {
        webSocketState.sendMessage("""
            {
                "type": "save_agent",
                "agent_name": "$agentName",
                "timestamp": ${System.currentTimeMillis()}
            }
        """.trimIndent())

        println("✅ Agent save request sent: $agentName")

    } catch (e: Exception) {
        // Handle error - you can show a toast or log
        println("❌ Failed to save agent: ${e.message}")
    }
}