package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageStorageUtils {
  fun saveImageToInternalStorage(context: Context, imageUri: Uri): String? {
    return copyUriToInternalStorage(context, imageUri, "img")
  }

  fun copyUriToInternalStorage(context: Context, uri: Uri, prefix: String = "img"): String? {
    return try {
      val contentResolver = context.contentResolver
      val inputStream: InputStream? = contentResolver.openInputStream(uri)
      if (inputStream != null) {
        val fileName = "${prefix}_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream.use { input ->
          outputStream.use { output ->
            input.copyTo(output)
          }
        }
        file.absolutePath
      } else {
        null
      }
    } catch (_: Exception) {
      null
    }
  }
}
