package com.example.habit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_task_logs")
data class HabitTaskLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val taskId: Long,
    val date: String,
    val isCompleted: Boolean = false,
    val timeSpentSeconds: Long = 0L
)
