package com.example.neuroed

import GetDateOfBirthScreen
import LanguageLearnScreen
import LoadingScreen
import com.example.neuroed.model.TestItem
import com.example.neuroed.model.TestType
import PipelineWorker
//import PlaygroundScreen
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
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.neuroed.model.TestList
import com.example.neuroed.model.UserAppVisitData
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.SessionRepository
import com.example.neuroed.repository.SubjectSyllabusGetRepository
import com.example.neuroed.repository.UservisitRepository
//import com.example.neuroed.ui.screens.StoreScreen
import com.example.neuroed.viewmodel.SessionViewModel
import com.example.neuroed.viewmodel.SessionViewModelFactory
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
import kotlinx.coroutines.delay
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.lifecycleScope
import com.example.neuroed.model.FirebaseAuthRequest
import com.example.neuroed.model.FirebaseAuthResponse
import retrofit2.Response
import retrofit2.Callback
import retrofit2.Call
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.lifecycle.LifecycleOwner
import com.example.neuroed.NeuroEdApp.Companion.INVALID_USER_ID
import com.example.neuroed.NeuroEdApp.Companion.setUserInfoId
import com.example.neuroed.SubscriptionManager
import com.example.neuroed.model.UserInfoViewModel
import com.example.neuroed.repository.SubjectlistRepository
import com.example.neuroed.viewmodel.SubjectlistViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.neuroed.voice.GlobalVoiceManager


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


