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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.RatingType
import com.example.ui.AnalyticsTab
import com.example.ui.DaySummary
import com.example.ui.HabitViewModel
import com.example.ui.MonthSummary
import com.example.ui.WeekSummary
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

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(12.dp))

    // Header Title
    Text(
      text = "Habit Analytics",
      style = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
      ),
      color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(10.dp))

    // 1) Week, 2) Month, 3) Days Tab Bar as requested
    TabRow(
      selectedTabIndex = selectedTab.ordinal,
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .testTag("analytics_tab_row"),
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Visual Color Legend matching user notes
    ColorLegendCard()

    Spacer(modifier = Modifier.height(12.dp))

    // Content based on selected tab
    when (selectedTab) {
      AnalyticsTab.WEEK -> WeekAnalyticsView(
        weeks = weeksAnalytics,
        onDayClick = onNavigateToDate
      )
      AnalyticsTab.MONTH -> MonthAnalyticsView(
        months = monthsAnalytics,
        onDayClick = onNavigateToDate
      )
      AnalyticsTab.DAYS -> DaysAnalyticsView(
        days = daysAnalytics,
        onDayClick = onNavigateToDate
      )
    }
  }
}

@Composable
private fun ColorLegendCard() {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      LegendItem(color = RatingBestGreen, label = "Best [A]", textColor = Color.White)
      LegendItem(color = RatingAverageGrey, label = "Average [B]", textColor = Color(0xFF1E293B))
      LegendItem(color = RatingWorstBlack, label = "Worst [C]", textColor = Color.White)
    }
  }
}

@Composable
private fun LegendItem(color: Color, label: String, textColor: Color) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(14.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(color)
        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp
      ),
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

// -------------------------------------------------------------
// 1) WEEK ANALYTICS VIEW
// -------------------------------------------------------------
@Composable
private fun WeekAnalyticsView(
  weeks: List<WeekSummary>,
  onDayClick: (String) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item(key = "week_rule_hint") {
      Text(
        text = "Rule: If 4 or more days in a week are rated Best, the entire week becomes Green!",
        style = MaterialTheme.typography.bodySmall.copy(
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Medium
        ),
        modifier = Modifier.padding(horizontal = 4.dp)
      )
    }

    items(weeks, key = { it.startDate }) { week ->
      WeekSummaryCard(week = week, onDayClick = onDayClick)
    }
  }
}

@Composable
private fun WeekSummaryCard(
  week: WeekSummary,
  onDayClick: (String) -> Unit
) {
  // Color based on user's sketch rule:
  // "on marking 4 or more days best that week becomes green on average clicking a day light grey and on worst black and similar analogy for week and month"
  val weekHeaderBgColor = when {
    week.isGreen -> RatingBestGreen
    week.overallRating == RatingType.AVERAGE -> RatingAverageGrey
    week.overallRating == RatingType.WORST -> RatingWorstBlack
    else -> MaterialTheme.colorScheme.surfaceVariant
  }

  val weekHeaderTextColor = when {
    week.isGreen || week.overallRating == RatingType.WORST -> Color.White
    else -> Color(0xFF1E293B)
  }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("week_card_${week.startDate}")
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Header banner indicating whether week turned Green, Average, or Worst
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(weekHeaderBgColor)
          .padding(horizontal = 14.dp, vertical = 10.dp)
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
                color = weekHeaderTextColor
              )
            )
            Text(
              text = if (week.isGreen) "★ Target Reached! Week is Green (4+ Best Days)"
              else when (week.overallRating) {
                RatingType.BEST -> "Week Status: Best"
                RatingType.AVERAGE -> "Week Status: Average"
                RatingType.WORST -> "Week Status: Needs Attention"
                null -> "No ratings recorded yet"
              },
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                color = weekHeaderTextColor.copy(alpha = 0.9f)
              )
            )
          }

          // Best days counter pill
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color.White.copy(alpha = 0.25f))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "${week.bestCount}/7 Best",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = weekHeaderTextColor
              )
            )
          }
        }
      }

      // Week Details
      Column(modifier = Modifier.padding(14.dp)) {
        // 7 Day Rating Boxes: [M] [T] [W] [T] [F] [S] [S]
        Text(
          text = "Days Breakdown (Click to view day):",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          val dayLetters = listOf("M", "T", "W", "T", "F", "S", "S")
          week.days.forEachIndexed { idx, day ->
            DayColorBoxMini(
              letter = dayLetters.getOrElse(idx) { day.dayOfWeekAbbr.take(1) },
              rating = day.rating,
              date = day.date,
              onClick = { onDayClick(day.date) }
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Metrics row: Total Time & Ratings tally
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Schedule,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Time: ${DateUtils.formatTimeWords(week.totalTimeSeconds)}",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
            )
          }

          Text(
            text = "${week.bestCount} Best • ${week.averageCount} Avg • ${week.worstCount} Worst",
            style = MaterialTheme.typography.bodySmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }
      }
    }
  }
}

