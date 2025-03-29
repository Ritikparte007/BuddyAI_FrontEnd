package com.example.neuroed

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
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
//import androidx.compose.ui.text.TextStyle
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
import java.util.Locale


import android.os.Handler
import android.os.Looper
import android.text.Layout
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
//import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureScope
//import androidx.compose.ui.layout.Placeable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.neuroed.model.TestCreate
import com.example.neuroed.network.MyWebSocketListener
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.SubjectSyllabusSaveRepository
import com.example.neuroed.repository.SubjectlistRepository
import com.example.neuroed.repository.TestCreateRepository
import com.example.neuroed.viewmodel.SubjectSyllabusSaveViewModel
import com.example.neuroed.viewmodel.SubjectSyllabusSaveViewModelFactory
import com.example.neuroed.viewmodel.SubjectlistViewModel
import com.example.neuroed.viewmodel.SubjectlistViewModelFactory
import com.example.neuroed.viewmodel.TestCreateViewModel
import com.example.neuroed.viewmodel.TestCreateViewModelFactory
import kotlinx.coroutines.launch


import okhttp3.WebSocket
import okhttp3.Request
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import kotlin.math.cos
import kotlin.math.sin

import androidx.compose.material3.Icon
import androidx.compose.material3.Text

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



//import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.drawscope.DrawScope
//import androidx.compose.ui.layout.Placeable










import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
//import androidx.compose.ui.graphics.drawscope.drawLine
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel

import coil.compose.rememberAsyncImagePainter
import com.example.neuroed.R

import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

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
    // WebSocket setup using DisposableEffect remains unchanged.
    DisposableEffect(Unit) {
        val client = okhttp3.OkHttpClient()
        val request = okhttp3.Request.Builder()
            .url("ws://localhost:8000/ws/agent/new/user/")
            .build()
        val webSocket = client.newWebSocket(request, MyWebSocketListener())
        onDispose {
            webSocket.close(1000, "HomeScreen disposed")
        }
    }

    // Drawer state and coroutine scope to control the drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

//    =============Test Create =============================================================
    val apiService = RetrofitClient.apiService
    val repository = TestCreateRepository(apiService)
    val viewModel: TestCreateViewModel = viewModel(
        factory = TestCreateViewModelFactory(repository)
    )

    LaunchedEffect(Unit) {
        val testCreateData = TestCreate(userId = 1)
        viewModel.TestCreateModel(testCreateData)
    }
//    ====================================================================================



    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.5f) // This sets the drawer width to 50% of the screen
            ) {
                // Customize your drawer content here.
                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = false,
                    onClick = { /* Navigate or perform action */ },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") }
                )
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = { /* Navigate or perform action */ },
                    icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") }
                )
                // Add more items as needed.
            }
        },
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        modifier = Modifier.shadow(4.dp),
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        // Add a navigationIcon (hamburger menu) that opens the drawer.
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    scope.launch { drawerState.open() }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu Icon"
                                )
                            }
                        },
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
                                    modifier = Modifier.size(25.dp),
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
                                            Text("0")
                                        }
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.notificationbell),
                                        contentDescription = "Notification Icon",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.Unspecified
                                    )
                                }
                            }
                        }
                    )
                },
//                floatingActionButton = {
//                    FloatingActionButton(
//                        onClick = { navController.navigate("AutoOpenCameraScreen") }
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.camera),
//                            contentDescription = "Camera Icon",
//                            modifier = Modifier.size(40.dp),
//                            tint = Color.Unspecified
//                        )
//                    }
//                },
                bottomBar = {
                    BottomNavigationBar(navController)
                }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    item { SectionHeader(title = "SyllabusHub") }
                    item { SubjectList(navController) }
                    item { SectionHeader(title = "VirtuBeings") }
                    item { AIProfileScreen(navController) }
                    item { UserProfile(navController) }
                    item { GridScreen(navController) }
                    item{AgentList()}
                    item{
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)) {
                            EmotionsDiagramAnimatedUI()
                        }
                    }
                }
            }
        }
    )
}



/** ---------------------------
 *       Websocket FUNCTION
 * --------------------------- */


private lateinit var webSocket: WebSocket






/** ---------------------------
 *       SECTION HEADER
 * --------------------------- */
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.W500,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.onBackground
    )
}


/** ---------------------------
 *       SUBJECT LIST
 * --------------------------- */


