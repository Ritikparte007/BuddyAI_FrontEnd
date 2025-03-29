package com.example.neuroed

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.neuroed.model.codeverification
import com.example.neuroed.model.Saveuserinfo
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.UserinfosaveRepository
import com.example.neuroed.repository.codeVerificationRepository
import com.example.neuroed.viewmodel.CodeVerificationViewModel
import com.example.neuroed.viewmodel.CodeVerificationViewModelFactory
import com.example.neuroed.viewmodel.UserInfoSaveViewModel
import com.example.neuroed.viewmodel.UserinfoSaveViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationCodeScreen(
    navController: NavController,
    phoneNumber: String,
    onVerificationSuccess: () -> Unit = {} // Callback on successful verification.
) {
    val context = LocalContext.current
    Log.d("PhoneNumber", phoneNumber)
    var codeInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Create a refined vertical gradient background.
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF121212), Color(0xFF1E1E1E))
    )

    // Initialize API service, repository, and viewModel for code verification.
    val apiService = RetrofitClient.apiService
    val repository = codeVerificationRepository(apiService)
    val viewModel: CodeVerificationViewModel = viewModel(
        factory = CodeVerificationViewModelFactory(repository)
    )

    // Initialize API service, repository, and viewModel for saving user info.
//    val apiServiceUserInfo = RetrofitClient.apiService
//    val repositoryUserInfo = UserinfosaveRepository(apiServiceUserInfo)
//    val userInfoSaveViewModel: UserInfoSaveViewModel = viewModel(
//        factory = UserinfoSaveViewModelFactory(repositoryUserInfo)
//    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header section with decorative text and divider.
            Text(
                text = "Verification",
                style = MaterialTheme.typography.displaySmall.copy(
                    color = Color(0xFF03DAC5),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter the code sent to your phone",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.height(32.dp))
            // Verification code input field.
            OutlinedTextField(
                value = codeInput,
                onValueChange = { codeInput = it },
                label = { Text("Verification Code", color = Color.LightGray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, shape = RoundedCornerShape(8.dp)),
                singleLine = true,
                isError = errorMessage.isNotEmpty(),
                colors = outlinedTextFieldColors(
                    containerColor = Color(0xFF2C2C2C),
                    cursorColor = Color.White,
                    focusedBorderColor = Color(0xFF03DAC5),
                    unfocusedBorderColor = Color.Gray
                )
            )
            AnimatedVisibility(
                visible = errorMessage.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(500)) + fadeIn(animationSpec = tween(500)),
                exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(500)) + fadeOut(animationSpec = tween(500))
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            // Verify button calls the API via the viewModel.
            Button(
                onClick = {
                    if (codeInput.isBlank()) {
                        errorMessage = "Please enter the verification code."
                    } else {
                        errorMessage = ""
                        isLoading = true
                        // Call the API using the ViewModel.
                        viewModel.fetchCodeVerification(
                            model = codeverification(phoneoremail = phoneNumber, code = codeInput),
                            onSuccess = {
                                isLoading = false
                                // Save that the user is signed up.
                                val sharedPrefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                                sharedPrefs.edit().putBoolean("isUserSignedUp", true).apply()

//                                val userInfo = Saveuserinfo(
//                                    username = "JohnDoe",
//                                    phone = "9876543210",
//                                    email = "johndoe@example.com"
//                                )
//                                userInfoSaveViewModel.saveUserInfo(userInfo)

                                // Navigate to home and clear the back stack.
                                navController.navigate("home") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            },
                            onError = { error ->
                                isLoading = false
                                errorMessage = error
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC5))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(text = "Verify", color = Color.White, fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            // Resend code link.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true, color = Color(0xFF03DAC5)),
                        onClick = { /* Implement resend logic here */ }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Didn't receive the code? Resend",
                    color = Color(0xFF03DAC5),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = TextDecoration.Underline,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}
