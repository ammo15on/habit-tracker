package com.example.ui

import com.example.data.model.HabitTask
import com.example.data.model.RatingType

enum class AnalyticsTab {
  DAY,
  WEEK,
  MONTH,
  NEET
}

data class TaskItemUiState(
  val task: HabitTask,
  val timeSpentSeconds: Long,
  val isCompleted: Boolean,
  val isRunning: Boolean
)

data class DaySummary(
  val date: String,
  val dayOfWeekAbbr: String,
  val dayOfWeekIndex: Int,
  val rating: RatingType?,
  val totalTimeSeconds: Long,
  val completedTasksCount: Int,
  val totalTasksCount: Int
) {
  val isBest: Boolean get() = rating == RatingType.BEST
  val isAverage: Boolean get() = rating == RatingType.AVERAGE
  val isWorst: Boolean get() = rating == RatingType.WORST
}

data class WeekSummary(
  val weekLabel: String,
  val startDate: String,
  val endDate: String,
  val days: List<DaySummary>,
  val bestCount: Int,
  val averageCount: Int,
  val worstCount: Int,
  val totalTimeSeconds: Long,
  val completedTasksCount: Int,
  val totalTasksCount: Int
) {
  val isGreenWeek: Boolean get() = bestCount >= 4
}

data class MonthSummary(
  val monthLabel: String,
  val yearMonth: String,
  val days: List<DaySummary>,
  val bestCount: Int,
  val averageCount: Int,
  val worstCount: Int,
  val totalTimeSeconds: Long,
  val completedTasksCount: Int,
  val totalTasksCount: Int
) {
  val ratedDaysCount: Int
    get() = bestCount + averageCount + worstCount

  val isGreenMonth: Boolean
    get() = (ratedDaysCount > 0 && bestCount.toFloat() / ratedDaysCount >= 0.5f) || bestCount >= 15
}

data class TaskTallyItem(
  val taskId: Long,
  val taskName: String,
  val completionCount: Int,
  val totalTimeSeconds: Long,
  val totalTargetMinutes: Int
)
