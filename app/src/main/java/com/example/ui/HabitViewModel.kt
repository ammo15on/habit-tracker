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
import com.example.data.model.PlannedTask
import com.example.data.model.RatingType
import com.example.data.repository.HabitRepository
import com.example.ui.theme.AppThemeColor
import com.example.util.DateUtils
import com.example.util.ThemePreferences
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
  private val themePreferences: ThemePreferences? = null
) : ViewModel() {

  // Current selected date for tracker screen
  val selectedDate = MutableStateFlow(DateUtils.today())

  // App Theme Selection State (Yellow, Golden, Black, Grey, Emerald, Blue, Purple)
  private val _fallbackThemeColor = MutableStateFlow(AppThemeColor.EMERALD)
  val selectedThemeColor: StateFlow<AppThemeColor> =
    themePreferences?.themeColor ?: _fallbackThemeColor.asStateFlow()

  fun setThemeColor(color: AppThemeColor) {
    if (themePreferences != null) {
      themePreferences.setThemeColor(color)
    } else {
      _fallbackThemeColor.value = color
    }
  }

  // Active running timer state
  val runningTaskId = MutableStateFlow<Long?>(null)
  val runningTimerSessionSeconds = MutableStateFlow(0L)
  private var timerJob: Job? = null

  // All tasks, ratings, logs, neet scores, planned tasks from DB
  private val allTasksFlow = repository.allTasks
  private val allRatingsFlow = repository.allRatings
  private val allLogsFlow = repository.allLogs
  val allTasksState: StateFlow<List<HabitTask>> = repository.allTasks
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
  val allLogsState: StateFlow<List<HabitTaskLog>> = repository.allLogs
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
  val allNeetScores: StateFlow<List<NeetTestScore>> = repository.allNeetScores
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allPlannedTasks: StateFlow<List<PlannedTask>> = repository.allPlannedTasks
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val defaultTasks: StateFlow<List<HabitTask>> = repository.defaultTasks
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allNeetChapters: StateFlow<List<NeetChapter>> = repository.allNeetChapters
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allNeetTallyCounters: StateFlow<List<NeetTallyCounter>> = repository.allNeetTallyCounters
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  init {
    // Populate default NEET chapters (including Class 11 and Class 12) and Tally counters
    viewModelScope.launch {
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
    }
  }

  // Combined UI State for currently selected date (Tasks sorted: Incomplete first, Finished at bottom)
  val tasksForSelectedDate: StateFlow<List<TaskItemUiState>> = combine(
    allTasksFlow,
    allLogsFlow,
    selectedDate,
    runningTaskId,
    runningTimerSessionSeconds
  ) { tasks, logs, date, runningId, sessionSeconds ->
    val dayOfWeekIdx = DateUtils.getDayOfWeekIndex(date)
    val logsForDate = logs.filter { it.date == date }.associateBy { it.taskId }

    // Filter tasks scheduled specifically for this date or recurring on this day
    val matchedHabitTasks = tasks.filter { it.isScheduledFor(date, dayOfWeekIdx) }

    val list = matchedHabitTasks.map { task ->
      val log = logsForDate[task.id]
      val isRunning = (task.id == runningId)
      val baseSeconds = log?.timeSpentSeconds ?: 0L
      val effectiveSeconds = if (isRunning) baseSeconds + sessionSeconds else baseSeconds
      val isCompleted = log?.isCompleted ?: false

      TaskItemUiState(
        task = task,
        timeSpentSeconds = effectiveSeconds,
        isCompleted = isCompleted,
        isRunning = isRunning
      )
    }

    // Move finished tasks to the bottom
    list.sortedWith(
      compareBy<TaskItemUiState> { it.isCompleted }
        .thenByDescending { it.isRunning }
        .thenBy { it.task.id }
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
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
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = 0L
    )

  // Analytics tab selection
  val selectedAnalyticsTab = MutableStateFlow(AnalyticsTab.DAY)

  // Effective logs flow including live running session for today
  private val effectiveLogsFlow = combine(
    allLogsFlow,
    runningTaskId,
    runningTimerSessionSeconds,
    selectedDate
  ) { logs, runningId, sessionSecs, curDate ->
    if (runningId == null || sessionSecs <= 0) {
      logs
    } else {
      val mutable = logs.toMutableList()
      val idx = mutable.indexOfFirst { it.taskId == runningId && it.date == curDate }
      if (idx >= 0) {
        val old = mutable[idx]
        mutable[idx] = old.copy(timeSpentSeconds = old.timeSpentSeconds + sessionSecs)
      } else {
        mutable.add(
          HabitTaskLog(
            taskId = runningId,
            date = curDate,
            timeSpentSeconds = sessionSecs,
            isCompleted = false
          )
        )
      }
      mutable
    }
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
    repeatDaysMask: Int = 0,
    targetMinutes: Int = 0,
    isDefault: Boolean = false,
    isStarred: Boolean = false,
    noteText: String = "",
    noteImageUri: String? = null
  ) {
    if (name.isBlank()) return
    viewModelScope.launch {
      val curDate = selectedDate.value
      // If no repeat days mask is specified and not marked as recurring default,
      // the task is specific to this selected date!
      val specificDate = if (!isDefault && repeatDaysMask == 0) curDate else null

      repository.insertTask(
        name = name.trim(),
        targetDate = specificDate,
        repeatDaysMask = repeatDaysMask,
        targetTimeMinutes = targetMinutes,
        isDefault = isDefault,
        isStarred = isStarred,
        noteText = noteText.trim(),
        noteImageUri = noteImageUri
      )

      // Sync: task added in tracker should also appear in plan for that date!
      val allPlans = repository.allPlannedTasks.stateIn(viewModelScope).value
      val existsInPlan = allPlans.any {
        it.title.equals(name.trim(), ignoreCase = true) && it.date == curDate
      }
      if (!existsInPlan) {
        repository.insertPlannedTask(
          PlannedTask(
            title = name.trim(),
            date = curDate,
            targetTimeMinutes = targetMinutes,
            notes = noteText.trim(),
            isStarred = isStarred,
            isCompleted = false
          )
        )
      }
    }
  }

  fun updateTask(task: HabitTask) {
    viewModelScope.launch {
      repository.updateTask(task)
    }
  }

  fun updateTaskTargetTimer(task: HabitTask, newTargetMinutes: Int) {
    viewModelScope.launch {
      repository.updateTask(task.copy(targetTimeMinutes = newTargetMinutes))
    }
  }

  fun deleteTask(taskId: Long) {
    if (runningTaskId.value == taskId) {
      stopTimer()
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

  // Timer controls
  fun toggleTimer(taskId: Long) {
    val currentRunning = runningTaskId.value
    if (currentRunning == taskId) {
      stopTimer()
    } else {
      if (currentRunning != null) {
        stopTimer()
      }
      startTimer(taskId)
    }
  }

  private fun startTimer(taskId: Long) {
    runningTaskId.value = taskId
    runningTimerSessionSeconds.value = 0L

    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (isActive) {
        delay(1000)
        runningTimerSessionSeconds.value += 1L
        if (runningTimerSessionSeconds.value % 30 == 0L) {
          repository.addTimeToTask(taskId, selectedDate.value, 30L)
          runningTimerSessionSeconds.value = 0L
        }
      }
    }
  }

  private fun stopTimer() {
    val taskId = runningTaskId.value ?: return
    val session = runningTimerSessionSeconds.value
    timerJob?.cancel()
    timerJob = null
    runningTaskId.value = null
    runningTimerSessionSeconds.value = 0L

    if (session > 0) {
      viewModelScope.launch {
        repository.addTimeToTask(taskId, selectedDate.value, session)
      }
    }
  }

  private fun flushActiveTimer() {
    val taskId = runningTaskId.value
    val session = runningTimerSessionSeconds.value
    if (taskId != null && session > 0) {
      stopTimer()
    }
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
    notes: String = ""
  ) {
    if (name.isBlank()) return
    viewModelScope.launch {
      val chapter = NeetChapter(
        name = name.trim(),
        subject = subject,
        isCompleted = isCompleted,
        isPyqDone = isPyqDone,
        isRevisionDone = isRevisionDone,
        notes = notes.trim()
      )
      repository.insertNeetChapter(chapter)
    }
  }

  fun updateNeetChapter(chapter: NeetChapter) {
    viewModelScope.launch {
      repository.updateNeetChapter(chapter)
    }
  }

  fun toggleChapterCompleted(chapter: NeetChapter) {
    viewModelScope.launch {
      repository.updateNeetChapter(chapter.copy(isCompleted = !chapter.isCompleted))
    }
  }

  fun toggleChapterPyq(chapter: NeetChapter) {
    viewModelScope.launch {
      repository.updateNeetChapter(chapter.copy(isPyqDone = !chapter.isPyqDone))
    }
  }

  fun toggleChapterRevision(chapter: NeetChapter) {
    viewModelScope.launch {
      repository.updateNeetChapter(chapter.copy(isRevisionDone = !chapter.isRevisionDone))
    }
  }

  fun deleteNeetChapter(chapter: NeetChapter) {
    viewModelScope.launch {
      repository.deleteNeetChapter(chapter)
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

  // Default Tasks Quick Management
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

  override fun onCleared() {
    super.onCleared()
    flushActiveTimer()
  }

  companion object {
    fun provideFactory(
      repository: HabitRepository,
      themePreferences: ThemePreferences? = null
    ): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return HabitViewModel(repository, themePreferences) as T
        }
      }
  }
}
