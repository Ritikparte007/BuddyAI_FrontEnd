package com.example.neuroed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.navigation.NavController

data class SyllabusItem(
    val topic: String,
    val questions: List<String>
)

fun sampleSyllabusData(): List<SyllabusItem> {
    return listOf(
        SyllabusItem("Topic 1", listOf("Question 1", "Question 2")),
        SyllabusItem("Topic 2", listOf("Question 3", "Question 4"))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabustopicScreen(
    navController: NavController
) {
    val syllabusData = remember { sampleSyllabusData() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chapter 1: Syllabus") },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                CustomFAB(
                    onClick = { /* TODO: Implement Add Topic Action */ },
                    icon = Icons.Default.Add,
                    description = "Add Topic"
                )
                CustomFAB(
                    onClick = { /* TODO: Implement Add Question Action */ },
                    icon = Icons.Default.Add,
                    description = "Add Question"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(syllabusData) { item ->
                TopicCard(syllabusItem = item)
            }
        }
    }
}

@Composable
fun CustomFAB(onClick: () -> Unit, icon: ImageVector, description: String) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.secondary
    ) {
        Icon(imageVector = icon, contentDescription = description)
    }
}

@Composable
fun TopicCard(syllabusItem: SyllabusItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = syllabusItem.topic,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            syllabusItem.questions.forEach { question ->
                Text(
                    text = "• $question",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

