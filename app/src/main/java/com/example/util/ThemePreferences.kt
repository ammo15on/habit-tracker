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

  private val _customColorHex = MutableStateFlow(loadCustomColorHex())
  val customColorHex: StateFlow<String> = _customColorHex.asStateFlow()
  val customUiHex: StateFlow<String> get() = customColorHex

  private val _customBgHex = MutableStateFlow(loadCustomBgHex())
  val customBgHex: StateFlow<String> = _customBgHex.asStateFlow()

  private val _customFontColorHex = MutableStateFlow(loadCustomFontColorHex())
  val customFontColorHex: StateFlow<String> = _customFontColorHex.asStateFlow()
  val customTextHex: StateFlow<String> get() = customFontColorHex

  private val _themeColor = MutableStateFlow(loadThemeColor())
  val themeColor: StateFlow<AppThemeColor> = _themeColor.asStateFlow()

  private val _fontColor = MutableStateFlow(loadFontColor())
  val fontColor: StateFlow<AppFontColor> = _fontColor.asStateFlow()

  private val _backgroundImageUri = MutableStateFlow(loadBackgroundImageUri())
  val backgroundImageUri: StateFlow<String?> = _backgroundImageUri.asStateFlow()

  private val _uiOpacity = MutableStateFlow(loadUiOpacity())
  val uiOpacity: StateFlow<Float> = _uiOpacity.asStateFlow()

  private val _textSizeScale = MutableStateFlow(loadTextSizeScale())
  val textSizeScale: StateFlow<Float> = _textSizeScale.asStateFlow()

  private val _aiChatDraft = MutableStateFlow(loadAiChatDraft())
  val aiChatDraft: StateFlow<String> = _aiChatDraft.asStateFlow()

  private val _aiExecutionMode = MutableStateFlow(loadAiExecutionMode())
  val aiExecutionMode: StateFlow<String> = _aiExecutionMode.asStateFlow()

  private fun loadCustomColorHex(): String {
    return prefs.getString(KEY_CUSTOM_UI_HEX, "#3B82F6") ?: "#3B82F6"
  }

  private fun loadCustomBgHex(): String {
    return prefs.getString(KEY_CUSTOM_BG_HEX, "#090B10") ?: "#090B10"
  }

  private fun loadCustomFontColorHex(): String {
    return prefs.getString(KEY_CUSTOM_FONT_HEX, "#F8FAFC") ?: "#F8FAFC"
  }

  private fun loadThemeColor(): AppThemeColor {
    val name = prefs.getString(KEY_THEME_COLOR, AppThemeColor.SLATE.name)
    return try {
      AppThemeColor.valueOf(name ?: AppThemeColor.SLATE.name)
    } catch (_: Exception) {
      AppThemeColor.SLATE
    }
  }

  private fun loadFontColor(): AppFontColor {
    val name = prefs.getString(KEY_FONT_COLOR, AppFontColor.DEFAULT.name)
    return try {
      AppFontColor.valueOf(name ?: AppFontColor.DEFAULT.name)
    } catch (_: Exception) {
      AppFontColor.DEFAULT
    }
  }

  private fun loadBackgroundImageUri(): String? {
    return prefs.getString(KEY_BG_IMAGE_URI, null)
  }

  private fun loadUiOpacity(): Float {
    return prefs.getFloat(KEY_UI_OPACITY, 0.85f)
  }

  private fun loadTextSizeScale(): Float {
    return prefs.getFloat(KEY_TEXT_SIZE_SCALE, 1.0f)
  }

  private fun loadAiChatDraft(): String {
    return prefs.getString(KEY_AI_CHAT_DRAFT, "") ?: ""
  }

  private fun loadAiExecutionMode(): String {
    return prefs.getString(KEY_AI_MODE, "on_device") ?: "on_device"
  }

  fun setCustomColorHex(hex: String) {
    prefs.edit().putString(KEY_CUSTOM_UI_HEX, hex).apply()
    _customColorHex.value = hex
  }

  fun setCustomUiHex(hex: String) = setCustomColorHex(hex)

  fun setCustomBgHex(hex: String) {
    prefs.edit().putString(KEY_CUSTOM_BG_HEX, hex).apply()
    _customBgHex.value = hex
  }

  fun setCustomFontColorHex(hex: String) {
    prefs.edit().putString(KEY_CUSTOM_FONT_HEX, hex).apply()
    _customFontColorHex.value = hex
  }

  fun setCustomTextHex(hex: String) = setCustomFontColorHex(hex)

  fun setThemeColor(color: AppThemeColor) {
    prefs.edit().putString(KEY_THEME_COLOR, color.name).apply()
    _themeColor.value = color
  }

  fun setFontColor(color: AppFontColor) {
    prefs.edit().putString(KEY_FONT_COLOR, color.name).apply()
    _fontColor.value = color
  }

  fun setBackgroundImageUri(uri: String?) {
    prefs.edit().putString(KEY_BG_IMAGE_URI, uri).apply()
    _backgroundImageUri.value = uri
  }

  fun setUiOpacity(opacity: Float) {
    val clamped = opacity.coerceIn(0.10f, 1.0f)
    prefs.edit().putFloat(KEY_UI_OPACITY, clamped).apply()
    _uiOpacity.value = clamped
  }

  fun setTextSizeScale(scale: Float) {
    val clamped = scale.coerceIn(0.80f, 1.35f)
    prefs.edit().putFloat(KEY_TEXT_SIZE_SCALE, clamped).apply()
    _textSizeScale.value = clamped
  }

  fun setAiChatDraft(draft: String) {
    prefs.edit().putString(KEY_AI_CHAT_DRAFT, draft).apply()
    _aiChatDraft.value = draft
  }

  fun setAiExecutionMode(mode: String) {
    prefs.edit().putString(KEY_AI_MODE, mode).apply()
    _aiExecutionMode.value = mode
  }

  companion object {
    private const val KEY_CUSTOM_UI_HEX = "key_custom_ui_hex"
    private const val KEY_CUSTOM_BG_HEX = "key_custom_bg_hex"
    private const val KEY_CUSTOM_FONT_HEX = "key_custom_font_hex"
    private const val KEY_THEME_COLOR = "key_theme_color"
    private const val KEY_FONT_COLOR = "key_font_color"
    private const val KEY_BG_IMAGE_URI = "key_bg_image_uri"
    private const val KEY_UI_OPACITY = "key_ui_opacity"
    private const val KEY_TEXT_SIZE_SCALE = "key_text_size_scale"
    private const val KEY_AI_CHAT_DRAFT = "key_ai_chat_draft"
    private const val KEY_AI_MODE = "key_ai_execution_mode"
  }
}
