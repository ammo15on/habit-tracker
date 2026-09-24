package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.TaskPreset
import com.example.ui.HabitViewModel
import com.example.ui.PastDayTasksGroup
import com.example.ui.PastTaskFilter
import com.example.ui.PastTaskUiItem
import com.example.ui.theme.HexColorPalette
import com.example.ui.theme.RatingBestGreen
import com.example.ui.theme.getContrastingTextColor
import com.example.ui.theme.parseHexColor
import com.example.util.DateUtils
import com.example.util.ImageStorageUtils
import kotlinx.coroutines.launch

enum class HexColorTarget(val label: String) {
  UI("UI / Accent"),
  TEXT("Text / Font")
}

enum class HamburgerPage {
  MAIN_MENU,
  THEME,
  DATA,
  PRESETS,
  TASKS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HamburgerMenuDialog(
  viewModel: HabitViewModel,
  onDismiss: () -> Unit
) {
  val haptic = LocalHapticFeedback.current
  var currentPage by remember { mutableStateOf(HamburgerPage.MAIN_MENU) }

  // Theme states
  val currentUiHex by viewModel.selectedUiHex.collectAsStateWithLifecycle()
  val currentTextHex by viewModel.selectedTextHex.collectAsStateWithLifecycle()
  val currentBgImageUri by viewModel.selectedBackgroundImageUri.collectAsStateWithLifecycle()

  // Presets state
  val presets by viewModel.presets.collectAsStateWithLifecycle()
  var customPresetInput by remember { mutableStateOf("") }
  var presetToSchedule by remember { mutableStateOf<TaskPreset?>(null) }

  // Past tasks state
  val pastTasksGroups by viewModel.pastTasksGroups.collectAsStateWithLifecycle()

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
      color = Color(0xFF0B0D13)
    ) {
      Crossfade(
        targetState = currentPage,
        label = "HamburgerPageTransition"
      ) { page ->
        when (page) {
          HamburgerPage.MAIN_MENU -> {
            HamburgerMainMenuScreen(
              presetsCount = presets.size,
              pastDaysCount = pastTasksGroups.size,
              currentUiHex = currentUiHex,
              currentTextHex = currentTextHex,
              onNavigateTo = { targetPage ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                currentPage = targetPage
              },
              onClose = onDismiss
            )
          }

          HamburgerPage.THEME -> {
            ThemeFullScreenPage(
              viewModel = viewModel,
              currentUiHex = currentUiHex,
              currentTextHex = currentTextHex,
              currentBgImageUri = currentBgImageUri,
              onBack = { currentPage = HamburgerPage.MAIN_MENU }
            )
          }

          HamburgerPage.DATA -> {
            DataFullScreenPage(
              viewModel = viewModel,
              onBack = { currentPage = HamburgerPage.MAIN_MENU }
            )
          }

          HamburgerPage.PRESETS -> {
            PresetsFullScreenPage(
              presets = presets,
              customPresetInput = customPresetInput,
              onCustomPresetInputChange = { customPresetInput = it },
              onAddPreset = { name -> viewModel.addPreset(name) },
              onDeletePreset = { id -> viewModel.deletePreset(id) },
              onSchedulePreset = { preset -> presetToSchedule = preset },
              onAddPresetToDay = { preset -> viewModel.addTaskFromPreset(preset) },
              onBack = { currentPage = HamburgerPage.MAIN_MENU }
            )
          }

          HamburgerPage.TASKS -> {
            PastTasksFullScreenPage(
              pastTasksGroups = pastTasksGroups,
              onBack = { currentPage = HamburgerPage.MAIN_MENU }
            )
          }
        }
      }
    }
  }

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