@Composable
fun SubjectList(
    navController: NavController,
    subjectlistViewModel: SubjectlistViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = SubjectlistViewModelFactory(
            // Provide your repository instance using RetrofitClient.apiService
            SubjectlistRepository(RetrofitClient.apiService)
        )
    )
) {
    // Collect the subject list state from the ViewModel
    val subjects by subjectlistViewModel.subjectList.collectAsState()

    // Trigger the API call when the composable enters composition
    LaunchedEffect(Unit) {
        subjectlistViewModel.fetchSubjectList() // Defaults to userId = 1 if defined that way
    }

    if (subjects.isEmpty()) {
        // Show a loading indicator while waiting for data
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Display fetched subjects in a horizontal list.
        // Each subject card is clickable and navigates to a detail screen.
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(subjects) { subject ->
                Card(
                    modifier = Modifier
                        .clickable {
                            // Replace with your actual navigation route.
                            // Assumes subject has an 'id' property.
                            navController.navigate("SyllabusScreen/${subject.id}/${subject.subjectDescription}/${subject.subject}")
                        }
                        .width(130.dp)
                        .height(190.dp),
                    shape = RoundedCornerShape(13.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Use Coil's AsyncImage with placeholder and error fallback.
                        AsyncImage(
                            model = subject.subjectImage ?: R.drawable.biology,
                            contentDescription = "Subject Image",
                            placeholder = painterResource(id = R.drawable.mathssub),
                            error = painterResource(id = R.drawable.biology),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp))
                        )
                        // Optional overlay (for loading/progress indication).
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(60.dp),
                                progress = 1.0f, // Replace with actual progress if available.
                                color = Color.White,
                                strokeWidth = 6.dp
                            )
                            Text(
                                text = "100%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        // Subject label overlay at top start.
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.TopStart)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = subject.subject,
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
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.biology),
                contentDescription = "User Profile",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "John Doe",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
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
 *       VOICE VISUALIZER
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

/** ---------------------------
 *       BOTTOM NAV BAR
 * --------------------------- */















@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(navController: NavController) {
    // State to toggle mic animation
    var isListening by remember { mutableStateOf(false) }
    // Track the currently selected route for visual feedback
    var selectedRoute by remember { mutableStateOf("CreateSubjectScreen") }
    val context = LocalContext.current


    // Wrap the NavigationBar inside a Surface for rounded corners and elevation
    Surface(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth() // full width surface

    ) {
        // Box to apply a gradient background behind the NavigationBar
        Box(
            modifier = Modifier
                .fillMaxWidth() // fill entire width
                .height(64.dp)
        ) {
            NavigationBar(
                containerColor = Color.Transparent, // Let the gradient show through
                modifier = Modifier.fillMaxSize()
            ) {
                NavigationBarItem(
                    selected = selectedRoute == "CreateSubjectScreen",
                    onClick = {
                        selectedRoute = "CreateSubjectScreen"
                        navController.navigate("CreateSubjectScreen")
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.curriculum),
                            contentDescription = "Subject Icon",
                            modifier = Modifier.size(22.dp),
                            tint = if (selectedRoute == "CreateSubjectScreen") MaterialTheme.colorScheme.primary else Color.Unspecified
                        )
                    },
                    label = {
                        Text(
                            "Subject",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedRoute == "CreateSubjectScreen") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
                NavigationBarItem(
                    selected = selectedRoute == "ReelsScreen",
                    onClick = {
                        selectedRoute = "ReelsScreen"
                        navController.navigate("ReelsScreen")
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.play),
                            contentDescription = "Reels",
                            modifier = Modifier.size(22.dp),
                            tint = if (selectedRoute == "ReelsScreen") MaterialTheme.colorScheme.primary else Color.Unspecified
                        )
                    },
                    label = {
                        Text(
                            "Feed",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedRoute == "ReelsScreen") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
                // Special mic button does not require selection state
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        isListening = !isListening
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                context as Activity,
                                arrayOf(Manifest.permission.RECORD_AUDIO),
                                101
                            )
                        } else {
                            startListening(context)
                        }
                    },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .clickable { /* Already handled in onClick */ },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isListening) {
                                VoiceVisualizer(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 4.dp)
                                )
                            }
                            Icon(
                                painter = painterResource(id = R.drawable.voice),
                                contentDescription = "Mic Icon",
                                modifier = Modifier.size(26.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    label = { Text("") }
                )
                NavigationBarItem(
                    selected = selectedRoute == "library",
                    onClick = {
                        selectedRoute = "library"
                        navController.navigate("library")
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.graph),
                            contentDescription = "Library Icon",
                            modifier = Modifier.size(22.dp),
                            tint = if (selectedRoute == "library") MaterialTheme.colorScheme.primary else Color.Unspecified
                        )
                    },
                    label = {
                        Text(
                            "Library",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedRoute == "library") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
                NavigationBarItem(
                    selected = selectedRoute == "settings",
                    onClick = {
                        selectedRoute = "settings"
                        navController.navigate("settings")
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.bot),
                            contentDescription = "Character Icon",
                            modifier = Modifier.size(22.dp),
                            tint = if (selectedRoute == "settings") MaterialTheme.colorScheme.primary else Color.Unspecified
                        )
                    },
                    label = {
                        Text(
                            "Character",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedRoute == "settings") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }
        }
    }
}








