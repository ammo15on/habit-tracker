package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun MiniCalendarPickerPopup(
  initialDates: Set<String> = emptySet(),
  allowMultiple: Boolean = true,
  title: String = "Select Date(s)",
  onDismiss: () -> Unit,
  onConfirm: (Set<String>) -> Unit
) {
  val haptic = LocalHapticFeedback.current
  var selectedDates by remember { mutableStateOf(initialDates.toMutableSet()) }

  val calendar = remember {
    val cal = Calendar.getInstance()
    val firstDate = initialDates.firstOrNull()
    if (firstDate != null) {
      try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        cal.time = sdf.parse(firstDate) ?: Calendar.getInstance().time
      } catch (_: Exception) {}
    }
    cal
  }

  var currentYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
  var currentMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }

  val todayStr = DateUtils.today()

  val monthName = remember(currentYear, currentMonth) {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, currentYear)
    cal.set(Calendar.MONTH, currentMonth)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
  }

  // Days matrix
  val daysInMonth = remember(currentYear, currentMonth) {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, currentYear)
    cal.set(Calendar.MONTH, currentMonth)
    cal.set(Calendar.DAY_OF_MONTH, 1)

    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
    // Offset for Mon=0 .. Sun=6
    val offset = (firstDayOfWeek + 5) % 7
    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val list = mutableListOf<String?>()
    for (i in 0 until offset) {
      list.add(null)
    }
    for (day in 1..maxDays) {
      val dateStr = String.format(Locale.US, "%04d-%02d-%02d", currentYear, currentMonth + 1, day)
      list.add(dateStr)
    }
    list
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("mini_calendar_popup"),
    shape = RoundedCornerShape(24.dp),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
        }
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Month and Navigation row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              if (currentMonth == 0) {
                currentMonth = 11
                currentYear -= 1
              } else {
                currentMonth -= 1
              }
            },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month", modifier = Modifier.size(18.dp))
          }

          Text(
            text = monthName,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )

          IconButton(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              if (currentMonth == 11) {
                currentMonth = 0
                currentYear += 1
              } else {
                currentMonth += 1
              }
            },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month", modifier = Modifier.size(18.dp))
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Days of week header (M, T, W, T, F, S, S)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
          listOf("M", "T", "W", "T", "F", "S", "S").forEach { dayLetter ->
            Text(
              text = dayLetter,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier.weight(1f)
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Grid of dates
        LazyVerticalGrid(
          columns = GridCells.Fixed(7),
          modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          items(daysInMonth.size) { index ->
            val dateStr = daysInMonth[index]
            if (dateStr == null) {
              Box(modifier = Modifier.aspectRatio(1f))
            } else {
              val isSelected = selectedDates.contains(dateStr)
              val isToday = (dateStr == todayStr)
              val dayNumber = dateStr.takeLast(2).toIntOrNull() ?: 1

              Box(
                modifier = Modifier
                  .aspectRatio(1f)
                  .clip(CircleShape)
                  .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else Color.Transparent
                  )
                  .border(
                    width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                    color = if (isToday && !isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape
                  )
                  .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val newSet = selectedDates.toMutableSet()
                    if (allowMultiple) {
                      if (newSet.contains(dateStr)) {
                        newSet.remove(dateStr)
                      } else {
                        newSet.add(dateStr)
                      }
                    } else {
                      newSet.clear()
                      newSet.add(dateStr)
                    }
                    selectedDates = newSet
                  },
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = dayNumber.toString(),
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                  ),
                  color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                  }
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick action row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(
              selected = selectedDates.contains(todayStr),
              onClick = {
                val newSet = selectedDates.toMutableSet()
                if (allowMultiple) {
                  if (newSet.contains(todayStr)) newSet.remove(todayStr) else newSet.add(todayStr)
                } else {
                  newSet.clear()
                  newSet.add(todayStr)
                }
                selectedDates = newSet
              },
              label = { Text("Today", fontSize = 11.sp) }
            )
            if (selectedDates.isNotEmpty()) {
              OutlinedButton(
                onClick = { selectedDates = mutableSetOf() },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(28.dp)
              ) {
                Text("Clear", fontSize = 11.sp)
              }
            }
          }

          Text(
            text = "${selectedDates.size} selected",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirm(selectedDates)
        },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Text("Done (${selectedDates.size})")
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("Cancel")
      }
    }
  )
}
