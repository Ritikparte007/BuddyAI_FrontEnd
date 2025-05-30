package com.example.neuroed

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.neuroed.R
import com.example.neuroed.model.CloudItem
import com.example.neuroed.model.StorageSummary
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.viewmodel.CloudViewModel
import com.example.neuroed.viewmodel.CloudViewModelFactory
import com.example.neuroed.utils.CloudFileOperation
import com.example.neuroed.utils.Resource
import com.example.neuroed.utils.StorageFilterOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context

// Extension function for Toast
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(navController: NavController) {
    // Create the ViewModel internally
    val apiService = remember { RetrofitClient.apiService }

    // Create ViewModel with API service
    val viewModel: CloudViewModel = viewModel(
        factory = CloudViewModelFactory(apiService)
    )

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Collect states from ViewModel
    val files = viewModel.files.collectAsState().value
    val isLoading = viewModel.isLoading.collectAsState().value
    val loadingMessage = viewModel.loadingMessage.collectAsState().value
    val currentOperation = viewModel.currentOperation.collectAsState().value
    val selectedFilter = viewModel.currentFilter.collectAsState().value
    val statusMessage = viewModel.statusMessage.collectAsState().value

    // Collect recent items and storage summary from ViewModel
    val recentItems = viewModel.recentItems.collectAsState().value
    val storageSummary = viewModel.storageSummary.collectAsState().value

    // State for search query
    var searchQuery by remember { mutableStateOf("") }

    // Load recent items and storage summary
    LaunchedEffect(Unit) {
        // Load data on first composition
        viewModel.loadRecentActivity()
        viewModel.loadStorageSummary()
    }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Upload file directly from composable without passing parameters
            scope.launch(Dispatchers.IO) {
                viewModel.uploadFile(context, it, null)
            }
        }
    }

    // Setup file operations handlers
    when (val operation = currentOperation) {
        is CloudFileOperation.View -> {
            FilePreviewDialog(
                item = operation.item,
                onDismiss = { viewModel.clearCurrentOperation() },
                onDownload = {
                    scope.launch(Dispatchers.IO) {
                        viewModel.downloadFile(context, operation.item.id, operation.item.name)
                    }
                    viewModel.clearCurrentOperation()
                },
                onShare = {
                    scope.launch(Dispatchers.IO) {
                        viewModel.shareItem(operation.item.id)
                    }
                    viewModel.clearCurrentOperation()
                }
            )
        }
        is CloudFileOperation.Delete -> {
            DeleteConfirmationDialog(
                item = operation.item,
                onDismiss = { viewModel.clearCurrentOperation() },
                onConfirmDelete = { item ->
                    scope.launch(Dispatchers.IO) {
                        viewModel.deleteItem(item.id)
                    }
                }
            )
        }
        is CloudFileOperation.Rename -> {
            RenameDialog(
                item = operation.item,
                onDismiss = { viewModel.clearCurrentOperation() },
                onRename = { item, newName ->
                    scope.launch(Dispatchers.IO) {
                        viewModel.renameItem(item.id, newName)
                    }
                }
            )
        }
        is CloudFileOperation.Upload -> {
            UploadOptionsDialog(
                onDismiss = { viewModel.clearCurrentOperation() },
                onSelectFile = {
                    viewModel.clearCurrentOperation()
                    filePickerLauncher.launch("*/*")
                },
                onTakePhoto = {
                    viewModel.clearCurrentOperation()
                    context.showToast("Opening camera...")
                    navController.navigate("camera")
                },
                onCreateFolder = {
                    viewModel.clearCurrentOperation()
                    viewModel.setCurrentOperation(CloudFileOperation.CreateFolder())
                }
            )
        }
        is CloudFileOperation.CreateFolder -> {
            CreateFolderDialog(
                onDismiss = { viewModel.clearCurrentOperation() },
                onCreateFolder = { folderName ->
                    scope.launch(Dispatchers.IO) {
                        viewModel.createFolder(folderName)
                    }
                }
            )
        }
        else -> {}
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Cloud Storage",
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
                        IconButton(onClick = {
                            // Search API call directly
                            scope.launch {
                                viewModel.searchFiles(searchQuery)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(onClick = {
                            viewModel.setCurrentOperation(CloudFileOperation.Upload())
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.filecloud),
                                contentDescription = "Upload",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                // Filter bar
                CloudFilterBar(
                    currentFilter = selectedFilter,
                    onFilterSelected = {
                        // API call directly from composable
                        scope.launch {
                            viewModel.applyFilter(it)
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setCurrentOperation(CloudFileOperation.Upload()) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val currentFiles = if (files is List<CloudItem>) files else emptyList()

                val currentRecentItems = when (recentItems) {
                    is Resource.Success -> recentItems.data ?: emptyList()
                    else -> emptyList()
                }

                val currentStorageSummary = when (storageSummary) {
                    is Resource.Success -> storageSummary.data
                    else -> null
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Storage summary card
                    item {
                        currentStorageSummary?.let { summary ->
                            StorageSummaryCard(summary)
                        }
                    }

                    // Recent activity section
                    item {
                        RecentActivitySection(
                            items = currentRecentItems,
                            onItemClick = { item ->
                                if (item.isFolder) {
                                    // Navigate to folder directly
                                    scope.launch {
                                        viewModel.navigateToFolder(item.id)
                                    }
                                } else {
                                    viewModel.setCurrentOperation(CloudFileOperation.View(item))
                                }
                            },
                            onActionSelected = { operation ->
                                viewModel.setCurrentOperation(operation)
                            }
                        )
                    }

                    // Filter title based on selected filter
                    item {
                        Text(
                            text = when (selectedFilter) {
                                StorageFilterOption.ALL -> "All Files"
                                StorageFilterOption.DOCUMENTS -> "Documents"
                                StorageFilterOption.IMAGES -> "Images"
                                StorageFilterOption.VIDEOS -> "Videos"
                                StorageFilterOption.AUDIO -> "Audio"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    if (currentFiles.isEmpty()) {
                        item {
                            EmptyCloudState(
                                onUploadClick = {
                                    viewModel.setCurrentOperation(CloudFileOperation.Upload())
                                }
                            )
                        }
                    } else {
                        items(currentFiles) { item ->
                            CloudItemCard(
                                item = item,
                                onClick = {
                                    if (item.isFolder) {
                                        // Navigate to folder directly
                                        scope.launch {
                                            viewModel.navigateToFolder(item.id)
                                        }
                                    } else {
                                        viewModel.setCurrentOperation(CloudFileOperation.View(item))
                                    }
                                },
                                onActionSelected = { operation ->
                                    viewModel.setCurrentOperation(operation)
                                }
                            )
                        }

                        // Load more item at the end
                        item {
                            Button(
                                onClick = {
                                    // Call loadNextPage directly
                                    scope.launch {
                                        viewModel.loadNextPage()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text("Load More")
                            }
                        }
                    }

                    // Spacer at the bottom for FAB
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }

                // Show loading overlay if needed
                LoadingOverlay(isLoading = isLoading, message = loadingMessage)

                // Show status message
                OperationStatusMessage(
                    message = statusMessage?.first ?: "",
                    isSuccess = statusMessage?.second ?: true,
                    isVisible = statusMessage != null,
                    onDismiss = { viewModel.clearStatusMessage() }
                )
            }
        }
    )
}

// Rest of the composables remain the same...
// Storage summary card
@Composable
fun StorageSummaryCard(storageSummary: StorageSummary) {
    val used = storageSummary.getStorageUsedGB()
    val total = storageSummary.getStorageTotalGB()
    val percentage = storageSummary.getStoragePercentage()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Storage Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = percentage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${String.format("%.1f", used)} GB of ${String.format("%.1f", total)} GB used",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "${(percentage * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StorageTypeItem(
                    color = Color(0xFF4285F4),
                    type = "Documents",
                    size = "${String.format("%.1f", storageSummary.getDocumentsGB())} GB"
                )

                StorageTypeItem(
                    color = Color(0xFFEA4335),
                    type = "Images",
                    size = "${String.format("%.1f", storageSummary.getImagesGB())} GB"
                )

                StorageTypeItem(
                    color = Color(0xFFFBBC05),
                    type = "Videos",
                    size = "${String.format("%.1f", storageSummary.getVideosGB())} GB"
                )

                StorageTypeItem(
                    color = Color(0xFF34A853),
                    type = "Other",
                    size = "${String.format("%.1f", storageSummary.getOtherGB())} GB"
                )
            }
        }
    }
}

// Improved FilterBar with icons and animations
@Composable
fun CloudFilterBar(
    currentFilter: StorageFilterOption,
    onFilterSelected: (StorageFilterOption) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterTab(
            icon = painterResource(id = R.drawable.bot),
            text = "All",
            isSelected = currentFilter == StorageFilterOption.ALL,
            onClick = { onFilterSelected(StorageFilterOption.ALL) }
        )

        FilterTab(
            icon = painterResource(id = R.drawable.bot),
            text = "Docs",
            isSelected = currentFilter == StorageFilterOption.DOCUMENTS,
            onClick = { onFilterSelected(StorageFilterOption.DOCUMENTS) }
        )

        FilterTab(
            icon = painterResource(id = R.drawable.camera),
            text = "Images",
            isSelected = currentFilter == StorageFilterOption.IMAGES,
            onClick = { onFilterSelected(StorageFilterOption.IMAGES) }
        )

        FilterTab(
            icon = painterResource(id = R.drawable.camera),
            text = "Videos",
            isSelected = currentFilter == StorageFilterOption.VIDEOS,
            onClick = { onFilterSelected(StorageFilterOption.VIDEOS) }
        )

        FilterTab(
            icon = painterResource(id = R.drawable.bot),
            text = "Audio",
            isSelected = currentFilter == StorageFilterOption.AUDIO,
            onClick = { onFilterSelected(StorageFilterOption.AUDIO) }
        )
    }
}

@Composable
fun FilterTab(
    icon: androidx.compose.ui.graphics.painter.Painter,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(
            painter = icon,
            contentDescription = text,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StorageTypeItem(
    color: Color,
    type: String,
    size: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(6.dp))
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = type,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp
        )

        Text(
            text = size,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Recent activity section
@Composable
fun RecentActivitySection(
    items: List<CloudItem>,
    onItemClick: (CloudItem) -> Unit,
    onActionSelected: (CloudFileOperation) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = "Recent Activity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (items.isEmpty()) {
            Text(
                text = "No recent activity",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            items.take(3).forEach { item ->
                CloudItemCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    onActionSelected = onActionSelected
                )
            }
        }
    }
}

// Continue with all other composables (CloudItemCard, EmptyCloudState, etc.)
// ... (rest of the composables remain the same as in your original code)










// Cloud file/folder item with more options menu
@Composable
fun CloudItemCard(
    item: CloudItem,
    onClick: () -> Unit,
    onActionSelected: (CloudFileOperation) -> Unit
) {
    var showOptionsMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon based on file type
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = when {
                            item.isFolder -> MaterialTheme.colorScheme.primaryContainer
                            item.type.contains("PDF") -> Color(0xFFF44336).copy(alpha = 0.2f)
                            item.type.contains("Excel") -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            item.type.contains("PowerPoint") -> Color(0xFFFF9800).copy(alpha = 0.2f)
                            item.type.contains("Word") -> Color(0xFF2196F3).copy(alpha = 0.2f)
                            item.type.contains("Video") -> Color(0xFF9C27B0).copy(alpha = 0.2f)
                            item.type.contains("Audio") -> Color(0xFF03A9F4).copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    item.isFolder -> Icon(
                        painter = painterResource(id = R.drawable.folder),
                        contentDescription = "Folder",
                        tint = Color.Unspecified
                    )
                    item.type.contains("PDF") -> Text(
                        text = "PDF",
                        color = Color(0xFFF44336),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    item.type.contains("Excel") -> Text(
                        text = "XLS",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    item.type.contains("PowerPoint") -> Text(
                        text = "PPT",
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    item.type.contains("Word") -> Text(
                        text = "DOC",
                        color = Color(0xFF2196F3),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    item.type.contains("Video") -> Icon(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = "Video",
                        tint = Color(0xFF9C27B0),
                        modifier = Modifier.size(20.dp)
                    )
                    item.type.contains("Audio") -> Text(
                        text = "MP3",
                        color = Color(0xFF03A9F4),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    else -> Icon(
                        painter = painterResource(id = R.drawable.filecloud),
                        contentDescription = "File",
                        tint = Color.Unspecified
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.size,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(item.lastModified),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box {
                IconButton(onClick = { showOptionsMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.Unspecified
                    )
                }

                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("View") },
                        onClick = {
                            onActionSelected(CloudFileOperation.View(item))
                            showOptionsMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.monitoring),
                                contentDescription = "View",
                                tint = Color.Unspecified
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Download") },
                        onClick = {
                            onActionSelected(CloudFileOperation.Download(item))
                            showOptionsMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.download),
                                contentDescription = "Download",
                                tint = Color.Unspecified
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = {
                            onActionSelected(CloudFileOperation.Share(item))
                            showOptionsMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = Color.Unspecified
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            onActionSelected(CloudFileOperation.Rename(item))
                            showOptionsMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Rename",
                                tint = Color.Unspecified
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = {
                            onActionSelected(CloudFileOperation.Delete(item))
                            showOptionsMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = Color.Red
                            )
                        }
                    )
                }
            }
        }
    }
}

// Empty state when no files are present
@Composable
fun EmptyCloudState(onUploadClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.upload),
            contentDescription = "Upload files",
            modifier = Modifier.size(80.dp),
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your cloud storage is empty",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Upload files to keep them safe and access them from anywhere",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onUploadClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.upload),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text("Upload Files")
        }
    }
}

// Dialog for creating a new folder
@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreateFolder: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Create New Folder",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (folderName.isNotBlank()) {
                                onCreateFolder(folderName)
                                onDismiss()
                            }
                        },
                        enabled = folderName.isNotBlank()
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

// Dialog for renaming a file
@Composable
fun RenameDialog(
    item: CloudItem,
    onDismiss: () -> Unit,
    onRename: (CloudItem, String) -> Unit
) {
    var newName by remember { mutableStateOf(item.name) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Rename ${if (item.isFolder) "Folder" else "File"}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (newName.isNotBlank()) {
                                onRename(item, newName)
                                onDismiss()
                            }
                        },
                        enabled = newName.isNotBlank() && newName != item.name
                    ) {
                        Text("Rename")
                    }
                }
            }
        }
    }
}

