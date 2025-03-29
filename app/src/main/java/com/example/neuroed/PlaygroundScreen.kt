import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
//import androidx.compose.ui.graphics.Stroke
//import androidx.compose.ui.graphics.drawscope.drawPath
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.neuroed.ChatMessage
import com.example.neuroed.R
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.SubjectlistRepository
import com.example.neuroed.viewmodel.SubjectSyllabusViewModel
import com.example.neuroed.viewmodel.SubjectSyllabusViewModelFactory
import com.example.neuroed.viewmodel.SubjectlistViewModelFactory
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.neuroed.viewmodel.SubjectlistViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import com.example.neuroed.model.SubjectSyllabusGetResponse
import com.example.neuroed.model.SubjectSyllabusHeadingTopic
import com.example.neuroed.model.SubjectSyllabusHeadingTopicSubtopic
import com.example.neuroed.model.SubjectlistResponse
import com.example.neuroed.repository.SubjectSyllabusGetRepository
import com.example.neuroed.repository.SubjectSyllabusHeadingRepository
import com.example.neuroed.repository.SubjectSyllabusHeadingSubtopicRepository
import com.example.neuroed.repository.SubjectSyllabusHeadingTopicRepository
import com.example.neuroed.viewmodel.SubjectSyllabusHeadingTopicSubtopicViewModel
import com.example.neuroed.viewmodel.SubjectSyllabusHeadingTopicSubtopicViewModelFactory
import com.example.neuroed.viewmodel.SubjectSyllabusHeadingTopicViewModel
import com.example.neuroed.viewmodel.SubjectSyllabusHeadingTopicViewModelFactory
import com.example.neuroed.viewmodel.SubjectSyllabusHeadingViewModel
import com.example.neuroed.viewmodel.SubjectSyllabusHeadingViewModelFactory
import org.json.JSONObject


// --------------------
// Google Fonts Setup (unchanged)
// --------------------
private val certRes: Int = R.array.com_google_android_gms_fonts_certs

private val robotoFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = certRes
)

private val robotoFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Roboto"),
        fontProvider = robotoFontProvider,
        weight = FontWeight.Normal
    ),
    Font(
        googleFont = GoogleFont("Roboto"),
        fontProvider = robotoFontProvider,
        weight = FontWeight.Bold
    )
)

private val NeuroEdTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = robotoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = Color.Black
    )
)

@Composable
fun NeuroEdTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        typography = NeuroEdTypography,
        content = content
    )
}

