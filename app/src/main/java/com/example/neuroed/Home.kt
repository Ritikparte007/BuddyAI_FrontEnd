package com.example.neuroed

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraProvider
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.*

/** ---------------------------
 *       DATA CLASSES
 * --------------------------- */
data class AIProfile(
    val image: Int,
    val name: String,
    val description: String,
    val rating: Double
)

val aiProfiles = listOf(
    AIProfile(R.drawable.biology, "DreamScapeAI", "Deep learning for stunning visuals", 4.6),
    AIProfile(R.drawable.biology, "NeuroVision",  "Next-gen AI-powered creativity",     4.7),
    AIProfile(R.drawable.biology, "MindGenix",    "Enhancing ideas with AI",            4.5)
)

/** ---------------------------
 *       MAIN SCREEN
 * --------------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    // A Scaffold that holds the top bar, bottom bar, and floating button


    Scaffold(
        topBar = {
            TopAppBar(
                // Slight elevation for depth
                modifier = Modifier.shadow(4.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    Text(
                        "BuddyAI",
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.W500
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("SubscriptionScreen") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.premium),
                            contentDescription = "Premium Icon",
                            modifier = Modifier
                                .size(25.dp),
                            tint = Color.Unspecified
                        )
                    }

                        IconButton(onClick = { navController.navigate("NotificationScreen") }) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = Color.White,
                                        contentColor = Color.Black
                                    ) {
                                        Text("0") // Replace with your dynamic count value
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.notificationbell),
                                    contentDescription = "Notification Icon",
                                    modifier = Modifier
                                        .size(20.dp),
//                                        .background(color = Color.White),
                                    tint = Color.Unspecified
                                )
                            }
                        }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("AutoOpenCameraScreen") },
//                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.camera),
                    contentDescription = "Camera Icon",
                    modifier = Modifier.size(40.dp),
                    tint = Color.Unspecified
                )
            }
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        // Main content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // "Subject" Title
            item {
                SectionHeader(title = "SyllabusHub")
            }

            // Subject items (slider)
            item {
                SubjectList()
            }

            // "Character" Title
            item {
                SectionHeader(title = "VirtuBeings")
            }

            // AI Profile Cards
            item {
                AIProfileScreen(navController)
            }

            // User Profile (gradient card)
            item {
                UserProfile(navController)
            }

            // Grid Screen (Documents, Subject, etc.)
            item {
                GridScreen(navController)
            }
        }
    }
}

/** ---------------------------
 *       SECTION HEADER
 * --------------------------- */
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.W500,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.onBackground
    )
}

/** ---------------------------
 *       SUBJECT LIST
 * --------------------------- */
@Composable
fun SubjectList() {
    // Sample progress
    val progress = 1.0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()) // Enables horizontal scrolling
            .fillMaxHeight()
//            .background(color = Color.Red)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(6) { // Display 6 subject cards
            Card(
                modifier = Modifier
                    .width(130.dp)
                    .height(190.dp),
                shape = RoundedCornerShape(13.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // Subject Image as Background
                    Image(
                        painter = painterResource(id = R.drawable.mathssub),
                        contentDescription = "Subject Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Overlapping Circular Progress in Center
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(60.dp),
                            progress = progress,
                            color = Color.White,
                            strokeWidth = 6.dp
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    // Subject Name in Top Left Corner
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopStart)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.7f)) // Semi-transparent background
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Biology", // Dynamic Subject Name can be set here
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

}

/** ---------------------------
 *       USER PROFILE
 * --------------------------- */

@Composable
fun UserProfile(
    navController: NavController,
    backgroundColor: Color = Color.Black
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { navController.navigate("ProfileScreen") }
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Image with a circular shape and border.
            Image(
                painter = painterResource(id = R.drawable.biology),
                contentDescription = "User Profile",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "John Doe",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = 0.7f,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color.Blue,
                        trackColor = Color.LightGray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "10",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star",
                            tint = if (index < 4) Color.Yellow else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}


/** ---------------------------
 *       BOTTOM NAV BAR
 * --------------------------- */


@Composable
fun VoiceVisualizer(
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    barColor: Color = Color.White,
    minHeight: Dp = 4.dp,
    maxHeight: Dp = 16.dp,
    barWidth: Dp = 4.dp,
    spaceBetween: Dp = 2.dp
) {
    val infiniteTransition = rememberInfiniteTransition()
    // Create an animated float for each bar
    val animations = List(barCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 500,
                    easing = LinearEasing,
                    delayMillis = index * 100
                ),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(spaceBetween)
    ) {
        animations.forEach { anim ->
            // Interpolate between minHeight and maxHeight based on the animated fraction
            val height = minHeight + (maxHeight - minHeight) * anim.value
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(height)
                    .background(barColor, shape = RoundedCornerShape(2.dp))
            )
        }
    }
}



