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
  val noteImageUri: String? = null,
  val createdAt: Long = System.currentTimeMillis()
) {
  companion object {
    val DEFAULT_PRESETS = listOf(
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
