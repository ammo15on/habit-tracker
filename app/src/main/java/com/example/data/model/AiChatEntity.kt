package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_chat_history")
data class AiChatEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val role: String, // "user" or "model"
  val text: String,
  val timestamp: Long = System.currentTimeMillis(),
  val isError: Boolean = false,
  val isCloud: Boolean = false
)
