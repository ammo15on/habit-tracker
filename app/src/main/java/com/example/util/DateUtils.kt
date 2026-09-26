package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
  private const val DATE_FORMAT = "yyyy-MM-dd"

  fun today(): String {
    val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    return sdf.format(Date())
  }

  fun tomorrow(): String {
    return getNextDay(today())
  }

  fun getPreviousDay(dateStr: String): String {
    return offsetDay(dateStr, -1)
  }

  fun getNextDay(dateStr: String): String {
    return offsetDay(dateStr, 1)
  }

  fun addDays(dateStr: String, amount: Int): String {
    return offsetDay(dateStr, amount)
  }

  private fun offsetDay(dateStr: String, amount: Int): String {
    val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    val cal = Calendar.getInstance()
    try {
      val parsed = sdf.parse(dateStr)
      if (parsed != null) cal.time = parsed
    } catch (_: Exception) {}
    cal.add(Calendar.DAY_OF_YEAR, amount)
    return sdf.format(cal.time)
  }

  fun formatShortDate(dateStr: String): String {
    val inSdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    val outSdf = SimpleDateFormat("MMM d", Locale.getDefault())
    return try {
      val date = inSdf.parse(dateStr)
      if (date != null) outSdf.format(date) else dateStr
    } catch (_: Exception) {
      dateStr
    }
  }

  fun formatDayOfWeekAbbr(dateStr: String): String {
    val inSdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    val outSdf = SimpleDateFormat("EEE", Locale.getDefault())
    return try {
      val date = inSdf.parse(dateStr)
      if (date != null) outSdf.format(date) else ""
    } catch (_: Exception) {
      ""
    }
  }

  fun formatFullDate(dateStr: String): String {
    val inSdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    val outSdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    return try {
      val date = inSdf.parse(dateStr)
      if (date != null) outSdf.format(date) else dateStr
    } catch (_: Exception) {
      dateStr
    }
  }

  fun getDayOfWeekIndex(dateStr: String): Int {
    // 0 = Monday, 1 = Tuesday, ..., 6 = Sunday
    val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    val cal = Calendar.getInstance()
    try {
      val date = sdf.parse(dateStr)
      if (date != null) cal.time = date
    } catch (_: Exception) {}
    return when (cal.get(Calendar.DAY_OF_WEEK)) {
      Calendar.MONDAY -> 0
      Calendar.TUESDAY -> 1
      Calendar.WEDNESDAY -> 2
      Calendar.THURSDAY -> 3
      Calendar.FRIDAY -> 4
      Calendar.SATURDAY -> 5
      Calendar.SUNDAY -> 6
      else -> 0
    }
  }

  fun getDaysInCurrentMonth(yearMonthStr: String): List<String> {
    // yearMonthStr: "yyyy-MM"
    val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val daySdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    val cal = Calendar.getInstance()
    try {
      val date = sdf.parse(yearMonthStr)
      if (date != null) cal.time = date
    } catch (_: Exception) {}
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val list = mutableListOf<String>()
    for (day in 1..maxDay) {
      cal.set(Calendar.DAY_OF_MONTH, day)
      list.add(daySdf.format(cal.time))
    }
    return list
  }

  fun getDaysInWeekForDate(dateStr: String): List<String> {
    val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    val cal = Calendar.getInstance()
    cal.firstDayOfWeek = Calendar.MONDAY
    try {
      val date = sdf.parse(dateStr)
      if (date != null) cal.time = date
    } catch (_: Exception) {}
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val list = mutableListOf<String>()
    for (i in 0..6) {
      list.add(sdf.format(cal.time))
      cal.add(Calendar.DAY_OF_YEAR, 1)
    }
    return list
  }

  fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
      String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
    } else {
      String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
    }
  }

  fun formatTimeWords(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return when {
      hours > 0 -> "${hours}h ${minutes}m"
      minutes > 0 -> "${minutes}m ${secs}s"
      else -> "${secs}s"
    }
  }

  fun formatTimestamp(millis: Long): String {
    val now = Calendar.getInstance()
    val msgCal = Calendar.getInstance().apply { timeInMillis = millis }
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val timeStr = timeFormat.format(Date(millis))
    return if (now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
      now.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR)
    ) {
      "Today $timeStr"
    } else if (now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
      now.get(Calendar.DAY_OF_YEAR) - msgCal.get(Calendar.DAY_OF_YEAR) == 1
    ) {
      "Yesterday $timeStr"
    } else {
      val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
      dateFormat.format(Date(millis))
    }
  }

  fun daysBetween(fromStr: String, toStr: String): Long {
    val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    return try {
      val fromDate = sdf.parse(fromStr) ?: return 0L
      val toDate = sdf.parse(toStr) ?: return 0L
      val diffMillis = toDate.time - fromDate.time
      diffMillis / (1000 * 60 * 60 * 24)
    } catch (_: Exception) {
      0L
    }
  }
}
