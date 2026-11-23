package com.example.ui.components

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.NoteEntity
import com.example.ui.HabitViewModel
import com.example.util.DateUtils
import kotlinx.coroutines.delay

@Composable
fun VoiceCommandPopupDialog(
  viewModel: HabitViewModel,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val chapters by viewModel.allNeetChapters.collectAsStateWithLifecycle()
  val tasks by viewModel.tasksForSelectedDate.collectAsStateWithLifecycle()
  val requireConfirmation by viewModel.requireConfirmationForDeletion.collectAsStateWithLifecycle()
  val totalTimeSeconds by viewModel.totalTimeTodaySeconds.collectAsStateWithLifecycle()

  var inputQuery by remember { mutableStateOf("") }
  var spokenText by remember { mutableStateOf("Listening for voice command...") }
  var isListening by remember { mutableStateOf(true) }
  var executedActions by remember { mutableStateOf<List<ExecutedVoiceAction>>(emptyList()) }
  var lastActionSummary by remember { mutableStateOf<String?>(null) }
  var pendingDeletionTaskName by remember { mutableStateOf<String?>(null) }

  // Pulsing animation for listening mic icon
  val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.2f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  fun parseDate(text: String): String {
    val lower = text.lowercase()
    val months = listOf(
      "january" to "01", "february" to "02", "march" to "03", "april" to "04",
      "may" to "05", "june" to "06", "july" to "07", "august" to "08",
      "september" to "09", "october" to "10", "november" to "11", "december" to "12",
      "jan" to "01", "feb" to "02", "mar" to "03", "apr" to "04", "jun" to "06",
      "jul" to "07", "aug" to "08", "sep" to "09", "oct" to "10", "nov" to "11", "dec" to "12"
    )

    for ((monthName, monthNum) in months) {
      if (lower.contains(monthName)) {
        val regex = Regex("(\\d{1,2})(?:st|nd|rd|th)?\\s*(?:of)?\\s*$monthName|$monthName\\s*(\\d{1,2})(?:st|nd|rd|th)?")
        val match = regex.find(lower)
        if (match != null) {
          val dayStr = (match.groups[1]?.value ?: match.groups[2]?.value)?.padStart(2, '0')
          if (dayStr != null) {
            return "2026-$monthNum-$dayStr"
          }
        }
        return "2026-$monthNum-03"
      }
    }
    if (lower.contains("tomorrow")) return DateUtils.getNextDay(DateUtils.today())
    return DateUtils.today()
  }

  fun executeCommand(rawText: String) {
    val text = rawText.trim().lowercase()
    if (text.isBlank()) return

    isListening = false
    var successMessage: String? = null
    var undoAction: (() -> Unit)? = null

    if (pendingDeletionTaskName != null) {
      if (text.contains("yes") || text.contains("confirm") || text.contains("ok")) {
        val taskToDelete = tasks.find { it.task.name.lowercase().contains(pendingDeletionTaskName!!) }
        if (taskToDelete != null) {
          viewModel.deleteTask(taskToDelete.task.id)
          successMessage = "Deleted task '${taskToDelete.task.name}'"
        } else {
          successMessage = "Task not found for deletion."
        }
      } else {
        successMessage = "Deletion cancelled."
      }
      pendingDeletionTaskName = null
      if (successMessage != null) {
        executedActions = executedActions + ExecutedVoiceAction(successMessage!!, null)
        lastActionSummary = "Executed: $successMessage"
        inputQuery = ""
      }
      return
    }

    if (text.contains("start timer") || text.contains("begin timer")) {
      val targetTaskName = text.substringAfter("timer for").substringAfter("timer").trim()
      val matchedTask = tasks.find { it.task.name.lowercase().contains(targetTaskName) }
      if (matchedTask != null) {
        viewModel.toggleTimer(matchedTask.task.id)
        successMessage = "Started timer for '${matchedTask.task.name}'"
      } else if (tasks.isNotEmpty()) {
        viewModel.toggleTimer(tasks.first().task.id)
        successMessage = "Started timer for '${tasks.first().task.name}'"
      } else {
        successMessage = "No active tasks found to start timer."
      }
    } else if (text.contains("pause timer")) {
      viewModel.pauseTimer()
      successMessage = "Paused active timer."
    } else if (text.contains("resume timer")) {
      viewModel.resumeTimer()
      successMessage = "Resumed active timer."
    } else if (text.contains("stop timer") || text.contains("finish timer")) {
      viewModel.stopActiveTimer()
      successMessage = "Stopped and saved active timer."
    } else if (text.contains("task") || text.contains("create task") || text.contains("add task")) {
      val name = if (text.contains("named")) {
        text.substringAfter("named").substringBefore(" on ").trim()
      } else {
        text.substringAfter("task").substringBefore(" on ").trim().ifBlank { "Study Task" }
      }
      val targetDate = parseDate(text)
      viewModel.addTask(
        name = name.replaceFirstChar { it.uppercase() },
        targetDates = setOf(targetDate),
        targetTimeMinutes = 30
      )
      successMessage = "Created task '${name.replaceFirstChar { it.uppercase() }}' for $targetDate"
    } else if (text.contains("note") || text.contains("add something") || text.contains("create a note")) {
      val chapterPart = text.substringAfter("in").substringBefore(" to ").substringBefore("remember").trim()
      val contentPart = if (text.contains("to ")) text.substringAfter("to ").trim()
                        else if (text.contains("remember ")) text.substringAfter("remember ").trim()
                        else "Note from Voice Assistant"

      val matchedChapter = chapters.find { it.name.lowercase().contains(chapterPart) }
      val subjectName = matchedChapter?.subject ?: "General"
      val chapterNameStr = matchedChapter?.name ?: chapterPart.replaceFirstChar { it.uppercase() }

      val note = NoteEntity(
        title = "Voice Note: $chapterNameStr",
        content = contentPart.replaceFirstChar { it.uppercase() },
        subject = subjectName,
        chapterName = chapterNameStr,
        chapterId = matchedChapter?.id
      )
      viewModel.saveNote(note)
      successMessage = "Added note in '$chapterNameStr': $contentPart"
      undoAction = { viewModel.deleteNote(note) }
    } else if (text.contains("mark") || text.contains("done") || text.contains("undone")) {
      val isDone = !text.contains("undone")
      val type = when {
        text.contains("pyq") -> "PYQ"
        text.contains("exercise") -> "Exercise"
        text.contains("assertion") || text.contains("a&r") -> "Assertion-Reason"
        text.contains("dpp") -> "DPP"
        text.contains("ncert") -> "NCERT"
        else -> "Chapter"
      }

      val chapterQuery = text.substringAfter("for").substringAfter("in").trim()
      val matchedChapter = chapters.find { it.name.lowercase().contains(chapterQuery) }

      if (matchedChapter != null) {
        val updated = when (type) {
          "PYQ" -> matchedChapter.copy(isPyqDone = isDone)
          "Exercise" -> matchedChapter.copy(isExerciseDone = isDone)
          "Assertion-Reason" -> matchedChapter.copy(isArDone = isDone)
          "NCERT" -> matchedChapter.copy(ncertReadCount = if (isDone) 1 else 0)
          else -> matchedChapter.copy(isCompleted = isDone)
        }
        viewModel.updateNeetChapter(updated)
        successMessage = "Marked $type ${if (isDone) "done" else "undone"} for ${matchedChapter.name}"
        undoAction = { viewModel.updateNeetChapter(matchedChapter) }
      } else {
        successMessage = "Matched chapter not found for: $chapterQuery"
      }
    } else if (text.contains("delete task") || text.contains("remove task")) {
      val taskNameQuery = text.substringAfter("delete task").substringAfter("remove task").trim()
      val matchedTask = tasks.find { it.task.name.lowercase().contains(taskNameQuery) }
      if (matchedTask != null) {
        if (requireConfirmation) {
          pendingDeletionTaskName = taskNameQuery
          successMessage = "Are you sure you want to delete '${matchedTask.task.name}'? Say 'Yes' to confirm."
        } else {
          viewModel.deleteTask(matchedTask.task.id)
          successMessage = "Deleted task '${matchedTask.task.name}'"
        }
      } else {
        successMessage = "Task not found: $taskNameQuery"
      }
    } else if (text.contains("progress") || text.contains("today's progress")) {
      val completed = tasks.count { it.isCompleted }
      val total = tasks.size
      successMessage = "Today's Progress: $completed / $total tasks completed."
    } else if (text.contains("pending") || text.contains("todo")) {
      val pending = tasks.filter { !it.isCompleted }.map { it.task.name }
      successMessage = "Pending tasks (${pending.size}): ${pending.take(3).joinToString(", ")}"
    } else if (text.contains("study time") || text.contains("time studied")) {
      val mins = totalTimeSeconds / 60
      successMessage = "Total study time today: $mins minutes."
    } else {
      val date = parseDate(text)
      viewModel.addTask(rawText.replaceFirstChar { it.uppercase() }, targetDates = setOf(date))
      successMessage = "Created task: ${rawText} for $date"
    }

    if (successMessage != null) {
      executedActions = executedActions + ExecutedVoiceAction(successMessage!!, undoAction)
      lastActionSummary = "Executed: $successMessage"
      inputQuery = ""
    }
  }

  val speechLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    if (result.resultCode == android.app.Activity.RESULT_OK) {
      val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
      if (!matches.isNullOrEmpty()) {
        val spoken = matches[0]
        spokenText = "Heard: \"$spoken\""
        executeCommand(spoken)
      } else {
        spokenText = "No speech detected. Type or tap mic."
        isListening = false
      }
    } else {
      spokenText = "Listening cancelled. Type or tap mic."
      isListening = false
    }
  }

  // Automatically trigger voice listening immediately upon opening dialog!
  LaunchedEffect(Unit) {
    try {
      val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening for your voice command...")
      }
      speechLauncher.launch(intent)
    } catch (e: Exception) {
      spokenText = "Speech recognition unavailable. Type your command below."
      isListening = false
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 12.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                MaterialTheme.colorScheme.surface
              )
            )
          )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Top Bar
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .scale(if (isListening) pulseScale else 1f)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Mic,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Neural Voice Assistant",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = if (isListening) "Listening now..." else "Ready",
                  style = MaterialTheme.typography.bodySmall,
                  color = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Close")
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Live Transcript Box
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                RoundedCornerShape(14.dp)
              )
              .padding(16.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = spokenText,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Modern Text Input
          OutlinedTextField(
            value = inputQuery,
            onValueChange = { inputQuery = it },
            placeholder = { Text("Type command (e.g. Semiconductor task)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
              IconButton(onClick = { executeCommand(inputQuery) }) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
              }
            },
            shape = RoundedCornerShape(14.dp)
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Executed Actions List
          if (executedActions.isNotEmpty()) {
            Text(
              text = "Actions Completed:",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(6.dp))
            executedActions.forEach { action ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                  .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = "✓ ${action.description}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                if (action.undo != null) {
                  TextButton(onClick = {
                    action.undo?.invoke()
                    executedActions = executedActions.filter { it != action }
                    lastActionSummary = "Undone: ${action.description}"
                  }) {
                    Text("Undo", fontSize = 11.sp)
                  }
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
            }
          }

          if (lastActionSummary != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = lastActionSummary!!,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Quick Action Suggestion Chips
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Button(
              onClick = {
                try {
                  isListening = true
                  val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak command...")
                  }
                  speechLauncher.launch(intent)
                } catch (_: Exception) {}
              },
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(2.dp)
            ) {
              Text("Listen Again", fontSize = 10.sp)
            }
            Button(
              onClick = { executeCommand("Create task for today named Semiconductor") },
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(2.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
              Text("Semiconductor", fontSize = 10.sp)
            }
            Button(
              onClick = { executeCommand("Check today's progress") },
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(2.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
              Text("Progress", fontSize = 10.sp)
            }
          }
        }
      }
    }
  }
}

data class ExecutedVoiceAction(
  val description: String,
  val undo: (() -> Unit)? = null
)
