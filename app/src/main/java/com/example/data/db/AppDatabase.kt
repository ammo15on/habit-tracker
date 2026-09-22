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
    TaskPreset::class
  ],
  version = 8,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun habitDao(): HabitDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "habit_tracker.db"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
