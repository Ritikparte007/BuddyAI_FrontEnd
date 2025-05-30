package com.example.neuroed

// ——— Imports ————————————————————————————————————————————————————————
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.neuroed.ViewModelFactory.AssignmentListViewModelFactory
import com.example.neuroed.model.Assignmentdata
import com.example.neuroed.model.UserInfoViewModel
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.AssignmentRepository
import com.example.neuroed.viewmodel.AssignmentListViewModel
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.platform.LocalContext

// ——— UI‑only data model ————————————————————————————————————————————————
data class AssignmentItem(
    val id: Int,
    val subject: String,
    val title: String,
    val description: String,
    val date: String,
    val status: AssignmentStatus,
    val fileRequired: Boolean = true,
    val supportedFileTypes: List<String> = listOf("image/*", "application/pdf")
)

enum class AssignmentStatus { PENDING, SUCCESSFUL, MISSING }

enum class SubmissionState { IDLE, UPLOADING, ANALYZING, SUCCESS, ERROR }

// ——— Mapping extension (backend → UI) ———————————————————————————————
fun Assignmentdata.toUi(): AssignmentItem = AssignmentItem(
    id = (id ?: 0),
    subject     = subject ?: "Unknown",
    title       = topic ?: "Untitled",
    description = description.orEmpty(),
    date        = dueDate.orEmpty(),
    status      = when (status?.lowercase()) {
        "completed", "success", "successful" -> AssignmentStatus.SUCCESSFUL
        "missing",   "missed"                -> AssignmentStatus.MISSING
        else                                   -> AssignmentStatus.PENDING
    },
    fileRequired = true // TODO: map real value if backend sends one
)

