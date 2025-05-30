package com.example.neuroed

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
//import androidx.compose.material.icons.filled.EmojiEvents
//import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
//import androidx.compose.material.icons.filled.Psychology
//import androidx.compose.material.icons.filled.Quiz
//import androidx.compose.material.icons.filled.School
//import androidx.compose.material.icons.filled.SelfImprovement
//import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class ActivityPerformance(
    val day: String,
    val test: Float,
    val task: Float,
    val exam: Float,
    val recallAssignment: Float,
    val challenge: Float,
    val game: Float,
    val meditation: Float,
    val activitiesCompleted: Int
)

data class ActivityStatCard(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon:  Int,
    val color: Color,
    val percentage: Float
)

data class ActivityType(
    val name: String,
    val iconRes: Int, // Changed from ImageVector to Int for drawable resource
    val color: Color,
    val score: Float,
    val completedToday: Int
)

@Composable
fun MatricScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    // Sample comprehensive performance data for the last 7 days
    val performanceData = listOf(
        ActivityPerformance("Mon", 0.85f, 0.78f, 0.82f, 0.75f, 0.88f, 0.92f, 0.95f, 8),
        ActivityPerformance("Tue", 0.72f, 0.85f, 0.79f, 0.68f, 0.82f, 0.89f, 0.91f, 9),
        ActivityPerformance("Wed", 0.88f, 0.75f, 0.85f, 0.82f, 0.78f, 0.95f, 0.88f, 7),
        ActivityPerformance("Thu", 0.65f, 0.68f, 0.72f, 0.70f, 0.85f, 0.87f, 0.92f, 6),
        ActivityPerformance("Fri", 0.79f, 0.85f, 0.88f, 0.75f, 0.92f, 0.91f, 0.96f, 10),
        ActivityPerformance("Sat", 0.91f, 0.88f, 0.95f, 0.89f, 0.87f, 0.98f, 0.94f, 12),
        ActivityPerformance("Sun", 0.76f, 0.82f, 0.78f, 0.72f, 0.85f, 0.93f, 0.97f, 8)
    )

    // Calculate comprehensive statistics
    val avgTest = (performanceData.map { it.test }.average() * 100).toInt()
    val avgTask = (performanceData.map { it.task }.average() * 100).toInt()
    val avgExam = (performanceData.map { it.exam }.average() * 100).toInt()
    val avgRecall = (performanceData.map { it.recallAssignment }.average() * 100).toInt()
    val avgChallenge = (performanceData.map { it.challenge }.average() * 100).toInt()
    val avgGame = (performanceData.map { it.game }.average() * 100).toInt()
    val avgMeditation = (performanceData.map { it.meditation }.average() * 100).toInt()
    val totalActivities = performanceData.sumOf { it.activitiesCompleted }
    val overallAvg = ((avgTest + avgTask + avgExam + avgRecall + avgChallenge + avgGame + avgMeditation) / 7)

    // Define activity types with their current performance
    val activityTypes = listOf(
        ActivityType("Test", R.drawable.test, Color(0xFF2196F3), avgTest / 100f, 3),
        ActivityType("Task", R.drawable.task, Color(0xFF4CAF50), avgTask / 100f, 5),
        ActivityType("Exam", R.drawable.exam, Color(0xFFFF9800), avgExam / 100f, 2),
        ActivityType("Recall", R.drawable.recall, Color(0xFF9C27B0), avgRecall / 100f, 4),
        ActivityType("Challenge", R.drawable.writing, Color(0xFFE91E63), avgChallenge / 100f, 3),
        ActivityType("Game", R.drawable.game, Color(0xFF00BCD4), avgGame / 100f, 6),
        ActivityType("Meditation", R.drawable.meditation, Color(0xFF8BC34A), avgMeditation / 100f, 2)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Overall Performance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Learning Matrix",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Complete activity performance tracking",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Overall grade badge
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        overallAvg >= 90 -> Color(0xFF4CAF50)
                        overallAvg >= 80 -> Color(0xFF2196F3)
                        overallAvg >= 70 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when {
                            overallAvg >= 90 -> "A+"
                            overallAvg >= 80 -> "A"
                            overallAvg >= 70 -> "B+"
                            else -> "B"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "$overallAvg%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White
                        )
                    )
                }
            }
        }

        // Activity Statistics Cards
        val statsCards = listOf(
            ActivityStatCard(
                title = "Tests",
                value = "$avgTest%",
                subtitle = "Avg this week",
                icon = R.drawable.test,
                color = Color(0xFF2196F3),
                percentage = avgTest / 100f
            ),
            ActivityStatCard(
                title = "Tasks",
                value = "$avgTask%",
                subtitle = "Avg this week",
                icon = R.drawable.task,
                color = Color(0xFF4CAF50),
                percentage = avgTask / 100f
            ),
            ActivityStatCard(
                title = "Exams",
                value = "$avgExam%",
                subtitle = "Avg this week",
                icon = R.drawable.exam,
                color = Color(0xFFFF9800),
                percentage = avgExam / 100f
            ),
            ActivityStatCard(
                title = "Activities",
                value = totalActivities.toString(),
                subtitle = "Total this week",
                icon = R.drawable.clipboard,
                color = Color(0xFF9C27B0),
                percentage = 1.0f
            )
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(statsCards) { stat ->
                EnhancedStatisticCard(stat)
            }
        }

        // Activity Types Grid
        Text(
            text = "Activity Performance",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Activity performance grid
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(activityTypes) { activity ->
                ActivityPerformanceCard(activity)
            }
        }

        // Comprehensive Performance Chart
        Text(
            text = "Weekly Performance Trends",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        // All Activities Chart
        ComprehensiveChartCard(
            title = "All Activities Performance",
            subtitle = "Track performance across all learning activities",
            performanceData = performanceData
        )

        // Academic vs Wellness Activities
        ComparisonChartCard(
            title = "Academic vs Wellness Balance",
            subtitle = "Compare academic activities vs wellness activities",
            academicData = performanceData.map { (it.test + it.task + it.exam + it.recallAssignment + it.challenge) / 5 },
            wellnessData = performanceData.map { (it.game + it.meditation) / 2 },
            labels = performanceData.map { it.day }
        )

        // Recent Activity Breakdown - Last 3 Days
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Activity (Last 3 Days)",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "View All",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Show last 3 days specifically
            val last3Days = performanceData.takeLast(3)
            last3Days.forEach { data ->
                RecentDayActivityCard(data)
            }
        }

        // Enhanced Weekly Summary Card with trending
        WeeklySummaryCard(
            performanceData = performanceData,
            activityTypes = activityTypes
        )

        // Add Quick Actions Card
        QuickActionsCard()

        // Add Performance Insights Card
        PerformanceInsightsCard(performanceData)
    }
}

