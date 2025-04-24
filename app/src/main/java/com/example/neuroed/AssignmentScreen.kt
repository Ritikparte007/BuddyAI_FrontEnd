package com.example.neuroed

import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CheckCircle
//import androidx.compose.material.icons.outlined.CloudUpload
//import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Enhanced color scheme for dark mode
object DarkThemeColors {
    val background = Color(0xFF121212)
    val surface = Color(0xFF1E1E1E)
    val surfaceVariant = Color(0xFF2D2D2D)
    val primary = Color(0xFF03DAC6)
    val primaryVariant = Color(0xFF018786)
    val secondary = Color(0xFFBB86FC)
    val error = Color(0xFFCF6679)
    val success = Color(0xFF4CAF50)
    val warning = Color(0xFFFFB74D)
    val textPrimary = Color(0xFFEEEEEE)
    val textSecondary = Color(0xFFAAAAAA)
    val divider = Color(0xFF2C2C2C)
}

/**
 * Enhanced data model for assignments with additional fields
 */
data class AssignmentItem(
    val id: String,
    val subject: String,
    val title: String,
    val description: String,
    val date: String,
    val status: AssignmentStatus,
    val fileRequired: Boolean = true,
    val supportedFileTypes: List<String> = listOf("image/*", "application/pdf")
)

enum class AssignmentStatus {
    PENDING, SUCCESSFUL, MISSING
}

enum class SubmissionState {
    IDLE, UPLOADING, ANALYZING, SUCCESS, ERROR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()

    // Assignment data
    val assignmentList = remember {
        listOf(
            AssignmentItem("1", "Biology", "DNA Structure Analysis", "Analyze the given DNA structure and identify key components", "12/12/25", AssignmentStatus.PENDING),
            AssignmentItem("2", "Physics", "Newton's Laws Application", "Apply Newton's laws to solve the given problems", "15/12/25", AssignmentStatus.PENDING),
            AssignmentItem("3", "Chemistry", "Chemical Reactions Lab", "Upload your observations from the lab experiment", "10/12/25", AssignmentStatus.PENDING),
            AssignmentItem("4", "Mathematics", "Differential Equations", "Solve the given set of differential equations", "18/12/25", AssignmentStatus.PENDING),
            AssignmentItem("5", "Computer Science", "Algorithm Analysis", "Analyze time complexity of the provided algorithms", "20/12/25", AssignmentStatus.PENDING)
        )
    }