// --------------------
// Compact Mode Switch (Radio Buttons)
// --------------------
@Composable
fun CompactModeSwitch(
    selectedMode: String,
    onModeSelected: (String) -> Unit
) {
    // Using a Row with very small RadioButtons and labels.
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 20.dp)

    ) {
        RadioButton(
            selected = selectedMode == "Anim",
            onClick = { onModeSelected("Anim") },
            modifier = Modifier.size(3.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = "Animation", fontSize = 10.sp)
        Spacer(modifier = Modifier.width(10.dp))
        RadioButton(
            selected = selectedMode == "Canvas",
            onClick = { onModeSelected("Canvas") },
            modifier = Modifier.size(5.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = "Canvas", fontSize = 10.sp)
        RadioButton(
            selected = selectedMode == "WebBrowser",
            onClick = { onModeSelected("WebBrowser") },
            modifier = Modifier.size(5.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = "WebBrowser", fontSize = 10.sp)
    }
}





// --------------------
// Playground Screen with Mode Switch
// --------------------


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaygroundScreen(
   navController: NavController
) {
    // State controlling the height ratio of the top card.
    var topWeight by remember { mutableStateOf(0.5f) }
    // Total container height in pixels.
    var containerHeight by remember { mutableStateOf(0) }
    // Mode: "Anim" for animation, "Canvas" for drawing.
    var mode by remember { mutableStateOf("Anim") }
    val density = LocalDensity.current


    var receivedData by remember { mutableStateOf("") }

    var simulationCode by remember { mutableStateOf("") }
    var explanationText by remember { mutableStateOf("") }



    val webSocketRef = remember { mutableStateOf<WebSocket?>(null) }
    DisposableEffect(Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("ws://localhost:8000/api/playground/")
            .build()
        val webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connection opened")
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    // Parse the incoming JSON text
                    val jsonObject = JSONObject(text)
                    // Extract the "message" object from the JSON
                    val messageObj = jsonObject.getJSONObject("message")
                    // Extract the explanation text and simulation code (if needed)
                    val explanation = messageObj.getString("Explanations")
                    val simulationCode = messageObj.getString("simulation_code")

                    // Post updates to the UI thread
                    Handler(Looper.getMainLooper()).post {
                        // You can update different state variables as needed.
                        receivedData = simulationCode  // Example: display simulation code in one place
                        explanationText = explanation    // Display explanation text separately
                        Log.d("WebSocket", "Simulation Code: $receivedData")
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Closing: $code / $reason")
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error: ${t.message}")
            }
        })
        webSocketRef.value = webSocket
        onDispose { webSocket.close(1000, "CascadingSubjectUnitTopicList disposed") }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Very compact mode switch at the top.
                CompactModeSwitch(selectedMode = mode, onModeSelected = { mode = it })
                // Container for top card (animation/canvas) and explanation card.
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            containerHeight = coordinates.size.height
                        }
                ) {
                    // Top card: displays either Animation (default) or Canvas mode.
                    Card(
                        modifier = Modifier
                            .weight(topWeight)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        elevation = cardElevation(defaultElevation = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = cardColors(
                            containerColor = if (mode == "Anim") Color.Black else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        if (mode == "Anim") {
                            // Animation mode: use AndroidView with WebView.
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { context ->
                                    WebView(context).apply {
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        // Initial load (if receivedData is not empty)
                                        if (receivedData.isNotEmpty()) {
                                            post {
                                                loadDataWithBaseURL(null, receivedData, "text/html", "UTF-8", null)
                                            }
                                        }
                                    }
                                },
                                update = { webView ->
                                    // Reload whenever receivedData changes
                                    if (receivedData.isNotEmpty()) {
                                        webView.post {
                                            webView.loadDataWithBaseURL(null, receivedData, "text/html", "UTF-8", null)
                                        }
                                    }
                                }
                            )
                        }else if (mode == "Camera"){

                        CameraPreview()

                        }else if(mode == "WebBrowser"){

                            BrowserSearchScreen()
                        }
                        else {
                            // Canvas mode: Show a simple drawing canvas.
                            DrawCanvas(modifier = Modifier.fillMaxSize())
                        }
                    }
                    // Draggable divider.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .pointerInput(containerHeight) {
                                detectVerticalDragGestures { _, dragAmount ->
                                    if (containerHeight > 0) {
                                        val fractionChange = dragAmount / containerHeight
                                        topWeight = (topWeight + fractionChange).coerceIn(0.2f, 0.8f)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .height(6.dp)
                                .background(
                                    color = Color.Gray.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                    }
                    // Bottom card: Explanation area.
                    Card(
                        modifier = Modifier
                            .weight(1f - topWeight)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        elevation = cardElevation(defaultElevation = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                                .verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.TopStart
                        ) {
                            Text(
                                    text = if (explanationText.isNotEmpty()) explanationText  else "Waiting for data...",
                                    style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                // Bottom area: Chat input bar.
                ChatInputBar(
                    webSocketRef = webSocketRef,
                    onModeToggle = {
                    mode = if (mode == "Anim") "Camera" else "Anim"
                })
            }
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserSearchScreen() {
    // State to hold the search query.
    var searchQuery by remember { mutableStateOf("") }
    // Reference to the WebView instance.
    var webViewInstance: WebView? by remember { mutableStateOf(null) }
    // Track the current URL loaded in the WebView.
    var currentUrl by remember { mutableStateOf("") }
    // Store the snapshot bitmap when captured.
    var snapshotBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search input field.
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        // Row for Search and Snapshot buttons.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    // Launch a search in the WebView using Google search.
                    val encodedQuery = Uri.encode(searchQuery)
                    val searchUrl = "https://www.google.com/search?q=$encodedQuery"
                    webViewInstance?.loadUrl(searchUrl)
                }
            ) {
                Text("Search")
            }
            Button(
                onClick = {
                    // Capture snapshot of the current WebView.
                    webViewInstance?.let { webView ->
                        snapshotBitmap = captureWebViewScreenshot(webView)
                        Log.d("WebView", "Snapshot captured of size: ${snapshotBitmap?.width}x${snapshotBitmap?.height}")
                    }
                }
            ) {
                Text("Take Snapshot")
            }
        }
        // WebView container.
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    // Override WebViewClient to track page loads.
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            currentUrl = url ?: ""
                            Log.d("WebView", "Page started loading: $currentUrl")
                        }
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            currentUrl = url ?: ""
                            Log.d("WebView", "Page finished loading: $currentUrl")
                        }
                    }
                    // Optionally, load a default page.
                    loadUrl("https://www.google.com")
                    // Save this instance so that we can access it later.
                    webViewInstance = this
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        // Display the captured snapshot (if any).
        snapshotBitmap?.let { bitmap ->
            Text(
                text = "Snapshot:",
                modifier = Modifier.padding(16.dp)
            )
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "WebView Snapshot",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp)
            )
        }
    }
}

