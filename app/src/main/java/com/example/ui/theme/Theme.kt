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

/**
 * Calculates high-contrast text color (black or white) based on background luminance.
 */
fun getContrastingTextColor(backgroundColor: Color): Color {
  val luminance = 0.299f * backgroundColor.red + 0.587f * backgroundColor.green + 0.114f * backgroundColor.blue
  return if (luminance > 0.55f) Color(0xFF0F172A) else Color.White
}

@Composable
fun MyApplicationTheme(
  uiHex: String = "#3B82F6",
  textHex: String = "#FFFFFF",
  themeColor: AppThemeColor = AppThemeColor.SLATE,
  fontColor: AppFontColor = AppFontColor.DEFAULT,
  backgroundImageUri: String? = null,
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val primaryColor = parseHexColor(uiHex, if (darkTheme) themeColor.primaryDark else themeColor.primaryLight)
  val textColor = parseHexColor(textHex, Color.White)
  val onPrimaryColor = getContrastingTextColor(primaryColor)

  // Pure transparent / deep dark foundation for glassmorphism
  val neutralBackground = Color(0xFF0B0D13)
  val transparentSurface = Color(0x18FFFFFF) // 10% translucent white for cards
  val transparentSurfaceVariant = Color(0x10FFFFFF)

  val colorScheme = darkColorScheme(
    primary = primaryColor,
    onPrimary = onPrimaryColor,
    primaryContainer = primaryColor.copy(alpha = 0.22f),
    onPrimaryContainer = if (getContrastingTextColor(primaryColor) == Color.White) primaryColor else Color.White,
    secondary = primaryColor.copy(alpha = 0.85f),
    onSecondary = onPrimaryColor,
    secondaryContainer = Color(0x20FFFFFF),
    onSecondaryContainer = textColor,
    background = neutralBackground,
    onBackground = textColor,
    surface = transparentSurface,
    onSurface = textColor,
    surfaceVariant = transparentSurfaceVariant,
    onSurfaceVariant = textColor.copy(alpha = 0.78f),
    outline = Color(0x35FFFFFF),
    outlineVariant = Color(0x20FFFFFF)
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
        // Neutral subtle dark gradient scrim for text readability WITHOUT tinting with UI colors
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.25f))
        )
        content()
      }
    } else {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(neutralBackground)
      ) {
        content()
      }
    }
  }
}
