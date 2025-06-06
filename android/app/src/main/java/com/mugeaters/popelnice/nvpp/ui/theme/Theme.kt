package com.mugeaters.popelnice.nvpp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Custom color scheme for the app theme
data class AppColors(
    val screenBackground: Color,
    val sectionBackground: Color,
    val regularText: Color,
    val grayText: Color,
    val buttonDarkBackground: Color,
    val buttonLightBackground: Color
)

private val LightAppColors = AppColors(
    screenBackground = ScreenBackgroundLight,
    sectionBackground = SectionBackgroundLight,
    regularText = RegularTextLight,
    grayText = GrayTextLight,
    buttonDarkBackground = ButtonDarkBackgroundLight,
    buttonLightBackground = ButtonLightBackgroundLight
)

private val DarkAppColors = AppColors(
    screenBackground = ScreenBackgroundDark,
    sectionBackground = SectionBackgroundDark,
    regularText = RegularTextDark,
    grayText = GrayTextDark,
    buttonDarkBackground = ButtonDarkBackgroundDark,
    buttonLightBackground = ButtonLightBackgroundDark
)

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    secondary = Yellow40,
    tertiary = Brown40,
    background = ScreenBackgroundLight,
    surface = SectionBackgroundLight,
    onBackground = RegularTextLight,
    onSurface = RegularTextLight
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    secondary = Yellow80,
    tertiary = Brown80,
    background = ScreenBackgroundDark,
    surface = SectionBackgroundDark,
    onBackground = RegularTextDark,
    onSurface = RegularTextDark
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

@Composable
fun NovaVesOdpadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled to use our custom colors
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
    
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = appColors.screenBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}