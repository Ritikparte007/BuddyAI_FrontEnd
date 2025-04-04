package com.example.neuroed

import android.Manifest
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoScreen(
    navController: NavController,
    onNextClicked: (String) -> Unit = {}
) {
    // State for storing the user's name input
    var userName by remember { mutableStateOf(TextFieldValue("")) }
    // State for storing the selected image URI (if any)
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    // State for storing all fetched image URIs from the device (fetched in background)
    var allImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    // Temporary selection in the bottom sheet
    var tempSelectedUri by remember { mutableStateOf<Uri?>(null) }
    // Controls whether the bottom sheet is visible
    var showBottomSheet by remember { mutableStateOf(false) }
    // State to track if permission is granted
    var hasPermission by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Launcher to request READ_EXTERNAL_STORAGE permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        // If permission is granted after clicking, open the bottom sheet.
        if (granted) {
            tempSelectedUri = selectedImageUri // initialize with current selection if any
            showBottomSheet = true
        }
    }

    // Fetch images once permission is granted
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            allImageUris = fetchAllImages(context)
        }
    }

    // Launcher to pick an image from the gallery (not used when using bottom sheet)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Main container with a dark background and safe padding
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Avatar section with a small profile edit button overlay
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.size(120.dp)
            ) {
                // Display selected image if available; otherwise, show default avatar
                if (selectedImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedImageUri),
                        contentDescription = "Selected Profile Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.man),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                // Small icon button for profile image change
                IconButton(
                    onClick = {
                        // If permission is already granted, show bottom sheet; otherwise, request permission.
                        if (hasPermission) {
                            tempSelectedUri = selectedImageUri
                            showBottomSheet = true
                        } else {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White, shape = CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = "Change Profile Image",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Text field to enter user name
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = {
                    Text(
                        "Enter your name",
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.Transparent,
                    focusedBorderColor = Color(0xFFBB86FC),
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.White,
//                    textColor = Color.White,
                    focusedLabelColor = Color(0xFFBB86FC),
                    unfocusedLabelColor = Color.Gray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), shape = MaterialTheme.shapes.medium)
            )


            Spacer(modifier = Modifier.weight(1f))

            // Next button at the bottom
            Button(
                onClick = { onNextClicked(userName.text) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    text = "Next",
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Modal Bottom Sheet for displaying all fetched images
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                containerColor = Color.DarkGray
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select Profile Image",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                    // Grid displaying all fetched images
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(8.dp),
                        contentPadding = PaddingValues(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(allImageUris) { uri ->
                            // When an image is tapped, update the temporary selection.
                            // Add a highlight border if this image is currently selected.
                            val borderModifier = if (uri == tempSelectedUri) {
                                Modifier.border(width = 3.dp, color = Color.Yellow, shape = MaterialTheme.shapes.small)
                            } else {
                                Modifier
                            }

                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Fetched image",
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .then(borderModifier)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(Color.Gray)
                                    .clickable { tempSelectedUri = uri },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    // Save button to confirm selection
                    Button(
                        onClick = {
                            selectedImageUri = tempSelectedUri
                            showBottomSheet = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(text = "Save", color = Color.Black)
                    }
                }
            }
        }
    }
}

// Function to fetch all image URIs from the device using the MediaStore
suspend fun fetchAllImages(context: Context): List<Uri> = withContext(Dispatchers.IO) {
    val imageUris = mutableListOf<Uri>()
    val projection = arrayOf(MediaStore.Images.Media._ID)
    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
    val query = context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        sortOrder
    )
    query?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val contentUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
            imageUris.add(contentUri)
        }
    }
    imageUris
}
