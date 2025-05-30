package com.example.neuroed

import android.annotation.SuppressLint
import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

// Data classes and enums
enum class ChessAppScreen {
    START_SCREEN,
    PROFILE_SETUP,
    GAME_SCREEN
}

data class ChatMessage(
    val sender: String,
    val message: String,
    val isAI: Boolean,
    val mood: String = "neutral",
    val timestamp: Long = System.currentTimeMillis()
)

// FIXED: Complete WebSocket Connection Handler
class GameConnectionHandler(private val serverUrl: String) {
    companion object {
        private const val TAG = "ChessWebSocket"
        private const val RECONNECT_INTERVAL = 3000L // 3 seconds
        private const val MAX_RECONNECT_ATTEMPTS = 5
    }

    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        ERROR,
        RECONNECTING
    }

    // Mutable state for UI updates
    var connectionState by mutableStateOf(ConnectionState.DISCONNECTED)
        private set

    var errorMessage by mutableStateOf("")
        private set

    // WebSocket client and connection
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private var webSocket: WebSocket? = null
    private var reconnectAttempts = 0
    private var shouldReconnect = true

    // Callback functions - these will be set by the UI
    var onConnected: (() -> Unit)? = null
    var onGameUpdate: ((JSONObject) -> Unit)? = null
    var onChatMessage: ((String, String, String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null

    fun connect() {
        if (connectionState == ConnectionState.CONNECTING ||
            connectionState == ConnectionState.CONNECTED) {
            Log.d(TAG, "Already connecting or connected")
            return
        }

        connectionState = ConnectionState.CONNECTING
        errorMessage = ""
        shouldReconnect = true

        val request = Request.Builder()
            .url(serverUrl)
            .addHeader("Upgrade", "websocket")
            .addHeader("Connection", "Upgrade")
            .build()

        Log.d(TAG, "Attempting to connect to: $serverUrl")

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connection opened successfully")
                connectionState = ConnectionState.CONNECTED
                reconnectAttempts = 0
                errorMessage = ""
                onConnected?.invoke()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received message: $text")
                try {
                    val jsonMessage = JSONObject(text)
                    handleIncomingMessage(jsonMessage)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket connection failed: ${t.message}")

                connectionState = ConnectionState.ERROR
                errorMessage = t.message ?: "Connection failed"
                onError?.invoke(errorMessage)

                // Attempt reconnection
                if (shouldReconnect && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                    scheduleReconnect()
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code - $reason")
                connectionState = ConnectionState.DISCONNECTED
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code - $reason")
                connectionState = ConnectionState.DISCONNECTED
                onDisconnected?.invoke()

                // Attempt reconnection if unexpected closure
                if (shouldReconnect && code != 1000 && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                    scheduleReconnect()
                }
            }
        })
    }

    private fun handleIncomingMessage(message: JSONObject) {
        try {
            val type = message.optString("type", "")

            when (type) {
                "connection_status" -> {
                    val status = message.optString("status", "")
                    Log.d(TAG, "Connection status: $status")
                }

                "game_update" -> {
                    val gameData = message.optJSONObject("game")
                    if (gameData != null) {
                        onGameUpdate?.invoke(gameData)
                    }
                }

                "chat_message", "chat" -> {
                    val sender = message.optString("sender", "AI")
                    val messageText = message.optString("message", "")
                    val mood = message.optString("mood", "neutral")
                    onChatMessage?.invoke(sender, messageText, mood)
                }

                "error" -> {
                    val errorMsg = message.optString("message", "Unknown error")
                    Log.e(TAG, "Server error: $errorMsg")
                    onError?.invoke(errorMsg)
                }

                else -> {
                    Log.d(TAG, "Unknown message type: $type")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling message: ${e.message}")
        }
    }

    private fun scheduleReconnect() {
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            Log.e(TAG, "Max reconnection attempts reached")
            connectionState = ConnectionState.ERROR
            errorMessage = "Failed to connect after $MAX_RECONNECT_ATTEMPTS attempts"
            return
        }

        reconnectAttempts++
        connectionState = ConnectionState.RECONNECTING

        Log.d(TAG, "Scheduling reconnection attempt $reconnectAttempts in ${RECONNECT_INTERVAL}ms")

        // Use a timer or coroutine to schedule reconnection
        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (shouldReconnect) {
                    Log.d(TAG, "Attempting reconnection $reconnectAttempts")
                    connect()
                }
            }
        }, RECONNECT_INTERVAL)
    }

    fun disconnect() {
        Log.d(TAG, "Disconnecting WebSocket")
        shouldReconnect = false
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        connectionState = ConnectionState.DISCONNECTED
    }

    fun sendMessage(message: JSONObject) {
        if (connectionState != ConnectionState.CONNECTED) {
            Log.w(TAG, "Cannot send message - not connected")
            return
        }

        val messageText = message.toString()
        Log.d(TAG, "Sending message: $messageText")

        val success = webSocket?.send(messageText) ?: false
        if (!success) {
            Log.e(TAG, "Failed to send message")
        }
    }

    // Convenience methods for sending specific message types
    fun sendMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int) {
        try {
            val message = JSONObject().apply {
                put("action", "move")
                put("fromRow", fromRow)
                put("fromCol", fromCol)
                put("toRow", toRow)
                put("toCol", toCol)
            }
            sendMessage(message)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating move message: ${e.message}")
        }
    }

    fun sendChatMessage(message: String) {
        try {
            val chatMessage = JSONObject().apply {
                put("action", "chat")
                put("message", message)
            }
            sendMessage(chatMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating chat message: ${e.message}")
        }
    }

    fun sendUserInfo(name: String, skillLevel: String) {
        try {
            val userInfo = JSONObject().apply {
                put("action", "user_info")
                put("name", name)
                put("skill_level", skillLevel)
            }
            sendMessage(userInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user info message: ${e.message}")
        }
    }

    fun restartGame() {
        try {
            val restartMessage = JSONObject().apply {
                put("action", "restart")
            }
            sendMessage(restartMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating restart message: ${e.message}")
        }
    }

    fun undoMove() {
        try {
            val undoMessage = JSONObject().apply {
                put("action", "undo")
            }
            sendMessage(undoMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating undo message: ${e.message}")
        }
    }

    fun requestHint() {
        try {
            val hintMessage = JSONObject().apply {
                put("action", "hint_request")
            }
            sendMessage(hintMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating hint message: ${e.message}")
        }
    }
}

// Simplified speech service for now
class AzureSpeechService(private val context: Context) {
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
    }

    fun initialize(subscriptionKey: String, region: String) {
        // For now, just use Android TTS
    }

    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
    }

    fun shutdown() {
        tts?.shutdown()
    }
}

// Complete Chess Game Logic
class ChessGame {
    enum class PieceType { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN, NONE }
    enum class PieceColor { WHITE, BLACK, NONE }

    data class ChessPiece(val type: PieceType, val color: PieceColor)

    private val board = Array(8) { Array(8) { ChessPiece(PieceType.NONE, PieceColor.NONE) } }
    private var currentTurn = PieceColor.WHITE

    var lastMoveFrom = Pair(-1, -1)
    var lastMoveTo = Pair(-1, -1)

    init {
        setupBoard()
    }

    private fun setupBoard() {
        // Setup pawns
        for (col in 0..7) {
            board[1][col] = ChessPiece(PieceType.PAWN, PieceColor.BLACK)
            board[6][col] = ChessPiece(PieceType.PAWN, PieceColor.WHITE)
        }

        // Setup other pieces
        val pieces = arrayOf(
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
            PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        )

        for (col in 0..7) {
            board[0][col] = ChessPiece(pieces[col], PieceColor.BLACK)
            board[7][col] = ChessPiece(pieces[col], PieceColor.WHITE)
        }
    }

    fun getPieceAt(row: Int, col: Int): ChessPiece {
        if (row < 0 || row >= 8 || col < 0 || col >= 8) {
            return ChessPiece(PieceType.NONE, PieceColor.NONE)
        }
        return board[row][col]
    }

    fun setPieceAt(row: Int, col: Int, piece: ChessPiece) {
        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            board[row][col] = piece
        }
    }

    fun getCurrentTurn(): PieceColor = currentTurn
    fun setCurrentTurn(turn: PieceColor) { currentTurn = turn }

    fun updateLastMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int) {
        lastMoveFrom = Pair(fromRow, fromCol)
        lastMoveTo = Pair(toRow, toCol)
    }

    fun getCapturedPieces(color: PieceColor): List<ChessPiece> = emptyList()

    // FIXED: Update board from server data
    fun updateFromServerData(boardData: JSONArray) {
        try {
            for (row in 0 until 8) {
                for (col in 0 until 8) {
                    val pieceData = boardData.getJSONArray(row).getJSONObject(col)
                    val pieceType = when (pieceData.getString("type")) {
                        "KING" -> PieceType.KING
                        "QUEEN" -> PieceType.QUEEN
                        "ROOK" -> PieceType.ROOK
                        "BISHOP" -> PieceType.BISHOP
                        "KNIGHT" -> PieceType.KNIGHT
                        "PAWN" -> PieceType.PAWN
                        else -> PieceType.NONE
                    }
                    val pieceColor = when (pieceData.getString("color")) {
                        "WHITE" -> PieceColor.WHITE
                        "BLACK" -> PieceColor.BLACK
                        else -> PieceColor.NONE
                    }
                    board[row][col] = ChessPiece(pieceType, pieceColor)
                }
            }
        } catch (e: Exception) {
            Log.e("ChessGame", "Error updating board from server: ${e.message}")
        }
    }
}

