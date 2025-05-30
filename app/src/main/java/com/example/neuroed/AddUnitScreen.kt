package com.example.neuroed

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.neuroed.model.SubjectSyllabusSaveModel
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.SubjectSyllabusSaveRepository
import com.example.neuroed.viewmodel.LlmExtractViewModel
import com.example.neuroed.viewmodel.LlmExtractViewModelFactory
import com.example.neuroed.viewmodel.SubjectSyllabusSaveViewModel
import com.example.neuroed.viewmodel.SubjectSyllabusSaveViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Custom dark theme colors reused in your project
private val PurpleAccent = Color(0xFFBB86FC)
private val PinkDelete = Color(0xFFFF5370)
private val DarkBackground = Color(0xFF121212)
private val DarkCard = Color(0xFF1E1E1E)
private val LightTextColor = Color(0xFFD0D0D0)
private val PremiumGold = Color(0xFFFFD700)

enum class InputMode {
    MANUAL, CAMERA, PDF, OTHER_FILE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUnitScreen(
    navController: NavController,
    id: Int,
    existingUnits: Set<Int> = emptySet()
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // UI State
    val unitOptions = listOf("Unit 1", "Unit 2", "Unit 3", "Unit 4", "Unit 5", "Unit 6", "Unit 7 (Premium)", "Unit 8 (Premium)", "Unit 9 (Premium)", "Unit 10 (Premium)")
    var expanded by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var syllabusInput by remember { mutableStateOf("") }
    var promptInput by remember { mutableStateOf("Extract important topics and key concepts") }
    var currentInputMode by remember { mutableStateOf(InputMode.MANUAL) }
    var isProcessing by remember { mutableStateOf(false) }
    var showPromptDialog by remember { mutableStateOf(false) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var showFilePickerOptions by remember { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }
    var showTitleRequiredDialog by remember { mutableStateOf(false) }

    // Add state for error message and visibility
    var errorMessageState by remember { mutableStateOf<String?>(null) }
    var showErrorSnackbar by remember { mutableStateOf(false) }

    // Check if the selected unit requires premium


    // Function to check if title is entered before allowing file/camera
    fun checkTitleBeforeAction(action: () -> Unit) {
        if (titleInput.isBlank()) {
            showTitleRequiredDialog = true
        } else {
            action()
        }
    }

    // Setup API and ViewModels
    val apiService = RetrofitClient.apiService
    val repository = SubjectSyllabusSaveRepository(apiService)
    val saveViewModel: SubjectSyllabusSaveViewModel = viewModel(
        factory = SubjectSyllabusSaveViewModelFactory(repository)
    )

    // Create LLM ViewModel to handle text extraction
    val llmViewModel: LlmExtractViewModel = viewModel(
        factory = LlmExtractViewModelFactory()
    )
    val extractedText by llmViewModel.extractedText.observeAsState()
    val llmErrorMessage by llmViewModel.errorMessage.observeAsState()
    val isLlmLoading by llmViewModel.isLoading.observeAsState(false)

    // In AddUnitScreen





    // Check if camera permission is already granted
    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Define all activity result launchers
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Image captured successfully
            showPromptDialog = true
            currentInputMode = InputMode.CAMERA
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedFileUri = it
            showPromptDialog = true
            currentInputMode = InputMode.PDF
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedFileUri = it
            showPromptDialog = true
            currentInputMode = InputMode.OTHER_FILE
        }
    }

    // Camera permission request launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            // Now that we have permission, create URI for camera and launch
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/NeuroeD")
                }
            }

            val imageUri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            imageUri?.let {
                selectedFileUri = it
                cameraLauncher.launch(it)
            }
        }
    }

    // Effect to update syllabus when extraction is complete
    LaunchedEffect(extractedText) {
        extractedText?.let {
            syllabusInput = it
            isProcessing = false
        }
    }

    // Effect to handle LLM errors
    LaunchedEffect(llmErrorMessage) {
        llmErrorMessage?.let {
            errorMessageState = it
            showErrorSnackbar = true
            isProcessing = false
        }
    }

    // Effect to sync loading states
    LaunchedEffect(isLlmLoading) {
        isProcessing = isLlmLoading
    }


    var isSaving by remember { mutableStateOf(false) }

