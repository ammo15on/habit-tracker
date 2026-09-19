package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RatingType
import com.example.ui.theme.RatingAverageGrey
import com.example.ui.theme.RatingBestGreen
import com.example.ui.theme.RatingWorstBlack

@Composable
fun DayRatingSection(
  selectedRating: RatingType?,
  dayAbbreviation: String,
  fullDateLabel: String,
  onRatingSelected: (RatingType) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Rate This Day",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          ),
          color = MaterialTheme.colorScheme.onSurface
        )

        // Day of week indicator (e.g. "Mo" / Monday)
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = dayAbbreviation,
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Rating Boxes Row: [A] Best, [B] Average, [C] Worst
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        RatingBoxItem(
          code = "A",
          title = "Best",
          ratingType = RatingType.BEST,
          isSelected = selectedRating == RatingType.BEST,
          activeBgColor = RatingBestGreen,
          activeTextColor = Color.White,
          onClick = { onRatingSelected(RatingType.BEST) },
          testTag = "rating_box_best"
        )

        RatingBoxItem(
          code = "B",
          title = "Average",
          ratingType = RatingType.AVERAGE,
          isSelected = selectedRating == RatingType.AVERAGE,
          activeBgColor = RatingAverageGrey,
          activeTextColor = Color(0xFF1E293B),
          onClick = { onRatingSelected(RatingType.AVERAGE) },
          testTag = "rating_box_average"
        )

        RatingBoxItem(
          code = "C",
          title = "Worst",
          ratingType = RatingType.WORST,
          isSelected = selectedRating == RatingType.WORST,
          activeBgColor = RatingWorstBlack,
          activeTextColor = Color.White,
          onClick = { onRatingSelected(RatingType.WORST) },
          testTag = "rating_box_worst"
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = when (selectedRating) {
          RatingType.BEST -> "★ Marked as Best Day (turns Green)"
          RatingType.AVERAGE -> "• Marked as Average Day (turns Light Grey)"
          RatingType.WORST -> "▲ Marked as Worst Day (turns Black)"
          null -> "Select A, B, or C to rate $fullDateLabel"
        },
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
        color = when (selectedRating) {
          RatingType.BEST -> RatingBestGreen
          RatingType.AVERAGE -> MaterialTheme.colorScheme.onSurfaceVariant
          RatingType.WORST -> MaterialTheme.colorScheme.onSurface
          null -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
private fun RatingBoxItem(
  code: String,
  title: String,
  ratingType: RatingType,
  isSelected: Boolean,
  activeBgColor: Color,
  activeTextColor: Color,
  onClick: () -> Unit,
  testTag: String
) {
  val bgColor by animateColorAsState(
    targetValue = if (isSelected) activeBgColor else MaterialTheme.colorScheme.surface,
    animationSpec = spring(),
    label = "bgColor"
  )

  val contentColor by animateColorAsState(
    targetValue = if (isSelected) activeTextColor else MaterialTheme.colorScheme.onSurface,
    animationSpec = spring(),
    label = "contentColor"
  )

  val borderColor = if (isSelected) {
    activeBgColor
  } else {
    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
  }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clickable { onClick() }
      .padding(4.dp)
      .testTag(testTag)
  ) {
    Box(
      modifier = Modifier
        .size(68.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(bgColor)
        .border(
          width = if (isSelected) 2.dp else 1.dp,
          color = borderColor,
          shape = RoundedCornerShape(16.dp)
        ),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = "[$code]",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Black,
            fontSize = 18.sp
          ),
          color = contentColor
        )
        if (isSelected) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Selected",
            tint = contentColor,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = title,
      style = MaterialTheme.typography.labelMedium.copy(
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        fontSize = 13.sp
      ),
      color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