object ChessColors {
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1e3c72), Color(0xFF2a5298))
    )
    val lightSquare = Color(0xFFF0D9B5)
    val darkSquare = Color(0xFFB58863)
    val goldAccent = Color(0xFFFFD700)
    val selectedSquare = Color(0xFF76C043)
    val validMove = Color(0xFF4CAF50).copy(alpha = 0.6f)
    val lastMoveHighlight = Color(0xFFFFCC00).copy(alpha = 0.5f)
    val cardBackground = Color(0xFF1E1E1E).copy(alpha = 0.8f)
    val aiColor = Color(0xFFE57373)
    val userColor = Color(0xFF64B5F6)
}

@Composable
fun ChessGameScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Game state
    val chessGame = remember { ChessGame() }
    var selectedSquare by remember { mutableStateOf(Pair(-1, -1)) }
    var validMoves by remember { mutableStateOf(setOf<Pair<Int, Int>>()) }
    var gameStatus by remember { mutableStateOf("Connecting...") }
    var isAiThinking by remember { mutableStateOf(false) }

    // Chat system
    var chatMessages = remember { mutableStateListOf<ChatMessage>() }
    var chatInput by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()

    // User info
    var userName by remember { mutableStateOf("Player") }
    var userSkillLevel by remember { mutableStateOf("Beginner") }
    var currentScreen by remember { mutableStateOf(ChessAppScreen.START_SCREEN) }

    // FIXED: WebSocket connection with proper URL
    val connectionHandler = remember {
        // Use your actual server IP - replace with your computer's IP address
        // For Android Emulator: "ws://10.0.2.2:8000/ws/chess/"
        // For Real Device: "ws://192.168.1.100:8000/ws/chess/" (replace with your IP)
        GameConnectionHandler("ws://localhost:8000/ws/chess/")
    }

    val speechService = remember { AzureSpeechService(context) }

    // FIXED: Setup WebSocket callbacks
    LaunchedEffect(Unit) {
        connectionHandler.onConnected = {
            gameStatus = "Connected! Setting up game..."
            connectionHandler.sendUserInfo(userName, userSkillLevel)
        }

        connectionHandler.onGameUpdate = { gameData ->
            try {
                // Update board
                val board = gameData.getJSONArray("board")
                chessGame.updateFromServerData(board)

                // Update turn
                val currentTurn = gameData.getString("currentTurn")
                chessGame.setCurrentTurn(
                    if (currentTurn == "WHITE") ChessGame.PieceColor.WHITE
                    else ChessGame.PieceColor.BLACK
                )

                // Update game status
                gameStatus = if (currentTurn == "WHITE") "Your turn" else "AI is thinking..."
                isAiThinking = currentTurn == "BLACK"

            } catch (e: Exception) {
                Log.e("ChessGame", "Error processing game update: ${e.message}")
            }
        }

        connectionHandler.onChatMessage = { sender, message, mood ->
            coroutineScope.launch {
                val isAI = sender == "BuddyAI"
                chatMessages.add(ChatMessage(sender, message, isAI, mood))

                // Auto-scroll to latest message
                if (chatMessages.isNotEmpty()) {
                    lazyListState.animateScrollToItem(chatMessages.size - 1)
                }

                // Speak AI messages
                if (isAI) {
                    speechService.speak(message)
                }
            }
        }

        connectionHandler.onError = { error ->
            gameStatus = "Connection error: $error"
            Log.e("ChessGame", "WebSocket error: $error")
        }

        connectionHandler.onDisconnected = {
            gameStatus = "Disconnected from server"
        }
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            connectionHandler.disconnect()
            speechService.shutdown()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChessColors.backgroundGradient)
        ) {
            when (currentScreen) {
                ChessAppScreen.START_SCREEN -> {
                    StartScreen(
                        onStartGame = { currentScreen = ChessAppScreen.PROFILE_SETUP }
                    )
                }

                ChessAppScreen.PROFILE_SETUP -> {
                    ProfileSetupScreen(
                        userName = userName,
                        onUserNameChange = { userName = it },
                        userSkillLevel = userSkillLevel,
                        onSkillLevelChange = { userSkillLevel = it },
                        onStartGame = {
                            currentScreen = ChessAppScreen.GAME_SCREEN
                            gameStatus = "Connecting to server..."
                            connectionHandler.connect()
                        }
                    )
                }

                ChessAppScreen.GAME_SCREEN -> {
                    GameScreen(
                        chessGame = chessGame,
                        selectedSquare = selectedSquare,
                        onSquareSelected = { row, col ->
                            handleSquareSelection(
                                row, col, chessGame, selectedSquare,
                                { selectedSquare = it },
                                { validMoves = it },
                                connectionHandler
                            )
                        },
                        validMoves = validMoves,
                        gameStatus = gameStatus,
                        isAiThinking = isAiThinking,
                        chatMessages = chatMessages,
                        chatInput = chatInput,
                        onChatInputChange = { chatInput = it },
                        onSendMessage = {
                            if (chatInput.isNotBlank()) {
                                connectionHandler.sendChatMessage(chatInput)
                                chatMessages.add(ChatMessage(userName, chatInput, false))
                                chatInput = ""
                            }
                        },
                        userName = userName,
                        lazyListState = lazyListState,
                        connectionHandler = connectionHandler
                    )
                }
            }
        }
    }
}

