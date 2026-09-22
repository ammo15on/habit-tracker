package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.NeetChapter
import com.example.data.model.NeetTallyCounter
import com.example.data.model.NeetTestScore
import com.example.data.model.RatingType
import com.example.ui.AnalyticsTab
import com.example.ui.DaySummary
import com.example.ui.HabitViewModel
import com.example.ui.MonthSummary
import com.example.ui.TaskTallyItem
import com.example.ui.WeekSummary
import com.example.ui.components.AddEditChapterDialog
import com.example.ui.components.AddEditTallyDialog
import com.example.ui.components.AddNeetScoreDialog
import com.example.ui.components.CompletedDayTasksDialog
import com.example.ui.theme.RatingAverageGrey
import com.example.ui.theme.RatingBestGreen
import com.example.ui.theme.RatingWorstBlack
import com.example.util.DateUtils

@Composable
fun AnalyticsScreen(
  viewModel: HabitViewModel,
  onNavigateToDate: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  val selectedTab by viewModel.selectedAnalyticsTab.collectAsStateWithLifecycle()
  val weeksAnalytics by viewModel.weeksAnalytics.collectAsStateWithLifecycle()
  val monthsAnalytics by viewModel.monthsAnalytics.collectAsStateWithLifecycle()
  val daysAnalytics by viewModel.daysAnalytics.collectAsStateWithLifecycle()
  val tallyAnalytics by viewModel.taskTallyAnalytics.collectAsStateWithLifecycle()
  val neetScores by viewModel.allNeetScores.collectAsStateWithLifecycle()
  val neetChapters by viewModel.allNeetChapters.collectAsStateWithLifecycle()
  val neetTallyCounters by viewModel.allNeetTallyCounters.collectAsStateWithLifecycle()

  var selectedDateForTasks by remember { mutableStateOf<String?>(null) }
  val dayTasks by remember(selectedDateForTasks, daysAnalytics) {
    derivedStateOf {
      selectedDateForTasks?.let { viewModel.getDayCompletedTasks(it) } ?: emptyList()
    }
  }

  var showAddNeetDialog by remember { mutableStateOf(false) }
  var showAddChapterDialog by remember { mutableStateOf(false) }
  var chapterToEdit by remember { mutableStateOf<NeetChapter?>(null) }
  var showAddTallyDialog by remember { mutableStateOf(false) }
  var tallyToEdit by remember { mutableStateOf<NeetTallyCounter?>(null) }

  val coroutineScope = rememberCoroutineScope()
  val pagerState = rememberPagerState(initialPage = selectedTab.ordinal, pageCount = { AnalyticsTab.entries.size })

  LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.settledPage }.collect { page ->
      val tab = AnalyticsTab.entries[page]
      if (viewModel.selectedAnalyticsTab.value != tab) {
        viewModel.selectedAnalyticsTab.value = tab
      }
    }
  }

  LaunchedEffect(selectedTab) {
    if (pagerState.currentPage != selectedTab.ordinal) {
      pagerState.animateScrollToPage(selectedTab.ordinal)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(12.dp))

    // Header Title
    Text(
      text = "Habit & Exam Analytics",
      style = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
      ),
      color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Scrollable Tab Row: Day, Week, Month, NEET (Strictly arranged in order, transparent background)
    ScrollableTabRow(
      selectedTabIndex = selectedTab.ordinal,
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .testTag("analytics_tab_row"),
      containerColor = Color.Transparent,
      divider = {},
      edgePadding = 0.dp
    ) {
      Tab(
        selected = selectedTab == AnalyticsTab.DAY,
        onClick = {
          coroutineScope.launch {
            pagerState.animateScrollToPage(AnalyticsTab.DAY.ordinal)
          }
        },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Day", fontWeight = FontWeight.Bold)
          }
        },
        modifier = Modifier.testTag("tab_day")
      )
      Tab(
        selected = selectedTab == AnalyticsTab.WEEK,
        onClick = {
          coroutineScope.launch {
            pagerState.animateScrollToPage(AnalyticsTab.WEEK.ordinal)
          }
        },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ViewWeek, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Week", fontWeight = FontWeight.Bold)
          }
        },
        modifier = Modifier.testTag("tab_week")
      )
      Tab(
        selected = selectedTab == AnalyticsTab.MONTH,
        onClick = {
          coroutineScope.launch {
            pagerState.animateScrollToPage(AnalyticsTab.MONTH.ordinal)
          }
        },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Month", fontWeight = FontWeight.Bold)
          }
        },
        modifier = Modifier.testTag("tab_month")
      )
      Tab(
        selected = selectedTab == AnalyticsTab.NEET,
        onClick = {
          coroutineScope.launch {
            pagerState.animateScrollToPage(AnalyticsTab.NEET.ordinal)
          }
        },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("NEET", fontWeight = FontWeight.Bold)
          }
        },
        modifier = Modifier.testTag("tab_neet")
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    HorizontalPager(
      state = pagerState,
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    ) { page ->
      when (AnalyticsTab.entries[page]) {
        AnalyticsTab.DAY -> {
          DaysAnalyticsView(
            days = daysAnalytics,
            onDayClick = { date ->
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              selectedDateForTasks = date
            }
          )
        }
        AnalyticsTab.WEEK -> {
          WeeksAnalyticsView(
            weeks = weeksAnalytics,
            onDayClick = { date ->
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              selectedDateForTasks = date
            }
          )
        }
        AnalyticsTab.MONTH -> {
          MonthsAnalyticsView(
            months = monthsAnalytics,
            onDayClick = { date ->
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              selectedDateForTasks = date
            }
          )
        }
        AnalyticsTab.NEET -> {
          NeetSectionView(
            chapters = neetChapters,
            tallyCounters = neetTallyCounters,
            habitTallyList = tallyAnalytics,
            testScores = neetScores,
            onToggleChapterCompleted = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              viewModel.toggleChapterCompleted(it)
            },
            onToggleChapterPyq = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              viewModel.toggleChapterPyq(it)
            },
            onToggleChapterRevision = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              viewModel.toggleChapterRevision(it)
            },
            onAddChapterClick = { showAddChapterDialog = true },
            onEditChapterClick = { chapterToEdit = it },
            onDeleteChapterClick = { viewModel.deleteNeetChapter(it) },
            onAddTallyClick = { showAddTallyDialog = true },
            onEditTallyClick = { tallyToEdit = it },
            onIncrementTally = { counter, delta ->
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              viewModel.incrementNeetTally(counter, delta)
            },
            onDecrementTally = { counter, delta ->
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              viewModel.decrementNeetTally(counter, delta)
            },
            onResetTally = { viewModel.resetNeetTally(it) },
            onDeleteTally = { viewModel.deleteNeetTallyCounter(it) },
            onAddScoreClick = { showAddNeetDialog = true },
            onDeleteScoreClick = { viewModel.deleteNeetScore(it) }
          )
        }
      }
    }
  }

  // Completed Tasks for Selected Day Dialog
  selectedDateForTasks?.let { date ->
    CompletedDayTasksDialog(
      date = date,
      tasks = dayTasks,
      onDismiss = { selectedDateForTasks = null },
      onNavigateToTracker = { targetDate ->
        selectedDateForTasks = null
        onNavigateToDate(targetDate)
      }
    )
  }

  // Add NEET Test Score Dialog
  if (showAddNeetDialog) {
    AddNeetScoreDialog(
      onDismiss = { showAddNeetDialog = false },
      onConfirm = { newScore ->
        viewModel.addNeetScore(newScore)
        showAddNeetDialog = false
      }
    )
  }

  // Add Chapter Dialog
  if (showAddChapterDialog) {
    AddEditChapterDialog(
      chapter = null,
      onDismiss = { showAddChapterDialog = false },
      onSave = { name, subject, isCompleted, isPyqDone, isRevisionDone, notes ->
        viewModel.addNeetChapter(
          name = name,
          subject = subject,
          isCompleted = isCompleted,
          isPyqDone = isPyqDone,
          isRevisionDone = isRevisionDone,
          notes = notes
        )
        showAddChapterDialog = false
      }
    )
  }

  // Edit Chapter Dialog
  chapterToEdit?.let { chapter ->
    AddEditChapterDialog(
      chapter = chapter,
      onDismiss = { chapterToEdit = null },
      onSave = { name, subject, isCompleted, isPyqDone, isRevisionDone, notes ->
        viewModel.updateNeetChapter(
          chapter.copy(
            name = name,
            subject = subject,
            isCompleted = isCompleted,
            isPyqDone = isPyqDone,
            isRevisionDone = isRevisionDone,
            notes = notes
          )
        )
        chapterToEdit = null
      },
      onDelete = {
        viewModel.deleteNeetChapter(chapter)
        chapterToEdit = null
      }
    )
  }

  // Add Tally Counter Dialog
  if (showAddTallyDialog) {
    AddEditTallyDialog(
      counter = null,
      onDismiss = { showAddTallyDialog = false },
      onSave = { title, initialCount, target, unit ->
        viewModel.addNeetTallyCounter(
          title = title,
          initialCount = initialCount,
          target = target,
          unit = unit
        )
        showAddTallyDialog = false
      }
    )
  }

  // Edit Tally Counter Dialog
  tallyToEdit?.let { counter ->
    AddEditTallyDialog(
      counter = counter,
      onDismiss = { tallyToEdit = null },
      onSave = { title, count, target, unit ->
        viewModel.updateNeetTallyCounter(
          counter.copy(
            title = title,
            count = count,
            target = target,
            unit = unit
          )
        )
        tallyToEdit = null
      },
      onDelete = {
        viewModel.deleteNeetTallyCounter(counter)
        tallyToEdit = null
      },
      onReset = {
        viewModel.resetNeetTally(counter)
        tallyToEdit = null
      }
    )
  }
}

