package com.example.habit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planned_tasks")
data class PlannedTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val date: String,
    val targetTimeMinutes: Int = 0,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val isArchived: Boolean = false,
    val isStarred: Boolean = false
)
