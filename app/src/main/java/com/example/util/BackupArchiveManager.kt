package com.example.util

import android.content.Context
import android.net.Uri
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
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import org.json.JSONArray
import org.json.JSONObject

data class BackupPayload(
  val tasks: List<HabitTask> = emptyList(),
  val logs: List<HabitTaskLog> = emptyList(),
  val ratings: List<DayRating> = emptyList(),
  val scores: List<NeetTestScore> = emptyList(),
  val plannedTasks: List<PlannedTask> = emptyList(),
  val events: List<PlanEvent> = emptyList(),
  val presets: List<TaskPreset> = emptyList(),
  val chapters: List<NeetChapter> = emptyList(),
  val counters: List<NeetTallyCounter> = emptyList(),
  val notes: List<NoteEntity> = emptyList(),
  val aiChats: List<AiChatEntity> = emptyList(),
  val customSubjects: List<String> = emptyList(),
  val customSubfolders: Map<String, List<String>> = emptyMap(),
  val themePrefs: JSONObject? = null,
  val goalPrefs: JSONObject? = null,
  val chapterButtonsJson: String? = null
)

data class BackupImportResult(
  val payload: BackupPayload,
  val mediaRestoredCount: Int
) {
  val totalRecordsCount: Int
    get() = payload.tasks.size +
      payload.logs.size +
      payload.ratings.size +
      payload.scores.size +
      payload.plannedTasks.size +
      payload.events.size +
      payload.presets.size +
      payload.chapters.size +
      payload.counters.size +
      payload.notes.size +
      payload.aiChats.size
}

object BackupArchiveManager {
  private const val BACKUP_JSON_ENTRY = "backup.json"
  private const val MEDIA_ZIP_PREFIX = "media/"
  private const val ZIP_REF_PREFIX = "zip://"