// -------------------------------------------------------------
// 1. MAIN MENU SCREEN (Arranged in Rows / Cards)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HamburgerMainMenuScreen(
  presetsCount: Int,
  pastDaysCount: Int,
  currentUiHex: String,
  currentTextHex: String,
  onNavigateTo: (HamburgerPage) -> Unit,
  onClose: () -> Unit
) {
  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Settings & Hub",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "v6.0",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
            }
          }
        },
        actions = {
          IconButton(onClick = onClose, modifier = Modifier.testTag("btn_close_hamburger")) {
            Icon(Icons.Default.Close, contentDescription = "Close Menu")
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = Color(0xFF0B0D13)
        )
      )
    },
    containerColor = Color(0xFF0B0D13)
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 20.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        Text(
          text = "Quick Navigation",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
      }

      // Row 1: Tasks (Past tasks history)
      item {
        HamburgerRowCard(
          icon = Icons.Default.History,
          title = "Tasks (Past History)",
          subtitle = "All past tasks grouped by date with collapse/expand and completion filters",
          badgeText = "$pastDaysCount days",
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
          iconTint = MaterialTheme.colorScheme.primary,
          onClick = { onNavigateTo(HamburgerPage.TASKS) }
        )
      }

      // Row 2: Theme & Appearance
      item {
        HamburgerRowCard(
          icon = Icons.Default.Palette,
          title = "Theme & Colors",
          subtitle = "Customize UI Accent, Text Color, Hex Color Chart, and Background Wallpaper",
          badgeText = currentUiHex,
          containerColor = parseHexColor(currentUiHex, MaterialTheme.colorScheme.primary).copy(alpha = 0.15f),
          iconTint = parseHexColor(currentUiHex, MaterialTheme.colorScheme.primary),
          onClick = { onNavigateTo(HamburgerPage.THEME) }
        )
      }

      // Row 3: Data Management
      item {
        HamburgerRowCard(
          icon = Icons.Default.Storage,
          title = "Data & Backup",
          subtitle = "1-Tap Share, JSON clipboard copy, and restore backups instantly",
          badgeText = "Export / Import",
          containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
          iconTint = MaterialTheme.colorScheme.secondary,
          onClick = { onNavigateTo(HamburgerPage.DATA) }
        )
      }

      // Row 4: Presets & Templates
      item {
        HamburgerRowCard(
          icon = Icons.Default.Tune,
          title = "Presets & Templates",
          subtitle = "Save habit templates and schedule them across multiple dates",
          badgeText = "$presetsCount presets",
          containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f),
          iconTint = MaterialTheme.colorScheme.tertiary,
          onClick = { onNavigateTo(HamburgerPage.PRESETS) }
        )
      }

      item {
        Spacer(modifier = Modifier.height(20.dp))
        Button(
          onClick = onClose,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Return to App")
        }
      }
    }
  }
}

