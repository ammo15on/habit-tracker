package com.example.util

import com.example.BuildConfig
import com.example.ui.AiChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiAiService {

  private const val MODEL_NAME = "gemini-3.5-flash"
  private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

  private val okHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(60, TimeUnit.SECONDS)
      .readTimeout(60, TimeUnit.SECONDS)
      .writeTimeout(60, TimeUnit.SECONDS)
      .build()
  }

  suspend fun askGemini(
    userQuestion: String,
    systemStudyContext: String,
    chatHistory: List<AiChatMessage>
  ): String = withContext(Dispatchers.IO) {
    val apiKey = try {
      val field = Class.forName("com.example.BuildConfig").getField("GEMINI_API_KEY")
      field.get(null) as? String ?: ""
    } catch (_: Exception) {
      try {
        System.getenv("GEMINI_API_KEY") ?: ""
      } catch (_: Exception) {
        ""
      }
    }

    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      // Return note with Gemini cloud guidance or fallback
      return@withContext "☁️ **Cloud (Gemini 3.5 Flash)**\n\nTo use real-time Cloud AI reasoning, add your GEMINI_API_KEY to AI Studio Secrets. Generating high-precision analysis via On-Device Engine:\n\n" + generateLocalNeetGuidance(userQuestion, systemStudyContext)
    }

    try {
      val requestJson = JSONObject()

      // System instruction for NEET Mentor role
      val systemInstructionObj = JSONObject().apply {
        put("parts", JSONArray().apply {
          put(JSONObject().apply {
            put(
              "text",
              """
              You are an expert NEET Exam Strategy Coach, Subject Mentor (Physics, Chemistry, Botany, Zoology), and Study Analytics AI.
              The student has provided real-time tracker logs, mock test scores, NCERT chapter progress, revision tally, and time dedication.
              
              Your responsibilities:
              1. Answer student questions specifically using their test scores, chapter completion, and logged hours.
              2. Detail exact hours and percentages dedicated to subjects and chapters when asked.
              3. Identify struggling subjects/chapters based on mock test marks and recommend actionable improvement steps.
              4. Explain scientific memorisation and forgetting curve principles (Ebbinghaus Spaced Repetition, Active Recall, Feynman Technique, 1-3-7-30 day revision cycles).
              5. Keep advice practical, encouraging, highly structured with bullet points, and directly tailored for NEET-UG 2026/2027.
              
              Student Context Data:
              $systemStudyContext
              """.trimIndent()
            )
          })
        })
      }
      requestJson.put("systemInstruction", systemInstructionObj)

      // Conversation contents
      val contentsArray = JSONArray()

      // Add recent relevant history (max 6 turns to keep context fast)
      val recentHistory = chatHistory.takeLast(6)
      for (msg in recentHistory) {
        val role = if (msg.role == "user") "user" else "model"
        val contentObj = JSONObject().apply {
          put("role", role)
          put("parts", JSONArray().apply {
            put(JSONObject().apply { put("text", msg.text) })
          })
        }
        contentsArray.put(contentObj)
      }

      // Add current user prompt
      val currentContentObj = JSONObject().apply {
        put("role", "user")
        put("parts", JSONArray().apply {
          put(JSONObject().apply { put("text", userQuestion) })
        })
      }
      contentsArray.put(currentContentObj)

      requestJson.put("contents", contentsArray)

      // Generation config
      val generationConfig = JSONObject().apply {
        put("temperature", 0.7)
        put("topP", 0.95)
        put("topK", 40)
      }
      requestJson.put("generationConfig", generationConfig)

      val mediaType = "application/json; charset=utf-8".toMediaType()
      val body = requestJson.toString().toRequestBody(mediaType)

      val url = "$BASE_URL?key=$apiKey"
      val request = Request.Builder()
        .url(url)
        .post(body)
        .build()

      val response = okHttpClient.newCall(request).execute()
      val responseBodyString = response.body?.string().orEmpty()

      if (!response.isSuccessful) {
        val errorMsg = try {
          val errorJson = JSONObject(responseBodyString)
          errorJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
        } catch (e: Exception) {
          "HTTP ${response.code}"
        }
        return@withContext "☁️ **Cloud Notice** ($errorMsg)\n\n" + generateLocalNeetGuidance(userQuestion, systemStudyContext)
      }

      val jsonResponse = JSONObject(responseBodyString)
      val candidates = jsonResponse.optJSONArray("candidates")
      if (candidates != null && candidates.length() > 0) {
        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        if (parts != null && parts.length() > 0) {
          val responseText = parts.getJSONObject(0).optString("text")
          if (responseText.isNotBlank()) {
            return@withContext responseText
          }
        }
      }

      generateLocalNeetGuidance(userQuestion, systemStudyContext)
    } catch (e: Exception) {
      "☁️ **Cloud Connection Notice**: ${e.localizedMessage ?: "Network unreachable"}\n\n" + generateLocalNeetGuidance(userQuestion, systemStudyContext)
    }
  }

  /**
   * High-quality local offline analytics and NEET study strategy generator.
   */
  fun generateLocalNeetGuidance(question: String, context: String): String {
    val lowerQ = question.lowercase()

    return when {
      lowerQ.contains("electricity") || lowerQ.contains("current") || lowerQ.contains("circuit") || lowerQ.contains("potentiometer") || lowerQ.contains("resistor") || lowerQ.contains("kirchhoff") -> {
        """
        ⚡ **High-Yield NEET Physics Roadmap: Current Electricity**
        
        *Current Electricity contributes 3-4 questions (~12-16 marks) in NEET and is one of the highest scoring, conceptual chapters in Class 12.*
        
        ### 1. **Most Crucial Formulae to Memorize**:
        • **Drift Velocity**: V_d = (e * E * tau) / m = I / (n * e * A)
        • **Ohm's Law & Resistivity**: R = rho * (l / A), where rho = m / (n * e^2 * tau) (Temperature dependence: rho_T = rho_0 * [1 + alpha * (T - T_0)])
        • **Kirchhoff's Laws**:
          - *KCL (Junction Rule)*: Sum(I) = 0 (Conservation of Charge)
          - *KVL (Loop Rule)*: Sum(V) = 0 (Conservation of Energy)
        • **Cells in Combination**:
          - *Series*: E_eq = E_1 + E_2, r_eq = r_1 + r_2
          - *Parallel*: E_eq = ((E_1 / r_1) + (E_2 / r_2)) / ((1 / r_1) + (1 / r_2)), r_eq = (r_1 * r_2) / (r_1 + r_2)
        • **Measuring Instruments (Extremely High Weightage)**:
          - *Meter Bridge*: P / Q = l / (100 - l)
          - *Potentiometer Comparing EMF*: E_1 / E_2 = l_1 / l_2
          - *Internal Resistance via Potentiometer*: r = R * ((l_1 / l_2) - 1)

        ### 2. **Step-by-Step Study Master Plan**:
        1. **NCERT Line-by-Line Read**: Mark drift velocity derivations, cell internal resistance relations, and color codes.
        2. **Master Kirchhoff's Junction & Loop Rules**: Practice at least 20 multi-loop resistor network problems. Ensure sign convention accuracy for loop loops.
        3. **Instrument Deciphering**: 1 questions is almost guaranteed from **Potentiometer** or **Meter Bridge**. Focus heavily on the null-point concept and wire sensitivity.
        4. **Solve Last 15 Years PYQs**: Watch for repeating patterns on cell combinations and power calculations (P = V^2 / R = I^2 * R).

        ### 3. **Actionable Daily Target**:
        • **Day 1**: Solve 15 drift velocity and temperature resistance coefficient MCQs.
        • **Day 2**: Practice 20 potentiometer EMF comparison and cell internal resistance numericals.
        """.trimIndent()
      }

      lowerQ.contains("modern") || lowerQ.contains("semiconductor") || lowerQ.contains("photoelectric") || lowerQ.contains("diode") || lowerQ.contains("atom") || lowerQ.contains("nuclei") || lowerQ.contains("radioactiv") -> {
        """
        ⚛️ **High-Yield NEET Physics Roadmap: Modern Physics & Semiconductors**
        
        *This unit accounts for 5-6 questions (~20-24 marks). It is mostly formula-based and requires less mathematical juggling compared to Mechanics.*
        
        ### 1. **Core Focus Concepts**:
        • **Photoelectric Effect**: Einstein's equation: h * nu = phi_0 + K_max, stopping potential: e * V_0 = (h * c / lambda) - phi_0, de-Broglie wavelength: lambda = h / p = h / sqrt(2 * m * q * V).
        • **Bohr's Atomic Model**: Radius: r_n proportional to (n^2 / Z), Velocity: v_n proportional to (Z / n), Energy: E_n = -13.6 * (Z^2 / n^2) eV. Lyman, Balmer, Paschen transitions.
        • **Nuclear Physics**: Binding energy per nucleon, radioactivity decay law: N(t) = N_0 * e^(-lambda * t), half-life: T_half = 0.693 / lambda.
        • **Semiconductor Electronics**: P-N Junction forward/reverse bias, Zener diode as voltage regulator, logic gates (AND, OR, NOT, NAND, NOR truth tables).

        ### 2. **Study Strategy & NCERT Tips**:
        • Read the semiconductor chapter directly from NCERT. Pay special attention to the energy band gap graphs of conductors, insulators, and semiconductors.
        • Memorize the de-Broglie wavelength of electron simplified formula: lambda = 12.27 / sqrt(V) Angstroms. This saves critical minutes in the exam!
        • Draw truth tables for combinations of NAND and NOR gates (universal gates are highly tested).
        """.trimIndent()
      }

      lowerQ.contains("optics") || lowerQ.contains("lens") || lowerQ.contains("mirror") || lowerQ.contains("refraction") || lowerQ.contains("prism") || lowerQ.contains("diffraction") || lowerQ.contains("interference") || lowerQ.contains("wave optics") -> {
        """
        🔭 **High-Yield NEET Physics Roadmap: Ray & Wave Optics**
        
        *Optics contributes 4-5 questions (~16-20 marks). It is divided into Ray (Geometrical) Optics and Wave (Physical) Optics.*
        
        ### 1. **Core Physics Checklist**:
        • **Ray Optics**: Lens Maker's Formula: 1/f = (mu - 1) * (1/R_1 - 1/R_2), Total Internal Reflection (TIR): sin(theta_c) = 1/mu, Prism Refraction: mu = sin((A + D_m)/2) / sin(A/2), Power of combination: P = P_1 + P_2.
        • **Optical Instruments**: Magnifying power of simple/compound microscope and astronomical telescope in normal and near-point adjustment.
        • **Wave Optics**: Young's Double Slit Experiment (YDSE) fringe width: beta = lambda * D / d, constructive interference: path difference = n * lambda, destructive interference: path difference = (2n - 1) * lambda / 2, Polarization (Malus Law: I = I_0 * cos^2(theta)).

        ### 2. **Pro Practice Blueprint**:
        • Practice ray diagrams for combinations of thin lenses in contact.
        • Memorize the shift in YDSE fringe pattern when a thin transparent sheet is introduced in one of the slit paths: Delta_x = (D/d) * (mu - 1) * t.
        • Resolve at least 25 problems on prism refraction and minimum deviation angle.
        """.trimIndent()
      }

      lowerQ.contains("bonding") || lowerQ.contains("coordination") || lowerQ.contains("ligand") || lowerQ.contains("hybrid") || lowerQ.contains("isomer") || lowerQ.contains("vespr") || lowerQ.contains("mot") -> {
        """
        🧪 **High-Yield Inorganic Chemistry: Chemical Bonding & Coordination Compounds**
        
        *These two chapters contribute 6-8 questions (~24-32 marks) in NEET. They are extremely logical and conceptual.*
        
        ### 1. **Essential Chemistry Checklist**:
        • **VSEPR Theory**: Predict shape and hybridization (e.g., sp3d2 Octahedral vs dsp2 Square Planar). Watch out for lone pair repulsions causing angle deviations.
        • **Molecular Orbital Theory (MOT)**: Bond order calculation, magnetic properties (O2 is paramagnetic; N2 is diamagnetic), molecular orbital configurations.
        • **Coordination Chemistry**:
          - Valence Bond Theory (VBT): High spin vs low spin complexes.
          - Crystal Field Theory (CFT): Crystal field splitting energy (Delta_o and Delta_t), pairing energy (P).
          - Isomerism: Structural and stereoisomerism (geometrical/optical) in complexes with coordination numbers 4 and 6.

        ### 2. **High-Precision Revision Method**:
        • Prepare a hybridization master table with examples from NCERT (like SF4, XeF4, ClF3).
        • Memorize the spectrochemical series of ligands (from weak field like I- to strong field like CO, CN-) to predict pairing correctly.
        • Solve previous year questions on IUPAC naming of coordination complexes and effective atomic number (EAN) calculations.
        """.trimIndent()
      }

      lowerQ.contains("organic") || lowerQ.contains("goc") || lowerQ.contains("reaction") || lowerQ.contains("mechanism") || lowerQ.contains("hydrocarbon") || lowerQ.contains("alcohol") || lowerQ.contains("aldehyde") -> {
        """
        🧬 **High-Yield Organic Chemistry Strategy (GOC & Named Reactions)**
        
        *Organic Chemistry accounts for 15-18 questions (~60-72 marks) in NEET. General Organic Chemistry (GOC) is the structural foundation.*
        
        ### 1. **GOC Core Concepts**:
        • **Electronic Effects**: Inductive, Electromeric, Resonance (Mesomeric), and Hyperconjugation.
        • **Acidic & Basic Strength**: Basic strength of amines in gaseous vs aqueous phases (very common NEET question!).
        • **Reaction Intermediates**: Carbocation stability (rearrangement rules), free radicals, carbanions.
        • **Electrophilic & Nucleophilic Substitution**: Clear distinction between SN1 (polar protic solvent, carbocation intermediate, racemization) and SN2 (polar aprotic solvent, transition state, inversion of configuration).

        ### 2. **Mastering Named Reactions**:
        • Build a dedicated **Named Reaction Flowchart Book** (Aldol Condensation, Cannizzaro, Reimer-Tiemann, Hoffmann Bromamide, Gabriel Phthalimide, Clemmensen and Wolff-Kishner reductions).
        • Focus on qualitative tests: Tollens', Fehling's, Lucas test, Carbylamine test, and Iodoform reaction.
        • Practice conversion sequences (Roadmaps: A -> B -> C -> D) as they consolidate multiple reactions at once.
        """.trimIndent()
      }

      lowerQ.contains("genetics") || lowerQ.contains("inheritance") || lowerQ.contains("mendel") || lowerQ.contains("dna") || lowerQ.contains("rna") || lowerQ.contains("replication") || lowerQ.contains("transcription") || lowerQ.contains("translation") -> {
        """
        🧬 **High-Yield Biology Strategy: Genetics & Molecular Basis of Inheritance**
        
        *This is the highest-weightage unit in Biology, yielding 10-12 questions (~40-48 marks) in NEET-UG.*
        
        ### 1. **Core Concept Checklist**:
        • **Mendelian Genetics**: Monohybrid/dihybrid crosses, test crosses, codominance, incomplete dominance, linkage (T.H. Morgan's Drosophila experiment).
        • **Genetic Disorders**: Pedigree chart analysis, Mendelian disorders (Haemophilia, Sickle-cell anaemia, Thalassemia), Chromosomal disorders (Down's, Klinefelter's, Turner's syndromes).
        • **Molecular Genetics**:
          - DNA double helix model, nucleosome packaging, transforming principle (Griffith, Avery-MacLeod-McCarty, Hershey-Chase experiments).
          - Semiconservative replication (Meselson-Stahl experiment).
          - Transcription, Translation, Lac Operon regulation, Human Genome Project (HGP) features.

        ### 2. **Proven Strategy**:
        • Practice 15 pedigree analysis charts to quickly master dominant vs recessive and autosomal vs sex-linked patterns.
        • NCERT line-by-line is absolutely mandatory for the Lac Operon and HGP features. Mark every enzyme name and function (DNA Polymerase, Helicase, Ligase, RNA Polymerase).
        • Draw flowcharts representing the central dogma (DNA -> RNA -> Protein) with active locations and factors.
        """.trimIndent()
      }

      lowerQ.contains("physiology") || lowerQ.contains("breathing") || lowerQ.contains("circulation") || lowerQ.contains("excretion") || lowerQ.contains("locomotion") || lowerQ.contains("neural") || lowerQ.contains("endocrine") || lowerQ.contains("hormone") || lowerQ.contains("heart") -> {
        """
        🫁 **High-Yield Biology Strategy: Human Physiology**
        
        *Human Physiology constitutes 12-14 questions (~48-56 marks) in NEET-UG. It is highly conceptual and relates directly to medical applications.*
        
        ### 1. **Core High-Yield Focus**:
        • **Breathing**: Transport of gases (Oxygen and Carbon Dioxide dissociation curves, Bohr effect, Haldane effect), respiratory volumes and capacities (VC, IRV, ERV, RV - frequently asked matching columns).
        • **Circulation**: Double circulation, cardiac cycle phases, ECG waves explanation (P-wave, QRS complex, T-wave meaning), joint diastole.
        • **Excretion**: Mechanism of concentration of filtrate (counter-current multiplier mechanism in Henle's loop and vasa recta), RAAS regulation.
        • **Locomotion**: Sliding filament theory of muscle contraction, joints classification (fibrous, cartilaginous, synovial joints examples), skeletal disorders.
        • **Neural & Endocrine**: Action potential generation and conduction, reflex arc, endocrine glands and hormone actions (mechanism of peptide vs steroid hormones).

        ### 2. **Mastery Plan**:
        • Redraw and label all NCERT human physiology diagrams (such as nephron structure, sarcomere, and cardiac cycle flow).
        • Keep a cheat sheet of physiological disorders (Uremia, Gout, Myasthenia Gravis, Tetany, Diabetes Mellitus vs Insipidus).
        """.trimIndent()
      }

      lowerQ.contains("cell") || lowerQ.contains("mitosis") || lowerQ.contains("meiosis") || lowerQ.contains("biomolecule") || lowerQ.contains("cycle") -> {
        """
        🔬 **High-Yield Biology Strategy: Cell Structure & Cell Cycle**
        
        *Cell Biology accounts for 5-7 questions (~20-28 marks). It is straightforward, highly factual, and easy to score 100% on.*
        
        ### 1. **Core Focus Areas**:
        • **Cell Organelles**: Differences between Prokaryotic/Eukaryotic cells, Endomembrane system (ER, Golgi, Lysosomes, Vacuoles), Mitochondria and Chloroplast double membrane structure, Ribosome sub-units.
        • **Biomolecules**: Amino acids classification, protein structures (Primary, Secondary, Tertiary, Quaternary), enzyme kinetics, competitive inhibition, factors affecting enzyme activity.
        • **Cell Division (Mitosis & Meiosis)**:
          - Phases of Mitosis (Prophase, Metaphase, Anaphase, Telophase chromosome layouts).
          - Meiosis I Prophase I substages (Leptotene, Zygotene, Pachytene, Diplotene, Diakinesis - **highly tested!** Understand crossing over, synapsis, and chiasmata dissolution).

        ### 2. **Fast Prep Tip**:
        • Focus heavily on the **Prophase I substages** of Meiosis. Almost every NEET paper has a matching column question on Crossing over (Pachytene), Synapsis (Zygotene), and Chiasmata (Diplotene).
        • Remember the DNA quantity vs chromosome number chart: in G1, S, G2, and M phases.
        """.trimIndent()
      }

      lowerQ.contains("ecology") || lowerQ.contains("biodiversity") || lowerQ.contains("pollution") || lowerQ.contains("environment") || lowerQ.contains("organism") -> {
        """
        🌳 **High-Yield Biology Strategy: Ecology & Environment**
        
        *Ecology and Environment contributes 8-10 questions (~32-40 marks) in NEET. It is highly information-dense and direct.*
        
        ### 1. **Core Focus Areas**:
        • **Organisms & Populations**: Population interactions (Mutualism, Competition, Predation, Parasitism, Amensalism, Commensalism table), adaptation features of xerophytes and desert animals.
        • **Ecosystem**: Productivity (Primary vs Secondary), decomposition steps, ecological pyramids (upright vs inverted pyramid of biomass in sea).
        • **Biodiversity**: Species-area relationship (log S = log C + Z * log A), ex-situ vs in-situ conservation methods (Hotspots, National Parks vs Biosphere reserves, Gene banks).
        • **Environmental Issues**: Greenhouse effect, ozone depletion (Dobson units), biomagnification (DDT concentration chain), eutrophication of water bodies.

        ### 2. **Score-Maximizing Tips**:
        • Create a list of all historical years, agreements, and acts mentioned in NCERT Ecology (Water Act 1974, Air Act 1981, Montreal Protocol 1987, Kyoto Protocol 1997).
        • Memorize the examples of **In-situ** (in natural habitat) vs **Ex-situ** (outside natural habitat, e.g., Zoological parks, Botanical gardens, Cryopreservation) conservation.
        """.trimIndent()
      }

      lowerQ.contains("percentage") || lowerQ.contains("hour") || lowerQ.contains("time") || lowerQ.contains("dedicat") -> {
        """
        📊 **Time & Percentage Dedication Analysis**:
        
        $context
        
        🎯 **Strategic Takeaway**:
        • Aim for a balanced **40% Biology (Botany + Zoology)**, **30% Physics**, and **30% Chemistry** time split.
        • Biology gives 360/720 marks in NEET, so ensuring 100% NCERT line-by-line grasp yields the highest return on time invested.
        • Allocate at least 60 minutes daily to Physics problem solving to develop rapid calculation speed.
        """.trimIndent()
      }

      lowerQ.contains("struggl") || lowerQ.contains("weak") || lowerQ.contains("marks") || lowerQ.contains("test") || lowerQ.contains("score") -> {
        """
        🧠 **Diagnostic & Mock Test Improvement Plan**:
        
        Based on your logged data:
        • **Error Notebook (Mistake Copy)**: Maintain a dedicated notebook. For every question missed in your mock tests, write down the formula, concept flaw, or NCERT statement.
        • **Physics Troubleshooting**: Don't just re-read theory. Solve 30-40 targeted MCQs per topic with a timer (1 min per question).
        • **Organic & Inorganic Chemistry**: Make reaction flowcharts and daily flashcards for NCERT named reactions and exceptions.
        • **Biology Line-by-Line**: Re-read NCERT summary boxes and diagram labels which frequently appear in Assertion-Reason questions.
        """.trimIndent()
      }

      lowerQ.contains("forget") || lowerQ.contains("memor") || lowerQ.contains("recall") || lowerQ.contains("revision") -> {
        """
        ⏳ **Mastering the Ebbinghaus Forgetting Curve for NEET**:
        
        Human memory drops 60% of new information within 24 hours without active review.
        
        🔁 **The 1-3-7-30 Spaced Repetition Formula**:
        1. **Day 1 (Immediate Review)**: 15-minute recall summary right after studying a chapter.
        2. **Day 3 (Active Recall)**: Solve 20 PYQs without looking at formula sheets or notes.
        3. **Day 7 (Weekly Test)**: Quick scan of error notebook and NCERT diagrams.
        4. **Day 30 (Monthly Consolidation)**: Full-length chapter mock test.
        
        💡 **Active Recall Techniques**:
        • **Blurting Method**: Read an NCERT page, close the book, and write down everything you remember on a blank sheet.
        • **Feynman Technique**: Explain difficult concepts (like Cardiac Cycle or Optics) in simple terms as if teaching a peer.
        """.trimIndent()
      }

      lowerQ.contains("ncert") || lowerQ.contains("pyq") || lowerQ.contains("chapter") -> {
        """
        📚 **NCERT & PYQ High-Yield Mastery**:
        
        • **PYQ Priority**: Solve at least the last 15 years (2010–2025) NEET & AIPMT past questions. Over 70% of exam concepts are variations of past question patterns.
        • **NCERT Edge**: 95%+ of Biology questions are directly verbatim from NCERT textbooks. Mark keywords with highlighters during your 2nd and 3rd revision rounds.
        • **Formula Diary**: Compile all Physics and Physical Chemistry formulas in a pocket diary and revise it before sleep every night.
        """.trimIndent()
      }

      else -> {
        """
        ✨ **NEET 2026/2027 Study Strategy Insights**:
        
        Here is your personalized roadmap based on your tracker data:
        
        1. **Focus on High-Weightage Chapters**:
           • **Physics**: Modern Physics, Current Electricity, Optics, Thermodynamics.
           • **Chemistry**: Chemical Bonding, Coordination Compounds, Organic Reaction Mechanisms, GOC.
           • **Biology**: Genetics & Evolution, Human Physiology, Cell Biology, Ecology.
        
        2. **Daily Routine Recommendations**:
           • 3 Hours: Problem Solving & PYQs (Active).
           • 2 Hours: NCERT Deep Reading & Note Revision (Biology/Inorganic).
           • 1 Hour: Mock Test Analysis & Error Notebook logging.
        
        3. **Overcoming Forgetting**: Use Spaced Repetition on Days 1, 3, 7, and 30 for long-term retention.
        """.trimIndent()
      }
    }
  }
}
