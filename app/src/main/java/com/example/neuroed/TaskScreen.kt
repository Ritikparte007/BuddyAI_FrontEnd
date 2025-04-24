package com.example.neuroed

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

// Define dark theme colors
val DarkBackgrounds = Color(0xFF121212)
val DarkSurfaces = Color(0xFF1E1E1E)
val DarkPrimary = Color(0xFF6B8AFD)
val DarkSecondary = Color(0xFF9C27B0)
val DarkOnBackground = Color(0xFFE1E1E1)
val DarkOnSurface = Color(0xFFE1E1E1)

// Task card background colors for dark mode
val DarkGreen = Color(0xFF1E3D2D)
val DarkBlue = Color(0xFF1A2B42)
val DarkRed = Color(0xFF3D2828)
val DarkOrange = Color(0xFF34281E)
val DarkPurple = Color(0xFF2D1A42)

enum class TaskCategory {
    TASK_LIST, COMPLETED, IN_PROGRESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(navController: NavController) {
    // Generate 14 past days in descending order so that index 0 is today.
    val selectedDayIndex = remember { mutableStateOf(0) }
    // Track the task category.
    val selectedTaskCategory = remember { mutableStateOf(TaskCategory.TASK_LIST) }
    // Get tasks based on the selected day and category.
    val tasks = getTasksForDayAndCategory(selectedDayIndex.value, selectedTaskCategory.value)
    // State to control calendar popup visibility.
    val showCalendarDialog = remember { mutableStateOf(false) }
    // State for error dialog when an invalid (future) date is selected.
    val showErrorDialog = remember { mutableStateOf(false) }
    // Remember a DatePickerState. Initialize it with current system time.
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    // State to keep track of the selected task (for bottom sheet)
    val selectedTask = remember { mutableStateOf<Task?>(null) }

    // Main Scaffold with dark theme
    Scaffold(
        topBar = { DarkModeTopBar(navController, onCalendarClick = { showCalendarDialog.value = true }) },
        containerColor = DarkBackgrounds
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(DarkBackgrounds)
        ) {
            DarkModeDateHeader()
            DarkModeDaySelectionRow(selectedDayIndex.value) { newIndex ->
                selectedDayIndex.value = newIndex
            }
            DarkModeTaskCategoryTabs(selectedTaskCategory.value) { newCategory ->
                selectedTaskCategory.value = newCategory
            }
            DarkModeMotivationalBanner()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 70.dp)
            ) {
                items(tasks) { task ->
                    DarkModeTaskCard(task = task, onTaskClick = {
                        // When task card is clicked, update the selected task to show bottom sheet.
                        selectedTask.value = task
                    })
                }
            }
        }
    }

    // DatePickerDialog with dark theme
    if (showCalendarDialog.value) {
        DatePickerDialog(
            onDismissRequest = { showCalendarDialog.value = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        try {
                            val selectedDate = LocalDateTime.ofInstant(
                                java.time.Instant.ofEpochMilli(selectedMillis),
                                ZoneId.systemDefault()
                            ).toLocalDate()

                            // Fixed "today" for demonstration.
                            val today = LocalDate.of(2025, 3, 10)
                            if (selectedDate.isAfter(today)) {
                                showErrorDialog.value = true
                            } else {
                                showCalendarDialog.value = false
                                // (Optional) Update your UI based on selectedDate here.
                            }
                        } catch (e: Exception) {
                            showErrorDialog.value = true
                        }
                    }
                }) {
                    Text("OK", color = DarkPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCalendarDialog.value = false }) {
                    Text("Cancel", color = DarkOnSurface)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = DarkSurfaces,
                titleContentColor = DarkOnSurface,
                headlineContentColor = DarkOnSurface,
                weekdayContentColor = DarkOnSurface,
                subheadContentColor = DarkOnSurface,
                yearContentColor = DarkOnSurface,
                currentYearContentColor = DarkPrimary,
                selectedYearContentColor = DarkOnSurface,
                selectedYearContainerColor = DarkPrimary,
                dayContentColor = DarkOnSurface,
                selectedDayContentColor = DarkSurfaces,
                selectedDayContainerColor = DarkPrimary,
                todayContentColor = DarkPrimary,
                todayDateBorderColor = DarkPrimary
            )
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Error dialog with dark theme
    if (showErrorDialog.value) {
        AlertDialog(
            onDismissRequest = { showErrorDialog.value = false },
            title = { Text("Invalid Date", color = DarkOnSurface) },
            text = { Text("Please select a valid date (today or earlier).", color = DarkOnSurface) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog.value = false }) {
                    Text("OK", color = DarkPrimary)
                }
            },
            containerColor = DarkSurfaces
        )
    }

    // Modal Bottom Sheet with dark theme
    if (selectedTask.value != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedTask.value = null },
            containerColor = DarkSurfaces
        ) {
            DarkModeTaskDetailContent(task = selectedTask.value!!) {
                // Dismiss button inside bottom sheet.
                selectedTask.value = null
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkModeTopBar(navController: NavController, onCalendarClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                "My Schedule",
                style = MaterialTheme.typography.titleMedium,
                color = DarkOnSurface
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = DarkOnSurface
                )
            }
        },
        actions = {
            IconButton(onClick = { /* Notification action */ }) {
                Icon(
                    Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = DarkOnSurface
                )
            }
            IconButton(onClick = onCalendarClick) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Calendar",
                    tint = DarkOnSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkSurfaces
        )
    )
}

