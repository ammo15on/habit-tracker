package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun MyApplicationTheme(
  uiHex: String = "#3B82F6",
  bgHex: String = "#121212",
  textHex: String = "#FFFFFF",
  themeColor: AppThemeColor = AppThemeColor.SLATE,
  fontColor: AppFontColor = AppFontColor.DEFAULT,
  backgroundImageUri: String? = null,
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val primaryColor = parseHexColor(uiHex, if (darkTheme) themeColor.primaryDark else themeColor.primaryLight)
  val backgroundColor = parseHexColor(bgHex, if (darkTheme) Color(0xFF121212) else Color(0xFFF8FAFC))
  val textColor = parseHexColor(textHex, if (darkTheme) Color.White else Color(0xFF0F172A))

  val colorScheme = darkColorScheme(
    primary = primaryColor,
    onPrimary = Color.White,
    primaryContainer = primaryColor.copy(alpha = 0.25f),
    onPrimaryContainer = primaryColor,
    secondary = primaryColor.copy(alpha = 0.85f),
    background = backgroundColor,
    surface = backgroundColor,
    surfaceVariant = backgroundColor.copy(alpha = 0.5f),
    onBackground = textColor,
    onSurface = textColor,
    onSurfaceVariant = textColor.copy(alpha = 0.75f)
  )

  MaterialTheme(colorScheme = colorScheme, typography = Typography) {
    if (!backgroundImageUri.isNullOrBlank()) {
      Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
          model = backgroundImageUri,
          contentDescription = "App Background",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )
        // Semi-transparent scrim to ensure UI readability and transparent effect
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              backgroundColor.copy(alpha = 0.72f)
            )
        )
        content()
      }
    } else {
      content()
    }
  }
}
