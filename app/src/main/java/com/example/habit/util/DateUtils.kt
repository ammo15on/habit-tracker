package com.example.habit.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateUtils {
    fun today(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Calendar.getInstance().time)
    }

    fun formatShortDate(dateStr: String): String {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(dateStr) ?: return dateStr
            val out = SimpleDateFormat("MMM d", Locale.US)
            return out.format(date)
        } catch (_: Exception) {
            return dateStr
        }
    }

    fun formatFullDate(dateStr: String): String {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(dateStr) ?: return dateStr
            val out = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US)
            return out.format(date)
        } catch (_: Exception) {
            return dateStr
        }
    }

    fun formatTime(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun addDays(dateStr: String, days: Int): String {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(dateStr) ?: return dateStr
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, days)
            return sdf.format(cal.time)
        } catch (_: Exception) {
            return dateStr
        }
    }

    fun daysBetween(startDate: String, endDate: String): Long {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val d1 = sdf.parse(startDate)?.time ?: 0L
            val d2 = sdf.parse(endDate)?.time ?: 0L
            val diff = d2 - d1
            return diff / (1000 * 60 * 60 * 24)
        } catch (_: Exception) {
            return 0L
        }
    }
}
