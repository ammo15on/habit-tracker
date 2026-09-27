package com.example.util

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import java.util.TimeZone

object CalendarSyncUtil {

  fun getDefaultCalendarId(context: Context): Long {
    val projection = arrayOf(
      CalendarContract.Calendars._ID,
      CalendarContract.Calendars.IS_PRIMARY
    )
    try {
      val cursor = context.contentResolver.query(
        CalendarContract.Calendars.CONTENT_URI,
        projection,
        null,
        null,
        null
      )
      cursor?.use {
        if (it.moveToFirst()) {
          val idCol = it.getColumnIndex(CalendarContract.Calendars._ID)
          val primaryCol = it.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)
          
          while (!it.isAfterLast) {
            val id = it.getLong(idCol)
            val isPrimary = if (primaryCol >= 0) it.getInt(primaryCol) == 1 else false
            if (isPrimary) {
              return id
            }
            it.moveToNext()
          }
          // Fallback to first calendar
          it.moveToFirst()
          return it.getLong(idCol)
        }
      }
    } catch (_: Exception) {}
    return 1L
  }

  fun syncEventToDeviceCalendar(
    context: Context,
    title: String,
    description: String,
    startDateStr: String, // "YYYY-MM-DD"
    endDateStr: String,   // "YYYY-MM-DD"
    targetTimeMinutes: Int = 0
  ): Boolean {
    try {
      val calendarId = getDefaultCalendarId(context)
      
      val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
      val startParsed = sdf.parse(startDateStr) ?: return false
      val endParsed = sdf.parse(endDateStr) ?: startParsed
      
      val calendar = java.util.Calendar.getInstance()
      calendar.time = startParsed
      calendar.set(java.util.Calendar.HOUR_OF_DAY, 9)
      calendar.set(java.util.Calendar.MINUTE, 0)
      val eventStartMillis = calendar.timeInMillis
      
      val durationMinutes = if (targetTimeMinutes > 0) targetTimeMinutes else 60
      
      // Calculate end time on the end date
      val endCalendar = java.util.Calendar.getInstance()
      endCalendar.time = endParsed
      endCalendar.set(java.util.Calendar.HOUR_OF_DAY, 9)
      endCalendar.set(java.util.Calendar.MINUTE, 0)
      val eventEndMillis = endCalendar.timeInMillis + (durationMinutes * 60 * 1000L)

      val values = ContentValues().apply {
        put(CalendarContract.Events.DTSTART, eventStartMillis)
        put(CalendarContract.Events.DTEND, eventEndMillis)
        put(CalendarContract.Events.TITLE, title)
        put(CalendarContract.Events.DESCRIPTION, description)
        put(CalendarContract.Events.CALENDAR_ID, calendarId)
        put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
      }
      
      val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
      return uri != null
    } catch (e: Exception) {
      e.printStackTrace()
      return false
    }
  }
}
