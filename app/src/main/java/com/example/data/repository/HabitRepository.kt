package com.example.data.repository

import com.example.data.db.HabitDao
import com.example.data.model.AiChatEntity
import com.example.data.model.DayRating
import com.example.data.model.HabitTask
import com.example.data.model.HabitTaskLog
import com.example.data.model.NeetChapter
import com.example.data.model.NeetTallyCounter
import com.example.data.model.NeetTestScore
import com.example.data.model.NoteEntity
import com.example.data.model.PlanEvent
import com.example.data.model.PlannedTask
import com.example.data.model.RatingType
import com.example.data.model.TaskPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.withLock

class HabitRepository(private val dao: HabitDao) {

  val allTasks: Flow<List<HabitTask>> = dao.getAllTasks()
  val defaultTasks: Flow<List<HabitTask>> = dao.getDefaultTasks()
  val allTaskPresets: Flow<List<TaskPreset>> = dao.getAllTaskPresets()
  val allRatings: Flow<List<DayRating>> = dao.getAllRatings()
  val allLogs: Flow<List<HabitTaskLog>> = dao.getAllLogs()
  val allNeetScores: Flow<List<NeetTestScore>> = dao.getAllNeetScores()
  val allPlannedTasks: Flow<List<PlannedTask>> = dao.getAllPlannedTasks()
  val allNeetChapters: Flow<List<NeetChapter>> = dao.getAllNeetChapters()
  val allNeetTallyCounters: Flow<List<NeetTallyCounter>> = dao.getAllNeetTallyCounters()
  val allPlanEvents: Flow<List<PlanEvent>> = dao.getAllPlanEvents()
  val allNotes: Flow<List<NoteEntity>> = dao.getAllNotes()

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
    noteImageUri: String? = null,
    reminderTime: String? = null
  ): Long {
    val task = HabitTask(
      name = name,
      targetDate = targetDate,
      repeatDaysMask = repeatDaysMask,
      targetTimeMinutes = targetTimeMinutes,
      isDefault = isDefault,
      isStarred = isStarred,
      noteText = noteText,
      noteImageUri = noteImageUri,
      reminderTime = reminderTime
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

  private val logMutex = kotlinx.coroutines.sync.Mutex()

  suspend fun addTimeToTask(taskId: Long, date: String, additionalSeconds: Long) {
    if (additionalSeconds <= 0L) return
    logMutex.withLock {
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
  }

  suspend fun setTimeSpent(taskId: Long, date: String, totalSeconds: Long) {
    logMutex.withLock {
      val existing = dao.getLog(taskId, date)
      if (existing != null) {
        val updated = existing.copy(timeSpentSeconds = totalSeconds)
        dao.insertOrUpdateLog(updated)
      } else {
        val newLog = HabitTaskLog(
          taskId = taskId,
          date = date,
          timeSpentSeconds = totalSeconds,
          isCompleted = false
        )
        dao.insertOrUpdateLog(newLog)
      }
    }
  }

  suspend fun toggleTaskComplete(taskId: Long, date: String) {
    logMutex.withLock {
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
  }

  suspend fun setDayRating(date: String, ratingType: RatingType) {
    val existing = dao.getDayRatingSync(date)
    if (existing != null && existing.rating == ratingType.name) {
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
  suspend fun insertNeetChapter(chapter: NeetChapter): Long {
    val id = dao.insertNeetChapter(chapter)
    if (chapter.notes.isNotBlank()) {
      syncChapterNotesFieldToNoteEntity(chapter.copy(id = id))
    }
    return id
  }

  suspend fun insertAllNeetChapters(chapters: List<NeetChapter>) = dao.insertAllNeetChapters(chapters)

  suspend fun updateNeetChapter(chapter: NeetChapter) {
    val previous = dao.getNeetChapterByIdSync(chapter.id)
    dao.updateNeetChapter(chapter)
    if (previous != null && previous.notes != chapter.notes) {
      syncChapterNotesFieldToNoteEntity(chapter)
    }
  }

  suspend fun deleteNeetChapter(chapter: NeetChapter) = dao.deleteNeetChapter(chapter)

  suspend fun deleteNeetChapterById(id: Long) = dao.deleteNeetChapterById(id)

  // Notes + Two-Way Chapter Sync
  suspend fun saveNoteWithChapterSync(note: NoteEntity): Long {
    val now = System.currentTimeMillis()
    val linkedChapter = when {
      note.chapterId != null -> dao.getNeetChapterByIdSync(note.chapterId)
      note.subject.isNotBlank() && note.chapterName.isNotBlank() ->
        dao.getNeetChapterByNameSync(note.subject.trim(), note.chapterName.trim())
      else -> null
    }

    val normalized = note.copy(
      title = note.title.trim(),
      subject = linkedChapter?.subject ?: note.subject.trim(),
      chapterName = linkedChapter?.name ?: note.chapterName.trim(),
      chapterId = linkedChapter?.id ?: note.chapterId,
      createdAt = if (note.id == 0L) now else note.createdAt,
      updatedAt = now
    )

    val savedId = if (normalized.id == 0L) {
      dao.insertNote(normalized)
    } else {
      dao.updateNote(normalized)
      normalized.id
    }

    if (linkedChapter != null) {
      refreshChapterNotesSummary(linkedChapter)
    }
    return savedId
  }

  suspend fun deleteNoteWithChapterSync(note: NoteEntity) {
    val linkedChapter = when {
      note.chapterId != null -> dao.getNeetChapterByIdSync(note.chapterId)
      note.subject.isNotBlank() && note.chapterName.isNotBlank() ->
        dao.getNeetChapterByNameSync(note.subject, note.chapterName)
      else -> null
    }
    dao.deleteNote(note)
    if (linkedChapter != null) {
      refreshChapterNotesSummary(linkedChapter)
    }
  }

  suspend fun syncLegacyChapterNotesToNotesTable() {
    val chapters = dao.getAllNeetChaptersSync()
    for (chapter in chapters) {
      if (chapter.notes.isNotBlank()) {
        val existingNotes = dao.getNotesForChapterSync(chapter.id, chapter.subject, chapter.name)
        if (existingNotes.isEmpty()) {
          dao.insertNote(
            NoteEntity(
              title = "${chapter.name} Notes",
              content = chapter.notes,
              subject = chapter.subject,
              chapterName = chapter.name,
              chapterId = chapter.id,
              createdAt = System.currentTimeMillis(),
              updatedAt = System.currentTimeMillis()
            )
          )
        }
      }
    }
  }

  private suspend fun syncChapterNotesFieldToNoteEntity(chapter: NeetChapter) {
    val existingNotes = dao.getNotesForChapterSync(chapter.id, chapter.subject, chapter.name)
    val now = System.currentTimeMillis()
    if (existingNotes.isEmpty()) {
      if (chapter.notes.isNotBlank()) {
        dao.insertNote(
          NoteEntity(
            title = "${chapter.name} Notes",
            content = chapter.notes,
            subject = chapter.subject,
            chapterName = chapter.name,
            chapterId = chapter.id,
            createdAt = now,
            updatedAt = now
          )
        )
      }
    } else if (existingNotes.size == 1 && chapter.notes.isNotBlank()) {
      dao.updateNote(
        existingNotes.first().copy(
          content = chapter.notes,
          chapterId = chapter.id,
          updatedAt = now
        )
      )
    }
  }

  private suspend fun refreshChapterNotesSummary(chapter: NeetChapter) {
    val notes = dao.getNotesForChapterSync(chapter.id, chapter.subject, chapter.name)
    val summary = notes.joinToString("\n") { n ->
      val body = n.content.trim()
      val title = n.title.trim()
      val attachments = n.getAttachments()
      val mediaTag = if (attachments.isNotEmpty()) {
        val imgs = attachments.count { it.type == NoteEntity.TYPE_IMAGE }
        val pdfs = attachments.count { it.type == NoteEntity.TYPE_PDF }
        listOfNotNull(
          if (imgs > 0) "${imgs} img" else null,
          if (pdfs > 0) "${pdfs} PDF" else null
        ).joinToString(", ")
      } else ""
      when {
        body.isNotBlank() -> body
        title.isNotBlank() && mediaTag.isNotBlank() -> "$title ($mediaTag)"
        title.isNotBlank() -> title
        mediaTag.isNotBlank() -> "Attachments: $mediaTag"
        else -> ""
      }
    }.trim()
    if (summary != chapter.notes) {
      dao.updateNeetChapter(chapter.copy(notes = summary))
    }
  }

  // NEET Tally Counter operations
  suspend fun insertNeetTallyCounter(counter: NeetTallyCounter): Long = dao.insertNeetTallyCounter(counter)

  suspend fun insertAllNeetTallyCounters(counters: List<NeetTallyCounter>) = dao.insertAllNeetTallyCounters(counters)

  suspend fun updateNeetTallyCounter(counter: NeetTallyCounter) = dao.updateNeetTallyCounter(counter)

  suspend fun deleteNeetTallyCounter(counter: NeetTallyCounter) = dao.deleteNeetTallyCounter(counter)

  suspend fun deleteNeetTallyCounterById(id: Long) = dao.deleteNeetTallyCounterById(id)

  // Plan Event operations
  suspend fun insertPlanEvent(event: PlanEvent): Long {
    val eventId = dao.insertPlanEvent(event)
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

  suspend fun updatePlanEvent(event: PlanEvent) {
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

  suspend fun deletePlanEvent(event: PlanEvent) {
    dao.deletePlanEvent(event)
    dao.deleteTaskByEventId(event.id)
  }

  suspend fun deletePlanEventById(id: Long) {
    dao.deletePlanEventById(id)
    dao.deleteTaskByEventId(id)
  }

  // Task Presets operations
  suspend fun insertTaskPreset(
    name: String,
    targetTimeMinutes: Int = 0,
    noteText: String = "",
    noteImageUri: String? = null
  ): Long {
    val preset = TaskPreset(
      name = name.trim(),
      targetTimeMinutes = targetTimeMinutes,
      noteText = noteText.trim(),
      noteImageUri = noteImageUri
    )
    return dao.insertTaskPreset(preset)
  }

  suspend fun insertAllTaskPresets(presets: List<TaskPreset>) = dao.insertAllTaskPresets(presets)

  suspend fun updateTaskPreset(preset: TaskPreset) = dao.updateTaskPreset(preset)

  suspend fun deleteTaskPreset(preset: TaskPreset) = dao.deleteTaskPreset(preset)

  suspend fun deleteTaskPresetById(id: Long) = dao.deleteTaskPresetById(id)

  // Batch insert helpers for import
  suspend fun importData(
    tasks: List<HabitTask>,
    logs: List<HabitTaskLog>,
    ratings: List<DayRating>,
    scores: List<NeetTestScore>,
    plannedTasks: List<PlannedTask>,
    events: List<PlanEvent>,
    chapters: List<NeetChapter>,
    counters: List<NeetTallyCounter>,
    presets: List<TaskPreset> = emptyList(),
    notes: List<NoteEntity> = emptyList(),
    aiChats: List<AiChatEntity> = emptyList()
  ) {
    if (tasks.isNotEmpty()) dao.insertAllTasks(tasks)
    if (logs.isNotEmpty()) dao.insertAllLogs(logs)
    if (ratings.isNotEmpty()) dao.insertAllRatings(ratings)
    if (scores.isNotEmpty()) dao.insertAllNeetScores(scores)
    if (plannedTasks.isNotEmpty()) dao.insertAllPlannedTasks(plannedTasks)
    if (events.isNotEmpty()) dao.insertAllPlanEvents(events)
    if (chapters.isNotEmpty()) dao.insertAllNeetChapters(chapters)
    if (counters.isNotEmpty()) dao.insertAllNeetTallyCounters(counters)
    if (presets.isNotEmpty()) dao.insertAllTaskPresets(presets)
    if (notes.isNotEmpty()) dao.insertAllNotes(notes)
    if (aiChats.isNotEmpty()) dao.insertAllAiChats(aiChats)
  }

  // AI Chat History
  val allAiChats: Flow<List<AiChatEntity>> = dao.getAllAiChats()

  suspend fun insertAiChat(chat: AiChatEntity): Long = dao.insertAiChat(chat)

  suspend fun deleteAiChatById(id: Long) = dao.deleteAiChatById(id)

  suspend fun deleteAiChatsByIds(ids: List<Long>) = dao.deleteAiChatsByIds(ids)

  suspend fun setAiChatPinned(id: Long, isPinned: Boolean) = dao.setAiChatPinned(id, isPinned)

  suspend fun clearAiChatHistory() = dao.clearAiChatHistory()

  suspend fun resetAllData() {
    dao.clearAllTasks()
    dao.clearAllLogs()
    dao.clearAllRatings()
    dao.clearAllScores()
    dao.clearAllPlannedTasks()
    dao.clearAllPlanEvents()
    dao.clearAllNotes()
  }
}
