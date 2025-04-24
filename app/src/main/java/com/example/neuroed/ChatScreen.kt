package com.example.neuroed

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data class for each message in the chat
data class ChatMessage(
    val text: String = "",
    val isUser: Boolean,      // true if it's the current user's message
    val userName: String,     // e.g. "Katy" or "You"
    val avatar: Int,          // Drawable resource for avatar
    val time: String,         // e.g. "8:38 PM"
    val isVoice: Boolean = false,  // true if this is a voice message
    val voiceDuration: String = "" // e.g. "1:04"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController,
               characterId: Int
) {
    // Mutable state for messages (so we can add new ones)
    val messages = remember { mutableStateListOf<ChatMessage>() }

    // Example initial messages
    val initialMessages = listOf(
        ChatMessage(
            text = "Hi, good to see you! We're starting work on a presentation for a new product today, right?",
            isUser = false,
            userName = "Katy",
            avatar = R.drawable.man,
            time = "8:36 PM"
        )
    )
    if (messages.isEmpty()) messages.addAll(initialMessages)

    // State for user text input
    var userInput by remember { mutableStateOf(TextFieldValue("")) }

    // For scrolling to the latest message
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll whenever a message is added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty())
            listState.animateScrollToItem(messages.size - 1)
    }

    // Hold a reference to the WebSocket so that we can send messages later.
    val webSocketRef = remember { mutableStateOf<WebSocket?>(null) }

    // Create and manage WebSocket connection
    DisposableEffect(Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("ws://localhost:8000/api/CharacterChat/")
            .build()
        val webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connection opened")
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Received text: $text")
                try {
                    val jsonParser = JsonParser()
                    val jsonObject = jsonParser.parse(text).asJsonObject

                    // Get the "message" object and the Agentmessage string
                    val messageObj = jsonObject.getAsJsonObject("message")
                    val content = messageObj.get("Agentmessage").asString
                    val currentTime =
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

                    Handler(Looper.getMainLooper()).post {
                        messages.add(
                            ChatMessage(
                                text = content,
                                isUser = false,
                                userName = "Server",
                                avatar = R.drawable.man,
                                time = currentTime
                            )
                        )
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Closing: $code / $reason")
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error: ${t.message}")
            }
        })
        webSocketRef.value = webSocket
        onDispose {
            webSocket.close(1000, "ChatScreen disposed")
        }
    }

    // Main container with a solid background color (Color(0xFF1E1E1E))
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top AppBar using the same container color as our chat bubbles
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            // When the user clicks the profile (avatar), navigate to AgentInfoScreen
                            navController.navigate("AgentInfoScreen")
                        }
                    ) {
                        // Contact's avatar wrapped in a clickable
                        Icon(
                            painter = painterResource(id = R.drawable.man),
                            contentDescription = "Profile Avatar",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Aysha Hayes",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )

            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message)
                }
            }

            // Bottom Bar (Input + Send)
            BottomBar(
                userInput = userInput.text,
                onInputChange = { userInput = TextFieldValue(it) },
                onSendClick = {
                    val text = userInput.text
                    if (text.isNotEmpty()) {
                        // Add the message to the UI list
                        messages.add(
                            ChatMessage(
                                text = text,
                                isUser = true,
                                userName = "You",
                                avatar = R.drawable.man,
                                time = "8:45 PM" // or use current time
                            )
                        )
                        // Send the message to the backend via WebSocket
                        val jsonMessage = """{"message": "${userInput.text}"}"""
                        webSocketRef.value?.send(jsonMessage)
                        userInput = TextFieldValue("")

                        // Scrolling is automatically handled by the LaunchedEffect.
                    }
                }
            )
        }
    }
}

// Renders each chat message (bubble + avatar + timestamp)
@Composable
fun ChatBubble(message: ChatMessage) {
    val horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = horizontalAlignment
    ) {
        if (!message.isUser) {
            Row(verticalAlignment = Alignment.Bottom) {
                Icon(
                    painter = painterResource(id = message.avatar),
                    contentDescription = "${message.userName} Avatar",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                BubbleContent(message)
            }
        } else {
            Row(verticalAlignment = Alignment.Bottom) {
                Spacer(modifier = Modifier.weight(1f))
                BubbleContent(message)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = message.avatar),
                    contentDescription = "Your Avatar",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    tint = Color.Unspecified
                )
            }
        }
        Box(modifier = Modifier.padding(horizontal = 48.dp, vertical = 2.dp)) {
            Text(
                text = message.time,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

// Enhanced chat bubble using Card with clear styling and border
@Composable
fun BubbleContent(message: ChatMessage) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            if (message.isVoice) {
                VoiceMessageBubble(duration = message.voiceDuration)
            } else {
                Text(
                    text = message.text,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Example voice message UI remains unchanged
@Composable
fun VoiceMessageBubble(duration: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = R.drawable.voice),
            contentDescription = "Play Voice",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(24.dp)
                .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = duration,
            color = Color.White
        )
    }
}

// Bottom bar with input and send button
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBar(
    userInput: String,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = userInput,
            onValueChange = onInputChange,
            placeholder = { Text("Type a message...", color = Color.Gray) },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color(0xFF2C2C2C),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            )
        )
        IconButton(onClick = onSendClick) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = Color.White
            )
        }
    }
}
