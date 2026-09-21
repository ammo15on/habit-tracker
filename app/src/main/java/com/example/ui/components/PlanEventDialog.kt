package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlanEvent
import com.example.util.DateUtils

@Composable
fun PlanEventDialog(
  eventToEdit: PlanEvent? = null,
  onDismiss: () -> Unit,
  onConfirm: (
    title: String,
    startDate: String,
    endDate: String,
    taskTitle: String,
    taskTargetMinutes: Int,
    notes: String
  ) -> Unit,
  onDelete: (() -> Unit)? = null
) {
  val today = DateUtils.today()
  val nextWeek = DateUtils.addDays(today, 7)

  var title by remember { mutableStateOf(eventToEdit?.title ?: "") }
  var startDate by remember { mutableStateOf(eventToEdit?.startDate ?: today) }
  var endDate by remember { mutableStateOf(eventToEdit?.endDate ?: nextWeek) }
  var taskTitle by remember { mutableStateOf(eventToEdit?.taskTitle ?: "") }
  var targetMinutesStr by remember {
    mutableStateOf(if ((eventToEdit?.taskTargetMinutes ?: 0) > 0) eventToEdit!!.taskTargetMinutes.toString() else "")
  }
  var notes by remember { mutableStateOf(eventToEdit?.notes ?: "") }

  var pickingStartDate by remember { mutableStateOf(false) }
  var pickingEndDate by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("plan_event_dialog"),
    shape = RoundedCornerShape(24.dp),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Event,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (eventToEdit == null) "Create Event" else "Edit Event",
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
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Events span a starting to ending date. The task to do will appear in Planned and Tracker views during this period.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Event Title
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Event Name / Subject") },
          placeholder = { Text("e.g. NEET Biology Marathon") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        // Date Range Selector (Start to End)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = { pickingStartDate = true },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Start Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.height(2.dp))
              Text(startDate, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
            }
          }

          OutlinedButton(
            onClick = { pickingEndDate = true },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("End Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.height(2.dp))
              Text(endDate, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
            }
          }
        }

        // Task to do in this event
        OutlinedTextField(
          value = taskTitle,
          onValueChange = { taskTitle = it },
          label = { Text("Task to do in this event") },
          placeholder = { Text("e.g. Complete 50 PYQs & NCERT revision") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        // Target minutes
        OutlinedTextField(
          value = targetMinutesStr,
          onValueChange = { targetMinutesStr = it.filter { ch -> ch.isDigit() } },
          label = { Text("Daily Target Timer (minutes)") },
          placeholder = { Text("e.g. 60 (optional)") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          leadingIcon = {
            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        // Notes
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Event Notes / Description (Optional)") },
          placeholder = { Text("Add key points or syllabus focus...") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 2,
          maxLines = 4,
          shape = RoundedCornerShape(12.dp)
        )

        // Delete button if editing
        if (eventToEdit != null && onDelete != null) {
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedButton(
            onClick = onDelete,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Remove Event")
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            val targetMinutes = targetMinutesStr.toIntOrNull() ?: 0
            val effectiveTaskTitle = if (taskTitle.isNotBlank()) taskTitle.trim() else title.trim()
            val effectiveStart = if (startDate <= endDate) startDate else endDate
            val effectiveEnd = if (startDate <= endDate) endDate else startDate
            onConfirm(
              title.trim(),
              effectiveStart,
              effectiveEnd,
              effectiveTaskTitle,
              targetMinutes,
              notes.trim()
            )
          }
        },
        enabled = title.isNotBlank(),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text(if (eventToEdit == null) "Create Event" else "Save Changes")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
        Text("Cancel")
      }
    }
  )

  // Start Date Picker Popup
  if (pickingStartDate) {
    MiniCalendarPickerPopup(
      initialDates = setOf(startDate),
      allowMultiple = false,
      title = "Select Start Date",
      onDismiss = { pickingStartDate = false },
      onConfirm = { dates ->
        dates.firstOrNull()?.let { startDate = it }
        pickingStartDate = false
      }
    )
  }

  // End Date Picker Popup
  if (pickingEndDate) {
    MiniCalendarPickerPopup(
      initialDates = setOf(endDate),
      allowMultiple = false,
      title = "Select End Date",
      onDismiss = { pickingEndDate = false },
      onConfirm = { dates ->
        dates.firstOrNull()?.let { endDate = it }
        pickingEndDate = false
      }
    )
  }
}