@Composable
private fun HamburgerRowCard(
  icon: ImageVector,
  title: String,
  subtitle: String,
  badgeText: String? = null,
  containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
  iconTint: Color = MaterialTheme.colorScheme.primary,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .clickable { onClick() },
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(iconTint.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(26.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = title,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            if (badgeText != null) {
              Spacer(modifier = Modifier.width(8.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = badgeText,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = "Open",
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

// -------------------------------------------------------------
// 2. PAST TASKS FULL-SCREEN PAGE (With Day Collapse/Expand & Filters)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PastTasksFullScreenPage(
  pastTasksGroups: List<PastDayTasksGroup>,
  onBack: () -> Unit
) {
  val haptic = LocalHapticFeedback.current
  var selectedFilter by remember { mutableStateOf(PastTaskFilter.ALL) }
  var searchQuery by remember { mutableStateOf("") }

  // Collapse / Expand state for days: true = expanded, false = collapsed
  val expandedDays = remember { mutableStateMapOf<String, Boolean>() }

  // Filtered groups
  val filteredGroups = remember(pastTasksGroups, selectedFilter, searchQuery) {
    pastTasksGroups.mapNotNull { group ->
      val matchingTasks = group.tasks.filter { task ->
        val matchesFilter = when (selectedFilter) {
          PastTaskFilter.ALL -> true
          PastTaskFilter.COMPLETED -> task.isCompleted
          PastTaskFilter.NOT_COMPLETED -> !task.isCompleted
        }
        val matchesSearch = searchQuery.isBlank() || task.taskName.contains(searchQuery.trim(), ignoreCase = true)
        matchesFilter && matchesSearch
      }

      if (matchingTasks.isNotEmpty() || (searchQuery.isBlank() && selectedFilter == PastTaskFilter.ALL)) {
        group.copy(tasks = matchingTasks)
      } else {
        null
      }
    }
  }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text("Past Tasks History", fontWeight = FontWeight.Bold)
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = Color(0xFF0B0D13)
        )
      )
    },
    containerColor = Color(0xFF0B0D13)
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp)
    ) {
      // Search Bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search past tasks...", fontSize = 13.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
        trailingIcon = {
          if (searchQuery.isNotBlank()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
            }
          }
        },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Filter Chips: All, Completed, Not Completed & Expand/Collapse All
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          PastTaskFilter.entries.forEach { filter ->
            FilterChip(
              selected = selectedFilter == filter,
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                selectedFilter = filter
              },
              label = {
                Text(
                  text = filter.label,
                  fontSize = 12.sp,
                  fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
              )
            )
          }
        }

        // Expand All / Collapse All Toggle
        val allExpanded = pastTasksGroups.all { expandedDays[it.date] != false }
        OutlinedButton(
          onClick = {
            val newExpanded = !allExpanded
            pastTasksGroups.forEach { expandedDays[it.date] = newExpanded }
          },
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.height(32.dp)
        ) {
          Icon(
            imageVector = if (allExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(if (allExpanded) "Collapse All" else "Expand All", fontSize = 11.sp)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Grouped Days List
      if (filteredGroups.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = if (searchQuery.isNotBlank()) "No past tasks matching \"$searchQuery\"" else "No past tasks logged yet",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(bottom = 60.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(filteredGroups, key = { it.date }) { group ->
            val isExpanded = expandedDays[group.date] != false // default expanded

            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
              border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                // Day Header with collapse/expand toggle
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                      expandedDays[group.date] = !isExpanded
                    }
                    .padding(vertical = 4.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.CalendarMonth,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = group.formattedDate,
                      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    // Completion counter chip
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                          if (group.completedTasksCount == group.totalTasksCount && group.totalTasksCount > 0)
                            RatingBestGreen.copy(alpha = 0.2f)
                          else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Text(
                        text = "${group.completedTasksCount}/${group.totalTasksCount} done",
                        style = MaterialTheme.typography.labelSmall.copy(
                          fontWeight = FontWeight.Bold,
                          fontSize = 11.sp,
                          color = if (group.completedTasksCount == group.totalTasksCount && group.totalTasksCount > 0)
                            RatingBestGreen
                          else MaterialTheme.colorScheme.primary
                        )
                      )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                      text = DateUtils.formatTime(group.totalTimeSeconds),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                      ),
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                      imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                      contentDescription = if (isExpanded) "Collapse" else "Expand",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }

                // Day Tasks List (when expanded)
                if (isExpanded) {
                  Spacer(modifier = Modifier.height(8.dp))

                  if (group.tasks.isEmpty()) {
                    Text(
                      text = "No tasks match the active filter.",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.padding(vertical = 6.dp)
                    )
                  } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                      group.tasks.forEach { task ->
                        PastTaskItemRow(task = task)
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PastTaskItemRow(task: PastTaskUiItem) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(Color.White.copy(alpha = 0.05f))
      .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
      .padding(horizontal = 10.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
        contentDescription = if (task.isCompleted) "Completed" else "Not Completed",
        tint = if (task.isCompleted) RatingBestGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.size(18.dp)
      )

      Spacer(modifier = Modifier.width(8.dp))

      Column {
        Text(
          text = task.taskName,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )
        if (!task.noteText.isNullOrBlank()) {
          Text(
            text = task.noteText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
          )
        }
      }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = DateUtils.formatTime(task.timeSpentSeconds),
        style = MaterialTheme.typography.labelSmall.copy(
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.primary
      )
    }
  }
}

// -------------------------------------------------------------
// 3. THEME FULL-SCREEN PAGE
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ThemeFullScreenPage(
  viewModel: HabitViewModel,
  currentUiHex: String,
  currentTextHex: String,
  currentBgImageUri: String?,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  var activeTarget by remember { mutableStateOf(HexColorTarget.UI) }

  val activeHex = when (activeTarget) {
    HexColorTarget.UI -> currentUiHex
    HexColorTarget.TEXT -> currentTextHex
  }

  var hexInputText by remember(activeTarget, activeHex) {
    mutableStateOf(activeHex.removePrefix("#"))
  }

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

  val currentUiOpacity by viewModel.selectedUiOpacity.collectAsStateWithLifecycle()

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = { Text("Theme & Styling", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF0B0D13))
      )
    },
    containerColor = Color(0xFF0B0D13)
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 20.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
      // 1. Target Selector: UI Accent, Text Color
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Select Element to Customize",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          HexColorTarget.entries.forEach { target ->
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
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent
              ),
              border = androidx.compose.foundation.BorderStroke(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)
              ),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(16.dp)
                  .clip(CircleShape)
                  .background(targetColor)
                  .border(0.5.dp, Color.Gray, CircleShape)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = target.label,
                fontSize = 13.sp,
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
                  HexColorTarget.UI -> viewModel.setCustomUiHex(formatted)
                  HexColorTarget.TEXT -> viewModel.setCustomTextHex(formatted)
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
              .size(52.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(parsedPreview)
              .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
          )
        }
      }

      // 3. Hexadecimal Colour Chart
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(swatchColor)
                    .border(
                      width = if (isSelected) 3.dp else 0.5.dp,
                      color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.2f),
                      shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                      hexInputText = hexCode.removePrefix("#")
                      when (activeTarget) {
                        HexColorTarget.UI -> viewModel.setCustomUiHex(hexCode)
                        HexColorTarget.TEXT -> viewModel.setCustomTextHex(hexCode)
                      }
                    }
                )
              }
            }
          }
        }
      }

      // 4. UI Opacity / Transparency Control Setting
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("UI Card Opacity / Transparency", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
          }
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = "${(currentUiOpacity * 100).toInt()}%",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            )
          }
        }

        Text(
          text = "Adjust the transparency level of UI cards, tiles, and containers over your background:",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Slider(
          value = currentUiOpacity,
          onValueChange = { viewModel.setUiOpacity(it) },
          valueRange = 0.08f..1.0f,
          steps = 18,
          modifier = Modifier.fillMaxWidth()
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          listOf(0.15f to "15% Glass", 0.35f to "35%", 0.60f to "60%", 0.85f to "85%", 1.0f to "Solid").forEach { (presetVal, label) ->
            val isSelected = kotlin.math.abs(currentUiOpacity - presetVal) < 0.06f
            OutlinedButton(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.setUiOpacity(presetVal)
              },
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else Color.Transparent
              ),
              border = androidx.compose.foundation.BorderStroke(
                if (isSelected) 1.5.dp else 0.8.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)
              ),
              modifier = Modifier.height(30.dp)
            ) {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            }
          }
        }
      }

      // 5. Background Wallpaper Image
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Device Background Wallpaper", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        }

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            if (!currentBgImageUri.isNullOrBlank()) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(130.dp)
                  .clip(RoundedCornerShape(10.dp))
              ) {
                AsyncImage(
                  model = currentBgImageUri,
                  contentDescription = "Current Background",
                  modifier = Modifier.fillMaxWidth(),
                  contentScale = ContentScale.Crop
                )
              }
              Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = {
                  bgPhotoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                  )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (currentBgImageUri.isNullOrBlank()) "Select Wallpaper" else "Change Wallpaper")
              }

              if (!currentBgImageUri.isNullOrBlank()) {
                OutlinedButton(
                  onClick = { viewModel.setBackgroundImageUri(null) },
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                  Text("Remove")
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(30.dp))
    }
  }
}