@Composable
fun DarkModeDateHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Friday, March 10, 2025",
            style = MaterialTheme.typography.bodyLarge.copy(color = DarkOnBackground)
        )

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(DarkPrimary.copy(alpha = 0.2f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Profile",
                tint = DarkPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun DarkModeDaySelectionRow(selectedIndex: Int, onDaySelected: (Int) -> Unit) {
    val days = remember { generatePastDays(14) }
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        days.forEachIndexed { index, day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (index == selectedIndex)
                            DarkPrimary
                        else DarkSurfaces
                    )
                    .clickable { onDaySelected(index) }
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = day.weekday,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (index == selectedIndex) Color.Black else DarkOnBackground
                    )
                )
                Text(
                    text = day.date,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (index == selectedIndex) Color.Black else DarkOnBackground
                    )
                )
            }
        }
    }
}

@Composable
fun DarkModeTaskCategoryTabs(selectedCategory: TaskCategory, onCategorySelected: (TaskCategory) -> Unit) {
    val tabTitles = listOf("Task List", "Completed", "In Progress")
    val selectedTabIndex = when (selectedCategory) {
        TaskCategory.TASK_LIST -> 0
        TaskCategory.COMPLETED -> 1
        TaskCategory.IN_PROGRESS -> 2
    }
    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = DarkBackgrounds,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                color = DarkPrimary,
                height = 3.dp
            )
        },
        divider = { Divider(color = DarkSurfaces) }
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = index == selectedTabIndex,
                onClick = {
                    onCategorySelected(
                        when (index) {
                            0 -> TaskCategory.TASK_LIST
                            1 -> TaskCategory.COMPLETED
                            else -> TaskCategory.IN_PROGRESS
                        }
                    )
                },
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if(index == selectedTabIndex) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selectedContentColor = DarkPrimary,
                unselectedContentColor = DarkOnBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun DarkModeMotivationalBanner() {
    val tips = listOf(
        "Break your tasks into small wins to boost your dopamine!",
        "Celebrate progress—every small step counts!",
        "Visualize success: completing tasks leads to greater rewards!",
        "Stay consistent: daily achievements build lasting habits!"
    )
    val tip = remember { tips.random() }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            DarkPrimary.copy(alpha = 0.3f),
                            DarkSecondary.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Tip",
                    tint = DarkPrimary,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(end = 12.dp)
                )
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = DarkOnSurface
                )
            }
        }
    }
}

