package com.example.habit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "neet_test_scores")
data class NeetTestScore(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val testName: String,
    val date: String,
    val score: Int,
    val totalMarks: Int = 720,
    val physicsScore: Int = 0,
    val chemistryScore: Int = 0,
    val biologyScore: Int = 0,
    val notes: String = ""
)
