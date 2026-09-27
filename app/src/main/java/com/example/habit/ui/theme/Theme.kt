package com.example.habit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    secondary = Color(0xFF10B981),
    background = Color(0xFF090B10),
    surface = Color(0xFF131722),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC)
)

@Composable
fun MyApplicationTheme(
    uiHex: String = "#3B82F6",
    textHex: String = "#F8FAFC",
    textSizeScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val primaryColor = try {
        Color(android.graphics.Color.parseColor(uiHex))
    } catch (_: Exception) {
        Color(0xFF3B82F6)
    }

    val textColor = try {
        Color(android.graphics.Color.parseColor(textHex))
    } catch (_: Exception) {
        Color(0xFFF8FAFC)
    }

    val customColorScheme = DarkColorScheme.copy(
        primary = primaryColor,
        onBackground = textColor,
        onSurface = textColor
    )

    MaterialTheme(
        colorScheme = customColorScheme,
        content = content
    )
}
