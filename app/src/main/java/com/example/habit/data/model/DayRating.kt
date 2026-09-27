package com.example.habit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RatingType { GREAT, GOOD, OKAY, BAD }

@Entity(tableName = "day_ratings")
data class DayRating(
    @PrimaryKey val date: String,
    val rating: RatingType,
    val note: String = ""
)