@Composable
fun DarkModeTaskCard(task: Task, onTaskClick: () -> Unit) {
    // Map task background color to dark theme colors
    val darkBackgroundColor = when (task.backgroundColor) {
        Color(0xFFEBF9F0) -> DarkGreen
        Color(0xFFEAF5FF) -> DarkBlue
        Color(0xFFFFF1F3) -> DarkRed
        Color(0xFFFFE0B2) -> DarkOrange
        Color(0xFFD0F0C0) -> DarkGreen.copy(alpha = 0.7f)
        else -> DarkPurple
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = darkBackgroundColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onTaskClick() }
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = DarkOnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkOnSurface.copy(alpha = 0.7f)
                )
                // Row for difficulty and points
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkPrimary.copy(alpha = 0.3f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Difficulty: ${task.difficulty}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = DarkPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSecondary.copy(alpha = 0.3f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Points: ${task.points}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = DarkSecondary
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = task.time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkOnSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    task.participants.forEach { avatarRes ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .border(1.dp, DarkSurfaces, CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = avatarRes),
                                contentDescription = "Participant Avatar",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DarkModeTaskDetailContent(task: Task, onDismiss: () -> Unit) {
    // Map task background color to dark theme colors
    val darkBackgroundColor = when (task.backgroundColor) {
        Color(0xFFEBF9F0) -> DarkGreen
        Color(0xFFEAF5FF) -> DarkBlue
        Color(0xFFFFF1F3) -> DarkRed
        Color(0xFFFFE0B2) -> DarkOrange
        Color(0xFFD0F0C0) -> DarkGreen.copy(alpha = 0.7f)
        else -> DarkPurple
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(darkBackgroundColor)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = DarkOnSurface
                    )
                    Text(
                        text = task.time,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkOnSurface.copy(alpha = 0.7f)
                    )
                }

                Divider(
                    color = DarkOnSurface.copy(alpha = 0.1f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = DarkOnSurface.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkPrimary.copy(alpha = 0.3f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Difficulty: ${task.difficulty}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = DarkPrimary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSecondary.copy(alpha = 0.3f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Points: ${task.points}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = DarkSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Participants:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkOnSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    task.participants.forEach { avatarRes ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(1.dp, DarkSurfaces, CircleShape)
                                .padding(2.dp)
                        ) {
                            Image(
                                painter = painterResource(id = avatarRes),
                                contentDescription = "Participant Avatar",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(
                    "Close",
                    modifier = Modifier.padding(vertical = 4.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}

// Keep the data classes and utility functions the same
data class Day(
    val weekday: String,
    val date: String,
    val isSelected: Boolean = false
)

data class Task(
    val title: String,
    val description: String,
    val time: String,
    val participants: List<Int>,
    val backgroundColor: Color,
    val difficulty: String = "",
    val points: Int = 0
)

fun generatePastDays(numDays: Int): List<Day> {
    val today = LocalDate.of(2025, 3, 10)
    return (0 until numDays).map { i ->
        val date = today.minusDays(i.toLong())
        val weekday = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        val dayOfMonth = date.dayOfMonth.toString()
        Day(weekday, dayOfMonth)
    }
}

// Keep the task data but change the background colors to match dark theme
fun getTasksForDayAndCategory(dayIndex: Int, category: TaskCategory): List<Task> {
    return if (category == TaskCategory.TASK_LIST) {
        when (dayIndex) {
            0 -> listOf(
                Task(
                    title = "Fri Task 1",
                    description = "Task details for Friday",
                    time = "10:00 AM",
                    participants = listOf(R.drawable.biology, R.drawable.man),
                    backgroundColor = Color(0xFFEBF9F0),  // Will be mapped to dark theme colors
                    difficulty = "Hard",
                    points = 50
                ),
                Task(
                    title = "Fri Task 2",
                    description = "More tasks for Friday",
                    time = "3:00 PM",
                    participants = listOf(R.drawable.man),
                    backgroundColor = Color(0xFFEAF5FF),  // Will be mapped to dark theme colors
                    difficulty = "Medium",
                    points = 30
                )
            )
            1 -> listOf(
                Task(
                    title = "Thu Task 1",
                    description = "Task details for Thursday",
                    time = "12:00 PM",
                    participants = listOf(R.drawable.man),
                    backgroundColor = Color(0xFFFFF1F3),  // Will be mapped to dark theme colors
                    difficulty = "Easy",
                    points = 20
                )
            )
            2 -> listOf(
                Task(
                    title = "Wed Task 1",
                    description = "Task details for Wednesday",
                    time = "11:00 AM",
                    participants = listOf(R.drawable.biology, R.drawable.man),
                    backgroundColor = Color(0xFFEAF5FF),  // Will be mapped to dark theme colors
                    difficulty = "Medium",
                    points = 30
                )
            )
            3 -> listOf(
                Task(
                    title = "Tue Task 1",
                    description = "Task details for Tuesday",
                    time = "10:00 AM",
                    participants = listOf(R.drawable.man),
                    backgroundColor = Color(0xFFEBF9F0),  // Will be mapped to dark theme colors
                    difficulty = "Easy",
                    points = 20
                )
            )
            4 -> listOf(
                Task(
                    title = "Mon Task 1",
                    description = "Task details for Monday",
                    time = "9:00 AM",
                    participants = listOf(R.drawable.biology),
                    backgroundColor = Color(0xFFFFF1F3),  // Will be mapped to dark theme colors
                    difficulty = "Medium",
                    points = 30
                )
            )
            else -> emptyList()
        }
    } else if (category == TaskCategory.COMPLETED) {
        when (dayIndex) {
            0 -> listOf(
                Task(
                    title = "Fri Completed",
                    description = "Completed task details for Friday",
                    time = "8:00 AM",
                    participants = listOf(R.drawable.man),
                    backgroundColor = Color(0xFFD0F0C0),  // Will be mapped to dark theme colors
                    difficulty = "Hard",
                    points = 50
                )
            )
            1 -> listOf(
                Task(
                    title = "Thu Completed",
                    description = "Completed task details for Thursday",
                    time = "8:30 AM",
                    participants = listOf(R.drawable.man),
                    backgroundColor = Color(0xFFD0F0C0),  // Will be mapped to dark theme colors
                    difficulty = "Easy",
                    points = 20
                )
            )
            else -> emptyList()
        }
    } else {
        when (dayIndex) {
            0 -> listOf(
                Task(
                    title = "Fri In Progress",
                    description = "In-progress task details for Friday",
                    time = "9:30 AM",
                    participants = listOf(R.drawable.man),
                    backgroundColor = Color(0xFFFFE0B2),  // Will be mapped to dark theme colors
                    difficulty = "Medium",
                    points = 30
                )
            )
            1 -> listOf(
                Task(
                    title = "Thu In Progress",
                    description = "In-progress task details for Thursday",
                    time = "11:30 AM",
                    participants = listOf(R.drawable.man),
                    backgroundColor = Color(0xFFFFE0B2),  // Will be mapped to dark theme colors
                    difficulty = "Hard",
                    points = 50
                )
            )
            else -> emptyList()
        }
    }
}