package com.example.neuroed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.neuroed.repository.SubjectSyllabusGetRepository
import com.example.neuroed.viewmodel.SubjectSyllabusViewModel
import com.example.neuroed.viewmodel.SubjectSyllabusViewModelFactory

// Example custom color references
private val PurpleAccent = Color(0xFFBB86FC)
private val PinkDelete = Color(0xFFFF5370)
private val DarkBackground = Color(0xFF121212)
private val DarkCard = Color(0xFF1E1E1E)
private val LightTextColor = Color(0xFFD0D0D0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusScreen(
    navController: NavController,
    id: Int,
    subjectDescription: String,
    subject: String,
    repository: SubjectSyllabusGetRepository
) {
    val scrollState = rememberScrollState()
    // Use the provided 'id' as the subject id
    val subjectId = id

    // State for showing the "Add Unit" dialog and its input fields
    var showAddDialog by remember { mutableStateOf(false) }
    var unitTitle by remember { mutableStateOf("") }
    var unitDescription by remember { mutableStateOf("") }

    // State for showing the delete subject confirmation dialog
    var showDeleteSubjectDialog by remember { mutableStateOf(false) }

    // Initialize the ViewModel using the custom factory
    val viewModel: SubjectSyllabusViewModel = viewModel(
        factory = SubjectSyllabusViewModelFactory(repository, id)
    )

    // Observe the LiveData from the ViewModel
    val syllabusList by viewModel.subjectSyllabus.observeAsState(emptyList())

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

    fun truncateText(text: String, wordLimit: Int = 15): String {
        val words = text.split(" ")
        return if (words.size > wordLimit) {
            words.take(wordLimit).joinToString(" ") + "..."
        } else {
            text
        }
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Syllabus") },
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
                        IconButton(onClick = { navController.navigate("AddUnitScreen/$id") }) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "Add Unit",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(onClick = {
                            // Handle edit action for syllabus here
                        }) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Edit Syllabus",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { showDeleteSubjectDialog = true }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete Syllabus",
                                tint = MaterialTheme.colorScheme.error
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(scrollState)
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Top Image (Subject Image)
                Image(
                    painter = painterResource(id = R.drawable.biology), // Replace with your image resource
                    contentDescription = "Biology Subject Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title and Subtitle
                Text(
                    text = subject,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subjectDescription,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Display each syllabus unit
                syllabusList.forEach { syllabus ->
                    UnitCard(
                        unitTitle = "Unit ${syllabus.syllabusUnit}: ${syllabus.syllabusChapterName}",
                        unitDescription = truncateText(syllabus.subjectSyllabusContent),
                        progress = 0.5f, // Adjust progress if needed
                        onViewContentClick = {
                            navController.navigate("SyllabusContentScreen")
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // AlertDialog to add a new unit
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add New Unit") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = unitTitle,
                            onValueChange = { unitTitle = it },
                            label = { Text("Unit Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = unitDescription,
                            onValueChange = { unitDescription = it },
                            label = { Text("Unit Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        // Handle adding the new unit to your data source
                        unitTitle = ""
                        unitDescription = ""
                        showAddDialog = false
                    }) {
                        Text("Submit")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // AlertDialog for delete subject confirmation
        if (showDeleteSubjectDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteSubjectDialog = false },
                title = { Text("Delete Subject") },
                text = { Text("Do you want to delete this subject?") },
                confirmButton = {
                    Button(
                        onClick = {
                            // Handle deletion of the subject here
                            showDeleteSubjectDialog = false
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDeleteSubjectDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun UnitCard(
    unitTitle: String,
    unitDescription: String,
    progress: Float,
    onViewContentClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = unitTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = unitDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
            ) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Progress: ${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onViewContentClick,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = "View Content")
            }
        }
    }
}