/**
 * Captures a snapshot of the given WebView.
 *
 * @param webView The WebView to capture.
 * @return A Bitmap containing the screenshot, or null if the WebView has no size.
 */
fun captureWebViewScreenshot(webView: WebView): Bitmap? {
    if (webView.width == 0 || webView.height == 0) return null
    val bitmap = Bitmap.createBitmap(webView.width, webView.height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    webView.draw(canvas)
    return bitmap
}



@Composable
fun CameraPreview(modifier: Modifier = Modifier.fillMaxSize()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            // Create and configure the PreviewView.
            val previewView = androidx.camera.view.PreviewView(ctx).apply {
                scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
            }

            // Get the camera provider.
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    // Create the Preview use case.
                    val preview = Preview.Builder().build()

                    // Post the SurfaceProvider setup after layout.
                    previewView.post {
                        preview.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    // Select the back camera.
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    // Unbind all use cases before binding the new one.
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview
                    )
                } catch (exc: Exception) {
                    Log.e("CameraPreview", "Failed to bind camera use cases", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}








@Composable
fun DrawCanvas(modifier: Modifier = Modifier) {
    var currentPath by remember { mutableStateOf(Path()) }
    var paths by remember { mutableStateOf(listOf<Path>()) }

    Canvas(
        modifier = modifier
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset: Offset ->
                        currentPath = Path().apply { moveTo(offset.x, offset.y) }
                    },
                    onDrag = { change: PointerInputChange, dragAmount: Offset ->
                        currentPath.lineTo(change.position.x, change.position.y)
                    },
                    onDragEnd = {
                        paths = paths + currentPath
                    }
                )
            }
    ) {
        // Draw all saved paths.
        paths.forEach { path ->
            drawPath(
                path = path,
                color = Color.Blue,
                style = Stroke(width = 4f)
            )
        }
        // Draw the current path.
        drawPath(
            path = currentPath,
            color = Color.Blue,
            style = Stroke(width = 4f)
        )
    }
}