// -------------------------------------------------------------
// 1. WEEKS VIEW
// -------------------------------------------------------------
@Composable
private fun WeeksAnalyticsView(
  weeks: List<WeekSummary>,
  onDayClick: (String) -> Unit
) {
  val totalBest = weeks.sumOf { it.bestCount }
  val totalAverage = weeks.sumOf { it.averageCount }
  val totalWorst = weeks.sumOf { it.worstCount }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      RatingCountSummaryCard(
        bestCount = totalBest,
        averageCount = totalAverage,
        worstCount = totalWorst,
        title = "Weekly Ratings Overview"
      )
    }

    items(weeks) { week ->
      WeekSummaryCard(week = week, onDayClick = onDayClick)
    }
  }
}

@Composable
private fun WeekSummaryCard(
  week: WeekSummary,
  onDayClick: (String) -> Unit
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
        .border(
          width = 1.dp,
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
          shape = RoundedCornerShape(16.dp)
        )
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = week.weekLabel,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Schedule,
              contentDescription = null,
              modifier = Modifier.size(13.dp),
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Total: ${DateUtils.formatTime(week.totalTimeSeconds)}",
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
              ),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
              .padding(horizontal = 6.dp, vertical = 4.dp)
          ) {
            Text(
              text = "😊 ${week.bestCount}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
          }
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
              .padding(horizontal = 6.dp, vertical = 4.dp)
          ) {
            Text(
              text = "😐 ${week.averageCount}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
          }
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
              .padding(horizontal = 6.dp, vertical = 4.dp)
          ) {
            Text(
              text = "😞 ${week.worstCount}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 7 Days row (Mon..Sun)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        week.days.forEach { day ->
          DayRatingBadge(
            day = day,
            onClick = { onDayClick(day.date) }
          )
        }
      }
    }
  }
}

