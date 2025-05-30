package com.example.neuroed

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import kotlin.random.Random
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically

// --- Data models ---

data class Player(
    val id: String,
    val name: String,
    val color: Color,
    val score: Int = 0,
    val isAI: Boolean = false,
    var tokenPositions: MutableList<Int> = mutableListOf(0, 0, 0, 0),
    var tokensInHome: Int = 4,
    var tokensCompleted: Int = 0
)

data class GameState(
    val currentPlayerIndex: Int = 0,
    val diceValue: Int = 6,
    val isRolling: Boolean = false,
    val gameMessage: String = "Tap dice to roll",
    val lastMoveTime: Long = 0L,
    val isGameActive: Boolean = true
)

// --- AI Agent ---

class AIAgent(
    private val webSocket: WebSocket?,
    private val player: Player,
    private val onMove: (Int, Int) -> Unit // tokenIndex, diceValue
) {
    fun makeMove(diceValue: Int) {
        // Simulate thinking
        Thread.sleep(Random.nextLong(800, 1500))

        val validTokenIndices = mutableListOf<Int>()

        // Check which tokens can be moved
        for (i in player.tokenPositions.indices) {
            val position = player.tokenPositions[i]

            // If token is in home and rolled a 6
            if (position == 0 && diceValue == 6) {
                validTokenIndices.add(i)
            }
            // If token is already on board
            else if (position > 0) {
                // Check if token can move without going over the end point (simplified)
                if (position + diceValue <= 57) {
                    validTokenIndices.add(i)
                }
            }
        }

        // Choose a token to move
        if (validTokenIndices.isNotEmpty()) {
            val tokenToMove = validTokenIndices.random()

            // Send move to server
            val movePayload = JSONObject().apply {
                put("type", "move_token")
                put("player_id", player.id)
                put("token_index", tokenToMove)
                put("dice_value", diceValue)
            }
            webSocket?.send(movePayload.toString())

            // Update UI
            onMove(tokenToMove, diceValue)
        } else {
            // No valid moves, pass turn
            val passPayload = JSONObject().apply {
                put("type", "pass_turn")
                put("player_id", player.id)
            }
            webSocket?.send(passPayload.toString())
        }
    }
}

// --- WebSocket Listener ---

class GameWebSocketListener(
    private val onMessageReceive: (String) -> Unit,
    private val onConnectionOpen: () -> Unit,
    private val onConnectionClosed: (Int, String) -> Unit,
    private val onConnectionError: (Throwable) -> Unit
) : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WS", "Connection opened")
        onConnectionOpen()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("WS", "Message received: $text")
        onMessageReceive(text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        onMessageReceive(bytes.utf8())
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("WS", "Connection closing: $code - $reason")
        onConnectionClosed(code, reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("WS", "Error: ${t.message}")
        onConnectionError(t)
    }
}

// --- UI Composables ---