// Update the LaunchedEffect for saveResponse
    val saveResponse by saveViewModel.saveResponse.observeAsState()
    LaunchedEffect(saveResponse) {
        saveResponse?.let { response ->
            // Set the flag to trigger a refresh when returning to SyllabusScreen
            navController.previousBackStackEntry?.savedStateHandle?.set("REFRESH_NEEDED", true)

            // Reset loading state
            isSaving = false

            // Show success message and navigate back
            navController.navigateUp()
        }
    }


    // REMOVE this first declaration
// var selectedUnit by remember { mutableStateOf(unitOptions.first()) } <- REMOVE THIS

// KEEP only this one, but move it BEFORE the filter
    var selectedUnit by remember {
        mutableStateOf(
            unitOptions.firstOrNull { !it.contains("Premium") && it.split(" ").getOrNull(1)?.toIntOrNull() !in existingUnits }
                ?: unitOptions.firstOrNull { !it.contains("Premium") }
                ?: unitOptions.first()
        )
    }

    val isPremiumUnit = selectedUnit.contains("Premium")

// Then define availableUnitOptions based on your existing units
    val availableUnitOptions = unitOptions.filter { unitOption ->
        val unitNumber = unitOption.split(" ").getOrNull(1)?.toIntOrNull()
        unitNumber == null || unitNumber !in existingUnits || unitOption.contains("Premium")
    }

    // Material 3 Theming
    val colorScheme = darkColorScheme(
        primary = PurpleAccent,
        onPrimary = Color.White,
        background = DarkBackground,
        onBackground = LightTextColor,
        surface = DarkCard,
        onSurface = LightTextColor,
        error = PinkDelete
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Add New Unit") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.attachfile),
                                contentDescription = "Back",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkCard,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Main content with vertical scrolling
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                ) {
                    // Unit selection dropdown
                    Text(
                        "Select Unit",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedUnit,
                            onValueChange = { /* Handled by dropdown */ },
                            readOnly = true,
                            label = { Text("Select Unit") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "Dropdown Arrow",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                        )

                        // Transparent clickable box over the TextField to open dropdown
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    onClick = { expanded = true }
                                )
                                .background(Color.Transparent)
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            availableUnitOptions.forEachIndexed { index, unit ->
                                // Only show available options
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = unit,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (unit.contains("Premium")) PremiumGold else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (unit.contains("Premium")) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Filled.Star,
                                                    contentDescription = "Premium",
                                                    tint = PremiumGold,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        if (unit.contains("Premium")) {
                                            showPremiumDialog = true
                                        } else {
                                            selectedUnit = unit
                                        }
                                        expanded = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title input field
                    Text(
                        "Chapter Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    var showTitleError by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = {
                            titleInput = it
                            // Clear error state when user types something
                            if (it.isNotBlank()) {
                                showTitleError = false
                            }
                        },
                        label = { Text("Chapter Title") },
                        modifier = Modifier.fillMaxWidth(),
                        // Only show error if explicitly triggered
                        isError = showTitleError && titleInput.isBlank(),
                        supportingText = {
                            if (showTitleError && titleInput.isBlank()) {
                                Text(
                                    "Chapter title is required before using camera or files",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )

                    fun checkTitleBeforeAction(action: () -> Unit) {
                        if (titleInput.isBlank()) {
                            showTitleError = true
                            showTitleRequiredDialog = true
                        } else {
                            action()
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input Method Selector Cards
                    Text(
                        "Choose Input Method",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Manual Input Option
                        InputMethodCard(
                            icon = R.drawable.pen,
                            title = "Manual",
                            isSelected = currentInputMode == InputMode.MANUAL,
                            onClick = {
                                // Manual entry doesn't require title validation
                                currentInputMode = InputMode.MANUAL
                            }
                        )

                        // Camera Option
                        InputMethodCard(
                            icon = R.drawable.camera,
                            title = "Camera",
                            isSelected = currentInputMode == InputMode.CAMERA,
                            onClick = {
                                // Check if title is entered before proceeding
                                checkTitleBeforeAction {
                                    // Request camera permission and launch camera intent
                                    if (hasCameraPermission) {
                                        // We already have permission, create URI and launch camera
                                        val timeStamp = SimpleDateFormat(
                                            "yyyyMMdd_HHmmss",
                                            Locale.getDefault()
                                        ).format(Date())
                                        val imageFileName = "JPEG_${timeStamp}_"

                                        val contentValues = ContentValues().apply {
                                            put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName)
                                            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                put(
                                                    MediaStore.MediaColumns.RELATIVE_PATH,
                                                    "Pictures/NeuroeD"
                                                )
                                            }
                                        }

                                        val imageUri = context.contentResolver.insert(
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                            contentValues
                                        )

                                        imageUri?.let {
                                            selectedFileUri = it
                                            cameraLauncher.launch(it)
                                        }
                                    } else {
                                        // Request permission
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            }
                        )

                        // PDF Option
                        InputMethodCard(
                            icon = R.drawable.pdffile,
                            title = "PDF",
                            isSelected = currentInputMode == InputMode.PDF,
                            onClick = {
                                // Check if title is entered before proceeding
                                checkTitleBeforeAction {
                                    pdfPickerLauncher.launch("application/pdf")
                                }
                            }
                        )

                        // Other File Option
                        InputMethodCard(
                            icon = R.drawable.attachfile,
                            title = "File",
                            isSelected = currentInputMode == InputMode.OTHER_FILE,
                            onClick = {
                                // Check if title is entered before proceeding
                                checkTitleBeforeAction {
                                    showFilePickerOptions = true
                                }
                            }
                        )
                    }

                    // Show different UI based on the selected input mode
                    when (currentInputMode) {
                        InputMode.MANUAL -> {
                            // Standard syllabus input field
                            OutlinedTextField(
                                value = syllabusInput,
                                onValueChange = { syllabusInput = it },
                                label = { Text("Syllabus Content") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(350.dp),
                                maxLines = 15
                            )
                        }

                        else -> {
                            // For camera, PDF, or other file modes
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(500.dp), // Increased the overall card height
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Input mode indicator
                                    Text(
                                        text = when (currentInputMode) {
                                            InputMode.CAMERA -> "Camera Input"
                                            InputMode.PDF -> "PDF Input"
                                            else -> "File Input"
                                        },
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    // Display the file name or image preview
                                    selectedFileUri?.let {
                                        Text(
                                            text = "Selected: ${it.lastPathSegment ?: "file"}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }

                                    // Display prompt field
                                    OutlinedTextField(
                                        value = promptInput,
                                        onValueChange = { promptInput = it },
                                        label = { Text("Extraction Prompt") },
                                        placeholder = { Text("e.g., Extract important topics and key concepts") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Extract button
                                    Button(
                                        onClick = {
                                            isProcessing = true
                                            coroutineScope.launch {
                                                // Process the file with LLM based on the prompt
                                                llmViewModel.extractTextFromFile(
                                                    fileUri = selectedFileUri,
                                                    prompt = promptInput,
                                                    inputMode = currentInputMode,
                                                    context = context
                                                )
                                            }
                                        },
                                        enabled = !isProcessing && selectedFileUri != null,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    ) {
                                        if (isProcessing) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        } else {
                                            Text("Extract Content")
                                        }
                                    }

                                    // Preview of extracted content
                                    if (!isProcessing && syllabusInput.isNotEmpty()) {
                                        Text(
                                            text = "Extracted Content Preview:",
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.padding(top = 16.dp)
                                        )

                                        // Fixed preview area to take most of the remaining space
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(280.dp) // Fixed larger height
                                                .weight(
                                                    1f,
                                                    fill = true
                                                ), // Take available space with weight
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            // Use Box with scrollable Text for more efficient layout
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    text = syllabusInput,
                                                    style = MaterialTheme.typography.bodyMedium, // Larger text
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .verticalScroll(rememberScrollState())
                                                )
                                            }
                                        }
                                    } else if (isProcessing) {
                                        // Loading indicator with text - match the height of content preview
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(280.dp) // Match the content preview height
                                                .weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            CircularProgressIndicator()
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                "Processing with AI...",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit button with loading indicator
                    // Submit button with loading indicator
                    Button(
                        onClick = {
                            // Prevent saving premium units
                            if (isPremiumUnit) {
                                showPremiumDialog = true
                                return@Button
                            }

                            // Extract unit number from selected unit
                            val unitNumber = selectedUnit.split(" ").getOrNull(1)?.toIntOrNull() ?: 1

                            // Create data model for saving
                            val model = SubjectSyllabusSaveModel(
                                subjectId = id.toString(),
                                subjectUnit = unitNumber,
                                subjectChapterName = titleInput,
                                subjectSyllabus = syllabusInput
                            )

                            // Set saving state to true
                            isSaving = true

                            // Save to database
                            saveViewModel.saveSubjectSyllabus(model)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        // Here use the local isSaving state instead of viewModel property
                        enabled = titleInput.isNotEmpty() && syllabusInput.isNotEmpty() && !isSaving,
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPremiumUnit) PremiumGold else MaterialTheme.colorScheme.primary,
                            contentColor = if (isPremiumUnit) Color.Black else MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (isSaving) {
                            // Show loading indicator inside the button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = if (isPremiumUnit) Color.Black else MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Saving...")
                            }
                        } else {
                            Text(if (isPremiumUnit) "Upgrade to Premium" else "Save Unit")
                        }
                    }
                }

                // Show loading overlay when processing
                if (isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .width(200.dp)
                                .height(150.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Processing...",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }

                // FIX: Error snackbar now properly dismisses when button is clicked
                if (showErrorSnackbar) {
                    Snackbar(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomCenter),
                        action = {
                            TextButton(
                                onClick = {
                                    // Fix: Clear the error message and hide the snackbar
                                    showErrorSnackbar = false
                                    errorMessageState = null
                                }
                            ) {
                                Text("Dismiss")
                            }
                        }
                    ) {
                        Text(errorMessageState ?: "An error occurred")
                    }
                }
            }

            // Title Required Dialog
            if (showTitleRequiredDialog) {
                AlertDialog(
                    onDismissRequest = { showTitleRequiredDialog = false },
                    title = { Text("Chapter Title Required") },
                    text = {
                        Text(
                            "Please enter a chapter title before using the camera or file options.",
                            textAlign = TextAlign.Center
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { showTitleRequiredDialog = false }
                        ) {
                            Text("OK")
                        }
                    }
                )
            }

            // Premium Feature Dialog
            if (showPremiumDialog) {
                AlertDialog(
                    onDismissRequest = { showPremiumDialog = false },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Premium",
                                tint = PremiumGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Premium Feature", color = PremiumGold)
                        }
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Units 7-10 are available only for premium users.",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                "Upgrade now to unlock additional units and premium features!",
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showPremiumDialog = false
                                // Here you would redirect to premium subscription page
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PremiumGold,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Upgrade to Premium")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPremiumDialog = false }) {
                            Text("Maybe Later")
                        }
                    }
                )
            }

            // File picker options dialog
            if (showFilePickerOptions) {
                AlertDialog(
                    onDismissRequest = { showFilePickerOptions = false },
                    title = { Text("Select File Type") },
                    text = {
                        Column {
                            ListItem(
                                headlineContent = { Text("Document (.doc, .docx)") },
                                leadingContent = {
                                    Image(
                                        painter = painterResource(id = R.drawable.description),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                modifier = Modifier.clickable {
                                    filePickerLauncher.launch("application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                                    showFilePickerOptions = false
                                }
                            )
                            ListItem(
                                headlineContent = { Text("Text (.txt)") },
                                leadingContent = {
                                    Image(
                                        painter = painterResource(id = R.drawable.search),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                modifier = Modifier.clickable {
                                    filePickerLauncher.launch("text/plain")
                                    showFilePickerOptions = false
                                }
                            )
                            ListItem(
                                headlineContent = { Text("Any File") },
                                leadingContent = {
                                    Image(
                                        painter = painterResource(id = R.drawable.description),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                modifier = Modifier.clickable {
                                    filePickerLauncher.launch("*/*")
                                    showFilePickerOptions = false
                                }
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showFilePickerOptions = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Prompt dialog after selecting file/camera
            if (showPromptDialog) {
                AlertDialog(
                    onDismissRequest = { showPromptDialog = false },
                    title = { Text("Extraction Settings") },
                    text = {
                        Column {
                            Text(
                                "What should be extracted from the ${
                                    when (currentInputMode) {
                                        InputMode.CAMERA -> "image"
                                        InputMode.PDF -> "PDF"
                                        else -> "file"
                                    }
                                }?",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            OutlinedTextField(
                                value = promptInput,
                                onValueChange = { promptInput = it },
                                label = { Text("Prompt") },
                                placeholder = { Text("e.g., Extract important topics and key concepts") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                isProcessing = true
                                showPromptDialog = false

                                // Process the file with LLM based on the prompt
                                llmViewModel.extractTextFromFile(
                                    fileUri = selectedFileUri,
                                    prompt = promptInput,
                                    inputMode = currentInputMode,
                                    context = context
                                )
                            }
                        ) {
                            Text("Extract")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPromptDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun InputMethodCard(
    icon: Int,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .height(90.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = title,
                modifier = Modifier
                    .size(24.dp)
                    .alpha(if (isSelected) 1f else 0.7f),
                colorFilter = if (isSelected)
                    ColorFilter.tint(MaterialTheme.colorScheme.primary)
                else
                    null
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}