package com.example.habit.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.habit.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habit_tasks")
    fun getAllTasks(): Flow<List<HabitTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: HabitTask): Long

    @Update
    suspend fun updateTask(task: HabitTask)

    @Delete
    suspend fun deleteTask(task: HabitTask)

    @Query("SELECT * FROM habit_task_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<HabitTaskLog>>

    @Query("SELECT * FROM habit_task_logs")
    fun getAllLogs(): Flow<List<HabitTaskLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitTaskLog): Long

    @Update
    suspend fun updateLog(log: HabitTaskLog)

    @Query("SELECT * FROM day_ratings WHERE date = :date")
    fun getRatingForDate(date: String): Flow<DayRating?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: DayRating)

    @Query("SELECT * FROM planned_tasks")
    fun getAllPlannedTasks(): Flow<List<PlannedTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedTask(task: PlannedTask): Long

    @Update
    suspend fun updatePlannedTask(task: PlannedTask)

    @Query("DELETE FROM planned_tasks WHERE id = :id")
    suspend fun deletePlannedTask(id: Long)

    @Query("SELECT * FROM plan_events")
    fun getAllPlanEvents(): Flow<List<PlanEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanEvent(event: PlanEvent): Long

    @Update
    suspend fun updatePlanEvent(event: PlanEvent)

    @Delete
    suspend fun deletePlanEvent(event: PlanEvent)

    @Query("DELETE FROM plan_events WHERE id = :id")
    suspend fun deletePlanEventById(id: Long)

    @Query("SELECT * FROM neet_chapters")
    fun getAllNeetChapters(): Flow<List<NeetChapter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNeetChapter(chapter: NeetChapter): Long

    @Update
    suspend fun updateNeetChapter(chapter: NeetChapter)

    @Delete
    suspend fun deleteNeetChapter(chapter: NeetChapter)

    @Query("SELECT * FROM neet_test_scores")
    fun getAllNeetScores(): Flow<List<NeetTestScore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNeetScore(score: NeetTestScore): Long

    @Delete
    suspend fun deleteNeetScore(score: NeetTestScore)

    @Query("SELECT * FROM neet_tally_counters")
    fun getAllTallyCounters(): Flow<List<NeetTallyCounter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTallyCounter(counter: NeetTallyCounter): Long

    @Update
    suspend fun updateTallyCounter(counter: NeetTallyCounter)

    @Delete
    suspend fun deleteTallyCounter(counter: NeetTallyCounter)

    @Query("SELECT * FROM task_presets")
    fun getAllTaskPresets(): Flow<List<TaskPreset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskPreset(preset: TaskPreset): Long

    @Delete
    suspend fun deleteTaskPreset(preset: TaskPreset)
}
