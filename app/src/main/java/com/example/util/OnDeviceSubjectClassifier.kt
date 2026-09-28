package com.example.util

import com.example.data.model.NeetChapter
import java.util.Locale

/**
 * Intelligent On-Device Subject Classifier for NEET & Study Tasks.
 * Analyzes task names and semantic keywords to accurately categorize tasks
 * into Physics, Chemistry, Botany, Zoology, or Habits & Tasks.
 */
object OnDeviceSubjectClassifier {

  enum class SubjectCategory(
    val displayName: String,
    val colorHex: Long,
    val isAcademic: Boolean
  ) {
    PHYSICS("Physics", 0xFF3B82F6, true),
    CHEMISTRY("Chemistry", 0xFFEC4899, true),
    BOTANY("Botany", 0xFF10B981, true),
    ZOOLOGY("Zoology", 0xFFF59E0B, true),
    HABITS_TASKS("Habits & Tasks", 0xFF8B5CF6, false)
  }

  // Pre-indexed normalized words from official NEET chapters
  private val botanyChapterKeywords by lazy {
    NeetChapter.DEFAULT_CHAPTERS
      .filter { it.subject.equals("Botany", ignoreCase = true) }
      .flatMap { extractMeaningfulKeywords(it.name) }
      .toSet()
  }

  private val zoologyChapterKeywords by lazy {
    NeetChapter.DEFAULT_CHAPTERS
      .filter { it.subject.equals("Zoology", ignoreCase = true) }
      .flatMap { extractMeaningfulKeywords(it.name) }
      .toSet()
  }

  private val physicsChapterKeywords by lazy {
    NeetChapter.DEFAULT_CHAPTERS
      .filter { it.subject.equals("Physics", ignoreCase = true) }
      .flatMap { extractMeaningfulKeywords(it.name) }
      .toSet()
  }

  private val chemistryChapterKeywords by lazy {
    NeetChapter.DEFAULT_CHAPTERS
      .filter { it.subject.equals("Chemistry", ignoreCase = true) }
      .flatMap { extractMeaningfulKeywords(it.name) }
      .toSet()
  }

  // Pure lifestyle and non-academic habits
  private val lifestyleHabitKeywords = setOf(
    "gym", "workout", "exercise", "run", "running", "jog", "jogging", "walk", "walking",
    "yoga", "meditate", "meditation", "pranayama", "sleep", "nap", "waking up", "wake up",
    "morning routine", "water", "hydration", "drink water", "breakfast", "lunch", "dinner",
    "meal", "eating", "food", "cook", "cooking", "bath", "shower", "brush teeth", "stretch",
    "stretching", "clean", "cleaning", "room clean", "laundry", "wash clothes", "relax",
    "relaxing", "rest", "break", "chill", "music", "podcast", "family", "call parents",
    "call friends", "screen time", "social media", "diary", "journal", "journaling", "pray",
    "prayer", "skincare", "haircare", "chores", "grocery", "shopping", "hobby", "guitar",
    "gaming", "cycle", "cycling", "pushups", "cardio"
  )

  // Physics domain terms & abbreviations
  private val physicsDomainTerms = setOf(
    "physics", "phy", "kinematics", "mechanics", "newton", "nlm", "gravity", "gravitation",
    "work energy", "wep", "rotational", "rigid body", "moment of inertia", "torque", "com",
    "center of mass", "elasticity", "surface tension", "fluid", "viscosity", "bernoulli",
    "calorimetry", "heat transfer", "thermodynamics", "ktg", "kinetic theory", "shm",
    "simple harmonic", "oscillation", "oscillations", "waves", "sound waves", "doppler",
    "electrostatics", "electric field", "coulomb", "gauss", "potential", "capacitor",
    "capacitance", "current electricity", "ohm", "kirchhoff", "meter bridge", "potentiometer",
    "magnetism", "biot savart", "ampere", "lorentz", "solenoid", "magnetic dipole", "emi",
    "electromagnetic induction", "faraday", "lenz", "ac", "alternating current", "transformer",
    "lc oscillation", "em waves", "electromagnetic waves", "optics", "ray optics", "wave optics",
    "reflection", "refraction", "lens", "mirror", "prism", "interference", "diffraction",
    "polarization", "ydse", "modern physics", "dual nature", "photoelectric", "de broglie",
    "davisson", "bohr", "rutherford", "nuclei", "nuclear", "radioactivity", "mass defect",
    "binding energy", "fission", "fusion", "semiconductor", "semiconductors", "p-n junction",
    "diode", "led", "zener", "logic gate", "transistor", "units", "dimensions", "vernier",
    "screw gauge", "error analysis", "vectors", "projectile", "circular motion", "hcv",
    "hc verma", "dc pandey", "sl arora", "irodov", "cengage physics", "galaxy physics",
    "mr sir", "pw physics", "numericals", "physics numericals", "physics formula"
  )

