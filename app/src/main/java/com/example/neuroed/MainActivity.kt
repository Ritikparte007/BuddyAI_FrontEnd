package com.example.neuroed

import GetDateOfBirthScreen
import LanguageLearnScreen
import PlaygroundScreen
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.neuroed.model.UserAppVisitData
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.SessionRepository
import com.example.neuroed.repository.SubjectSyllabusGetRepository
import com.example.neuroed.repository.TestNotificationModelPredicationRepository
import com.example.neuroed.repository.UservisitRepository
import com.example.neuroed.viewmodel.SessionViewModel
import com.example.neuroed.viewmodel.SessionViewModelFactory
import com.example.neuroed.viewmodel.TestNotificationViewModel
import com.example.neuroed.viewmodel.TestNotificationViewModelFactory
import com.example.neuroed.viewmodel.UservisitdataViewModelFactory
import com.example.neuroed.viewmodel.UservisitdataViewmodel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.TimeUnit

// --------------------------------------------------------------
// ViewModel to hold authentication state.
// --------------------------------------------------------------
class AuthViewModel : ViewModel() {
    var isUserSignedUp by mutableStateOf(false)
        private set

    fun markUserSignedUp() {
        isUserSignedUp = true
    }
}

// --------------------------------------------------------------
// Main Activity
// --------------------------------------------------------------
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val azureSubscriptionKey = "YOUR_AZURE_SUBSCRIPTION_KEY" // Replace with your actual key
    private val azureServiceRegion = "YOUR_AZURE_REGION"

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Configure Google Sign-In with your web client ID
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // from res/values/strings.xml
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Create an Activity Result Launcher for Google Sign-In
        val googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(Exception::class.java) as GoogleSignInAccount
                    firebaseAuthWithGoogle(account)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Google sign-in failed", e)
                }
            }
        }

        schedulePipelineWorker()

        // Set Compose content
        setContent {
            val apiService  = RetrofitClient.apiService
            val sessionRepo = SessionRepository(apiService)

            // Track session for user 1
            SessionTrackerHost(userId = 1, repo = sessionRepo)
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                NeuroEdApp(
                    onGoogleSignUpClick = {
                        val signInIntent = googleSignInClient.signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    },
                    authViewModel = authViewModel
                )
            }
        }

    }


    private fun schedulePipelineWorker() {
        // Grab your authenticated user’s ID, or fall back to 1
        val userId = firebaseAuth.currentUser?.uid?.hashCode() ?: 1

        // Build input data for the worker
        val inputData = workDataOf("user_id" to userId)

        // Create a 30‑minute repeating request
        val request = PeriodicWorkRequestBuilder<PipelineWorker>(
            30, TimeUnit.MINUTES
        )
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        // Enqueue as a unique work so it won’t be duplicated
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "pipeline_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )

        Log.d("MainActivity", "PipelineWorker scheduled every 30 minutes (userId=$userId)")
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Mark the user as signed up in SharedPreferences.
                    val sharedPrefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                    sharedPrefs.edit().putBoolean("isUserSignedUp", true).apply()
                    Log.d("MainActivity", "Firebase authentication successful")

                    // Inform the ViewModel that the user is now signed up.
                    authViewModel.markUserSignedUp()

                    // Show some info about the signed-in user
                    val displayName = account.displayName ?: "No Display Name"
                    val email = account.email ?: "No Email"
                    val photoUrl = account.photoUrl?.toString() ?: "No Photo URL"
                    Log.d("MainActivity", "Google Account Info - Name: $displayName, Email: $email, Photo URL: $photoUrl")

                    // Additional data from FirebaseUser
                    val firebaseUser = firebaseAuth.currentUser
                    firebaseUser?.let { user ->
                        Log.d("MainActivity", "Firebase User UID: ${user.uid}")
                        Log.d("MainActivity", "User Providers: ${user.providerData.joinToString { it.providerId }}")
                        Log.d("MainActivity", "Account Created At: ${user.metadata?.creationTimestamp}")
                        Log.d("MainActivity", "Last Sign-In At: ${user.metadata?.lastSignInTimestamp}")
                        val phoneNumber = user.phoneNumber ?: "No Phone Number"
                        Log.d("MainActivity", "User Phone Number: $phoneNumber")
                    }

                } else {
                    Log.e("MainActivity", "Firebase authentication failed", task.exception)
                }
            }
    }
}

