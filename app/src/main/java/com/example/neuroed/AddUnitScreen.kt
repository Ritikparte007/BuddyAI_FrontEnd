package com.example.neuroed

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.neuroed.model.SubjectSyllabusSaveModel
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.SubjectCreateRepository
import com.example.neuroed.repository.SubjectSyllabusSaveRepository
import com.example.neuroed.viewmodel.SubjectCreateViewModel
import com.example.neuroed.viewmodel.SubjectCreateViewModelFactory
import com.example.neuroed.viewmodel.SubjectSyllabusSaveViewModel
import com.example.neuroed.viewmodel.SubjectSyllabusSaveViewModelFactory
import com.example.neuroed.viewmodel.SubjectlistViewModelFactory

// Custom dark theme colors reused in your project
private val PurpleAccent = Color(0xFFBB86FC)
private val PinkDelete = Color(0xFFFF5370)
private val DarkBackground = Color(0xFF121212)
private val DarkCard = Color(0xFF1E1E1E)
private val LightTextColor = Color(0xFFD0D0D0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUnitScreen(
    navController: NavController,
    id:Int,
) {
    // Dropdown state for unit selection
    val unitOptions = listOf("Unit 1", "Unit 2", "Unit 3")
    var expanded by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf(unitOptions.first()) }

    // Other inputs: title and syllabus
    var titleInput by remember { mutableStateOf("") }
    var syllabusInput by remember { mutableStateOf("") }

    // File upload state (if needed for syllabus extraction)
    var selectedFileUri by remember { mutableStateOf<String?>(null) }
    var showFilePickerOptions by remember { mutableStateOf(false) }

    // Launchers for file picking
    val libraryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedFileUri = uri?.toString()
    }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedFileUri = uri?.toString()
    }

    val apiService = RetrofitClient.apiService
    val repository = SubjectSyllabusSaveRepository(apiService)
    val viewModel: SubjectSyllabusSaveViewModel = viewModel(
        factory = SubjectSyllabusSaveViewModelFactory(repository)
    )



    // Observe response from the ViewModel (e.g., a success message)
    val saveResponse by viewModel.saveResponse.observeAsState()
    LaunchedEffect(saveResponse) {
        saveResponse?.let { response ->
            // You can show a snackbar, dialog, or navigate based on the response
            // For example, navigate back on a successful save:
            // navController.navigateUp()
        }
    }

    // Dark theme with custom accent colors
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
                                Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    actions = {
                        // Camera option (placeholder)
                        IconButton(onClick = {
                            // Handle camera open action here
                        }) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Open Camera",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        // File upload option: open a bottom sheet with file picker options
                        IconButton(onClick = {
                            showFilePickerOptions = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Upload File",
                                tint = MaterialTheme.colorScheme.onBackground
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
            // Main content with vertical scrolling
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Unit selection dropdown
                OutlinedTextField(
                    value = selectedUnit,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Unit") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Dropdown Arrow",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.clickable { expanded = !expanded }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    unitOptions.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit) },
                            onClick = {
                                selectedUnit = unit
                                expanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title input field (e.g., chapter name)
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Title Input") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Syllabus input field with increased height
                OutlinedTextField(
                    value = syllabusInput,
                    onValueChange = { syllabusInput = it },
                    label = { Text("Syllabus") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    maxLines = 10
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Submit button to send data to server
                Button(
                    onClick = {
                        // Convert the selected unit (e.g., "Unit 1") into an integer value.
                        val unitNumber = selectedUnit.split(" ").getOrNull(1)?.toIntOrNull() ?: 0
                        // For demonstration purposes, a placeholder subject ID is used.
                        // Replace with an actual subject ID as needed.
                        val subjectId = id
                        // Create a model instance using the input values.
                        val model = SubjectSyllabusSaveModel(
                            subjectId = subjectId.toString(),
                            subjectUnit = unitNumber,
                            subjectChapterName = titleInput,
                            subjectSyllabus = syllabusInput
                        )
                        // Trigger the save operation in the ViewModel.
                        viewModel.saveSubjectSyllabus(model)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Submit")
                }
            }

            // Bottom modal sheet with file picker options
            if (showFilePickerOptions) {
                ModalBottomSheet(
                    onDismissRequest = { showFilePickerOptions = false }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Select File Source",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Option 1: Library
                        ListItem(
                            headlineContent = { Text("Library") },
                            modifier = Modifier.clickable {
                                libraryPickerLauncher.launch("image/*")
                                showFilePickerOptions = false
                            }
                        )
                        // Option 2: File System
                        ListItem(
                            headlineContent = { Text("File System") },
                            modifier = Modifier.clickable {
                                filePickerLauncher.launch("*/*")
                                showFilePickerOptions = false
                            }
                        )
                    }
                }
            }

            // AlertDialog showing file details and an option to extract the syllabus if a file is selected
            if (selectedFileUri != null) {
                AlertDialog(
                    onDismissRequest = { selectedFileUri = null },
                    title = { Text("File Selected") },
                    text = { Text("Selected file URI:\n$selectedFileUri") },
                    confirmButton = {
                        Button(
                            onClick = {
                                // Handle syllabus extraction logic here
                                selectedFileUri = null
                            }
                        ) {
                            Text("Extract Syllabus")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { selectedFileUri = null }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
