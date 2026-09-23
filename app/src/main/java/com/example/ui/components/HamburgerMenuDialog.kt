package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
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
import com.example.data.model.TaskPreset
import com.example.ui.HabitViewModel
import com.example.ui.theme.HexColorPalette
import com.example.ui.theme.getContrastingTextColor
import com.example.ui.theme.parseHexColor
import com.example.util.ImageStorageUtils
import kotlinx.coroutines.launch

enum class HexColorTarget(val label: String) {
  UI("UI / Accent"),
  TEXT("Text / Font")
}

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
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current

  var selectedTab by remember { mutableStateOf(initialTab) }

  // Theme states
  val currentUiHex by viewModel.selectedUiHex.collectAsStateWithLifecycle()
  val currentTextHex by viewModel.selectedTextHex.collectAsStateWithLifecycle()
  val currentBgImageUri by viewModel.selectedBackgroundImageUri.collectAsStateWithLifecycle()

  // Presets state
  val presets by viewModel.presets.collectAsStateWithLifecycle()
  var customPresetInput by remember { mutableStateOf("") }
  var presetToSchedule by remember { mutableStateOf<TaskPreset?>(null) }

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
              currentUiHex = currentUiHex,
              currentTextHex = currentTextHex,
              currentBgImageUri = currentBgImageUri,
              onSetUiHex = { viewModel.setCustomUiHex(it) },
              onSetTextHex = { viewModel.setCustomTextHex(it) },
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
              onSchedulePreset = { preset ->
                presetToSchedule = preset
              },
              onAddPresetToDay = { preset ->
                viewModel.addTaskFromPreset(preset)
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
  currentUiHex: String,
  currentTextHex: String,
  currentBgImageUri: String?,
  onSetUiHex: (String) -> Unit,
  onSetTextHex: (String) -> Unit,
  onPickBackgroundImage: () -> Unit,
  onRemoveBackgroundImage: () -> Unit
) {
  val haptic = LocalHapticFeedback.current
  var activeTarget by remember { mutableStateOf(HexColorTarget.UI) }

  val activeHex = when (activeTarget) {
    HexColorTarget.UI -> currentUiHex
    HexColorTarget.TEXT -> currentTextHex
  }

  var hexInputText by remember(activeTarget, activeHex) {
    mutableStateOf(activeHex.removePrefix("#"))
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Target Selector: UI Accent, Text Color
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(
        text = "Select Element to Customize",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        HexColorTarget.values().forEach { target ->
          val isSelected = target == activeTarget
          val targetColor = when (target) {
            HexColorTarget.UI -> parseHexColor(currentUiHex, Color(0xFF3B82F6))
            HexColorTarget.TEXT -> parseHexColor(currentTextHex, Color(0xFFFFFFFF))
          }

          OutlinedButton(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              activeTarget = target
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent
            ),
            border = androidx.compose.foundation.BorderStroke(
              if (isSelected) 2.dp else 1.dp,
              if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)
            ),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(targetColor)
                .border(0.5.dp, Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = target.label,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
          }
        }
      }
    }

    // 2. Write Hexadecimal Code
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(
        text = "Hexadecimal Code Input",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
      )
      Text(
        text = "Write or paste any 6-digit hex color code for ${activeTarget.label}:",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = hexInputText,
          onValueChange = { input ->
            val cleaned = input.filter { it.isLetterOrDigit() }.take(6).uppercase()
            hexInputText = cleaned
            if (cleaned.length == 6) {
              val formatted = "#$cleaned"
              when (activeTarget) {
                HexColorTarget.UI -> onSetUiHex(formatted)
                HexColorTarget.TEXT -> onSetTextHex(formatted)
              }
            }
          },
          leadingIcon = {
            Text("#", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
          },
          label = { Text("${activeTarget.label} Hex") },
          placeholder = { Text("e.g. 3B82F6") },
          singleLine = true,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp)
        )

        val parsedPreview = parseHexColor("#$hexInputText", parseHexColor(activeHex, Color.Gray))
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(parsedPreview)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
        )
      }
    }

    // 3. Live Transparent Effect Preview Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(
        containerColor = Color.White.copy(alpha = 0.08f)
      ),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
    ) {
      Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val textColor = parseHexColor(currentTextHex, Color.White)
        val uiColor = parseHexColor(currentUiHex, Color(0xFF3B82F6))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Transparent Glass Preview",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor
          )

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(uiColor)
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Text(
              text = "Accent Button",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = getContrastingTextColor(uiColor)
            )
          }
        }

        Text(
          text = "Text and UI accent colors stay clear and transparent over wallpapers.",
          fontSize = 12.sp,
          color = textColor.copy(alpha = 0.85f)
        )
      }
    }

    // 4. Hexadecimal Colour Chart
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(
        text = "Hexadecimal Colour Chart",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
      )
      Text(
        text = "Tap any color swatch below to apply to ${activeTarget.label}:",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(Color.White.copy(alpha = 0.06f))
          .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
          .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        HexColorPalette.rows.forEach { rowColors ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            rowColors.forEach { hexCode ->
              val swatchColor = parseHexColor(hexCode, Color.Gray)
              val isSelected = hexCode.equals(activeHex, ignoreCase = true) ||
                "#$hexInputText".equals(hexCode, ignoreCase = true)

              Box(
                modifier = Modifier
                  .size(26.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(swatchColor)
                  .border(
                    width = if (isSelected) 2.5.dp else 0.5.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                  )
                  .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    hexInputText = hexCode.removePrefix("#")
                    when (activeTarget) {
                      HexColorTarget.UI -> onSetUiHex(hexCode)
                      HexColorTarget.TEXT -> onSetTextHex(hexCode)
                    }
                  }
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(2.dp))

    // 5. Device Background Wallpaper Image
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text("Device Background Wallpaper", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
    }

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
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
  var showPasteInput by remember { mutableStateOf(false) }
  var manualJsonText by remember { mutableStateOf("") }

  val clipboardManager = remember {
    context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
  }

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
            if (isSuccess) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
          )
          .border(
            1.dp,
            if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            RoundedCornerShape(10.dp)
          )
          .padding(10.dp)
      ) {
        Text(
          text = msg,
          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
          color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
      }
    }

    // Export Card (Easy 1-Tap Share & 1-Tap Clipboard Copy)
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.FileUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Export Data Backup", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          "Export your tasks, presets, NEET chapters, ratings, and timer logs into portable JSON.",
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
                  statusMessage = "Backup ready to save or share!"
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
            Text("Share Backup", fontSize = 12.sp)
          }

          OutlinedButton(
            onClick = {
              coroutineScope.launch {
                try {
                  val json = viewModel.exportDataToJson()
                  val clip = ClipData.newPlainText("Habit_Tracker_Backup", json)
                  clipboardManager?.setPrimaryClip(clip)
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  statusMessage = "JSON backup copied to clipboard! ✓"
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

    // Import Card (1-Tap Paste from Clipboard & 1-Tap Select File)
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Import Data Backup", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          "Restore tasks, presets, logs, and events instantly from clipboard or a file.",
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
                    statusMessage = "Successfully restored $count records from clipboard!"
                    isSuccess = true
                  } else {
                    statusMessage = "Clipboard is empty! Copy JSON backup first."
                    isSuccess = false
                  }
                } catch (e: Exception) {
                  statusMessage = "Clipboard import error: ${e.message}"
                  isSuccess = false
                } finally {
                  isProcessing = false
                }
              }
            },
            enabled = !isProcessing,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Paste & Restore", fontSize = 12.sp)
          }

          OutlinedButton(
            onClick = { importFileLauncher.launch("*/*") },
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
                    statusMessage = "Imported $count records from input text!"
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetsCategoryContent(
  presets: List<TaskPreset>,
  customPresetInput: String,
  onCustomPresetInputChange: (String) -> Unit,
  onAddPreset: (String) -> Unit,
  onDeletePreset: (Long) -> Unit,
  onSchedulePreset: (TaskPreset) -> Unit,
  onAddPresetToDay: (TaskPreset) -> Unit = {}
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Text(
      text = "Preset Templates",
      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
    )
    Text(
      text = "Presets are saved templates. They are only added to your tracker on days you choose to add them.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(4.dp))

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
        shape = RoundedCornerShape(10.dp)
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
          "No presets saved yet. Add a preset above.",
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
            Column(modifier = Modifier.padding(10.dp)) {
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
                    onClick = { onDeletePreset(preset.id) },
                    modifier = Modifier.size(26.dp)
                  ) {
                    Icon(
                      Icons.Default.Delete,
                      contentDescription = "Delete",
                      tint = MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(6.dp))

              Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Button(
                  onClick = { onAddPresetToDay(preset) },
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.height(28.dp)
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(3.dp))
                  Text("Add to Day", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

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
                    text = "Schedule...",
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
}
