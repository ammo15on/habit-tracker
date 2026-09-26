package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Default Deep Glass Colors
val DarkBackground = Color(0xFF090B10)
val DarkSurface = Color(0xFF131722)
val DarkSurfaceVariant = Color(0xFF1E2333)

// Day Rating & Indicator Colors
val RatingBestGreen = Color(0xFF22C55E)
val RatingAverageGrey = Color(0xFF94A3B8)
val RatingWorstBlack = Color(0xFFEF4444)

fun parseHexColor(hex: String, fallback: Color = Color(0xFF3B82F6)): Color {
  return HexColorPalette.parseColor(hex, fallback)
}

enum class AppThemeColor(val primaryHex: String) {
  SLATE("#64748B"),
  INDIGO("#6366F1"),
  BLUE("#3B82F6"),
  CYAN("#06B6D4"),
  PURPLE("#8B5CF6"),
  ROSE("#FB7185"),
  CRIMSON("#EF4444"),
  AMBER("#F59E0B"),
  GOLDEN("#EAB308"),
  OBSIDIAN("#1E293B"),
  GRAPHITE("#334155"),
  EMERALD("#10B981")
}

enum class AppFontColor(val hex: String) {
  DEFAULT("#F8FAFC"),
  WHITE("#FFFFFF"),
  SOFT_GREY("#E2E8F0"),
  PASTEL_YELLOW("#FEF08A"),
  MINT_GREEN("#BBF7D0"),
  ICE_BLUE("#BAE6FD"),
  ROSE("#FBCFE8")
}

// Preset Hex Color Palettes
object HexColorPalette {
  val PRESET_COLORS = listOf(
    "#3B82F6", // Royal Blue (Default)
    "#10B981", // Emerald Green
    "#8B5CF6", // Purple / Violet
    "#EC4899", // Magenta Pink
    "#F59E0B", // Amber Gold
    "#06B6D4", // Cyan Aqua
    "#EF4444", // Crimson Red
    "#6366F1", // Indigo
    "#14B8A6", // Teal
    "#F97316"  // Coral Orange
  )

  val PRESET_FONT_COLORS = listOf(
    "#F8FAFC", // Pure Light Slate
    "#E2E8F0", // Soft Grey White
    "#FEF08A", // Soft Yellow Pastel
    "#BBF7D0", // Soft Mint Green
    "#BAE6FD", // Soft Ice Blue
    "#FBCFE8"  // Soft Rose
  )

  val rows = listOf(
    // Neutrals, Dark & Light Monochrome
    listOf("#000000", "#121212", "#1E293B", "#334155", "#475569", "#64748B", "#94A3B8", "#CBD5E1", "#E2E8F0", "#FFFFFF"),
    // Crimson & Red
    listOf("#450A0A", "#7F1D1D", "#991B1B", "#DC2626", "#EF4444", "#F87171", "#FCA5A5", "#FECACA", "#FEE2E2", "#FFF1F2"),
    // Orange & Amber
    listOf("#451A03", "#78350F", "#92400E", "#D97706", "#F59E0B", "#FBBF24", "#FCD34D", "#FDE68A", "#FEF3C7", "#FFFBEB"),
    // Emerald & Green
    listOf("#022C22", "#064E3B", "#047857", "#059669", "#10B981", "#34D399", "#6EE7B7", "#A7F3D0", "#D1FAE5", "#ECFDF5"),
    // Teal & Cyan
    listOf("#083344", "#164E63", "#0E7490", "#0891B2", "#06B6D4", "#22D3EE", "#67E8F9", "#A5F3FC", "#CFFAFE", "#ECFEFF"),
    // Ocean & Sky Blue
    listOf("#172554", "#1E3A8A", "#1D4ED8", "#2563EB", "#3B82F6", "#60A5FA", "#93C5FD", "#BFDBFE", "#DBEAFE", "#EFF6FF"),
    // Indigo
    listOf("#1E1B4B", "#312E81", "#3730A3", "#4338CA", "#4F46E5", "#6366F1", "#818CF8", "#A5B4FC", "#C7D2FE", "#EEF2FF"),
    // Purple & Violet
    listOf("#2E1065", "#4C1D95", "#5B21B6", "#6D28D9", "#7C3AED", "#8B5CF6", "#A78BFA", "#C4B5FD", "#DDD6FE", "#F5F3FF"),
    // Pink & Rose
    listOf("#4C0519", "#881337", "#9F1239", "#BE123C", "#E11D48", "#F43F5E", "#FB7185", "#FDA4AF", "#FECDD3", "#FFF1F2")
  )

  fun parseColor(hex: String, fallback: Color = Color(0xFF3B82F6)): Color {
    return try {
      val clean = hex.removePrefix("#").trim()
      val colorLong = when (clean.length) {
        6 -> "FF$clean".toLong(16)
        8 -> clean.toLong(16)
        3 -> {
          val r = clean[0]
          val g = clean[1]
          val b = clean[2]
          "FF$r$r$g$g$b$b".toLong(16)
        }
        else -> return fallback
      }
      Color(colorLong)
    } catch (_: Exception) {
      fallback
    }
  }
}
