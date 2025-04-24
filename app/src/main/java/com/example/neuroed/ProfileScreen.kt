package com.example.neuroed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.navigation.NavController
import com.example.neuroed.model.AttendanceData
import com.example.neuroed.model.SubjectProgress
import com.example.neuroed.model.UserProfile
import com.example.neuroed.network.ApiService
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.AttendanceRepository
import com.example.neuroed.repository.LearningProgressRepository
import com.example.neuroed.repository.UserProfileRepository
import com.example.neuroed.viewmodel.AttendanceViewModel
import com.example.neuroed.viewmodel.AttendanceViewModelFactory
import com.example.neuroed.viewmodel.LearningProgressViewModel
import com.example.neuroed.viewmodel.LearningProgressViewModelFactory
import com.example.neuroed.viewmodel.UserProfileViewModel
import com.example.neuroed.viewmodel.UserProfileViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin








@Composable
fun ProfileScreen(navController: NavController) {
    // Scroll state for when content exceeds screen height
    val scrollState = rememberScrollState()

    // Theme colors
    val primaryGreen = Color(0xFF9CCC65)
    val secondaryBlue = Color(0xFF64B5F6)
    val accentOrange = Color(0xFFFFB74D)
    val accentPurple = Color(0xFFBA68C8)
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1D1D1D)


    val apiService = remember { RetrofitClient.apiService }
    val repository = remember { UserProfileRepository(apiService) }

    // 2) Obtain your ViewModel, passing userId = "1"
    val viewModel: UserProfileViewModel = viewModel(
        factory = UserProfileViewModelFactory(repository, 1)
    )

    // 3) Observe the LiveData as Compose state
    val profile by viewModel.userProfile.observeAsState()

    val attendanceApi  = remember { RetrofitClient.apiService }
    val attendanceRepo = remember { AttendanceRepository(attendanceApi) }
    val attendanceVm: AttendanceViewModel = viewModel(
        factory = AttendanceViewModelFactory(attendanceRepo)
    )

    val attendanceData by attendanceVm.attendance.collectAsState()
    val isLoading      by attendanceVm.isLoading.collectAsState()
    val errorMessage   by attendanceVm.errorMessage.collectAsState()

    // ❶ When the screen first appears, mark today and then load the month
    LaunchedEffect(Unit) {
        val today = java.time.LocalDate.now()
        val month = today.month
            .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())
        val year  = today.year

        // 2) GET → fetch the updated attendance for display
        attendanceVm.loadAttendance(userId = 1, month = month, year = year)
    }


    val progressapiService = remember { RetrofitClient.apiService }
    val progressRepo = remember { LearningProgressRepository(progressapiService) }
    //  — viewModel
    val progressVm: LearningProgressViewModel = viewModel(
        factory = LearningProgressViewModelFactory(progressRepo, /* userId = */ 1)
    )
    val learningProgress by progressVm.learningProgress.observeAsState()



    // Overall background is black
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // 1) Top user info section with a gradient background
            TopUserInfoSection(profile)

            // 2) Motivation quote banner
            Spacer(modifier = Modifier.height(16.dp))
            MotivationBanner()

            // 3) Tab row for different views
            Spacer(modifier = Modifier.height(16.dp))
            ProfileTabs()

            // 4) Learning progress card with streak
            Spacer(modifier = Modifier.height(16.dp))
            learningProgress?.let { data ->
                LearningProgressCard(
                    overallProgress = data.overallCompletionPercentage,
                    subjects = data.subjectsProgress,
                    primaryColor = primaryGreen
                )
            } ?: Text(
                text = "Loading progress…",
                color = primaryGreen,
                modifier = Modifier
                    .padding(16.dp)
            )

            // 5) Quick stats row
            Spacer(modifier = Modifier.height(16.dp))
            QuickStatsRow(
                primaryGreen   = primaryGreen,
                secondaryBlue  = secondaryBlue,
                accentOrange   = accentOrange,
                accentPurple   = accentPurple,
                streakDays     = attendanceData?.currentStreak ?: 0
            )
            // 6) Attendance calendar view
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoading) {
                Text("Loading attendance…", color = primaryGreen)
            } else if (errorMessage != null) {
                Text("Error: $errorMessage", color = Color.Red)
            } else if (attendanceData != null) {
                AttendanceCalendarView(
                    attendance   = attendanceData!!,
                    primaryColor = primaryGreen
                )
            }


            // 7) Monthly progress chart
            Spacer(modifier = Modifier.height(16.dp))
            MonthlyProgressChart(cardBackground, primaryGreen)

            // 8) Achievement badges
            Spacer(modifier = Modifier.height(16.dp))
            AchievementBadges(accentOrange)

            // 9) Learning buddies - moved up to replace removed sections
            Spacer(modifier = Modifier.height(16.dp))
            LearningBuddiesSection()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


