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
import com.example.data.model.NeetChapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class NeetProgressAppWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    // Push immediate synchronous RemoteViews before Room IO so home screen launcher never fails widget creation
    for (appWidgetId in appWidgetIds) {
      try {
        val initialViews = buildRemoteViews(context, appWidgetId, emptyList())
        appWidgetManager.updateAppWidget(appWidgetId, initialViews)
      } catch (e: Exception) {
        Log.e(TAG, "Error pushing immediate NEET widget views", e)
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
    if (intent.action == ACTION_REFRESH_NEET_WIDGET) {
      val pendingResult = goAsync()
      CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
        try {
          withTimeoutOrNull(8000L) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, NeetProgressAppWidgetProvider::class.java))
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
    private const val TAG = "NeetProgressAppWidget"
    const val ACTION_REFRESH_NEET_WIDGET = "com.example.widget.ACTION_REFRESH_NEET_WIDGET"

    internal fun buildRemoteViews(
      context: Context,
      appWidgetId: Int,
      chapters: List<NeetChapter>
    ): RemoteViews {
      val defaultSize = NeetChapter.DEFAULT_CHAPTERS.size.coerceAtLeast(1)
      val total = if (chapters.isNotEmpty()) chapters.size else defaultSize
      val completed = chapters.count { it.isCompleted }
      val pyq = chapters.count { it.isPyqDone }
      val ncert = chapters.count { it.ncertReadCount > 0 || it.isRevisionDone }
      val overallPercent = ((completed + pyq + ncert) * 100) / (total * 3)

      val views = RemoteViews(context.packageName, R.layout.widget_neet_progress).apply {
        setTextViewText(R.id.widget_overall_percent, "$overallPercent%")

        setTextViewText(R.id.widget_chapters_count, "$completed / $total")
        setProgressBar(R.id.widget_progress_chapters, total, completed, false)

        setTextViewText(R.id.widget_pyq_count, "$pyq / $total")
        setProgressBar(R.id.widget_progress_pyq, total, pyq, false)

        setTextViewText(R.id.widget_ncert_count, "$ncert / $total")
        setProgressBar(R.id.widget_progress_ncert, total, ncert, false)

        val openIntent = Intent(context, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
          context,
          appWidgetId,
          openIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setOnClickPendingIntent(R.id.widget_root, openPending)

        val refreshIntent = Intent(context, NeetProgressAppWidgetProvider::class.java).apply {
          action = ACTION_REFRESH_NEET_WIDGET
          setPackage(context.packageName)
        }
        val refreshPending = PendingIntent.getBroadcast(
          context,
          appWidgetId + 2000,
          refreshIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setOnClickPendingIntent(R.id.widget_neet_btn_refresh, refreshPending)
      }
      return views
    }

    private fun updateAppWidgetInternal(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
      try {
        val db = AppDatabase.getDatabase(context)
        val chapters = try {
          db.habitDao().getAllNeetChaptersDirect()
        } catch (e: Exception) {
          Log.e(TAG, "Error fetching NEET chapters for widget", e)
          emptyList()
        }

        val views = buildRemoteViews(context, appWidgetId, chapters)
        appWidgetManager.updateAppWidget(appWidgetId, views)
      } catch (e: Exception) {
        Log.e(TAG, "Critical error updating NEET widget", e)
      }
    }

    fun notifyDataChanged(context: Context) {
      try {
        val intent = Intent(context, NeetProgressAppWidgetProvider::class.java).apply {
          action = ACTION_REFRESH_NEET_WIDGET
          setPackage(context.packageName)
        }
        context.sendBroadcast(intent)
      } catch (e: Exception) {
        Log.e(TAG, "Failed to send NEET widget update broadcast", e)
      }
    }
  }
}