// Loading indicator overlay
@Composable
fun LoadingOverlay(isLoading: Boolean, message: String) {
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// File operation status message
@Composable
fun OperationStatusMessage(
    message: String,
    isSuccess: Boolean,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // To avoid FAB
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336)
                ),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = message,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

// File Preview Dialog
@Composable
fun FilePreviewDialog(
    item: CloudItem,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // File Type Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = when {
                                item.isFolder -> MaterialTheme.colorScheme.primaryContainer
                                item.type.contains("PDF") -> Color(0xFFF44336).copy(alpha = 0.2f)
                                item.type.contains("Excel") -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                item.type.contains("PowerPoint") -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                item.type.contains("Word") -> Color(0xFF2196F3).copy(alpha = 0.2f)
                                item.type.contains("Video") -> Color(0xFF9C27B0).copy(alpha = 0.2f)
                                item.type.contains("Audio") -> Color(0xFF03A9F4).copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        item.isFolder -> Icon(
                            painter = painterResource(id = R.drawable.filecloud),
                            contentDescription = "Folder",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(32.dp)
                        )
                        item.type.contains("PDF") -> Text(
                            text = "PDF",
                            color = Color(0xFFF44336),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        item.type.contains("Excel") -> Text(
                            text = "XLS",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        item.type.contains("PowerPoint") -> Text(
                            text = "PPT",
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        item.type.contains("Word") -> Text(
                            text = "DOC",
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        item.type.contains("Video") -> Icon(
                            painter = painterResource(id = R.drawable.camera),
                            contentDescription = "Video",
                            tint = Color(0xFF9C27B0),
                            modifier = Modifier.size(32.dp)
                        )
                        item.type.contains("Audio") -> Text(
                            text = "MP3",
                            color = Color(0xFF03A9F4),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        else -> Icon(
                            painter = painterResource(id = R.drawable.filecloud),
                            contentDescription = "File",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // File Name and Details
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Type: ${item.type} • Size: ${item.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Modified: ${SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(item.lastModified)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // File Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = onDownload,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.download),
                                contentDescription = "Download",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Download",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Share",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

// Upload Options Dialog
@Composable
fun UploadOptionsDialog(
    onDismiss: () -> Unit,
    onSelectFile: () -> Unit,
    onTakePhoto: () -> Unit,
    onCreateFolder: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Upload Options",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Select File Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onSelectFile)
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.filecloud),
                        contentDescription = "Select File",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Select File",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "Upload a file from your device",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Take Photo Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onTakePhoto)
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = "Take Photo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Take Photo",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "Take a photo and upload it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Create Folder Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onCreateFolder)
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.filecloud),
                        contentDescription = "Create Folder",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Create Folder",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "Create a new folder to organize your files",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cancel button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

// Dialog for confirming deletion of a file
@Composable
fun DeleteConfirmationDialog(
    item: CloudItem,
    onDismiss: () -> Unit,
    onConfirmDelete: (CloudItem) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Delete ${if (item.isFolder) "Folder" else "File"}?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Are you sure you want to delete \"${item.name}\"? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = { onConfirmDelete(item) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}