// --------------------------------------------------------------
// The top-level Composable that hosts the NavGraph
// --------------------------------------------------------------
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NeuroEdApp(
    onGoogleSignUpClick: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    MaterialTheme(colorScheme = darkColorScheme()) {
        val navController = rememberNavController()
        val webSocketState = rememberWebSocketState()

        // Clean up WebSocket when the app exits
        DisposableEffect(Unit) {
            onDispose {
                webSocketState.disconnect()
            }
        }

        // The app starts in "my_splash" (renamed to avoid confusion)
        NavHost(navController = navController, startDestination = "my_splash") {

            // The renamed Composable route
            composable("my_splash") { MySplashScreen(navController) }

            composable("home") { HomeScreen(navController) }
            composable("GamesScreen") {GamesScreen(navController)  }
            composable("ChessGameScreen") {ChessGameScreen(navController)  }
            composable("LudoGameScreen"){LudoGameScreen(navController)}

            composable("SignUpScreen") {
                SignUpScreen(navController = navController, onGoogleSignUpClick = onGoogleSignUpClick)
            }
            composable(
                route = "ChatScreen/{characterId}",
                arguments = listOf(
                    navArgument("characterId") {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                // Extract it from the back stack
                val characterId = backStackEntry
                    .arguments
                    ?.getInt("characterId")
                    ?: throw IllegalStateException("characterId missing!")

                ChatScreen(
                    navController = navController,
                    characterId   = characterId
                )
            }

            composable("AutoOpenCameraScreen") {
                CameraScreen(navController, onEnableCameraClick = {})
            }
            composable("CreateSubjectScreen") { CreateSubjectScreen(navController) }
            composable("AICharacterListScreen") { AICharacterListScreen(navController) }
            composable("NotificationScreen") { NotificationScreen(navController, onHistoricalClick = {}) }
            composable("SubscriptionScreen") {
                SubscriptionScreen(
                    navController,
                    onContinueClick = {},
                    onRestorePurchaseClick = {}
                )
            }
//            composable("ReelsScreen") { ReelsScreen(navController) }
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
            composable("SyllabustopicScreen") { SyllabustopicScreen(navController) }
            composable("TestsScreen") { TestsScreen(navController) }
            composable("FillInTheBlankQuestionScreen") { FillInTheBlankQuestionScreen(navController) }
            composable("ShortAnswerQuestionScreen") { ShortAnswerQuestionScreen(navController) }
            composable("ForgettingCurveScreen") { ForgettingCurveScreen(navController) }
            composable("MeditationScreen") { MeditationScreen(navController) }
            composable("TaskScreen") { TaskScreen(navController) }
            composable("SyllabusContentScreen") { SyllabusContentScreen(navController) }
            composable("ExamScreen") { ExamScreen(navController) }
            composable("RecallingScreen") { RecallingScreen(navController) }
            composable("StoreScreen") { StoreScreen(navController) }
            composable("SignInScreen") { SignInScreen(navController) }
            composable("MeditationGenerateScreen") { MeditationGenerateScreen(navController) }
            composable("liveSessionScreen") { liveSessionScreen(navController) }
            composable("PlaygroundScreen") { PlaygroundScreen(navController) }
            composable("UserInfoScreen") { UserInfoScreen(navController) }
            composable("GetDateOfBirthScreen") { GetDateOfBirthScreen(navController) }
            composable("SelectionScreen") { SelectionScreen(navController) }
            composable("AssignmentScreen"){AssignmentScreen(navController)}
            composable("LanguagelearnScreen") {LanguageLearnScreen(navController)  }
            composable("AgentInfoScreen") { AgentInfoScreen(navController) }
            composable("CharacterCreateScreen"){CharacterCreateScreen(navController)}

            composable("verification/{phoneNumber}") { backStackEntry ->
                val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
                VerificationCodeScreen(
                    navController = navController,
                    phoneNumber = phoneNumber
                ) {
                    navController.navigate("home")
                }
            }

            composable("AgentCreateScreen") {
                CreateAgentScreen(navController, webSocketState)
            }
            composable(Screen.AgentProcessing.route) {
                AgentProcessingScreen(
                    navController = navController,
                    webSocketState = webSocketState,
                    onStopClick = { navController.popBackStack() }
                )
            }
        }
    }
}

// --------------------------------------------------------------
// Re-named Splash Composable to avoid confusion with
// androidx.core.splashscreen.SplashScreen
// --------------------------------------------------------------
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MySplashScreen(navController: NavController) {
    val context = LocalContext.current

    // --- Start of unchanged API and SharedPreferences logic ---
    val apiService = RetrofitClient.apiService
    val visitRepository = UservisitRepository(apiService)
    val userVisitViewModel: UservisitdataViewmodel = viewModel(
        factory = UservisitdataViewModelFactory(visitRepository)
    )
//    val testNotificationViewModel: TestNotificationViewModel = viewModel(
//        factory = TestNotificationViewModelFactory(
//            TestNotificationModelPredicationRepository(apiService)
//        )
//    )

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

    if (visitCount == 0) {
        val userId = 1
        val dayOfWeek = LocalDate.now().dayOfWeek.toString()
        val visitTime = (System.currentTimeMillis() / 1000).toInt()

        Log.d("VisitCount", "First visit detected. Updating visit count to 1.")
        sharedPreferences.edit().putInt("visit_count", 1).apply()
        val updatedVisitCount = sharedPreferences.getInt("visit_count", 0)
        Log.d("VisitCount", "Updated visit count: $updatedVisitCount")

        val userVisitData = UserAppVisitData(
            userid = userId,
            day = dayOfWeek,
            visitCount = updatedVisitCount,
            visitTime = visitTime,
            date = currentDate
        )
        userVisitViewModel.Uservisitdata(userVisitData)
//        testNotificationViewModel.fetchTestNotification(userId)
    } else {
        val newVisitCount = visitCount + 1
        sharedPreferences.edit().putInt("visit_count", newVisitCount).apply()
        Log.d("VisitCount", "Subsequent visit. Today's visit count: $newVisitCount")
        val userId = 1
//        testNotificationViewModel.fetchTestNotification(userId)
    }
    // --- End of unchanged session logic ---

    // Animations for scaling from 0.8f -> 1f, alpha 0 -> 1
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

    // Shimmer effect
    val shimmerTransition = rememberInfiniteTransition()
    val shimmerOffsetX by shimmerTransition.animateFloat(
        initialValue = 0f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val gradientColors = listOf(Color.Gray, Color.White, Color.Gray)
    val shimmerBrush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset(shimmerOffsetX, 0f),
        end = Offset(shimmerOffsetX + 200f, 0f)
    )

    // Time-based delay (e.g., 3 seconds), then navigate
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000) // 3-second delay
        val mainPrefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val isUserSignedUp = mainPrefs.getBoolean("isUserSignedUp", false)

        if (isUserSignedUp) {
            navController.navigate("home") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            navController.navigate("SignUpScreen") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // The UI layout for the splash screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Center "BuddyAI" text with scale & alpha + shimmer
        Text(
            text = "BuddyAI",
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(alphaAnim)
                .graphicsLayer {
                    scaleX = scaleAnim
                    scaleY = scaleAnim
                },
            style = TextStyle(
                brush = shimmerBrush,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )

        // "version 1.1" text at bottom with slide-in + fade-in
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(durationMillis = 1500, delayMillis = 500)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 1500, delayMillis = 500)
                )
            ) {
                Text(
                    text = "version 1.1",
                    modifier = Modifier
                        .padding(bottom = 16.dp),
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}




@Composable
fun SessionTrackerHost(
    userId: Int,
    repo: SessionRepository,
) {
    val vm: SessionViewModel = viewModel(
        factory = SessionViewModelFactory(repo)
    )
    val sessionKey = remember { UUID.randomUUID().toString() }

    // Prepare client‐side timestamps
    val nowIso = remember {
        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
    val today = remember {
        LocalDate.now().toString()
    }
    val tz = remember {
        ZoneId.systemDefault().id
    }

    LaunchedEffect(Unit) {
        vm.startSession(
            userId         = userId,
            sessionKey     = sessionKey,
            deviceInfo     = emptyMap(),
            clientTime     = nowIso,
            clientDate     = today,
            clientTimezone = tz
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            vm.endSession(
                userId     = userId,
                sessionKey = sessionKey,
                clientTime = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            )
        }
    }
}