@Composable
fun TopUserInfoSection(
    profile: UserProfile?
) {
    // A rounded surface at the top with gradient
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1976D2),
                            Color(0xFF42A5F5)
                        )
                    )
                )
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
                        onClick = { /* Open settings */ },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Profile info with level indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // User avatar with level badge
                    Box {
                        AsyncImage(
                            model = profile?.avatarUrl ?: "",
                            contentDescription = "User Avatar",
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                        )
                        // Level badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 4.dp, y = 4.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFB74D))
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile?.level?.toString() ?: "0",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Name, email, and progress bar
                    Column {
                        Text(
                            text = profile?.user_name ?: "",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = profile?.user_email ?: "",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // XP Progress to next level
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val progressFraction = if (profile != null && profile.maxXpForLevel > 0) {
                                profile.currentXp.toFloat() / profile.maxXpForLevel.toFloat()
                            } else 0f

                            // XP progress bar
                            LinearProgressIndicator(
                                progress = progressFraction,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // XP Text
                            Text(
                                text = "${profile?.currentXp ?: 0}/${profile?.maxXpForLevel ?: 0} XP",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MotivationBanner() {
    val quotes = listOf(
        "Every day is a new opportunity to learn and grow!",
        "Small steps lead to big achievements.",
        "Consistency is the key to mastery.",
        "Your future self will thank you for the effort today."
    )

    var currentQuoteIndex by remember { mutableStateOf(0) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable {
                // Cycle through quotes on click
                currentQuoteIndex = (currentQuoteIndex + 1) % quotes.size
            },
        color = Color(0xFF6A1B9A), // Deep purple
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Motivation",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = quotes[currentQuoteIndex],
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ProfileTabs() {
    // Tab row for Stats, Progress, Achievements
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Status", "Progress", "Achievement")

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
fun LearningProgressCard(
    overallProgress: Float,
    subjects: List<SubjectProgress>,
    primaryColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = Color(0xFF1C1C1C),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Learning Progress",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "You're making great progress!",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
                // Streak (hard-coded for now)
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Streak",
                            tint = Color(0xFFFF8F00),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "12",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = "Day Streak",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // ① Circular overall progress
                val animated = animateFloatAsState(
                    targetValue = overallProgress / 100f,
                    animationSpec = tween(1500, easing = FastOutSlowInEasing)
                ).value

                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(80.dp)) {
                        drawArc(
                            color = Color(0xFF333333),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 10f,
                                cap = StrokeCap.Round
                            )
                        )
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = 360f * animated,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 10f,
                                cap = StrokeCap.Round
                            )
                        )
                    }
                    Text(
                        text = "${(animated * 100).toInt()}%",
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // ② Scrollable list of subject bars
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    subjects.forEach { subj ->
                        ProgressCategory(
                            title = subj.subjectName,
                            progress = subj.completionPercentage / 100f,
                            color = Color(subj.color)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}



@Composable
fun ProgressCategory(title: String, progress: Float, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            // Provide your Float via a lambda
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF333333)  // replaces backgroundColor
        )
    }
}


@Composable
fun QuickStatsRow(primaryGreen: Color, secondaryBlue: Color, accentOrange: Color, accentPurple: Color, streakDays: Int   ) {
    // A row with multiple small stat cards with icons that properly fits the screen
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp), // Reduced horizontal padding
        horizontalArrangement = Arrangement.spacedBy(8.dp) // Even spacing between items
    ) {
        StatCard(
            modifier = Modifier.weight(1f), // Use weight to distribute space evenly
            icon = Icons.Default.Person,
            title =  "$streakDays days",
            subtitle = "Streak",
            color = accentOrange
        )

        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Person,
            title = "99 min",
            subtitle = "Today",
            color = secondaryBlue
        )

        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Check,
            title = "6/24",
            subtitle = "Activities",
            color = primaryGreen
        )

        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Star,
            title = "4/100",
            subtitle = "Trophies",
            color = accentPurple
        )
    }
}

