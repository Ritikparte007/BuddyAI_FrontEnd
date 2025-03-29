package com.example.neuroed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.neuroed.viewmodel.NotificationViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.neuroed.repository.NotificationRepository
import com.example.neuroed.viewmodel.NotificationViewModelFactory


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    onHistoricalClick: () -> Unit,
) {

    // Create the repository and factory here (or use dependency injection in a real app)
    val repository = NotificationRepository()
    val factory = NotificationViewModelFactory(repository)


    // Obtain the ViewModel using the custom factory
    val viewModel: NotificationViewModel = viewModel(factory = factory)

    // Trigger API call when this composable is first composed.
    LaunchedEffect(Unit) {
        viewModel.fetchNotifications()
    }

    // Collect notifications from StateFlow as state.
    val notificationsState = viewModel.notifications.collectAsState()

    // Print the values every time they change
    LaunchedEffect(notificationsState.value) {
        println("Notifications: ${notificationsState.value}")
    }



    // Static list of notifications.
    val notifications = listOf(
        NotificationData(
            iconRes = R.drawable.notificationbell, // Replace with your icon resource.
            title = "New Message",
            message = "You have received a new message!",
            date = "10:30 AM"
        ),
        NotificationData(
            iconRes = R.drawable.notificationbell,
            title = "App Update",
            message = "Your app has been updated successfully.",
            date = "9:45 AM"
        ),
        NotificationData(
            iconRes = R.drawable.notificationbell,
            title = "Reminder",
            message = "Meeting in 15 minutes.",
            date = "8:00 AM"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Notifications", fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back navigation */ }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle settings action */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (notifications.isEmpty()) {
                EmptyNotificationsState(onHistoricalClick)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    items(notifications) { notif ->
                        NotificationItem(notif)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyNotificationsState(onHistoricalClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.notificationbell),
            contentDescription = "No notifications illustration",
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No notifications yet",
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your notifications will appear here once you have any.",
            style = TextStyle(fontSize = 14.sp, color = Color.Gray)
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onHistoricalClick) {
            Text(text = "Missing notifications? Go to historical notifications.")
        }
    }
}

@Composable
fun NotificationItem(notification: NotificationData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = notification.iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            Text(
                text = notification.date,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// Data class for static notifications.
data class NotificationData(
    val iconRes: Int,
    val title: String,
    val message: String,
    val date: String
)