// -------------------------------------------------------------
// 4. DATA FULL-SCREEN PAGE
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DataFullScreenPage(
  viewModel: HabitViewModel,
  onBack: () -> Unit
) {
  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = { Text("Data Backup & Restore", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF0B0D13))
      )
    },
    containerColor = Color(0xFF0B0D13)
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 20.dp)
    ) {
      DataManagementDialogBody(viewModel = viewModel)
    }
  }
}

// -------------------------------------------------------------
// 5. PRESETS FULL-SCREEN PAGE
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PresetsFullScreenPage(
  presets: List<TaskPreset>,
  customPresetInput: String,
  onCustomPresetInputChange: (String) -> Unit,
  onAddPreset: (String) -> Unit,
  onDeletePreset: (Long) -> Unit,
  onSchedulePreset: (TaskPreset) -> Unit,
  onAddPresetToDay: (TaskPreset) -> Unit,
  onBack: () -> Unit
) {
  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = { Text("Presets & Templates", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF0B0D13))
      )
    },
    containerColor = Color(0xFF0B0D13)
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 20.dp)
    ) {
      PresetsCategoryContent(
        presets = presets,
        customPresetInput = customPresetInput,
        onCustomPresetInputChange = onCustomPresetInputChange,
        onAddPreset = onAddPreset,
        onDeletePreset = onDeletePreset,
        onSchedulePreset = onSchedulePreset,
        onAddPresetToDay = onAddPresetToDay
      )
    }
  }
}