// Keep all other UI composables the same as before...
@Composable
fun GameScreen(
    chessGame: ChessGame,
    selectedSquare: Pair<Int, Int>,
    onSquareSelected: (Int, Int) -> Unit,
    validMoves: Set<Pair<Int, Int>>,
    gameStatus: String,
    isAiThinking: Boolean,
    chatMessages: List<ChatMessage>,
    chatInput: String,
    onChatInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    userName: String,
    lazyListState: LazyListState,
    connectionHandler: GameConnectionHandler
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Connection Status Bar
        ConnectionStatusBar(connectionHandler.connectionState, gameStatus)

        Spacer(modifier = Modifier.height(8.dp))

        // Responsive layout
        if (LocalContext.current.resources.configuration.screenWidthDp > 600) {
            // Tablet layout
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChessBoardSection(
                    chessGame = chessGame,
                    selectedSquare = selectedSquare,
                    onSquareSelected = onSquareSelected,
                    validMoves = validMoves,
                    userName = userName,
                    isAiThinking = isAiThinking,
                    connectionHandler = connectionHandler,
                    modifier = Modifier.weight(1f)
                )

                ChatSection(
                    chatMessages = chatMessages,
                    chatInput = chatInput,
                    onChatInputChange = onChatInputChange,
                    onSendMessage = onSendMessage,
                    lazyListState = lazyListState,
                    modifier = Modifier.width(280.dp)
                )
            }
        } else {
            // Mobile layout
            ChessBoardSection(
                chessGame = chessGame,
                selectedSquare = selectedSquare,
                onSquareSelected = onSquareSelected,
                validMoves = validMoves,
                userName = userName,
                isAiThinking = isAiThinking,
                connectionHandler = connectionHandler,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            ChatSection(
                chatMessages = chatMessages,
                chatInput = chatInput,
                onChatInputChange = onChatInputChange,
                onSendMessage = onSendMessage,
                lazyListState = lazyListState,
                modifier = Modifier.height(200.dp)
            )
        }
    }
}

