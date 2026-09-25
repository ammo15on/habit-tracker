package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.mutableFloatStateOf
import com.example.ui.components.HamburgerMenuDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.db.AppDatabase
import com.example.data.repository.HabitRepository
import com.example.ui.HabitViewModel
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.outlined.Insights
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.PlanScreen
import com.example.ui.screens.TrackerScreen
import com.example.ui.theme.MyApplicationTheme

enum class MainNavigationTab {
  TRACKER,
  PLAN,
  DETAIL
}

class MainActivity : ComponentActivity() {
  private val activeTabState = kotlinx.coroutines.flow.MutableStateFlow(MainNavigationTab.TRACKER)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Request maximum display refresh rate (90Hz / 120Hz / 144Hz) for maximum fluid FPS
    try {
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        val maxMode = display?.supportedModes?.maxByOrNull { it.refreshRate }
        if (maxMode != null) {
          val lp = window.attributes
          lp.preferredDisplayModeId = maxMode.modeId
          window.attributes = lp
        }
      } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        @Suppress("DEPRECATION")
        val maxMode = window.windowManager.defaultDisplay.supportedModes?.maxByOrNull { it.refreshRate }
        if (maxMode != null) {
          val lp = window.attributes
          lp.preferredDisplayModeId = maxMode.modeId
          window.attributes = lp
        }
      }
    } catch (_: Exception) {}

    checkIntentForTargetTab(intent)

    val database = AppDatabase.getDatabase(applicationContext)
    val repository = HabitRepository(database.habitDao())
    val themePreferences = com.example.util.ThemePreferences(applicationContext)
    val goalPreferences = com.example.util.GoalPreferences(applicationContext)
    com.example.util.TimerManager.initialize(applicationContext, repository)

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
        requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
      }
    }

    setContent {
      val viewModel: HabitViewModel = viewModel(
        factory = HabitViewModel.provideFactory(repository, themePreferences, goalPreferences, applicationContext)
      )
      val currentUiHex by viewModel.selectedUiHex.collectAsState()
      val currentBgHex by viewModel.selectedBgHex.collectAsState()
      val currentTextHex by viewModel.selectedTextHex.collectAsState()
      val currentThemeColor by viewModel.selectedThemeColor.collectAsState()
      val currentFontColor by viewModel.selectedFontColor.collectAsState()
      val currentBackgroundUri by viewModel.selectedBackgroundImageUri.collectAsState()
      val currentUiOpacity by viewModel.selectedUiOpacity.collectAsState()
      val currentTextSizeScale by viewModel.selectedTextSizeScale.collectAsState()
      val activeTab by activeTabState.collectAsState()

      MyApplicationTheme(
        uiHex = currentUiHex,
        bgHex = currentBgHex,
        textHex = currentTextHex,
        themeColor = currentThemeColor,
        fontColor = currentFontColor,
        uiOpacity = currentUiOpacity,
        textSizeScale = currentTextSizeScale,
        backgroundImageUri = currentBackgroundUri
      ) {
        MainAppContent(
          viewModel = viewModel,
          hasBackgroundImage = !currentBackgroundUri.isNullOrBlank(),
          currentTab = activeTab,
          onTabChanged = { activeTabState.value = it }
        )
      }
    }
  }

  override fun onNewIntent(intent: android.content.Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    checkIntentForTargetTab(intent)
  }

  private fun checkIntentForTargetTab(intent: android.content.Intent?) {
    val targetTab = intent?.getStringExtra(com.example.widget.NeetProgressAppWidgetProvider.EXTRA_TARGET_TAB)
    if (targetTab == "DETAIL") {
      activeTabState.value = MainNavigationTab.DETAIL
    }
  }
}

