package com.example.util

import android.content.Context
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

data class ActiveTimerState(
  val taskId: Long? = null,
  val taskName: String = "",
  val date: String = "",
  val isRunning: Boolean = false,
  val elapsedSeconds: Long = 0L,
  val startEpochMs: Long = 0L
) {
  val currentTotalSeconds: Long get() = elapsedSeconds
}

object TimerManager {
  private var appContext: Context? = null
  private var repo: HabitRepository? = null
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

  private val _timerState = MutableStateFlow(ActiveTimerState())
  val activeTimerState: StateFlow<ActiveTimerState> = _timerState.asStateFlow()
  val timerState: StateFlow<ActiveTimerState> get() = activeTimerState

  val runningTaskId = MutableStateFlow<Long?>(null)
  val isTimerRunning = MutableStateFlow(false)

  private var tickerJob: Job? = null

  private const val PREFS_NAME = "timer_persistence_prefs"
  private const val KEY_TASK_ID = "key_task_id"
  private const val KEY_TASK_NAME = "key_task_name"
  private const val KEY_DATE = "key_date"
  private const val KEY_IS_RUNNING = "key_is_running"
  private const val KEY_START_EPOCH = "key_start_epoch"
  private const val KEY_ACCUMULATED_SECS = "key_accumulated_secs"

  fun init(context: Context, repository: HabitRepository) {
    appContext = context.applicationContext
    repo = repository

    // Robustly restore active timer state to prevent any time loss upon app restart or process kill
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val taskId = prefs.getLong(KEY_TASK_ID, -1L)
    val isRunning = prefs.getBoolean(KEY_IS_RUNNING, false)
    if (taskId != -1L && isRunning) {
      val taskName = prefs.getString(KEY_TASK_NAME, "Study Task") ?: "Study Task"
      val date = prefs.getString(KEY_DATE, DateUtils.today()) ?: DateUtils.today()
      val startEpoch = prefs.getLong(KEY_START_EPOCH, System.currentTimeMillis())
      val accumulated = prefs.getLong(KEY_ACCUMULATED_SECS, 0L)

      val now = System.currentTimeMillis()
      val calculatedSeconds = ((now - startEpoch) / 1000L).coerceAtLeast(accumulated)

      _timerState.value = ActiveTimerState(
        taskId = taskId,
        taskName = taskName,
        date = date,
        isRunning = true,
        elapsedSeconds = calculatedSeconds,
        startEpochMs = startEpoch
      )
      runningTaskId.value = taskId
      isTimerRunning.value = true

      startTickerLoop(taskId, taskName, date, startEpoch)
    }
  }

  private fun persistState(taskId: Long?, taskName: String, date: String, isRunning: Boolean, startEpochMs: Long, elapsedSeconds: Long) {
    appContext?.let { ctx ->
      val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      prefs.edit().apply {
        if (taskId != null && isRunning) {
          putLong(KEY_TASK_ID, taskId)
          putString(KEY_TASK_NAME, taskName)
          putString(KEY_DATE, date)
          putBoolean(KEY_IS_RUNNING, true)
          putLong(KEY_START_EPOCH, startEpochMs)
          putLong(KEY_ACCUMULATED_SECS, elapsedSeconds)
        } else {
          putBoolean(KEY_IS_RUNNING, false)
          remove(KEY_TASK_ID)
        }
        apply()
      }
    }
  }

  fun isTimerRunning(taskId: Long): Boolean {
    val current = _timerState.value
    return current.isRunning && current.taskId == taskId
  }

  fun isTimerRunning(taskId: Long, date: String): Boolean {
    val current = _timerState.value
    return current.isRunning && current.taskId == taskId && current.date == date
  }

  fun getEffectiveTaskSeconds(taskId: Long, date: String, dbSeconds: Long): Long {
    val current = _timerState.value
    return if (current.taskId == taskId && current.date == date) {
      current.elapsedSeconds
    } else {
      dbSeconds
    }
  }

  fun toggleTimer(taskId: Long, taskName: String, date: String, currentLoggedSeconds: Long = 0L) {
    val current = _timerState.value
    if (current.isRunning && current.taskId == taskId && current.date == date) {
      pauseTimer()
    } else if (!current.isRunning && current.taskId == taskId && current.date == date) {
      resumeTimer()
    } else {
      startTimer(taskId, taskName, date, currentLoggedSeconds)
    }
  }

