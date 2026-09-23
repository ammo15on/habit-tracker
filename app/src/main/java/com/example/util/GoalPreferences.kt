package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppGoal(
  val title: String,
  val targetDate: String // YYYY-MM-DD
)

class GoalPreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("goal_preferences", Context.MODE_PRIVATE)

  private val _goal = MutableStateFlow(loadGoal())
  val goal: StateFlow<AppGoal?> = _goal.asStateFlow()

  private fun loadGoal(): AppGoal? {
    val title = prefs.getString(KEY_GOAL_TITLE, null)
    val date = prefs.getString(KEY_GOAL_DATE, null)
    return if (!title.isNullOrBlank() && !date.isNullOrBlank()) {
      AppGoal(title, date)
    } else {
      null
    }
  }

  fun setGoal(title: String, targetDate: String) {
    prefs.edit()
      .putString(KEY_GOAL_TITLE, title.trim())
      .putString(KEY_GOAL_DATE, targetDate.trim())
      .apply()
    _goal.value = AppGoal(title.trim(), targetDate.trim())
  }

  fun clearGoal() {
    prefs.edit()
      .remove(KEY_GOAL_TITLE)
      .remove(KEY_GOAL_DATE)
      .apply()
    _goal.value = null
  }

  companion object {
    private const val KEY_GOAL_TITLE = "app_goal_title"
    private const val KEY_GOAL_DATE = "app_goal_date"
  }
}