@Composable
fun MainAppContent(
  viewModel: HabitViewModel,
  hasBackgroundImage: Boolean = false,
  currentTab: MainNavigationTab = MainNavigationTab.TRACKER,
  onTabChanged: (MainNavigationTab) -> Unit = {}
) {
  val isBottomBarVisible by viewModel.isBottomBarVisible.collectAsState()
  val isHamburgerOpen by viewModel.isHamburgerMenuOpen.collectAsState()

  // NestedScrollConnection: hides bottom menu when scrolling down on ANY screen!
  val nestedScrollConnection = remember {
    object : NestedScrollConnection {
      override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        viewModel.onScrollDelta(available.y)
        return Offset.Zero
      }
    }
  }

  // Left-edge swipe detector: swiping from the left edge opens hamburger menu from ANY screen!
  var touchStartX by remember { mutableFloatStateOf(0f) }
  var totalDragX by remember { mutableFloatStateOf(0f) }
  val density = androidx.compose.ui.platform.LocalDensity.current
  val edgeThresholdPx = remember(density) { with(density) { 48.dp.toPx() } }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .nestedScroll(nestedScrollConnection)
      .pointerInput(Unit) {
        detectHorizontalDragGestures(
          onDragStart = { offset ->
            touchStartX = offset.x
            totalDragX = 0f
          },
          onDragEnd = {
            if (touchStartX <= edgeThresholdPx && totalDragX > 45f) {
              viewModel.openHamburgerMenu()
            }
            totalDragX = 0f
          },
          onDragCancel = { totalDragX = 0f },
          onHorizontalDrag = { _, dragAmount ->
            totalDragX += dragAmount
          }
        )
      }
  ) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = androidx.compose.ui.graphics.Color.Transparent,
      bottomBar = {
        AnimatedVisibility(
          visible = isBottomBarVisible,
          enter = slideInVertically { it } + fadeIn(),
          exit = slideOutVertically { it } + fadeOut()
        ) {
          NavigationBar(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.testTag("main_bottom_nav")
          ) {
            // Tracker Tab
            NavigationBarItem(
              selected = currentTab == MainNavigationTab.TRACKER,
              onClick = { onTabChanged(MainNavigationTab.TRACKER) },
              colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
              ),
              icon = {
                Icon(
                  imageVector = if (currentTab == MainNavigationTab.TRACKER) Icons.Filled.CheckCircle
                  else Icons.Outlined.CheckCircleOutline,
                  contentDescription = "Tracker"
                )
              },
              label = { Text("Tracker", fontWeight = FontWeight.SemiBold) },
              modifier = Modifier.testTag("nav_item_tracker")
            )

            // Plan Tab
            NavigationBarItem(
              selected = currentTab == MainNavigationTab.PLAN,
              onClick = { onTabChanged(MainNavigationTab.PLAN) },
              colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
              ),
              icon = {
                Icon(
                  imageVector = if (currentTab == MainNavigationTab.PLAN) Icons.Filled.CalendarMonth
                  else Icons.Outlined.CalendarMonth,
                  contentDescription = "Plan"
                )
              },
              label = { Text("Plan", fontWeight = FontWeight.SemiBold) },
              modifier = Modifier.testTag("nav_item_plan")
            )

            // Detail Tab
            NavigationBarItem(
              selected = currentTab == MainNavigationTab.DETAIL,
              onClick = { onTabChanged(MainNavigationTab.DETAIL) },
              colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
              ),
              icon = {
                Icon(
                  imageVector = if (currentTab == MainNavigationTab.DETAIL) Icons.Filled.Insights
                  else Icons.Outlined.Insights,
                  contentDescription = "Detail"
                )
              },
              label = { Text("Detail", fontWeight = FontWeight.SemiBold) },
              modifier = Modifier.testTag("nav_item_detail")
            )
          }
        }
      }
    ) { innerPadding ->
      when (currentTab) {
        MainNavigationTab.TRACKER -> {
          TrackerScreen(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
          )
        }
        MainNavigationTab.PLAN -> {
          PlanScreen(
            viewModel = viewModel,
            onJumpToTask = { plannedTask ->
              viewModel.jumpToPlannedTask(plannedTask)
              onTabChanged(MainNavigationTab.TRACKER)
            },
            modifier = Modifier.padding(innerPadding)
          )
        }
        MainNavigationTab.DETAIL -> {
          DetailScreen(
            viewModel = viewModel,
            onNavigateToDate = { targetDate ->
              viewModel.selectDate(targetDate)
              onTabChanged(MainNavigationTab.TRACKER)
            },
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }

    // Global Hamburger Menu Dialog (accessible from any screen & left edge swipe)
    if (isHamburgerOpen) {
      HamburgerMenuDialog(
        viewModel = viewModel,
        onDismiss = { viewModel.closeHamburgerMenu() }
      )
    }
  }
}
