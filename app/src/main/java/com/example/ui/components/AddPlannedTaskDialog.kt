package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlannedTask
import com.example.ui.theme.RatingBestGreen
import com.example.util.DateUtils
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddPlannedTaskDialog(
  initialDate: String = DateUtils.today(),
  onDismiss: () -> Unit,
  onConfirm: (List<PlannedTask>) -> Unit
) {
  val haptic = LocalHapticFeedback.current
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  var title by remember { mutableStateOf("") }
  var selectedDates by remember { mutableStateOf<Set<String>>(setOf(initialDate)) }
  var targetMinutesStr by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }
  var showCalendarPopup by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    delay(150)
    try {
      focusRequester.requestFocus()
      keyboardController?.show()
    } catch (_: Exception) {}
  }

  AlertDialog(
    onDismissRequest = onDismiss,
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
          text = "Plan Next Task / Event",
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
          text = "Task Title",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          placeholder = { Text("e.g. phy ch q, bot ncert read") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag("plan_task_title_input"),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Presets
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          listOf("phy q", "bot ncert read", "org revision").forEach { preset ->
            SuggestionChip(
              onClick = { title = preset },
              label = { Text(preset, fontSize = 11.sp) }
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Multi-Date Selection Section (v5.2)
        Text(
          text = "Scheduled Date(s)",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
        Spacer(modifier = Modifier.height(4.dp))

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val today = DateUtils.today()
          val tomorrow = DateUtils.addDays(today, 1)

          FilterChip(
            selected = selectedDates.size == 1 && selectedDates.contains(today),
            onClick = {
              selectedDates = setOf(today)
            },
            label = { Text("Today", fontSize = 12.sp) }
          )

          FilterChip(
            selected = selectedDates.size == 1 && selectedDates.contains(tomorrow),
            onClick = {
              selectedDates = setOf(tomorrow)
            },
            label = { Text("Tomorrow", fontSize = 12.sp) }
          )

          FilterChip(
            selected = selectedDates.size > 1 || (!selectedDates.contains(today) && !selectedDates.contains(tomorrow) && selectedDates.isNotEmpty()),
            onClick = {
              showCalendarPopup = true
            },
            leadingIcon = {
              Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            label = {
              Text(
                if (selectedDates.isEmpty()) "Select Date(s)..."
                else if (selectedDates.size == 1) "${selectedDates.first()} (Multi-date)"
                else "${selectedDates.size} dates selected",
                fontSize = 12.sp,
                fontWeight = if (selectedDates.size > 1) FontWeight.Bold else FontWeight.Normal
              )
            }
          )
        }

        // Show chips for all selected dates if multiple or custom date selected
        if (selectedDates.isNotEmpty()) {
          Spacer(modifier = Modifier.height(6.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            selectedDates.sorted().forEach { dateItem ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                  .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                  )
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = DateUtils.formatShortDate(dateItem),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                  )
                  if (selectedDates.size > 1) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                      imageVector = Icons.Default.Close,
                      contentDescription = "Remove date",
                      tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                      modifier = Modifier
                        .size(14.dp)
                        .clickable {
                          selectedDates = selectedDates - dateItem
                        }
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Planned Duration (Minutes)",
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
          placeholder = { Text("e.g. 60") },
          leadingIcon = {
            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Notes / Topics",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          placeholder = { Text("e.g. Chapter 4 Electrostatics 30 questions") },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 3,
          shape = RoundedCornerShape(12.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val datesToSchedule = if (selectedDates.isNotEmpty()) selectedDates else setOf(initialDate)
            val plans = datesToSchedule.map { d ->
              PlannedTask(
                title = title.trim(),
                date = d,
                targetTimeMinutes = targetMinutesStr.toIntOrNull() ?: 0,
                notes = notes.trim(),
                isStarred = false
              )
            }
            onConfirm(plans)
          }
        },
        enabled = title.isNotBlank(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RatingBestGreen)
      ) {
        val count = selectedDates.size
        Text(
          text = if (count > 1) "Save Plan ($count days)" else "Save Plan",
          fontWeight = FontWeight.Bold
        )
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

  if (showCalendarPopup) {
    MiniCalendarPickerPopup(
      initialDates = selectedDates,
      allowMultiple = true,
      title = "Select Plan Date(s)",
      onDismiss = { showCalendarPopup = false },
      onConfirm = { dates ->
        if (dates.isNotEmpty()) {
          selectedDates = dates
        }
        showCalendarPopup = false
      }
    )
  }
}
