package com.example.neuroed

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.neuroed.R
import com.example.neuroed.model.SubjectCreateModel
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.SubjectCreateRepository
import com.example.neuroed.viewmodel.SubjectCreateViewModel
import com.example.neuroed.viewmodel.SubjectCreateViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSubjectScreen(navController: NavController) {
    // Input field states.
    var describeSubject by remember { mutableStateOf("") }
    var selectEducation by remember { mutableStateOf("") }
    var selectSubject by remember { mutableStateOf("") }
    var goals by remember { mutableStateOf("") }
    var learningType by remember { mutableStateOf("") }

    // States for date, time, and day.
    var subjectDate by remember { mutableStateOf("") }
    var subjectTime by remember { mutableStateOf("") }
    var subjectDay by remember { mutableStateOf("") }

    // Automatically fetch current date, time, and day.
    LaunchedEffect(Unit) {
        val calendar = Calendar.getInstance()
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dayFormatter = SimpleDateFormat("EEEE", Locale.getDefault())
        subjectDate = dateFormatter.format(calendar.time)
        subjectTime = timeFormatter.format(calendar.time)
        subjectDay = dayFormatter.format(calendar.time)
    }

    // Profile image state.
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePickerSheet by remember { mutableStateOf(false) }
    val imagePickerLauncher = rememberLauncherForActivityResult(contract = GetContent()) { uri: Uri? ->
        if (uri != null) {
            profileImageUri = uri
            showImagePickerSheet = false
        }
    }

    // Loading state.
    var isLoading by remember { mutableStateOf(false) }

    // Flag to check if the user has attempted to submit.
    var isSubmitted by remember { mutableStateOf(false) }

    // Initialize network components.
    val apiService = RetrofitClient.apiService
    val repository = SubjectCreateRepository(apiService)
    val viewModel: SubjectCreateViewModel = viewModel(
        factory = SubjectCreateViewModelFactory(repository)
    )

    // Observe creation response.
    val createdResponse = viewModel.createdSubjectResponse
    if (createdResponse != null) {
        LaunchedEffect(createdResponse) {
            navController.navigate("home") {
                popUpTo("createSubjectScreen") { inclusive = true }
            }
        }
    }

    // Only mark an error if the user has attempted submission.
    val isDescribeError = isSubmitted && describeSubject.isBlank()
    val isEducationError = isSubmitted && selectEducation.isBlank()
    val isSubjectError = isSubmitted && selectSubject.isBlank()
    val isGoalsError = isSubmitted && goals.isBlank()
    val isLearningTypeError = isSubmitted && learningType.isBlank()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Create Subject",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF121212),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            containerColor = Color(0xFF121212)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                // Profile image.
                Box(modifier = Modifier.size(100.dp)) {
                    if (profileImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(model = profileImageUri),
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.biology),
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                    IconButton(
                        onClick = { showImagePickerSheet = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .background(Color.White, shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                ModernInputBox(
                    label = "Describe Subject",
                    value = describeSubject,
                    onValueChange = { describeSubject = it },
                    isError = isDescribeError,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                ModernSelectBox(
                    label = "Select Education",
                    options = listOf("High School", "Undergraduate", "Postgraduate", "Other"),
                    selectedOption = selectEducation,
                    onOptionSelected = { selectEducation = it },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                ModernSelectBox(
                    label = "Select Subject",
                    options = listOf("Biology", "Mathematics", "Chemistry", "Physics", "Other"),
                    selectedOption = selectSubject,
                    onOptionSelected = { selectSubject = it },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                ModernInputBox(
                    label = "Goals",
                    value = goals,
                    onValueChange = { goals = it },
                    isError = isGoalsError,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                ModernSelectBox(
                    label = "Learning Type",
                    options = listOf("Online", "In-Person", "Hybrid"),
                    selectedOption = learningType,
                    onOptionSelected = { learningType = it },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                ModernSubmitButton(
                    text = "Create",
                    onClick = {
                        // Set the submit flag to true so errors show.
                        isSubmitted = true
                        // Prevent submission if any required field is empty.
                        if (isDescribeError || isEducationError || isSubjectError || isGoalsError || isLearningTypeError) {
                            return@ModernSubmitButton
                        }
                        isLoading = true
                        val userId = 1  // Replace with your actual user ID if needed.
                        val image = profileImageUri?.toString() ?: "default_image_url"
                        val subjectData = SubjectCreateModel(
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
                        viewModel.createSubject(subjectData)
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // Bottom sheet for image selection.
        if (showImagePickerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showImagePickerSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Select Profile Image",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pick from Gallery")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showImagePickerSheet = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }

        // Loading dialog pop-up.
        if (isLoading) {
            LoadingDialog()
        }
    }
}

@Composable
fun LoadingDialog() {
    Dialog(onDismissRequest = { /* Prevent dismiss */ }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(16.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Creating your subject...",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSelectBox(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp)
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
            shape = shape,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF1F7A8C),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(text = option, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernInputBox(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) Color.Red else Color.Gray
            )
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
        shape = shape,
        isError = isError,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = if (isError) Color.Red else Color(0xFF1F7A8C),
            unfocusedBorderColor = if (isError) Color.Red else Color.Gray,
            cursorColor = Color.White
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSubmitButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp)
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun CreateSubjectScreenPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        CreateSubjectScreen(navController = navController)
    }
}
