package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NeetTallyCounter
import com.example.ui.theme.RatingBestGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditTallyDialog(
  counter: NeetTallyCounter? = null,
  onDismiss: () -> Unit,
  onSave: (title: String, initialCount: Int, target: Int, unit: String) -> Unit,
  onDelete: (() -> Unit)? = null,
  onReset: (() -> Unit)? = null
) {
  val isEditing = counter != null
  var title by remember { mutableStateOf(counter?.title ?: "") }
  var countStr by remember { mutableStateOf(counter?.count?.toString() ?: "0") }
  var targetStr by remember { mutableStateOf(if ((counter?.target ?: 0) > 0) counter!!.target.toString() else "") }
  var unit by remember { mutableStateOf(counter?.unit ?: "questions") }

  val presets = listOf(
    "bot ncert read" to "chapters",
    "bot q" to "questions",
    "zoo ncert read" to "chapters",
    "zoo q" to "questions",
    "phy q" to "questions",
    "chem q" to "questions",
    "mock test review" to "tests",
    "formula revision" to "times"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("add_edit_tally_dialog"),
    shape = RoundedCornerShape(24.dp),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.FormatListNumbered,
            contentDescription = null,
            tint = RatingBestGreen,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isEditing) "Edit Tally Counter" else "Add Tally Counter",
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
          .verticalScroll(rememberScrollState())
      ) {
        Text(
          text = "Counter Title / Activity",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          placeholder = { Text("e.g. bot ncert read, phy q") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("tally_title_input"),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Quick Presets:",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          presets.forEach { (presetName, presetUnit) ->
            SuggestionChip(
              onClick = {
                title = presetName
                unit = presetUnit
              },
              label = { Text(presetName, fontSize = 11.sp) },
              colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = if (title.equals(presetName, ignoreCase = true)) {
                  MaterialTheme.colorScheme.primaryContainer
                } else {
                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Current Count
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Current Count",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = countStr,
              onValueChange = { countStr = it.filter { ch -> ch.isDigit() } },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              shape = RoundedCornerShape(12.dp)
            )
          }

          // Target Count
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Target Goal",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = targetStr,
              onValueChange = { targetStr = it.filter { ch -> ch.isDigit() } },
              placeholder = { Text("e.g. 500") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              shape = RoundedCornerShape(12.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Counting Unit",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          listOf("questions", "chapters", "pages", "times", "topics").forEach { u ->
            SuggestionChip(
              onClick = { unit = u },
              label = { Text(u, fontSize = 11.sp) },
              colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = if (unit == u) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
              )
            )
          }
        }

        if (isEditing && onReset != null) {
          Spacer(modifier = Modifier.height(14.dp))
          OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reset Count to 0")
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            val count = countStr.toIntOrNull() ?: 0
            val target = targetStr.toIntOrNull() ?: 0
            onSave(title.trim(), count, target, unit)
          }
        },
        enabled = title.isNotBlank(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RatingBestGreen)
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(if (isEditing) "Save Counter" else "Create Counter")
      }
    },
    dismissButton = {
      if (isEditing && onDelete != null) {
        OutlinedButton(
          onClick = onDelete,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
          Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Delete")
        }
      }
    }
  )
}
