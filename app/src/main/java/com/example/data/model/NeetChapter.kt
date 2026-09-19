package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "neet_chapters")
data class NeetChapter(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val name: String,
  val subject: String, // "Botany", "Zoology", "Physics", "Chemistry"
  val isCompleted: Boolean = false, // Chapter completed (true) vs To Complete (false)
  val isPyqDone: Boolean = false, // PYQ done tick mark
  val isRevisionDone: Boolean = false, // Notes / Revision read tick mark
  val notes: String = "",
  val orderIndex: Int = 0
) {
  companion object {
    val SUBJECTS = listOf("Botany", "Zoology", "Physics", "Chemistry")

    val DEFAULT_CHAPTERS = listOf(
      // Botany
      NeetChapter(name = "Cell: The Unit of Life", subject = "Botany", isCompleted = true, isPyqDone = true),
      NeetChapter(name = "Cell Cycle & Cell Division", subject = "Botany", isCompleted = true, isPyqDone = false),
      NeetChapter(name = "Photosynthesis in Higher Plants", subject = "Botany", isCompleted = false, isPyqDone = false),
      NeetChapter(name = "Plant Growth & Development", subject = "Botany", isCompleted = false, isPyqDone = false),
      NeetChapter(name = "Principles of Inheritance & Variation", subject = "Botany", isCompleted = false, isPyqDone = false),
      NeetChapter(name = "Molecular Basis of Inheritance", subject = "Botany", isCompleted = false, isPyqDone = false),

      // Zoology
      NeetChapter(name = "Human Reproduction", subject = "Zoology", isCompleted = true, isPyqDone = true),
      NeetChapter(name = "Reproductive Health", subject = "Zoology", isCompleted = true, isPyqDone = false),
      NeetChapter(name = "Evolution", subject = "Zoology", isCompleted = false, isPyqDone = false),
      NeetChapter(name = "Human Health & Disease", subject = "Zoology", isCompleted = false, isPyqDone = false),
      NeetChapter(name = "Biotechnology: Principles & Processes", subject = "Zoology", isCompleted = false, isPyqDone = false),
      NeetChapter(name = "Breathing & Exchange of Gases", subject = "Zoology", isCompleted = false, isPyqDone = false),

      // Physics
      NeetChapter(name = "Units & Measurements", subject = "Physics", isCompleted = true, isPyqDone = true),
      NeetChapter(name = "Motion in a Straight Line", subject = "Physics", isCompleted = true, isPyqDone = false),
      NeetChapter(name = "Laws of Motion", subject = "Physics", isCompleted = false, isPyqDone = false),
      NeetChapter(name = "Work, Energy & Power", subject = "Physics", isCompleted = false, isPyqDone = false),
      NeetChapter(name = "Thermodynamics", subject = "Physics", isCompleted = false, isPyqDone = false),
      NeetChapter(name = "Current Electricity", subject = "Physics", isCompleted = false, isPyqDone = false),
      NeetChapter(name = "Ray Optics & Optical Instruments", subject = "Physics", isCompleted = false, isPyqDone = false),

      // Chemistry
      NeetChapter(name = "Some Basic Concepts of Chemistry (Mole)", subject = "Chemistry", isCompleted = true, isPyqDone = true),
      NeetChapter(name = "Structure of Atom", subject = "Chemistry", isCompleted = true, isPyqDone = false),
      NeetChapter(name = "Chemical Bonding & Molecular Structure", subject = "Chemistry", isCompleted = false, isPyqDone = false),
      NeetChapter(name = "Chemical Thermodynamics", subject = "Chemistry", isCompleted = false, isPyqDone = false),
      NeetChapter(name = "Equilibrium", subject = "Chemistry", isCompleted = false, isPyqDone = false),
      NeetChapter(name = "Organic Chemistry: Basic Principles", subject = "Chemistry", isCompleted = false, isPyqDone = false),
      NeetChapter(name = "Hydrocarbons", subject = "Chemistry", isCompleted = false, isPyqDone = false)
    )
  }
}
