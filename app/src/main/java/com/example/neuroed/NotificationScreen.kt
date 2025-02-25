package com.example.neuroed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
