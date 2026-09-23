package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import com.example.ui.components.GoalDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.HabitTask
import com.example.data.model.RatingType
import com.example.ui.HabitViewModel
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.DayRatingSection
import com.example.ui.components.EditTaskDialog
import com.example.ui.components.HamburgerMenuDialog
import com.example.ui.components.PresetsDialog
import com.example.ui.components.TaskRowItem
import com.example.util.DateUtils

@Composable
fun TrackerScreen(
  viewModel: HabitViewModel,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
  val tasks by viewModel.tasksForSelectedDate.collectAsStateWithLifecycle()
  val presets by viewModel.presets.collectAsStateWithLifecycle()
  val currentRating by viewModel.currentDayRating.collectAsStateWithLifecycle()
  val totalTimeSeconds by viewModel.totalTimeTodaySeconds.collectAsStateWithLifecycle()
  val planEvents by viewModel.allPlanEvents.collectAsStateWithLifecycle()
  val goal by viewModel.goal.collectAsStateWithLifecycle()

  var showAddDialog by remember { mutableStateOf(false) }
  var showPresetsDialog by remember { mutableStateOf(false) }
  var showGoalDialog by remember { mutableStateOf(false) }
  var showHamburgerMenu by remember { mutableStateOf(false) }
  var taskToEdit by remember { mutableStateOf<HabitTask?>(null) }
  var totalDragX by remember { mutableFloatStateOf(0f) }
  var expandedEventIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
  var isCompletedExpanded by remember { mutableStateOf(false) }

  val isToday = (selectedDate == DateUtils.today())
  val prevDay = DateUtils.getPreviousDay(selectedDate)
  val nextDay = DateUtils.getNextDay(selectedDate)

  val prevDayFormatted = DateUtils.formatShortDate(prevDay)
  val nextDayFormatted = DateUtils.formatShortDate(nextDay)
  val dayAbbr = DateUtils.formatDayOfWeekAbbr(selectedDate)
  val fullDate = DateUtils.formatFullDate(selectedDate)

  val activeTasks = remember(tasks) { tasks.filter { !it.isCompleted } }
  val completedTasks = remember(tasks) { tasks.filter { it.isCompleted } }

  val activeEventsWithSubtasksToday = remember(planEvents, selectedDate) {
    planEvents.filter { event ->
      selectedDate >= event.startDate && selectedDate <= event.endDate &&
        event.getSubtasks().any { event.isSubtaskApplicableForDate(it, selectedDate) }
    }
  }

  Column(modifier = modifier.fillMaxSize()) {
    // Upper scrolling area with day header, task counter, habits list, and FAB
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .pointerInput(Unit) {
          detectHorizontalDragGestures(
            onDragStart = { totalDragX = 0f },
            onDragEnd = {
              if (totalDragX < -70f) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.goToNextDay()
              } else if (totalDragX > 70f) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.goToPreviousDay()
              }
              totalDragX = 0f
            },
            onDragCancel = { totalDragX = 0f },
            onHorizontalDrag = { _, dragAmount ->
              totalDragX += dragAmount
            }
          )
        }
    ) {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // 1. Top Bar Header: (X) Previous Day  |  (Total Time)  |  (Y) Next Day
        item(key = "top_day_nav") {
          DayNavigationHeader(
            selectedDate = selectedDate,
            prevDayText = prevDayFormatted,
            nextDayText = nextDayFormatted,
            totalTimeSeconds = totalTimeSeconds,
            isToday = isToday,
            onPreviousClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              viewModel.goToPreviousDay()
            },
            onNextClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              viewModel.goToNextDay()
            },
            onTodayClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              viewModel.goToToday()
            }
          )
        }

        // 2. Active Event Subtasks for Today (if any)
        if (activeEventsWithSubtasksToday.isNotEmpty()) {
          item(key = "event_subtasks_section") {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              )
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Event Subtasks for Today",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))

                activeEventsWithSubtasksToday.forEach { event ->
                  val applicableSubtasks = event.getSubtasks().filter {
                    event.isSubtaskApplicableForDate(it, selectedDate)
                  }
                  if (applicableSubtasks.isNotEmpty()) {
                    val isExpanded = expandedEventIds.contains(event.id)
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                          expandedEventIds = if (isExpanded) expandedEventIds - event.id else expandedEventIds + event.id
                        }
                        .padding(vertical = 4.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        text = event.title,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                      IconButton(
                        onClick = {
                          expandedEventIds = if (isExpanded) expandedEventIds - event.id else expandedEventIds + event.id
                        },
                        modifier = Modifier.size(28.dp)
                      ) {
                        Icon(
                          imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                          contentDescription = if (isExpanded) "Collapse subtasks" else "Expand subtasks",
                          tint = MaterialTheme.colorScheme.primary,
                          modifier = Modifier.size(20.dp)
                        )
                      }
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                        text = "(${applicableSubtasks.count { it.isCompleted }}/${applicableSubtasks.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }

                    if (isExpanded) {
                      applicableSubtasks.forEach { subtask ->
                        Row(
                          modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                              viewModel.toggleEventSubtaskComplete(event, subtask.id)
                            }
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Checkbox(
                            checked = subtask.isCompleted,
                            onCheckedChange = {
                              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                              viewModel.toggleEventSubtaskComplete(event, subtask.id)
                            }
                          )
                          Spacer(modifier = Modifier.width(6.dp))
                          Text(
                            text = subtask.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                              textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (subtask.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                   else MaterialTheme.colorScheme.onSurface
                          )
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }

        // 2. Tasks List Header with counter and Default Tasks manager
        item(key = "tasks_header") {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f, fill = false)
            ) {
              Text(
                text = "Habits & Tasks",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer)
                  .padding(horizontal = 7.dp, vertical = 2.dp)
              ) {
                val completedCount = tasks.count { it.isCompleted }
                Text(
                  text = "$completedCount/${tasks.size}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                  )
                )
              }
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              // Goal Countdown display: If a goal exists, just show no of days in right side of habit and task
              if (goal != null) {
                val daysLeft = DateUtils.daysBetween(DateUtils.today(), goal!!.targetDate)
                val countdownLabel = if (daysLeft > 1L) "$daysLeft days" else if (daysLeft == 1L) "1 day" else if (daysLeft == 0L) "0 days" else "${-daysLeft} days ago"

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                    .clickable { showGoalDialog = true }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = countdownLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 11.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                  )
                }
              }

              OutlinedButton(
                onClick = { showAddDialog = true },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .defaultMinSize(minWidth = 1.dp, minHeight = 32.dp)
                  .testTag("add_task_text_button")
              ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Add", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }

        // 3. Tasks List Items: Active tasks + Expandable Completed Tasks section
        if (tasks.isEmpty()) {
          item(key = "empty_tasks") {
            EmptyTasksPlaceholder(
              onAddTaskClick = { showAddDialog = true }
            )
          }
        } else {
          // Active Tasks
          items(
            items = activeTasks,
            key = { it.task.id }
          ) { taskState ->
            TaskRowItem(
              taskUiState = taskState,
              onToggleTimer = { viewModel.toggleTimer(taskState.task.id) },
              onToggleComplete = { viewModel.toggleTaskComplete(taskState.task.id) },
              onEditTask = { taskToEdit = taskState.task },
              onDeleteTask = { viewModel.deleteTask(taskState.task.id) }
            )
          }

          // Completed Tasks in an expandable section with down arrow
          if (completedTasks.isNotEmpty()) {
            item(key = "completed_tasks_header") {
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isCompletedExpanded = !isCompletedExpanded
                  },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = Color.White.copy(alpha = 0.06f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  Color.White.copy(alpha = 0.12f)
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
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = null,
                      tint = com.example.ui.theme.RatingBestGreen,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "Completed Tasks",
                      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Text(
                        text = "${completedTasks.size}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurface
                      )
                    }
                  }

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = if (isCompletedExpanded) "Hide" else "Show",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    IconButton(
                      onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isCompletedExpanded = !isCompletedExpanded
                      },
                      modifier = Modifier.size(28.dp)
                    ) {
                      Icon(
                        imageVector = if (isCompletedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isCompletedExpanded) "Collapse completed tasks" else "Expand completed tasks",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                      )
                    }
                  }
                }
              }
            }

            if (isCompletedExpanded) {
              items(
                items = completedTasks,
                key = { it.task.id }
              ) { taskState ->
                TaskRowItem(
                  taskUiState = taskState,
                  onToggleTimer = { viewModel.toggleTimer(taskState.task.id) },
                  onToggleComplete = { viewModel.toggleTaskComplete(taskState.task.id) },
                  onEditTask = { taskToEdit = taskState.task },
                  onDeleteTask = { viewModel.deleteTask(taskState.task.id) }
                )
              }
            }
          }
        }
      }

      // Floating Actions: Hamburger Theme Menu + Add Task (+) Button
      Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(end = 20.dp, bottom = 16.dp)
      ) {
        // Small hamburger icon just above plus icon for Theme, Data, Presets
        SmallFloatingActionButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showHamburgerMenu = true
          },
          modifier = Modifier
            .padding(bottom = 12.dp)
            .testTag("fab_theme_menu"),
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
          shape = CircleShape
        ) {
          Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "App Settings and Menu",
            modifier = Modifier.size(20.dp)
          )
        }

        // Floating Add Task (+) Button
        FloatingActionButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showAddDialog = true
          },
          modifier = Modifier.testTag("fab_add_task"),
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          shape = CircleShape
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Task",
            modifier = Modifier.size(28.dp)
          )
        }
      }
    }

    // 4. Rate this day: small and bottom of UI without background box, just above tracker, plan, and analytics
    DayRatingSection(
      selectedRating = currentRating,
      dayAbbreviation = dayAbbr,
      fullDateLabel = fullDate,
      onRatingSelected = { rating ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        viewModel.setDayRating(rating)
      }
    )
  }

  // Add Task Dialog Sheet
  if (showAddDialog) {
    AddTaskDialog(
      selectedDate = selectedDate,
      onDismiss = { showAddDialog = false },
      onConfirm = { name, targetDates, repeatMask, targetMinutes, reminderTime, noteText, noteImageUri ->
        viewModel.addTask(
          name = name,
          targetDates = targetDates,
          repeatDaysMask = repeatMask,
          targetTimeMinutes = targetMinutes,
          isDefault = false,
          isStarred = false,
          noteText = noteText,
          noteImageUri = noteImageUri,
          reminderTime = reminderTime
        )
        showAddDialog = false
      }
    )
  }

  // Hamburger Menu Dialog: Theme (Font Color + Background Image), Data (Export/Import), Presets
  if (showHamburgerMenu) {
    HamburgerMenuDialog(
      viewModel = viewModel,
      onDismiss = { showHamburgerMenu = false }
    )
  }

  // Edit Task Dialog (Change Name, Frequency of Days, Target Timer, Default status, Text/Image notes)
  taskToEdit?.let { task ->
    EditTaskDialog(
      task = task,
      onDismiss = { taskToEdit = null },
      onConfirm = { updatedTask ->
        viewModel.updateTask(updatedTask)
        taskToEdit = null
      },
      onDelete = {
        viewModel.deleteTask(task.id)
        taskToEdit = null
      }
    )
  }

  // Presets Dialog (replaces Defaults)
  if (showPresetsDialog) {
    PresetsDialog(
      presets = presets,
      onDismiss = { showPresetsDialog = false },
      onAddPreset = { name -> viewModel.addPreset(name) },
      onDeletePreset = { id -> viewModel.deletePreset(id) },
      onSchedulePresetForDates = { preset, dates ->
        viewModel.schedulePresetForDates(preset, dates)
      },
      onAddPresetToDay = { preset ->
        viewModel.addTaskFromPreset(preset, selectedDate)
        // Keep dialog open so user can add multiple presets without having to reopen
      }
    )
  }

  // Goal Dialog
  if (showGoalDialog) {
    GoalDialog(
      currentGoal = goal,
      onSetGoal = { title, targetDate -> viewModel.setGoal(title, targetDate) },
      onClearGoal = { viewModel.clearGoal() },
      onDismiss = { showGoalDialog = false }
    )
  }
}