class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var subscriptionManager: SubscriptionManager


    private lateinit var subjectListRepository: SubjectlistRepository
    private lateinit var subjectListViewModel: SubjectlistViewModel
    private val userInfoViewModel: UserInfoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
        subscriptionManager = SubscriptionManager(this, lifecycleScope)

        // Configure Google Sign-In with your web client ID
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // from res/values/strings.xml
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Create an Activity Result Launcher for Google Sign-In
        googleSignInLauncher = registerForActivityResult(
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
        initializeSubjectListPreloading()
        setupAppLifecycleObserver()

        try {
            Log.d("MainActivity", "🎤 Initializing GlobalVoiceManager in onCreate")
            GlobalVoiceManager.initialize(this)
            Log.d("MainActivity", "✅ GlobalVoiceManager initialized successfully in onCreate")
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ Error initializing GlobalVoiceManager: ${e.message}", e)
        }


        // Set Compose content
        setContent {
            val apiService = RetrofitClient.apiService
            val sessionRepo = SessionRepository(apiService)

            // Track session for user 1
            SessionTrackerHost(userId = 1, repo = sessionRepo)
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                NeuroEdAppUI(
                    onGoogleSignUpClick = {
                        val signInIntent = googleSignInClient.signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    },
                    authViewModel = authViewModel
                )
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.BLACK

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController?.isAppearanceLightStatusBars = false

        preloadUserData()
    }


    override fun onDestroy() {
        Log.d("MainActivity", "🛑 MainActivity onDestroy called")
        try {
            // Only cleanup on actual destroy, not on pause
            GlobalVoiceManager.cleanup()
            Log.d("MainActivity", "✅ GlobalVoiceManager cleaned up in onDestroy")
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ Error cleaning up: ${e.message}", e)
        }
        super.onDestroy()
    }







    private fun setupAppLifecycleObserver() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_STOP -> {
                        Log.d("MainActivity", "📱 App going to background - stopping voice if active")
                        try {
                            // Only stop listening, don't cleanup completely
                            if (GlobalVoiceManager.isListening.value) {
                                GlobalVoiceManager.stopListening()
                            }
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error stopping voice on background: ${e.message}")
                        }
                        startBackgroundWorker()
                    }
                    Lifecycle.Event.ON_START -> {
                        Log.d("MainActivity", "📱 App coming to foreground - voice manager should be ready")
                        // ✅ No need to reinitialize, just verify it's healthy
                        try {
                            if (!GlobalVoiceManager.isInitialized.value) {
                                Log.w("MainActivity", "Voice manager not initialized, reinitializing...")
                                GlobalVoiceManager.initialize(this@MainActivity)
                            }
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error checking voice on foreground: ${e.message}")
                        }
                        stopBackgroundWorker()
                    }
                    else -> {}
                }
            }
        })
    }

    private fun startBackgroundWorker() {
        val sharedPrefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getInt("userInfoId", -1)
        if (userId <= 0) return

        val inputData = workDataOf("user_id" to userId)
        val request = OneTimeWorkRequestBuilder<PipelineWorker>()
            .setInitialDelay(1, TimeUnit.SECONDS)
            .setInputData(inputData)
            .build()
        WorkManager.getInstance(this)
            .enqueueUniqueWork("background_worker", ExistingWorkPolicy.REPLACE, request)
    }

    private fun stopBackgroundWorker() {
        WorkManager.getInstance(this).cancelUniqueWork("background_worker")
    }

    private fun schedulePipelineWorker() {
        val userId = firebaseAuth.currentUser?.uid?.hashCode() ?: 1
        val inputData = workDataOf("user_id" to userId)

        val request = OneTimeWorkRequestBuilder<PipelineWorker>()
            .setInitialDelay(5, TimeUnit.MINUTES)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniqueWork(
                "pipeline_worker_test",
                ExistingWorkPolicy.REPLACE,
                request
            )

        Log.d("MainActivity", "PipelineWorker scheduled to run after 5 minutes (userId=$userId)")
    }


    private fun initializeSubjectListPreloading() {
        val apiService = RetrofitClient.apiService
        subjectListRepository = SubjectlistRepository(apiService)
        subjectListViewModel = SubjectlistViewModel(subjectListRepository)

        // Log initialization
        Log.d("MainActivity", "Initialized SubjectList repositories and ViewModels")
    }



    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
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

                        // Firebase से ID टोकन प्राप्त करें
                        user.getIdToken(true).addOnCompleteListener { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                val idToken = tokenTask.result?.token
                                // Django बैकएंड से ऑथेंटिकेट करें
                                authenticateWithDjango(idToken, displayName, email, photoUrl)
                            } else {
                                Log.e("MainActivity", "Failed to get Firebase token", tokenTask.exception)
                                // टोकन प्राप्त नहीं हुआ, डिफ़ॉल्ट ID से प्रोसीड करें
                                handleAuthFailure()
                            }
                        }
                    } ?: run {
                        // FirebaseUser नल है, डिफ़ॉल्ट ID के साथ प्रोसीड करें
                        handleAuthFailure()
                    }
                } else {
                    Log.e("MainActivity", "Firebase authentication failed", task.exception)
                    handleAuthFailure()
                }
            }
    }

    private fun authenticateWithDjango(idToken: String?, displayName: String, email: String, photoUrl: String) {
        if (idToken == null) {
            Log.e("MainActivity", "No Firebase token available")
            handleAuthFailure()
            return
        }

        // RetrofitClient से ApiService प्राप्त करें
        val apiService = RetrofitClient.apiService
        Log.d("MainActivity", "Making authentication request to Django backend")

        // Django API कॉल
        apiService.authenticateWithFirebase(FirebaseAuthRequest(idToken))
            .enqueue(object : Callback<FirebaseAuthResponse> {
                override fun onResponse(
                    call: Call<FirebaseAuthResponse>,
                    response: Response<FirebaseAuthResponse>
                ) {
                    if (response.isSuccessful) {
                        val userInfoId = response.body()?.userInfoId
                        if (userInfoId != null) {
                            // सफल प्रतिक्रिया - userInfoId स्टोर करें
                            Log.d("Userdata", "Received userInfoId from Django: $userInfoId")
                            saveUserInfoId(userInfoId)
                            navigateToHomeScreen(userInfoId)
                        } else {
                            Log.e("MainActivity", "Received null userInfoId")
                            handleAuthFailure()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("MainActivity", "Django auth failed: $errorBody")

                        // Check for integrity or duplicate error
                        if (errorBody.contains("Duplicate entry") ||
                            errorBody.contains("IntegrityError") ||
                            response.code() == 400) {
                            Log.d("MainActivity", "Detected duplicate user error, trying getUserByToken")
                            getUserByToken(idToken)
                        } else {
                            handleAuthFailure()
                        }
                    }
                }

                override fun onFailure(call: Call<FirebaseAuthResponse>, t: Throwable) {
                    Log.e("MainActivity", "Network error: ${t.message}")
                    handleAuthFailure()
                }
            })
    }

    private fun getUserByToken(idToken: String) {
        Log.d("MainActivity", "Attempting to get user by token")

        val apiService = RetrofitClient.apiService
        Log.d("MainActivity", "API URL base: ${RetrofitClient.BASE_URL}")

        apiService.getUserByToken(FirebaseAuthRequest(idToken))
            .enqueue(object : Callback<FirebaseAuthResponse> {
                override fun onResponse(
                    call: Call<FirebaseAuthResponse>,
                    response: Response<FirebaseAuthResponse>
                ) {
                    if (response.isSuccessful) {
                        val userInfoId = response.body()?.userInfoId
                        if (userInfoId != null) {
                            Log.d("MainActivity", "Successfully got existing user: $userInfoId")
                            saveUserInfoId(userInfoId)
                            navigateToHomeScreen(userInfoId)
                        } else {
                            Log.e("MainActivity", "Received null userInfoId from getUserByToken")
                            handleAuthFailure()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("MainActivity", "Get user by token failed: $errorBody")
                        handleAuthFailure()
                    }
                }

                override fun onFailure(call: Call<FirebaseAuthResponse>, t: Throwable) {
                    Log.e("MainActivity", "Network error getting user by token: ${t.message}")
                    handleAuthFailure()
                }
            })
    }

    // userInfoId स्टोर करें
    private fun saveUserInfoId(userInfoId: Int) {
        try {
            // SharedPreferences में स्टोर करें
            val sharedPrefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            val result = sharedPrefs.edit()
                .putInt("userInfoId", userInfoId)
                .putBoolean("isUserSignedUp", true)
                .commit()  // Use commit instead of apply

            // Log whether the save was successful
            if (result) {
                Log.d("MainActivity", "Successfully saved userInfoId: $userInfoId")
            } else {
                Log.e("MainActivity", "Failed to save userInfoId: $userInfoId")
            }

            // ऐप ऑब्जेक्ट में स्टोर करें
            NeuroEdApp.setUserInfoId(userInfoId)

            // ViewModel को अपडेट करें
            authViewModel.markUserSignedUp()

            // Verify that the data was stored
            val storedId = sharedPrefs.getInt("userInfoId", -1)
            Log.d("MainActivity", "Verification - userInfoId after save: $storedId")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error saving userInfoId: ${e.message}", e)
        }
    }

    // फेलियर हैंडल करें
    private fun handleAuthFailure() {
        try {
            // फिर भी यूज़र को लॉग्ड इन मानें
            val sharedPrefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            val result = sharedPrefs.edit()
                .putBoolean("isUserSignedUp", true)
                .commit()  // Use commit instead of apply for immediate result

            if (result) {
                Log.d("MainActivity", "Successfully marked user as signed up in handleAuthFailure")
            } else {
                Log.e("MainActivity", "Failed to mark user as signed up in handleAuthFailure")
            }

            authViewModel.markUserSignedUp()

            // होम स्क्रीन पर नेविगेट करें (डिफ़ॉल्ट ID के साथ)
            navigateToHomeScreen(1)  // डिफ़ॉल्ट ID 1 पास करें
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in handleAuthFailure: ${e.message}", e)
            // As a last resort, try to navigate to home screen anyway
            navigateToHomeScreen(1)
        }
    }

    private fun navigateToHomeScreen(userId: Int) {
        Log.d("MainActivity", "Navigating to home screen with userInfoId: $userId")

        // Preload subject list data before navigation
        preloadSubjectListData(userId)

        setContent {
            val apiService = RetrofitClient.apiService
            val sessionRepo = SessionRepository(apiService)

            // userId का उपयोग करें
            SessionTrackerHost(userId = userId, repo = sessionRepo)

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                NeuroEdAppUI(
                    onGoogleSignUpClick = {
                        val signInIntent = googleSignInClient.signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    },
                    authViewModel = authViewModel,
                    startOnHomeScreen = true
                )
            }
        }
    }

    private fun preloadSubjectListData(userId: Int) {
        // Use application scope for this operation
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d("MainActivity", "Started fetching subject list for user: $userId")

                // Fetch the data
                subjectListViewModel.fetchSubjectList(userId)

                // Log success
                Log.d("MainActivity", "Successfully preloaded subject list data")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error preloading subject list: ${e.message}", e)
            }
        }
    }

    private fun preloadUserData() {
        // Load user data first
        userInfoViewModel.loadUserId(this)

        // Observe userId changes to preload subject list data when available
        lifecycleScope.launch {
            userInfoViewModel.userId.collect { userId ->
                if (userId != INVALID_USER_ID) {
                    Log.d("MainActivity", "Preloading subject list data for user: $userId")
                    preloadSubjectListData(userId)
                }
            }
        }
    }

    fun openSubscriptionManagement() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/account/subscriptions")
                setPackage("com.android.vending") // Google Play Store package
            }
            startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions"))
            startActivity(webIntent)
        }
    }
}


