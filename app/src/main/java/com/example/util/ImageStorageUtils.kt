package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageStorageUtils {
  fun copyUriToInternalStorage(context: Context, uri: Uri, prefix: String = "task_img"): String? {
    return try {
      val imagesDir = File(context.filesDir, "task_media").apply { mkdirs() }
      val targetFile = File(imagesDir, "${prefix}_${System.currentTimeMillis()}.jpg")
      context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(targetFile).use { output ->
          input.copyTo(output)
        }
      }
      targetFile.absolutePath
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }
}
