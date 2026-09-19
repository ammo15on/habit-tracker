package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.RatingType
import com.example.ui.HabitViewModel
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.DayRatingSection
import com.example.ui.components.TaskRowItem
import com.example.ui.theme.RatingBestGreen
import com.example.util.DateUtils

@Composable
fun TrackerScreen(
  viewModel: HabitViewModel,
  modifier: Modifier = Modifier
) {
  val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
  val tasks by viewModel.tasksForSelectedDate.collectAsStateWithLifecycle()
  val currentRating by viewModel.currentDayRating.collectAsStateWithLifecycle()
  val totalTimeSeconds by viewModel.totalTimeTodaySeconds.collectAsStateWithLifecycle()

  var showAddDialog by remember { mutableStateOf(false) }

  val isToday = (selectedDate == DateUtils.today())
  val prevDay = DateUtils.getPreviousDay(selectedDate)
  val nextDay = DateUtils.getNextDay(selectedDate)

  val prevDayFormatted = DateUtils.formatShortDate(prevDay)
  val nextDayFormatted = DateUtils.formatShortDate(nextDay)
  val dayAbbr = DateUtils.formatDayOfWeekAbbr(selectedDate)
  val fullDate = DateUtils.formatFullDate(selectedDate)

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Top Bar Header matching sketch: (X) Previous Day  |  (Total Time)  |  (Y) Next Day
      item(key = "top_day_nav") {
        DayNavigationHeader(
          selectedDate = selectedDate,
          prevDayText = prevDayFormatted,
          nextDayText = nextDayFormatted,
          totalTimeSeconds = totalTimeSeconds,
          isToday = isToday,
          onPreviousClick = { viewModel.goToPreviousDay() },
          onNextClick = { viewModel.goToNextDay() },
          onTodayClick = { viewModel.goToToday() }
        )
      }

      // 2. Day Rating Section: Box [A] Best, Box [B] Average, Box [C] Worst, with "Mo" (Day Abbr)
      item(key = "day_rating_section") {
        DayRatingSection(
          selectedRating = currentRating,
          dayAbbreviation = dayAbbr,
          fullDateLabel = fullDate,
          onRatingSelected = { rating ->
            viewModel.setDayRating(rating)
          }
        )
      }

      // 3. Tasks List Header
      item(key = "tasks_header") {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Habits & Tasks",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
              ),
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              val completedCount = tasks.count { it.isCompleted }
              Text(
                text = "$completedCount / ${tasks.size}",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              )
            }
          }

          TextButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.testTag("add_task_text_button")
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Habit")
          }
        }
      }

      // 4. Tasks List Items (Task-1 time spent, pause/resume, complete check)
      if (tasks.isEmpty()) {
        item(key = "empty_tasks") {
          EmptyTasksPlaceholder(
            onAddTaskClick = { showAddDialog = true }
          )
        }
      } else {
        items(
          items = tasks,
          key = { it.task.id }
        ) { taskState ->
          TaskRowItem(
            taskUiState = taskState,
            onToggleTimer = { viewModel.toggleTimer(taskState.task.id) },
            onToggleComplete = { viewModel.toggleTaskComplete(taskState.task.id) },
            onDeleteTask = { viewModel.deleteTask(taskState.task.id) }
          )
        }
      }
    }

    // 5. Sketched Add Task (+) Button
    FloatingActionButton(
      onClick = { showAddDialog = true },
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 20.dp, bottom = 20.dp)
        .testTag("fab_add_task"),
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

    // Add Task Dialog Sheet
    if (showAddDialog) {
      AddTaskDialog(
        onDismiss = { showAddDialog = false },
        onConfirm = { name, repeatMask ->
          viewModel.addTask(name, repeatMask)
          showAddDialog = false
        }
      )
    }
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
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
      // Row with (X) Previous Day  |  (Total Time)  |  (Y) Next Day
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // (X) Button: Previous Day with that day date
        FilledTonalButton(
          onClick = onPreviousClick,
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.testTag("button_prev_day")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Previous Day",
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = prevDayText,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
          )
        }

        // Center: (Total time) spent
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.testTag("total_time_display")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
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
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
          Text(
            text = DateUtils.formatTime(totalTimeSeconds),
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              fontSize = 17.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
          )
        }

        // (Y) Button: Next Day with that day date
        FilledTonalButton(
          onClick = onNextClick,
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.testTag("button_next_day")
        ) {
          Text(
            text = nextDayText,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Next Day",
            modifier = Modifier.size(16.dp)
          )
        }
      }

      // Date subtitle bar with "Today" indicator
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = DateUtils.formatFullDate(selectedDate),
          style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )

        if (!isToday) {
          TextButton(
            onClick = onTodayClick,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.testTag("button_return_today")
          ) {
            Icon(
              imageVector = Icons.Default.Today,
              contentDescription = null,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Go to Today", style = MaterialTheme.typography.labelSmall)
          }
        } else {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(RatingBestGreen.copy(alpha = 0.15f))
              .padding(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text(
              text = "Today",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = RatingBestGreen
              )
            )
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
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = Icons.Default.Schedule,
        contentDescription = null,
        modifier = Modifier.size(44.dp),
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "No habits scheduled for this day",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Add tasks that repeat everyday or on this specific day of the week.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(14.dp))
      ElevatedButton(
        onClick = onAddTaskClick,
        modifier = Modifier.testTag("empty_add_task_button")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Create First Task")
      }
    }
  }
}
