package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NeetProgressAppWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    for (appWidgetId in appWidgetIds) {
      updateAppWidget(context, appWidgetManager, appWidgetId)
    }
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val thisWidget = ComponentName(context, NeetProgressAppWidgetProvider::class.java)
    val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
    for (appWidgetId in appWidgetIds) {
      updateAppWidget(context, appWidgetManager, appWidgetId)
    }
  }

  companion object {
    fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
      CoroutineScope(Dispatchers.IO).launch {
        val db = AppDatabase.getDatabase(context)
        val chapters = try { db.habitDao().getAllNeetChapters().first() } catch (_: Exception) { emptyList() }
        val total = chapters.size.coerceAtLeast(1)
        val completed = chapters.count { it.isCompleted }
        val pyq = chapters.count { it.isPyqDone }
        val ncert = chapters.count { it.isRevisionDone }
        val overallPercent = ((completed + pyq + ncert) * 100) / (total * 3)

        val views = RemoteViews(context.packageName, R.layout.widget_neet_progress).apply {
          setTextViewText(R.id.widget_overall_percent, "$overallPercent%")

          setTextViewText(R.id.widget_chapters_count, "$completed / $total")
          setProgressBar(R.id.widget_progress_chapters, total, completed, false)

          setTextViewText(R.id.widget_pyq_count, "$pyq / $total")
          setProgressBar(R.id.widget_progress_pyq, total, pyq, false)

          setTextViewText(R.id.widget_ncert_count, "$ncert / $total")
          setProgressBar(R.id.widget_progress_ncert, total, ncert, false)

          val intent = Intent(context, MainActivity::class.java)
          val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )
          setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
      }
    }

    fun updateAllWidgets(context: Context) {
      val intent = Intent(context, NeetProgressAppWidgetProvider::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
      }
      val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
        ComponentName(context, NeetProgressAppWidgetProvider::class.java)
      )
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
      context.sendBroadcast(intent)
    }
  }
}