  fun serializePayloadToJson(
    payload: BackupPayload,
    mediaPathMapper: (String) -> String = { it }
  ): String {
    val root = JSONObject()
    root.put("backupFormatVersion", 2)
    root.put("exportedAt", System.currentTimeMillis())

    val tasksArray = JSONArray()
    payload.tasks.forEach { t ->
      val mappedImg = t.noteImageUri?.takeIf { it.isNotBlank() }?.let(mediaPathMapper) ?: ""
      tasksArray.put(JSONObject().apply {
        put("id", t.id)
        put("name", t.name)
        put("targetDate", t.targetDate ?: "")
        put("repeatDaysMask", t.repeatDaysMask)
        put("targetTimeMinutes", t.targetTimeMinutes)
        put("isDefault", t.isDefault)
        put("isStarred", t.isStarred)
        put("targetDates", t.targetDates ?: "")
        put("startDate", t.startDate ?: "")
        put("endDate", t.endDate ?: "")
        put("noteText", t.noteText)
        put("noteImageUri", mappedImg)
        put("reminderTime", t.reminderTime ?: "")
        put("isArchived", t.isArchived)
        if (t.eventId != null) put("eventId", t.eventId)
      })
    }
    root.put("tasks", tasksArray)

    val logsArray = JSONArray()
    payload.logs.forEach { l ->
      logsArray.put(JSONObject().apply {
        put("id", l.id)
        put("taskId", l.taskId)
        put("date", l.date)
        put("timeSpentSeconds", l.timeSpentSeconds)
        put("isCompleted", l.isCompleted)
      })
    }
    root.put("logs", logsArray)

    val ratingsArray = JSONArray()
    payload.ratings.forEach { r ->
      ratingsArray.put(JSONObject().apply {
        put("date", r.date)
        put("rating", r.rating)
      })
    }
    root.put("ratings", ratingsArray)

    val scoresArray = JSONArray()
    payload.scores.forEach { s ->
      scoresArray.put(JSONObject().apply {
        put("id", s.id)
        put("testName", s.testName)
        put("date", s.date)
        put("physicsScore", s.physicsScore)
        put("chemistryScore", s.chemistryScore)
        put("botanyScore", s.botanyScore)
        put("zoologyScore", s.zoologyScore)
        put("maxPhysics", s.maxPhysics)
        put("maxChemistry", s.maxChemistry)
        put("maxBotany", s.maxBotany)
        put("maxZoology", s.maxZoology)
        put("timestamp", s.timestamp)
      })
    }
    root.put("scores", scoresArray)

    val plannedArray = JSONArray()
    payload.plannedTasks.forEach { p ->
      plannedArray.put(JSONObject().apply {
        put("id", p.id)
        put("title", p.title)
        put("date", p.date)
        put("targetTimeMinutes", p.targetTimeMinutes)
        put("notes", p.notes)
        put("isStarred", p.isStarred)
        put("isArchived", p.isArchived)
        put("isCompleted", p.isCompleted)
      })
    }
    root.put("plannedTasks", plannedArray)

    val eventsArray = JSONArray()
    payload.events.forEach { e ->
      eventsArray.put(JSONObject().apply {
        put("id", e.id)
        put("title", e.title)
        put("startDate", e.startDate)
        put("endDate", e.endDate)
        put("taskTitle", e.taskTitle)
        put("taskTargetMinutes", e.taskTargetMinutes)
        put("notes", e.notes)
        put("subtasksJson", e.subtasksJson)
      })
    }
    root.put("events", eventsArray)

    val presetsArray = JSONArray()
    payload.presets.forEach { p ->
      val mappedImg = p.noteImageUri?.takeIf { it.isNotBlank() }?.let(mediaPathMapper) ?: ""
      presetsArray.put(JSONObject().apply {
        put("id", p.id)
        put("name", p.name)
        put("targetTimeMinutes", p.targetTimeMinutes)
        put("noteText", p.noteText)
        put("noteImageUri", mappedImg)
      })
    }
    root.put("presets", presetsArray)

    val chaptersArray = JSONArray()
    payload.chapters.forEach { c ->
      chaptersArray.put(JSONObject().apply {
        put("id", c.id)
        put("name", c.name)
        put("subject", c.subject)
        put("isCompleted", c.isCompleted)
        put("isPyqDone", c.isPyqDone)
        put("isRevisionDone", c.isRevisionDone)
        put("isExerciseDone", c.isExerciseDone)
        put("isArDone", c.isArDone)
        put("ncertReadCount", c.ncertReadCount)
        put("notes", c.notes)
        put("customFlags", c.customFlags)
      })
    }
    root.put("chapters", chaptersArray)

    val countersArray = JSONArray()
    payload.counters.forEach { c ->
      countersArray.put(JSONObject().apply {
        put("id", c.id)
        put("title", c.title)
        put("count", c.count)
        put("target", c.target)
        put("unit", c.unit)
      })
    }
    root.put("counters", countersArray)

    val notesArray = JSONArray()
    payload.notes.forEach { n ->
      val mappedAttachments = n.getAttachments().map { att ->
        att.copy(filePath = mediaPathMapper(att.filePath))
      }
      notesArray.put(JSONObject().apply {
        put("id", n.id)
        put("title", n.title)
        put("content", n.content)
        put("subject", n.subject)
        put("chapterName", n.chapterName)
        if (n.chapterId != null) put("chapterId", n.chapterId)
        put("attachmentsJson", NoteEntity.serializeAttachments(mappedAttachments))
        put("createdAt", n.createdAt)
        put("updatedAt", n.updatedAt)
      })
    }
    root.put("notes", notesArray)

    val aiChatsArray = JSONArray()
    payload.aiChats.forEach { c ->
      aiChatsArray.put(JSONObject().apply {
        put("id", c.id)
        put("role", c.role)
        put("text", c.text)
        put("timestamp", c.timestamp)
        put("isError", c.isError)
        put("isCloud", c.isCloud)
        put("isPinned", c.isPinned)
      })
    }
    root.put("aiChats", aiChatsArray)

    val foldersObj = JSONObject().apply {
      put("customSubjects", JSONArray(payload.customSubjects))
      val subsObj = JSONObject()
      payload.customSubfolders.forEach { (k, v) ->
        subsObj.put(k, JSONArray(v))
      }
      put("customSubfolders", subsObj)
    }
    root.put("noteFolders", foldersObj)

    if (payload.themePrefs != null) root.put("themePrefs", payload.themePrefs)
    if (payload.goalPrefs != null) root.put("goalPrefs", payload.goalPrefs)
    if (!payload.chapterButtonsJson.isNullOrBlank()) root.put("chapterButtonsJson", payload.chapterButtonsJson)

    return root.toString(2)
  }

