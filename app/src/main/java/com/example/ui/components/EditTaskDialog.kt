package com.example.ui.components

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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notes
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.HabitTask
import com.example.ui.theme.RatingBestGreen
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
  var noteText by remember { mutableStateOf(task.noteText) }
  var noteImageUri by remember { mutableStateOf(task.noteImageUri) }

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
            text = "Edit Task & Habit",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
        }

        if (onDelete != null) {
          IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete Task",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(20.dp)
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
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = taskName,
          onValueChange = { taskName = it },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Target Timer
        Text(
          text = "Target Timer (Minutes)",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = targetMinutesStr,
          onValueChange = { input ->
            if (input.all { it.isDigit() } && input.length <= 4) {
              targetMinutesStr = input
            }
          },
          placeholder = { Text("e.g. 45 (or 0 for no target)") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          leadingIcon = {
            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Date Selection and Frequency of Days
        Text(
          text = "Date Selection & Frequency",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(4.dp))

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Date Selection via Mini Calendar Popup
          FilterChip(
            selected = targetDates.isNotEmpty(),
            onClick = { showCalendarPopup = true },
            leadingIcon = {
              Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
            },
            label = {
              Text(
                if (targetDates.isEmpty()) "Select Date(s)..."
                else "${targetDates.size} date(s) selected",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (targetDates.isNotEmpty()) FontWeight.Bold else FontWeight.Normal
                )
              )
            }
          )

          FilterChip(
            selected = isEveryday,
            onClick = {
              repeatMask = if (isEveryday) 0 else HabitTask.EVERYDAY_MASK
              if (!isEveryday) targetDates = emptySet()
            },
            label = {
              Text(
                "Everyday",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isEveryday) FontWeight.Bold else FontWeight.Normal
                )
              )
            },
            leadingIcon = {
              Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(14.dp))
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
        if (targetDates.isEmpty()) {
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
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Option to add/keep as Preset
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = null,
              tint = if (isPreset) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Preset Habit",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
              )
              Text(
                text = "Keep this habit in quick presets list",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Switch(
            checked = isPreset,
            onCheckedChange = { isPreset = it },
            modifier = Modifier.testTag("switch_edit_default")
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Note in task: Text Note
        Text(
          text = "Note / Study Reminder (Text)",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = noteText,
          onValueChange = { noteText = it },
          placeholder = { Text("e.g. Chapter summary, formulas, tips...") },
          leadingIcon = {
            Icon(Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(18.dp))
          },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 4,
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Note in task: Image Note
        Text(
          text = "Image Note",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(6.dp))

        if (noteImageUri != null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp)
              .clip(RoundedCornerShape(12.dp))
              .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
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
            val updated = task.copy(
              name = taskName.trim(),
              repeatDaysMask = if (targetDates.isNotEmpty()) 0 else repeatMask,
              targetDate = if (targetDates.isNotEmpty()) singleTargetDate else task.targetDate,
              targetDates = targetDatesStr,
              targetTimeMinutes = target,
              isDefault = isPreset,
              noteText = noteText.trim(),
              noteImageUri = noteImageUri
            )
            onConfirm(updated)
          }
        },
        enabled = taskName.isNotBlank(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RatingBestGreen),
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
