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
import com.example.data.model.NeetTestScore
import com.example.data.model.PlannedTask
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
}
