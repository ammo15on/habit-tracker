package com.example.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.example.data.model.NoteAttachment
import com.example.data.model.NoteEntity
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object NoteMediaUtils {
  private const val MEDIA_DIR_NAME = "notes_media"

  fun getMediaDir(context: Context): File {
    val dir = File(context.filesDir, MEDIA_DIR_NAME)
    if (!dir.exists()) {
      dir.mkdirs()
    }
    return dir
  }

  fun saveAttachmentFromUri(
    context: Context,
    uri: Uri,
    isPdf: Boolean
  ): NoteAttachment? {
    return try {
      val contentResolver = context.contentResolver
      val rawName = resolveDisplayName(context, uri)
      val mimeType = contentResolver.getType(uri)
        ?: if (isPdf) "application/pdf" else "image/jpeg"
      val ext = when {
        isPdf || mimeType.contains("pdf", ignoreCase = true) || rawName.endsWith(".pdf", ignoreCase = true) -> "pdf"
        mimeType.contains("png", ignoreCase = true) || rawName.endsWith(".png", ignoreCase = true) -> "png"
        mimeType.contains("webp", ignoreCase = true) || rawName.endsWith(".webp", ignoreCase = true) -> "webp"
        else -> "jpg"
      }
      val type = if (ext == "pdf") NoteEntity.TYPE_PDF else NoteEntity.TYPE_IMAGE
      val prefix = if (type == NoteEntity.TYPE_PDF) "pdf" else "img"
      val uniqueFileName = "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.$ext"
      val destFile = File(getMediaDir(context), uniqueFileName)

      contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(destFile).use { output ->
          input.copyTo(output)
        }
      } ?: return null

      if (!destFile.exists() || destFile.length() == 0L) {
        destFile.delete()
        return null
      }

      val cleanDisplayName = rawName.ifBlank {
        if (type == NoteEntity.TYPE_PDF) "Document.pdf" else "Image.$ext"
      }

      NoteAttachment(
        id = UUID.randomUUID().toString(),
        type = type,
        filePath = destFile.absolutePath,
        displayName = cleanDisplayName,
        mimeType = mimeType
      )
    } catch (_: Exception) {
      null
    }
  }

  fun resolveDisplayName(context: Context, uri: Uri): String {
    try {
      context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
          val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          if (idx >= 0) {
            val name = cursor.getString(idx)
            if (!name.isNullOrBlank()) return name
          }
        }
      }
    } catch (_: Exception) {}
    return uri.lastPathSegment?.substringAfterLast('/') ?: ""
  }

  fun getContentUriForFile(context: Context, file: File): Uri? {
    return try {
      FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } catch (_: Exception) {
      null
    }
  }

  fun deleteMediaFileIfLocal(filePath: String) {
    try {
      if (filePath.isBlank()) return
      val f = File(filePath)
      if (f.exists() && f.absolutePath.contains(MEDIA_DIR_NAME)) {
        f.delete()
      }
    } catch (_: Exception) {}
  }
}
