package com.example.neuroed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

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
fun ChatScreen(navController: NavController) {
    // Example messages
    val initialMessages = listOf(
        ChatMessage(
            text = "Hi, good to see you! We're starting work on a presentation for a new product today, right?",
            isUser = false,
            userName = "Katy",
            avatar = R.drawable.man,
            time = "8:36 PM"
        ),
        ChatMessage(
            text = "Yes, that's right. Let's discuss the main points and structure of the presentation.",
            isUser = false,
            userName = "Katy",
            avatar = R.drawable.man,
            time = "8:38 PM"
        ),
        ChatMessage(
            isUser = true,
            userName = "You",
            avatar = R.drawable.man,
            time = "8:40 PM",
            isVoice = true,
            voiceDuration = "1:04"
        ),
        ChatMessage(
            text = "Okay, then let's divide the presentation into a few main sections: introduction, product description, features and benefits, use cases, and conclusion.",
            isUser = false,
            userName = "Katy",
            avatar = R.drawable.man,
            time = "8:42 PM"
        )
    )

    // Mutable state for messages (so we can add new ones)
    val messages = remember { mutableStateListOf<ChatMessage>().apply { addAll(initialMessages) } }

    // State for user text input
    var userInput by remember { mutableStateOf(TextFieldValue("")) }

    // For scrolling to the latest message
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Main container with gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Black, Color(0xFF1E1E1E)) // black to dark gray
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Bar
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Contact's avatar
                        Icon(
                            painter = painterResource(id = R.drawable.man),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Aysha Hayes",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: handle notification click */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.notificationbell),
                            contentDescription = "Notifications",
                            tint = Color.White
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
                        messages.add(
                            ChatMessage(
                                text = text,
                                isUser = true,
                                userName = "You",
                                avatar = R.drawable.man,
                                time = "8:45 PM" // or get current time
                            )
                        )
                        userInput = TextFieldValue("")

                        // Scroll to the newest message
                        coroutineScope.launch {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    }
                },
                onMicClick = {
                    // TODO: handle voice recording
                }
            )
        }
    }
}

// Renders each chat message (bubble + avatar + timestamp)
@Composable
fun ChatBubble(message: ChatMessage) {
    // Align bubble left (other user) or right (you)
    val horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = horizontalAlignment
    ) {
        // Show avatar on left if not user, on right if user
        if (!message.isUser) {
            // Other user bubble
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
            // Current user bubble
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

        // Timestamp
        Box(modifier = Modifier.padding(horizontal = 48.dp, vertical = 2.dp)) {
            Text(
                text = message.time,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

// Decides whether to show text or voice bubble
@Composable
fun BubbleContent(message: ChatMessage) {
    val bubbleColor = if (message.isUser) Color(0xFF4CAF50) else Color(0xFF3A3A3A)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bubbleColor)
            .padding(12.dp)
    ) {
        if (message.isVoice) {
            VoiceMessageBubble(duration = message.voiceDuration)
        } else {
            Text(
                text = message.text,
                color = Color.White
            )
        }
    }
}

// Example voice message UI
@Composable
fun VoiceMessageBubble(duration: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Play icon
        Icon(
            painter = painterResource(id = R.drawable.voice),
            contentDescription = "Play Voice",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Simple waveform placeholder
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(24.dp)
                .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Duration text
        Text(
            text = duration,
            color = Color.White
        )
    }
}

// Bottom bar with mic + input + send
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBar(
    userInput: String,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onMicClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mic icon
        IconButton(onClick = onMicClick) {
            Icon(
                painter = painterResource(id = R.drawable.voice),
                contentDescription = "Mic",
                tint = Color.White
            )
        }

        // Text field
        TextField(
            value = userInput,
            onValueChange = onInputChange,
            placeholder = { Text("Type a message...", color = Color.Gray) },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.textFieldColors(
//                textColor = Color.White,
                containerColor = Color(0xFF2C2C2C), // A dark container background
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
//                placeholderColor = Color.LightGray
            )
        )


        // Send icon
        IconButton(onClick = onSendClick) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = Color.White
            )
        }
    }
}
