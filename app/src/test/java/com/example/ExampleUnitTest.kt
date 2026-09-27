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
    assertFalse(chapter.isExerciseDone)
    assertFalse(chapter.isArDone)
    assertEquals(0, chapter.ncertReadCount)

    val updated = chapter.copy(
      isCompleted = true,
      isPyqDone = true,
      isExerciseDone = true,
      isArDone = true,
      ncertReadCount = 3,
      isRevisionDone = true
    )
    assertTrue(updated.isCompleted)
    assertTrue(updated.isPyqDone)
    assertTrue(updated.isExerciseDone)
    assertTrue(updated.isArDone)
    assertEquals(3, updated.ncertReadCount)
    assertTrue(updated.isRevisionDone)
  }

  @Test
  fun neetTallyCounter_presets_containRequestedStudyItems() {
    val presetNames = NeetTallyCounter.DEFAULT_COUNTERS.map { it.title }
    assertTrue(presetNames.contains("Full Mock Tests Given"))
    assertTrue(presetNames.contains("Physics Numericals Solved"))
    assertTrue(presetNames.contains("NCERT Biology Line-by-Line Revisions"))
    assertTrue(presetNames.contains("OMR Sheet Bubble Practice"))
  }
}
