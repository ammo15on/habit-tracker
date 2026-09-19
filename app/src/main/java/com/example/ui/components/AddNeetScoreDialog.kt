package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.NeetTestScore
import com.example.ui.theme.RatingBestGreen
import com.example.util.DateUtils

@Composable
fun AddNeetScoreDialog(
  onDismiss: () -> Unit,
  onConfirm: (NeetTestScore) -> Unit
) {
  var testName by remember { mutableStateOf("") }
  var testDate by remember { mutableStateOf(DateUtils.today()) }
  var phyScore by remember { mutableStateOf("") }
  var chemScore by remember { mutableStateOf("") }
  var botScore by remember { mutableStateOf("") }
  var zooScore by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Assignment,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Log NEET Exam Marks",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = testName,
          onValueChange = { testName = it },
          label = { Text("Test Name / Series") },
          placeholder = { Text("e.g. Major Test 1, Mock 5") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = testDate,
          onValueChange = { testDate = it },
          label = { Text("Date (YYYY-MM-DD)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Subject Marks (Max 180 each)",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = phyScore,
            onValueChange = { phyScore = it.filter { ch -> ch.isDigit() } },
            label = { Text("Physics") },
            placeholder = { Text("180") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )

          OutlinedTextField(
            value = chemScore,
            onValueChange = { chemScore = it.filter { ch -> ch.isDigit() } },
            label = { Text("Chem") },
            placeholder = { Text("180") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = botScore,
            onValueChange = { botScore = it.filter { ch -> ch.isDigit() } },
            label = { Text("Botany") },
            placeholder = { Text("180") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )

          OutlinedTextField(
            value = zooScore,
            onValueChange = { zooScore = it.filter { ch -> ch.isDigit() } },
            label = { Text("Zoology") },
            placeholder = { Text("180") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )
        }

        val p = phyScore.toIntOrNull() ?: 0
        val c = chemScore.toIntOrNull() ?: 0
        val b = botScore.toIntOrNull() ?: 0
        val z = zooScore.toIntOrNull() ?: 0
        val total = p + c + b + z

        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "Total: $total / 720  (${if (total > 0) "%.1f".format((total / 720f) * 100) else "0"}%)",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = RatingBestGreen
          )
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val p = phyScore.toIntOrNull() ?: 0
          val c = chemScore.toIntOrNull() ?: 0
          val b = botScore.toIntOrNull() ?: 0
          val z = zooScore.toIntOrNull() ?: 0
          val score = NeetTestScore(
            testName = if (testName.isBlank()) "NEET Mock Test" else testName.trim(),
            date = testDate.trim(),
            physicsScore = p,
            chemistryScore = c,
            botanyScore = b,
            zoologyScore = z
          )
          onConfirm(score)
        },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RatingBestGreen)
      ) {
        Text("Save Marks", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("Cancel")
      }
    }
  )
}