@Composable
private fun DayColorBoxMini(
  letter: String,
  rating: RatingType?,
  date: String,
  onClick: () -> Unit
) {
  val boxBg = when (rating) {
    RatingType.BEST -> RatingBestGreen
    RatingType.AVERAGE -> RatingAverageGrey
    RatingType.WORST -> RatingWorstBlack
    null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
  }

  val textColor = when (rating) {
    RatingType.BEST, RatingType.WORST -> Color.White
    RatingType.AVERAGE -> Color(0xFF1E293B)
    null -> MaterialTheme.colorScheme.onSurfaceVariant
  }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable { onClick() }
  ) {
    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(boxBg)
        .border(
          width = 1.dp,
          color = if (rating != null) boxBg else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
          shape = RoundedCornerShape(8.dp)
        ),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = letter,
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.Black,
          fontSize = 14.sp
        ),
        color = textColor
      )
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = DateUtils.formatShortDate(date).split(" ").lastOrNull() ?: "",
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 9.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    )
  }
}

// -------------------------------------------------------------
// 2) MONTH ANALYTICS VIEW
// -------------------------------------------------------------
@Composable
private fun MonthAnalyticsView(
  months: List<MonthSummary>,
  onDayClick: (String) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item(key = "month_rule_hint") {
      Text(
        text = "Rule: If >= 50% or 15+ rated days are Best, the Month becomes Green (Average = Light Grey, Worst = Black).",
        style = MaterialTheme.typography.bodySmall.copy(
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Medium
        ),
        modifier = Modifier.padding(horizontal = 4.dp)
      )
    }

    items(months, key = { it.yearMonth }) { month ->
      MonthSummaryCard(month = month, onDayClick = onDayClick)
    }
  }
}

