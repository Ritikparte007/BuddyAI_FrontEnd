package com.example.neuroed

import Feed
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

// ✅ ADD: Permission imports
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import android.Manifest

/* ─────────────────── consolidated design tokens ─────────────────── */
object NeuroEdColors {
    /* brand colors */
    val primaryPurple   = Color(0xFF6A5ACD)
    val secondaryPurple = Color(0xFF9370DB)

    /* LIGHT */
    val lightBg      = Color(0xFFF9FAFB)   // page background
    val lightSurface = Color(0xFFFFFFFF)   // cards, surfaces
    val lightChip    = Color(0xFFE6E6FA)   // chips, badges
    val lightText    = Color(0xFF6B7280)   // secondary text
    val onSurface    = Color(0xFF1F2937)   // primary text

    /* DARK */
    val darkBg       = Color(0xFF121212)   // dark background
    val darkSurface  = Color(0xFF1E1E1E)   // dark surfaces
    val darkChip     = Color(0xFF242424)   // dark chips
    val darkText     = Color.White         // primary text in dark
    val darkTextLite = Color(0xFFB3B3B3)   // secondary text in dark

    /* utility */
    val dividerColor  = Color(0xFFE5E7EB)
    val successGreen  = Color(0xFF34D399)
    val infoBlue      = Color(0xFF3B82F6)
    val warningYellow = Color(0xFFFBBF24)
    val overlay       = Color(0x88000000)  // image overlays
}

/* ─────────────────── spacing system ─────────────────── */
object NeuroEdSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp

    val iconSize    = 24.dp
    val buttonSize  = 48.dp
    val avatarSize  = 32.dp
}

/* ─────────────────── shape system ─────────────────── */
object NeuroEdShapes {
    val small  = RoundedCornerShape(12.dp)
    val medium = RoundedCornerShape(16.dp)
    val large  = RoundedCornerShape(24.dp)  // pill shape
    val circle = CircleShape
}

/* ─────────────────── typography system ─────────────────── */
object NeuroEdTypography {
    val titleL  = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
    val titleM  = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
    val titleS  = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
    val bodyL   = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
    val bodyM   = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
    val bodyS   = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)
    val caption = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal)
}

// -------- Data Model --------
data class NewsContent(
    val id: Int,
    val title: String,
    val description: String,
    val imageRes: Int,
    val imageBitmap: ImageBitmap? = null,
    val source: String,
    val timePosted: String,
    val likes: Int
)

