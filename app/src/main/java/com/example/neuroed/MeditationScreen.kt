package com.example.neuroed

import androidx.compose.foundation.Image
import com.example.neuroed.utils.speakSsmlSuspend
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share // Keep share icon if needed
import androidx.compose.material.icons.outlined.Share // Or use outlined version if preferred
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.neuroed.ViewModelFactory.MeditationListViewModelFactory
import com.example.neuroed.network.RetrofitClient
import com.example.neuroed.repository.MeditationRepository
import com.example.neuroed.viewmodel.MeditationListViewModel
import androidx.compose.ui.text.style.TextOverflow

// --------- Design Tokens (Using HelpSupportScreen's style) ----------

// Re-use or import HelpSupportColors from its location
// If defined in another file, make sure to import it.
// object HelpSupportColors { ... } // Defined here for clarity if not imported

// Define common dimensions inspired by HelpSupportScreen style
object AppDimens { // This object correctly holds dimensions
    val paddingXs     = 4.dp
    val paddingSm     = 8.dp
    val paddingMd     = 16.dp // Common padding
    val paddingLg     = 24.dp // Common large padding/spacing
    val cornerMd      = RoundedCornerShape(12.dp) // Standard card/item corner
    val cornerLg      = RoundedCornerShape(16.dp) // Larger card corner (optional)
    val cornerPill    = RoundedCornerShape(24.dp) // Pill shape for buttons/inputs
    val indicatorSize = 8.dp // Smaller indicator like a dot
    val iconButtonSize = 40.dp
    val iconSizeSm = 20.dp
    val iconSizeMd = 24.dp // Standard icon size
}

// Define common typography inspired by HelpSupportScreen style
// RENAMED AS REQUESTED - NOTE: This name doesn't reflect content (TextStyles)
object AppDimen { // Formerly AppTypography
    // Adjust sizes and weights to match HelpSupportScreen's visual hierarchy
    val TitleLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold) // Like Screen Titles
    val TitleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold) // Like Section Headers
    val BodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold) // Like primary list text
    val BodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal) // Like secondary list text / descriptions
    val LabelMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium) // Like button text / tabs
    val LabelSmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal) // Like small captions
}

// --------- Model (Keep as is) ---------
data class MeditationItem(
    val title: String,
    val subtitle: String,
    val time: String,
    val progress: Float
)

