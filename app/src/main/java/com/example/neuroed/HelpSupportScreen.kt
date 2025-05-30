package com.example.neuroed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/* ─────────────────── colour tokens ─────────────────── */
object HelpSupportColors {
    /* brand */
    val primaryPurple   = Color(0xFF6A5ACD)
    val secondaryPurple = Color(0xFF9370DB)

    /* LIGHT */
    val lightBg      = Color(0xFFF9FAFB)   // page
    val lightSurface = Color(0xFFFFFFFF)   // cards
    val lightChip    = Color(0xFFE6E6FA)   // chips
    val lightText    = Color(0xFF6B7280)   // secondary text

    /* DARK (two tones like light mode) */
    val darkBg       = Color(0xFF121212)   // page
    val darkSurface  = Color(0xFF1E1E1E)   // cards
    val darkChip     = Color(0xFF242424)   // chips
    val darkText     = Color.White         // primary text
    val darkTextLite = Color(0xFFB3B3B3)   // secondary text

    /* misc */
    val dividerColor  = Color(0xFFE5E7EB)
    val successGreen  = Color(0xFF34D399)
    val infoBlue      = Color(0xFF3B82F6)
    val warningYellow = Color(0xFFFBBF24)
    val errorRed      = Color(0xFFE53935)
}

