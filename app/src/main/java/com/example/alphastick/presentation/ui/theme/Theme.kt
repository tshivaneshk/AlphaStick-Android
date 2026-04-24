package com.example.alphastick.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

private val DarkColorScheme = darkColorScheme(
    background = Color(0xFF13131A),
    surface = Color(0xFF1E1E2C),
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2B2B3D),
    onSurfaceVariant = Color.LightGray,
    primary = Color(0xFF6C63FF)
)

private val LightColorScheme = lightColorScheme(
    background = Color(0xFFF3F4F6),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF13131A),
    onSurface = Color(0xFF1E1E2C),
    surfaceVariant = Color(0xFFE5E7EB),
    onSurfaceVariant = Color.DarkGray,
    primary = Color(0xFF6C63FF)
)

@Composable
fun AlphaStickTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
