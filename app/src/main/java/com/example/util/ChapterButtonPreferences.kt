package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class ChapterButtonItem(
  val id: String,
  val name: String,
  val colorHex: Long,
  val isBuiltIn: Boolean = false,
  val isEnabled: Boolean = true,
  val isCounter: Boolean = false
)

class ChapterButtonPreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("chapter_button_preferences", Context.MODE_PRIVATE)

  private val _buttons = MutableStateFlow(loadButtons())
  val buttons: StateFlow<List<ChapterButtonItem>> = _buttons.asStateFlow()

  companion object {
    private const val KEY_BUTTONS_JSON = "key_buttons_json"

    val DEFAULT_BUTTONS = listOf(
      ChapterButtonItem(id = "completed", name = "Completed", colorHex = 0xFF10B981, isBuiltIn = true, isEnabled = true, isCounter = false),
      ChapterButtonItem(id = "ncert", name = "NCERT Read", colorHex = 0xFFD97706, isBuiltIn = true, isEnabled = true, isCounter = true),
      ChapterButtonItem(id = "pyq", name = "PYQ Done", colorHex = 0xFF2563EB, isBuiltIn = true, isEnabled = true, isCounter = false),
      ChapterButtonItem(id = "exercise", name = "Exercise Done", colorHex = 0xFF8B5CF6, isBuiltIn = true, isEnabled = true, isCounter = false),
      ChapterButtonItem(id = "ar", name = "A&R Done", colorHex = 0xFFEC4899, isBuiltIn = true, isEnabled = true, isCounter = false)
    )
  }

  private fun loadButtons(): List<ChapterButtonItem> {
    val jsonStr = prefs.getString(KEY_BUTTONS_JSON, null) ?: return DEFAULT_BUTTONS
    return try {
      val jsonArray = JSONArray(jsonStr)
      val list = mutableListOf<ChapterButtonItem>()
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        list.add(
          ChapterButtonItem(
            id = obj.getString("id"),
            name = obj.getString("name"),
            colorHex = obj.optLong("colorHex", 0xFF2563EB),
            isBuiltIn = obj.optBoolean("isBuiltIn", false),
            isEnabled = obj.optBoolean("isEnabled", true),
            isCounter = obj.optBoolean("isCounter", false)
          )
        )
      }
      if (list.isEmpty()) DEFAULT_BUTTONS else list
    } catch (_: Exception) {
      DEFAULT_BUTTONS
    }
  }

  private fun saveButtons(buttons: List<ChapterButtonItem>) {
    val jsonArray = JSONArray()
    for (btn in buttons) {
      val obj = JSONObject().apply {
        put("id", btn.id)
        put("name", btn.name)
        put("colorHex", btn.colorHex)
        put("isBuiltIn", btn.isBuiltIn)
        put("isEnabled", btn.isEnabled)
        put("isCounter", btn.isCounter)
      }
      jsonArray.put(obj)
    }
    prefs.edit().putString(KEY_BUTTONS_JSON, jsonArray.toString()).apply()
    _buttons.value = buttons
  }

  fun toggleButtonEnabled(buttonId: String) {
    val current = _buttons.value
    val updated = current.map {
      if (it.id == buttonId) it.copy(isEnabled = !it.isEnabled) else it
    }
    saveButtons(updated)
  }

  fun addCustomButton(name: String, colorHex: Long, isCounter: Boolean) {
    val id = "custom_" + System.currentTimeMillis()
    val newButton = ChapterButtonItem(
      id = id,
      name = name.trim(),
      colorHex = colorHex,
      isBuiltIn = false,
      isEnabled = true,
      isCounter = isCounter
    )
    val current = _buttons.value.toMutableList()
    current.add(newButton)
    saveButtons(current)
  }

  fun removeButton(buttonId: String) {
    val current = _buttons.value
    // If it's built-in, we disable/hide it; if custom, we delete it completely
    val updated = current.mapNotNull { btn ->
      if (btn.id == buttonId) {
        if (btn.isBuiltIn) btn.copy(isEnabled = false) else null
      } else {
        btn
      }
    }
    saveButtons(updated)
  }

  fun resetToDefaults() {
    saveButtons(DEFAULT_BUTTONS)
  }
}
