package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_tasks")
data class HabitTask(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val name: String,
  val targetDate: String? = null, // If set, specific to one date (YYYY-MM-DD)
  val repeatDaysMask: Int = 0, // Bitmask: bit 0 = Monday ... bit 6 = Sunday. (127 = everyday)
  val targetTimeMinutes: Int = 0, // In minutes (0 = no target / count-up only)
  val isDefault: Boolean = false,
  val isStarred: Boolean = false,
  val targetDates: String? = null, // Comma-separated dates e.g. "2026-09-24,2026-09-25"
  val startDate: String? = null,
  val endDate: String? = null,
  val noteText: String = "",
  val noteImageUri: String? = null,
  val reminderTime: String? = null, // Format: "HH:mm"
  val isArchived: Boolean = false,
  val eventId: Long? = null
) {
  fun isRepeatingOn(dayOfWeekIndex: Int): Boolean {
    return (repeatDaysMask and (1 shl dayOfWeekIndex)) != 0
  }

  fun isScheduledFor(date: String, dayOfWeekIndex: Int): Boolean {
    // 1. Single target date match
    if (targetDate != null && targetDate == date) return true

    // 2. Multiple target dates set
    if (!targetDates.isNullOrBlank()) {
      val datesList = targetDates.split(",").map { it.trim() }
      if (datesList.contains(date)) return true
    }

    // 3. Date Range + Repeat mask
    if (startDate != null && endDate != null) {
      if (date >= startDate && date <= endDate) {
        if (repeatDaysMask == 0 || repeatDaysMask == EVERYDAY_MASK) return true
        val bit = 1 shl dayOfWeekIndex
        if ((repeatDaysMask and bit) != 0) return true
      }
      return false
    }

    // 4. Default everyday tasks
    if (isDefault) return true

    // 5. Weekly repeating recurrence bitmask
    if (repeatDaysMask != 0) {
      val bit = 1 shl dayOfWeekIndex
      return (repeatDaysMask and bit) != 0
    }

    return false
  }

  companion object {
    const val EVERYDAY_MASK = 0x7F // 1111111 in binary (7 days)
    const val MON_TO_SAT_MASK = 0x3F // Monday through Saturday
    const val MON_TO_FRI_MASK = 0x1F // Monday through Friday
    val DAY_LETTERS = listOf("M", "T", "W", "T", "F", "S", "S")
    val DEFAULT_NEET_PRESETS = listOf(
      "Biology NCERT",
      "Physics Problem Solving",
      "Chemistry Organic/Inorganic",
      "Mock Test Analysis",
      "Formula Revision",
      "Flashcards"
    )
  }
}
