package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material.icons.filled.Check
import com.example.data.model.TaskPreset

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresetsDialog(
  presets: List<TaskPreset>,
  onDismiss: () -> Unit,
  onDeletePreset: (Long) -> Unit,
  onAddPreset: (name: String) -> Unit,
  onSchedulePresetForDates: (preset: TaskPreset, dates: Set<String>) -> Unit,
  onAddPresetToDay: ((preset: TaskPreset) -> Unit)? = null
) {
  val haptic = LocalHapticFeedback.current
  var customPresetInput by remember { mutableStateOf("") }
  var presetToSchedule by remember { mutableStateOf<TaskPreset?>(null) }
  var addedPresetIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("presets_dialog"),
    shape = RoundedCornerShape(24.dp),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Presets (${presets.size})",
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
          .heightIn(max = 480.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "Presets sync across Add Task and Tracker. Schedule them for specific dates using the calendar.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Quick add custom preset field
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = customPresetInput,
            onValueChange = { customPresetInput = it },
            placeholder = { Text("New preset name...", fontSize = 13.sp) },
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("input_custom_preset"),
            shape = RoundedCornerShape(12.dp)
          )

          Button(
            onClick = {
              if (customPresetInput.isNotBlank()) {
                onAddPreset(customPresetInput.trim())
                customPresetInput = ""
              }
            },
            enabled = customPresetInput.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add")
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Active Presets List
        if (presets.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No presets saved yet. Add one above.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(presets, key = { it.id }) { preset ->
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                  0.5.dp,
                  MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = preset.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                      )
                      if (preset.targetTimeMinutes > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                          )
                          Spacer(modifier = Modifier.width(3.dp))
                          Text(
                            text = "${preset.targetTimeMinutes} min target",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                          )
                        }
                      }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                      IconButton(
                        onClick = { onDeletePreset(preset.id) },
                        modifier = Modifier.size(28.dp)
                      ) {
                        Icon(
                          Icons.Default.Delete,
                          contentDescription = "Delete",
                          tint = MaterialTheme.colorScheme.error,
                          modifier = Modifier.size(16.dp)
                        )
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(6.dp))

                  Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    if (onAddPresetToDay != null) {
                      val isAdded = addedPresetIds.contains(preset.id)
                      Button(
                        onClick = {
                          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                          onAddPresetToDay(preset)
                          addedPresetIds = addedPresetIds + preset.id
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(30.dp),
                        colors = if (isAdded) ButtonDefaults.buttonColors(
                          containerColor = MaterialTheme.colorScheme.secondary
                        ) else ButtonDefaults.buttonColors()
                      ) {
                        Icon(
                          if (isAdded) Icons.Default.Check else Icons.Default.Add,
                          contentDescription = null,
                          modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                          text = if (isAdded) "Added ✓" else "Add to Day",
                          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                      }
                    }

                    // "Schedule for Dates" Action Button with Mini Calendar
                    OutlinedButton(
                      onClick = { presetToSchedule = preset },
                      contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                      shape = RoundedCornerShape(8.dp),
                      modifier = Modifier.height(30.dp)
                    ) {
                      Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "Schedule...",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
        Text("Done")
      }
    }
  )

  // Mini Calendar Popup for scheduling preset across multiple dates
  presetToSchedule?.let { preset ->
    MiniCalendarPickerPopup(
      title = "Schedule \"${preset.name}\"",
      allowMultiple = true,
      onDismiss = { presetToSchedule = null },
      onConfirm = { selectedDates ->
        if (selectedDates.isNotEmpty()) {
          onSchedulePresetForDates(preset, selectedDates)
        }
        presetToSchedule = null
      }
    )
  }
}
