package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.data.repository.HabitRepository
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ActiveTimerState(
  val taskId: Long? = null,
  val taskName: String? = null,
  val date: String? = null,
  val initialSeconds: Long = 0L,
  val sessionSeconds: Long = 0L,
  val isRunning: Boolean = false
) {
  val currentTotalSeconds: Long
    get() = initialSeconds + sessionSeconds
}

object TimerManager {
  private const val PREFS_NAME = "habit_tracker_timer_prefs"
  private const val KEY_TASK_ID = "active_task_id"
  private const val KEY_TASK_NAME = "active_task_name"
  private const val KEY_TASK_DATE = "active_task_date"
  private const val KEY_START_WALL_CLOCK_MS = "active_start_wall_clock_ms"
  private const val KEY_INITIAL_SECONDS = "active_initial_seconds"

  private var context: Context? = null
  private var repository: HabitRepository? = null
  private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
  private var tickerJob: Job? = null
  private var isInitialized = false

  private var startWallClockMs: Long = 0L
  private var initialSeconds: Long = 0L

  // In-memory cache of latest confirmed seconds for (taskId, date)
  // Guarantees zero-flicker and monotonic display across task switches before Room emits
  private val cachedTaskSeconds = ConcurrentHashMap<String, Long>()

  private val _activeTimerState = MutableStateFlow(ActiveTimerState())
  val activeTimerState: StateFlow<ActiveTimerState> = _activeTimerState.asStateFlow()

  val runningTaskId: StateFlow<Long?> = _activeTimerState
    .map { it.taskId }
    .stateIn(scope, SharingStarted.Eagerly, _activeTimerState.value.taskId)

  val isTimerRunning: StateFlow<Boolean> = _activeTimerState
    .map { it.isRunning }
    .stateIn(scope, SharingStarted.Eagerly, _activeTimerState.value.isRunning)

  fun isTimerRunning(taskId: Long, date: String? = null): Boolean {
    val state = _activeTimerState.value
    if (!state.isRunning || state.taskId != taskId) return false
    return date == null || state.date == date
  }

  fun getEffectiveTaskSeconds(taskId: Long, date: String, dbSeconds: Long): Long {
    val state = _activeTimerState.value
    if (state.isRunning && state.taskId == taskId && state.date == date) {
      return state.currentTotalSeconds
    }
    val cached = cachedTaskSeconds["$taskId:$date"] ?: 0L
    return maxOf(dbSeconds, cached)
  }

