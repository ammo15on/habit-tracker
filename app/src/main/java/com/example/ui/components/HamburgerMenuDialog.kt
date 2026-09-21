package com.example.ui.components

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.HabitTask
import com.example.ui.HabitViewModel
import com.example.ui.theme.AppFontColor
import com.example.ui.theme.AppThemeColor
import com.example.ui.theme.RatingBestGreen
import com.example.util.ImageStorageUtils
import kotlinx.coroutines.launch

enum class HamburgerMenuTab(val label: String) {
  THEME("Theme"),
  DATA("Data"),
  PRESETS("Presets")
}

@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HamburgerMenuDialog(
  viewModel: HabitViewModel,
  initialTab: HamburgerMenuTab = HamburgerMenuTab.THEME,
  onDismiss: () -> Unit,
  onEditPreset: (HabitTask) -> Unit = {}
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current

  var selectedTab by remember { mutableStateOf(initialTab) }

  // Theme states
  val currentThemeColor by viewModel.selectedThemeColor.collectAsStateWithLifecycle()
  val currentFontColor by viewModel.selectedFontColor.collectAsStateWithLifecycle()
  val currentBgImageUri by viewModel.selectedBackgroundImageUri.collectAsStateWithLifecycle()

  // Presets state
  val presets by viewModel.presets.collectAsStateWithLifecycle()
  var customPresetInput by remember { mutableStateOf("") }
  var presetToSchedule by remember { mutableStateOf<HabitTask?>(null) }

  // Photo picker for background image
  val bgPhotoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      val savedPath = ImageStorageUtils.copyUriToInternalStorage(context, uri, "app_background_image")
      if (savedPath != null) {
        viewModel.setBackgroundImageUri(savedPath)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
      }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("hamburger_menu_dialog"),
    shape = RoundedCornerShape(24.dp),
    title = {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "App Menu",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3 Categories: Theme, Data, Presets
        SecondaryTabRow(
          selectedTabIndex = selectedTab.ordinal,
          modifier = Modifier.fillMaxWidth()
        ) {
          HamburgerMenuTab.values().forEach { tab ->
            Tab(
              selected = selectedTab == tab,
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                selectedTab = tab
              },
              text = {
                Text(
                  text = tab.label,
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                  )
                )
              }
            )
          }
        }
      }
    },
    text = {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 340.dp, max = 500.dp)
      ) {
        when (selectedTab) {
          HamburgerMenuTab.THEME -> {
            ThemeCategoryContent(
              currentThemeColor = currentThemeColor,
              currentFontColor = currentFontColor,
              currentBgImageUri = currentBgImageUri,
              onThemeColorSelected = { viewModel.setThemeColor(it) },
              onFontColorSelected = { viewModel.setFontColor(it) },
              onPickBackgroundImage = {
                bgPhotoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              onRemoveBackgroundImage = {
                viewModel.setBackgroundImageUri(null)
              }
            )
          }
          HamburgerMenuTab.DATA -> {
            DataCategoryContent(viewModel = viewModel)
          }
          HamburgerMenuTab.PRESETS -> {
            PresetsCategoryContent(
              presets = presets,
              customPresetInput = customPresetInput,
              onCustomPresetInputChange = { customPresetInput = it },
              onAddPreset = { name -> viewModel.addPreset(name) },
              onDeletePreset = { id -> viewModel.deletePreset(id) },
              onEditPreset = { preset ->
                onDismiss()
                onEditPreset(preset)
              },
              onSchedulePreset = { preset ->
                presetToSchedule = preset
              }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
        Text("Done")
      }
    }
  )

  // Mini Calendar Popup for scheduling preset across multiple dates
  presetToSchedule?.let { preset ->
    MiniCalendarPickerPopup(
      title = "Schedule \"${preset.name}\"",
      allowMultiple = true,
      onDismiss = { presetToSchedule = null },
      onConfirm = { selectedDates ->
        if (selectedDates.isNotEmpty()) {
          viewModel.schedulePresetForDates(preset, selectedDates)
        }
        presetToSchedule = null
      }
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThemeCategoryContent(
  currentThemeColor: AppThemeColor,
  currentFontColor: AppFontColor,
  currentBgImageUri: String?,
  onThemeColorSelected: (AppThemeColor) -> Unit,
  onFontColorSelected: (AppFontColor) -> Unit,
  onPickBackgroundImage: () -> Unit,
  onRemoveBackgroundImage: () -> Unit
) {
  val haptic = LocalHapticFeedback.current

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Accent Theme Color
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text("App Theme Accent", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
    }

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      AppThemeColor.values().forEach { themeColor ->
        val isSelected = themeColor == currentThemeColor
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
              if (isSelected) MaterialTheme.colorScheme.primaryContainer
              else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .border(
              width = if (isSelected) 2.dp else 0.5.dp,
              color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
              shape = RoundedCornerShape(10.dp)
            )
            .clickable {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              onThemeColorSelected(themeColor)
            }
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(themeColor.previewHex)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = themeColor.displayName,
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // 2. Font Color Selection
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(Icons.Default.FormatColorText, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text("Font Color Selection", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
    }

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      AppFontColor.values().forEach { fontColor ->
        val isSelected = fontColor == currentFontColor
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
              if (isSelected) MaterialTheme.colorScheme.primaryContainer
              else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
            .border(
              width = if (isSelected) 1.5.dp else 0.5.dp,
              color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
              shape = RoundedCornerShape(8.dp)
            )
            .clickable {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              onFontColorSelected(fontColor)
            }
            .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(fontColor.previewColor)
                .border(0.5.dp, Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = fontColor.displayName,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // 3. UI Background Image
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text("Device Background Image", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
    }

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        if (!currentBgImageUri.isNullOrBlank()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(100.dp)
              .clip(RoundedCornerShape(8.dp))
          ) {
            AsyncImage(
              model = currentBgImageUri,
              contentDescription = "Current Background",
              modifier = Modifier.fillMaxWidth(),
              contentScale = ContentScale.Crop
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = onPickBackgroundImage,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (currentBgImageUri.isNullOrBlank()) "Select Image" else "Change Image", fontSize = 12.sp)
          }

          if (!currentBgImageUri.isNullOrBlank()) {
            OutlinedButton(
              onClick = onRemoveBackgroundImage,
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
              Text("Remove", fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DataCategoryContent(viewModel: HabitViewModel) {
  DataManagementDialogBody(viewModel = viewModel)
}

@Composable
private fun DataManagementDialogBody(viewModel: HabitViewModel) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val coroutineScope = rememberCoroutineScope()

  var statusMessage by remember { mutableStateOf<String?>(null) }
  var isSuccess by remember { mutableStateOf(true) }
  var isProcessing by remember { mutableStateOf(false) }

  val importFileLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    if (uri != null) {
      coroutineScope.launch {
        isProcessing = true
        try {
          val inputStream = context.contentResolver.openInputStream(uri)
          val jsonString = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
          if (jsonString.isNotBlank()) {
            val count = viewModel.importDataFromJson(jsonString)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            statusMessage = "Imported $count records successfully!"
            isSuccess = true
          } else {
            statusMessage = "Selected file was empty."
            isSuccess = false
          }
        } catch (e: Exception) {
          statusMessage = "Import error: ${e.message}"
          isSuccess = false
        } finally {
          isProcessing = false
        }
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    statusMessage?.let { msg ->
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(
            if (isSuccess) RatingBestGreen.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
          )
          .padding(10.dp)
      ) {
        Text(
          text = msg,
          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
          color = if (isSuccess) RatingBestGreen else MaterialTheme.colorScheme.error
        )
      }
    }

    // Export Data Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text("Export Data", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          "Export your tasks, presets, NEET chapters, ratings, and logs as a portable JSON backup file.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
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
                statusMessage = "Export ready to save or share!"
                isSuccess = true
              } catch (e: Exception) {
                statusMessage = "Export failed: ${e.message}"
                isSuccess = false
              }
            }
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
          Text("Export Backup JSON")
        }
      }
    }

    // Import Data Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text("Import Data", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          "Restore tasks, presets, and logs from a previously exported JSON backup file.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
          onClick = { importFileLauncher.launch("*/*") },
          enabled = !isProcessing,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = RatingBestGreen)
        ) {
          Text(if (isProcessing) "Importing..." else "Select & Import JSON File")
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetsCategoryContent(
  presets: List<HabitTask>,
  customPresetInput: String,
  onCustomPresetInputChange: (String) -> Unit,
  onAddPreset: (String) -> Unit,
  onDeletePreset: (Long) -> Unit,
  onEditPreset: (HabitTask) -> Unit,
  onSchedulePreset: (HabitTask) -> Unit
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Add custom preset input
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = customPresetInput,
        onValueChange = onCustomPresetInputChange,
        placeholder = { Text("New preset habit...", fontSize = 12.sp) },
        singleLine = true,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp)
      )

      Button(
        onClick = {
          if (customPresetInput.isNotBlank()) {
            onAddPreset(customPresetInput.trim())
            onCustomPresetInputChange("")
          }
        },
        enabled = customPresetInput.isNotBlank(),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RatingBestGreen)
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Add")
      }
    }

    // Presets list
    if (presets.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          "No custom presets yet. Add a preset above or save from Add Task.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        items(presets, key = { it.id }) { preset ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
          ) {
            Column(modifier = Modifier.padding(8.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = preset.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  if (preset.targetTimeMinutes > 0) {
                    Text(
                      text = "${preset.targetTimeMinutes}m target",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.primary
                    )
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  IconButton(
                    onClick = { onEditPreset(preset) },
                    modifier = Modifier.size(26.dp)
                  ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                  }
                  IconButton(
                    onClick = { onDeletePreset(preset.id) },
                    modifier = Modifier.size(26.dp)
                  ) {
                    Icon(
                      Icons.Default.Delete,
                      contentDescription = "Delete",
                      tint = MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(14.dp)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(4.dp))

              OutlinedButton(
                onClick = { onSchedulePreset(preset) },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(28.dp)
              ) {
                Icon(
                  Icons.Default.CalendarMonth,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Schedule for Dates...",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                )
              }
            }
          }
        }
      }
    }
  }
}
