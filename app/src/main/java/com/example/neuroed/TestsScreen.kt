package com.example.neuroed
import com.example.neuroed.model.TestItem
import com.example.neuroed.model.TestType
import com.example.neuroed.model.UserInfoViewModel
/* ──────────────────────────────────────────────────
   TestsScreen – aligned with global UI pattern
   Self‑contained colours & dimensions (no external theme file)
   Author: ChatGPT (o3)
   ────────────────────────────────────────────────── */

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.neuroed.network.RetrofitClient.apiService
import com.example.neuroed.repository.TestListRepository
import com.example.neuroed.viewmodel.TestListViewModel
import com.example.neuroed.viewmodel.TestListViewModelFactory
import kotlinx.coroutines.delay


/* ─────────────────── design‑system tokens (local) ─────────────────── */
object TestsPalette {
    /* brand purple gradient */
    val primaryPurple   = Color(0xFF7F66D3)
    val secondaryPurple = Color(0xFF916DFF)

    /* light theme */
    val lightBg          = Color(0xFFF8F9FB)
    val lightSurface     = Color(0xFFFFFFFF)
    val lightChip        = Color(0xFFE5E6F8)
    val lightTextHigh    = Color(0xFF0F1419)
    val lightTextMedium  = Color(0xFF535C64)
    val outlineLight     = Color(0xFFC9D2DA)

    /* dark theme */
    val darkBg           = Color(0xFF121212)
    val darkSurface      = Color(0xFF1E1E1E)
    val darkChip         = Color.White.copy(alpha = .04f)
    val darkTextHigh     = Color(0xFFFFFFFF)
    val darkTextMedium   = Color(0xFFB3B3B3)
    val outlineDark      = Color(0xFF2F3339)

    /* semantic */
    val progressTrackLight = outlineLight
    val progressTrackDark  = outlineDark
}

/* ─────────────────── data models ─────────────────── */

/* ─────────────────── screen ─────────────────── */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestsScreen(
    navController: NavController,
    viewModel: TestListViewModel = viewModel(
        factory = TestListViewModelFactory(TestListRepository(apiService))
    ),
    userInfoViewModel: UserInfoViewModel = viewModel()
) {
    val context = LocalContext.current

    /* theme switch */
    val dark = isSystemInDarkTheme()
    val bg          = if (dark) TestsPalette.darkBg         else TestsPalette.lightBg
    val surface      = if (dark) TestsPalette.darkSurface    else TestsPalette.lightSurface
    val chipBg       = if (dark) TestsPalette.darkChip       else TestsPalette.lightChip
    val textPrimary  = if (dark) TestsPalette.darkTextHigh   else TestsPalette.lightTextHigh
    val textSecondary= if (dark) TestsPalette.darkTextMedium else TestsPalette.lightTextMedium
    val outline      = if (dark) TestsPalette.outlineDark    else TestsPalette.outlineLight
    val progressTrack= if (dark) TestsPalette.progressTrackDark else TestsPalette.progressTrackLight

    /* Load user ID and observe it */
    LaunchedEffect(Unit) {
        userInfoViewModel.loadUserId(context)
    }
    val currentUserId by userInfoViewModel.userId.collectAsState()

    /* fetch data with real user ID */
    LaunchedEffect(currentUserId) {
        if (currentUserId != NeuroEdApp.INVALID_USER_ID) {
            viewModel.fetchTestList(userId = currentUserId)
        }
    }
    val apiList = viewModel.testList.value

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(chipBg)
                    ) {
                        Icon(Icons.Filled.ArrowBack, null, tint = TestsPalette.primaryPurple)
                    }
                },
                title = {
                    Text(
                        "Tests", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg)
            )
        }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad).background(bg)) {
            when {
                currentUserId == NeuroEdApp.INVALID_USER_ID -> {
                    // Show loading or user not logged in state
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TestsPalette.primaryPurple)
                    }
                }
                apiList.isNullOrEmpty() -> EmptyState(textSecondary)
                else -> TestFeed(
                    items = apiList.map { api ->
                        TestItem(
                            id = api.id,
                            user_id = api.user_id,
                            subject = api.Subject,
                            difficulty = api.Difficulty,
                            questionCount = api.TotalQuestion,
                            SolveQuestion = api.SolveQuestion,
                            coins = api.TotalQuestion,
                            timeLeft = api.TimeCountDown,
                            note = api.Subtopic,
                            quotes = api.quotes,
                            testType = api.test_type?: "",
                            topic_id = api.topic_id,
                            topic = api.Topic,
                            Subtopic_id = api.Subtopic_id,
                            subtopic = api.Subtopic,
                            totalQuestion = api.TotalQuestion,
                            endTimeMs = api.endTimeMs
                        )
                    },
                    surface = surface,
                    chipBg = chipBg,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    outline = outline,
                    progressTrack = progressTrack,
                    navController = navController
                )
            }
        }
    }
}

