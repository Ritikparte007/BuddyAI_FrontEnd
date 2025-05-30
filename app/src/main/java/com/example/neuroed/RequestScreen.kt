package com.example.neuroed

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class UserProfile(
    val name: String,
    val level: Int,
    val isAppUser: Boolean
)

@Composable
fun RequestScreen(navController: NavController) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var contactList by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var loadingUser by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            contactList = getContacts(context)
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            contactList = getContacts(context)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Top search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp)
                    .height(56.dp),
                placeholder = {
                    Text("Search contacts", color = Color(0xFFAAAAAA))
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFFAAAAAA)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0096FF),
                    unfocusedBorderColor = Color(0xFF444444),
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.White)
            )

            val filteredContacts = contactList.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }

            LazyColumn {
                items(filteredContacts) { user ->
                    UserProfileCard(
                        user = user,
                        isLoading = loadingUser == user.name,
                        onAddClick = {
                            loadingUser = user.name
                            coroutineScope.launch {
                                delay(1500) // Simulated action
                                loadingUser = null
                            }
                        },
                        onInviteClick = {
                            shareInvite(context, user.name)
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun UserProfileCard(
    user: UserProfile,
    isLoading: Boolean,
    onAddClick: () -> Unit,
    onInviteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .shadow(6.dp, shape = RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with gradient
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF70CCFF), Color(0xFF005F99))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.firstOrNull()?.uppercase() ?: "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = user.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Level ${user.level.toString().padStart(2, '0')}",
                    fontSize = 14.sp,
                    color = Color(0xFFBBBBBB)
                )
            }

            // Action: Add or Share
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (user.isAppUser) Color(0xFF0096FF) else Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    IconButton(
                        onClick = {
                            if (user.isAppUser) {
                                onAddClick()
                            } else {
                                onInviteClick()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (user.isAppUser) Icons.Default.Add else Icons.Default.Send,
                            contentDescription = if (user.isAppUser) "Add" else "Invite",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

fun getContacts(context: Context): List<UserProfile> {
    val contacts = mutableListOf<UserProfile>()
    val cursor = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
        null,
        null,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
    )

    cursor?.use {
        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        while (it.moveToNext()) {
            val name = it.getString(nameIndex)
            val isAppUser = (0..1).random() == 1 // Simulated logic
            if (!name.isNullOrBlank()) {
                contacts.add(
                    UserProfile(
                        name = name,
                        level = (1..99).random(),
                        isAppUser = isAppUser
                    )
                )
            }
        }
    }

    return contacts.distinctBy { it.name }
}

fun shareInvite(context: Context, contactName: String) {
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            "Hey $contactName! Join me on NeuroED – the smart learning app. Here's the link: https://example.com"
        )
        type = "text/plain"
    }
    val chooser = Intent.createChooser(intent, "Invite $contactName via")
    context.startActivity(chooser)
}