/** ---------------------------
 *       GRID SCREEN
 * --------------------------- */

data class GridItemModel(
    val title: String,
    @DrawableRes val iconRes: Int,
    val route: String,
    val badgecount: Int = 0
)

@Composable
fun GridScreen(navController: NavController) {
    val items = listOf(
        GridItemModel("Test", R.drawable.test, "TestsScreen", badgecount = 2),
        GridItemModel("Task", R.drawable.taskuser, "TaskScreen",badgecount = 10),
        GridItemModel("Exam", R.drawable.exam, "ExamScreen", badgecount = 1),
        GridItemModel("Recall", R.drawable.recall, "RecallingScreen"),
        GridItemModel("Store", R.drawable.folders, "StoreScreen"),
        GridItemModel("Mindmap", R.drawable.mindmap, "DocumentsScreen"),
        GridItemModel("Assignents", R.drawable.contract, "PlaygroundScreen"),
        GridItemModel("Playground", R.drawable.readingbook, "PlaygroundScreen"),
        GridItemModel("Meditation",R.drawable.meditation," Meditation"),
        GridItemModel("Brainhealth",R.drawable.brainhealth," Meditation"),
        GridItemModel("Meditation",R.drawable.translation," Meditation"),
        GridItemModel("Meditation",R.drawable.question,"MeditationScreen")

        // Add more items as needed...
    )

    Box(
        modifier = Modifier
            .height(350.dp)
            .padding(16.dp)
//            .background(color = Color.Red)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            userScrollEnabled = false
        ) {
            items(items) { item ->
                GridItem(
                    text = item.title,
                    iconRes = item.iconRes,
                    badgeCount = item.badgecount,
                    onClick = { navController.navigate(item.route) }
                )
            }
        }
    }
}


