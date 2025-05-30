package com.example.neuroed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Build
//import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.neuroed.viewmodel.NotificationViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.neuroed.repository.NotificationRepository
import com.example.neuroed.viewmodel.NotificationViewModelFactory
import kotlinx.coroutines.delay

// Define custom colors for better visual appeal
object NotificationColors {
    val primaryBlue = Color(0xFF3A86FF)
    val secondaryBlue = Color(0xFF8ECDFF)
    val lightBlue = Color(0xFFE0F0FF)
    val successGreen = Color(0xFF38B000)
    val warningOrange = Color(0xFFFFA500)
    val importantRed = Color(0xFFE63946)
    val neutralGray = Color(0xFF9CA3AF)
    val darkText = Color(0xFF1F2937)
    val lightText = Color(0xFF6B7280)
    val backgroundLight = Color(0xFFF9FAFB)
    val surfaceLight = Color(0xFFFFFFFF)
    val unreadBadge = Color(0xFFFF4D6D)
}

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

    // Trigger API call when this composable is first composed
    LaunchedEffect(Unit) {
        viewModel.fetchNotifications()
    }

    // Collect notifications from StateFlow as state
    val notificationsState = viewModel.notifications.collectAsState()

    // Static list of notifications with different types
    val notifications = listOf(
        NotificationData(
            iconRes = R.drawable.notificationbell,
            title = "Study Reminder",
            message = "Complete your daily learning goal - only 2 modules left!",
            date = "10:30 AM",
            isUnread = true,
            type = NotificationType.REMINDER
        ),
        NotificationData(
            iconRes = R.drawable.notificationbell,
            title = "Quiz Result",
            message = "Congratulations! You scored 92% on your Neural Networks quiz.",
            date = "Yesterday",
            isUnread = true,
            type = NotificationType.SUCCESS
        ),
        NotificationData(
            iconRes = R.drawable.notificationbell,
            title = "New Course Available",
            message = "Advanced Machine Learning techniques course is now available.",
            date = "Apr 26",
            isUnread = false,
            type = NotificationType.COURSE
        ),
        NotificationData(
            iconRes = R.drawable.notificationbell,
            title = "Forum Activity",
            message = "John replied to your question in the AI Ethics forum.",
            date = "Apr 25",
            isUnread = false,
            type = NotificationType.DISCUSSION
        ),
        NotificationData(
            iconRes = R.drawable.notificationbell,
            title = "Badge Earned",
            message = "You've earned the 'Deep Learning Explorer' badge! Keep up the great work.",
            date = "Apr 24",
            isUnread = false,
            type = NotificationType.ACHIEVEMENT
        ),
        NotificationData(
            iconRes = R.drawable.notificationbell,
            title = "Important Update",
            message = "Please update your profile information to continue accessing all features.",
            date = "Apr 23",
            isUnread = false,
            type = NotificationType.IMPORTANT
        )
    )

    // Categories for notifications
    val categories = listOf("All", "Unread", "Courses", "Discussions", "Reminders")
    var selectedCategory by remember { mutableStateOf("All") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notifications",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = NotificationColors.darkText
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(NotificationColors.lightBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = NotificationColors.primaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NotificationColors.surfaceLight,
                    titleContentColor = NotificationColors.darkText
                )
            )
        },
        containerColor = NotificationColors.backgroundLight
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(NotificationColors.backgroundLight)
        ) {
            if (notifications.isEmpty()) {
                EmptyNotificationsState(onHistoricalClick)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Notification categories tabs with custom styling
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 2.dp,
                        color = NotificationColors.surfaceLight
                    ) {
                        ScrollableTabRow(
                            selectedTabIndex = categories.indexOf(selectedCategory),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            edgePadding = 16.dp,
                            divider = {},
                            containerColor = NotificationColors.surfaceLight,
                            contentColor = NotificationColors.primaryBlue,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    modifier = Modifier
                                        .tabIndicatorOffset(tabPositions[categories.indexOf(selectedCategory)])
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
                                    color = NotificationColors.primaryBlue
                                )
                            }
                        ) {
                            categories.forEachIndexed { index, category ->
                                Tab(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    text = {
                                        Text(
                                            text = category,
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                fontWeight = if (selectedCategory == category)
                                                    FontWeight.Bold else FontWeight.Medium
                                            )
                                        )
                                    },
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    selectedContentColor = NotificationColors.primaryBlue,
                                    unselectedContentColor = NotificationColors.neutralGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Notification header with count and clear all button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = NotificationColors.primaryBlue.copy(alpha = 0.1f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${notifications.size}",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NotificationColors.primaryBlue
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Notifications",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = NotificationColors.darkText
                                )
                            )
                        }

                        Button(
                            onClick = { /* Clear all notifications action */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NotificationColors.lightBlue,
                                contentColor = NotificationColors.primaryBlue
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear All",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Clear All",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Notification list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        itemsIndexed(notifications) { index, notification ->
                            // Animation delay based on index
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(index * 50L)
                                visible = true
                            }

                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(animationSpec = tween(400)) +
                                        slideInVertically(
                                            initialOffsetY = { it / 2 },
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        )
                            ) {
                                EnhancedNotificationItem(notification)
                            }

                            if (index < notifications.size - 1) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }

                // Historical notifications button at the bottom with gradient
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    NotificationColors.backgroundLight.copy(alpha = 0.8f),
                                    NotificationColors.backgroundLight
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = onHistoricalClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NotificationColors.primaryBlue
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View Historical Notifications",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedNotificationItem(notification: NotificationData) {
    val notificationColor = when (notification.type) {
        NotificationType.REMINDER -> NotificationColors.warningOrange
        NotificationType.SUCCESS -> NotificationColors.successGreen
        NotificationType.IMPORTANT -> NotificationColors.importantRed
        NotificationType.COURSE -> NotificationColors.primaryBlue
        NotificationType.DISCUSSION -> NotificationColors.secondaryBlue
        NotificationType.ACHIEVEMENT -> NotificationColors.warningOrange
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (notification.isUnread) 4.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = notificationColor.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (notification.isUnread) 1.5.dp else 0.dp,
                color = if (notification.isUnread) notificationColor.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { /* Handle notification click */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isUnread)
                NotificationColors.surfaceLight
            else
                NotificationColors.surfaceLight.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left color indicator for notification type
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(notificationColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Notification icon with colored background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        notificationColor.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = notification.iconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.Center
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Notification title with type indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = if (notification.isUnread) FontWeight.Bold else FontWeight.SemiBold,
                            color = NotificationColors.darkText
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (notification.isUnread) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(NotificationColors.unreadBadge)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Notification message
                Text(
                    text = notification.message,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = NotificationColors.lightText,
                        lineHeight = 20.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom row with date and action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date with small icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = NotificationColors.neutralGray.copy(alpha = 0.1f),
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.notificationbell),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = NotificationColors.neutralGray
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = notification.date,
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = NotificationColors.neutralGray
                            )
                        )
                    }

                    // Mark as read button (only for unread notifications)
                    if (notification.isUnread) {
                        TextButton(
                            onClick = { /* Mark as read */ },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = notificationColor
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Mark as read",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Mark as read",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
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
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Empty state illustration with gradient
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NotificationColors.lightBlue,
                            NotificationColors.lightBlue.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Notifications,
                contentDescription = "No notifications",
                modifier = Modifier
                    .size(80.dp),
                tint = NotificationColors.primaryBlue
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "No Notifications Yet",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = NotificationColors.darkText
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "We'll notify you when there's something new to see here.",
            style = TextStyle(
                fontSize = 16.sp,
                color = NotificationColors.lightText,
                lineHeight = 24.sp
            ),
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onHistoricalClick,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NotificationColors.primaryBlue
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Build,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "View Historical Notifications",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

// Notification types to determine colors and styling
enum class NotificationType {
    REMINDER,
    SUCCESS,
    IMPORTANT,
    COURSE,
    DISCUSSION,
    ACHIEVEMENT
}

// Enhanced data class for notifications
data class NotificationData(
    val iconRes: Int,
    val title: String,
    val message: String,
    val date: String,
    val isUnread: Boolean = false,
    val type: NotificationType = NotificationType.REMINDER
)