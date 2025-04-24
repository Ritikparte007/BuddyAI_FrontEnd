package com.example.neuroed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
//import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun ChessGameScreen(navController: NavController) {
    val darkBrown = Color(0xFF8B4513)
    val lightBrown = Color(0xFFDEB887)
    val darkBackground = Color(0xFF121212)
    val goldColor = Color(0xFFFFD700)

    // Game state
    val chessGame = remember { ChessGame() }
    var selectedRow by remember { mutableStateOf(-1) }
    var selectedCol by remember { mutableStateOf(-1) }
    var boardState by remember { mutableStateOf(0) } // Used to force recomposition

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top player info (BuddyAI)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left player with additional sound icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    ) {
                        Image(
                            painter = painterResource(id = android.R.drawable.ic_dialog_info),
                            contentDescription = "Player Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "BuddyAI",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Sound icon for BuddyAI
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.5f))
                            .clickable { /* Handle sound toggle */ }
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_lock_silent_mode_off),
                            contentDescription = "Sound",
                            tint = Color.White,
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.Center)
                        )
                    }
                }

                // Right score
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "150",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        painter = painterResource(id = android.R.drawable.btn_star_big_on),
                        contentDescription = "Coins",
                        tint = goldColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Current turn indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Turn: ${if (chessGame.getCurrentTurn() == ChessGame.PieceColor.WHITE) "White" else "Black"}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Chessboard
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(darkBrown)
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (row in 0 until 8) {
                        Row(modifier = Modifier.weight(1f)) {
                            for (col in 0 until 8) {
                                val isLightSquare = (row + col) % 2 == 0
                                val piece = chessGame.getPieceAt(row, col)
                                val isSelected = row == selectedRow && col == selectedCol

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(
                                            when {
                                                isSelected -> Color.Green.copy(alpha = 0.5f)
                                                isLightSquare -> lightBrown
                                                else -> darkBrown
                                            }
                                        )
                                        .clickable {
                                            if (selectedRow == -1 && selectedCol == -1) {
                                                // First click - select piece
                                                if (piece.type != ChessGame.PieceType.NONE) {
                                                    selectedRow = row
                                                    selectedCol = col
                                                }
                                            } else {
                                                // Second click - try to move
                                                val moved = chessGame.makeMove(selectedRow, selectedCol, row, col)
                                                selectedRow = -1
                                                selectedCol = -1
                                                if (moved) {
                                                    boardState++ // Force recomposition
                                                }
                                            }
                                        }
                                ) {
                                    // Display chess piece
                                    if (piece.type != ChessGame.PieceType.NONE) {
                                        val iconResId = when (piece.type) {
                                            ChessGame.PieceType.KING -> android.R.drawable.btn_star_big_on
                                            ChessGame.PieceType.QUEEN -> android.R.drawable.btn_star_big_off
                                            ChessGame.PieceType.ROOK -> android.R.drawable.ic_menu_directions
                                            ChessGame.PieceType.BISHOP -> android.R.drawable.ic_menu_send
                                            ChessGame.PieceType.KNIGHT -> android.R.drawable.ic_menu_rotate
                                            ChessGame.PieceType.PAWN -> android.R.drawable.ic_menu_zoom
                                            else -> android.R.drawable.ic_menu_help
                                        }

                                        val pieceColor = if (piece.color == ChessGame.PieceColor.WHITE)
                                            Color.White else Color.Black

                                        Icon(
                                            painter = painterResource(id = iconResId),
                                            contentDescription = "${piece.color} ${piece.type}",
                                            tint = pieceColor,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom player info (Ritik parte)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left score
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "150",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        painter = painterResource(id = android.R.drawable.btn_star_big_on),
                        contentDescription = "Coins",
                        tint = goldColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Right player with microphone icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Mic icon for Ritik parte
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.5f))
                            .clickable { /* Handle mic action */ }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Microphone",
                            tint = Color.White,
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Ritik parte",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    ) {
                        Image(
                            painter = painterResource(id = android.R.drawable.ic_dialog_info),
                            contentDescription = "Player Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Game controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        chessGame.resetGame()
                        boardState++
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Restart Game")
                }

                Button(
                    onClick = { /* Handle undo move */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                ) {
                    Text("Undo Move")
                }
            }
        }
    }
}