// ——— Main screen ————————————————————————————————————————————————————————
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AssignmentScreen(
    navController: NavController,
    userInfoViewModel: UserInfoViewModel = viewModel()
) {
    // Coroutine scope
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    /* Load user ID and observe it */
    LaunchedEffect(Unit) {
        userInfoViewModel.loadUserId(context)
    }
    val currentUserId by userInfoViewModel.userId.collectAsState()

    // ViewModel via factory - only create when we have valid user ID
    val viewModel: AssignmentListViewModel? = if (currentUserId != NeuroEdApp.INVALID_USER_ID) {
        viewModel(
            factory = AssignmentListViewModelFactory(
                repository = AssignmentRepository(RetrofitClient.apiService),
                userId     = currentUserId // ← real user ID
            )
        )
    } else null

    // Live data → Compose state - only observe if viewModel exists
    val assignmentList by if (viewModel != null) {
        viewModel.assignments.observeAsState(emptyList())
    } else {
        remember { mutableStateOf(emptyList<Assignmentdata>()) }
    }

    // Transform to UI model
    val assignmentListUi by remember(assignmentList) {
        derivedStateOf { assignmentList.map { it.toUi() } }
    }

    // Pending badge count
    val pendingCount by remember(assignmentListUi) {
        derivedStateOf { assignmentListUi.count { it.status == AssignmentStatus.PENDING } }
    }

    // UI state
    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedAssignment by remember { mutableStateOf<AssignmentItem?>(null) }
    var showSubmissionDialog by remember { mutableStateOf(false) }
    var showSuccessPopup   by remember { mutableStateOf(false) }
    var submissionState    by remember { mutableStateOf(SubmissionState.IDLE) }
    var selectedFileUri    by remember { mutableStateOf<Uri?>(null) }
    var answerText         by remember { mutableStateOf("") }

    // Back handler for preventing back button during submission
    BackHandler(enabled = submissionState != SubmissionState.IDLE) {
        // Prevent back navigation while submission is in progress
    }

    // Tabs
    val tabs = listOf("Pending ($pendingCount)", "Completed", "Missing")

    // Filtered list per tab
    val filteredList by remember(selectedTabIndex, assignmentListUi) {
        derivedStateOf {
            when (selectedTabIndex) {
                0 -> assignmentListUi.filter { it.status == AssignmentStatus.PENDING }
                1 -> assignmentListUi.filter { it.status == AssignmentStatus.SUCCESSFUL }
                2 -> assignmentListUi.filter { it.status == AssignmentStatus.MISSING }
                else -> assignmentListUi
            }
        }
    }

    // File picker launcher
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedFileUri = uri
    }

    // Colors (assume tokens defined elsewhere)
    val isDark         = isSystemInDarkTheme()
    val pageBg         = if (isDark) HelpSupportColors.darkBg else HelpSupportColors.lightBg
    val cardBg         = if (isDark) HelpSupportColors.darkSurface else HelpSupportColors.lightSurface
    val chipBg         = if (isDark) HelpSupportColors.darkChip else HelpSupportColors.lightChip
    val textPrimary    = if (isDark) HelpSupportColors.darkText else Color(0xFF1F2937)
    val textSecondary  = if (isDark) HelpSupportColors.darkTextLite else HelpSupportColors.lightText
    val primaryColor   = HelpSupportColors.primaryPurple
    val successColor   = HelpSupportColors.successGreen
    val warningColor   = HelpSupportColors.warningYellow
    val errorColor     = HelpSupportColors.errorRed
    val dividerColor   = if (isDark) HelpSupportColors.darkSurface.copy(alpha = 0.5f) else HelpSupportColors.dividerColor

    // ——— Scaffold ————————————————————————————————————————————————
    Scaffold(
        containerColor = pageBg,
        topBar = {
            TopAppBar(
                title = { Text("Assignments", color = textPrimary, style = AppDimen.TitleLarge) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(start = AppDimens.paddingMd - AppDimens.paddingXs)
                            .size(AppDimens.iconButtonSize)
                            .clip(CircleShape)
                            .background(chipBg)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = primaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedAssignment != null && selectedTabIndex == 0,
                enter = fadeIn() + scaleIn(),
                exit  = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        selectedAssignment?.let {
                            if (it.fileRequired && selectedFileUri == null) {
                                fileLauncher.launch(it.supportedFileTypes.first())
                            } else {
                                showSubmissionDialog = true
                            }
                        }
                    },
                    containerColor = primaryColor,
                    contentColor   = Color.White,
                    shape = CircleShape
                ) {
                    Row(Modifier.padding(horizontal = AppDimens.paddingMd), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Send, contentDescription = "Submit")
                        Spacer(Modifier.width(AppDimens.paddingSm))
                        Text("Submit", style = AppDimen.LabelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).fillMaxSize()) {

            /* Show loading if user ID not available */
            if (currentUserId == NeuroEdApp.INVALID_USER_ID || viewModel == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor)
                }
                return@Column
            }

            // ——— Tabs ——————————————————————————————————————————
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor   = pageBg,
                contentColor     = primaryColor,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            height = 3.dp,
                            color  = primaryColor
                        )
                    }
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick  = {
                            selectedTabIndex = index
                            selectedAssignment = null
                            selectedFileUri = null
                        },
                        text = {
                            val style = AppDimen.LabelMedium.copy(fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium)
                            val color = if (selectedTabIndex == index) primaryColor else textSecondary

                            if (index == 0) {
                                BadgedBox(badge = {
                                    Badge(containerColor = warningColor, contentColor = Color.Black) {
                                        Text(pendingCount.toString(), style = AppDimen.LabelSmall.copy(fontSize = 10.sp))
                                    }
                                }) {
                                    Text(label, style = style, color = color)
                                }
                            } else {
                                Text(label, style = style, color = color)
                            }
                        }
                    )
                }
            }

            // ——— Instructions / Upload area ————————————————————
            AnimatedVisibility(visible = selectedAssignment == null && selectedTabIndex == 0) {
                InstructionBanner(chipBg, primaryColor, textPrimary, textSecondary)
            }
            AnimatedVisibility(visible = selectedAssignment != null && selectedTabIndex == 0) {
                FileUploadArea(
                    onUploadClick = {
                        selectedAssignment?.let {
                            fileLauncher.launch(it.supportedFileTypes.first())
                        }
                    },
                    fileUri = selectedFileUri,
                    required = selectedAssignment?.fileRequired ?: false,
                    defaultBackgroundColor = chipBg,
                    successBackgroundColor = successColor.copy(alpha = 0.1f),
                    borderColor = primaryColor,
                    successBorderColor = successColor,
                    iconColor = primaryColor,
                    successIconColor = successColor,
                    primaryTextColor = textPrimary,
                    secondaryTextColor = textSecondary
                )
            }

            // ——— List ————————————————————————————————————————
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = AppDimens.paddingSm, horizontal = AppDimens.paddingMd),
                verticalArrangement = Arrangement.spacedBy(AppDimens.paddingMd)
            ) {
                items(filteredList) { item ->
                    SimplifiedAssignmentCard(
                        assignment = item,
                        isSelected = selectedAssignment?.id == item.id,
                        onClick = {
                            selectedAssignment = if (selectedAssignment?.id == item.id) null else item
                            if (selectedAssignment?.id != item.id) selectedFileUri = null
                        },
                        cardColor = cardBg,
                        selectedCardColor = primaryColor.copy(alpha = 0.1f),
                        borderColor = dividerColor,
                        selectedBorderColor = primaryColor,
                        primaryTextColor = textPrimary,
                        secondaryTextColor = textSecondary,
                        tagColor = primaryColor,
                        pendingColor = warningColor,
                        successColor = successColor,
                        missingColor = errorColor
                    )
                }
                // Spacer for FAB
                if (selectedAssignment != null && selectedTabIndex == 0) {
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    // ——— Dialogs & Overlay —————————————————————————————————————————

    // Inside the AssignmentScreen composable function
    if (showSubmissionDialog && selectedAssignment != null && viewModel != null) {
        SimplifiedSubmissionDialog(
            assignment = selectedAssignment!!,
            fileUri = selectedFileUri,
            answerText = answerText,
            onAnswerChange = { answerText = it },
            onDismiss = { showSubmissionDialog = false },
            // Replace the existing onSubmit lambda with this new one:
            onSubmit = {
                val currentAssignment = selectedAssignment
                if (currentAssignment != null) {
                    val assignmentId = currentAssignment.id

                    if (assignmentId > 0) {
                        viewModel.submitAssignment(
                            assignmentId = assignmentId,
                            fileUri = selectedFileUri,
                            answerText = answerText,
                            context = context,
                            onStartUpload = {
                                // Hide dialog and show loading state
                                showSubmissionDialog = false
                                submissionState = SubmissionState.UPLOADING
                            },
                            onSuccess = {
                                // Use a new coroutine scope for UI updates
                                coroutineScope.launch {
                                    try {
                                        // Show analysis state
                                        submissionState = SubmissionState.ANALYZING
                                        delay(1000)

                                        // Show success state
                                        submissionState = SubmissionState.SUCCESS
                                        showSuccessPopup = true

                                        // Do not automatically clear the success popup
                                        // The reset will happen when the user clicks OK

                                        // Log for debugging
                                        Log.d("AssignmentScreen", "Success flow completed")
                                    } catch (e: Exception) {
                                        // Log and handle any exceptions during the UI update process
                                        Log.e("AssignmentScreen", "Error in success flow", e)
                                        submissionState = SubmissionState.ERROR
                                        snackbarHostState.showSnackbar(
                                            message = "Error: ${e.message}",
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                }
                            },
                            onError = { errorMessage ->
                                coroutineScope.launch {
                                    // Show error state
                                    submissionState = SubmissionState.ERROR

                                    // Show error message in snackbar
                                    snackbarHostState.showSnackbar(
                                        message = errorMessage,
                                        duration = SnackbarDuration.Long
                                    )

                                    // Reset to idle state after delay
                                    delay(2000)
                                    submissionState = SubmissionState.IDLE
                                }
                            }
                        )
                    } else {
                        // Handle invalid assignment ID
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Invalid assignment ID",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
            },
            dialogBackgroundColor = chipBg,
            primaryTextColor = textPrimary,
            secondaryTextColor = textSecondary,
            primaryAccentColor = primaryColor,
            successColor = successColor,
            warningColor = warningColor,
            dividerColor = dividerColor
        )
    }

    if (showSuccessPopup && viewModel != null) {
        SuccessDialog(
            onDismiss = {
                // Just close the dialog without resetting state
                showSuccessPopup = false
            },
            onDismissWithReset = {
                // Reset all UI states safely
                showSuccessPopup = false
                submissionState = SubmissionState.IDLE
                selectedFileUri = null
                answerText = ""

                // Reset assignment last to prevent any race conditions
                selectedAssignment = null

                // Refresh the list to show updated status
                viewModel.loadAssignments()
            },
            dialogBackgroundColor = chipBg,
            successColor = successColor,
            primaryTextColor = textPrimary,
            secondaryTextColor = textSecondary,
            buttonColor = primaryColor
        )
    }

    AnimatedVisibility(
        visible = submissionState != SubmissionState.IDLE && submissionState != SubmissionState.SUCCESS,
        enter = fadeIn(),
        exit  = fadeOut()
    ) {
        SubmissionOverlay(
            state = submissionState,
            overlayBackgroundColor = pageBg.copy(alpha = 0.85f),
            cardBackgroundColor = chipBg,
            primaryColor = primaryColor,
            successColor = successColor,
            textColor = textPrimary
        )
    }
}


// --- Helper Composable Components ---
@Composable
fun InstructionBanner(
    containerColor: Color,
    iconColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.paddingMd, vertical = AppDimens.paddingSm),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = AppDimens.cornerMd
    ) {
        Row(
            modifier = Modifier.padding(AppDimens.paddingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Instructions",
                tint = iconColor,
                modifier = Modifier.size(AppDimens.iconSizeMd)
            )
            Spacer(modifier = Modifier.width(AppDimens.paddingMd))
            Column {
                Text(
                    text = "How to submit assignments:",
                    color = primaryTextColor,
                    style = AppDimen.BodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(AppDimens.paddingXs))
                Text(
                    text = "1. Select an assignment\n2. Upload file (if required)\n3. Tap Submit button below",
                    color = secondaryTextColor,
                    style = AppDimen.BodyMedium
                )
            }
        }
    }
}

@Composable
fun FileUploadArea(
    onUploadClick: () -> Unit,
    fileUri: Uri?,
    required: Boolean,
    defaultBackgroundColor: Color,
    successBackgroundColor: Color,
    borderColor: Color,
    successBorderColor: Color,
    iconColor: Color,
    successIconColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color
) {
    val currentBorderColor = if (fileUri == null) borderColor else successBorderColor
    val currentIconColor = if (fileUri == null) iconColor else successIconColor
    val currentBackgroundColor = if (fileUri == null) defaultBackgroundColor else successBackgroundColor
    val currentIcon = if (fileUri == null) Icons.Outlined.AddCircle else Icons.Default.CheckCircle

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.paddingMd, vertical = AppDimens.paddingSm)
            .height(100.dp)
            .clickable { onUploadClick() },
        colors = CardDefaults.cardColors(containerColor = currentBackgroundColor),
        border = BorderStroke(width = 1.5.dp, color = currentBorderColor),
        shape = AppDimens.cornerMd
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(AppDimens.paddingMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = currentIcon,
                contentDescription = if (fileUri == null) "Upload File" else "File Selected",
                tint = currentIconColor,
                modifier = Modifier.size(AppDimens.iconSizeMd + 8.dp)
            )
            Spacer(modifier = Modifier.width(AppDimens.paddingMd))
            Column {
                Text(
                    text = when {
                        fileUri != null -> "File Selected"
                        required -> "Upload Required File"
                        else -> "Upload Optional File"
                    },
                    color = if (fileUri != null) successIconColor else primaryTextColor,
                    style = AppDimen.BodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (fileUri == null) "Tap to browse files" else "Tap to change file",
                    color = secondaryTextColor,
                    style = AppDimen.LabelSmall
                )
            }
        }
    }
}


