package com.example.habit.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppGoal(
    val title: String = "NEET Goal 2026",
    val examDate: String = "2026-05-05",
    val targetHours: Int = 8
)

class GoalPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("goal_preferences", Context.MODE_PRIVATE)

    private val _goal = MutableStateFlow(loadGoal())
    val goal: StateFlow<AppGoal> = _goal.asStateFlow()

    private fun loadGoal(): AppGoal {
        val title = prefs.getString("goal_title", "NEET Goal 2026") ?: "NEET Goal 2026"
        val examDate = prefs.getString("goal_date", "2026-05-05") ?: "2026-05-05"
        val hours = prefs.getInt("goal_hours", 8)
        return AppGoal(title, examDate, hours)
    }

    fun updateGoal(newGoal: AppGoal) {
        prefs.edit()
            .putString("goal_title", newGoal.title)
            .putString("goal_date", newGoal.examDate)
            .putInt("goal_hours", newGoal.targetHours)
            .apply()
        _goal.value = newGoal
    }
}
