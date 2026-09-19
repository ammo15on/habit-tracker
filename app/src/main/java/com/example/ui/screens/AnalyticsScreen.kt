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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.NeetTestScore
import com.example.data.model.RatingType
import com.example.ui.AnalyticsTab
import com.example.ui.DaySummary
import com.example.ui.HabitViewModel
import com.example.ui.MonthSummary
import com.example.ui.TaskTallyItem
import com.example.ui.WeekSummary
import com.example.ui.components.AddNeetScoreDialog
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
  val selectedTab by viewModel.selectedAnalyticsTab.collectAsStateWithLifecycle()
  val weeksAnalytics by viewModel.weeksAnalytics.collectAsStateWithLifecycle()
  val monthsAnalytics by viewModel.monthsAnalytics.collectAsStateWithLifecycle()
  val daysAnalytics by viewModel.daysAnalytics.collectAsStateWithLifecycle()
  val tallyAnalytics by viewModel.taskTallyAnalytics.collectAsStateWithLifecycle()
  val neetScores by viewModel.allNeetScores.collectAsStateWithLifecycle()

  var showAddNeetDialog by remember { mutableStateOf(false) }

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

    // Scrollable Tab Row: Week, Month, Days, Tally Counter, NEET Marks
    ScrollableTabRow(
      selectedTabIndex = selectedTab.ordinal,
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .testTag("analytics_tab_row"),
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
      edgePadding = 8.dp
    ) {
      Tab(
        selected = selectedTab == AnalyticsTab.WEEK,
        onClick = { viewModel.selectedAnalyticsTab.value = AnalyticsTab.WEEK },
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
        onClick = { viewModel.selectedAnalyticsTab.value = AnalyticsTab.MONTH },
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
        selected = selectedTab == AnalyticsTab.DAYS,
        onClick = { viewModel.selectedAnalyticsTab.value = AnalyticsTab.DAYS },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Days", fontWeight = FontWeight.Bold)
          }
        },
        modifier = Modifier.testTag("tab_days")
      )
      Tab(
        selected = selectedTab == AnalyticsTab.TALLY,
        onClick = { viewModel.selectedAnalyticsTab.value = AnalyticsTab.TALLY },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Tally Counter", fontWeight = FontWeight.Bold)
          }
        },
        modifier = Modifier.testTag("tab_tally")
      )
      Tab(
        selected = selectedTab == AnalyticsTab.NEET_MARKS,
        onClick = { viewModel.selectedAnalyticsTab.value = AnalyticsTab.NEET_MARKS },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("NEET Marks", fontWeight = FontWeight.Bold)
          }
        },
        modifier = Modifier.testTag("tab_neet")
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    when (selectedTab) {
      AnalyticsTab.WEEK -> {
        WeeksAnalyticsView(
          weeks = weeksAnalytics,
          onDayClick = onNavigateToDate
        )
      }
      AnalyticsTab.MONTH -> {
        MonthsAnalyticsView(
          months = monthsAnalytics,
          onDayClick = onNavigateToDate
        )
      }
      AnalyticsTab.DAYS -> {
        DaysAnalyticsView(
          days = daysAnalytics,
          onDayClick = onNavigateToDate
        )
      }
      AnalyticsTab.TALLY -> {
        TaskTallyView(
          tallyList = tallyAnalytics
        )
      }
      AnalyticsTab.NEET_MARKS -> {
        NeetMarksTrackingView(
          scores = neetScores,
          onAddScoreClick = { showAddNeetDialog = true },
          onDeleteScore = { viewModel.deleteNeetScore(it) }
        )
      }
    }
  }

  if (showAddNeetDialog) {
    AddNeetScoreDialog(
      onDismiss = { showAddNeetDialog = false },
      onConfirm = { newScore ->
        viewModel.addNeetScore(newScore)
        showAddNeetDialog = false
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
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
          containerColor = RatingBestGreen.copy(alpha = 0.12f)
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = RatingBestGreen,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Week Rule: If 4 or more days in a week are rated Best (😊), the entire week turns Green.",
            style = MaterialTheme.typography.bodySmall.copy(
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurface
            )
          )
        }
      }
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
  val isGreen = week.isGreenWeek
  val cardBg = if (isGreen) RatingBestGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
  val borderColor = if (isGreen) RatingBestGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = cardBg),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isGreen) 2.dp else 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .border(
          width = if (isGreen) 2.dp else 1.dp,
          color = borderColor,
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

        if (isGreen) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(RatingBestGreen)
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "GREEN WEEK",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
            }
          }
        } else {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "${week.bestCount}/4 Best Days",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
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
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = RatingBestGreen.copy(alpha = 0.12f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = RatingBestGreen,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Month Rule: If ≥ 50% or 15+ rated days are Best (😊), Month turns Green. (Average = Light Grey, Worst = Black)",
            style = MaterialTheme.typography.bodySmall.copy(
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurface
            )
          )
        }
      }
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
  val isGreen = month.isGreenMonth
  val cardBg = if (isGreen) RatingBestGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
  val borderColor = if (isGreen) RatingBestGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = cardBg)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .border(
          width = if (isGreen) 2.dp else 1.dp,
          color = borderColor,
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

        if (isGreen) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(RatingBestGreen)
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "GREEN MONTH",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
            }
          }
        } else {
          val percent = if (month.ratedDaysCount > 0) (month.bestCount.toFloat() / month.ratedDaysCount * 100).toInt() else 0
          Text(
            text = "${month.bestCount} Best ($percent%)",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Month Grid of Days (7 columns)
      LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
          .fillMaxWidth()
          .height(190.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        items(month.days) { day ->
          MiniDayGridCell(
            day = day,
            onClick = { onDayClick(day.date) }
          )
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
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
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
    RatingType.BEST -> RatingBestGreen
    RatingType.AVERAGE -> RatingAverageGrey
    RatingType.WORST -> RatingWorstBlack
    null -> MaterialTheme.colorScheme.surfaceVariant
  }
  val textColor = when (rating) {
    RatingType.BEST, RatingType.WORST -> Color.White
    RatingType.AVERAGE -> Color(0xFF1E293B)
    null -> MaterialTheme.colorScheme.onSurfaceVariant
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
              RatingType.WORST -> "😢"
              null -> day.dayOfWeekAbbr
            },
            fontSize = if (rating != null) 18.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
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
    RatingType.BEST -> RatingBestGreen
    RatingType.AVERAGE -> RatingAverageGrey
    RatingType.WORST -> RatingWorstBlack
    null -> MaterialTheme.colorScheme.surfaceVariant
  }
  val textCol = when (rating) {
    RatingType.BEST, RatingType.WORST -> Color.White
    RatingType.AVERAGE -> Color(0xFF1E293B)
    null -> MaterialTheme.colorScheme.onSurfaceVariant
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
          RatingType.WORST -> "😢"
          null -> "-"
        },
        fontSize = if (rating != null) 14.sp else 12.sp,
        fontWeight = FontWeight.Bold,
        color = textCol
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
    RatingType.BEST -> RatingBestGreen
    RatingType.AVERAGE -> RatingAverageGrey
    RatingType.WORST -> RatingWorstBlack
    null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
  }
  val textCol = when (rating) {
    RatingType.BEST, RatingType.WORST -> Color.White
    RatingType.AVERAGE -> Color(0xFF1E293B)
    null -> MaterialTheme.colorScheme.onSurfaceVariant
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
    Text(
      text = when (rating) {
        RatingType.BEST -> "😊"
        RatingType.AVERAGE -> "😐"
        RatingType.WORST -> "😢"
        null -> dayNum
      },
      fontSize = if (rating != null) 12.sp else 10.sp,
      fontWeight = FontWeight.Bold,
      color = textCol
    )
  }
}
