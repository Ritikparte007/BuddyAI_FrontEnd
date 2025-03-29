package com.example.neuroed

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

// Data models
data class MemoryItem(
    val id: Int,
    val title: String,
    val description: String,
    val imageRes: Int
)

// Sample data for personal memories (Image filter)
val samplePersonalMemories = listOf(
    MemoryItem(1, "Birthday Celebration", "A memorable birthday with friends and family.", R.drawable.biology),
    MemoryItem(2, "Vacation Time", "A relaxing vacation at the beach.", R.drawable.biology)
)

// Sample data for videos (Video filter)
val sampleVideos = listOf(
    MemoryItem(1, "Vacation Video", "A short clip from a vacation.", R.drawable.camera),
    MemoryItem(2, "Birthday Video", "Highlights from a birthday celebration.", R.drawable.bot)
)

// Sample data for notebook entries (Notebook filter)
val sampleNotebooks = listOf(
    "Notebook Entry 1: Meeting notes...",
    "Notebook Entry 2: Project ideas..."
)

// Filter options enum – note: we removed FILE and TEXT, and added VIDEO and NOTEBOOK.
enum class FilterOption {
    ALL, IMAGE, VIDEO, NOTEBOOK
}

// Modern Section Header with background color and rounded corners
@Composable
fun MySectionHeader(title: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(16.dp)
        )
    }
}

// Card composable for displaying both personal memories and video items.
@Composable
fun MemoryCard(item: MemoryItem) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { /* Handle item click event */ },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// NotebookCard for displaying text notes (Notebook filter)
@Composable
fun NotebookCard(note: String) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = note,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

// Filter bar below the top bar for filtering by ALL, Image, Video, or Notebook
@Composable
fun FilterBar(
    currentFilter: FilterOption,
    onFilterSelected: (FilterOption) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ALL filter button
        TextButton(onClick = { onFilterSelected(FilterOption.ALL) }) {
            Text(
                text = "All",
                color = if (currentFilter == FilterOption.ALL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
        // Image filter button
        TextButton(onClick = { onFilterSelected(FilterOption.IMAGE) }) {
            Icon(
                painter = painterResource(id = R.drawable.camera),
                contentDescription = "Image Filter",
                modifier = Modifier.size(20.dp),
                tint = if (currentFilter == FilterOption.IMAGE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Image",
                color = if (currentFilter == FilterOption.IMAGE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
        // Video filter button
        TextButton(onClick = { onFilterSelected(FilterOption.VIDEO) }) {
            Icon(
                painter = painterResource(id = R.drawable.man), // Replace with your video icon
                contentDescription = "Video Filter",
                modifier = Modifier.size(20.dp),
                tint = if (currentFilter == FilterOption.VIDEO) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Video",
                color = if (currentFilter == FilterOption.VIDEO) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
        // Notebook filter button
        TextButton(onClick = { onFilterSelected(FilterOption.NOTEBOOK) }) {
            Icon(
                painter = painterResource(id = R.drawable.bot), // Replace with your notebook icon
                contentDescription = "Notebook Filter",
                modifier = Modifier.size(20.dp),
                tint = if (currentFilter == FilterOption.NOTEBOOK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Notebook",
                color = if (currentFilter == FilterOption.NOTEBOOK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(navController: NavController) {
    // A state to hold the current filter selection
    val selectedFilter = remember { mutableStateOf(FilterOption.ALL) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Store",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Camera Icon Button
                    IconButton(onClick = {
                        navController.navigate("AutoOpenCameraScreen")
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.camera),
                            contentDescription = "Camera",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    // File Icon Button (if needed)
                    IconButton(onClick = {
                        navController.navigate("FileScreen")
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.stats),
                            contentDescription = "File",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Insert FilterBar below the top bar
                item {
                    FilterBar(
                        currentFilter = selectedFilter.value,
                        onFilterSelected = { selectedFilter.value = it }
                    )
                }
                // Conditionally show content based on filter selection
                when (selectedFilter.value) {
                    FilterOption.ALL -> {
                        // Show all sections: Images, Videos, and Notebooks.
                        item { MySectionHeader(title = "Personal Memories") }
                        item {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                items(samplePersonalMemories) { memory ->
                                    MemoryCard(memory)
                                }
                            }
                        }
                        item { MySectionHeader(title = "Videos") }
                        item {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                items(sampleVideos) { video ->
                                    MemoryCard(video)
                                }
                            }
                        }
                        item { MySectionHeader(title = "Notebooks") }
                        items(sampleNotebooks) { note ->
                            NotebookCard(note)
                        }
                    }
                    FilterOption.IMAGE -> {
                        item { MySectionHeader(title = "Personal Memories") }
                        item {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                items(samplePersonalMemories) { memory ->
                                    MemoryCard(memory)
                                }
                            }
                        }
                    }
                    FilterOption.VIDEO -> {
                        item { MySectionHeader(title = "Videos") }
                        item {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                items(sampleVideos) { video ->
                                    MemoryCard(video)
                                }
                            }
                        }
                    }
                    FilterOption.NOTEBOOK -> {
                        item { MySectionHeader(title = "Notebooks") }
                        items(sampleNotebooks) { note ->
                            NotebookCard(note)
                        }
                    }
                }
            }
        }
    )
}
