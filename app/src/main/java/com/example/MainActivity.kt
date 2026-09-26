package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.HabitViewModel
import com.example.ui.components.HamburgerMenuContent
import com.example.ui.components.HamburgerMenuDialog
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.PlanScreen
import com.example.ui.screens.TrackerScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

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
          BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenWidth = maxWidth
            val density = LocalDensity.current
            val screenWidthPx = with(density) { screenWidth.toPx() }
            val edgeTriggerPx = with(density) { 48.dp.toPx() }

            val coroutineScope = rememberCoroutineScope()
            val dragOffset = remember { Animatable(0f) }
            var isEdgeSwiping by remember { mutableStateOf(false) }

            // When isHamburgerOpen changes programmatically, smoothly animate dragOffset
            LaunchedEffect(isHamburgerOpen) {
              if (isHamburgerOpen && dragOffset.value < screenWidthPx) {
                dragOffset.animateTo(
                  screenWidthPx,
                  spring(dampingRatio = 0.85f, stiffness = 400f)
                )
              } else if (!isHamburgerOpen && dragOffset.value > 0f) {
                dragOffset.animateTo(
                  0f,
                  spring(dampingRatio = 0.95f, stiffness = 400f)
                )
              }
            }

            Box(
              modifier = Modifier
                .fillMaxSize()
                .pointerInput(screenWidthPx, isHamburgerOpen) {
                  detectHorizontalDragGestures(
                    onDragStart = { offset ->
                      if (offset.x <= edgeTriggerPx || isHamburgerOpen) {
                        isEdgeSwiping = true
                      }
                    },
                    onDragEnd = {
                      if (isEdgeSwiping) {
                        isEdgeSwiping = false
                        coroutineScope.launch {
                          val current = dragOffset.value
                          val threshold = screenWidthPx * 0.30f
                          if (current > threshold) {
                            viewModel.openHamburger()
                            dragOffset.animateTo(screenWidthPx, spring(dampingRatio = 0.85f, stiffness = 400f))
                          } else {
                            viewModel.closeHamburger()
                            dragOffset.animateTo(0f, spring(dampingRatio = 0.95f, stiffness = 400f))
                          }
                        }
                      }
                    },
                    onDragCancel = {
                      if (isEdgeSwiping) {
                        isEdgeSwiping = false
                        coroutineScope.launch {
                          if (dragOffset.value > screenWidthPx * 0.5f) {
                            viewModel.openHamburger()
                            dragOffset.animateTo(screenWidthPx)
                          } else {
                            viewModel.closeHamburger()
                            dragOffset.animateTo(0f)
                          }
                        }
                      }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                      if (isEdgeSwiping) {
                        change.consume()
                        coroutineScope.launch {
                          val next = (dragOffset.value + dragAmount).coerceIn(0f, screenWidthPx)
                          dragOffset.snapTo(next)
                        }
                      }
                    }
                  )
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

              // Progressive Hamburger Overlay: Slides seamlessly as finger moves
              val currentX = dragOffset.value
              if (currentX > 0.5f || isHamburgerOpen) {
                val progress = (currentX / screenWidthPx).coerceIn(0f, 1f)

                // Progressive Scrim overlay (dimming the background)
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.60f * progress))
                    .clickable {
                      coroutineScope.launch {
                        viewModel.closeHamburger()
                        dragOffset.animateTo(0f)
                      }
                    }
                )

                // The Hamburger Menu Content moving smoothly with finger
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                      translationX = currentX - screenWidthPx
                    }
                ) {
                  HamburgerMenuContent(
                    viewModel = viewModel,
                    onDismiss = {
                      coroutineScope.launch {
                        viewModel.closeHamburger()
                        dragOffset.animateTo(0f)
                      }
                    }
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
