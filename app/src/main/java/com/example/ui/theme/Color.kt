package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Habit Rating Colors specified by design
val RatingBestGreen = Color(0xFF22C55E) // Vibrant emerald green for Best
val RatingBestGreenDark = Color(0xFF15803D)
val RatingAverageGrey = Color(0xFFCBD5E1) // Light grey for Average
val RatingAverageGreyDark = Color(0xFF64748B)
val RatingWorstBlack = Color(0xFF1E293B) // Dark slate / black for Worst
val RatingWorstBlackPure = Color(0xFF0F172A)

// App Themes & UI Elements Color options
enum class AppThemeColor(
  val displayName: String,
  val primaryLight: Color,
  val secondaryLight: Color,
  val containerLight: Color,
  val primaryDark: Color,
  val secondaryDark: Color,
  val containerDark: Color,
  val previewHex: Color
) {
  SLATE(
    displayName = "Slate / Neutral",
    primaryLight = Color(0xFF334155),
    secondaryLight = Color(0xFF475569),
    containerLight = Color(0xFFE2E8F0),
    primaryDark = Color(0xFF94A3B8),
    secondaryDark = Color(0xFF64748B),
    containerDark = Color(0xFF1E293B),
    previewHex = Color(0xFF475569)
  ),
  INDIGO(
    displayName = "Indigo",
    primaryLight = Color(0xFF4F46E5),
    secondaryLight = Color(0xFF4338CA),
    containerLight = Color(0xFFE0E7FF),
    primaryDark = Color(0xFF818CF8),
    secondaryDark = Color(0xFF6366F1),
    containerDark = Color(0xFF312E81),
    previewHex = Color(0xFF4F46E5)
  ),
  BLUE(
    displayName = "Ocean Blue",
    primaryLight = Color(0xFF2563EB),
    secondaryLight = Color(0xFF1D4ED8),
    containerLight = Color(0xFFDBEAFE),
    primaryDark = Color(0xFF60A5FA),
    secondaryDark = Color(0xFF3B82F6),
    containerDark = Color(0xFF1E3A8A),
    previewHex = Color(0xFF2563EB)
  ),
  CYAN(
    displayName = "Cyan / Teal",
    primaryLight = Color(0xFF0891B2),
    secondaryLight = Color(0xFF0E7490),
    containerLight = Color(0xFFCFFAFE),
    primaryDark = Color(0xFF22D3EE),
    secondaryDark = Color(0xFF06B6D4),
    containerDark = Color(0xFF164E63),
    previewHex = Color(0xFF06B6D4)
  ),
  PURPLE(
    displayName = "Purple",
    primaryLight = Color(0xFF7C3AED),
    secondaryLight = Color(0xFF6D28D9),
    containerLight = Color(0xFFEDE9FE),
    primaryDark = Color(0xFFA78BFA),
    secondaryDark = Color(0xFF8B5CF6),
    containerDark = Color(0xFF4C1D95),
    previewHex = Color(0xFF7C3AED)
  ),
  ROSE(
    displayName = "Rose Pink",
    primaryLight = Color(0xFFE11D48),
    secondaryLight = Color(0xFFBE123C),
    containerLight = Color(0xFFFFE4E6),
    primaryDark = Color(0xFFFB7185),
    secondaryDark = Color(0xFFF43F5E),
    containerDark = Color(0xFF881337),
    previewHex = Color(0xFFF43F5E)
  ),
  CRIMSON(
    displayName = "Crimson Red",
    primaryLight = Color(0xFFDC2626),
    secondaryLight = Color(0xFFB91C1C),
    containerLight = Color(0xFFFEE2E2),
    primaryDark = Color(0xFFF87171),
    secondaryDark = Color(0xFFEF4444),
    containerDark = Color(0xFF7F1D1D),
    previewHex = Color(0xFFDC2626)
  ),
  AMBER(
    displayName = "Amber Orange",
    primaryLight = Color(0xFFD97706),
    secondaryLight = Color(0xFFB45309),
    containerLight = Color(0xFFFEF3C7),
    primaryDark = Color(0xFFFBBF24),
    secondaryDark = Color(0xFFF59E0B),
    containerDark = Color(0xFF78350F),
    previewHex = Color(0xFFF59E0B)
  ),
  GOLDEN(
    displayName = "Golden",
    primaryLight = Color(0xFFCA8A04),
    secondaryLight = Color(0xFFA16207),
    containerLight = Color(0xFFFEF08A),
    primaryDark = Color(0xFFFACC15),
    secondaryDark = Color(0xFFEAB308),
    containerDark = Color(0xFF713F12),
    previewHex = Color(0xFFEAB308)
  ),
  BLACK(
    displayName = "Obsidian / Black",
    primaryLight = Color(0xFF0F172A),
    secondaryLight = Color(0xFF334155),
    containerLight = Color(0xFFE2E8F0),
    primaryDark = Color(0xFFF8FAFC),
    secondaryDark = Color(0xFF94A3B8),
    containerDark = Color(0xFF1E293B),
    previewHex = Color(0xFF0F172A)
  ),
  GREY(
    displayName = "Graphite Grey",
    primaryLight = Color(0xFF475569),
    secondaryLight = Color(0xFF64748B),
    containerLight = Color(0xFFF1F5F9),
    primaryDark = Color(0xFFCBD5E1),
    secondaryDark = Color(0xFF94A3B8),
    containerDark = Color(0xFF334155),
    previewHex = Color(0xFF64748B)
  ),
  EMERALD(
    displayName = "Emerald Green",
    primaryLight = Color(0xFF16A34A),
    secondaryLight = Color(0xFF15803D),
    containerLight = Color(0xFFDCFCE7),
    primaryDark = Color(0xFF4ADE80),
    secondaryDark = Color(0xFF22C55E),
    containerDark = Color(0xFF14532D),
    previewHex = Color(0xFF16A34A)
  )
}

