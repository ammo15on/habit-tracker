package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

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
      val currentTextHex by viewModel.selectedTextHex.collectAsState()
      val currentThemeColor by viewModel.selectedThemeColor.collectAsState()
      val currentFontColor by viewModel.selectedFontColor.collectAsState()
      val currentBackgroundUri by viewModel.selectedBackgroundImageUri.collectAsState()
      val currentUiOpacity by viewModel.selectedUiOpacity.collectAsState()

      MyApplicationTheme(
        uiHex = currentUiHex,
        textHex = currentTextHex,
        themeColor = currentThemeColor,
        fontColor = currentFontColor,
        uiOpacity = currentUiOpacity,
        backgroundImageUri = currentBackgroundUri
      ) {
        MainAppContent(
          viewModel = viewModel,
          hasBackgroundImage = !currentBackgroundUri.isNullOrBlank()
        )
      }
    }
  }
}

@Composable
fun MainAppContent(
  viewModel: HabitViewModel,
  hasBackgroundImage: Boolean = false
) {
  var currentTab by remember { mutableStateOf(MainNavigationTab.TRACKER) }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = androidx.compose.ui.graphics.Color.Transparent,
    bottomBar = {
      NavigationBar(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        tonalElevation = 0.dp,
        modifier = Modifier.testTag("main_bottom_nav")
      ) {
        // Tracker Tab
        NavigationBarItem(
          selected = currentTab == MainNavigationTab.TRACKER,
          onClick = { currentTab = MainNavigationTab.TRACKER },
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
          onClick = { currentTab = MainNavigationTab.PLAN },
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
          onClick = { currentTab = MainNavigationTab.DETAIL },
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
            currentTab = MainNavigationTab.TRACKER
          },
          modifier = Modifier.padding(innerPadding)
        )
      }
      MainNavigationTab.DETAIL -> {
        DetailScreen(
          viewModel = viewModel,
          onNavigateToDate = { targetDate ->
            viewModel.selectDate(targetDate)
            currentTab = MainNavigationTab.TRACKER
          },
          modifier = Modifier.padding(innerPadding)
        )
      }
    }
  }
}
