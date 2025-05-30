package com.example.neuroed

import android.content.Context
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.Locale

/**
 * Modern SignUpScreen composable with enhanced visual design
 */
@Composable
fun SignUpScreen(navController: NavController?, onGoogleSignUpClick: () -> Unit) {


    val context = LocalContext.current
    val isUserSignedUp = remember {
        context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            .getBoolean("isUserSignedUp", false)
    }

    LaunchedEffect(isUserSignedUp) {
        if (isUserSignedUp) {
            navController?.navigate("home") {
                popUpTo("SignUpScreen") { inclusive = true }
            }
        }
    }

    // Get screen dimensions for responsive layout
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    // State for fade-in animation of the entire screen
    val alphaAnim = remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        alphaAnim.value = 1f
    }

    // Scale animation for the logo
    val logoScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800)
    )

    // Animated background gradient with modern colors
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0A0A1E), // Dark blue
            Color(0xFF15153A)  // Slightly lighter blue
        )
    )

    // Add decorative circles for modern UI elements
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        // Decorative blurred circle at the top
        Box(
            modifier = Modifier
                .size(screenWidth * 0.7f)
                .offset(x = screenWidth * 0.3f, y = -screenWidth * 0.4f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF6a11cb).copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
                .blur(radius = 60.dp)
        )

        // Decorative blurred circle at the bottom
        Box(
            modifier = Modifier
                .size(screenWidth * 0.9f)
                .offset(x = -screenWidth * 0.5f, y = screenHeight * 0.6f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2575fc).copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
                .blur(radius = 70.dp)
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .alpha(alphaAnim.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(screenHeight * 0.1f))

            // App logo with animation
            ModernLogoImage(logoScale = logoScale)

            Spacer(modifier = Modifier.height(24.dp))

            // Welcome text with modern styling
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to Buddy.Ai",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Your intelligent companion for a smarter future",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
            }

            // Add expanding spacer to push the Google button toward the bottom
            Spacer(modifier = Modifier.weight(1f))

            // Modern Google sign-up button
            ModernGoogleSignUpButton(onGoogleSignUpClick = onGoogleSignUpClick)

            Spacer(modifier = Modifier.height(16.dp))

            // Login text with improved styling

            Spacer(modifier = Modifier.height(24.dp))

            // Footer with terms and privacy policy
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                ModernTermsAndPrivacyText()
            }
        }
    }
}

/**
 * Enhanced logo display with modern styling
 */
@Composable
fun ModernLogoImage(logoScale: Float) {
    val primaryGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF6a11cb), // Purple
            Color(0xFF2575fc)  // Blue
        )
    )

    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(logoScale)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF15153A),
                        Color(0xFF0A0A1E)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = primaryGradient,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "B",
            style = TextStyle(
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                brush = primaryGradient
            )
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Buddy.Ai",
        modifier = Modifier.scale(logoScale),
        style = TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            brush = primaryGradient,
            shadow = Shadow(
                color = Color.Black,
                offset = Offset(1f, 1f),
                blurRadius = 2f
            )
        )
    )
}

/**
 * Modern Google sign-up button with enhanced visual appeal
 */
@Composable
fun ModernGoogleSignUpButton(onGoogleSignUpClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 150)
    )

    // Elevated card for the button to give it depth
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                pressed = true
                onGoogleSignUpClick()
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Google icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "Google Icon",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Divider line
            Spacer(modifier = Modifier.width(16.dp))

            // Button text
            Text(
                text = "Continue with Google",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF303030),
                modifier = Modifier.weight(1f)
            )

            // Arrow icon
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Continue",
                tint = Color(0xFF303030),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Modern login text with enhanced styling
 */


/**
 * Combined terms and privacy text for cleaner footer
 */
@Composable
fun ModernTermsAndPrivacyText() {
    Text(
        text = "By continuing, you agree to our Terms of Service and Privacy Policy",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF8E8E8E),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}