// --------------------------------------------------------------
// The top-level Composable that hosts the NavGraph
// --------------------------------------------------------------
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NeuroEdAppUI(
    onGoogleSignUpClick: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    startOnHomeScreen: Boolean = false
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

        LaunchedEffect(Unit) {
            if (startOnHomeScreen) {
                navController.navigate("home") {
                    popUpTo("my_splash") { inclusive = true }
                }
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
            composable("FullSubjectListScreen") {
                FullSubjectListScreen(
                    navController = navController
                )
            }
            composable("FullCharacterListScreen/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 1
                FullCharacterListScreen(
                    navController = navController,
                    userId = userId
                )
            }
            composable("agent_action") {
                AgentAction(navController = navController)
            }

            composable(Screen.AgentProcessing.route) {
                AgentProcessingScreen(navController, webSocketState)
            }

            composable(Screen.AgentPlanning.route) {
                AgentPlanningScreen(navController, webSocketState)
            }

            composable(Screen.AgentExecution.route) {
                AgentExecutionScreen(navController, webSocketState)
            }

            composable("agenttest") {
                AgentTestScreen(navController, webSocketState)
            }

            composable(
                route = "createSubjectScreenEdit/{subjectId}",
                arguments = listOf(navArgument("subjectId") { type = NavType.IntType })
            ) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: -1
                CreateSubjectScreenEdit(
                    navController = navController,
                    subjectId = subjectId
                )
            }

            composable("SignUpScreen") {
                SignUpScreen(navController = navController, onGoogleSignUpClick = onGoogleSignUpClick)
            }
            composable(
                route = "ChatScreen/{characterId}/{userId}",
                arguments = listOf(
                    navArgument("characterId") { type = NavType.IntType },
                    navArgument("userId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val characterId = backStackEntry.arguments?.getInt("characterId") ?: 0
                val userId = backStackEntry.arguments?.getString("userId")

                ChatScreen(
                    navController = navController,
                    characterId = characterId,
                    userId = userId
                )
            }

            composable("AutoOpenCameraScreen") {
                CameraScreen(navController, onEnableCameraClick = {})
            }
            composable("CreateSubjectScreen") { CreateSubjectScreen(navController) }
            composable("MindmapScreen") {MindmapScreen(navController)  }
            composable("AICharacterListScreen") { AICharacterListScreen(navController) }
            composable("HelpSupportScreen") {HelpSupportScreen(navController)  }
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
            composable("MeditationsAgentScreen") { MeditationsAgentScreen(navController)  }
            composable("FeedScreen") {
                // Access these from your parent composable where NavHost is defined
                val scope = rememberCoroutineScope()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val  voiceMessageViewModel: VoiceMessageViewModel = androidx.lifecycle.viewmodel.compose.viewModel()// Or however you access your ViewModel

                FeedScreen(
                    navController = navController,
                    voiceMessageViewModel = voiceMessageViewModel,
                    scope = scope,
                    drawerState = drawerState
                )
            }

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
            // In your NavGraph.kt or where your routes are defined
            composable(
                route = "AddUnitScreen/{id}/{existingUnits}",
                arguments = listOf(
                    navArgument("id") { type = NavType.IntType },
                    navArgument("existingUnits") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                val existingUnitsString = backStackEntry.arguments?.getString("existingUnits") ?: ""
                val existingUnits = existingUnitsString.split(",")
                    .filter { it.isNotEmpty() }
                    .map { it.toInt() }
                    .toSet()

                AddUnitScreen(
                    navController = navController,
                    id = id,
                    existingUnits = existingUnits
                )
            }

            composable("TestStartScreen") {
                TestStartScreen(navController = navController)
            }
            composable("ExamStartScreen"){
                ExamStartScreen(navController = navController)
            }
            composable("SyllabustopicScreen") { SyllabustopicScreen(navController) }
            composable("TestsScreen") { TestsScreen(navController) }
//            composable("FillInTheBlankQuestionScreen") { FillInTheBlankQuestionScreen(navController) }
//            composable("ShortAnswerQuestionScreen") { ShortAnswerQuestionScreen(navController) }
            composable("ForgettingCurveScreen") { ForgettingCurveScreen(navController) }
            composable("MeditationScreen") { MeditationScreen(navController) }
            composable("TaskScreen") { TaskScreen(navController) }
            // In your NavHost setup
            // In your NavHost setup
            composable(
                route = "SyllabusContentScreen/{unitId}/{unitTitle}/{unitContent}",
                arguments = listOf(
                    navArgument("unitId") { type = NavType.IntType },
                    navArgument("unitTitle") {
                        type = NavType.StringType
                        nullable = true
                    },
                    navArgument("unitContent") {
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val unitId = backStackEntry.arguments?.getInt("unitId") ?: 0

                // Use Uri.decode instead of URLDecoder
                val unitTitle = backStackEntry.arguments?.getString("unitTitle")?.let {
                    android.net.Uri.decode(it)
                } ?: ""

                val unitContent = backStackEntry.arguments?.getString("unitContent")?.let {
                    android.net.Uri.decode(it)
                } ?: ""

                // Now call SyllabusContentScreen with properly decoded parameters
                SyllabusContentScreen(
                    navController = navController,
                    unitId = unitId,
                    unitTitle = unitTitle,
                    unitContent = unitContent
                )
            }
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
            composable("MatricScreen") {MatricScreen(navController)  }
            composable(
                route = "AgentInfoScreen/{characterId}/{userId}",
                arguments = listOf(
                    navArgument("characterId") { type = NavType.IntType },
                    navArgument("userId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val characterId = backStackEntry.arguments?.getInt("characterId") ?: 0
                val userId = backStackEntry.arguments?.getString("userId")
                AgentInfoScreen(characterId, navController, userId)
            }
            composable("CharacterCreateScreen") {
                CharacterCreateScreen(navController = navController)
            }
            composable("EmojiFaceOffScreen"){EmojiGameNewUIScreen(navController)}
            composable("ChallengeScreen") { ChallengeScreen(navController) }
            composable("RequestScreen") { RequestScreen(navController)}
            composable("LoadingScreen") { LoadingScreen(navController) }
            composable("SubjectCardScreen"){SubjectCardScreen(navController)}
            composable("CollectionListScreen") {CollectionListScreen(navController)  }

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

    // API and SharedPreferences logic (unchanged)
    val apiService = RetrofitClient.apiService
    val visitRepository = UservisitRepository(apiService)
    val userVisitViewModel: UservisitdataViewmodel = viewModel(
        factory = UservisitdataViewModelFactory(visitRepository)
    )

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
    } else {
        val newVisitCount = visitCount + 1
        sharedPreferences.edit().putInt("visit_count", newVisitCount).apply()
        Log.d("VisitCount", "Subsequent visit. Today's visit count: $newVisitCount")
    }

    // Theme detection
    val isSystemInDarkTheme = isSystemInDarkTheme()

    // Theme colors
    val backgroundColor = if (isSystemInDarkTheme) {
        Color(0xFF121212)
    } else {
        Color(0xFFFAFAFA)
    }

    val primaryColor = if (isSystemInDarkTheme) {
        Color(0xFF4CAF50) // Green for dark theme
    } else {
        Color(0xFF2E7D32) // Darker green for light theme
    }

    val secondaryColor = if (isSystemInDarkTheme) {
        Color(0xFF81C784)
    } else {
        Color(0xFF66BB6A)
    }

    val textColor = if (isSystemInDarkTheme) {
        Color.White
    } else {
        Color(0xFF212121)
    }

    val subtitleColor = if (isSystemInDarkTheme) {
        Color(0xFFB0B0B0)
    } else {
        Color(0xFF757575)
    }

    // Animation states
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }

    // Start animations
    LaunchedEffect(Unit) {
        // Logo animation
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(800, easing = FastOutSlowInEasing)
            )
        }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        delay(400)
        // Text animations
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            )
        }
        delay(200)
        launch {
            subtitleAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            )
        }
    }

    // Navigation after delay
    LaunchedEffect(Unit) {
        delay(3000)
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

    // Splash Screen UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo/Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                        alpha = logoAlpha.value
                    }
                    .background(
                        color = primaryColor,
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Education icon or "B" letter
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "BuddyAi Logo",
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Name
            Text(
                text = "BuddyAi",
                style = TextStyle(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier
                    .graphicsLayer {
                        alpha = textAlpha.value
                    }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Smart Learning Companion",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = subtitleColor,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier
                    .graphicsLayer {
                        alpha = subtitleAlpha.value
                    }
            )
        }

        // Bottom elements
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Loading indicator
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        alpha = subtitleAlpha.value
                    },
                color = primaryColor,
                strokeWidth = 2.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Version
            Text(
                text = "Version 1.1",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = subtitleColor,
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier
                    .graphicsLayer {
                        alpha = subtitleAlpha.value
                    }
            )
        }
    }
}

// Custom easing function for smooth animations
val CustomEaseOut = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)



@Composable
fun SessionTrackerHost(
    userId: Int,
    repo: SessionRepository,
) {
    // 1) Get or create your ViewModel
    val vm: SessionViewModel = viewModel(
        factory = SessionViewModelFactory(repo)
    )

    // 2) Generate a sessionKey once per composition
    val sessionKey = rememberSaveable { UUID.randomUUID().toString() }

    // 3) Helpers to format timestamps
    val tz    = remember { ZoneId.systemDefault().id }
    val nowFn = { OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) }
    val todayFn = { LocalDate.now().toString() }

    // 4) Start the session when this composable first appears
    LaunchedEffect(Unit) {
        vm.startSession(
            userId         = userId,
            sessionKey     = sessionKey,
            deviceInfo     = emptyMap(),
            clientTime     = nowFn(),
            clientDate     = todayFn(),
            clientTimezone = tz
        )
    }

    // 5) Observe the Activity lifecycle and only fire on ON_STOP
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                vm.endSession(
                    userId     = userId,
                    sessionKey = sessionKey,
                    clientTime = nowFn()
                )
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
