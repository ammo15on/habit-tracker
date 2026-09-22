package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.data.repository.HabitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object TimerManager {
  private const val PREFS_NAME = "habit_tracker_timer_prefs"
  private const val KEY_TASK_ID = "active_task_id"
  private const val KEY_TASK_NAME = "active_task_name"
  private const val KEY_TASK_DATE = "active_task_date"
  private const val KEY_START_WALL_CLOCK_MS = "active_start_wall_clock_ms"

  private var context: Context? = null
  private var repository: HabitRepository? = null
  private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
  private var tickerJob: Job? = null

  private var startWallClockMs: Long = 0L

  private val _runningTaskId = MutableStateFlow<Long?>(null)
  val runningTaskId: StateFlow<Long?> = _runningTaskId.asStateFlow()

  private val _runningTaskName = MutableStateFlow<String?>(null)
  val runningTaskName: StateFlow<String?> = _runningTaskName.asStateFlow()

  private val _runningTaskDate = MutableStateFlow<String?>(null)
  val runningTaskDate: StateFlow<String?> = _runningTaskDate.asStateFlow()

  private val _runningTimerSessionSeconds = MutableStateFlow(0L)
  val runningTimerSessionSeconds: StateFlow<Long> = _runningTimerSessionSeconds.asStateFlow()

  private val _isTimerRunning = MutableStateFlow(false)
  val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

  fun initialize(appContext: Context, repo: HabitRepository) {
    context = appContext.applicationContext
    repository = repo

    val prefs = getPrefs()
    val savedTaskId = prefs.getLong(KEY_TASK_ID, -1L)
    if (savedTaskId != -1L) {
      val savedTaskName = prefs.getString(KEY_TASK_NAME, "Task") ?: "Task"
      val savedDate = prefs.getString(KEY_TASK_DATE, DateUtils.today()) ?: DateUtils.today()
      val savedStartMs = prefs.getLong(KEY_START_WALL_CLOCK_MS, 0L)

      if (savedStartMs > 0L) {
        val now = System.currentTimeMillis()
        val elapsedSecs = (now - savedStartMs).coerceAtLeast(0L) / 1000L

        if (elapsedSecs > 0) {
          scope.launch {
            repo.addTimeToTask(savedTaskId, savedDate, elapsedSecs)
          }
        }
        // Continue tracking seamlessly
        startTimer(savedTaskId, savedTaskName, savedDate)
      }
    }
  }

  private fun getPrefs(): SharedPreferences {
    val ctx = context ?: throw IllegalStateException("TimerManager must be initialized before use")
    return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  fun startTimer(taskId: Long, taskName: String, date: String) {
    val ctx = context ?: return
    val repo = repository ?: return

    // If another timer was running, flush it first
    if (_isTimerRunning.value && _runningTaskId.value != null && _runningTaskId.value != taskId) {
      stopTimer()
    }

    startWallClockMs = System.currentTimeMillis()
    _runningTaskId.value = taskId
    _runningTaskName.value = taskName
    _runningTaskDate.value = date
    _runningTimerSessionSeconds.value = 0L
    _isTimerRunning.value = true

    // Save in SharedPreferences for persistent background recovery
    getPrefs().edit()
      .putLong(KEY_TASK_ID, taskId)
      .putString(KEY_TASK_NAME, taskName)
      .putString(KEY_TASK_DATE, date)
      .putLong(KEY_START_WALL_CLOCK_MS, startWallClockMs)
      .apply()

    // Start Foreground Service with notification
    TimerForegroundService.startService(ctx, taskId, taskName, date)

    // Start tick job
    tickerJob?.cancel()
    tickerJob = scope.launch {
      var lastFlushMs = startWallClockMs
      while (isActive) {
        delay(1000)
        val now = System.currentTimeMillis()
        val totalSessionElapsed = ((now - startWallClockMs) / 1000).coerceAtLeast(0L)
        _runningTimerSessionSeconds.value = totalSessionElapsed

        // Automatically flush incremental chunks every 15 seconds to database
        if (now - lastFlushMs >= 15_000L) {
          val chunk = ((now - lastFlushMs) / 1000).coerceAtLeast(0L)
          if (chunk > 0) {
            repo.addTimeToTask(taskId, date, chunk)
            lastFlushMs = now
            // Update startWallClockMs so totalSessionElapsed stays aligned
            startWallClockMs = now
            _runningTimerSessionSeconds.value = 0L
            getPrefs().edit()
              .putLong(KEY_START_WALL_CLOCK_MS, now)
              .apply()
          }
        }
      }
    }
  }

  fun stopTimer() {
    val ctx = context
    val repo = repository
    val taskId = _runningTaskId.value
    val date = _runningTaskDate.value

    tickerJob?.cancel()
    tickerJob = null

    if (taskId != null && date != null && startWallClockMs > 0L && repo != null) {
      val now = System.currentTimeMillis()
      val remainingSecs = ((now - startWallClockMs) / 1000).coerceAtLeast(0L)
      if (remainingSecs > 0) {
        scope.launch {
          repo.addTimeToTask(taskId, date, remainingSecs)
        }
      }
    }

    startWallClockMs = 0L
    _runningTaskId.value = null
    _runningTaskName.value = null
    _runningTaskDate.value = null
    _runningTimerSessionSeconds.value = 0L
    _isTimerRunning.value = false

    try {
      getPrefs().edit().clear().apply()
    } catch (_: Exception) {}

    if (ctx != null) {
      TimerForegroundService.stopService(ctx)
    }
  }

  fun flushTimer() {
    val taskId = _runningTaskId.value
    val date = _runningTaskDate.value
    val repo = repository
    if (taskId != null && date != null && startWallClockMs > 0L && repo != null) {
      val now = System.currentTimeMillis()
      val elapsed = ((now - startWallClockMs) / 1000).coerceAtLeast(0L)
      if (elapsed > 0) {
        scope.launch {
          repo.addTimeToTask(taskId, date, elapsed)
        }
      }
      startWallClockMs = now
      _runningTimerSessionSeconds.value = 0L
      try {
        getPrefs().edit()
          .putLong(KEY_START_WALL_CLOCK_MS, now)
          .apply()
      } catch (_: Exception) {}
    }
  }
}
