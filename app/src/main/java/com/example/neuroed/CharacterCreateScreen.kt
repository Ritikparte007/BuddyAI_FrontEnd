package com.example.neuroed

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.neuroed.NeuroEdApp.Companion.INVALID_USER_ID
import com.example.neuroed.model.CharacterCreate
import com.example.neuroed.model.UserInfoViewModel
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.CharacterCreateRepository
import com.example.neuroed.viewmodel.CharacterCreateViewModel
import com.example.neuroed.viewmodel.CharacterCreateViewModelFactory
import kotlinx.coroutines.delay

// Enhanced theming with a more vibrant primary color and matching secondary colors
private val AppDarkColorScheme = darkColorScheme(
    primary = Color(0xFF6C63FF),          // Vibrant purple
    secondary = Color(0xFFAC6262),        // Accent red
    tertiary = Color(0xFF3E8CF1),         // Accent blue
    background = Color(0xFF121212),       // Deep dark background
    surface = Color(0xFF1E1E1E),          // Slightly lighter surface
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

// Custom colors
val ProfileImagePlaceholder = Color(0xFF2A2A2A)
val InputFieldBackground = Color(0xFF2A2A2A)
val ButtonGradientStart = Color(0xFF6C63FF)
val ButtonGradientEnd = Color(0xFF8E85FF)
val SwitchActiveColor = Color(0xFF6C63FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreateScreen(
    navController: NavController,
    userId: String? = null // Add userId parameter from navigation
) {
    val context = LocalContext.current

    // Get UserInfoViewModel to fetch current user ID
    val userInfoViewModel: UserInfoViewModel = viewModel()
    val userIdFromViewModel by userInfoViewModel.userId.collectAsState()

    // Load userId when composable starts
    LaunchedEffect(Unit) {
        userInfoViewModel.loadUserId(context)
    }

    // Determine the actual user ID to use - NO DEFAULT FALLBACK
    val actualUserId = remember(userId, userIdFromViewModel) {
        when {
            // 1. Use navigation parameter if available and valid
            userId != null && userId != "null" && userId.isNotEmpty() -> userId.toIntOrNull()
            // 2. Use ViewModel user ID if valid
            userIdFromViewModel != INVALID_USER_ID -> userIdFromViewModel
            // 3. Get from SharedPreferences as fallback
            else -> {
                val sharedPrefs = context.getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE)
                val storedUserId = sharedPrefs.getInt("userInfoId", INVALID_USER_ID)
                if (storedUserId != INVALID_USER_ID) {
                    storedUserId
                } else {
                    null // NO DEFAULT
                }
            }
        }
    }

    // If no valid user ID found, show error or redirect to login
    if (actualUserId == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "User not authenticated",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Please login to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        navController.navigate("SignUpScreen") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Login")
                }
            }
        }
        return
    }

    // Create an instance of your ApiService first.
    val apiService = RetrofitClient.apiService
    val repository = CharacterCreateRepository(apiService)
    val factory = CharacterCreateViewModelFactory(repository)

    // Use the Compose viewModel() API to obtain your ViewModel instance.
    val viewModel: CharacterCreateViewModel = viewModel(factory = factory)
    val characterResponse by viewModel.characterCreateResponse.observeAsState()

    // State for showing success popup
    var showSuccessPopup by remember { mutableStateOf(false) }

    // Modified LaunchedEffect to show popup before navigating
    LaunchedEffect(characterResponse) {
        characterResponse?.let { response ->
            if (response.response == "success") {
                // Show popup first
                showSuccessPopup = true
                // Wait 1.5 seconds before navigating to home
                delay(1500)
                navController.navigate("home")
            }
        }
    }

    // States for text fields and switches
    var characterName by remember { mutableStateOf("") }
    var instruction1 by remember { mutableStateOf("") }
    var instruction2 by remember { mutableStateOf("") }
    var firstMessage by remember { mutableStateOf("") }
    var memorySwitch by remember { mutableStateOf(false) }
    var privateSwitch by remember { mutableStateOf(false) }
    var isNameFocused by remember { mutableStateOf(false) }
    var isFormValid by remember { mutableStateOf(false) }

    // State to store the selected image URI (initially null)
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Validate form whenever inputs change
    LaunchedEffect(characterName, instruction1) {
        isFormValid = characterName.isNotBlank() && instruction1.isNotBlank()
    }

    // Launcher for file explorer using the GetContent contract
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    MaterialTheme(
        colorScheme = AppDarkColorScheme
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Create Character",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF121212),
                        scrolledContainerColor = Color(0xFF121212),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            containerColor = Color(0xFF121212)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF121212)),
                    color = Color(0xFF121212)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Display current user info
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = AppDarkColorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "User",
                                    tint = AppDarkColorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Creating as User ID: $actualUserId",
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Enhanced profile avatar area with animation and better shadows
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Circular background with animated border when image is selected
                            Surface(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .shadow(elevation = 8.dp, shape = CircleShape, spotColor = ButtonGradientStart)
                                    .then(
                                        if (imageUri != null) {
                                            Modifier.border(width = 2.dp, color = ButtonGradientStart, shape = CircleShape)
                                        } else Modifier
                                    ),
                                color = ProfileImagePlaceholder,
                                shape = CircleShape
                            ) {
                                // Display the selected image if available
                                if (imageUri != null) {
                                    AsyncImage(
                                        model = imageUri,
                                        contentDescription = "Profile Image",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    // Camera icon as placeholder
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.AddCircle,
                                            contentDescription = "Add Photo",
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }
                            }

                            // "Add" button overlay with gradient background
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 6.dp, y = 6.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(ButtonGradientStart, ButtonGradientEnd)
                                        )
                                    )
                                    .clickable { launcher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Profile Image",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Text(
                            text = "Choose a photo for your character",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                        )

                        // Character Name Input with animation
                        StyledInputField(
                            label = "Character Name",
                            value = characterName,
                            onValueChange = { characterName = it },
                            placeholder = "Enter a unique name",
                            isFocused = isNameFocused,
                            onFocusChange = { isNameFocused = it },
                            isRequired = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Primary Instruction Input
                        StyledInputField(
                            label = "Primary Instruction",
                            value = instruction1,
                            onValueChange = { instruction1 = it },
                            placeholder = "Describe your character's personality",
                            isRequired = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Give your character clear instructions on how they should behave",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, bottom = 16.dp)
                        )

                        // Secondary Instruction Input
                        StyledInputField(
                            label = "Secondary Instruction",
                            value = instruction2,
                            onValueChange = { instruction2 = it },
                            placeholder = "Add additional details or behaviors"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // First Message Input
                        StyledInputField(
                            label = "First Message",
                            value = firstMessage,
                            onValueChange = { firstMessage = it },
                            placeholder = "How should your character start the conversation?"
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This message will be sent when someone first chats with your character",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, bottom = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Section divider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "OPTIONS",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Enhanced switches with descriptions
                        SwitchRowWithDescription(
                            label = "Memory",
                            description = "Character will remember past conversations",
                            checked = memorySwitch,
                            onCheckedChange = { memorySwitch = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SwitchRowWithDescription(
                            label = "Private",
                            description = "Only you can access this character",
                            checked = privateSwitch,
                            onCheckedChange = { privateSwitch = it }
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        // Save Button that sends all the inputs to the backend
                        Button(
                            onClick = {
                                // Create the CharacterCreate model with the authenticated user id
                                val character = CharacterCreate(
                                    UserId = actualUserId, // Use authenticated user ID
                                    name = characterName,
                                    instruction1 = instruction1,
                                    instruction2 = instruction2,
                                    firstMessage = firstMessage,
                                    memory = memorySwitch,
                                    isPrivate = privateSwitch
                                )
                                viewModel.createCharacter(character)
                            },
                            enabled = isFormValid,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = if (isFormValid) {
                                                listOf(ButtonGradientStart, ButtonGradientEnd)
                                            } else {
                                                listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.3f))
                                            }
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isFormValid) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(
                                        "SAVE CHARACTER",
                                        color = if (isFormValid) Color.White else Color.White.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Success popup overlay
                AnimatedVisibility(
                    visible = showSuccessPopup,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x99000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(32.dp)
                                .width(300.dp)
                                .shadow(elevation = 16.dp, shape = RoundedCornerShape(16.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFF1E1E1E), Color(0xFF252525))
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(ButtonGradientStart, ButtonGradientEnd)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Success icon with circular background
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(ButtonGradientStart.copy(alpha = 0.2f), Color.Transparent),
                                            radius = 80f
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(ButtonGradientStart, ButtonGradientEnd)
                                            ),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Success",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Character Created!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Your character has been created successfully.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Progress indicator to show it's about to navigate away
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = ButtonGradientStart,
                                trackColor = Color(0xFF2A2A2A)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isFocused: Boolean = false,
    onFocusChange: (Boolean) -> Unit = {},
    isRequired: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        ) {
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            if (isRequired) {
                Text(
                    text = " *",
                    color = AppDarkColorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = Color.White
            ),
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 16.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { state -> onFocusChange(state.isFocused) },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = InputFieldBackground,
                focusedBorderColor = ButtonGradientStart,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = ButtonGradientStart,
                focusedLabelColor = ButtonGradientStart,
                unfocusedLabelColor = Color.Gray,
                selectionColors = TextSelectionColors(
                    handleColor = ButtonGradientStart,
                    backgroundColor = ButtonGradientStart.copy(alpha = 0.3f)
                )
            )
        )

        AnimatedVisibility(
            visible = isFocused,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            if (label == "Character Name") {
                Text(
                    text = "Choose a unique and memorable name",
                    color = ButtonGradientStart.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SwitchRowWithDescription(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SwitchActiveColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                )
            )
        }
        Text(
            text = description,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}