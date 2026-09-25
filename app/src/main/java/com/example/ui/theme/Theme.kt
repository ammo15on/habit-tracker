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
  bgHex: String = "#0B0D13",
  textHex: String = "#FFFFFF",
  themeColor: AppThemeColor = AppThemeColor.SLATE,
  fontColor: AppFontColor = AppFontColor.DEFAULT,
  uiOpacity: Float = 0.25f,
  textSizeScale: Float = 1.0f,
  backgroundImageUri: String? = null,
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val primaryColor = parseHexColor(uiHex, if (darkTheme) themeColor.primaryDark else themeColor.primaryLight)
  val textColor = parseHexColor(textHex, Color.White)
  val onPrimaryColor = getContrastingTextColor(primaryColor)

  val neutralBackground = parseHexColor(bgHex, Color(0xFF0B0D13))
  val safeOpacity = uiOpacity.coerceIn(0.05f, 1.0f)
  
  // Clean translucent obsidian/slate glass or transparent when opacity is low
  // Eliminates whitish/blackish cloudy boxes around text!
  val cardBase = Color(0xFF131722)
  val surfaceColor = if (safeOpacity <= 0.15f) Color.Transparent else cardBase.copy(alpha = (safeOpacity * 0.45f).coerceIn(0.02f, 0.85f))
  val surfaceVariantColor = if (safeOpacity <= 0.15f) Color.Transparent else Color(0xFF1B2030).copy(alpha = (safeOpacity * 0.35f).coerceIn(0.02f, 0.75f))

  val colorScheme = darkColorScheme(
    primary = primaryColor,
    onPrimary = onPrimaryColor,
    primaryContainer = primaryColor.copy(alpha = (safeOpacity * 0.30f).coerceIn(0.12f, 0.65f)),
    onPrimaryContainer = if (getContrastingTextColor(primaryColor) == Color.White) primaryColor else Color.White,
    secondary = primaryColor.copy(alpha = 0.85f),
    onSecondary = onPrimaryColor,
    secondaryContainer = Color(0xFF1E2433).copy(alpha = (safeOpacity * 0.35f).coerceIn(0.10f, 0.65f)),
    onSecondaryContainer = textColor,
    background = neutralBackground,
    onBackground = textColor,
    surface = surfaceColor,
    onSurface = textColor,
    surfaceVariant = surfaceVariantColor,
    onSurfaceVariant = textColor.copy(alpha = 0.82f),
    surfaceTint = Color.Transparent, // Crucial: prevents M3 from adding automatic tinted elevation wash!
    outline = Color(0xFF64748B).copy(alpha = (safeOpacity * 0.45f).coerceIn(0.18f, 0.65f)),
    outlineVariant = Color(0xFF475569).copy(alpha = (safeOpacity * 0.35f).coerceIn(0.15f, 0.50f))
  )

  val currentDensity = androidx.compose.ui.platform.LocalDensity.current
  val scaledDensity = androidx.compose.ui.unit.Density(
    density = currentDensity.density,
    fontScale = currentDensity.fontScale * textSizeScale.coerceIn(0.80f, 1.35f)
  )

  androidx.compose.runtime.CompositionLocalProvider(
    androidx.compose.ui.platform.LocalDensity provides scaledDensity
  ) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography) {
      if (!backgroundImageUri.isNullOrBlank()) {
        Box(modifier = Modifier.fillMaxSize().background(neutralBackground)) {
          // Transparency effect directly controls opacity of background image!
          AsyncImage(
            model = backgroundImageUri,
            contentDescription = "App Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = safeOpacity.coerceIn(0.10f, 1.0f)
          )
          // Subtle dark scrim so text remains crystal clear at any wallpaper opacity
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = (1f - safeOpacity * 0.70f).coerceIn(0.15f, 0.85f)))
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
}
