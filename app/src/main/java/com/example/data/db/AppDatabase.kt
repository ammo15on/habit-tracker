package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AiChatEntity
import com.example.data.model.DayRating
import com.example.data.model.HabitTask
import com.example.data.model.HabitTaskLog
import com.example.data.model.NeetChapter
import com.example.data.model.NeetTallyCounter
import com.example.data.model.NeetTestScore
import com.example.data.model.PlanEvent
import com.example.data.model.PlannedTask
import com.example.data.model.TaskPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
  version = 14,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun habitDao(): HabitDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    private fun ensureAllTablesAndColumns(db: SupportSQLiteDatabase) {
      db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS habit_tasks (
          id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          name TEXT NOT NULL,
          targetDate TEXT,
          repeatDaysMask INTEGER NOT NULL DEFAULT 0,
          targetTimeMinutes INTEGER NOT NULL DEFAULT 0,
          isDefault INTEGER NOT NULL DEFAULT 0,
          isStarred INTEGER NOT NULL DEFAULT 0,
          targetDates TEXT,
          startDate TEXT,
          endDate TEXT,
          noteText TEXT NOT NULL DEFAULT '',
          noteImageUri TEXT,
          reminderTime TEXT,
          isArchived INTEGER NOT NULL DEFAULT 0,
          eventId INTEGER
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
          FOREIGN KEY(taskId) REFERENCES habit_tasks(id) ON DELETE CASCADE
        )
        """.trimIndent()
      )
      db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_habit_task_logs_taskId_date ON habit_task_logs(taskId, date)")
      db.execSQL("CREATE INDEX IF NOT EXISTS index_habit_task_logs_date ON habit_task_logs(date)")

      db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS day_ratings (
          date TEXT PRIMARY KEY NOT NULL,
          rating TEXT NOT NULL
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
          maxPhysics INTEGER NOT NULL DEFAULT 180,
          maxChemistry INTEGER NOT NULL DEFAULT 180,
          maxBotany INTEGER NOT NULL DEFAULT 180,
          maxZoology INTEGER NOT NULL DEFAULT 180,
          timestamp INTEGER NOT NULL DEFAULT 0
        )
        """.trimIndent()
      )

      db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS planned_tasks (
          id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          title TEXT NOT NULL,
          date TEXT NOT NULL,
          targetTimeMinutes INTEGER NOT NULL DEFAULT 0,
          notes TEXT NOT NULL DEFAULT '',
          isStarred INTEGER NOT NULL DEFAULT 0,
          isArchived INTEGER NOT NULL DEFAULT 0,
          isCompleted INTEGER NOT NULL DEFAULT 0
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
          taskTitle TEXT NOT NULL DEFAULT '',
          taskTargetMinutes INTEGER NOT NULL DEFAULT 0,
          notes TEXT NOT NULL DEFAULT '',
          subtasksJson TEXT NOT NULL DEFAULT '[]'
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
          target INTEGER NOT NULL DEFAULT 0,
          unit TEXT NOT NULL DEFAULT 'times'
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
          timestamp INTEGER NOT NULL DEFAULT 0,
          isError INTEGER NOT NULL DEFAULT 0,
          isCloud INTEGER NOT NULL DEFAULT 0,
          isPinned INTEGER NOT NULL DEFAULT 0
        )
        """.trimIndent()
      )
    }

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "habit_tracker.db"
        )
          .fallbackToDestructiveMigration()
          .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
              super.onCreate(db)
              ensureAllTablesAndColumns(db)
              CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                  val dao = database.habitDao()
                  dao.insertAllTaskPresets(TaskPreset.DEFAULT_PRESETS.map { TaskPreset(name = it) })
                  dao.insertAllNeetChapters(NeetChapter.DEFAULT_CHAPTERS)
                  dao.insertAllNeetTallyCounters(NeetTallyCounter.DEFAULT_COUNTERS)
                }
              }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
              super.onOpen(db)
              ensureAllTablesAndColumns(db)
            }
          })
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