  fun parsePayloadFromJson(
    jsonString: String,
    mediaPathRestorer: (String) -> String = { it }
  ): BackupPayload {
    val root = JSONObject(jsonString)

    val tasksList = mutableListOf<HabitTask>()
    if (root.has("tasks")) {
      val arr = root.getJSONArray("tasks")
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        val rawImg = o.optString("noteImageUri", "").takeIf { it.isNotBlank() }
        tasksList.add(
          HabitTask(
            id = o.optLong("id", 0L),
            name = o.optString("name", ""),
            targetDate = o.optString("targetDate", "").takeIf { it.isNotBlank() },
            repeatDaysMask = o.optInt("repeatDaysMask", 0),
            targetTimeMinutes = o.optInt("targetTimeMinutes", 0),
            isDefault = o.optBoolean("isDefault", false),
            isStarred = o.optBoolean("isStarred", false),
            targetDates = o.optString("targetDates", "").takeIf { it.isNotBlank() },
            startDate = o.optString("startDate", "").takeIf { it.isNotBlank() },
            endDate = o.optString("endDate", "").takeIf { it.isNotBlank() },
            noteText = o.optString("noteText", ""),
            noteImageUri = rawImg?.let(mediaPathRestorer),
            reminderTime = o.optString("reminderTime", "").takeIf { it.isNotBlank() },
            isArchived = o.optBoolean("isArchived", false),
            eventId = if (o.has("eventId") && !o.isNull("eventId")) o.optLong("eventId") else null
          )
        )
      }
    }

