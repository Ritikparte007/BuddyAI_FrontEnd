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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
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
        // Minimal, clean top bar
        topBar = {
            TopAppBar(
                // Slight elevation for depth
                modifier = Modifier.shadow(2.dp),
                colors = TopAppBarDefaults.smallTopAppBarColors(
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
                    Row {
                        Icon(
                            painter = painterResource(id = R.drawable.notificationbell),
                            contentDescription = "Notification Bell",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified // Keeps the original color of the drawable
                        )
                    }
                    IconButton(onClick = { navController.navigate("NotificationScreen") }) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text("3") // Replace "3" with your dynamic count value
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.notificationbell),
                                contentDescription = "Notification Icon",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("AutoOpenCameraScreen") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.camera),
                    contentDescription = "Camera Icon",
                    tint = MaterialTheme.colorScheme.onPrimary
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
                SectionHeader(title = "Syllabus Hub")
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
            .horizontalScroll(rememberScrollState())
            .height(230.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(6) { // Show fewer items for clarity
            Card(
                modifier = Modifier
                    .width(160.dp)
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.biology),
                        contentDescription = "Subject Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    // Overlapping Circular Progress
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(60.dp),
                            progress = progress,
                            color = MaterialTheme.colorScheme.primary,
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
                }
            }
        }
    }
}

/** ---------------------------
 *       USER PROFILE
 * --------------------------- */
@Composable
fun UserProfile(navController: NavController) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF6DD5FA), Color(0xFF2980B9))
                )
            )
            .padding(16.dp)
            .clickable {
                navController.navigate("ProfileScreen")
            }

    ) {
        // User Image
        Image(
            painter = painterResource(id = R.drawable.biology),
            contentDescription = "User Profile",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "John Doe",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Row
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    fontSize = 12.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rating Stars
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

/** ---------------------------
 *       BOTTOM NAV BAR
 * --------------------------- */

@Composable
fun BottomNavigationBar(navController: NavController) {
    var isListening by remember { mutableStateOf(false) }

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("CreateSubjectScreen") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_icon),
                    contentDescription = "Subject Icon",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            label = { Text("Subject", color = MaterialTheme.colorScheme.onSurface) }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("ReelsScreen") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_icon),
                    contentDescription = "Profile Icon",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            label = { Text("Profile", color = MaterialTheme.colorScheme.onSurface) }
        )

        // Microphone button with animation
        NavigationBarItem(
            selected = false,
            onClick = { isListening = !isListening },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { isListening = !isListening },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_icon), // Replace with your mic icon
                        contentDescription = "Mic Icon",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
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
                    painter = painterResource(id = R.drawable.ic_icon),
                    contentDescription = "Library Icon",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            label = { Text("Library", color = MaterialTheme.colorScheme.onSurface) }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("settings") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.square_plus),
                    contentDescription = "Character Icon",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            label = { Text("Character", color = MaterialTheme.colorScheme.onSurface) }
        )
    }

    // Listening Animation
    if (isListening) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(80.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
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
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            // Optionally place an icon or image inside
            Icon(
                painter = painterResource(id = R.drawable.ic_icon),
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
            .padding(16.dp)
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            .width(150.dp)
            .padding(4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            Image(
                painter = painterResource(id = profile.image),
                contentDescription = "AI Profile Image",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Name
            Text(
                text = profile.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Description
            Text(
                text = profile.description,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Rating
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = Color.Yellow,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${profile.rating}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
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
