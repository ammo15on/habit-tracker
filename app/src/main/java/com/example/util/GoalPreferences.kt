package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppGoal(
  val title: String = "NEET 2026 Target: 680+ Score",
  val examDate: String = "2026-05-03",
  val dailyStudyHoursTarget: Int = 10,
  val motivationQuote: String = "Every single formula and NCERT line matters. Stay relentless."
) {
  val targetDate: String get() = examDate
}

class GoalPreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("goal_preferences", Context.MODE_PRIVATE)

  private val _goal = MutableStateFlow(loadGoal())
  val goal: StateFlow<AppGoal> = _goal.asStateFlow()

  private fun loadGoal(): AppGoal {
    return AppGoal(
      title = prefs.getString(KEY_TITLE, "NEET 2026 Target: 680+ Score") ?: "NEET 2026 Target: 680+ Score",
      examDate = prefs.getString(KEY_EXAM_DATE, "2026-05-03") ?: "2026-05-03",
      dailyStudyHoursTarget = prefs.getInt(KEY_DAILY_HOURS, 10),
      motivationQuote = prefs.getString(KEY_QUOTE, "Every single formula and NCERT line matters. Stay relentless.")
        ?: "Every single formula and NCERT line matters. Stay relentless."
    )
  }

  fun updateGoal(newGoal: AppGoal) {
    prefs.edit()
      .putString(KEY_TITLE, newGoal.title)
      .putString(KEY_EXAM_DATE, newGoal.examDate)
      .putInt(KEY_DAILY_HOURS, newGoal.dailyStudyHoursTarget)
      .putString(KEY_QUOTE, newGoal.motivationQuote)
      .apply()
    _goal.value = newGoal
  }

  companion object {
    private const val KEY_TITLE = "key_goal_title"
    private const val KEY_EXAM_DATE = "key_goal_exam_date"
    private const val KEY_DAILY_HOURS = "key_goal_daily_hours"
    private const val KEY_QUOTE = "key_goal_quote"
  }
}
