package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "neet_chapters")
data class NeetChapter(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val name: String,
  val subject: String, // Botany, Zoology, Physics, Chemistry
  val isCompleted: Boolean = false,
  val isPyqDone: Boolean = false,
  val isRevisionDone: Boolean = false,
  val notes: String = ""
) {
  companion object {
    val SUBJECTS = listOf("Botany", "Zoology", "Physics", "Chemistry")
    val DEFAULT_CHAPTERS = listOf(
      // Botany
      NeetChapter(name = "The Living World", subject = "Botany"),
      NeetChapter(name = "Biological Classification", subject = "Botany"),
      NeetChapter(name = "Plant Kingdom", subject = "Botany"),
      NeetChapter(name = "Morphology of Flowering Plants", subject = "Botany"),
      NeetChapter(name = "Anatomy of Flowering Plants", subject = "Botany"),
      NeetChapter(name = "Cell: The Unit of Life", subject = "Botany"),
      NeetChapter(name = "Cell Cycle and Cell Division", subject = "Botany"),
      NeetChapter(name = "Photosynthesis in Higher Plants", subject = "Botany"),
      NeetChapter(name = "Respiration in Plants", subject = "Botany"),
      NeetChapter(name = "Plant Growth and Development", subject = "Botany"),
      NeetChapter(name = "Sexual Reproduction in Flowering Plants", subject = "Botany"),
      NeetChapter(name = "Principles of Inheritance and Variation", subject = "Botany"),
      NeetChapter(name = "Molecular Basis of Inheritance", subject = "Botany"),
      NeetChapter(name = "Microbes in Human Welfare", subject = "Botany"),
      NeetChapter(name = "Organisms and Populations", subject = "Botany"),
      NeetChapter(name = "Ecosystem", subject = "Botany"),
      NeetChapter(name = "Biodiversity and Conservation", subject = "Botany"),

      // Zoology
      NeetChapter(name = "Animal Kingdom", subject = "Zoology"),
      NeetChapter(name = "Structural Organisation in Animals", subject = "Zoology"),
      NeetChapter(name = "Biomolecules", subject = "Zoology"),
      NeetChapter(name = "Breathing and Exchange of Gases", subject = "Zoology"),
      NeetChapter(name = "Body Fluids and Circulation", subject = "Zoology"),
      NeetChapter(name = "Excretory Products and their Elimination", subject = "Zoology"),
      NeetChapter(name = "Locomotion and Movement", subject = "Zoology"),
      NeetChapter(name = "Neural Control and Coordination", subject = "Zoology"),
      NeetChapter(name = "Chemical Coordination and Integration", subject = "Zoology"),
      NeetChapter(name = "Human Reproduction", subject = "Zoology"),
      NeetChapter(name = "Reproductive Health", subject = "Zoology"),
      NeetChapter(name = "Evolution", subject = "Zoology"),
      NeetChapter(name = "Human Health and Disease", subject = "Zoology"),
      NeetChapter(name = "Biotechnology: Principles and Processes", subject = "Zoology"),
      NeetChapter(name = "Biotechnology and its Applications", subject = "Zoology"),

      // Physics
      NeetChapter(name = "Units and Measurements", subject = "Physics"),
      NeetChapter(name = "Motion in a Straight Line", subject = "Physics"),
      NeetChapter(name = "Motion in a Plane", subject = "Physics"),
      NeetChapter(name = "Laws of Motion", subject = "Physics"),
      NeetChapter(name = "Work, Energy and Power", subject = "Physics"),
      NeetChapter(name = "System of Particles and Rotational Motion", subject = "Physics"),
      NeetChapter(name = "Gravitation", subject = "Physics"),
      NeetChapter(name = "Mechanical Properties of Solids", subject = "Physics"),
      NeetChapter(name = "Mechanical Properties of Fluids", subject = "Physics"),
      NeetChapter(name = "Thermal Properties of Matter", subject = "Physics"),
      NeetChapter(name = "Thermodynamics", subject = "Physics"),
      NeetChapter(name = "Kinetic Theory", subject = "Physics"),
      NeetChapter(name = "Oscillations", subject = "Physics"),
      NeetChapter(name = "Waves", subject = "Physics"),
      NeetChapter(name = "Electric Charges and Fields", subject = "Physics"),
      NeetChapter(name = "Electrostatic Potential and Capacitance", subject = "Physics"),
      NeetChapter(name = "Current Electricity", subject = "Physics"),
      NeetChapter(name = "Moving Charges and Magnetism", subject = "Physics"),
      NeetChapter(name = "Magnetism and Matter", subject = "Physics"),
      NeetChapter(name = "Electromagnetic Induction", subject = "Physics"),
      NeetChapter(name = "Alternating Current", subject = "Physics"),
      NeetChapter(name = "Electromagnetic Waves", subject = "Physics"),
      NeetChapter(name = "Ray Optics and Optical Instruments", subject = "Physics"),
      NeetChapter(name = "Wave Optics", subject = "Physics"),
      NeetChapter(name = "Dual Nature of Radiation and Matter", subject = "Physics"),
      NeetChapter(name = "Atoms", subject = "Physics"),
      NeetChapter(name = "Nuclei", subject = "Physics"),
      NeetChapter(name = "Semiconductor Electronics", subject = "Physics"),

      // Chemistry
      NeetChapter(name = "Some Basic Concepts of Chemistry", subject = "Chemistry"),
      NeetChapter(name = "Structure of Atom", subject = "Chemistry"),
      NeetChapter(name = "Classification of Elements and Periodicity", subject = "Chemistry"),
      NeetChapter(name = "Chemical Bonding and Molecular Structure", subject = "Chemistry"),
      NeetChapter(name = "Chemical Thermodynamics", subject = "Chemistry"),
      NeetChapter(name = "Equilibrium", subject = "Chemistry"),
      NeetChapter(name = "Redox Reactions", subject = "Chemistry"),
      NeetChapter(name = "p-Block Elements", subject = "Chemistry"),
      NeetChapter(name = "Organic Chemistry: Some Basic Principles", subject = "Chemistry"),
      NeetChapter(name = "Hydrocarbons", subject = "Chemistry"),
      NeetChapter(name = "Solutions", subject = "Chemistry"),
      NeetChapter(name = "Electrochemistry", subject = "Chemistry"),
      NeetChapter(name = "Chemical Kinetics", subject = "Chemistry"),
      NeetChapter(name = "d and f Block Elements", subject = "Chemistry"),
      NeetChapter(name = "Coordination Compounds", subject = "Chemistry"),
      NeetChapter(name = "Haloalkanes and Haloarenes", subject = "Chemistry"),
      NeetChapter(name = "Alcohols, Phenols and Ethers", subject = "Chemistry"),
      NeetChapter(name = "Aldehydes, Ketones and Carboxylic Acids", subject = "Chemistry"),
      NeetChapter(name = "Amines", subject = "Chemistry"),
      NeetChapter(name = "Biomolecules (Chemistry)", subject = "Chemistry")
    )
  }
}
