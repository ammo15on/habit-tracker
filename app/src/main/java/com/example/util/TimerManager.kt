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

  fun init(context: Context, repository: HabitRepository) {
    appContext = context.applicationContext
    repo = repository
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

    appContext?.let { ctx ->
      TimerForegroundService.start(ctx, taskName, accumulated, isPaused = false)
    }

    playFeedbackSound()
    startTickerLoop(taskId, taskName, date, baseStartEpoch)
  }

  private fun startTickerLoop(taskId: Long, taskName: String, date: String, baseStartEpoch: Long) {
    tickerJob?.cancel()
    tickerJob = scope.launch(Dispatchers.Default) {
      var lastSavedSecs = 0L
      while (isActive && _timerState.value.isRunning) {
        delay(1000L)
        val now = System.currentTimeMillis()
        val calculatedSeconds = ((now - baseStartEpoch) / 1000L).coerceAtLeast(0L)

        _timerState.value = _timerState.value.copy(elapsedSeconds = calculatedSeconds)

        // Update notification periodically for smooth background tracking
        if (calculatedSeconds % 5 == 0L) {
          appContext?.let { ctx ->
            TimerForegroundService.start(ctx, taskName, calculatedSeconds, isPaused = false)
          }
        }

        // Persist to database every 5 seconds
        if (calculatedSeconds - lastSavedSecs >= 5L) {
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
