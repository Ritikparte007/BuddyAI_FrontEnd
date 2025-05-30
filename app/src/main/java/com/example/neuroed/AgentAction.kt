package com.example.neuroed

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// WebSocket imports
import okhttp3.*
import okio.ByteString
import com.google.gson.Gson
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// WebSocket Message Models
data class WSMessage(
    val type: String, // "user_message", "agent_response", "status_update", "error"
    val content: String,
    val session_id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, Any> = emptyMap()
)

data class AgentStatus(
    val isConnected: Boolean = false,
    val currentAgent: String? = null,
    val isProcessing: Boolean = false,
    val progress: Int = 0,
    val currentStep: String = "",
    val error: String? = null
)

// Enhanced Message data class
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromAgent: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: Uri? = null,
    val messageType: String = "text", // "text", "image", "status", "error"
    val metadata: Map<String, Any> = emptyMap()
)

// WebSocket ViewModel
class AgentWebSocketViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _agentStatus = MutableStateFlow(AgentStatus())
    val agentStatus: StateFlow<AgentStatus> = _agentStatus

    private var webSocket: WebSocket? = null
    private val gson = Gson()
    private val sessionId = UUID.randomUUID().toString()

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            _agentStatus.value = _agentStatus.value.copy(isConnected = true, error = null)

            // Add connection message
            val connectMessage = Message(
                content = "🟢 Connected to AI Agent",
                isFromAgent = true,
                messageType = "status"
            )
            _messages.value = _messages.value + connectMessage
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                val wsMessage = gson.fromJson(text, WSMessage::class.java)
                handleWebSocketMessage(wsMessage)
            } catch (e: Exception) {
                // Handle parsing error
                val errorMessage = Message(
                    content = "Error parsing message: ${e.message}",
                    isFromAgent = true,
                    messageType = "error"
                )
                _messages.value = _messages.value + errorMessage
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            _agentStatus.value = _agentStatus.value.copy(
                isConnected = false,
                isProcessing = false,
                error = "Connection failed: ${t.message}"
            )

            val errorMessage = Message(
                content = "🔴 Connection lost: ${t.message}",
                isFromAgent = true,
                messageType = "error"
            )
            _messages.value = _messages.value + errorMessage
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            _agentStatus.value = _agentStatus.value.copy(isConnected = false)

            val closeMessage = Message(
                content = "🔴 Disconnected from AI Agent",
                isFromAgent = true,
                messageType = "status"
            )
            _messages.value = _messages.value + closeMessage
        }
    }

    init {
        connectWebSocket()
    }

    private fun connectWebSocket() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("ws://localhost:8000/agent/")
            .build()

        webSocket = client.newWebSocket(request, webSocketListener)
    }

    private fun handleWebSocketMessage(wsMessage: WSMessage) {
        when (wsMessage.type) {
            "agent_response" -> {
                val message = Message(
                    content = wsMessage.content,
                    isFromAgent = true,
                    messageType = "text",
                    metadata = wsMessage.metadata
                )
                _messages.value = _messages.value + message

                // Update processing status
                _agentStatus.value = _agentStatus.value.copy(isProcessing = false)
            }

            "status_update" -> {
                val currentAgent = wsMessage.metadata["current_agent"] as? String
                val progress = (wsMessage.metadata["progress"] as? Double)?.toInt() ?: 0
                val isProcessing = wsMessage.metadata["is_processing"] as? Boolean ?: false

                _agentStatus.value = _agentStatus.value.copy(
                    currentAgent = currentAgent,
                    progress = progress,
                    currentStep = wsMessage.content,
                    isProcessing = isProcessing
                )

                // Add status message to chat
                if (wsMessage.content.isNotEmpty()) {
                    val statusMessage = Message(
                        content = "🔄 ${wsMessage.content}",
                        isFromAgent = true,
                        messageType = "status"
                    )
                    _messages.value = _messages.value + statusMessage
                }
            }

            "error" -> {
                _agentStatus.value = _agentStatus.value.copy(
                    error = wsMessage.content,
                    isProcessing = false
                )

                val errorMessage = Message(
                    content = "❌ ${wsMessage.content}",
                    isFromAgent = true,
                    messageType = "error"
                )
                _messages.value = _messages.value + errorMessage
            }
        }
    }

    fun sendMessage(content: String) {
        if (webSocket == null || !_agentStatus.value.isConnected) {
            val errorMessage = Message(
                content = "❌ Not connected to agent",
                isFromAgent = true,
                messageType = "error"
            )
            _messages.value = _messages.value + errorMessage
            return
        }

        // Add user message to chat
        val userMessage = Message(
            content = content,
            isFromAgent = false
        )
        _messages.value = _messages.value + userMessage

        // Send to WebSocket
        val wsMessage = WSMessage(
            type = "user_message",
            content = content,
            session_id = sessionId
        )

        val json = gson.toJson(wsMessage)
        webSocket?.send(json)

        // Update processing status
        _agentStatus.value = _agentStatus.value.copy(isProcessing = true)
    }

    fun reconnect() {
        webSocket?.close(1000, "Reconnecting")
        connectWebSocket()
    }

    fun resetAgent() {
        val wsMessage = WSMessage(
            type = "reset_agent",
            content = "reset",
            session_id = sessionId
        )

        val json = gson.toJson(wsMessage)
        webSocket?.send(json)

        // Clear local messages
        _messages.value = listOf(
            Message(
                content = "🔄 Agent reset requested",
                isFromAgent = true,
                messageType = "status"
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        webSocket?.close(1000, "ViewModel cleared")
    }
}

// Enhanced UI Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentAction(navController: NavController) {
    val viewModel = remember { AgentWebSocketViewModel() }
    val messages by viewModel.messages.collectAsState()
    val agentStatus by viewModel.agentStatus.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Auto-scroll when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Connection status animation
    val connectionColor by animateColorAsState(
        targetValue = if (agentStatus.isConnected)
            Color(0xFF4CAF50) else Color(0xFFF44336),
        animationSpec = tween(300)
    )

    val pulseAnimation = rememberInfiniteTransition()
    val pulseScale by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = if (agentStatus.isProcessing) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Agent avatar with connection status
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    if (agentStatus.isProcessing)
                                        MaterialTheme.colorScheme.secondaryContainer
                                    else
                                        MaterialTheme.colorScheme.primaryContainer
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (agentStatus.isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "🤖",
                                    fontSize = 20.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Web Agent",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Connection indicator
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(connectionColor)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = when {
                                        !agentStatus.isConnected -> "Disconnected"
                                        agentStatus.isProcessing -> "${agentStatus.currentAgent ?: "Working"} (${agentStatus.progress}%)"
                                        else -> "Ready"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Reconnect button
                    if (!agentStatus.isConnected) {
                        IconButton(onClick = { viewModel.reconnect() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reconnect",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Settings
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                tonalElevation = 3.dp
            ) {
                Column {
                    // Status bar for current task
                    AnimatedVisibility(
                        visible = agentStatus.isProcessing && agentStatus.currentStep.isNotEmpty(),
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    progress = agentStatus.progress / 100f,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = agentStatus.currentStep,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (agentStatus.currentAgent != null) {
                                        Text(
                                            text = "Active: ${agentStatus.currentAgent}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Text(
                                    text = "${agentStatus.progress}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    // Input row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            placeholder = {
                                Text(
                                    when {
                                        !agentStatus.isConnected -> "Connecting to agent..."
                                        agentStatus.isProcessing -> "Agent is working..."
                                        else -> "Ask me to navigate websites, take screenshots..."
                                    }
                                )
                            },
                            enabled = agentStatus.isConnected && !agentStatus.isProcessing,
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(24.dp),
                            trailingIcon = {
                                IconButton(
                                    onClick = { showOptionsMenu = !showOptionsMenu },
                                    enabled = agentStatus.isConnected
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More Options"
                                    )
                                }
                            },
                            maxLines = 4
                        )

                        // Send button with enhanced states
                        FloatingActionButton(
                            onClick = {
                                if (inputText.isNotEmpty() && agentStatus.isConnected && !agentStatus.isProcessing) {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                }
                            },
                            containerColor = when {
                                !agentStatus.isConnected -> MaterialTheme.colorScheme.surfaceVariant
                                agentStatus.isProcessing -> MaterialTheme.colorScheme.surfaceVariant
                                else -> MaterialTheme.colorScheme.primary
                            },
                            contentColor = when {
                                !agentStatus.isConnected -> MaterialTheme.colorScheme.onSurfaceVariant
                                agentStatus.isProcessing -> MaterialTheme.colorScheme.onSurfaceVariant
                                else -> MaterialTheme.colorScheme.onPrimary
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            when {
                                agentStatus.isProcessing -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                                !agentStatus.isConnected -> {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Disconnected"
                                    )
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Chat messages
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                state = listState,
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Welcome message if no messages
                if (messages.isEmpty()) {
                    item {
                        WelcomeMessage(isConnected = agentStatus.isConnected)
                    }
                }

                items(messages) { message ->
                    EnhancedChatMessage(message = message)
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            // Options menu
            AnimatedVisibility(
                visible = showOptionsMenu,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 72.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier.width(200.dp)
                    ) {
                        OptionItem(
                            icon = Icons.Outlined.Refresh,
                            text = "Reset Agent",
                            onClick = {
                                showResetDialog = true
                                showOptionsMenu = false
                            }
                        )

                        OptionItem(
                            icon = Icons.Outlined.Refresh,
                            text = "Reconnect",
                            onClick = {
                                viewModel.reconnect()
                                showOptionsMenu = false
                            }
                        )

                        OptionItem(
                            icon = Icons.Outlined.Settings,
                            text = "Settings",
                            onClick = {
                                showSettingsDialog = true
                                showOptionsMenu = false
                            }
                        )
                    }
                }
            }

            // Reset Dialog
            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { showResetDialog = false },
                    title = { Text("Reset Agent") },
                    text = { Text("This will reset the agent session and clear all chat history. The agent will restart with default settings.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.resetAgent()
                                showResetDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Reset")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun WelcomeMessage(isConnected: Boolean) {
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
                text = "🤖 Web Automation Agent",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isConnected)
                    "I'm ready to help you automate web tasks! Try saying:"
                else
                    "Connecting to agent...",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isConnected) {
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ExamplePrompt("Go to https://google.com and search for 'AI news'")
                    ExamplePrompt("Take a screenshot of https://github.com")
                    ExamplePrompt("Navigate to YouTube and find trending videos")
                    ExamplePrompt("Open Chess.com and start a new game")
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
fun ExamplePrompt(text: String) {
    Text(
        text = "• $text",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
fun EnhancedChatMessage(message: Message) {
    val isFromAgent = message.isFromAgent

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromAgent) Arrangement.Start else Arrangement.End
    ) {
        if (isFromAgent) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when (message.messageType) {
                            "error" -> MaterialTheme.colorScheme.errorContainer
                            "status" -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    )
                    .align(Alignment.Top),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (message.messageType) {
                        "error" -> "❌"
                        "status" -> "ℹ️"
                        else -> "🤖"
                    },
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.weight(0.8f),
            horizontalAlignment = if (isFromAgent) Alignment.Start else Alignment.End
        ) {
            Surface(
                color = when {
                    !isFromAgent -> MaterialTheme.colorScheme.primaryContainer
                    message.messageType == "error" -> MaterialTheme.colorScheme.errorContainer
                    message.messageType == "status" -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                },
                shape = RoundedCornerShape(
                    topStart = if (isFromAgent) 4.dp else 16.dp,
                    topEnd = if (isFromAgent) 16.dp else 4.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                )
            ) {
                if (message.imageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(message.imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Shared Image",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = when {
                            !isFromAgent -> MaterialTheme.colorScheme.onPrimaryContainer
                            message.messageType == "error" -> MaterialTheme.colorScheme.onErrorContainer
                            message.messageType == "status" -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(12.dp)
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

        if (!isFromAgent) {
            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .align(Alignment.Top),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👤", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun OptionItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}