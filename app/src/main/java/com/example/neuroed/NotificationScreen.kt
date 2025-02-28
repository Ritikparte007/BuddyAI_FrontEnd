package com.example.neuroed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    onCustomizeClick: () -> Unit,
    onHistoricalClick: () -> Unit
) {
    // Simulated notifications for demo
    val notifications = remember {
        mutableStateListOf<NotificationData>(
            // Add or remove items to test empty vs. populated states
            NotificationData(
                iconRes = R.drawable.biology, // your own icon
                title = "Vista rewards club",
                message = "Earn Points without making a purchase!",
                date = "Dec 16, 2023"
            ),
            NotificationData(
                iconRes = R.drawable.biology,
                title = "Vista rewards club",
                message = "Keep paying with Vista to boost your points!",
                date = "Dec 8, 2023"
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontSize = 20.sp) },
                actions = {
                    Button(
                        onClick = onCustomizeClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "Customize your notifications!",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            if (notifications.isEmpty()) {
                // Empty state
                EmptyNotificationsState(onHistoricalClick)
            } else {
                // Populated list of notifications
                Column(modifier = Modifier.fillMaxSize()) {
                    PreviouslySection()
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
        // Large illustration or icon
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

        // Historical link
        TextButton(onClick = onHistoricalClick) {
            Text(text = "Missing notifications? Go to historical notifications.")
        }
    }
}

@Composable
fun PreviouslySection() {
    Text(
        text = "Previously",
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun NotificationItem(notification: NotificationData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Notification Icon
            Image(
                painter = painterResource(id = notification.iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Notification Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Notification Date
            Text(
                text = notification.date,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

// Simple data class for notifications
data class NotificationData(
    val iconRes: Int,
    val title: String,
    val message: String,
    val date: String
)


// Data model for a notification
data class NotificationItem(
    val id: Int,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false
)

// Sample data for lock-screen notifications
val sampleLockScreenNotifications = listOf(
    NotificationItem(1, "New Message", "You have received a new message!", "10:30 AM"),
    NotificationItem(2, "App Update", "Your app has been updated successfully.", "9:45 AM"),
    NotificationItem(3, "Reminder", "Meeting in 15 minutes.", "8:00 AM")
)

@Composable
fun LockScreenNotification(notification: NotificationItem) {
    // Each notification is shown as a translucent card with rounded corners.
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Notification icon
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notification Icon",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            // Notification details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.timestamp,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.6f)
                    )
                )
            }
        }
    }
}

@Composable
fun LockScreenNotificationScreen(
    notifications: List<NotificationItem> = sampleLockScreenNotifications
) {
    // Full-screen container with a solid black background.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Digital clock display at the top
            Text(
                text = "12:45",  // You can dynamically update this clock.
                style = MaterialTheme.typography.displaySmall.copy(color = Color.White),
                modifier = Modifier.padding(top = 64.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            // List of notifications
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications) { notification ->
                    LockScreenNotification(notification)
                }
            }
        }
    }
}

