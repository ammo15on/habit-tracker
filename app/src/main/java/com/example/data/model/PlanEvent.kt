package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

data class PlanSubtask(
  val id: String = java.util.UUID.randomUUID().toString(),
  val title: String,
  val isCompleted: Boolean = false,
  val targetDate: String? = null, // Specific date if tied to day
  val assignedDates: String = "" // Comma-separated dates e.g. "2026-09-24,2026-09-25"
)

typealias EventSubtask = PlanSubtask

@Entity(tableName = "plan_events")
data class PlanEvent(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val startDate: String, // YYYY-MM-DD
  val endDate: String, // YYYY-MM-DD
  val taskTitle: String = "",
  val taskTargetMinutes: Int = 0,
  val notes: String = "",
  val subtasksJson: String = "[]" // Serialized JSON list of PlanSubtask
) {
  fun getSubtasks(): List<PlanSubtask> {
    return try {
      val list = mutableListOf<PlanSubtask>()
      val arr = JSONArray(subtasksJson)
      for (i in 0 until arr.length()) {
        val obj = arr.getJSONObject(i)
        list.add(
          PlanSubtask(
            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
            title = obj.optString("title", ""),
            isCompleted = obj.optBoolean("isCompleted", false),
            targetDate = obj.optString("targetDate", null).takeIf { !it.isNullOrBlank() },
            assignedDates = obj.optString("assignedDates", "")
          )
        )
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }

  fun isSubtaskApplicableForDate(subtask: PlanSubtask, date: String): Boolean {
    if (subtask.assignedDates.isNotBlank()) {
      val datesList = subtask.assignedDates.split(",").map { it.trim() }
      return datesList.contains(date)
    }
    if (subtask.targetDate != null) {
      return subtask.targetDate == date
    }
    return date >= startDate && date <= endDate
  }

  companion object {
    fun serializeSubtasks(subtasks: List<PlanSubtask>): String {
      val arr = JSONArray()
      subtasks.forEach { st ->
        val obj = JSONObject().apply {
          put("id", st.id)
          put("title", st.title)
          put("isCompleted", st.isCompleted)
          if (st.targetDate != null) put("targetDate", st.targetDate)
          if (st.assignedDates.isNotBlank()) put("assignedDates", st.assignedDates)
        }
        arr.put(obj)
      }
      return arr.toString()
    }
  }
}