@Composable
fun SimplifiedAssignmentCard(
    assignment: AssignmentItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    cardColor: Color,
    selectedCardColor: Color,
    borderColor: Color,
    selectedBorderColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    tagColor: Color,
    pendingColor: Color,
    successColor: Color,
    missingColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = AppDimens.cornerMd,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) selectedCardColor else cardColor
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) selectedBorderColor else borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.paddingMd),
            verticalAlignment = Alignment.Top
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .padding(top = AppDimens.paddingXs)
                    .size(AppDimens.indicatorSize)
                    .background(
                        when (assignment.status) {
                            AssignmentStatus.PENDING -> pendingColor
                            AssignmentStatus.SUCCESSFUL -> successColor
                            AssignmentStatus.MISSING -> missingColor
                        },
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(AppDimens.paddingMd))

            // Main content column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = assignment.subject.uppercase(),
                    style = AppDimen.LabelSmall.copy(fontWeight = FontWeight.Bold),
                    color = tagColor,
                    modifier = Modifier.padding(bottom = AppDimens.paddingXs)
                )
                Text(
                    text = assignment.title,
                    style = AppDimen.BodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor
                )
                AnimatedVisibility(visible = isSelected) {
                    Text(
                        text = assignment.description,
                        style = AppDimen.BodyMedium,
                        color = secondaryTextColor,
                        modifier = Modifier.padding(top = AppDimens.paddingSm)
                    )
                }
            }
            Spacer(modifier = Modifier.width(AppDimens.paddingMd))

            // Date and File Requirement column
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Due",
                    style = AppDimen.LabelSmall,
                    color = secondaryTextColor
                )
                Text(
                    text = assignment.date,
                    style = AppDimen.BodyMedium,
                    color = primaryTextColor,
                    fontWeight = FontWeight.Medium
                )
                // *** CORRECTED INDICATOR ***
                AnimatedVisibility(visible = isSelected && assignment.fileRequired) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = AppDimens.paddingSm)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info, // Use Attachment Icon
                            contentDescription = "File Required",
                            modifier = Modifier.size(AppDimens.iconSizeSm)
                        )
                        Spacer(modifier = Modifier.width(AppDimens.paddingXs))
                        Text(
                            text = "File Req.",
                            style = AppDimen.LabelSmall
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimplifiedSubmissionDialog(
    assignment: AssignmentItem,
    fileUri: Uri?,
    answerText: String,
    onAnswerChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    dialogBackgroundColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    primaryAccentColor: Color,
    successColor: Color,
    warningColor: Color,
    dividerColor: Color
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Surface(
            shape = AppDimens.cornerLg,
            color = dialogBackgroundColor,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(AppDimens.paddingLg)) {
                Text(
                    text = "Submit Assignment",
                    style = AppDimen.TitleMedium,
                    color = primaryTextColor,
                )
                Spacer(modifier = Modifier.height(AppDimens.paddingMd))
                Text(
                    text = assignment.title,
                    style = AppDimen.BodyLarge,
                    color = primaryTextColor,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(AppDimens.paddingXs))
                Text(
                    text = assignment.description,
                    style = AppDimen.BodyMedium,
                    color = secondaryTextColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(AppDimens.paddingMd))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            fileUri != null -> successColor.copy(alpha = 0.1f)
                            assignment.fileRequired -> warningColor.copy(alpha = 0.1f)
                            else -> dialogBackgroundColor.copy(alpha=0.5f)
                        }
                    ),
                    shape = AppDimens.cornerMd,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(AppDimens.paddingMd)
                    ) {
                        val statusIcon = when {
                            fileUri != null -> Icons.Default.CheckCircle
                            assignment.fileRequired -> Icons.Default.Warning
                            else -> Icons.Default.Info
                        }
                        val statusTint = when {
                            fileUri != null -> successColor
                            assignment.fileRequired -> warningColor
                            else -> secondaryTextColor
                        }
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = "File Status",
                            tint = statusTint,
                            modifier = Modifier.size(AppDimens.iconSizeMd)
                        )
                        Spacer(modifier = Modifier.width(AppDimens.paddingMd))
                        Text(
                            text = when {
                                fileUri != null -> "File attached"
                                assignment.fileRequired -> "File required"
                                else -> "No file attached (optional)"
                            },
                            color = statusTint,
                            style = AppDimen.BodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AppDimens.paddingMd))
                OutlinedTextField(
                    value = answerText,
                    onValueChange = onAnswerChange,
                    placeholder = { Text("Type your answer or notes here...", color = secondaryTextColor) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = primaryAccentColor,
                        focusedBorderColor = primaryAccentColor,
                        unfocusedBorderColor = dividerColor,
                        containerColor = dialogBackgroundColor.copy(alpha = 0.6f),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = AppDimens.cornerMd,
                    textStyle = LocalTextStyle.current.copy(color = primaryTextColor)
                )
                Spacer(modifier = Modifier.height(AppDimens.paddingLg))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = secondaryTextColor)
                    ) {
                        Text("Cancel", style = AppDimen.LabelMedium)
                    }
                    Spacer(Modifier.width(AppDimens.paddingSm))
                    Button(
                        onClick = onSubmit,
                        enabled = answerText.isNotBlank() || (!assignment.fileRequired && fileUri == null) || (fileUri != null),
                        shape = AppDimens.cornerPill,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryAccentColor,
                            contentColor = Color.White,
                            disabledContainerColor = dialogBackgroundColor.copy(alpha = 0.5f),
                            disabledContentColor = secondaryTextColor.copy(alpha = 0.7f)
                        )
                    ) {
                        Text("Submit", style = AppDimen.LabelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}


@Composable
fun SuccessDialog(
    onDismiss: () -> Unit,
    onDismissWithReset: (() -> Unit)? = null,
    dialogBackgroundColor: Color,
    successColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    buttonColor: Color
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Surface(
            shape = AppDimens.cornerLg,
            color = dialogBackgroundColor,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(AppDimens.paddingLg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(successColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Success",
                        tint = successColor,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(AppDimens.paddingMd))
                Text(
                    text = "Success!",
                    style = AppDimen.TitleMedium,
                    color = primaryTextColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(AppDimens.paddingSm))
                Text(
                    text = "Your assignment was submitted",
                    style = AppDimen.BodyMedium,
                    color = secondaryTextColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(AppDimens.paddingLg))
                Button(
                    onClick = { onDismissWithReset?.invoke() ?: onDismiss() },
                    shape = AppDimens.cornerPill,
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK", style = AppDimen.LabelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}


@Composable
fun SubmissionOverlay(
    state: SubmissionState,
    overlayBackgroundColor: Color,
    cardBackgroundColor: Color,
    primaryColor: Color,
    successColor: Color,
    textColor: Color
) {
    val message = when (state) {
        SubmissionState.UPLOADING -> "Uploading..."
        SubmissionState.ANALYZING -> "Analyzing..."
        SubmissionState.SUCCESS -> "Success!"
        SubmissionState.ERROR -> "Error"
        else -> ""
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(overlayBackgroundColor)
            .clickable(enabled = false, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(AppDimens.cornerMd)
                .background(cardBackgroundColor)
                .padding(AppDimens.paddingLg)
        ) {
            if (state == SubmissionState.UPLOADING || state == SubmissionState.ANALYZING) {
                CircularProgressIndicator(color = primaryColor)
            } else if (state == SubmissionState.SUCCESS) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = successColor,
                    modifier = Modifier.size(48.dp)
                )
            } else if (state == SubmissionState.ERROR) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Error",
                    tint = Color.Red,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(AppDimens.paddingMd))
            Text(
                text = message,
                style = AppDimen.BodyLarge,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}