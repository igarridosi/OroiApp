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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.oroiapp.data.ThemeSetting

// Kolore paleta ilunerako
private val DarkColorScheme = darkColorScheme(
    primary = MoreaArgia,             // Botoi nagusiak (kolore argia kontrastea egiteko)
    onPrimary = MoreaArgia,           // Botoi nagusien gaineko testua (kolore iluna)
    primaryContainer = MoreaDistiratsua,    // Bigarren mailako elementuak

    background = MoreaIluna,          // Pantailaren fondo orokorra
    surface = MoreaIluna,             // Txartelen eta gainazalen fondoa

    onBackground = Zuria,             // Fondoaren gaineko testu eta ikonoak
    onSurface = Zuria,                // Txartelen gaineko testu eta ikonoak

    error = GorriErrorea,
    onError = Zuria,

    onTertiary = MoreaIluna,
    onTertiaryContainer = MoreaArroxa,
    onSecondary = Grisa,

    onTertiaryFixed = MoreaArgia,
    onSecondaryFixed = MoreaArgia
)

// --- MODU ARGIRAKO PALETA ---
private val LightColorScheme = lightColorScheme(
    primary = MoreaDistiratsua,       // Botoi nagusiak
    onPrimary = MoreaIluna,                // Botoi nagusien gaineko testua
    primaryContainer = MoreaArgia,          // Bigarren mailako elementuak

    background = ZuriHautsa,          // Pantailaren fondo orokorra
    surface = Zuria,                  // Txartelen eta gainazalen fondoa

    onBackground = Beltza,            // Fondoaren gaineko testu eta ikonoak
    onSurface = Beltza,               // Txartelen gaineko testu eta ikonoak

    error = GorriErrorea,
    onError = Zuria,

    onTertiary = Grisa,
    onTertiaryContainer = MoreaIluna,
    onSecondary = MoreaDistiratsua,

    onTertiaryFixed = MoreaIluna,
    onSecondaryFixed = MoreaArgia
)


@Composable
fun OroiTheme(
    themeSetting: ThemeSetting = ThemeSetting.SYSTEM,
    dynamicColor: Boolean = false, // Zure app-ak nortasun handia du, hobe kolore dinamikoak desgaitzea
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}