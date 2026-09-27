package com.example.habit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class EventSubtask(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val assignedDates: String = ""
)

@Entity(tableName = "plan_events")
data class PlanEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val startDate: String,
    val endDate: String,
    val taskTitle: String = "",
    val taskTargetMinutes: Int = 0,
    val notes: String = "",
    val subtasksJson: String = "[]"
) {
    fun getSubtasks(): List<EventSubtask> {
        return try {
            Json.decodeFromString(subtasksJson)
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        fun serializeSubtasks(subtasks: List<EventSubtask>): String {
            return try {
                Json.encodeToString(subtasks)
            } catch (_: Exception) {
                "[]"
            }
        }
    }
}