/* ─────────────────── screen ─────────────────── */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(navController: NavController) {

    /* theme-aware colours */
    val isDark        = isSystemInDarkTheme()
    val pageBg        = if (isDark) HelpSupportColors.darkBg      else HelpSupportColors.lightBg
    val cardBg        = if (isDark) HelpSupportColors.darkSurface else HelpSupportColors.lightSurface
    val chipBg        = if (isDark) HelpSupportColors.darkChip    else HelpSupportColors.lightChip
    val textPrimary   = if (isDark) HelpSupportColors.darkText    else Color(0xFF1F2937)
    val textSecondary = if (isDark) HelpSupportColors.darkTextLite else HelpSupportColors.lightText

    val visible = remember {
        MutableTransitionState(false).apply { targetState = true }
    }

    Scaffold(
        containerColor = pageBg,
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
                        Icon(Icons.Default.ArrowBack, null, tint = HelpSupportColors.primaryPurple)
                    }
                },
                title = {
                    Text("Help & Support",
                        style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textPrimary))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = pageBg)
            )
        }
    ) { innerPad ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(innerPad)
                .background(pageBg),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            /* ───── header ───── */
            item {
                AnimatedVisibility(
                    visible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -40 }
                ) { HelpSupportHeader() }
            }

            /* ───── search ───── */
            item {
                var query by remember { mutableStateOf("") }
                AnimatedVisibility(
                    visible,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search for help topics...", color = textSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = HelpSupportColors.primaryPurple) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = HelpSupportColors.primaryPurple,
                            unfocusedBorderColor = HelpSupportColors.dividerColor,
                            cursorColor = HelpSupportColors.primaryPurple,
//                            textColor = textPrimary
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                }
            }

            /* ───── quick help chips ───── */
            item {
                AnimatedVisibility(
                    visible,
                    enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { -40 }
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Quick Help", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            QuickHelpItem(Icons.Outlined.Phone, "Tutorials",
                                HelpSupportColors.infoBlue, chipBg, textPrimary, Modifier.weight(1f)) {}
                            Spacer(Modifier.width(12.dp))
                            QuickHelpItem(Icons.Outlined.Notifications, "Live Chat",
                                HelpSupportColors.successGreen, chipBg, textPrimary, Modifier.weight(1f)) {}
                            Spacer(Modifier.width(12.dp))
                            QuickHelpItem(Icons.Outlined.Check, "Forums",
                                HelpSupportColors.warningYellow, chipBg, textPrimary, Modifier.weight(1f)) {}
                        }
                    }
                }
            }

            /* ───── support option cards ───── */
            item {
                AnimatedVisibility(
                    visible,
                    enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { -40 }
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Support Options", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Spacer(Modifier.height(16.dp))
                        SupportOptionCard(Icons.Outlined.Email, "Email Support",
                            "Get help via email within 24 hours", cardBg, chipBg, textPrimary, textSecondary)
                        Spacer(Modifier.height(12.dp))
                        SupportOptionCard(Icons.Outlined.Phone, "Phone Support",
                            "Call us at +1 (555) 123-4567", cardBg, chipBg, textPrimary, textSecondary)
                        Spacer(Modifier.height(12.dp))
                        SupportOptionCard(Icons.Outlined.Check, "Submit a Ticket",
                            "Create a support ticket for complex issues", cardBg, chipBg, textPrimary, textSecondary)
                    }
                }
            }

            /* ───── FAQ section ───── */
            item {
                AnimatedVisibility(
                    visible,
                    enter = fadeIn(tween(900)) + slideInVertically(tween(900)) { -40 }
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Frequently Asked Questions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Spacer(Modifier.height(16.dp))
                        FAQItem("How do I reset my password?",
                            "You can reset your password by going to the login screen and selecting 'Forgot Password'. Follow the instructions sent to your email to create a new password.",
                            cardBg, textPrimary, textSecondary)
                        FAQItem("How can I enroll in a course?",
                            "To enroll, navigate to the course catalog, select the course, and click 'Enroll'.",
                            cardBg, textPrimary, textSecondary)
                        FAQItem("Can I download course materials for offline viewing?",
                            "Yes, most course materials can be downloaded for offline access.",
                            cardBg, textPrimary, textSecondary)
                        FAQItem("How do I update my profile information?",
                            "Go to the 'Profile' section, then select 'Edit Profile'.",
                            cardBg, textPrimary, textSecondary)
                    }
                }
            }

            /* ───── community card ───── */
            item {
                AnimatedVisibility(
                    visible,
                    enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { -40 }
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Community Support", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Spacer(Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {},
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg)
                        ) {
                            Column(Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.Center,      // ⬅️ vertical centring
                                horizontalAlignment = Alignment.Start
                            ) {
                                Image(painterResource(id = R.drawable.notificationbell), null,
                                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop)
                                Spacer(Modifier.height(16.dp))
                                Text("Join Our Community", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                Spacer(Modifier.height(8.dp))
                                Text("Connect with other learners, share your experiences, and get answers from the community.",
                                    fontSize = 14.sp, color = textSecondary, textAlign = TextAlign.Center)
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = {},
                                    colors = ButtonDefaults.buttonColors(containerColor = HelpSupportColors.primaryPurple),
                                    shape = RoundedCornerShape(24.dp)) { Text("Join Community") }
                            }
                        }
                    }
                }
            }

            /* ───── feedback card ───── */
            item {
                AnimatedVisibility(
                    visible,
                    enter = fadeIn(tween(1100)) + slideInVertically(tween(1100)) { -40 }
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = chipBg)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Notifications, null, tint = HelpSupportColors.primaryPurple, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Share Your Feedback", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                Spacer(Modifier.height(4.dp))
                                Text("Help us improve by sharing your thoughts and suggestions", fontSize = 14.sp, color = textSecondary)
                            }
                            TextButton(onClick = {}, colors = ButtonDefaults.textButtonColors(contentColor = HelpSupportColors.primaryPurple)) {
                                Text("SHARE")
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ─────────────────── header banner ─────────────────── */
@Composable
fun HelpSupportHeader() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(
                Brush.verticalGradient(
                    listOf(HelpSupportColors.primaryPurple, HelpSupportColors.secondaryPurple)
                )
            )
    ) {
        Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center) {
            Text("How can we help you?",
                fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text("Find answers, get support, or contact us directly",
                fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

/* ─────────────────── quick help chip ─────────────────── */
@Composable
fun QuickHelpItem(
    icon: ImageVector,
    title: String,
    backgroundColor: Color,
    chipBg: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(chipBg)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(backgroundColor.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = backgroundColor, modifier = Modifier.size(24.dp)) }
        Spacer(Modifier.height(8.dp))
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textColor, textAlign = TextAlign.Center)
    }
}

/* ─────────────────── support option card ─────────────────── */
@Composable
fun SupportOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    cardColor: Color,
    circleBg: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable {},
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(circleBg), Alignment.Center) {
                Icon(icon, null, tint = HelpSupportColors.primaryPurple, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(description, fontSize = 14.sp, color = textSecondary)
            }
            Icon(Icons.Default.Email, null, tint = textSecondary, modifier = Modifier.size(24.dp))
        }
    }
}

/* ─────────────────── FAQ card ─────────────────── */
@Composable
fun FAQItem(
    question: String,
    answer: String,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(question, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = textPrimary, modifier = Modifier.weight(1f))
                Icon(
                    if (expanded) Icons.Outlined.Email else Icons.Outlined.Email,
                    null, tint = HelpSupportColors.primaryPurple, modifier = Modifier.size(24.dp)
                )
            }
            AnimatedVisibility(expanded) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Divider(color = HelpSupportColors.dividerColor, thickness = 1.dp)
                    Spacer(Modifier.height(8.dp))
                    Text(answer, fontSize = 14.sp, color = textSecondary, lineHeight = 20.sp)
                }
            }
        }
    }
    Spacer(Modifier.height(12.dp))
}