@Composable
fun AIAgentStatusBar(
    player: Player,
    isThinking: Boolean,
    diceValue: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFF1A237E))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(player.color)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Person, contentDescription = "AI", tint = Color.White)
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = player.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = if (isThinking) "Thinking..." else "Ready",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isThinking) {
                val infiniteTransition = rememberInfiniteTransition(label = "thinking")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(500),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = alpha)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = diceValue.toString(),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Badge(
                    containerColor = Color(0xFF4FC3F7),
                    contentColor = Color.Black
                ) {
                    Text(
                        text = "Score: ${player.score}",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UserProfileBar(
    profileImage: ImageBitmap,
    player: Player,
    onVoiceClick: () -> Unit,
    connectionStatus: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        color = Color(0xFF2C2C2C),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    bitmap = profileImage,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, player.color, CircleShape)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = player.name,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (connectionStatus == "Connected") Color.Green else Color.Red)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = connectionStatus,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Badge(
                    containerColor = Color(0xFFFFC107),
                    contentColor = Color.Black
                ) {
                    Text(
                        text = "Score: ${player.score}",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                IconButton(onClick = onVoiceClick) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Voice Command",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun StatusCard(currentPlayer: Player, gameMessage: String, isYourTurn: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isYourTurn) Color(0xFF2E7D32) else Color(0xFF2C2C2C)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(currentPlayer.color)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${currentPlayer.name}'s turn",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isYourTurn) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Your Turn",
                        tint = Color.White,
                        modifier = Modifier.graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = gameMessage,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PlayerInfo(player: Player, isCurrentPlayer: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .border(
                width = if (isCurrentPlayer) 2.dp else 0.dp,
                color = if (isCurrentPlayer) player.color else Color.Transparent,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(8.dp)
    ) {
        // Avatar/Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(player.color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (player.isAI) Icons.Rounded.Info else Icons.Rounded.Person,
                contentDescription = "Player",
                tint = Color.White
            )
        }

        Spacer(Modifier.width(8.dp))

        // Player info
        Column {
            Text(
                text = player.name,
                color = Color.White,
                fontWeight = if (isCurrentPlayer) FontWeight.Bold else FontWeight.Normal
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Home",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = "${player.tokensInHome}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )

                Spacer(Modifier.width(6.dp))

                Icon(
                    Icons.Default.Star,
                    contentDescription = "Completed",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = "${player.tokensCompleted}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }

        if (isCurrentPlayer) {
            Spacer(Modifier.width(8.dp))
            Badge(
                containerColor = player.color.copy(alpha = 0.3f),
                contentColor = Color.White
            ) {
                Text("Active", fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun AnimatedDice(diceValue: Int, isRolling: Boolean, onDiceClick: () -> Unit, isEnabled: Boolean) {
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            scale.animateTo(
                targetValue = 1.2f,
                animationSpec = tween(durationMillis = 100)
            )
            rotation.animateTo(
                targetValue = rotation.value + 720f,
                animationSpec = repeatable(
                    iterations = 2,
                    animation = tween(durationMillis = 500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300)
            )
        }
    }

    Box(
        modifier = Modifier
            .size(80.dp)
            .graphicsLayer {
                rotationZ = rotation.value
                scaleX = scale.value
                scaleY = scale.value
            }
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isRolling) Color(0xFFE0E0E0)
                else if (isEnabled) Color.White
                else Color.Gray.copy(alpha = 0.7f),
                RoundedCornerShape(12.dp)
            )
            .border(2.dp, Color(0xFFBDBDBD), RoundedCornerShape(12.dp))
            .clickable(enabled = !isRolling && isEnabled) { onDiceClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width/2, size.height/2)
            val r = size.width/10
            fun dot(x: Float, y: Float) = drawCircle(color = Color.Black, radius = r, center = Offset(x,y))
            when (diceValue) {
                1 -> dot(center.x, center.y)
                2 -> { dot(size.width*0.25f, size.height*0.25f); dot(size.width*0.75f, size.height*0.75f) }
                3 -> { dot(size.width*0.25f, size.height*0.25f); dot(center.x, center.y); dot(size.width*0.75f, size.height*0.75f) }
                4 -> { dot(size.width*0.25f, size.height*0.25f); dot(size.width*0.75f, size.height*0.25f);
                    dot(size.width*0.25f, size.height*0.75f); dot(size.width*0.75f, size.height*0.75f) }
                5 -> { dot(size.width*0.25f, size.height*0.25f); dot(size.width*0.75f, size.height*0.25f);
                    dot(center.x, center.y);
                    dot(size.width*0.25f, size.height*0.75f); dot(size.width*0.75f, size.height*0.75f) }
                6 -> for (cx in listOf(0.25f,0.75f)) for (cy in listOf(0.25f,0.5f,0.75f)) dot(size.width*cx, size.height*cy)
            }
        }

//        if (!isEnabled && !isRolling) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Black.copy(alpha = 0.3f)),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    Icons.Default.Lock,
//                    contentDescription = "Disabled",
//                    tint = Color.White
//                )
//            }
//        }
    }
}

@Composable
fun TokenView(
    color: Color,
    isSelected: Boolean,
    onTokenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color.White else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onTokenClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

@Composable
fun LudoBoard(
    modifier: Modifier = Modifier,
    greenColor: Color,
    yellowColor: Color,
    redColor: Color,
    blueColor: Color,
    whiteColor: Color,
    players: List<Player>,
    onPositionClick: (Int, Int) -> Unit, // row, col
    currentPlayerTokens: List<Pair<Int, Int>> // row, col
) {
    Box(modifier = modifier.background(Color(0xFFEEEEEE))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val boardSize = size.width
            val cell = boardSize / 15f

            // Draw board
            for (i in 0 until 15) {
                for (j in 0 until 15) {
                    val tl = Offset(i * cell, j * cell)
                    val bg = when {
                        // Home bases
                        i in 0..5 && j in 0..5 -> greenColor.copy(alpha = 0.7f)
                        i in 9..14 && j in 0..5 -> yellowColor.copy(alpha = 0.7f)
                        i in 0..5 && j in 9..14 -> redColor.copy(alpha = 0.7f)
                        i in 9..14 && j in 9..14 -> blueColor.copy(alpha = 0.7f)

                        // Center
                        (i in 6..8 && j in 6..8) -> Color(0xFF424242)

                        // Paths
                        (i == 7 && j < 6) || (i == 7 && j > 8) -> Color(0xFFE0E0E0)
                        (j == 7 && i < 6) || (j == 7 && i > 8) -> Color(0xFFE0E0E0)

                        // Safe spots
                        (i == 2 && j == 6) || (i == 8 && j == 2) -> Color(0xFF90CAF9)
                        (i == 12 && j == 8) || (i == 6 && j == 12) -> Color(0xFF90CAF9)

                        else -> whiteColor
                    }
                    drawRect(color = bg, topLeft = tl, size = Size(cell, cell), style = Fill)
                    drawRect(color = Color.Gray.copy(alpha = 0.5f), topLeft = tl, size = Size(cell, cell), style = Stroke(width = 1f))

                    // Draw home arrows
                    if (i == 7 && j == 1) drawArrow(greenColor, tl, cell, ArrowDirection.DOWN)
                    if (i == 13 && j == 7) drawArrow(yellowColor, tl, cell, ArrowDirection.LEFT)
                    if (i == 7 && j == 13) drawArrow(redColor, tl, cell, ArrowDirection.UP)
                    if (i == 1 && j == 7) drawArrow(blueColor, tl, cell, ArrowDirection.RIGHT)
                }
            }
        }

        // Place tokens on the board
        Box(modifier = Modifier.fillMaxSize()) {
            // Draw tokens for each player at their positions
            // This is simplified - you would map game positions to x,y coordinates

            // Green home tokens
            Box(
                modifier = Modifier
                    .offset(x = 55.dp, y = 55.dp)
                    .size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TokenView(
                            color = greenColor,
                            isSelected = false,
                            onTokenClick = { onPositionClick(1, 1) }
                        )
                        TokenView(
                            color = greenColor,
                            isSelected = false,
                            onTokenClick = { onPositionClick(1, 4) }
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TokenView(
                            color = greenColor,
                            isSelected = false,
                            onTokenClick = { onPositionClick(4, 1) }
                        )
                        TokenView(
                            color = greenColor,
                            isSelected = false,
                            onTokenClick = { onPositionClick(4, 4) }
                        )
                    }
                }
            }

            // Yellow home tokens
            Box(
                modifier = Modifier
                    .offset(x = 255.dp, y = 55.dp)
                    .size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TokenView(
                            color = yellowColor,
                            isSelected = false,
                            onTokenClick = { onPositionClick(1, 10) }
                        )
                        TokenView(
                            color = yellowColor,
                            isSelected = false,
                            onTokenClick = { onPositionClick(1, 13) }
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TokenView(
                            color = yellowColor,
                            isSelected = false,
                            onTokenClick = { onPositionClick(4, 10) }
                        )
                        TokenView(
                            color = yellowColor,
                            isSelected = false,
                            onTokenClick = { onPositionClick(4, 13) }
                        )
                    }
                }
            }

            // Red home tokens
            Box(
                modifier = Modifier
                    .offset(x = 55.dp, y = 255.dp)
                    .size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TokenView(
                            color = redColor,
                            isSelected = currentPlayerTokens.contains(Pair(10, 1)),
                            onTokenClick = { onPositionClick(10, 1) }
                        )
                        TokenView(
                            color = redColor,
                            isSelected = currentPlayerTokens.contains(Pair(10, 4)),
                            onTokenClick = { onPositionClick(10, 4) }
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TokenView(
                            color = redColor,
                            isSelected = currentPlayerTokens.contains(Pair(13, 1)),
                            onTokenClick = { onPositionClick(13, 1) }
                        )
                        TokenView(
                            color = redColor,
                            isSelected = currentPlayerTokens.contains(Pair(13, 4)),
                            onTokenClick = { onPositionClick(13, 4) }
                        )
                    }
                }
            }

            // Blue home tokens
            Box(
                modifier = Modifier
                    .offset(x = 255.dp, y = 255.dp)
                    .size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TokenView(
                            color = blueColor,
                            isSelected = false,
                            onTokenClick = { onPositionClick(10, 10) }
                        )
                        TokenView(
                            color = blueColor,
                            isSelected = false,
                            onTokenClick = { onPositionClick(10, 13) }
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TokenView(
                            color = blueColor,
                            isSelected = false,
                            onTokenClick = { onPositionClick(13, 10) }
                        )
                        TokenView(
                            color = blueColor,
                            isSelected = false,
                            onTokenClick = { onPositionClick(13, 13) }
                        )
                    }
                }
            }

            // Example of tokens on the path
            TokenView(
                color = redColor,
                isSelected = currentPlayerTokens.contains(Pair(8, 11)),
                onTokenClick = { onPositionClick(8, 11) },
                modifier = Modifier.offset(x = 168.dp, y = 264.dp)
            )

            TokenView(
                color = greenColor,
                isSelected = false,
                onTokenClick = { onPositionClick(6, 7) },
                modifier = Modifier.offset(x = 144.dp)
                )
        }
    }
}

