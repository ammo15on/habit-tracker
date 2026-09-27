package com.example.habit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "neet_chapters")
data class NeetChapter(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val subject: String, // "Physics", "Chemistry", "Biology"
    val chapterName: String,
    val isCompletedNcert: Boolean = false,
    val isCompletedQuestions: Boolean = false,
    val isCompletedPyq: Boolean = false,
    val revisionCount: Int = 0
)
