package com.example.neuroed.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.graphics.Brush


private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

/**
 * Design tokens for colors, typography, shapes, spacing, and gradients
 * matching the Help & Support UI in both dark and light modes.
 */
object Colors {
    /* Brand Colors */
    val primaryPurple   = Color(0xFF6A5ACD)
    val secondaryPurple = Color(0xFF9370DB)

    /* LIGHT Palette */
    val lightBg         = Color(0xFFF9FAFB)  // page background
    val lightSurface    = Color(0xFFFFFFFF)  // cards, containers
    val lightChip       = Color(0xFFE6E6FA)  // chip and icon backgrounds
    val lightText       = Color(0xFF1F2937)  // primary text
    val lightTextSubtle = Color(0xFF6B7280)  // secondary text

    /* DARK Palette */
    val darkBg          = Color(0xFF121212)  // page background
    val darkSurface     = Color(0xFF1E1E1E)  // cards, containers
    val darkChip        = Color(0xFF242424)  // chip and icon backgrounds
    val darkText        = Color.White         // primary text
    val darkTextSubtle  = Color(0xFFB3B3B3)  // secondary text

    /* Misc */
    val divider         = Color(0xFFE5E7EB)
    val successGreen    = Color(0xFF34D399)
    val infoBlue        = Color(0xFF3B82F6)
    val warningYellow   = Color(0xFFFBBF24)
}

/** Light and dark color schemes for Material3 */
val LightColors = lightColorScheme(
    primary        = Colors.primaryPurple,
    onPrimary      = Color.White,
    background     = Colors.lightBg,
    surface        = Colors.lightSurface,
    onSurface      = Colors.lightText,
    surfaceVariant = Colors.lightChip,
    onSurfaceVariant = Colors.lightTextSubtle,
    outline        = Colors.divider
)

val DarkColors = darkColorScheme(
    primary        = Colors.primaryPurple,
    onPrimary      = Color.White,
    background     = Colors.darkBg,
    surface        = Colors.darkSurface,
    onSurface      = Colors.darkText,
    surfaceVariant = Colors.darkChip,
    onSurfaceVariant = Colors.darkTextSubtle,
    outline        = Colors.divider
)

/**
 * Typography rules matching the design:
 * - Page titles: 24.sp, Bold
 * - Section headers: 18.sp, Bold
 * - Card titles: 16.sp, SemiBold
 * - Body text: 14.sp, Regular
 * - Supporting text: 12.sp, Regular
 */
val AppTypography = Typography(
    titleLarge  = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),     // Page titles
    titleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),     // Section headers
    titleSmall  = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold), // Card titles
    bodyMedium  = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),   // Body text
    labelSmall  = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal)    // Supporting text
)

/**
 * Shape rules:
 * - Large: 24.dp
 * - Medium: 16.dp
 * - Small: 12.dp
 * - Icon containers: CircleShape
 */
val AppShapes = Shapes(
    small  = RoundedCornerShape(12.dp), // Chips, quick help cards
    medium = RoundedCornerShape(16.dp), // Support option cards
    large  = RoundedCornerShape(24.dp)  // Search fields, floating buttons
)

/**
 * Standard spacing values:
 * - small: 8.dp between internal elements
 * - medium: 16.dp between list items or sections
 * - large: 24.dp padding for scrollable content
 */
object AppSpacing {
    val small  = 8.dp
    val medium = 16.dp
    val large  = 24.dp
}

/**
 * Gradient definitions for header backgrounds:
 */
object AppGradients {
    val header = Brush.verticalGradient(
        colors = listOf(Colors.primaryPurple, Colors.secondaryPurple)
    )
}


//Shape & Border Radius
//
//Large components (Search fields, floating buttons): 24.dp rounded corners
//Medium components (Cards, main containers): 16.dp rounded corners
//Small components (Chips, support options, list items): 12.dp rounded corners
//Icon containers: CircleShape
//
//Typography
//
//Page titles: 24.sp, FontWeight.Bold
//Section headers: 18.sp, FontWeight.Bold
//Card titles: 16.sp, FontWeight.Bold/SemiBold
//Body text: 14.sp, Regular weight
//Supporting text: 12.sp, Regular weight
//
//Spacing & Layout
//
//Standard padding around containers: 16.dp
//Section spacing: 16.dp between major sections
//Item spacing: 12.dp between list items
//Internal spacing: 8.dp between related elements (like icon + text)
//Content padding: PaddingValues(bottom = 24.dp) for scrollable content
//
//Animation Standards
//
//Use MutableTransitionState for entry animations
//Staggered timing (base + 100ms increments)
//fadeIn(tween(duration)) + slideInVertically(tween(duration)) { -40 }
//AnimatedVisibility for expandable content






object AppColors {

    object Light {
        val primary           = Color(0xFF6A5ACD)
        val primaryLight      = Color(0xFF9370DB)
        val background        = Color(0xFFF9FAFB)
        val surface           = Color(0xFFFFFFFF)
        val surfaceHighlight  = Color(0xFFE6E6FA)
        val textDark          = Color(0xFF1F2937)
        val textLight         = Color(0xFF6B7280)
        val divider           = Color(0xFFE5E7EB)
        val success           = Color(0xFF34D399)
        val info              = Color(0xFF3B82F6)
        val warning           = Color(0xFFFBBF24)
    }

    object Dark {
        val primary           = Color(0xFF9370DB)
        val primaryLight      = Color(0xFF6A5ACD)
        val background        = Color(0xFF121212)
        val surface           = Color(0xFF1E1E1E)
        val surfaceHighlight  = Color(0xFF242424)
        val textDark          = Color(0xFFFFFFFF)
        val textLight         = Color(0xFFB3B3B3)
        val divider           = Color(0xFF2E2E2E)
        val success           = Color(0xFF34D399)
        val info              = Color(0xFF3B82F6)
        val warning           = Color(0xFFFBBF24)
    }
}


@Composable
fun NeuroEDTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography =  AppTypography,
        content = content
    )
}