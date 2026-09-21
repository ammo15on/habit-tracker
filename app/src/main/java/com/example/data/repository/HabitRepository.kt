package com.example.data.repository

import com.example.data.db.HabitDao
import com.example.data.model.DayRating
import com.example.data.model.HabitTask
import com.example.data.model.HabitTaskLog
import com.example.data.model.NeetChapter
import com.example.data.model.NeetTallyCounter
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
  val allNeetChapters: Flow<List<NeetChapter>> = dao.getAllNeetChapters()
  val allNeetTallyCounters: Flow<List<NeetTallyCounter>> = dao.getAllNeetTallyCounters()
  val allPlanEvents: Flow<List<com.example.data.model.PlanEvent>> = dao.getAllPlanEvents()

  fun getLogsForDate(date: String): Flow<List<HabitTaskLog>> = dao.getLogsForDate(date)

  fun getDayRating(date: String): Flow<DayRating?> = dao.getDayRating(date)

  suspend fun insertTask(
    name: String,
    targetDate: String? = null,
    repeatDaysMask: Int = 0,
    targetTimeMinutes: Int = 0,
    isDefault: Boolean = false,
    isStarred: Boolean = false,
    noteText: String = "",
    noteImageUri: String? = null
  ): Long {
    val task = HabitTask(
      name = name,
      targetDate = targetDate,
      repeatDaysMask = repeatDaysMask,
      targetTimeMinutes = targetTimeMinutes,
      isDefault = isDefault,
      isStarred = isStarred,
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

  // NEET Chapter operations
  suspend fun insertNeetChapter(chapter: NeetChapter): Long = dao.insertNeetChapter(chapter)

  suspend fun insertAllNeetChapters(chapters: List<NeetChapter>) = dao.insertAllNeetChapters(chapters)

  suspend fun updateNeetChapter(chapter: NeetChapter) = dao.updateNeetChapter(chapter)

  suspend fun deleteNeetChapter(chapter: NeetChapter) = dao.deleteNeetChapter(chapter)

  suspend fun deleteNeetChapterById(id: Long) = dao.deleteNeetChapterById(id)

  // NEET Tally Counter operations
  suspend fun insertNeetTallyCounter(counter: NeetTallyCounter): Long = dao.insertNeetTallyCounter(counter)

  suspend fun insertAllNeetTallyCounters(counters: List<NeetTallyCounter>) = dao.insertAllNeetTallyCounters(counters)

  suspend fun updateNeetTallyCounter(counter: NeetTallyCounter) = dao.updateNeetTallyCounter(counter)

  suspend fun deleteNeetTallyCounter(counter: NeetTallyCounter) = dao.deleteNeetTallyCounter(counter)

  suspend fun deleteNeetTallyCounterById(id: Long) = dao.deleteNeetTallyCounterById(id)

  // Plan Event operations
  suspend fun insertPlanEvent(event: com.example.data.model.PlanEvent): Long {
    val eventId = dao.insertPlanEvent(event)
    // Synchronize event task to habit_tasks so it appears seamlessly in tracker view during event dates
    val eventTask = HabitTask(
      name = event.taskTitle.ifBlank { event.title },
      startDate = event.startDate,
      endDate = event.endDate,
      eventId = eventId,
      targetTimeMinutes = event.taskTargetMinutes,
      noteText = if (event.notes.isNotBlank()) "${event.title} - ${event.notes}" else event.title
    )
    dao.insertTask(eventTask)
    return eventId
  }

  suspend fun updatePlanEvent(event: com.example.data.model.PlanEvent) {
    dao.updatePlanEvent(event)
    val existingTask = dao.getTaskByEventId(event.id)
    if (existingTask != null) {
      val updated = existingTask.copy(
        name = event.taskTitle.ifBlank { event.title },
        startDate = event.startDate,
        endDate = event.endDate,
        targetTimeMinutes = event.taskTargetMinutes,
        noteText = if (event.notes.isNotBlank()) "${event.title} - ${event.notes}" else event.title
      )
      dao.updateTask(updated)
    } else {
      val eventTask = HabitTask(
        name = event.taskTitle.ifBlank { event.title },
        startDate = event.startDate,
        endDate = event.endDate,
        eventId = event.id,
        targetTimeMinutes = event.taskTargetMinutes,
        noteText = if (event.notes.isNotBlank()) "${event.title} - ${event.notes}" else event.title
      )
      dao.insertTask(eventTask)
    }
  }

  suspend fun deletePlanEvent(event: com.example.data.model.PlanEvent) {
    dao.deletePlanEvent(event)
    dao.deleteTaskByEventId(event.id)
  }

  suspend fun deletePlanEventById(id: Long) {
    dao.deletePlanEventById(id)
    dao.deleteTaskByEventId(id)
  }

  // Batch insert helpers for import
  suspend fun importData(
    tasks: List<HabitTask>,
    logs: List<HabitTaskLog>,
    ratings: List<DayRating>,
    scores: List<NeetTestScore>,
    plannedTasks: List<PlannedTask>,
    events: List<com.example.data.model.PlanEvent>,
    chapters: List<NeetChapter>,
    counters: List<NeetTallyCounter>
  ) {
    if (tasks.isNotEmpty()) dao.insertAllTasks(tasks)
    if (logs.isNotEmpty()) dao.insertAllLogs(logs)
    if (ratings.isNotEmpty()) dao.insertAllRatings(ratings)
    if (scores.isNotEmpty()) dao.insertAllNeetScores(scores)
    if (plannedTasks.isNotEmpty()) dao.insertAllPlannedTasks(plannedTasks)
    if (events.isNotEmpty()) dao.insertAllPlanEvents(events)
    if (chapters.isNotEmpty()) dao.insertAllNeetChapters(chapters)
    if (counters.isNotEmpty()) dao.insertAllNeetTallyCounters(counters)
  }
}
