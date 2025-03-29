package com.example.neuroed

import PlaygroundScreen
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.SubjectSyllabusGetRepository
import com.example.neuroed.repository.UservisitRepository
import com.example.neuroed.repository.TestNotificationModelPredicationRepository
import com.example.neuroed.viewmodel.TestNotificationViewModel
import com.example.neuroed.viewmodel.TestNotificationViewModelFactory
import com.example.neuroed.viewmodel.UservisitdataViewModelFactory
import com.example.neuroed.viewmodel.UservisitdataViewmodel
import com.example.neuroed.model.UserAppVisitData
import kotlinx.coroutines.delay
import java.util.Locale
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private val RECORD_AUDIO_REQUEST_CODE = 101
    private lateinit var tts: TextToSpeech
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported")
                } else {
                    customizeVoice()
                    speak(
                        "Hello, I'm your personal assistant. How can I help you today? " +
                                "I’m here to make your day a little easier by assisting you with a wide range of tasks, " +
                                "from managing your schedule and setting reminders to answering your questions and providing useful information. " +
                                "My design is focused on understanding your needs and preferences, which means I can learn from our interactions " +
                                "to offer more personalized support over time"
                    )
                }
            } else {
                Log.e("TTS", "Initialization failed")
            }
        }, "com.google.android.tts") // Specifying the Google TTS engine.

        // Create the notification channel (only required once).
        NotificationHelper.createLockScreenNotificationChannel(this)
        // Request notification permission on Android 13+.
        NotificationHelper.requestNotificationPermission(this)
        // Optionally, show a test notification.
        NotificationHelper.showLockScreenNotification(
            this,
            title = "Lock Screen Notification",
            message = "This notification will appear on the lock screen."
        )

        // Request RECORD_AUDIO permission if not already granted.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_REQUEST_CODE
            )
        }
        setContent {
            NeuroEdApp()
        }
    }

    // Customize the TTS voice: adjust pitch, speech rate, and select a preferred voice.
    private fun customizeVoice() {
        tts.setPitch(1.0f)
        tts.setSpeechRate(0.9f)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val voices: Set<Voice> = tts.voices
            for (voice in voices) {
                if (voice.locale == Locale.US && voice.name.contains("google", ignoreCase = true)) {
                    tts.voice = voice
                    break
                }
            }
        }
    }

    // Helper method to speak text using TTS.
    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}

