package com.example.neuroed

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.neuroed.model.codeverification
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.codeVerificationRepository
import com.example.neuroed.viewmodel.CodeVerificationViewModel
import com.example.neuroed.viewmodel.CodeVerificationViewModelFactory
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Returns the SIM country code (in uppercase) if available.
 * If not available, returns null.
 */
fun getSimCountry(context: Context): String? {
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    return telephonyManager?.simCountryIso?.toUpperCase(Locale.ROOT)
}

/**
 * Returns the Android ID (a unique device identifier).
 */
fun getAndroidId(context: Context): String {
    return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
}

/**
 * Retrieves battery information as a Pair of battery percentage and charging status.
 */
fun getBatteryInfo(context: Context): Pair<Int, Boolean> {
    val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val batteryStatus = context.registerReceiver(null, ifilter)
    val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else -1
    val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
    return Pair(batteryPct, isCharging)
}

/**
 * Retrieves the network connection type (e.g., WiFi, Cellular).
 */
fun getNetworkType(context: Context): String {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
    val activeNetwork = connectivityManager?.activeNetwork ?: return "No Connection"
    val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
    return when {
        networkCapabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
        networkCapabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular"
        else -> "Other"
    }
}

/**
 * Retrieves memory information as a Pair of total memory and available memory in bytes.
 */
fun getMemoryInfo(context: Context): Pair<Long, Long> {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    val totalMem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) memoryInfo.totalMem else -1L
    return Pair(totalMem, memoryInfo.availMem)
}

/**
 * Retrieves a list of available sensor names on the device.
 */
fun getAvailableSensors(context: Context): List<String> {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? android.hardware.SensorManager
    return sensorManager?.getSensorList(android.hardware.Sensor.TYPE_ALL)?.map { it.name } ?: emptyList()
}

/**
 * Retrieves the app version from the package manager.
 */
fun getAppVersion(context: Context): String {
    return try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
    } catch (e: Exception) {
        "unknown"
    }
}

/**
 * VerificationCodeScreen displays the OTP verification UI, starts the SMS Retriever,
 * collects device-related information, and initiates the verification API call.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationCodeScreen(
    navController: NavController,
    phoneNumber: String,
    onVerificationSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    Log.d("PhoneNumber", phoneNumber)

    var codeInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Start SMS Retriever API to auto-read OTP.
    LaunchedEffect(Unit) {
        val client = SmsRetriever.getClient(context)
        client.startSmsRetriever()
            .addOnSuccessListener { Log.d("SMSRetriever", "Started successfully") }
            .addOnFailureListener { e -> Log.e("SMSRetriever", "Failed to start: ${e.toString()}") }
    }

    // Register BroadcastReceiver for OTP SMS.
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
                    val extras = intent.extras
                    val status = extras?.get(SmsRetriever.EXTRA_STATUS) as? Status
                    when (status?.statusCode) {
                        CommonStatusCodes.SUCCESS -> {
                            val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String
                            Log.d("SMSRetriever", "Message received: $message")
                            val otp = Regex("\\d{6}").find(message)?.value
                            if (otp != null) {
                                codeInput = otp
                            }
                        }
                        CommonStatusCodes.TIMEOUT -> {
                            Log.e("SMSRetriever", "Timeout waiting for SMS")
                        }
                    }
                }
            }
        }
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        context.registerReceiver(receiver, intentFilter)
        onDispose { context.unregisterReceiver(receiver) }
    }

    // Initialize API service, repository, and ViewModel.
    val apiService = RetrofitClient.apiService
    val repository = codeVerificationRepository(apiService)
    val viewModel: CodeVerificationViewModel = viewModel(
        factory = CodeVerificationViewModelFactory(repository)
    )

    // Collect device-related data.
    val defaultCountry = getSimCountry(context) ?: Locale.getDefault().country
    val deviceModel = Build.MODEL
    val manufacturer = Build.MANUFACTURER
    val language = Locale.getDefault().language
    val metrics = context.resources.displayMetrics
    val screenWidth = metrics.widthPixels
    val screenHeight = metrics.heightPixels
    val osVersion = Build.VERSION.RELEASE
    val androidId = getAndroidId(context)
    val (batteryLevel, isCharging) = getBatteryInfo(context)
    val networkType = getNetworkType(context)
    val (totalMem, availMem) = getMemoryInfo(context)
    val sensors = getAvailableSensors(context)
    val appVersion = getAppVersion(context)
    val timeZone = java.util.TimeZone.getDefault().id

    // Log all device information.
    Log.d("DeviceInfo", """
        Device Info: $manufacturer $deviceModel, OS: $osVersion, Country: $defaultCountry, Language: $language, Resolution: ${screenWidth}x$screenHeight
        Android ID: $androidId, Battery: $batteryLevel%, Charging: $isCharging
        Network: $networkType, Memory: Total=$totalMem, Available=$availMem
        Time Zone: $timeZone, App Version: $appVersion
        Sensors: ${sensors.joinToString(", ")}
    """.trimIndent())

    // Create background brush.
    val backgroundBrush = Brush.verticalGradient(colors = listOf(Color(0xFF121212), Color(0xFF1E1E1E)))

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
            // Header.
            Text(
                text = "Verification",
                style = MaterialTheme.typography.displaySmall.copy(
                    color = Color(0xFF03DAC5),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    shadow = Shadow(color = Color.Black, offset = Offset(2f, 2f), blurRadius = 4f)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter the code sent to your phone",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.Gray))
            Spacer(modifier = Modifier.height(32.dp))
            // OTP input field.
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
            // Verify button.
            Button(
                onClick = {
                    if (codeInput.isBlank()) {
                        errorMessage = "Please enter the verification code."
                    } else {
                        errorMessage = ""
                        isLoading = true
                        viewModel.fetchCodeVerification(
                            model = codeverification(phoneoremail = phoneNumber, code = codeInput),
                            onSuccess = {
                                isLoading = false
                                // Save sign-up state.
                                val sharedPrefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                                sharedPrefs.edit().putBoolean("isUserSignedUp", true).apply()
                                // Navigate to next screen.
                                navController.navigate("UserInfoScreen") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onError = { error ->
                                isLoading = false
                                errorMessage = error.toString()
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC5))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
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
