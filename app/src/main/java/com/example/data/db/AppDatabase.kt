package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.DayRating
import com.example.data.model.HabitTask
import com.example.data.model.HabitTaskLog
import com.example.data.model.NeetChapter
import com.example.data.model.NeetTallyCounter
import com.example.data.model.NeetTestScore
import com.example.data.model.PlanEvent
import com.example.data.model.PlannedTask
import com.example.data.model.TaskPreset

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AiChatEntity

@Database(
  entities = [
    HabitTask::class,
    HabitTaskLog::class,
    DayRating::class,
    NeetTestScore::class,
    PlannedTask::class,
    PlanEvent::class,
    NeetChapter::class,
    NeetTallyCounter::class,
    TaskPreset::class,
    AiChatEntity::class
  ],
  version = 11,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun habitDao(): HabitDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    private fun ensureAllTablesAndColumns(db: SupportSQLiteDatabase) {
      // 1. Ensure all tables exist without wiping existing records
      db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS habit_tasks (
          id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          name TEXT NOT NULL,
          colorHex TEXT NOT NULL DEFAULT '#3B82F6',
          targetTimeMinutes INTEGER NOT NULL DEFAULT 0,
          isCompletedToday INTEGER NOT NULL DEFAULT 0,
          streakCount INTEGER NOT NULL DEFAULT 0,
          totalTimeSpentSeconds INTEGER NOT NULL DEFAULT 0,
          isDefault INTEGER NOT NULL DEFAULT 1,
          isArchived INTEGER NOT NULL DEFAULT 0,
          startDate TEXT,
          endDate TEXT,
          eventId INTEGER,
          reminderTime TEXT,
          noteText TEXT,
          noteImageUri TEXT
        )
        """.trimIndent()
      )

      db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS habit_task_logs (
          id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          taskId INTEGER NOT NULL,
          date TEXT NOT NULL,
          timeSpentSeconds INTEGER NOT NULL DEFAULT 0,
          isCompleted INTEGER NOT NULL DEFAULT 0,
          targetTimeMinutes INTEGER NOT NULL DEFAULT 0,
          FOREIGN KEY(taskId) REFERENCES habit_tasks(id) ON DELETE CASCADE
        )
        """.trimIndent()
      )

      db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS day_ratings (
          date TEXT PRIMARY KEY NOT NULL,
          rating TEXT,
          totalTimeSeconds INTEGER NOT NULL DEFAULT 0,
          completedTasksCount INTEGER NOT NULL DEFAULT 0,
          totalTasksCount INTEGER NOT NULL DEFAULT 0
        )
        """.trimIndent()
      )

      db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS neet_test_scores (
          id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          testName TEXT NOT NULL,
          date TEXT NOT NULL,
          physicsScore INTEGER NOT NULL DEFAULT 0,
          chemistryScore INTEGER NOT NULL DEFAULT 0,
          botanyScore INTEGER NOT NULL DEFAULT 0,
          zoologyScore INTEGER NOT NULL DEFAULT 0,
          totalScore INTEGER NOT NULL DEFAULT 0,
          notes TEXT NOT NULL DEFAULT ''
        )
        """.trimIndent()
      )

      db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS planned_tasks (
          id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          name TEXT NOT NULL,
          date TEXT NOT NULL,
          targetMinutes INTEGER NOT NULL DEFAULT 0,
          isCompleted INTEGER NOT NULL DEFAULT 0,
          noteText TEXT NOT NULL DEFAULT '',
          category TEXT NOT NULL DEFAULT 'General'
        )
        """.trimIndent()
      )

      db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS plan_events (
          id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          title TEXT NOT NULL,
          startDate TEXT NOT NULL,
          endDate TEXT NOT NULL,
          colorHex TEXT NOT NULL DEFAULT '#3B82F6',
          taskTitle TEXT NOT NULL DEFAULT '',
          taskTargetMinutes INTEGER NOT NULL DEFAULT 0,
          notes TEXT NOT NULL DEFAULT ''
        )
        """.trimIndent()
      )

      db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS neet_chapters (
          id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          name TEXT NOT NULL,
          subject TEXT NOT NULL,
          isCompleted INTEGER NOT NULL DEFAULT 0,
          isPyqDone INTEGER NOT NULL DEFAULT 0,
          isRevisionDone INTEGER NOT NULL DEFAULT 0,
          isExerciseDone INTEGER NOT NULL DEFAULT 0,
          isArDone INTEGER NOT NULL DEFAULT 0,
          notes TEXT NOT NULL DEFAULT ''
        )
        """.trimIndent()
      )

      db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS neet_tally_counters (
          id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          title TEXT NOT NULL,
          count INTEGER NOT NULL DEFAULT 0,
          target INTEGER NOT NULL DEFAULT 100,
          unit TEXT NOT NULL DEFAULT 'Questions',
          notes TEXT NOT NULL DEFAULT ''
        )
        """.trimIndent()
      )

      db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS task_presets (
          id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          name TEXT NOT NULL,
          targetTimeMinutes INTEGER NOT NULL DEFAULT 0,
          noteText TEXT NOT NULL DEFAULT '',
          noteImageUri TEXT
        )
        """.trimIndent()
      )

      db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS ai_chat_history (
          id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          role TEXT NOT NULL,
          text TEXT NOT NULL,
          timestamp INTEGER NOT NULL,
          isError INTEGER NOT NULL DEFAULT 0,
          isCloud INTEGER NOT NULL DEFAULT 0,
          isPinned INTEGER NOT NULL DEFAULT 0
        )
        """.trimIndent()
      )

      // 2. Safely add any new columns to pre-existing tables if upgrading from older versions
      val alterCommands = listOf(
        "ALTER TABLE neet_chapters ADD COLUMN isExerciseDone INTEGER NOT NULL DEFAULT 0",
        "ALTER TABLE neet_chapters ADD COLUMN isArDone INTEGER NOT NULL DEFAULT 0",
        "ALTER TABLE ai_chat_history ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0",
        "ALTER TABLE ai_chat_history ADD COLUMN isCloud INTEGER NOT NULL DEFAULT 0",
        "ALTER TABLE habit_tasks ADD COLUMN noteText TEXT",
        "ALTER TABLE habit_tasks ADD COLUMN noteImageUri TEXT",
        "ALTER TABLE habit_tasks ADD COLUMN reminderTime TEXT",
        "ALTER TABLE habit_tasks ADD COLUMN startDate TEXT",
        "ALTER TABLE habit_tasks ADD COLUMN endDate TEXT",
        "ALTER TABLE habit_tasks ADD COLUMN eventId INTEGER"
      )
      for (cmd in alterCommands) {
        try {
          db.execSQL(cmd)
        } catch (_: Exception) {}
      }
    }

    val MIGRATION_10_11 = object : Migration(10, 11) {
      override fun migrate(db: SupportSQLiteDatabase) {
        ensureAllTablesAndColumns(db)
      }
    }

    val MIGRATION_9_10 = object : Migration(9, 10) {
      override fun migrate(db: SupportSQLiteDatabase) {
        ensureAllTablesAndColumns(db)
      }
    }

    private fun createMigration(from: Int, to: Int): Migration = object : Migration(from, to) {
      override fun migrate(db: SupportSQLiteDatabase) {
        ensureAllTablesAndColumns(db)
      }
    }

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val migrations = (1..9).map { fromVersion ->
          createMigration(fromVersion, 11)
        }.toTypedArray()

        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "habit_tracker.db"
        )
          .addMigrations(MIGRATION_9_10, MIGRATION_10_11, *migrations)
          .fallbackToDestructiveMigrationOnDowngrade()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