// Chess piece class and game logic
class ChessGame {
    // Piece types
    enum class PieceType { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN, NONE }

    // Piece colors
    enum class PieceColor { WHITE, BLACK, NONE }

    // Chess piece data class
    data class ChessPiece(val type: PieceType, val color: PieceColor)

    // Board representation: 8x8 grid with pieces
    private val board = Array(8) { Array(8) { ChessPiece(PieceType.NONE, PieceColor.NONE) } }

    // Current player's turn
    private var currentTurn = PieceColor.WHITE

    // Game history for undo
    private val moveHistory = mutableListOf<Move>()

    data class Move(
        val fromRow: Int,
        val fromCol: Int,
        val toRow: Int,
        val toCol: Int,
        val capturedPiece: ChessPiece
    )

    // Initialize the board with standard chess setup
    init {
        setupBoard()
    }

    private fun setupBoard() {
        // Setup pawns
        for (col in 0..7) {
            board[1][col] = ChessPiece(PieceType.PAWN, PieceColor.BLACK)
            board[6][col] = ChessPiece(PieceType.PAWN, PieceColor.WHITE)
        }

        // Setup other black pieces
        board[0][0] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)
        board[0][1] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK)
        board[0][2] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK)
        board[0][3] = ChessPiece(PieceType.QUEEN, PieceColor.BLACK)
        board[0][4] = ChessPiece(PieceType.KING, PieceColor.BLACK)
        board[0][5] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK)
        board[0][6] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK)
        board[0][7] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)

        // Setup other white pieces
        board[7][0] = ChessPiece(PieceType.ROOK, PieceColor.WHITE)
        board[7][1] = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE)
        board[7][2] = ChessPiece(PieceType.BISHOP, PieceColor.WHITE)
        board[7][3] = ChessPiece(PieceType.QUEEN, PieceColor.WHITE)
        board[7][4] = ChessPiece(PieceType.KING, PieceColor.WHITE)
        board[7][5] = ChessPiece(PieceType.BISHOP, PieceColor.WHITE)
        board[7][6] = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE)
        board[7][7] = ChessPiece(PieceType.ROOK, PieceColor.WHITE)
    }

    // Get piece at position
    fun getPieceAt(row: Int, col: Int): ChessPiece {
        return board[row][col]
    }

    // Check if move is valid - Basic implementation for demonstration
    fun isValidMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
        // Boundary checks
        if (fromRow !in 0..7 || fromCol !in 0..7 || toRow !in 0..7 || toCol !in 0..7)
            return false

        val fromPiece = board[fromRow][fromCol]
        val toPiece = board[toRow][toCol]

        // Can't move empty square
        if (fromPiece.type == PieceType.NONE) return false

        // Can't move opponent's piece
        if (fromPiece.color != currentTurn) return false

        // Can't capture own piece
        if (toPiece.color == currentTurn) return false

        // Basic movement validation based on piece type
        when (fromPiece.type) {
            PieceType.PAWN -> {
                val direction = if (fromPiece.color == PieceColor.WHITE) -1 else 1
                val startRow = if (fromPiece.color == PieceColor.WHITE) 6 else 1

                // Move forward one square
                if (fromCol == toCol && toRow == fromRow + direction && toPiece.type == PieceType.NONE) {
                    return true
                }

                // Move forward two squares from starting position
                if (fromCol == toCol && fromRow == startRow &&
                    toRow == fromRow + 2 * direction &&
                    toPiece.type == PieceType.NONE &&
                    board[fromRow + direction][fromCol].type == PieceType.NONE) {
                    return true
                }

                // Capture diagonally
                if (Math.abs(fromCol - toCol) == 1 && toRow == fromRow + direction &&
                    toPiece.type != PieceType.NONE && toPiece.color != fromPiece.color) {
                    return true
                }

                return false
            }

            PieceType.ROOK -> {
                // Rook moves horizontally or vertically
                if (fromRow != toRow && fromCol != toCol) return false

                // Check if path is clear
                if (fromRow == toRow) {
                    val startCol = Math.min(fromCol, toCol) + 1
                    val endCol = Math.max(fromCol, toCol)
                    for (col in startCol until endCol) {
                        if (board[fromRow][col].type != PieceType.NONE) return false
                    }
                } else {
                    val startRow = Math.min(fromRow, toRow) + 1
                    val endRow = Math.max(fromRow, toRow)
                    for (row in startRow until endRow) {
                        if (board[row][fromCol].type != PieceType.NONE) return false
                    }
                }

                return true
            }

            PieceType.KNIGHT -> {
                // Knight moves in L-shape
                val rowDiff = Math.abs(toRow - fromRow)
                val colDiff = Math.abs(toCol - fromCol)
                return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)
            }

            PieceType.BISHOP -> {
                // Bishop moves diagonally
                val rowDiff = Math.abs(toRow - fromRow)
                val colDiff = Math.abs(toCol - fromCol)
                if (rowDiff != colDiff) return false

                // Check if path is clear
                val rowDir = if (toRow > fromRow) 1 else -1
                val colDir = if (toCol > fromCol) 1 else -1

                var checkRow = fromRow + rowDir
                var checkCol = fromCol + colDir

                while (checkRow != toRow && checkCol != toCol) {
                    if (board[checkRow][checkCol].type != PieceType.NONE) return false
                    checkRow += rowDir
                    checkCol += colDir
                }

                return true
            }

            PieceType.QUEEN -> {
                // Queen moves like rook or bishop
                val rowDiff = Math.abs(toRow - fromRow)
                val colDiff = Math.abs(toCol - fromCol)

                // Diagonal move (like bishop)
                if (rowDiff == colDiff) {
                    val rowDir = if (toRow > fromRow) 1 else -1
                    val colDir = if (toCol > fromCol) 1 else -1

                    var checkRow = fromRow + rowDir
                    var checkCol = fromCol + colDir

                    while (checkRow != toRow && checkCol != toCol) {
                        if (board[checkRow][checkCol].type != PieceType.NONE) return false
                        checkRow += rowDir
                        checkCol += colDir
                    }

                    return true
                }

                // Straight move (like rook)
                if (fromRow == toRow || fromCol == toCol) {
                    if (fromRow == toRow) {
                        val startCol = Math.min(fromCol, toCol) + 1
                        val endCol = Math.max(fromCol, toCol)
                        for (col in startCol until endCol) {
                            if (board[fromRow][col].type != PieceType.NONE) return false
                        }
                    } else {
                        val startRow = Math.min(fromRow, toRow) + 1
                        val endRow = Math.max(fromRow, toRow)
                        for (row in startRow until endRow) {
                            if (board[row][fromCol].type != PieceType.NONE) return false
                        }
                    }

                    return true
                }

                return false
            }

            PieceType.KING -> {
                // King moves one square in any direction
                val rowDiff = Math.abs(toRow - fromRow)
                val colDiff = Math.abs(toCol - fromCol)
                return rowDiff <= 1 && colDiff <= 1 && (rowDiff != 0 || colDiff != 0)
            }

            else -> return false
        }
    }

    // Make a move
    fun makeMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
        if (!isValidMove(fromRow, fromCol, toRow, toCol)) return false

        // Save move for undo
        val capturedPiece = board[toRow][toCol]
        moveHistory.add(Move(fromRow, fromCol, toRow, toCol, capturedPiece))

        // Make the move
        board[toRow][toCol] = board[fromRow][fromCol]
        board[fromRow][fromCol] = ChessPiece(PieceType.NONE, PieceColor.NONE)

        // Switch turns
        currentTurn = if (currentTurn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE

        return true
    }

    // Undo last move
    fun undoMove(): Boolean {
        if (moveHistory.isEmpty()) return false

        val lastMove = moveHistory.removeAt(moveHistory.size - 1)

        // Restore pieces
        board[lastMove.fromRow][lastMove.fromCol] = board[lastMove.toRow][lastMove.toCol]
        board[lastMove.toRow][lastMove.toCol] = lastMove.capturedPiece

        // Switch turn back
        currentTurn = if (currentTurn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE

        return true
    }

    // Reset game
    fun resetGame() {
        for (row in 0..7) {
            for (col in 0..7) {
                board[row][col] = ChessPiece(PieceType.NONE, PieceColor.NONE)
            }
        }
        setupBoard()
        currentTurn = PieceColor.WHITE
        moveHistory.clear()
    }

    // Get current turn
    fun getCurrentTurn(): PieceColor {
        return currentTurn
    }
}

@Preview(showBackground = true)
@Composable
fun ChessScreenPreview() {
    MaterialTheme {
        ChessGameScreen(rememberNavController())
    }
}