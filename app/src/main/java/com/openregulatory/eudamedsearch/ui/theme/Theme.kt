package com.openregulatory.eudamedsearch.ui.theme

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

val EuBlue = Color(0xFF003399)
val EuBlueDark = Color(0xFF001F5C)
val EuYellow = Color(0xFFFFCC00)

private val LightColors = lightColorScheme(
    primary = EuBlue,
    onPrimary = Color.White,
    secondary = EuYellow,
    onSecondary = Color.Black,
    background = Color(0xFFF7F8FC),
    surface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6E8FE0),
    onPrimary = Color.Black,
    secondary = EuYellow,
    onSecondary = Color.Black,
    background = Color(0xFF101319),
    surface = Color(0xFF191D24)
)

@Composable
fun EudamedSearchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
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
