package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DayRating
import com.example.data.model.HabitTask
import com.example.data.model.HabitTaskLog
import com.example.data.model.NeetChapter
import com.example.data.model.NeetTallyCounter
import com.example.data.model.NeetTestScore
import com.example.data.model.PlanEvent
import com.example.data.model.PlannedTask
import com.example.data.model.TaskPreset
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
  // Tasks
  @Query("SELECT * FROM habit_tasks WHERE isArchived = 0 ORDER BY id ASC")
  fun getAllTasks(): Flow<List<HabitTask>>

  @Query("SELECT * FROM habit_tasks WHERE isDefault = 1 AND isArchived = 0 ORDER BY id ASC")
  fun getDefaultTasks(): Flow<List<HabitTask>>

  @Query("SELECT * FROM habit_tasks WHERE id = :id LIMIT 1")
  suspend fun getTaskById(id: Long): HabitTask?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTask(task: HabitTask): Long

  @Update
  suspend fun updateTask(task: HabitTask)

  @Delete
  suspend fun deleteTask(task: HabitTask)

  @Query("DELETE FROM habit_tasks WHERE id = :id")
  suspend fun deleteTaskById(id: Long)

  // Logs
  @Query("SELECT * FROM habit_task_logs WHERE date = :date")
  fun getLogsForDate(date: String): Flow<List<HabitTaskLog>>

  @Query("SELECT * FROM habit_task_logs WHERE taskId = :taskId AND date = :date LIMIT 1")
  suspend fun getLog(taskId: Long, date: String): HabitTaskLog?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateLog(log: HabitTaskLog): Long

  @Query("SELECT * FROM habit_task_logs")
  fun getAllLogs(): Flow<List<HabitTaskLog>>

  @Query("SELECT * FROM habit_task_logs WHERE date BETWEEN :startDate AND :endDate")
  fun getLogsBetweenDates(startDate: String, endDate: String): Flow<List<HabitTaskLog>>

  // Day Ratings
  @Query("SELECT * FROM day_ratings WHERE date = :date LIMIT 1")
  fun getDayRating(date: String): Flow<DayRating?>

  @Query("SELECT * FROM day_ratings WHERE date = :date LIMIT 1")
  suspend fun getDayRatingSync(date: String): DayRating?

  @Query("SELECT * FROM day_ratings")
  fun getAllRatings(): Flow<List<DayRating>>

  @Query("SELECT * FROM day_ratings WHERE date BETWEEN :startDate AND :endDate")
  fun getRatingsBetweenDates(startDate: String, endDate: String): Flow<List<DayRating>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateRating(dayRating: DayRating)

  @Query("DELETE FROM day_ratings WHERE date = :date")
  suspend fun deleteRating(date: String)

  // NEET Test Scores
  @Query("SELECT * FROM neet_test_scores ORDER BY date DESC, id DESC")
  fun getAllNeetScores(): Flow<List<NeetTestScore>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNeetScore(score: NeetTestScore): Long

  @Delete
  suspend fun deleteNeetScore(score: NeetTestScore)

  // Planned Tasks
  @Query("SELECT * FROM planned_tasks ORDER BY date ASC, id ASC")
  fun getAllPlannedTasks(): Flow<List<PlannedTask>>

  @Query("SELECT * FROM planned_tasks WHERE date >= :fromDate ORDER BY date ASC, id ASC")
  fun getUpcomingPlannedTasks(fromDate: String): Flow<List<PlannedTask>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPlannedTask(task: PlannedTask): Long

  @Update
  suspend fun updatePlannedTask(task: PlannedTask)

  @Delete
  suspend fun deletePlannedTask(task: PlannedTask)

  @Query("DELETE FROM planned_tasks WHERE id = :id")
  suspend fun deletePlannedTaskById(id: Long)

  // NEET Chapters
  @Query("SELECT * FROM neet_chapters ORDER BY id ASC")
  fun getAllNeetChapters(): Flow<List<NeetChapter>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNeetChapter(chapter: NeetChapter): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllNeetChapters(chapters: List<NeetChapter>)

  @Update
  suspend fun updateNeetChapter(chapter: NeetChapter)

  @Delete
  suspend fun deleteNeetChapter(chapter: NeetChapter)

  @Query("DELETE FROM neet_chapters WHERE id = :id")
  suspend fun deleteNeetChapterById(id: Long)

  // NEET Tally Counters
  @Query("SELECT * FROM neet_tally_counters ORDER BY id ASC")
  fun getAllNeetTallyCounters(): Flow<List<NeetTallyCounter>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNeetTallyCounter(counter: NeetTallyCounter): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllNeetTallyCounters(counters: List<NeetTallyCounter>)

  @Update
  suspend fun updateNeetTallyCounter(counter: NeetTallyCounter)

  @Delete
  suspend fun deleteNeetTallyCounter(counter: NeetTallyCounter)

  @Query("DELETE FROM neet_tally_counters WHERE id = :id")
  suspend fun deleteNeetTallyCounterById(id: Long)

  // Plan Events
  @Query("SELECT * FROM plan_events ORDER BY startDate ASC, id ASC")
  fun getAllPlanEvents(): Flow<List<com.example.data.model.PlanEvent>>

  @Query("SELECT * FROM plan_events WHERE id = :id LIMIT 1")
  suspend fun getPlanEventById(id: Long): com.example.data.model.PlanEvent?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPlanEvent(event: com.example.data.model.PlanEvent): Long

  @Update
  suspend fun updatePlanEvent(event: com.example.data.model.PlanEvent)

  @Delete
  suspend fun deletePlanEvent(event: com.example.data.model.PlanEvent)

  @Query("DELETE FROM plan_events WHERE id = :id")
  suspend fun deletePlanEventById(id: Long)

  @Query("SELECT * FROM habit_tasks WHERE eventId = :eventId LIMIT 1")
  suspend fun getTaskByEventId(eventId: Long): HabitTask?

  @Query("DELETE FROM habit_tasks WHERE eventId = :eventId")
  suspend fun deleteTaskByEventId(eventId: Long)

  // Batch insert helpers for Data Import
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllTasks(tasks: List<HabitTask>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllLogs(logs: List<HabitTaskLog>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllRatings(ratings: List<DayRating>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllNeetScores(scores: List<NeetTestScore>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllPlannedTasks(tasks: List<PlannedTask>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllPlanEvents(events: List<com.example.data.model.PlanEvent>)

  // Task Presets (independent storage, not added as active tasks until chosen)
  @Query("SELECT * FROM task_presets ORDER BY id ASC")
  fun getAllTaskPresets(): Flow<List<TaskPreset>>

  @Query("SELECT * FROM task_presets WHERE id = :id LIMIT 1")
  suspend fun getTaskPresetById(id: Long): TaskPreset?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTaskPreset(preset: TaskPreset): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllTaskPresets(presets: List<TaskPreset>)

  @Update
  suspend fun updateTaskPreset(preset: TaskPreset)

  @Delete
  suspend fun deleteTaskPreset(preset: TaskPreset)

  @Query("DELETE FROM task_presets WHERE id = :id")
  suspend fun deleteTaskPresetById(id: Long)

  // AI Chat History
  @Query("SELECT * FROM ai_chat_history ORDER BY timestamp DESC, id DESC")
  fun getAllAiChats(): Flow<List<com.example.data.model.AiChatEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAiChat(chat: com.example.data.model.AiChatEntity): Long

  @Query("DELETE FROM ai_chat_history")
  suspend fun clearAiChatHistory()
}