@Composable
fun QuickActionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    title = "Take Test",
                    iconRes = R.drawable.test, // Using drawable resource
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    title = "Start Task",
                    iconRes = R.drawable.task, // Using drawable resource
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    title = "Meditate",
                    iconRes = R.drawable.meditation, // Using drawable resource
                    color = Color(0xFF8BC34A),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    iconRes: Int, // Changed to drawable resource
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Use Image instead of Icon for drawable resources
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun PerformanceInsightsCard(performanceData: List<ActivityPerformance>) {
    val latestDay = performanceData.lastOrNull()
    val previousDay = performanceData.dropLast(1).lastOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Performance Insights",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (latestDay != null && previousDay != null) {
                val avgToday = (latestDay.test + latestDay.task + latestDay.exam +
                        latestDay.recallAssignment + latestDay.challenge +
                        latestDay.game + latestDay.meditation) / 7
                val avgYesterday = (previousDay.test + previousDay.task + previousDay.exam +
                        previousDay.recallAssignment + previousDay.challenge +
                        previousDay.game + previousDay.meditation) / 7
                val improvement = ((avgToday - avgYesterday) * 100).toInt()

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InsightRow(
                        icon = if (improvement >= 0) Icons.Default.Check else Icons.Default.Info,
                        text = if (improvement >= 0)
                            "You improved by $improvement% since yesterday! 🎉"
                        else
                            "Performance dipped by ${-improvement}%. Focus on weak areas.",
                        isPositive = improvement >= 0
                    )

                    val bestActivity = listOf(
                        "Test" to latestDay.test,
                        "Task" to latestDay.task,
                        "Exam" to latestDay.exam,
                        "Recall" to latestDay.recallAssignment,
                        "Challenge" to latestDay.challenge,
                        "Game" to latestDay.game,
                        "Meditation" to latestDay.meditation
                    ).maxByOrNull { it.second }

                    InsightRow(
                        icon = Icons.Default.Check,
                        text = "Your strongest area: ${bestActivity?.first} (${((bestActivity?.second ?: 0f) * 100).toInt()}%)",
                        isPositive = true
                    )

                    val weeklyTotal = performanceData.sumOf { it.activitiesCompleted }
                    InsightRow(
                        icon = Icons.Default.Person,
                        text = "Weekly goal: $weeklyTotal/49 activities completed",
                        isPositive = weeklyTotal >= 35
                    )
                }
            }
        }
    }
}

