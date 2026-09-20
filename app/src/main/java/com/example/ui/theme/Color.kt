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

// App Themes requested: Yellow, Golden, Black, Grey, Emerald, Blue, Purple
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
  YELLOW(
    displayName = "Yellow",
    primaryLight = Color(0xFFCA8A04),
    secondaryLight = Color(0xFFA16207),
    containerLight = Color(0xFFFEF08A),
    primaryDark = Color(0xFFFACC15),
    secondaryDark = Color(0xFFEAB308),
    containerDark = Color(0xFF713F12),
    previewHex = Color(0xFFEAB308)
  ),
  GOLDEN(
    displayName = "Golden",
    primaryLight = Color(0xFFB45309),
    secondaryLight = Color(0xFF92400E),
    containerLight = Color(0xFFFDE68A),
    primaryDark = Color(0xFFFBBF24),
    secondaryDark = Color(0xFFF59E0B),
    containerDark = Color(0xFF78350F),
    previewHex = Color(0xFFD97706)
  ),
  BLACK(
    displayName = "Black / Slate",
    primaryLight = Color(0xFF0F172A),
    secondaryLight = Color(0xFF334155),
    containerLight = Color(0xFFE2E8F0),
    primaryDark = Color(0xFFF8FAFC),
    secondaryDark = Color(0xFF94A3B8),
    containerDark = Color(0xFF1E293B),
    previewHex = Color(0xFF0F172A)
  ),
  GREY(
    displayName = "Grey",
    primaryLight = Color(0xFF475569),
    secondaryLight = Color(0xFF64748B),
    containerLight = Color(0xFFF1F5F9),
    primaryDark = Color(0xFFCBD5E1),
    secondaryDark = Color(0xFF94A3B8),
    containerDark = Color(0xFF334155),
    previewHex = Color(0xFF64748B)
  ),
  EMERALD(
    displayName = "Emerald",
    primaryLight = Color(0xFF16A34A),
    secondaryLight = Color(0xFF15803D),
    containerLight = Color(0xFFDCFCE7),
    primaryDark = Color(0xFF4ADE80),
    secondaryDark = Color(0xFF22C55E),
    containerDark = Color(0xFF14532D),
    previewHex = Color(0xFF16A34A)
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
  PURPLE(
    displayName = "Purple",
    primaryLight = Color(0xFF7C3AED),
    secondaryLight = Color(0xFF6D28D9),
    containerLight = Color(0xFFEDE9FE),
    primaryDark = Color(0xFFA78BFA),
    secondaryDark = Color(0xFF8B5CF6),
    containerDark = Color(0xFF4C1D95),
    previewHex = Color(0xFF7C3AED)
  )
}