/* ─────────────────── empty state ─────────────────── */

@Composable
private fun EmptyState(textColor: Color) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Email, contentDescription = null, tint = textColor, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(8.dp))
            Text("No tests available", color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/* ─────────────────── feed list ─────────────────── */

@Composable
private fun TestFeed(
    items: List<TestItem>,
    surface: Color,
    chipBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    outline: Color,
    progressTrack: Color,
    navController: NavController
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { item ->
            TestCard(
                item = item,
                surface = surface,
                chipBg = chipBg,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                outline = outline,
                progressTrack = progressTrack,
                navController = navController
            )
        }
    }
}

/* ─────────────────── individual card ─────────────────── */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TestCard(
    item: TestItem,
    surface: Color,
    chipBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    outline: Color,
    progressTrack: Color,
    navController: NavController
) {
    /* animations -------------------------------------------------------- */
    val rawProgress = if (item.questionCount > 0) {
        item.SolveQuestion.toFloat() / item.questionCount
    } else 0f

    // 2⃣ animate toward that value
    val progress by animateFloatAsState(
        targetValue = rawProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900)
    )
    val percent = (progress * 100).toInt()

    var expanded by remember { mutableStateOf(false) }
    val arrowRot by animateFloatAsState(if (expanded) 180f else 0f)

    /* card -------------------------------------------------------------- */
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),                   // smooth expand/collapse
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            /* ───────── header row ───────── */
            Row(verticalAlignment = Alignment.CenterVertically) {
                /* subject icon */
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(chipBg),
                    contentAlignment = Alignment.Center
                ) {
                    Image(painterResource(R.drawable.biology), null)
                }
                Spacer(Modifier.width(12.dp))

                /* subject + topic/subtopic */
                Column(Modifier.weight(1f)) {
                    Text(
                        item.subject,
                        color = textPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${item.topic}  •  ${item.subtopic}",
                        color = textSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                /* difficulty chip */
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = chipBg,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .border(1.dp, outline, RoundedCornerShape(12.dp))
                ) {
                    Text(
                        item.difficulty,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = textSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            /* ───────── progress row ─────── */
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${item.SolveQuestion}/${item.questionCount}",
                    color = textPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.width(8.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = TestsPalette.primaryPurple,
                    trackColor = progressTrack
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "$percent%",
                    color = textSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            /* ───────── meta row ─────────── */
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CountdownTimer(
                    endTimestampMs = item.endTimeMs,
                    textColor      = textPrimary
                )

                Spacer(Modifier.width(8.dp))

                // this weight pushes the button to the far right
                Spacer(Modifier.weight(1f))

                Button(onClick = {
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("test", item)
                    navController.navigate("TestStartScreen")
                }) {
                    Text("Start")
                }
            }

            /* ───────── footer ───────────── */
            Spacer(Modifier.height(12.dp))
            Divider(color = outline)
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    if (expanded) "Show less" else "Show more",
                    color = TestsPalette.primaryPurple
                )
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier
                        .rotate(arrowRot)
                        .size(16.dp),
                    tint = TestsPalette.primaryPurple
                )
            }

            /* expanded details */
            AnimatedVisibility(expanded) {
                Column(Modifier.padding(top = 8.dp)) {
                    Text(item.quotes.orEmpty(), color = textPrimary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/* ─────────────────── countdown pill ─────────────────── */

@Composable
fun CountdownTimer(endTimestampMs: Long, textColor: Color) {

    val now by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            delay(1000L)
            value = System.currentTimeMillis()
        }
    }

    val remainSec = ((endTimestampMs - now) / 1000L).coerceAtLeast(0L).toInt()
    val h = remainSec / 3600
    val m = (remainSec % 3600) / 60
    val s = remainSec % 60
    val timeText = String.format("%02d:%02d:%02d", h, m, s)

    Box(
        modifier = Modifier
            .background(
                color = TestsPalette.primaryPurple,    // ← अपना scelcted कलर
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = timeText,
            color = Color.White,                         // बटन जैसा कॉन्ट्रास्ट
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}