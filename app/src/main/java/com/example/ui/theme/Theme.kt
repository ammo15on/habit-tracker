package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun MyApplicationTheme(
  themeColor: AppThemeColor = AppThemeColor.EMERALD,
  fontColor: AppFontColor = AppFontColor.DEFAULT,
  backgroundImageUri: String? = null,
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val customTextColor = when {
    fontColor == AppFontColor.DEFAULT -> null
    darkTheme -> fontColor.darkColor
    else -> fontColor.lightColor
  }

  val baseColorScheme = if (darkTheme) {
    darkColorScheme(
      primary = themeColor.primaryDark,
      onPrimary = Color.Black,
      primaryContainer = themeColor.containerDark,
      onPrimaryContainer = Color.White,
      secondary = themeColor.secondaryDark,
      background = if (themeColor == AppThemeColor.BLACK) Color(0xFF000000) else Color(0xFF121212),
      surface = if (themeColor == AppThemeColor.BLACK) Color(0xFF10141C) else Color(0xFF1E1E1E),
      onBackground = customTextColor ?: Color.White,
      onSurface = customTextColor ?: Color.White,
      onSurfaceVariant = customTextColor?.copy(alpha = 0.8f) ?: Color(0xFFCBD5E1)
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
      onBackground = customTextColor ?: Color(0xFF0F172A),
      onSurface = customTextColor ?: Color(0xFF0F172A),
      onSurfaceVariant = customTextColor?.copy(alpha = 0.8f) ?: Color(0xFF475569)
    )
  }

  MaterialTheme(colorScheme = baseColorScheme, typography = Typography) {
    if (!backgroundImageUri.isNullOrBlank()) {
      Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
          model = backgroundImageUri,
          contentDescription = "App Background",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )
        // Semi-transparent scrim to ensure UI readability and contrast
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              if (darkTheme) Color.Black.copy(alpha = 0.65f)
              else Color.White.copy(alpha = 0.82f)
            )
        )
        content()
      }
    } else {
      content()
    }
  }
}