@Composable
fun NeuroEdApp() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        val navController = rememberNavController()
        val context = LocalContext.current
        var hasNavigated by remember { mutableStateOf(false) }


        LaunchedEffect(Unit) {
            delay(1000)  // Wait for 1 second
            if (!hasNavigated) {
                hasNavigated = true  // Ensure this block runs only once
                val sharedPrefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                val isUserSignedUp = sharedPrefs.getBoolean("isUserSignedUp", false)
                if (isUserSignedUp) {
                    // Navigate to home screen and clear the back stack.
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    // Navigate to sign-up screen and clear the back stack.
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }


        NavHost(navController = navController, startDestination = "splash") {
            composable("splash") { SplashScreen(navController) }
            composable("home") { HomeScreen(navController) }
            composable("SignUpScreen") { SignUpScreen(navController) }
            composable("ChatScreen") { ChatScreen(navController) }
            composable("AutoOpenCameraScreen") {
                CameraScreen(navController, onEnableCameraClick = {})
            }
            composable("CreateSubjectScreen") { CreateSubjectScreen(navController) }
            composable("AICharacterListScreen") { AICharacterListScreen(navController) }
            composable("NotificationScreen") { NotificationScreen(navController, onHistoricalClick = {}) }
            composable("SubscriptionScreen") {
                SubscriptionScreen(navController, onContinueClick = {}, onRestorePurchaseClick = {})
            }
            composable("ReelsScreen") { ReelsScreen(navController) }
            composable("ProfileScreen") { ProfileScreen(navController) }
            composable(
                route = "SyllabusScreen/{id}/{subjectDescription}/{subject}",
                arguments = listOf(
                    navArgument("id") { type = NavType.IntType },
                    navArgument("subjectDescription") { type = NavType.StringType },
                    navArgument("subject") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                val subjectDescription = backStackEntry.arguments?.getString("subjectDescription") ?: ""
                val subject = backStackEntry.arguments?.getString("subject") ?: ""
                val repository = SubjectSyllabusGetRepository(RetrofitClient.apiService)
                SyllabusScreen(
                    navController = navController,
                    id = id,
                    subjectDescription = subjectDescription,
                    subject = subject,
                    repository = repository
                )
            }
            composable(
                route = "AddUnitScreen/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                AddUnitScreen(navController, id)
            }

            composable(
                route = "TestQuestionScreen/{questionType}/{id}/{topic}/{subtopic}/{questionCount}/{subject}/{difficulty}/{totalQuestion}",
                arguments = listOf(
                    navArgument("questionType") {
                        type = NavType.StringType
                        defaultValue = "MCQ"
                    },
                    navArgument("id") {
                        type = NavType.IntType
                        defaultValue = 1
                    },
                    navArgument("topic") {
                        type = NavType.StringType
                        defaultValue = "DefaultTopic"
                    },
                    navArgument("subtopic") {
                        type = NavType.StringType
                        defaultValue = "DefaultSubtopic"
                    },
                    navArgument("questionCount") {
                        type = NavType.IntType
                        defaultValue = 10
                    },
                    navArgument("subject") {
                        type = NavType.StringType
                        defaultValue = "DefaultSubject"
                    },
                    navArgument("difficulty") {
                        type = NavType.StringType
                        defaultValue = "Easy"
                    },
                    navArgument("totalQuestion") {
                        type = NavType.IntType
                        defaultValue = 10
                    }
                )
            ) { backStackEntry ->
                val questionTypeString = backStackEntry.arguments?.getString("questionType") ?: "MCQ"
                val questionType = try {
                    TestType.valueOf(questionTypeString)
                } catch (e: IllegalArgumentException) {
                    TestType.MCQ
                }
                val id = backStackEntry.arguments?.getInt("id") ?: 1
                val topic = backStackEntry.arguments?.getString("topic") ?: "DefaultTopic"
                val subtopic = backStackEntry.arguments?.getString("subtopic") ?: "DefaultSubtopic"
                val questionCount = backStackEntry.arguments?.getInt("questionCount") ?: 10
                val subject = backStackEntry.arguments?.getString("subject") ?: "DefaultSubject"
                val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "Easy"
                val totalQuestion = backStackEntry.arguments?.getInt("totalQuestion") ?: 10

                TestQuestionScreen(
                    navController = navController,
                    questionType = questionType,
                    id = id,
                    topic = topic,
                    subtopic = subtopic,
                    questionCount = questionCount,
                    subject = subject,
                    difficulty = difficulty,
                    totalQuestion = totalQuestion
                )
            }
//            composable("TestQuestionScreen") { TestQuestionScreen(navController) }
            composable("SyllabustopicScreen") { SyllabustopicScreen(navController) }
            composable("TestsScreen") { TestsScreen(navController) }
            composable("FillInTheBlankQuestionScreen") { FillInTheBlankQuestionScreen(navController) }
//            composable("TrueFalseQuestionScreen") { TrueFalseQuestionScreen(navController) }
            composable("ShortAnswerQuestionScreen") { ShortAnswerQuestionScreen(navController) }
            composable("ForgettingCurveScreen") { ForgettingCurveScreen(navController) }
            composable("MeditationScreen") { MeditationScreen(navController) }
            composable("TaskScreen") { TaskScreen(navController) }
            composable("SyllabusContentScreen") { SyllabusContentScreen(navController) }
            composable("ExamScreen") { ExamScreen(navController) }
            composable("RecallingScreen") { RecallingScreen(navController) }
            composable("StoreScreen") { StoreScreen(navController) }
            composable("SignInScreen"){SignInScreen(navController)}
            composable("MeditationGenerateScreen") {MeditationGenerateScreen(navController)  }
            composable("liveSessionScreen"){liveSessionScreen(navController)}
            composable("PlaygroundScreen"){PlaygroundScreen(navController)}

            composable("verification/{phoneNumber}") { backStackEntry ->
                val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
                VerificationCodeScreen(
                    navController = navController,
                    phoneNumber = phoneNumber
                ) {
                    // Navigate to your home screen upon successful verification.
                    navController.navigate("home")
                }
            }
        }
    }
}


@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current

    // Define a vertical gradient background.
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E1E1E), // Dark gray top.
            Color(0xFF343434)  // Slightly lighter gray bottom.
        )
    )

    // Scale and alpha animation states.
    val scaleState = remember { mutableStateOf(0.8f) }
    LaunchedEffect(Unit) { scaleState.value = 1f }
    val scaleAnim by animateFloatAsState(
        targetValue = scaleState.value,
        animationSpec = tween(durationMillis = 1500)
    )
    val alphaAnim by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1500)
    )

    // Initialize API service and repositories.
    val apiService = RetrofitClient.apiService
    val visitRepository = UservisitRepository(apiService)
    val userVisitViewModel: UservisitdataViewmodel = viewModel(
        factory = UservisitdataViewModelFactory(visitRepository)
    )
    val testNotificationViewModel: TestNotificationViewModel = viewModel(
        factory = TestNotificationViewModelFactory(
            TestNotificationModelPredicationRepository(apiService)
        )
    )

    // Session day logic using SharedPreferences.


        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val currentDate = LocalDate.now().toString()
        val lastVisitDate = sharedPreferences.getString("last_visit_date", "")

        if (currentDate != lastVisitDate) {
            sharedPreferences.edit()
                .putInt("visit_count", 0)
                .putString("last_visit_date", currentDate)
                .apply()
        }
        val visitCount = sharedPreferences.getInt("visit_count", 0)

        // If it's the first visit of the day, call the API.
        if (visitCount == 0) {
            val userId = 1
            val dayOfWeek = LocalDate.now().dayOfWeek.toString()
            val visitTime = (System.currentTimeMillis() / 1000).toInt()

            Log.d("VisitCount", "First visit detected. Updating visit count to 1.")
            sharedPreferences.edit().putInt("visit_count", 1).apply()
            val updatedVisitCount = sharedPreferences.getInt("visit_count", 0)
            Log.d("VisitCount", "Updated visit count: $updatedVisitCount")

            // Create the UserAppVisitData object.
            val userVisitData = UserAppVisitData(
                userid = userId,
                day = dayOfWeek,
                visitCount = updatedVisitCount,
                visitTime = visitTime,
                date = currentDate
            )
            // Call the user visit API via the view model.
            userVisitViewModel.Uservisitdata(userVisitData)

            // Trigger the test notification API call.
            testNotificationViewModel.fetchTestNotification(userId)
        } else {
            // Subsequent visits: increment the count.
            val newVisitCount = visitCount + 1
            sharedPreferences.edit().putInt("visit_count", newVisitCount).apply()
            Log.d("VisitCount", "Subsequent visit. Today's visit count: $newVisitCount")
            val userId = 1
            testNotificationViewModel.fetchTestNotification(userId)
        }


    // Splash screen UI.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "NeuroEd",
            modifier = Modifier
                .alpha(alphaAnim)
                .graphicsLayer {
                    scaleX = scaleAnim
                    scaleY = scaleAnim
                }
                .padding(horizontal = 24.dp),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                color = Color.White
            )
        )
    }
}