@Composable
fun GridItem(
    text: String,
    @DrawableRes iconRes: Int,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
//            .background(color = Color.Green)
            .width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Use BadgedBox to overlay a badge on the icon
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge {
                        Text(
                            text = badgeCount.toString(),
                            fontSize = 10.sp, // Adjust the font size if needed
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = text,
                    tint = Color.Unspecified
                )
            }
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


@Composable
fun AgentList() {
    // Root column
    Column(
        modifier = Modifier
            .fillMaxSize()               // Fill the parent container .background(Color(0xFF555555)) // Example background color from the screenshot
            .padding(16.dp)             // Outer padding
    ) {
        // Title
        Text(
            text = "Agent",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Row that holds the avatar/name and the plus icon
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Column for agent avatar + name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular avatar (replace with your image resource)
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                ) {
                    // For a local drawable resource
                    Image(
                        painter = painterResource(R.drawable.man), // Replace with your drawable
                        contentDescription = "Agent Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Agent name
                Text(
                    text = "Areax",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontSize = 12.sp,

                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            // Circular box for plus button
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Add Agent",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun EmotionsDiagramAnimatedUI(modifier: Modifier = Modifier) {
    // Updated list of emojis (8 in total).
    val emojis = listOf("😊", "😡", "😨", "😔", "❤️", "😎", "🤩", "😍")
    // Base angles for 8 emojis, evenly distributed around the circle.
    val baseAngles = listOf(270f, 315f, 0f, 45f, 90f, 135f, 180f, 225f)
    val circleRadius = 140.dp

    // New lists for adjusting orbit distances and sizes.
    val emojiRadiusMultipliers = listOf(0.8f, 1.0f, 1.2f, 0.9f, 1.1f, 0.85f, 1.15f, 1.0f)
    val emojiFontSizes = listOf(24.sp, 32.sp, 40.sp, 28.sp, 36.sp, 30.sp, 38.sp, 34.sp)

    // No animation – rotation remains constant (0f).
    val rotation = 0f

    // State to hold computed line positions (for drawing connecting lines).
    var linePositions by remember { mutableStateOf(emptyList<Pair<Offset, Offset>>()) }

    // Outer Box: draws background, lines, and overlays our custom Layout.
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Canvas draws the connecting lines.
        Canvas(modifier = Modifier.matchParentSize()) {
            linePositions.forEach { (start, end) ->
                drawLine(
                    color = Color.White,
                    start = start,
                    end = end,
                    strokeWidth = 4f
                )
            }
        }

        // Custom Layout for positioning the center avatar and emojis.
        Layout(
            content = {
                // Center avatar.
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.man),
                        contentDescription = "Person",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(65.dp)
                    )
                }
                // Emojis around the center with varying sizes.
                emojis.forEachIndexed { index, emoji ->
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = emoji,
                            style = TextStyle(fontSize = emojiFontSizes[index]),
                            modifier = Modifier
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { measurables, constraints ->
            // Measure children.
            val centerMeasurable = measurables.first()
            val emojiMeasurables = measurables.drop(1)

            val centerPlaceable = centerMeasurable.measure(constraints)
            val emojiPlaceables = emojiMeasurables.map { it.measure(constraints) }

            val layoutWidth = constraints.maxWidth
            val layoutHeight = constraints.maxHeight
            val radiusPx = circleRadius.toPx()

            val centerX = layoutWidth / 2f
            val centerY = layoutHeight / 2f

            // Compute center avatar's top-left position.
            val centerLeft = (centerX - centerPlaceable.width / 2f).toInt()
            val centerTop = (centerY - centerPlaceable.height / 2f).toInt()

            // Compute each emoji's position and corresponding connecting line.
            val computedData = emojiPlaceables.mapIndexed { index, placeable ->
                // Use the base angle plus a constant rotation (0f) in this static diagram.
                val angleDeg = baseAngles[index] + rotation
                val angleRad = Math.toRadians(angleDeg.toDouble())
                // Adjust orbit distance using a multiplier.
                val multiplier = emojiRadiusMultipliers[index]
                val offsetX = (radiusPx * multiplier * cos(angleRad)).toFloat()
                val offsetY = (radiusPx * multiplier * sin(angleRad)).toFloat()

                val left = (centerX + offsetX - placeable.width / 2f).toInt()
                val top = (centerY + offsetY - placeable.height / 2f).toInt()

                // Return both the line (for Canvas) and the position (for placing the emoji).
                Pair(Offset(centerX, centerY) to Offset(centerX + offsetX, centerY + offsetY), left to top)
            }
            val emojiPositions = computedData.map { it.second }
            // Update state to draw connecting lines.
            linePositions = computedData.map { it.first }

            layout(layoutWidth, layoutHeight) {
                // Place the center avatar.
                centerPlaceable.place(centerLeft, centerTop)
                // Place each emoji.
                emojiPlaceables.forEachIndexed { index, placeable ->
                    val (left, top) = emojiPositions[index]
                    placeable.place(left, top)
                }
            }
        }
    }
}




/** ---------------------------
 *       AI PROFILE SCREEN
 * --------------------------- */
@Composable
fun AIProfileScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(aiProfiles) { profile ->
                AIProfileCard(profile) { navController.navigate("ChatScreen") }
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
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
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
            Text(
                text = profile.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = profile.description,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFD700),
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
 *   SPEECH RECOGNITION SETUP
 * --------------------------- */


private var speechRecognizer: SpeechRecognizer? = null
private var isListening = false

private fun startListening(context: Context) {
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
        Toast.makeText(context, "Speech Recognition not available", Toast.LENGTH_SHORT).show()
        return
    }
    // Create a persistent SpeechRecognizer instance if needed
    if (speechRecognizer == null) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    }

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    speechRecognizer?.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d("SpeechRecognizer", "Ready for speech")
        }
        override fun onBeginningOfSpeech() {
            Log.d("SpeechRecognizer", "Speech started")
        }
        override fun onRmsChanged(rmsdB: Float) { }
        override fun onBufferReceived(buffer: ByteArray?) { }
        override fun onEndOfSpeech() {
            Log.d("SpeechRecognizer", "Speech ended")
            // Add a slight delay before restarting listening
            Handler(Looper.getMainLooper()).postDelayed({
                if (isListening) {
                    speechRecognizer?.startListening(intent)
                }
            }, 500)
        }
        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                else -> "Unknown error"
            }
            Log.e("SpeechRecognizer", "Error occurred: $error - $errorMessage")
            // Add a slight delay before restarting listening (if appropriate)
            Handler(Looper.getMainLooper()).postDelayed({
                if (isListening && error != SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                    speechRecognizer?.startListening(intent)
                }
            }, 500)
        }
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.firstOrNull()?.let { finalText ->
                Log.d("SpeechRecognizer", "Final result: $finalText")
            }
        }
        override fun onPartialResults(partialResults: Bundle?) {
            val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            partialMatches?.firstOrNull()?.let { partialText ->
                Log.d("SpeechRecognizer", "Partial result: $partialText")
            }
        }
        override fun onEvent(eventType: Int, params: Bundle?) { }
    })

    // Set listening state and start listening
    isListening = true
    speechRecognizer?.startListening(intent)
}

// Remember to add a stop function to properly release resources when needed
private fun stopListening() {
    isListening = false
    speechRecognizer?.stopListening()
    speechRecognizer?.cancel()
    speechRecognizer?.destroy()
    speechRecognizer = null
}


/** ---------------------------
 *    OPTIONAL: PREVIEWS
 * --------------------------- */
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        val navController = rememberNavController()
        HomeScreen(navController = navController)
    }
}



