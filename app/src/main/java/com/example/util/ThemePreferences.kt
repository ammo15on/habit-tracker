package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.theme.AppThemeColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("theme_preferences", Context.MODE_PRIVATE)

  private val _themeColor = MutableStateFlow(loadThemeColor())
  val themeColor: StateFlow<AppThemeColor> = _themeColor.asStateFlow()

  private fun loadThemeColor(): AppThemeColor {
    val savedId = prefs.getString(KEY_THEME_COLOR, AppThemeColor.EMERALD.name) ?: AppThemeColor.EMERALD.name
    return try {
      AppThemeColor.valueOf(savedId)
    } catch (_: Exception) {
      AppThemeColor.EMERALD
    }
  }

  fun setThemeColor(color: AppThemeColor) {
    prefs.edit().putString(KEY_THEME_COLOR, color.name).apply()
    _themeColor.value = color
  }

  companion object {
    private const val KEY_THEME_COLOR = "app_theme_color"
  }
}
