package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object AlarmScheduler {
  fun scheduleTaskReminder(context: Context, taskId: Long, taskName: String, timeStr: String) {
    try {
      val parts = timeStr.split(":")
      if (parts.size != 2) return
      val hour = parts[0].toIntOrNull() ?: return
      val minute = parts[1].toIntOrNull() ?: return

      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

      val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) {
          add(Calendar.DAY_OF_YEAR, 1)
        }
      }

      val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
        putExtra("task_id", taskId)
        putExtra("task_name", taskName)
      }

      val pendingIntent = PendingIntent.getBroadcast(
        context,
        taskId.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
          alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
          )
        } else {
          alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
      } else {
        alarmManager.setExactAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          calendar.timeInMillis,
          pendingIntent
        )
      }
    } catch (_: Exception) {
      // Ignore security exceptions if permission not granted
    }
  }

  fun cancelTaskReminder(context: Context, taskId: Long) {
    try {
      val intent = Intent(context, TaskAlarmReceiver::class.java)
      val pendingIntent = PendingIntent.getBroadcast(
        context,
        taskId.toInt(),
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
      )
      if (pendingIntent != null) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmManager?.cancel(pendingIntent)
      }
    } catch (_: Exception) {}
  }

  fun scheduleAlarm(context: Context, task: com.example.data.model.HabitTask) {
    val time = task.reminderTime
    if (!time.isNullOrBlank()) {
      scheduleTaskReminder(context, task.id, task.name, time)
    }
  }

  fun cancelAlarm(context: Context, taskId: Long) {
    cancelTaskReminder(context, taskId)
  }
}
