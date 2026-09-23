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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.HabitTask
import com.example.util.DateUtils
import com.example.util.ImageStorageUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditTaskDialog(
  task: HabitTask,
  onDismiss: () -> Unit,
  onConfirm: (updatedTask: HabitTask) -> Unit,
  onDelete: (() -> Unit)? = null
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current

  var taskName by remember { mutableStateOf(task.name) }
  var repeatMask by remember { mutableIntStateOf(task.repeatDaysMask) }
  var targetDates by remember {
    val initial = mutableSetOf<String>()
    if (!task.targetDates.isNullOrBlank()) {
      initial.addAll(task.targetDates.split(",").filter { it.isNotBlank() })
    } else if (!task.targetDate.isNullOrBlank()) {
      initial.add(task.targetDate)
    }
    mutableStateOf(initial.toSet())
  }
  var showCalendarPopup by remember { mutableStateOf(false) }

  var targetMinutesStr by remember {
    mutableStateOf(if (task.targetTimeMinutes > 0) task.targetTimeMinutes.toString() else "")
  }
  var isPreset by remember { mutableStateOf(task.isDefault) }

  // Alarm / Reminder feature
  var reminderEnabled by remember { mutableStateOf(!task.reminderTime.isNullOrBlank()) }
  var reminderTimeStr by remember { mutableStateOf(task.reminderTime ?: "08:00") }

  var noteText by remember { mutableStateOf(task.noteText) }
  var noteImageUri by remember { mutableStateOf(task.noteImageUri) }
  var showFullScreenImage by remember { mutableStateOf(false) }

  val isEveryday = (repeatMask == HabitTask.EVERYDAY_MASK)
  val dayLabels = HabitTask.DAY_LETTERS

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      val savedPath = ImageStorageUtils.copyUriToInternalStorage(context, uri, "task_${task.id}")
      if (savedPath != null) {
        noteImageUri = savedPath
      }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("edit_task_dialog"),
    shape = RoundedCornerShape(24.dp),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Edit Task",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
        }

        if (onDelete != null) {
          IconButton(
            onClick = onDelete,
            modifier = Modifier.testTag("delete_task_button")
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete Task",
              tint = MaterialTheme.colorScheme.error
            )
          }
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        // Task Name
        Text(
          text = "Task Name",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = taskName,
          onValueChange = { taskName = it },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_task_name_input"),
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
          placeholder = { Text("0 = No target timer") },
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
            .testTag("edit_task_target_timer_input"),
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
              text = if (targetDates.isEmpty()) "Specific or Multiple Dates" else "${targetDates.size} date(s) selected",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
              color = if (targetDates.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                if (targetDates.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
              )
              .testTag("btn_edit_select_multiple_dates")
          ) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = "Select Dates Calendar",
              tint = if (targetDates.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        if (targetDates.isNotEmpty()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Scheduled for: " + targetDates.sorted().joinToString(", ") { DateUtils.formatShortDate(it) },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Schedule / Recurrence
        if (targetDates.isEmpty()) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Repeat,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Repeat Days",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilterChip(
              selected = isEveryday,
              onClick = {
                repeatMask = if (isEveryday) 0 else HabitTask.EVERYDAY_MASK
                targetDates = emptySet()
              },
              label = {
                Text(
                  "Every Day",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isEveryday) FontWeight.Bold else FontWeight.Normal
                  )
                )
              }
            )

            FilterChip(
              selected = !isEveryday && repeatMask != 0,
              onClick = {
                if (repeatMask == 0) repeatMask = 0b0111110 // Mon-Fri
                targetDates = emptySet()
              },
              label = {
                Text(
                  "Week Days",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (!isEveryday && repeatMask != 0) FontWeight.Bold else FontWeight.Normal
                  )
                )
              }
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          // 7 Day circles
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            dayLabels.forEachIndexed { index, letter ->
              val bit = 1 shl index
              val isSelected = (repeatMask and bit) != 0

              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                  )
                  .border(
                    width = 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = CircleShape
                  )
                  .clickable {
                    repeatMask = if (isSelected) {
                      repeatMask and bit.inv()
                    } else {
                      repeatMask or bit
                    }
                  },
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = letter,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  ),
                  color = if (isSelected) MaterialTheme.colorScheme.onPrimary
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
          label = { Text("Task Notes") },
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
            val target = targetMinutesStr.toIntOrNull() ?: 0
            val targetDatesStr = if (targetDates.isNotEmpty()) targetDates.joinToString(",") else null
            val singleTargetDate = if (targetDates.size == 1) targetDates.first() else task.targetDate
            val reminder = if (reminderEnabled) reminderTimeStr else null
            val updated = task.copy(
              name = taskName.trim(),
              repeatDaysMask = if (targetDates.isNotEmpty()) 0 else repeatMask,
              targetDate = if (targetDates.isNotEmpty()) singleTargetDate else task.targetDate,
              targetDates = targetDatesStr,
              targetTimeMinutes = target,
              isDefault = isPreset,
              noteText = noteText.trim(),
              noteImageUri = noteImageUri,
              reminderTime = reminder
            )
            onConfirm(updated)
          }
        },
        enabled = taskName.isNotBlank(),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("save_edit_task_button")
      ) {
        Text("Save Changes", fontWeight = FontWeight.Bold)
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

  // Mini Calendar Picker Popup for date selection
  if (showCalendarPopup) {
    MiniCalendarPickerPopup(
      initialDates = targetDates,
      allowMultiple = true,
      title = "Select Date(s) for Task",
      onDismiss = { showCalendarPopup = false },
      onConfirm = { dates ->
        targetDates = dates
        if (dates.isNotEmpty()) {
          repeatMask = 0
        }
        showCalendarPopup = false
      }
    )
  }
}
