package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "neet_test_scores")
data class NeetTestScore(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val testName: String,
  val date: String, // Format: YYYY-MM-DD
  val physicsScore: Int = 0,
  val chemistryScore: Int = 0,
  val botanyScore: Int = 0,
  val zoologyScore: Int = 0,
  val maxPhysics: Int = 180,
  val maxChemistry: Int = 180,
  val maxBotany: Int = 180,
  val maxZoology: Int = 180,
  val timestamp: Long = System.currentTimeMillis()
) {
  val totalScore: Int
    get() = physicsScore + chemistryScore + botanyScore + zoologyScore

  val maxTotal: Int
    get() = maxPhysics + maxChemistry + maxBotany + maxZoology
}