// -------------------------------------------------------------
// Data Management Body (Export & Import)
// -------------------------------------------------------------
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
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    statusMessage?.let { msg ->
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(
            if (isSuccess) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
          )
          .border(
            1.dp,
            if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            RoundedCornerShape(12.dp)
          )
          .padding(12.dp)
      ) {
        Text(
          text = msg,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
          color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
      }
    }

    // Export Card (Easy 1-Tap Share & 1-Tap Clipboard Copy)
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.FileUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text("Export Data Backup", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          "Export your tasks, presets, NEET chapters, ratings, and timer logs into portable JSON.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
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
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Share Backup")
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
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Copy JSON")
          }
        }
      }
    }

    // Import Card (1-Tap Paste from Clipboard & 1-Tap Select File)
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text("Import Data Backup", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          "Restore tasks, presets, logs, and events instantly from clipboard or a file.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
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
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Paste & Restore")
          }

          OutlinedButton(
            onClick = { importFileLauncher.launch("*/*") },
            enabled = !isProcessing,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Select File")
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = manualJsonText,
            onValueChange = { manualJsonText = it },
            label = { Text("Paste JSON here") },
            placeholder = { Text("{\"version\": ..., \"tasks\": [...]}") },
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = 100.dp, max = 160.dp),
            shape = RoundedCornerShape(12.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
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
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Import Pasted JSON")
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// Presets Category Content
// -------------------------------------------------------------
@Composable
private fun PresetsCategoryContent(
  presets: List<TaskPreset>,
  customPresetInput: String,
  onCustomPresetInputChange: (String) -> Unit,
  onAddPreset: (String) -> Unit,
  onDeletePreset: (Long) -> Unit,
  onSchedulePreset: (TaskPreset) -> Unit,
  onAddPresetToDay: (TaskPreset) -> Unit
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Text(
      text = "Preset Templates",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    )
    Text(
      text = "Presets are saved habit templates. Add them to today or schedule them across upcoming dates.",
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
        placeholder = { Text("New preset habit...", fontSize = 13.sp) },
        singleLine = true,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp)
      )

      Button(
        onClick = {
          if (customPresetInput.isNotBlank()) {
            onAddPreset(customPresetInput.trim())
            onCustomPresetInputChange("")
          }
        },
        enabled = customPresetInput.isNotBlank(),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Add")
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Presets list
    if (presets.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 24.dp),
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(presets, key = { it.id }) { preset ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = preset.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
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

                IconButton(
                  onClick = { onDeletePreset(preset.id) },
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Button(
                  onClick = { onAddPresetToDay(preset) },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.height(32.dp)
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Add to Today", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                  onClick = { onSchedulePreset(preset) },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.height(32.dp)
                ) {
                  Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Schedule...",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp)
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
