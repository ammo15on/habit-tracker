package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "habit_task_logs",
  foreignKeys = [
    ForeignKey(
      entity = HabitTask::class,
      parentColumns = ["id"],
      childColumns = ["taskId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [
    Index(value = ["taskId", "date"], unique = true),
    Index(value = ["date"])
  ]
)
data class HabitTaskLog(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val taskId: Long,
  val date: String, // Format: YYYY-MM-DD
  val timeSpentSeconds: Long = 0L,
  val isCompleted: Boolean = false
)
