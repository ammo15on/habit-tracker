package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.ChapterButtonItem

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditChapterButtonsDialog(
  buttons: List<ChapterButtonItem>,
  onToggleEnabled: (String) -> Unit,
  onAddButton: (name: String, colorHex: Long, isCounter: Boolean) -> Unit,
  onRemoveButton: (String) -> Unit,
  onResetDefaults: () -> Unit,
  onDismiss: () -> Unit
) {
  var newButtonName by remember { mutableStateOf("") }
  var isCounterType by remember { mutableStateOf(false) }
  var selectedColorHex by remember { mutableLongStateOf(0xFF3B82F6) }

  val colorPalette = listOf(
    0xFF10B981L, // Emerald
    0xFF2563EBL, // Blue
    0xFF8B5CF6L, // Purple
    0xFFEC4899L, // Pink
    0xFFD97706L, // Amber
    0xFF06B6D4L, // Cyan
    0xFFF97316L, // Orange
    0xFF6366F1L, // Indigo
    0xFF14B8A6L  // Teal
  )

  val quickPresetSuggestions = listOf(
    "NCERT Exemplar", "Short Notes", "Formula Sheet", "Class DPP", "Mind Map", "Mock Questions"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("edit_chapter_buttons_dialog"),
    shape = RoundedCornerShape(24.dp),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Edit Chapter Buttons",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
        }
      }
    },
    text = {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 500.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        item {
          Text(
            text = "Customize which buttons and trackers appear on each chapter card. You can show/hide standard buttons or add your own.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Active Buttons List
        item {
          Text(
            text = "Active Buttons (${buttons.count { it.isEnabled }} of ${buttons.size})",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
        }

        items(buttons, key = { it.id }) { btn ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (btn.isEnabled) 0.5f else 0.25f)
            ),
            border = androidx.compose.foundation.BorderStroke(
              width = 1.dp,
              color = if (btn.isEnabled) Color(btn.colorHex).copy(alpha = 0.4f) else Color.Transparent
            )
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                // Color swatch
                Box(
                  modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(btn.colorHex))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = btn.name,
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                      color = if (btn.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = if (btn.isCounter) "(Counter +/−)" else "(Toggle ✓)",
                      fontSize = 10.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                  }
                  if (!btn.isBuiltIn) {
                    Text(
                      text = "Custom Button",
                      fontSize = 10.sp,
                      color = Color(btn.colorHex)
                    )
                  }
                }
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                // Switch to toggle enabled/disabled
                Switch(
                  checked = btn.isEnabled,
                  onCheckedChange = { onToggleEnabled(btn.id) },
                  modifier = Modifier.padding(end = 4.dp)
                )

                // Delete custom button
                if (!btn.isBuiltIn) {
                  IconButton(
                    onClick = { onRemoveButton(btn.id) },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      Icons.Default.Delete,
                      contentDescription = "Delete custom button",
                      tint = MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            }
          }
        }

        // Add New Custom Button Section
        item {
          Spacer(modifier = Modifier.height(4.dp))
          Divider()
          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Add New Chapter Button",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )

          Spacer(modifier = Modifier.height(6.dp))

          // Quick preset tags to fill name
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            quickPresetSuggestions.forEach { suggestion ->
              FilterChip(
                selected = (newButtonName == suggestion),
                onClick = { newButtonName = suggestion },
                label = { Text(suggestion, fontSize = 11.sp) },
                shape = RoundedCornerShape(8.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = newButtonName,
            onValueChange = { newButtonName = it },
            placeholder = { Text("Button name (e.g. NCERT Exemplar)", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Button Type Selector: Check Toggle vs Step Counter
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilterChip(
              selected = !isCounterType,
              onClick = { isCounterType = false },
              label = { Text("✓ Checkmark Toggle", fontSize = 11.sp) },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp)
            )
            FilterChip(
              selected = isCounterType,
              onClick = { isCounterType = true },
              label = { Text("± Step Counter", fontSize = 11.sp) },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Color palette picker
          Text("Button Color:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            colorPalette.forEach { hex ->
              val isSelected = selectedColorHex == hex
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(Color(hex))
                  .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                    shape = CircleShape
                  )
                  .clickable { selectedColorHex = hex },
                contentAlignment = Alignment.Center
              ) {
                if (isSelected) {
                  Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = {
              if (newButtonName.isNotBlank()) {
                onAddButton(newButtonName.trim(), selectedColorHex, isCounterType)
                newButtonName = ""
              }
            },
            enabled = newButtonName.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Button to Chapters", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }

        // Reset to Defaults
        item {
          Spacer(modifier = Modifier.height(4.dp))
          Divider()
          Spacer(modifier = Modifier.height(4.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Restore default 5 buttons",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
              onClick = onResetDefaults,
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Reset Defaults", fontSize = 12.sp)
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Done")
      }
    }
  )
}