  private fun playFeedbackSound() {
    try {
      appContext?.let { ctx ->
        val ringtoneUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = android.media.RingtoneManager.getRingtone(ctx, ringtoneUri)
        ringtone?.play()
      }
    } catch (_: Exception) {}
  }

  fun startTimer(taskId: Long, taskName: String, date: String, initialSeconds: Long = 0L) {
    val current = _timerState.value
    if (current.isRunning && current.taskId != taskId) {
      flushTimer()
    }

    val now = System.currentTimeMillis()
    val baseStartEpoch = now - (initialSeconds * 1000L)

    _timerState.value = ActiveTimerState(
      taskId = taskId,
      taskName = taskName,
      date = date,
      isRunning = true,
      elapsedSeconds = initialSeconds,
      startEpochMs = baseStartEpoch
    )
    runningTaskId.value = taskId
    isTimerRunning.value = true

    persistState(taskId, taskName, date, true, baseStartEpoch, initialSeconds)

    appContext?.let { ctx ->
      TimerForegroundService.start(ctx, taskName, initialSeconds, isPaused = false)
    }

    playFeedbackSound()
    startTickerLoop(taskId, taskName, date, baseStartEpoch)
  }

  fun resumeTimer() {
    val current = _timerState.value
    val taskId = current.taskId ?: return
    val taskName = current.taskName
    val date = current.date
    val accumulated = current.elapsedSeconds

    val now = System.currentTimeMillis()
    val baseStartEpoch = now - (accumulated * 1000L)

    _timerState.value = current.copy(
      isRunning = true,
      startEpochMs = baseStartEpoch
    )
    runningTaskId.value = taskId
    isTimerRunning.value = true

    persistState(taskId, taskName, date, true, baseStartEpoch, accumulated)

    appContext?.let { ctx ->
      TimerForegroundService.start(ctx, taskName, accumulated, isPaused = false)
    }

    playFeedbackSound()
    startTickerLoop(taskId, taskName, date, baseStartEpoch)
  }

  private fun startTickerLoop(taskId: Long, taskName: String, date: String, baseStartEpoch: Long) {
    tickerJob?.cancel()
    tickerJob = scope.launch(Dispatchers.Default) {
      var lastSavedSecs = _timerState.value.elapsedSeconds
      while (isActive && _timerState.value.isRunning) {
        delay(1000L)
        val now = System.currentTimeMillis()
        val calculatedSeconds = ((now - baseStartEpoch) / 1000L).coerceAtLeast(0L)

        _timerState.value = _timerState.value.copy(elapsedSeconds = calculatedSeconds)

        // Persist accumulated seconds continuously to SharedPreferences every second so time is NEVER lost
        persistState(taskId, taskName, date, true, baseStartEpoch, calculatedSeconds)

        // Update notification periodically for smooth background tracking
        if (calculatedSeconds % 2 == 0L) {
          appContext?.let { ctx ->
            TimerForegroundService.start(ctx, taskName, calculatedSeconds, isPaused = false)
          }
        }

        // Persist to Room database frequently
        if (calculatedSeconds != lastSavedSecs) {
          lastSavedSecs = calculatedSeconds
          repo?.setTimeSpent(taskId, date, calculatedSeconds)
        }
      }
    }
  }

  fun pauseTimer() {
    flushTimer()
    val current = _timerState.value
    _timerState.value = current.copy(isRunning = false)
    isTimerRunning.value = false
    tickerJob?.cancel()

    persistState(null, "", "", false, 0L, 0L)

    appContext?.let { ctx ->
      if (current.taskId != null) {
        TimerForegroundService.start(ctx, current.taskName, current.elapsedSeconds, isPaused = true)
      } else {
        TimerForegroundService.stop(ctx)
      }
    }
    playFeedbackSound()
  }

  fun stopTimer() {
    flushTimer()
    _timerState.value = ActiveTimerState()
    runningTaskId.value = null
    isTimerRunning.value = false
    tickerJob?.cancel()

    persistState(null, "", "", false, 0L, 0L)

    appContext?.let { ctx ->
      TimerForegroundService.stop(ctx)
    }
    playFeedbackSound()
  }

  fun stopAndReset() = stopTimer()

  fun flushTimer() {
    val current = _timerState.value
    if (current.taskId != null && current.date.isNotBlank()) {
      val tid = current.taskId
      val dt = current.date
      val secs = current.elapsedSeconds
      scope.launch(Dispatchers.IO) {
        repo?.setTimeSpent(tid, dt, secs)
      }
    }
  }
}