@Composable
fun InsightRow(
    icon: ImageVector,
    text: String,
    isPositive: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isPositive) Color(0xFF4CAF50) else Color(0xFFFF9800),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}


@Composable
fun EnhancedStatisticCard(stat: ActivityStatCard) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = stat.icon,
                    contentDescription = null.toString(),
                    tint = stat.color,
                    modifier = Modifier.size(20.dp)
                )
                // Progress indicator
                CircularProgressIndicator(
                    progress = stat.percentage,
                    modifier = Modifier.size(16.dp),
                    color = stat.color,
                    strokeWidth = 2.dp
                )
            }

            Column {
                Text(
                    text = stat.value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stat.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stat.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun ActivityPerformanceCard(activity: ActivityType) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = activity.color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Use Image instead of Icon for drawable resources
            Image(
                painter = painterResource(id = activity.iconRes),
                contentDescription = activity.name,
                modifier = Modifier.size(24.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${(activity.score * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = activity.color
                    )
                )
                Text(
                    text = activity.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${activity.completedToday} today",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun ComprehensiveChartCard(
    title: String,
    subtitle: String,
    performanceData: List<ActivityPerformance>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Legend for all activities
                LazyRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        listOf(
                            "Test" to Color(0xFF2196F3),
                            "Task" to Color(0xFF4CAF50),
                            "Exam" to Color(0xFFFF9800),
                            "Recall" to Color(0xFF9C27B0),
                            "Challenge" to Color(0xFFE91E63),
                            "Game" to Color(0xFF00BCD4),
                            "Meditation" to Color(0xFF8BC34A)
                        )
                    ) { (name, color) ->
                        LegendItem(name, color)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                ComprehensiveLineChart(
                    modifier = Modifier.fillMaxSize(),
                    performanceData = performanceData
                )
            }
        }
    }
}

@Composable
fun RecentDayActivityCard(data: ActivityPerformance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Day header with date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = data.day,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${data.activitiesCompleted} activities completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Daily average score
                val dailyAvg = ((data.test + data.task + data.exam + data.recallAssignment +
                        data.challenge + data.game + data.meditation) / 7 * 100).toInt()

                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            dailyAvg >= 90 -> Color(0xFF4CAF50)
                            dailyAvg >= 80 -> Color(0xFF2196F3)
                            dailyAvg >= 70 -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                    )
                ) {
                    Text(
                        text = "$dailyAvg%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Activity scores in a cleaner grid layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ActivityScoreRow("📝 Test", (data.test * 100).toInt(), Color(0xFF2196F3))
                    ActivityScoreRow("✅ Task", (data.task * 100).toInt(), Color(0xFF4CAF50))
                    ActivityScoreRow("🎓 Exam", (data.exam * 100).toInt(), Color(0xFFFF9800))
                    ActivityScoreRow("🧠 Recall", (data.recallAssignment * 100).toInt(), Color(0xFF9C27B0))
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ActivityScoreRow("🏆 Challenge", (data.challenge * 100).toInt(), Color(0xFFE91E63))
                    ActivityScoreRow("🎮 Game", (data.game * 100).toInt(), Color(0xFF00BCD4))
                    ActivityScoreRow("🧘 Meditation", (data.meditation * 100).toInt(), Color(0xFF8BC34A))

                    // Total activities indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📊 Total:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${data.activitiesCompleted}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklySummaryCard(
    performanceData: List<ActivityPerformance>,
    activityTypes: List<ActivityType>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Weekly Summary",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            val bestActivity = activityTypes.maxByOrNull { it.score }
            val totalActivities = performanceData.sumOf { it.activitiesCompleted }
            val avgPerformance = activityTypes.map { it.score }.average()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${(avgPerformance * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Avg Performance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = totalActivities.toString(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Total Activities",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = bestActivity?.name ?: "N/A",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Best Activity",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityScoreRow(label: String, score: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Small progress bar
            Box(
                modifier = Modifier
                    .width(30.dp)
                    .height(4.dp)
                    .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(score / 100f)
                        .background(color, RoundedCornerShape(2.dp))
                )
            }

            Text(
                text = "$score%",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
    }
}

@Composable
fun ComparisonChartCard(
    title: String,
    subtitle: String,
    academicData: List<Float>,
    wellnessData: List<Float>,
    labels: List<String>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LegendItem("Academic", Color(0xFF2196F3))
                    LegendItem("Wellness", Color(0xFF4CAF50))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                ComparisonLineChart(
                    modifier = Modifier.fillMaxSize(),
                    taskData = academicData,
                    examData = wellnessData,
                    labels = labels
                )
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ComprehensiveLineChart(
    modifier: Modifier = Modifier,
    performanceData: List<ActivityPerformance>
) {
    Canvas(modifier = modifier.padding(16.dp)) {
        val width = size.width
        val height = size.height

        if (performanceData.isEmpty()) return@Canvas

        // Draw grid lines
        val gridColor = Color.Gray.copy(alpha = 0.2f)
        for (i in 0 until performanceData.size) {
            val x = (width / (performanceData.size - 1)) * i
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1.dp.toPx()
            )
        }

        for (i in 0..4) {
            val y = (height / 4) * i
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw all activity lines
        drawDataLine(performanceData.map { it.test }, Color(0xFF2196F3), width, height)
        drawDataLine(performanceData.map { it.task }, Color(0xFF4CAF50), width, height)
        drawDataLine(performanceData.map { it.exam }, Color(0xFFFF9800), width, height)
        drawDataLine(performanceData.map { it.recallAssignment }, Color(0xFF9C27B0), width, height)
        drawDataLine(performanceData.map { it.challenge }, Color(0xFFE91E63), width, height)
        drawDataLine(performanceData.map { it.game }, Color(0xFF00BCD4), width, height)
        drawDataLine(performanceData.map { it.meditation }, Color(0xFF8BC34A), width, height)
    }
}

@Composable
fun ComparisonLineChart(
    modifier: Modifier = Modifier,
    taskData: List<Float>,
    examData: List<Float>,
    labels: List<String>
) {
    Canvas(modifier = modifier.padding(16.dp)) {
        val width = size.width
        val height = size.height

        if (taskData.isEmpty() || examData.isEmpty()) return@Canvas

        // Draw grid lines
        val gridColor = Color.Gray.copy(alpha = 0.2f)
        for (i in 0 until taskData.size) {
            val x = (width / (taskData.size - 1)) * i
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1.dp.toPx()
            )
        }

        for (i in 0..4) {
            val y = (height / 4) * i
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw both lines
        drawDataLine(taskData, Color(0xFF2196F3), width, height)
        drawDataLine(examData, Color(0xFF4CAF50), width, height)
    }
}

fun DrawScope.drawDataLine(
    data: List<Float>,
    color: Color,
    width: Float,
    height: Float
) {
    if (data.size < 2) return

    val path = Path()
    val stepX = width / (data.size - 1)

    // Move to first point
    path.moveTo(0f, height * (1f - data[0]))

    // Draw lines to subsequent points
    for (i in 1 until data.size) {
        val x = stepX * i
        val y = height * (1f - data[i])
        path.lineTo(x, y)
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 2.dp.toPx())
    )

    // Draw points
    for (i in data.indices) {
        val x = stepX * i
        val y = height * (1f - data[i])
        drawCircle(
            color = color,
            radius = 3.dp.toPx(),
            center = Offset(x, y)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MatricScreenPreview() {
    MaterialTheme {
        // MatricScreen(navController = rememberNavController())
    }
}