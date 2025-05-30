@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.neuroed

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.neuroed.NeuroEdApp.Companion.INVALID_USER_ID
import com.example.neuroed.model.UserInfoViewModel
import com.example.neuroed.R

@Composable
fun AgentInfoScreen(
    characterId: Int,
    navController: NavController,
    currentUserId: String? = null // From navigation parameters
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    // Get UserInfoViewModel to fetch current user ID
    val userInfoViewModel: UserInfoViewModel = viewModel()
    val userIdFromViewModel by userInfoViewModel.userId.collectAsState()

    // Load userId when composable starts
    LaunchedEffect(Unit) {
        userInfoViewModel.loadUserId(context)
    }

    // Determine the actual user ID to use - NO DEFAULT FALLBACK
    val actualUserId = remember(currentUserId, userIdFromViewModel) {
        when {
            // 1. Use navigation parameter if available and valid
            currentUserId != null && currentUserId != "null" && currentUserId.isNotEmpty() -> currentUserId
            // 2. Use ViewModel user ID if valid
            userIdFromViewModel != INVALID_USER_ID -> userIdFromViewModel.toString()
            // 3. Get from SharedPreferences as fallback
            else -> {
                val sharedPrefs = context.getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE)
                val storedUserId = sharedPrefs.getInt("userInfoId", INVALID_USER_ID)
                if (storedUserId != INVALID_USER_ID) {
                    storedUserId.toString()
                } else {
                    null // NO DEFAULT
                }
            }
        }
    }

    // If no valid user ID found, show error or redirect to login
    if (actualUserId == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = colorScheme.error
                )
                Text(
                    text = "User not authenticated",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colorScheme.error
                )
                Text(
                    text = "Please login to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        navController.navigate("SignUpScreen") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Login")
                }
            }
        }
        return
    }

    // Character data state
    var character by remember(characterId) { mutableStateOf<Character?>(null) }
    var isLoading by remember(characterId) { mutableStateOf(true) }

    // Edit states
    var instruction by remember { mutableStateOf(TextFieldValue("")) }
    var messageFirst by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var memoryEnabled by remember { mutableStateOf(false) }
    var isPublic by remember { mutableStateOf(false) }
    var isPrivate by remember { mutableStateOf(false) }

    // UI states
    var menuExpanded by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    val scrollState = rememberScrollState()

    // Check if current user can edit this character
    val canEdit = remember(character, actualUserId) {
        character?.let { char ->
            // Can edit if:
            // 1. Character is not private, OR
            // 2. Current user is the creator of this character
            val isPrivate = char.isPrivate ?: false
            val createdBy = char.createdBy
            !isPrivate || createdBy == actualUserId
        } ?: false
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (canEdit) profileImageUri = uri
    }

    // Load character data
    LaunchedEffect(characterId, actualUserId) {
        // API call to fetch character details with user context
        loadCharacter(characterId, actualUserId) { loadedCharacter ->
            character = loadedCharacter
            loadedCharacter?.let {
                instruction = TextFieldValue(it.instruction ?: "")
                messageFirst = TextFieldValue(it.firstMessage ?: "")
                description = TextFieldValue(it.description ?: "")
                memoryEnabled = it.memoryActive ?: false
                isPublic = it.isPublic ?: false
                isPrivate = it.isPrivate ?: false
            }
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading character...")
            }
        }
        return
    }

    Surface(
        color = colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Box {
            // Top App Bar
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = character?.name ?: "Character Info",
                            fontSize = 18.sp
                        )
                        if (character?.isPrivate == true) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Private",
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(16.dp),
                                tint = colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (canEdit) {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuExpanded = false
                                    // Handle delete action with user ID
                                    deleteCharacter(characterId, actualUserId) {
                                        navController.popBackStack()
                                    }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = colorScheme.surface,
                    titleContentColor = colorScheme.onSurface
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(top = 64.dp, bottom = 16.dp)
            ) {
                // Scrollable content
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Avatar Section
                    ProfileAvatarSection(
                        profileImageUri = profileImageUri,
                        characterName = character?.name ?: "Unknown",
                        onEditClick = {
                            if (canEdit) imagePickerLauncher.launch("image/*")
                        },
                        canEdit = canEdit
                    )

                    // Show access denied message if can't edit
                    if (!canEdit && (character?.isPrivate == true)) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Private",
                                    tint = colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "This is a private character. Only the creator can edit it.",
                                    color = colorScheme.error,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    // Show character creator info if different from current user
                    if (character?.createdBy != actualUserId && character?.createdBy != null) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Creator",
                                    tint = colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Created by User ID: ${character?.createdBy}",
                                    color = colorScheme.secondary,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    // Card for general inputs
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "General",
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.primary,
                                fontSize = 16.sp
                            )
                            EnhancedTextField(
                                value = instruction,
                                onValueChange = { if (canEdit) instruction = it },
                                label = "Instruction",
                                placeholder = "Enter instruction...",
                                enabled = canEdit
                            )
                            EnhancedTextField(
                                value = messageFirst,
                                onValueChange = { if (canEdit) messageFirst = it },
                                label = "Message First",
                                placeholder = "Enter initial message...",
                                enabled = canEdit
                            )
                            EnhancedTextField(
                                value = description,
                                onValueChange = { if (canEdit) description = it },
                                label = "Description",
                                placeholder = "Enter description...",
                                enabled = canEdit
                            )
                        }
                    }

                    // Settings Card
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            Text(
                                "Settings",
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.primary,
                                fontSize = 16.sp
                            )
                            LabeledSwitch(
                                "Memory",
                                memoryEnabled,
                                enabled = canEdit
                            ) { if (canEdit) memoryEnabled = it }

                            LabeledSwitch(
                                "Public",
                                isPublic,
                                enabled = canEdit
                            ) { if (canEdit) isPublic = it }

                            LabeledSwitch(
                                "Private",
                                isPrivate,
                                enabled = canEdit
                            ) { if (canEdit) isPrivate = it }
                        }
                    }

                    // Info: Rating & Chat Count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RatingStars(character?.rating ?: 0)
                        Text(
                            "Chat Count: ${character?.chatCount ?: 0}",
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action Buttons Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = if (canEdit) Arrangement.spacedBy(12.dp) else Arrangement.Center
                ) {
                    // Chat Button (always available)
                    Button(
                        onClick = {
                            navController.navigate("ChatScreen/$characterId/$actualUserId")
                        },
                        modifier = Modifier
                            .then(if (canEdit) Modifier.weight(1f) else Modifier.fillMaxWidth())
                            .height(60.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Start Chat", fontWeight = FontWeight.Bold)
                    }

                    // Save Button (only show if can edit)
                    if (canEdit) {
                        Button(
                            onClick = {
                                // Handle save with user ID
                                saveCharacterChanges(
                                    characterId = characterId,
                                    userId = actualUserId,
                                    instruction = instruction.text,
                                    messageFirst = messageFirst.text,
                                    description = description.text,
                                    memoryEnabled = memoryEnabled,
                                    isPublic = isPublic,
                                    isPrivate = isPrivate
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = colorScheme.primary
                            )
                        ) {
                            Text("Save Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileAvatarSection(
    profileImageUri: Uri?,
    characterName: String,
    onEditClick: () -> Unit,
    canEdit: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box {
            if (profileImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(profileImageUri),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = characterName.take(2).uppercase(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Only show edit button if user can edit
            if (canEdit) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Photo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = characterName,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun LabeledSwitch(
    title: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 15.sp
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
fun RatingStars(rating: Int) {
    Row {
        Text("Rating ", color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
        repeat(maxOf(0, minOf(5, rating))) {
            Icon(
                painter = painterResource(id = R.drawable.star),
                contentDescription = "Star",
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EnhancedTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    placeholder: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 56.dp)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        enabled = enabled,
        modifier = modifier,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        ),
        maxLines = 3
    )
}

// Data classes (add these to your models)
data class Character(
    val id: Int,
    val name: String?,
    val instruction: String?,
    val firstMessage: String?,
    val description: String?,
    val memoryActive: Boolean?,
    val isPublic: Boolean?,
    val isPrivate: Boolean?,
    val createdBy: String?, // User ID who created this character
    val rating: Int?,
    val chatCount: Int?
)

// API functions (implement these with user ID context)
suspend fun loadCharacter(characterId: Int, userId: String, onResult: (Character?) -> Unit) {
    // Implement API call to load character with user context
    // Example:
    // val response = apiService.getCharacter(characterId, userId)
    // onResult(response)
}

fun saveCharacterChanges(
    characterId: Int,
    userId: String,
    instruction: String,
    messageFirst: String,
    description: String,
    memoryEnabled: Boolean,
    isPublic: Boolean,
    isPrivate: Boolean
) {
    // Implement API call to save character changes with user context
    // Example:
    // apiService.updateCharacter(characterId, userId, CharacterUpdateRequest(...))
}

fun deleteCharacter(characterId: Int, userId: String, onSuccess: () -> Unit) {
    // Implement API call to delete character with user context
    // Example:
    // apiService.deleteCharacter(characterId, userId) {
    //     onSuccess()
    // }
}