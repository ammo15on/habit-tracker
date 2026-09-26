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
  val elapsedSeconds: Long = 0L
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
    return if (current.isRunning && current.taskId == taskId && current.date == date) {
      current.elapsedSeconds
    } else {
      dbSeconds
    }
  }

  fun toggleTimer(taskId: Long, taskName: String, date: String, currentLoggedSeconds: Long = 0L) {
    val current = _timerState.value
    if (current.isRunning && current.taskId == taskId && current.date == date) {
      pauseTimer()
    } else {
      startTimer(taskId, taskName, date, currentLoggedSeconds)
    }
  }

  fun startTimer(taskId: Long, taskName: String, date: String, initialSeconds: Long = 0L) {
    val current = _timerState.value
    if (current.isRunning && current.taskId != taskId) {
      flushTimer()
    }

    _timerState.value = ActiveTimerState(
      taskId = taskId,
      taskName = taskName,
      date = date,
      isRunning = true,
      elapsedSeconds = initialSeconds
    )
    runningTaskId.value = taskId
    isTimerRunning.value = true

    appContext?.let { ctx ->
      TimerForegroundService.start(ctx, taskName, initialSeconds)
    }

    tickerJob?.cancel()
    tickerJob = scope.launch(Dispatchers.Default) {
      while (isActive && _timerState.value.isRunning) {
        delay(1000L)
        val updated = _timerState.value.elapsedSeconds + 1L
        _timerState.value = _timerState.value.copy(elapsedSeconds = updated)

        // Update notification periodically
        if (updated % 5 == 0L) {
          appContext?.let { ctx ->
            TimerForegroundService.start(ctx, taskName, updated)
          }
        }

        // Persist to database every 10 seconds
        if (updated % 10 == 0L) {
          repo?.setTimeSpent(taskId, date, updated)
        }
      }
    }
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
  }

  fun pauseTimer() {
    flushTimer()
    _timerState.value = _timerState.value.copy(isRunning = false)
    isTimerRunning.value = false
    tickerJob?.cancel()
    appContext?.let { ctx ->
      TimerForegroundService.stop(ctx)
    }
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
