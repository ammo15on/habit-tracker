package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.DayRating
import com.example.data.model.HabitTask
import com.example.data.model.HabitTaskLog
import com.example.data.model.NeetChapter
import com.example.data.model.NeetTallyCounter
import com.example.data.model.NeetTestScore
import com.example.data.model.NoteEntity
import com.example.data.model.PlannedTask
import com.example.data.model.RatingType
import com.example.data.model.TaskPreset
import com.example.data.repository.HabitRepository
import com.example.ui.theme.AppThemeColor
import com.example.util.AlarmScheduler
import com.example.util.AppGoal
import com.example.util.BackupArchiveManager
import com.example.util.BackupImportResult
import com.example.util.BackupPayload
import com.example.util.CalendarSyncUtil
import com.example.util.ChapterButtonItem
import com.example.util.ChapterButtonPreferences
import com.example.util.DateUtils
import com.example.util.GoalPreferences
import com.example.util.NoteFolderPreferences
import com.example.util.NoteMediaUtils
import com.example.util.OnDeviceSubjectClassifier
import com.example.util.ThemePreferences
import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DayCompletedTaskItem(
  val taskName: String,
  val timeSpentSeconds: Long,
  val isCompleted: Boolean,
  val targetMinutes: Int
)

typealias CompletedDayTaskInfo = DayCompletedTaskItem