    // UI State
    val tabs = listOf("Pending", "Successful", "Missing")
    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedAssignment by remember { mutableStateOf<AssignmentItem?>(null) }
    var showSubmissionDialog by remember { mutableStateOf(false) }
    var showSuccessPopup by remember { mutableStateOf(false) }
    var submissionState by remember { mutableStateOf(SubmissionState.IDLE) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var answerText by remember { mutableStateOf("") }

    // File picker launcher
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            // If we have all we need, we can auto-proceed to submission
            if (answerText.isNotBlank()) {
                submissionState = SubmissionState.UPLOADING
            }
        }
    }

    // Main scaffold with top app bar
    Scaffold(
        containerColor = DarkThemeColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Assignment Assistant",
                        color = DarkThemeColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkThemeColors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkThemeColors.background
                ),
                actions = {
                    IconButton(onClick = { /* Show help or settings */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = DarkThemeColors.textPrimary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(DarkThemeColors.background)
        ) {
            // Upload area with pulsating animation
            UploadArea(
                onUploadClick = {
                    selectedAssignment?.let {
                        fileLauncher.launch(it.supportedFileTypes.firstOrNull() ?: "*/*")
                    } ?: run {
                        // Show a message to select an assignment first
                        coroutineScope.launch {
                            // We might show a snackbar or temporary message here
                        }
                    }
                },
                fileUri = selectedFileUri,
                hasAssignmentSelected = selectedAssignment != null
            )

            // Status indicator for pending assignments
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = DarkThemeColors.background,
                contentColor = DarkThemeColors.primary,
                divider = { Divider(color = DarkThemeColors.divider) }
            ) {
                tabs.forEachIndexed { index, text ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            if (text == "Pending") {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = DarkThemeColors.error,
                                            contentColor = DarkThemeColors.textPrimary
                                        ) {
                                            Text(text = "5")
                                        }
                                    }
                                ) {
                                    Text(
                                        text = text,
                                        color = if (selectedTabIndex == index)
                                            DarkThemeColors.primary else DarkThemeColors.textSecondary
                                    )
                                }
                            } else {
                                Text(
                                    text = text,
                                    color = if (selectedTabIndex == index)
                                        DarkThemeColors.primary else DarkThemeColors.textSecondary
                                )
                            }
                        }
                    )
                }
            }

            // Filter assignments based on selected tab
            val filteredList = when (selectedTabIndex) {
                0 -> assignmentList.filter { it.status == AssignmentStatus.PENDING }
                1 -> assignmentList.filter { it.status == AssignmentStatus.SUCCESSFUL }
                2 -> assignmentList.filter { it.status == AssignmentStatus.MISSING }
                else -> assignmentList
            }

            // Display assignments
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList) { item ->
                    EnhancedAssignmentCard(
                        assignment = item,
                        isSelected = selectedAssignment?.id == item.id,
                        onClick = {
                            selectedAssignment = if (selectedAssignment?.id == item.id) null else item
                            // Reset the file if we select a different assignment
                            if (selectedAssignment?.id != item.id) {
                                selectedFileUri = null
                            }
                        },
                        onSubmitClick = {
                            if (selectedFileUri != null || !item.fileRequired) {
                                showSubmissionDialog = true
                            } else {
                                fileLauncher.launch(item.supportedFileTypes.firstOrNull() ?: "*/*")
                            }
                        }
                    )
                }
            }
        }
    }

    // Assignment submission dialog
    if (showSubmissionDialog) {
        SubmissionDialog(
            assignment = selectedAssignment!!,
            fileUri = selectedFileUri,
            answerText = answerText,
            onAnswerChange = { answerText = it },
            onDismiss = { showSubmissionDialog = false },
            onSubmit = {
                showSubmissionDialog = false
                submissionState = SubmissionState.UPLOADING

                // Simulate submission process
                coroutineScope.launch {
                    // Upload phase
                    delay(1500)
                    submissionState = SubmissionState.ANALYZING

                    // Analysis phase
                    delay(2500)
                    submissionState = SubmissionState.SUCCESS
                    showSuccessPopup = true

                    // Reset after a delay
                    delay(3000)
                    showSuccessPopup = false
                    submissionState = SubmissionState.IDLE
                    selectedFileUri = null
                }
            }
        )
    }

    // Success popup with analysis results
    if (showSuccessPopup) {
        SuccessDialog(
            onDismiss = { showSuccessPopup = false }
        )
    }

    // Loading overlay during submission process
    if (submissionState != SubmissionState.IDLE) {
        SubmissionOverlay(state = submissionState)
    }
}

