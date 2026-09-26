package com.example.ui.theme

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
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
  primary = Color(0xFF3B82F6),
  secondary = PurpleGrey80,
  tertiary = Pink80,
  background = Color(0xFF090B10),
  surface = Color(0xFF131722),
  onPrimary = Color.White,
  onSecondary = Color.White,
  onTertiary = Color.White,
  onBackground = Color(0xFFF8FAFC),
  onSurface = Color(0xFFF8FAFC),
  outline = Color(0xFF2E384D),
  outlineVariant = Color(0xFF1E2536)
)

private val LightColorScheme = lightColorScheme(
  primary = Color(0xFF2563EB),
  secondary = PurpleGrey40,
  tertiary = Pink40,
  background = Color(0xFF090B10),
  surface = Color(0xFF131722),
  onPrimary = Color.White,
  onSecondary = Color.White,
  onTertiary = Color.White,
  onBackground = Color(0xFFF8FAFC),
  onSurface = Color(0xFFF8FAFC),
  outline = Color(0xFF2E384D),
  outlineVariant = Color(0xFF1E2536)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  uiHex: String = "#3B82F6",
  bgHex: String = "#090B10",
  textHex: String = "#F8FAFC",
  themeColor: Any? = null,
  fontColor: Any? = null,
  uiOpacity: Float = 0.85f,
  textSizeScale: Float = 1.0f,
  backgroundImageUri: String? = null,
  content: @Composable () -> Unit
) {
  val primaryColor = parseHexColor(uiHex, fallback = Color(0xFF3B82F6))
  val backgroundColor = parseHexColor(bgHex, fallback = Color(0xFF090B10))
  val fontColorActual = parseHexColor(textHex, fallback = Color(0xFFF8FAFC))

  val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val colorScheme = baseScheme.copy(
    primary = primaryColor,
    background = backgroundColor,
    surface = backgroundColor.copy(alpha = 0.95f),
    onBackground = fontColorActual,
    onSurface = fontColorActual,
    outline = primaryColor.copy(alpha = 0.4f),
    outlineVariant = primaryColor.copy(alpha = 0.2f)
  )

  val scaledTypography = scaleTypography(Typography, textSizeScale)

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = Color(0xFF090B10).toArgb()
        window.navigationBarColor = Color(0xFF090B10).toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = scaledTypography,
    content = content
  )
}
