package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.model.HabitTask
import com.example.data.model.HabitTaskLog
import com.example.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class TasksAppWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    // Immediately push valid PendingIntent-wired RemoteViews synchronously so launchers never show "Can't load widget"
    for (appWidgetId in appWidgetIds) {
      try {
        val initialViews = buildRemoteViews(context, appWidgetId, emptyList(), emptyList())
        appWidgetManager.updateAppWidget(appWidgetId, initialViews)
      } catch (e: Exception) {
        Log.e(TAG, "Error pushing immediate Tasks widget views", e)
      }
    }

    val pendingResult = goAsync()
    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
      try {
        withTimeoutOrNull(8000L) {
          for (appWidgetId in appWidgetIds) {
            updateAppWidgetInternal(context, appWidgetManager, appWidgetId)
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error in onUpdate", e)
      } finally {
        try {
          pendingResult?.finish()
        } catch (_: Exception) {}
      }
    }
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: android.os.Bundle?
  ) {
    super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
  }

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == ACTION_REFRESH_WIDGET) {
      val pendingResult = goAsync()
      CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
        try {
          withTimeoutOrNull(8000L) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, TasksAppWidgetProvider::class.java))
            for (id in ids) {
              updateAppWidgetInternal(context, appWidgetManager, id)
            }
          }
        } catch (e: Exception) {
          Log.e(TAG, "Error in onReceive refresh", e)
        } finally {
          try {
            pendingResult?.finish()
          } catch (_: Exception) {}
        }
      }
    } else {
      super.onReceive(context, intent)
    }
  }

  companion object {
    private const val TAG = "TasksAppWidget"
    const val ACTION_REFRESH_WIDGET = "com.example.widget.ACTION_REFRESH_WIDGET"

    internal fun buildRemoteViews(
      context: Context,
      appWidgetId: Int,
      allTasks: List<HabitTask>,
      allLogs: List<HabitTaskLog>,
      today: String = DateUtils.today()
    ): RemoteViews {
      val todayIdx = DateUtils.getDayOfWeekIndex(today)
      val logsMap = allLogs.filter { it.date == today }.associateBy { it.taskId }

      val scheduledToday = allTasks.filter { !it.isArchived && it.isScheduledFor(today, todayIdx) }
      val displayTasks = scheduledToday.ifEmpty { allTasks.filter { !it.isArchived } }
        .sortedWith(
          compareBy<HabitTask> { logsMap[it.id]?.isCompleted == true }
            .thenBy { it.id }
        )

      val doneCount = displayTasks.count { logsMap[it.id]?.isCompleted == true }
      val views = RemoteViews(context.packageName, R.layout.widget_tasks_layout)

      val headerTitle = if (displayTasks.isNotEmpty()) {
        "Today's Tasks ($doneCount/${displayTasks.size})"
      } else {
        "Study & Habit Tasks"
      }
      views.setTextViewText(R.id.widget_title, headerTitle)

      // Slot 1
      val task1 = displayTasks.getOrNull(0)
      if (task1 != null) {
        val log1 = logsMap[task1.id]
        val isDone1 = log1?.isCompleted == true
        val loggedMins1 = (log1?.timeSpentSeconds ?: 0L) / 60
        views.setTextViewText(R.id.widget_task_1_title, (if (isDone1) "✓ " else "• ") + task1.name)
        views.setTextViewText(
          R.id.widget_task_1_subtitle,
          formatTaskSubtitle(isDone1, loggedMins1, task1.targetTimeMinutes)
        )
      } else {
        views.setTextViewText(R.id.widget_task_1_title, "No tasks for today")
        views.setTextViewText(R.id.widget_task_1_subtitle, "Tap to add a study task")
      }

      // Slot 2
      val task2 = displayTasks.getOrNull(1)
      if (task2 != null) {
        val log2 = logsMap[task2.id]
        val isDone2 = log2?.isCompleted == true
        val loggedMins2 = (log2?.timeSpentSeconds ?: 0L) / 60
        views.setTextViewText(R.id.widget_task_2_title, (if (isDone2) "✓ " else "• ") + task2.name)
        views.setTextViewText(
          R.id.widget_task_2_subtitle,
          formatTaskSubtitle(isDone2, loggedMins2, task2.targetTimeMinutes)
        )
      } else {
        views.setTextViewText(R.id.widget_task_2_title, if (displayTasks.isEmpty()) "Daily Focus Ready" else "All caught up!")
        views.setTextViewText(R.id.widget_task_2_subtitle, if (displayTasks.isEmpty()) "Plan habits & study blocks" else "Great job today")
      }

      // Slot 3
      val task3 = displayTasks.getOrNull(2)
      if (task3 != null) {
        val log3 = logsMap[task3.id]
        val isDone3 = log3?.isCompleted == true
        val loggedMins3 = (log3?.timeSpentSeconds ?: 0L) / 60
        views.setTextViewText(R.id.widget_task_3_title, (if (isDone3) "✓ " else "• ") + task3.name)
        views.setTextViewText(
          R.id.widget_task_3_subtitle,
          formatTaskSubtitle(isDone3, loggedMins3, task3.targetTimeMinutes)
        )
      } else {
        val remaining = (displayTasks.size - 3).coerceAtLeast(0)
        if (remaining > 0) {
          views.setTextViewText(R.id.widget_task_3_title, "+$remaining more tasks")
          views.setTextViewText(R.id.widget_task_3_subtitle, "Tap to open Study OS")
        } else {
          views.setTextViewText(R.id.widget_task_3_title, "Study & Habit OS")
          views.setTextViewText(R.id.widget_task_3_subtitle, "Track progress & exams")
        }
      }

      val openIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }
      val openPending = PendingIntent.getActivity(
        context,
        appWidgetId,
        openIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      views.setOnClickPendingIntent(R.id.widget_tasks_root, openPending)

      val refreshIntent = Intent(context, TasksAppWidgetProvider::class.java).apply {
        action = ACTION_REFRESH_WIDGET
        setPackage(context.packageName)
      }
      val refreshPending = PendingIntent.getBroadcast(
        context,
        appWidgetId + 1000,
        refreshIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPending)

      return views
    }

    private fun formatTaskSubtitle(isDone: Boolean, loggedMins: Long, targetMinutes: Int): String {
      val status = if (isDone) "Done" else "Pending"
      return when {
        targetMinutes > 0 -> "$status • ${loggedMins}m / ${targetMinutes}m"
        loggedMins > 0 -> "$status • ${loggedMins}m logged"
        else -> "$status • Study Task"
      }
    }

    private fun updateAppWidgetInternal(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
      try {
        val db = AppDatabase.getDatabase(context)
        val allTasks = try {
          db.habitDao().getAllTasksDirect()
        } catch (e: Exception) {
          Log.e(TAG, "DB tasks fetch failed", e)
          emptyList()
        }
        val allLogs = try {
          db.habitDao().getAllLogsDirect()
        } catch (e: Exception) {
          Log.e(TAG, "DB logs fetch failed", e)
          emptyList()
        }

        val views = buildRemoteViews(context, appWidgetId, allTasks, allLogs)
        appWidgetManager.updateAppWidget(appWidgetId, views)
      } catch (e: Exception) {
        Log.e(TAG, "Critical error updating TasksAppWidget", e)
      }
    }

    fun notifyDataChanged(context: Context) {
      try {
        val intent = Intent(context, TasksAppWidgetProvider::class.java).apply {
          action = ACTION_REFRESH_WIDGET
          setPackage(context.packageName)
        }
        context.sendBroadcast(intent)
      } catch (e: Exception) {
        Log.e(TAG, "Failed to send Tasks widget update broadcast", e)
      }
    }
  }
}
