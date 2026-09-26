package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
  bodyLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
  ),
  titleLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp
  ),
  labelSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
  )
)

fun scaleTypography(base: Typography, scale: Float): Typography {
  if (scale == 1.0f) return base
  fun scaleStyle(style: TextStyle): TextStyle {
    return if (style.fontSize.isSp) {
      style.copy(fontSize = (style.fontSize.value * scale).sp)
    } else {
      style
    }
  }

  return Typography(
    displayLarge = scaleStyle(base.displayLarge),
    displayMedium = scaleStyle(base.displayMedium),
    displaySmall = scaleStyle(base.displaySmall),
    headlineLarge = scaleStyle(base.headlineLarge),
    headlineMedium = scaleStyle(base.headlineMedium),
    headlineSmall = scaleStyle(base.headlineSmall),
    titleLarge = scaleStyle(base.titleLarge),
    titleMedium = scaleStyle(base.titleMedium),
    titleSmall = scaleStyle(base.titleSmall),
    bodyLarge = scaleStyle(base.bodyLarge),
    bodyMedium = scaleStyle(base.bodyMedium),
    bodySmall = scaleStyle(base.bodySmall),
    labelLarge = scaleStyle(base.labelLarge),
    labelMedium = scaleStyle(base.labelMedium),
    labelSmall = scaleStyle(base.labelSmall)
  )
}
