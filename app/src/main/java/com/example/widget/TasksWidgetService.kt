package com.example.widget

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.model.HabitTask
import com.example.util.DateUtils
import com.example.util.TimerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TasksWidgetService : RemoteViewsService() {
  override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
    return TasksRemoteViewsFactory(applicationContext)
  }
}

private sealed class WidgetListItem {
  data class Header(val title: String) : WidgetListItem()
  data class TaskRow(
    val task: HabitTask,
    val date: String,
    val isCompleted: Boolean,
    val timeSpentSeconds: Long,
    val isRunning: Boolean
  ) : WidgetListItem()
}

private class TasksRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

  private val items = mutableListOf<WidgetListItem>()

  override fun onCreate() {
    // Initial setup handled in onDataSetChanged
  }

  override fun onDataSetChanged() {
    items.clear()
    try {
      val db = AppDatabase.getDatabase(context)
      val allTasks = runBlocking(Dispatchers.IO) {
        try { db.habitDao().getAllTasks().first() } catch (_: Exception) { emptyList() }
      }
      val allLogs = runBlocking(Dispatchers.IO) {
        try { db.habitDao().getAllLogs().first() } catch (_: Exception) { emptyList() }
      }

      val today = DateUtils.today()
      val tomorrow = DateUtils.getNextDay(today)
      val todayIdx = DateUtils.getDayOfWeekIndex(today)
      val tomorrowIdx = DateUtils.getDayOfWeekIndex(tomorrow)

      val currentTimer = TimerManager.activeTimerState.value
      val logsByDateAndTask = allLogs.associateBy { "${it.taskId}_${it.date}" }

      // 1. Group tasks scheduled for Today
      val todayTasks = allTasks.filter { it.isScheduledFor(today, todayIdx) }

      // 2. Group tasks scheduled for Tomorrow
      val tomorrowTasks = allTasks.filter {
        !todayTasks.contains(it) && it.isScheduledFor(tomorrow, tomorrowIdx)
      }

      // 3. Group upcoming tasks for dates beyond tomorrow
      val upcomingTasks = allTasks.filter { task ->
        if (todayTasks.contains(task) || tomorrowTasks.contains(task)) return@filter false
        val specificDate = task.targetDate
        if (specificDate != null && specificDate > tomorrow) return@filter true
        val targetDatesList = task.targetDates?.split(",")?.map { it.trim() } ?: emptyList()
        targetDatesList.any { it > tomorrow }
      }

      // 4. Any remaining active or recurring tasks
      val otherTasks = allTasks.filter {
        !todayTasks.contains(it) && !tomorrowTasks.contains(it) && !upcomingTasks.contains(it)
      }

      // Construct segregated list with date headers (visual line breaks)
      if (todayTasks.isNotEmpty()) {
        items.add(WidgetListItem.Header("TODAY • " + DateUtils.formatShortDate(today).uppercase()))
        for (task in todayTasks) {
          val logKey = "${task.id}_$today"
          val log = logsByDateAndTask[logKey]
          val isRunning = currentTimer.isRunning && currentTimer.taskId == task.id && currentTimer.date == today
          val effectiveSeconds = if (isRunning) currentTimer.elapsedSeconds else (log?.timeSpentSeconds ?: 0L)
          items.add(
            WidgetListItem.TaskRow(
              task = task,
              date = today,
              isCompleted = log?.isCompleted ?: false,
              timeSpentSeconds = effectiveSeconds,
              isRunning = isRunning
            )
          )
        }
      }

      if (tomorrowTasks.isNotEmpty()) {
        items.add(WidgetListItem.Header("TOMORROW • " + DateUtils.formatShortDate(tomorrow).uppercase()))
        for (task in tomorrowTasks) {
          val logKey = "${task.id}_$tomorrow"
          val log = logsByDateAndTask[logKey]
          items.add(
            WidgetListItem.TaskRow(
              task = task,
              date = tomorrow,
              isCompleted = log?.isCompleted ?: false,
              timeSpentSeconds = log?.timeSpentSeconds ?: 0L,
              isRunning = false
            )
          )
        }
      }

      if (upcomingTasks.isNotEmpty()) {
        items.add(WidgetListItem.Header("UPCOMING TASKS"))
        for (task in upcomingTasks) {
          val date = task.targetDate ?: "Upcoming"
          items.add(
            WidgetListItem.TaskRow(
              task = task,
              date = date,
              isCompleted = false,
              timeSpentSeconds = 0L,
              isRunning = false
            )
          )
        }
      }

      if (otherTasks.isNotEmpty() && items.isEmpty()) {
        items.add(WidgetListItem.Header("ALL HABIT TASKS"))
        for (task in otherTasks) {
          items.add(
            WidgetListItem.TaskRow(
              task = task,
              date = today,
              isCompleted = false,
              timeSpentSeconds = 0L,
              isRunning = false
            )
          )
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override fun onDestroy() {
    items.clear()
  }

  override fun getCount(): Int = items.size

  override fun getViewAt(position: Int): RemoteViews {
    if (position !in items.indices) {
      return RemoteViews(context.packageName, R.layout.widget_tasks_header_item)
    }

    return when (val item = items[position]) {
      is WidgetListItem.Header -> {
        RemoteViews(context.packageName, R.layout.widget_tasks_header_item).apply {
          setTextViewText(R.id.widget_header_title, item.title)
        }
      }
      is WidgetListItem.TaskRow -> {
        RemoteViews(context.packageName, R.layout.widget_tasks_task_item).apply {
          // Task Title
          setTextViewText(R.id.widget_item_title, item.task.name)

          // Subtitle: Target & Logged minutes
          val loggedMinutes = item.timeSpentSeconds / 60
          val targetMinutes = item.task.targetTimeMinutes
          val subtitle = if (targetMinutes > 0) {
            "⏱ Logged: ${loggedMinutes}m / Target: ${targetMinutes}m"
          } else if (loggedMinutes > 0) {
            "⏱ Logged: ${loggedMinutes}m"
          } else {
            "Count-up timer"
          }
          setTextViewText(R.id.widget_item_subtitle, subtitle)

          // In Front: Play/Pause button icon state
          if (item.isRunning) {
            setImageViewResource(R.id.widget_item_play_pause_btn, R.drawable.ic_widget_pause)
          } else {
            setImageViewResource(R.id.widget_item_play_pause_btn, R.drawable.ic_widget_play)
          }

          // In Front: Toggle timer directly without opening app
          val playPauseFillIn = Intent().apply {
            action = TasksAppWidgetProvider.ACTION_TOGGLE_TIMER
            putExtra(TasksAppWidgetProvider.EXTRA_TASK_ID, item.task.id)
            putExtra(TasksAppWidgetProvider.EXTRA_TASK_NAME, item.task.name)
            putExtra(TasksAppWidgetProvider.EXTRA_TASK_DATE, item.date)
          }
          setOnClickFillInIntent(R.id.widget_item_play_pause_btn, playPauseFillIn)

          // Clicking text area opens app
          val openAppFillIn = Intent().apply {
            action = TasksAppWidgetProvider.ACTION_OPEN_TASK
            putExtra(TasksAppWidgetProvider.EXTRA_TASK_ID, item.task.id)
          }
          setOnClickFillInIntent(R.id.widget_item_click_area, openAppFillIn)

          // Completion status chip
          if (item.isCompleted) {
            setViewVisibility(R.id.widget_item_status, View.VISIBLE)
          } else {
            setViewVisibility(R.id.widget_item_status, View.GONE)
          }
        }
      }
    }
  }

  override fun getLoadingView(): RemoteViews? = null

  override fun getViewTypeCount(): Int = 2

  override fun getItemId(position: Int): Long = position.toLong()

  override fun hasStableIds(): Boolean = false
}
