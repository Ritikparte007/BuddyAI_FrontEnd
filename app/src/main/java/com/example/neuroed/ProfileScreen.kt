package com.example.neuroed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import androidx.navigation.NavController

@Composable
fun ProfileScreen(navController: NavController) {
    // Scroll state for when content exceeds screen height
    val scrollState = rememberScrollState()

    // Overall background is black
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // 1) Top user info section with a rounded white background
            TopUserInfoSection()

            // 2) Tab row for Stats, Profile, Security
            Spacer(modifier = Modifier.height(16.dp))
            ProfileTabs()

            // 3) Learning progress card
            Spacer(modifier = Modifier.height(16.dp))
            LearningProgressCard()

            // 4) Quick stats row (daily streak, time spent, activities, trophies)
            Spacer(modifier = Modifier.height(16.dp))
            QuickStatsRow()

            // 5) Monthly progress or milestones
            Spacer(modifier = Modifier.height(16.dp))
            MonthlyProgressSection()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TopUserInfoSection() {
    // A rounded white surface at the top
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            // Row with a settings icon in the top-right
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { /* TODO: open settings */ },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Settings",
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // User avatar, name, email
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User avatar
                AsyncImage(
                    model = "https://images.pexels.com/photos/35537/child-children-girl-happy.jpg?auto=compress&cs=tinysrgb&w=600",
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))

                // Name & email
                Column {
                    Text(
                        text = "Marta Smith",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "marta.real@gmail.com",
                        color = Color.DarkGray,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileTabs() {
    // Simulate a tab row for Stats, Profile, Security
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Stats", "Profile", "Security")

    // Dark background behind the tabs
    // (the screen behind is black, so let's use a dark surface)
    Surface(
        color = Color(0xFF1C1C1C),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = (index == selectedTab)
                Text(
                    text = title,
                    color = if (isSelected) Color.White else Color.Gray,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF333333) else Color.Transparent)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { selectedTab = index }
                )
            }
        }
    }
}

@Composable
fun LearningProgressCard() {
    // Card showing learning progress
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = Color(0xFF1C1C1C),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Learning progress",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            // e.g., 60% progress
            Text(
                text = "60%",
                color = Color(0xFF9CCC65), // a greenish color
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tracking your crypto knowledge journey",
                color = Color.Gray,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = 0.6f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF9CCC65),
                trackColor = Color(0xFF333333)
            )
        }
    }
}

@Composable
fun QuickStatsRow() {
    // A row with multiple small stat cards: daily streak, time spent, activities, trophies
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatCard(title = "12", subtitle = "Daily streak")
        StatCard(title = "99 min", subtitle = "Time spent")
        StatCard(title = "6/24", subtitle = "Activities")
        StatCard(title = "4/100", subtitle = "Trophies")
    }
}

@Composable
fun StatCard(title: String, subtitle: String) {
    Surface(
        color = Color(0xFF1C1C1C),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .size(width = 80.dp, height = 80.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                color = Color(0xFF9CCC65),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun MonthlyProgressSection() {
    // "May 2024" and some milestone icons or a timeline
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = Color(0xFF1C1C1C),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "May 2024",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                text = "Visualizing your learning milestones",
                color = Color.Gray,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Example row of icons or placeholders for days or milestones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MilestoneBox("1")
                MilestoneBox("2")
                MilestoneBox("3")
                MilestoneBox("4")
                MilestoneBox("5")
            }
        }
    }
}

@Composable
fun MilestoneBox(day: String) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF333333)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            color = Color(0xFF9CCC65),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
