package com.example.ui

import com.example.data.model.HabitTask
import com.example.data.model.RatingType

enum class AnalyticsTab {
  WEEK,
  MONTH,
  DAYS
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
  // Rule from sketch: "on marking 4 or more days best that week becomes green"
  val isGreen: Boolean get() = bestCount >= 4

  val overallRating: RatingType?
    get() = when {
      bestCount >= 4 -> RatingType.BEST
      averageCount >= worstCount && (averageCount > 0 || bestCount > 0) -> RatingType.AVERAGE
      worstCount > averageCount -> RatingType.WORST
      else -> null
    }
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
  // Similar analogy for month: 50% or >= 15 best days is green
  val isGreen: Boolean
    get() {
      val totalRated = bestCount + averageCount + worstCount
      return if (totalRated >= 4) {
        bestCount >= 15 || (totalRated > 0 && bestCount.toDouble() / totalRated >= 0.5)
      } else {
        bestCount >= 4
      }
    }

  val overallRating: RatingType?
    get() = when {
      isGreen -> RatingType.BEST
      averageCount >= worstCount && (averageCount > 0 || bestCount > 0) -> RatingType.AVERAGE
      worstCount > averageCount -> RatingType.WORST
      else -> null
    }
}
