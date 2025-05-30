package com.example.neuroed

import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.neuroed.model.ExamItem
import com.example.neuroed.model.TestType
import com.example.neuroed.model.TestUpdateRequest
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private object ExamThemeStyles {
    val Purple       = Color(0xFF7F66D3)
    val BgLight      = Color(0xFFF8F9FB)
    val BgDark       = Color(0xFF121212)
    val SurfaceLight = Color.White
    val SurfaceDark  = Color(0xFF1E1E1E)
    val OutlineLight = Color(0xFFC9D2DA)
    val OutlineDark  = Color(0xFF2F3339)
    val TxtLight     = Color(0xFF1F2937)
    val TxtDark      = Color.White
    val TxtLightSub  = Color(0xFF535C64)
    val TxtDarkSub   = Color(0xFFB3B3B3)
}

private data class ExamQuestion(
    val qText: String,
    val type: TestType,
    val options: List<String> = emptyList()
)

// Enhanced Face Analyzer with detailed logging and accuracy improvements
private class FaceAnalyzer(
    private val onFacesDetected: (faces: List<Face>, eyesOpen: Boolean) -> Unit
) : ImageAnalysis.Analyzer {
    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE) // More accurate detection
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setMinFaceSize(0.15f) // Increase minimum face size for better detection
            .enableTracking()
            .build()
    )

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    var eyesOpen = false
                    if (faces.isNotEmpty()) {
                        val face = faces[0]

                        // Check if both eyes are open with improved thresholds
                        if (face.leftEyeOpenProbability != null && face.rightEyeOpenProbability != null) {
                            val leftEyeProbability = face.leftEyeOpenProbability ?: 0f
                            val rightEyeProbability = face.rightEyeOpenProbability ?: 0f

                            // Log eye probabilities for debugging
                            Log.d("FaceAnalyzer", "Left eye: $leftEyeProbability, Right eye: $rightEyeProbability")

                            // Both eyes must have a probability > 0.6 to be considered "open"
                            eyesOpen = leftEyeProbability > 0.6f && rightEyeProbability > 0.6f
                        }
                    }

                    onFacesDetected(faces, eyesOpen)
                }
                .addOnFailureListener { e ->
                    Log.e("FaceAnalyzer", "Face detection failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamStartScreen(navController: NavController) {
    val examItem = navController
        .previousBackStackEntry
        ?.savedStateHandle
        ?.get<ExamItem>("exam")
        ?: return

    // Initialize from stored progress with null safety
    var currentQuestion by remember { mutableStateOf<ExamQuestion?>(null) }
    var questionIndex by remember { mutableStateOf(examItem.SolveQuestion ?: 0) }
    var questionsAnswered by remember { mutableStateOf(examItem.SolveQuestion ?: 0) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var fillAnswer by remember { mutableStateOf(TextFieldValue("")) }
    var trueFalseAnswer by remember { mutableStateOf<Boolean?>(null) }
    var secsLeft by remember { mutableStateOf(300) }
    var isLoading by remember { mutableStateOf(false) }

    // State for camera and face detection
    var cameraPermissionGranted by remember { mutableStateOf(false) }
    var isFaceDetected by remember { mutableStateOf(false) }
    var areEyesOpen by remember { mutableStateOf(false) }
    var facesCount by remember { mutableStateOf(0) }
    var lastDetectionTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var cameraExecutor: ExecutorService? by remember { mutableStateOf(null) }

    // Timer for no face detection
    var noFaceDetectionDuration by remember { mutableStateOf(0) }
    var examCancelled by remember { mutableStateOf(false) }

    // Constants for timeout
    val faceDetectionTimeout = 60 // 60 seconds (1 minute) timeout for no face detection

    // State variables for exam completion
    var isExamCompleted by remember { mutableStateOf(false) }
    var showCompletionAnimation by remember { mutableStateOf(false) }

    // Camera permission
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        cameraPermissionGranted = isGranted
    }

    // Check for camera permission
    LaunchedEffect(Unit) {
        cameraExecutor = Executors.newSingleThreadExecutor()

        val permissionCheckResult = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        )

        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
            cameraPermissionGranted = true
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Clean up executor
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor?.shutdown()
        }
    }

    // Log the initial state for debugging
    LaunchedEffect(Unit) {
        Log.d("ExamStart", "Initial state: SolveQuestion=${examItem.SolveQuestion}, totalQuestion=${examItem.totalQuestion}")
    }

    val gson = remember { Gson() }
    val wsReference = remember { mutableStateOf<WebSocket?>(null) }
    val scrollState = rememberScrollState()

    // Function to request the next question
    fun requestNextQuestion() {
        val totalQuestions = examItem.totalQuestion ?: 10
        val nextIndex = questionIndex + 1

        // Don't request if we've reached the end
        if (nextIndex >= totalQuestions) {
            Log.d("ExamStart", "Not requesting next question as we've reached the end: $nextIndex/$totalQuestions")
            return
        }

        Log.d("ExamStart", "Explicitly requesting next question (index: $nextIndex)")

        // Create request for the next question
        val nextQuestionRequest = mutableMapOf<String, Any>().apply {
            put("id", examItem.id)
            put("user_id", examItem.user_id)
            put("currentQuestionIndex", nextIndex)
            put("SolveQuestion", questionsAnswered)
            put("totalQuestion", totalQuestions)
            // Add exam type if available
            if (examItem.exam_type != null) {
                put("testType", examItem.exam_type)
            }
        }

        // Send the request
        wsReference.value?.send(gson.toJson(nextQuestionRequest))
    }

    // Function to update exam status
    fun updateExamStatus(solveQuestion: Int, completed: Boolean) {
        // Log what we're trying to update
        Log.d("ExamUpdate", "Updating exam ${examItem.id} with solveQuestion=$solveQuestion, completed=$completed")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get ApiService
                val apiService = RetrofitClient.apiService

                // Create request with snake_case field names for Django
                val request = TestUpdateRequest(
                    testId = examItem.id,
                    userId = examItem.user_id,
                    solveQuestion = solveQuestion,
                    completed = completed
                )

                // Log the request details before sending
                Log.d("ExamUpdate", "Request: exam_id=${request.testId}, user_id=${request.userId}, " +
                        "solve_question=${request.solveQuestion}, completed=${request.completed}")

                // Make API call
                val response = ApiHelper.executeWithToken { token ->
                    apiService.updateTestStatus(examItem.id, request, token)
                }

                // Log the response
                if (response.success) {
                    Log.d("ExamUpdate", "Success: ${response.message}")

                    // Update examItem with the value from server if available
                    if (response.solve_question != null) {
                        Log.d("ExamUpdate", "Server returned solve_question=${response.solve_question}")

                        withContext(Dispatchers.Main) {
                            // Update the ExamItem object
                            examItem.SolveQuestion = response.solve_question

                            // Update state variables if needed
                            if (response.solve_question > questionsAnswered) {
                                questionsAnswered = response.solve_question
                            }
                        }
                    }
                } else {
                    Log.e("ExamUpdate", "Failed: ${response.message}")
                }
            } catch (e: Exception) {
                // Log the exception with detailed info
                Log.e("ExamUpdate", "Exception: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // Back handler to catch back button presses
    BackHandler {
        if (!isExamCompleted && questionsAnswered > 0 && !examCancelled) {
            updateExamStatus(questionsAnswered, false) // Not completed
        }
        navController.popBackStack()
    }

    // WebSocket setup
    DisposableEffect(Unit) {
        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()
        val request = Request.Builder()
            .url("ws://localhost:8000/api/ExamStart/Question/Create/")
            .build()
        val listener = object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: okhttp3.Response) {
                // Convert examItem to a map
                val examMap = gson.fromJson(gson.toJson(examItem), Map::class.java) as Map<String, Any>
                val payload = HashMap<String, Any>(examMap)

                // Add resumeFromQuestion parameter with null safety
                payload["resumeFromQuestion"] = examItem.SolveQuestion ?: 0

                Log.d("ExamStart", "Opening WebSocket with resumeFromQuestion=${examItem.SolveQuestion ?: 0}")

                // Send the modified payload
                ws.send(gson.toJson(payload))
            }

            override fun onMessage(ws: WebSocket, text: String) {
                val obj = JsonParser.parseString(text).asJsonObject

                // Log the received message
                Log.d("ExamStart", "Received WebSocket message: ${text.take(100)}...")

                // Handle answer processed response - NEW CODE
                if (obj.has("answerProcessed") && obj.get("answerProcessed").asBoolean) {
                    Log.d("ExamStart", "Answer processed successfully")

                    // Get isCorrect status if available
                    val isCorrect = if (obj.has("isCorrect")) obj.get("isCorrect").asBoolean else false

                    // Update UI status
                    Handler(Looper.getMainLooper()).post {
                        // Set loading to false now that the answer is processed
                        isLoading = false

                        // If the answer is correct, increment questionsAnswered
                        if (isCorrect && !isExamCompleted) {
                            questionsAnswered += 1
                            Log.d("ExamStart", "Answer was correct, incremented questionsAnswered to $questionsAnswered")
                        }
                    }

                    // Check if this is the last question
                    val isLastQuestion = obj.has("isLastQuestion") && obj.get("isLastQuestion").asBoolean

                    if (isLastQuestion) {
                        Log.d("ExamStart", "This was the last question")

                        // Check if exam is complete
                        if (obj.has("examComplete") && obj.get("examComplete").asBoolean) {
                            Log.d("ExamStart", "Exam is now complete")

                            Handler(Looper.getMainLooper()).post {
                                isExamCompleted = true
                                showCompletionAnimation = true
                            }
                            return
                        }
                    } else {
                        // Request the next question with a slight delay
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (!isExamCompleted && !examCancelled) {
                                requestNextQuestion()
                            }
                        }, 500) // Short delay to give server time to process
                    }

                    return
                }

                // Check if the server indicates exam is already completed
                if (obj.has("completed") && obj.get("completed").asBoolean) {
                    Log.d("ExamStart", "Server indicates exam is completed")

                    val totalQuestions = examItem.totalQuestion ?: 0
                    val currentlySolved = questionIndex

                    Log.d("ExamStart", "Current status: solved $currentlySolved of $totalQuestions")

                    // Only mark as completed if we're on the last question
                    if (currentlySolved >= totalQuestions - 1) {
                        Log.d("ExamStart", "This is valid completion - marking exam as done")
                        Handler(Looper.getMainLooper()).post {
                            isExamCompleted = true
                            showCompletionAnimation = true
                        }
                        return
                    } else {
                        // Keep going if we haven't finished all questions
                        Log.d("ExamStart", "Server indicated completion but only on question $currentlySolved/$totalQuestions. Continuing exam.")
                    }
                }

                // Handle cancellation messages
                if (obj.has("cancelled") && obj.get("cancelled").asBoolean) {
                    Handler(Looper.getMainLooper()).post {
                        examCancelled = true
                    }
                    return
                }

                // Handle warning messages
                if (obj.has("warning")) {
                    val warningObj = obj.get("warning").asJsonObject
                    val message = warningObj.get("message").asString
                    val timeRemaining = warningObj.get("timeRemaining").asInt

                    Log.w("ExamStart", "Warning from server: $message (${timeRemaining}s remaining)")
                    // No UI changes needed as local warnings are already implemented
                    return
                }

                // Only process question data if we have the Question field
                // Only process question data if we have the Question field
                if (obj.has("Question")) {
                    val qText = obj["Question"].asString
                    val qType = when (obj["type"].asString) {
                        "MCQ" -> TestType.MCQ
                        "FILL_BLANK" -> TestType.FILL_BLANK
                        "SHORT_ANSWER" -> TestType.SHORT_ANSWER
                        "TRUE_FALSE" -> TestType.TRUE_FALSE
                        else -> TestType.MCQ // Default
                    }
                    val opts = if (obj.has("option")) obj["option"].asJsonArray.map { it.asString } else emptyList()

                    // Check for question number from server
                    val questionNumber = if (obj.has("questionNumber")) {
                        obj.get("questionNumber").asInt - 1  // 1-based to 0-based
                    } else {
                        questionIndex  // Keep current index if server doesn't provide it
                    }

                    Log.d("ExamStart", "Server sent questionNumber=$questionNumber (or using questionIndex+1)")

                    Handler(Looper.getMainLooper()).post {
                        // Use the question number from server or increment
                        questionIndex = questionNumber

                        Log.d("ExamStart", "Updated questionIndex=$questionIndex")

                        // Only increment questionsAnswered if this is a new question
                        if (questionNumber > questionsAnswered) {
                            questionsAnswered = questionNumber
                            Log.d("ExamStart", "Updated questionsAnswered=$questionsAnswered")
                        }

                        // IMPORTANT: Set loading to false ONLY when we actually receive a new question
                        isLoading = false

                        // Update current question
                        currentQuestion = ExamQuestion(qText, qType, opts)
                        selected = null
                        fillAnswer = TextFieldValue("")
                        trueFalseAnswer = null
                        secsLeft = 300

                        // Check if this is the last question - but only mark completed
                        // after the user actually submits their answer
                        val totalQuestions = examItem.totalQuestion
                        if (questionIndex >= totalQuestions) {
                            // We'll mark it as completed after the user submits this last answer
                            // Don't set isExamCompleted = true here
                            Log.d("ExamStart", "Last question reached, will complete after submission")
                        }
                    }
                }
            }
        }

        wsReference.value = client.newWebSocket(request, listener)

        // Return onDispose lambda
        onDispose {
            wsReference.value?.close(1000, "dispose")

            // Update exam status when screen is closed
            if (!isExamCompleted && questionsAnswered > 0 && !examCancelled) {
                updateExamStatus(questionsAnswered, false) // Not completed
            }
        }
    }

    // Check for continuous face absence to implement the timeout
    LaunchedEffect(Unit) {
        while (!isExamCompleted && !examCancelled) {
            // Calculate time since last face detection
            if (!isFaceDetected) {
                val nowTime = System.currentTimeMillis()
                val timeSinceLastDetection = (nowTime - lastDetectionTime) / 1000
                noFaceDetectionDuration = timeSinceLastDetection.toInt()

                // Check if we need to cancel the exam
                if (noFaceDetectionDuration >= faceDetectionTimeout) {
                    Log.d("ExamStart", "Face not detected for $noFaceDetectionDuration seconds. Cancelling exam.")
                    examCancelled = true
                }
            } else {
                // Reset if face is detected
                noFaceDetectionDuration = 0
            }

            // Send face detection status to server every 5 seconds
            val faceStatusPayload = mutableMapOf<String, Any>().apply {
                put("id", examItem.id)
                put("user_id", examItem.user_id)
                put("faceDetectionStatus", mapOf(
                    "faceDetected" to isFaceDetected,
                    "eyesOpen" to areEyesOpen,
                    "durationWithoutFace" to noFaceDetectionDuration
                ))
            }
            wsReference.value?.send(gson.toJson(faceStatusPayload))

            // Wait before sending the next update
            delay(5000)
        }
    }

    // Function to send an answer - MODIFIED FOR QUESTION TYPE
    // Function to send an answer - MODIFIED FOR QUESTION TYPE
    fun sendAnswer() {
        // Set loading to true and KEEP it true until next question is received
        isLoading = true
        // Clear current question to show loading indicator
        currentQuestion = null  // <-- THIS IS THE KEY CHANGE to show loading

        val totalQuestions = examItem.totalQuestion ?: 0
        val isLastQuestion = questionIndex >= (totalQuestions - 1)

        Log.d("ExamStart", "Submitting answer for question $questionIndex of $totalQuestions (isLastQuestion=$isLastQuestion)")

        if (isLastQuestion) {
            Log.d("ExamStart", "This is the last question, will mark exam as completed")
            updateExamStatus(questionsAnswered, true)
            isExamCompleted = true
            showCompletionAnimation = true
        } else {
            updateExamStatus(questionsAnswered, false)
        }

        val rawAnswer: Map<String, Any?> = when (currentQuestion?.type) {
            TestType.MCQ -> mapOf("answerIndex" to selected)
            TestType.FILL_BLANK, TestType.SHORT_ANSWER -> mapOf("answerText" to fillAnswer.text)
            TestType.TRUE_FALSE -> mapOf("answerBool" to trueFalseAnswer)
            else -> emptyMap()
        }

        val answerPart = rawAnswer.filterValues { it != null }
        val submitPart = mapOf("submit" to true)

        val itemMap: Map<String, Any> = gson.fromJson(
            gson.toJson(examItem),
            Map::class.java
        ) as Map<String, Any>

        val payload = mutableMapOf<String, Any>().apply {
            putAll(itemMap)
            putAll(submitPart)
            putAll(answerPart as Map<String, Any>)

            // Add current question type explicitly - CRITICAL FIX
            put("type", currentQuestion?.type?.name ?: "FILL_BLANK")

            // Add question text if available
            currentQuestion?.qText?.let {
                put("Question", it)
            }

            // Add current progress
            put("currentQuestionIndex", questionIndex)
            put("SolveQuestion", questionsAnswered)

            // Only mark as completed when it's the last question
            if (isLastQuestion) {
                put("completed", true)
            }
        }

        // Log the payload for debugging
        Log.d("ExamStart", "Sending answer with type=${currentQuestion?.type?.name}")

        wsReference.value?.send(gson.toJson(payload))

        // Note: We don't reset isLoading here - it will be reset when we receive the next question
    }

    // Timer for question countdown
    LaunchedEffect(currentQuestion) {
        if (!isExamCompleted && !examCancelled) {
            secsLeft = 300
            while (secsLeft > 0 && !isExamCompleted && !examCancelled) {
                delay(1000L)
                secsLeft--
            }
        }
    }

    LaunchedEffect(secsLeft) {
        if (secsLeft == 0 && currentQuestion != null && !isLoading && !isExamCompleted && !examCancelled) {
            sendAnswer()
        }
    }

    // Show completion animation after exam is completed
    LaunchedEffect(isExamCompleted) {
        if (isExamCompleted) {
            showCompletionAnimation = true
        }
    }

    // Colors based on theme
    val dark = isSystemInDarkTheme()
    val bg = if (dark) ExamThemeStyles.BgDark else ExamThemeStyles.BgLight
    val surface = if (dark) ExamThemeStyles.SurfaceDark else ExamThemeStyles.SurfaceLight
    val txtPrimary = if (dark) ExamThemeStyles.TxtDark else ExamThemeStyles.TxtLight
    val txtSecondary = if (dark) ExamThemeStyles.TxtDarkSub else ExamThemeStyles.TxtLightSub
    val outline = if (dark) ExamThemeStyles.OutlineDark else ExamThemeStyles.OutlineLight

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        // Update exam progress when navigating back
                        if (!isExamCompleted && questionsAnswered > 0 && !examCancelled) {
                            updateExamStatus(questionsAnswered, false)
                        }
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = txtPrimary
                        )
                    }
                },
                title = {
                    if (!isExamCompleted && !examCancelled) {
                        // Show the correct question number accounting for already answered questions
                        val totalQuestions = examItem.totalQuestion
                        Text(
                            if (currentQuestion != null)
                                "Question ${questionIndex + 1}/${totalQuestions}"
                            else
                                "Loading Question...",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = txtPrimary
                        )
                    } else if (examCancelled) {
                        Text(
                            "Exam Cancelled",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Red
                        )
                    } else {
                        Text(
                            "Exam Completed",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = txtPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = surface,
                    titleContentColor = txtPrimary,
                    navigationIconContentColor = txtPrimary
                )
            )
        }
    ) { pad ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            // Exam cancelled screen
            AnimatedVisibility(
                visible = examCancelled,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ExamCancelledScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // Exam questions screen
            AnimatedVisibility(
                visible = !showCompletionAnimation && !examCancelled,
                enter = fadeIn(),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Top row with countdown and camera preview
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Camera Preview UI with real camera integration
                        Box(
                            Modifier
                                .size(100.dp, 75.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 2.dp,
                                    color = when {
                                        isFaceDetected && areEyesOpen -> Color.Green
                                        isFaceDetected && !areEyesOpen -> Color.Yellow
                                        else -> Color.Red
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(Color.Black)
                        ) {
                            if (cameraPermissionGranted) {
                                AndroidView(
                                    factory = { ctx ->
                                        val previewView = PreviewView(ctx).apply {
                                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                        }

                                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                        cameraProviderFuture.addListener({
                                            val cameraProvider = cameraProviderFuture.get()

                                            // Setup the preview use case
                                            val preview = Preview.Builder().build().also {
                                                it.setSurfaceProvider(previewView.surfaceProvider)
                                            }

                                            // Setup image analysis use case
                                            val imageAnalysis = ImageAnalysis.Builder()
                                                .setTargetResolution(Size(640, 480))
                                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                                .build()
                                                .also {
                                                    it.setAnalyzer(
                                                        cameraExecutor ?: Executors.newSingleThreadExecutor(),
                                                        FaceAnalyzer { faces, eyesOpenStatus ->// Update face detection status
                                                            facesCount = faces.size
                                                            isFaceDetected = faces.isNotEmpty()
                                                            areEyesOpen = eyesOpenStatus

                                                            if (faces.isNotEmpty()) {
                                                                lastDetectionTime = System.currentTimeMillis()
                                                                Log.d("FaceDetector", "Face detected! Eyes open: $areEyesOpen")
                                                            } else {
                                                                Log.d("FaceDetector", "No face detected! Time since last detection: ${(System.currentTimeMillis() - lastDetectionTime)/1000}s")
                                                            }
                                                        }
                                                    )
                                                }

                                            try {
                                                // Unbind any bound use cases before rebinding
                                                cameraProvider.unbindAll()

                                                // Bind use cases to camera - use front camera
                                                cameraProvider.bindToLifecycle(
                                                    lifecycleOwner,
                                                    CameraSelector.DEFAULT_FRONT_CAMERA,
                                                    preview,
                                                    imageAnalysis
                                                )
                                            } catch (e: Exception) {
                                                Log.e("ExamStart", "Camera binding failed", e)
                                            }
                                        }, ContextCompat.getMainExecutor(ctx))

                                        previewView
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Face detection status indicator
                                Box(
                                    Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(4.dp)
                                        .size(10.dp)
                                        .background(
                                            when {
                                                isFaceDetected && areEyesOpen -> Color.Green
                                                isFaceDetected && !areEyesOpen -> Color.Yellow
                                                else -> Color.Red
                                            },
                                            shape = RoundedCornerShape(5.dp)
                                        )
                                )

                                // Status text
                                Text(
                                    text = when {
                                        !isFaceDetected -> "No Face"
                                        !areEyesOpen -> "Eyes Closed"
                                        else -> "Face OK"
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 4.dp),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                // Time warning
                                if (!isFaceDetected && noFaceDetectionDuration > 0) {
                                    Text(
                                        text = "${faceDetectionTimeout - noFaceDetectionDuration}s",
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp),
                                        color = Color.Red,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                // Show camera permission message
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Camera\nPermission\nRequired",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Countdown
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ExamThemeStyles.Purple.copy(alpha = .1f))
                                .padding(8.dp)
                        ) {
                            Text("%02d:%02d".format(secsLeft / 60, secsLeft % 60), color = ExamThemeStyles.Purple, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Face detection warning (if face not detected)
                    AnimatedVisibility(visible = !isFaceDetected || !areEyesOpen) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = Color.Red
                                )
                                Column {
                                    Text(
                                        if (!isFaceDetected)
                                            "Your face is not visible. Please stay in front of the camera."
                                        else
                                            "Please keep your eyes open during the exam.",
                                        color = Color.Red,
                                        fontSize = 14.sp
                                    )

                                    if (!isFaceDetected && noFaceDetectionDuration > 0) {
                                        Text(
                                            "Exam will be cancelled in ${faceDetectionTimeout - noFaceDetectionDuration} seconds if face not detected",
                                            color = Color.Red,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Question card
                    currentQuestion?.let { q ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = surface),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .animateContentSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(q.qText, fontSize = 16.sp, color = txtPrimary)
                                when (q.type) {
                                    TestType.MCQ -> q.options.forEachIndexed { i, opt ->
                                        Row(
                                            Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.dp, if (selected == i) ExamThemeStyles.Purple else outline, RoundedCornerShape(12.dp))
                                                .clickable { selected = i }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = selected == i,
                                                onClick = { selected = i },
                                                colors = RadioButtonDefaults.colors(selectedColor = ExamThemeStyles.Purple, unselectedColor = outline)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(opt, fontSize = 14.sp, color = txtPrimary)
                                        }
                                    }
                                    TestType.FILL_BLANK -> OutlinedTextField(
                                        value = fillAnswer,
                                        onValueChange = { fillAnswer = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Type your answer", color = txtSecondary) }
                                    )
                                    TestType.SHORT_ANSWER -> OutlinedTextField(
                                        value = fillAnswer,
                                        onValueChange = { fillAnswer = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 100.dp),
                                        placeholder = { Text("Write a short answer...", color = txtSecondary) }
                                    )
                                    TestType.TRUE_FALSE -> Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Button(
                                            onClick = { trueFalseAnswer = true },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (trueFalseAnswer == true) ExamThemeStyles.Purple else ExamThemeStyles.Purple.copy(alpha = 0.6f)
                                            )
                                        ) {
                                            Text("True", fontSize = 14.sp)
                                        }
                                        Button(
                                            onClick = { trueFalseAnswer = false },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (trueFalseAnswer == false) ExamThemeStyles.Purple else ExamThemeStyles.Purple.copy(alpha = 0.6f)
                                            )
                                        ) {
                                            Text("False", fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    } ?: AnimatedVisibility(
                        visible = currentQuestion == null && !isExamCompleted && !examCancelled,
                        enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(600)
                        ),
                        exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
                            targetOffsetY = { it / 2 },
                            animationSpec = tween(300)
                        )
                    ) {
                        // Improved loading UI
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = ExamThemeStyles.Purple,
                                    strokeWidth = 4.dp
                                )

                                Spacer(Modifier.height(24.dp))

                                Text(
                                    text = if (isLoading) "Submitting answer..." else "Loading next question...",
                                    color = txtPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = "Please wait",
                                    color = txtSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Next/Submit button
                    // Next/Submit button
                    Button(
                        onClick = { sendAnswer() },
                        enabled = currentQuestion?.let { q ->
                            when (q.type) {
                                TestType.MCQ -> selected != null
                                TestType.FILL_BLANK -> fillAnswer.text.isNotBlank()
                                TestType.SHORT_ANSWER -> fillAnswer.text.isNotBlank()
                                TestType.TRUE_FALSE -> trueFalseAnswer != null
                            }
                        } == true && !isLoading && isFaceDetected && areEyesOpen, // Only enable if face is detected AND eyes are open
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ExamThemeStyles.Purple,
                            contentColor = Color.White,
                            disabledContainerColor = ExamThemeStyles.Purple.copy(alpha = .40f),
                            disabledContentColor = Color.White.copy(alpha = .60f)
                        )
                    ) {
                        if (isLoading) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Submitting...", fontSize = 16.sp)
                            }
                        } else {
                            val totalQuestions = examItem.totalQuestion
                            if (questionIndex == totalQuestions - 1) {
                                Text("Finish Exam", fontSize = 16.sp)
                            } else {
                                Text("Next", fontSize = 16.sp)
                            }
                        }
                    }

                    // Face detection timer warning
                    if (!isFaceDetected && noFaceDetectionDuration > faceDetectionTimeout / 2) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "⚠️ Warning: Face Detection Required",
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Your exam will be cancelled in ${faceDetectionTimeout - noFaceDetectionDuration} seconds if no face is detected.",
                                    color = Color.Red,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Exam completion screen with animation
            AnimatedVisibility(
                visible = showCompletionAnimation && !examCancelled,
                enter = fadeIn(animationSpec = tween(800)) + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(800)
                )
            ) {
                ExamCompletedScreen(
                    examItem = examItem,
                    questionsAnswered = questionsAnswered,
                    onFinish = {
                        // Make sure exam is marked as completed before leaving
                        if (!isExamCompleted) {
                            updateExamStatus(questionsAnswered, true)
                        }
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun ExamCompletedScreen(
    examItem: ExamItem,
    questionsAnswered: Int,
    onFinish: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val txtPrimary = if (dark) ExamThemeStyles.TxtDark else ExamThemeStyles.TxtLight
    val txtSecondary = if (dark) ExamThemeStyles.TxtDarkSub else ExamThemeStyles.TxtLightSub

    // Animation states
    var showCheckmark by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    // Trigger animations in sequence
    LaunchedEffect(Unit) {
        showCheckmark = true
        delay(1000)
        showText = true
        delay(800)
        showButton = true
    }

    // Scale animation for checkmark
    val checkmarkScale by animateFloatAsState(
        targetValue = if (showCheckmark) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Checkmark icon with animation
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(60.dp))
                .background(ExamThemeStyles.Purple.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = ExamThemeStyles.Purple,
                modifier = Modifier
                    .size(80.dp)
                    .scale(checkmarkScale)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Congratulations text with animation
        AnimatedVisibility(
            visible = showText,
            enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                initialOffsetY = { 40 },
                animationSpec = tween(500)
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Exam Completed!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = txtPrimary
                )

                Text(
                    "You've successfully completed the exam",
                    fontSize = 16.sp,
                    color = txtSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Show actual questions answered vs total
                val totalQuestions = examItem.totalQuestion
                Text(
                    "Questions answered: $questionsAnswered/${totalQuestions}",
                    fontSize = 16.sp,
                    color = txtPrimary
                )

                // Show completion status
                val totalQ = examItem.totalQuestion.toFloat()
                val completionPercentage = if (totalQ > 0) (questionsAnswered.toFloat() / totalQ) * 100 else 0f
                Text(
                    when {
                        completionPercentage >= 100 -> "Perfect! You completed the entire exam."
                        completionPercentage >= 75 -> "Great job! You've completed most of the exam."
                        completionPercentage >= 50 -> "Good progress! You've completed half of the exam."
                        else -> "You've started working on this exam."
                    },
                    fontSize = 14.sp,
                    color = txtSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Return to exams button with animation
        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                initialOffsetY = { 40 },
                animationSpec = tween(500)
            )
        ) {
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ExamThemeStyles.Purple,
                    contentColor = Color.White
                )
            ) {
                Text("Return to Exams", fontSize = 16.sp)
            }
        }
    }
}


@Composable
fun ExamCancelledScreen(onBackClick: () -> Unit) {
    val dark = isSystemInDarkTheme()
    val txtPrimary = if (dark) ExamThemeStyles.TxtDark else ExamThemeStyles.TxtLight
    val txtSecondary = if (dark) ExamThemeStyles.TxtDarkSub else ExamThemeStyles.TxtLightSub
    val surface = if (dark) ExamThemeStyles.SurfaceDark else ExamThemeStyles.SurfaceLight

    // Animation states
    var showWarningIcon by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    // Trigger animations in sequence
    LaunchedEffect(Unit) {
        showWarningIcon = true
        delay(800)
        showText = true
        delay(800)
        showButton = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Warning icon with animation
        AnimatedVisibility(
            visible = showWarningIcon,
            enter = fadeIn(animationSpec = tween(500))
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(60.dp))
                    .background(Color.Red.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Exam Cancelled",
                    tint = Color.Red,
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Cancelled text with animation
        AnimatedVisibility(
            visible = showText,
            enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                initialOffsetY = { 40 },
                animationSpec = tween(500)
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Exam Cancelled",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Your exam has been cancelled because your face was not detected for more than 1 minute.",
                    fontSize = 16.sp,
                    color = txtSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = surface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "To prevent this in the future:",
                            fontWeight = FontWeight.Bold,
                            color = txtPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("• Ensure good lighting", color = txtPrimary)
                            Text("• Position your camera correctly", color = txtPrimary)
                            Text("• Stay in front of the camera", color = txtPrimary)
                            Text("• Keep your face visible throughout the exam", color = txtPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Please try again next time",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = txtPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Return button with animation
        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                initialOffsetY = { 40 },
                animationSpec = tween(500)
            )
        ) {
            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text("Return to Exams", fontSize = 16.sp)
            }
        }
    }
}