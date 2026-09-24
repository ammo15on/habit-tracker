package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class NeetProgressAppWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    updateWidgetsInternal(context, appWidgetManager, appWidgetIds)
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    if (intent.action == ACTION_UPDATE_WIDGETS || intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
      val appWidgetManager = AppWidgetManager.getInstance(context)
      val ids = appWidgetManager.getAppWidgetIds(
        ComponentName(context, NeetProgressAppWidgetProvider::class.java)
      )
      updateWidgetsInternal(context, appWidgetManager, ids)
    }
  }

  companion object {
    const val ACTION_UPDATE_WIDGETS = "com.example.widget.ACTION_UPDATE_NEET_WIDGET"
    const val EXTRA_TARGET_TAB = "TARGET_TAB"

    fun updateAllWidgets(context: Context) {
      val intent = Intent(context, NeetProgressAppWidgetProvider::class.java).apply {
        action = ACTION_UPDATE_WIDGETS
      }
      context.sendBroadcast(intent)
    }

    fun pinWidgetToHomeScreen(context: Context): Boolean {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val myProvider = ComponentName(context, NeetProgressAppWidgetProvider::class.java)
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
          appWidgetManager.requestPinAppWidget(myProvider, null, null)
          return true
        }
      }
      return false
    }

    private fun updateWidgetsInternal(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetIds: IntArray
    ) {
      if (appWidgetIds.isEmpty()) return

      CoroutineScope(Dispatchers.IO).launch {
        try {
          val dao = AppDatabase.getDatabase(context).habitDao()
          val chapters = dao.getAllNeetChapters().first()

          val total = chapters.size
          val chaptersDone = chapters.count { it.isCompleted }
          val pyqDone = chapters.count { it.isPyqDone }
          val ncertDone = chapters.count { it.isRevisionDone }

          val chPercent = if (total > 0) (chaptersDone * 100) / total else 0
          val pyqPercent = if (total > 0) (pyqDone * 100) / total else 0
          val ncertPercent = if (total > 0) (ncertDone * 100) / total else 0

          val overall = ((chPercent + pyqPercent + ncertPercent) / 3)

          val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_TAB, "DETAIL")
          }
          val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          } else {
            PendingIntent.FLAG_UPDATE_CURRENT
          }
          val pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, pendingFlags)

          for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_neet_progress)

            views.setTextViewText(R.id.widget_title, "NEET Prep Tracker")
            views.setTextViewText(R.id.widget_overall_percent, "$overall% Overall")

            // Chapters
            views.setTextViewText(R.id.widget_chapters_count, "$chaptersDone/$total")
            views.setProgressBar(R.id.widget_chapters_progress, 100, chPercent, false)
            views.setTextViewText(R.id.widget_chapters_percent, "$chPercent%")

            // PYQ Done
            views.setTextViewText(R.id.widget_pyq_count, "$pyqDone/$total")
            views.setProgressBar(R.id.widget_pyq_progress, 100, pyqPercent, false)
            views.setTextViewText(R.id.widget_pyq_percent, "$pyqPercent%")

            // NCERT Read
            views.setTextViewText(R.id.widget_ncert_count, "$ncertDone/$total")
            views.setProgressBar(R.id.widget_ncert_progress, 100, ncertPercent, false)
            views.setTextViewText(R.id.widget_ncert_percent, "$ncertPercent%")

            views.setTextViewText(
              R.id.widget_status_text,
              "Tap to open NEET Study Hub • $chaptersDone done, ${total - chaptersDone} left"
            )

            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }
}
