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
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class TimerForegroundService : Service() {

  companion object {
    const val CHANNEL_ID = "habit_active_timer_channel"
    const val NOTIFICATION_ID = 2001

    const val ACTION_START = "com.example.action.START_TIMER"
    const val ACTION_STOP = "com.example.action.STOP_TIMER"

    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_NAME = "extra_task_name"
    const val EXTRA_DATE = "extra_date"

    fun startService(context: Context, taskId: Long, taskName: String, date: String) {
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
    }

    fun stopService(context: Context) {
      val intent = Intent(context, TimerForegroundService::class.java).apply {
        action = ACTION_STOP
      }
      context.startService(intent)
    }
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_START -> {
        val taskName = intent.getStringExtra(EXTRA_TASK_NAME) ?: "Task"
        startForegroundTimer(taskName)
      }
      ACTION_STOP -> {
        stopForegroundTimer()
      }
    }
    return START_STICKY
  }

  private fun startForegroundTimer(taskName: String) {
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
      action = ACTION_STOP
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
          .bigText("Active background tracking is in progress. Tap anywhere to open the tracker or tap below to stop and save.")
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
  }

  private fun stopForegroundTimer() {
    TimerManager.stopTimer()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      stopForeground(STOP_FOREGROUND_REMOVE)
    } else {
      @Suppress("DEPRECATION")
      stopForeground(true)
    }
    stopSelf()
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
