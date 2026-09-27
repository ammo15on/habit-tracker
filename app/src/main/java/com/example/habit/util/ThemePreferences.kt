package com.example.habit.util

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

    private val _autoCalendarSync = MutableStateFlow(loadAutoCalendarSync())
    val autoCalendarSync: StateFlow<Boolean> = _autoCalendarSync.asStateFlow()

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

    private fun loadAutoCalendarSync(): Boolean {
        return prefs.getBoolean(KEY_AUTO_CALENDAR_SYNC, false)
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

    fun setAutoCalendarSync(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CALENDAR_SYNC, enabled).apply()
        _autoCalendarSync.value = enabled
    }

    companion object {
        private const val KEY_CUSTOM_UI_HEX = "key_custom_ui_hex"
        private const val KEY_CUSTOM_FONT_HEX = "key_custom_font_hex"
        private const val KEY_BG_IMAGE_URI = "key_bg_image_uri"
        private const val KEY_UI_OPACITY = "key_ui_opacity"
        private const val KEY_TEXT_SIZE_SCALE = "key_text_size_scale"
        private const val KEY_AUTO_CALENDAR_SYNC = "key_auto_calendar_sync"
    }
}