// -------------------------------------------------------------
// 2. MONTHS VIEW
// -------------------------------------------------------------
@Composable
private fun MonthsAnalyticsView(
  months: List<MonthSummary>,
  onDayClick: (String) -> Unit
) {
  val totalBest = months.sumOf { it.bestCount }
  val totalAverage = months.sumOf { it.averageCount }
  val totalWorst = months.sumOf { it.worstCount }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      RatingCountSummaryCard(
        bestCount = totalBest,
        averageCount = totalAverage,
        worstCount = totalWorst,
        title = "Monthly Ratings Overview"
      )
    }

    items(months) { month ->
      MonthSummaryCard(month = month, onDayClick = onDayClick)
    }
  }
}

@Composable
private fun MonthSummaryCard(
  month: MonthSummary,
  onDayClick: (String) -> Unit
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
        .border(
          width = 1.dp,
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
          shape = RoundedCornerShape(16.dp)
        )
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = month.monthLabel,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Total Active: ${DateUtils.formatTime(month.totalTimeSeconds)}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
              .padding(horizontal = 6.dp, vertical = 4.dp)
          ) {
            Text(
              text = "😊 ${month.bestCount}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
          }
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
              .padding(horizontal = 6.dp, vertical = 4.dp)
          ) {
            Text(
              text = "😐 ${month.averageCount}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
          }
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
              .padding(horizontal = 6.dp, vertical = 4.dp)
          ) {
            Text(
              text = "😞 ${month.worstCount}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Weekday column headers
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        listOf("M", "T", "W", "T", "F", "S", "S").forEach { dayHeader ->
          Text(
            text = dayHeader,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Month grid non-lazy chunked by 7 for 100% smooth scroll without jank
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        month.days.chunked(7).forEach { weekChunk ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            weekChunk.forEach { day ->
              Box(modifier = Modifier.weight(1f)) {
                MiniDayGridCell(
                  day = day,
                  onClick = { onDayClick(day.date) }
                )
              }
            }
            if (weekChunk.size < 7) {
              repeat(7 - weekChunk.size) {
                Spacer(modifier = Modifier.weight(1f))
              }
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// 3. DAYS VIEW
// -------------------------------------------------------------
@Composable
private fun DaysAnalyticsView(
  days: List<DaySummary>,
  onDayClick: (String) -> Unit
) {
  val totalBest = days.count { it.rating == RatingType.BEST }
  val totalAverage = days.count { it.rating == RatingType.AVERAGE }
  val totalWorst = days.count { it.rating == RatingType.WORST }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      RatingCountSummaryCard(
        bestCount = totalBest,
        averageCount = totalAverage,
        worstCount = totalWorst,
        title = "Daily Ratings Overview"
      )
    }

    items(days) { day ->
      DayAnalyticsRow(day = day, onClick = { onDayClick(day.date) })
    }
  }
}

@Composable
private fun DayAnalyticsRow(
  day: DaySummary,
  onClick: () -> Unit
) {
  val rating = day.rating
  val ratingBg = when (rating) {
    RatingType.BEST -> RatingBestGreen.copy(alpha = 0.2f)
    RatingType.AVERAGE -> RatingAverageGrey.copy(alpha = 0.4f)
    RatingType.WORST -> RatingWorstBlack.copy(alpha = 0.2f)
    null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ratingBg),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = when (rating) {
              RatingType.BEST -> "😊"
              RatingType.AVERAGE -> "😐"
              RatingType.WORST -> "😞"
              null -> day.dayOfWeekAbbr
            },
            fontSize = if (rating != null) 18.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = DateUtils.formatFullDate(day.date),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Rating: ${rating?.label ?: "Unrated"}  •  Tasks: ${day.completedTasksCount}/${day.totalTasksCount}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Text(
        text = DateUtils.formatTime(day.totalTimeSeconds),
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        ),
        color = MaterialTheme.colorScheme.primary
      )
    }
  }
}

@Composable
private fun RatingCountSummaryCard(
  bestCount: Int,
  averageCount: Int,
  worstCount: Int,
  title: String
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    )
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        RatingCountEmojiChip(
          emoji = "😊",
          label = "Best",
          count = bestCount,
          modifier = Modifier.weight(1f)
        )
        RatingCountEmojiChip(
          emoji = "😐",
          label = "Average",
          count = averageCount,
          modifier = Modifier.weight(1f)
        )
        RatingCountEmojiChip(
          emoji = "😞",
          label = "Worst",
          count = worstCount,
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun RatingCountEmojiChip(
  emoji: String,
  label: String,
  count: Int,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Text(text = emoji, fontSize = 18.sp)
      Spacer(modifier = Modifier.width(6.dp))
      Column {
        Text(
          text = "$count",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

// -------------------------------------------------------------
// 4. TASK TALLY VIEW
// -------------------------------------------------------------
@Composable
private fun TaskTallyView(
  tallyList: List<TaskTallyItem>
) {
  if (tallyList.isEmpty()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "No habits created yet. Add habits to view their completion tally!",
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  } else {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(bottom = 80.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Repetition Tally: Total number of days you marked each habit completed.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      items(tallyList) { item ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = item.taskName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Total Time Logged: ${DateUtils.formatTime(item.totalTimeSeconds)}",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(RatingBestGreen.copy(alpha = 0.15f))
                .padding(horizontal = 14.dp, vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "${item.completionCount}",
                  style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = RatingBestGreen
                  )
                )
                Text(
                  text = "times done",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = RatingBestGreen
                  )
                )
              }
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// 5. NEET TEST MARKS TRACKING VIEW
// -------------------------------------------------------------
@Composable
private fun NeetMarksTrackingView(
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
            text = "Track Physics, Chemistry, Botany & Zoology",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Button(
          onClick = onAddScoreClick,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = RatingBestGreen)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Log Marks")
        }
      }
    }

    if (scores.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(48.dp), tint = RatingBestGreen)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No NEET test scores logged yet",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Log marks of Physics, Chem, Botany and Zoology tests to track your improvement curve.",
              style = MaterialTheme.typography.bodySmall,
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      // Recent average banner
      item {
        val avgTotal = scores.map { it.totalScore }.average().toInt()
        val highest = scores.maxOfOrNull { it.totalScore } ?: 0

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Average Score", style = MaterialTheme.typography.labelSmall)
              Text(
                text = "$avgTotal / 720",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Highest Score", style = MaterialTheme.typography.labelSmall)
              Text(
                text = "$highest / 720",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = RatingBestGreen
                )
              )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Tests Given", style = MaterialTheme.typography.labelSmall)
              Text(
                text = "${scores.size}",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold
                )
              )
            }
          }
        }
      }

      items(scores) { score ->
        NeetScoreCard(score = score, onDelete = { onDeleteScore(score) })
      }
    }
  }
}

