package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.NeetChapter
import com.example.data.model.NoteAttachment
import com.example.data.model.NoteEntity
import com.example.ui.HabitViewModel
import com.example.ui.components.ImageLightboxDialog
import com.example.ui.components.PdfViewerDialog
import com.example.util.NoteMediaUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FullScreenNotesHubDialog(
  viewModel: HabitViewModel,
  launchRequest: HabitViewModel.NotesLaunchRequest,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val allNotes by viewModel.allNotes.collectAsStateWithLifecycle()
  val allChapters by viewModel.allNeetChapters.collectAsStateWithLifecycle()
  val customSubjects by viewModel.customNoteSubjects.collectAsStateWithLifecycle()
  val customSubfolders by viewModel.customNoteSubfolders.collectAsStateWithLifecycle()
  val currentBgUri by viewModel.selectedBackgroundImageUri.collectAsStateWithLifecycle()
  val currentUiOpacity by viewModel.selectedUiOpacity.collectAsStateWithLifecycle()

  val launchedDirectlyToChapter = !launchRequest.initialSubject.isNullOrBlank() &&
    !launchRequest.initialChapterName.isNullOrBlank()

  var selectedSubject by remember(launchRequest) { mutableStateOf(launchRequest.initialSubject) }
  var selectedChapterName by remember(launchRequest) { mutableStateOf(launchRequest.initialChapterName) }
  var selectedChapterId by remember(launchRequest) { mutableStateOf(launchRequest.initialChapterId) }
  var editingNote by remember { mutableStateOf<NoteEntity?>(null) }

  var searchQuery by remember { mutableStateOf("") }
  var showCreateSubjectDialog by remember { mutableStateOf(false) }
  var showCreateSubfolderDialog by remember { mutableStateOf(false) }
  var previewImageAttachment by remember { mutableStateOf<NoteAttachment?>(null) }
  var previewPdfAttachment by remember { mutableStateOf<NoteAttachment?>(null) }

  val handleBackNavigation: () -> Unit = {
    when {
      editingNote != null -> editingNote = null
      launchedDirectlyToChapter -> onDismiss()
      selectedChapterName != null -> {
        selectedChapterName = null
        selectedChapterId = null
      }
      selectedSubject != null -> selectedSubject = null
      else -> onDismiss()
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
  ) {
    BackHandler(enabled = true) {
      handleBackNavigation()
    }

    Surface(
      modifier = Modifier
        .fillMaxSize()
        .testTag("full_screen_notes_hub"),
      color = MaterialTheme.colorScheme.background
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
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

        val currentNoteToEdit = editingNote
        if (currentNoteToEdit != null) {
          NoteEditorScreen(
            initialNote = currentNoteToEdit,
            allChapters = allChapters,
            customSubjects = customSubjects,
            customSubfolders = customSubfolders,
            onSave = { saved ->
              viewModel.saveNote(saved) {
                editingNote = null
              }
            },
            onDelete = { toDelete ->
              if (toDelete.id > 0) {
                viewModel.deleteNote(toDelete)
              }
              editingNote = null
            },
            onCancel = { editingNote = null },
            onPreviewImage = { previewImageAttachment = it },
            onPreviewPdf = { previewPdfAttachment = it }
          )
        } else {
          val subjectsList = remember(customSubjects, allChapters, allNotes) {
            val ordered = linkedSetOf<String>()
            NeetChapter.SUBJECTS.forEach { ordered.add(it) }
            customSubjects.forEach { if (it.isNotBlank()) ordered.add(it.trim()) }
            allChapters.forEach { if (it.subject.isNotBlank()) ordered.add(it.subject.trim()) }
            allNotes.forEach { n ->
              if (n.subject.isNotBlank() && ordered.none { it.equals(n.subject, ignoreCase = true) }) {
                ordered.add(n.subject.trim())
              }
            }
            ordered.toList()
          }

          Scaffold(
            topBar = {
              TopAppBar(
                title = {
                  Column {
                    Text(
                      text = when {
                        selectedChapterName != null -> selectedChapterName!!
                        selectedSubject != null -> selectedSubject!!
                        else -> "Notes & Study Material"
                      },
                      fontWeight = FontWeight.Bold,
                      fontSize = 18.sp,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    if (selectedSubject != null) {
                      Text(
                        text = if (selectedChapterName != null) {
                          "${selectedSubject} › Chapter Subfolder"
                        } else {
                          "Notes › Subject Folder"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                },
                navigationIcon = {
                  IconButton(
                    onClick = handleBackNavigation,
                    modifier = Modifier.testTag("notes_back_btn")
                  ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                  }
                },
                actions = {
                  if (selectedSubject == null) {
                    IconButton(
                      onClick = { showCreateSubjectDialog = true },
                      modifier = Modifier.testTag("notes_new_subject_btn")
                    ) {
                      Icon(Icons.Default.CreateNewFolder, contentDescription = "New Subject Folder")
                    }
                  } else if (selectedChapterName == null) {
                    IconButton(
                      onClick = { showCreateSubfolderDialog = true },
                      modifier = Modifier.testTag("notes_new_subfolder_btn")
                    ) {
                      Icon(Icons.Default.CreateNewFolder, contentDescription = "New Chapter Subfolder")
                    }
                  }
                  IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("notes_close_btn")
                  ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Notes")
                  }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
              )
            },
            floatingActionButton = {
              FloatingActionButton(
                onClick = {
                  editingNote = NoteEntity(
                    title = if (selectedChapterName != null) "${selectedChapterName} Notes" else "",
                    subject = selectedSubject ?: "",
                    chapterName = selectedChapterName ?: "",
                    chapterId = selectedChapterId
                  )
                },
                modifier = Modifier.testTag("notes_fab_add")
              ) {
                Icon(Icons.Default.NoteAdd, contentDescription = "Create Note")
              }
            },
            containerColor = Color.Transparent
          ) { padding ->
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
            ) {
              // Search bar on Root & Subject screens
              if (selectedChapterName == null) {
                OutlinedTextField(
                  value = searchQuery,
                  onValueChange = { searchQuery = it },
                  placeholder = { Text("Search notes, subjects, chapters...", fontSize = 13.sp) },
                  leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                  trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                      IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                      }
                    }
                  },
                  singleLine = true,
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notes_search_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
              }

              val trimmedQuery = searchQuery.trim()
              if (trimmedQuery.isNotBlank() && selectedChapterName == null) {
                val matchingNotes = allNotes.filter { n ->
                  val matchesFolder = selectedSubject == null || n.subject.equals(selectedSubject, ignoreCase = true)
                  val matchesText = n.title.contains(trimmedQuery, ignoreCase = true) ||
                    n.content.contains(trimmedQuery, ignoreCase = true) ||
                    n.subject.contains(trimmedQuery, ignoreCase = true) ||
                    n.chapterName.contains(trimmedQuery, ignoreCase = true)
                  matchesFolder && matchesText
                }
                LazyColumn(
                  modifier = Modifier.fillMaxSize(),
                  contentPadding = PaddingValues(bottom = 88.dp),
                  verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  item {
                    Text(
                      text = "Search Results (${matchingNotes.size})",
                      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                  }
                  if (matchingNotes.isEmpty()) {
                    item {
                      EmptyNotesCard(
                        title = "No matching notes",
                        subtitle = "Try another keyword or create a new note."
                      )
                    }
                  } else {
                    items(matchingNotes, key = { it.id }) { note ->
                      NoteListCard(
                        note = note,
                        showFolderBadge = true,
                        onOpen = { editingNote = note },
                        onDelete = { viewModel.deleteNote(note) },
                        onPreviewImage = { previewImageAttachment = it },
                        onPreviewPdf = { previewPdfAttachment = it }
                      )
                    }
                  }
                }
              } else if (selectedSubject == null) {
                // ROOT VIEW: Subject Folders + Normal Notes (No Subfolder)
                val normalNotes = remember(allNotes) {
                  allNotes.filter { it.subject.isBlank() && it.chapterName.isBlank() }
                }

                LazyColumn(
                  modifier = Modifier.fillMaxSize(),
                  contentPadding = PaddingValues(bottom = 88.dp),
                  verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                  // Quick Action Bar
                  item {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      Button(
                        onClick = {
                          editingNote = NoteEntity(subject = "", chapterName = "", chapterId = null)
                        },
                        modifier = Modifier
                          .weight(1f)
                          .testTag("btn_create_normal_note"),
                        shape = RoundedCornerShape(12.dp)
                      ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Normal Note", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                      }
                      OutlinedButton(
                        onClick = { showCreateSubjectDialog = true },
                        modifier = Modifier
                          .weight(1f)
                          .testTag("btn_create_subject_folder"),
                        shape = RoundedCornerShape(12.dp)
                      ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Subject Folder", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                      }
                    }
                  }

                  item {
                    Text(
                      text = "Subject Folders (${subjectsList.size})",
                      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                  }

                  items(subjectsList, key = { "subj_$it" }) { subj ->
                    val subjColor = subjectAccentColor(subj)
                    val subjChapters = allChapters.filter { it.subject.equals(subj, ignoreCase = true) }
                    val extraSubs = customSubfolders[subj].orEmpty()
                    val noteSubNames = allNotes
                      .filter { it.subject.equals(subj, ignoreCase = true) && it.chapterName.isNotBlank() }
                      .map { it.chapterName.trim() }
                    val subfolderCount = (subjChapters.map { it.name.trim() } + extraSubs + noteSubNames)
                      .distinctBy { it.lowercase() }.size
                    val subjNotes = allNotes.filter { it.subject.equals(subj, ignoreCase = true) }
                    val attachmentCount = subjNotes.sumOf { it.getAttachments().size }

                    Card(
                      modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_folder_${subj.lowercase()}")
                        .clickable {
                          selectedSubject = subj
                          selectedChapterName = null
                          selectedChapterId = null
                        },
                      shape = RoundedCornerShape(14.dp),
                      colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                      border = BorderStroke(1.dp, subjColor.copy(alpha = 0.35f))
                    ) {
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Box(
                          modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(subjColor.copy(alpha = 0.16f)),
                          contentAlignment = Alignment.Center
                        ) {
                          Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "$subj Folder",
                            tint = subjColor,
                            modifier = Modifier.size(24.dp)
                          )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                          Text(
                            text = subj,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                          )
                          Text(
                            text = "$subfolderCount chapter subfolders • ${subjNotes.size} notes" +
                              if (attachmentCount > 0) " • $attachmentCount files" else "",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                          )
                        }
                      }
                    }
                  }

                  item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        text = "Normal Notes (No Subfolder) (${normalNotes.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                      )
                    }
                  }

                  if (normalNotes.isEmpty()) {
                    item {
                      EmptyNotesCard(
                        title = "No normal notes yet",
                        subtitle = "Tap '+ Normal Note' above for quick notes outside any subject folder."
                      )
                    }
                  } else {
                    items(normalNotes, key = { it.id }) { note ->
                      NoteListCard(
                        note = note,
                        showFolderBadge = false,
                        onOpen = { editingNote = note },
                        onDelete = { viewModel.deleteNote(note) },
                        onPreviewImage = { previewImageAttachment = it },
                        onPreviewPdf = { previewPdfAttachment = it }
                      )
                    }
                  }
                }
              } else if (selectedChapterName == null) {
                // SUBJECT VIEW: Chapter Sub-folders + Subject-level notes
                val currentSubj = selectedSubject!!
                val subjColor = subjectAccentColor(currentSubj)
                val chapterSubfolders = remember(currentSubj, allChapters, customSubfolders, allNotes) {
                  val map = linkedMapOf<String, Pair<NeetChapter?, String>>()
                  allChapters
                    .filter { it.subject.equals(currentSubj, ignoreCase = true) }
                    .forEach { ch -> map[ch.name.trim().lowercase()] = ch to ch.name }
                  customSubfolders[currentSubj].orEmpty().forEach { subName ->
                    val k = subName.trim().lowercase()
                    if (!map.containsKey(k)) map[k] = null to subName.trim()
                  }
                  allNotes
                    .filter { it.subject.equals(currentSubj, ignoreCase = true) && it.chapterName.isNotBlank() }
                    .forEach { n ->
                      val k = n.chapterName.trim().lowercase()
                      if (!map.containsKey(k)) map[k] = null to n.chapterName.trim()
                    }
                  map.values.toList()
                }
                val subjectDirectNotes = remember(currentSubj, allNotes) {
                  allNotes.filter {
                    it.subject.equals(currentSubj, ignoreCase = true) && it.chapterName.isBlank()
                  }
                }

                LazyColumn(
                  modifier = Modifier.fillMaxSize(),
                  contentPadding = PaddingValues(bottom = 88.dp),
                  verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  item {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      OutlinedButton(
                        onClick = { showCreateSubfolderDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                      ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Subfolder", fontSize = 12.sp)
                      }
                      Button(
                        onClick = {
                          editingNote = NoteEntity(
                            subject = currentSubj,
                            chapterName = "",
                            chapterId = null
                          )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                      ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Subject Note", fontSize = 12.sp)
                      }
                    }
                  }

                  item {
                    Text(
                      text = "Chapter Subfolders (${chapterSubfolders.size})",
                      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                  }

                  items(chapterSubfolders, key = { (_, name) -> "sub_${currentSubj}_$name" }) { (neetChapter, chName) ->
                    val chNotes = allNotes.filter { n ->
                      (neetChapter != null && n.chapterId == neetChapter.id) ||
                        (n.subject.equals(currentSubj, ignoreCase = true) && n.chapterName.equals(chName, ignoreCase = true))
                    }
                    val mediaCount = chNotes.sumOf { it.getAttachments().size }

                    Card(
                      modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chapter_subfolder_${chName.lowercase()}")
                        .clickable {
                          selectedChapterName = chName
                          selectedChapterId = neetChapter?.id
                        },
                      shape = RoundedCornerShape(12.dp),
                      colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                      border = BorderStroke(
                        1.dp,
                        if (chNotes.isNotEmpty()) subjColor.copy(alpha = 0.45f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                      )
                    ) {
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Box(
                          modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(subjColor.copy(alpha = 0.14f)),
                          contentAlignment = Alignment.Center
                        ) {
                          Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "$chName Subfolder",
                            tint = subjColor,
                            modifier = Modifier.size(20.dp)
                          )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                          Text(
                            text = chName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                          )
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                          ) {
                            Text(
                              text = "${chNotes.size} note${if (chNotes.size == 1) "" else "s"}" +
                                if (mediaCount > 0) " • $mediaCount files" else "",
                              fontSize = 11.sp,
                              color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (neetChapter != null) {
                              Box(
                                modifier = Modifier
                                  .clip(RoundedCornerShape(4.dp))
                                  .background(Color(0xFF10B981).copy(alpha = 0.14f))
                                  .padding(horizontal = 5.dp, vertical = 1.dp)
                              ) {
                                Text(
                                  text = "NEET Sync",
                                  fontSize = 10.sp,
                                  fontWeight = FontWeight.Bold,
                                  color = Color(0xFF10B981)
                                )
                              }
                            }
                          }
                        }
                      }
                    }
                  }

                  if (subjectDirectNotes.isNotEmpty()) {
                    item {
                      Spacer(modifier = Modifier.height(6.dp))
                      Text(
                        text = "$currentSubj General Notes (${subjectDirectNotes.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                      )
                    }
                    items(subjectDirectNotes, key = { it.id }) { note ->
                      NoteListCard(
                        note = note,
                        showFolderBadge = false,
                        onOpen = { editingNote = note },
                        onDelete = { viewModel.deleteNote(note) },
                        onPreviewImage = { previewImageAttachment = it },
                        onPreviewPdf = { previewPdfAttachment = it }
                      )
                    }
                  }
                }
              } else {
                // CHAPTER SUBFOLDER VIEW (synced with NEET chapter)
                val currentSubj = selectedSubject!!
                val currentChap = selectedChapterName!!
                val linkedChapter = remember(allChapters, selectedChapterId, currentSubj, currentChap) {
                  allChapters.find {
                    (selectedChapterId != null && it.id == selectedChapterId) ||
                      (it.subject.equals(currentSubj, ignoreCase = true) && it.name.equals(currentChap, ignoreCase = true))
                  }
                }
                val chapterNotes = remember(allNotes, linkedChapter, currentSubj, currentChap) {
                  allNotes.filter { n ->
                    (linkedChapter != null && n.chapterId == linkedChapter.id) ||
                      (n.subject.equals(currentSubj, ignoreCase = true) && n.chapterName.equals(currentChap, ignoreCase = true))
                  }
                }

                LazyColumn(
                  modifier = Modifier.fillMaxSize(),
                  contentPadding = PaddingValues(bottom = 88.dp),
                  verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  item {
                    Card(
                      shape = RoundedCornerShape(12.dp),
                      colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.10f)),
                      border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f)),
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                          Icon(
                            imageVector = if (linkedChapter != null) Icons.Default.Sync else Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                          )
                          Spacer(modifier = Modifier.width(8.dp))
                          Text(
                            text = if (linkedChapter != null) {
                              "Synced with NEET › $currentSubj › $currentChap"
                            } else {
                              "$currentSubj › $currentChap"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                          )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                          onClick = {
                            editingNote = NoteEntity(
                              title = "$currentChap Notes",
                              subject = currentSubj,
                              chapterName = currentChap,
                              chapterId = linkedChapter?.id ?: selectedChapterId
                            )
                          },
                          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                          shape = RoundedCornerShape(8.dp),
                          modifier = Modifier.testTag("btn_add_chapter_note")
                        ) {
                          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                          Spacer(modifier = Modifier.width(4.dp))
                          Text("Add Note", fontSize = 12.sp)
                        }
                      }
                    }
                  }

                  if (chapterNotes.isEmpty()) {
                    item {
                      EmptyNotesCard(
                        title = "No notes for $currentChap yet",
                        subtitle = "Tap '+ Add Note' to write numbered/bullet points, attach images, or add PDFs. Changes automatically sync with NEET and Notes Hub!"
                      )
                    }
                  } else {
                    items(chapterNotes, key = { it.id }) { note ->
                      NoteListCard(
                        note = note,
                        showFolderBadge = false,
                        expandedPreview = true,
                        onOpen = { editingNote = note },
                        onDelete = { viewModel.deleteNote(note) },
                        onPreviewImage = { previewImageAttachment = it },
                        onPreviewPdf = { previewPdfAttachment = it }
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
  }

  if (showCreateSubjectDialog) {
    var inputName by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showCreateSubjectDialog = false },
      title = { Text("New Subject Folder", fontWeight = FontWeight.Bold) },
      text = {
        OutlinedTextField(
          value = inputName,
          onValueChange = { inputName = it },
          label = { Text("Subject Name (e.g. Organic Chem)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      },
      confirmButton = {
        Button(
          onClick = {
            if (inputName.isNotBlank()) {
              viewModel.addCustomSubjectFolder(inputName.trim())
              selectedSubject = inputName.trim()
              selectedChapterName = null
              selectedChapterId = null
              showCreateSubjectDialog = false
            }
          }
        ) { Text("Create") }
      },
      dismissButton = {
        TextButton(onClick = { showCreateSubjectDialog = false }) { Text("Cancel") }
      }
    )
  }

  if (showCreateSubfolderDialog && selectedSubject != null) {
    var inputSubName by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showCreateSubfolderDialog = false },
      title = { Text("New Subfolder in ${selectedSubject}", fontWeight = FontWeight.Bold) },
      text = {
        OutlinedTextField(
          value = inputSubName,
          onValueChange = { inputSubName = it },
          label = { Text("Chapter / Topic Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      },
      confirmButton = {
        Button(
          onClick = {
            if (inputSubName.isNotBlank()) {
              val sub = inputSubName.trim()
              viewModel.addCustomChapterSubfolder(selectedSubject!!, sub)
              selectedChapterName = sub
              selectedChapterId = null
              showCreateSubfolderDialog = false
            }
          }
        ) { Text("Create") }
      },
      dismissButton = {
        TextButton(onClick = { showCreateSubfolderDialog = false }) { Text("Cancel") }
      }
    )
  }

  previewImageAttachment?.let { att ->
    ImageLightboxDialog(
      filePath = att.filePath,
      title = att.displayName,
      onDismiss = { previewImageAttachment = null }
    )
  }

  previewPdfAttachment?.let { att ->
    PdfViewerDialog(
      filePath = att.filePath,
      title = att.displayName,
      onDismiss = { previewPdfAttachment = null }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun NoteEditorScreen(
  initialNote: NoteEntity,
  allChapters: List<NeetChapter>,
  customSubjects: List<String>,
  customSubfolders: Map<String, List<String>>,
  onSave: (NoteEntity) -> Unit,
  onDelete: (NoteEntity) -> Unit,
  onCancel: () -> Unit,
  onPreviewImage: (NoteAttachment) -> Unit,
  onPreviewPdf: (NoteAttachment) -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var title by remember(initialNote.id) { mutableStateOf(initialNote.title) }
  var contentState by remember(initialNote.id) {
    mutableStateOf(
      TextFieldValue(
        text = initialNote.content,
        selection = TextRange(initialNote.content.length)
      )
    )
  }
  var subject by remember(initialNote.id) { mutableStateOf(initialNote.subject) }
  var chapterName by remember(initialNote.id) { mutableStateOf(initialNote.chapterName) }
  var chapterId by remember(initialNote.id) { mutableStateOf(initialNote.chapterId) }
  var attachments by remember(initialNote.id) { mutableStateOf(initialNote.getAttachments()) }
  var showFolderPicker by remember { mutableStateOf(false) }

  val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      scope.launch {
        val saved = withContext(Dispatchers.IO) {
          NoteMediaUtils.saveAttachmentFromUri(context, uri, isPdf = false)
        }
        if (saved != null) {
          attachments = attachments + saved
        } else {
          Toast.makeText(context, "Could not attach image", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  val pdfPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri: Uri? ->
    if (uri != null) {
      scope.launch {
        val saved = withContext(Dispatchers.IO) {
          NoteMediaUtils.saveAttachmentFromUri(context, uri, isPdf = true)
        }
        if (saved != null) {
          attachments = attachments + saved
        } else {
          Toast.makeText(context, "Could not attach PDF", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  val saveAndExit = {
    val hasContent = title.isNotBlank() || contentState.text.isNotBlank() || attachments.isNotEmpty()
    if (hasContent || initialNote.id > 0) {
      val resolvedTitle = title.trim().ifBlank {
        chapterName.takeIf { it.isNotBlank() }?.let { "$it Notes" }
          ?: contentState.text.lineSequence().firstOrNull { it.isNotBlank() }?.take(40)
          ?: "Untitled Note"
      }
      onSave(
        initialNote.copy(
          title = resolvedTitle,
          content = contentState.text,
          subject = subject.trim(),
          chapterName = chapterName.trim(),
          chapterId = chapterId,
          attachmentsJson = NoteEntity.serializeAttachments(attachments),
          updatedAt = System.currentTimeMillis()
        )
      )
    } else {
      onCancel()
    }
  }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            text = if (initialNote.id > 0) "Edit Note" else "New Note",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
        },
        navigationIcon = {
          IconButton(onClick = saveAndExit, modifier = Modifier.testTag("note_editor_back_btn")) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Save and Back")
          }
        },
        actions = {
          if (initialNote.id > 0) {
            IconButton(
              onClick = { onDelete(initialNote) },
              modifier = Modifier.testTag("note_editor_delete_btn")
            ) {
              Icon(Icons.Default.Delete, contentDescription = "Delete Note", tint = MaterialTheme.colorScheme.error)
            }
          }
          TextButton(
            onClick = saveAndExit,
            modifier = Modifier.testTag("note_editor_save_btn")
          ) {
            Text("Save", fontWeight = FontWeight.Bold)
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
      )
    },
    containerColor = Color.Transparent
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .imePadding()
        .navigationBarsPadding()
        .padding(horizontal = 16.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Folder location pill
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
          .clickable { showFolderPicker = true }
          .padding(horizontal = 12.dp, vertical = 9.dp)
          .testTag("note_folder_picker_row"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Icon(
            imageVector = if (subject.isBlank()) Icons.Default.Description else Icons.Default.Folder,
            contentDescription = null,
            tint = if (subject.isBlank()) MaterialTheme.colorScheme.primary else subjectAccentColor(subject),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = when {
              subject.isBlank() -> "Normal Notes (No Subfolder)"
              chapterName.isBlank() -> "$subject (Subject Folder)"
              else -> "$subject › $chapterName"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.DriveFileMove, contentDescription = "Move Note", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(4.dp))
          Text("Move", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
      }

      // Note Title
      OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text("Note Title") },
        placeholder = { Text("Enter title...") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("note_title_input")
      )

      // Formatting & Media Toolbar: Bullet points, Serial Numbered list, Image, PDF
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        OutlinedButton(
          onClick = { contentState = insertBulletPrefix(contentState) },
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("btn_insert_bullet")
        ) {
          Icon(Icons.Default.FormatListBulleted, contentDescription = "Bullet Point", modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Bullet", fontSize = 12.sp)
        }

        OutlinedButton(
          onClick = { contentState = insertSerialNumberPrefix(contentState) },
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("btn_insert_serial")
        ) {
          Icon(Icons.Default.FormatListNumbered, contentDescription = "Serial Number", modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("1. 2. 3.", fontSize = 12.sp)
        }

        OutlinedButton(
          onClick = {
            imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
          },
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("btn_add_note_image")
        ) {
          Icon(Icons.Default.Image, contentDescription = "Add Image", modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Add Image", fontSize = 12.sp)
        }

        OutlinedButton(
          onClick = {
            pdfPickerLauncher.launch(arrayOf("application/pdf"))
          },
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("btn_add_note_pdf")
        ) {
          Icon(Icons.Default.PictureAsPdf, contentDescription = "Add PDF", modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Add PDF", fontSize = 12.sp)
        }
      }

      // Main Note Content Editor with smart bullet / serial-number Enter continuation
      OutlinedTextField(
        value = contentState,
        onValueChange = { incoming ->
          contentState = applySmartListContinuation(contentState, incoming)
        },
        label = { Text("Notes Content") },
        placeholder = { Text("Write formulas, NCERT highlights, numbered steps, or bullet points...") },
        minLines = 9,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("note_content_input")
      )

      // Attached Images Section
      val imageAttachments = attachments.filter { it.type == NoteEntity.TYPE_IMAGE }
      if (imageAttachments.isNotEmpty()) {
        Text(
          text = "Attached Images (${imageAttachments.size})",
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          items(imageAttachments, key = { it.id }) { att ->
            Box(
              modifier = Modifier
                .size(104.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .clickable { onPreviewImage(att) }
            ) {
              AsyncImage(
                model = File(att.filePath),
                contentDescription = att.displayName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
              IconButton(
                onClick = {
                  NoteMediaUtils.deleteMediaFileIfLocal(att.filePath)
                  attachments = attachments.filterNot { it.id == att.id }
                },
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .size(28.dp)
                  .padding(2.dp)
                  .clip(CircleShape)
                  .background(Color.Black.copy(alpha = 0.65f))
              ) {
                Icon(Icons.Default.Close, contentDescription = "Remove Image", tint = Color.White, modifier = Modifier.size(14.dp))
              }
            }
          }
        }
      }

      // Attached PDFs Section
      val pdfAttachments = attachments.filter { it.type == NoteEntity.TYPE_PDF }
      if (pdfAttachments.isNotEmpty()) {
        Text(
          text = "Attached PDFs (${pdfAttachments.size})",
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          pdfAttachments.forEach { att ->
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
              border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onPreviewPdf(att) }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(att.displayName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                  Text("Tap to open PDF reader", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(
                  onClick = {
                    NoteMediaUtils.deleteMediaFileIfLocal(att.filePath)
                    attachments = attachments.filterNot { it.id == att.id }
                  }
                ) {
                  Icon(Icons.Default.Close, contentDescription = "Remove PDF", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  if (showFolderPicker) {
    FolderPickerDialog(
      currentSubject = subject,
      currentChapterName = chapterName,
      allChapters = allChapters,
      customSubjects = customSubjects,
      customSubfolders = customSubfolders,
      onSelect = { chosenSubj, chosenChapName, chosenChapId ->
        subject = chosenSubj
        chapterName = chosenChapName
        chapterId = chosenChapId
        showFolderPicker = false
      },
      onDismiss = { showFolderPicker = false }
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FolderPickerDialog(
  currentSubject: String,
  currentChapterName: String,
  allChapters: List<NeetChapter>,
  customSubjects: List<String>,
  customSubfolders: Map<String, List<String>>,
  onSelect: (subject: String, chapterName: String, chapterId: Long?) -> Unit,
  onDismiss: () -> Unit
) {
  val subjects = (NeetChapter.SUBJECTS + customSubjects).distinctBy { it.lowercase() }
  var pickedSubject by remember { mutableStateOf(currentSubject) }

  val chapterChoices = remember(pickedSubject, allChapters, customSubfolders) {
    if (pickedSubject.isBlank()) {
      emptyList()
    } else {
      val map = linkedMapOf<String, Pair<Long?, String>>()
      allChapters
        .filter { it.subject.equals(pickedSubject, ignoreCase = true) }
        .forEach { ch -> map[ch.name.lowercase()] = ch.id to ch.name }
      customSubfolders[pickedSubject].orEmpty().forEach { sub ->
        if (!map.containsKey(sub.lowercase())) map[sub.lowercase()] = null to sub
      }
      map.values.toList()
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Choose Folder", fontWeight = FontWeight.Bold) },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        FilterChip(
          selected = pickedSubject.isBlank(),
          onClick = { onSelect("", "", null) },
          label = { Text("Normal Notes (No Folder)") }
        )

        Text("Subject Folder", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          subjects.forEach { s ->
            FilterChip(
              selected = pickedSubject.equals(s, ignoreCase = true),
              onClick = { pickedSubject = s },
              label = { Text(s, fontSize = 12.sp) }
            )
          }
        }

        if (pickedSubject.isNotBlank()) {
          OutlinedButton(
            onClick = { onSelect(pickedSubject, "", null) },
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Save directly inside $pickedSubject (no chapter subfolder)", fontSize = 12.sp)
          }
          if (chapterChoices.isNotEmpty()) {
            Text("Or choose Chapter Subfolder:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            chapterChoices.forEach { (chId, chName) ->
              val isCurr = pickedSubject.equals(currentSubject, ignoreCase = true) &&
                chName.equals(currentChapterName, ignoreCase = true)
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(
                    if (isCurr) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                  )
                  .clickable { onSelect(pickedSubject, chName, chId) }
                  .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(chName, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text("Close") }
    }
  )
}

@Composable
private fun NoteListCard(
  note: NoteEntity,
  showFolderBadge: Boolean,
  expandedPreview: Boolean = false,
  onOpen: () -> Unit,
  onDelete: () -> Unit,
  onPreviewImage: (NoteAttachment) -> Unit,
  onPreviewPdf: (NoteAttachment) -> Unit
) {
  val attachments = remember(note.attachmentsJson) { note.getAttachments() }
  val images = attachments.filter { it.type == NoteEntity.TYPE_IMAGE }
  val pdfs = attachments.filter { it.type == NoteEntity.TYPE_PDF }
  val dateLabel = remember(note.updatedAt) {
    SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(note.updatedAt))
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("note_card_${note.id}")
      .clickable { onOpen() },
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = note.title.ifBlank { "Untitled Note" },
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          if (showFolderBadge) {
            val badge = when {
              note.subject.isBlank() -> "Normal Note"
              note.chapterName.isBlank() -> note.subject
              else -> "${note.subject} › ${note.chapterName}"
            }
            Text(
              text = badge,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (note.subject.isBlank()) MaterialTheme.colorScheme.primary else subjectAccentColor(note.subject)
            )
          }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(dateLabel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Note", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
          }
        }
      }

      if (note.content.isNotBlank()) {
        Text(
          text = note.content,
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = if (expandedPreview) 8 else 3,
          overflow = TextOverflow.Ellipsis
        )
      }

      if (images.isNotEmpty()) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(images, key = { it.id }) { img ->
            AsyncImage(
              model = File(img.filePath),
              contentDescription = img.displayName,
              modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                .clickable { onPreviewImage(img) },
              contentScale = ContentScale.Crop
            )
          }
        }
      }

      if (pdfs.isNotEmpty()) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(pdfs, key = { it.id }) { pdf ->
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .clickable { onPreviewPdf(pdf) }
                .padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(pdf.displayName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun EmptyNotesCard(title: String, subtitle: String) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
      Spacer(modifier = Modifier.height(8.dp))
      Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

private fun subjectAccentColor(subject: String): Color {
  return when (subject.trim().lowercase()) {
    "botany" -> Color(0xFF059669)
    "zoology" -> Color(0xFFD97706)
    "physics" -> Color(0xFF2563EB)
    "chemistry" -> Color(0xFF7C3AED)
    else -> Color(0xFF0EA5E9)
  }
}

internal fun insertBulletPrefix(state: TextFieldValue): TextFieldValue {
  val text = state.text
  val cursor = state.selection.start.coerceIn(0, text.length)
  val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
  val prefix = "• "
  val updated = if (cursor == lineStart || text.substring(lineStart, cursor).isBlank()) {
    text.substring(0, lineStart) + prefix + text.substring(lineStart)
  } else {
    text.substring(0, cursor) + "\n" + prefix + text.substring(cursor)
  }
  val newCursor = if (cursor == lineStart || text.substring(lineStart, cursor).isBlank()) {
    lineStart + prefix.length
  } else {
    cursor + 1 + prefix.length
  }
  return TextFieldValue(text = updated, selection = TextRange(newCursor))
}

internal fun insertSerialNumberPrefix(state: TextFieldValue): TextFieldValue {
  val text = state.text
  val cursor = state.selection.start.coerceIn(0, text.length)
  val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
  val beforeLine = text.substring(0, lineStart).trimEnd('\n')
  val prevLine = beforeLine.substringAfterLast('\n')
  val prevNumMatch = Regex("^\\s*(\\d+)\\.\\s+.*").find(prevLine)
  val nextNum = (prevNumMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0) + 1
  val currentLineBeforeCursor = text.substring(lineStart, cursor)

  return if (cursor == lineStart || currentLineBeforeCursor.isBlank()) {
    val prefix = "$nextNum. "
    val updated = text.substring(0, lineStart) + prefix + text.substring(lineStart)
    TextFieldValue(text = updated, selection = TextRange(lineStart + prefix.length))
  } else {
    val currNumMatch = Regex("^\\s*(\\d+)\\.\\s+.*").find(currentLineBeforeCursor)
    val nextAfterCurr = (currNumMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0) + 1
    val prefix = "\n$nextAfterCurr. "
    val updated = text.substring(0, cursor) + prefix + text.substring(cursor)
    TextFieldValue(text = updated, selection = TextRange(cursor + prefix.length))
  }
}

internal fun applySmartListContinuation(old: TextFieldValue, incoming: TextFieldValue): TextFieldValue {
  val oldText = old.text
  val newText = incoming.text
  if (newText.length != oldText.length + 1) return incoming
  val cursor = incoming.selection.start
  if (cursor <= 0 || cursor > newText.length || newText[cursor - 1] != '\n') return incoming

  val prevLineEnd = cursor - 1
  val prevLineStart = newText.lastIndexOf('\n', (prevLineEnd - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
  val prevLine = newText.substring(prevLineStart, prevLineEnd)

  if (prevLine == "• ") {
    val stripped = newText.removeRange(prevLineStart, cursor)
    return TextFieldValue(text = stripped, selection = TextRange(prevLineStart))
  }
  if (prevLine.startsWith("• ")) {
    val insert = "• "
    val updated = newText.substring(0, cursor) + insert + newText.substring(cursor)
    return TextFieldValue(text = updated, selection = TextRange(cursor + insert.length))
  }

  val emptyNum = Regex("^(\\d+)\\. $").find(prevLine)
  if (emptyNum != null) {
    val stripped = newText.removeRange(prevLineStart, cursor)
    return TextFieldValue(text = stripped, selection = TextRange(prevLineStart))
  }

  val numMatch = Regex("^(\\d+)\\.\\s+.+").find(prevLine)
  if (numMatch != null) {
    val nextNum = (numMatch.groupValues[1].toIntOrNull() ?: 0) + 1
    val insert = "$nextNum. "
    val updated = newText.substring(0, cursor) + insert + newText.substring(cursor)
    return TextFieldValue(text = updated, selection = TextRange(cursor + insert.length))
  }

  return incoming
}