class HabitViewModel(
  private val repository: HabitRepository,
  private val themePreferences: ThemePreferences? = null,
  private val goalPreferences: GoalPreferences? = null,
  private val appContext: Context? = null
) : ViewModel() {

  // Current selected date for tracker screen
  val selectedDate = MutableStateFlow(DateUtils.today())

  // Global Bars & Navigation visibility (scrolling down hides both top header and bottom menu)
  val isBarsVisible = MutableStateFlow(true)
  val isBottomBarVisible: StateFlow<Boolean> = isBarsVisible.asStateFlow()
  val isTopBarVisible: StateFlow<Boolean> = isBarsVisible.asStateFlow()

  fun setBarsVisible(visible: Boolean) {
    if (isBarsVisible.value != visible) {
      isBarsVisible.value = visible
    }
  }

  fun setBottomBarVisible(visible: Boolean) = setBarsVisible(visible)
  fun setTopBarVisible(visible: Boolean) = setBarsVisible(visible)

  fun onScrollDelta(delta: Float) {
    if (delta < -8f && isBarsVisible.value) {
      isBarsVisible.value = false
    } else if (delta > 8f && !isBarsVisible.value) {
      isBarsVisible.value = true
    }
  }

  // Global Hamburger Menu state (can be triggered by left edge swipe or header button)
  val isHamburgerMenuOpen = MutableStateFlow(false)
  val isHamburgerOpen: StateFlow<Boolean> = isHamburgerMenuOpen.asStateFlow()

  fun openHamburgerMenu() {
    isHamburgerMenuOpen.value = true
  }

  fun closeHamburgerMenu() {
    isHamburgerMenuOpen.value = false
  }

  fun openHamburger() = openHamburgerMenu()
  fun closeHamburger() = closeHamburgerMenu()

  // Full-Screen Notes Overlay request (can open root Notes Hub or directly to a NEET Chapter folder)
  data class NotesLaunchRequest(
    val initialSubject: String? = null,
    val initialChapterId: Long? = null,
    val initialChapterName: String? = null
  )

  private val _notesLaunchRequest = MutableStateFlow<NotesLaunchRequest?>(null)
  val notesLaunchRequest: StateFlow<NotesLaunchRequest?> = _notesLaunchRequest.asStateFlow()

  fun openFullScreenNotes(
    subject: String? = null,
    chapterId: Long? = null,
    chapterName: String? = null
  ) {
    isHamburgerMenuOpen.value = false
    _notesLaunchRequest.value = NotesLaunchRequest(
      initialSubject = subject,
      initialChapterId = chapterId,
      initialChapterName = chapterName
    )
  }

  fun closeFullScreenNotes() {
    _notesLaunchRequest.value = null
  }

  fun setSelectedDate(date: String) {
    selectedDate.value = date
  }

  // Custom Hexadecimal Color States
  private val _fallbackUiHex = MutableStateFlow("#3B82F6")
  val selectedColorHex: StateFlow<String> =
    themePreferences?.customColorHex ?: _fallbackUiHex.asStateFlow()
  val selectedUiHex: StateFlow<String> = selectedColorHex

  private val _fallbackBgHex = MutableStateFlow("#121212")
  val selectedBgHex: StateFlow<String> =
    themePreferences?.customColorHex ?: _fallbackBgHex.asStateFlow()

  private val _fallbackTextHex = MutableStateFlow("#FFFFFF")
  val selectedFontHex: StateFlow<String> =
    themePreferences?.customFontColorHex ?: _fallbackTextHex.asStateFlow()
  val selectedTextHex: StateFlow<String> = selectedFontHex

  fun setCustomColorHex(hex: String) {
    if (themePreferences != null) {
      themePreferences.setCustomColorHex(hex)
    } else {
      _fallbackUiHex.value = hex
    }
  }

  fun setCustomUiHex(hex: String) = setCustomColorHex(hex)

  fun setCustomBgHex(hex: String) {
    _fallbackBgHex.value = hex
  }

  fun setCustomFontColorHex(hex: String) {
    if (themePreferences != null) {
      themePreferences.setCustomFontColorHex(hex)
    } else {
      _fallbackTextHex.value = hex
    }
  }

  fun setCustomTextHex(hex: String) = setCustomFontColorHex(hex)

  fun setBackgroundImage(uri: android.net.Uri?) {
    setBackgroundImageUri(uri?.toString())
  }

  // App Goal state
  val appGoal: StateFlow<AppGoal> =
    goalPreferences?.goal ?: MutableStateFlow(AppGoal()).asStateFlow()
  val goal: StateFlow<AppGoal?> = appGoal

  fun updateGoal(newGoal: AppGoal) {
    goalPreferences?.updateGoal(newGoal)
  }

  fun setGoal(title: String, targetDate: String) {
    goalPreferences?.updateGoal(AppGoal(title = title, examDate = targetDate))
  }

  fun clearGoal() {
    goalPreferences?.updateGoal(AppGoal(title = "", examDate = DateUtils.today()))
  }

  // App Theme Selection State (Slate, Indigo, Blue, Cyan, Purple, Rose, Crimson, Amber, Golden, Obsidian, Graphite, Emerald)
  private val _fallbackThemeColor = MutableStateFlow(AppThemeColor.SLATE)
  val selectedThemeColor: StateFlow<AppThemeColor> = _fallbackThemeColor.asStateFlow()

  private val _fallbackFontColor = MutableStateFlow(com.example.ui.theme.AppFontColor.DEFAULT)
  val selectedFontColor: StateFlow<com.example.ui.theme.AppFontColor> = _fallbackFontColor.asStateFlow()

  private val _fallbackBgImageUri = MutableStateFlow<String?>(null)
  val selectedBackgroundImageUri: StateFlow<String?> =
    themePreferences?.backgroundImageUri ?: _fallbackBgImageUri.asStateFlow()

  private val _fallbackUiOpacity = MutableStateFlow(0.25f)
  val selectedUiOpacity: StateFlow<Float> =
    themePreferences?.uiOpacity ?: _fallbackUiOpacity.asStateFlow()

  private val _fallbackTextSizeScale = MutableStateFlow(1.0f)
  val selectedTextSizeScale: StateFlow<Float> =
    themePreferences?.textSizeScale ?: _fallbackTextSizeScale.asStateFlow()

  private val _fallbackAutoCalendarSync = MutableStateFlow(false)
  val autoCalendarSyncEnabled: StateFlow<Boolean> =
    themePreferences?.autoCalendarSync ?: _fallbackAutoCalendarSync.asStateFlow()

  fun setAutoCalendarSyncEnabled(enabled: Boolean) {
    if (themePreferences != null) {
      themePreferences.setAutoCalendarSync(enabled)
    } else {
      _fallbackAutoCalendarSync.value = enabled
    }
  }

  fun setUiOpacity(opacity: Float) {
    if (themePreferences != null) {
      themePreferences.setUiOpacity(opacity)
    } else {
      _fallbackUiOpacity.value = opacity.coerceIn(0.05f, 1.0f)
    }
  }

  fun setTextSizeScale(scale: Float) {
    if (themePreferences != null) {
      themePreferences.setTextSizeScale(scale)
    } else {
      _fallbackTextSizeScale.value = scale.coerceIn(0.80f, 1.35f)
    }
  }

  fun setThemeColor(color: AppThemeColor) {
    _fallbackThemeColor.value = color
  }

  fun setFontColor(color: com.example.ui.theme.AppFontColor) {
    _fallbackFontColor.value = color
  }

  fun setBackgroundImageUri(uri: String?) {
    if (themePreferences != null) {
      themePreferences.setBackgroundImageUri(uri)
    } else {
      _fallbackBgImageUri.value = uri
    }
  }

  // Chapter Buttons Customization Preferences
  val chapterButtonPreferences: ChapterButtonPreferences? = appContext?.let { ChapterButtonPreferences(it) }
  val chapterButtons: StateFlow<List<ChapterButtonItem>> =
    chapterButtonPreferences?.buttons ?: MutableStateFlow(ChapterButtonPreferences.DEFAULT_BUTTONS).asStateFlow()

  fun toggleChapterButtonEnabled(buttonId: String) {
    chapterButtonPreferences?.toggleButtonEnabled(buttonId)
  }

  fun addCustomChapterButton(name: String, colorHex: Long, isCounter: Boolean) {
    chapterButtonPreferences?.addCustomButton(name, colorHex, isCounter)
  }

  fun removeChapterButton(buttonId: String) {
    chapterButtonPreferences?.removeButton(buttonId)
  }

  fun resetChapterButtonsToDefaults() {
    chapterButtonPreferences?.resetToDefaults()
  }

  fun toggleChapterCustomFlag(chapter: NeetChapter, buttonId: String) {
    viewModelScope.launch {
      val updated = chapter.toggleCustomFlag(buttonId)
      repository.updateNeetChapter(updated)
    }
  }

  fun updateChapterCustomCounter(chapter: NeetChapter, buttonId: String, delta: Int) {
    viewModelScope.launch {
      val updated = chapter.updateCustomCounter(buttonId, delta)
      repository.updateNeetChapter(updated)
    }
  }

  // Active running timer state (Background-enabled via TimerManager)
  val activeTimerState: StateFlow<com.example.util.ActiveTimerState> = com.example.util.TimerManager.activeTimerState
  val runningTaskId: StateFlow<Long?> = com.example.util.TimerManager.runningTaskId
  val isTimerRunning: StateFlow<Boolean> = com.example.util.TimerManager.isTimerRunning

  // All tasks, ratings, logs, neet scores, planned tasks from DB
  private val allTasksFlow = repository.allTasks
  private val allRatingsFlow = repository.allRatings
  private val allLogsFlow = repository.allLogs

  val allTasks: StateFlow<List<HabitTask>> = repository.allTasks
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
  val allTasksState: StateFlow<List<HabitTask>> = allTasks

  val allLogs: StateFlow<List<HabitTaskLog>> = repository.allLogs
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
  val allLogsState: StateFlow<List<HabitTaskLog>> = allLogs

  val allRatings: StateFlow<List<DayRating>> = repository.allRatings
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  val allNeetScores: StateFlow<List<NeetTestScore>> = repository.allNeetScores
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allPlannedTasks: StateFlow<List<PlannedTask>> = repository.allPlannedTasks
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val defaultTasks: StateFlow<List<HabitTask>> = repository.defaultTasks
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allTaskPresets: StateFlow<List<TaskPreset>> = repository.allTaskPresets
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val presets: StateFlow<List<TaskPreset>> = allTaskPresets
  val allPlanEvents: StateFlow<List<com.example.data.model.PlanEvent>> = repository.allPlanEvents
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allNeetChapters: StateFlow<List<NeetChapter>> = repository.allNeetChapters
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allNeetTallyCounters: StateFlow<List<NeetTallyCounter>> = repository.allNeetTallyCounters
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allNotes: StateFlow<List<NoteEntity>> = repository.allNotes
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
  val allAiChats = repository.allAiChats
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val noteFolderPreferences: NoteFolderPreferences? = appContext?.let { NoteFolderPreferences(it) }
  val customNoteSubjects: StateFlow<List<String>> =
    noteFolderPreferences?.customSubjects ?: MutableStateFlow(emptyList<String>()).asStateFlow()
  val customNoteSubfolders: StateFlow<Map<String, List<String>>> =
    noteFolderPreferences?.customSubfolders ?: MutableStateFlow(emptyMap<String, List<String>>()).asStateFlow()

  init {
    // Initialize TimerManager with context and repository to restore any active session robustly
    appContext?.let {
      com.example.util.TimerManager.init(it, repository)
    }

    // Populate default NEET chapters (including Class 11 and Class 12), Tally counters, and Initial Presets
    viewModelScope.launch {
      val existingPresets = repository.allTaskPresets.first()
      if (existingPresets.isEmpty()) {
        val initialPresets = TaskPreset.DEFAULT_PRESETS.map { TaskPreset(name = it) }
        repository.insertAllTaskPresets(initialPresets)
      }
      val existingChapters = repository.allNeetChapters.first()
      if (existingChapters.isEmpty()) {
        repository.insertAllNeetChapters(NeetChapter.DEFAULT_CHAPTERS)
      } else {
        // Automatically insert any missing Class 12 or new default chapters
        val existingNames = existingChapters.map { it.name.trim().lowercase() }.toSet()
        val missingChapters = NeetChapter.DEFAULT_CHAPTERS.filter {
          !existingNames.contains(it.name.trim().lowercase())
        }
        if (missingChapters.isNotEmpty()) {
          repository.insertAllNeetChapters(missingChapters)
        }
      }
      val existingCounters = repository.allNeetTallyCounters.first()
      if (existingCounters.isEmpty()) {
        repository.insertAllNeetTallyCounters(NeetTallyCounter.DEFAULT_COUNTERS)
      }
      repository.syncLegacyChapterNotesToNotesTable()
      notifyNeetWidgetUpdated()
      notifyTasksWidgetUpdated()
    }

    viewModelScope.launch {
      repository.allNeetChapters.collect {
        notifyNeetWidgetUpdated()
      }
    }

    viewModelScope.launch {
      combine(repository.allTasks, repository.allLogs) { tasks, logs -> tasks.size to logs.count { it.isCompleted } }
        .collect {
          notifyTasksWidgetUpdated()
        }
    }
  }

  private fun notifyNeetWidgetUpdated() {
    appContext?.let { ctx ->
      com.example.widget.NeetProgressAppWidgetProvider.notifyDataChanged(ctx)
    }
  }

  private fun notifyTasksWidgetUpdated() {
    appContext?.let { ctx ->
      com.example.widget.TasksAppWidgetProvider.notifyDataChanged(ctx)
    }
  }

  // Notes Hub & Two-Way NEET Chapter Sync operations
  fun addCustomSubjectFolder(subjectName: String) {
    noteFolderPreferences?.addCustomSubject(subjectName)
  }

  fun addCustomChapterSubfolder(subjectName: String, subfolderName: String) {
    noteFolderPreferences?.addCustomSubfolder(subjectName, subfolderName)
  }

  fun saveNote(note: NoteEntity, onSaved: (Long) -> Unit = {}) {
    viewModelScope.launch {
      if (note.subject.isNotBlank() && NeetChapter.SUBJECTS.none { it.equals(note.subject, ignoreCase = true) }) {
        noteFolderPreferences?.addCustomSubject(note.subject)
      }
      if (note.subject.isNotBlank() && note.chapterName.isNotBlank() && note.chapterId == null) {
        noteFolderPreferences?.addCustomSubfolder(note.subject, note.chapterName)
      }
      val id = repository.saveNoteWithChapterSync(note)
      onSaved(id)
    }
  }

  fun deleteNote(note: NoteEntity) {
    viewModelScope.launch {
      note.getAttachments().forEach { att ->
        NoteMediaUtils.deleteMediaFileIfLocal(att.filePath)
      }
      repository.deleteNoteWithChapterSync(note)
    }
  }

  // Combined UI State for currently selected date (Tasks sorted: Incomplete first, Finished at bottom)
  val tasksForSelectedDate: StateFlow<List<TaskItemUiState>> = combine(
    allTasksFlow,
    allLogsFlow,
    selectedDate,
    activeTimerState
  ) { tasks, logs, date, timerState ->
    val dayOfWeekIdx = DateUtils.getDayOfWeekIndex(date)
    val logsForDate = logs.filter { it.date == date }.associateBy { it.taskId }

    // Filter tasks scheduled specifically for this date or recurring on this day
    val matchedHabitTasks = tasks.filter { it.isScheduledFor(date, dayOfWeekIdx) }

    val list = matchedHabitTasks.map { task ->
      val log = logsForDate[task.id]
      val isRunning = (timerState.isRunning && timerState.taskId == task.id && timerState.date == date)
      val dbSeconds = log?.timeSpentSeconds ?: 0L
      val effectiveSeconds = com.example.util.TimerManager.getEffectiveTaskSeconds(task.id, date, dbSeconds)
      val isCompleted = log?.isCompleted ?: false

      TaskItemUiState(
        task = task,
        timeSpentSeconds = effectiveSeconds,
        isCompleted = isCompleted,
        isRunning = isRunning
      )
    }

    // Move finished tasks to the bottom, keeping stable order so tasks don't jump around when played
    list.sortedWith(
      compareBy<TaskItemUiState> { it.isCompleted }
        .thenBy { it.task.id }
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = emptyList()
  )

  // Day Rating for selected date
  val currentDayRating: StateFlow<RatingType?> = combine(
    allRatingsFlow,
    selectedDate
  ) { ratings, date ->
    ratings.find { it.date == date }?.ratingType
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = null
  )

  // Total time spent today (includes running timer session)
  val totalTimeTodaySeconds: StateFlow<Long> = tasksForSelectedDate
    .map { tasks -> tasks.sumOf { it.timeSpentSeconds } }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = 0L
    )

  // Detail screen tab selection & Timeline cycle mode
  val selectedDetailTab = MutableStateFlow(DetailTab.TIMELINE)
  val timelineCycleMode = MutableStateFlow(TimelineCycleMode.DAY)

  fun cycleTimelineMode() {
    timelineCycleMode.value = timelineCycleMode.value.next()
  }

  fun setTimelineMode(mode: TimelineCycleMode) {
    timelineCycleMode.value = mode
  }

  // Analytics tab selection (legacy compatibility)
  val selectedAnalyticsTab = MutableStateFlow(AnalyticsTab.DAY)

  // Effective logs flow including live running session for today
  private val effectiveLogsFlow = combine(
    allLogsFlow,
    activeTimerState,
    selectedDate
  ) { logs, timerState, curDate ->
    val mutable = logs.map { log ->
      val effective = com.example.util.TimerManager.getEffectiveTaskSeconds(log.taskId, log.date, log.timeSpentSeconds)
      if (effective != log.timeSpentSeconds) log.copy(timeSpentSeconds = effective) else log
    }.toMutableList()

    if (timerState.isRunning && timerState.taskId != null) {
      val runningId = timerState.taskId
      val timerDate = timerState.date ?: curDate
      val exists = mutable.any { it.taskId == runningId && it.date == timerDate }
      if (!exists) {
        mutable.add(
          HabitTaskLog(
            taskId = runningId,
            date = timerDate,
            timeSpentSeconds = timerState.currentTotalSeconds,
            isCompleted = false
          )
        )
      }
    }
    mutable
  }

  // Analytics: Days Summary (recent 30 days)
  val daysAnalytics: StateFlow<List<DaySummary>> = combine(
    allRatingsFlow,
    effectiveLogsFlow,
    allTasksFlow
  ) { ratings, logs, tasks ->
    val ratingsMap = ratings.associateBy { it.date }
    val logsMap = logs.groupBy { it.date }
    val today = DateUtils.today()

    val list = mutableListOf<DaySummary>()
    var cur = today
    for (i in 0..29) {
      val dayRatings = ratingsMap[cur]?.ratingType
      val dayLogs = logsMap[cur] ?: emptyList()
      val dayOfWeekIdx = DateUtils.getDayOfWeekIndex(cur)
      val scheduledTasks = tasks.filter { it.isScheduledFor(cur, dayOfWeekIdx) }
      val totalTime = dayLogs.sumOf { it.timeSpentSeconds }
      val completedCount = dayLogs.count { it.isCompleted }

      list.add(
        DaySummary(
          date = cur,
          dayOfWeekAbbr = DateUtils.formatDayOfWeekAbbr(cur),
          dayOfWeekIndex = dayOfWeekIdx,
          rating = dayRatings,
          totalTimeSeconds = totalTime,
          completedTasksCount = completedCount,
          totalTasksCount = scheduledTasks.size
        )
      )
      cur = DateUtils.getPreviousDay(cur)
    }
    list
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  // Analytics: Weeks Summary (current week + previous 5 weeks)
  val weeksAnalytics: StateFlow<List<WeekSummary>> = combine(
    allRatingsFlow,
    effectiveLogsFlow,
    allTasksFlow
  ) { ratings, logs, tasks ->
    val ratingsMap = ratings.associateBy { it.date }
    val logsMap = logs.groupBy { it.date }
    val today = DateUtils.today()

    val weeksList = mutableListOf<WeekSummary>()
    var anchorDate = today

    for (w in 0..5) {
      val daysInWeek = DateUtils.getDaysInWeekForDate(anchorDate)
      val daySummaries = daysInWeek.map { dayDate ->
        val dayRating = ratingsMap[dayDate]?.ratingType
        val dayLogs = logsMap[dayDate] ?: emptyList()
        val dayOfWeekIdx = DateUtils.getDayOfWeekIndex(dayDate)
        val scheduledTasks = tasks.filter { it.isScheduledFor(dayDate, dayOfWeekIdx) }

        DaySummary(
          date = dayDate,
          dayOfWeekAbbr = DateUtils.formatDayOfWeekAbbr(dayDate),
          dayOfWeekIndex = dayOfWeekIdx,
          rating = dayRating,
          totalTimeSeconds = dayLogs.sumOf { it.timeSpentSeconds },
          completedTasksCount = dayLogs.count { it.isCompleted },
          totalTasksCount = scheduledTasks.size
        )
      }

      val bestCount = daySummaries.count { it.rating == RatingType.BEST }
      val averageCount = daySummaries.count { it.rating == RatingType.AVERAGE }
      val worstCount = daySummaries.count { it.rating == RatingType.WORST }
      val totalTime = daySummaries.sumOf { it.totalTimeSeconds }
      val completed = daySummaries.sumOf { it.completedTasksCount }
      val totalTasks = daySummaries.sumOf { it.totalTasksCount }

      val startFormatted = DateUtils.formatShortDate(daysInWeek.first())
      val endFormatted = DateUtils.formatShortDate(daysInWeek.last())
      val label = if (w == 0) "This Week ($startFormatted - $endFormatted)" else "$startFormatted - $endFormatted"

      weeksList.add(
        WeekSummary(
          weekLabel = label,
          startDate = daysInWeek.first(),
          endDate = daysInWeek.last(),
          days = daySummaries,
          bestCount = bestCount,
          averageCount = averageCount,
          worstCount = worstCount,
          totalTimeSeconds = totalTime,
          completedTasksCount = completed,
          totalTasksCount = totalTasks
        )
      )

      anchorDate = DateUtils.getDaysInWeekForDate(anchorDate).first().let {
        DateUtils.getPreviousDay(it)
      }
    }
    weeksList
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  // Analytics: Month Summary
  val monthsAnalytics: StateFlow<List<MonthSummary>> = combine(
    allRatingsFlow,
    effectiveLogsFlow,
    allTasksFlow
  ) { ratings, logs, tasks ->
    val ratingsMap = ratings.associateBy { it.date }
    val logsMap = logs.groupBy { it.date }

    val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val monthTitleSdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val cal = Calendar.getInstance()

    val monthSummaries = mutableListOf<MonthSummary>()
    for (m in 0..3) {
      val ym = sdf.format(cal.time)
      val monthLabel = monthTitleSdf.format(cal.time)
      val daysInMonth = DateUtils.getDaysInCurrentMonth(ym)

      val daySummaries = daysInMonth.map { dayDate ->
        val dayRating = ratingsMap[dayDate]?.ratingType
        val dayLogs = logsMap[dayDate] ?: emptyList()
        val dayOfWeekIdx = DateUtils.getDayOfWeekIndex(dayDate)
        val scheduledTasks = tasks.filter { it.isScheduledFor(dayDate, dayOfWeekIdx) }

        DaySummary(
          date = dayDate,
          dayOfWeekAbbr = DateUtils.formatDayOfWeekAbbr(dayDate),
          dayOfWeekIndex = dayOfWeekIdx,
          rating = dayRating,
          totalTimeSeconds = dayLogs.sumOf { it.timeSpentSeconds },
          completedTasksCount = dayLogs.count { it.isCompleted },
          totalTasksCount = scheduledTasks.size
        )
      }

      val bestCount = daySummaries.count { it.rating == RatingType.BEST }
      val averageCount = daySummaries.count { it.rating == RatingType.AVERAGE }
      val worstCount = daySummaries.count { it.rating == RatingType.WORST }
      val totalTime = daySummaries.sumOf { it.totalTimeSeconds }
      val completed = daySummaries.sumOf { it.completedTasksCount }
      val totalTasks = daySummaries.sumOf { it.totalTasksCount }

      monthSummaries.add(
        MonthSummary(
          monthLabel = monthLabel,
          yearMonth = ym,
          days = daySummaries,
          bestCount = bestCount,
          averageCount = averageCount,
          worstCount = worstCount,
          totalTimeSeconds = totalTime,
          completedTasksCount = completed,
          totalTasksCount = totalTasks
        )
      )

      cal.add(Calendar.MONTH, -1)
    }
    monthSummaries
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  // Tally counter for repeated tasks in Analytics
  val taskTallyAnalytics: StateFlow<List<TaskTallyItem>> = combine(
    allTasksFlow,
    effectiveLogsFlow
  ) { tasks, logs ->
    val logsByTask = logs.groupBy { it.taskId }
    tasks.map { task ->
      val taskLogs = logsByTask[task.id] ?: emptyList()
      val completions = taskLogs.count { it.isCompleted }
      val totalTime = taskLogs.sumOf { it.timeSpentSeconds }
      TaskTallyItem(
        taskId = task.id,
        taskName = task.name,
        completionCount = completions,
        totalTimeSeconds = totalTime,
        totalTargetMinutes = task.targetTimeMinutes
      )
    }.sortedByDescending { it.completionCount }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  // Past Tasks grouped by date (all dates prior to today)
  val pastTasksGroups: StateFlow<List<PastDayTasksGroup>> = combine(
    allTasksFlow,
    effectiveLogsFlow,
    allPlannedTasks
  ) { tasks, logs, plannedTasks ->
    val today = DateUtils.today()
    val logsByDate = logs.groupBy { it.date }
    val plannedByDate = plannedTasks.groupBy { it.date }
    val tasksMap = tasks.associateBy { it.id }

    // Collect all unique past dates where logs, planned tasks, or scheduled tasks exist
    val pastDates = mutableSetOf<String>()
    logsByDate.keys.filter { it < today }.forEach { pastDates.add(it) }
    plannedByDate.keys.filter { it < today }.forEach { pastDates.add(it) }

    // Also look at past 60 days to catch any scheduled tasks
    var checkDate = DateUtils.getPreviousDay(today)
    for (i in 0..59) {
      val dayIdx = DateUtils.getDayOfWeekIndex(checkDate)
      if (tasks.any { it.isScheduledFor(checkDate, dayIdx) }) {
        pastDates.add(checkDate)
      }
      checkDate = DateUtils.getPreviousDay(checkDate)
    }

    pastDates.sortedDescending().mapNotNull { date ->
      val dayLogs = logsByDate[date]?.associateBy { it.taskId } ?: emptyMap()
      val dayOfWeekIdx = DateUtils.getDayOfWeekIndex(date)

      // Scheduled tasks for this date
      val scheduledTasks = tasks.filter { it.isScheduledFor(date, dayOfWeekIdx) }
      val scheduledTaskIds = scheduledTasks.map { it.id }.toSet()

      // Logged tasks that might not be in scheduled list
      val loggedExtraTasks = (logsByDate[date] ?: emptyList())
        .filter { !scheduledTaskIds.contains(it.taskId) }
        .mapNotNull { log -> tasksMap[log.taskId] }

      val allDayTasks = (scheduledTasks + loggedExtraTasks).distinctBy { it.id }

      val items = allDayTasks.map { task ->
        val log = dayLogs[task.id]
        PastTaskUiItem(
          taskId = task.id,
          taskName = task.name,
          date = date,
          timeSpentSeconds = log?.timeSpentSeconds ?: 0L,
          isCompleted = log?.isCompleted ?: false,
          targetMinutes = task.targetTimeMinutes,
          noteText = task.noteText
        )
      }

      if (items.isNotEmpty()) {
        PastDayTasksGroup(
          date = date,
          formattedDate = DateUtils.formatFullDate(date),
          totalTasksCount = items.size,
          completedTasksCount = items.count { it.isCompleted },
          totalTimeSeconds = items.sumOf { it.timeSpentSeconds },
          tasks = items
        )
      } else {
        null
      }
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  // Subject Time Breakdown (Physics, Chemistry, Botany, Zoology, General Habits)
  // AI decides time dedication and allocation based on task name using on-device semantic classification
  val subjectTimeBreakdown: StateFlow<List<SubjectTimeBreakdown>> = combine(
    allTasksFlow,
    effectiveLogsFlow
  ) { tasks, logs ->
    val logsByTask = logs.groupBy { it.taskId }
    var physicsSecs = 0L
    var chemistrySecs = 0L
    var botanySecs = 0L
    var zoologySecs = 0L
    var habitsSecs = 0L

    for (task in tasks) {
      val taskLogs = logsByTask[task.id] ?: emptyList()
      val totalSecs = taskLogs.sumOf { it.timeSpentSeconds }
      val category = OnDeviceSubjectClassifier.classify(task.name, task.noteText)

      when (category) {
        OnDeviceSubjectClassifier.SubjectCategory.PHYSICS -> physicsSecs += totalSecs
        OnDeviceSubjectClassifier.SubjectCategory.CHEMISTRY -> chemistrySecs += totalSecs
        OnDeviceSubjectClassifier.SubjectCategory.BOTANY -> botanySecs += totalSecs
        OnDeviceSubjectClassifier.SubjectCategory.ZOOLOGY -> zoologySecs += totalSecs
        OnDeviceSubjectClassifier.SubjectCategory.HABITS_TASKS -> habitsSecs += totalSecs
      }
    }

    val grandTotal = physicsSecs + chemistrySecs + botanySecs + zoologySecs + habitsSecs
    val denom = if (grandTotal > 0L) grandTotal.toFloat() else 1f

    listOf(
      SubjectTimeBreakdown("Physics", physicsSecs, DateUtils.formatTime(physicsSecs), if (grandTotal > 0) (physicsSecs / denom) * 100f else 0f),
      SubjectTimeBreakdown("Chemistry", chemistrySecs, DateUtils.formatTime(chemistrySecs), if (grandTotal > 0) (chemistrySecs / denom) * 100f else 0f),
      SubjectTimeBreakdown("Botany", botanySecs, DateUtils.formatTime(botanySecs), if (grandTotal > 0) (botanySecs / denom) * 100f else 0f),
      SubjectTimeBreakdown("Zoology", zoologySecs, DateUtils.formatTime(zoologySecs), if (grandTotal > 0) (zoologySecs / denom) * 100f else 0f),
      SubjectTimeBreakdown("Habits & Tasks", habitsSecs, DateUtils.formatTime(habitsSecs), if (grandTotal > 0) (habitsSecs / denom) * 100f else 0f)
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  // AI-decided Task Subject Attributions: maps each task to its AI-allocated subject
  val taskSubjectAttributions: StateFlow<List<TaskSubjectAttribution>> = combine(
    allTasksFlow,
    effectiveLogsFlow
  ) { tasks, logs ->
    val logsByTask = logs.groupBy { it.taskId }
    tasks.map { task ->
      val taskLogs = logsByTask[task.id] ?: emptyList()
      val totalSecs = taskLogs.sumOf { it.timeSpentSeconds }
      val category = OnDeviceSubjectClassifier.classify(task.name, task.noteText)
      TaskSubjectAttribution(
        taskId = task.id,
        taskName = task.name,
        subject = category.displayName,
        timeSpentSeconds = totalSecs,
        formattedTime = DateUtils.formatTime(totalSecs),
        colorHex = category.colorHex
      )
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  // AI Chat and Analytics Assistant
  private val suggestionsRefreshTrigger = MutableStateFlow(0)

  fun refreshSuggestions() {
    suggestionsRefreshTrigger.value += 1
  }

  val aiChatMessages: StateFlow<List<AiChatMessage>> = repository.allAiChats
    .map { entities ->
      val mapped = entities.map { entity ->
        AiChatMessage(
          id = entity.id,
          role = entity.role,
          text = entity.text,
          timestamp = entity.timestamp,
          isError = entity.isError,
          modelMode = if (entity.isCloud) "cloud" else "on_device",
          isPinned = entity.isPinned
        )
      }
      if (mapped.isEmpty()) {
        listOf(
          AiChatMessage(
            role = "model",
            text = "👋 Hello! I am your AI NEET Mentor & Analytics Coach.\n\nI can analyze your logged study hours, time percentage allocations, mock test score trends, NCERT chapter revisions, and PYQ progress. Ask me anything like:\n• \"Which chapters am I struggling with?\"\n• \"How much time in hours and percentage have I spent on Physics vs Bio?\"\n• \"How to approach Organic Chemistry mechanisms without forgetting?\"\n• \"What is the best active recall strategy for NEET Biology?\"",
            timestamp = 0L // oldest to stay at the very bottom
          )
        )
      } else {
        mapped + AiChatMessage(
          role = "model",
          text = "👋 Hello! I am your AI NEET Mentor & Analytics Coach.\n\nI can analyze your logged study hours, time percentage allocations, mock test score trends, NCERT chapter revisions, and PYQ progress. Ask me anything like:\n• \"Which chapters am I struggling with?\"\n• \"How much time in hours and percentage have I spent on Physics vs Bio?\"\n• \"How to approach Organic Chemistry mechanisms without forgetting?\"\n• \"What is the best active recall strategy for NEET Biology?\"",
          timestamp = 0L // oldest to stay at the very bottom
        )
      }
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val isAiLoading = MutableStateFlow(false)
  private val _fallbackAiChatDraft = MutableStateFlow("")
  val aiChatDraft: StateFlow<String> =
    themePreferences?.aiChatDraft ?: _fallbackAiChatDraft.asStateFlow()

  fun setAiChatDraft(draft: String) {
    if (themePreferences != null) {
      themePreferences.setAiChatDraft(draft)
    } else {
      _fallbackAiChatDraft.value = draft
    }
  }

  // Dynamic AI Suggestions (Universal + Personalized from actual student data)
  val dynamicAiSuggestions: StateFlow<List<String>> = combine(
    allNeetChapters,
    allNeetScores,
    subjectTimeBreakdown,
    daysAnalytics,
    suggestionsRefreshTrigger
  ) { chapters, scores, subjectTimes, days, trigger ->
    val list = mutableListOf<String>()

    // 1. Personalized Suggestions from Chapters:
    val incompleteChapters = chapters.filter { !it.isCompleted }
    if (incompleteChapters.isNotEmpty()) {
      val sample = incompleteChapters.firstOrNull()
      if (sample != null) {
        list.add("How to finish \"${sample.name}\" (${sample.subject}) quickly?")
      }
    }
    val unrevisedChapters = chapters.filter { it.isCompleted && !it.isRevisionDone }
    if (unrevisedChapters.isNotEmpty()) {
      list.add("Spaced repetition schedule for ${unrevisedChapters.size} completed chapters")
    }

    // 2. Personalized Suggestions from Scores:
    if (scores.isNotEmpty()) {
      val latest = scores.maxByOrNull { it.date }
      if (latest != null) {
        list.add("My latest mock score is ${latest.totalScore}/720. How to score 650+?")
        val subjects = listOf("Physics" to latest.physicsScore, "Chemistry" to latest.chemistryScore, "Botany" to latest.botanyScore, "Zoology" to latest.zoologyScore)
        val lowestSub = subjects.minByOrNull { it.second }
        if (lowestSub != null) {
          list.add("Improve ${lowestSub.first} marks from ${lowestSub.second}/180")
        }
      }
    }

    // 3. Personalized Suggestions from Time Breakdown:
    val lowTimeSubject = subjectTimes.filter { it.name != "Habits & Tasks" }.minByOrNull { it.percentage }
    if (lowTimeSubject != null && lowTimeSubject.percentage < 25f && lowTimeSubject.seconds > 0L) {
      list.add("Balance study hours for ${lowTimeSubject.name} (${String.format(Locale.getDefault(), "%.0f", lowTimeSubject.percentage)}% total)")
    }

    // 4. Universal High-Yield NEET suggestions:
    list.add("Analyze my NEET readiness & weekly consistency")
    list.add("Top high-yield NCERT Biology topics to revise")
    list.add("Physics formula memorization & problem-solving strategy")
    list.add("How to avoid negative marking in mock tests?")

    val uniqueList = list.distinct()
    // Implement smart shuffling or rotation on trigger to keep suggestions active and dynamic!
    if (trigger > 0 && uniqueList.size > 2) {
      val shuffled = uniqueList.shuffled(java.util.Random(trigger.toLong()))
      shuffled.take(4)
    } else {
      uniqueList.take(4)
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = listOf(
      "Analyze my NEET readiness & weekly consistency",
      "Top high-yield NCERT Biology topics to revise",
      "Physics formula memorization & problem-solving strategy",
      "Spaced repetition schedule for completed chapters"
    )
  )

  fun sendAiQuestion(userQuestion: String, askCloud: Boolean = false) {
    if (userQuestion.isBlank() || isAiLoading.value) return
    val cleanQ = userQuestion.trim()
    setAiChatDraft("")
    isAiLoading.value = true

    viewModelScope.launch {
      // 1. Insert user message to local Room DB
      val userTime = System.currentTimeMillis()
      repository.insertAiChat(
        com.example.data.model.AiChatEntity(
          role = "user",
          text = cleanQ,
          timestamp = userTime,
          isCloud = askCloud
        )
      )

      // 2. Generate response (either on-device or cloud)
      val responseText = if (askCloud) {
        val contextString = buildStudyContext()
        val currentHistory = aiChatMessages.value
        com.example.util.GeminiAiService.askGemini(cleanQ, contextString, currentHistory)
      } else {
        // On-Device Model
        val tasks = allTasks.value
        val logs = allLogs.value
        val ratings = allRatings.value
        val planned = allPlannedTasks.value
        val chapters = allNeetChapters.value
        val scores = allNeetScores.value
        val tallies = allNeetTallyCounters.value
        val subjectTimes = subjectTimeBreakdown.value
        // Simulate a slight on-device computation delay (600ms) for realistic feel
        delay(600)
        com.example.util.OnDeviceModelEngine.generateResponse(
          question = cleanQ,
          tasks = tasks,
          logs = logs,
          ratings = ratings,
          plannedTasks = planned,
          chapters = chapters,
          scores = scores,
          tallies = tallies,
          subjectTimes = subjectTimes
        )
      }

      // 3. Insert model response to local Room DB
      repository.insertAiChat(
        com.example.data.model.AiChatEntity(
          role = "model",
          text = responseText,
          timestamp = System.currentTimeMillis(),
          isCloud = askCloud
        )
      )

      isAiLoading.value = false
      refreshSuggestions()
    }
  }

  fun clearAiChat() {
    viewModelScope.launch {
      repository.clearAiChatHistory()
      refreshSuggestions()
    }
  }

  fun deleteAiChatMessage(id: Long) {
    viewModelScope.launch {
      repository.deleteAiChatById(id)
      refreshSuggestions()
    }
  }

  fun deleteAiChatMessages(ids: Set<Long>) {
    viewModelScope.launch {
      repository.deleteAiChatsByIds(ids.toList())
      refreshSuggestions()
    }
  }

  fun togglePinAiChatMessage(id: Long, currentPinned: Boolean) {
    viewModelScope.launch {
      repository.setAiChatPinned(id, !currentPinned)
    }
  }

  fun buildStudyContext(): String {
    val scores = allNeetScores.value
    val chapters = allNeetChapters.value
    val tallies = allNeetTallyCounters.value
    val subjectTimes = subjectTimeBreakdown.value
    val totalTime = tasksForSelectedDate.value.sumOf { it.timeSpentSeconds }

    val sb = StringBuilder()
    sb.appendLine("=== STUDENT STUDY METRICS & PROFILE ===")

    // Time dedication breakdown
    sb.appendLine("\n--- Study Time Dedication (Hours & %) ---")
    subjectTimes.forEach { sub ->
      val hours = sub.seconds / 3600.0
      val hoursStr = String.format(Locale.getDefault(), "%.1fh", hours)
      sb.appendLine("• ${sub.name}: $hoursStr (${DateUtils.formatTime(sub.seconds)}) -> ${String.format(Locale.getDefault(), "%.1f", sub.percentage)}%")
    }

    // NEET Chapter Progress
    sb.appendLine("\n--- NEET Chapters Completion (Total: ${chapters.size}) ---")
    val completedCount = chapters.count { it.isCompleted }
    val pyqCount = chapters.count { it.isPyqDone }
    val revCount = chapters.count { it.isRevisionDone }
    sb.appendLine("• Completed Chapters: $completedCount / ${chapters.size}")
    sb.appendLine("• PYQs Solved Chapters: $pyqCount / ${chapters.size}")
    sb.appendLine("• Revised Chapters: $revCount / ${chapters.size}")

    val pendingChapters = chapters.filter { !it.isCompleted }.take(10).map { "${it.name} (${it.subject})" }
    if (pendingChapters.isNotEmpty()) {
      sb.appendLine("• Sample Incomplete Chapters: ${pendingChapters.joinToString(", ")}")
    }

    // Mock Test Scores
    sb.appendLine("\n--- Mock Test Performance (Recent Tests: ${scores.size}) ---")
    if (scores.isEmpty()) {
      sb.appendLine("• No mock tests logged yet.")
    } else {
      scores.takeLast(5).forEach { sc ->
        sb.appendLine("• Test '${sc.testName}' on ${sc.date}: Total=${sc.totalScore}/720 (Physics=${sc.physicsScore}/180, Chem=${sc.chemistryScore}/180, Botany=${sc.botanyScore}/180, Zoology=${sc.zoologyScore}/180)")
      }
      val avgTotal = scores.map { it.totalScore }.average().toInt()
      val avgPhysics = scores.map { it.physicsScore }.average().toInt()
      val avgChem = scores.map { it.chemistryScore }.average().toInt()
      val avgBio = scores.map { it.botanyScore + it.zoologyScore }.average().toInt()
      sb.appendLine("• Averages: Total=$avgTotal/720 | Physics=$avgPhysics/180 | Chem=$avgChem/180 | Bio=$avgBio/360")
    }

    // Tally Counters
    sb.appendLine("\n--- Tally Counters & Practice Goals ---")
    tallies.forEach { t ->
      sb.appendLine("• ${t.title}: ${t.count} / ${t.target} ${t.unit}")
    }

    return sb.toString()
  }

  // Navigation across days
  fun selectDate(date: String) {
    flushActiveTimer()
    selectedDate.value = date
  }

  fun goToPreviousDay() {
    flushActiveTimer()
    selectedDate.value = DateUtils.getPreviousDay(selectedDate.value)
  }

  fun goToNextDay() {
    flushActiveTimer()
    selectedDate.value = DateUtils.getNextDay(selectedDate.value)
  }

  fun goToToday() {
    flushActiveTimer()
    selectedDate.value = DateUtils.today()
  }

  // Day Rating Action
  fun setDayRating(ratingType: RatingType) {
    viewModelScope.launch {
      repository.setDayRating(selectedDate.value, ratingType)
    }
  }

  // Task Actions
  fun addTask(
    name: String,
    targetDates: Set<String> = emptySet(),
    repeatDaysMask: Int = 0,
    targetTimeMinutes: Int = 0,
    isDefault: Boolean = false,
    isStarred: Boolean = false,
    noteText: String = "",
    noteImageUri: String? = null,
    reminderTime: String? = null
  ) {
    if (name.isBlank()) return
    viewModelScope.launch {
      val curDate = selectedDate.value

      if (targetDates.isNotEmpty()) {
        // Multi-day selection option
        targetDates.forEach { date ->
          val newId = repository.insertTask(
            name = name.trim(),
            targetDate = date,
            repeatDaysMask = 0,
            targetTimeMinutes = targetTimeMinutes,
            isDefault = false,
            isStarred = isStarred,
            noteText = noteText.trim(),
            noteImageUri = noteImageUri,
            reminderTime = reminderTime
          )
          if (!reminderTime.isNullOrBlank() && appContext != null) {
            val taskForAlarm = HabitTask(
              id = newId,
              name = name.trim(),
              targetDate = date,
              reminderTime = reminderTime
            )
            AlarmScheduler.scheduleAlarm(appContext, taskForAlarm)
          }
          val allPlans = repository.allPlannedTasks.first()
          val existsInPlan = allPlans.any {
            it.title.equals(name.trim(), ignoreCase = true) && it.date == date
          }
          if (!existsInPlan) {
            repository.insertPlannedTask(
              PlannedTask(
                title = name.trim(),
                date = date,
                targetTimeMinutes = targetTimeMinutes,
                notes = noteText.trim(),
                isStarred = isStarred,
                isCompleted = false
              )
            )
          }
        }
      } else {
        // If no repeat days mask is specified and not marked as recurring default,
        // the task is specific to this selected date!
        val specificDate = if (!isDefault && repeatDaysMask == 0) curDate else null

        val newId = repository.insertTask(
          name = name.trim(),
          targetDate = specificDate,
          repeatDaysMask = repeatDaysMask,
          targetTimeMinutes = targetTimeMinutes,
          isDefault = isDefault,
          isStarred = isStarred,
          noteText = noteText.trim(),
          noteImageUri = noteImageUri,
          reminderTime = reminderTime
        )

        if (!reminderTime.isNullOrBlank() && appContext != null) {
          val taskForAlarm = HabitTask(
            id = newId,
            name = name.trim(),
            targetDate = specificDate,
            repeatDaysMask = repeatDaysMask,
            reminderTime = reminderTime
          )
          AlarmScheduler.scheduleAlarm(appContext, taskForAlarm)
        }

        // Sync: task added in tracker should also appear in plan for that date!
        val allPlans = repository.allPlannedTasks.first()
        val existsInPlan = allPlans.any {
          it.title.equals(name.trim(), ignoreCase = true) && it.date == curDate
        }
        if (!existsInPlan) {
          repository.insertPlannedTask(
            PlannedTask(
              title = name.trim(),
              date = curDate,
              targetTimeMinutes = targetTimeMinutes,
              notes = noteText.trim(),
              isStarred = isStarred,
              isCompleted = false
            )
          )
        }
      }
    }
  }

  fun updateTask(task: HabitTask) {
    viewModelScope.launch {
      repository.updateTask(task)
      if (appContext != null) {
        if (!task.reminderTime.isNullOrBlank()) {
          AlarmScheduler.scheduleAlarm(appContext, task)
        } else {
          AlarmScheduler.cancelAlarm(appContext, task.id)
        }
      }
    }
  }

  fun updateTaskTargetTimer(task: HabitTask, newTargetMinutes: Int) {
    viewModelScope.launch {
      repository.updateTask(task.copy(targetTimeMinutes = newTargetMinutes))
    }
  }

  fun deleteTask(taskId: Long) {
    if (com.example.util.TimerManager.isTimerRunning(taskId)) {
      com.example.util.TimerManager.stopTimer()
    }
    viewModelScope.launch {
      val allTasks = allTasksFlow.stateIn(viewModelScope).value
      val task = allTasks.find { it.id == taskId }
      repository.deleteTaskById(taskId)

      // If task was for a specific date, also delete any corresponding planned task
      if (task != null && task.targetDate != null) {
        val plans = repository.allPlannedTasks.stateIn(viewModelScope).value
        val matchedPlan = plans.find {
          it.title.equals(task.name, ignoreCase = true) && it.date == task.targetDate
        }
        if (matchedPlan != null) {
          repository.deletePlannedTaskById(matchedPlan.id)
        }
      }
    }
  }

  fun toggleTaskComplete(taskId: Long) {
    val curDate = selectedDate.value
    // If the task being completed currently has a running timer, stop and save it
    if (com.example.util.TimerManager.isTimerRunning(taskId, curDate)) {
      com.example.util.TimerManager.stopTimer()
    }
    viewModelScope.launch {
      repository.toggleTaskComplete(taskId, curDate)

      // Sync completion with PlannedTask if one exists for curDate
      val allTasks = allTasksFlow.stateIn(viewModelScope).value
      val task = allTasks.find { it.id == taskId }
      if (task != null) {
        val plans = repository.allPlannedTasks.stateIn(viewModelScope).value
        val matchedPlan = plans.find {
          it.title.equals(task.name, ignoreCase = true) && it.date == curDate
        }
        if (matchedPlan != null) {
          val logs = repository.getLogsForDate(curDate).stateIn(viewModelScope).value
          val isDone = logs.find { it.taskId == taskId }?.isCompleted ?: true
          repository.updatePlannedTask(matchedPlan.copy(isCompleted = isDone))
        }
      }
    }
  }

  fun toggleTaskCompleteForDate(taskId: Long, date: String) {
    if (com.example.util.TimerManager.isTimerRunning(taskId, date)) {
      com.example.util.TimerManager.stopTimer()
    }
    viewModelScope.launch {
      repository.toggleTaskComplete(taskId, date)
    }
  }

  // Timer controls with Background Tracking
  fun toggleTimer(taskId: Long) {
    val curDate = selectedDate.value
    val current = com.example.util.TimerManager.activeTimerState.value
    if (current.taskId == taskId && current.date == curDate) {
      if (current.isRunning) {
        com.example.util.TimerManager.pauseTimer()
      } else {
        com.example.util.TimerManager.resumeTimer()
      }
    } else {
      val taskItem = tasksForSelectedDate.value.find { it.task.id == taskId }
      val taskName = taskItem?.task?.name
        ?: allTasksState.value.find { it.id == taskId }?.name
        ?: "Task"
      val effectiveInitial = taskItem?.timeSpentSeconds
        ?: com.example.util.TimerManager.getEffectiveTaskSeconds(taskId, curDate, 0L)
      com.example.util.TimerManager.startTimer(taskId, taskName, curDate, effectiveInitial)
    }
  }

  fun pauseTimer() {
    com.example.util.TimerManager.pauseTimer()
  }

  fun resumeTimer() {
    com.example.util.TimerManager.resumeTimer()
  }

  fun stopActiveTimer() {
    com.example.util.TimerManager.stopTimer()
  }

  private fun flushActiveTimer() {
    com.example.util.TimerManager.flushTimer()
  }

  // NEET Test Score actions
  fun addNeetScore(score: NeetTestScore) {
    viewModelScope.launch {
      repository.insertNeetScore(score)
    }
  }

  fun deleteNeetScore(score: NeetTestScore) {
    viewModelScope.launch {
      repository.deleteNeetScore(score)
    }
  }

  // Planned Task actions (Sync with Tracker!)
  fun addPlannedTask(task: PlannedTask) {
    viewModelScope.launch {
      repository.insertPlannedTask(task)

      // Automatically sync with device calendar if enabled
      if (autoCalendarSyncEnabled.value && appContext != null) {
        com.example.util.CalendarSyncUtil.syncEventToDeviceCalendar(
          context = appContext,
          title = task.title,
          description = task.notes.ifBlank { "Planned task inside Habit Tracker app" },
          startDateStr = task.date,
          endDateStr = task.date,
          targetTimeMinutes = task.targetTimeMinutes
        )
      }

      // Also create a HabitTask for that specific date so it appears in Tracker on that date!
      val allTasks = allTasksFlow.stateIn(viewModelScope).value
      val existing = allTasks.find {
        it.name.equals(task.title.trim(), ignoreCase = true) &&
          (it.targetDate == task.date || it.isDefault)
      }
      if (existing == null) {
        repository.insertTask(
          name = task.title.trim(),
          targetDate = task.date, // Specific to this date only!
          repeatDaysMask = 0,
          targetTimeMinutes = task.targetTimeMinutes,
          isDefault = false,
          isStarred = task.isStarred,
          noteText = task.notes
        )
      }
    }
  }

  fun togglePlannedTaskCompleted(task: PlannedTask) {
    viewModelScope.launch {
      val newCompleted = !task.isCompleted
      repository.updatePlannedTask(task.copy(isCompleted = newCompleted))

      // Sync with HabitTaskLog in Tracker for that date
      val allTasks = allTasksFlow.stateIn(viewModelScope).value
      val matched = allTasks.find {
        it.name.equals(task.title.trim(), ignoreCase = true) &&
          (it.targetDate == task.date || it.isDefault || it.isScheduledFor(task.date, DateUtils.getDayOfWeekIndex(task.date)))
      }
      if (matched != null) {
        val logs = repository.getLogsForDate(task.date).stateIn(viewModelScope).value
        val log = logs.find { it.taskId == matched.id }
        if (log != null && log.isCompleted != newCompleted) {
          repository.toggleTaskComplete(matched.id, task.date)
        } else if (log == null && newCompleted) {
          repository.toggleTaskComplete(matched.id, task.date)
        }
      }
    }
  }

  fun togglePlannedTaskStarred(task: PlannedTask) {
    viewModelScope.launch {
      val newStarred = !task.isStarred
      repository.updatePlannedTask(task.copy(isStarred = newStarred))

      // Sync starred status with HabitTask if exists
      val allTasks = allTasksFlow.stateIn(viewModelScope).value
      val matched = allTasks.find {
        it.name.equals(task.title.trim(), ignoreCase = true) && it.targetDate == task.date
      }
      if (matched != null) {
        repository.updateTask(matched.copy(isStarred = newStarred))
      }
    }
  }

  fun togglePlannedTaskArchived(task: PlannedTask) {
    viewModelScope.launch {
      val newArchived = !task.isArchived
      repository.updatePlannedTask(task.copy(isArchived = newArchived))
    }
  }

  fun toggleEventSubtaskComplete(event: com.example.data.model.PlanEvent, subtaskId: String) {
    viewModelScope.launch {
      val subtasks = event.getSubtasks()
      val updated = subtasks.map {
        if (it.id == subtaskId) it.copy(isCompleted = !it.isCompleted) else it
      }
      val updatedEvent = event.copy(subtasksJson = com.example.data.model.PlanEvent.serializeSubtasks(updated))
      repository.updatePlanEvent(updatedEvent)
    }
  }

  fun deletePlannedTask(taskId: Long) {
    viewModelScope.launch {
      val plans = repository.allPlannedTasks.stateIn(viewModelScope).value
      val plan = plans.find { it.id == taskId }
      repository.deletePlannedTaskById(taskId)

      // Also clean up matching HabitTask if it was single-day for that date
      if (plan != null) {
        val allTasks = allTasksFlow.stateIn(viewModelScope).value
        val matched = allTasks.find {
          it.name.equals(plan.title, ignoreCase = true) && it.targetDate == plan.date
        }
        if (matched != null) {
          repository.deleteTaskById(matched.id)
        }
      }
    }
  }

  // Jump directly to a planned task: switches date to planned date, ensures habit exists, and starts tracking
  fun jumpToPlannedTask(plannedTask: PlannedTask) {
    selectDate(plannedTask.date)
    viewModelScope.launch {
      val existingTasks = allTasksFlow.stateIn(viewModelScope).value
      val match = existingTasks.find {
        it.name.equals(plannedTask.title, ignoreCase = true) &&
          (it.targetDate == plannedTask.date || it.isDefault)
      }
      if (match == null) {
        repository.insertTask(
          name = plannedTask.title,
          targetDate = plannedTask.date,
          repeatDaysMask = 0,
          targetTimeMinutes = plannedTask.targetTimeMinutes,
          isStarred = plannedTask.isStarred,
          noteText = plannedTask.notes
        )
      }
    }
  }

  // Query completed tasks on a specific date for Analytics Day Popup
  fun getDayCompletedTasks(date: String): List<DayCompletedTaskItem> {
    val dayOfWeekIdx = DateUtils.getDayOfWeekIndex(date)
    val tasks = allTasksState.value
    val logs = allLogsState.value.filter { it.date == date }.associateBy { it.taskId }
    val scheduled = tasks.filter { it.isScheduledFor(date, dayOfWeekIdx) }
    return scheduled.map { task ->
      val log = logs[task.id]
      DayCompletedTaskItem(
        taskName = task.name,
        timeSpentSeconds = log?.timeSpentSeconds ?: 0L,
        isCompleted = log?.isCompleted ?: false,
        targetMinutes = task.targetTimeMinutes
      )
    }
  }

  // NEET Chapters Operations
  fun addNeetChapter(
    name: String,
    subject: String,
    isCompleted: Boolean = false,
    isPyqDone: Boolean = false,
    isRevisionDone: Boolean = false,
    isExerciseDone: Boolean = false,
    isArDone: Boolean = false,
    ncertReadCount: Int = 0,
    notes: String = ""
  ) {
    if (name.isBlank()) return
    viewModelScope.launch {
      val chapter = NeetChapter(
        name = name.trim(),
        subject = subject,
        isCompleted = isCompleted,
        isPyqDone = isPyqDone,
        isRevisionDone = isRevisionDone || (ncertReadCount > 0),
        isExerciseDone = isExerciseDone,
        isArDone = isArDone,
        ncertReadCount = ncertReadCount,
        notes = notes.trim()
      )
      repository.insertNeetChapter(chapter)
      notifyNeetWidgetUpdated()
    }
  }

  fun updateNeetChapter(chapter: NeetChapter) {
    viewModelScope.launch {
      repository.updateNeetChapter(chapter)
      notifyNeetWidgetUpdated()
    }
  }

  fun toggleChapterCompleted(chapter: NeetChapter) {
    viewModelScope.launch {
      repository.updateNeetChapter(chapter.copy(isCompleted = !chapter.isCompleted))
      notifyNeetWidgetUpdated()
    }
  }

  fun toggleChapterPyq(chapter: NeetChapter) {
    viewModelScope.launch {
      repository.updateNeetChapter(chapter.copy(isPyqDone = !chapter.isPyqDone))
      notifyNeetWidgetUpdated()
    }
  }

  fun toggleChapterRevision(chapter: NeetChapter) {
    viewModelScope.launch {
      repository.updateNeetChapter(chapter.copy(isRevisionDone = !chapter.isRevisionDone))
      notifyNeetWidgetUpdated()
    }
  }

  fun toggleChapterExercise(chapter: NeetChapter) {
    viewModelScope.launch {
      repository.updateNeetChapter(chapter.copy(isExerciseDone = !chapter.isExerciseDone))
      notifyNeetWidgetUpdated()
    }
  }

  fun toggleChapterAr(chapter: NeetChapter) {
    viewModelScope.launch {
      repository.updateNeetChapter(chapter.copy(isArDone = !chapter.isArDone))
      notifyNeetWidgetUpdated()
    }
  }

  fun incrementChapterNcert(chapter: NeetChapter) {
    val newCount = chapter.ncertReadCount + 1
    viewModelScope.launch {
      repository.updateNeetChapter(
        chapter.copy(
          ncertReadCount = newCount,
          isRevisionDone = (newCount > 0)
        )
      )
      notifyNeetWidgetUpdated()
    }
  }

  fun decrementChapterNcert(chapter: NeetChapter) {
    val newCount = (chapter.ncertReadCount - 1).coerceAtLeast(0)
    viewModelScope.launch {
      repository.updateNeetChapter(
        chapter.copy(
          ncertReadCount = newCount,
          isRevisionDone = (newCount > 0)
        )
      )
      notifyNeetWidgetUpdated()
    }
  }

  fun deleteNeetChapter(chapter: NeetChapter) {
    viewModelScope.launch {
      repository.deleteNeetChapter(chapter)
      notifyNeetWidgetUpdated()
    }
  }

  // NEET Tally Counters Operations
  fun addNeetTallyCounter(
    title: String,
    initialCount: Int = 0,
    target: Int = 0,
    unit: String = "times"
  ) {
    if (title.isBlank()) return
    viewModelScope.launch {
      val counter = NeetTallyCounter(
        title = title.trim(),
        count = initialCount.coerceAtLeast(0),
        target = target.coerceAtLeast(0),
        unit = unit.trim().ifBlank { "times" }
      )
      repository.insertNeetTallyCounter(counter)
    }
  }

  fun updateNeetTallyCounter(counter: NeetTallyCounter) {
    viewModelScope.launch {
      repository.updateNeetTallyCounter(counter)
    }
  }

  fun incrementNeetTally(counter: NeetTallyCounter, delta: Int = 1) {
    viewModelScope.launch {
      val newCount = (counter.count + delta).coerceAtLeast(0)
      repository.updateNeetTallyCounter(counter.copy(count = newCount))
    }
  }

  fun decrementNeetTally(counter: NeetTallyCounter, delta: Int = 1) {
    viewModelScope.launch {
      val newCount = (counter.count - delta).coerceAtLeast(0)
      repository.updateNeetTallyCounter(counter.copy(count = newCount))
    }
  }

  fun resetNeetTally(counter: NeetTallyCounter) {
    viewModelScope.launch {
      repository.updateNeetTallyCounter(counter.copy(count = 0))
    }
  }

  fun deleteNeetTallyCounter(counter: NeetTallyCounter) {
    viewModelScope.launch {
      repository.deleteNeetTallyCounter(counter)
    }
  }

  // Presets Management (Independent of daily tasks until added)
  fun addPreset(
    name: String,
    targetTimeMinutes: Int = 0,
    noteText: String = "",
    noteImageUri: String? = null
  ) {
    if (name.isBlank()) return
    viewModelScope.launch {
      repository.insertTaskPreset(
        name = name.trim(),
        targetTimeMinutes = targetTimeMinutes,
        noteText = noteText.trim(),
        noteImageUri = noteImageUri
      )
    }
  }

  fun deletePreset(id: Long) {
    viewModelScope.launch {
      repository.deleteTaskPresetById(id)
    }
  }

  fun addTaskFromPreset(
    preset: TaskPreset,
    targetDate: String? = null,
    targetDates: Set<String> = emptySet(),
    repeatDaysMask: Int = 0
  ) {
    if (targetDates.isNotEmpty()) {
      scheduleTaskForDates(
        name = preset.name,
        dates = targetDates,
        targetTimeMinutes = preset.targetTimeMinutes,
        noteText = preset.noteText,
        noteImageUri = preset.noteImageUri
      )
    } else if (repeatDaysMask != 0) {
      addTask(
        name = preset.name,
        repeatDaysMask = repeatDaysMask,
        targetTimeMinutes = preset.targetTimeMinutes,
        isDefault = false,
        noteText = preset.noteText,
        noteImageUri = preset.noteImageUri
      )
    } else {
      // Default to the current day / selected day the task is being added on!
      val dayToAdd = targetDate ?: selectedDate.value
      addTask(
        name = preset.name,
        targetDates = setOf(dayToAdd),
        repeatDaysMask = 0,
        targetTimeMinutes = preset.targetTimeMinutes,
        isDefault = false,
        noteText = preset.noteText,
        noteImageUri = preset.noteImageUri
      )
    }
  }

  fun schedulePresetForDates(preset: TaskPreset, dates: Set<String>) {
    if (dates.isEmpty()) return
    scheduleTaskForDates(
      name = preset.name,
      dates = dates,
      targetTimeMinutes = preset.targetTimeMinutes,
      isStarred = false,
      noteText = preset.noteText,
      noteImageUri = preset.noteImageUri
    )
  }

  fun scheduleTaskForDates(
    name: String,
    dates: Set<String>,
    targetTimeMinutes: Int = 0,
    isStarred: Boolean = false,
    noteText: String = "",
    noteImageUri: String? = null
  ) {
    if (name.isBlank() || dates.isEmpty()) return
    viewModelScope.launch {
      val allTasks = repository.allTasks.first()
      dates.forEach { date ->
        val alreadyScheduled = allTasks.any {
          it.name.equals(name.trim(), ignoreCase = true) && it.targetDate == date
        }
        if (!alreadyScheduled) {
          repository.insertTask(
            name = name.trim(),
            targetDate = date,
            targetTimeMinutes = targetTimeMinutes,
            isStarred = isStarred,
            noteText = noteText.trim(),
            noteImageUri = noteImageUri
          )
        }
        val allPlans = repository.allPlannedTasks.first()
        val existsInPlan = allPlans.any {
          it.title.equals(name.trim(), ignoreCase = true) && it.date == date
        }
        if (!existsInPlan) {
          repository.insertPlannedTask(
            PlannedTask(
              title = name.trim(),
              date = date,
              targetTimeMinutes = targetTimeMinutes,
              notes = noteText.trim(),
              isStarred = isStarred
            )
          )
        }
      }
    }
  }

  // Plan Event Management
  fun addPlanEvent(
    title: String,
    startDate: String,
    endDate: String,
    taskTitle: String,
    taskTargetMinutes: Int,
    notes: String,
    subtasks: List<com.example.data.model.EventSubtask> = emptyList()
  ) {
    viewModelScope.launch {
      val event = com.example.data.model.PlanEvent(
        title = title,
        startDate = startDate,
        endDate = endDate,
        taskTitle = taskTitle,
        taskTargetMinutes = taskTargetMinutes,
        notes = notes,
        subtasksJson = com.example.data.model.PlanEvent.serializeSubtasks(subtasks)
      )
      repository.insertPlanEvent(event)

      // Automatically sync with device calendar if enabled
      if (autoCalendarSyncEnabled.value && appContext != null) {
        com.example.util.CalendarSyncUtil.syncEventToDeviceCalendar(
          context = appContext,
          title = title,
          description = notes.ifBlank { "NEET Study Event in Habit Tracker" },
          startDateStr = startDate,
          endDateStr = endDate,
          targetTimeMinutes = taskTargetMinutes
        )
      }
    }
  }

  fun updatePlanEvent(event: com.example.data.model.PlanEvent) {
    viewModelScope.launch {
      repository.updatePlanEvent(event)
    }
  }

  fun deletePlanEvent(event: com.example.data.model.PlanEvent) {
    viewModelScope.launch {
      repository.deletePlanEvent(event)
    }
  }

  fun deletePlanEventById(id: Long) {
    viewModelScope.launch {
      repository.deletePlanEventById(id)
    }
  }

  // Full Data Export & Import (All DB tables + Notes + Images + PDFs + Preferences)
  private suspend fun buildFullBackupPayload(): BackupPayload {
    val themeObj = themePreferences?.let { tp ->
      org.json.JSONObject().apply {
        put("customColorHex", tp.customColorHex.value)
        put("customFontColorHex", tp.customFontColorHex.value)
        put("uiOpacity", tp.uiOpacity.value.toDouble())
        put("textSizeScale", tp.textSizeScale.value.toDouble())
        put("autoCalendarSync", tp.autoCalendarSync.value)
      }
    }
    val goalObj = goalPreferences?.let { gp ->
      val g = gp.goal.value
      org.json.JSONObject().apply {
        put("title", g.title)
        put("examDate", g.examDate)
        put("dailyStudyHoursTarget", g.dailyStudyHoursTarget)
        put("motivationQuote", g.motivationQuote)
      }
    }

    val chapterBtnsJson = chapterButtonPreferences?.let { cbp ->
      val arr = org.json.JSONArray()
      cbp.buttons.value.forEach { btn ->
        arr.put(org.json.JSONObject().apply {
          put("id", btn.id)
          put("name", btn.name)
          put("colorHex", btn.colorHex)
          put("isBuiltIn", btn.isBuiltIn)
          put("isEnabled", btn.isEnabled)
          put("isCounter", btn.isCounter)
        })
      }
      arr.toString()
    }

    return BackupPayload(
      tasks = repository.allTasks.first(),
      logs = repository.allLogs.first(),
      ratings = repository.allRatings.first(),
      scores = repository.allNeetScores.first(),
      plannedTasks = repository.allPlannedTasks.first(),
      events = repository.allPlanEvents.first(),
      presets = repository.allTaskPresets.first(),
      chapters = repository.allNeetChapters.first(),
      counters = repository.allNeetTallyCounters.first(),
      notes = repository.allNotes.first(),
      aiChats = repository.allAiChats.first(),
      customSubjects = noteFolderPreferences?.customSubjects?.value ?: emptyList(),
      customSubfolders = noteFolderPreferences?.customSubfolders?.value ?: emptyMap(),
      themePrefs = themeObj,
      goalPrefs = goalObj,
      chapterButtonsJson = chapterBtnsJson
    )
  }

  suspend fun exportDataToJson(): String {
    val payload = buildFullBackupPayload()
    return BackupArchiveManager.serializePayloadToJson(payload)
  }

  suspend fun exportAllDataToJson(): String = exportDataToJson()

  suspend fun exportFullBackupZipToUri(context: Context, destUri: Uri) {
    val payload = buildFullBackupPayload()
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
      context.contentResolver.openOutputStream(destUri)?.use { os ->
        BackupArchiveManager.exportPayloadToZipStream(payload, os, context)
      } ?: throw IllegalStateException("Unable to open destination file for export")
    }
  }

  suspend fun exportFullBackupZipToCacheFile(context: Context): File {
    val payload = buildFullBackupPayload()
    return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
      val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
      val file = File(exportDir, "Habit_Tracker_Full_Backup_${DateUtils.today()}.zip")
      FileOutputStream(file).use { fos ->
        BackupArchiveManager.exportPayloadToZipStream(payload, fos, context)
      }
      file
    }
  }

  private suspend fun applyImportedPayload(result: BackupImportResult) {
    val p = result.payload
    repository.importData(
      tasks = p.tasks,
      logs = p.logs,
      ratings = p.ratings,
      scores = p.scores,
      plannedTasks = p.plannedTasks,
      events = p.events,
      chapters = p.chapters,
      counters = p.counters,
      presets = p.presets,
      notes = p.notes,
      aiChats = p.aiChats
    )
    repository.syncLegacyChapterNotesToNotesTable()

    if (p.customSubjects.isNotEmpty() || p.customSubfolders.isNotEmpty()) {
      noteFolderPreferences?.importFolders(p.customSubjects, p.customSubfolders)
    }

    p.themePrefs?.let { tp ->
      tp.optString("customColorHex", "").takeIf { it.isNotBlank() }?.let { setCustomColorHex(it) }
      tp.optString("customFontColorHex", "").takeIf { it.isNotBlank() }?.let { setCustomFontColorHex(it) }
      if (tp.has("uiOpacity")) setUiOpacity(tp.optDouble("uiOpacity", 0.85).toFloat())
      if (tp.has("textSizeScale")) setTextSizeScale(tp.optDouble("textSizeScale", 1.0).toFloat())
      if (tp.has("autoCalendarSync")) setAutoCalendarSyncEnabled(tp.optBoolean("autoCalendarSync", false))
    }

    p.goalPrefs?.let { gp ->
      updateGoal(
        AppGoal(
          title = gp.optString("title", "NEET 2026 Target: 680+ Score"),
          examDate = gp.optString("examDate", "2026-05-03"),
          dailyStudyHoursTarget = gp.optInt("dailyStudyHoursTarget", 10),
          motivationQuote = gp.optString("motivationQuote", "Every single formula and NCERT line matters. Stay relentless.")
        )
      )
    }

    notifyNeetWidgetUpdated()
    notifyTasksWidgetUpdated()
  }

  suspend fun importBackupFromUri(context: Context, uri: Uri): BackupImportResult {
    val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
      val mediaDir = NoteMediaUtils.getMediaDir(context)
      context.contentResolver.openInputStream(uri)?.use { ins ->
        BackupArchiveManager.importFromStream(ins, mediaDir)
      } ?: throw IllegalStateException("Unable to read selected backup file")
    }
    applyImportedPayload(result)
    return result
  }

  suspend fun importDataFromJson(jsonString: String): Int {
    val payload = BackupArchiveManager.parsePayloadFromJson(jsonString)
    val result = BackupImportResult(payload = payload, mediaRestoredCount = 0)
    applyImportedPayload(result)
    return result.totalRecordsCount
  }

  fun resetAllData() {
    viewModelScope.launch {
      repository.resetAllData()
      notifyNeetWidgetUpdated()
      notifyTasksWidgetUpdated()
    }
  }

  fun addPresetAsDefaultTask(presetName: String) {
    if (presetName.isBlank()) return
    viewModelScope.launch {
      val allTasks = repository.allTasks.first()
      val existing = allTasks.find { it.name.equals(presetName.trim(), ignoreCase = true) }
      if (existing != null) {
        repository.updateTask(existing.copy(isDefault = true))
      } else {
        repository.insertTask(
          name = presetName.trim(),
          repeatDaysMask = HabitTask.EVERYDAY_MASK,
          targetTimeMinutes = 0,
          isDefault = true
        )
      }
    }
  }

  fun removeDefaultStatus(taskId: Long) {
    viewModelScope.launch {
      val task = repository.allTasks.first().find { it.id == taskId }
      if (task != null) {
        repository.updateTask(task.copy(isDefault = false))
      }
    }
  }

  fun removeDefaultStatusByName(taskName: String) {
    viewModelScope.launch {
      val task = repository.allTasks.first().find { it.name.equals(taskName.trim(), ignoreCase = true) }
      if (task != null) {
        repository.updateTask(task.copy(isDefault = false))
      }
    }
  }

  fun syncAllTasksToCalendar(onResult: (Int) -> Unit) {
    viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
      val context = appContext
      if (context != null) {
        val tasksList = repository.allTasks.first()
        val count = CalendarSyncUtil.syncAllTasksToDeviceCalendar(context, tasksList)
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
          onResult(count)
        }
      } else {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(0) }
      }
    }
  }

  fun removeAllCalendarSync(onResult: (Int) -> Unit) {
    viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
      val context = appContext
      if (context != null) {
        val count = CalendarSyncUtil.removeAllSyncedEventsFromDeviceCalendar(context)
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
          onResult(count)
        }
      } else {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(0) }
      }
    }
  }

  fun importCalendarEvents(onResult: (Int) -> Unit) {
    viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
      val context = appContext
      if (context != null) {
        val imported = CalendarSyncUtil.importEventsFromDeviceCalendar(context)
        val existingTasks = repository.allTasks.first()
        var count = 0
        for (t in imported) {
          val alreadyExists = existingTasks.any {
            it.name.equals(t.name, ignoreCase = true) &&
              (it.targetDate == t.targetDate || (it.targetDate == null && t.targetDate == null))
          }
          if (!alreadyExists) {
            repository.insertTask(name = t.name, targetDate = t.targetDate, noteText = t.noteText)
            count++
          }
        }
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
          onResult(count)
        }
      } else {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(0) }
      }
    }
  }

  fun moveTaskToNextDay(taskId: Long) {
    viewModelScope.launch {
      val task = repository.allTasks.first().find { it.id == taskId }
      if (task != null) {
        val baseDate = task.targetDate ?: task.targetDates?.split(",")?.firstOrNull() ?: DateUtils.today()
        val nextDay = DateUtils.getNextDay(baseDate)
        val updated = task.copy(targetDate = nextDay, targetDates = null)
        repository.updateTask(updated)
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    flushActiveTimer()
  }

  companion object {
    fun provideFactory(
      repository: HabitRepository,
      themePreferences: ThemePreferences? = null,
      goalPreferences: GoalPreferences? = null,
      context: Context? = null
    ): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return HabitViewModel(repository, themePreferences, goalPreferences, context?.applicationContext) as T
        }
      }
  }
}
