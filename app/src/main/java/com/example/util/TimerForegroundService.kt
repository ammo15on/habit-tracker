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
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class TimerForegroundService : Service() {

  private var wakeLock: PowerManager.WakeLock? = null

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    try {
      val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
      wakeLock = powerManager?.newWakeLock(
        PowerManager.PARTIAL_WAKE_LOCK,
        "HabitTracker:TimerWakeLock"
      )?.apply {
        setReferenceCounted(false)
        acquire(3 * 60 * 60 * 1000L) // Safe max timeout of 3 hours
      }
    } catch (_: Exception) {}
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val action = intent?.action

    when (action) {
      ACTION_STOP_SERVICE -> {
        releaseWakeLock()
        try {
          stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {}
        stopSelf()
        return START_NOT_STICKY
      }
      ACTION_PAUSE -> {
        TimerManager.pauseTimer()
        return START_STICKY
      }
      ACTION_RESUME -> {
        TimerManager.resumeTimer()
        return START_STICKY
      }
      ACTION_STOP -> {
        TimerManager.stopTimer()
        releaseWakeLock()
        try {
          stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {}
        stopSelf()
        return START_NOT_STICKY
      }
    }

    val taskName = intent?.getStringExtra(EXTRA_TASK_NAME) ?: "Study Task"
    val baseStartEpoch = intent?.getLongExtra(EXTRA_BASE_START_EPOCH, System.currentTimeMillis()) ?: System.currentTimeMillis()
    val elapsedSeconds = intent?.getLongExtra(EXTRA_ELAPSED_SECONDS, 0L) ?: 0L
    val isPaused = intent?.getBooleanExtra(EXTRA_IS_PAUSED, false) ?: false

    createNotificationChannel(this)
    val notification = buildNotification(this, taskName, baseStartEpoch, elapsedSeconds, isPaused)

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        try {
          startForeground(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
          )
        } catch (_: Exception) {
          try {
            startForeground(
              NOTIFICATION_ID,
              notification,
              ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
          } catch (_: Exception) {
            startForeground(NOTIFICATION_ID, notification)
          }
        }
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        try {
          startForeground(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
          )
        } catch (_: Exception) {
          startForeground(NOTIFICATION_ID, notification)
        }
      } else {
        startForeground(NOTIFICATION_ID, notification)
      }
    } catch (_: Exception) {
      try {
        startForeground(NOTIFICATION_ID, notification)
      } catch (_: Exception) {}
    }

    return START_STICKY
  }

  override fun onDestroy() {
    releaseWakeLock()
    super.onDestroy()
  }

  private fun releaseWakeLock() {
    try {
      if (wakeLock?.isHeld == true) {
        wakeLock?.release()
      }
    } catch (_: Exception) {}
  }

  companion object {
    const val CHANNEL_ID = "habit_active_timer_channel"
    const val NOTIFICATION_ID = 2001
    const val ACTION_STOP_SERVICE = "com.example.ACTION_STOP_TIMER_SERVICE"
    const val ACTION_PAUSE = "com.example.ACTION_PAUSE_TIMER"
    const val ACTION_RESUME = "com.example.ACTION_RESUME_TIMER"
    const val ACTION_STOP = "com.example.ACTION_STOP_TIMER"

    const val EXTRA_TASK_NAME = "extra_task_name"
    const val EXTRA_BASE_START_EPOCH = "extra_base_start_epoch"
    const val EXTRA_ELAPSED_SECONDS = "extra_elapsed_seconds"
    const val EXTRA_IS_PAUSED = "extra_is_paused"

    fun createNotificationChannel(context: Context) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
          CHANNEL_ID,
          "Live Tracking Timer",
          NotificationManager.IMPORTANCE_LOW
        ).apply {
          description = "Displays live active task tracking with pause and stop controls"
          setShowBadge(false)
          enableVibration(false)
          setSound(null, null)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
      }
    }

    fun buildNotification(
      context: Context,
      taskName: String,
      baseStartEpoch: Long,
      elapsedSeconds: Long,
      isPaused: Boolean
    ): Notification {
      val formatted = DateUtils.formatTime(elapsedSeconds)

      val openIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }
      val openPendingIntent = PendingIntent.getActivity(
        context,
        0,
        openIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      // Pause or Resume Intent
      val toggleAction = if (isPaused) {
        val resumeIntent = Intent(context, TimerForegroundService::class.java).apply {
          action = ACTION_RESUME
        }
        val resumePendingIntent = PendingIntent.getService(
          context,
          1,
          resumeIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        NotificationCompat.Action.Builder(
          android.R.drawable.ic_media_play,
          "Resume",
          resumePendingIntent
        ).build()
      } else {
        val pauseIntent = Intent(context, TimerForegroundService::class.java).apply {
          action = ACTION_PAUSE
        }
        val pausePendingIntent = PendingIntent.getService(
          context,
          2,
          pauseIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        NotificationCompat.Action.Builder(
          android.R.drawable.ic_media_pause,
          "Pause",
          pausePendingIntent
        ).build()
      }

      // Stop Intent
      val stopIntent = Intent(context, TimerForegroundService::class.java).apply {
        action = ACTION_STOP
      }
      val stopPendingIntent = PendingIntent.getService(
        context,
        3,
        stopIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      val stopAction = NotificationCompat.Action.Builder(
        android.R.drawable.ic_menu_close_clear_cancel,
        "Stop",
        stopPendingIntent
      ).build()

      val title = if (isPaused) "⏸ $taskName (Paused)" else "⏱ $taskName"
      val statusText = if (isPaused) {
        "Paused at $formatted • Tap Resume"
      } else {
        "Live tracking session active • Tap to open"
      }

      val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(if (isPaused) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
        .setContentTitle(title)
        .setContentText(statusText)
        .setSubText("Live Tracker")
        .setContentIntent(openPendingIntent)
        .setOngoing(!isPaused)
        .setOnlyAlertOnce(true)
        .setColor(0xFF3B82F6.toInt())
        .setColorized(false) // Clean, translucent look that adapts to user theme
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .addAction(toggleAction)
        .addAction(stopAction)

      // Use system chronometer when running so the Android OS natively ticks the live second count
      // in the notification shade with zero CPU load and zero background battery drain!
      if (!isPaused && baseStartEpoch > 0L) {
        builder.setUsesChronometer(true)
        builder.setWhen(baseStartEpoch)
        builder.setShowWhen(true)
      } else {
        builder.setUsesChronometer(false)
        builder.setShowWhen(false)
      }

      return builder.build()
    }

    fun start(
      context: Context,
      taskName: String,
      baseStartEpoch: Long,
      elapsedSeconds: Long,
      isPaused: Boolean = false
    ) {
      try {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
          putExtra(EXTRA_TASK_NAME, taskName)
          putExtra(EXTRA_BASE_START_EPOCH, baseStartEpoch)
          putExtra(EXTRA_ELAPSED_SECONDS, elapsedSeconds)
          putExtra(EXTRA_IS_PAUSED, isPaused)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          try {
            context.startForegroundService(intent)
          } catch (_: Exception) {
            context.startService(intent)
          }
        } else {
          context.startService(intent)
        }
      } catch (_: Exception) {}
    }

    fun updateNotificationDirect(
      context: Context,
      taskName: String,
      baseStartEpoch: Long,
      elapsedSeconds: Long,
      isPaused: Boolean
    ) {
      try {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        createNotificationChannel(context)
        val notif = buildNotification(context, taskName, baseStartEpoch, elapsedSeconds, isPaused)
        manager?.notify(NOTIFICATION_ID, notif)
      } catch (_: Exception) {}
    }

    fun stop(context: Context) {
      try {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
          action = ACTION_STOP_SERVICE
        }
        context.startService(intent)
      } catch (_: Exception) {}
      try {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.cancel(NOTIFICATION_ID)
      } catch (_: Exception) {}
    }
  }
}
