package com.example.habit.util

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
import com.example.habit.MainActivity

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
                acquire(3 * 60 * 60 * 1000L)
            }
        } catch (_: Exception) {}
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_STOP_SERVICE -> {
                releaseWakeLock()
                stopForeground(STOP_FOREGROUND_REMOVE)
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
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val taskName = intent?.getStringExtra(EXTRA_TASK_NAME) ?: "Active Habit Timer"
        val elapsedSeconds = intent?.getLongExtra(EXTRA_ELAPSED_SECONDS, 0L) ?: 0L
        val isPaused = intent?.getBooleanExtra(EXTRA_IS_PAUSED, false) ?: false

        createNotificationChannel()
        val notification = buildNotification(taskName, elapsedSeconds, isPaused)

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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Background Habit Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live background timer and focus sessions with quick controls"
                setShowBadge(false)
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(taskName: String, elapsedSeconds: Long, isPaused: Boolean): Notification {
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

        val toggleAction = if (isPaused) {
            val resumeIntent = Intent(this, TimerForegroundService::class.java).apply {
                action = ACTION_RESUME
            }
            val resumePendingIntent = PendingIntent.getService(
                this,
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
            val pauseIntent = Intent(this, TimerForegroundService::class.java).apply {
                action = ACTION_PAUSE
            }
            val pausePendingIntent = PendingIntent.getService(
                this,
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

        val stopIntent = Intent(this, TimerForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            3,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_save,
            "Stop & Save",
            stopPendingIntent
        ).build()

        val statusText = if (isPaused) "Paused • Background Tracking Ready" else "Tracking in background"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("⏱ $taskName: $formatted")
            .setContentText(statusText)
            .setSubText("Ongoing Study Task")
            .setContentIntent(openPendingIntent)
            .setOngoing(!isPaused)
            .setOnlyAlertOnce(true)
            .addAction(toggleAction)
            .addAction(stopAction)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "habit_active_timer_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_STOP_SERVICE = "com.example.habit.ACTION_STOP_TIMER_SERVICE"
        const val ACTION_PAUSE = "com.example.habit.ACTION_PAUSE_TIMER"
        const val ACTION_RESUME = "com.example.habit.ACTION_RESUME_TIMER"
        const val ACTION_STOP = "com.example.habit.ACTION_STOP_TIMER"

        const val EXTRA_TASK_NAME = "extra_task_name"
        const val EXTRA_ELAPSED_SECONDS = "extra_elapsed_seconds"
        const val EXTRA_IS_PAUSED = "extra_is_paused"

        fun start(context: Context, taskName: String, elapsedSeconds: Long, isPaused: Boolean = false) {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                putExtra(EXTRA_TASK_NAME, taskName)
                putExtra(EXTRA_ELAPSED_SECONDS, elapsedSeconds)
                putExtra(EXTRA_IS_PAUSED, isPaused)
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
