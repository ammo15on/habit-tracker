package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.HabitTask
import com.example.data.model.TaskPreset
import com.example.ui.HabitViewModel
import com.example.ui.theme.HexColorPalette
import com.example.util.AppGoal
import com.example.util.DateUtils

enum class HamburgerPage {
  MAIN_MENU,
  THEME,
  GOAL,
  TASKS,
  PRESETS,
  DATA
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HamburgerMenuDialog(
  viewModel: HabitViewModel,
  onDismiss: () -> Unit
) {
  var currentPage by remember { mutableStateOf(HamburgerPage.MAIN_MENU) }
  val currentBgImageUri by viewModel.selectedBackgroundImageUri.collectAsStateWithLifecycle()
  val currentUiOpacity by viewModel.selectedUiOpacity.collectAsStateWithLifecycle()

  BackHandler(enabled = true) {
    if (currentPage != HamburgerPage.MAIN_MENU) {
      currentPage = HamburgerPage.MAIN_MENU
    } else {
      onDismiss()
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
      usePlatformDefaultWidth = false,
      decorFitsSystemWindows = false
    )
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .testTag("hamburger_full_screen_dialog"),
      color = MaterialTheme.colorScheme.background
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        if (!currentBgImageUri.isNullOrBlank()) {
          AsyncImage(
            model = currentBgImageUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color(0xFF090B10).copy(alpha = currentUiOpacity.coerceIn(0.2f, 0.95f)))
          )
        }

        Crossfade(
          targetState = currentPage,
          label = "HamburgerPageTransition"
        ) { page ->
          when (page) {
            HamburgerPage.MAIN_MENU -> {
              HamburgerMainMenuScreen(
                onNavigate = { currentPage = it },
                onClose = onDismiss
              )
            }
            HamburgerPage.THEME -> {
              ThemeFullScreenPage(
                viewModel = viewModel,
                onBack = { currentPage = HamburgerPage.MAIN_MENU }
              )
            }
            HamburgerPage.GOAL -> {
              GoalFullScreenPage(
                viewModel = viewModel,
                onBack = { currentPage = HamburgerPage.MAIN_MENU }
              )
            }
            HamburgerPage.TASKS -> {
              PastTasksFullScreenPage(
                viewModel = viewModel,
                onBack = { currentPage = HamburgerPage.MAIN_MENU }
              )
            }
            HamburgerPage.PRESETS -> {
              PresetsFullScreenPage(
                viewModel = viewModel,
                onBack = { currentPage = HamburgerPage.MAIN_MENU }
              )
            }
            HamburgerPage.DATA -> {
              DataFullScreenPage(
                viewModel = viewModel,
                onBack = { currentPage = HamburgerPage.MAIN_MENU }
              )
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HamburgerMainMenuScreen(
  onNavigate: (HamburgerPage) -> Unit,
  onClose: () -> Unit
) {
  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Settings & Hub", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                "v6.3",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        },
        actions = {
          IconButton(onClick = onClose, modifier = Modifier.testTag("close_hamburger_btn")) {
            Icon(Icons.Default.Close, contentDescription = "Close Menu")
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = Color.Transparent
        )
      )
    },
    containerColor = Color.Transparent
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        HamburgerRowCard(
          icon = Icons.Default.ColorLens,
          title = "Theme & Customization",
          subtitle = "Glass UI, hex colors, background opacity & text scaling",
          onClick = { onNavigate(HamburgerPage.THEME) },
          tag = "nav_theme_btn"
        )
      }
      item {
        HamburgerRowCard(
          icon = Icons.Default.TrackChanges,
          title = "Target & Motivation",
          subtitle = "NEET target score, countdown & daily hours",
          onClick = { onNavigate(HamburgerPage.GOAL) },
          tag = "nav_goal_btn"
        )
      }
      item {
        HamburgerRowCard(
          icon = Icons.Default.History,
          title = "Habits & Task History",
          subtitle = "Review and manage all habit tasks & reminders",
          onClick = { onNavigate(HamburgerPage.TASKS) },
          tag = "nav_tasks_btn"
        )
      }
      item {
        HamburgerRowCard(
          icon = Icons.Default.Bookmark,
          title = "Task Presets",
          subtitle = "Manage quick-add templates for study blocks",
          onClick = { onNavigate(HamburgerPage.PRESETS) },
          tag = "nav_presets_btn"
        )
      }
      item {
        HamburgerRowCard(
          icon = Icons.Default.Settings,
          title = "Data & Database",
          subtitle = "Export JSON backup, restore & reset options",
          onClick = { onNavigate(HamburgerPage.DATA) },
          tag = "nav_data_btn"
        )
      }
    }
  }
}

@Composable
private fun HamburgerRowCard(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  tag: String
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag(tag)
      .clickable { onClick() },
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
      }
      Icon(
        Icons.AutoMirrored.Filled.ArrowForwardIos,
        contentDescription = null,
        modifier = Modifier.size(14.dp),
        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeFullScreenPage(
  viewModel: HabitViewModel,
  onBack: () -> Unit
) {
  val currentHex by viewModel.selectedColorHex.collectAsStateWithLifecycle()
  val currentFontHex by viewModel.selectedFontHex.collectAsStateWithLifecycle()
  val currentBgImageUri by viewModel.selectedBackgroundImageUri.collectAsStateWithLifecycle()
  val currentOpacity by viewModel.selectedUiOpacity.collectAsStateWithLifecycle()
  val currentTextScale by viewModel.selectedTextSizeScale.collectAsStateWithLifecycle()

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    viewModel.setBackgroundImage(uri)
  }

  var customHexInput by remember { mutableStateOf(currentHex) }
  var customFontHexInput by remember { mutableStateOf(currentFontHex) }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = { Text("Theme & Styling", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
      )
    },
    containerColor = Color.Transparent
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Primary Accent Color
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Primary Accent Color", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              items(HexColorPalette.PRESET_COLORS) { hex ->
                val color = HexColorPalette.parseColor(hex)
                val isSelected = hex.equals(currentHex, ignoreCase = true)
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                      width = if (isSelected) 3.dp else 1.dp,
                      color = if (isSelected) Color.White else Color.Transparent,
                      shape = CircleShape
                    )
                    .clickable {
                      viewModel.setCustomColorHex(hex)
                      customHexInput = hex
                    }
                )
              }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedTextField(
                value = customHexInput,
                onValueChange = { customHexInput = it },
                label = { Text("Custom Hex (e.g. #3B82F6)") },
                modifier = Modifier.weight(1f),
                singleLine = true
              )
              Button(
                onClick = { viewModel.setCustomColorHex(customHexInput) },
                modifier = Modifier.testTag("apply_primary_hex_btn")
              ) {
                Text("Apply")
              }
            }
          }
        }
      }

      // Font / Text Color
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Font Color Scheme", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              items(HexColorPalette.PRESET_FONT_COLORS) { hex ->
                val color = HexColorPalette.parseColor(hex)
                val isSelected = hex.equals(currentFontHex, ignoreCase = true)
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                      width = if (isSelected) 3.dp else 1.dp,
                      color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                      shape = CircleShape
                    )
                    .clickable {
                      viewModel.setCustomFontColorHex(hex)
                      customFontHexInput = hex
                    }
                )
              }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedTextField(
                value = customFontHexInput,
                onValueChange = { customFontHexInput = it },
                label = { Text("Custom Font Hex") },
                modifier = Modifier.weight(1f),
                singleLine = true
              )
              Button(onClick = { viewModel.setCustomFontColorHex(customFontHexInput) }) {
                Text("Apply")
              }
            }
          }
        }
      }

      // Background Image & Opacity
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Background Image & Opacity", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = { photoPickerLauncher.launch("image/*") },
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Select Wallpaper")
              }
              if (!currentBgImageUri.isNullOrBlank()) {
                OutlinedButton(onClick = { viewModel.setBackgroundImage(null) }) {
                  Text("Remove")
                }
              }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              "Background Overlay Darkness: ${(currentOpacity * 100).toInt()}%",
              fontSize = 13.sp
            )
            Slider(
              value = currentOpacity,
              onValueChange = { viewModel.setUiOpacity(it) },
              valueRange = 0.20f..0.95f
            )
          }
        }
      }

      // Text Size Scaling
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Text Size Scale: ${(currentTextScale * 100).toInt()}%", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
              value = currentTextScale,
              onValueChange = { viewModel.setTextSizeScale(it) },
              valueRange = 0.85f..1.30f
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Compact (85%)", fontSize = 11.sp)
              Text("Default (100%)", fontSize = 11.sp)
              Text("Large (130%)", fontSize = 11.sp)
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalFullScreenPage(
  viewModel: HabitViewModel,
  onBack: () -> Unit
) {
  val goal by viewModel.appGoal.collectAsStateWithLifecycle()
  var title by remember(goal) { mutableStateOf(goal.title) }
  var examDate by remember(goal) { mutableStateOf(goal.examDate) }
  var dailyHours by remember(goal) { mutableStateOf(goal.dailyStudyHoursTarget.toString()) }
  var quote by remember(goal) { mutableStateOf(goal.motivationQuote) }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = { Text("Target & Goals", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
      )
    },
    containerColor = Color.Transparent
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Goal Title") },
          modifier = Modifier.fillMaxWidth()
        )
      }
      item {
        OutlinedTextField(
          value = examDate,
          onValueChange = { examDate = it },
          label = { Text("Exam Target Date (YYYY-MM-DD)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
      item {
        OutlinedTextField(
          value = dailyHours,
          onValueChange = { dailyHours = it },
          label = { Text("Daily Study Hours Target") },
          modifier = Modifier.fillMaxWidth()
        )
      }
      item {
        OutlinedTextField(
          value = quote,
          onValueChange = { quote = it },
          label = { Text("Motivation Mantra") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 2
        )
      }
      item {
        Button(
          onClick = {
            val hours = dailyHours.toIntOrNull() ?: 10
            viewModel.updateGoal(
              AppGoal(
                title = title,
                examDate = examDate,
                dailyStudyHoursTarget = hours,
                motivationQuote = quote
              )
            )
            onBack()
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("save_goal_btn")
        ) {
          Text("Save Goal")
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PastTasksFullScreenPage(
  viewModel: HabitViewModel,
  onBack: () -> Unit
) {
  val tasks by viewModel.allTasks.collectAsStateWithLifecycle()

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = { Text("Habit Tasks & History", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
      )
    },
    containerColor = Color.Transparent
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      if (tasks.isEmpty()) {
        item {
          Text("No habit tasks configured yet.", modifier = Modifier.padding(20.dp))
        }
      }
      items(tasks, key = { it.id }) { task ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(task.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
              Text(
                if (task.targetTimeMinutes > 0) "Target: ${task.targetTimeMinutes} mins" else "Count-up timer",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
              )
              if (!task.reminderTime.isNullOrBlank()) {
                Text("⏰ Alarm: ${task.reminderTime}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
              }
            }
            IconButton(onClick = { viewModel.deleteTask(task.id) }) {
              Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PresetsFullScreenPage(
  viewModel: HabitViewModel,
  onBack: () -> Unit
) {
  val presets by viewModel.allTaskPresets.collectAsStateWithLifecycle()
  var newPresetName by remember { mutableStateOf("") }
  var targetMinutes by remember { mutableStateOf("60") }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = { Text("Task Presets", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
      )
    },
    containerColor = Color.Transparent
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Add New Preset Template", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            OutlinedTextField(
              value = newPresetName,
              onValueChange = { newPresetName = it },
              label = { Text("Preset Title (e.g. Inorganic NCERT)") },
              modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
              value = targetMinutes,
              onValueChange = { targetMinutes = it },
              label = { Text("Target Minutes") },
              modifier = Modifier.fillMaxWidth()
            )
            Button(
              onClick = {
                if (newPresetName.isNotBlank()) {
                  val mins = targetMinutes.toIntOrNull() ?: 60
                  viewModel.addPreset(newPresetName.trim(), mins)
                  newPresetName = ""
                }
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Save Preset")
            }
          }
        }
      }

      items(presets, key = { it.id }) { preset ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(preset.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
              if (preset.targetTimeMinutes > 0) {
                Text("${preset.targetTimeMinutes} mins", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
              }
            }
            IconButton(onClick = { viewModel.deletePreset(preset.id) }) {
              Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DataFullScreenPage(
  viewModel: HabitViewModel,
  onBack: () -> Unit
) {
  val clipboardManager = LocalClipboardManager.current
  val coroutineScope = rememberCoroutineScope()
  var showResetConfirm by remember { mutableStateOf(false) }
  var statusMessage by remember { mutableStateOf("") }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = { Text("Data & Storage", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
      )
    },
    containerColor = Color.Transparent
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Backup & Export", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Text("Copy your complete tracker data in JSON format for safe backup.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
          Button(
            onClick = {
              coroutineScope.launch {
                val json = viewModel.exportAllDataToJson()
                clipboardManager.setText(AnnotatedString(json))
                statusMessage = "Backup copied to clipboard!"
              }
            },
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.FileDownload, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Copy JSON Data to Clipboard")
          }
        }
      }

      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Factory Reset", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFEF4444))
          Text("Clear all study logs, ratings, mock scores, and habit timers to start fresh.", fontSize = 13.sp)
          Button(
            onClick = { showResetConfirm = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Reset All Data")
          }
        }
      }

      if (statusMessage.isNotBlank()) {
        Text(statusMessage, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
      }

      if (showResetConfirm) {
        androidx.compose.material3.AlertDialog(
          onDismissRequest = { showResetConfirm = false },
          title = { Text("Confirm Data Reset") },
          text = { Text("Are you sure you want to delete all logged progress and reset the app to defaults?") },
          confirmButton = {
            TextButton(
              onClick = {
                viewModel.resetAllData()
                showResetConfirm = false
                statusMessage = "All data reset to initial defaults."
              }
            ) {
              Text("Yes, Reset", color = Color(0xFFEF4444))
            }
          },
          dismissButton = {
            TextButton(onClick = { showResetConfirm = false }) {
              Text("Cancel")
            }
          }
        )
      }
    }
  }
}
