package com.example.data.repository

import com.example.data.db.HabitDao
import com.example.data.model.DayRating
import com.example.data.model.HabitTask
import com.example.data.model.HabitTaskLog
import com.example.data.model.RatingType
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val dao: HabitDao) {

  val allTasks: Flow<List<HabitTask>> = dao.getAllTasks()
  val allRatings: Flow<List<DayRating>> = dao.getAllRatings()
  val allLogs: Flow<List<HabitTaskLog>> = dao.getAllLogs()

  fun getLogsForDate(date: String): Flow<List<HabitTaskLog>> = dao.getLogsForDate(date)

  fun getDayRating(date: String): Flow<DayRating?> = dao.getDayRating(date)

  suspend fun insertTask(name: String, repeatDaysMask: Int): Long {
    val task = HabitTask(name = name, repeatDaysMask = repeatDaysMask)
    return dao.insertTask(task)
  }

  suspend fun deleteTask(task: HabitTask) {
    dao.deleteTask(task)
  }

  suspend fun deleteTaskById(taskId: Long) {
    dao.deleteTaskById(taskId)
  }

  suspend fun updateTask(task: HabitTask) {
    dao.updateTask(task)
  }

  suspend fun addTimeToTask(taskId: Long, date: String, additionalSeconds: Long) {
    val existing = dao.getLog(taskId, date)
    if (existing != null) {
      val updated = existing.copy(
        timeSpentSeconds = existing.timeSpentSeconds + additionalSeconds
      )
      dao.insertOrUpdateLog(updated)
    } else {
      val newLog = HabitTaskLog(
        taskId = taskId,
        date = date,
        timeSpentSeconds = additionalSeconds,
        isCompleted = false
      )
      dao.insertOrUpdateLog(newLog)
    }
  }

  suspend fun toggleTaskComplete(taskId: Long, date: String) {
    val existing = dao.getLog(taskId, date)
    if (existing != null) {
      val updated = existing.copy(isCompleted = !existing.isCompleted)
      dao.insertOrUpdateLog(updated)
    } else {
      val newLog = HabitTaskLog(
        taskId = taskId,
        date = date,
        timeSpentSeconds = 0,
        isCompleted = true
      )
      dao.insertOrUpdateLog(newLog)
    }
  }

  suspend fun setDayRating(date: String, ratingType: RatingType) {
    val existing = dao.getDayRatingSync(date)
    if (existing != null && existing.rating == ratingType.name) {
      // If tapping the already selected rating, toggle it off/clear it
      dao.deleteRating(date)
    } else {
      val rating = DayRating(date = date, rating = ratingType.name)
      dao.insertOrUpdateRating(rating)
    }
  }

  suspend fun seedSampleTasksIfEmpty() {
    // Seed standard tasks so user has initial items matching the sketch ("Task-1", "Task-2")
    val defaultTask1 = HabitTask(name = "Task-1 (Morning Routine)", repeatDaysMask = HabitTask.EVERYDAY_MASK)
    val defaultTask2 = HabitTask(name = "Task-2 (Focus & Study)", repeatDaysMask = HabitTask.EVERYDAY_MASK)
    val defaultTask3 = HabitTask(name = "Task-3 (Workout & Health)", repeatDaysMask = HabitTask.EVERYDAY_MASK)
    dao.insertTask(defaultTask1)
    dao.insertTask(defaultTask2)
    dao.insertTask(defaultTask3)
  }
}
