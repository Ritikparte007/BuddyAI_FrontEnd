package com.example.neuroed

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import kotlinx.coroutines.delay
import androidx.compose.material3.Text
import androidx.compose.runtime.*

import androidx.compose.ui.graphics.graphicsLayer


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NeuroEdApp()
        }
    }
}

@Composable
fun NeuroEdApp() {
    // Enable dark mode theme
    MaterialTheme(
        colorScheme = darkColorScheme(),  // Dark mode color scheme
    ) {
        val navController = rememberNavController()

        LaunchedEffect(Unit) {
            delay(1000)  // 2-second delay before navigating
            navController.navigate("home")
        }

        NavHost(navController = navController, startDestination = "splash") {
            composable("splash") { SplashScreen() }
            composable("home") { HomeScreen(navController) }
            composable("SignUpScreen") { SignUpScreen(navController) }
            composable("ChatScreen") { ChatScreen(navController) }
            composable("AutoOpenCameraScreen") { CameraScreen(
                navController,
                onEnableCameraClick={}) }
            composable("CreateSubjectScreen") { CreateSubjectScreen(navController) }
            composable("AICharacterListScreen") { AICharacterListScreen(navController) }
            composable("NotificationScreen") { NotificationScreen(navController, onCustomizeClick = {
            }, onHistoricalClick = {}) }
            composable("SubscriptionScreen"){SubscriptionScreen(navController, onContinueClick={},onRestorePurchaseClick={})}
            composable("ReelsScreen") {ReelsScreen(navController)  }
            composable("ProfileScreen"){ProfileScreen(navController)}
        }
    }
}



@Composable
fun SplashScreen() {
    // Log device information
    val brand = Build.BRAND
    Log.d("DeviceInfo", "Brand: $brand")

    // Create a mutable state for the scale animation; start at 0.8f
    val scaleState = remember { mutableStateOf(0.8f) }
    // When the composable is launched, animate the scale to 1f
    LaunchedEffect(Unit) {
        scaleState.value = 1f
    }

    // Animate scale from the current value to the target value over 1.5 seconds
    val scaleAnim by animateFloatAsState(
        targetValue = scaleState.value,
        animationSpec = tween(durationMillis = 1500)
    )
    // Animate alpha from 0 to 1 for a fade-in effect
    val alphaAnim by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1500)
    )

    // Define a vertical gradient background for a modern look
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E1E1E), // Dark gray top
            Color(0xFF343434)  // Slightly lighter gray bottom
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "NeuroEd",
            modifier = Modifier
                .alpha(alphaAnim)
                .graphicsLayer {
                    scaleX = scaleAnim
                    scaleY = scaleAnim
                }
                .padding(horizontal = 24.dp),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                color = Color.White
            )
        )
    }
}

