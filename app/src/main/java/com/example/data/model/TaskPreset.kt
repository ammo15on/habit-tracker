package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_presets")
data class TaskPreset(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val name: String,
  val targetTimeMinutes: Int = 0,
  val noteText: String = "",
  val noteImageUri: String? = null
) {
  companion object {
    val DEFAULT_PRESETS = listOf(
      "Physics Practice",
      "Organic Chemistry Mechanisms",
      "Inorganic NCERT Line by Line",
      "Botany Revision",
      "Zoology Notes & Diagrams",
      "Full Length Mock Test (3h 20m)",
      "Daily Morning Meditation",
      "Evening Physical Exercise"
    )
  }
}