    val logsList = mutableListOf<HabitTaskLog>()
    if (root.has("logs")) {
      val arr = root.getJSONArray("logs")
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        logsList.add(
          HabitTaskLog(
            id = o.optLong("id", 0L),
            taskId = o.optLong("taskId", 0L),
            date = o.optString("date", ""),
            timeSpentSeconds = o.optLong("timeSpentSeconds", 0L),
            isCompleted = o.optBoolean("isCompleted", false)
          )
        )
      }
    }

    val ratingsList = mutableListOf<DayRating>()
    if (root.has("ratings")) {
      val arr = root.getJSONArray("ratings")
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        ratingsList.add(
          DayRating(
            date = o.optString("date", ""),
            rating = o.optString("rating", "")
          )
        )
      }
    }

    val scoresList = mutableListOf<NeetTestScore>()
    if (root.has("scores")) {
      val arr = root.getJSONArray("scores")
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        scoresList.add(
          NeetTestScore(
            id = o.optLong("id", 0L),
            testName = o.optString("testName", ""),
            date = o.optString("date", ""),
            physicsScore = o.optInt("physicsScore", 0),
            chemistryScore = o.optInt("chemistryScore", 0),
            botanyScore = o.optInt("botanyScore", 0),
            zoologyScore = o.optInt("zoologyScore", 0),
            maxPhysics = o.optInt("maxPhysics", 180),
            maxChemistry = o.optInt("maxChemistry", 180),
            maxBotany = o.optInt("maxBotany", 180),
            maxZoology = o.optInt("maxZoology", 180),
            timestamp = o.optLong("timestamp", System.currentTimeMillis())
          )
        )
      }
    }

    val plannedList = mutableListOf<PlannedTask>()
    if (root.has("plannedTasks")) {
      val arr = root.getJSONArray("plannedTasks")
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        plannedList.add(
          PlannedTask(
            id = o.optLong("id", 0L),
            title = o.optString("title", ""),
            date = o.optString("date", ""),
            targetTimeMinutes = o.optInt("targetTimeMinutes", 0),
            notes = o.optString("notes", ""),
            isStarred = o.optBoolean("isStarred", false),
            isArchived = o.optBoolean("isArchived", false),
            isCompleted = o.optBoolean("isCompleted", false)
          )
        )
      }
    }

    val eventsList = mutableListOf<PlanEvent>()
    if (root.has("events")) {
      val arr = root.getJSONArray("events")
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        eventsList.add(
          PlanEvent(
            id = o.optLong("id", 0L),
            title = o.optString("title", ""),
            startDate = o.optString("startDate", ""),
            endDate = o.optString("endDate", ""),
            taskTitle = o.optString("taskTitle", ""),
            taskTargetMinutes = o.optInt("taskTargetMinutes", 0),
            notes = o.optString("notes", ""),
            subtasksJson = o.optString("subtasksJson", "[]")
          )
        )
      }
    }

    val presetsList = mutableListOf<TaskPreset>()
    if (root.has("presets")) {
      val arr = root.getJSONArray("presets")
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        val rawImg = o.optString("noteImageUri", "").takeIf { it.isNotBlank() }
        presetsList.add(
          TaskPreset(
            id = o.optLong("id", 0L),
            name = o.optString("name", ""),
            targetTimeMinutes = o.optInt("targetTimeMinutes", 0),
            noteText = o.optString("noteText", ""),
            noteImageUri = rawImg?.let(mediaPathRestorer)
          )
        )
      }
    }

    val chaptersList = mutableListOf<NeetChapter>()
    if (root.has("chapters")) {
      val arr = root.getJSONArray("chapters")
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        chaptersList.add(
          NeetChapter(
            id = o.optLong("id", 0L),
            name = o.optString("name", o.optString("chapterName", "")),
            subject = o.optString("subject", "Physics"),
            isCompleted = o.optBoolean("isCompleted", o.optBoolean("theoryCompleted", false)),
            isPyqDone = o.optBoolean("isPyqDone", o.optBoolean("pyqsSolved", false)),
            isRevisionDone = o.optBoolean("isRevisionDone", false),
            isExerciseDone = o.optBoolean("isExerciseDone", false),
            isArDone = o.optBoolean("isArDone", false),
            ncertReadCount = o.optInt("ncertReadCount", if (o.optBoolean("isRevisionDone", false)) 1 else 0),
            notes = o.optString("notes", ""),
            customFlags = o.optString("customFlags", "")
          )
        )
      }
    }

    val countersList = mutableListOf<NeetTallyCounter>()
    if (root.has("counters")) {
      val arr = root.getJSONArray("counters")
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        countersList.add(
          NeetTallyCounter(
            id = o.optLong("id", 0L),
            title = o.optString("title", o.optString("name", "")),
            count = o.optInt("count", 0),
            target = o.optInt("target", o.optInt("targetCount", 0)),
            unit = o.optString("unit", "times")
          )
        )
      }
    }

    val notesList = mutableListOf<NoteEntity>()
    if (root.has("notes")) {
      val arr = root.getJSONArray("notes")
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        val rawAtts = NoteEntity.parseAttachments(o.optString("attachmentsJson", "[]"))
        val restoredAtts = rawAtts.map { att ->
          att.copy(filePath = mediaPathRestorer(att.filePath))
        }
        notesList.add(
          NoteEntity(
            id = o.optLong("id", 0L),
            title = o.optString("title", ""),
            content = o.optString("content", ""),
            subject = o.optString("subject", ""),
            chapterName = o.optString("chapterName", ""),
            chapterId = if (o.has("chapterId") && !o.isNull("chapterId")) o.optLong("chapterId") else null,
            attachmentsJson = NoteEntity.serializeAttachments(restoredAtts),
            createdAt = o.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
          )
        )
      }
    }

    val aiChatsList = mutableListOf<AiChatEntity>()
    if (root.has("aiChats")) {
      val arr = root.getJSONArray("aiChats")
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        aiChatsList.add(
          AiChatEntity(
            id = o.optLong("id", 0L),
            role = o.optString("role", "user"),
            text = o.optString("text", ""),
            timestamp = o.optLong("timestamp", System.currentTimeMillis()),
            isError = o.optBoolean("isError", false),
            isCloud = o.optBoolean("isCloud", false),
            isPinned = o.optBoolean("isPinned", false)
          )
        )
      }
    }

    val customSubjects = mutableListOf<String>()
    val customSubfolders = mutableMapOf<String, List<String>>()
    if (root.has("noteFolders")) {
      val folders = root.optJSONObject("noteFolders")
      folders?.optJSONArray("customSubjects")?.let { arr ->
        for (i in 0 until arr.length()) {
          arr.optString(i).takeIf { it.isNotBlank() }?.let { customSubjects.add(it) }
        }
      }
      folders?.optJSONObject("customSubfolders")?.let { subs ->
        val keys = subs.keys()
        while (keys.hasNext()) {
          val k = keys.next()
          val arr = subs.optJSONArray(k)
          if (arr != null) {
            val list = (0 until arr.length()).mapNotNull { arr.optString(it).takeIf { s -> s.isNotBlank() } }
            if (list.isNotEmpty()) customSubfolders[k] = list
          }
        }
      }
    }

    return BackupPayload(
      tasks = tasksList,
      logs = logsList,
      ratings = ratingsList,
      scores = scoresList,
      plannedTasks = plannedList,
      events = eventsList,
      presets = presetsList,
      chapters = chaptersList,
      counters = countersList,
      notes = notesList,
      aiChats = aiChatsList,
      customSubjects = customSubjects,
      customSubfolders = customSubfolders,
      themePrefs = root.optJSONObject("themePrefs"),
      goalPrefs = root.optJSONObject("goalPrefs"),
      chapterButtonsJson = root.optString("chapterButtonsJson", "").takeIf { it.isNotBlank() }
    )
  }

  /**
   * Exports all app records + referenced local media files (images, PDFs) to a ZIP OutputStream.
   */
  fun exportPayloadToZipStream(
    payload: BackupPayload,
    outputStream: OutputStream,
    context: Context? = null
  ) {
    val pathToZipEntry = linkedMapOf<String, String>() // source path/uri -> zip entry "media/..."
    var counter = 0

    fun registerMedia(rawPath: String): String {
      if (rawPath.isBlank()) return rawPath
      pathToZipEntry[rawPath]?.let { return "$ZIP_REF_PREFIX$it" }

      val candidateFile = resolveReadableFile(rawPath)
      val canReadFromUri = candidateFile == null && context != null && rawPath.startsWith("content://")
      if (candidateFile == null && !canReadFromUri) {
        return rawPath
      }

      val baseName = (candidateFile?.name ?: "media_${counter}").replace(Regex("[^a-zA-Z0-9._-]"), "_")
      counter++
      val entryName = "$MEDIA_ZIP_PREFIX${counter}_$baseName"
      pathToZipEntry[rawPath] = entryName
      return "$ZIP_REF_PREFIX$entryName"
    }

    val jsonString = serializePayloadToJson(payload) { rawPath -> registerMedia(rawPath) }

    ZipOutputStream(outputStream).use { zos ->
      // 1. Write backup.json
      zos.putNextEntry(ZipEntry(BACKUP_JSON_ENTRY))
      zos.write(jsonString.toByteArray(Charsets.UTF_8))
      zos.closeEntry()

      // 2. Write every referenced media file (images & PDFs)
      for ((sourcePath, entryName) in pathToZipEntry) {
        try {
          val file = resolveReadableFile(sourcePath)
          if (file != null && file.exists() && file.canRead()) {
            zos.putNextEntry(ZipEntry(entryName))
            FileInputStream(file).use { fis -> fis.copyTo(zos) }
            zos.closeEntry()
          } else if (context != null && sourcePath.startsWith("content://")) {
            context.contentResolver.openInputStream(Uri.parse(sourcePath))?.use { ins ->
              zos.putNextEntry(ZipEntry(entryName))
              ins.copyTo(zos)
              zos.closeEntry()
            }
          }
        } catch (_: Exception) {
          // Skip missing individual media file cleanly without aborting backup
        }
      }
    }
  }

  /**
   * Imports either a ZIP archive (restoring backup.json + all images/PDFs into mediaDestDir)
   * OR a plain JSON backup stream.
   */
  fun importFromStream(
    inputStream: InputStream,
    mediaDestDir: File
  ): BackupImportResult {
    if (!mediaDestDir.exists()) mediaDestDir.mkdirs()

    val buffered = BufferedInputStream(inputStream)
    buffered.mark(4)
    val header = ByteArray(2)
    val readCount = buffered.read(header)
    buffered.reset()

    val isZip = readCount == 2 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte()
    if (!isZip) {
      val jsonText = buffered.bufferedReader(Charsets.UTF_8).use { it.readText() }
      val payload = parsePayloadFromJson(jsonText)
      return BackupImportResult(payload = payload, mediaRestoredCount = 0)
    }

    var backupJsonText: String? = null
    val zipEntryToRestoredPath = mutableMapOf<String, String>()
    val canonicalDestRoot = mediaDestDir.canonicalPath + File.separator

    ZipInputStream(buffered).use { zis ->
      var entry = zis.nextEntry
      while (entry != null) {
        if (!entry.isDirectory) {
          val entryName = entry.name
          if (entryName == BACKUP_JSON_ENTRY) {
            val baos = ByteArrayOutputStream()
            zis.copyTo(baos)
            backupJsonText = baos.toString(Charsets.UTF_8.name())
          } else if (entryName.startsWith(MEDIA_ZIP_PREFIX)) {
            val safeFileName = File(entryName).name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val uniqueName = "restored_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}_$safeFileName"
            val destFile = File(mediaDestDir, uniqueName)
            if (destFile.canonicalPath.startsWith(canonicalDestRoot)) {
              FileOutputStream(destFile).use { fos ->
                zis.copyTo(fos)
              }
              zipEntryToRestoredPath[entryName] = destFile.absolutePath
            }
          }
        }
        zis.closeEntry()
        entry = zis.nextEntry
      }
    }

    val json = backupJsonText ?: throw IllegalStateException("ZIP archive does not contain backup.json")
    val payload = parsePayloadFromJson(json) { storedPath ->
      if (storedPath.startsWith(ZIP_REF_PREFIX)) {
        val key = storedPath.removePrefix(ZIP_REF_PREFIX)
        zipEntryToRestoredPath[key] ?: storedPath
      } else {
        storedPath
      }
    }

    return BackupImportResult(
      payload = payload,
      mediaRestoredCount = zipEntryToRestoredPath.size
    )
  }

  private fun resolveReadableFile(rawPath: String): File? {
    if (rawPath.startsWith("file://")) {
      val path = Uri.parse(rawPath).path ?: return null
      return File(path).takeIf { it.exists() && it.isFile }
    }
    if (rawPath.startsWith("/")) {
      return File(rawPath).takeIf { it.exists() && it.isFile }
    }
    return null
  }
}