// All other composables remain the same...
@Composable
fun StartScreen(onStartGame: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ChessColors.cardBackground)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "♜♞♝♛♚♝♞♜",
                    fontSize = 32.sp,
                    modifier = Modifier.padding(16.dp)
                )

                Text(
                    text = "Smart Chess",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = ChessColors.goldAccent,
                        fontSize = 36.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Play against AI Super Agent",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onStartGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChessColors.goldAccent)
                ) {
                    Text(
                        text = "START PLAYING",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileSetupScreen(
    userName: String,
    onUserNameChange: (String) -> Unit,
    userSkillLevel: String,
    onSkillLevelChange: (String) -> Unit,
    onStartGame: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ChessColors.cardBackground)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Setup Your Profile",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = userName,
                    onValueChange = onUserNameChange,
                    label = { Text("Your Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ChessColors.goldAccent,
                        focusedLabelColor = ChessColors.goldAccent
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Skill Level",
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Beginner", "Intermediate", "Advanced").forEach { level ->
                        val isSelected = userSkillLevel == level
                        Button(
                            onClick = { onSkillLevelChange(level) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) ChessColors.goldAccent
                                else Color.Gray.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = level,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onStartGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChessColors.goldAccent)
                ) {
                    Text(
                        text = "START GAME",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                }
            }
        }
    }
}

