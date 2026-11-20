package com.example.ui.components

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.util.NoteMediaUtils
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageLightboxDialog(
  filePath: String,
  title: String,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var scale by remember { mutableFloatStateOf(1f) }
  var offsetX by remember { mutableFloatStateOf(0f) }
  var offsetY by remember { mutableFloatStateOf(0f) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
  ) {
    Scaffold(
      topBar = {
        CenterAlignedTopAppBar(
          title = {
            Text(
              text = title.ifBlank { "Image Preview" },
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold
            )
          },
          navigationIcon = {
            IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_image_preview_btn")) {
              Icon(Icons.Default.Close, contentDescription = "Close Image", tint = Color.White)
            }
          },
          actions = {
            IconButton(
              onClick = {
                val file = File(filePath)
                if (file.exists()) {
                  NoteMediaUtils.getContentUriForFile(context, file)?.let { uri ->
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                      setDataAndType(uri, "image/*")
                      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try {
                      context.startActivity(Intent.createChooser(intent, "Open Image"))
                    } catch (_: Exception) {}
                  }
                }
              }
            ) {
              Icon(Icons.Default.OpenInNew, contentDescription = "Open External", tint = Color.White)
            }
          },
          colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xCC090B10))
        )
      },
      containerColor = Color.Black
    ) { padding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
              scale = (scale * zoom).coerceIn(1f, 5f)
              if (scale > 1f) {
                offsetX += pan.x
                offsetY += pan.y
              } else {
                offsetX = 0f
                offsetY = 0f
              }
            }
          },
        contentAlignment = Alignment.Center
      ) {
        AsyncImage(
          model = File(filePath),
          contentDescription = title,
          modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
              scaleX = scale,
              scaleY = scale,
              translationX = offsetX,
              translationY = offsetY
            ),
          contentScale = ContentScale.Fit
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerDialog(
  filePath: String,
  title: String,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var renderedPages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
  var isLoading by remember { mutableStateOf(true) }
  var errorText by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(filePath) {
    isLoading = true
    errorText = null
    val pages = mutableListOf<Bitmap>()
    withContext(Dispatchers.IO) {
      try {
        val file = File(filePath)
        if (!file.exists()) {
          errorText = "PDF file could not be found."
          return@withContext
        }
        val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        renderer.use { pdf ->
          val count = pdf.pageCount.coerceAtMost(50)
          for (i in 0 until count) {
            pdf.openPage(i).use { page ->
              val targetWidth = 1080
              val aspect = page.height.toFloat() / page.width.coerceAtLeast(1)
              val targetHeight = (targetWidth * aspect).toInt().coerceAtLeast(1)
              val bmp = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
              bmp.eraseColor(android.graphics.Color.WHITE)
              page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
              pages.add(bmp)
            }
          }
        }
        pfd.close()
      } catch (e: Exception) {
        errorText = e.message ?: "Unable to render PDF."
      }
    }
    renderedPages = pages
    isLoading = false
  }

  DisposableEffect(Unit) {
    onDispose {
      renderedPages.forEach { if (!it.isRecycled) it.recycle() }
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
  ) {
    Scaffold(
      topBar = {
        CenterAlignedTopAppBar(
          title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = title.ifBlank { "PDF Document" },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
              if (renderedPages.isNotEmpty()) {
                Text(
                  text = "${renderedPages.size} page${if (renderedPages.size == 1) "" else "s"}",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          },
          navigationIcon = {
            IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_pdf_viewer_btn")) {
              Icon(Icons.Default.Close, contentDescription = "Close PDF")
            }
          },
          actions = {
            IconButton(
              onClick = {
                val file = File(filePath)
                if (file.exists()) {
                  NoteMediaUtils.getContentUriForFile(context, file)?.let { uri ->
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                      setDataAndType(uri, "application/pdf")
                      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try {
                      context.startActivity(Intent.createChooser(intent, "Open PDF With"))
                    } catch (_: Exception) {}
                  }
                }
              }
            ) {
              Icon(Icons.Default.OpenInNew, contentDescription = "Open PDF Externally")
            }
          }
        )
      }
    ) { padding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
      ) {
        when {
          isLoading -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              CircularProgressIndicator()
              Spacer(modifier = Modifier.height(10.dp))
              Text("Rendering PDF...", color = Color.White, fontSize = 13.sp)
            }
          }
          errorText != null -> {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(24.dp)
            ) {
              Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(48.dp))
              Spacer(modifier = Modifier.height(12.dp))
              Text(errorText ?: "Error opening PDF", color = Color.White, fontWeight = FontWeight.Medium)
            }
          }
          renderedPages.isEmpty() -> {
            Text("PDF has no pages.", color = Color.White)
          }
          else -> {
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(12.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              itemsIndexed(renderedPages) { idx, bitmap ->
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = Color.White,
                  shadowElevation = 4.dp,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column {
                    Image(
                      bitmap = bitmap.asImageBitmap(),
                      contentDescription = "Page ${idx + 1}",
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                      contentScale = ContentScale.FillWidth
                    )
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                      horizontalArrangement = Arrangement.End
                    ) {
                      Text(
                        text = "Page ${idx + 1} / ${renderedPages.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569)
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
}