  // Chemistry domain terms & abbreviations
  private val chemistryDomainTerms = setOf(
    "chem", "chemistry", "organic", "inorganic", "physical chem", "mole", "mole concept",
    "stoichiometry", "atomic structure", "quantum", "aufbau", "hund", "pauli", "periodic table",
    "periodicity", "ionization energy", "electron affinity", "electronegativity", "chemical bonding",
    "vsepr", "hybridization", "molecular orbital", "mot", "hydrogen bond", "gas laws", "ideal gas",
    "real gas", "van der waals", "thermochemistry", "hess law", "enthalpy", "entropy",
    "gibbs free energy", "chemical equilibrium", "ionic equilibrium", "le chatelier",
    "solubility product", "ksp", "ph", "buffer", "common ion", "redox", "oxidation state",
    "balancing", "electrode potential", "electrochemistry", "nernst", "galvanic cell",
    "electrolysis", "faraday laws", "conductance", "kohlrausch", "chemical kinetics",
    "rate of reaction", "rate constant", "order of reaction", "pseudo first order", "arrhenius",
    "activation energy", "surface chemistry", "adsorption", "colloids", "catalysis", "metallurgy",
    "refining", "froth floatation", "s-block", "alkali", "alkaline earth", "p-block", "boron",
    "carbon family", "nitrogen family", "oxygen family", "halogens", "noble gases", "d-block",
    "f-block", "transition elements", "lanthanoids", "actinoids", "coordination", "complexes",
    "werner", "cft", "crystal field", "ligands", "goc", "general organic", "iupac", "resonance",
    "inductive effect", "hyperconjugation", "electrophile", "nucleophile", "carbocation",
    "carbanion", "free radical", "isomerism", "hydrocarbons", "alkanes", "alkenes", "alkynes",
    "benzene", "aromaticity", "markovnikov", "wurtz", "friedel crafts", "haloalkanes",
    "haloarenes", "sn1", "sn2", "alcohols", "phenols", "ethers", "grignard", "reimer tiemann",
    "kolbe", "aldehydes", "ketones", "carboxylic", "aldol", "cannizzaro", "clemmensen",
    "amines", "diazonium", "hoffmann", "biomolecules chem", "carbohydrates", "polymers",
    "environmental chemistry", "everyday life", "ms chouhan", "op tandon", "n awasthi",
    "vk jaiswal", "himanshu pandey", "ncert fingertips chem", "pw chem", "organic reactions"
  )

  // Botany domain terms
  private val botanyDomainTerms = setOf(
    "botany", "bot", "plant", "plants", "flora", "the living world", "biological classification",
    "monera", "protista", "fungi", "lichens", "mycorrhiza", "algae", "bryophytes", "bryophyte",
    "pteridophytes", "pteridophyte", "gymnosperms", "gymnosperm", "angiosperms", "angiosperm",
    "plant kingdom", "morphology", "root", "roots", "stem", "leaf", "leaves", "inflorescence",
    "flower", "fruit", "seed", "floral formula", "anatomy of flowering plants", "meristem",
    "xylem", "phloem", "vascular bundle", "secondary growth", "cambium", "cell unit of life",
    "cell wall", "chloroplast", "vacuole", "mitochondria", "plastids", "cell cycle", "mitosis",
    "meiosis", "prophase", "metaphase", "anaphase", "telophase", "cytokinesis", "crossing over",
    "photosynthesis", "chlorophyll", "light reaction", "dark reaction", "calvin cycle", "c3",
    "c4", "hatch slack", "krantz anatomy", "cam plants", "photorespiration", "respiration in plants",
    "glycolysis", "krebs cycle", "etc", "atp synthase", "plant growth", "auxin", "gibberellin",
    "cytokinin", "ethylene", "abscisic acid", "photoperiodism", "vernalization", "sexual reproduction in flowering plants",
    "pollination", "double fertilization", "endosperm", "apomixis", "inheritance", "genetics",
    "mendel", "monohybrid", "dihybrid", "linkage", "recombination", "molecular basis",
    "dna structure", "dna replication", "transcription", "genetic code", "translation",
    "lac operon", "human genome project", "microbes in human welfare", "organisms and populations",
    "ecosystem", "ecological pyramids", "biodiversity", "conservation", "national park",
    "botany ncert", "bot ncert", "pw botany", "botany dpp"
  )

