package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitTask
import com.example.ui.theme.RatingBestGreen

@Composable
fun AddTaskDialog(
  onDismiss: () -> Unit,
  onConfirm: (name: String, repeatMask: Int) -> Unit
) {
  var taskName by remember { mutableStateOf("") }
  // default everyday: mask = 127
  var repeatMask by remember { mutableIntStateOf(HabitTask.EVERYDAY_MASK) }
  val isEveryday = (repeatMask == HabitTask.EVERYDAY_MASK)

  val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
  val dayFullNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

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
      Column(modifier = Modifier.fillMaxWidth()) {
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
          placeholder = { Text("e.g. Task-1, Morning Workout, Reading") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("task_name_input"),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Repeat Schedule",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )

          FilterChip(
            selected = isEveryday,
            onClick = {
              repeatMask = if (isEveryday) {
                // If untoggling everyday, default to Monday
                1
              } else {
                HabitTask.EVERYDAY_MASK
              }
            },
            label = { Text("Everyday", fontWeight = FontWeight.SemiBold) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = RatingBestGreen.copy(alpha = 0.2f),
              selectedLabelColor = RatingBestGreen
            ),
            modifier = Modifier.testTag("repeat_everyday_chip")
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sketched M, T, W, T, F, S, S chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          dayLabels.forEachIndexed { index, label ->
            val bit = 1 shl index
            val isSelected = (repeatMask and bit) != 0

            DaySelectionCircle(
              letter = label,
              fullName = dayFullNames[index],
              isSelected = isSelected,
              onClick = {
                repeatMask = if (isSelected) {
                  // Don't allow clearing all days (keep at least 1)
                  val newMask = repeatMask and bit.inv()
                  if (newMask == 0) repeatMask else newMask
                } else {
                  repeatMask or bit
                }
              }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (taskName.isNotBlank()) {
            onConfirm(taskName, repeatMask)
          }
        },
        enabled = taskName.isNotBlank(),
        modifier = Modifier.testTag("save_task_button")
      ) {
        Text("Save Habit")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
private fun DaySelectionCircle(
  letter: String,
  fullName: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .size(36.dp)
      .clip(CircleShape)
      .background(
        if (isSelected) RatingBestGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
      )
      .border(
        width = 1.dp,
        color = if (isSelected) RatingBestGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        shape = CircleShape
      )
      .clickable { onClick() }
      .testTag("day_chip_$fullName"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = letter,
      style = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
      ),
      color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
