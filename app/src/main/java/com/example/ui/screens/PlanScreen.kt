package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PlanEvent
import com.example.data.model.PlannedTask
import com.example.ui.HabitViewModel
import com.example.ui.components.AddPlannedTaskDialog
import com.example.ui.components.PlanEventDialog
import com.example.util.DateUtils

@Composable
fun PlanScreen(
  viewModel: HabitViewModel,
  onJumpToTask: (PlannedTask) -> Unit,
  modifier: Modifier = Modifier
) {
  val plannedTasks by viewModel.allPlannedTasks.collectAsStateWithLifecycle()
  val planEvents by viewModel.allPlanEvents.collectAsStateWithLifecycle()
  var showAddDialog by remember { mutableStateOf(false) }
  var showAddEventDialog by remember { mutableStateOf(false) }
  var eventToEdit by remember { mutableStateOf<PlanEvent?>(null) }
  var filterStarredOnly by remember { mutableStateOf(false) }

  val today = DateUtils.today()
  val upcomingTasks = plannedTasks.filter { it.date >= today }
  val pastTasks = plannedTasks.filter { it.date < today }

  val upcomingStarredTasks = upcomingTasks.filter { it.isStarred }
  val upcomingStarredDaysCount = upcomingStarredTasks.map { it.date }.distinct().size

  val displayedUpcoming = if (filterStarredOnly) upcomingStarredTasks else upcomingTasks
  val haptic = LocalHapticFeedback.current

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Events Header & Add Event Action
      item {
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
              text = "Events",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
              Text(
                text = "${planEvents.size}",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              )
            }
          }

          OutlinedButton(
            onClick = { showAddEventDialog = true },
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.testTag("btn_add_event")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("+ Add Event", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }

      // Active Events Cards List
      if (planEvents.isNotEmpty()) {
        items(
          items = planEvents,
          key = { "event_${it.id}" }
        ) { event ->
          EventCardItem(
            event = event,
            onEdit = { eventToEdit = event },
            onDelete = { viewModel.deletePlanEvent(event) }
          )
        }
      }

      // Upcoming count summary & Starred tasks in days banner - Minimalist subtle container
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
          ),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
          )
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Upcoming Planned Events",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "${upcomingTasks.size} total tasks scheduled for upcoming dates",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant)
                  .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "${upcomingTasks.size}",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Starred tasks banner indicator - clean neutral container with star
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "⭐ ${upcomingStarredTasks.size} Starred Task${if (upcomingStarredTasks.size == 1) "" else "s"}",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                )
                Text(
                  text = if (upcomingStarredTasks.isEmpty()) {
                    "No starred tasks yet — star a task to prioritize"
                  } else {
                    "Scheduled across $upcomingStarredDaysCount upcoming day${if (upcomingStarredDaysCount == 1) "" else "s"}"
                  },
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )
              }
            }
          }
        }
      }

      // Filter chips: All vs Starred Only
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          FilterChip(
            selected = !filterStarredOnly,
            onClick = { filterStarredOnly = false },
            label = { Text("All (${upcomingTasks.size})") }
          )

          FilterChip(
            selected = filterStarredOnly,
            onClick = { filterStarredOnly = true },
            leadingIcon = {
              Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(16.dp)
              )
            },
            label = { Text("Starred (${upcomingStarredTasks.size})") }
          )
        }
      }

      if (displayedUpcoming.isEmpty() && pastTasks.isEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = if (filterStarredOnly) "No upcoming starred tasks" else "No future events planned yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Plan ahead by choosing dates, target study minutes, starring priority tasks, and jump directly to start tracking.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Plan New Event")
              }
            }
          }
        }
      } else {
        // Upcoming items
        if (displayedUpcoming.isNotEmpty()) {
          item {
            Text(
              text = if (filterStarredOnly) "Upcoming Starred Tasks" else "Upcoming Schedule",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            )
          }

          items(displayedUpcoming, key = { it.id }) { task ->
            PlannedTaskCard(
              plannedTask = task,
              onToggleComplete = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.togglePlannedTaskCompleted(task)
              },
              onToggleStarred = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.togglePlannedTaskStarred(task)
              },
              onJumpToTask = { onJumpToTask(task) },
              onDelete = { viewModel.deletePlannedTask(task.id) }
            )
          }
        }

        // Past planned items (only show when not filtering starred)
        if (!filterStarredOnly && pastTasks.isNotEmpty()) {
          item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Past Planned Tasks",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }

          items(pastTasks, key = { it.id }) { task ->
            PlannedTaskCard(
              plannedTask = task,
              onToggleComplete = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.togglePlannedTaskCompleted(task)
              },
              onToggleStarred = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.togglePlannedTaskStarred(task)
              },
              onJumpToTask = { onJumpToTask(task) },
              onDelete = { viewModel.deletePlannedTask(task.id) }
            )
          }
        }
      }
    }

    // Floating Action Button to plan new task
    FloatingActionButton(
      onClick = { showAddDialog = true },
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 20.dp, bottom = 20.dp)
        .testTag("fab_add_plan"),
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      shape = CircleShape
    ) {
      Icon(Icons.Default.Add, contentDescription = "Add Plan", modifier = Modifier.size(28.dp))
    }

    if (showAddDialog) {
      AddPlannedTaskDialog(
        onDismiss = { showAddDialog = false },
        onConfirm = { newPlan ->
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          viewModel.addPlannedTask(newPlan)
          showAddDialog = false
        }
      )
    }

    if (showAddEventDialog) {
      PlanEventDialog(
        onDismiss = { showAddEventDialog = false },
        onConfirm = { title, startDate, endDate, taskTitle, taskTargetMinutes, notes ->
          viewModel.addPlanEvent(
            title = title,
            startDate = startDate,
            endDate = endDate,
            taskTitle = taskTitle,
            taskTargetMinutes = taskTargetMinutes,
            notes = notes
          )
          showAddEventDialog = false
        }
      )
    }

    eventToEdit?.let { ev ->
      PlanEventDialog(
        eventToEdit = ev,
        onDismiss = { eventToEdit = null },
        onConfirm = { title, startDate, endDate, taskTitle, taskTargetMinutes, notes ->
          viewModel.updatePlanEvent(
            ev.copy(
              title = title,
              startDate = startDate,
              endDate = endDate,
              taskTitle = taskTitle,
              taskTargetMinutes = taskTargetMinutes,
              notes = notes
            )
          )
          eventToEdit = null
        },
        onDelete = {
          viewModel.deletePlanEvent(ev)
          eventToEdit = null
        }
      )
    }
  }
}

