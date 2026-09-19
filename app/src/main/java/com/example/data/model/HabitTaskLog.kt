package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "habit_task_logs",
  indices = [
    Index(value = ["taskId", "date"], unique = true),
    Index(value = ["date"])
  ]
)
data class HabitTaskLog(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val taskId: Long,
  val date: String, // "yyyy-MM-dd"
  val timeSpentSeconds: Long = 0,
  val isCompleted: Boolean = false
)
