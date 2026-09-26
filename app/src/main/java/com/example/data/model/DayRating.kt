package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RatingType(val label: String, val emoji: String) {
  BEST("Best", "😊"),
  AVERAGE("Average", "😐"),
  WORST("Worst", "😞")
}

@Entity(tableName = "day_ratings")
data class DayRating(
  @PrimaryKey
  val date: String, // Format: YYYY-MM-DD
  val rating: String // BEST, AVERAGE, WORST
) {
  val ratingType: RatingType?
    get() = try {
      RatingType.valueOf(rating)
    } catch (_: Exception) {
      null
    }
}