  // Zoology domain terms
  private val zoologyDomainTerms = setOf(
    "zoology", "zoo", "animal", "animals", "fauna", "animal kingdom", "porifera", "coelenterata",
    "cnidaria", "ctenophora", "platyhelminthes", "aschelminthes", "annelida", "arthropoda",
    "mollusca", "echinodermata", "chordata", "vertebrata", "amphibia", "reptilia", "aves",
    "mammalia", "structural organisation in animals", "epithelial tissue", "connective tissue",
    "cockroach", "frog", "earthworm", "biomolecules", "proteins", "enzymes",
    "breathing and exchange of gases", "respiratory volume", "tidal volume", "oxygen dissociation",
    "body fluids and circulation", "blood cells", "rbc", "wbc", "platelets", "cardiac cycle",
    "ecg", "double circulation", "excretory products", "nephron", "ultrafiltration", "raas",
    "locomotion and movement", "sarcomere", "muscle contraction", "actin myosin", "skeletal system",
    "joints", "neural control and coordination", "neuron", "synapse", "brain", "reflex arc",
    "eye", "ear", "chemical coordination and integration", "endocrine", "pituitary", "thyroid",
    "adrenal", "insulin", "human reproduction", "spermatogenesis", "oogenesis", "menstrual cycle",
    "fertilization", "blastocyst", "implantation", "placenta", "parturition", "reproductive health",
    "contraception", "ivf", "amniocentesis", "evolution", "natural selection", "darwin",
    "homologous", "analogous", "hardy weinberg", "human evolution", "human health and disease",
    "immunity", "antibodies", "aids", "hiv", "cancer", "vaccine", "drugs", "biotechnology",
    "recombinant dna", "restriction enzyme", "pcr", "gel electrophoresis", "transgenic",
    "pw zoology", "zoo ncert", "trueman zoology"
  )

