package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HexColorPalette
import com.example.ui.theme.parseHexColor

enum class ColorTarget {
  UI_ACCENT,
  BACKGROUND,
  TEXT
}

@Composable
fun ThemePickerDialog(
  currentUiHex: String,
  currentBgHex: String,
  currentTextHex: String,
  onApplyTheme: (uiHex: String, bgHex: String, textHex: String) -> Unit,
  onDismiss: () -> Unit
) {
  val haptic = LocalHapticFeedback.current

  var selectedTarget by remember { mutableStateOf(ColorTarget.UI_ACCENT) }
  var uiHex by remember { mutableStateOf(currentUiHex) }
  var bgHex by remember { mutableStateOf(currentBgHex) }
  var textHex by remember { mutableStateOf(currentTextHex) }

  val currentActiveHex = when (selectedTarget) {
    ColorTarget.UI_ACCENT -> uiHex
    ColorTarget.BACKGROUND -> bgHex
    ColorTarget.TEXT -> textHex
  }

  fun updateActiveHex(newHex: String) {
    val clean = if (newHex.startsWith("#")) newHex else "#$newHex"
    when (selectedTarget) {
      ColorTarget.UI_ACCENT -> uiHex = clean
      ColorTarget.BACKGROUND -> bgHex = clean
      ColorTarget.TEXT -> textHex = clean
    }
  }

  val previewUiColor = parseHexColor(uiHex, Color(0xFF3B82F6))
  val previewBgColor = parseHexColor(bgHex, Color(0xFF121212))
  val previewTextColor = parseHexColor(textHex, Color(0xFFFFFFFF))

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("hex_theme_picker_dialog"),
    shape = RoundedCornerShape(24.dp),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Palette,
            contentDescription = null,
            tint = previewUiColor,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Theme & Color Chart",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
        }

        IconButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            uiHex = "#3B82F6"
            bgHex = "#121212"
            textHex = "#FFFFFF"
          },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Reset Defaults",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Target selection chips
        Text(
          text = "Select What to Customize:",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChip(
            selected = selectedTarget == ColorTarget.UI_ACCENT,
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              selectedTarget = ColorTarget.UI_ACCENT
            },
            label = { Text("UI Accent", fontSize = 11.sp) },
            leadingIcon = {
              Box(
                modifier = Modifier
                  .size(12.dp)
                  .clip(CircleShape)
                  .background(previewUiColor)
              )
            },
            modifier = Modifier.weight(1f)
          )

          FilterChip(
            selected = selectedTarget == ColorTarget.BACKGROUND,
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              selectedTarget = ColorTarget.BACKGROUND
            },
            label = { Text("Background", fontSize = 11.sp) },
            leadingIcon = {
              Box(
                modifier = Modifier
                  .size(12.dp)
                  .clip(CircleShape)
                  .background(previewBgColor)
                  .border(0.5.dp, Color.Gray, CircleShape)
              )
            },
            modifier = Modifier.weight(1f)
          )

          FilterChip(
            selected = selectedTarget == ColorTarget.TEXT,
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              selectedTarget = ColorTarget.TEXT
            },
            label = { Text("Text Color", fontSize = 11.sp) },
            leadingIcon = {
              Box(
                modifier = Modifier
                  .size(12.dp)
                  .clip(CircleShape)
                  .background(previewTextColor)
                  .border(0.5.dp, Color.Gray, CircleShape)
              )
            },
            modifier = Modifier.weight(1f)
          )
        }

        // Hex Code Text Input Field
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = currentActiveHex,
            onValueChange = { updateActiveHex(it) },
            label = {
              Text(
                when (selectedTarget) {
                  ColorTarget.UI_ACCENT -> "UI Hex Code (#RRGGBB)"
                  ColorTarget.BACKGROUND -> "Background Hex (#RRGGBB)"
                  ColorTarget.TEXT -> "Text Hex Code (#RRGGBB)"
                }
              )
            },
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.width(10.dp))

          // Current Color Swatch Preview
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(parseHexColor(currentActiveHex, Color.Gray))
              .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
          )
        }

        // Live Transparent Effect Preview Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(previewBgColor)
            .padding(10.dp)
        ) {
          // Transparent card inside
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = previewUiColor.copy(alpha = 0.18f),
            border = androidx.compose.foundation.BorderStroke(1.dp, previewUiColor.copy(alpha = 0.45f))
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Live Theme Preview",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = previewTextColor
                )
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(previewUiColor)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "Accent",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                  )
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Semi-transparent card effect is preserved across the entire app.",
                style = MaterialTheme.typography.bodySmall,
                color = previewTextColor.copy(alpha = 0.8f)
              )
            }
          }
        }

        // Hexadecimal Color Chart Grid
        Text(
          text = "Hexadecimal Color Chart (Tap any swatch):",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          HexColorPalette.rows.forEach { colorRow ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              colorRow.forEach { hex ->
                val swatchColor = parseHexColor(hex, Color.Transparent)
                val isSelected = currentActiveHex.equals(hex, ignoreCase = true)

                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(swatchColor)
                    .border(
                      width = if (isSelected) 2.dp else 0.5.dp,
                      color = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.35f),
                      shape = RoundedCornerShape(6.dp)
                    )
                    .clickable {
                      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                      updateActiveHex(hex)
                    },
                  contentAlignment = Alignment.Center
                ) {
                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = "Selected",
                      tint = if (hex == "#FFFFFF" || hex.endsWith("F2") || hex.endsWith("EB") || hex.endsWith("F5")) Color.Black else Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          onApplyTheme(uiHex, bgHex, textHex)
          onDismiss()
        },
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("Apply Theme")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
        Text("Cancel")
      }
    }
  )
}
