package com.example

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.FrameLayout
import android.widget.TextView
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.AiChatEntity
import com.example.data.model.DayRating
import com.example.data.model.HabitTask
import com.example.data.model.HabitTaskLog
import com.example.data.model.NeetChapter
import com.example.data.model.NeetTallyCounter
import com.example.data.model.NeetTestScore
import com.example.data.model.NoteAttachment
import com.example.data.model.NoteEntity
import com.example.data.model.PlanEvent
import com.example.data.model.PlannedTask
import com.example.data.model.TaskPreset
import com.example.data.repository.HabitRepository
import com.example.ui.screens.applySmartListContinuation
import com.example.ui.screens.insertBulletPrefix
import com.example.ui.screens.insertSerialNumberPrefix
import com.example.util.BackupArchiveManager
import com.example.util.BackupPayload
import com.example.util.DateUtils
import com.example.widget.NeetProgressAppWidgetProvider
import com.example.widget.TasksAppWidgetProvider
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WidgetAndNotesTest {

  private lateinit var context: Context
  private lateinit var db: AppDatabase
  private lateinit var repository: HabitRepository

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = HabitRepository(db.habitDao())
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun tasksWidget_buildAndInflateRemoteViews_succeeds() {
    val today = DateUtils.today()
    val tasks = listOf(
      HabitTask(id = 1L, name = "Physics Electrostatics", targetDate = today, targetTimeMinutes = 90),
      HabitTask(id = 2L, name = "Botany NCERT Revision", targetDate = today, targetTimeMinutes = 60)
    )
    val logs = listOf(
      HabitTaskLog(id = 10L, taskId = 2L, date = today, timeSpentSeconds = 3600L, isCompleted = true)
    )

    val views = TasksAppWidgetProvider.buildRemoteViews(
      context = context,
      appWidgetId = 101,
      allTasks = tasks,
      allLogs = logs,
      today = today
    )

    val container = FrameLayout(context)
    val inflated = views.apply(context, container)
    assertNotNull(inflated)

    val titleView = inflated.findViewById<TextView>(R.id.widget_title)
    val row1Title = inflated.findViewById<TextView>(R.id.widget_task_1_title)
    val row2Title = inflated.findViewById<TextView>(R.id.widget_task_2_title)

    assertEquals("Today's Tasks (1/2)", titleView.text.toString())
    assertTrue(row1Title.text.toString().contains("Physics Electrostatics"))
    assertTrue(row2Title.text.toString().contains("Botany NCERT Revision"))
  }

  @Test
  fun neetWidget_buildAndInflateRemoteViews_succeedsWithChaptersAndEmptyFallback() {
    val emptyViews = NeetProgressAppWidgetProvider.buildRemoteViews(context, 201, emptyList())
    val emptyInflated = emptyViews.apply(context, FrameLayout(context))
    assertEquals(
      "0 / ${NeetChapter.DEFAULT_CHAPTERS.size}",
      emptyInflated.findViewById<TextView>(R.id.widget_chapters_count).text.toString()
    )

    val chapters = listOf(
      NeetChapter(id = 1, name = "Cell", subject = "Botany", isCompleted = true, isPyqDone = true, ncertReadCount = 2),
      NeetChapter(id = 2, name = "Kinematics", subject = "Physics", isCompleted = false, isPyqDone = true, ncertReadCount = 0)
    )
    val populatedViews = NeetProgressAppWidgetProvider.buildRemoteViews(context, 202, chapters)
    val populatedInflated = populatedViews.apply(context, FrameLayout(context))
    assertEquals("1 / 2", populatedInflated.findViewById<TextView>(R.id.widget_chapters_count).text.toString())
    assertEquals("2 / 2", populatedInflated.findViewById<TextView>(R.id.widget_pyq_count).text.toString())
    assertEquals("1 / 2", populatedInflated.findViewById<TextView>(R.id.widget_ncert_count).text.toString())
    assertEquals("40%", populatedInflated.findViewById<TextView>(R.id.widget_overall_percent).text.toString())
  }

  @Test
  fun widgetProviders_onUpdateOnMainThread_doNotThrow() {
    val manager = AppWidgetManager.getInstance(context)
    TasksAppWidgetProvider().onUpdate(context, manager, intArrayOf(1))
    NeetProgressAppWidgetProvider().onUpdate(context, manager, intArrayOf(2))
  }

  @Test
  fun backupArchiveManager_zipRoundTrip_preservesAllDataAndMediaFiles() {
    val tempMediaSource = File(context.cacheDir, "test_src_${UUID.randomUUID()}").apply { mkdirs() }
    val tempMediaDest = File(context.cacheDir, "test_dest_${UUID.randomUUID()}").apply { mkdirs() }
    try {
      val imageBytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 1, 2, 3, 4, 5)
      val pdfBytes = "%PDF-1.4\n1 0 obj\n<<>>\nendobj\ntrailer\n<<>>\n%%EOF".toByteArray()

      val imgFile = File(tempMediaSource, "diagram.png").apply { writeBytes(imageBytes) }
      val pdfFile = File(tempMediaSource, "notes.pdf").apply { writeBytes(pdfBytes) }

      val attachments = listOf(
        NoteAttachment("a1", NoteEntity.TYPE_IMAGE, imgFile.absolutePath, "diagram.png", "image/png"),
        NoteAttachment("a2", NoteEntity.TYPE_PDF, pdfFile.absolutePath, "notes.pdf", "application/pdf")
      )
      val note = NoteEntity(
        id = 7L,
        title = "Electrostatics Short Notes",
        content = "1. Gauss law\n• Electric flux",
        subject = "Physics",
        chapterName = "Electrostatic Potential and Capacitance",
        chapterId = 42L,
        attachmentsJson = NoteEntity.serializeAttachments(attachments)
      )

      val payload = BackupPayload(
        tasks = listOf(HabitTask(id = 1L, name = "Solve 50 MCQs", targetDate = "2026-11-20")),
        logs = listOf(HabitTaskLog(id = 2L, taskId = 1L, date = "2026-11-20", timeSpentSeconds = 1800L, isCompleted = true)),
        ratings = listOf(DayRating(date = "2026-11-20", rating = "BEST")),
        scores = listOf(NeetTestScore(id = 3L, testName = "Mock 1", date = "2026-11-20", physicsScore = 160, chemistryScore = 165, botanyScore = 175, zoologyScore = 170)),
        plannedTasks = listOf(PlannedTask(id = 4L, title = "Revise Optics", date = "2026-11-21")),
        events = listOf(PlanEvent(id = 5L, title = "Full Test Week", startDate = "2026-11-20", endDate = "2026-11-26")),
        presets = listOf(TaskPreset(id = 6L, name = "NCERT Drill", targetTimeMinutes = 45)),
        chapters = listOf(NeetChapter(id = 42L, name = "Electrostatic Potential and Capacitance", subject = "Physics", isCompleted = true, notes = "1. Gauss law")),
        counters = listOf(NeetTallyCounter(id = 8L, title = "MCQs", count = 120, target = 500, unit = "Qs")),
        notes = listOf(note),
        aiChats = listOf(AiChatEntity(id = 9L, role = "user", text = "Explain Gauss Law", timestamp = 1000L)),
        customSubjects = listOf("Organic Notes"),
        customSubfolders = mapOf("Physics" to listOf("Formula Sheets")),
        themePrefs = JSONObject().put("customColorHex", "#10B981"),
        goalPrefs = JSONObject().put("title", "NEET 700+")
      )

      val baos = ByteArrayOutputStream()
      BackupArchiveManager.exportPayloadToZipStream(payload, baos, context)
      val zipBytes = baos.toByteArray()
      assertTrue(zipBytes.isNotEmpty())

      val imported = BackupArchiveManager.importFromStream(ByteArrayInputStream(zipBytes), tempMediaDest)
      assertEquals(2, imported.mediaRestoredCount)
      assertEquals(1, imported.payload.tasks.size)
      assertEquals(1, imported.payload.notes.size)
      assertEquals(1, imported.payload.chapters.size)
      assertEquals(1, imported.payload.aiChats.size)
      assertEquals(listOf("Organic Notes"), imported.payload.customSubjects)
      assertEquals(listOf("Formula Sheets"), imported.payload.customSubfolders["Physics"])

      val restoredAtts = imported.payload.notes.first().getAttachments()
      assertEquals(2, restoredAtts.size)
      val restoredImgFile = File(restoredAtts[0].filePath)
      val restoredPdfFile = File(restoredAtts[1].filePath)
      assertTrue(restoredImgFile.exists())
      assertTrue(restoredPdfFile.exists())
      assertArrayEquals(imageBytes, restoredImgFile.readBytes())
      assertArrayEquals(pdfBytes, restoredPdfFile.readBytes())
    } finally {
      tempMediaSource.deleteRecursively()
      tempMediaDest.deleteRecursively()
    }
  }

  @Test
  fun notesAndNeetChapters_twoWaySync_worksBothDirections() = runBlocking {
    val chId = repository.insertNeetChapter(
      NeetChapter(name = "Photosynthesis in Higher Plants", subject = "Botany")
    )

    // 1. Save a note inside Subject -> Chapter -> should update NeetChapter.notes
    repository.saveNoteWithChapterSync(
      NoteEntity(
        title = "Photosynthesis Notes",
        content = "1. Light reaction occurs in thylakoid\n• PSI P700",
        subject = "Botany",
        chapterName = "Photosynthesis in Higher Plants",
        chapterId = chId
      )
    )
    val updatedChapter = db.habitDao().getNeetChapterByIdSync(chId)!!
    assertTrue(updatedChapter.notes.contains("Light reaction occurs in thylakoid"))

    // 2. Edit NeetChapter directly -> should update linked NoteEntity content
    repository.updateNeetChapter(
      updatedChapter.copy(notes = "1. Calvin cycle in stroma\n2. C4 pathway")
    )
    val syncedNotes = db.habitDao().getNotesForChapterSync(chId, "Botany", "Photosynthesis in Higher Plants")
    assertEquals(1, syncedNotes.size)
    assertTrue(syncedNotes.first().content.contains("Calvin cycle in stroma"))
  }

  @Test
  fun noteEditorFormatting_bulletsAndSerialNumbers_workCleanly() {
    val bulletState = insertBulletPrefix(TextFieldValue("", TextRange(0)))
    assertEquals("• ", bulletState.text)

    val typedBullet = TextFieldValue("• First point\n", TextRange("• First point\n".length))
    val continuedBullet = applySmartListContinuation(
      TextFieldValue("• First point", TextRange("• First point".length)),
      typedBullet
    )
    assertEquals("• First point\n• ", continuedBullet.text)

    val num1 = insertSerialNumberPrefix(TextFieldValue("", TextRange(0)))
    assertEquals("1. ", num1.text)

    val afterLine1 = TextFieldValue("1. First law\n", TextRange("1. First law\n".length))
    val continuedNum2 = applySmartListContinuation(
      TextFieldValue("1. First law", TextRange("1. First law".length)),
      afterLine1
    )
    assertEquals("1. First law\n2. ", continuedNum2.text)
  }
}
