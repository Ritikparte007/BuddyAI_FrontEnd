package com.example.neuroed

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.neuroed.model.ExamUiModel
import com.example.neuroed.model.UserInfoViewModel
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.ExamRepository
import com.example.neuroed.viewmodel.ExamViewModel
import com.example.neuroed.viewmodel.ExamViewModelFactory
import com.example.neuroed.viewmodel.ExamsUiState
import kotlinx.coroutines.delay
import com.example.neuroed.model.toExamItem

/*───────── shared tokens (inline) ─────────*/
private object DS {
    /* brand */
    val Purple     = Color(0xFF7F66D3)
    val PurpleLit  = Color(0xFF916DFF)

    /* surfaces */
    val BgLight    = Color(0xFFF8F9FB)
    val BgDark     = Color(0xFF121212)
    val SurfLight  = Color.White
    val SurfDark   = Color(0xFF1E1E1E)
    val ChipLight  = Color(0xFFE5E6F8)
    val ChipDark   = Color.White.copy(alpha = .08f)

    /* outline / track */
    val OutlineLight = Color(0xFFC9D2DA)
    val OutlineDark  = Color(0xFF2F3339)

    /* text */
    val TxtPriLight  = Color(0xFF1F2937)
    val TxtPriDark   = Color.White
    val TxtSecLight  = Color(0xFF535C64)
    val TxtSecDark   = Color(0xFFB4B7BA)

    /* status */
    val Error   = Color(0xFFCF6679)
    val Warn    = Color(0xFFE6B422)
    val Info    = Color(0xFF42A5F5)
    val Success = Color(0xFF66BB6A)
}

/*───────── top-level screen ─────────*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(
    navController: NavController,
    userInfoViewModel: UserInfoViewModel = viewModel()
) {
    val context = LocalContext.current

    /* Load user ID and observe it */
    LaunchedEffect(Unit) {
        userInfoViewModel.loadUserId(context)
    }
    val currentUserId by userInfoViewModel.userId.collectAsState()

    /* ViewModel - only create when we have valid user ID */
    val examViewModel: ExamViewModel? = if (currentUserId != NeuroEdApp.INVALID_USER_ID) {
        viewModel(
            factory = ExamViewModelFactory(
                ExamRepository(RetrofitClient.apiService),
                currentUserId
            )
        )
    } else null

    /* dynamic palette */
    val dark    = isSystemInDarkTheme()
    val bg      = if (dark) DS.BgDark else DS.BgLight
    val surf    = if (dark) DS.SurfDark else DS.SurfLight
    val chipBg  = if (dark) DS.ChipDark else DS.ChipLight
    val txtPri  = if (dark) DS.TxtPriDark else DS.TxtPriLight
    val txtSec  = if (dark) DS.TxtSecDark else DS.TxtSecLight
    val outline = if (dark) DS.OutlineDark else DS.OutlineLight

    /* local UI state */
    var tabIdx by remember { mutableStateOf(0) }
    val tabs   = listOf("Pending Exam", "Completed Exam")

    /* data state - only observe if viewModel exists */
    val pendingExamsState by if (examViewModel != null) {
        examViewModel.pendingExamsState.collectAsStateWithLifecycle()
    } else {
        remember { mutableStateOf(ExamsUiState.Loading) }
    }

    val completedExamsState by if (examViewModel != null) {
        examViewModel.completedExamsState.collectAsStateWithLifecycle()
    } else {
        remember { mutableStateOf(ExamsUiState.Loading) }
    }

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Exam",
                        color      = txtPri,
                        fontSize   = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(DS.Purple.copy(alpha = .1f))
                    ) {
                        Icon(Icons.Default.ArrowBack, null, tint = DS.Purple)
                    }
                },
                actions = {
                    if (examViewModel != null) {
                        IconButton(onClick = examViewModel::refreshExams) {
                            Icon(Icons.Default.Refresh, "Refresh", tint = DS.Purple)
                        }
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = bg)
            )
        }
    ) { pad ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp)
        ) {

            /* Show loading if user ID not available */
            if (currentUserId == NeuroEdApp.INVALID_USER_ID || examViewModel == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DS.Purple)
                }
                return@Column
            }

            /*──────── tabs ────────*/
            TabRow(
                selectedTabIndex = tabIdx,
                containerColor   = surf,
                indicator = { positions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(positions[tabIdx]),
                        color = DS.Purple
                    )
                }
            ) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = tabIdx == i,
                        onClick  = { tabIdx = i },
                        text     = {
                            Text(
                                title,
                                fontSize = 14.sp,
                                color    = if (tabIdx == i) DS.Purple else txtSec
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            /*──────── content ────────*/
            when (tabIdx) {
                0 -> handleTab(
                    uiState     = pendingExamsState,
                    emptyMsg    = "No pending exams found",
                    txtPri      = txtPri,
                    onReload    = examViewModel::loadPendingExams,
                    examsAction = { exam ->
                        val examItem = exam.toExamItem(currentUserId)
                        navController.currentBackStackEntry?.savedStateHandle?.set("exam", examItem)
                        navController.navigate("ExamStartScreen")
                    },
                    surf = surf, chipBg = chipBg, txtSec = txtSec, outline = outline
                )

                1 -> handleTab(
                    uiState     = completedExamsState,
                    emptyMsg    = "No completed exams found",
                    txtPri      = txtPri,
                    onReload    = examViewModel::loadCompletedExams,
                    examsAction = { exam ->
                        val examItem = exam.toExamItem(currentUserId)
                        navController.currentBackStackEntry?.savedStateHandle?.set("exam", examItem)
                        navController.navigate("exam_results_screen")
                    },
                    surf = surf, chipBg = chipBg, txtSec = txtSec, outline = outline
                )
            }
        }
    }
}