@Composable
fun BottomNavigationBar(navController: NavController) {
    var isListening by remember { mutableStateOf(false) }

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("CreateSubjectScreen") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.curriculum),
                    contentDescription = "Subject Icon",
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified
                )
            },
            label = { Text("Subject", color = MaterialTheme.colorScheme.onSurface) }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("ReelsScreen") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.play),
                    contentDescription = "Reels",
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified
                )
            },
            label = { Text("Reels", color = MaterialTheme.colorScheme.onSurface) }
        )

        // Microphone button with animation inside the circle.
        NavigationBarItem(
            selected = false,
            onClick = { isListening = !isListening },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
//                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { isListening = !isListening },
                    contentAlignment = Alignment.Center
                ) {
                    if (isListening) {
                        // Draw a circular animated indicator inside the mic button.
                        VoiceVisualizer(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                        )
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.voice), // Replace with your mic icon
                        contentDescription = "Mic Icon",
                        modifier = Modifier.size(32.dp
                        ),
                        tint = Color.Unspecified
                    )
                }
            },
            label = { Text("") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("library") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.graph),
                    contentDescription = "Library Icon",
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified
                )
            },
            label = { Text("Library", color = MaterialTheme.colorScheme.onSurface) }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("settings") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.bot),
                    contentDescription = "Character Icon",
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified
                )
            },
            label = { Text("Character", color = MaterialTheme.colorScheme.onSurface) }
        )
    }
}


/** ---------------------------
 *       GRID SCREEN
 * --------------------------- */
@Composable
fun GridScreen(navController: NavController) {
    val items = listOf(
        "Documents", "Subject", "Character", "Game",
        "Documents", "Documents", "Documents", "Documents"
    )

    Box(
        modifier = Modifier
            .height(300.dp)
            .padding(16.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4), // 4 columns
            userScrollEnabled = false
        ) {
            items(items) { item ->
                GridItem(text = item) {
                    // Navigate or handle click
                    navController.navigate("AICharacterListScreen")
                }
            }
        }
    }
}

@Composable
fun GridItem(text: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
//                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            // Optionally place an icon or image inside
            Icon(
                painter = painterResource(id = R.drawable.test),
                contentDescription = text,
                tint = Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = text,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/** ---------------------------
 *       AI PROFILE SCREEN
 * --------------------------- */
@Composable
fun AIProfileScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()

//            .background(color = Color.Red)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ){
            items(aiProfiles) { profile ->
                AIProfileCard(profile) {
                    navController.navigate("ChatScreen")
                }
            }
        }
    }
}
@Composable
fun AIProfileCard(
    profile: AIProfile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .height(220.dp)
            .padding(8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), // Reduced from 12.dp
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image Container
            Box(
                modifier = Modifier
                    .size(80.dp) // Reduced from 90.dp to fit height
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = profile.image),
                    contentDescription = "AI Profile Image",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Name
            Text(
                text = profile.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp, // Slightly larger for readability
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Description
            Text(
                text = profile.description,
                fontSize = 10.sp, // Slightly bigger than 9.sp
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Rating & Chat Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFD700), // Gold color
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${profile.rating}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Chat
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MailOutline,
                        contentDescription = "Chat count",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Chat",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Chat Button
            Button(
                onClick = { onClick() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Text(
                    text = "Chat Now",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/** ---------------------------
 *    OPTIONAL: PREVIEWS
 * --------------------------- */
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        // Use rememberNavController() which is a composable function.
        val navController = rememberNavController()
        HomeScreen(navController = navController)
    }
}
