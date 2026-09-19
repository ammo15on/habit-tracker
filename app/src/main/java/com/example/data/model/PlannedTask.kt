package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planned_tasks")
data class PlannedTask(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val date: String, // yyyy-MM-dd
  val targetTimeMinutes: Int = 0, // Target timer in minutes
  val notes: String = "",
  val isStarred: Boolean = false,
  val isCompleted: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)
