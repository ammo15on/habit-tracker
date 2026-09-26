package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_chat_messages")
data class AiChatMessage(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val role: String, // "user" or "assistant"
  val text: String,
  val mode: String = "on_device", // "on_device" or "cloud"
  val timestamp: Long = System.currentTimeMillis()
)
