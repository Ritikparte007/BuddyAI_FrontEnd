package com.example.neuroed

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.neuroed.model.CharacterGetData
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.UserCharacterGet
import com.example.neuroed.viewmodel.CharacterGetViewModelFactory
import com.example.neuroed.viewmodel.UserCharacterListViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullCharacterListScreen(
    navController: NavController,
    userId: Int,
    characterListViewModel: UserCharacterListViewModel = viewModel(
        factory = CharacterGetViewModelFactory(
            UserCharacterGet(RetrofitClient.apiService),
            userId
        )
    )
) {
    // Get the context and keyboard controller
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Observe the character list
    val characters by characterListViewModel.userCharacterList.observeAsState(initial = emptyList())

    // Loading state
    var isLoading by remember { mutableStateOf(true) }

    // Search functionality
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Grid state
    val gridState = rememberLazyGridState()

    // Custom pull refresh state
    var pullOffset by remember { mutableStateOf(0f) }
    var showRefreshIndicator by remember { mutableStateOf(false) }

    // Refresh threshold
    val density = LocalDensity.current
    val refreshThreshold = with(density) { 80.dp.toPx() }

    // Handle refresh trigger
    fun triggerRefresh() {
        if (!isLoading) {
            // Refresh characters logic here
            // characterListViewModel.refreshCharacters() // You'll need to implement this
        }
    }

    // Update loading state when characters are loaded
    LaunchedEffect(characters) {
        if (characters.isNotEmpty()) {
            isLoading = false
        }
    }

    // Add a timeout to prevent infinite loading state
    LaunchedEffect(Unit) {
        delay(5000) // 5 seconds timeout
        isLoading = false
    }

    // Auto-focus search field when search is activated
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            delay(100)
            focusRequester.requestFocus()
        } else {
            keyboardController?.hide()
        }
    }

    // Filter characters based on search query
    val filteredCharacters = remember(characters, searchQuery) {
        if (searchQuery.isBlank()) {
            characters
        } else {
            characters.filter { character ->
                character.Character_name?.contains(searchQuery, ignoreCase = true) == true ||
                        character.Description?.contains(searchQuery, ignoreCase = true) == true
            }
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
        // Top App Bar with Search - exactly like FullSubjectListScreen
        TopAppBar(
            title = {
                if (isSearchActive) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search characters...",
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
                        text = "Character List",
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

        // Content with Custom Pull-to-Refresh - exactly like FullSubjectListScreen
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
                                if (!isLoading) {
                                    showRefreshIndicator = false
                                }
                            },
                            onDragEnd = {
                                if (pullOffset >= refreshThreshold && !isLoading) {
                                    triggerRefresh()
                                } else if (!isLoading) {
                                    pullOffset = 0f
                                    showRefreshIndicator = false
                                }
                            },
                            onDrag = { _, dragAmount ->
                                val isAtTop = when {
                                    characters.isEmpty() -> true
                                    gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset == 0 -> true
                                    else -> false
                                }

                                if (isAtTop && dragAmount.y > 0 && !isLoading) {
                                    pullOffset = (pullOffset + dragAmount.y * 0.5f).coerceAtMost(refreshThreshold * 1.5f)
                                    showRefreshIndicator = pullOffset > 20f
                                }
                            }
                        )
                    }
            ) {
                when {
                    isLoading && characters.isEmpty() -> {
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
                                    text = "Loading characters...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    filteredCharacters.isEmpty() && searchQuery.isNotBlank() -> {
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
                                    text = "No characters found",
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

                    characters.isEmpty() -> {
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
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No characters available",
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
                        // Character list - Vertical grid with 2 columns
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
                                items = filteredCharacters,
                                key = { character -> character.id }
                            ) { character ->
                                NullSafeAIProfileCard(character = character) {
                                    try {
                                        navController.navigate("ChatScreen/${character.id}")
                                    } catch (e: Exception) {
                                        Log.e("FullCharacterListScreen", "Navigation error", e)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Custom refresh indicator
            if (showRefreshIndicator || isLoading) {
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

// Note: NullSafeAIProfileCard and AIProfileCard are already defined in another file
// This screen will use those existing components