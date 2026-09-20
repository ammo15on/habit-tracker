package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "neet_chapters")
data class NeetChapter(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val name: String,
  val subject: String, // "Botany", "Zoology", "Physics", "Chemistry"
  val classLevel: Int = 11, // 11 or 12
  val isCompleted: Boolean = false, // Chapter completed (true) vs To Complete (false)
  val isPyqDone: Boolean = false, // PYQ done tick mark
  val isRevisionDone: Boolean = false, // Notes / Revision read tick mark
  val notes: String = "",
  val orderIndex: Int = 0
) {
  companion object {
    val SUBJECTS = listOf("Botany", "Zoology", "Physics", "Chemistry")

    val DEFAULT_CHAPTERS = listOf(
      // ==================== BOTANY CLASS 11 ====================
      NeetChapter(name = "The Living World", subject = "Botany", classLevel = 11),
      NeetChapter(name = "Biological Classification", subject = "Botany", classLevel = 11),
      NeetChapter(name = "Plant Kingdom", subject = "Botany", classLevel = 11),
      NeetChapter(name = "Morphology of Flowering Plants", subject = "Botany", classLevel = 11),
      NeetChapter(name = "Anatomy of Flowering Plants", subject = "Botany", classLevel = 11),
      NeetChapter(name = "Cell: The Unit of Life", subject = "Botany", classLevel = 11, isCompleted = true, isPyqDone = true),
      NeetChapter(name = "Cell Cycle & Cell Division", subject = "Botany", classLevel = 11, isCompleted = true, isPyqDone = true),
      NeetChapter(name = "Photosynthesis in Higher Plants", subject = "Botany", classLevel = 11),
      NeetChapter(name = "Respiration in Plants", subject = "Botany", classLevel = 11),
      NeetChapter(name = "Plant Growth & Development", subject = "Botany", classLevel = 11),

      // ==================== BOTANY CLASS 12 ====================
      NeetChapter(name = "Sexual Reproduction in Flowering Plants", subject = "Botany", classLevel = 12),
      NeetChapter(name = "Principles of Inheritance & Variation", subject = "Botany", classLevel = 12),
      NeetChapter(name = "Molecular Basis of Inheritance", subject = "Botany", classLevel = 12),
      NeetChapter(name = "Microbes in Human Welfare", subject = "Botany", classLevel = 12),
      NeetChapter(name = "Organisms & Populations", subject = "Botany", classLevel = 12),
      NeetChapter(name = "Ecosystem", subject = "Botany", classLevel = 12),
      NeetChapter(name = "Biodiversity & Conservation", subject = "Botany", classLevel = 12),

      // ==================== ZOOLOGY CLASS 11 ====================
      NeetChapter(name = "Animal Kingdom", subject = "Zoology", classLevel = 11),
      NeetChapter(name = "Structural Organisation in Animals", subject = "Zoology", classLevel = 11),
      NeetChapter(name = "Biomolecules", subject = "Zoology", classLevel = 11),
      NeetChapter(name = "Breathing & Exchange of Gases", subject = "Zoology", classLevel = 11),
      NeetChapter(name = "Body Fluids & Circulation", subject = "Zoology", classLevel = 11),
      NeetChapter(name = "Excretory Products & their Elimination", subject = "Zoology", classLevel = 11),
      NeetChapter(name = "Locomotion & Movement", subject = "Zoology", classLevel = 11),
      NeetChapter(name = "Neural Control & Coordination", subject = "Zoology", classLevel = 11),
      NeetChapter(name = "Chemical Coordination & Integration", subject = "Zoology", classLevel = 11),

      // ==================== ZOOLOGY CLASS 12 ====================
      NeetChapter(name = "Human Reproduction", subject = "Zoology", classLevel = 12, isCompleted = true, isPyqDone = true),
      NeetChapter(name = "Reproductive Health", subject = "Zoology", classLevel = 12, isCompleted = true),
      NeetChapter(name = "Evolution", subject = "Zoology", classLevel = 12),
      NeetChapter(name = "Human Health & Disease", subject = "Zoology", classLevel = 12),
      NeetChapter(name = "Biotechnology: Principles & Processes", subject = "Zoology", classLevel = 12),
      NeetChapter(name = "Biotechnology & its Applications", subject = "Zoology", classLevel = 12),

      // ==================== PHYSICS CLASS 11 ====================
      NeetChapter(name = "Units & Measurements", subject = "Physics", classLevel = 11, isCompleted = true, isPyqDone = true),
      NeetChapter(name = "Motion in a Straight Line", subject = "Physics", classLevel = 11, isCompleted = true),
      NeetChapter(name = "Motion in a Plane", subject = "Physics", classLevel = 11),
      NeetChapter(name = "Laws of Motion", subject = "Physics", classLevel = 11),
      NeetChapter(name = "Work, Energy & Power", subject = "Physics", classLevel = 11),
      NeetChapter(name = "System of Particles & Rotational Motion", subject = "Physics", classLevel = 11),
      NeetChapter(name = "Gravitation", subject = "Physics", classLevel = 11),
      NeetChapter(name = "Mechanical Properties of Solids", subject = "Physics", classLevel = 11),
      NeetChapter(name = "Mechanical Properties of Fluids", subject = "Physics", classLevel = 11),
      NeetChapter(name = "Thermal Properties of Matter", subject = "Physics", classLevel = 11),
      NeetChapter(name = "Thermodynamics", subject = "Physics", classLevel = 11),
      NeetChapter(name = "Kinetic Theory", subject = "Physics", classLevel = 11),
      NeetChapter(name = "Oscillations & Waves", subject = "Physics", classLevel = 11),

      // ==================== PHYSICS CLASS 12 ====================
      NeetChapter(name = "Electric Charges & Fields", subject = "Physics", classLevel = 12),
      NeetChapter(name = "Electrostatic Potential & Capacitance", subject = "Physics", classLevel = 12),
      NeetChapter(name = "Current Electricity", subject = "Physics", classLevel = 12),
      NeetChapter(name = "Moving Charges & Magnetism", subject = "Physics", classLevel = 12),
      NeetChapter(name = "Magnetism & Matter", subject = "Physics", classLevel = 12),
      NeetChapter(name = "Electromagnetic Induction", subject = "Physics", classLevel = 12),
      NeetChapter(name = "Alternating Current", subject = "Physics", classLevel = 12),
      NeetChapter(name = "Electromagnetic Waves", subject = "Physics", classLevel = 12),
      NeetChapter(name = "Ray Optics & Optical Instruments", subject = "Physics", classLevel = 12),
      NeetChapter(name = "Wave Optics", subject = "Physics", classLevel = 12),
      NeetChapter(name = "Dual Nature of Radiation & Matter", subject = "Physics", classLevel = 12),
      NeetChapter(name = "Atoms", subject = "Physics", classLevel = 12),
      NeetChapter(name = "Nuclei", subject = "Physics", classLevel = 12),
      NeetChapter(name = "Semiconductor Electronics", subject = "Physics", classLevel = 12),

      // ==================== CHEMISTRY CLASS 11 ====================
      NeetChapter(name = "Some Basic Concepts of Chemistry (Mole)", subject = "Chemistry", classLevel = 11, isCompleted = true, isPyqDone = true),
      NeetChapter(name = "Structure of Atom", subject = "Chemistry", classLevel = 11, isCompleted = true),
      NeetChapter(name = "Classification of Elements & Periodicity", subject = "Chemistry", classLevel = 11),
      NeetChapter(name = "Chemical Bonding & Molecular Structure", subject = "Chemistry", classLevel = 11),
      NeetChapter(name = "Chemical Thermodynamics", subject = "Chemistry", classLevel = 11),
      NeetChapter(name = "Equilibrium", subject = "Chemistry", classLevel = 11),
      NeetChapter(name = "Redox Reactions", subject = "Chemistry", classLevel = 11),
      NeetChapter(name = "Organic Chemistry: Some Basic Principles & Techniques", subject = "Chemistry", classLevel = 11),
      NeetChapter(name = "Hydrocarbons", subject = "Chemistry", classLevel = 11),

      // ==================== CHEMISTRY CLASS 12 ====================
      NeetChapter(name = "Solutions", subject = "Chemistry", classLevel = 12),
      NeetChapter(name = "Electrochemistry", subject = "Chemistry", classLevel = 12),
      NeetChapter(name = "Chemical Kinetics", subject = "Chemistry", classLevel = 12),
      NeetChapter(name = "The d- and f-Block Elements", subject = "Chemistry", classLevel = 12),
      NeetChapter(name = "Coordination Compounds", subject = "Chemistry", classLevel = 12),
      NeetChapter(name = "Haloalkanes & Haloarenes", subject = "Chemistry", classLevel = 12),
      NeetChapter(name = "Alcohols, Phenols & Ethers", subject = "Chemistry", classLevel = 12),
      NeetChapter(name = "Aldehydes, Ketones & Carboxylic Acids", subject = "Chemistry", classLevel = 12),
      NeetChapter(name = "Amines", subject = "Chemistry", classLevel = 12),
      NeetChapter(name = "Biomolecules (Chemistry)", subject = "Chemistry", classLevel = 12)
    )
  }
}

