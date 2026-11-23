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
import com.example.data.model.HabitTask
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
  val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
  val tasksForDate by viewModel.tasksForSelectedDate.collectAsStateWithLifecycle()
  val requireConfirmation by viewModel.requireConfirmationForDeletion.collectAsStateWithLifecycle()
  val totalTimeSeconds by viewModel.totalTimeTodaySeconds.collectAsStateWithLifecycle()

  var inputQuery by remember { mutableStateOf("") }
  var spokenText by remember { mutableStateOf("Listening for voice command...") }
  var isListening by remember { mutableStateOf(true) }
  var executedActions by remember { mutableStateOf<List<ExecutedVoiceAction>>(emptyList()) }
  var lastActionSummary by remember { mutableStateOf<String?>(null) }
  var pendingDeletionTaskId by remember { mutableStateOf<Long?>(null) }
  var pendingDeletionTaskTitle by remember { mutableStateOf<String?>(null) }

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

    // 1. Confirmation handling for destructive actions
    if (pendingDeletionTaskId != null) {
      if (text.contains("yes") || text.contains("confirm") || text.contains("ok") || text.contains("sure")) {
        val taskId = pendingDeletionTaskId!!
        val title = pendingDeletionTaskTitle ?: "Task"
        viewModel.deleteTask(taskId)
        successMessage = "Deleted task '$title'"
      } else {
        successMessage = "Deletion cancelled."
      }
      pendingDeletionTaskId = null
      pendingDeletionTaskTitle = null
      if (successMessage != null) {
        executedActions = executedActions + ExecutedVoiceAction(successMessage, null)
        lastActionSummary = "Executed: $successMessage"
        inputQuery = ""
      }
      return
    }

    // 2. Task Timer Commands
    if (text.contains("start timer") || text.contains("begin timer") || text.contains("start study")) {
      val targetTaskName = text.substringAfter("timer for").substringAfter("timer").substringAfter("study for").trim()
      val matchedTask = tasksForDate.find { it.task.name.lowercase().contains(targetTaskName) }
        ?: allTasks.find { it.name.lowercase().contains(targetTaskName) }
      if (matchedTask != null) {
        val taskId = if (matchedTask is com.example.ui.TaskItemUiState) matchedTask.task.id else (matchedTask as HabitTask).id
        val taskName = if (matchedTask is com.example.ui.TaskItemUiState) matchedTask.task.name else (matchedTask as HabitTask).name
        viewModel.toggleTimer(taskId)
        successMessage = "Started timer for '$taskName'"
      } else if (tasksForDate.isNotEmpty()) {
        viewModel.toggleTimer(tasksForDate.first().task.id)
        successMessage = "Started timer for '${tasksForDate.first().task.name}'"
      } else {
        successMessage = "No matching task found to start timer."
      }
    } else if (text.contains("pause timer")) {
      viewModel.pauseTimer()
      successMessage = "Paused active task timer."
    } else if (text.contains("resume timer")) {
      viewModel.resumeTimer()
      successMessage = "Resumed active task timer."
    } else if (text.contains("stop timer") || text.contains("finish timer")) {
      viewModel.stopActiveTimer()
      successMessage = "Stopped and saved task timer session."
    }
    // 3. Create Tasks
    else if (text.contains("task") || text.contains("create task") || text.contains("add task")) {
      val name = if (text.contains("named")) {
        text.substringAfter("named").substringBefore(" on ").substringBefore(" for ").trim()
      } else if (text.contains("for today")) {
        text.substringAfter("for today").substringAfter("named").trim()
      } else {
        text.substringAfter("task").substringBefore(" on ").substringBefore(" for ").trim().ifBlank { "Study Task" }
      }
      val targetDate = parseDate(text)
      viewModel.addTask(
        name = name.replaceFirstChar { it.uppercase() },
        targetDates = setOf(targetDate),
        targetTimeMinutes = 30
      )
      successMessage = "Created task '${name.replaceFirstChar { it.uppercase() }}' for $targetDate"
    }
    // 4. Create Notes
    else if (text.contains("note") || text.contains("add something") || text.contains("create a note")) {
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
    }
    // 5. Mark Complete / Undone (Chapters, Subjects, PYQ, Exercise, Assertion-Reason, DPP, NCERT)
    else if (text.contains("mark") || text.contains("complete") || text.contains("done") || text.contains("undone") || text.contains("uncomplete")) {
      val isDone = !text.contains("undone") && !text.contains("uncomplete")
      val type = when {
        text.contains("pyq") -> "PYQ"
        text.contains("exercise") -> "Exercise"
        text.contains("assertion") || text.contains("a&r") || text.contains("reason") -> "Assertion-Reason"
        text.contains("dpp") -> "DPP"
        text.contains("ncert") -> "NCERT"
        else -> "Chapter"
      }

      val chapterQuery = text.substringAfter("for").substringAfter("in").substringAfter("chapter").trim()
      val matchedChapter = chapters.find { it.name.lowercase().contains(chapterQuery) || chapterQuery.contains(it.name.lowercase()) }

      if (matchedChapter != null) {
        val updated = when (type) {
          "PYQ" -> matchedChapter.copy(isPyqDone = isDone)
          "Exercise" -> matchedChapter.copy(isExerciseDone = isDone)
          "Assertion-Reason" -> matchedChapter.copy(isArDone = isDone)
          "NCERT" -> matchedChapter.copy(ncertReadCount = if (isDone) 1 else 0)
          else -> matchedChapter.copy(isCompleted = isDone)
        }
        viewModel.updateNeetChapter(updated)
        successMessage = "Marked $type ${if (isDone) "completed" else "undone"} for ${matchedChapter.name}"
        undoAction = { viewModel.updateNeetChapter(matchedChapter) }
      } else {
        successMessage = "Chapter matching '$chapterQuery' not found. Please specify exact chapter name."
      }
    }
    // 6. Delete Task
    else if (text.contains("delete task") || text.contains("remove task") || text.contains("delete")) {
      val taskNameQuery = text.substringAfter("delete task").substringAfter("remove task").substringAfter("delete").trim()
      val matchedTask = tasksForDate.find { it.task.name.lowercase().contains(taskNameQuery) }
        ?: allTasks.find { it.name.lowercase().contains(taskNameQuery) }

      if (matchedTask != null) {
        val taskId = if (matchedTask is com.example.ui.TaskItemUiState) matchedTask.task.id else (matchedTask as HabitTask).id
        val taskTitle = if (matchedTask is com.example.ui.TaskItemUiState) matchedTask.task.name else (matchedTask as HabitTask).name

        if (requireConfirmation) {
          pendingDeletionTaskId = taskId
          pendingDeletionTaskTitle = taskTitle
          successMessage = "Delete '$taskTitle'? Say 'Yes' to confirm."
        } else {
          viewModel.deleteTask(taskId)
          successMessage = "Deleted task '$taskTitle'"
        }
      } else {
        successMessage = "Task matching '$taskNameQuery' not found."
      }
    }
    // 7. Smart Commands & Detailed Voice Progress Stats
    else if (text.contains("progress") || text.contains("status") || text.contains("summary") || text.contains("how am i doing")) {
      val completedTasks = tasksForDate.count { it.isCompleted }
      val totalTasks = tasksForDate.size
      val totalChaptersCount = chapters.size
      val completedChapters = chapters.count { it.isCompleted }
      val pyqDone = chapters.count { it.isPyqDone }
      val arDone = chapters.count { it.isArDone }
      val exerciseDone = chapters.count { it.isExerciseDone }

      successMessage = "Progress: Tasks $completedTasks/$totalTasks | Chapters $completedChapters/$totalChaptersCount | PYQ $pyqDone | A&R $arDone | Ex $exerciseDone"
    } else if (text.contains("pending") || text.contains("todo")) {
      val pending = tasksForDate.filter { !it.isCompleted }.map { it.task.name }
      successMessage = "Pending tasks (${pending.size}): ${if (pending.isEmpty()) "None! All done." else pending.joinToString(", ")}"
    } else if (text.contains("study time") || text.contains("time studied")) {
      val mins = totalTimeSeconds / 60
      successMessage = "Total study time recorded today: $mins minutes."
    } else {
      val date = parseDate(text)
      viewModel.addTask(rawText.replaceFirstChar { it.uppercase() }, targetDates = setOf(date))
      successMessage = "Created task: '${rawText.replaceFirstChar { it.uppercase() }}' for $date"
    }

    if (successMessage != null) {
      executedActions = executedActions + ExecutedVoiceAction(successMessage, undoAction)
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
            placeholder = { Text("Type command (e.g. create task for today named Physics)") },
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
              onClick = { executeCommand("Check today's progress") },
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(2.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
              Text("Progress", fontSize = 10.sp)
            }
            Button(
              onClick = { executeCommand("View pending tasks") },
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(2.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
              Text("Pending", fontSize = 10.sp)
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
