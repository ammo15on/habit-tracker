package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.HabitViewModel
import com.example.ui.components.HamburgerMenuDialog
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.PlanScreen
import com.example.ui.screens.TrackerScreen
import com.example.ui.theme.MyApplicationTheme

enum class MainNavigationTab(val title: String, val icon: ImageVector) {
  TRACKER("Tracker", Icons.Default.CheckCircle),
  PLAN("Planner", Icons.Default.CalendarMonth),
  DETAIL("NEET & AI", Icons.Default.Analytics)
}

class MainActivity : ComponentActivity() {
  private val viewModel: HabitViewModel by viewModels {
    val db = com.example.data.db.AppDatabase.getDatabase(applicationContext)
    val repo = com.example.data.repository.HabitRepository(db.habitDao())
    val themePrefs = com.example.util.ThemePreferences(applicationContext)
    val goalPrefs = com.example.util.GoalPreferences(applicationContext)
    HabitViewModel.provideFactory(repo, themePrefs, goalPrefs, applicationContext)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      val customColorHex by viewModel.selectedColorHex.collectAsStateWithLifecycle()
      val customFontHex by viewModel.selectedFontHex.collectAsStateWithLifecycle()
      val currentBgUri by viewModel.selectedBackgroundImageUri.collectAsStateWithLifecycle()
      val currentUiOpacity by viewModel.selectedUiOpacity.collectAsStateWithLifecycle()
      val currentTextScale by viewModel.selectedTextSizeScale.collectAsStateWithLifecycle()
      val isHamburgerOpen by viewModel.isHamburgerOpen.collectAsStateWithLifecycle()

      var currentTab by remember { mutableStateOf(MainNavigationTab.TRACKER) }

      MyApplicationTheme(
        uiHex = customColorHex,
        textHex = customFontHex,
        textSizeScale = currentTextScale
      ) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .pointerInput(Unit) {
                // Edge swipe from left to open hamburger menu
                detectHorizontalDragGestures { change, dragAmount ->
                  if (change.position.x < 100f && dragAmount > 25f) {
                    viewModel.openHamburger()
                  }
                }
              }
          ) {
            // Optional Background Wallpaper
            if (!currentBgUri.isNullOrBlank()) {
              AsyncImage(
                model = currentBgUri,
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

            MainAppScaffold(
              viewModel = viewModel,
              currentTab = currentTab,
              onTabSelected = { currentTab = it }
            )

            // Full Screen Hamburger Dialog
            if (isHamburgerOpen) {
              HamburgerMenuDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.closeHamburger() }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun MainAppScaffold(
  viewModel: HabitViewModel,
  currentTab: MainNavigationTab,
  onTabSelected: (MainNavigationTab) -> Unit
) {
  val isBottomBarVisible by viewModel.isBottomBarVisible.collectAsStateWithLifecycle()

  Scaffold(
    bottomBar = {
      AnimatedVisibility(
        visible = isBottomBarVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
      ) {
        NavigationBar(
          containerColor = Color.Transparent,
          tonalElevation = 0.dp,
          modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF131722).copy(alpha = 0.90f))
        ) {
          MainNavigationTab.entries.forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
              selected = isSelected,
              onClick = { onTabSelected(tab) },
              icon = {
                Icon(
                  tab.icon,
                  contentDescription = tab.title
                )
              },
              label = {
                Text(
                  tab.title,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                )
              },
              colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
              )
            )
          }
        }
      }
    },
    containerColor = Color.Transparent
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      when (currentTab) {
        MainNavigationTab.TRACKER -> TrackerScreen(viewModel = viewModel)
        MainNavigationTab.PLAN -> PlanScreen(
          viewModel = viewModel,
          onJumpToTask = { task ->
            viewModel.setSelectedDate(task.date)
            onTabSelected(MainNavigationTab.TRACKER)
          }
        )
        MainNavigationTab.DETAIL -> DetailScreen(
          viewModel = viewModel,
          onNavigateToDate = { date ->
            viewModel.setSelectedDate(date)
            onTabSelected(MainNavigationTab.TRACKER)
          }
        )
      }
    }
  }
}
