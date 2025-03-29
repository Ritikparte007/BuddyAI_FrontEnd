package com.example.neuroed

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Star
// Uncomment if needed
// import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

// Custom modern colors for dark mode
private val ModernPrimary = Color(0xFF6200EE)
private val ModernOnPrimary = Color.White
private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)
private val ModernError = Color(0xFFB00020)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusContentScreen(
    navController: NavController,
    unitTitle: String = "Unit 1: Introduction to Biology",
    unitContent: String = "In this unit, you will explore the fundamentals of biology. The content covers the history of biological research, the nature of scientific inquiry, the cell as the basic unit of life, and an introduction to the principles that govern living organisms. Detailed explanations, examples, and illustrations are provided to give you a comprehensive understanding of the subject.",
    questions: List<String> = listOf(
        "What is cell theory?",
        "Describe the structure of a cell.",
        "How do cells divide?",
        "What role do cells play in the function of living organisms?",
        "How does the history of biology influence modern scientific methods?"
    ),
    topics: List<String> = listOf(
        "Cellular Processes",
        "Genetic Variation",
        "Evolutionary Adaptations",
        "Molecular Biology",
        "Biotechnology Advances"
    )
) {
    // Local state for the editable unit title.
    var editableUnitTitle by remember { mutableStateOf(unitTitle) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Tab titles for filtering the content
    val tabTitles = listOf("Unit Content", "Related Questions", "Topics")
    var selectedTabIndex by remember { mutableStateOf(0) }

    // State for filtering questions (used in the Related Questions tab)
    var filterText by remember { mutableStateOf("") }

    // State variables for the content that can be modified
    var unitContentState by remember { mutableStateOf(unitContent) }
    var questionsState by remember { mutableStateOf(questions) }
    var topicsState by remember { mutableStateOf(topics) }
    val filteredQuestions = questionsState.filter { it.contains(filterText, ignoreCase = true) }

    // State for selected items (multiple selection)
    val selectedTopics = remember { mutableStateListOf<String>() }
    val selectedQuestions = remember { mutableStateListOf<String>() }

    // Default progress values for topics and questions (values between 0 and 1)
    val defaultTopicProgress = mapOf(
        "Cellular Processes" to 0.3f,
        "Genetic Variation" to 0.5f,
        "Evolutionary Adaptations" to 0.9f,
        "Molecular Biology" to 0.2f,
        "Biotechnology Advances" to 0.7f
    )
    val topicProgress = remember { mutableStateMapOf<String, Float>().apply { putAll(defaultTopicProgress) } }

    val defaultQuestionProgress = mapOf(
        "What is cell theory?" to 0.1f,
        "Describe the structure of a cell." to 0.3f,
        "How do cells divide?" to 0.5f,
        "What role do cells play in the function of living organisms?" to 0.7f,
        "How does the history of biology influence modern scientific methods?" to 0.9f
    )
    val questionProgress = remember { mutableStateMapOf<String, Float>().apply { putAll(defaultQuestionProgress) } }

    // State variables for dialog
    var showDialog by remember { mutableStateOf(false) }
    var dialogInputText by remember { mutableStateOf("") }

    // State for delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Launchers for file picking and camera capture
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            unitContentState += "\n[PDF File Uploaded: ${uri.lastPathSegment}]"
        }
        showDialog = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            unitContentState += "\n[Camera Image Added]"
        }
        showDialog = false
    }

    // Sample mapping of topics to subtopics
    val topicSubtopicsMap = mapOf(
        "Cellular Processes" to listOf("Cell Membrane", "Mitochondria", "Nucleus"),
        "Genetic Variation" to listOf("Mutations", "Recombination", "Gene Flow"),
        "Evolutionary Adaptations" to listOf("Natural Selection", "Speciation", "Adaptation Mechanisms"),
        "Molecular Biology" to listOf("DNA Replication", "Protein Synthesis", "Gene Expression"),
        "Biotechnology Advances" to listOf("CRISPR", "Gene Therapy", "Bioinformatics")
    )

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = ModernPrimary,
            onPrimary = ModernOnPrimary,
            background = DarkBackground,
            surface = DarkSurface,
            onSurface = Color.White,
            error = ModernError
        )
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        // Display the current editable unit title.
                        Text(
                            text = editableUnitTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = ModernOnPrimary
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = ModernPrimary
                    ),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = ModernOnPrimary
                            )
                        }
                    },
                    actions = {
                        // Edit icon opens the edit title dialog.
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Unit Title",
                                tint = ModernOnPrimary
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete Unit",
                                tint = ModernOnPrimary
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = ModernPrimary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Item",
                        tint = ModernOnPrimary
                    )
                }
            },
            containerColor = DarkBackground,
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = DarkSurface,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = ModernPrimary
                            )
                        }
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        color = if (selectedTabIndex == index) ModernOnPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    when (selectedTabIndex) {
                        0 -> {
                            // Unit Content Tab
                            Text(
                                text = "Unit Content",
                                style = MaterialTheme.typography.titleMedium,
                                color = ModernPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                shape = MaterialTheme.shapes.medium,
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface)
                            ) {
                                Text(
                                    text = unitContentState,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        1 -> {
                            // Related Questions Tab with selectable items and "Start Questions" button
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "Related Questions",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ModernPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = filterText,
                                    onValueChange = { filterText = it },
                                    label = { Text("Filter Questions") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = ModernPrimary,
                                        unfocusedBorderColor = ModernPrimary,
                                        cursorColor = ModernPrimary,
                                        focusedLabelColor = ModernPrimary,
                                        unfocusedLabelColor = Color.White
                                    ),
                                    keyboardOptions = KeyboardOptions.Default,
                                    keyboardActions = KeyboardActions.Default
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyColumn(modifier = Modifier.weight(1f)) {
                                    items(filteredQuestions) { question ->
                                        SelectableQuestionItem(
                                            question = question,
                                            selectedQuestions = selectedQuestions,
                                            questionProgress = questionProgress
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { navController.navigate("questionsScreen") },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Start Questions")
                                }
                            }
                        }
                        2 -> {
                            // Topics Tab with expandable subtopics, selectable items and "Start Topics" button
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "More Topics",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ModernPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyColumn(modifier = Modifier.weight(1f)) {
                                    items(topicsState) { topic ->
                                        val subtopics = topicSubtopicsMap[topic] ?: emptyList()
                                        SelectableTopicItem(
                                            topic = topic,
                                            subtopics = subtopics,
                                            selectedTopics = selectedTopics,
                                            topicProgress = topicProgress
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { navController.navigate("topicsScreen") },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Start Topics")
                                }
                            }
                        }
                    }
                }
            }
        )

        // Delete confirmation dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirm Delete") },
                text = { Text("Are you sure you want to delete this unit?") },
                confirmButton = {
                    Button(onClick = {
                        navController.popBackStack()
                        showDeleteDialog = false
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Edit Unit Title dialog
        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Unit Title") },
                text = {
                    OutlinedTextField(
                        value = editableUnitTitle,
                        onValueChange = { editableUnitTitle = it },
                        label = { Text("Unit Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = { showEditDialog = false }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showEditDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Dialog for adding new content
        if (showDialog) {
            val dialogTitle = when (selectedTabIndex) {
                0 -> "Add Content"
                1 -> "Add Question"
                2 -> "Add Topic"
                else -> "Add Item"
            }
            val dialogLabel = when (selectedTabIndex) {
                0 -> "Enter additional content"
                1 -> "Enter new question"
                2 -> "Enter new topic"
                else -> "Enter text"
            }
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = dialogTitle) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = dialogInputText,
                            onValueChange = { dialogInputText = it },
                            label = { Text(dialogLabel) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = ModernPrimary,
                                unfocusedBorderColor = ModernPrimary,
                                cursorColor = ModernPrimary,
                                focusedLabelColor = ModernPrimary,
                                unfocusedLabelColor = Color.White
                            )
                        )
                        if (selectedTabIndex == 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(onClick = { cameraLauncher.launch() }) {
                                        Icon(
                                            imageVector = Icons.Filled.Email,
                                            contentDescription = "Camera",
                                            tint = ModernPrimary
                                        )
                                    }
                                    Text("Camera", color = ModernPrimary, fontSize = 12.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(onClick = { filePickerLauncher.launch("application/pdf") }) {
                                        Icon(
                                            imageVector = Icons.Filled.Email,
                                            contentDescription = "File Upload",
                                            tint = ModernPrimary
                                        )
                                    }
                                    Text("File Upload", color = ModernPrimary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (dialogInputText.isNotBlank()) {
                                when (selectedTabIndex) {
                                    0 -> unitContentState += "\n$dialogInputText"
                                    1 -> questionsState = questionsState + dialogInputText
                                    2 -> topicsState = topicsState + dialogInputText
                                }
                                dialogInputText = ""
                            }
                            showDialog = false
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            dialogInputText = ""
                            showDialog = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SelectableTopicItem(
    topic: String,
    subtopics: List<String>,
    selectedTopics: MutableList<String>,
    topicProgress: MutableMap<String, Float>
) {
    var expanded by remember { mutableStateOf(false) }
    val isSelected = selectedTopics.contains(topic)
    val progress = topicProgress[topic] ?: 0f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { expanded = !expanded },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (progress >= 1f) Color(0xFF388E3C) else DarkSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        subtopics.forEach { subtopic ->
                            Text(
                                text = subtopic,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
            // Replace the Checkbox with a RadioButton for selection.
            RadioButton(
                selected = isSelected,
                onClick = {
                    if (isSelected) selectedTopics.remove(topic) else selectedTopics.add(topic)
                },
                colors = RadioButtonDefaults.colors(
                    selectedColor = ModernPrimary,
                    unselectedColor = Color.Gray
                )
            )
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxSize(),
                    color = ModernPrimary
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = ModernOnPrimary
                )
            }
        }
    }
}

@Composable
fun SelectableQuestionItem(
    question: String,
    selectedQuestions: MutableList<String>,
    questionProgress: MutableMap<String, Float>
) {
    val isSelected = selectedQuestions.contains(question)
    val progress = questionProgress[question] ?: 0f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (progress >= 1f) Color(0xFF388E3C) else DarkSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            // Replace the Checkbox with a RadioButton for selection.
            RadioButton(
                selected = isSelected,
                onClick = {
                    if (isSelected) selectedQuestions.remove(question) else selectedQuestions.add(question)
                },
                colors = RadioButtonDefaults.colors(
                    selectedColor = ModernPrimary,
                    unselectedColor = Color.Gray
                )
            )
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxSize(),
                    color = ModernPrimary
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = ModernOnPrimary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UnitDetailScreenPreview() {
    SyllabusContentScreen(navController = rememberNavController())
}