enum class ArrowDirection { UP, DOWN, LEFT, RIGHT }

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrow(
    color: Color,
    topLeft: Offset,
    cellSize: Float,
    direction: ArrowDirection
) {
    val arrowSize = cellSize * 0.6f
    val center = Offset(topLeft.x + cellSize/2, topLeft.y + cellSize/2)

    when (direction) {
        ArrowDirection.UP -> {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x, center.y - arrowSize/2)
                lineTo(center.x - arrowSize/3, center.y + arrowSize/2)
                lineTo(center.x + arrowSize/3, center.y + arrowSize/2)
                close()
            }
            drawPath(path = path, color = color, style = Fill)
        }
        ArrowDirection.DOWN -> {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x, center.y + arrowSize/2)
                lineTo(center.x - arrowSize/3, center.y - arrowSize/2)
                lineTo(center.x + arrowSize/3, center.y - arrowSize/2)
                close()
            }
            drawPath(path = path, color = color, style = Fill)
        }
        ArrowDirection.LEFT -> {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x - arrowSize/2, center.y)
                lineTo(center.x + arrowSize/2, center.y - arrowSize/3)
                lineTo(center.x + arrowSize/2, center.y + arrowSize/3)
                close()
            }
            drawPath(path = path, color = color, style = Fill)
        }
        ArrowDirection.RIGHT -> {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x + arrowSize/2, center.y)
                lineTo(center.x - arrowSize/2, center.y - arrowSize/3)
                lineTo(center.x - arrowSize/2, center.y + arrowSize/3)
                close()
            }
            drawPath(path = path, color = color, style = Fill)
        }
    }
}