@Composable
fun UploadArea(
    onUploadClick: () -> Unit,
    fileUri: Uri?,
    hasAssignmentSelected: Boolean
) {
    // Animation for pulsing effect
    val pulsateTransition = rememberInfiniteTransition()
    val scale by pulsateTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Gradient animation
    val gradientColors by pulsateTransition.animateColor(
        initialValue = DarkThemeColors.primaryVariant,
        targetValue = DarkThemeColors.primary,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkThemeColors.surfaceVariant,
                        DarkThemeColors.surface
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        gradientColors.copy(alpha = 0.5f),
                        gradientColors.copy(alpha = 0.2f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = hasAssignmentSelected) { onUploadClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (fileUri != null) {
                // Show file selected indicator
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "File Selected",
                    tint = DarkThemeColors.success,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "File Selected",
                    color = DarkThemeColors.textPrimary,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                // Upload icon with pulsate effect if assignment is selected
                Icon(
                    imageVector = Icons.Outlined.Build,
                    contentDescription = "Upload File",
                    tint = if (hasAssignmentSelected) DarkThemeColors.primary else DarkThemeColors.textSecondary,
                    modifier = Modifier
                        .size(60.dp)
                        .alpha(if (hasAssignmentSelected) 1f else 0.6f)
                        .then(if (hasAssignmentSelected) Modifier.scale(scale) else Modifier)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (hasAssignmentSelected)
                        "Upload Assignment File"
                    else
                        "Select an assignment first",
                    color = if (hasAssignmentSelected)
                        DarkThemeColors.textPrimary
                    else
                        DarkThemeColors.textSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Images, PDFs, and Documents",
                color = DarkThemeColors.textSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun EnhancedAssignmentCard(
    assignment: AssignmentItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onSubmitClick: () -> Unit
) {
    val cardColor = if (isSelected)
        DarkThemeColors.surfaceVariant.copy(alpha = 0.8f)
    else
        DarkThemeColors.surface

    val borderColor = if (isSelected)
        DarkThemeColors.primary
    else
        DarkThemeColors.divider

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Subject chip
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(DarkThemeColors.secondary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = assignment.subject,
                            style = MaterialTheme.typography.labelMedium,
                            color = DarkThemeColors.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Title
                    Text(
                        text = assignment.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkThemeColors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Description (conditionally shown if selected)
                    AnimatedVisibility(visible = isSelected) {
                        Text(
                            text = assignment.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkThemeColors.textSecondary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    // Due date
                    Text(
                        text = "Due: ${assignment.date}",
                        style = MaterialTheme.typography.labelMedium,
                        color = DarkThemeColors.textSecondary
                    )

                    // Status indicator dot
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(12.dp)
                            .background(
                                when (assignment.status) {
                                    AssignmentStatus.PENDING -> DarkThemeColors.warning
                                    AssignmentStatus.SUCCESSFUL -> DarkThemeColors.success
                                    AssignmentStatus.MISSING -> DarkThemeColors.error
                                },
                                CircleShape
                            )
                    )
                }
            }

            // Submit button (only appears if selected)
            AnimatedVisibility(visible = isSelected) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onSubmitClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkThemeColors.primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Submit",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Submit Response")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionDialog(
    assignment: AssignmentItem,
    fileUri: Uri?,
    answerText: String,
    onAnswerChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkThemeColors.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Submit Assignment",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DarkThemeColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = assignment.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkThemeColors.textPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = assignment.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkThemeColors.textSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // File selection status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            DarkThemeColors.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    if (fileUri != null) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "File Attached",
                            tint = DarkThemeColors.success
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "File attached",
                            color = DarkThemeColors.textPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else if (assignment.fileRequired) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "File Required",
                            tint = DarkThemeColors.warning
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "File required for submission",
                            color = DarkThemeColors.warning,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "No File",
                            tint = DarkThemeColors.textSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "No file attached (optional)",
                            color = DarkThemeColors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Answer text field
                OutlinedTextField(
                    value = answerText,
                    onValueChange = onAnswerChange,
                    label = { Text("Your answer") },
                    placeholder = { Text("Provide your answer here...") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
//                        textColor = DarkThemeColors.textPrimary,
                        focusedBorderColor = DarkThemeColors.primary,
                        unfocusedBorderColor = DarkThemeColors.divider,
                        focusedLabelColor = DarkThemeColors.primary,
                        unfocusedLabelColor = DarkThemeColors.textSecondary,
                        cursorColor = DarkThemeColors.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = DarkThemeColors.textSecondary
                        )
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onSubmit,
                        enabled = answerText.isNotBlank() && (fileUri != null || !assignment.fileRequired),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkThemeColors.primary,
                            disabledContainerColor = DarkThemeColors.surface
                        )
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkThemeColors.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success icon with animation
                val infiniteTransition = rememberInfiniteTransition()
                val rotationAnimation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3000, easing = LinearEasing)
                    )
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(DarkThemeColors.success.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Success",
                        tint = DarkThemeColors.success,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Assignment Submitted!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DarkThemeColors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your response has been analyzed and submitted successfully.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkThemeColors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Analysis results
                Surface(
                    color = DarkThemeColors.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Analysis Results",
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkThemeColors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        AnalysisResultItem(
                            icon = Icons.Default.CheckCircle,
                            title = "Completion",
                            value = "98%",
                            color = DarkThemeColors.success
                        )

                        AnalysisResultItem(
                            icon = Icons.Default.Check,
                            title = "Correctness",
                            value = "92%",
                            color = DarkThemeColors.primary
                        )

                        AnalysisResultItem(
                            icon = Icons.Default.Share,
                            title = "Creativity",
                            value = "85%",
                            color = DarkThemeColors.secondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkThemeColors.primary
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Continue")
                }
            }
        }
    }
}

@Composable
fun AnalysisResultItem(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = DarkThemeColors.textPrimary,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun SubmissionOverlay(state: SubmissionState) {
    // Different messages based on submission state
    val message = when (state) {
        SubmissionState.UPLOADING -> "Uploading your submission..."
        SubmissionState.ANALYZING -> "Analyzing your answer..."
        SubmissionState.SUCCESS -> "Submission successful!"
        SubmissionState.ERROR -> "Error in submission"
        else -> ""
    }

    // Different colors based on state
    val color = when (state) {
        SubmissionState.SUCCESS -> DarkThemeColors.success
        SubmissionState.ERROR -> DarkThemeColors.error
        else -> DarkThemeColors.primary
    }

    // Animation for the indicator
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing)
        )
    )

    // Scale animation for success/error states
    val scale by animateFloatAsState(
        targetValue = if (state == SubmissionState.SUCCESS || state == SubmissionState.ERROR) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessLow
        )
    )

    // Fullscreen overlay with semi-transparent background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkThemeColors.background.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        // Content container with blur and animation
        Surface(
            modifier = Modifier
                .padding(32.dp)
                .scale(scale),
            shape = RoundedCornerShape(20.dp),
            color = DarkThemeColors.surfaceVariant,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Different icons based on state
                when (state) {
                    SubmissionState.UPLOADING, SubmissionState.ANALYZING -> {
                        // Rotating progress indicator
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = color,
                                modifier = Modifier.size(64.dp)
                            )

                            // Center icon based on state
                            Icon(
                                imageVector = if (state == SubmissionState.UPLOADING)
                                    Icons.Default.CheckCircle
                                else
                                    Icons.Default.Star,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    SubmissionState.SUCCESS -> {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Success",
                            tint = color,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    SubmissionState.ERROR -> {
                        Icon(
                            imageVector = Icons.Outlined.Build,
                            contentDescription = "Error",
                            tint = color,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    else -> {}
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkThemeColors.textPrimary,
                    textAlign = TextAlign.Center
                )

                if (state == SubmissionState.ANALYZING) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Add visualization for analysis process
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        // Animated progress dots
                        val dotStates = remember {
                            List(5) { mutableStateOf(0f) }
                        }

                        dotStates.forEachIndexed { index, animatedValue ->
                            val animSpec = infiniteRepeatable<Float>(
                                animation = tween(600, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse,
                                initialStartOffset = StartOffset(index * 100)
                            )

                            val progress by rememberInfiniteTransition().animateFloat(
                                initialValue = 0f,
                                targetValue = 1f,
                                animationSpec = animSpec
                            )

                            animatedValue.value = progress

                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .scale(0.5f + (animatedValue.value * 0.5f))
                                    .background(
                                        color = color.copy(alpha = 0.5f + (animatedValue.value * 0.5f)),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "This may take a moment",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkThemeColors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}