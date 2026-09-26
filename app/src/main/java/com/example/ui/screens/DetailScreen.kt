package com.example.ui.screens

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import java.text.SimpleDateFormat
import java.util.Locale

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.NeetChapter
import com.example.data.model.NeetTallyCounter
import com.example.data.model.NeetTestScore
import com.example.data.model.RatingType
import com.example.ui.AiChatMessage
import com.example.ui.DaySummary
import com.example.ui.DetailTab
import com.example.ui.HabitViewModel
import com.example.ui.MonthSummary
import com.example.ui.SubjectTimeBreakdown
import com.example.ui.TimelineCycleMode
import com.example.ui.WeekSummary
import com.example.ui.components.AddEditChapterDialog
import com.example.ui.components.AddEditTallyDialog
import com.example.ui.components.AddNeetScoreDialog
import com.example.ui.components.CompletedDayTasksDialog
import com.example.ui.components.HamburgerMenuDialog
import com.example.ui.components.NeetProgressWidgetCard
import androidx.compose.material.icons.filled.Insights
import com.example.ui.theme.RatingAverageGrey
import com.example.ui.theme.RatingBestGreen
import com.example.ui.theme.RatingWorstBlack
import com.example.util.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
  viewModel: HabitViewModel,
  onNavigateToDate: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  val selectedTab by viewModel.selectedDetailTab.collectAsStateWithLifecycle()
  val timelineMode by viewModel.timelineCycleMode.collectAsStateWithLifecycle()

  val weeksAnalytics by viewModel.weeksAnalytics.collectAsStateWithLifecycle()
  val monthsAnalytics by viewModel.monthsAnalytics.collectAsStateWithLifecycle()
  val daysAnalytics by viewModel.daysAnalytics.collectAsStateWithLifecycle()
  val tallyAnalytics by viewModel.taskTallyAnalytics.collectAsStateWithLifecycle()
  val neetScores by viewModel.allNeetScores.collectAsStateWithLifecycle()
  val neetChapters by viewModel.allNeetChapters.collectAsStateWithLifecycle()
  val neetTallyCounters by viewModel.allNeetTallyCounters.collectAsStateWithLifecycle()
  val subjectTimeBreakdown by viewModel.subjectTimeBreakdown.collectAsStateWithLifecycle()
  val aiChatMessages by viewModel.aiChatMessages.collectAsStateWithLifecycle()
  val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
  val aiSuggestions by viewModel.dynamicAiSuggestions.collectAsStateWithLifecycle()
  val aiChatDraft by viewModel.aiChatDraft.collectAsStateWithLifecycle()

  var showHamburgerMenu by remember { mutableStateOf(false) }
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

  val pagerState = rememberPagerState(
    initialPage = selectedTab.ordinal,
    pageCount = { 3 }
  )
  val coroutineScope = rememberCoroutineScope()

  // Sync pagerState -> viewModel.selectedDetailTab
  LaunchedEffect(pagerState.currentPage) {
    if (viewModel.selectedDetailTab.value.ordinal != pagerState.currentPage) {
      viewModel.selectedDetailTab.value = DetailTab.entries[pagerState.currentPage]
    }
  }

  // Sync viewModel.selectedDetailTab -> pagerState
  LaunchedEffect(selectedTab) {
    if (pagerState.currentPage != selectedTab.ordinal) {
      pagerState.animateScrollToPage(selectedTab.ordinal)
    }
  }

  var isTopBarVisible by remember { mutableStateOf(true) }
  val nestedScrollConnection = remember {
    object : NestedScrollConnection {
      override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.y
        if (delta < -12f && isTopBarVisible) {
          isTopBarVisible = false
        } else if (delta > 12f && !isTopBarVisible) {
          isTopBarVisible = true
        }
        return Offset.Zero
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .nestedScroll(nestedScrollConnection)
      .padding(horizontal = 16.dp)
  ) {
    // Top Bar & Tab Row - Automatically hides when scrolling down for more screen space!
    AnimatedVisibility(
      visible = isTopBarVisible,
      enter = expandVertically() + fadeIn(),
      exit = shrinkVertically() + fadeOut()
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top Header: Hamburger on top left corner (clean, no halo)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.Start,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              showHamburgerMenu = true
            },
            modifier = Modifier
              .size(40.dp)
              .testTag("btn_detail_hamburger")
          ) {
            Icon(
              imageVector = Icons.Default.Menu,
              contentDescription = "Open Settings & Hub",
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(24.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 3 Main Tabs: Timeline (Cycles Day/Week/Month on tab click), NEET, Analytics
        TabRow(
          selectedTabIndex = pagerState.currentPage,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .testTag("detail_tab_row"),
          containerColor = Color.Transparent,
          divider = {}
        ) {
          // 1. Timeline Tab
          Tab(
            selected = pagerState.currentPage == 0,
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              if (pagerState.currentPage == 0) {
                // Already on timeline -> cycle to next mode smoothly!
                viewModel.cycleTimelineMode()
              } else {
                coroutineScope.launch {
                  pagerState.animateScrollToPage(0)
                }
              }
            },
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = when (timelineMode) {
                    TimelineCycleMode.DAY -> Icons.Default.DateRange
                    TimelineCycleMode.WEEK -> Icons.Default.ViewWeek
                    TimelineCycleMode.MONTH -> Icons.Default.CalendarMonth
                  },
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Timeline (${timelineMode.label})",
                  fontWeight = if (pagerState.currentPage == 0) FontWeight.Bold else FontWeight.Normal
                )
              }
            },
            modifier = Modifier.testTag("tab_timeline")
          )

          // 2. NEET Tab
          Tab(
            selected = pagerState.currentPage == 1,
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              coroutineScope.launch {
                pagerState.animateScrollToPage(1)
              }
            },
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("NEET", fontWeight = if (pagerState.currentPage == 1) FontWeight.Bold else FontWeight.Normal)
              }
            },
            modifier = Modifier.testTag("tab_neet")
          )

          // 3. Analytics Tab
          Tab(
            selected = pagerState.currentPage == 2,
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              coroutineScope.launch {
                pagerState.animateScrollToPage(2)
              }
            },
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Analytics", fontWeight = if (pagerState.currentPage == 2) FontWeight.Bold else FontWeight.Normal)
              }
            },
            modifier = Modifier.testTag("tab_analytics")
          )
        }

        Spacer(modifier = Modifier.height(10.dp))
      }
    }

    // Main Tab Content Area with Smooth Horizontal Pager Swiping across Tabs!
    HorizontalPager(
      state = pagerState,
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    ) { page ->
      when (page) {
        0 -> {
          TimelineSectionView(
            mode = timelineMode,
            days = daysAnalytics,
            weeks = weeksAnalytics,
            months = monthsAnalytics,
            onDayClick = { date ->
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              selectedDateForTasks = date
            }
          )
        }

        1 -> {
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

        2 -> {
          AnalyticsSectionView(
            chapters = neetChapters,
            testScores = neetScores,
            subjectTimeBreakdown = subjectTimeBreakdown,
            days = daysAnalytics,
            chatMessages = aiChatMessages,
            isLoading = isAiLoading,
            suggestions = aiSuggestions,
            draftText = aiChatDraft,
            onDraftChange = { viewModel.setAiChatDraft(it) },
            onSendQuestion = { question, askCloud ->
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              viewModel.sendAiQuestion(question, askCloud)
            },
            onClearChat = { viewModel.clearAiChat() }
          )
        }
      }
    }
  }

  // Full Screen Hamburger Menu Dialog
  if (showHamburgerMenu) {
    HamburgerMenuDialog(
      viewModel = viewModel,
      onDismiss = { showHamburgerMenu = false }
    )
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
// TIMELINE SECTION VIEW (Cycling strictly via Timeline tab click)
// -------------------------------------------------------------
@Composable
private fun TimelineSectionView(
  mode: TimelineCycleMode,
  days: List<DaySummary>,
  weeks: List<WeekSummary>,
  months: List<MonthSummary>,
  onDayClick: (String) -> Unit
) {
  Column(modifier = Modifier.fillMaxSize()) {
    // Smooth animated transition between Day, Week, Month
    AnimatedContent(
      targetState = mode,
      transitionSpec = {
        (fadeIn() + slideInHorizontally { width -> width / 4 }) togetherWith
          (fadeOut() + slideOutHorizontally { width -> -width / 4 })
      },
      label = "TimelineCycleTransition",
      modifier = Modifier.fillMaxSize()
    ) { targetMode ->
      when (targetMode) {
        TimelineCycleMode.DAY -> {
          DaysAnalyticsView(days = days, onDayClick = onDayClick)
        }
        TimelineCycleMode.WEEK -> {
          WeeksAnalyticsView(weeks = weeks, onDayClick = onDayClick)
        }
        TimelineCycleMode.MONTH -> {
          MonthsAnalyticsView(months = months, onDayClick = onDayClick)
        }
      }
    }
  }
}

// -------------------------------------------------------------
// REFINED ANALYTICS SECTION (Rich progress data & focused AI coach)
// -------------------------------------------------------------
@Composable
private fun AnalyticsSectionView(
  chapters: List<NeetChapter>,
  testScores: List<NeetTestScore>,
  subjectTimeBreakdown: List<SubjectTimeBreakdown>,
  days: List<DaySummary>,
  chatMessages: List<AiChatMessage>,
  isLoading: Boolean,
  suggestions: List<String>,
  draftText: String,
  onDraftChange: (String) -> Unit,
  onSendQuestion: (String, Boolean) -> Unit,
  onClearChat: () -> Unit
) {
  val haptic = LocalHapticFeedback.current
  var userInputText by remember(draftText) { mutableStateOf(draftText) }
  val listState = rememberLazyListState()

  val totalChapters = chapters.size
  val completedChapters = chapters.count { it.isCompleted }

  val bestRatingsCount = days.count { it.rating == RatingType.BEST }
  val avgRatingsCount = days.count { it.rating == RatingType.AVERAGE }
  val worstRatingsCount = days.count { it.rating == RatingType.WORST }
  val ratedDaysCount = bestRatingsCount + avgRatingsCount + worstRatingsCount

  val totalStudySeconds = days.sumOf { it.totalTimeSeconds }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .imePadding()
      .navigationBarsPadding()
  ) {
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      contentPadding = PaddingValues(bottom = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. HOME SCREEN WIDGET COMPONENT (Chapters, PYQ, NCERT completed out of total)
      item(key = "neet_widget_component") {
        NeetProgressWidgetCard(
          chapters = chapters
        )
      }

      // 2. SUBJECT-WISE DETAILED PROGRESS MATRIX
      item(key = "subject_mastery_matrix") {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.School,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Subject Completion Matrix",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }

              Text(
                text = "$completedChapters/$totalChapters Chapters",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = RatingBestGreen
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            listOf("Botany", "Zoology", "Physics", "Chemistry").forEach { subj ->
              val subjChapters = chapters.filter { it.subject.equals(subj, ignoreCase = true) }
              val subTotal = subjChapters.size
              val subDone = subjChapters.count { it.isCompleted }
              val subPyq = subjChapters.count { it.isPyqDone }
              val subNcert = subjChapters.count { it.isRevisionDone }
              val subProgress = if (subTotal > 0) subDone.toFloat() / subTotal else 0f
              val subPercent = if (subTotal > 0) (subDone * 100) / subTotal else 0

              val subjColor = when (subj) {
                "Botany" -> Color(0xFF059669)
                "Zoology" -> Color(0xFFD97706)
                "Physics" -> Color(0xFF2563EB)
                else -> Color(0xFF7C3AED)
              }

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 5.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(subjColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = subj,
                      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }

                  Text(
                    text = "$subDone/$subTotal ($subPercent%)  •  PYQ: $subPyq  •  NCERT: $subNcert",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                  progress = { subProgress },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                  color = subjColor,
                  trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
              }
            }
          }
        }
      }

      // 3. TIME DEDICATION & ALLOCATION (% VS TARGET)
      item(key = "subject_breakdown_card") {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Schedule,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Time Dedication & Allocation",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }

              Text(
                text = "Target: 40% Bio | 30% Phy | 30% Chem",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              subjectTimeBreakdown.forEach { sub ->
                val barColor = when (sub.name) {
                  "Physics" -> Color(0xFF3B82F6)
                  "Chemistry" -> Color(0xFFEC4899)
                  "Botany" -> Color(0xFF10B981)
                  "Zoology" -> Color(0xFFF59E0B)
                  else -> Color(0xFF8B5CF6)
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = sub.name,
                      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "${sub.formattedTime}  (${String.format(java.util.Locale.getDefault(), "%.1f", sub.percentage)}%)",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                      ),
                      color = barColor
                    )
                  }

                  LinearProgressIndicator(
                    progress = { (sub.percentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(6.dp)
                      .clip(RoundedCornerShape(3.dp)),
                    color = barColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                  )
                }
              }
            }
          }
        }
      }

      // 4. STUDY CONSISTENCY & DAILY RATINGS INSIGHTS
      item(key = "consistency_insights_card") {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.TrendingUp,
                  contentDescription = null,
                  tint = RatingBestGreen,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Study Consistency & Ratings",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }

              Text(
                text = "Total: ${DateUtils.formatTime(totalStudySeconds)}",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              val bestPct = if (ratedDaysCount > 0) (bestRatingsCount * 100) / ratedDaysCount else 0
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(10.dp))
                  .background(RatingBestGreen.copy(alpha = 0.12f))
                  .border(1.dp, RatingBestGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                  .padding(8.dp)
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                  Text("😊 Best", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RatingBestGreen)
                  Text("$bestRatingsCount days", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RatingBestGreen)
                  Text("$bestPct%", fontSize = 10.sp, color = RatingBestGreen)
                }
              }

              val avgPct = if (ratedDaysCount > 0) (avgRatingsCount * 100) / ratedDaysCount else 0
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color.Transparent)
                  .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                  .padding(8.dp)
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                  Text("😐 Average", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                  Text("$avgRatingsCount days", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                  Text("$avgPct%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }

              val worstPct = if (ratedDaysCount > 0) (worstRatingsCount * 100) / ratedDaysCount else 0
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                  .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                  .padding(8.dp)
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                  Text("😞 Worst", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                  Text("$worstRatingsCount days", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                  Text("$worstPct%", fontSize = 10.sp, color = Color(0xFFEF4444))
                }
              }
            }
          }
        }
      }

      // 5. NEET TEST SCORES OVERVIEW (If scores exist)
      if (testScores.isNotEmpty()) {
        item(key = "test_scores_analytics_card") {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f))
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Mock Tests Performance (${testScores.size} tests)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(8.dp))

              val avgScore = testScores.map { it.totalScore }.average().toInt()
              val maxScore = testScores.maxOf { it.totalScore }
              val latestScore = testScores.maxByOrNull { it.date }?.totalScore ?: 0

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF3B82F6).copy(alpha = 0.12f))
                    .padding(8.dp)
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Average", fontSize = 10.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.SemiBold)
                    Text("$avgScore/720", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                  }
                }

                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(RatingBestGreen.copy(alpha = 0.12f))
                    .padding(8.dp)
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("High Score", fontSize = 10.sp, color = RatingBestGreen, fontWeight = FontWeight.SemiBold)
                    Text("$maxScore/720", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RatingBestGreen)
                  }
                }

                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF8B5CF6).copy(alpha = 0.12f))
                    .padding(8.dp)
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Latest", fontSize = 10.sp, color = Color(0xFF8B5CF6), fontWeight = FontWeight.SemiBold)
                    Text("$latestScore/720", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                  }
                }
              }
            }
          }
        }
      }

      // 6. AI STUDY COACH (Focused, Clean, NO "what to ask AI" suggestion chips!)
      item(key = "ai_coach_header") {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "AI Study Assistant",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          if (chatMessages.isNotEmpty()) {
            IconButton(onClick = onClearChat, modifier = Modifier.size(26.dp)) {
              Icon(Icons.Default.Refresh, contentDescription = "Clear Chat", modifier = Modifier.size(16.dp))
            }
          }
        }
      }

      // Chat Messages History
      items(chatMessages) { message ->
        ChatMessageBubble(message = message)
      }

      // Loading Spinner
      if (isLoading) {
        item(key = "loading_indicator") {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              strokeWidth = 2.dp,
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Analyzing your study records...",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    val focusRequester = remember { FocusRequester() }
    var isSearchFieldVisible by remember { mutableStateOf(false) }
    var useCloudModel by remember { mutableStateOf(false) }
    val isExpanded = isSearchFieldVisible || userInputText.isNotEmpty()

    LaunchedEffect(isExpanded) {
      if (isExpanded) {
        try {
          focusRequester.requestFocus()
        } catch (e: Exception) {
          // Safe guard against focus errors in pager transition
        }
      }
    }

    // Model Selection Toggle Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 4.dp, bottom = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(
        text = "Model Mode:",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      FilterChip(
        selected = !useCloudModel,
        onClick = { useCloudModel = false },
        label = { Text("💻 On-Device Model", fontSize = 11.sp) },
        leadingIcon = if (!useCloudModel) {
          { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.testTag("chip_on_device")
      )

      FilterChip(
        selected = useCloudModel,
        onClick = { useCloudModel = true },
        label = { Text("☁️ Cloud Model", fontSize = 11.sp) },
        leadingIcon = if (useCloudModel) {
          { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.testTag("chip_cloud")
      )
    }

    // Dynamic Progressive Chat Bar & AI Suggestions Line (v6.3)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      val charCount = userInputText.length

      // Magnifying Glass / Expanding Input Field on the Bottom Left
      if (!isExpanded) {
        // Small compact magnifying glass icon button
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f), CircleShape)
            .clickable {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              isSearchFieldVisible = true
            }
            .testTag("btn_magnify_glass"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Ask AI Coach",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
        }
      } else {
        // Progressively growing chat box as letters are written
        val inputModifier = if (charCount >= 16) {
          Modifier.weight(1f)
        } else {
          Modifier.width((130 + charCount * 12).coerceAtMost(280).dp)
        }

        OutlinedTextField(
          value = userInputText,
          onValueChange = { newText ->
            userInputText = newText
            onDraftChange(newText)
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          },
          trailingIcon = {
            if (userInputText.isNotBlank()) {
              IconButton(
                onClick = {
                  userInputText = ""
                  onDraftChange("")
                  isSearchFieldVisible = false
                },
                modifier = Modifier.size(20.dp)
              ) {
                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
              }
            }
          },
          placeholder = { Text("Ask AI Coach...", fontSize = 11.sp, maxLines = 1) },
          singleLine = true,
          textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
          shape = RoundedCornerShape(22.dp),
          modifier = inputModifier
            .height(50.dp)
            .focusRequester(focusRequester)
        )
      }

      // In the SAME LINE as magnifying glass: AI suggestions (universal + personalized from user data)
      if (charCount < 16) {
        LazyRow(
          modifier = Modifier.weight(1f),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
          contentPadding = PaddingValues(end = 4.dp)
        ) {
          items(suggestions) { suggestion ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .clickable {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  onSendQuestion(suggestion, useCloudModel)
                  userInputText = ""
                  onDraftChange("")
                  isSearchFieldVisible = false
                }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = suggestion,
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1
                )
              }
            }
          }
        }
      }

      // Send Button when text is present
      if (userInputText.isNotBlank()) {
        IconButton(
          onClick = {
            if (userInputText.isNotBlank()) {
              val query = userInputText.trim()
              userInputText = ""
              onDraftChange("")
              isSearchFieldVisible = false
              onSendQuestion(query, useCloudModel)
            }
          },
          enabled = userInputText.isNotBlank() && !isLoading,
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
              if (userInputText.isNotBlank() && !isLoading) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
          Icon(
            imageVector = Icons.Default.Send,
            contentDescription = "Send",
            tint = if (userInputText.isNotBlank() && !isLoading) MaterialTheme.colorScheme.onPrimary
                   else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun ChatMessageBubble(message: AiChatMessage) {
  val isUser = message.role == "user"
  val formattedTime = remember(message.timestamp) {
    if (message.timestamp == 0L) {
      ""
    } else {
      try {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(java.util.Date(message.timestamp))
      } catch (e: Exception) {
        ""
      }
    }
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    if (!isUser) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Psychology,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
    }

    Card(
      modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 0.92f),
      shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isUser) 16.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 16.dp
      ),
      colors = CardDefaults.cardColors(
        containerColor = if (isUser) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
      ),
      border = if (!isUser) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) else null
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text(
          text = message.text,
          style = MaterialTheme.typography.bodyMedium.copy(
            lineHeight = 20.sp,
            fontSize = 13.sp
          ),
          color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
        if (formattedTime.isNotEmpty()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = formattedTime,
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 9.sp,
              fontWeight = FontWeight.Normal
            ),
            color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.65f)
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.End)
          )
        }
      }
    }
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
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .border(
          width = 1.dp,
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
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
              .background(Color.Transparent)
              .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                RoundedCornerShape(8.dp)
              )
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
              .background(Color.Transparent)
              .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                RoundedCornerShape(8.dp)
              )
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
              .background(Color.Transparent)
              .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                RoundedCornerShape(8.dp)
              )
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
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .border(
          width = 1.dp,
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
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
              .background(Color.Transparent)
              .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                RoundedCornerShape(8.dp)
              )
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
              .background(Color.Transparent)
              .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                RoundedCornerShape(8.dp)
              )
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
              .background(Color.Transparent)
              .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                RoundedCornerShape(8.dp)
              )
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

      val firstDate = month.days.firstOrNull()?.date
      val startDayOffset = if (firstDate != null) DateUtils.getDayOfWeekIndex(firstDate) else 0
      val gridCells = buildList<DaySummary?> {
        repeat(startDayOffset) { add(null) }
        addAll(month.days)
      }

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        gridCells.chunked(7).forEach { weekChunk ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            weekChunk.forEach { day ->
              Box(modifier = Modifier.weight(1f)) {
                if (day != null) {
                  MiniDayGridCell(
                    day = day,
                    onClick = { onDayClick(day.date) }
                  )
                }
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
    null -> Color.Transparent
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
            .background(ratingBg)
            .border(
              width = 1.dp,
              color = if (rating != null) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
              shape = RoundedCornerShape(10.dp)
            ),
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
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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

@Composable
private fun DayRatingBadge(
  day: DaySummary,
  onClick: () -> Unit
) {
  val rating = day.rating
  val ratingBg = when (rating) {
    RatingType.BEST -> RatingBestGreen.copy(alpha = 0.2f)
    RatingType.AVERAGE -> RatingAverageGrey.copy(alpha = 0.4f)
    RatingType.WORST -> RatingWorstBlack.copy(alpha = 0.2f)
    null -> Color.Transparent
  }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .clickable { onClick() }
      .padding(4.dp)
  ) {
    Text(
      text = day.dayOfWeekAbbr,
      style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(4.dp))
    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(ratingBg)
        .border(
          width = 1.dp,
          color = if (rating != null) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
          shape = RoundedCornerShape(8.dp)
        ),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = when (rating) {
          RatingType.BEST -> "😊"
          RatingType.AVERAGE -> "😐"
          RatingType.WORST -> "😞"
          null -> day.date.takeLast(2)
        },
        fontSize = if (rating != null) 16.sp else 11.sp,
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
  val ratingBg = when (rating) {
    RatingType.BEST -> RatingBestGreen.copy(alpha = 0.2f)
    RatingType.AVERAGE -> RatingAverageGrey.copy(alpha = 0.4f)
    RatingType.WORST -> RatingWorstBlack.copy(alpha = 0.2f)
    null -> Color.Transparent
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(34.dp)
      .clip(RoundedCornerShape(6.dp))
      .background(ratingBg)
      .border(
        width = 1.dp,
        color = if (rating != null) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
        shape = RoundedCornerShape(6.dp)
      )
      .clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = when (rating) {
        RatingType.BEST -> "😊"
        RatingType.AVERAGE -> "😐"
        RatingType.WORST -> "😞"
        null -> day.date.takeLast(2)
      },
      fontSize = if (rating != null) 14.sp else 11.sp,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
