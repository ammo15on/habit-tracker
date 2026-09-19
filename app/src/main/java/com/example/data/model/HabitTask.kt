package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_tasks")
data class HabitTask(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val name: String,
  val repeatDaysMask: Int = EVERYDAY_MASK, // Default everyday
  val targetTimeMinutes: Int = 0, // 0 = no target set, >0 = target timer in minutes
  val isDefault: Boolean = false, // option to add as default task and edit default tasks
  val noteText: String = "", // text note
  val noteImageUri: String? = null, // image note URI/path
  val createdAt: Long = System.currentTimeMillis(),
  val isArchived: Boolean = false
) {
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
