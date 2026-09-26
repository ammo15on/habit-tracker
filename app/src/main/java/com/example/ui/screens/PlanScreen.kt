package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Unarchive
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
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.model.EventSubtask
import com.example.data.model.PlanEvent
import com.example.data.model.PlannedTask
import com.example.ui.HabitViewModel
import com.example.ui.components.AddPlannedTaskDialog
import com.example.ui.components.PlanEventDialog
import androidx.compose.material.icons.filled.Flag
import com.example.ui.components.GoalDialog
import com.example.util.DateUtils

@Composable
fun PlanScreen(
  viewModel: HabitViewModel,
  onJumpToTask: (PlannedTask) -> Unit,
  modifier: Modifier = Modifier
) {
  val plannedTasks by viewModel.allPlannedTasks.collectAsStateWithLifecycle()
  val planEvents by viewModel.allPlanEvents.collectAsStateWithLifecycle()
  val goal by viewModel.goal.collectAsStateWithLifecycle()
  var showAddDialog by remember { mutableStateOf(false) }
  var showAddEventDialog by remember { mutableStateOf(false) }
  var showGoalDialog by remember { mutableStateOf(false) }
  var eventToEdit by remember { mutableStateOf<PlanEvent?>(null) }
  var viewArchivedOnly by remember { mutableStateOf(false) }

  val today = DateUtils.today()
  val activeTasks = plannedTasks.filter { !it.isArchived && it.date >= today }
  val archivedTasks = plannedTasks.filter { it.isArchived && it.date >= today }

  val displayedTasks = if (viewArchivedOnly) archivedTasks else activeTasks
  val groupedTasks = displayedTasks.groupBy { it.date }.toSortedMap(compareBy { it })

  // Days collapse/expand state: by default, all dates are expanded (true)
  val collapsedDays = remember { mutableStateMapOf<String, Boolean>() }

  val haptic = LocalHapticFeedback.current

  Box(modifier = modifier.fillMaxSize().background(Color.Transparent)) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Top Heading - Events Section with transparent background
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.openHamburgerMenu()
              },
              modifier = Modifier
                .size(40.dp)
                .testTag("btn_plan_hamburger")
            ) {
              Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Open Settings & Hub",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Default.Event,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
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

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
              onClick = { showGoalDialog = true },
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier.testTag("btn_plan_goal")
            ) {
              Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (goal != null) "🎯 Goal" else "+ Set Goal", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
      }

      // Active Target Goal Card (if set)
      if (goal != null) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .clickable { showGoalDialog = true },
            colors = CardDefaults.cardColors(
              containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Flag,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "🎯 ${goal!!.title}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "Target Date: ${DateUtils.formatFullDate(goal!!.targetDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              val daysLeft = DateUtils.daysBetween(DateUtils.today(), goal!!.targetDate)
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color.Transparent)
                  .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = if (daysLeft > 0) "$daysLeft days left" else if (daysLeft == 0L) "Today!" else "${-daysLeft} days ago",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }

      // Active Events Cards List with Subtasks
      if (planEvents.isNotEmpty()) {
        items(
          items = planEvents,
          key = { "event_${it.id}" }
        ) { event ->
          EventCardItem(
            event = event,
            onEdit = { eventToEdit = event },
            onDelete = { viewModel.deletePlanEvent(event) },
            onToggleSubtask = { subtaskId ->
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              viewModel.toggleEventSubtaskComplete(event, subtaskId)
            }
          )
        }
      }

      // Filter Tabs: All Tasks vs Archived Tasks
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          FilterChip(
            selected = !viewArchivedOnly,
            onClick = { viewArchivedOnly = false },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            label = {
              Text("All Tasks (${activeTasks.size})", fontWeight = if (!viewArchivedOnly) FontWeight.Bold else FontWeight.Normal)
            }
          )

          FilterChip(
            selected = viewArchivedOnly,
            onClick = { viewArchivedOnly = true },
            leadingIcon = {
              Icon(
                Icons.Default.Archive,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            label = {
              Text("Archived (${archivedTasks.size})", fontWeight = if (viewArchivedOnly) FontWeight.Bold else FontWeight.Normal)
            }
          )
        }
      }

      // Grouped Tasks by Day
      if (groupedTasks.isEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = if (viewArchivedOnly) Icons.Default.Archive else Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = if (viewArchivedOnly) "No archived tasks" else "No planned tasks scheduled",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = if (viewArchivedOnly) {
                  "Archived tasks remain visible on their day in Tracker, but are hidden from your active Plan."
                } else {
                  "Plan tasks for specific days with target minutes and jump directly to track them."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              if (!viewArchivedOnly) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                  onClick = { showAddDialog = true },
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Plan New Task")
                }
              }
            }
          }
        }
      } else {
        // Render each day group
        groupedTasks.forEach { (date, tasksForDate) ->
          val isCollapsed = collapsedDays[date] == true
          val isToday = date == today

          item(key = "day_header_$date") {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { collapsedDays[date] = !isCollapsed },
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
              ),
              elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
              )
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = when {
                      isToday -> "Today (${DateUtils.formatShortDate(date)})"
                      date == DateUtils.addDays(today, 1) -> "Tomorrow (${DateUtils.formatShortDate(date)})"
                      else -> DateUtils.formatFullDate(date)
                    },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(Color.Transparent)
                      .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = "${tasksForDate.size}",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = if (isCollapsed) "Expand" else "Collapse",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(
                    imageVector = if (isCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = if (isCollapsed) "Expand day" else "Collapse day",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            }
          }

          if (!isCollapsed) {
            items(tasksForDate, key = { "task_${it.id}" }) { task ->
              PlannedTaskCard(
                plannedTask = task,
                onToggleComplete = {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  viewModel.togglePlannedTaskCompleted(task)
                },
                onToggleArchived = {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  viewModel.togglePlannedTaskArchived(task)
                },
                onJumpToTask = { onJumpToTask(task) },
                onDelete = { viewModel.deletePlannedTask(task.id) }
              )
            }
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

    if (showGoalDialog) {
      GoalDialog(
        currentGoal = goal,
        onSetGoal = { title, targetDate -> viewModel.setGoal(title, targetDate) },
        onClearGoal = { viewModel.clearGoal() },
        onDismiss = { showGoalDialog = false }
      )
    }

    if (showAddDialog) {
      AddPlannedTaskDialog(
        onDismiss = { showAddDialog = false },
        onConfirm = { newPlans ->
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          newPlans.forEach { plan ->
            viewModel.addPlannedTask(plan)
          }
          showAddDialog = false
        }
      )
    }

    if (showAddEventDialog) {
      PlanEventDialog(
        onDismiss = { showAddEventDialog = false },
        onConfirm = { title, startDate, endDate, taskTitle, taskTargetMinutes, notes, subtasks ->
          viewModel.addPlanEvent(
            title = title,
            startDate = startDate,
            endDate = endDate,
            taskTitle = taskTitle,
            taskTargetMinutes = taskTargetMinutes,
            notes = notes,
            subtasks = subtasks
          )
          showAddEventDialog = false
        }
      )
    }

    eventToEdit?.let { ev ->
      PlanEventDialog(
        eventToEdit = ev,
        onDismiss = { eventToEdit = null },
        onConfirm = { title, startDate, endDate, taskTitle, taskTargetMinutes, notes, subtasks ->
          viewModel.updatePlanEvent(
            ev.copy(
              title = title,
              startDate = startDate,
              endDate = endDate,
              taskTitle = taskTitle,
              taskTargetMinutes = taskTargetMinutes,
              notes = notes,
              subtasksJson = PlanEvent.serializeSubtasks(subtasks)
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
  onDelete: () -> Unit,
  onToggleSubtask: (String) -> Unit
) {
  val subtasks = event.getSubtasks()
  var isSubtasksExpanded by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = Color.Transparent
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
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
          if (subtasks.isNotEmpty()) {
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
              onClick = { isSubtasksExpanded = !isSubtasksExpanded },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                imageVector = if (isSubtasksExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isSubtasksExpanded) "Collapse subtasks" else "Expand subtasks",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            }
          }
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
            .background(Color.Transparent)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "🎯 Goal: ${event.taskTitle}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            if (event.taskTargetMinutes > 0) {
              Text(
                text = " (${event.taskTargetMinutes}m)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // Subtasks in Event Section (Collapsible)
      if (subtasks.isNotEmpty()) {
        AnimatedVisibility(
          visible = isSubtasksExpanded,
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut()
        ) {
          Column {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                .padding(8.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { isSubtasksExpanded = !isSubtasksExpanded }
              ) {
                Icon(
                  imageVector = Icons.Default.Checklist,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Subtasks (${subtasks.count { it.isCompleted }}/${subtasks.size})",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }

              subtasks.forEach { st ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onToggleSubtask(st.id) }
                    .padding(vertical = 3.dp, horizontal = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = if (st.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (st.isCompleted) "Completed" else "Incomplete",
                    tint = if (st.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = st.title,
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (st.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                      ),
                      color = if (st.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    val assignedList = st.assignedDates.split(",").filter { it.isNotBlank() }
                    if (assignedList.isNotEmpty()) {
                      Text(
                        text = "🗓️ " + assignedList.take(3).joinToString(", ") { DateUtils.formatShortDate(it) },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    } else {
                      Text(
                        text = "🗓️ Everyday during event",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      if (event.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
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
  onToggleArchived: () -> Unit,
  onJumpToTask: () -> Unit,
  onDelete: () -> Unit
) {
  val isCompleted = plannedTask.isCompleted
  val isArchived = plannedTask.isArchived
  val isToday = plannedTask.date == DateUtils.today()

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = Color.Transparent
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .border(
          width = 1.dp,
          color = when {
            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
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
              .background(Color.Transparent)
              .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
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
                .background(Color.Transparent)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
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
          // Archive / Unarchive Action
          IconButton(onClick = onToggleArchived, modifier = Modifier.size(32.dp)) {
            Icon(
              imageVector = if (isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
              contentDescription = if (isArchived) "Unarchive Task" else "Archive Task",
              tint = if (isArchived) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }

          IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        if (isArchived) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "Archived",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
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
