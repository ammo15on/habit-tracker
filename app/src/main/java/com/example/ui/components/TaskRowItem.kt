package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ui.TaskItemUiState
import com.example.ui.theme.RatingBestGreen
import com.example.util.DateUtils

@Composable
fun TaskRowItem(
  taskUiState: TaskItemUiState,
  onToggleTimer: () -> Unit,
  onToggleComplete: () -> Unit,
  onEditTask: () -> Unit,
  onDeleteTask: () -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  val task = taskUiState.task
  val isRunning = taskUiState.isRunning
  val isCompleted = taskUiState.isCompleted
  val targetTimeSeconds = task.targetTimeMinutes * 60L
  val hasTarget = task.targetTimeMinutes > 0
  val isExceeded = hasTarget && (taskUiState.timeSpentSeconds > targetTimeSeconds)
  val excessSeconds = if (isExceeded) taskUiState.timeSpentSeconds - targetTimeSeconds else 0L

  var showMenu by remember { mutableStateOf(false) }
  var showImageZoom by remember { mutableStateOf(false) }

  val cardBgColor by animateColorAsState(
    targetValue = when {
      isRunning -> RatingBestGreen.copy(alpha = 0.12f)
      isCompleted -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
      else -> MaterialTheme.colorScheme.surface
    },
    label = "cardBgColor"
  )

  val borderColor = when {
    isExceeded && isRunning -> Color(0xFFE11D48)
    isRunning -> RatingBestGreen.copy(alpha = 0.8f)
    isCompleted -> RatingBestGreen.copy(alpha = 0.3f)
    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("task_card_${task.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = cardBgColor),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isRunning) 3.dp else 1.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .border(
          width = if (isRunning) 1.5.dp else 1.dp,
          color = borderColor,
          shape = RoundedCornerShape(16.dp)
        )
        .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Left Column: Task Name & Time Spent + Target Indicator
          Column(
            modifier = Modifier
              .weight(1f)
              .padding(end = 8.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              if (task.isDefault) {
                Icon(
                  imageVector = Icons.Default.Star,
                  contentDescription = "Default Task",
                  tint = Color(0xFFF59E0B),
                  modifier = Modifier
                    .size(16.dp)
                    .padding(end = 2.dp)
                )
              }

              Text(
                text = task.name,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 16.sp,
                  textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                ),
                color = if (isCompleted) {
                  MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                } else {
                  MaterialTheme.colorScheme.onSurface
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
              )

              if (hasTarget) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "Goal: ${task.targetTimeMinutes}m",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Time spent ticker & Excess time in RED if exceeded
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(
                    if (isRunning) RatingBestGreen.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                  )
                  .padding(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text(
                  text = DateUtils.formatTime(taskUiState.timeSpentSeconds),
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                  ),
                  color = if (isRunning) RatingBestGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              // If taking more time than timer, show excess time in RED
              if (isExceeded) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFFE4E6))
                    .border(1.dp, Color(0xFFF43F5E), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Warning,
                      contentDescription = null,
                      tint = Color(0xFFE11D48),
                      modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                      text = "+${DateUtils.formatTime(excessSeconds)} excess",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFFBE123C)
                      )
                    )
                  }
                }
              }

              if (isRunning) {
                Text(
                  text = "Tracking...",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  ),
                  color = RatingBestGreen
                )
              }
            }
          }

          // Right side: Action Buttons (Pause/Resume + Complete checkmark + Edit/Delete menu)
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            // Pause / Resume Timer Button
            FilledIconButton(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleTimer()
              },
              modifier = Modifier
                .size(42.dp)
                .testTag("toggle_timer_${task.id}"),
              colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (isRunning) {
                  RatingBestGreen
                } else {
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                },
                contentColor = if (isRunning) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
              )
            ) {
              Icon(
                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isRunning) "Pause Timer" else "Resume Timer",
                modifier = Modifier.size(22.dp)
              )
            }

            // Complete Checkmark Button
            FilledIconButton(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleComplete()
              },
              modifier = Modifier
                .size(42.dp)
                .testTag("toggle_complete_${task.id}"),
              colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (isCompleted) {
                  RatingBestGreen
                } else {
                  MaterialTheme.colorScheme.surfaceVariant
                },
                contentColor = if (isCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            ) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Mark Task Complete",
                modifier = Modifier.size(20.dp)
              )
            }

            // More options (Edit task, Delete)
            Box {
              IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.MoreVert,
                  contentDescription = "More Options",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
              ) {
                DropdownMenuItem(
                  text = { Text("Edit Task & Notes") },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.Edit,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary
                    )
                  },
                  onClick = {
                    showMenu = false
                    onEditTask()
                  }
                )
                DropdownMenuItem(
                  text = { Text("Delete Habit") },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.error
                    )
                  },
                  onClick = {
                    showMenu = false
                    onDeleteTask()
                  }
                )
              }
            }
          }
        }

        // Note section: Text Note and/or Image Note thumbnail
        if (task.noteText.isNotBlank() || task.noteImageUri != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            if (task.noteText.isNotBlank()) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
              ) {
                Icon(
                  imageVector = Icons.Default.Notes,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = task.noteText,
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            if (task.noteImageUri != null) {
              Spacer(modifier = Modifier.width(8.dp))
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                  .clickable { showImageZoom = true }
              ) {
                AsyncImage(
                  model = task.noteImageUri,
                  contentDescription = "Thumbnail note image",
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.matchParentSize()
                )
              }
            }
          }
        }
      }
    }
  }

  // Zoomed Image Note Dialog (Full Screen)
  if (showImageZoom && task.noteImageUri != null) {
    FullScreenImageViewerDialog(
      imageUri = task.noteImageUri,
      onDismiss = { showImageZoom = false }
    )
  }
}
