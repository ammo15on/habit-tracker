package com.example

import com.example.data.model.HabitTask
import com.example.data.model.NeetChapter
import com.example.data.model.NeetTallyCounter
import com.example.ui.AnalyticsTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun analyticsTab_order_isDayWeekMonthNeet() {
    val tabs = AnalyticsTab.values()
    assertEquals(AnalyticsTab.DAY, tabs[0])
    assertEquals(AnalyticsTab.WEEK, tabs[1])
    assertEquals(AnalyticsTab.MONTH, tabs[2])
    assertEquals(AnalyticsTab.NEET, tabs[3])
  }

  @Test
  fun neetChapter_defaults_and_toggle() {
    val chapter = NeetChapter(
      name = "Cell: The Unit of Life",
      subject = "Botany"
    )
    assertFalse(chapter.isCompleted)
    assertFalse(chapter.isPyqDone)
    assertFalse(chapter.isRevisionDone)

    val updated = chapter.copy(isCompleted = true, isPyqDone = true)
    assertTrue(updated.isCompleted)
    assertTrue(updated.isPyqDone)
  }

  @Test
  fun neetTallyCounter_presets_containRequestedStudyItems() {
    val presetNames = NeetTallyCounter.DEFAULT_COUNTERS.map { it.title }
    assertTrue(presetNames.contains("bot ncert read"))
    assertTrue(presetNames.contains("bot q"))
    assertTrue(presetNames.contains("zoo ncert read"))
    assertTrue(presetNames.contains("zoo q"))
  }
}
