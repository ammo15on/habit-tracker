package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_tasks")
data class HabitTask(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val name: String,
  val targetDate: String? = null, // Specific date (yyyy-MM-dd) if task is for a single day, null if recurring habit
  val repeatDaysMask: Int = 0, // Days of week bitmask (1 shl dayIdx)
  val targetTimeMinutes: Int = 0, // 0 = no target set, >0 = target timer in minutes
  val isDefault: Boolean = false, // Option to add as default task and edit default tasks
  val isStarred: Boolean = false, // Starred / high priority task
  val targetDates: String? = null, // Multiple specific dates separated by comma (e.g. "2026-09-21,2026-09-23")
  val startDate: String? = null, // Event starting date (yyyy-MM-dd)
  val endDate: String? = null, // Event ending date (yyyy-MM-dd)
  val eventId: Long? = null, // Optional parent event ID
  val noteText: String = "", // Text note
  val noteImageUri: String? = null, // Image note URI/path
  val createdAt: Long = System.currentTimeMillis(),
  val isArchived: Boolean = false
) {
  val isPreset: Boolean get() = isDefault

  fun isScheduledFor(date: String, dayOfWeekIndex: Int): Boolean {
    // 1. If assigned to an event date range
    if (startDate != null && endDate != null) {
      return (date in startDate..endDate)
    }
    // 2. If assigned to multiple specific dates
    if (!targetDates.isNullOrBlank()) {
      val datesList = targetDates.split(",")
      if (datesList.contains(date)) return true
    }
    // 3. If assigned to a single specific target date
    if (targetDate != null) {
      return targetDate == date
    }
    // 4. If it has recurring day mask configured
    if (repeatDaysMask != 0) {
      val bit = 1 shl dayOfWeekIndex
      return (repeatDaysMask and bit) != 0
    }
    // 5. Fallback for preset recurring tasks
    return isDefault
  }

  fun isRepeatingOn(dayOfWeekIndex: Int): Boolean {
    // dayOfWeekIndex: 0 = Mon, 1 = Tue, 2 = Wed, 3 = Thu, 4 = Fri, 5 = Sat, 6 = Sun
    val bit = 1 shl dayOfWeekIndex
    return (repeatDaysMask and bit) != 0
  }

  companion object {
    const val EVERYDAY_MASK = 0b1111111 // 127 (Mon..Sun)
    val DAY_LETTERS = listOf("M", "T", "W", "T", "F", "S", "S")
    val DAY_NAMES = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    // Quick-add default NEET study presets requested by user
    val DEFAULT_NEET_PRESETS = listOf(
      "bot ncert read",
      "bot q",
      "zoo ncert read",
      "zoo q",
      "phy q",
      "phy ch q",
      "inorg ncert",
      "org revision"
    )
  }
}
