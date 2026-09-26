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

class TimerForegroundService : Service() {

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val action = intent?.action
    if (action == ACTION_STOP_SERVICE) {
      stopForeground(STOP_FOREGROUND_REMOVE)
      stopSelf()
      return START_NOT_STICKY
    }

    val taskName = intent?.getStringExtra(EXTRA_TASK_NAME) ?: "Active Habit Timer"
    val elapsedSeconds = intent?.getLongExtra(EXTRA_ELAPSED_SECONDS, 0L) ?: 0L

    createNotificationChannel()
    val notification = buildNotification(taskName, elapsedSeconds)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      startForeground(
        NOTIFICATION_ID,
        notification,
        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
      )
    } else {
      startForeground(NOTIFICATION_ID, notification)
    }

    return START_STICKY
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Active Study Timer",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Displays running study & habit timer in notification tray"
        setShowBadge(false)
      }
      val manager = getSystemService(NotificationManager::class.java)
      manager?.createNotificationChannel(channel)
    }
  }

  private fun buildNotification(taskName: String, elapsedSeconds: Long): Notification {
    val formatted = DateUtils.formatTime(elapsedSeconds)

    val openIntent = Intent(this, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    val openPendingIntent = PendingIntent.getActivity(
      this,
      0,
      openIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_media_play)
      .setContentTitle("⏱ $taskName: $formatted")
      .setContentText("Focus session in progress")
      .setContentIntent(openPendingIntent)
      .setOngoing(true)
      .setOnlyAlertOnce(true)
      .build()
  }

  companion object {
    const val CHANNEL_ID = "habit_active_timer_channel"
    const val NOTIFICATION_ID = 2001
    const val ACTION_STOP_SERVICE = "com.example.ACTION_STOP_TIMER_SERVICE"
    const val EXTRA_TASK_NAME = "extra_task_name"
    const val EXTRA_ELAPSED_SECONDS = "extra_elapsed_seconds"

    fun start(context: Context, taskName: String, elapsedSeconds: Long) {
      val intent = Intent(context, TimerForegroundService::class.java).apply {
        putExtra(EXTRA_TASK_NAME, taskName)
        putExtra(EXTRA_ELAPSED_SECONDS, elapsedSeconds)
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
      } else {
        context.startService(intent)
      }
    }

    fun stop(context: Context) {
      val intent = Intent(context, TimerForegroundService::class.java).apply {
        action = ACTION_STOP_SERVICE
      }
      context.startService(intent)
    }
  }
}