// -------- Styled Feed Screen --------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class)
@Composable
fun FeedScreen(
    navController: NavController,
    voiceMessageViewModel: VoiceMessageViewModel,
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    // ✅ ADD: Define recordAudioPermission for this screen
    val recordAudioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    /* theme-aware colours */
    val isDark = isSystemInDarkTheme()
    val pageBg = if (isDark) NeuroEdColors.darkBg else NeuroEdColors.lightBg
    val cardBg = if (isDark) NeuroEdColors.darkSurface else NeuroEdColors.lightSurface
    val chipBg = if (isDark) NeuroEdColors.darkChip else NeuroEdColors.lightChip
    val textPrimary = if (isDark) NeuroEdColors.darkText else NeuroEdColors.onSurface
    val textSecondary = if (isDark) NeuroEdColors.darkTextLite else NeuroEdColors.lightText

    // API integration
    val context = LocalContext.current
    val apiService = RetrofitClient.apiService

    // State holders
    var feeds by remember { mutableStateOf<List<Feed>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentContentType by remember { mutableStateOf<String?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Track time spent viewing
    var currentFeedId by remember { mutableStateOf<Int?>(null) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Fetch feeds on initial load
    LaunchedEffect(currentContentType) {
        isLoading = true
        error = null

        try {
            val response = ApiHelper.executeWithToken { token ->
                currentContentType?.let { type ->
                    // This block only runs if currentContentType is not null
                    apiService.getFeedsByContentType(type, token)
                } ?: apiService.getFeeds(token)
            }

            if (response.isSuccessful) {
                feeds = response.body() ?: emptyList()
            } else {
                error = "Failed to load feeds: ${response.code()}"
            }
        } catch (e: Exception) {
            error = e.message ?: "Unknown error occurred"
        }

        isLoading = false
    }

    // Update time spent when feed changes
    LaunchedEffect(currentFeedId) {
        if (currentFeedId != null) {
            val previousFeedId = currentFeedId
            val timeSpent = ((System.currentTimeMillis() - startTime) / 1000).toInt()

            if (timeSpent > 0 && previousFeedId != null) {
                try {
                    ApiHelper.executeWithToken { token ->
                        apiService.updateTimeSpent(
                            previousFeedId,
                            mapOf("spending_time_seconds" to timeSpent),
                            token
                        )
                    }
                } catch (e: Exception) {
                    // Silently handle error, don't disrupt user experience
                }
            }
        }

        startTime = System.currentTimeMillis()
    }

    // Animation state
    val visible = remember {
        MutableTransitionState(false).apply { targetState = true }
    }

    // Content Type Filter Dialog
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter by Content Type") },
            text = {
                Column {
                    // All option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                currentContentType = null
                                showFilterDialog = false
                            },
                        color = if (currentContentType == null) NeuroEdColors.lightChip else Color.Transparent
                    ) {
                        Text(
                            text = "All",
                            style = NeuroEdTypography.bodyL,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    // Content type options
                    listOf("Technology", "Chemistry", "Biology", "Physics", "Other").forEach { type ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    currentContentType = type
                                    showFilterDialog = false
                                },
                            color = if (currentContentType == type) NeuroEdColors.lightChip else Color.Transparent
                        ) {
                            Text(
                                text = type,
                                style = NeuroEdTypography.bodyL,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = pageBg,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier
                            .padding(NeuroEdSpacing.sm)
                            .clip(NeuroEdShapes.circle)
                            .background(chipBg)
                    ) {
                        Icon(Icons.Filled.Menu, contentDescription = null, tint = NeuroEdColors.primaryPurple)
                    }
                },
                title = {},
                actions = {
                    // Filter button
                    IconButton(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier
                            .padding(NeuroEdSpacing.sm)
                            .clip(NeuroEdShapes.circle)
                            .background(chipBg)
                    ) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "Filter feeds",
                            tint = NeuroEdColors.primaryPurple
                        )
                    }

                    IconButton(
                        onClick = { /* premium */ },
                        modifier = Modifier
                            .padding(NeuroEdSpacing.sm)
                            .clip(NeuroEdShapes.circle)
                            .background(chipBg)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.premium),
                            contentDescription = null,
                            tint = NeuroEdColors.primaryPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            // ✅ FIXED: Complete BottomNavigationBar call with all required parameters
            BottomNavigationBar(
                navController = navController,
                voiceMessageViewModel = voiceMessageViewModel,
                onSoundLevelChanged = { level ->
                    // Handle sound level changes if needed for feed screen
                    // You can leave this empty or implement functionality
                },
                recordAudioPermission = recordAudioPermission,
                isListening = false,  // You can connect this to actual voice state if needed
                recognizedText = "",  // You can connect this to actual voice state if needed
                startVoice = {
                    // Handle start voice functionality
                    // You can leave this empty if voice is not needed on feed screen
                },
                stopVoice = {
                    // Handle stop voice functionality
                    // You can leave this empty if voice is not needed on feed screen
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(pageBg)
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    // Loading state
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = NeuroEdColors.primaryPurple
                    )
                }

                error != null -> {
                    // Error state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(NeuroEdSpacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = NeuroEdColors.warningYellow
                        )

                        Spacer(Modifier.height(NeuroEdSpacing.lg))

                        Text(
                            "Something went wrong",
                            style = NeuroEdTypography.titleM.copy(color = textPrimary)
                        )

                        Spacer(Modifier.height(NeuroEdSpacing.md))

                        Text(
                            error ?: "Unknown error",
                            style = NeuroEdTypography.bodyM.copy(color = textSecondary),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(NeuroEdSpacing.lg))

                        Button(
                            onClick = {
                                isLoading = true
                                error = null
                                currentContentType = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeuroEdColors.primaryPurple
                            )
                        ) {
                            Text("Try Again")
                        }
                    }
                }

                feeds.isEmpty() -> {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(NeuroEdSpacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = NeuroEdColors.primaryPurple.copy(alpha = 0.5f)
                        )

                        Spacer(Modifier.height(NeuroEdSpacing.lg))

                        Text(
                            "No feeds available",
                            style = NeuroEdTypography.titleM.copy(color = textPrimary)
                        )

                        Spacer(Modifier.height(NeuroEdSpacing.md))

                        Text(
                            "Try a different filter or check back later",
                            style = NeuroEdTypography.bodyM.copy(color = textSecondary),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(NeuroEdSpacing.lg))

                        Button(
                            onClick = {
                                isLoading = true
                                error = null
                                currentContentType = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeuroEdColors.primaryPurple
                            )
                        ) {
                            Text("Refresh")
                        }
                    }
                }

                else -> {
                    // Convert API feeds to UI model
                    val newsContents = feeds.map { feed ->
                        val imageBitmap = feed.imageBase64?.let { base64 ->
                            try {
                                val imageBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                                val inputStream = java.io.ByteArrayInputStream(imageBytes)
                                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                                bitmap?.asImageBitmap()
                            } catch (e: Exception) {
                                null
                            }
                        }

                        NewsContent(
                            id = feed.id,
                            title = feed.title ?: "Untitled",
                            description = feed.content ?: "No content",
                            imageRes = R.drawable.maths, // Fallback image
                            imageBitmap = imageBitmap,
                            source = when (feed.contentType) {
                                null -> "NeuroEd" // Handle null case explicitly
                                "Technology" -> "Tech News"
                                "Chemistry" -> "Chemistry Today"
                                "Biology" -> "Bio Science"
                                "Physics" -> "Physics World"
                                else -> "NeuroEd"
                            },
                            timePosted = formatTimePosted(feed.createdAt ?: ""), // Add null check here too
                            likes = 0
                        )
                    }

                    if (newsContents.isNotEmpty()) {
                        val pagerState = rememberPagerState(pageCount = { newsContents.size })

                        // Update current feed ID when page changes
                        LaunchedEffect(pagerState.currentPage) {
                            currentFeedId = feeds[pagerState.currentPage].id
                        }

                        VerticalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            // Animate each page entry
                            AnimatedVisibility(
                                visible,
                                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
                            ) {
                                val content = newsContents[page]
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(cardBg)
                                ) {
                                    // Image with overlay
                                    if (content.imageBitmap != null) {
                                        Image(
                                            bitmap = content.imageBitmap,
                                            contentDescription = content.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        // Fallback to resource image
                                        Image(
                                            painter = painterResource(content.imageRes),
                                            contentDescription = content.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    // Overlay gradient
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        Color(0x66000000),
                                                        Color(0xAA000000)
                                                    ),
                                                    startY = 0f,
                                                    endY = Float.POSITIVE_INFINITY
                                                )
                                            )
                                    )

                                    // Content card at bottom
                                    Column(
                                        Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomCenter)
                                            .padding(
                                                start = NeuroEdSpacing.lg,
                                                end = NeuroEdSpacing.lg,
                                                bottom = NeuroEdSpacing.xl,
                                                top = NeuroEdSpacing.xl
                                            )
                                    ) {
                                        Text(
                                            content.title,
                                            style = NeuroEdTypography.titleM.copy(color = Color.White),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(Modifier.height(NeuroEdSpacing.sm))

                                        Text(
                                            content.description,
                                            style = NeuroEdTypography.bodyM.copy(color = Color.White.copy(alpha = 0.8f)),
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(Modifier.height(NeuroEdSpacing.lg))

                                        // Source and share row
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                // Avatar circle
                                                Box(
                                                    Modifier
                                                        .size(NeuroEdSpacing.buttonSize)
                                                        .clip(NeuroEdShapes.circle)
                                                        .background(NeuroEdColors.primaryPurple),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(content.source.first().toString(), color = Color.White)
                                                }

                                                Spacer(Modifier.width(NeuroEdSpacing.md))

                                                // Source info
                                                Column {
                                                    Text(
                                                        content.source,
                                                        style = NeuroEdTypography.bodyS.copy(color = Color.White)
                                                    )
                                                    Text(
                                                        content.timePosted,
                                                        style = NeuroEdTypography.caption.copy(color = Color.White.copy(alpha = 0.7f))
                                                    )
                                                }
                                            }

                                            // Share button
                                            IconButton(
                                                onClick = {
                                                    val share = Intent(Intent.ACTION_SEND).apply {
                                                        putExtra(Intent.EXTRA_TEXT, "${content.title}\n\n${content.description}")
                                                        type = "text/plain"
                                                    }
                                                    context.startActivity(Intent.createChooser(share, "Share via"))
                                                },
                                                modifier = Modifier
                                                    .size(NeuroEdSpacing.buttonSize)
                                                    .clip(NeuroEdShapes.circle)
                                                    .background(Color.White.copy(alpha = 0.2f))
                                            ) {
                                                Icon(
                                                    Icons.Filled.Share,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(NeuroEdSpacing.iconSize),
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper to format time posted
private fun formatTimePosted(createdAt: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(createdAt)
        val now = Calendar.getInstance().time

        val diffInMillis = now.time - (date?.time ?: now.time)
        val diffInHours = diffInMillis / (1000 * 60 * 60)

        when {
            diffInHours < 1 -> "Just now"
            diffInHours < 24 -> "$diffInHours hours ago"
            else -> "${diffInHours / 24} days ago"
        }
    } catch (e: Exception) {
        "Recently"
    }
}