package com.example.ui.components

import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.HabitTask
import com.example.util.DateUtils
import com.example.util.ImageStorageUtils
import kotlinx.coroutines.delay
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTaskDialog(
  selectedDate: String = "",
  onDismiss: () -> Unit,
  onConfirm: (
    name: String,
    targetDates: Set<String>,
    repeatMask: Int,
    targetMinutes: Int,
    reminderTime: String?,
    noteText: String,
    noteImageUri: String?
  ) -> Unit
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  var taskName by remember { mutableStateOf("") }
  var isSpecificDayOnly by remember { mutableStateOf(true) }
  var repeatMask by remember { mutableIntStateOf(0) }
  val effectiveDate = if (selectedDate.isNotBlank()) selectedDate else DateUtils.today()
  var selectedDates by remember { mutableStateOf(setOf<String>()) }
  var showCalendarPopup by remember { mutableStateOf(false) }

  var targetMinutesStr by remember { mutableStateOf("") }

  // Alarm / Reminder feature with notification & sound
  var reminderEnabled by remember { mutableStateOf(false) }
  var reminderTimeStr by remember { mutableStateOf("08:00") }

  var noteText by remember { mutableStateOf("") }
  var noteImageUri by remember { mutableStateOf<String?>(null) }
  var showFullScreenImage by remember { mutableStateOf(false) }

  val isEveryday = (repeatMask == HabitTask.EVERYDAY_MASK)
  val dayLabels = HabitTask.DAY_LETTERS

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      val savedPath = ImageStorageUtils.copyUriToInternalStorage(context, uri, "task_${System.currentTimeMillis()}")
      if (savedPath != null) {
        noteImageUri = savedPath
      }
    }
  }

  // Request focus and open keyboard immediately when Add Task dialog opens
  LaunchedEffect(Unit) {
    delay(100)
    focusRequester.requestFocus()
    keyboardController?.show()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("add_task_dialog"),
    shape = RoundedCornerShape(24.dp),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Add Habit / Task",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
        }

        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        // Task Name Input with autofocus
        Text(
          text = "Task Name",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = taskName,
          onValueChange = { taskName = it },
          placeholder = { Text("e.g. Physics Revision, Bio MCQ 50") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag("task_name_input"),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Target Timer input (in minutes)
        Text(
          text = "Target Timer (Minutes)",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = targetMinutesStr,
          onValueChange = { input ->
            if (input.all { it.isDigit() } && input.length <= 4) {
              targetMinutesStr = input
            }
          },
          label = { Text("Target Duration (e.g. 45 min)") },
          placeholder = { Text("0 = No timer target") },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Timer,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          },
          trailingIcon = {
            if (targetMinutesStr.isNotBlank()) {
              IconButton(onClick = { targetMinutesStr = "" }) {
                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
              }
            }
          },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("task_target_timer_input"),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Quick timer chips: 25m, 45m, 60m, 90m, 120m
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(25, 45, 60, 90, 120).forEach { mins ->
            val isSelected = targetMinutesStr == mins.toString()
            SuggestionChip(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                targetMinutesStr = if (isSelected) "" else mins.toString()
              },
              label = { Text("${mins}m", fontSize = 11.sp) },
              modifier = Modifier.height(28.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Alarm / Reminder with Sound Notification
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Alarm,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Reminder / Alarm",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
              )
              Text(
                text = if (reminderEnabled) "Alert with sound at $reminderTimeStr" else "Sound alert at scheduled time",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Switch(
            checked = reminderEnabled,
            onCheckedChange = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              reminderEnabled = it
            }
          )
        }

        if (reminderEnabled) {
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedButton(
              onClick = {
                val parts = reminderTimeStr.split(":")
                val curH = parts.getOrNull(0)?.toIntOrNull() ?: 8
                val curM = parts.getOrNull(1)?.toIntOrNull() ?: 0
                TimePickerDialog(context, { _, hourOfDay, minute ->
                  reminderTimeStr = String.format("%02d:%02d", hourOfDay, minute)
                }, curH, curM, true).show()
              },
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Time: $reminderTimeStr")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              listOf("06:00", "08:00", "14:00", "20:00").forEach { time ->
                SuggestionChip(
                  onClick = { reminderTimeStr = time },
                  label = { Text(time, fontSize = 10.sp) },
                  modifier = Modifier.height(26.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Multi-Date Selection option: Minimal with just calendar icon (no text)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = if (selectedDates.isEmpty()) "Specific or Multiple Dates" else "${selectedDates.size} date(s) selected",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
              color = if (selectedDates.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
          }

          IconButton(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              showCalendarPopup = true
            },
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(
                if (selectedDates.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
              )
              .testTag("btn_select_multiple_dates")
          ) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = "Select Dates Calendar",
              tint = if (selectedDates.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        if (selectedDates.isNotEmpty()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Active on: " + selectedDates.sorted().joinToString(", ") { DateUtils.formatShortDate(it) },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Day Selection / Recurrence
        if (selectedDates.isEmpty()) {
          Text(
            text = "Schedule & Recurrence",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilterChip(
              selected = isSpecificDayOnly && repeatMask == 0,
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isSpecificDayOnly = true
                repeatMask = 0
              },
              label = { Text("Today Only (${DateUtils.formatShortDate(effectiveDate)})") },
              modifier = Modifier.weight(1f)
            )

            FilterChip(
              selected = isEveryday,
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (isEveryday) {
                  repeatMask = 0
                  isSpecificDayOnly = true
                } else {
                  repeatMask = HabitTask.EVERYDAY_MASK
                  isSpecificDayOnly = false
                }
              },
              label = { Text("Every Day") },
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Individual day bubbles M T W T F S S
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            dayLabels.forEachIndexed { index, letter ->
              val bit = 1 shl index
              val isDaySelected = (repeatMask and bit) != 0
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(
                    if (isDaySelected && !isSpecificDayOnly) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                  )
                  .border(
                    width = 1.dp,
                    color = if (isDaySelected && !isSpecificDayOnly) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape
                  )
                  .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isSpecificDayOnly = false
                    repeatMask = if (isDaySelected) {
                      repeatMask and bit.inv()
                    } else {
                      repeatMask or bit
                    }
                  },
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = letter,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = if (isDaySelected && !isSpecificDayOnly) MaterialTheme.colorScheme.onPrimary
                  else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
        }

        // Note Text
        Text(
          text = "Notes & Details",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = noteText,
          onValueChange = { noteText = it },
          label = { Text("Task Notes (e.g. Chapter 14 page 100-120)") },
          leadingIcon = {
            Icon(Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(18.dp))
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          maxLines = 3
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Image Attachment
        if (noteImageUri != null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp)
              .clip(RoundedCornerShape(12.dp))
              .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
              .clickable { showFullScreenImage = true }
          ) {
            AsyncImage(
              model = noteImageUri,
              contentDescription = "Task image note",
              contentScale = ContentScale.Crop,
              modifier = Modifier.matchParentSize()
            )

            IconButton(
              onClick = { noteImageUri = null },
              modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(30.dp)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove Image",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        } else {
          OutlinedButton(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.Default.AddPhotoAlternate,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Attach Image Note")
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (taskName.isNotBlank()) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val mask = if (isSpecificDayOnly && selectedDates.isEmpty()) 0 else repeatMask
            val target = targetMinutesStr.toIntOrNull() ?: 0
            val reminder = if (reminderEnabled) reminderTimeStr else null
            onConfirm(
              taskName.trim(),
              selectedDates,
              mask,
              target,
              reminder,
              noteText.trim(),
              noteImageUri
            )
          }
        },
        enabled = taskName.isNotBlank(),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("confirm_add_task_button")
      ) {
        Text("Create Task", fontWeight = FontWeight.Bold)
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

  if (showFullScreenImage && noteImageUri != null) {
    FullScreenImageViewerDialog(
      imageUri = noteImageUri!!,
      onDismiss = { showFullScreenImage = false }
    )
  }

  // Mini Calendar Popup for multi-date selection
  if (showCalendarPopup) {
    MiniCalendarPickerPopup(
      initialDates = if (selectedDates.isNotEmpty()) selectedDates else setOf(effectiveDate),
      allowMultiple = true,
      title = "Select Dates for Task",
      onDismiss = { showCalendarPopup = false },
      onConfirm = { dates ->
        selectedDates = dates
        if (dates.isNotEmpty()) {
          isSpecificDayOnly = false
          repeatMask = 0
        }
        showCalendarPopup = false
      }
    )
  }
}