// --------- Screen ---------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeditationScreen(
    navController: NavController,
) {

    val userId     = 1
    val apiService = RetrofitClient.apiService
    val repository = remember { MeditationRepository(apiService) }
    val factory    = remember { MeditationListViewModelFactory(repository, userId) }

    val viewModel: MeditationListViewModel = viewModel(
        factory = factory
    )

    val meditations by viewModel.meditations.observeAsState(emptyList())


    // Theme-aware colours from HelpSupportColors
    val isDark        = isSystemInDarkTheme()
    val pageBg        = if (isDark) HelpSupportColors.darkBg      else HelpSupportColors.lightBg
    val cardBg        = if (isDark) HelpSupportColors.darkSurface else HelpSupportColors.lightSurface
    val chipBg        = if (isDark) HelpSupportColors.darkChip    else HelpSupportColors.lightChip
    val textPrimary   = if (isDark) HelpSupportColors.darkText    else Color(0xFF1F2937) // Match HelpSupport
    val textSecondary = if (isDark) HelpSupportColors.darkTextLite else HelpSupportColors.lightText // Match HelpSupport
    val primaryColor  = HelpSupportColors.primaryPurple // Use the consistent primary color


    var selectedTab by remember { mutableStateOf("In Progress") }
    var selectedDay by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Completed", "In Progress")
    val days = remember { generateDays(14) } // Keep helper

    val items = meditations.map { dto ->
        MeditationItem(
            title    = dto.title,
            subtitle = dto.description,
            time     = dto.date.substringAfter('T').take(5),
            progress = if (dto.status == "completed") 1f else 0.5f
        )
    }


    val listToShow = when (selectedTab) {
        "Completed"   -> items.filter { it.progress >= 1f }
        "In Progress" -> items.filter { it.progress in 0.01f..0.99f }
        else          -> items
    }


    Scaffold(
        topBar = {
            // Use standard TopAppBar like HelpSupportScreen
            TopAppBar(
                title = {
                    Text("Meditation",
                        style = AppDimen.TitleLarge, // Use renamed object here
                        color = textPrimary
                    )
                },
                navigationIcon = {
                    // Use the same styled IconButton as HelpSupportScreen
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(start = AppDimens.paddingMd - AppDimens.paddingXs) // Adjust padding to align visually
                            .size(AppDimens.iconButtonSize)
                            .clip(CircleShape)
                            .background(chipBg) // Use chip background for consistency
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryColor
                        )
                    }
                },
                // Use transparent background and let Scaffold handle page color
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = pageBg // Apply page background to the Scaffold
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                // .background(pageBg) // Scaffold handles this now
                .padding(horizontal = AppDimens.paddingMd) // Consistent horizontal padding
        ){
            // Use consistent spacing
            Spacer(Modifier.height(AppDimens.paddingMd))
            DaySelector(
                days = days,
                selectedDayIndex = selectedDay,
                onDaySelected = { selectedDay = it },
                chipBackgroundColor = chipBg,
                selectedChipColor = primaryColor,
                textColor = textPrimary,
                selectedTextColor = Color.White // White text on primary color background
            )
            Spacer(Modifier.height(AppDimens.paddingLg)) // More space before tabs
            TabSelector(
                tabs = tabs,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                selectedColor = primaryColor,
                unselectedColor = textSecondary, // Use secondary text color for unselected tabs
                containerColor = pageBg // Tabs should blend with page background
            )
            Spacer(Modifier.height(AppDimens.paddingLg)) // Space before list

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(AppDimens.paddingMd),
                modifier = Modifier.weight(1f)
            ) {
                items(listToShow) { item ->
                    MeditationCard(
                        item               = item,
                        navController      = navController,
                        cardColor          = cardBg,
                        primaryTextColor   = textPrimary,
                        secondaryTextColor = textSecondary,
                        primaryAccentColor = primaryColor,
                        completedColor     = HelpSupportColors.successGreen,
                        progressColor      = HelpSupportColors.warningYellow
                    )
                }
            }
            Spacer(Modifier.height(AppDimens.paddingMd)) // Space before button
            // Main action button styled like HelpSupport buttons ("Next", "Start")
            Button(
                onClick = { navController.navigate("MeditationGenerateScreen") }, // Keep navigation target
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp), // Standard button height
                shape = AppDimens.cornerPill, // Use pill shape
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor, // Primary color background
                    contentColor = Color.White // White text
                )
            ) {
                Text(
                    "Create New Session",
                    style = AppDimen.LabelMedium.copy(fontWeight = FontWeight.SemiBold) // Use renamed object here
                )
            }
            Spacer(Modifier.height(AppDimens.paddingLg)) // Bottom padding
        }
    }
}

// --------- Components (Refactored for HelpSupport Style) ---------

@Composable
fun DaySelector(
    days: List<Pair<String, String>>,
    selectedDayIndex: Int,
    onDaySelected: (Int) -> Unit,
    chipBackgroundColor: Color,
    selectedChipColor: Color,
    textColor: Color,
    selectedTextColor: Color
) {
    Row(
        Modifier
            .horizontalScroll(rememberScrollState())
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSm) // Slightly less space between chips
    ) {
        days.forEachIndexed { index, day ->
            val isSelected = index == selectedDayIndex
            Box(
                modifier = Modifier
                    .clip(AppDimens.cornerMd) // Consistent rounding
                    .background(if (isSelected) selectedChipColor else chipBackgroundColor)
                    .clickable { onDaySelected(index) }
                    .padding(vertical = AppDimens.paddingSm, horizontal = AppDimens.paddingMd) // Adjust padding
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Use AppDimen styles (formerly AppTypography)
                    Text(
                        day.first, // e.g., "Mon"
                        style = AppDimen.LabelSmall, // Use renamed object here
                        color = if (isSelected) selectedTextColor else textColor.copy(alpha = 0.7f) // Dim unselected slightly
                    )
                    Spacer(Modifier.height(AppDimens.paddingXs))
                    Text(
                        day.second, // e.g., "14"
                        style = AppDimen.BodyMedium.copy(fontWeight = FontWeight.Bold), // Use renamed object here
                        color = if (isSelected) selectedTextColor else textColor
                    )
                }
            }
        }
    }
}

