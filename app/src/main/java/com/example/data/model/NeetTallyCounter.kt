package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "neet_tally_counters")
data class NeetTallyCounter(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String, // e.g. "bot ncert read", "bot q", "zoo ncert read", "zoo q", "phy q", "chem q"
  val count: Int = 0,
  val target: Int = 0, // 0 = no target set
  val unit: String = "times", // "times", "questions", "chapters", "pages"
  val step: Int = 1
) {
  companion object {
    val DEFAULT_COUNTERS = listOf(
      NeetTallyCounter(title = "bot ncert read", count = 12, target = 30, unit = "chapters"),
      NeetTallyCounter(title = "bot q", count = 350, target = 1000, unit = "questions"),
      NeetTallyCounter(title = "zoo ncert read", count = 10, target = 30, unit = "chapters"),
      NeetTallyCounter(title = "zoo q", count = 280, target = 1000, unit = "questions"),
      NeetTallyCounter(title = "phy q", count = 195, target = 800, unit = "questions"),
      NeetTallyCounter(title = "chem q", count = 240, target = 800, unit = "questions")
    )
  }
}
