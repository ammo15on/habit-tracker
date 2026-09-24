package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.HabitViewModel

@Composable
fun AnalyticsScreen(
  viewModel: HabitViewModel,
  onNavigateToDate: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  DetailScreen(
    viewModel = viewModel,
    onNavigateToDate = onNavigateToDate,
    modifier = modifier
  )
}
