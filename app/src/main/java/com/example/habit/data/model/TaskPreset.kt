package com.example.habit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_presets")
data class TaskPreset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val targetTimeMinutes: Int = 60
)
