package com.example.habit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "neet_tally_counters")
data class NeetTallyCounter(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val count: Int = 0,
    val targetCount: Int = 100,
    val category: String = "General"
)
