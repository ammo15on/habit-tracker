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

  private val _fallbackFontColor = MutableStateFlow(com.example.ui.theme.AppFontColor.DEFAULT)
  val selectedFontColor: StateFlow<com.example.ui.theme.AppFontColor> =
    themePreferences?.fontColor ?: _fallbackFontColor.asStateFlow()

  private val _fallbackBgImageUri = MutableStateFlow<String?>(null)
  val selectedBackgroundImageUri: StateFlow<String?> =
    themePreferences?.backgroundImageUri ?: _fallbackBgImageUri.asStateFlow()

  fun setThemeColor(color: AppThemeColor) {
    if (themePreferences != null) {
      themePreferences.setThemeColor(color)
    } else {
      _fallbackThemeColor.value = color
    }
  }

  fun setFontColor(color: com.example.ui.theme.AppFontColor) {
    if (themePreferences != null) {
      themePreferences.setFontColor(color)
    } else {
      _fallbackFontColor.value = color
    }
  }

  fun setBackgroundImageUri(uri: String?) {
    if (themePreferences != null) {
      themePreferences.setBackgroundImageUri(uri)
    } else {
      _fallbackBgImageUri.value = uri
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
  val presets: StateFlow<List<HabitTask>> = defaultTasks
  val allPlanEvents: StateFlow<List<com.example.data.model.PlanEvent>> = repository.allPlanEvents
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
    targetDates: Set<String> = emptySet(),
    repeatDaysMask: Int = 0,
    targetTimeMinutes: Int = 0,
    isDefault: Boolean = false,
    isStarred: Boolean = false,
    noteText: String = "",
    noteImageUri: String? = null
  ) {
    if (name.isBlank()) return
    viewModelScope.launch {
      val curDate = selectedDate.value

      if (targetDates.isNotEmpty()) {
        // Multi-day selection option
        targetDates.forEach { date ->
          repository.insertTask(
            name = name.trim(),
            targetDate = date,
            repeatDaysMask = 0,
            targetTimeMinutes = targetTimeMinutes,
            isDefault = false,
            isStarred = isStarred,
            noteText = noteText.trim(),
            noteImageUri = noteImageUri
          )
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

        repository.insertTask(
          name = name.trim(),
          targetDate = specificDate,
          repeatDaysMask = repeatDaysMask,
          targetTimeMinutes = targetTimeMinutes,
          isDefault = isDefault,
          isStarred = isStarred,
          noteText = noteText.trim(),
          noteImageUri = noteImageUri
        )

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
  // Default Tasks & Presets Management
  fun addPreset(name: String) = addPresetAsDefaultTask(name)

  fun deletePreset(id: Long) = removeDefaultStatus(id)

  fun schedulePresetForDates(preset: HabitTask, dates: Set<String>) {
    if (dates.isEmpty()) return
    viewModelScope.launch {
      val allTasks = repository.allTasks.first()
      dates.forEach { date ->
        val alreadyScheduled = allTasks.any {
          it.name.equals(preset.name, ignoreCase = true) && it.targetDate == date
        }
        if (!alreadyScheduled) {
          repository.insertTask(
            name = preset.name,
            targetDate = date,
            targetTimeMinutes = preset.targetTimeMinutes,
            noteText = preset.noteText,
            noteImageUri = preset.noteImageUri
          )
        }
        // Also ensure it is recorded in planned tasks for that day
        val allPlans = repository.allPlannedTasks.first()
        val existsInPlan = allPlans.any {
          it.title.equals(preset.name, ignoreCase = true) && it.date == date
        }
        if (!existsInPlan) {
          repository.insertPlannedTask(
            PlannedTask(
              title = preset.name,
              date = date,
              targetTimeMinutes = preset.targetTimeMinutes,
              notes = preset.noteText,
              isStarred = preset.isStarred
            )
          )
        }
      }
    }
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
    notes: String
  ) {
    viewModelScope.launch {
      val event = com.example.data.model.PlanEvent(
        title = title,
        startDate = startDate,
        endDate = endDate,
        taskTitle = taskTitle,
        taskTargetMinutes = taskTargetMinutes,
        notes = notes
      )
      repository.insertPlanEvent(event)
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

  // Data Export & Import (Pure Android JSON)
  suspend fun exportDataToJson(): String {
    val root = org.json.JSONObject()

    val tasks = repository.allTasks.first()
    val tasksArray = org.json.JSONArray()
    tasks.forEach { t ->
      val obj = org.json.JSONObject().apply {
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
      }
      tasksArray.put(obj)
    }
    root.put("tasks", tasksArray)

    val logs = repository.allLogs.first()
    val logsArray = org.json.JSONArray()
    logs.forEach { l ->
      val obj = org.json.JSONObject().apply {
        put("id", l.id)
        put("taskId", l.taskId)
        put("date", l.date)
        put("timeSpentSeconds", l.timeSpentSeconds)
        put("isCompleted", l.isCompleted)
      }
      logsArray.put(obj)
    }
    root.put("logs", logsArray)

    val ratings = repository.allRatings.first()
    val ratingsArray = org.json.JSONArray()
    ratings.forEach { r ->
      val obj = org.json.JSONObject().apply {
        put("date", r.date)
        put("rating", r.rating)
      }
      ratingsArray.put(obj)
    }
    root.put("ratings", ratingsArray)

    val scores = repository.allNeetScores.first()
    val scoresArray = org.json.JSONArray()
    scores.forEach { s ->
      val obj = org.json.JSONObject().apply {
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
      }
      scoresArray.put(obj)
    }
    root.put("scores", scoresArray)

    val planned = repository.allPlannedTasks.first()
    val plannedArray = org.json.JSONArray()
    planned.forEach { p ->
      val obj = org.json.JSONObject().apply {
        put("id", p.id)
        put("title", p.title)
        put("date", p.date)
        put("targetTimeMinutes", p.targetTimeMinutes)
        put("notes", p.notes)
        put("isStarred", p.isStarred)
        put("isCompleted", p.isCompleted)
      }
      plannedArray.put(obj)
    }
    root.put("plannedTasks", plannedArray)

    val events = repository.allPlanEvents.first()
    val eventsArray = org.json.JSONArray()
    events.forEach { e ->
      val obj = org.json.JSONObject().apply {
        put("id", e.id)
        put("title", e.title)
        put("startDate", e.startDate)
        put("endDate", e.endDate)
        put("taskTitle", e.taskTitle)
        put("taskTargetMinutes", e.taskTargetMinutes)
        put("notes", e.notes)
      }
      eventsArray.put(obj)
    }
    root.put("events", eventsArray)

    return root.toString(2)
  }

  suspend fun importDataFromJson(jsonString: String): Int {
    val root = org.json.JSONObject(jsonString)
    var importedCount = 0

    val tasksList = mutableListOf<HabitTask>()
    if (root.has("tasks")) {
      val arr = root.getJSONArray("tasks")
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
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
            noteText = o.optString("noteText", "")
          )
        )
      }
      importedCount += tasksList.size
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
      importedCount += logsList.size
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
      importedCount += ratingsList.size
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
      importedCount += scoresList.size
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
            isCompleted = o.optBoolean("isCompleted", false)
          )
        )
      }
      importedCount += plannedList.size
    }

    val eventsList = mutableListOf<com.example.data.model.PlanEvent>()
    if (root.has("events")) {
      val arr = root.getJSONArray("events")
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        eventsList.add(
          com.example.data.model.PlanEvent(
            id = o.optLong("id", 0L),
            title = o.optString("title", ""),
            startDate = o.optString("startDate", ""),
            endDate = o.optString("endDate", ""),
            taskTitle = o.optString("taskTitle", ""),
            taskTargetMinutes = o.optInt("taskTargetMinutes", 0),
            notes = o.optString("notes", "")
          )
        )
      }
      importedCount += eventsList.size
    }

    repository.importData(
      tasks = tasksList,
      logs = logsList,
      ratings = ratingsList,
      scores = scoresList,
      plannedTasks = plannedList,
      events = eventsList,
      chapters = emptyList(),
      counters = emptyList()
    )

    return importedCount
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
