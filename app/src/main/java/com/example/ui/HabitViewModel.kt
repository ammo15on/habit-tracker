package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.DayRating
import com.example.data.model.HabitTask
import com.example.data.model.HabitTaskLog
import com.example.data.model.NeetTestScore
import com.example.data.model.PlannedTask
import com.example.data.model.RatingType
import com.example.data.repository.HabitRepository
import com.example.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HabitViewModel(private val repository: HabitRepository) : ViewModel() {

  // Current selected date for tracker screen
  val selectedDate = MutableStateFlow(DateUtils.today())

  // Active running timer state
  val runningTaskId = MutableStateFlow<Long?>(null)
  val runningTimerSessionSeconds = MutableStateFlow(0L)
  private var timerJob: Job? = null

  // All tasks, ratings, logs, neet scores, planned tasks from DB
  private val allTasksFlow = repository.allTasks
  private val allRatingsFlow = repository.allRatings
  private val allLogsFlow = repository.allLogs
  val allNeetScores: StateFlow<List<NeetTestScore>> = repository.allNeetScores
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allPlannedTasks: StateFlow<List<PlannedTask>> = repository.allPlannedTasks
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val defaultTasks: StateFlow<List<HabitTask>> = repository.defaultTasks
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    // Filter tasks that repeat on this day-of-week
    val list = tasks.filter { it.isRepeatingOn(dayOfWeekIdx) }.map { task ->
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
  val selectedAnalyticsTab = MutableStateFlow(AnalyticsTab.WEEK)

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
      val scheduledTasks = tasks.filter { it.isRepeatingOn(dayOfWeekIdx) }
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
        val scheduledTasks = tasks.filter { it.isRepeatingOn(dayOfWeekIdx) }

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
        val scheduledTasks = tasks.filter { it.isRepeatingOn(dayOfWeekIdx) }

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
    repeatDaysMask: Int,
    targetMinutes: Int = 0,
    isDefault: Boolean = false,
    noteText: String = "",
    noteImageUri: String? = null
  ) {
    if (name.isBlank()) return
    viewModelScope.launch {
      repository.insertTask(
        name = name.trim(),
        repeatDaysMask = repeatDaysMask,
        targetTimeMinutes = targetMinutes,
        isDefault = isDefault,
        noteText = noteText.trim(),
        noteImageUri = noteImageUri
      )
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
      repository.deleteTaskById(taskId)
    }
  }

  fun toggleTaskComplete(taskId: Long) {
    viewModelScope.launch {
      repository.toggleTaskComplete(taskId, selectedDate.value)
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

  // Planned Task actions
  fun addPlannedTask(task: PlannedTask) {
    viewModelScope.launch {
      repository.insertPlannedTask(task)
    }
  }

  fun togglePlannedTaskCompleted(task: PlannedTask) {
    viewModelScope.launch {
      repository.updatePlannedTask(task.copy(isCompleted = !task.isCompleted))
    }
  }

  fun togglePlannedTaskStarred(task: PlannedTask) {
    viewModelScope.launch {
      repository.updatePlannedTask(task.copy(isStarred = !task.isStarred))
    }
  }

  fun deletePlannedTask(taskId: Long) {
    viewModelScope.launch {
      repository.deletePlannedTaskById(taskId)
    }
  }

  // Jump directly to a planned task: switches date to planned date, ensures habit exists, and starts tracking
  fun jumpToPlannedTask(plannedTask: PlannedTask) {
    selectDate(plannedTask.date)
    viewModelScope.launch {
      val existingTasks = allTasksFlow.stateIn(viewModelScope).value
      val match = existingTasks.find { it.name.equals(plannedTask.title, ignoreCase = true) }
      if (match == null) {
        repository.insertTask(
          name = plannedTask.title,
          repeatDaysMask = HabitTask.EVERYDAY_MASK,
          targetTimeMinutes = plannedTask.targetTimeMinutes
        )
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    flushActiveTimer()
  }

  companion object {
    fun provideFactory(repository: HabitRepository): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return HabitViewModel(repository) as T
        }
      }
  }
}
