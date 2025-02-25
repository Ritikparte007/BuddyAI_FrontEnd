package com.example.neuroed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage

data class ReelItem(
    val imageUrl: String,
    val skillLevel: String,  // Unused in this UI
    val title: String,
    val authorName: String,
    val daysAgo: String,
    val description: String
)

@Composable
fun ReelsScreen(navController: NavController) {
    // Sample data
    val reels = listOf(
        ReelItem(
            imageUrl = "https://images.unsplash.com/photo-1558981285-6f0c94958bb6",
            skillLevel = "BEGINNER",
            title = "Skateboard jump step by step",
            authorName = "Samantha William",
            daysAgo = "2 days ago",
            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit..."
        ),
        ReelItem(
            imageUrl = "https://images.unsplash.com/photo-1590069978863-43a1e6a18c34",
            skillLevel = "ADVANCED",
            title = "Street Skating Basics",
            authorName = "John Carter",
            daysAgo = "5 days ago",
            description = "Etiam at metus in urna placerat feugiat at vitae arcu..."
        )
        // Add more items as needed
    )

    // Get the device screen height in dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    // Vertical scroll for reels, each item is full screen height
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(reels) { reel ->
            ReelCard(
                reel = reel,
                itemHeight = screenHeight
            )
        }
    }
}

@Composable
fun ReelCard(
    reel: ReelItem,
    itemHeight: Dp
) {
    // Local state to show/hide the comment section overlay
    var showCommentSection by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight)
            .background(Color.Black)
    ) {
        // Background image (the reel content)
        AsyncImage(
            model = reel.imageUrl,
            contentDescription = "Reel background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient overlay at the bottom to improve text contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black),
                        startY = 300f
                    )
                )
        )

        // Bottom overlay content
        // Left: Title, author, days ago, description.
        // Right: Action icons.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(end = 56.dp)
            ) {
                Text(
                    text = reel.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${reel.authorName}, ${reel.daysAgo}",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = reel.description,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 3
                )
            }

            // Right side: action icons
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ActionIcon(icon = Icons.Default.ThumbUp, contentDescription = "Like") {
                    // Handle like action here
                }
                ActionIcon(icon = Icons.Default.ThumbUp, contentDescription = "Dislike") {
                    // Handle dislike action here
                }
                ActionIcon(icon = Icons.Default.Edit, contentDescription = "Comment") {
                    // When Comment icon is clicked, show comment section
                    showCommentSection = true
                }
            }
        }

        // Overlay comment section if needed
        if (showCommentSection) {
            CommentSection(onDismiss = { showCommentSection = false })
        }
    }
}

@Composable
fun ActionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .background(Color(0x55FFFFFF)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White
        )
    }
}

@Composable
fun CommentSection(onDismiss: () -> Unit) {
    // This overlay covers the entire screen with a semi-transparent background.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
    ) {
        // Comment content displayed at the bottom.
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.White)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Comments",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Dummy comments
            Text(text = "User1: Great reel!", color = Color.Black)
            Text(text = "User2: Awesome!", color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))
            // A "Close" action to dismiss the comment section
            Text(
                text = "Close",
                color = Color.Blue,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { onDismiss() }
            )
        }
    }
}
