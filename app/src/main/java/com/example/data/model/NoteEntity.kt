package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class NoteAttachment(
  val id: String,
  val type: String, // "IMAGE" or "PDF"
  val filePath: String,
  val displayName: String,
  val mimeType: String = if (type == "PDF") "application/pdf" else "image/jpeg"
)

@Entity(tableName = "notes")
data class NoteEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String = "",
  val content: String = "",
  val subject: String = "", // "" = Normal note (no folder), otherwise Subject folder name
  val chapterName: String = "", // "" = Subject-level or Normal note, otherwise Chapter subfolder
  val chapterId: Long? = null, // Links to NeetChapter.id for two-way sync
  val attachmentsJson: String = "[]",
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
) {
  fun getAttachments(): List<NoteAttachment> {
    if (attachmentsJson.isBlank() || attachmentsJson == "[]") return emptyList()
    return try {
      jsonFormat.decodeFromString<List<NoteAttachment>>(attachmentsJson)
    } catch (_: Exception) {
      emptyList()
    }
  }

  fun withAttachments(attachments: List<NoteAttachment>): NoteEntity {
    return copy(attachmentsJson = serializeAttachments(attachments))
  }

  companion object {
    const val TYPE_IMAGE = "IMAGE"
    const val TYPE_PDF = "PDF"

    private val jsonFormat = Json { ignoreUnknownKeys = true }

    fun serializeAttachments(list: List<NoteAttachment>): String {
      return try {
        jsonFormat.encodeToString(list)
      } catch (_: Exception) {
        "[]"
      }
    }

    fun parseAttachments(raw: String): List<NoteAttachment> {
      if (raw.isBlank() || raw == "[]") return emptyList()
      return try {
        jsonFormat.decodeFromString<List<NoteAttachment>>(raw)
      } catch (_: Exception) {
        emptyList()
      }
    }
  }
}
