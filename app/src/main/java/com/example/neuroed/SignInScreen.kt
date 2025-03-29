package com.example.neuroed

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.scale
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.neuroed.model.PhoneNumberVerification
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.PhoneNumberRepository
import com.example.neuroed.viewmodel.PhoneNumberEmailVerificationViewModel
import com.example.neuroed.viewmodel.PhoneNumberEmailVerificationViewModelFactory
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.NumberParseException
import kotlinx.coroutines.launch
// Import Material icons.
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning

// Renamed helper function to validate the input.
fun checkUserInputValidity(input: String): Boolean {
    return if (input.contains("@")) {
        Patterns.EMAIL_ADDRESS.matcher(input).matches()
    } else {
        input.length >= 10 && input.all { it.isDigit() }
    }
}

// Renamed helper function to format phone numbers into E.164 format (default region: IN).
fun formatToE164Number(input: String, region: String = "IN"): String {
    val phoneUtil = PhoneNumberUtil.getInstance()
    return try {
        val numberProto = phoneUtil.parse(input, region)
        if (phoneUtil.isValidNumber(numberProto)) {
            phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164)
        } else {
            input
        }
    } catch (e: NumberParseException) {
        e.printStackTrace()
        input
    }
}

@Composable
fun SignInScreen(navController: NavController?) {
    // Initialize API service, repository, and ViewModel.
    val apiService = RetrofitClient.apiService
    val repository = PhoneNumberRepository(apiService)
    val viewModel: PhoneNumberEmailVerificationViewModel = viewModel(
        factory = PhoneNumberEmailVerificationViewModelFactory(repository)
    )

    // Fade-in animation.
    val alphaAnim = remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        alphaAnim.value = 1f
    }

    // Scale animation for the logo.
    val logoScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800)
    )

    // Animated background gradient.
    val animatedBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF121212), Color(0xFF1E1E1E))
    )

    var emailOrPhoneInput by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    // Loading state to control button progress indicator.
    var isLoading by remember { mutableStateOf(false) }
    // Default country is "IN" (India).
    val defaultCountry = "IN"
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alphaAnim.value)
            .background(brush = animatedBrush),
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
            AppLogo(logoScale = logoScale)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sign in to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2C2C2C).copy(alpha = 0.85f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                CredentialInputFields(
                    input = emailOrPhoneInput,
                    onInputChange = { emailOrPhoneInput = it },
                    showError = submitted
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            LoginButton(
                isLoading = isLoading,
                buttonText = "Sign In",
                onClick = {
                    submitted = true
                    if (checkUserInputValidity(emailOrPhoneInput)) {
                        // Format phone number if needed.
                        val formattedInput = if (!emailOrPhoneInput.contains("@") && !emailOrPhoneInput.startsWith("+")) {
                            formatToE164Number(emailOrPhoneInput, defaultCountry)
                        } else {
                            emailOrPhoneInput
                        }
                        println("Original input: $emailOrPhoneInput, Formatted input: $formattedInput")
                        val verificationData = PhoneNumberVerification(phoneNumberoremail = formattedInput)
                        isLoading = true
                        viewModel.fetchPhoneNumberVerification(
                            verificationData,
                            onSuccess = {
                                println("Backend response: successfully")
                                isLoading = false
                                // Navigate on a successful response (update the route as needed).
                                navController?.navigate("home")
                            },
                            onError = { error ->
                                println("Backend error: $error")
                                isLoading = false
                                // Optionally show an error message.
                            }
                        )
                    } else {
                        println("Invalid input: $emailOrPhoneInput")
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            DividerForSocialLogins()
            Spacer(modifier = Modifier.height(16.dp))
            SocialLoginActionButton(
                buttonText = "Sign In with Google",
                onClick = { /* Handle Google sign in */ }
            )
            Spacer(modifier = Modifier.height(24.dp))
            NavigateToSignUpText { navController?.navigate("signup") }
        }
    }
}

@Composable
fun AppLogo(logoScale: Float) {
    val textGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFBB86FC), Color(0xFF03DAC5))
    )
    Text(
        text = "Buddy.Ai",
        modifier = Modifier.scale(logoScale),
        style = TextStyle(
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            brush = textGradient,
            shadow = Shadow(
                color = Color.Black,
                offset = Offset(2f, 2f),
                blurRadius = 4f
            )
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialInputFields(input: String, onInputChange: (String) -> Unit, showError: Boolean) {
    val errorMessage = when {
        input.isEmpty() && showError -> "Please enter your email or phone"
        input.contains("@") && !Patterns.EMAIL_ADDRESS.matcher(input).matches() && showError -> "Please enter a valid email"
        !input.contains("@") && (input.length < 10 || !input.all { it.isDigit() }) && showError -> "Please enter a valid phone number"
        else -> ""
    }
    val leadingIcon = if (input.isNotEmpty() && input.all { it.isDigit() }) {
        Icons.Filled.Phone
    } else {
        Icons.Filled.Email
    }
    val iconDescription = if (input.isNotEmpty() && input.all { it.isDigit() }) "Phone Icon" else "Email Icon"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            label = { Text("Email or Phone", color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage.isNotEmpty(),
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = iconDescription,
                    tint = Color.LightGray
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color(0xFF2C2C2C),
                cursorColor = Color.White,
                focusedBorderColor = Color(0xFFBB86FC),
                unfocusedBorderColor = Color.Gray
            )
        )
        AnimatedVisibility(
            visible = errorMessage.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Error Icon",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun LoginButton(isLoading: Boolean, buttonText: String, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150)
    )
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .clickable(
                onClick = onClick,
                onClickLabel = buttonText,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC))
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = buttonText,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }
}

@Composable
fun DividerForSocialLogins() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(
            modifier = Modifier.weight(1f),
            color = Color.Gray,
            thickness = 1.dp
        )
        Text(
            text = "  Or sign in with  ",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )
        Divider(
            modifier = Modifier.weight(1f),
            color = Color.Gray,
            thickness = 1.dp
        )
    }
}

@Composable
fun SocialLoginActionButton(buttonText: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC5))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.google), // Ensure this resource exists.
                contentDescription = "Google Icon",
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = buttonText,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun NavigateToSignUpText(onClick: () -> Unit) {
    var clicked by remember { mutableStateOf(false) }
    val textColor by animateColorAsState(
        targetValue = if (clicked) Color.White else Color.LightGray,
        animationSpec = tween(300)
    )
    Text(
        text = "Don't have an account? Sign Up",
        style = MaterialTheme.typography.bodyMedium.copy(
            textDecoration = TextDecoration.Underline
        ),
        color = textColor,
        modifier = Modifier
            .padding(16.dp)
            .clickable {
                clicked = true
                onClick()
            }
    )
}
