package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("theme_preferences", Context.MODE_PRIVATE)

  private val _customColorHex = MutableStateFlow(loadCustomColorHex())
  val customColorHex: StateFlow<String> = _customColorHex.asStateFlow()

  private val _customFontColorHex = MutableStateFlow(loadCustomFontColorHex())
  val customFontColorHex: StateFlow<String> = _customFontColorHex.asStateFlow()

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

  private val _autoCalendarSync = MutableStateFlow(loadAutoCalendarSync())
  val autoCalendarSync: StateFlow<Boolean> = _autoCalendarSync.asStateFlow()

  private val _isWakeWordEnabled = MutableStateFlow(loadWakeWordEnabled())
  val isWakeWordEnabled: StateFlow<Boolean> = _isWakeWordEnabled.asStateFlow()

  private val _isAutoListenEnabled = MutableStateFlow(loadAutoListenEnabled())
  val isAutoListenEnabled: StateFlow<Boolean> = _isAutoListenEnabled.asStateFlow()

  private val _requireConfirmationForDeletion = MutableStateFlow(loadRequireConfirmation())
  val requireConfirmationForDeletion: StateFlow<Boolean> = _requireConfirmationForDeletion.asStateFlow()

  private val _speechLanguage = MutableStateFlow(loadSpeechLanguage())
  val speechLanguage: StateFlow<String> = _speechLanguage.asStateFlow()

  private fun loadCustomColorHex(): String {
    return prefs.getString(KEY_CUSTOM_UI_HEX, "#3B82F6") ?: "#3B82F6"
  }

  private fun loadCustomFontColorHex(): String {
    return prefs.getString(KEY_CUSTOM_FONT_HEX, "#F8FAFC") ?: "#F8FAFC"
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

  private fun loadAutoCalendarSync(): Boolean {
    return prefs.getBoolean(KEY_AUTO_CALENDAR_SYNC, false)
  }

  private fun loadWakeWordEnabled(): Boolean {
    return prefs.getBoolean(KEY_WAKE_WORD_ENABLED, false)
  }

  private fun loadAutoListenEnabled(): Boolean {
    return prefs.getBoolean(KEY_AUTO_LISTEN_ENABLED, false)
  }

  private fun loadRequireConfirmation(): Boolean {
    return prefs.getBoolean(KEY_REQUIRE_CONFIRMATION, true)
  }

  private fun loadSpeechLanguage(): String {
    return prefs.getString(KEY_SPEECH_LANG, "en-US") ?: "en-US"
  }

  fun setCustomColorHex(hex: String) {
    prefs.edit().putString(KEY_CUSTOM_UI_HEX, hex).apply()
    _customColorHex.value = hex
  }

  fun setCustomFontColorHex(hex: String) {
    prefs.edit().putString(KEY_CUSTOM_FONT_HEX, hex).apply()
    _customFontColorHex.value = hex
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

  fun setAutoCalendarSync(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_AUTO_CALENDAR_SYNC, enabled).apply()
    _autoCalendarSync.value = enabled
  }

  fun setWakeWordEnabled(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_WAKE_WORD_ENABLED, enabled).apply()
    _isWakeWordEnabled.value = enabled
  }

  fun setAutoListenEnabled(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_AUTO_LISTEN_ENABLED, enabled).apply()
    _isAutoListenEnabled.value = enabled
  }

  fun setRequireConfirmationForDeletion(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_REQUIRE_CONFIRMATION, enabled).apply()
    _requireConfirmationForDeletion.value = enabled
  }

  fun setSpeechLanguage(lang: String) {
    prefs.edit().putString(KEY_SPEECH_LANG, lang).apply()
    _speechLanguage.value = lang
  }

  companion object {
    private const val KEY_CUSTOM_UI_HEX = "key_custom_ui_hex"
    private const val KEY_CUSTOM_FONT_HEX = "key_custom_font_hex"
    private const val KEY_BG_IMAGE_URI = "key_bg_image_uri"
    private const val KEY_UI_OPACITY = "key_ui_opacity"
    private const val KEY_TEXT_SIZE_SCALE = "key_text_size_scale"
    private const val KEY_AI_CHAT_DRAFT = "key_ai_chat_draft"
    private const val KEY_AI_MODE = "key_ai_execution_mode"
    private const val KEY_AUTO_CALENDAR_SYNC = "key_auto_calendar_sync"
    private const val KEY_WAKE_WORD_ENABLED = "key_wake_word_enabled"
    private const val KEY_AUTO_LISTEN_ENABLED = "key_auto_listen_enabled"
    private const val KEY_REQUIRE_CONFIRMATION = "key_require_confirmation"
    private const val KEY_SPEECH_LANG = "key_speech_lang"
  }
}
