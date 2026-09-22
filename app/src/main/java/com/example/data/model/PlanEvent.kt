package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class EventSubtask(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val assignedDates: String = "", // Comma-separated yyyy-MM-dd. If blank/empty, appears everyday during event!
  val isCompleted: Boolean = false
)

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
  val subtasksJson: String = "[]",
  val createdAt: Long = System.currentTimeMillis()
) {
  fun getSubtasks(): List<EventSubtask> {
    if (subtasksJson.isBlank() || subtasksJson == "[]") return emptyList()
    return try {
      val arr = JSONArray(subtasksJson)
      val list = mutableListOf<EventSubtask>()
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        list.add(
          EventSubtask(
            id = o.optString("id", UUID.randomUUID().toString()),
            title = o.optString("title", ""),
            assignedDates = o.optString("assignedDates", ""),
            isCompleted = o.optBoolean("isCompleted", false)
          )
        )
      }
      list
    } catch (e: Exception) {
      emptyList()
    }
  }

  fun isSubtaskApplicableForDate(subtask: EventSubtask, date: String): Boolean {
    if (startDate.isNotBlank() && endDate.isNotBlank()) {
      if (date < startDate || date > endDate) return false
    }
    if (subtask.assignedDates.isBlank()) return true
    val dates = subtask.assignedDates.split(",").map { it.trim() }
    return dates.contains(date)
  }

  companion object {
    fun serializeSubtasks(subtasks: List<EventSubtask>): String {
      val arr = JSONArray()
      for (st in subtasks) {
        val obj = JSONObject()
        obj.put("id", st.id)
        obj.put("title", st.title)
        obj.put("assignedDates", st.assignedDates)
        obj.put("isCompleted", st.isCompleted)
        arr.put(obj)
      }
      return arr.toString()
    }
  }
}
