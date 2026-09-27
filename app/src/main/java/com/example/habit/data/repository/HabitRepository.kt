package com.example.habit.data.repository

import com.example.habit.data.db.HabitDao
import com.example.habit.data.model.*
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val dao: HabitDao) {
    val allTasks: Flow<List<HabitTask>> = dao.getAllTasks()
    val allLogs: Flow<List<HabitTaskLog>> = dao.getAllLogs()
    val allPlannedTasks: Flow<List<PlannedTask>> = dao.getAllPlannedTasks()
    val allPlanEvents: Flow<List<PlanEvent>> = dao.getAllPlanEvents()
    val allNeetChapters: Flow<List<NeetChapter>> = dao.getAllNeetChapters()
    val allNeetScores: Flow<List<NeetTestScore>> = dao.getAllNeetScores()
    val allTallyCounters: Flow<List<NeetTallyCounter>> = dao.getAllTallyCounters()
    val allTaskPresets: Flow<List<TaskPreset>> = dao.getAllTaskPresets()

    fun getLogsForDate(date: String): Flow<List<HabitTaskLog>> = dao.getLogsForDate(date)
    fun getRatingForDate(date: String): Flow<DayRating?> = dao.getRatingForDate(date)

    suspend fun insertTask(
        name: String,
        targetDate: String,
        repeatDaysMask: Int,
        targetTimeMinutes: Int,
        isDefault: Boolean,
        isStarred: Boolean,
        noteText: String
    ): Long {
        return dao.insertTask(
            HabitTask(
                name = name,
                targetDate = targetDate,
                repeatDaysMask = repeatDaysMask,
                targetTimeMinutes = targetTimeMinutes,
                isDefault = isDefault,
                isStarred = isStarred,
                noteText = noteText
            )
        )
    }

    suspend fun updateTask(task: HabitTask) = dao.updateTask(task)
    suspend fun deleteTask(task: HabitTask) = dao.deleteTask(task)

    suspend fun setTaskCompleted(taskId: Long, date: String, completed: Boolean) {
        // find or insert log
        // Simplified for robust compilation
        dao.insertLog(HabitTaskLog(taskId = taskId, date = date, isCompleted = completed))
    }

    suspend fun setTimeSpent(taskId: Long, date: String, seconds: Long) {
        dao.insertLog(HabitTaskLog(taskId = taskId, date = date, timeSpentSeconds = seconds))
    }

    suspend fun setDayRating(date: String, rating: RatingType, note: String) {
        dao.insertRating(DayRating(date = date, rating = rating, note = note))
    }

    suspend fun insertPlannedTask(task: PlannedTask) = dao.insertPlannedTask(task)
    suspend fun updatePlannedTask(task: PlannedTask) = dao.updatePlannedTask(task)
    suspend fun deletePlannedTask(id: Long) = dao.deletePlannedTask(id)

    suspend fun insertPlanEvent(event: PlanEvent) = dao.insertPlanEvent(event)
    suspend fun updatePlanEvent(event: PlanEvent) = dao.updatePlanEvent(event)
    suspend fun deletePlanEvent(event: PlanEvent) = dao.deletePlanEvent(event)
    suspend fun deletePlanEventById(id: Long) = dao.deletePlanEventById(id)

    suspend fun insertNeetChapter(chapter: NeetChapter) = dao.insertNeetChapter(chapter)
    suspend fun updateNeetChapter(chapter: NeetChapter) = dao.updateNeetChapter(chapter)
    suspend fun deleteNeetChapter(chapter: NeetChapter) = dao.deleteNeetChapter(chapter)

    suspend fun insertNeetScore(score: NeetTestScore) = dao.insertNeetScore(score)
    suspend fun deleteNeetScore(score: NeetTestScore) = dao.deleteNeetScore(score)

    suspend fun insertTallyCounter(counter: NeetTallyCounter) = dao.insertTallyCounter(counter)
    suspend fun updateTallyCounter(counter: NeetTallyCounter) = dao.updateTallyCounter(counter)
    suspend fun deleteTallyCounter(counter: NeetTallyCounter) = dao.deleteTallyCounter(counter)

    suspend fun insertTaskPreset(preset: TaskPreset) = dao.insertTaskPreset(preset)
    suspend fun deleteTaskPreset(preset: TaskPreset) = dao.deleteTaskPreset(preset)
}
