import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.neuroed.R
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    navController: NavController
) {
    var isConnecting by remember { mutableStateOf(true) }
    var dots by remember { mutableStateOf("") }
    var currentAvatarIndex by remember { mutableStateOf(0) }

    val avatarColors = listOf(
        Color(0xFFB8D8F8), // Light blue for business avatar
        Color(0xFF64B5F6), // Medium blue for user avatars
        Color(0xFF64B5F6)  // Medium blue for user avatars
    )

    val avatarNames = listOf("Business", "User 1", "User 2")

    val avatarAlphas = remember {
        List(3) { index ->
            Animatable(if (index == 0) 1f else 0.5f)
        }
    }

    // Animate dots for loading text
    LaunchedEffect(key1 = true) {
        while (isConnecting) {
            dots = when (dots) {
                "" -> "."
                "." -> ".."
                ".." -> "....."
                else -> ""
            }
            delay(500)
        }
    }

    // Animate avatars for connection visualization
    LaunchedEffect(key1 = true) {
        while (isConnecting) {
            avatarAlphas.forEachIndexed { index, animatable ->
                if (index == currentAvatarIndex) {
                    animatable.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(500)
                    )
                } else {
                    animatable.animateTo(
                        targetValue = 0.5f,
                        animationSpec = tween(500)
                    )
                }
            }

            delay(700)
            currentAvatarIndex = (currentAvatarIndex + 1) % 3

            // End the connection animation after some time
            if (currentAvatarIndex == 0) {
                delay(1500)
                isConnecting = false
                // Navigate using NavController
                navController.navigate("main_screen") {
                    // Optional: Configure navigation options
                    popUpTo("loading_screen") { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // User avatars with names
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                avatarColors.forEachIndexed { index, color ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar Circle
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .alpha(avatarAlphas[index].value)
                                .clip(CircleShape)
                                .background(color),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (index == 0) R.drawable.man else R.drawable.man
                                ),
                                contentDescription = avatarNames[index],
                                tint = Color(0xFF0D47A1),
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Avatar Name
                        Text(
                            text = avatarNames[index],
                            color = Color.White.copy(alpha = avatarAlphas[index].value),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(64.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Loading text with animated dots
            Text(
                text = "Starting$dots",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Optional: Add a linear progress indicator
            LinearProgressIndicator(
                modifier = Modifier
                    .width(200.dp)
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

