package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NeetChapter
import com.example.ui.theme.RatingBestGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditChapterDialog(
  chapter: NeetChapter? = null,
  initialSubject: String = "Botany",
  onDismiss: () -> Unit,
  onSave: (name: String, subject: String, isCompleted: Boolean, isPyqDone: Boolean, isRevisionDone: Boolean, notes: String) -> Unit,
  onDelete: (() -> Unit)? = null
) {
  val isEditing = chapter != null
  var name by remember { mutableStateOf(chapter?.name ?: "") }
  var subject by remember { mutableStateOf(chapter?.subject ?: initialSubject) }
  var isCompleted by remember { mutableStateOf(chapter?.isCompleted ?: false) }
  var isPyqDone by remember { mutableStateOf(chapter?.isPyqDone ?: false) }
  var isRevisionDone by remember { mutableStateOf(chapter?.isRevisionDone ?: false) }
  var notes by remember { mutableStateOf(chapter?.notes ?: "") }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("add_edit_chapter_dialog"),
    shape = RoundedCornerShape(24.dp),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Book,
            contentDescription = null,
            tint = RatingBestGreen,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isEditing) "Edit NEET Chapter" else "Add NEET Chapter",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
        }

        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        Text(
          text = "Chapter Name",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          placeholder = { Text("e.g. Molecular Basis of Inheritance") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("chapter_name_input"),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Subject",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          NeetChapter.SUBJECTS.forEach { subj ->
            FilterChip(
              selected = (subject == subj),
              onClick = { subject = subj },
              label = { Text(subj, fontSize = 12.sp) },
              shape = RoundedCornerShape(8.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Progress Checkmarks",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Checkmark 1: Chapter Completed
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(
            checked = isCompleted,
            onCheckedChange = { isCompleted = it }
          )
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text("Chapter Completed", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(
              text = if (isCompleted) "✓ Status: Completed" else "Status: To Complete",
              style = MaterialTheme.typography.bodySmall,
              color = if (isCompleted) RatingBestGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Checkmark 2: PYQ Done
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(
            checked = isPyqDone,
            onCheckedChange = { isPyqDone = it }
          )
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text("PYQ Done", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(
              text = if (isPyqDone) "✓ Previous year questions solved" else "PYQs pending",
              style = MaterialTheme.typography.bodySmall,
              color = if (isPyqDone) RatingBestGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Checkmark 3: Revision / NCERT Read
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(
            checked = isRevisionDone,
            onCheckedChange = { isRevisionDone = it }
          )
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text("NCERT & Revision Done", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(
              text = if (isRevisionDone) "✓ Read NCERT line by line & notes revised" else "NCERT reading pending",
              style = MaterialTheme.typography.bodySmall,
              color = if (isRevisionDone) RatingBestGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Notes / Important Formulas (Optional)",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          placeholder = { Text("e.g. Focus on Watson-Crick model, Meselson-Stahl") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 2,
          maxLines = 4,
          shape = RoundedCornerShape(12.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onSave(name.trim(), subject, isCompleted, isPyqDone, isRevisionDone, notes.trim())
          }
        },
        enabled = name.isNotBlank(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RatingBestGreen)
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(if (isEditing) "Save Changes" else "Add Chapter")
      }
    },
    dismissButton = {
      if (isEditing && onDelete != null) {
        OutlinedButton(
          onClick = onDelete,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
          Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Delete")
        }
      }
    }
  )
}