// FIXED: Connection Status Bar with better status messages
@Composable
fun ConnectionStatusBar(connectionState: GameConnectionHandler.ConnectionState, gameStatus: String) {
    AnimatedVisibility(
        visible = connectionState != GameConnectionHandler.ConnectionState.CONNECTED,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (connectionState) {
                    GameConnectionHandler.ConnectionState.CONNECTING -> Color(0xFFFF9800)
                    GameConnectionHandler.ConnectionState.RECONNECTING -> Color(0xFF2196F3)
                    GameConnectionHandler.ConnectionState.ERROR -> Color(0xFFF44336)
                    else -> Color(0xFF4CAF50)
                }
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (connectionState) {
                        GameConnectionHandler.ConnectionState.CONNECTING,
                        GameConnectionHandler.ConnectionState.RECONNECTING -> Icons.Default.Refresh
                        GameConnectionHandler.ConnectionState.ERROR -> Icons.Default.Warning
                        else -> Icons.Default.CheckCircle
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = if (connectionState == GameConnectionHandler.ConnectionState.CONNECTING ||
                        connectionState == GameConnectionHandler.ConnectionState.RECONNECTING) {
                        Modifier.then(
                            Modifier.animateRotation()
                        )
                    } else Modifier
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = when (connectionState) {
                        GameConnectionHandler.ConnectionState.CONNECTING -> "Connecting to AI..."
                        GameConnectionHandler.ConnectionState.RECONNECTING -> "Reconnecting..."
                        GameConnectionHandler.ConnectionState.ERROR -> "Connection failed - Check server"
                        GameConnectionHandler.ConnectionState.DISCONNECTED -> "Disconnected"
                        else -> gameStatus
                    },
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (connectionState == GameConnectionHandler.ConnectionState.CONNECTING ||
                    connectionState == GameConnectionHandler.ConnectionState.RECONNECTING) {
                    Spacer(modifier = Modifier.weight(1f))
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

// Helper extension for rotation animation
@Composable
fun Modifier.animateRotation(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )
    return this.then(Modifier.rotate(angle))
}

@Composable
fun ChessBoardSection(
    chessGame: ChessGame,
    selectedSquare: Pair<Int, Int>,
    onSquareSelected: (Int, Int) -> Unit,
    validMoves: Set<Pair<Int, Int>>,
    userName: String,
    isAiThinking: Boolean,
    connectionHandler: GameConnectionHandler,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // AI Player Info
        PlayerInfoCard(
            playerName = "AI Super Agent",
            isAI = true,
            isCurrentTurn = chessGame.getCurrentTurn() == ChessGame.PieceColor.BLACK,
            isThinking = isAiThinking,
            capturedPieces = 0
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Chess Board
        ChessBoardComponent(
            chessGame = chessGame,
            selectedSquare = selectedSquare,
            validMoves = validMoves,
            onSquareClick = onSquareSelected,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // User Player Info
        PlayerInfoCard(
            playerName = userName,
            isAI = false,
            isCurrentTurn = chessGame.getCurrentTurn() == ChessGame.PieceColor.WHITE,
            isThinking = false,
            capturedPieces = 0
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Game Controls
        GameControlsSection(
            onRestartGame = { connectionHandler.restartGame() },
            onUndoMove = { connectionHandler.undoMove() },
            onRequestHint = { connectionHandler.requestHint() },
            connectionHandler = connectionHandler
        )
    }
}

@Composable
fun PlayerInfoCard(
    playerName: String,
    isAI: Boolean,
    isCurrentTurn: Boolean,
    isThinking: Boolean,
    capturedPieces: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ChessColors.cardBackground),
        shape = RoundedCornerShape(12.dp),
        border = if (isCurrentTurn) BorderStroke(2.dp, ChessColors.goldAccent) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isAI) ChessColors.aiColor else ChessColors.userColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isAI) "🤖" else (playerName.firstOrNull()?.toString() ?: "🧑"),
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playerName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = when {
                        isThinking -> "🤔 Thinking..."
                        isCurrentTurn -> "⚡ Your turn!"
                        else -> "⏳ Waiting..."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.7f)
                    )
                )
            }

            // Turn indicator with pulsing animation
            if (isCurrentTurn) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_alpha"
                )

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(ChessColors.goldAccent.copy(alpha = alpha))
                )
            }
        }
    }
}

