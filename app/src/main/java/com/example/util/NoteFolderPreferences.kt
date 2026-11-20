package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class NoteFolderPreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("note_folder_preferences", Context.MODE_PRIVATE)

  private val _customSubjects = MutableStateFlow(loadCustomSubjects())
  val customSubjects: StateFlow<List<String>> = _customSubjects.asStateFlow()

  private val _customSubfolders = MutableStateFlow(loadCustomSubfolders())
  val customSubfolders: StateFlow<Map<String, List<String>>> = _customSubfolders.asStateFlow()

  private fun loadCustomSubjects(): List<String> {
    val raw = prefs.getString(KEY_CUSTOM_SUBJECTS, "[]") ?: "[]"
    return try {
      val arr = JSONArray(raw)
      (0 until arr.length()).mapNotNull { arr.optString(it).takeIf { s -> s.isNotBlank() } }
    } catch (_: Exception) {
      emptyList()
    }
  }

  private fun loadCustomSubfolders(): Map<String, List<String>> {
    val raw = prefs.getString(KEY_CUSTOM_SUBFOLDERS, "{}") ?: "{}"
    return try {
      val obj = JSONObject(raw)
      val map = mutableMapOf<String, List<String>>()
      val keys = obj.keys()
      while (keys.hasNext()) {
        val subj = keys.next()
        val arr = obj.optJSONArray(subj)
        if (arr != null) {
          val list = (0 until arr.length()).mapNotNull { arr.optString(it).takeIf { s -> s.isNotBlank() } }
          if (list.isNotEmpty()) map[subj] = list
        }
      }
      map
    } catch (_: Exception) {
      emptyMap()
    }
  }

  fun addCustomSubject(subjectName: String) {
    val trimmed = subjectName.trim()
    if (trimmed.isBlank()) return
    val current = _customSubjects.value.toMutableList()
    if (current.none { it.equals(trimmed, ignoreCase = true) }) {
      current.add(trimmed)
      val arr = JSONArray()
      current.forEach { arr.put(it) }
      prefs.edit().putString(KEY_CUSTOM_SUBJECTS, arr.toString()).apply()
      _customSubjects.value = current
    }
  }

  fun addCustomSubfolder(subjectName: String, subfolderName: String) {
    val subj = subjectName.trim()
    val sub = subfolderName.trim()
    if (subj.isBlank() || sub.isBlank()) return
    val current = _customSubfolders.value.toMutableMap()
    val existing = current[subj]?.toMutableList() ?: mutableListOf()
    if (existing.none { it.equals(sub, ignoreCase = true) }) {
      existing.add(sub)
      current[subj] = existing
      saveSubfoldersMap(current)
    }
  }

  fun importFolders(subjects: List<String>, subfolders: Map<String, List<String>>) {
    val mergedSubjects = (_customSubjects.value + subjects).map { it.trim() }.filter { it.isNotBlank() }.distinctBy { it.lowercase() }
    val arr = JSONArray()
    mergedSubjects.forEach { arr.put(it) }
    prefs.edit().putString(KEY_CUSTOM_SUBJECTS, arr.toString()).apply()
    _customSubjects.value = mergedSubjects

    val mergedSubs = _customSubfolders.value.toMutableMap()
    subfolders.forEach { (k, v) ->
      val cur = (mergedSubs[k] ?: emptyList()) + v
      mergedSubs[k] = cur.map { it.trim() }.filter { it.isNotBlank() }.distinctBy { it.lowercase() }
    }
    saveSubfoldersMap(mergedSubs)
  }

  private fun saveSubfoldersMap(map: Map<String, List<String>>) {
    val obj = JSONObject()
    map.forEach { (k, v) ->
      val arr = JSONArray()
      v.forEach { arr.put(it) }
      obj.put(k, arr)
    }
    prefs.edit().putString(KEY_CUSTOM_SUBFOLDERS, obj.toString()).apply()
    _customSubfolders.value = map
  }

  companion object {
    private const val KEY_CUSTOM_SUBJECTS = "key_custom_subjects"
    private const val KEY_CUSTOM_SUBFOLDERS = "key_custom_subfolders"
  }
}
