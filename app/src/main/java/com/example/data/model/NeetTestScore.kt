package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "neet_test_scores")
data class NeetTestScore(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val testName: String,
  val date: String, // yyyy-MM-dd
  val physicsScore: Int,
  val chemistryScore: Int,
  val botanyScore: Int,
  val zoologyScore: Int,
  val maxPhysics: Int = 180,
  val maxChemistry: Int = 180,
  val maxBotany: Int = 180,
  val maxZoology: Int = 180,
  val timestamp: Long = System.currentTimeMillis()
) {
  val totalScore: Int
    get() = physicsScore + chemistryScore + botanyScore + zoologyScore

  val maxTotalScore: Int
    get() = maxPhysics + maxChemistry + maxBotany + maxZoology

  val percentage: Float
    get() = if (maxTotalScore > 0) (totalScore.toFloat() / maxTotalScore) * 100f else 0f
}