@Composable
private fun EventCardItem(
  event: PlanEvent,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    )
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Icon(
            imageVector = Icons.Default.Event,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = event.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit Event",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
          }
          IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete Event",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.CalendarMonth,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "${DateUtils.formatShortDate(event.startDate)} → ${DateUtils.formatShortDate(event.endDate)}",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      if (event.taskTitle.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "🎯 Task: ${event.taskTitle}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (event.taskTargetMinutes > 0) {
              Text(
                text = " (${event.taskTargetMinutes}m)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }
        }
      }

      if (event.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = event.notes,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
private fun PlannedTaskCard(
  plannedTask: PlannedTask,
  onToggleComplete: () -> Unit,
  onToggleStarred: () -> Unit,
  onJumpToTask: () -> Unit,
  onDelete: () -> Unit
) {
  val isCompleted = plannedTask.isCompleted
  val isToday = plannedTask.date == DateUtils.today()
  val isStarred = plannedTask.isStarred

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isCompleted) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
      } else {
        MaterialTheme.colorScheme.surface
      }
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .border(
          width = 1.dp,
          color = when {
            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
          },
          shape = RoundedCornerShape(16.dp)
        )
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isToday) "Today (${DateUtils.formatShortDate(plannedTask.date)})" else DateUtils.formatFullDate(plannedTask.date),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              )
            }
          }

          if (plannedTask.targetTimeMinutes > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "${plannedTask.targetTimeMinutes}m target",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                )
              }
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Star Icon Button
          IconButton(onClick = onToggleStarred, modifier = Modifier.size(32.dp)) {
            Icon(
              imageVector = if (isStarred) Icons.Default.Star else Icons.Default.StarBorder,
              contentDescription = if (isStarred) "Unstar Task" else "Star Task",
              tint = if (isStarred) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(22.dp)
            )
          }

          IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        if (isStarred) {
          Icon(
            Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
        }

        Text(
          text = plannedTask.title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
          ),
          color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
        )
      }

      if (plannedTask.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = plannedTask.notes,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Toggle Completed Button
        OutlinedButton(
          onClick = onToggleComplete,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent,
            contentColor = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
          ),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isCompleted) "Completed" else "Mark Done",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        // Jump Directly to Task button
        OutlinedButton(
          onClick = onJumpToTask,
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Jump to Task", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
      }
    }
  }
}
