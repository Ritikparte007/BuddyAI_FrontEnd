package com.example.neuroed

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.SubjectSyllabusGetRepository
import com.example.neuroed.repository.SubjectSyllabusHeadingRepository
import com.example.neuroed.repository.SubjectSyllabusHeadingSubtopicRepository
import com.example.neuroed.repository.SubjectSyllabusHeadingTopicRepository
import com.example.neuroed.viewmodel.*
import com.example.neuroed.model.SubjectSyllabusGetResponse
import com.example.neuroed.model.SubjectSyllabusHeadingTopic
import com.example.neuroed.model.SubjectSyllabusHeadingTopicSubtopic
import kotlinx.coroutines.launch

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
    unitId: Int,
    unitTitle: String,
    unitContent: String,
) {
    // Local state for the editable unit title.
    var UnitTitle by remember { mutableStateOf(unitTitle) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Tab titles for filtering the content
    val tabTitles = listOf("Unit Content", "Topics", "Questions")
    var selectedTabIndex by remember { mutableStateOf(0) }

    // State for filtering questions (used in the Related Questions tab)
    var filterText by remember { mutableStateOf("") }

    // State variables for the content that can be modified
    var unitContentState by remember { mutableStateOf(unitContent) }
    val decodedContent = remember {
        try {
            // Replace + with spaces and decode URL encoding
            android.net.Uri.decode(unitContent)
        } catch (e: Exception) {
            // Fallback to original content if decoding fails
            unitContent
        }
    }

    // State for delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Launchers for file picking and camera capture
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            unitContentState += "\n[PDF File Uploaded: ${uri.lastPathSegment}]"
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            unitContentState += "\n[Camera Image Added]"
        }
    }

    // State variables for dialog
    var showDialog by remember { mutableStateOf(false) }
    var dialogInputText by remember { mutableStateOf("") }

    // Setup ViewModel for Syllabus data (this would fetch the complete syllabus for the unit)
    val syllabusViewModel: SubjectSyllabusViewModel = viewModel(
        key = "syllabus_$unitId",
        factory = SubjectSyllabusViewModelFactory(
            repository = SubjectSyllabusGetRepository(RetrofitClient.apiService),
            subjectId = unitId
        )
    )

    // Get our syllabus data for the current unit
    val syllabus by syllabusViewModel.subjectSyllabus.observeAsState(emptyList())
    // Find the current unit data
    val currentUnit = syllabus.find { it.id == unitId }

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
                TopAppBar(
                    title = {
                        // Display unit title on left side
                        Text(
                            text = UnitTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = ModernOnPrimary
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ModernPrimary,
                        titleContentColor = ModernOnPrimary,
                        navigationIconContentColor = ModernOnPrimary,
                        actionIconContentColor = ModernOnPrimary
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

                            // Scrollable Card
                            Card(
                                shape = MaterialTheme.shapes.medium,
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                // Create scrollState
                                val scrollState = rememberScrollState()

                                // Clean up content
                                val cleanContent = decodedContent
                                    .replace("###", "")
                                    .replace("**", "")

                                // Scrollable content with scrollbar
                                Box(modifier = Modifier.fillMaxSize()) {
                                    // Main content
                                    Text(
                                        text = cleanContent,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(scrollState)
                                            .padding(16.dp)
                                    )

                                    // Simple scrollbar
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .fillMaxHeight()
                                            .width(4.dp)
                                            .padding(end = 2.dp)
                                            .background(
                                                color = ModernPrimary.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(2.dp)
                                            )
                                    )
                                }
                            }
                        }
                        1 -> {
                            // Topics Tab with direct topic display
                            DirectTopicsTabContent(
                                unitId = unitId,
                                navController = navController
                            )
                        }
                        2 -> {
                            // Questions Tab
                            QuestionTabContent(
                                unitId = unitId,
                                navController = navController
                            )
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
                        value = UnitTitle,
                        onValueChange = { UnitTitle = it },
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
                1 -> "Add Topic"
                2 -> "Add Question"
                else -> "Add Item"
            }
            val dialogLabel = when (selectedTabIndex) {
                0 -> "Enter additional content"
                1 -> "Enter new topic"
                2 -> "Enter new question"
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
                                    // For topics and questions, you would add to your database here
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectTopicsTabContent(
    unitId: Int,
    navController: NavController
) {
    // State for topic selection and UI interaction
    var searchText by remember { mutableStateOf("") }
    val selectedTopics = remember { mutableStateListOf<SubjectSyllabusHeadingTopic>() }
    val expandedTopics = remember { mutableStateListOf<Int>() }
    val selectedSubtopics = remember { mutableStateMapOf<Int, SnapshotStateList<SubjectSyllabusHeadingTopicSubtopic>>() }

    // Progress tracking (mock data for demonstration)
    val topicProgress = remember { mutableStateMapOf<Int, Float>() }
    val subtopicProgress = remember { mutableStateMapOf<Int, Float>() }

    // This will store all topics from all headings
    val allTopics = remember { mutableStateListOf<SubjectSyllabusHeadingTopic>() }

    // Loading state
    var isLoading by remember { mutableStateOf(true) }

    // We'll use headingViewModel to get all headings, but won't display them
    val headingViewModel: SubjectSyllabusHeadingViewModel = viewModel(
        key = "heading_$unitId",
        factory = SubjectSyllabusHeadingViewModelFactory(
            repository = SubjectSyllabusHeadingRepository(RetrofitClient.apiService),
            syllabus_id = unitId
        )
    )

    // Get headings for the current unit
    val headings by headingViewModel.subjectSyllabusHeading.observeAsState(emptyList())

    // Track how many headings we've processed
    val headingsProcessed = remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    // Load topics for all headings and flatten them
    LaunchedEffect(headings) {
        isLoading = true

        // Clear previous topics
        allTopics.clear()
        headingsProcessed.value = 0

        // If no headings, show no data state
        if (headings.isEmpty()) {
            isLoading = false
            return@LaunchedEffect
        }

        // For each heading, load its topics
        headings.forEach { heading ->
            coroutineScope.launch {
                val topicViewModel = SubjectSyllabusHeadingTopicViewModel(
                    repository = SubjectSyllabusHeadingTopicRepository(RetrofitClient.apiService),
                    title_id = heading.id
                )

                // Observe topics and add them to our flat list
                topicViewModel.subjectsyllabusheadingTopic.observeForever { topics ->
                    // Add the topics to our flat list
                    allTopics.addAll(topics)

                    // Count this heading as processed
                    headingsProcessed.value++

                    // When all headings are processed, set loading to false
                    if (headingsProcessed.value >= headings.size) {
                        isLoading = false
                    }
                }
            }
        }
    }

    // Filter topics based on search text
    val filteredTopics = allTopics.filter {
        it.topic.contains(searchText, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Topics & Subtopics",
            style = MaterialTheme.typography.titleMedium,
            color = ModernPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Search filter
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search Topics") },
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

        if (isLoading) {
            // Loading indicator
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ModernPrimary)
            }
        } else if (allTopics.isEmpty()) {
            // Empty state - no topics available
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = ModernPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Data",
                        style = MaterialTheme.typography.titleLarge,
                        color = ModernPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No topics available for this unit",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (filteredTopics.isEmpty() && searchText.isNotEmpty()) {
            // No search results
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = ModernPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No search results",
                        style = MaterialTheme.typography.titleMedium,
                        color = ModernPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No topics matching \"$searchText\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Display topics in a flat list (no heading hierarchy)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredTopics) { topic ->
                    TopicCard(
                        topic = topic,
                        isSelected = selectedTopics.contains(topic),
                        isExpanded = expandedTopics.contains(topic.id),
                        progress = topicProgress[topic.id] ?: 0f,
                        onTopicSelected = {
                            if (selectedTopics.contains(topic)) {
                                selectedTopics.remove(topic)
                            } else {
                                selectedTopics.add(topic)
                            }
                        },
                        onTopicExpanded = {
                            if (expandedTopics.contains(topic.id)) {
                                expandedTopics.remove(topic.id)
                            } else {
                                expandedTopics.add(topic.id)
                            }
                        },
                        selectedSubtopics = selectedSubtopics.getOrPut(topic.id) { mutableStateListOf() },
                        subtopicProgress = subtopicProgress
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action button
        Button(
            onClick = { navController.navigate("topicsStudyScreen/$unitId") },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = selectedTopics.isNotEmpty()
        ) {
            Text("Start Topics")
        }
    }
}

@Composable
fun TopicCard(
    topic: SubjectSyllabusHeadingTopic,
    isSelected: Boolean,
    isExpanded: Boolean,
    progress: Float,
    onTopicSelected: () -> Unit,
    onTopicExpanded: () -> Unit,
    selectedSubtopics: SnapshotStateList<SubjectSyllabusHeadingTopicSubtopic>,
    subtopicProgress: MutableMap<Int, Float>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onTopicExpanded() },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                ModernPrimary.copy(alpha = 0.2f)
            else
                DarkSurface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Topic row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onTopicSelected() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = ModernPrimary,
                        uncheckedColor = Color.Gray
                    )
                )

                Text(
                    text = topic.topic,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                // Progress indicator
                if (progress > 0) {
                    Box(
                        modifier = Modifier.size(30.dp),
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
                            color = ModernOnPrimary,
                            fontSize = 8.sp
                        )
                    }
                }

                // Expand/collapse icon
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = ModernPrimary
                )
            }

            // Subtopics section (if expanded)
            if (isExpanded) {
                // Load subtopics for this topic
                val subtopicViewModel: SubjectSyllabusHeadingTopicSubtopicViewModel = viewModel(
                    key = "subtopic_${topic.id}",
                    factory = SubjectSyllabusHeadingTopicSubtopicViewModelFactory(
                        repository = SubjectSyllabusHeadingSubtopicRepository(RetrofitClient.apiService),
                        topic_id = topic.id
                    )
                )
                val subtopics by subtopicViewModel.subjectsyllabusheadingTopicSubtopic.observeAsState(emptyList())

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 36.dp, bottom = 12.dp, end = 12.dp)
                ) {
                    Text(
                        text = "Subtopics:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = ModernPrimary.copy(alpha = 0.8f)
                    )

                    Divider(
                        color = ModernPrimary.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    if (subtopics.isEmpty()) {
                        // Empty state for subtopics
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color.Gray.copy(alpha = 0.7f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Subtopics Not Available",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        subtopics.forEach { subtopic ->
                            val isSubtopicSelected = selectedSubtopics.contains(subtopic)
                            val subProgress = subtopicProgress[subtopic.id] ?: 0f

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        if (isSubtopicSelected) {
                                            selectedSubtopics.remove(subtopic)
                                        } else {
                                            selectedSubtopics.add(subtopic)
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSubtopicSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            selectedSubtopics.add(subtopic)
                                        } else {
                                            selectedSubtopics.remove(subtopic)
                                        }
                                    },
                                    modifier = Modifier.size(30.dp),
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = ModernPrimary,
                                        uncheckedColor = Color.Gray
                                    )
                                )

                                Text(
                                    text = subtopic.subtopic,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                // Progress indicator for subtopic
                                if (subProgress > 0) {
                                    Box(
                                        modifier = Modifier.size(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            progress = subProgress,
                                            modifier = Modifier.fillMaxSize(),
                                            color = ModernPrimary
                                        )
                                        Text(
                                            text = "${(subProgress * 100).toInt()}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ModernOnPrimary,
                                            fontSize = 6.sp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionTabContent(
    unitId: Int,
    navController: NavController
) {
    // This would be implemented to load questions from API similar to how topics are loaded
    // For now using a simplified version with mock data

    var filterText by remember { mutableStateOf("") }
    val selectedQuestions = remember { mutableStateListOf<String>() }

    // Mock questions for demonstration
    val questions = listOf(
        "What is cell theory?",
        "Describe the structure of a cell.",
        "How do cells divide?",
        "What role do cells play in the function of living organisms?",
        "How does the history of biology influence modern scientific methods?"
    )

    // Default progress values for questions (values between 0 and 1)
    val defaultQuestionProgress = mapOf(
        "What is cell theory?" to 0.1f,
        "Describe the structure of a cell." to 0.3f,
        "How do cells divide?" to 0.5f,
        "What role do cells play in the function of living organisms?" to 0.7f,
        "How does the history of biology influence modern scientific methods?" to 0.9f
    )
    val questionProgress = remember { mutableStateMapOf<String, Float>().apply { putAll(defaultQuestionProgress) } }

    // Filter questions based on search text
    val filteredQuestions = questions.filter { it.contains(filterText, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Related Questions",
            style = MaterialTheme.typography.titleMedium,
            color = ModernPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Search filter
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

        // Questions list
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

        // Action button
        Button(
            onClick = { navController.navigate("questionsScreen/$unitId") },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = selectedQuestions.isNotEmpty()
        ) {
            Text("Start Questions")
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

            // Checkbox for selection
            Checkbox(
                checked = isSelected,
                onCheckedChange = {
                    if (isSelected) selectedQuestions.remove(question) else selectedQuestions.add(question)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = ModernPrimary,
                    uncheckedColor = Color.Gray
                )
            )

            // Progress indicator
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