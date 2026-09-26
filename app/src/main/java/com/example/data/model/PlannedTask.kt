package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planned_tasks")
data class PlannedTask(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val date: String, // Format: YYYY-MM-DD
  val targetTimeMinutes: Int = 0,
  val notes: String = "",
  val isStarred: Boolean = false,
  val isArchived: Boolean = false,
  val isCompleted: Boolean = false
)