@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CascadingSubjectUnitTopicList(
    webSocketRef: MutableState<WebSocket?>,
    subjectListViewModel: SubjectlistViewModel = viewModel(
        factory = SubjectlistViewModelFactory(
            SubjectlistRepository(RetrofitClient.apiService)
        )
    ),
    onSelection: (selections: Map<String, Map<String, Map<String, List<String>>>>, Any?) -> Unit
) {
    // Fetch subjects on composition.
    LaunchedEffect(Unit) {
        subjectListViewModel.fetchSubjectList(userId = 1)
    }
    // Observe subjects.
    val subjects by subjectListViewModel.subjectList.collectAsState(initial = emptyList())

    // Local state holders.
    val expandedSubjects = remember { mutableStateListOf<Int>() }
    val expandedUnitsMap = remember { mutableStateMapOf<Int, SnapshotStateList<Int>>() }

    // Instead of storing only IDs, we now store the full selected objects.
    // For the unit selection, we store one unit per subject.
    val selectedUnitDataMap: SnapshotStateMap<Int, SubjectSyllabusGetResponse> =
        remember { mutableStateMapOf() }
    // For topics, we allow multiple selections per unit.
    val selectedTopicDataMap: SnapshotStateMap<Int, SnapshotStateList<SubjectSyllabusHeadingTopic>> =
        remember { mutableStateMapOf() }
    // For subtopics, keyed by topic id, we allow multiple subtopics per topic.
    val selectedSubtopicsDataMap: SnapshotStateMap<Int, SnapshotStateList<SubjectSyllabusHeadingTopicSubtopic>> =
        remember { mutableStateMapOf() }



    val scrollState = rememberScrollState()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Select Subject",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        subjects.forEach { subject ->
            Card(
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        // When a new subject is selected, clear all previous selections.
                        expandedSubjects.clear()
                        expandedUnitsMap.clear()
                        selectedUnitDataMap.clear()
                        selectedTopicDataMap.clear()
                        selectedSubtopicsDataMap.clear()
                        expandedSubjects.add(subject.id)
                    }
                    .animateContentSize(animationSpec = tween(300))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = subject.subject,
                        style = MaterialTheme.typography.titleMedium
                    )
                    AnimatedVisibility(
                        visible = expandedSubjects.contains(subject.id),
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
                        // Each subject loads its units (syllabus).
                        val syllabusViewModel: SubjectSyllabusViewModel = viewModel(
                            key = "syllabus_${subject.id}",
                            factory = SubjectSyllabusViewModelFactory(
                                repository = SubjectSyllabusGetRepository(RetrofitClient.apiService),
                                subjectId = subject.id
                            )
                        )
                        val syllabusList by syllabusViewModel.subjectSyllabus.observeAsState(emptyList())

                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Select Unit", style = MaterialTheme.typography.titleSmall)

                            syllabusList.forEach { syllabus ->
                                // When selecting a unit, store the full Syllabus object.
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .clickable {
                                            // Toggle unit expansion.
                                            val subjectUnits = expandedUnitsMap.getOrPut(subject.id) {
                                                mutableStateListOf()
                                            }
                                            if (subjectUnits.contains(syllabus.id)) {
                                                subjectUnits.remove(syllabus.id)
                                                selectedUnitDataMap.remove(subject.id)
                                            } else {
                                                subjectUnits.add(syllabus.id)
                                                selectedUnitDataMap[subject.id] = syllabus
                                            }
                                        }
                                        .animateContentSize(animationSpec = tween(300))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(
                                            text = "Unit: ${syllabus.syllabusChapterName}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = if (expandedUnitsMap[subject.id]?.contains(syllabus.id) == true)
                                                "Collapse" else "Expand"
                                        )
                                    }
                                }
                                AnimatedVisibility(
                                    visible = expandedUnitsMap[subject.id]?.contains(syllabus.id) == true,
                                    enter = fadeIn(animationSpec = tween(300)),
                                    exit = fadeOut(animationSpec = tween(300))
                                ) {
                                    // Load headings for the selected unit.
                                    val headingViewModel: SubjectSyllabusHeadingViewModel = viewModel(
                                        key = "heading_${syllabus.id}",
                                        factory = SubjectSyllabusHeadingViewModelFactory(
                                            repository = SubjectSyllabusHeadingRepository(RetrofitClient.apiService),
                                            syllabus_id = syllabus.id
                                        )
                                    )
                                    val headings by headingViewModel.subjectSyllabusHeading.observeAsState(emptyList())
                                    Text(
                                        text = "Headings count: ${headings.size}",
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    // If there is at least one heading, automatically pick the first heading.
                                    val topics = remember { mutableStateListOf<SubjectSyllabusHeadingTopic>() }
                                    if (headings.isNotEmpty()) {
                                        val firstHeading = headings.first()
                                        val topicViewModel: SubjectSyllabusHeadingTopicViewModel = viewModel(
                                            key = "topic_${firstHeading.id}",
                                            factory = SubjectSyllabusHeadingTopicViewModelFactory(
                                                repository = SubjectSyllabusHeadingTopicRepository(RetrofitClient.apiService),
                                                title_id = firstHeading.id
                                            )
                                        )
                                        val fetchedTopics by topicViewModel.subjectsyllabusheadingTopic.observeAsState(emptyList())
                                        topics.clear()
                                        topics.addAll(fetchedTopics)
                                    }

                                    Column(modifier = Modifier.padding(start = 16.dp)) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Select Topic(s)", style = MaterialTheme.typography.bodySmall)
                                        topics.forEach { topic ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        // Toggle topic selection.
                                                        val topicsForUnit = selectedTopicDataMap.getOrPut(syllabus.id) {
                                                            mutableStateListOf()
                                                        }
                                                        if (topicsForUnit.contains(topic)) {
                                                            topicsForUnit.remove(topic)
                                                        } else {
                                                            topicsForUnit.add(topic)
                                                        }
                                                    }
                                                    .padding(vertical = 6.dp)
                                            ) {
                                                Checkbox(
                                                    checked = selectedTopicDataMap[syllabus.id]?.contains(topic)
                                                        ?: false,
                                                    onCheckedChange = { checked ->
                                                        val topicsForUnit = selectedTopicDataMap.getOrPut(syllabus.id) {
                                                            mutableStateListOf()
                                                        }
                                                        if (checked)
                                                            topicsForUnit.add(topic)
                                                        else
                                                            topicsForUnit.remove(topic)
                                                    }
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(topic.topic, style = MaterialTheme.typography.bodyLarge)
                                            }
                                            // If this topic is selected, show its subtopics.
                                            if (selectedTopicDataMap[syllabus.id]?.contains(topic) == true) {
                                                // Load subtopics for the selected topic.
                                                val subtopicViewModel: SubjectSyllabusHeadingTopicSubtopicViewModel = viewModel(
                                                    key = "subtopic_${topic.id}",
                                                    factory = SubjectSyllabusHeadingTopicSubtopicViewModelFactory(
                                                        repository = SubjectSyllabusHeadingSubtopicRepository(RetrofitClient.apiService),
                                                        topic_id = topic.id
                                                    )
                                                )
                                                val subtopics by subtopicViewModel.subjectsyllabusheadingTopicSubtopic.observeAsState(emptyList())
                                                AnimatedVisibility(
                                                    visible = subtopics.isNotEmpty(),
                                                    enter = fadeIn(animationSpec = tween(300)),
                                                    exit = fadeOut(animationSpec = tween(300))
                                                ) {
                                                    Column(modifier = Modifier.padding(start = 16.dp)) {
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text("Select Subtopic(s)", style = MaterialTheme.typography.bodySmall)
                                                        subtopics.forEach { subtopic ->
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .clickable {
                                                                        val selectedSubs = selectedSubtopicsDataMap.getOrPut(topic.id) {
                                                                            mutableStateListOf()
                                                                        }
                                                                        if (selectedSubs.contains(subtopic))
                                                                            selectedSubs.remove(subtopic)
                                                                        else
                                                                            selectedSubs.add(subtopic)
                                                                    }
                                                                    .padding(vertical = 4.dp)
                                                            ) {
                                                                Checkbox(
                                                                    checked = selectedSubtopicsDataMap[topic.id]?.contains(subtopic)
                                                                        ?: false,
                                                                    onCheckedChange = { checked ->
                                                                        val selectedSubs = selectedSubtopicsDataMap.getOrPut(topic.id) {
                                                                            mutableStateListOf()
                                                                        }
                                                                        if (checked)
                                                                            selectedSubs.add(subtopic)
                                                                        else
                                                                            selectedSubs.remove(subtopic)
                                                                    }
                                                                )
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                Text(subtopic.subtopic, style = MaterialTheme.typography.bodySmall)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                // Build a nested map using descriptive names.
                val finalSelections: MutableMap<String, Map<String, Map<String, List<String>>>> = mutableMapOf()
                subjects.forEach { subject ->
                    // Get the selected unit for the subject.
                    selectedUnitDataMap[subject.id]?.let { unit ->
                        val unitName = unit.syllabusChapterName
                        // Get the list of selected topics for the unit.
                        val topicsForUnit = selectedTopicDataMap[unit.id] ?: mutableStateListOf()
                        // Build a map for topics and their subtopics.
                        val topicMap: MutableMap<String, List<String>> = mutableMapOf()
                        topicsForUnit.forEach { topic ->
                            val topicName = topic.topic
                            // Get subtopics for each topic.
                            val subtopicNames = selectedSubtopicsDataMap[topic.id]?.map { it.subtopic } ?: emptyList()
                            topicMap["Topic: $topicName"] = subtopicNames
                        }
                        val unitMap = mapOf("Unit: $unitName" to topicMap)
                        finalSelections[subject.subject] = unitMap
                    }
                }

                val finalData = mapOf(
                    "selections" to finalSelections,
                    "screenWidth" to screenWidth,
                    "screenHeight" to screenHeight
                )



                if (finalSelections.isNotEmpty()) {
                    onSelection(finalSelections, null)
                    val gson = Gson()
                    val jsonSelections = gson.toJson(finalData)
                    Log.d("WebSocket", "Sending selection: $jsonSelections")
                    webSocketRef.value?.send(jsonSelections)
                }
            },
            enabled = selectedUnitDataMap.isNotEmpty() && selectedTopicDataMap.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirm Selection", style = MaterialTheme.typography.bodyLarge)
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputBar(
    webSocketRef: MutableState<WebSocket?>,
    onModeToggle: () -> Unit
) {
    var message by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val focusManager = LocalFocusManager.current


    var isVisible by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }


    // State for the selected file URI (if any)
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    // Create a file picker launcher that accepts all file types.
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // When a file is picked, store its URI.
        uri?.let { selectedFileUri = it }
    }

    val borderColor by animateColorAsState(
        targetValue = if (message.text.isEmpty()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 300)
    )

    val density = LocalDensity.current
    val keyboardHeight = WindowInsets.ime.getBottom(density)
    val isKeyboardOpen = keyboardHeight > 0

    // Control whether the modal bottom sheet is shown.
    var showSubjectSheet by remember { mutableStateOf(false) }

    // Control whether the Learn option dialog is shown.
    var showDialog by remember { mutableStateOf(false) }
    // State for the current learning option (displayed in the text below the icon)
    var currentLearnOption by remember { mutableStateOf("Learn") }

    val micOn = ImageVector.vectorResource(id = R.drawable.baseline_mic_24)
    val micOff = ImageVector.vectorResource(id = R.drawable.baseline_mic_off_24)
    val Camera = ImageVector.vectorResource(id = R.drawable.baseline_photo_camera_24)
    val Book = ImageVector.vectorResource(id = R.drawable.baseline_book_24)
    val file = ImageVector.vectorResource(id = R.drawable.baseline_attach_file_24)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
                .focusRequester(focusRequester)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                ),
            // If a file is selected, show a circular indicator as the leading icon.

            leadingIcon = selectedFileUri?.let {
                {
                    BadgedBox(
                        badge = {
                            Badge(
                                modifier = Modifier.clickable { selectedFileUri = null }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Remove File",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            },
            placeholder = {
                Text(
                    text = "Message ChatGPT",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            },
            trailingIcon = {
                IconButton(onClick = { focusManager.clearFocus() }) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send Message",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color.Transparent,
                unfocusedBorderColor = borderColor,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (!isKeyboardOpen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button with Label for Mic On/Off
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    var isMicOn by remember { mutableStateOf(true) }
                    IconButton(
                        onClick = { isMicOn = !isMicOn },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isMicOn) micOn else micOff,
                            contentDescription = if (isMicOn) "Mic On" else "Mic Off",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = if (isMicOn) "On" else "Off",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                // Play Button with Label (opens the Learn options dialog)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Learn",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = currentLearnOption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                // File Button with Label (opens the file picker)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = file,
                            contentDescription = "Select File",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "File",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                // Toggle Camera/Animation Button with Label
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { onModeToggle() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Camera,
                            contentDescription = "Toggle Camera/Animation",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Camera",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                // Select Subject/Unit/Topic Button with Label
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { showSubjectSheet = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Book,
                            contentDescription = "Select Subject/Unit/Topic",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Subject",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }

    // Show modal bottom sheet for subjects
    if (showSubjectSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSubjectSheet = false }
        ) {
            CascadingSubjectUnitTopicList(
                webSocketRef = webSocketRef,
                onSelection = { selections, _ ->
                    // Handle the selection here.
                    showSubjectSheet = false
                }
            )
        }
    }

    // Show the Learn option popup when showDialog is true.
    if (showDialog) {
        LearnOptionPopup(
            onDismiss = { showDialog = false },
            onSave = { selected ->
                currentLearnOption = selected
                showDialog = false
            },
            initialOption = if (currentLearnOption == "Learn") "Normal Learn" else currentLearnOption
        )
    }
}





@Composable
fun LearnOptionPopup(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    initialOption: String = "Normal Learn"
) {
    val options = listOf("Deep Learn", "Normal Learn", "Hard Learn")
    var selectedOption by remember { mutableStateOf(initialOption) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Learning Type",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selectedOption == option)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else Color.Transparent
                            )
                            .clickable { selectedOption = option }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedOption == option),
                            onClick = { selectedOption = option },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedOption) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Save", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}
