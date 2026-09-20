package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,

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

@Composable
fun MyApplicationTheme(
  themeColor: AppThemeColor = AppThemeColor.EMERALD,
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) {
    darkColorScheme(
      primary = themeColor.primaryDark,
      onPrimary = Color.Black,
      primaryContainer = themeColor.containerDark,
      onPrimaryContainer = Color.White,
      secondary = themeColor.secondaryDark,
      background = if (themeColor == AppThemeColor.BLACK) Color(0xFF000000) else Color(0xFF121212),
      surface = if (themeColor == AppThemeColor.BLACK) Color(0xFF10141C) else Color(0xFF1E1E1E),
      onBackground = Color.White,
      onSurface = Color.White
    )
  } else {
    lightColorScheme(
      primary = themeColor.primaryLight,
      onPrimary = Color.White,
      primaryContainer = themeColor.containerLight,
      onPrimaryContainer = Color(0xFF0F172A),
      secondary = themeColor.secondaryLight,
      background = Color(0xFFF8FAFC),
      surface = Color(0xFFFFFFFF),
      onBackground = Color(0xFF0F172A),
      onSurface = Color(0xFF0F172A)
    )
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

