package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.theme.AppFontColor
import com.example.ui.theme.AppThemeColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("theme_preferences", Context.MODE_PRIVATE)

  private val _themeColor = MutableStateFlow(loadThemeColor())
  val themeColor: StateFlow<AppThemeColor> = _themeColor.asStateFlow()

  private val _fontColor = MutableStateFlow(loadFontColor())
  val fontColor: StateFlow<AppFontColor> = _fontColor.asStateFlow()

  private val _backgroundImageUri = MutableStateFlow(loadBackgroundImageUri())
  val backgroundImageUri: StateFlow<String?> = _backgroundImageUri.asStateFlow()

  private fun loadThemeColor(): AppThemeColor {
    val savedId = prefs.getString(KEY_THEME_COLOR, AppThemeColor.SLATE.name) ?: AppThemeColor.SLATE.name
    return try {
      AppThemeColor.valueOf(savedId)
    } catch (_: Exception) {
      AppThemeColor.SLATE
    }
  }

  private fun loadFontColor(): AppFontColor {
    val savedId = prefs.getString(KEY_FONT_COLOR, AppFontColor.DEFAULT.name) ?: AppFontColor.DEFAULT.name
    return try {
      AppFontColor.valueOf(savedId)
    } catch (_: Exception) {
      AppFontColor.DEFAULT
    }
  }

  private fun loadBackgroundImageUri(): String? {
    return prefs.getString(KEY_BACKGROUND_IMAGE_URI, null)
  }

  fun setThemeColor(color: AppThemeColor) {
    prefs.edit().putString(KEY_THEME_COLOR, color.name).apply()
    _themeColor.value = color
  }

  fun setFontColor(color: AppFontColor) {
    prefs.edit().putString(KEY_FONT_COLOR, color.name).apply()
    _fontColor.value = color
  }

  fun setBackgroundImageUri(uriString: String?) {
    if (uriString != null) {
      prefs.edit().putString(KEY_BACKGROUND_IMAGE_URI, uriString).apply()
    } else {
      prefs.edit().remove(KEY_BACKGROUND_IMAGE_URI).apply()
    }
    _backgroundImageUri.value = uriString
  }

  companion object {
    private const val KEY_THEME_COLOR = "app_theme_color"
    private const val KEY_FONT_COLOR = "app_font_color"
    private const val KEY_BACKGROUND_IMAGE_URI = "app_bg_image_uri"
  }
}

