package com.example.neuroed


import GetDateOfBirthScreen
import PlaygroundScreen
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.neuroed.model.UserAppVisitData
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.SubjectSyllabusGetRepository
import com.example.neuroed.repository.TestNotificationModelPredicationRepository
import com.example.neuroed.repository.UservisitRepository
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


// ViewModel to hold authentication state.
class AuthViewModel : ViewModel() {
    var isUserSignedUp by mutableStateOf(false)
        private set

    fun markUserSignedUp() {
        isUserSignedUp = true
    }
}

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
            .requestIdToken(getString(R.string.default_web_client_id)) // Ensure this string exists in res/values/strings.xml
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
        setContent {
            NeuroEdApp(onGoogleSignUpClick = {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            },
                authViewModel = authViewModel
            )
        }
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

                    // Inform the ViewModel (if used) that the user is now signed up.
                    authViewModel.markUserSignedUp()

                    // Retrieve and log details from the GoogleSignInAccount.
                    val displayName = account.displayName ?: "No Display Name"
                    val email = account.email ?: "No Email"
                    val photoUrl = account.photoUrl?.toString() ?: "No Photo URL"
                    Log.d("MainActivity", "Google Account Info - Name: $displayName, Email: $email, Photo URL: $photoUrl")

                    // Retrieve additional data from FirebaseUser.
                    val firebaseUser = firebaseAuth.currentUser
                    firebaseUser?.let { user ->
                        Log.d("MainActivity", "Firebase User UID: ${user.uid}")
                        Log.d("MainActivity", "User Providers: ${user.providerData.joinToString { it.providerId }}")
                        Log.d("MainActivity", "Account Created At: ${user.metadata?.creationTimestamp}")
                        Log.d("MainActivity", "Last Sign-In At: ${user.metadata?.lastSignInTimestamp}")

                        // Retrieve the phone number if available.
                        val phoneNumber = user.phoneNumber ?: "No Phone Number"
                        Log.d("MainActivity", "User Phone Number: $phoneNumber")
                    }

                    // You can now pass this user data to your UI or store it in your database.
                    // For example, you could navigate to the home screen and display user details.
                } else {
                    Log.e("MainActivity", "Firebase authentication failed", task.exception)
                }
            }
    }


}

@Composable
fun NeuroEdApp(onGoogleSignUpClick: () -> Unit,
               authViewModel: AuthViewModel = viewModel()) {


        MaterialTheme(colorScheme = darkColorScheme()) {
            val navController = rememberNavController()
            val context = LocalContext.current

            // Initial check on app start: navigate based on saved sign-up state.
            LaunchedEffect(Unit) {
                val sharedPrefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                val isUserSignedUp = sharedPrefs.getBoolean("isUserSignedUp", false)
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

            // Observe the authentication state from the ViewModel.
            LaunchedEffect(authViewModel.isUserSignedUp) {
                if (authViewModel.isUserSignedUp) {
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }


        NavHost(navController = navController, startDestination = "splash") {
            composable("splash") { SplashScreen(navController) }
            composable("home") { HomeScreen(navController) }
            composable("SignUpScreen") {
                // Pass the callback down to the sign-up screen.
                SignUpScreen(navController = navController, onGoogleSignUpClick = onGoogleSignUpClick)
            }
            composable("ChatScreen") { ChatScreen(navController) }
            composable("AgentCreateScreen"){CreateAgentScreen(navController)}
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
            composable("UserInfoScreen") { UserInfoScreen(navController) }
            composable("GetDateOfBirthScreen") {GetDateOfBirthScreen(navController)  }
            composable("SelectionScreen") { SelectionScreen(navController)  }

            composable(
                route = "agent_processing_screen"
            ) {
                // Call your AgentProcessingScreen composable here
                AgentProcessingScreen(
                    // Provide the required onStopClick lambda
                    onStopClick = {
                        navController.popBackStack()
                    }
                )
            }

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
