package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.util.DateUtils
import com.example.util.TimerManager

class TasksAppWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    for (appWidgetId in appWidgetIds) {
      updateAppWidget(context, appWidgetManager, appWidgetId)
    }
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    when (intent.action) {
      ACTION_TOGGLE_TIMER -> {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val taskName = intent.getStringExtra(EXTRA_TASK_NAME) ?: "Task"
        val taskDate = intent.getStringExtra(EXTRA_TASK_DATE) ?: DateUtils.today()
        if (taskId != -1L) {
          TimerManager.init(context)
          TimerManager.toggleTimer(taskId, taskName, taskDate)
          notifyDataChanged(context)
        }
      }
      ACTION_CYCLE_TRANSPARENCY -> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentLevel = prefs.getInt(KEY_TRANSPARENCY_LEVEL, 0)
        val nextLevel = (currentLevel + 1) % TRANSPARENCY_LEVELS.size
        prefs.edit().putInt(KEY_TRANSPARENCY_LEVEL, nextLevel).apply()
        updateAllWidgets(context)
      }
      ACTION_REFRESH_WIDGET -> {
        notifyDataChanged(context)
      }
      ACTION_OPEN_TASK -> {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(launchIntent)
      }
    }
  }

  companion object {
    const val ACTION_TOGGLE_TIMER = "com.example.widget.ACTION_TOGGLE_TIMER"
    const val ACTION_CYCLE_TRANSPARENCY = "com.example.widget.ACTION_CYCLE_TRANSPARENCY"
    const val ACTION_REFRESH_WIDGET = "com.example.widget.ACTION_REFRESH_WIDGET"
    const val ACTION_OPEN_TASK = "com.example.widget.ACTION_OPEN_TASK"

    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_NAME = "extra_task_name"
    const val EXTRA_TASK_DATE = "extra_task_date"

    private const val PREFS_NAME = "tasks_widget_prefs"
    private const val KEY_TRANSPARENCY_LEVEL = "widget_transparency_level"

    private data class TransparencyOption(val label: String, val colorArgb: Int)

    private val TRANSPARENCY_LEVELS = listOf(
      TransparencyOption("💧 100%", 0xFF0F172A.toInt()), // Solid dark slate
      TransparencyOption("💧 75%", 0xC00F172A.toInt()),  // 75% opacity
      TransparencyOption("💧 50%", 0x800F172A.toInt()),  // 50% opacity
      TransparencyOption("💧 25%", 0x400F172A.toInt())   // 25% opacity
    )

    fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
      val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      val levelIndex = prefs.getInt(KEY_TRANSPARENCY_LEVEL, 0).coerceIn(0, TRANSPARENCY_LEVELS.lastIndex)
      val option = TRANSPARENCY_LEVELS[levelIndex]

      val views = RemoteViews(context.packageName, R.layout.widget_tasks_layout).apply {
        // Apply dynamic background transparency
        setInt(R.id.widget_tasks_root, "setBackgroundColor", option.colorArgb)
        setTextViewText(R.id.widget_btn_transparency, option.label)

        // Setup transparency cycle broadcast
        val cycleIntent = Intent(context, TasksAppWidgetProvider::class.java).apply {
          action = ACTION_CYCLE_TRANSPARENCY
        }
        val cyclePending = PendingIntent.getBroadcast(
          context,
          101,
          cycleIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setOnClickPendingIntent(R.id.widget_btn_transparency, cyclePending)

        // Setup refresh broadcast
        val refreshIntent = Intent(context, TasksAppWidgetProvider::class.java).apply {
          action = ACTION_REFRESH_WIDGET
        }
        val refreshPending = PendingIntent.getBroadcast(
          context,
          102,
          refreshIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPending)

        // Setup RemoteViewsService adapter for scrollable list
        val serviceIntent = Intent(context, TasksWidgetService::class.java).apply {
          putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
          data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
        }
        setRemoteAdapter(R.id.widget_tasks_list, serviceIntent)
        setEmptyView(R.id.widget_tasks_list, R.id.widget_empty_view)

        // Setup fill-in intent template for list item clicks and in-front play/pause buttons
        val templateIntent = Intent(context, TasksAppWidgetProvider::class.java)
        val templatePending = PendingIntent.getBroadcast(
          context,
          appWidgetId,
          templateIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        setPendingIntentTemplate(R.id.widget_tasks_list, templatePending)
      }

      appWidgetManager.updateAppWidget(appWidgetId, views)
      appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_tasks_list)
    }

    fun updateAllWidgets(context: Context) {
      val appWidgetManager = AppWidgetManager.getInstance(context)
      val ids = appWidgetManager.getAppWidgetIds(
        ComponentName(context, TasksAppWidgetProvider::class.java)
      )
      for (id in ids) {
        updateAppWidget(context, appWidgetManager, id)
      }
    }

    fun notifyDataChanged(context: Context) {
      val appWidgetManager = AppWidgetManager.getInstance(context)
      val ids = appWidgetManager.getAppWidgetIds(
        ComponentName(context, TasksAppWidgetProvider::class.java)
      )
      if (ids.isNotEmpty()) {
        appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.widget_tasks_list)
      }
    }
  }
}