/*──────── common tab-body handler ─────────*/
@Composable
private fun handleTab(
    uiState: ExamsUiState,
    emptyMsg: String,
    txtPri: Color,
    onReload: () -> Unit,
    examsAction: (ExamUiModel) -> Unit,
    surf: Color,
    chipBg: Color,
    txtSec: Color,
    outline: Color
) {
    when (uiState) {
        is ExamsUiState.Loading -> {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = DS.Purple)
            }
        }

        is ExamsUiState.Empty -> {
            EmptyExamState(message = emptyMsg, txtPri = txtPri)
        }

        is ExamsUiState.Success -> {
            ExamList(
                exams     = uiState.exams,
                surf      = surf,
                chipBg    = chipBg,
                txtPri    = txtPri,
                txtSec    = txtSec,
                outline   = outline,
                onStartExam = examsAction
            )
        }

        is ExamsUiState.Error -> {
            ErrorExamState(
                message = uiState.message,
                txtPri  = txtPri,
                onRetry = onReload
            )
        }
    }
}

/*──────── list of cards ─────────*/
@Composable
private fun ExamList(
    exams: List<ExamUiModel>,
    surf: Color,
    chipBg: Color,
    txtPri: Color,
    txtSec: Color,
    outline: Color,
    onStartExam: (ExamUiModel) -> Unit
) {
    LazyColumn {
        items(exams) { exam ->
            ExamItemCard(
                exam      = exam,
                surf      = surf,
                chipBg    = chipBg,
                txtPri    = txtPri,
                txtSec    = txtSec,
                outline   = outline,
                onStartExam = { onStartExam(exam) }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

/*──────── empty & error states ─────────*/
@Composable
private fun EmptyExamState(message: String, txtPri: Color) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Text(
            message,
            color      = txtPri,
            fontSize   = 16.sp,
            textAlign  = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorExamState(
    message: String,
    txtPri: Color,
    onRetry: () -> Unit
) {
    Column(
        modifier             = Modifier.fillMaxSize(),
        horizontalAlignment  = Alignment.CenterHorizontally,
        verticalArrangement  = Arrangement.Center
    ) {
        Icon(Icons.Default.Warning, "Error", tint = DS.Error, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text("Error loading exams", color = txtPri, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            color     = txtPri.copy(alpha = 0.7f),
            fontSize  = 14.sp,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors  = ButtonDefaults.buttonColors(containerColor = DS.Purple)
        ) {
            Text("Retry")
        }
    }
}

/*──────── card ─────────*/
@Composable
private fun ExamItemCard(
    exam: ExamUiModel,
    surf: Color,
    chipBg: Color,
    txtPri: Color,
    txtSec: Color,
    outline: Color,
    onStartExam: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape      = RoundedCornerShape(16.dp),
        colors     = CardDefaults.cardColors(containerColor = surf),
        elevation  = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(20.dp)) {

            /* header */
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = exam.subject,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = txtPri
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape  = RoundedCornerShape(8.dp),
                            color  = Color.Transparent,
                            border = BorderStroke(1.dp, DS.Purple)
                        ) {
                            Text(
                                "${exam.marks} Marks",
                                fontSize = 12.sp,
                                color    = DS.Purple,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = exam.unitWithName,
                        fontSize = 14.sp,
                        color = txtSec
                    )
                }

                if (exam.logoUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(exam.logoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Exam logo",
                        contentScale       = ContentScale.Crop,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .border(2.dp, DS.Purple, CircleShape)
                            .padding(2.dp)
                    )
                } else {
                    Box(
                        Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .border(2.dp, DS.Purple, CircleShape)
                            .padding(2.dp)
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            null,
                            tint    = DS.Purple,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            /* progress quartet */
            QuartetProgress(
                bad   = exam.badThreshold,
                avg   = exam.averageThreshold,
                good  = exam.goodThreshold,
                excel = exam.excellenceThreshold
            )

            Spacer(Modifier.height(18.dp))

            /* footer */
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                CountdownPill(exam.remainingTimeMs, txtPri)
                Button(
                    onClick = onStartExam,
                    shape   = RoundedCornerShape(24.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = DS.Purple)
                ) {
                    Text(
                        if (!exam.isActive) "View Results" else "Start Exam",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/*──────── vertical progress quartet ─────────*/
@Composable
private fun QuartetProgress(bad: Float, avg: Float, good: Float, excel: Float) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
        VBar("Bad",      bad,   DS.Error)
        VBar("Average",  avg,   DS.Warn)
        VBar("Good",     good,  DS.Info)
        VBar("Excel.",   excel, DS.Success)
    }
}

@Composable
private fun VBar(label: String, prog: Float, col: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = DS.TxtSecLight,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .height(70.dp)
                .width(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(DS.OutlineDark.copy(alpha = .5f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(prog)
                    .background(col)
                    .align(Alignment.BottomCenter)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${(prog * 100).toInt()}%",
            fontSize = 12.sp,
            color = col
        )
    }
}

/*──────── countdown pill ─────────*/
@Composable
private fun CountdownPill(ms: Long, txtPri: Color) {
    var left by remember { mutableStateOf(ms) }
    LaunchedEffect(ms) {
        while (left > 0) {
            delay(1_000)
            left -= 1_000
        }
    }
    val d = left / 86_400_000
    val h = (left % 86_400_000) / 3_600_000
    val m = (left % 3_600_000) / 60_000
    val s = (left % 60_000) / 1_000

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = txtPri.copy(alpha = .1f)
    ) {
        Text(
            " ${d}d ${h}h ${m}m ${s}s ",
            fontSize   = 12.sp,
            fontWeight = FontWeight.Bold,
            color      = txtPri,
            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}