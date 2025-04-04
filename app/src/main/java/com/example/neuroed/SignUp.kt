package com.example.neuroed

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
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
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale

/**
 * Helper function to validate if the input is a valid email or phone number.
 * For email, it uses a regex pattern match.
 * For phone, it checks if it contains only digits and has at least 10 characters.
 */
fun isInputValid(input: String): Boolean {
    return if (input.contains("@")) {
        Patterns.EMAIL_ADDRESS.matcher(input).matches()
    } else {
        input.length >= 10 && input.all { it.isDigit() }
    }
}

/**
 * Formats a phone number into E.164 format.
 * Uses a default region ("IN" for India) if none is provided.
 * If parsing or validation fails, it returns the original input.
 */
fun formatPhoneNumber(input: String, region: String = "IN"): String {
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


/**
 * SignUpScreen composable displays the sign-up interface.
 * It includes user input fields, device-related data collection,
 * animations, and actions to validate and process the sign-up.
 */
@Composable
fun SignUpScreen(navController: NavController?, onGoogleSignUpClick: () -> Unit) {
    // Retrieve the current context for accessing resources and system services.
    val context = LocalContext.current

    // Initialize Retrofit API service and repository for phone/email verification.
    val apiService = RetrofitClient.apiService
    val repository = PhoneNumberRepository(apiService)
    // Obtain the ViewModel using a custom factory.
    val viewModel: PhoneNumberEmailVerificationViewModel = viewModel(
        factory = PhoneNumberEmailVerificationViewModelFactory(repository)
    )

    // State for fade-in animation of the entire screen.
    val alphaAnim = remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        alphaAnim.value = 1f
    }

    // Scale animation for the logo image.
    val logoScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800)
    )

    // Define an animated vertical gradient as the background.
    val animatedBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF121212), Color(0xFF1E1E1E))
    )

    // States for user input and loading/submission status.
    var emailOrPhoneInput by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Collect device-related data.
    // Try to obtain the SIM country first; if not available, fall back to Locale.
    val defaultCountry = getSimCountry(context) ?: Locale.getDefault().country
    val deviceModel = Build.MODEL                          // e.g., "Pixel 6"
    val manufacturer = Build.MANUFACTURER                  // e.g., "Google"
    val language = Locale.getDefault().language             // e.g., "en"
    val metrics = context.resources.displayMetrics         // Display metrics for screen info
    val screenWidth = metrics.widthPixels                  // Screen width in pixels
    val screenHeight = metrics.heightPixels                // Screen height in pixels
    val osVersion = Build.VERSION.RELEASE                  // OS version (e.g., "10")
    val androidId = getAndroidId(context)                  // Unique device identifier





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
            // Display the logo with scaling animation.
            LogoImage(logoScale = logoScale)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Create Your Account",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Join Buddy.Ai and explore a smarter future",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Card container for input fields.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2C2C2C).copy(alpha = 0.85f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                InputFields(
                    input = emailOrPhoneInput,
                    onInputChange = { emailOrPhoneInput = it },
                    showError = submitted
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            // Sign-up button with loading indicator.
            SignupButton(
                isLoading = isLoading,
                onClick = {
                    submitted = true
                    if (isInputValid(emailOrPhoneInput)) {
                        // If the input is a phone number, format it using the default country.
                        val formattedInput = if (!emailOrPhoneInput.contains("@") && !emailOrPhoneInput.startsWith("+")) {
                            formatPhoneNumber(emailOrPhoneInput, defaultCountry)
                        } else {
                            emailOrPhoneInput
                        }
                        println("Original input: $emailOrPhoneInput, Formatted input: $formattedInput")
                        val verificationData = PhoneNumberVerification(phoneNumberoremail = formattedInput)
                        isLoading = true
                        // Call the API to verify the phone number or email.
                        viewModel.fetchPhoneNumberVerification(
                            verificationData,
                            onSuccess = {
                                println("Backend response: successfully")
                                isLoading = false
                                // Navigate to verification screen on success.
                                navController?.navigate("verification/${formattedInput.replace("+", "")}")
                            },
                            onError = { error ->
                                println("Backend error: $error")
                                isLoading = false
                                // Optionally, display an error message to the user.
                            }
                        )
                    } else {
                        println("Invalid input: $emailOrPhoneInput")
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            SocialLoginDivider()
            Spacer(modifier = Modifier.height(16.dp))
            SignUpSocial(onGoogleSignUpClick = onGoogleSignUpClick)
            Spacer(modifier = Modifier.height(24.dp))
            LoginText { navController?.navigate("login") }
            Spacer(modifier = Modifier.height(16.dp))
            TermsAndConditionsText()
            Spacer(modifier = Modifier.height(8.dp))
            PrivacyPolicyText()
        }
    }
}

/**
 * Displays the logo text with a linear gradient and scaling effect.
 */
@Composable
fun LogoImage(logoScale: Float) {
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

/**
 * InputFields composable displays an outlined text field for user input.
 * It shows an appropriate leading icon (email or phone) and an error message if needed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputFields(input: String, onInputChange: (String) -> Unit, showError: Boolean) {
    val errorMessage = when {
        input.isEmpty() && showError -> "Please enter your email or phone"
        input.contains("@") && !Patterns.EMAIL_ADDRESS.matcher(input).matches() && showError -> "Please enter a valid email"
        !input.contains("@") && (input.length < 10 || !input.all { it.isDigit() }) && showError -> "Please enter a valid phone number"
        else -> ""
    }
    val leadingIcon = if (input.isNotEmpty() && input.all { it.isDigit() }) {
        Icons.Default.Phone
    } else {
        Icons.Default.Email
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
        // Animate visibility of the error message.
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
                    imageVector = Icons.Default.Warning,
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

/**
 * SignupButton composable displays a sign-up button with a scaling effect on press.
 * It shows a CircularProgressIndicator when the action is loading.
 */
@Composable
fun SignupButton(isLoading: Boolean, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150)
    )
    Button(
        onClick = {
            onClick()
            pressed = true
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White) // White background
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.Black, // Changed to black for contrast on white background
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = "Sign Up",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black // Black text for contrast
            )
        }
    }
}