@Composable
private fun DayNavigationHeader(
  selectedDate: String,
  prevDayText: String,
  nextDayText: String,
  totalTimeSeconds: Long,
  isToday: Boolean,
  onPreviousClick: () -> Unit,
  onNextClick: () -> Unit,
  onTodayClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("top_day_nav_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = Color.Transparent
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Previous Day Button
        OutlinedButton(
          onClick = onPreviousClick,
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
          modifier = Modifier.testTag("btn_prev_day")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Previous Day",
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = prevDayText,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)
          )
        }

        // Center: Total Active Time Ticker
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(horizontal = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Schedule,
              contentDescription = null,
              modifier = Modifier.size(14.dp),
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Total Time",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }

          Text(
            text = DateUtils.formatTime(totalTimeSeconds),
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.primary
          )
        }

        // Next Day Button
        OutlinedButton(
          onClick = onNextClick,
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
          modifier = Modifier.testTag("btn_next_day")
        ) {
          Text(
            text = nextDayText,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Next Day",
            modifier = Modifier.size(16.dp)
          )
        }
      }

      if (!isToday) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
              .clickable { onTodayClick() }
              .padding(horizontal = 12.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Today,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Jump to Today",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun EmptyTasksPlaceholder(
  onAddTaskClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp),
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
        imageVector = Icons.Default.EventNote,
        contentDescription = null,
        modifier = Modifier.size(48.dp),
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "No tasks created yet",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Create your own habits and study goals with custom timers (or choose NEET presets).",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(16.dp))
      Button(
        onClick = onAddTaskClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Add First Task")
      }
    }
  }
}
