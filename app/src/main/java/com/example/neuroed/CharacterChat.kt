package com.example.neuroed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun CharacterChat(navController: NavController){
    CharacterTopbar()
    CharacterBottom()
}

@Composable
fun CharacterBottom() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter // Align content to the bottom center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Optional padding for better visuals
            horizontalArrangement = Arrangement.Center // Center horizontally within the Row
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = { /* Handle text input */ },
                modifier = Modifier
                    .weight(1f) // Take up available space
                    .padding(end = 8.dp),
                placeholder = { Text("Type a message...") }
            )

            // Button for sending message
            Button(
                onClick = { /* Handle send action */ },
                modifier = Modifier
                    .height(56.dp) // Match TextField height
            ) {
                Text("Send")
            }
        }
    }
}


@Composable
fun CharacterTopbar(){
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.Red)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        IconButton(onClick = { /* Handle call click */ }) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_small_left), // Replace with your call icon resource
                contentDescription = "Call User",
                tint = Color.Green
            )
        }
        Image(
            painter = painterResource(id = R.drawable.biology),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(48.dp)
                .padding(end = 8.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Text(
            text = "User Name", // Replace with dynamic username
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = { /* Handle call click */ }) {
            Icon(
                painter = painterResource(id = R.drawable.phone_call), // Replace with your call icon resource
                contentDescription = "Call User",
                tint = Color.Green
            )
        }
    }
}