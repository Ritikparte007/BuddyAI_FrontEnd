package com.example.neuroed

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
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
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.neuroed.R
import com.example.neuroed.model.SubjectEditModel
import com.example.neuroed.model.UserInfoViewModel
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.SubjectEditRepository
import com.example.neuroed.viewmodel.SubjectEditViewModel
import com.example.neuroed.viewmodel.SubjectEditViewModelFactory
import kotlinx.coroutines.delay
import androidx.activity.ComponentActivity

// Enhanced theming matching Character Creation - same as CreateSubjectScreen
private val SubjectEditAppDarkColorScheme = darkColorScheme(
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

// Custom colors for Subject Edit
val SubjectEditImagePlaceholder = Color(0xFF2A2A2A)
val SubjectEditInputFieldBackground = Color(0xFF2A2A2A)
val SubjectEditButtonGradientStart = Color(0xFF6C63FF)
val SubjectEditButtonGradientEnd = Color(0xFF8E85FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSubjectScreenEdit(
    navController: NavController,
    subjectId: Int
) {
    // Input field states with initial values from existing subject
    var describeSubject by remember { mutableStateOf("") }
    var selectEducation by remember { mutableStateOf("") }
    var selectSubject by remember { mutableStateOf("") }
    var goals by remember { mutableStateOf("") }
    var learningType by remember { mutableStateOf("") }

    // States for date, time, and day
    var subjectDate by remember { mutableStateOf("") }
    var subjectTime by remember { mutableStateOf("") }
    var subjectDay by remember { mutableStateOf("") }

    // Profile image state
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var aiGeneratedImage by remember { mutableStateOf<String?>(null) }

    // Focus states for animated hints
    var isSubjectFocused by remember { mutableStateOf(false) }
    var isGoalsFocused by remember { mutableStateOf(false) }

    // Form validation
    var isFormValid by remember { mutableStateOf(false) }

    // State for showing success popup
    var showSuccessPopup by remember { mutableStateOf(false) }

    val userInfoViewModel: UserInfoViewModel = viewModel()
    val context = LocalContext.current

    // Observe the userId
    val userId by userInfoViewModel.userId.collectAsState()

    // Initialize network components
    val apiService = RetrofitClient.apiService
    val repository = SubjectEditRepository(apiService)
    val viewModel: SubjectEditViewModel = viewModel(
        factory = SubjectEditViewModelFactory(repository)
    )

    // State for loading subject details
    var isLoadingSubject by remember { mutableStateOf(true) }
    var subjectLoadError by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(contract = GetContent()) { uri: Uri? ->
        if (uri != null) {
            profileImageUri = uri
            showImagePickerDialog = false
        }
    }

    // Load the userId when the composable is first created
    LaunchedEffect(Unit) {
        userInfoViewModel.loadUserId(context)
        // Fetch subject details
        viewModel.getSubjectById(subjectId)
    }

    // Observe subject details
    val subjectDetails by viewModel.subject.collectAsState()

    // Observe error state and update subjectLoadError accordingly
    val errorState by viewModel.error.collectAsState()
    LaunchedEffect(errorState) {
        if (errorState != null) {
            subjectLoadError = true
            isLoadingSubject = false
        }
    }

    // Update form fields when subject details are loaded
    LaunchedEffect(subjectDetails) {
        subjectDetails?.let { subject ->
            describeSubject = subject.subjectDescription
            selectEducation = subject.education
            selectSubject = subject.subject
            goals = subject.goals
            learningType = subject.learningTypes
            subjectDate = subject.date
            subjectTime = subject.time
            subjectDay = subject.day

            // Set initial image
            if (subject.image.isNotEmpty() && subject.image != "default_image_url") {
                if (subject.image.startsWith("http")) {
                    aiGeneratedImage = subject.image
                } else {
                    try {
                        profileImageUri = Uri.parse(subject.image)
                    } catch (e: Exception) {
                        // If parsing fails, just keep null
                    }
                }
            }

            isLoadingSubject = false
        }
    }

    // Validate form whenever inputs change
    LaunchedEffect(describeSubject, selectEducation, selectSubject, goals, learningType) {
        isFormValid = describeSubject.isNotBlank() &&
                selectEducation.isNotBlank() &&
                selectSubject.isNotBlank() &&
                goals.isNotBlank() &&
                learningType.isNotBlank()
    }

    // Observe update response with success popup
    val updateResponse = viewModel.updatedSubjectResponse
    if (updateResponse != null) {
        LaunchedEffect(updateResponse) {
            // Show success popup first
            showSuccessPopup = true
            // Wait 1.5 seconds before navigating
            delay(1500)

            // Set result to indicate subject list should be refreshed
            val resultIntent = Intent()
            resultIntent.putExtra("REFRESH_SUBJECTS", true)
            (context as? ComponentActivity)?.setResult(Activity.RESULT_OK, resultIntent)

            // Navigate back to home screen
            navController.navigate("home") {
                popUpTo("createSubjectScreenEdit") { inclusive = true }
            }
        }
    }

    MaterialTheme(
        colorScheme = SubjectEditAppDarkColorScheme
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Edit Subject",
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
                if (isLoadingSubject) {
                    // Show loading state for initial subject data fetch
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = SubjectEditButtonGradientStart,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading subject details...",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        }
                    }
                } else if (subjectLoadError) {
                    // Show error state with Character Creation style
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                "Failed to load subject details",
                                color = SubjectEditAppDarkColorScheme.secondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    isLoadingSubject = true
                                    subjectLoadError = false
                                    viewModel.getSubjectById(subjectId)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(SubjectEditButtonGradientStart, SubjectEditButtonGradientEnd)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        "Retry",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Show main form with Character Creation design
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
                                        .shadow(elevation = 8.dp, shape = CircleShape, spotColor = SubjectEditButtonGradientStart)
                                        .then(
                                            if (profileImageUri != null || aiGeneratedImage != null) {
                                                Modifier.border(width = 2.dp, color = SubjectEditButtonGradientStart, shape = CircleShape)
                                            } else Modifier
                                        ),
                                    color = SubjectEditImagePlaceholder,
                                    shape = CircleShape
                                ) {
                                    // Display the selected image if available
                                    when {
                                        profileImageUri != null -> {
                                            AsyncImage(
                                                model = profileImageUri,
                                                contentDescription = "Subject Image",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        aiGeneratedImage != null -> {
                                            AsyncImage(
                                                model = aiGeneratedImage,
                                                contentDescription = "AI Generated Image",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        else -> {
                                            // Default subject placeholder
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
                                                colors = listOf(SubjectEditButtonGradientStart, SubjectEditButtonGradientEnd)
                                            )
                                        )
                                        .clickable { showImagePickerDialog = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Change Subject Image",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Tap to change subject image",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                            )

                            // Subject Description Input with animation
                            SubjectEditStyledInputField(
                                label = "Describe Subject",
                                value = describeSubject,
                                onValueChange = { describeSubject = it },
                                placeholder = "Enter subject description",
                                isFocused = isSubjectFocused,
                                onFocusChange = { isSubjectFocused = it },
                                isRequired = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Education Level Dropdown
                            SubjectEditStyledDropdownField(
                                label = "Education Level",
                                options = listOf("High School", "Undergraduate", "Postgraduate", "Other"),
                                selectedOption = selectEducation,
                                onOptionSelected = { selectEducation = it },
                                isRequired = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Subject Category Dropdown
                            SubjectEditStyledDropdownField(
                                label = "Subject Category",
                                options = listOf("Biology", "Mathematics", "Chemistry", "Physics", "Other"),
                                selectedOption = selectSubject,
                                onOptionSelected = { selectSubject = it },
                                isRequired = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Goals Input
                            SubjectEditStyledInputField(
                                label = "Learning Goals",
                                value = goals,
                                onValueChange = { goals = it },
                                placeholder = "What do you want to achieve?",
                                isFocused = isGoalsFocused,
                                onFocusChange = { isGoalsFocused = it },
                                isRequired = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Describe your learning objectives and what you hope to accomplish",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 4.dp, bottom = 16.dp)
                            )

                            // Learning Type Dropdown
                            SubjectEditStyledDropdownField(
                                label = "Learning Type",
                                options = listOf("Online", "In-Person", "Hybrid"),
                                selectedOption = learningType,
                                onOptionSelected = { learningType = it },
                                isRequired = true
                            )

                            Spacer(modifier = Modifier.height(48.dp))

                            // Update Button with Character Creation style
                            Button(
                                onClick = {
                                    val image = profileImageUri?.toString() ?: aiGeneratedImage ?: "default_image_url"
                                    val subjectData = SubjectEditModel(
                                        id = subjectId,
                                        userId = userId,
                                        image = image,
                                        subjectDescription = describeSubject,
                                        education = selectEducation,
                                        subject = selectSubject,
                                        goals = goals,
                                        learningTypes = learningType,
                                        date = subjectDate,
                                        time = subjectTime,
                                        day = subjectDay
                                    )
                                    viewModel.updateSubject(subjectData)
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
                                                    listOf(SubjectEditButtonGradientStart, SubjectEditButtonGradientEnd)
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
                                            "UPDATE SUBJECT",
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
                }

                // Simple Image Picker Dialog
                if (showImagePickerDialog) {
                    Dialog(
                        onDismissRequest = { showImagePickerDialog = false },
                        properties = DialogProperties(dismissOnClickOutside = true)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1E1E1E)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Choose Image Source",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                // Gallery option
                                ElevatedButton(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = ButtonDefaults.elevatedButtonColors(
                                        containerColor = Color(0xFF2A2A2A),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.gallery),
                                            contentDescription = "Gallery",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text("Choose from Gallery")
                                    }
                                }

                                TextButton(
                                    onClick = { showImagePickerDialog = false },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Text("Cancel", color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                // Success popup overlay matching Character Creation
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
                                        colors = listOf(SubjectEditButtonGradientStart, SubjectEditButtonGradientEnd)
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
                                            colors = listOf(SubjectEditButtonGradientStart.copy(alpha = 0.2f), Color.Transparent),
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
                                                colors = listOf(SubjectEditButtonGradientStart, SubjectEditButtonGradientEnd)
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
                                text = "Subject Updated!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Your subject has been updated successfully.",
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
                                color = SubjectEditButtonGradientStart,
                                trackColor = Color(0xFF2A2A2A)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Subject Edit styled components matching Character Creation style

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectEditStyledInputField(
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
                    color = SubjectEditAppDarkColorScheme.secondary,
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
                containerColor = SubjectEditInputFieldBackground,
                focusedBorderColor = SubjectEditButtonGradientStart,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = SubjectEditButtonGradientStart,
                focusedLabelColor = SubjectEditButtonGradientStart,
                unfocusedLabelColor = Color.Gray,
                selectionColors = TextSelectionColors(
                    handleColor = SubjectEditButtonGradientStart,
                    backgroundColor = SubjectEditButtonGradientStart.copy(alpha = 0.3f)
                )
            )
        )

        AnimatedVisibility(
            visible = isFocused,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            when (label) {
                "Describe Subject" -> {
                    Text(
                        text = "Provide a detailed description of your subject",
                        color = SubjectEditButtonGradientStart.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
                "Learning Goals" -> {
                    Text(
                        text = "What specific outcomes do you want to achieve?",
                        color = SubjectEditButtonGradientStart.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectEditStyledDropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    isRequired: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

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
                    color = SubjectEditAppDarkColorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White
                ),
                placeholder = {
                    Text(
                        text = "Select $label",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 16.sp
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = SubjectEditInputFieldBackground,
                    focusedBorderColor = SubjectEditButtonGradientStart,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = SubjectEditButtonGradientStart
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF1E1E1E))
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = Color.White
                        )
                    )
                }
            }
        }
    }
}