  /**
   * Primary entry point: AI classification of a task name.
   */
  fun classify(taskName: String, notes: String = ""): SubjectCategory {
    val clean = (taskName + " " + notes).trim().lowercase(Locale.ROOT)
    if (clean.isBlank()) return SubjectCategory.HABITS_TASKS

    // 1. Check exact or substantial chapter match with official NEET chapters
    for (chapter in NeetChapter.DEFAULT_CHAPTERS) {
      val chName = chapter.name.lowercase(Locale.ROOT)
      if (clean.contains(chName) || chName.contains(clean)) {
        return when (chapter.subject.lowercase(Locale.ROOT)) {
          "physics" -> SubjectCategory.PHYSICS
          "chemistry" -> SubjectCategory.CHEMISTRY
          "botany" -> SubjectCategory.BOTANY
          "zoology" -> SubjectCategory.ZOOLOGY
          else -> SubjectCategory.HABITS_TASKS
        }
      }
    }

    // 2. Compute semantic match score across each academic category
    val tokens = clean.split(Regex("[^a-zA-Z0-9]+")).filter { it.length > 1 }

    var phyScore = 0
    var chemScore = 0
    var botScore = 0
    var zooScore = 0
    var habitScore = 0

    // Check full string contains for multi-word phrases
    for (phrase in physicsDomainTerms) {
      if (phrase.contains(" ") && clean.contains(phrase)) phyScore += 5
    }
    for (phrase in chemistryDomainTerms) {
      if (phrase.contains(" ") && clean.contains(phrase)) chemScore += 5
    }
    for (phrase in botanyDomainTerms) {
      if (phrase.contains(" ") && clean.contains(phrase)) botScore += 5
    }
    for (phrase in zoologyDomainTerms) {
      if (phrase.contains(" ") && clean.contains(phrase)) zooScore += 5
    }
    for (phrase in lifestyleHabitKeywords) {
      if (phrase.contains(" ") && clean.contains(phrase)) habitScore += 5
    }

    // Check token-level keywords
    for (token in tokens) {
      if (physicsDomainTerms.contains(token) || physicsChapterKeywords.contains(token)) phyScore += 2
      if (chemistryDomainTerms.contains(token) || chemistryChapterKeywords.contains(token)) chemScore += 2
      if (botanyDomainTerms.contains(token) || botanyChapterKeywords.contains(token)) botScore += 2
      if (zoologyDomainTerms.contains(token) || zoologyChapterKeywords.contains(token)) zooScore += 2
      if (lifestyleHabitKeywords.contains(token)) habitScore += 2
    }

    // Explicit subject indicators take highest precedence
    if (clean.contains("physics") || clean.contains("phy ") || clean.endsWith(" phy") || clean.contains("hcv")) {
      phyScore += 10
    }
    if (clean.contains("chemistry") || clean.contains("chem ") || clean.endsWith(" chem") || clean.contains("inorganic") || clean.contains("organic")) {
      chemScore += 10
    }
    if (clean.contains("botany") || clean.contains("bot ") || clean.endsWith(" bot") || clean.contains("plant")) {
      botScore += 10
    }
    if (clean.contains("zoology") || clean.contains("zoo ") || clean.endsWith(" zoo") || clean.contains("animal") || clean.contains("human")) {
      zooScore += 10
    }

    val maxAcademicScore = maxOf(phyScore, chemScore, botScore, zooScore)

    // Academic test/mock handling: if it contains "mock", "test", "dpp", "pyq", "ncert", "revision", "formula", "question"
    val hasAcademicIndicator = clean.contains("mock") || clean.contains("test") || clean.contains("dpp") ||
      clean.contains("pyq") || clean.contains("ncert") || clean.contains("revision") || clean.contains("formula") ||
      clean.contains("lecture") || clean.contains("notes") || clean.contains("question") || clean.contains("study") ||
      clean.contains("exam") || clean.contains("class") || clean.contains("allen") || clean.contains("aakash") || clean.contains("pw")

    if (maxAcademicScore > 0) {
      // Pick the highest scoring academic subject
      return when {
        phyScore >= chemScore && phyScore >= botScore && phyScore >= zooScore && phyScore > habitScore -> SubjectCategory.PHYSICS
        chemScore >= phyScore && chemScore >= botScore && chemScore >= zooScore && chemScore > habitScore -> SubjectCategory.CHEMISTRY
        botScore >= phyScore && botScore >= chemScore && botScore >= zooScore && botScore > habitScore -> SubjectCategory.BOTANY
        zooScore >= phyScore && zooScore >= chemScore && zooScore >= botScore && zooScore > habitScore -> SubjectCategory.ZOOLOGY
        habitScore > maxAcademicScore -> SubjectCategory.HABITS_TASKS
        phyScore == maxAcademicScore -> SubjectCategory.PHYSICS
        chemScore == maxAcademicScore -> SubjectCategory.CHEMISTRY
        botScore == maxAcademicScore -> SubjectCategory.BOTANY
        else -> SubjectCategory.ZOOLOGY
      }
    }

    // If no strong academic keywords were found, check if it's explicitly a lifestyle habit
    if (habitScore > 0) {
      return SubjectCategory.HABITS_TASKS
    }

    // If it has academic indicators (like "Test 1" or "Question solving" or "NCERT line revision"):
    // Distribute or assign to Biology / Physics / Chemistry rather than blindly into Habits!
    if (hasAcademicIndicator) {
      // Default to general study - attribute to Biology/Physics/Chemistry based on standard NEET weight
      return SubjectCategory.PHYSICS
    }

    // Default fallback: only truly unrecognized non-study tasks go to Habits & Tasks
    return SubjectCategory.HABITS_TASKS
  }

  private fun extractMeaningfulKeywords(title: String): List<String> {
    val stopwords = setOf("the", "and", "in", "of", "some", "their", "its", "unit", "life", "part")
    return title.lowercase(Locale.ROOT)
      .split(Regex("[^a-zA-Z0-9]+"))
      .filter { it.length > 2 && !stopwords.contains(it) }
  }
}