@Composable
private fun NeetScoreCard(
  score: NeetTestScore,
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
        Column {
          Text(
            text = score.testName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = DateUtils.formatFullDate(score.date),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(RatingBestGreen.copy(alpha = 0.15f))
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Text(
              text = "${score.totalScore} / 720 (%.1f%%)".format(score.percentage),
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = RatingBestGreen
              )
            )
          }

          IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 4 Subject breakdown chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        SubjectScoreBadge("Physics", score.physicsScore, modifier = Modifier.weight(1f))
        SubjectScoreBadge("Chemistry", score.chemistryScore, modifier = Modifier.weight(1f))
        SubjectScoreBadge("Botany", score.botanyScore, modifier = Modifier.weight(1f))
        SubjectScoreBadge("Zoology", score.zoologyScore, modifier = Modifier.weight(1f))
      }
    }
  }
}

@Composable
private fun SubjectScoreBadge(
  name: String,
  score: Int,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      .padding(vertical = 6.dp, horizontal = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = name,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = "$score",
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        ),
        color = if (score >= 140) RatingBestGreen else MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

// -------------------------------------------------------------
// Small UI helpers
// -------------------------------------------------------------
@Composable
private fun DayRatingBadge(
  day: DaySummary,
  onClick: () -> Unit
) {
  val rating = day.rating
  val bg = when (rating) {
    RatingType.BEST -> RatingBestGreen.copy(alpha = 0.2f)
    RatingType.AVERAGE -> RatingAverageGrey.copy(alpha = 0.4f)
    RatingType.WORST -> RatingWorstBlack.copy(alpha = 0.2f)
    null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
  }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clickable { onClick() }
      .padding(2.dp)
  ) {
    Text(
      text = day.dayOfWeekAbbr,
      style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(3.dp))
    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(bg),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = when (rating) {
          RatingType.BEST -> "😊"
          RatingType.AVERAGE -> "😐"
          RatingType.WORST -> "😞"
          null -> "-"
        },
        fontSize = if (rating != null) 14.sp else 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

@Composable
private fun MiniDayGridCell(
  day: DaySummary,
  onClick: () -> Unit
) {
  val rating = day.rating
  val bg = when (rating) {
    RatingType.BEST -> RatingBestGreen.copy(alpha = 0.22f)
    RatingType.AVERAGE -> RatingAverageGrey.copy(alpha = 0.4f)
    RatingType.WORST -> RatingWorstBlack.copy(alpha = 0.18f)
    null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
  }

  val dayNum = try {
    day.date.split("-").last().toInt().toString()
  } catch (e: Exception) {
    ""
  }

  Box(
    modifier = Modifier
      .aspectRatio(1f)
      .clip(RoundedCornerShape(6.dp))
      .background(bg)
      .clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = dayNum,
        fontSize = 9.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
      )
      if (rating != null) {
        Text(
          text = when (rating) {
            RatingType.BEST -> "😊"
            RatingType.AVERAGE -> "😐"
            RatingType.WORST -> "😞"
          },
          fontSize = 10.sp
        )
      }
    }
  }
}
