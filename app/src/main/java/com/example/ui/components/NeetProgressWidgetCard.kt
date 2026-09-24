package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NeetChapter
import com.example.ui.theme.RatingBestGreen
import com.example.widget.NeetProgressAppWidgetProvider
import java.util.Locale

@Composable
fun NeetProgressWidgetCard(
  chapters: List<NeetChapter>,
  modifier: Modifier = Modifier,
  showPinButton: Boolean = true
) {
  val context = LocalContext.current

  val totalChapters = chapters.size
  val completedChapters = chapters.count { it.isCompleted }
  val pyqDoneChapters = chapters.count { it.isPyqDone }
  val ncertDoneChapters = chapters.count { it.isRevisionDone }

  val chapterProgress = if (totalChapters > 0) completedChapters.toFloat() / totalChapters else 0f
  val pyqProgress = if (totalChapters > 0) pyqDoneChapters.toFloat() / totalChapters else 0f
  val ncertProgress = if (totalChapters > 0) ncertDoneChapters.toFloat() / totalChapters else 0f

  val animatedChProgress by animateFloatAsState(targetValue = chapterProgress, label = "chProgress")
  val animatedPyqProgress by animateFloatAsState(targetValue = pyqProgress, label = "pyqProgress")
  val animatedNcertProgress by animateFloatAsState(targetValue = ncertProgress, label = "ncertProgress")

  val overallPercent = if (totalChapters > 0) {
    (((completedChapters + pyqDoneChapters + ncertDoneChapters).toFloat() / (totalChapters * 3f)) * 100).toInt()
  } else 0

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .border(
          width = 1.5.dp,
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
          shape = RoundedCornerShape(20.dp)
        )
        .padding(16.dp)
    ) {
      // Header Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(RatingBestGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Widgets,
              contentDescription = "Widget",
              tint = RatingBestGreen,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Home Screen Widget",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Live NEET progress overview • $overallPercent% total",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        if (showPinButton) {
          Button(
            onClick = {
              val pinned = NeetProgressAppWidgetProvider.pinWidgetToHomeScreen(context)
              if (pinned) {
                Toast.makeText(context, "Adding widget to home screen...", Toast.LENGTH_SHORT).show()
              } else {
                Toast.makeText(
                  context,
                  "Long-press your phone's Home Screen and choose 'Habit Tracker' widget",
                  Toast.LENGTH_LONG
                ).show()
              }
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.height(34.dp)
          ) {
            Icon(Icons.Default.PinDrop, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Pin Widget", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 3 Main Metric Columns: Chapters, PYQ, NCERT
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Metric 1: Chapters Completed
        WidgetProgressColumn(
          title = "CHAPTERS",
          count = completedChapters,
          total = totalChapters,
          progress = animatedChProgress,
          color = RatingBestGreen,
          icon = Icons.Default.MenuBook,
          unitLabel = "chapters done",
          modifier = Modifier.weight(1f)
        )

        // Metric 2: PYQ Solved
        WidgetProgressColumn(
          title = "PYQ DONE",
          count = pyqDoneChapters,
          total = totalChapters,
          progress = animatedPyqProgress,
          color = Color(0xFF3B82F6),
          icon = Icons.Default.Quiz,
          unitLabel = "pyqs solved",
          modifier = Modifier.weight(1f)
        )

        // Metric 3: NCERT Read
        WidgetProgressColumn(
          title = "NCERT READ",
          count = ncertDoneChapters,
          total = totalChapters,
          progress = animatedNcertProgress,
          color = Color(0xFFF59E0B),
          icon = Icons.Default.Bookmark,
          unitLabel = "ncert revised",
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Subject Mini Breakdown row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        listOf("Botany", "Zoology", "Physics", "Chemistry").forEach { subject ->
          val subChapters = chapters.filter { it.subject.equals(subject, ignoreCase = true) }
          val subDone = subChapters.count { it.isCompleted }
          val subTotal = subChapters.size

          val subColor = when (subject) {
            "Botany" -> Color(0xFF059669)
            "Zoology" -> Color(0xFFD97706)
            "Physics" -> Color(0xFF2563EB)
            else -> Color(0xFF7C3AED)
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(subColor.copy(alpha = 0.12f))
              .border(0.8.dp, subColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
              .padding(vertical = 4.dp, horizontal = 2.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = subject.take(4),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = subColor
              )
              Text(
                text = "$subDone/$subTotal",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun WidgetProgressColumn(
  title: String,
  count: Int,
  total: Int,
  progress: Float,
  color: Color,
  icon: ImageVector,
  unitLabel: String,
  modifier: Modifier = Modifier
) {
  val percent = if (total > 0) ((count.toFloat() / total) * 100).toInt() else 0

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .background(color.copy(alpha = 0.10f))
      .border(1.dp, color.copy(alpha = 0.28f), RoundedCornerShape(14.dp))
      .padding(10.dp)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = color
        )
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = color,
          modifier = Modifier.size(13.dp)
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(verticalAlignment = Alignment.Bottom) {
        Text(
          text = "$count",
          fontSize = 18.sp,
          fontWeight = FontWeight.ExtraBold,
          color = color
        )
        Text(
          text = "/$total",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(bottom = 1.dp, start = 2.dp)
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(5.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
      )

      Spacer(modifier = Modifier.height(4.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "$percent%",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = color
        )
        Text(
          text = "${total - count} left",
          fontSize = 9.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