@Composable
fun ChessBoardComponent(
    chessGame: ChessGame,
    selectedSquare: Pair<Int, Int>,
    validMoves: Set<Pair<Int, Int>>,
    onSquareClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            for (row in 0 until 8) {
                Row(modifier = Modifier.weight(1f)) {
                    for (col in 0 until 8) {
                        ChessSquare(
                            row = row,
                            col = col,
                            piece = chessGame.getPieceAt(row, col),
                            isSelected = selectedSquare.first == row && selectedSquare.second == col,
                            isValidMove = validMoves.contains(Pair(row, col)),
                            isLastMove = (row == chessGame.lastMoveFrom.first && col == chessGame.lastMoveFrom.second) ||
                                    (row == chessGame.lastMoveTo.first && col == chessGame.lastMoveTo.second),
                            onClick = { onSquareClick(row, col) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChessSquare(
    row: Int,
    col: Int,
    piece: ChessGame.ChessPiece,
    isSelected: Boolean,
    isValidMove: Boolean,
    isLastMove: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLightSquare = (row + col) % 2 == 0
    val backgroundColor = when {
        isSelected -> ChessColors.selectedSquare
        isValidMove -> ChessColors.validMove
        isLastMove -> ChessColors.lastMoveHighlight
        isLightSquare -> ChessColors.lightSquare
        else -> ChessColors.darkSquare
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(backgroundColor)
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, ChessColors.goldAccent)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (piece.type != ChessGame.PieceType.NONE) {
            val pieceIcon = when (piece.type) {
                ChessGame.PieceType.KING -> if (piece.color == ChessGame.PieceColor.WHITE) "♔" else "♚"
                ChessGame.PieceType.QUEEN -> if (piece.color == ChessGame.PieceColor.WHITE) "♕" else "♛"
                ChessGame.PieceType.ROOK -> if (piece.color == ChessGame.PieceColor.WHITE) "♖" else "♜"
                ChessGame.PieceType.BISHOP -> if (piece.color == ChessGame.PieceColor.WHITE) "♗" else "♝"
                ChessGame.PieceType.KNIGHT -> if (piece.color == ChessGame.PieceColor.WHITE) "♘" else "♞"
                ChessGame.PieceType.PAWN -> if (piece.color == ChessGame.PieceColor.WHITE) "♙" else "♟"
                else -> ""
            }

            val scale = if (isSelected) 1.2f else 1.0f
            val animatedScale by animateFloatAsState(
                targetValue = scale,
                animationSpec = tween(200),
                label = "piece_scale"
            )

            Text(
                text = pieceIcon,
                fontSize = 20.sp,
                modifier = Modifier.scale(animatedScale)
            )
        }

        // Valid move indicator
        if (isValidMove && piece.type == ChessGame.PieceType.NONE) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(ChessColors.validMove)
            )
        } else if (isValidMove && piece.type != ChessGame.PieceType.NONE) {
            // Capture indicator
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(3.dp, ChessColors.validMove, CircleShape)
            )
        }
    }
}

@Composable
fun GameControlsSection(
    onRestartGame: () -> Unit,
    onUndoMove: () -> Unit,
    onRequestHint: () -> Unit,
    connectionHandler: GameConnectionHandler
) {
    val isConnected = connectionHandler.connectionState == GameConnectionHandler.ConnectionState.CONNECTED

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Button(
            onClick = onRestartGame,
            enabled = isConnected,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = ChessColors.aiColor,
                disabledContainerColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text("New", fontSize = 12.sp)
        }

        Button(
            onClick = onUndoMove,
            enabled = isConnected,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = ChessColors.userColor,
                disabledContainerColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text("Undo", fontSize = 12.sp)
        }

        Button(
            onClick = onRequestHint,
            enabled = isConnected,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = ChessColors.goldAccent,
                disabledContainerColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text("Hint", fontSize = 12.sp, color = Color.Black)
        }
    }
}

@Composable
fun ChatSection(
    chatMessages: List<ChatMessage>,
    chatInput: String,
    onChatInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ChessColors.cardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💬 Chat with AI",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                if (chatMessages.isNotEmpty()) {
                    Text(
                        text = "${chatMessages.size}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ChessColors.goldAccent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (chatMessages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🤖 Start chatting with your AI opponent!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.6f)
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                items(chatMessages) { message ->
                    ChatMessageItem(message = message)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = onChatInputChange,
                    placeholder = {
                        Text(
                            "Type a message...",
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ChessColors.goldAccent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )

                IconButton(
                    onClick = onSendMessage,
                    enabled = chatInput.isNotBlank(),
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (chatInput.isNotBlank()) ChessColors.goldAccent else Color.Gray,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (chatInput.isNotBlank()) Color.Black else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val alignment = if (message.isAI) Alignment.CenterStart else Alignment.CenterEnd
    val backgroundColor = if (message.isAI) Color(0xFF424242) else ChessColors.userColor

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Card(
            modifier = Modifier.widthIn(max = 240.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            shape = RoundedCornerShape(
                topStart = if (message.isAI) 4.dp else 16.dp,
                topEnd = if (message.isAI) 16.dp else 4.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                if (message.sender.isNotEmpty()) {
                    Text(
                        text = if (message.isAI) "🤖 ${message.sender}" else "👤 ${message.sender}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White
                    )
                )
            }
        }
    }
}

// Helper functions for square selection and move validation
fun handleSquareSelection(
    row: Int,
    col: Int,
    chessGame: ChessGame,
    selectedSquare: Pair<Int, Int>,
    onSquareSelected: (Pair<Int, Int>) -> Unit,
    onValidMovesChanged: (Set<Pair<Int, Int>>) -> Unit,
    connectionHandler: GameConnectionHandler
) {
    try {
        // Only allow moves when it's player's turn and connected
        if (chessGame.getCurrentTurn() != ChessGame.PieceColor.WHITE) return
        if (connectionHandler.connectionState != GameConnectionHandler.ConnectionState.CONNECTED) return

        val piece = chessGame.getPieceAt(row, col)
        val (selectedRow, selectedCol) = selectedSquare

        when {
            selectedRow == -1 && selectedCol == -1 -> {
                // No square selected, select if it's user's piece
                if (piece.type != ChessGame.PieceType.NONE &&
                    piece.color == ChessGame.PieceColor.WHITE) {
                    onSquareSelected(Pair(row, col))
                    onValidMovesChanged(getValidMoves(chessGame, row, col))
                }
            }

            selectedRow == row && selectedCol == col -> {
                // Same square clicked, deselect
                onSquareSelected(Pair(-1, -1))
                onValidMovesChanged(emptySet())
            }

            else -> {
                // Different square clicked
                if (piece.type != ChessGame.PieceType.NONE &&
                    piece.color == ChessGame.PieceColor.WHITE) {
                    // Select new piece
                    onSquareSelected(Pair(row, col))
                    onValidMovesChanged(getValidMoves(chessGame, row, col))
                } else {
                    // Try to make a move - send to server
                    connectionHandler.sendMove(selectedRow, selectedCol, row, col)
                    onSquareSelected(Pair(-1, -1))
                    onValidMovesChanged(emptySet())
                }
            }
        }
    } catch (e: Exception) {
        Log.e("ChessGame", "Error in square selection: ${e.message}")
    }
}

fun getValidMoves(chessGame: ChessGame, row: Int, col: Int): Set<Pair<Int, Int>> {
    val validMoves = mutableSetOf<Pair<Int, Int>>()

    try {
        val piece = chessGame.getPieceAt(row, col)
        if (piece.type == ChessGame.PieceType.NONE) return validMoves

        // Simplified move validation for UI feedback
        for (toRow in 0 until 8) {
            for (toCol in 0 until 8) {
                if (isValidMove(chessGame, row, col, toRow, toCol)) {
                    validMoves.add(Pair(toRow, toCol))
                }
            }
        }
    } catch (e: Exception) {
        Log.e("ChessGame", "Error getting valid moves: ${e.message}")
    }

    return validMoves
}

fun isValidMove(chessGame: ChessGame, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
    try {
        val piece = chessGame.getPieceAt(fromRow, fromCol)
        val targetPiece = chessGame.getPieceAt(toRow, toCol)

        // Basic validation
        if (piece.type == ChessGame.PieceType.NONE) return false
        if (targetPiece.type != ChessGame.PieceType.NONE && targetPiece.color == piece.color) return false
        if (fromRow == toRow && fromCol == toCol) return false

        val rowDiff = toRow - fromRow
        val colDiff = toCol - fromCol
        val rowDistance = kotlin.math.abs(rowDiff)
        val colDistance = kotlin.math.abs(colDiff)

        return when (piece.type) {
            ChessGame.PieceType.PAWN -> {
                val direction = if (piece.color == ChessGame.PieceColor.WHITE) -1 else 1
                val startRow = if (piece.color == ChessGame.PieceColor.WHITE) 6 else 1

                when {
                    colDiff == 0 && targetPiece.type == ChessGame.PieceType.NONE -> {
                        rowDiff == direction || (fromRow == startRow && rowDiff == 2 * direction)
                    }
                    kotlin.math.abs(colDiff) == 1 && rowDiff == direction -> {
                        targetPiece.type != ChessGame.PieceType.NONE
                    }
                    else -> false
                }
            }

            ChessGame.PieceType.ROOK -> {
                (rowDiff == 0 || colDiff == 0) && isPathClear(chessGame, fromRow, fromCol, toRow, toCol)
            }

            ChessGame.PieceType.BISHOP -> {
                rowDistance == colDistance && isPathClear(chessGame, fromRow, fromCol, toRow, toCol)
            }

            ChessGame.PieceType.QUEEN -> {
                (rowDiff == 0 || colDiff == 0 || rowDistance == colDistance) &&
                        isPathClear(chessGame, fromRow, fromCol, toRow, toCol)
            }

            ChessGame.PieceType.KNIGHT -> {
                (rowDistance == 2 && colDistance == 1) || (rowDistance == 1 && colDistance == 2)
            }

            ChessGame.PieceType.KING -> {
                rowDistance <= 1 && colDistance <= 1
            }

            ChessGame.PieceType.NONE -> false
        }
    } catch (e: Exception) {
        Log.e("ChessGame", "Error validating move: ${e.message}")
        return false
    }
}

fun isPathClear(chessGame: ChessGame, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
    try {
        val rowStep = when {
            toRow > fromRow -> 1
            toRow < fromRow -> -1
            else -> 0
        }

        val colStep = when {
            toCol > fromCol -> 1
            toCol < fromCol -> -1
            else -> 0
        }

        var checkRow = fromRow + rowStep
        var checkCol = fromCol + colStep

        while (checkRow != toRow || checkCol != toCol) {
            if (chessGame.getPieceAt(checkRow, checkCol).type != ChessGame.PieceType.NONE) {
                return false
            }
            checkRow += rowStep
            checkCol += colStep
        }

        return true
    } catch (e: Exception) {
        Log.e("ChessGame", "Error checking path: ${e.message}")
        return false
    }
}