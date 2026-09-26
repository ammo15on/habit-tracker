package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "neet_tally_counters")
data class NeetTallyCounter(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val count: Int = 0,
  val target: Int = 0,
  val unit: String = "times"
) {
  companion object {
    val DEFAULT_COUNTERS = listOf(
      NeetTallyCounter(title = "Full Mock Tests Given", count = 0, target = 25, unit = "tests"),
      NeetTallyCounter(title = "Physics Numericals Solved", count = 0, target = 3000, unit = "MCQs"),
      NeetTallyCounter(title = "NCERT Biology Line-by-Line Revisions", count = 0, target = 5, unit = "cycles"),
      NeetTallyCounter(title = "OMR Sheet Bubble Practice", count = 0, target = 20, unit = "sheets")
    )
  }
}