@Composable
fun TabSelector(
    tabs: List<String>,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    selectedColor: Color,
    unselectedColor: Color,
    containerColor: Color
) {
    val selectedIndex = tabs.indexOf(selectedTab)
    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = containerColor, // Blend with background
        contentColor = selectedColor, // Default content color (applies to indicator)
        indicator = { tabPositions ->
            if (selectedIndex < tabPositions.size) {
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    height = 3.dp, // Standard indicator height
                    color = selectedColor // Use primary color for indicator
                )
            }
        },
        divider = {} // Remove the default divider line if present
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selectedTab
            Tab(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        tab,
                        style = AppDimen.LabelMedium, // Use renamed object here
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium // Highlight selected
                    )
                },
                selectedContentColor = selectedColor, // Color for text when selected
                unselectedContentColor = unselectedColor // Color for text when not selected
            )
        }
    }
}

@Composable
fun MeditationCard(
    item: MeditationItem,
    navController: NavController, // Renamed nav to navController for clarity
    cardColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    primaryAccentColor: Color,
    completedColor: Color,
    progressColor: Color
) {
    // Determine status color based on HelpSupport colors
    val statusColor = when {
        item.progress >= 1f -> completedColor // Green for completed
        item.progress > 0f -> progressColor    // Yellow for in progress
        else -> secondaryTextColor.copy(alpha = 0.5f) // Dimmed secondary for not started
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("MeditationsAgentScreen") }, // Keep navigation target
        colors = CardDefaults.cardColors(containerColor = cardColor), // Use theme-aware card color
        shape = AppDimens.cornerMd // Consistent card rounding
    ) {
        Row(
            Modifier.padding(AppDimens.paddingMd), // Consistent padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Indicator (small dot)
            Box(
                Modifier
                    .size(AppDimens.indicatorSize) // Use small indicator size
                    .background(statusColor, shape = CircleShape)
            )
            Spacer(Modifier.width(AppDimens.paddingMd)) // Space after indicator

            // Title and Subtitle
            Column(Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = AppDimen.BodyLarge.copy(fontWeight = FontWeight.Medium), // Use renamed object here
                    color = primaryTextColor
                )
                Spacer(Modifier.height(AppDimens.paddingXs))
                Text(
                    item.subtitle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = AppDimen.BodyMedium, // Use renamed object here
                    color = secondaryTextColor
                )
            }
            Spacer(Modifier.width(AppDimens.paddingMd)) // Space before the right side column

            // Share Icon and Time
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Icon button styled like HelpSupport icon backgrounds
                Box(
                    Modifier
                        .size(AppDimens.iconButtonSize) // Standard size
                        .clip(CircleShape)
                        .background(primaryAccentColor.copy(alpha = 0.1f)) // Light primary tint background
                        .clickable { /* TODO: Implement Share action */ },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.playbutton),
                        contentDescription = "Share",
                        modifier = Modifier.size(AppDimens.iconSizeMd),
                        colorFilter = null
                    )
                }
            }
        }
    }
}


// --------- Helper (Keep as is) ---------
fun generateDays(count: Int): List<Pair<String, String>> {
    val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    // Simple modulo for weekday, add 1 to index for day number
    return List(count) { i -> weekdays[i % 7] to "${i + 1}" }
}

// Make sure HelpSupportColors is accessible here
/*
object HelpSupportColors {
    // ... colors defined here ...
}
*/