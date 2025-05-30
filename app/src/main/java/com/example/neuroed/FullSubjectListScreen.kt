package com.example.neuroed

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.neuroed.NeuroEdApp
import com.example.neuroed.SubjectCard
import com.example.neuroed.model.UserInfoViewModel
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.SubjectlistRepository
import com.example.neuroed.viewmodel.SubjectlistViewModel
import com.example.neuroed.viewmodel.SubjectlistViewModelFactory
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullSubjectListScreen(
    navController: NavController,
    subjectlistViewModel: SubjectlistViewModel = viewModel(
        factory = SubjectlistViewModelFactory(
            SubjectlistRepository(RetrofitClient.apiService)
        )
    )
) {
    // Get the UserInfoViewModel
    val userInfoViewModel: UserInfoViewModel = viewModel()

    // Get the context and keyboard controller
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current

    // Observe the userId
    val userId by userInfoViewModel.userId.collectAsState()

    // Observe the subject list
    val subjects by subjectlistViewModel.subjectList.collectAsState()

    // Observe loading state
    val isLoading by subjectlistViewModel.isLoading.collectAsState()

    // Search functionality
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Grid state
    val gridState = rememberLazyGridState()

    // Custom pull refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    var pullOffset by remember { mutableStateOf(0f) }
    var showRefreshIndicator by remember { mutableStateOf(false) }

    // Refresh threshold
    val refreshThreshold = with(density) { 80.dp.toPx() }

    // Handle refresh trigger
    fun triggerRefresh() {
        if (userId != NeuroEdApp.INVALID_USER_ID && !isLoading) {
            isRefreshing = true
            subjectlistViewModel.refreshSubjectList(userId)
        }
    }

    // Handle loading state changes
    LaunchedEffect(isLoading) {
        if (!isLoading && isRefreshing) {
            isRefreshing = false
            pullOffset = 0f
            showRefreshIndicator = false
        }
    }

    // Auto-focus search field when search is activated
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            delay(100) // Small delay to ensure UI is ready
            focusRequester.requestFocus()
        } else {
            keyboardController?.hide()
        }
    }

    // Filter subjects based on search query
    val filteredSubjects = remember(subjects, searchQuery) {
        if (searchQuery.isBlank()) {
            subjects
        } else {
            subjects.filter { subject ->
                subject.subject.contains(searchQuery, ignoreCase = true) ||
                        subject.subjectDescription.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Load the userId when the composable is first created
    LaunchedEffect(Unit) {
        userInfoViewModel.loadUserId(context)
    }

    // Load subjects when userId is available
    LaunchedEffect(userId) {
        if (userId != NeuroEdApp.INVALID_USER_ID) {
            Log.d("FullSubjectListScreen", "Loading subjects for user: $userId")
            subjectlistViewModel.fetchSubjectList(userId)
        } else {
            Log.w("FullSubjectListScreen", "Invalid user ID, cannot load subjects")
        }
    }

    // Handle back press when search is active
    LaunchedEffect(isSearchActive) {
        if (!isSearchActive) {
            searchQuery = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar with Search
        TopAppBar(
            title = {
                if (isSearchActive) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search subjects...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                } else {
                    Text(
                        text = "Subject List",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        if (isSearchActive) {
                            isSearchActive = false
                            searchQuery = ""
                        } else {
                            navController.navigateUp()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = if (isSearchActive) "Close Search" else "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            actions = {
                // Search button
                if (!isSearchActive) {
                    IconButton(
                        onClick = {
                            isSearchActive = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Content with Custom Pull-to-Refresh
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Main content with pull gesture detection
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, pullOffset.roundToInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                // Reset any previous state
                                if (!isRefreshing) {
                                    showRefreshIndicator = false
                                }
                            },
                            onDragEnd = {
                                if (pullOffset >= refreshThreshold && !isRefreshing) {
                                    triggerRefresh()
                                } else if (!isRefreshing) {
                                    pullOffset = 0f
                                    showRefreshIndicator = false
                                }
                            },
                            onDrag = { _, dragAmount ->
                                // Only allow pulling down when at the top
                                val isAtTop = when {
                                    subjects.isEmpty() -> true
                                    gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset == 0 -> true
                                    else -> false
                                }

                                if (isAtTop && dragAmount.y > 0 && !isRefreshing) {
                                    pullOffset = (pullOffset + dragAmount.y * 0.5f).coerceAtMost(refreshThreshold * 1.5f)
                                    showRefreshIndicator = pullOffset > 20f
                                }
                            }
                        )
                    }
            ) {
                when {
                    userId == NeuroEdApp.INVALID_USER_ID -> {
                        // Invalid user state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "User not found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Please log in again",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    isLoading && subjects.isEmpty() -> {
                        // Loading state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Loading subjects...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    filteredSubjects.isEmpty() && searchQuery.isNotBlank() -> {
                        // No search results
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No subjects found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Try searching with different keywords",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    subjects.isEmpty() -> {
                        // Empty state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No subjects available",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Pull down to refresh",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    else -> {
                        // Subject list - Vertical grid with 2 columns
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            state = gridState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = filteredSubjects,
                                key = { subject -> subject.id }
                            ) { subject ->
                                SubjectCard(
                                    subject = subject,
                                    onClick = {
                                        try {
                                            navController.navigate(
                                                "SyllabusScreen/${subject.id}/${subject.subjectDescription}/${subject.subject}"
                                            )
                                        } catch (e: Exception) {
                                            Log.e("FullSubjectListScreen", "Navigation error", e)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Custom refresh indicator - shows only when pulling or refreshing
            if (showRefreshIndicator || isRefreshing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset { IntOffset(0, (pullOffset * 0.8f).roundToInt()) }
                        .padding(top = 16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}