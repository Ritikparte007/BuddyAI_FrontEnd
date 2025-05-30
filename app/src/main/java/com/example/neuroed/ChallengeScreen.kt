package com.example.neuroed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.neuroed.ViewModelFactory.ChallengeViewModelFactory
import com.example.neuroed.model.HostedChallenge
import com.example.neuroed.model.JoinRequestModel
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.ChallengeRepository
import com.example.neuroed.viewmodel.ChallengeViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreen(
    navController: NavController,
    viewModel: ChallengeViewModel = viewModel(
        factory = ChallengeViewModelFactory(
            repository = ChallengeRepository(
                api = RetrofitClient.apiService
            )
        )
    )
) {
    // Collect states from ViewModel
    val createdChallenges by viewModel.createdChallenges.observeAsState(emptyList())
    val joinRequests by viewModel.joinRequests.observeAsState(emptyList())
    val isLoading by viewModel.loading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val challengeCreated by viewModel.challengeCreated.observeAsState(false)

    // Load data when screen is first created
    LaunchedEffect(key1 = Unit) {
        viewModel.loadCreatedChallenges()
        viewModel.loadJoinRequests()
    }

    // Basic states
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Screen state - can be "create", "manage", "requests"
    var currentScreen by remember { mutableStateOf("create") }

    // Challenge creation states
    var challengeTitle by remember { mutableStateOf("") }
    var challengeDescription by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("10") }
    var selectedDifficulty by remember { mutableStateOf("Intermediate") }
    var isPrivate by remember { mutableStateOf(false) }
    var challengeTopics by remember { mutableStateOf("") }

    // Difficulty dropdown state
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val difficulties = listOf("Beginner", "Intermediate", "Advanced")

    // Generate a random challenge ID
    val challengeId = remember {
        val chars = ('A'..'Z') + ('0'..'9')
        (1..6).map { chars.random() }.joinToString("")
    }

    // Error handling
    LaunchedEffect(key1 = error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Challenge creation success handling
    LaunchedEffect(key1 = challengeCreated) {
        if (challengeCreated) {
            snackbarHostState.showSnackbar("Challenge created successfully!")
            viewModel.resetChallengeCreated()
            currentScreen = "manage"
        }
    }

    // Improved theme colors
    val backgroundColor = Color(0xFF0A0A0A)
    val accentColor = Color(0xFF2196F3)
    val accentGradientStart = Color(0xFF2196F3)
    val accentGradientEnd = Color(0xFF00B0FF)
    val cardBackgroundColor = Color(0xFF1A1A1A)
    val topBarColor = Color(0xFF1E1E1E)  // Slightly different to make it stand out
    val textPrimaryColor = Color.White
    val textSecondaryColor = Color(0xFFB0B0B0)

    Scaffold(
        containerColor = backgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // IMPROVED TOP BAR DESIGN WITH BETTER VISIBILITY
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 12.dp),
                color = topBarColor,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Top section with back button and title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back button with better prominence
                        Surface(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape),
                            color = accentColor.copy(alpha = 0.2f),
                            shadowElevation = 4.dp
                        ) {
                            IconButton(
                                onClick = {
                                    if (currentScreen == "create") {
                                        navController.popBackStack()
                                    } else {
                                        currentScreen = "create"
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = accentColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Title
                        Text(
                            text = when(currentScreen) {
                                "create" -> "Create Challenge"
                                "manage" -> "My Challenges"
                                "requests" -> "Join Requests"
                                else -> "Challenges"
                            },
                            color = textPrimaryColor,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        // Notification badge
                        if (currentScreen != "requests" && joinRequests.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape),
                                color = accentColor,
                                shadowElevation = 4.dp
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { currentScreen = "requests" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = joinRequests.size.toString(),
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Tab row
                    TabRow(
                        selectedTabIndex = when(currentScreen) {
                            "create" -> 0
                            "manage" -> 1
                            "requests" -> 2
                            else -> 0
                        },
                        containerColor = topBarColor,
                        contentColor = accentColor,
                        divider = {
                            Divider(
                                thickness = 1.dp,
                                color = Color(0xFF303030)
                            )
                        },
                        indicator = { tabPositions ->
                            Box(
                                modifier = Modifier
                                    .tabIndicatorOffset(
                                        tabPositions[when(currentScreen) {
                                            "create" -> 0
                                            "manage" -> 1
                                            "requests" -> 2
                                            else -> 0
                                        }]
                                    )
                                    .height(3.dp)
                                    .padding(horizontal = 40.dp)
                                    .background(
                                        accentColor,
                                        shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                                    )
                            )
                        }
                    ) {
                        // Create Tab
                        Tab(
                            selected = currentScreen == "create",
                            onClick = { currentScreen = "create" },
                            text = {
                                Text(
                                    "Create",
                                    color = if (currentScreen == "create") accentColor else textSecondaryColor,
                                    fontWeight = if (currentScreen == "create") FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create",
                                    tint = if (currentScreen == "create") accentColor else textSecondaryColor
                                )
                            }
                        )

                        // My Challenges Tab
                        Tab(
                            selected = currentScreen == "manage",
                            onClick = { currentScreen = "manage" },
                            text = {
                                Text(
                                    "My Challenges",
                                    color = if (currentScreen == "manage") accentColor else textSecondaryColor,
                                    fontWeight = if (currentScreen == "manage") FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Manage",
                                    tint = if (currentScreen == "manage") accentColor else textSecondaryColor
                                )
                            }
                        )

                        // Requests Tab
                        Tab(
                            selected = currentScreen == "requests",
                            onClick = { currentScreen = "requests" },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Requests",
                                        color = if (currentScreen == "requests") accentColor else textSecondaryColor,
                                        fontWeight = if (currentScreen == "requests") FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (joinRequests.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(accentColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = joinRequests.size.toString(),
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Requests",
                                    tint = if (currentScreen == "requests") accentColor else textSecondaryColor
                                )
                            }
                        )
                    }

                    // Add a clear divider to separate top bar from content
                    Divider(
                        color = Color(0xFF303030),
                        thickness = 2.dp
                    )
                }
            }
        }
    ) { paddingValues ->
        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = accentColor)
            }
        } else {
            when (currentScreen) {
                "create" -> {
                    // Create Challenge Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Challenge ID (Generated)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = accentColor.copy(alpha = 0.15f)
                            ),
                            border = BorderStroke(1.dp, accentColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Your Challenge ID",
                                    color = textPrimaryColor,
                                    fontSize = 16.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = challengeId,
                                        color = accentColor,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(onClick = {
                                        // Copy to clipboard action
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Challenge ID copied to clipboard")
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Copy ID",
                                            tint = accentColor
                                        )
                                    }
                                }

                                Text(
                                    text = "Share this ID with others to join your challenge",
                                    color = textSecondaryColor,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Challenge Title
                        OutlinedTextField(
                            value = challengeTitle,
                            onValueChange = { challengeTitle = it },
                            label = { Text("Challenge Title", color = textSecondaryColor) },
                            placeholder = { Text("Enter a name for your challenge", color = textSecondaryColor) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = Color(0xFF555555),
                                containerColor = cardBackgroundColor,
//                                textColor = textPrimaryColor
                            )
                        )

                        // Challenge Description
                        OutlinedTextField(
                            value = challengeDescription,
                            onValueChange = { challengeDescription = it },
                            label = { Text("Description", color = textSecondaryColor) },
                            placeholder = { Text("Describe your challenge", color = textSecondaryColor) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = Color(0xFF555555),
                                containerColor = cardBackgroundColor,
//                                textColor = textPrimaryColor
                            ),
                            maxLines = 3
                        )

                        // Challenge Topics
                        OutlinedTextField(
                            value = challengeTopics,
                            onValueChange = { challengeTopics = it },
                            label = { Text("Topics (comma separated)", color = textSecondaryColor) },
                            placeholder = { Text("e.g. Math, Algebra, Geometry", color = textSecondaryColor) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = Color(0xFF555555),
                                containerColor = cardBackgroundColor,
//                                textColor = textPrimaryColor
                            )
                        )

                        // Max Participants & Difficulty in a row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Max Participants
                            OutlinedTextField(
                                value = maxParticipants,
                                onValueChange = {
                                    if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 2..100)) {
                                        maxParticipants = it
                                    }
                                },
                                label = { Text("Max Participants", color = textSecondaryColor) },
                                modifier = Modifier.weight(0.5f),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = Color(0xFF555555),
                                    containerColor = cardBackgroundColor,
//                                    textColor = textPrimaryColor
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            // Difficulty dropdown
                            Box(modifier = Modifier.weight(0.5f)) {
                                OutlinedTextField(
                                    value = selectedDifficulty,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Difficulty", color = textSecondaryColor) },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Select Difficulty",
                                            tint = accentColor,
                                            modifier = Modifier.clickable { isDropdownExpanded = !isDropdownExpanded }
                                        )
                                    },
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = accentColor,
                                        unfocusedBorderColor = Color(0xFF555555),
                                        containerColor = cardBackgroundColor,
//                                        textColor = textPrimaryColor
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                DropdownMenu(
                                    expanded = isDropdownExpanded,
                                    onDismissRequest = { isDropdownExpanded = false },
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .background(cardBackgroundColor)
                                ) {
                                    difficulties.forEach { difficulty ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = difficulty,
                                                    color = textPrimaryColor
                                                )
                                            },
                                            onClick = {
                                                selectedDifficulty = difficulty
                                                isDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Privacy setting
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Challenge Privacy: ",
                                color = textPrimaryColor,
                                modifier = Modifier.weight(1f)
                            )

                            Switch(
                                checked = isPrivate,
                                onCheckedChange = { isPrivate = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = accentColor,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFF555555)
                                )
                            )

                            Text(
                                text = if (isPrivate) "Private" else "Public",
                                color = textPrimaryColor,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Create Button
                        Button(
                            onClick = {
                                if (challengeTitle.isBlank()) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Please enter a challenge title")
                                    }
                                    return@Button
                                }

                                // Create the challenge using ViewModel
                                viewModel.createChallenge(
                                    title = challengeTitle,
                                    description = challengeDescription,
                                    topics = challengeTopics.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                    maxParticipants = maxParticipants.toIntOrNull() ?: 10,
                                    difficulty = selectedDifficulty,
                                    isPrivate = isPrivate
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "CREATE CHALLENGE",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                "manage" -> {
                    // Manage Challenges Screen
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        if (createdChallenges.isEmpty()) {
                            // Empty state
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "No challenges",
                                    tint = textSecondaryColor,
                                    modifier = Modifier.size(80.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "No challenges created yet",
                                    color = textPrimaryColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Create your first challenge to get started!",
                                    color = textSecondaryColor,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = { currentScreen = "create" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentColor
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Create",
                                            tint = Color.White
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "CREATE NEW",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            // List of created challenges
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    Text(
                                        text = "Your Challenges",
                                        color = accentColor,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                }

                                items(createdChallenges) { challenge ->
                                    HostedChallengeCard(
                                        challenge = challenge,
                                        accentColor = accentColor,
                                        cardBackgroundColor = cardBackgroundColor,
                                        textPrimaryColor = textPrimaryColor,
                                        textSecondaryColor = textSecondaryColor,
                                        onViewRequests = {
                                            currentScreen = "requests"
                                        },
                                        onManageChallenge = {
                                            // Navigate to challenge details/management
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Managing challenge: ${challenge.title}")
                                            }
                                        }
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }

                            // FAB to create new challenge
                            FloatingActionButton(
                                onClick = { currentScreen = "create" },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp),
                                containerColor = accentColor,
                                contentColor = Color.White
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create Challenge"
                                )
                            }
                        }
                    }
                }

                "requests" -> {
                    // Join Requests Screen
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        if (joinRequests.isEmpty()) {
                            // Empty state
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "No requests",
                                    tint = textSecondaryColor,
                                    modifier = Modifier.size(80.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "No pending join requests",
                                    color = textPrimaryColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "When someone requests to join your challenges, they will appear here",
                                    color = textSecondaryColor,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            // List of join requests
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    Text(
                                        text = "Join Requests",
                                        color = accentColor,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                }

                                items(joinRequests) { request ->
                                    JoinRequestCard(
                                        request = request,
                                        accentColor = accentColor,
                                        cardBackgroundColor = cardBackgroundColor,
                                        textPrimaryColor = textPrimaryColor,
                                        textSecondaryColor = textSecondaryColor,
                                        onAccept = {
                                            // Accept request using ViewModel
                                            viewModel.acceptJoinRequest(request.id)
                                        },
                                        onDecline = {
                                            // Decline request using ViewModel
                                            viewModel.declineJoinRequest(request.id)
                                        }
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

@Composable
fun HostedChallengeCard(
    challenge: HostedChallenge,
    accentColor: Color,
    cardBackgroundColor: Color,
    textPrimaryColor: Color,
    textSecondaryColor: Color,
    onViewRequests: () -> Unit,
    onManageChallenge: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with title and ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Challenge icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = accentColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Challenge",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = challenge.title,
                            color = textPrimaryColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (challenge.isPrivate) {
                            Card(
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(width = 70.dp, height = 24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF673AB7))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "PRIVATE",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ID: ",
                            color = textSecondaryColor,
                            fontSize = 14.sp
                        )

                        Text(
                            text = challenge.id,
                            color = accentColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Expand/collapse icon
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowDropDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Difficulty badge
                Card(
                    modifier = Modifier
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when(challenge.difficulty) {
                            "Beginner" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            "Intermediate" -> Color(0xFFFFA000).copy(alpha = 0.2f)
                            "Advanced" -> Color(0xFFE53935).copy(alpha = 0.2f)
                            else -> accentColor.copy(alpha = 0.2f)
                        }
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = when(challenge.difficulty) {
                            "Beginner" -> Color(0xFF4CAF50)
                            "Intermediate" -> Color(0xFFFFA000)
                            "Advanced" -> Color(0xFFE53935)
                            else -> accentColor
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = challenge.difficulty,
                        color = when(challenge.difficulty) {
                            "Beginner" -> Color(0xFF4CAF50)
                            "Intermediate" -> Color(0xFFFFA000)
                            "Advanced" -> Color(0xFFE53935)
                            else -> accentColor
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Participants indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Participants",
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "${challenge.participantCount}/${challenge.maxParticipants}",
                        color = accentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Expanded content
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // Description if available
                    if (challenge.description.isNotEmpty()) {
                        Text(
                            text = "Description:",
                            color = textSecondaryColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = challenge.description,
                            color = textPrimaryColor,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Topics
                    if (challenge.topics.isNotEmpty()) {
                        Text(
                            text = "Topics:",
                            color = textSecondaryColor,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            challenge.topics.take(4).forEach { topic ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF333333)
                                    )
                                ) {
                                    Text(
                                        text = topic,
                                        color = textPrimaryColor,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (challenge.topics.size > 4) {
                                Text(
                                    text = "+${challenge.topics.size - 4} more",
                                    color = textSecondaryColor,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(start = 4.dp)
                                )
                            }
                        }
                    }

                    // Action buttons
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // View Requests button
                        OutlinedButton(
                            onClick = onViewRequests,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = accentColor
                            ),
                            border = BorderStroke(1.dp, accentColor),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Join Requests",
                                    tint = accentColor,
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = "REQUESTS",
                                    color = accentColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Manage button
                        Button(
                            onClick = onManageChallenge,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Manage",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = "MANAGE",
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
}

@Composable
fun JoinRequestCard(
    request: JoinRequestModel,
    accentColor: Color,
    cardBackgroundColor: Color,
    textPrimaryColor: Color,
    textSecondaryColor: Color,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = accentColor.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.username,
                        color = textPrimaryColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "wants to join your challenge:",
                        color = textSecondaryColor,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Challenge info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardBackgroundColor.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, Color(0xFF555555))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = request.challengeTitle,
                            color = textPrimaryColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when(request.difficulty) {
                                    "Beginner" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                    "Intermediate" -> Color(0xFFFFA000).copy(alpha = 0.2f)
                                    "Advanced" -> Color(0xFFE53935).copy(alpha = 0.2f)
                                    else -> accentColor.copy(alpha = 0.2f)
                                }
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = when(request.difficulty) {
                                    "Beginner" -> Color(0xFF4CAF50)
                                    "Intermediate" -> Color(0xFFFFA000)
                                    "Advanced" -> Color(0xFFE53935)
                                    else -> accentColor
                                }
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = request.difficulty,
                                color = when(request.difficulty) {
                                    "Beginner" -> Color(0xFF4CAF50)
                                    "Intermediate" -> Color(0xFFFFA000)
                                    "Advanced" -> Color(0xFFE53935)
                                    else -> accentColor
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ID: ",
                            color = textSecondaryColor,
                            fontSize = 14.sp
                        )

                        Text(
                            text = request.challengeId,
                            color = accentColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Decline button
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFE53935)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFE53935)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Decline",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "DECLINE",
                            color = Color(0xFFE53935),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Accept button
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Accept",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "ACCEPT",
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