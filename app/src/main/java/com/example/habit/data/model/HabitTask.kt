package com.example.habit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_tasks")
data class HabitTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val targetDate: String = "",
    val repeatDaysMask: Int = 0,
    val targetTimeMinutes: Int = 0,
    val isDefault: Boolean = false,
    val isStarred: Boolean = false,
    val noteText: String = ""
)
