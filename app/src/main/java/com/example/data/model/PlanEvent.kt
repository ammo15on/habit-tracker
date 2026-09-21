package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plan_events")
data class PlanEvent(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val startDate: String, // yyyy-MM-dd
  val endDate: String,   // yyyy-MM-dd
  val taskTitle: String, // Task to do during this event
  val taskTargetMinutes: Int = 0,
  val notes: String = "",
  val createdAt: Long = System.currentTimeMillis()
)