@Composable
fun ConnectionStatusBanner(status: String, isConnected: Boolean) {
    val backgroundColor = if (isConnected) Color(0xFF388E3C) else Color(0xFFD32F2F)
    val icon = if (isConnected) Icons.Default.DateRange else Icons.Default.PlayArrow

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = "Connection Status", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(status, color = Color.White, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun GameControlButtons(
    onHelpClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OutlinedButton(
            onClick = onHelpClick,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, Color.White)
        ) {
            Icon(Icons.Default.Home, contentDescription = "Help")
            Spacer(modifier = Modifier.width(4.dp))
            Text("Help")
        }

        OutlinedButton(
            onClick = onSettingsClick,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, Color.White)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
            Spacer(modifier = Modifier.width(4.dp))
            Text("Settings")
        }

        Button(
            onClick = onChatClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Icon(Icons.Default.Face, contentDescription = "Chat")
            Spacer(modifier = Modifier.width(4.dp))
            Text("Chat")
        }
    }
}

@Composable
fun LudoGameScreen(navController: NavController) {
    val greenColor = Color(0xFF4CAF50)
    val yellowColor = Color(0xFFFFEB3B)
    val redColor = Color(0xFFF44336)
    val blueColor = Color(0xFF2196F3)
    val whiteColor = Color(0xFFFFFFFF)
    val darkBg = Color(0xFF1E1E1E)

    var gameState by remember { mutableStateOf(GameState()) }
    var players by remember {
        mutableStateOf(
            listOf(
                Player("ai_agent", "BuddyAI", greenColor, isAI = true),
                Player("user", "You", redColor),
                Player("player3", "Player 3", yellowColor),
                Player("player4", "Player 4", blueColor, isAI = true)
            )
        )
    }

    var connectionStatus by remember { mutableStateOf("Connecting...") }
    var isConnected by remember { mutableStateOf(false) }
    var aiThinking by remember { mutableStateOf(false) }
    var selectedTokenIndices by remember { mutableStateOf(emptyList<Pair<Int, Int>>()) }

    val coroutineScope = rememberCoroutineScope()

    // Load avatar resource
    val avatar = ImageBitmap.imageResource(R.drawable.man)

    // WebSocket setup
    var socket by remember { mutableStateOf<WebSocket?>(null) }
    val webSocketListener = remember {
        GameWebSocketListener(
            onMessageReceive = { message ->
                try {
                    val json = JSONObject(message)
                    when (json.getString("type")) {
                        "game_state_update" -> {
                            val stateJson = json.getJSONObject("state")
                            val currentPlayerIdx = stateJson.getInt("current_player_index")
                            val diceVal = stateJson.getInt("dice_value")
                            val gameMsg = stateJson.getString("message")

                            gameState = gameState.copy(
                                currentPlayerIndex = currentPlayerIdx,
                                diceValue = diceVal,
                                gameMessage = gameMsg
                            )

                            // Update AI thinking state
                            aiThinking = players[currentPlayerIdx].isAI

                            // Update player data from server
                            val playersJson = stateJson.getJSONArray("players")
                            val updatedPlayers = players.toMutableList()

                            for (i in 0 until playersJson.length()) {
                                val playerJson = playersJson.getJSONObject(i)
                                val id = playerJson.getString("id")
                                val score = playerJson.getInt("score")
                                val tokensInHome = playerJson.getInt("tokens_in_home")
                                val tokensCompleted = playerJson.getInt("tokens_completed")

                                val tokenPositionsJson = playerJson.getJSONArray("token_positions")
                                val tokenPositions = MutableList(4) { 0 }
                                for (j in 0 until tokenPositionsJson.length()) {
                                    tokenPositions[j] = tokenPositionsJson.getInt(j)
                                }

                                val playerIndex = updatedPlayers.indexOfFirst { it.id == id }
                                if (playerIndex >= 0) {
                                    updatedPlayers[playerIndex] = updatedPlayers[playerIndex].copy(
                                        score = score,
                                        tokensInHome = tokensInHome,
                                        tokensCompleted = tokensCompleted,
                                        tokenPositions = tokenPositions
                                    )
                                }
                            }

                            players = updatedPlayers
                        }
                        "ai_action" -> {
                            val aiId = json.getString("player_id")
                            val action = json.getString("action")
                            val diceValue = json.optInt("dice_value", 0)
                            val tokenIndex = json.optInt("token_index", -1)

                            when (action) {
                                "rolling" -> {
                                    gameState = gameState.copy(
                                        isRolling = true,
                                        gameMessage = "${players.first { it.id == aiId }.name} is rolling..."
                                    )
                                }
                                "move_token" -> {
                                    val position = json.getInt("position")
                                    gameState = gameState.copy(
                                        gameMessage = "${players.first { it.id == aiId }.name} moved token ${tokenIndex+1} by $diceValue steps"
                                    )
                                }
                                "pass_turn" -> {
                                    gameState = gameState.copy(
                                        gameMessage = "${players.first { it.id == aiId }.name} has no valid moves - passing turn"
                                    )
                                }
                            }
                        }
                        "error" -> {
                            val errorMsg = json.getString("message")
                            gameState = gameState.copy(gameMessage = "Error: $errorMsg")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WS", "Error parsing message: ${e.message}")
                }
            },
            onConnectionOpen = {
                connectionStatus = "Connected to game server"
                isConnected = true

                // Send initial connection message
                val joinPayload = JSONObject().apply {
                    put("type", "join_game")
                    put("player_name", players[1].name)
                    put("player_id", players[1].id)
                }
                socket?.send(joinPayload.toString())
            },
            onConnectionClosed = { code, reason ->
                connectionStatus = "Disconnected: $reason"
                isConnected = false
            },
            onConnectionError = { error ->
                connectionStatus = "Connection error: ${error.message}"
                isConnected = false
            }
        )
    }

    // AI Agent setup
    val aiAgent = remember {
        AIAgent(
            webSocket = socket,
            player = players[0]
        ) { tokenIndex, diceValue ->
            // Update UI when AI makes a move
            gameState = gameState.copy(
                gameMessage = "AI moved token ${tokenIndex+1} by $diceValue"
            )
            aiThinking = false

            // AI turn handling would be managed by the server
            // This is just for UI feedback
        }
    }
    // Connect WebSocket on start
    LaunchedEffect(Unit) {
        val roomName = "room42"
        val client = OkHttpClient.Builder()
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url("ws://localhost:8000/ws/api/ludo/game/$roomName/")
            .build()
        socket = client.newWebSocket(request, webSocketListener)
    }

    // Handle AI turns
    LaunchedEffect(gameState.currentPlayerIndex) {
        val currentPlayer = players[gameState.currentPlayerIndex]
        if (currentPlayer.isAI && isConnected) {
            // AI will act after a short delay to appear more natural
            aiThinking = true
            delay(2000)

            // Roll dice
            val dicePayload = JSONObject().apply {
                put("type", "roll_dice")
                put("player_id", currentPlayer.id)
            }
            socket?.send(dicePayload.toString())

            // The server will send back the dice roll result
            // and then AI will make its move
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = darkBg) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // AI Agent status bar (top)
            AIAgentStatusBar(
                player = players[0],
                isThinking = aiThinking,
                diceValue = gameState.diceValue
            )

            // Connection status banner
            ConnectionStatusBanner(
                status = connectionStatus,
                isConnected = isConnected
            )

            // Game content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StatusCard(
                    currentPlayer = players[gameState.currentPlayerIndex],
                    gameMessage = gameState.gameMessage,
                    isYourTurn = gameState.currentPlayerIndex == 1
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LudoBoard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .shadow(8.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp)),
                        greenColor = greenColor,
                        yellowColor = yellowColor,
                        redColor = redColor,
                        blueColor = blueColor,
                        whiteColor = whiteColor,
                        players = players,
                        onPositionClick = { row, col ->
                            // Handle token selection
                            if (gameState.currentPlayerIndex == 1) { // User's turn
                                selectedTokenIndices = listOf(Pair(row, col))

                                // Send token move to server
                                val movePayload = JSONObject().apply {
                                    put("type", "move_token")
                                    put("player_id", players[1].id)
                                    put("position", "${row}_${col}")
                                    put("dice_value", gameState.diceValue)
                                }
                                socket?.send(movePayload.toString())
                            }
                        },
                        currentPlayerTokens = selectedTokenIndices
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                GameControlButtons(
                    onHelpClick = {
                        // Show help dialog
                    },
                    onSettingsClick = {
                        // Show settings
                    },
                    onChatClick = {
                        // Open chat interface
                    }
                )
            }

            // User profile & dice controls (bottom)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedDice(
                    diceValue = gameState.diceValue,
                    isRolling = gameState.isRolling,
                    isEnabled = gameState.currentPlayerIndex == 1 && !gameState.isRolling,
                    onDiceClick = {
                        if (gameState.currentPlayerIndex == 1) { // User's turn
                            coroutineScope.launch {
                                // Update local state for animation
                                gameState = gameState.copy(
                                    isRolling = true,
                                    gameMessage = "Rolling dice..."
                                )

                                // Send roll request to server
                                val rollPayload = JSONObject().apply {
                                    put("type", "roll_dice")
                                    put("player_id", players[1].id)
                                }
                                socket?.send(rollPayload.toString())

                                // Let animation play (server will send back the result)
                                delay(1000)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Badge(
                            containerColor = redColor.copy(alpha = 0.2f),
                            contentColor = redColor
                        ) {
                            Text(
                                "Your tokens",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0..3) {
                            TokenView(
                                color = redColor,
                                isSelected = selectedTokenIndices.contains(Pair(0, i)),
                                onTokenClick = {
                                    if (gameState.currentPlayerIndex == 1) {
                                        // Send token selection to server
                                        val selectPayload = JSONObject().apply {
                                            put("type", "select_token")
                                            put("player_id", players[1].id)
                                            put("token_index", i)
                                        }
                                        socket?.send(selectPayload.toString())
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // User profile bar (bottom)
            UserProfileBar(
                profileImage = avatar,
                player = players[1],
                connectionStatus = if (isConnected) "Connected" else "Disconnected",
                onVoiceClick = {
                    // Handle voice command
                    val voicePayload = JSONObject().apply {
                        put("type", "voice_command")
                        put("player_id", players[1].id)
                    }
                    socket?.send(voicePayload.toString())
                }
            )
        }
    }
}

// JSON handling utilities
object GameDataParser {
    fun parseGameState(json: String): GameState {
        val jsonObj = JSONObject(json)
        return GameState(
            currentPlayerIndex = jsonObj.getInt("current_player_index"),
            diceValue = jsonObj.getInt("dice_value"),
            isRolling = jsonObj.getBoolean("is_rolling"),
            gameMessage = jsonObj.getString("game_message"),
            lastMoveTime = jsonObj.getLong("last_move_time"),
            isGameActive = jsonObj.getBoolean("is_game_active")
        )
    }

    fun parsePlayerData(json: String): List<Player> {
        val jsonObj = JSONObject(json)
        val playersArray = jsonObj.getJSONArray("players")
        val players = mutableListOf<Player>()

        for (i in 0 until playersArray.length()) {
            val playerObj = playersArray.getJSONObject(i)
            val tokenPositionsArray = playerObj.getJSONArray("token_positions")
            val tokenPositions = MutableList(tokenPositionsArray.length()) { 0 }

            for (j in 0 until tokenPositionsArray.length()) {
                tokenPositions[j] = tokenPositionsArray.getInt(j)
            }

            val player = Player(
                id = playerObj.getString("id"),
                name = playerObj.getString("name"),
                color = Color(playerObj.getInt("color")),
                score = playerObj.getInt("score"),
                isAI = playerObj.getBoolean("is_ai"),
                tokenPositions = tokenPositions,
                tokensInHome = playerObj.getInt("tokens_in_home"),
                tokensCompleted = playerObj.getInt("tokens_completed")
            )
            players.add(player)
        }

        return players
    }
}

// Receiver for AI agent commands from backend
class AICommandReceiver(
    private val agent: AIAgent,
    private val updateGameState: (GameState) -> Unit
) {
    fun handleCommand(command: String) {
        try {
            val json = JSONObject(command)
            when (json.getString("action")) {
                "roll_dice" -> {
                    // Update UI to show AI is rolling
                    updateGameState(
                        GameState(
                            isRolling = true,
                            gameMessage = "AI is rolling the dice..."
                        )
                    )
                }
                "move_token" -> {
                    val diceValue = json.getInt("dice_value")
                    agent.makeMove(diceValue)
                }
            }
        } catch (e: Exception) {
            Log.e("AI", "Error parsing AI command: ${e.message}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLudoGameScreen() {
    MaterialTheme {
        LudoGameScreen(rememberNavController())
    }
}