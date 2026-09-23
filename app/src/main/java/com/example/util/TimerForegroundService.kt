package com.example.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class TimerForegroundService : Service() {

  companion object {
    const val CHANNEL_ID = "habit_active_timer_channel"
    const val NOTIFICATION_ID = 2001

    const val ACTION_START = "com.example.action.START_TIMER"
    const val ACTION_STOP_SERVICE = "com.example.action.STOP_SERVICE"
    const val ACTION_USER_STOP = "com.example.action.USER_STOP"

    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_NAME = "extra_task_name"
    const val EXTRA_DATE = "extra_date"

    fun startService(context: Context, taskId: Long, taskName: String, date: String) {
      try {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
          action = ACTION_START
          putExtra(EXTRA_TASK_ID, taskId)
          putExtra(EXTRA_TASK_NAME, taskName)
          putExtra(EXTRA_DATE, date)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          context.startForegroundService(intent)
        } else {
          context.startService(intent)
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    fun stopService(context: Context) {
      try {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
          action = ACTION_STOP_SERVICE
        }
        context.startService(intent)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    try {
      createNotificationChannel()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    try {
      when (intent?.action) {
        ACTION_START -> {
          val taskName = intent.getStringExtra(EXTRA_TASK_NAME) ?: "Task"
          showOrUpdateNotification(taskName)
        }
        ACTION_USER_STOP -> {
          // Triggered when user clicks "Stop & Save" on the notification
          TimerManager.stopTimer()
          removeNotificationAndStopSelf()
        }
        ACTION_STOP_SERVICE -> {
          // Triggered by TimerManager from in-app UI
          removeNotificationAndStopSelf()
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return START_NOT_STICKY
  }

  private fun showOrUpdateNotification(taskName: String) {
    try {
      val openAppIntent = Intent(this, MainActivity::class.java).apply {
        this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }
      val openAppPendingIntent = PendingIntent.getActivity(
        this,
        0,
        openAppIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
      )

      val stopIntent = Intent(this, TimerForegroundService::class.java).apply {
        action = ACTION_USER_STOP
      }
      val stopPendingIntent = PendingIntent.getService(
        this,
        1,
        stopIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
      )

      val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("⏱️ $taskName")
        .setContentText("Focus session in progress")
        .setSubText("Active Tracker")
        .setSmallIcon(R.drawable.ic_timer_notification)
        .setColor(0xFF3B82F6.toInt())
        .setOngoing(true)
        .setUsesChronometer(true)
        .setShowWhen(true)
        .setWhen(System.currentTimeMillis())
        .setContentIntent(openAppPendingIntent)
        .setStyle(
          NotificationCompat.BigTextStyle()
            .setBigContentTitle("⏱️ Focusing: $taskName")
            .bigText("Active background tracking in progress. Tap to open or tap below to stop and save.")
        )
        .addAction(
          NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel,
            "⏹ Stop & Save",
            stopPendingIntent
          ).build()
        )
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setCategory(NotificationCompat.CATEGORY_WORKOUT)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .build()

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        startForeground(
          NOTIFICATION_ID,
          notification,
          ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
      } else {
        startForeground(NOTIFICATION_ID, notification)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun removeNotificationAndStopSelf() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        stopForeground(STOP_FOREGROUND_REMOVE)
      } else {
        @Suppress("DEPRECATION")
        stopForeground(true)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      stopSelf()
    }
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Active Habit Timer",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Displays ongoing timer when tracking habits or study sessions in background"
        setShowBadge(false)
      }
      val manager = getSystemService(NotificationManager::class.java)
      manager?.createNotificationChannel(channel)
    }
  }
}
