package com.example.habit.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.habit.data.model.*

@Database(
    entities = [
        HabitTask::class,
        HabitTaskLog::class,
        DayRating::class,
        PlannedTask::class,
        PlanEvent::class,
        NeetChapter::class,
        NeetTestScore::class,
        NeetTallyCounter::class,
        TaskPreset::class
    ],
    version = 10,
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
                    "habit_tracker_database"
                )
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .addMigrations()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