@Composable
private fun MonthSummaryCard(
  month: MonthSummary,
  onDayClick: (String) -> Unit
) {
  val monthBgColor = when {
    month.isGreen -> RatingBestGreen
    month.overallRating == RatingType.AVERAGE -> RatingAverageGrey
    month.overallRating == RatingType.WORST -> RatingWorstBlack
    else -> MaterialTheme.colorScheme.surfaceVariant
  }

  val monthTextColor = when {
    month.isGreen || month.overallRating == RatingType.WORST -> Color.White
    else -> Color(0xFF1E293B)
  }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("month_card_${month.yearMonth}")
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Month Header
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(monthBgColor)
          .padding(horizontal = 14.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = month.monthLabel,
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = monthTextColor
              )
            )
            Text(
              text = if (month.isGreen) "★ Month Status: Green (Consistent High Performance!)"
              else when (month.overallRating) {
                RatingType.BEST -> "Month Status: High Performance"
                RatingType.AVERAGE -> "Month Status: Average Performance"
                RatingType.WORST -> "Month Status: Needs Improvement"
                null -> "No ratings logged this month"
              },
              style = MaterialTheme.typography.labelSmall.copy(
                color = monthTextColor.copy(alpha = 0.9f)
              )
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color.White.copy(alpha = 0.25f))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "${month.bestCount} Best",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = monthTextColor
              )
            )
          }
        }
      }

      Column(modifier = Modifier.padding(14.dp)) {
        // Month stats
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Total Logged: ${DateUtils.formatTimeWords(month.totalTimeSeconds)}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
          )
          Text(
            text = "Rated Days: ${month.bestCount + month.averageCount + month.worstCount} / ${month.days.size}",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Weekday header labels: M, T, W, T, F, S, S
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          listOf("M", "T", "W", "T", "F", "S", "S").forEach {
            Text(
              text = it,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              ),
              modifier = Modifier.width(32.dp),
              textAlign = TextAlign.Center
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Days Grid for Month
        val firstDayOffset = month.days.firstOrNull()?.dayOfWeekIndex ?: 0
        val totalCells = firstDayOffset + month.days.size

        // Render rows of 7
        val rows = (totalCells + 6) / 7
        for (r in 0 until rows) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            for (c in 0..6) {
              val cellIdx = r * 7 + c
              if (cellIdx < firstDayOffset || (cellIdx - firstDayOffset) >= month.days.size) {
                Spacer(modifier = Modifier.size(32.dp))
              } else {
                val day = month.days[cellIdx - firstDayOffset]
                val dayNumber = day.date.split("-").lastOrNull()?.toIntOrNull() ?: 1

                val cellBg = when (day.rating) {
                  RatingType.BEST -> RatingBestGreen
                  RatingType.AVERAGE -> RatingAverageGrey
                  RatingType.WORST -> RatingWorstBlack
                  null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                }

                val cellText = when (day.rating) {
                  RatingType.BEST, RatingType.WORST -> Color.White
                  RatingType.AVERAGE -> Color(0xFF1E293B)
                  null -> MaterialTheme.colorScheme.onSurface
                }

                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(cellBg)
                    .border(
                      width = 1.dp,
                      color = if (day.rating != null) cellBg else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                      shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onDayClick(day.date) },
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "$dayNumber",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 11.sp
                    ),
                    color = cellText
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

// -------------------------------------------------------------
// 3) DAYS ANALYTICS VIEW
// -------------------------------------------------------------
@Composable
private fun DaysAnalyticsView(
  days: List<DaySummary>,
  onDayClick: (String) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(days, key = { it.date }) { day ->
      DayAnalyticsRowItem(day = day, onClick = { onDayClick(day.date) })
    }
  }
}

@Composable
private fun DayAnalyticsRowItem(
  day: DaySummary,
  onClick: () -> Unit
) {
  val ratingColor = when (day.rating) {
    RatingType.BEST -> RatingBestGreen
    RatingType.AVERAGE -> RatingAverageGrey
    RatingType.WORST -> RatingWorstBlack
    null -> MaterialTheme.colorScheme.surfaceVariant
  }

  val textColor = when (day.rating) {
    RatingType.BEST, RatingType.WORST -> Color.White
    RatingType.AVERAGE -> Color(0xFF1E293B)
    null -> MaterialTheme.colorScheme.onSurfaceVariant
  }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("day_row_${day.date}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left: Date info & time
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Rating box indicator
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ratingColor)
            .border(
              1.dp,
              if (day.rating != null) ratingColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
              RoundedCornerShape(10.dp)
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = when (day.rating) {
              RatingType.BEST -> "[A]"
              RatingType.AVERAGE -> "[B]"
              RatingType.WORST -> "[C]"
              null -> "-"
            },
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold
            ),
            color = textColor
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = DateUtils.formatFullDate(day.date),
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 15.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = when (day.rating) {
              RatingType.BEST -> "★ Rated Best Day"
              RatingType.AVERAGE -> "• Rated Average Day"
              RatingType.WORST -> "▲ Rated Worst Day"
              null -> "Not rated"
            },
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Medium,
              color = when (day.rating) {
                RatingType.BEST -> RatingBestGreen
                RatingType.AVERAGE -> MaterialTheme.colorScheme.onSurfaceVariant
                RatingType.WORST -> MaterialTheme.colorScheme.onSurface
                null -> MaterialTheme.colorScheme.onSurfaceVariant
              }
            )
          )
        }
      }

      // Right: Time Spent on that day
      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = DateUtils.formatTime(day.totalTimeSeconds),
          style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          ),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "${day.completedTasksCount} done",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
