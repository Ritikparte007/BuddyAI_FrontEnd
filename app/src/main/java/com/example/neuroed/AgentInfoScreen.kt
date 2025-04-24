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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.neuroed.R

@Composable
fun AgentInfoScreen(navController: NavController) {
    val colorScheme = MaterialTheme.colorScheme

    // State declarations
    var instruction by remember { mutableStateOf(TextFieldValue("")) }
    var messageFirst by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var memoryEnabled by remember { mutableStateOf(false) }
    var isPublic by remember { mutableStateOf(false) }
    val rating = 5
    val chatCount = 15
    var menuExpanded by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    val scrollState = rememberScrollState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> profileImageUri = uri }

    Surface(
        color = colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Box {
            // Top App Bar with only 3-dot menu, reduced height
            TopAppBar(
                title = { /* Removed title for cleaner UI */ },
                actions = {
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
                                // handle delete action here
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = colorScheme.surface,
                    titleContentColor = colorScheme.onSurface
                ),
                modifier = Modifier.height(40.dp) // Smaller height
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(top = 56.dp, bottom = 16.dp) // adjusted for smaller top bar
            ) {
                // Scrollable content
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Avatar Section with smaller edit button
                    ProfileAvatarSection(
                        profileImageUri = profileImageUri,
                        onEditClick = { imagePickerLauncher.launch("image/*") }
                    )

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
                                onValueChange = { instruction = it },
                                label = "Instruction",
                                placeholder = "Enter instruction..."
                            )
                            EnhancedTextField(
                                value = messageFirst,
                                onValueChange = { messageFirst = it },
                                label = "Message First",
                                placeholder = "Enter initial message..."
                            )
                            EnhancedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = "Description",
                                placeholder = "Enter description..."
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
                            LabeledSwitch("Memory", memoryEnabled) { memoryEnabled = it }
                            LabeledSwitch("Public", isPublic) { isPublic = it }
                        }
                    }

                    // Info: Rating & Chat Count row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RatingStars(rating)
                        Text("Chat Count: $chatCount", color = colorScheme.onSurfaceVariant)
                    }
                }

                // Save Button with white background, increased height
                Button(
                    onClick = { /* handle save */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = colorScheme.primary
                    )
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProfileAvatarSection(
    profileImageUri: Uri?,
    onEditClick: () -> Unit
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
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
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
    Spacer(Modifier.height(8.dp))
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Arex",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun LabeledSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun RatingStars(rating: Int) {
    Row {
        Text("Rating ", color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
        repeat(rating) {
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
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 56.dp)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
        maxLines = 3
    )
}