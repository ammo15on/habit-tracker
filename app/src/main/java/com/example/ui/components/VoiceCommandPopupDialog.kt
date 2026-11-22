package com.example.ui.components

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
  var inputQuery by remember { mutableStateOf("") }
  var spokenText by remember { mutableStateOf("Tap the microphone or type your command below...") }
  var executedActions by remember { mutableStateOf<List<ExecutedVoiceAction>>(emptyList()) }
  var lastActionSummary by remember { mutableStateOf<String?>(null) }

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

    var successMessage: String? = null
    var undoAction: (() -> Unit)? = null

    if (text.contains("task") || text.contains("create task") || text.contains("add task")) {
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
      undoAction = {}
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
      undoAction = {
        viewModel.deleteNote(note)
      }
    } else if (text.contains("mark") || text.contains("done") || text.contains("undone")) {
      val isDone = !text.contains("undone")
      val type = when {
        text.contains("pyq") -> "PYQ"
        text.contains("exercise") -> "Exercise"
        text.contains("ncert") -> "NCERT"
        text.contains("dpp") -> "DPP"
        else -> "Chapter"
      }

      val chapterQuery = text.substringAfter("for").substringAfter("in").trim()
      val matchedChapter = chapters.find { it.name.lowercase().contains(chapterQuery) }

      if (matchedChapter != null) {
        val updated = when (type) {
          "PYQ" -> matchedChapter.copy(isPyqDone = isDone)
          "Exercise" -> matchedChapter.copy(isExerciseDone = isDone)
          "NCERT" -> matchedChapter.copy(ncertReadCount = if (isDone) 1 else 0)
          else -> matchedChapter.copy(isCompleted = isDone)
        }
        viewModel.updateNeetChapter(updated)
        successMessage = "Marked $type ${if (isDone) "done" else "undone"} for ${matchedChapter.name}"
        undoAction = {
          viewModel.updateNeetChapter(matchedChapter)
        }
      } else {
        successMessage = "Processed: $rawText"
      }
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
      }
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = {
                try {
                  val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your command...")
                  }
                  speechLauncher.launch(intent)
                } catch (e: Exception) {
                  spokenText = "Speech recognition unavailable on device. Please type below."
                }
              },
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Tap to speak",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Voice & Text Assistant",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
              RoundedCornerShape(12.dp)
            )
            .padding(14.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = spokenText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Text input field for typing commands directly
        OutlinedTextField(
          value = inputQuery,
          onValueChange = { inputQuery = it },
          placeholder = { Text("e.g. create task on 3 January named physics practice") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          trailingIcon = {
            IconButton(onClick = { executeCommand(inputQuery) }) {
              Icon(Icons.Default.Send, contentDescription = "Send Command")
            }
          },
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (executedActions.isNotEmpty()) {
          Text(
            text = "Actions Done:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Start)
          )
          Spacer(modifier = Modifier.height(4.dp))
          executedActions.forEach { action ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = "• ${action.description}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
              TextButton(onClick = {
                action.undo?.invoke()
                executedActions = executedActions.filter { it != action }
                lastActionSummary = "Undone: ${action.description}"
              }) {
                Text("Undo", fontSize = 12.sp)
              }
            }
          }
        }

        if (lastActionSummary != null) {
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = lastActionSummary!!,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick sample chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Button(
            onClick = {
              executeCommand("create task on 3 January named Physics Practice")
            },
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(4.dp)
          ) {
            Text("Task on 3 Jan", fontSize = 10.sp)
          }

          Button(
            onClick = {
              executeCommand("create a note in Morphology to remember floral formula")
            },
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(4.dp)
          ) {
            Text("Note in Chapter", fontSize = 10.sp)
          }

          Button(
            onClick = {
              executeCommand("mark pyq done for Cell")
            },
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(4.dp)
          ) {
            Text("Mark PYQ", fontSize = 10.sp)
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
