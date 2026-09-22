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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EventSubtask
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
    notes: String,
    subtasks: List<EventSubtask>
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
  var subtasks by remember { mutableStateOf(eventToEdit?.getSubtasks() ?: emptyList()) }
  var newSubtaskTitle by remember { mutableStateOf("") }

  var pickingStartDate by remember { mutableStateOf(false) }
  var pickingEndDate by remember { mutableStateOf(false) }
  var subtaskToPickDatesFor by remember { mutableStateOf<EventSubtask?>(null) }

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
          text = "Events span a starting to ending date. Subtasks can be assigned to specific dates, or appear everyday during the event.",
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

        // Subtasks Inside Event Section
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Checklist,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Subtasks inside Event",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
              }
              Text(
                text = "${subtasks.size}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
              )
            }

            // Existing Subtasks List
            subtasks.forEachIndexed { index, st ->
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "${index + 1}. ${st.title}",
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                      color = MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.weight(1f)
                    )
                    IconButton(
                      onClick = { subtasks = subtasks.filter { it.id != st.id } },
                      modifier = Modifier.size(24.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove subtask",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(6.dp))

                  // Assigned Dates badge / selector button
                  val assignedDatesList = st.assignedDates.split(",").filter { it.isNotBlank() }
                  val datesLabel = if (assignedDatesList.isEmpty()) {
                    "🗓️ Everyday during event"
                  } else {
                    "🗓️ ${assignedDatesList.size} date(s): " + assignedDatesList.take(2).joinToString(", ") { DateUtils.formatShortDate(it) } + if (assignedDatesList.size > 2) "..." else ""
                  }

                  OutlinedButton(
                    onClick = { subtaskToPickDatesFor = st },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text(
                      text = datesLabel,
                      style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                      color = if (assignedDatesList.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                  }
                }
              }
            }

            // Add new subtask input
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              OutlinedTextField(
                value = newSubtaskTitle,
                onValueChange = { newSubtaskTitle = it },
                placeholder = { Text("Add subtask title...", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              )
              Button(
                onClick = {
                  if (newSubtaskTitle.isNotBlank()) {
                    subtasks = subtasks + EventSubtask(
                      title = newSubtaskTitle.trim(),
                      assignedDates = "" // defaults to everyday during event!
                    )
                    newSubtaskTitle = ""
                  }
                },
                enabled = newSubtaskTitle.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("Add", fontSize = 12.sp)
              }
            }
          }
        }

        // Primary Task (optional overarching task)
        OutlinedTextField(
          value = taskTitle,
          onValueChange = { taskTitle = it },
          label = { Text("Primary Event Goal / Habit (Optional)") },
          placeholder = { Text("e.g. Complete 50 PYQs & NCERT revision") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        // Target minutes
        OutlinedTextField(
          value = targetMinutesStr,
          onValueChange = { targetMinutesStr = it.filter { ch -> ch.isDigit() } },
          label = { Text("Daily Target Timer (minutes, optional)") },
          placeholder = { Text("e.g. 60") },
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
              notes.trim(),
              subtasks
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

  // Subtask Assigned Dates Picker Popup
  subtaskToPickDatesFor?.let { targetSubtask ->
    val currentAssignedDates = targetSubtask.assignedDates.split(",").filter { it.isNotBlank() }.toSet()
    MiniCalendarPickerPopup(
      initialDates = currentAssignedDates,
      allowMultiple = true,
      title = "Assign Dates for \"${targetSubtask.title}\" (leave empty for everyday)",
      onDismiss = { subtaskToPickDatesFor = null },
      onConfirm = { selectedDates ->
        val sortedDates = selectedDates.sorted().joinToString(",")
        subtasks = subtasks.map {
          if (it.id == targetSubtask.id) it.copy(assignedDates = sortedDates) else it
        }
        subtaskToPickDatesFor = null
      }
    )
  }
}
