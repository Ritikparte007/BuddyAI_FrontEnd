package com.example.neuroed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.neuroed.ui.theme.AppColors


// Color scheme definition


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(navController: NavController) {
    val isDarkMode = isSystemInDarkTheme()

    val backgroundColor = if (isDarkMode) {
        AppColors.Dark.background
    } else {
        AppColors.Light.background
    }

    val surfaceColor = if (isDarkMode) {
        AppColors.Dark.surface
    } else {
        AppColors.Light.surface
    }

    val primaryColor = if (isDarkMode) {
        AppColors.Dark.primary
    } else {
        AppColors.Light.primary
    }

    val accentColor = if (isDarkMode) {
        AppColors.Dark.primaryLight    // या जो भी “accent” के लिए आपने मैप किया है
    } else {
        AppColors.Light.primaryLight
    }

    val textColor = if (isDarkMode) {
        AppColors.Dark.textDark
    } else {
        AppColors.Light.textDark
    }

    val secondaryTextColor = if (isDarkMode) {
        AppColors.Dark.textLight
    } else {
        AppColors.Light.textLight
    }


    val gameMatches = listOf(
        GameMatch("Chess", "BuddyAI", 150, "Areax", 120, 15),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Game Matches",
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(gameMatches) { match ->
                GameMatchCard(
                    match = match,
                    surfaceColor = surfaceColor,
                    primaryColor = primaryColor,
                    accentColor = accentColor,
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor,
                    onCardClick = {
                        // Navigate to proper game screen based on game type
                        when(match.gameName) {

                            "Chess" -> navController.navigate("ChessGameScreen")
                            "Ludo" -> navController.navigate("LudoGameScreen")
                            "EmojiFaceOffScreen" -> navController.navigate("EmojiFaceOffScreen")
                            else -> { /* Handle other game types */ }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun GameMatchCard(
    match: GameMatch,
    surfaceColor: Color,
    primaryColor: Color,
    accentColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row with game name and trophies
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Points indicator with star icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⭐ 500",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }

                // Game name
                Text(
                    text = match.gameName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                // Game specific icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when(match.gameName) {
                            "Chess" -> "♟️"
                            "Ludo" -> "🎲"
                            "Emoji Face-Off" -> "😀"
                            else -> "🏆"
                        },
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Player vs Player section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player 1
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = match.player1Name,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (match.gameName.contains("Emoji")) {
                            Text(
                                text = "Score: ${match.player1Score} 😊",
                                fontWeight = FontWeight.Medium,
                                color = secondaryTextColor
                            )
                        } else {
                            Text(
                                text = "Points: ${match.player1Score} 😊",
                                fontWeight = FontWeight.Medium,
                                color = secondaryTextColor
                            )
                        }
                    }
                }

                // VS indicator
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VS",
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Player 2
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = match.player2Name,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = 16.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (match.gameName.contains("Emoji")) {
                            Text(
                                text = "Score: ${match.player2Score} 😊",
                                fontWeight = FontWeight.Medium,
                                color = secondaryTextColor
                            )
                        } else {
                            Text(
                                text = "Points: ${match.player2Score} 😊",
                                fontWeight = FontWeight.Medium,
                                color = secondaryTextColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total matches with divider and better styling
            Divider(color = accentColor.copy(alpha = 0.5f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Total Matches: ",
                    fontSize = 14.sp,
                    color = secondaryTextColor
                )
                Text(
                    text = "${match.totalMatches}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }
        }
    }
}

data class GameMatch(
    val gameName: String,
    val player1Name: String,
    val player1Score: Int,
    val player2Name: String,
    val player2Score: Int,
    val totalMatches: Int
)

@Preview(showBackground = true)
@Composable
fun GamesScreenPreview() {
    GamesScreen(rememberNavController())
}