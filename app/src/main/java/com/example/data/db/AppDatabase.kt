package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DayRating
import com.example.data.model.HabitTask
import com.example.data.model.HabitTaskLog
import com.example.data.model.NeetChapter
import com.example.data.model.NeetTallyCounter
import com.example.data.model.NeetTestScore
import com.example.data.model.PlanEvent
import com.example.data.model.PlannedTask
import com.example.data.model.TaskPreset
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
  version = 14,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun habitDao(): HabitDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    private fun migrateAll(db: SupportSQLiteDatabase) {
      // 1. Ensure all tables are created if missing with their exact schemas
      db.execSQL("CREATE TABLE IF NOT EXISTS `habit_tasks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `targetDate` TEXT, `repeatDaysMask` INTEGER NOT NULL, `targetTimeMinutes` INTEGER NOT NULL, `isDefault` INTEGER NOT NULL, `isStarred` INTEGER NOT NULL, `targetDates` TEXT, `startDate` TEXT, `endDate` TEXT, `noteText` TEXT NOT NULL, `noteImageUri` TEXT, `reminderTime` TEXT, `isArchived` INTEGER NOT NULL, `eventId` INTEGER)")
      db.execSQL("CREATE TABLE IF NOT EXISTS `habit_task_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `taskId` INTEGER NOT NULL, `date` TEXT NOT NULL, `timeSpentSeconds` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, FOREIGN KEY(`taskId`) REFERENCES `habit_tasks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
      db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_habit_task_logs_taskId_date` ON `habit_task_logs` (`taskId`, `date`)")
      db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_task_logs_date` ON `habit_task_logs` (`date`)")
      db.execSQL("CREATE TABLE IF NOT EXISTS `day_ratings` (`date` TEXT NOT NULL, `rating` TEXT NOT NULL, PRIMARY KEY(`date`))")
      db.execSQL("CREATE TABLE IF NOT EXISTS `neet_test_scores` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `testName` TEXT NOT NULL, `date` TEXT NOT NULL, `physicsScore` INTEGER NOT NULL, `chemistryScore` INTEGER NOT NULL, `botanyScore` INTEGER NOT NULL, `zoologyScore` INTEGER NOT NULL, `maxPhysics` INTEGER NOT NULL, `maxChemistry` INTEGER NOT NULL, `maxBotany` INTEGER NOT NULL, `maxZoology` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)")
      db.execSQL("CREATE TABLE IF NOT EXISTS `planned_tasks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `date` TEXT NOT NULL, `targetTimeMinutes` INTEGER NOT NULL, `notes` TEXT NOT NULL, `isStarred` INTEGER NOT NULL, `isArchived` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL)")
      db.execSQL("CREATE TABLE IF NOT EXISTS `plan_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `startDate` TEXT NOT NULL, `endDate` TEXT NOT NULL, `taskTitle` TEXT NOT NULL, `taskTargetMinutes` INTEGER NOT NULL, `notes` TEXT NOT NULL, `subtasksJson` TEXT NOT NULL)")
      db.execSQL("CREATE TABLE IF NOT EXISTS `neet_chapters` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `subject` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL, `isPyqDone` INTEGER NOT NULL, `isRevisionDone` INTEGER NOT NULL, `isExerciseDone` INTEGER NOT NULL DEFAULT 0, `isArDone` INTEGER NOT NULL DEFAULT 0, `ncertReadCount` INTEGER NOT NULL DEFAULT 0, `notes` TEXT NOT NULL, `customFlags` TEXT NOT NULL DEFAULT '')")
      db.execSQL("CREATE TABLE IF NOT EXISTS `neet_tally_counters` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `count` INTEGER NOT NULL, `target` INTEGER NOT NULL, `unit` TEXT NOT NULL)")
      db.execSQL("CREATE TABLE IF NOT EXISTS `task_presets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `targetTimeMinutes` INTEGER NOT NULL, `noteText` TEXT NOT NULL, `noteImageUri` TEXT)")
      db.execSQL("CREATE TABLE IF NOT EXISTS `ai_chat_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `role` TEXT NOT NULL, `text` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `isError` INTEGER NOT NULL, `isCloud` INTEGER NOT NULL, `isPinned` INTEGER NOT NULL DEFAULT 0)")

      // 2. Perform dynamic, safe column additions for older versions of existing tables
      try { db.execSQL("ALTER TABLE `habit_tasks` ADD COLUMN `targetDates` TEXT") } catch (_: Exception) {}
      try { db.execSQL("ALTER TABLE `habit_tasks` ADD COLUMN `startDate` TEXT") } catch (_: Exception) {}
      try { db.execSQL("ALTER TABLE `habit_tasks` ADD COLUMN `endDate` TEXT") } catch (_: Exception) {}
      try { db.execSQL("ALTER TABLE `habit_tasks` ADD COLUMN `noteText` TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
      try { db.execSQL("ALTER TABLE `habit_tasks` ADD COLUMN `noteImageUri` TEXT") } catch (_: Exception) {}
      try { db.execSQL("ALTER TABLE `habit_tasks` ADD COLUMN `reminderTime` TEXT") } catch (_: Exception) {}
      try { db.execSQL("ALTER TABLE `habit_tasks` ADD COLUMN `isArchived` INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
      try { db.execSQL("ALTER TABLE `habit_tasks` ADD COLUMN `eventId` INTEGER") } catch (_: Exception) {}

      try { db.execSQL("ALTER TABLE `habit_task_logs` ADD COLUMN `timeSpentSeconds` INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
      try { db.execSQL("ALTER TABLE `habit_task_logs` ADD COLUMN `isCompleted` INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}

      try { db.execSQL("ALTER TABLE `plan_events` ADD COLUMN `taskTitle` TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
      try { db.execSQL("ALTER TABLE `plan_events` ADD COLUMN `taskTargetMinutes` INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
      try { db.execSQL("ALTER TABLE `plan_events` ADD COLUMN `notes` TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
      try { db.execSQL("ALTER TABLE `plan_events` ADD COLUMN `subtasksJson` TEXT NOT NULL DEFAULT '[]'") } catch (_: Exception) {}

      try { db.execSQL("ALTER TABLE `neet_chapters` ADD COLUMN `isExerciseDone` INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
      try { db.execSQL("ALTER TABLE `neet_chapters` ADD COLUMN `isArDone` INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
      try { db.execSQL("ALTER TABLE `neet_chapters` ADD COLUMN `ncertReadCount` INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
      try { db.execSQL("ALTER TABLE `neet_chapters` ADD COLUMN `customFlags` TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
      try { db.execSQL("UPDATE `neet_chapters` SET `ncertReadCount` = 1 WHERE `isRevisionDone` = 1 AND `ncertReadCount` = 0") } catch (_: Exception) {}

      try { db.execSQL("ALTER TABLE `ai_chat_history` ADD COLUMN `isPinned` INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
    }

    private val MIGRATION_1_14 = object : Migration(1, 14) { override fun migrate(db: SupportSQLiteDatabase) = migrateAll(db) }
    private val MIGRATION_2_14 = object : Migration(2, 14) { override fun migrate(db: SupportSQLiteDatabase) = migrateAll(db) }
    private val MIGRATION_3_14 = object : Migration(3, 14) { override fun migrate(db: SupportSQLiteDatabase) = migrateAll(db) }
    private val MIGRATION_4_14 = object : Migration(4, 14) { override fun migrate(db: SupportSQLiteDatabase) = migrateAll(db) }
    private val MIGRATION_5_14 = object : Migration(5, 14) { override fun migrate(db: SupportSQLiteDatabase) = migrateAll(db) }
    private val MIGRATION_6_14 = object : Migration(6, 14) { override fun migrate(db: SupportSQLiteDatabase) = migrateAll(db) }
    private val MIGRATION_7_14 = object : Migration(7, 14) { override fun migrate(db: SupportSQLiteDatabase) = migrateAll(db) }
    private val MIGRATION_8_14 = object : Migration(8, 14) { override fun migrate(db: SupportSQLiteDatabase) = migrateAll(db) }
    private val MIGRATION_9_14 = object : Migration(9, 14) { override fun migrate(db: SupportSQLiteDatabase) = migrateAll(db) }
    private val MIGRATION_10_14 = object : Migration(10, 14) { override fun migrate(db: SupportSQLiteDatabase) = migrateAll(db) }
    private val MIGRATION_11_14 = object : Migration(11, 14) { override fun migrate(db: SupportSQLiteDatabase) = migrateAll(db) }
    private val MIGRATION_12_14 = object : Migration(12, 14) { override fun migrate(db: SupportSQLiteDatabase) = migrateAll(db) }
    private val MIGRATION_13_14 = object : Migration(13, 14) { override fun migrate(db: SupportSQLiteDatabase) = migrateAll(db) }

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "habit_tracker.db"
        )
          .addMigrations(
            MIGRATION_1_14,
            MIGRATION_2_14,
            MIGRATION_3_14,
            MIGRATION_4_14,
            MIGRATION_5_14,
            MIGRATION_6_14,
            MIGRATION_7_14,
            MIGRATION_8_14,
            MIGRATION_9_14,
            MIGRATION_10_14,
            MIGRATION_11_14,
            MIGRATION_12_14,
            MIGRATION_13_14
          )
          .fallbackToDestructiveMigration() // Guarantees the app will never crash on database open
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
