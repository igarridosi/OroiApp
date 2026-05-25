package com.example.oroiapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.oroiapp.data.ThemeSetting

// ╔══════════════════════════════════════════════════════════════╗
// ║  DARK — Near-black with purple accent pops                  ║
// ╚══════════════════════════════════════════════════════════════╝
private val DarkColorScheme = darkColorScheme(
    // ── Primary ──
    primary            = PurpleAccent,       // Buttons, FABs, active accents
    onPrimary          = White,              // Text ON purple buttons
    primaryContainer   = PurpleMuted,        // Chip/container fill (dark purple)
    onPrimaryContainer = PurpleLight,        // Text on dark-purple containers

    // ── Secondary ──
    secondary            = PurpleGlow,
    onSecondary          = DarkBackground,
    secondaryContainer   = DarkSurfaceHigh,
    onSecondaryContainer = TextPrimaryDark,

    // ── Tertiary ──
    tertiary             = PurpleFuchsia,
    onTertiary           = DarkBackground,
    tertiaryContainer    = DarkSurfaceHigh,
    onTertiaryContainer  = PurpleFuchsia,

    // ── Background / Surface ──
    background       = DarkBackground,       // Deepest layer
    surface          = DarkSurface,          // Cards, sheets
    surfaceVariant   = DarkSurfaceHigh,      // Chips, secondary panels
    surfaceTint      = PurpleAccent,

    // ── On colors ──
    onBackground     = TextPrimaryDark,
    onSurface        = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,

    // ── Outline ──
    outline        = DarkOutline,
    outlineVariant = DarkOutlineVar,

    // ── Inverse ──
    inverseSurface   = TextPrimaryDark,
    inverseOnSurface = DarkBackground,
    inversePrimary   = PurpleBright,

    // ── Error ──
    error            = ErrorRed,
    onError          = White,
    errorContainer   = Color(0xFF3B1212),
    onErrorContainer = ErrorRed,

    scrim = Color(0x99000000)
)

// ╔══════════════════════════════════════════════════════════════╗
// ║  LIGHT — Clean whites with purple brand accents             ║
// ╚══════════════════════════════════════════════════════════════╝
private val LightColorScheme = lightColorScheme(
    // ── Primary ──
    primary            = PurpleBright,       // Brand purple
    onPrimary          = White,              // White text on purple
    primaryContainer   = Color(0xFFEDE7FF),  // Very light purple
    onPrimaryContainer = Color(0xFF21005D),  // Dark purple text

    // ── Secondary ──
    secondary            = PurpleFuchsia,
    onSecondary          = White,
    secondaryContainer   = Color(0xFFF3E8FF),
    onSecondaryContainer = Color(0xFF2B0052),

    // ── Tertiary ──
    tertiary             = Color(0xFF6750A4),
    onTertiary           = White,
    tertiaryContainer    = Color(0xFFE8DEF8),
    onTertiaryContainer  = Color(0xFF1D192B),

    // ── Background / Surface ──
    background       = LightBackground,
    surface          = LightSurface,
    surfaceVariant   = LightSurfaceVar,
    surfaceTint      = PurpleBright,

    // ── On colors ──
    onBackground     = TextPrimaryLight,
    onSurface        = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,

    // ── Outline ──
    outline        = LightOutline,
    outlineVariant = Color(0xFFE0DDE5),

    // ── Inverse ──
    inverseSurface   = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary   = PurpleLight,

    // ── Error ──
    error            = ErrorRedBright,
    onError          = White,
    errorContainer   = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    scrim = Color(0x66000000)
)


@Composable
fun OroiTheme(
    themeSetting: ThemeSetting = ThemeSetting.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeSetting) {
        ThemeSetting.LIGHT -> false
        ThemeSetting.DARK -> true
        ThemeSetting.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status bar matches the background — seamless
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
