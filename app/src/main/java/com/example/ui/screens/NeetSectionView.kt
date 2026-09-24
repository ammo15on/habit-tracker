package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NeetChapter
import com.example.data.model.NeetTallyCounter
import com.example.data.model.NeetTestScore
import com.example.ui.TaskTallyItem
import com.example.ui.components.NeetProgressWidgetCard
import com.example.ui.theme.RatingBestGreen
import com.example.util.DateUtils

enum class NeetSubTab {
  CHAPTERS,
  TALLY,
  TEST_MARKS
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun NeetSectionView(
  chapters: List<NeetChapter>,
  tallyCounters: List<NeetTallyCounter>,
  habitTallyList: List<TaskTallyItem>,
  testScores: List<NeetTestScore>,
  onToggleChapterCompleted: (NeetChapter) -> Unit,
  onToggleChapterPyq: (NeetChapter) -> Unit,
  onToggleChapterRevision: (NeetChapter) -> Unit,
  onAddChapterClick: () -> Unit,
  onEditChapterClick: (NeetChapter) -> Unit,
  onDeleteChapterClick: (NeetChapter) -> Unit,
  onAddTallyClick: () -> Unit,
  onEditTallyClick: (NeetTallyCounter) -> Unit,
  onIncrementTally: (NeetTallyCounter, Int) -> Unit,
  onDecrementTally: (NeetTallyCounter, Int) -> Unit,
  onResetTally: (NeetTallyCounter) -> Unit,
  onDeleteTally: (NeetTallyCounter) -> Unit,
  onAddScoreClick: () -> Unit,
  onDeleteScoreClick: (NeetTestScore) -> Unit
) {
  var selectedSubTab by remember { mutableStateOf(NeetSubTab.CHAPTERS) }

  Column(modifier = Modifier.fillMaxSize()) {
    // Inner NEET Sub Navigation Tabs
    SecondaryTabRow(
      selectedTabIndex = selectedSubTab.ordinal,
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .testTag("neet_sub_tabs"),
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
      Tab(
        selected = selectedSubTab == NeetSubTab.CHAPTERS,
        onClick = { selectedSubTab = NeetSubTab.CHAPTERS },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(5.dp))
            val completedCount = chapters.count { it.isCompleted }
            Text("Chapters ($completedCount/${chapters.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      )
      Tab(
        selected = selectedSubTab == NeetSubTab.TALLY,
        onClick = { selectedSubTab = NeetSubTab.TALLY },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text("Tally (${tallyCounters.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      )
      Tab(
        selected = selectedSubTab == NeetSubTab.TEST_MARKS,
        onClick = { selectedSubTab = NeetSubTab.TEST_MARKS },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text("Test Marks", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    when (selectedSubTab) {
      NeetSubTab.CHAPTERS -> {
        NeetChaptersView(
          chapters = chapters,
          onToggleCompleted = onToggleChapterCompleted,
          onTogglePyq = onToggleChapterPyq,
          onToggleRevision = onToggleChapterRevision,
          onAddChapterClick = onAddChapterClick,
          onEditChapterClick = onEditChapterClick,
          onDeleteChapterClick = onDeleteChapterClick
        )
      }
      NeetSubTab.TALLY -> {
        NeetTallyCountersView(
          counters = tallyCounters,
          habitTallyList = habitTallyList,
          onAddCounterClick = onAddTallyClick,
          onEditCounterClick = onEditTallyClick,
          onIncrement = onIncrementTally,
          onDecrement = onDecrementTally,
          onReset = onResetTally,
          onDelete = onDeleteTally
        )
      }
      NeetSubTab.TEST_MARKS -> {
        NeetTestScoresView(
          scores = testScores,
          onAddScoreClick = onAddScoreClick,
          onDeleteScore = onDeleteScoreClick
        )
      }
    }
  }
}

// ----------------------------------------------------------------------------
// 1. NEET CHAPTERS COMPONENT (Completed, To Complete, PYQ Done with tick marks)
// ----------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NeetChaptersView(
  chapters: List<NeetChapter>,
  onToggleCompleted: (NeetChapter) -> Unit,
  onTogglePyq: (NeetChapter) -> Unit,
  onToggleRevision: (NeetChapter) -> Unit,
  onAddChapterClick: () -> Unit,
  onEditChapterClick: (NeetChapter) -> Unit,
  onDeleteChapterClick: (NeetChapter) -> Unit
) {
  var selectedSubjectFilter by remember { mutableStateOf("All") }
  var selectedStatusFilter by remember { mutableStateOf("All") }

  val totalCount = chapters.size
  val completedCount = chapters.count { it.isCompleted }
  val toCompleteCount = totalCount - completedCount
  val pyqDoneCount = chapters.count { it.isPyqDone }

  val filteredChapters = chapters.filter { chapter ->
    val matchesSubject = (selectedSubjectFilter == "All" || chapter.subject.equals(selectedSubjectFilter, ignoreCase = true))
    val matchesStatus = when (selectedStatusFilter) {
      "To Complete" -> !chapter.isCompleted
      "Completed" -> chapter.isCompleted
      "PYQ Done" -> chapter.isPyqDone
      else -> true
    }
    matchesSubject && matchesStatus
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // 1. Home Screen Widget Component (Chapters, PYQ, NCERT completed out of total)
    item {
      NeetProgressWidgetCard(
        chapters = chapters,
        showPinButton = true
      )
    }

    // 2. Filters & Add Chapter button
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Chapters List (${filteredChapters.size})",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )

          Button(
            onClick = onAddChapterClick,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RatingBestGreen),
            modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 32.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("Add Chapter", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Subject Filter chips
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          listOf("All", "Botany", "Zoology", "Physics", "Chemistry").forEach { subj ->
            FilterChip(
              selected = (selectedSubjectFilter == subj),
              onClick = { selectedSubjectFilter = subj },
              label = { Text(subj, fontSize = 11.sp) },
              shape = RoundedCornerShape(8.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Status Filter chips
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          listOf("All", "To Complete", "Completed", "PYQ Done").forEach { st ->
            FilterChip(
              selected = (selectedStatusFilter == st),
              onClick = { selectedStatusFilter = st },
              label = { Text(st, fontSize = 11.sp) },
              shape = RoundedCornerShape(8.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
              )
            )
          }
        }
      }
    }

    // 3. Chapters List
    if (filteredChapters.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text("No chapters match this filter", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              "Tap '+ Add Chapter' above to add custom chapters or topics.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(filteredChapters, key = { it.id }) { chapter ->
        ChapterItemCard(
          chapter = chapter,
          onToggleCompleted = { onToggleCompleted(chapter) },
          onTogglePyq = { onTogglePyq(chapter) },
          onToggleRevision = { onToggleRevision(chapter) },
          onEdit = { onEditChapterClick(chapter) },
          onDelete = { onDeleteChapterClick(chapter) }
        )
      }
    }
  }
}

@Composable
private fun ChapterItemCard(
  chapter: NeetChapter,
  onToggleCompleted: () -> Unit,
  onTogglePyq: () -> Unit,
  onToggleRevision: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  val subjectColor = when (chapter.subject.lowercase()) {
    "botany" -> Color(0xFF059669)
    "zoology" -> Color(0xFFD97706)
    "physics" -> Color(0xFF2563EB)
    "chemistry" -> Color(0xFF7C3AED)
    else -> MaterialTheme.colorScheme.primary
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .border(
          width = 1.dp,
          color = if (chapter.isCompleted) RatingBestGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
          shape = RoundedCornerShape(14.dp)
        )
        .padding(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Subject pill
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(subjectColor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text(
            text = chapter.subject,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = subjectColor
          )
        }

        // Action icons
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Chapter", modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Chapter", modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.error)
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Chapter Title (clickable to edit)
      Text(
        text = chapter.name,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.clickable { onEdit() }
      )

      if (chapter.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = chapter.notes,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 3 Tick Mark Buttons with clear visual indicators
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // 1. Chapter Completed Tick
        TickButton(
          label = if (chapter.isCompleted) "✓ Completed" else "To Complete",
          isChecked = chapter.isCompleted,
          activeColor = RatingBestGreen,
          onClick = onToggleCompleted,
          modifier = Modifier.weight(1f)
        )

        // 2. PYQ Done Tick
        TickButton(
          label = if (chapter.isPyqDone) "✓ PYQ Done" else "PYQ Done",
          isChecked = chapter.isPyqDone,
          activeColor = Color(0xFF2563EB),
          onClick = onTogglePyq,
          modifier = Modifier.weight(1f)
        )

        // 3. NCERT / Revision Tick
        TickButton(
          label = if (chapter.isRevisionDone) "✓ NCERT" else "NCERT Read",
          isChecked = chapter.isRevisionDone,
          activeColor = Color(0xFFD97706),
          onClick = onToggleRevision,
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun TickButton(
  label: String,
  isChecked: Boolean,
  activeColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (isChecked) activeColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      .border(
        width = 1.dp,
        color = if (isChecked) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
      )
      .clickable { onClick() }
      .padding(vertical = 6.dp, horizontal = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
        contentDescription = null,
        modifier = Modifier.size(13.dp),
        tint = if (isChecked) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
        color = if (isChecked) activeColor else MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

// ----------------------------------------------------------------------------
// 2. NEET TALLY COUNTERS COMPONENT (bot ncert read, bot q, etc. + add/edit)
// ----------------------------------------------------------------------------
@Composable
private fun NeetTallyCountersView(
  counters: List<NeetTallyCounter>,
  habitTallyList: List<TaskTallyItem>,
  onAddCounterClick: () -> Unit,
  onEditCounterClick: (NeetTallyCounter) -> Unit,
  onIncrement: (NeetTallyCounter, Int) -> Unit,
  onDecrement: (NeetTallyCounter, Int) -> Unit,
  onReset: (NeetTallyCounter) -> Unit,
  onDelete: (NeetTallyCounter) -> Unit
) {
  var showHabitRepetitions by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Header & Add Counter
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Study Activity Tally",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "Count NCERT readings, question practice, & revisions",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Button(
          onClick = onAddCounterClick,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = RatingBestGreen),
          modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 32.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(3.dp))
          Text("Add Tally", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // Tally Counter Items
    if (counters.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text("No tally counters yet", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              "Tap '+ Add Tally' above to start counting questions and NCERT reads.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(counters, key = { it.id }) { counter ->
        NeetTallyCard(
          counter = counter,
          onIncrement = { delta -> onIncrement(counter, delta) },
          onDecrement = { delta -> onDecrement(counter, delta) },
          onReset = { onReset(counter) },
          onEdit = { onEditCounterClick(counter) },
          onDelete = { onDelete(counter) }
        )
      }
    }

    // Expandable Habit Repetitions Tally
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showHabitRepetitions = !showHabitRepetitions },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Habit Repetitions Tally (${habitTallyList.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
            }
            Icon(
              imageVector = if (showHabitRepetitions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          AnimatedVisibility(visible = showHabitRepetitions) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              if (habitTallyList.isEmpty()) {
                Text(
                  text = "No daily habit completions logged yet.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              } else {
                habitTallyList.forEach { habit ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                      .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text(habit.taskName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                      Text(
                        text = "Time logged: ${DateUtils.formatTime(habit.totalTimeSeconds)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }

                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RatingBestGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                      Text(
                        text = "${habit.completionCount} done",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = RatingBestGreen
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
  }
}

@Composable
private fun NeetTallyCard(
  counter: NeetTallyCounter,
  onIncrement: (Int) -> Unit,
  onDecrement: (Int) -> Unit,
  onReset: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = counter.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = if (counter.target > 0) "Goal: ${counter.target} ${counter.unit}" else "Unit: ${counter.unit}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Tally", modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Tally", modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.error)
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Big Count Display
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.Bottom) {
          Text(
            text = "${counter.count}",
            style = MaterialTheme.typography.headlineLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              fontSize = 36.sp,
              color = RatingBestGreen
            )
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = counter.unit,
            style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            modifier = Modifier.padding(bottom = 6.dp)
          )
        }

        // Quick decrement & reset
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          OutlinedButton(
            onClick = onReset,
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 28.dp)
          ) {
            Icon(Icons.Default.RestartAlt, contentDescription = "Reset", modifier = Modifier.size(12.dp))
          }

          OutlinedButton(
            onClick = { onDecrement(1) },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 28.dp)
          ) {
            Text("-1", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      if (counter.target > 0) {
        Spacer(modifier = Modifier.height(6.dp))
        val progress = (counter.count.toFloat() / counter.target).coerceIn(0f, 1f)
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = RatingBestGreen,
          trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Quick Increment buttons (+1, +5, +10)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = { onIncrement(1) },
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = RatingBestGreen),
          modifier = Modifier.weight(1f)
        ) {
          Text("+1", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Button(
          onClick = { onIncrement(5) },
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = RatingBestGreen.copy(alpha = 0.85f)),
          modifier = Modifier.weight(1f)
        ) {
          Text("+5", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Button(
          onClick = { onIncrement(10) },
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = RatingBestGreen.copy(alpha = 0.7f)),
          modifier = Modifier.weight(1f)
        ) {
          Text("+10", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }
    }
  }
}

// ----------------------------------------------------------------------------
// 3. NEET TEST SCORES COMPONENT
// ----------------------------------------------------------------------------
@Composable
private fun NeetTestScoresView(
  scores: List<NeetTestScore>,
  onAddScoreClick: () -> Unit,
  onDeleteScore: (NeetTestScore) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "NEET Exam Test Tracker",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Physics, Chemistry, Botany & Zoology out of 720",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Button(
          onClick = onAddScoreClick,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = RatingBestGreen),
          modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 32.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(3.dp))
          Text("Log Marks", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    if (scores.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text("No test scores logged yet", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              "Tap 'Log Marks' above to track your mock test performance and accuracy.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      // Summary score metrics
      item {
        val avgScore = scores.map { it.totalScore }.average().toInt()
        val maxScore = scores.maxOf { it.totalScore }
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Average Score", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("$avgScore / 720", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = RatingBestGreen))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Highest Score", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("$maxScore / 720", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF2563EB)))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Tests Given", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("${scores.size}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFFD97706)))
            }
          }
        }
      }

      items(scores, key = { it.id }) { score ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(score.testName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(score.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "${score.totalScore} / 720",
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = RatingBestGreen
                  )
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = { onDeleteScore(score) }, modifier = Modifier.size(28.dp)) {
                  Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.error)
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Breakdown
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Phy: ${score.physicsScore}/180", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("Chem: ${score.chemistryScore}/180", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("Bot: ${score.botanyScore}/180", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("Zoo: ${score.zoologyScore}/180", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }
  }
}
