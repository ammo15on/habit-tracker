package com.example.data.repository

import com.example.data.db.HabitDao
import com.example.data.model.DayRating
import com.example.data.model.HabitTask
import com.example.data.model.HabitTaskLog
import com.example.data.model.NeetTestScore
import com.example.data.model.PlannedTask
import com.example.data.model.RatingType
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val dao: HabitDao) {

  val allTasks: Flow<List<HabitTask>> = dao.getAllTasks()
  val defaultTasks: Flow<List<HabitTask>> = dao.getDefaultTasks()
  val allRatings: Flow<List<DayRating>> = dao.getAllRatings()
  val allLogs: Flow<List<HabitTaskLog>> = dao.getAllLogs()
  val allNeetScores: Flow<List<NeetTestScore>> = dao.getAllNeetScores()
  val allPlannedTasks: Flow<List<PlannedTask>> = dao.getAllPlannedTasks()

  fun getLogsForDate(date: String): Flow<List<HabitTaskLog>> = dao.getLogsForDate(date)

  fun getDayRating(date: String): Flow<DayRating?> = dao.getDayRating(date)

  suspend fun insertTask(
    name: String,
    repeatDaysMask: Int,
    targetTimeMinutes: Int = 0,
    isDefault: Boolean = false,
    noteText: String = "",
    noteImageUri: String? = null
  ): Long {
    val task = HabitTask(
      name = name,
      repeatDaysMask = repeatDaysMask,
      targetTimeMinutes = targetTimeMinutes,
      isDefault = isDefault,
      noteText = noteText,
      noteImageUri = noteImageUri
    )
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

  // NEET test score operations
  suspend fun insertNeetScore(score: NeetTestScore): Long = dao.insertNeetScore(score)

  suspend fun deleteNeetScore(score: NeetTestScore) = dao.deleteNeetScore(score)

  // Planned tasks operations
  suspend fun insertPlannedTask(task: PlannedTask): Long = dao.insertPlannedTask(task)

  suspend fun updatePlannedTask(task: PlannedTask) = dao.updatePlannedTask(task)

  suspend fun deletePlannedTask(task: PlannedTask) = dao.deletePlannedTask(task)

  suspend fun deletePlannedTaskById(id: Long) = dao.deletePlannedTaskById(id)
}