// You'll need to update your StatCard function to accept a modifier parameter
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color
) {
    // Your existing StatCard implementation, but with the modifier parameter added
    Card(
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(1f), // Makes cards square for consistency
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun <ImageVector : Any> StatCard(icon: ImageVector, title: String, subtitle: String, color: Color) {
    Surface(
        color = Color(0xFF1C1C1C),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(85.dp)
            .height(85.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = R.drawable.man,
                contentDescription = subtitle,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun <ImageVector> Icon(imageVector: ImageVector, contentDescription: String, tint: Color, modifier: Modifier) {

}




@Composable
fun AttendanceIndicator(count: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}




@Composable
fun DayIndicator(
    day: Int,
    isPresent: Boolean,
    isToday: Boolean,
    isAbsent: Boolean,
    primaryColor: Color
) {
    val backgroundColor = when {
        isToday -> primaryColor
        isPresent -> Color(0xFF333333)
        isAbsent -> Color(0xFF2C2C2C) // Slightly darker for absent days
        else -> Color(0xFF1C1C1C)
    }

    val textColor = when {
        isToday -> Color.Black
        isPresent -> primaryColor
        isAbsent -> Color(0xFFFF5252) // Red for absent days
        else -> Color.Gray
    }

    val borderColor = when {
        isToday -> primaryColor
        isAbsent -> Color(0xFFFF5252).copy(alpha = 0.5f) // Light red border for absent
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(if (isAbsent) 1.dp else (if (isToday) 1.dp else 0.dp), borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (isAbsent) {
            // Show X for absent days
            Canvas(modifier = Modifier.size(18.dp)) {
                drawLine(
                    color = Color(0xFFFF5252),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0xFFFF5252),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2f
                )
            }
        }

        Text(
            text = day.toString(),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun MonthlyProgressChart(cardBackground: Color, primaryColor: Color) {
    // Monthly progress chart with improved UI
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = cardBackground,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Progress",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = "Watch your growth over time",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }

                // Toggle view buttons or filter
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF333333))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var selectedView by remember { mutableStateOf(0) }
                    val views = listOf("Days", "Hours")

                    views.forEachIndexed { index, text ->
                        Text(
                            text = text,
                            color = if (selectedView == index) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = if (selectedView == index) FontWeight.Medium else FontWeight.Normal,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { selectedView = index }
                                .background(if (selectedView == index) Color(0xFF666666) else Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Improved bar chart with value labels
            val monthlyData = listOf(0.2f, 0.5f, 0.3f, 0.7f, 0.6f, 0.85f, 0.4f)
            val months = listOf("Nov", "Dec", "Jan", "Feb", "Mar", "Apr", "May")
            val hoursSpent = listOf(4, 10, 6, 14, 12, 17, 8) // Hours corresponding to the progress

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Increased height for better visualization
                    .padding(bottom = 24.dp, top = 24.dp) // Added top padding for value labels
            ) {
                // Chart grid lines with labels
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val widthStep = size.width / (monthlyData.size + 1)

                    // Horizontal grid lines with percentage labels
                    for (i in 0..4) {
                        val y = size.height * (1 - i * 0.25f)
                        drawLine(
                            color = Color(0xFF333333),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )

                        // Grid line labels
                        drawContext.canvas.nativeCanvas.drawText(
                            "${i * 25}%",
                            -30f,
                            y + 5f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = 30f
                                textAlign = android.graphics.Paint.Align.RIGHT
                            }
                        )
                    }
                }

                // Bars with labels
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    monthlyData.forEachIndexed { index, value ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            // Value label
                            Text(
                                text = "${hoursSpent[index]}h",
                                color = if (index == monthlyData.size - 1) primaryColor else Color.Gray,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            // Bar
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height((140 * value).dp)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        if (index == monthlyData.size - 1) primaryColor
                                        else primaryColor.copy(alpha = 0.5f)
                                    )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Month label
                            Text(
                                text = months[index],
                                color = if (index == monthlyData.size - 1) Color.White else Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementBadges(accentColor: Color) {
    // Achievement badges section
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = Color(0xFF1C1C1C),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Achievements",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )

                Text(
                    text = "View All",
                    color = accentColor,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { /* View all achievements */ }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Achievement badges
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(4) { index ->
                    val badges = listOf(
                        "First Quiz" to Icons.Default.PlayArrow,
                        "5 Day Streak" to Icons.Default.PlayArrow,
                        "Early Bird" to Icons.Default.PlayArrow,
                        "Quiz Master" to Icons.Default.PlayArrow
                    )

                    val (name, icon) = badges[index]

                    AchievementBadge(
                        name = name,
                        icon = icon,
                        color = when (index) {
                            0 -> Color(0xFF64B5F6) // Blue
                            1 -> Color(0xFFFFB74D) // Orange
                            2 -> Color(0xFFFFD54F) // Amber
                            else -> Color(0xFF9CCC65) // Green
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementBadge(name: String, icon: ImageVector, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        // Badge icon
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(0xFF333333)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = color,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Badge name
        Text(
            text = name,
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}




@Composable
fun YearlyStat(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 13.sp
        )

        Text(
            text = value,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun LearningBuddiesSection() {
    // Learning buddies section
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = Color(0xFF1C1C1C),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Learning Buddies",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add buddy",
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { /* Add buddy action */ }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Learn together, achieve more!",
                color = Color.Gray,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Learning buddies list
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(5) { index ->
                    LearningBuddyItem(
                        name = when (index) {
                            0 -> "Alex K."
                            1 -> "Jamie T."
                            2 -> "Morgan S."
                            3 -> "Sam B."
                            else -> "Add New"
                        },
                        isAddNew = index == 4
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Challenge a buddy button
            Button(
                onClick = { /* Challenge action */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF673AB7) // Purple
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Challenge",
                    tint = Color.White
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Challenge a Buddy",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun LearningBuddyItem(name: String, isAddNew: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp)
    ) {
        // Avatar or add button
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    if (isAddNew) Color(0xFF333333) else Color(0xFF64B5F6).copy(alpha = 0.2f)
                )
                .border(
                    width = 2.dp,
                    color = if (isAddNew) Color(0xFF64B5F6) else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isAddNew) {
                // Add icon
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add buddy",
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                // Avatar placeholder with initials
                Text(
                    text = name.split(" ").map { it.first() }.joinToString(""),
                    color = Color(0xFF64B5F6),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Name
        Text(
            text = name,
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}




@Composable
fun AttendanceCalendarView(
    attendance: AttendanceData,
    primaryColor: Color
) {
    var showingCurrentMonth by remember { mutableStateOf(true) }

    // Pull out current/previous month
    val cm = attendance.currentMonth
    val pm = attendance.previousMonth

    // Labels
    val currentMonthName = cm.month
    val currentYear      = cm.year
    val prevMonthName    = pm?.month.orEmpty()
    val prevYear         = pm?.year ?: currentYear

    // Today’s day-of-month
    val todayInMonth = cm.currentDay ?: 0

    // Day-sets
    val currPresent = cm.presentDays.toSet()
    val prevPresent = pm?.presentDays?.toSet().orEmpty()

    // ─── Calculate weekday offset ───────────────────────────────
    fun monthNameToInt(name: String): Int {
        val sdf = SimpleDateFormat("MMM", Locale.getDefault())
        val date = sdf.parse(name) ?: throw IllegalArgumentException("Invalid month: $name")
        return Calendar.getInstance().apply { time = date }
            .get(Calendar.MONTH) + 1
    }

    val monthInt  = monthNameToInt(currentMonthName)
    val totalDays = cm.totalDays

    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR,  currentYear)
        set(Calendar.MONTH, monthInt - 1)  // zero-based
        set(Calendar.DAY_OF_MONTH, 1)
    }
    // Sunday=1…Saturday=7 → Monday=0…Sunday=6
    val startOffset = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7

    val totalCells = startOffset + totalDays
    val rows       = (totalCells + 6) / 7
    // ─────────────────────────────────────────────────────────────

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = Color(0xFF1C1C1C),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title + Month Selector
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment   = Alignment.CenterVertically
            ) {
                Text(
                    text       = "Attendance Streak",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White
                )
                Row {
                    Text(
                        text       = if (prevMonthName.isNotEmpty()) prevMonthName else "—",
                        fontSize   = 12.sp,
                        fontWeight = if (!showingCurrentMonth) FontWeight.Medium else FontWeight.Normal,
                        color      = if (!showingCurrentMonth) Color.White else Color.Gray,
                        modifier   = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (!showingCurrentMonth && pm != null) Color(0xFF666666)
                                else Color.Transparent
                            )
                            .clickable(enabled = pm != null) { showingCurrentMonth = false }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text       = currentMonthName,
                        fontSize   = 12.sp,
                        fontWeight = if (showingCurrentMonth) FontWeight.Medium else FontWeight.Normal,
                        color      = if (showingCurrentMonth) Color.White else Color.Gray,
                        modifier   = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (showingCurrentMonth) Color(0xFF666666)
                                else Color.Transparent
                            )
                            .clickable { showingCurrentMonth = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Date display
            Text(
                text     = "$currentMonthName $todayInMonth, $currentYear",
                color    = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(Modifier.height(12.dp))

            // Days-of-week header
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun").forEach { d ->
                    Text(
                        text      = d,
                        color     = Color.Gray,
                        fontSize  = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.width(32.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Calendar Grid
            for (week in 0 until rows) {
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0..6) {
                        val index = week * 7 + col
                        val day   = index - startOffset + 1

                        if (day in 1..totalDays) {
                            DayIndicator(
                                day          = day,
                                isPresent    = if (showingCurrentMonth) day in currPresent else day in prevPresent,
                                isToday      = showingCurrentMonth && day == todayInMonth,
                                isAbsent     = (showingCurrentMonth && day < todayInMonth && day !in currPresent)
                                        || (!showingCurrentMonth && day !in prevPresent),
                                primaryColor = primaryColor
                            )
                        } else {
                            Spacer(modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Streak Summary
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AttendanceIndicator(
                    count = "${(if (showingCurrentMonth) currPresent.size else prevPresent.size)}/$totalDays",
                    label = "Days this month",
                    color = primaryColor
                )
                AttendanceIndicator(
                    count = attendance.currentStreak.toString(),
                    label = "Current streak",
                    color = Color(0xFFFFB74D)
                )
                AttendanceIndicator(
                    count = attendance.longestStreak.toString(),
                    label = "Longest streak",
                    color = Color(0xFFFF8A65)
                )
            }
        }
    }
}



// Required for border extension
fun Modifier.border(
    width: Dp,
    color: Color,
    shape: androidx.compose.ui.graphics.Shape
) = composed {
    Modifier
        .drawBehind {
            drawRect(
                color = color,
                size = Size(size.width, size.height),
                style = Stroke(width = width.toPx())
            )
        }
        .then(Modifier)
}
