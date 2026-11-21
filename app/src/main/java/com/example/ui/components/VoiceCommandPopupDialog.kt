package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.HabitViewModel
import kotlinx.coroutines.delay

@Composable
fun VoiceCommandPopupDialog(
  viewModel: HabitViewModel,
  onDismiss: () -> Unit
) {
  var spokenText by remember { mutableStateOf("Listening for commands...") }
  var executedActions by remember { mutableStateOf<List<String>>(emptyList()) }
  var lastActionSummary by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    delay(1000)
    spokenText = "Example: Create task on 3 January named physics practice"
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
            Icon(
              imageVector = Icons.Default.Mic,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Voice Assistant",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
              RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = spokenText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
              Text(text = "• $action", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
              TextButton(onClick = {
                executedActions = executedActions.filter { it != action }
                lastActionSummary = "Undone: $action"
              }) {
                Text("Undo", fontSize = 12.sp)
              }
            }
          }
        }

        if (lastActionSummary != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = lastActionSummary!!,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = {
              val sample = "Created task: Physics Practice for Jan 3"
              executedActions = executedActions + sample
              lastActionSummary = null
              viewModel.addTask("Physics Practice", targetDates = setOf("2026-01-03"), targetTimeMinutes = 30)
            },
            modifier = Modifier.weight(1f)
          ) {
            Text("Simulate Task")
          }

          Button(
            onClick = {
              val sample = "Added note in Morphology: Floral formula"
              executedActions = executedActions + sample
              lastActionSummary = null
            },
            modifier = Modifier.weight(1f)
          ) {
            Text("Simulate Note")
          }
        }
      }
    }
  }
}
