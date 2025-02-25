package com.example.neuroed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AICharacterListScreen(navController: NavController) {
    var filterOption by remember { mutableStateOf("All") }
    val characters = getAICharacters().let { list ->
        when (filterOption) {
            "High Rating" -> list.sortedByDescending { it.rating }
            "Most Used" -> list.sortedByDescending { it.chatCount }
            "Less Used" -> list.filter { it.chatCount < 1000 }.sortedBy { it.chatCount }
            else -> list
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1E1E1E), Color(0xFF3A3A3A))
                )
            )
            .padding(16.dp)
    ) {
        FilterSection(filterOption) { filterOption = it }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(characters.chunked(2)) { _, rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    for (character in rowItems) {
                        AICharacterCard(
                            character,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSection(selectedOption: String, onFilterSelected: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf("All", "High Rating", "Most Used").forEach { filter ->
            Button(
                onClick = { onFilterSelected(filter) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedOption == filter) Color(0xFF4CAF50) else Color(0xFF2A2A2A)
                )
            ) {
                Text(text = filter, color = Color.White)
            }
        }
    }
}

@Composable
fun AICharacterCard(character: AICharacter, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .height(200.dp)
            .width(180.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = character.imageRes),
                contentDescription = character.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = character.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE3E3E3)
            )
            Text(
                text = character.description,
                fontSize = 14.sp,
                color = Color(0xFFAAAAAA)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "⭐ ${character.rating}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )
                Text(
                    text = "💬 ${character.chatCount} chats",
                    fontSize = 14.sp,
                    color = Color(0xFFAAAAAA)
                )
            }
        }
    }
}

data class AICharacter(
    val name: String,
    val description: String,
    val imageRes: Int,
    val rating: Double,
    val chatCount: Int
)

fun getAICharacters(): List<AICharacter> {
    return listOf(
        AICharacter("DreamScapeAI", "Unleash deep-learning magic!", R.drawable.biology, 4.8, 1200),
        AICharacter("NeuroGenAI", "AI that understands the brain", R.drawable.biology, 4.7, 950),
        AICharacter("VisionXAI", "AI with superhuman vision", R.drawable.biology, 4.9, 1500),
        AICharacter("CyberMind", "AI for futuristic intelligence", R.drawable.biology, 4.6, 800),
        AICharacter("QuantumSynth", "AI running on quantum speed", R.drawable.biology, 4.9, 1350),
        AICharacter("MetaConscious", "AI that thinks like you", R.drawable.biology, 4.5, 720)
    )
}
