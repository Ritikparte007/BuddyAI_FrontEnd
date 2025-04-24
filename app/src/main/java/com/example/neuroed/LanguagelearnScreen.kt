import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
//import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
//import androidx.compose.material.icons.outlined.Lightbulb
//import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage

// Custom theme colors
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val PrimaryBlue = Color(0xFF2C8DFF)
val SecondaryTeal = Color(0xFF00C2CB)
val HighlightOrange = Color(0xFFFF8A3D)
val ErrorRed = Color(0xFFFF5252)
val GradientStart = Color(0xFF2C8DFF)
val GradientEnd = Color(0xFF00C2CB)
val CardBackground = Color(0xFF2A2A2A)
val ProfilePlaceholder = Color(0xFF3D3D3D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageLearnScreen(navController: NavController) {
    // States
    var selectedLeftLanguage by remember { mutableStateOf("English") }
    var selectedRightLanguage by remember { mutableStateOf("Hindi") }

    // Language options with flags and levels
    val languageOptions = listOf(
        LanguageOption("English", "🇺🇸", "Native"),
        LanguageOption("Spanish", "🇪🇸", "Intermediate"),
        LanguageOption("French", "🇫🇷", "Beginner"),
        LanguageOption("German", "🇩🇪", "Beginner"),
        LanguageOption("Hindi", "🇮🇳", "Advanced"),
        LanguageOption("Tamil", "🇮🇳", "Intermediate"),
        LanguageOption("Marathi", "🇮🇳", "Beginner"),
        LanguageOption("Bengali", "🇮🇳", "Beginner")
    )

    // Animated progress values
    val analyzingProgress = remember { mutableStateOf(0.3f) }
    val animatedAnalyzingProgress by animateFloatAsState(
        targetValue = analyzingProgress.value,
        animationSpec = tween(1000)
    )

    val progressingProgress = remember { mutableStateOf(0.6f) }
    val animatedProgressingProgress by animateFloatAsState(
        targetValue = progressingProgress.value,
        animationSpec = tween(1000)
    )

    // Card expanded state
    var isErrorCardExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            // Top App Bar with back button and title
            TopAppBar(
                title = {
                    Text(
                        "Language Learning",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = Color.White
                )
            )

            // Main content with padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile section with avatar
                ProfileSection()

                Spacer(modifier = Modifier.height(24.dp))

                // Language selection with improved dropdowns
                LanguageSelectionSection(
                    leftLanguageOptions = languageOptions,
                    rightLanguageOptions = languageOptions,
                    selectedLeftLanguage = selectedLeftLanguage,
                    selectedRightLanguage = selectedRightLanguage,
                    onLeftLanguageSelected = { selectedLeftLanguage = it },
                    onRightLanguageSelected = { selectedRightLanguage = it }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Progress indicators with improved visuals
                ProgressSection(
                    analyzingProgress = animatedAnalyzingProgress,
                    progressingProgress = animatedProgressingProgress
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Error correction card with animation
                ErrorCorrectionCard(
                    isExpanded = isErrorCardExpanded,
                    onExpandToggle = { isErrorCardExpanded = !isErrorCardExpanded }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Suggestion buttons with icons and better layout
                SuggestionButtonsSection()
            }
        }
    }
}

@Composable
fun ProfileSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar with border and shadow
        Box(
            modifier = Modifier
                .size(100.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(ProfilePlaceholder, Color(0xFF333333))
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "JD",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Status indicator
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50))
                    .border(2.dp, DarkBackground, CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // User name and level
        Text(
            text = "John Doe",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = SecondaryTeal,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Level 23 • 142 days streak",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionSection(
    leftLanguageOptions: List<LanguageOption>,
    rightLanguageOptions: List<LanguageOption>,
    selectedLeftLanguage: String,
    selectedRightLanguage: String,
    onLeftLanguageSelected: (String) -> Unit,
    onRightLanguageSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "I want to translate between",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left language dropdown
            LanguageDropdown(
                selectedLanguage = selectedLeftLanguage,
                languageOptions = leftLanguageOptions,
                onLanguageSelected = onLeftLanguageSelected,
                modifier = Modifier.weight(1f)
            )

            // Switch button
            IconButton(
                onClick = { /* Swap languages logic */ },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Switch languages",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Right language dropdown
            LanguageDropdown(
                selectedLanguage = selectedRightLanguage,
                languageOptions = rightLanguageOptions,
                onLanguageSelected = onRightLanguageSelected,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDropdown(
    selectedLanguage: String,
    languageOptions: List<LanguageOption>,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOption = languageOptions.find { it.name == selectedLanguage } ?: languageOptions.first()

    Box(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedOption.name,
                onValueChange = {},
                readOnly = true,
                leadingIcon = {
                    Text(
                        text = selectedOption.flag,
                        fontSize = 20.sp
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand dropdown",
                        tint = if (expanded) PrimaryBlue else Color.White
                    )
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = CardBackground,
//                    textColor = Color.White,
                    cursorColor = PrimaryBlue,
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(DarkSurface)
            ) {
                languageOptions.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = option.flag,
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Column {
                                    Text(
                                        text = option.name,
                                        color = Color.White
                                    )
                                    Text(
                                        text = option.level,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onLanguageSelected(option.name)
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = Color.White,
                            leadingIconColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressSection(
    analyzingProgress: Float,
    progressingProgress: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Your Progress",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Analyzing progress
            ProgressIndicator(
                title = "Grammar Analysis",
                progress = analyzingProgress,
                color = PrimaryBlue,
                percentage = (analyzingProgress * 100).toInt(),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Learning progress
            ProgressIndicator(
                title = "Learning Progress",
                progress = progressingProgress,
                color = SecondaryTeal,
                percentage = (progressingProgress * 100).toInt(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ProgressIndicator(
    title: String,
    progress: Float,
    color: Color,
    percentage: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "$percentage%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        // Custom styled progress indicator
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Color.Gray.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun ErrorCorrectionCard(
    isExpanded: Boolean,
    onExpandToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandToggle() },
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Grammar Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = SecondaryTeal
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sentence with error highlighting
            Text(
                buildAnnotatedString {
                    append("Yesterday, I ")
                    withStyle(
                        style = SpanStyle(
                            color = ErrorRed,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("go")
                    }
                    append(" to the mall and ")
                    withStyle(
                        style = SpanStyle(
                            color = ErrorRed,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("buyed")
                    }
                    append(" a new shirt.")
                },
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )

            // Details section that expands/collapses
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // Error 1
                    ErrorExplanation(
                        errorNumber = 1,
                        title = "Consistency of Tense",
                        explanation = "The word 'Yesterday' signals past tense, but 'go' does not match.",
                        correction = "'go' → 'went'",
                        rule = "Use past tense verbs with past time indicators."
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Error 2
                    ErrorExplanation(
                        errorNumber = 2,
                        title = "Irregular Verb Form",
                        explanation = "'Buyed' is incorrect. 'Buy' is an irregular verb.",
                        correction = "'buyed' → 'bought'",
                        rule = "Irregular verbs don't follow the standard '-ed' pattern."
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Corrected version
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1D3C54))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Corrected Version",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Yesterday, I went to the mall and bought a new shirt.",
                            color = Color.White,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorExplanation(
    errorNumber: Int,
    title: String,
    explanation: String,
    correction: String,
    rule: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Error number circle
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(ErrorRed),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = errorNumber.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Error details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = explanation,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = SecondaryTeal,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Correction: $correction",
                    color = SecondaryTeal,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Rule: $rule",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun SuggestionButtonsSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Learning Activities",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SuggestionButton(
                text = "Practice Speaking",
                icon = Icons.Outlined.Edit,
                gradient = Brush.linearGradient(listOf(Color(0xFF4A6FE3), Color(0xFF2D55E6))),
                modifier = Modifier.weight(1f)
            )

            SuggestionButton(
                text = "Write a Story",
                icon = Icons.Outlined.Edit,
                gradient = Brush.linearGradient(listOf(Color(0xFFE6525C), Color(0xFFDD2A36))),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SuggestionButton(
            text = "Get Language Tips",
            icon = Icons.Outlined.Check,
            gradient = Brush.linearGradient(listOf(Color(0xFFEAB830), Color(0xFFE59C00))),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SuggestionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { /* TODO */ },
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = text,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Data class for language options
data class LanguageOption(
    val name: String,
    val flag: String,
    val level: String
)