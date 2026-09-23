package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.HabitTask
import java.util.Calendar

object AlarmScheduler {
  fun scheduleAlarm(context: Context, task: HabitTask) {
    val reminder = task.reminderTime
    if (reminder.isNullOrBlank()) {
      cancelAlarm(context, task.id)
      return
    }

    try {
      val parts = reminder.split(":")
      if (parts.size != 2) return
      val hour = parts[0].toIntOrNull() ?: return
      val minute = parts[1].toIntOrNull() ?: return

      val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) {
          add(Calendar.DAY_OF_YEAR, 1)
        }
      }

      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
        putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, task.id)
        putExtra(TaskAlarmReceiver.EXTRA_TASK_NAME, task.name)
        putExtra(TaskAlarmReceiver.EXTRA_TIME, reminder)
      }

      val pendingIntent = PendingIntent.getBroadcast(
        context,
        (task.id % Int.MAX_VALUE).toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
      )

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
          alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        } else {
          alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        }
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
      } else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
      }
    } catch (e: Exception) {
      Log.e("AlarmScheduler", "Failed to schedule alarm", e)
    }
  }

  fun cancelAlarm(context: Context, taskId: Long) {
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      val intent = Intent(context, TaskAlarmReceiver::class.java)
      val pendingIntent = PendingIntent.getBroadcast(
        context,
        (taskId % Int.MAX_VALUE).toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
      )
      alarmManager.cancel(pendingIntent)
    } catch (e: Exception) {
      Log.e("AlarmScheduler", "Failed to cancel alarm", e)
    }
  }
}
