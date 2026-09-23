package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.HabitViewModel
import com.example.ui.theme.RatingBestGreen
import kotlinx.coroutines.launch

@Composable
fun DataManagementDialog(
  viewModel: HabitViewModel,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val coroutineScope = rememberCoroutineScope()

  var statusMessage by remember { mutableStateOf<String?>(null) }
  var isSuccess by remember { mutableStateOf(true) }
  var isProcessing by remember { mutableStateOf(false) }
  var showPasteInput by remember { mutableStateOf(false) }
  var manualJsonText by remember { mutableStateOf("") }

  val clipboardManager = remember {
    context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
  }

  // Import JSON launcher
  val importFileLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      coroutineScope.launch {
        isProcessing = true
        try {
          val inputStream = context.contentResolver.openInputStream(uri)
          val jsonString = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
          if (jsonString.isNotBlank()) {
            val count = viewModel.importDataFromJson(jsonString)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            statusMessage = "Successfully imported $count records!"
            isSuccess = true
          } else {
            statusMessage = "Empty file selected."
            isSuccess = false
          }
        } catch (e: Exception) {
          statusMessage = "Failed to import: ${e.message}"
          isSuccess = false
        } finally {
          isProcessing = false
        }
      }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("data_management_dialog"),
    shape = RoundedCornerShape(24.dp),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Storage,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Data Management",
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
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "Export your habit history, NEET scores, and presets as a backup, or restore data anytime.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Status banner
        statusMessage?.let { msg ->
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(
                if (isSuccess) RatingBestGreen.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
              )
              .border(
                1.dp,
                if (isSuccess) RatingBestGreen else MaterialTheme.colorScheme.error,
                RoundedCornerShape(12.dp)
              )
              .padding(12.dp)
          ) {
            Text(
              text = msg,
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
              color = if (isSuccess) RatingBestGreen else MaterialTheme.colorScheme.error
            )
          }
        }

        // Export Data Card (Share & Clipboard)
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.06f)
          ),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.FileUpload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Export Backup",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Export all habits, logs, ratings, events, and scores to JSON.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = {
                  coroutineScope.launch {
                    try {
                      val json = viewModel.exportDataToJson()
                      val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, json)
                        putExtra(Intent.EXTRA_TITLE, "Habit_Tracker_Backup.json")
                        type = "text/plain"
                      }
                      val shareIntent = Intent.createChooser(sendIntent, "Export Habit Tracker Data")
                      context.startActivity(shareIntent)
                      statusMessage = "Export generated and ready to share!"
                      isSuccess = true
                    } catch (e: Exception) {
                      statusMessage = "Export failed: ${e.message}"
                      isSuccess = false
                    }
                  }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share JSON", fontSize = 12.sp)
              }

              OutlinedButton(
                onClick = {
                  coroutineScope.launch {
                    try {
                      val json = viewModel.exportDataToJson()
                      val clip = ClipData.newPlainText("Habit_Tracker_Backup", json)
                      clipboardManager?.setPrimaryClip(clip)
                      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                      statusMessage = "Backup JSON copied to clipboard! ✓"
                      isSuccess = true
                    } catch (e: Exception) {
                      statusMessage = "Copy failed: ${e.message}"
                      isSuccess = false
                    }
                  }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy JSON", fontSize = 12.sp)
              }
            }
          }
        }

        // Import Data Card (Paste & File Picker)
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.06f)
          ),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = null,
                tint = RatingBestGreen,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Import Backup",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Restore all your data effortlessly from clipboard or a file.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = {
                  coroutineScope.launch {
                    isProcessing = true
                    try {
                      val clipData = clipboardManager?.primaryClip
                      val clipText = if (clipData != null && clipData.itemCount > 0) {
                        clipData.getItemAt(0).text?.toString() ?: ""
                      } else ""

                      if (clipText.isNotBlank()) {
                        val count = viewModel.importDataFromJson(clipText)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        statusMessage = "Restored $count records from clipboard! ✓"
                        isSuccess = true
                      } else {
                        statusMessage = "Clipboard is empty! Copy JSON backup first."
                        isSuccess = false
                      }
                    } catch (e: Exception) {
                      statusMessage = "Import failed: ${e.message}"
                      isSuccess = false
                    } finally {
                      isProcessing = false
                    }
                  }
                },
                enabled = !isProcessing,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RatingBestGreen)
              ) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Paste & Restore", fontSize = 12.sp)
              }

              OutlinedButton(
                onClick = {
                  importFileLauncher.launch("*/*")
                },
                enabled = !isProcessing,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Select File", fontSize = 12.sp)
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Optional direct paste text field toggle
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { showPasteInput = !showPasteInput }
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (showPasteInput) "Hide text input ▲" else "Or paste raw JSON manually ▼",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
              )
            }

            if (showPasteInput) {
              Spacer(modifier = Modifier.height(6.dp))
              OutlinedTextField(
                value = manualJsonText,
                onValueChange = { manualJsonText = it },
                label = { Text("Paste JSON here") },
                placeholder = { Text("{\"version\": ..., \"tasks\": [...]}") },
                modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(min = 100.dp, max = 160.dp),
                shape = RoundedCornerShape(10.dp)
              )
              Spacer(modifier = Modifier.height(6.dp))
              Button(
                onClick = {
                  if (manualJsonText.isNotBlank()) {
                    coroutineScope.launch {
                      isProcessing = true
                      try {
                        val count = viewModel.importDataFromJson(manualJsonText)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        statusMessage = "Imported $count records from input text! ✓"
                        isSuccess = true
                        manualJsonText = ""
                        showPasteInput = false
                      } catch (e: Exception) {
                        statusMessage = "Invalid JSON: ${e.message}"
                        isSuccess = false
                      } finally {
                        isProcessing = false
                      }
                    }
                  }
                },
                enabled = manualJsonText.isNotBlank() && !isProcessing,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Import Pasted JSON")
              }
            }
          }
        }
      }
    },
    confirmButton = {
      OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
        Text("Close")
      }
    }
  )
}
