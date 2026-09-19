package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RatingType
import com.example.ui.theme.RatingAverageGrey
import com.example.ui.theme.RatingBestGreen
import com.example.ui.theme.RatingWorstBlack

/**
 * Small Day Rating component positioned at the bottom of the UI without a background box,
 * sitting right above the Tracker, Plan, and Analytics navigation.
 */
@Composable
fun DayRatingSection(
  selectedRating: RatingType?,
  dayAbbreviation: String,
  fullDateLabel: String,
  onRatingSelected: (RatingType) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag("day_rating_bottom_bar"),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Left Label: Day Indicator & "Rate day"
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
          .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = dayAbbreviation,
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 11.sp
          )
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Rate this day:",
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    // Right: 3 Compact Emoji rating buttons without background box
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      SmallRatingEmojiButton(
        emoji = "😊",
        title = "Best",
        isSelected = selectedRating == RatingType.BEST,
        activeColor = RatingBestGreen,
        onClick = { onRatingSelected(RatingType.BEST) },
        testTag = "rating_box_best"
      )

      SmallRatingEmojiButton(
        emoji = "😐",
        title = "Average",
        isSelected = selectedRating == RatingType.AVERAGE,
        activeColor = RatingAverageGrey,
        onClick = { onRatingSelected(RatingType.AVERAGE) },
        testTag = "rating_box_average"
      )

      SmallRatingEmojiButton(
        emoji = "😢",
        title = "Worst",
        isSelected = selectedRating == RatingType.WORST,
        activeColor = RatingWorstBlack,
        onClick = { onRatingSelected(RatingType.WORST) },
        testTag = "rating_box_worst"
      )
    }
  }
}

@Composable
private fun SmallRatingEmojiButton(
  emoji: String,
  title: String,
  isSelected: Boolean,
  activeColor: Color,
  onClick: () -> Unit,
  testTag: String
) {
  val bgColor by animateColorAsState(
    targetValue = if (isSelected) activeColor.copy(alpha = 0.2f) else Color.Transparent,
    animationSpec = spring(),
    label = "emojiBgColor"
  )

  val borderColor = if (isSelected) {
    activeColor
  } else {
    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
  }

  Box(
    modifier = Modifier
      .size(42.dp)
      .clip(CircleShape)
      .background(bgColor)
      .border(
        width = if (isSelected) 2.dp else 1.dp,
        color = borderColor,
        shape = CircleShape
      )
      .clickable { onClick() }
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = emoji,
      fontSize = 20.sp
    )
  }
}
