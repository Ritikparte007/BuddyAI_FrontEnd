package com.example.neuroed

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.flowlayout.FlowRow

@Composable
fun GoalChip(
    goal: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    // Each chip maintains its own scale state.
    var chipScale by remember { mutableStateOf(1f) }
    val scaleAnim by animateFloatAsState(
        targetValue = chipScale,
        animationSpec = tween(durationMillis = 100) // Slightly faster scale animation
    )
    // Animate background and border colors.
    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.DarkGray,
        animationSpec = tween(durationMillis = 200) // Slightly faster color animation
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray,
        animationSpec = tween(durationMillis = 200) // Slightly faster color animation
    )

    FilterChip(
        selected = selected,
        onClick = {
            chipScale = 0.90f // Slightly smaller scale down
            onClick()
            chipScale = 1f
        },
        label = {
            Text(text = goal, color = Color.White, fontSize = 16.sp) // Slightly larger font for better readability
        },
        modifier = Modifier
            .scale(scaleAnim)
            .border(width = 1.5.dp, color = animatedBorderColor, shape = RoundedCornerShape(12.dp)) // Thicker border with rounded corners
            .clip(RoundedCornerShape(12.dp)), // Match clip with border
        colors = FilterChipDefaults.filterChipColors(
            containerColor = animatedBackgroundColor,
            labelColor = Color.White,
            selectedContainerColor = MaterialTheme.colorScheme.primary, // Ensure selected color is consistent
            selectedLabelColor = Color.White
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Unique list of goals to display.
    val goals = listOf(
        "Personal learning",
        "Personal friends",
        "Being intelligent",
        "Critical thinking",
        "Creative problem solving"
    )

    // Keep track of selected goals.
    val (selectedGoals, setSelectedGoals) = remember { mutableStateOf(setOf<String>()) }

    // Toggle selection.
    fun onGoalClicked(goal: String) {
        setSelectedGoals(
            if (goal in selectedGoals) selectedGoals - goal else selectedGoals + goal
        )
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Text(
                        "Select Goals",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold // Make title more prominent
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp) // Increased padding for better spacing
                .animateContentSize(animationSpec = tween(durationMillis = 200)) // Slightly faster content size animation
        ) {
            Text(
                "What are your learning goals?", // More descriptive heading
                color = Color.White.copy(alpha = 0.8f), // Slightly less opaque for visual hierarchy
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp) // Add some space below the heading
            )

            // Display chips using FlowRow.
            FlowRow(
                mainAxisSpacing = 12.dp, // Increased spacing between chips
                crossAxisSpacing = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                goals.forEach { goal ->
                    GoalChip(
                        goal = goal,
                        selected = goal in selectedGoals,
                        onClick = { onGoalClicked(goal) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Push buttons to the bottom

            Divider(color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp)) // More subtle divider

            // "Custom goals" button.
            Button(
                onClick = {
                    // Handle custom goals here.
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp), // Slightly taller button
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp), // More rounded corners
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp) // Slightly less elevation
            ) {
                Text(text = "Add Custom Goal", color = Color.Black, fontSize = 18.sp) // More explicit text
            }

            Spacer(modifier = Modifier.height(12.dp))

            // "Start" button.
            Button(
                onClick = {
                    // Handle next action here.
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Use primary color for emphasis
                    contentColor = Color.White // White text on primary color
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(text = "Start Learning", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) // More engaging text with slightly bold font
            }
        }
    }
}