/**
 * SocialLoginDivider displays a horizontal divider with a label,
 * used to separate alternative social login options.
 */
@Composable
fun SocialLoginDivider() {
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
            text = "  Or sign up with  ",
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

/**
 * SignUpSocial displays a button to sign up using a social account (e.g., Google).
 */
@Composable
fun SignUpSocial(onGoogleSignUpClick: () -> Unit) {
    Button(
        onClick = onGoogleSignUpClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White) // Changed to white
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.google), // Ensure this resource exists
                contentDescription = "Google Icon",
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sign Up with Google",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black  // Using black text for contrast
            )
        }
    }
}


/**
 * LoginText displays a clickable text that navigates to the login screen.
 */
@Composable
fun LoginText(onClick: () -> Unit) {
    var clicked by remember { mutableStateOf(false) }
    val textColor by animateColorAsState(
        targetValue = if (clicked) Color.White else Color.LightGray,
        animationSpec = tween(300)
    )
    Text(
        text = "Already have an account? Login",
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

/**
 * TermsAndConditionsText displays clickable text for Terms & Conditions.
 */
@Composable
fun TermsAndConditionsText() {
    var clicked by remember { mutableStateOf(false) }
    val textColor by animateColorAsState(
        targetValue = if (clicked) Color.White else Color.Gray,
        animationSpec = tween(300)
    )
    Text(
        text = "By signing up, you agree to our Terms & Conditions",
        style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline),
        color = textColor,
        modifier = Modifier
            .padding(8.dp)
            .clickable { clicked = !clicked }
    )
}

/**
 * PrivacyPolicyText displays clickable text for the Privacy Policy.
 */
@Composable
fun PrivacyPolicyText() {
    var clicked by remember { mutableStateOf(false) }
    val textColor by animateColorAsState(
        targetValue = if (clicked) Color.White else Color.Gray,
        animationSpec = tween(300)
    )
    Text(
        text = "Privacy Policy",
        style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline),
        color = textColor,
        modifier = Modifier
            .padding(8.dp)
            .clickable { clicked = !clicked }
    )
}
