package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class TaskAlarmReceiver : BroadcastReceiver() {
  companion object {
    const val CHANNEL_ID = "habit_task_alarm_channel"
    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_NAME = "extra_task_name"
    const val EXTRA_TIME = "extra_time"
  }

  override fun onReceive(context: Context, intent: Intent) {
    val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L)
    val taskName = intent.getStringExtra(EXTRA_TASK_NAME) ?: "Habit Task"
    val timeStr = intent.getStringExtra(EXTRA_TIME) ?: ""

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
      ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val audioAttributes = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_ALARM)
        .build()

      val channel = NotificationChannel(
        CHANNEL_ID,
        "Task Alarms & Reminders",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Sound and notifications for task alarms and reminders"
        enableLights(true)
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 600, 300, 600)
        setSound(soundUri, audioAttributes)
      }
      notificationManager.createNotificationChannel(channel)
    }

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      (taskId % Int.MAX_VALUE).toInt(),
      openAppIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_timer_notification)
      .setContentTitle("⏰ Alarm: $taskName")
      .setContentText("Time for your task ($timeStr)")
      .setStyle(
        NotificationCompat.BigTextStyle()
          .setBigContentTitle("⏰ Alarm: $taskName")
          .bigText("Scheduled alarm for $timeStr. Tap to open Habit Tracker.")
      )
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .setSound(soundUri)
      .setVibrate(longArrayOf(0, 600, 300, 600))
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .build()

    notificationManager.notify((20000 + (taskId % 10000)).toInt(), notification)
  }
}
