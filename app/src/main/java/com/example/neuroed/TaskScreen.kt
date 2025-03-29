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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    // Main Scaffold
    Scaffold(
        topBar = { ModernTopBar(navController, onCalendarClick = { showCalendarDialog.value = true }) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            ModernDateHeader()
            ModernDaySelectionRow(selectedDayIndex.value) { newIndex ->
                selectedDayIndex.value = newIndex
            }
            TaskCategoryTabs(selectedTaskCategory.value) { newCategory ->
                selectedTaskCategory.value = newCategory
            }
            MotivationalBanner()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 70.dp)
            ) {
                items(tasks) { task ->
                    ModernTaskCard(task = task, onTaskClick = {
                        // When task card is clicked, update the selected task to show bottom sheet.
                        selectedTask.value = task
                    })
                }
            }
        }
    }

    // DatePickerDialog for calendar selection.
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
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCalendarDialog.value = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Error dialog for invalid (future) date.
    if (showErrorDialog.value) {
        AlertDialog(
            onDismissRequest = { showErrorDialog.value = false },
            title = { Text("Invalid Date") },
            text = { Text("Please select a valid date (today or earlier).") },
            confirmButton = {
                TextButton(onClick = { showErrorDialog.value = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Modal Bottom Sheet to show task details.
    if (selectedTask.value != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedTask.value = null }
        ) {
            TaskDetailContent(task = selectedTask.value!!) {
                // Dismiss button inside bottom sheet.
                selectedTask.value = null
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopBar(navController: NavController, onCalendarClick: () -> Unit) {
    SmallTopAppBar(
        title = {
            Text(
                "My Schedule",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        actions = {
            IconButton(onClick = onCalendarClick) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Calendar",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@Composable
fun ModernDateHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Friday, March 10, 2025",
            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
        )
    }
}

@Composable
fun ModernDaySelectionRow(selectedIndex: Int, onDaySelected: (Int) -> Unit) {
    val days = remember { generatePastDays(14) }
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEachIndexed { index, day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (index == selectedIndex)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else Color.Transparent
                    )
                    .then(
                        if (index == selectedIndex)
                            Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(12.dp))
                        else Modifier
                    )
                    .clickable { onDaySelected(index) }
                    .padding(12.dp)
            ) {
                Text(
                    text = day.weekday,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground)
                )
                Text(
                    text = day.date,
                    style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onBackground)
                )
            }
        }
    }
}

@Composable
fun TaskCategoryTabs(selectedCategory: TaskCategory, onCategorySelected: (TaskCategory) -> Unit) {
    val tabTitles = listOf("Task List", "Completed", "In Progress")
    val selectedTabIndex = when (selectedCategory) {
        TaskCategory.TASK_LIST -> 0
        TaskCategory.COMPLETED -> 1
        TaskCategory.IN_PROGRESS -> 2
    }
    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.Transparent,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                color = MaterialTheme.colorScheme.primary
            )
        }
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
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MotivationalBanner() {
    val tips = listOf(
        "Break your tasks into small wins to boost your dopamine!",
        "Celebrate progress—every small step counts!",
        "Visualize success: completing tasks leads to greater rewards!",
        "Stay consistent: daily achievements build lasting habits!"
    )
    val tip = tips.random()
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = tip,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun ModernTaskCard(task: Task, onTaskClick: () -> Unit) {
    // The card changes visually on click (you can add ripple effects automatically).
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = task.backgroundColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onTaskClick() } // When tapped, call onTaskClick.
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Row for difficulty and points.
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "Difficulty: ${task.difficulty}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Points: ${task.points}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = task.time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    task.participants.forEach { avatarRes ->
                        Image(
                            painter = painterResource(id = avatarRes),
                            contentDescription = "Participant Avatar",
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskDetailContent(task: Task, onDismiss: () -> Unit) {
    // Content shown in the bottom sheet.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = task.title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = task.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Text(
                text = "Difficulty: ${task.difficulty}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Points Earned: ${task.points}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onDismiss) {
            Text("Close")
        }
    }
}

data class Day(
    val weekday: String,
    val date: String,
    val isSelected: Boolean = false
)

// Updated Task data class with difficulty and points.
data class Task(
    val title: String,
    val description: String,
    val time: String,
    val participants: List<Int>,
    val backgroundColor: Color,
    val difficulty: String = "",  // e.g., "Easy", "Medium", "Hard"
    val points: Int = 0           // points earned for completing the task
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

fun getTasksForDayAndCategory(dayIndex: Int, category: TaskCategory): List<Task> {
    return if (category == TaskCategory.TASK_LIST) {
        when (dayIndex) {
            0 -> listOf(
                Task(
                    title = "Fri Task 1",
                    description = "Task details for Friday",
                    time = "10:00 AM",
                    participants = listOf(R.drawable.biology, R.drawable.man),
                    backgroundColor = Color(0xFFEBF9F0),
                    difficulty = "Hard",
                    points = 50
                ),
                Task(
                    title = "Fri Task 2",
                    description = "More tasks for Friday",
                    time = "3:00 PM",
                    participants = listOf(R.drawable.man),
                    backgroundColor = Color(0xFFEAF5FF),
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
                    backgroundColor = Color(0xFFFFF1F3),
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
                    backgroundColor = Color(0xFFEAF5FF),
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
                    backgroundColor = Color(0xFFEBF9F0),
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
                    backgroundColor = Color(0xFFFFF1F3),
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
                    backgroundColor = Color(0xFFD0F0C0),
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
                    backgroundColor = Color(0xFFD0F0C0),
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
                    backgroundColor = Color(0xFFFFE0B2),
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
                    backgroundColor = Color(0xFFFFE0B2),
                    difficulty = "Hard",
                    points = 50
                )
            )
            else -> emptyList()
        }
    }
}
