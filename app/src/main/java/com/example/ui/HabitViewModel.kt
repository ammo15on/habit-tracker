package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.DayRating
import com.example.data.model.HabitTask
import com.example.data.model.HabitTaskLog
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

  // All tasks, ratings, and logs from DB
  private val allTasksFlow = repository.allTasks
  private val allRatingsFlow = repository.allRatings
  private val allLogsFlow = repository.allLogs

  init {
    viewModelScope.launch {
      // Check if we need to seed initial default tasks
      // Wait for initial emission
      allTasksFlow.collect { list ->
        if (list.isEmpty()) {
          repository.seedSampleTasksIfEmpty()
        }
      }
    }
  }

  // Combined UI State for currently selected date
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
    tasks.filter { it.isRepeatingOn(dayOfWeekIdx) }.map { task ->
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

  // Total time spent today
  val totalTimeTodaySeconds: StateFlow<Long> = tasksForSelectedDate
    .map { tasks -> tasks.sumOf { it.timeSpentSeconds } }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = 0L
    )

  // Analytics tab selection
  val selectedAnalyticsTab = MutableStateFlow(AnalyticsTab.WEEK)

  // Analytics: Days Summary (recent 30 days)
  val daysAnalytics: StateFlow<List<DaySummary>> = combine(
    allRatingsFlow,
    allLogsFlow,
    allTasksFlow
  ) { ratings, logs, tasks ->
    val ratingsMap = ratings.associateBy { it.date }
    val logsMap = logs.groupBy { it.date }
    val today = DateUtils.today()

    // Generate past 30 days including today
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

  // Analytics: Weeks Summary (current week + previous 7 weeks)
  val weeksAnalytics: StateFlow<List<WeekSummary>> = combine(
    allRatingsFlow,
    allLogsFlow,
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

      // Move anchorDate to 7 days earlier
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

  // Analytics: Month Summary (current month + past 3 months)
  val monthsAnalytics: StateFlow<List<MonthSummary>> = combine(
    allRatingsFlow,
    allLogsFlow,
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

  // Date Navigation Actions
  fun goToPreviousDay() {
    flushActiveTimer()
    selectedDate.value = DateUtils.getPreviousDay(selectedDate.value)
  }

  fun goToNextDay() {
    flushActiveTimer()
    selectedDate.value = DateUtils.getNextDay(selectedDate.value)
  }

  fun selectDate(date: String) {
    flushActiveTimer()
    selectedDate.value = date
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
  fun addTask(name: String, repeatDaysMask: Int) {
    if (name.isBlank()) return
    viewModelScope.launch {
      repository.insertTask(name.trim(), repeatDaysMask)
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
      // Pause
      stopTimer()
    } else {
      // If another was running, flush it first
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
        // Flush every 30 seconds to DB to prevent lost time if app closes
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