  fun initialize(appContext: Context, repo: HabitRepository) {
    context = appContext.applicationContext
    repository = repo

    if (isInitialized) return
    isInitialized = true

    try {
      val prefs = getPrefs() ?: return
      val savedTaskId = prefs.getLong(KEY_TASK_ID, -1L)
      if (savedTaskId != -1L) {
        val savedDate = prefs.getString(KEY_TASK_DATE, DateUtils.today()) ?: DateUtils.today()
        val savedStartMs = prefs.getLong(KEY_START_WALL_CLOCK_MS, 0L)
        val savedInitial = prefs.getLong(KEY_INITIAL_SECONDS, 0L)

        if (savedStartMs > 0L) {
          val now = System.currentTimeMillis()
          val elapsedSecs = (now - savedStartMs).coerceAtLeast(0L) / 1000L

          // If recovered elapsed time is reasonable (< 12 hours), flush it to database
          if (elapsedSecs in 1..43200) {
            val total = savedInitial + elapsedSecs
            cachedTaskSeconds["$savedTaskId:$savedDate"] = total
            scope.launch(Dispatchers.IO) {
              repo.addTimeToTask(savedTaskId, savedDate, elapsedSecs)
            }
          }
          // Clear prefs on launch recovery so it doesn't run indefinitely
          prefs.edit().clear().apply()
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun getPrefs(): SharedPreferences? {
    val ctx = context ?: return null
    return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  fun startTimer(taskId: Long, taskName: String, date: String, currentBaseSeconds: Long = 0L) {
    val repo = repository

    // If another timer was running, flush and stop it cleanly first
    if (_activeTimerState.value.isRunning && _activeTimerState.value.taskId != taskId) {
      stopTimerInternal(flushOnly = true)
    }

    startWallClockMs = System.currentTimeMillis()
    initialSeconds = currentBaseSeconds

    _activeTimerState.value = ActiveTimerState(
      taskId = taskId,
      taskName = taskName,
      date = date,
      initialSeconds = currentBaseSeconds,
      sessionSeconds = 0L,
      isRunning = true
    )

    // Save in SharedPreferences for crash/power loss recovery
    try {
      getPrefs()?.edit()
        ?.putLong(KEY_TASK_ID, taskId)
        ?.putString(KEY_TASK_NAME, taskName)
        ?.putString(KEY_TASK_DATE, date)
        ?.putLong(KEY_START_WALL_CLOCK_MS, startWallClockMs)
        ?.putLong(KEY_INITIAL_SECONDS, currentBaseSeconds)
        ?.apply()
    } catch (_: Exception) {}

    // Update Foreground Service with notification
    try {
      context?.let { ctx ->
        TimerForegroundService.startService(ctx, taskId, taskName, date)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }

    // Start tick job
    tickerJob?.cancel()
    tickerJob = scope.launch {
      while (isActive) {
        delay(1000)
        val now = System.currentTimeMillis()
        val sessionSecs = ((now - startWallClockMs) / 1000).coerceAtLeast(0L)
        val totalSecs = initialSeconds + sessionSecs

        _activeTimerState.value = _activeTimerState.value.copy(
          sessionSeconds = sessionSecs
        )
        cachedTaskSeconds["$taskId:$date"] = totalSecs
      }
    }
  }

  private fun stopTimerInternal(flushOnly: Boolean = false) {
    val ctx = context
    val repo = repository
    val state = _activeTimerState.value
    val taskId = state.taskId
    val date = state.date

    tickerJob?.cancel()
    tickerJob = null

    if (taskId != null && date != null && startWallClockMs > 0L && repo != null) {
      val now = System.currentTimeMillis()
      val sessionSecs = ((now - startWallClockMs) / 1000).coerceAtLeast(0L)
      if (sessionSecs > 0) {
        val finalTotal = initialSeconds + sessionSecs
        cachedTaskSeconds["$taskId:$date"] = finalTotal
        scope.launch(Dispatchers.IO) {
          repo.addTimeToTask(taskId, date, sessionSecs)
        }
      }
    }

    startWallClockMs = 0L
    initialSeconds = 0L

    if (!flushOnly) {
      _activeTimerState.value = ActiveTimerState()
      try {
        getPrefs()?.edit()?.clear()?.apply()
      } catch (_: Exception) {}

      if (ctx != null) {
        try {
          TimerForegroundService.stopService(ctx)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  fun stopTimer() {
    stopTimerInternal(flushOnly = false)
  }

  fun flushTimer() {
    val state = _activeTimerState.value
    val taskId = state.taskId
    val date = state.date
    val repo = repository
    if (taskId != null && date != null && startWallClockMs > 0L && repo != null) {
      val now = System.currentTimeMillis()
      val sessionSecs = ((now - startWallClockMs) / 1000).coerceAtLeast(0L)
      if (sessionSecs > 0) {
        val total = initialSeconds + sessionSecs
        cachedTaskSeconds["$taskId:$date"] = total
        scope.launch(Dispatchers.IO) {
          repo.addTimeToTask(taskId, date, sessionSecs)
        }
        initialSeconds = total
        startWallClockMs = now
        _activeTimerState.value = _activeTimerState.value.copy(
          initialSeconds = total,
          sessionSeconds = 0L
        )
      }
    }
  }
}
