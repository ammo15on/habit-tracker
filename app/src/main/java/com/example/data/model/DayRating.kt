package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RatingType(val code: String, val label: String, val emoji: String) {
  BEST("A", "Best", "😊"),
  AVERAGE("B", "Average", "😐"),
  WORST("C", "Worst", "😢");

  companion object {
    fun fromString(value: String?): RatingType? {
      return entries.find { it.name.equals(value, ignoreCase = true) || it.code.equals(value, ignoreCase = true) }
    }
  }
}

@Entity(tableName = "day_ratings")
data class DayRating(
  @PrimaryKey
  val date: String, // "yyyy-MM-dd"
  val rating: String, // "BEST", "AVERAGE", "WORST"
  val updatedAt: Long = System.currentTimeMillis()
) {
  val ratingType: RatingType?
    get() = RatingType.fromString(rating)
}
