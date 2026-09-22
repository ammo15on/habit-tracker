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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notes
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
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.TaskPreset
import com.example.util.DateUtils
import com.example.util.ImageStorageUtils
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTaskDialog(
  selectedDate: String = "",
  presets: List<TaskPreset> = emptyList(),
  onDismiss: () -> Unit,
  onOpenPresetsManager: () -> Unit = {},
  onAddPreset: ((String) -> Unit)? = null,
  onDeletePreset: ((Long) -> Unit)? = null,
  onConfirm: (
    name: String,
    targetDates: Set<String>,
    repeatMask: Int,
    targetMinutes: Int,
    isDefault: Boolean,
    noteText: String,
    noteImageUri: String?
  ) -> Unit
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(Unit) {
    delay(150)
    try {
      focusRequester.requestFocus()
      keyboardController?.show()
    } catch (_: Exception) {}
  }

  val effectiveDate = if (selectedDate.isNotBlank()) selectedDate else DateUtils.today()
  val dayOfWeekIndex = DateUtils.getDayOfWeekIndex(effectiveDate)

  var taskName by remember { mutableStateOf("") }
  var repeatMask by remember { mutableIntStateOf(0) }
  var selectedDates by remember { mutableStateOf<Set<String>>(emptySet()) }
  var isSpecificDayOnly by remember { mutableStateOf(true) }
  var showCalendarPopup by remember { mutableStateOf(false) }

  var targetMinutesStr by remember { mutableStateOf("") }
  var isPreset by remember { mutableStateOf(false) }
  var noteText by remember { mutableStateOf("") }
  var noteImageUri by remember { mutableStateOf<String?>(null) }

  val isEveryday = (repeatMask == HabitTask.EVERYDAY_MASK)
  val dayLabels = HabitTask.DAY_LETTERS

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      val savedPath = ImageStorageUtils.copyUriToInternalStorage(context, uri, "new_task")
      if (savedPath != null) {
        noteImageUri = savedPath
      }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("add_task_dialog"),
    shape = RoundedCornerShape(24.dp),
    title = {
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
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
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
          placeholder = { Text("e.g. bot ncert read, phy q") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag("task_name_input"),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Presets & Quick NEET study presets
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Presets:",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary
            )
          )

          TextButton(
            onClick = onOpenPresetsManager,
            modifier = Modifier.testTag("btn_edit_presets")
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Manage Presets", fontSize = 11.sp)
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          if (presets.isEmpty()) {
            Text(
              text = "No presets saved yet. Tap 'Manage Presets' to add presets.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            // Show user presets with quick tap to use and delete icon
            presets.forEach { presetItem ->
              SuggestionChip(
                onClick = {
                  taskName = presetItem.name
                  if (presetItem.targetTimeMinutes > 0) {
                    targetMinutesStr = presetItem.targetTimeMinutes.toString()
                  }
                  noteText = presetItem.noteText
                  noteImageUri = presetItem.noteImageUri
                  isPreset = true
                },
                label = {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = "⭐ ${presetItem.name}",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                      )
                    )
                    if (onDeletePreset != null) {
                      Spacer(modifier = Modifier.width(4.dp))
                      Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove preset",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                          .size(14.dp)
                          .clickable { onDeletePreset(presetItem.id) }
                      )
                    }
                  }
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                  containerColor = if (taskName.equals(presetItem.name, ignoreCase = true)) {
                    MaterialTheme.colorScheme.primaryContainer
                  } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                  },
                  labelColor = MaterialTheme.colorScheme.onSurface
                )
              )
            }
          }
        }

        if (taskName.isNotBlank() && onAddPreset != null && !presets.any { it.name.equals(taskName.trim(), ignoreCase = true) }) {
          Spacer(modifier = Modifier.height(4.dp))
          TextButton(
            onClick = { onAddPreset(taskName.trim()) },
            modifier = Modifier.align(Alignment.End)
          ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Save '$taskName' as Preset", fontSize = 11.sp)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
          placeholder = { Text("e.g. 45 (excess time will show in Red)") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          leadingIcon = {
            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Date selection and Frequency options
        Text(
          text = "Schedule for:",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Schedule mode chips
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChip(
            selected = isSpecificDayOnly && selectedDates.isEmpty(),
            onClick = {
              isSpecificDayOnly = true
              selectedDates = emptySet()
              repeatMask = 0
            },
            label = { Text("This Day ($effectiveDate)", style = MaterialTheme.typography.labelSmall) }
          )

          FilterChip(
            selected = selectedDates.isNotEmpty(),
            onClick = {
              showCalendarPopup = true
            },
            leadingIcon = {
              Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
            },
            label = {
              Text(
                if (selectedDates.isEmpty()) "Select Date(s)..."
                else "${selectedDates.size} date(s) selected",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
              )
            }
          )

          FilterChip(
            selected = !isSpecificDayOnly && isEveryday,
            onClick = {
              isSpecificDayOnly = false
              selectedDates = emptySet()
              repeatMask = HabitTask.EVERYDAY_MASK
            },
            label = { Text("Everyday", style = MaterialTheme.typography.labelSmall) }
          )

          FilterChip(
            selected = !isSpecificDayOnly && !isEveryday && selectedDates.isEmpty(),
            onClick = {
              isSpecificDayOnly = false
              selectedDates = emptySet()
              if (repeatMask == 0) {
                repeatMask = 1 shl dayOfWeekIndex
              }
            },
            label = { Text("Week Days", style = MaterialTheme.typography.labelSmall) }
          )
        }

        // Custom Week Days (if chosen)
        if (!isSpecificDayOnly && selectedDates.isEmpty()) {
          Spacer(modifier = Modifier.height(8.dp))
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

        // Option to save as Preset
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
                text = "Save as Preset",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
              )
              Text(
                text = "Syncs with presets manager in hamburger menu",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Switch(
            checked = isPreset,
            onCheckedChange = { isPreset = it },
            modifier = Modifier.testTag("switch_add_as_preset")
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
          placeholder = { Text("e.g. Chapter formula list or questions to solve") },
          leadingIcon = {
            Icon(Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(18.dp))
          },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 3,
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
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val mask = if (isSpecificDayOnly && selectedDates.isEmpty()) 0 else repeatMask
            val target = targetMinutesStr.toIntOrNull() ?: 0
            onConfirm(
              taskName.trim(),
              selectedDates,
              mask,
              target,
              isPreset,
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
