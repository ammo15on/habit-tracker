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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.db.AppDatabase
import com.example.data.repository.HabitRepository
import com.example.ui.HabitViewModel
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.PlanScreen
import com.example.ui.screens.TrackerScreen
import com.example.ui.theme.MyApplicationTheme

enum class MainNavigationTab {
  TRACKER,
  PLAN,
  ANALYTICS
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = AppDatabase.getDatabase(applicationContext)
    val repository = HabitRepository(database.habitDao())

    setContent {
      MyApplicationTheme {
        val viewModel: HabitViewModel = viewModel(
          factory = HabitViewModel.provideFactory(repository)
        )

        MainAppContent(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun MainAppContent(viewModel: HabitViewModel) {
  var currentTab by remember { mutableStateOf(MainNavigationTab.TRACKER) }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    bottomBar = {
      NavigationBar(
        modifier = Modifier.testTag("main_bottom_nav")
      ) {
        // Tracker Tab
        NavigationBarItem(
          selected = currentTab == MainNavigationTab.TRACKER,
          onClick = { currentTab = MainNavigationTab.TRACKER },
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

        // Analytics Tab
        NavigationBarItem(
          selected = currentTab == MainNavigationTab.ANALYTICS,
          onClick = { currentTab = MainNavigationTab.ANALYTICS },
          icon = {
            Icon(
              imageVector = if (currentTab == MainNavigationTab.ANALYTICS) Icons.Filled.Analytics
              else Icons.Outlined.Analytics,
              contentDescription = "Analytics"
            )
          },
          label = { Text("Analytics", fontWeight = FontWeight.SemiBold) },
          modifier = Modifier.testTag("nav_item_analytics")
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
      MainNavigationTab.ANALYTICS -> {
        AnalyticsScreen(
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