// User-customizable font color options
enum class AppFontColor(
  val displayName: String,
  val lightColor: Color,
  val darkColor: Color,
  val previewColor: Color
) {
  DEFAULT(
    displayName = "Auto (Theme Default)",
    lightColor = Color(0xFF0F172A),
    darkColor = Color(0xFFF8FAFC),
    previewColor = Color(0xFF64748B)
  ),
  PURE_WHITE(
    displayName = "Pure White",
    lightColor = Color(0xFFFFFFFF),
    darkColor = Color(0xFFFFFFFF),
    previewColor = Color(0xFFFFFFFF)
  ),
  PURE_BLACK(
    displayName = "Pure Black",
    lightColor = Color(0xFF000000),
    darkColor = Color(0xFF0F172A),
    previewColor = Color(0xFF000000)
  ),
  AMBER_GOLD(
    displayName = "Amber Gold",
    lightColor = Color(0xFFD97706),
    darkColor = Color(0xFFFBBF24),
    previewColor = Color(0xFFF59E0B)
  ),
  SLATE_BLUE(
    displayName = "Sky Blue",
    lightColor = Color(0xFF0284C7),
    darkColor = Color(0xFF38BDF8),
    previewColor = Color(0xFF38BDF8)
  ),
  EMERALD_GREEN(
    displayName = "Emerald Green",
    lightColor = Color(0xFF059669),
    darkColor = Color(0xFF34D399),
    previewColor = Color(0xFF10B981)
  ),
  CRIMSON_ROSE(
    displayName = "Crimson Rose",
    lightColor = Color(0xFFE11D48),
    darkColor = Color(0xFFFB7185),
    previewColor = Color(0xFFF43F5E)
  )
}

fun parseHexColor(hexString: String?, defaultColor: Color): Color {
  if (hexString.isNullOrBlank()) return defaultColor
  return try {
    var hex = hexString.trim().removePrefix("#")
    if (hex.length == 6) {
      hex = "FF$hex"
    }
    if (hex.length == 8) {
      Color(hex.toLong(16))
    } else {
      defaultColor
    }
  } catch (_: Exception) {
    defaultColor
  }
}

fun Color.toHexRgb(): String {
  val red = (this.red * 255).toInt().coerceIn(0, 255)
  val green = (this.green * 255).toInt().coerceIn(0, 255)
  val blue = (this.blue * 255).toInt().coerceIn(0, 255)
  return String.format("#%02X%02X%02X", red, green, blue)
}


