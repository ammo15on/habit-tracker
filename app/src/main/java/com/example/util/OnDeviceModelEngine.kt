package com.example.util

import com.example.data.model.DayRating
import com.example.data.model.HabitTask
import com.example.data.model.HabitTaskLog
import com.example.data.model.NeetChapter
import com.example.data.model.NeetTallyCounter
import com.example.data.model.NeetTestScore
import com.example.data.model.PlannedTask
import com.example.data.model.RatingType
import com.example.ui.SubjectTimeBreakdown
import java.util.Locale

object OnDeviceModelEngine {

  fun generateResponse(
    question: String,
    tasks: List<HabitTask> = emptyList(),
    logs: List<HabitTaskLog> = emptyList(),
    ratings: List<DayRating> = emptyList(),
    plannedTasks: List<PlannedTask> = emptyList(),
    chapters: List<NeetChapter> = emptyList(),
    scores: List<NeetTestScore> = emptyList(),
    tallies: List<NeetTallyCounter> = emptyList(),
    subjectTimes: List<SubjectTimeBreakdown> = emptyList()
  ): String {
    val cleanQ = question.trim().lowercase(Locale.ROOT)

    val today = DateUtils.today()
    val past7Days = (0..6).map { DateUtils.offsetDate(today, -it) }.toSet()
    val past30Days = (0..29).map { DateUtils.offsetDate(today, -it) }.toSet()

    val logs7 = logs.filter { it.date in past7Days }
    val logs30 = logs.filter { it.date in past30Days }

    val totalStudySecs7 = logs7.sumOf { it.timeSpentSeconds }
    val totalHours7 = totalStudySecs7 / 3600.0
    val completedTasksCount7 = logs7.count { it.isCompleted }

    val totalStudySecs30 = logs30.sumOf { it.timeSpentSeconds }
    val totalHours30 = totalStudySecs30 / 3600.0
    val completedTasksCount30 = logs30.count { it.isCompleted }

    val activeTasksCount = tasks.size
    val missedTasks7 = tasks.filter { t -> logs7.none { it.taskId == t.id && it.isCompleted } }

    val totalChapters = chapters.size
    val completedChapters = chapters.count { it.isCompleted }
    val pyqChapters = chapters.count { it.isPyqDone }
    val ncertChapters = chapters.count { it.isRevisionDone }
    val exerciseChapters = chapters.count { it.isExerciseDone }
    val arChapters = chapters.count { it.isArDone }

    val avgScore = if (scores.isNotEmpty()) scores.map { it.totalScore }.average().toInt() else 0
    val latestScore = scores.maxByOrNull { it.date }?.totalScore ?: 0

    val physicsTime = subjectTimes.find { it.name.equals("Physics", ignoreCase = true) }?.seconds ?: 0L
    val chemistryTime = subjectTimes.find { it.name.equals("Chemistry", ignoreCase = true) }?.seconds ?: 0L
    val botanyTime = subjectTimes.find { it.name.equals("Botany", ignoreCase = true) }?.seconds ?: 0L
    val zoologyTime = subjectTimes.find { it.name.equals("Zoology", ignoreCase = true) }?.seconds ?: 0L
    val totalSubSecs = physicsTime + chemistryTime + botanyTime + zoologyTime

    val bioPct = if (totalSubSecs > 0) ((botanyTime + zoologyTime) * 100f / totalSubSecs) else 0f
    val phyPct = if (totalSubSecs > 0) (physicsTime * 100f / totalSubSecs) else 0f
    val chemPct = if (totalSubSecs > 0) (chemistryTime * 100f / totalSubSecs) else 0f

    return when {
      // 1. ACTIVE RECALL & ERROR NOTEBOOK SYSTEM
      cleanQ.contains("error notebook") || cleanQ.contains("active recall") || cleanQ.contains("mistake copy") || cleanQ.contains("mock test system") -> {
        """
        📓 **Active Recall & Error Notebook System for Mock Tests**:

        1. **4-Quadrant Error Notebook Structure**:
           • **Quadrant 1 (Concept Flaw)**: Write the core NCERT formula or fact you didn't know.
           • **Quadrant 2 (Calculation / Silly Mistake)**: Redo the numerical math step in red pen.
           • **Quadrant 3 (Question Misread)**: Underline the missed trap word (*"Incorrect"*, *"Except"*, *"Not true"*).
           • **Quadrant 4 (Over-attempting / Guesswork)**: Note down why you guessed and commit to leaving it blank next time.

        2. **24-Hour Review Rule**:
           • Log every single incorrect and unattempted question within 24 hours of completing a mock test.
           • Re-attempt the entire error notebook without solutions on **Day 3** and **Day 7**.

        3. **Active Recall Protocol (3 Core Techniques)**:
           • **Blurting Method**: Close your notes and write out all formulas/diagrams from memory on a blank sheet.
           • **Feynman Technique**: Explain a complex concept (e.g. Cardiac Cycle, Lens Formula) out loud as if teaching a beginner.
           • **Flashcard Testing**: Test formula cards before sleep without looking at the reverse side.
        """.trimIndent()
      }

      // 2. WEEKLY / MONTHLY SUMMARY & TASKS AUDIT
      cleanQ.contains("week") || cleanQ.contains("month") || cleanQ.contains("summary") || cleanQ.contains("overview") -> {
        val ratings7 = ratings.filter { it.date in past7Days }
        val best7 = ratings7.count { it.ratingType == RatingType.BEST }
        val avg7 = ratings7.count { it.ratingType == RatingType.AVERAGE }
        val worst7 = ratings7.count { it.ratingType == RatingType.WORST }

        val pendingPlanned = plannedTasks.filter { !it.isCompleted }.take(4)

        """
        📊 **Performance & Tasks Audit (Local AI Engine)**:

        📅 **7-Day Weekly Metrics**:
        • **Total Study Time**: ${String.format(Locale.getDefault(), "%.1f", totalHours7)} hours (${DateUtils.formatTime(totalStudySecs7)})
        • **Completed Task Sessions**: $completedTasksCount7 across $activeTasksCount active habits
        • **Consistency**: $best7 Days Rated Best (😊), $avg7 Average (😐), $worst7 Worst (😞)

        📅 **30-Day Monthly Metrics**:
        • **Total Study Time**: ${String.format(Locale.getDefault(), "%.1f", totalHours30)} hours (${DateUtils.formatTime(totalStudySecs30)})
        • **Completed Tasks**: $completedTasksCount30 completions

        ${if (missedTasks7.isNotEmpty()) "⚠️ **Neglected Tasks This Week**:\n" + missedTasks7.joinToString("\n") { "• **${it.name}** (0 completions in past 7 days)" } else "✅ **Task Adherence**: All active habits were logged this week!"}

        ${if (pendingPlanned.isNotEmpty()) "📌 **Upcoming Scheduled Tasks**:\n" + pendingPlanned.joinToString("\n") { "• [${it.date}] ${it.title} (${it.targetTimeMinutes}m)" } else ""}

        📚 **NEET Chapters Snapshot**:
        • Completed: $completedChapters/$totalChapters | NCERT Read: $ncertChapters | PYQ: $pyqChapters | Exercise: $exerciseChapters | A&R: $arChapters
        """.trimIndent()
      }

      // 3. WHAT AM I MISSING & WHAT SHOULD I WORK ON
      cleanQ.contains("missing") || cleanQ.contains("miss") || cleanQ.contains("what should i work on") || cleanQ.contains("suggest") || cleanQ.contains("neglect") || cleanQ.contains("weak") -> {
        val unpracticedChapters = chapters.filter { it.isCompleted && (!it.isPyqDone || !it.isExerciseDone || !it.isArDone) }
        val sampleUnpracticed = unpracticedChapters.take(5)

        val weakestSubject = if (scores.isNotEmpty()) {
          val avgP = scores.map { it.physicsScore }.average().toInt()
          val avgC = scores.map { it.chemistryScore }.average().toInt()
          val avgB = scores.map { (it.botanyScore + it.zoologyScore) / 2 }.average().toInt()
          val list = listOf("Physics" to avgP, "Chemistry" to avgC, "Biology" to avgB)
          list.minByOrNull { it.second }
        } else null

        """
        🔍 **Targeted Diagnostics: What You Are Missing**:

        1. **Incomplete Practice on Completed Chapters**:
        ${if (sampleUnpracticed.isNotEmpty()) {
          sampleUnpracticed.joinToString("\n") { ch ->
            val pendingParts = mutableListOf<String>()
            if (!ch.isPyqDone) pendingParts.add("PYQ")
            if (!ch.isExerciseDone) pendingParts.add("Exercise")
            if (!ch.isArDone) pendingParts.add("A&R")
            "• **${ch.name}** (${ch.subject}): Missing ${pendingParts.joinToString(", ")}"
          }
        } else {
          "• All completed chapters currently have PYQ, Exercise, and A&R marked!"
        }}

        2. **Tasks & Habit Inactivity**:
        ${if (missedTasks7.isNotEmpty()) {
          "• You have not logged ${missedTasks7.size} tasks in the last 7 days: ${missedTasks7.take(3).joinToString(", ") { it.name }}."
        } else {
          "• Daily habit tracking is active and consistent."
        }}

        3. **Mock Test Diagnostic**:
        ${if (weakestSubject != null) {
          "• Lowest scoring subject is **${weakestSubject.first}** (avg: ${weakestSubject.second}/180). Dedicate your next study block here."
        } else {
          "• Log more mock tests under the NEET tab to unlock automated score diagnosis."
        }}

        🎯 **Immediate Action Checklist**:
        1. Pick **${sampleUnpracticed.firstOrNull()?.name ?: "your next incomplete chapter"}** and solve 30 PYQs + 15 Assertion-Reason questions.
        2. Allocate at least 60 uninterrupted minutes to ${weakestSubject?.first ?: "Physics numericals"}.
        3. Clear pending error notebook entries before starting new topics.
        """.trimIndent()
      }

      // 4. NEET DELETED TOPICS & NMC/NTA SYLLABUS
      cleanQ.contains("delete") || cleanQ.contains("syllabus") || cleanQ.contains("nta") || cleanQ.contains("nmc") || cleanQ.contains("update") -> {
        """
        📋 **NEET 2026/2027 Officially Deleted Topics & Updated Syllabus**:

        ⚡ **PHYSICS**:
        • **Deleted**:
          - Rolling motion detailed dynamics (pure rotation/translation kept)
          - Reynolds number, Heat engines & refrigerators
          - Free, damped oscillations & resonance
          - Doppler effect in acoustics
          - Van de Graaff generator, Colour coding of carbon resistors, Potentiometer (principle & applications)
          - Cyclotron, Earth's magnetic elements & magnetic hysteresis loops
          - Logic gates and transistor as amplifier/switch
        • **Retained & Added**:
          - Practical Physics: Vernier calliper, Screw gauge, Meter bridge, Simple pendulum, Ohm's law, Resonance tube, p-n junction diode characteristics.

        🧪 **CHEMISTRY**:
        • **Completely Deleted Chapters**:
          - Solid State, Surface Chemistry, Metallurgy (General Principles of Isolation)
          - Hydrogen, s-Block Elements (Alkali & Alkaline Earth Metals)
          - Polymers, Environmental Chemistry, Chemistry in Everyday Life, States of Matter (Gases & Liquids)
        • **Retained & Added**:
          - Principles Related to Practical Chemistry: Systematic qualitative analysis (Cations & Anions detection), Functional groups detection, Enthalpy experiments.

        🌿 **BIOLOGY**:
        • **Deleted Chapters**:
          - Transport in Plants, Mineral Nutrition, Digestion and Absorption
          - Reproduction in Organisms, Strategies for Enhancement in Food Production
        • **Added/Modified Families & Organisms**:
          - Plant families added to Morphology: Malvaceae, Cruciferae, Leguminosae, Compositae, Gramineae / Poaceae
          - Frog morphology & anatomy included; Earthworm excluded
          - Dengue and Chikungunya added to Human Health & Disease
        """.trimIndent()
      }

      // 5. ASSERTION & REASON (A&R) STRATEGY
      cleanQ.contains("assertion") || cleanQ.contains("a&r") || cleanQ.contains("reason") -> {
        """
        🎯 **Assertion & Reason (A&R) 3-Step Solving Formula**:

        1. **Step 1 (Validate Assertion)**: Read the Assertion independently. Is it Factually True or False according to NCERT?
        2. **Step 2 (Validate Reason)**: Read the Reason independently. Is it Factually True or False according to NCERT?
           • *If either Assertion or Reason is False*, the answer is immediately determined (Option 3 or Option 4).
        3. **Step 3 (The 'BECAUSE' Test)**: If BOTH are True, join them:
           *"[Assertion statement] **BECAUSE** [Reason statement]."*
           • If Reason directly provides the scientific cause/mechanism of Assertion -> **Option 1 (Correct explanation)**.
           • If Reason is true but unrelated to why Assertion occurs -> **Option 2 (Not correct explanation)**.
        """.trimIndent()
      }

      // 6. NEGATIVE MARKING & 3-ROUND EXAM STRATEGY
      cleanQ.contains("negative") || cleanQ.contains("guess") || cleanQ.contains("time management") || cleanQ.contains("exam strategy") -> {
        """
        🛡️ **NEET Exam Execution & Negative Marking Elimination**:

        1. **3-Round Paper Attempting Protocol**:
           • **Round 1 (0–60 min)**: Speed run. Solve 100% direct NCERT Biology + straightforward Chemistry questions. Fill OMR immediately.
           • **Round 2 (60–140 min)**: Moderate Physics numericals & Organic mechanisms. Skip questions that take >2 mins to solve.
           • **Round 3 (140–180 min)**: Review marked 50-50 questions. Double-check calculation units and OMR bubble alignments.

        2. **Golden Rule of Negative Marking**:
           • If you cannot eliminate at least 2 options, **LEAVE IT BLANK**.
           • Leaving 1 uncertain question blank saves **+1 mark** from deduction.
        """.trimIndent()
      }

      // 7. PHYSICS STUDY STRATEGY
      cleanQ.contains("physics") || cleanQ.contains("numerical") || cleanQ.contains("formula") -> {
        """
        ⚡ **Physics 160+ Marks Roadmap for NEET**:

        1. **High-Yield Chapter Priority**:
           • **Tier 1 (Guaranteed 70+ marks)**: Modern Physics (Dual Nature, Atoms, Nuclei, Semiconductors), Current Electricity, Ray & Wave Optics.
           • **Tier 2 (Formula Heavy)**: Thermodynamics & KTG, Gravitation, Work Energy Power.
           • **Tier 3 (Concept Heavy)**: Electrostatics, Magnetism, Mechanics & Rotational Motion.

        2. **Numerical Problem-Solving Protocol**:
           • Step 1: Write down Given values with SI units.
           • Step 2: Identify the governing formula before writing calculations.
           • Step 3: Eliminate extreme option orders of magnitude before full multiplication.
           • Solve at least **35-40 timed numericals daily**.
        """.trimIndent()
      }

      // 8. CHEMISTRY STUDY STRATEGY
      cleanQ.contains("chemistry") || cleanQ.contains("organic") || cleanQ.contains("inorganic") -> {
        """
        🧪 **Chemistry 170+ Marks Roadmap for NEET**:

        1. **Inorganic Chemistry (NCERT Pure Line-by-Line)**:
           • Periodic Table trends, Chemical Bonding (MOT, Hybridization, VSEPR), Coordination Compounds (CFT, Isomerism), d & f block.
           • Memorize all NCERT textbook tables and exception footnotes.

        2. **Organic Chemistry (Mechanism Mastery)**:
           • Master GOC first: Inductive, Mesomeric, Hyperconjugation, Carbocation stability, Acidity/Basicity orders.
           • Named reactions master sheet: Aldol, Cannizzaro, Reimer-Tiemann, Kolbe, Hoffmann Bromamide.

        3. **Physical Chemistry (Formula & Practice)**:
           • Solutions, Electrochemistry (Nernst eq), Chemical Kinetics, Equilibrium.
           • Maintain a 2-page formula summary per chapter.
        """.trimIndent()
      }

      // 9. BIOLOGY STUDY STRATEGY
      cleanQ.contains("biology") || cleanQ.contains("botany") || cleanQ.contains("zoology") || cleanQ.contains("ncert") -> {
        """
        🌿 **Biology 350+/360 Blueprint for NEET**:

        1. **NCERT 3-Pass Rule**:
           • **Pass 1**: Active pencil reading. Highlight new scientific terms.
           • **Pass 2**: Study all diagram labels, summary boxes, and introductory scientist biographies.
           • **Pass 3**: Line-by-line self questioning (*"Why is this statement true?"*).

        2. **High-Weightage Chapters**:
           • Genetics & Molecular Basis of Inheritance (12-15 questions).
           • Human Physiology (10-12 questions).
           • Biotechnology Principles & Applications (6-8 questions).
           • Cell Biology & Cell Cycle (6-8 questions).
           • Plant Morphology & New Families (Malvaceae, Cruciferae, Leguminosae, Compositae, Gramineae).
        """.trimIndent()
      }

      // 10. HOW TO STUDY SPECIFIC CHAPTERS & METHODOLOGY
      cleanQ.contains("how to study") || cleanQ.contains("correct way") || cleanQ.contains("approach") || cleanQ.contains("strategy") || cleanQ.contains("method") -> {
        """
        📖 **High-Yield 5-Step Chapter Mastery Protocol**:

        1. **First NCERT Deep Read (Line-by-Line)**:
           - Read textbook actively. Highlight exceptions, footnotes, and summary paragraphs.
           - For Biology & Inorganic Chemistry, 95% of questions are verbatim NCERT lines.

        2. **In-Chapter & Back Exercises**:
           - Complete all in-text examples and end-of-chapter exercises.
           - Mark questions that took more than 2 minutes for later review.

        3. **15-Year PYQs (2010–2025)**:
           - Solve at least 40-50 past questions under timed conditions (1 min per question).
           - Do not look at solutions before attempting twice.

        4. **Assertion & Reason (A&R) Practice**:
           - Practice 20 A&R questions per chapter to eliminate negative marking confusion.
           - Rule: If both statements are true, ask *"Does Reason directly explain WHY Assertion happens?"*

        5. **The 1-3-7-30 Spaced Repetition Formula**:
           - Day 1: 10-minute active recall summary after finishing.
           - Day 3: Solve 15 mixed MCQs without books.
           - Day 7: Error Notebook scan.
           - Day 30: Timed sectional test.
        """.trimIndent()
      }

      // 11. STUDY HOURS & TIME DEDICATION
      cleanQ.contains("time") || cleanQ.contains("hour") || cleanQ.contains("percentage") || cleanQ.contains("dedicat") || cleanQ.contains("balance") -> {
        val phyHrs = String.format(Locale.getDefault(), "%.1fh", physicsTime / 3600.0)
        val chemHrs = String.format(Locale.getDefault(), "%.1fh", chemistryTime / 3600.0)
        val bioHrs = String.format(Locale.getDefault(), "%.1fh", (botanyTime + zoologyTime) / 3600.0)

        """
        ⏱️ **Subject Time Dedication & Balance Audit**:

        • 🌿 **Biology (Botany + Zoology)**: $bioHrs (${String.format(Locale.getDefault(), "%.1f", bioPct)}%)
        • ⚡ **Physics**: $phyHrs (${String.format(Locale.getDefault(), "%.1f", phyPct)}%)
        • 🧪 **Chemistry**: $chemHrs (${String.format(Locale.getDefault(), "%.1f", chemPct)}%)

        🎯 **Allocation Guidance**:
        • **Target**: 40-50% Biology (360 marks), 25-30% Physics (180 marks), 25-30% Chemistry (180 marks).
        • ${if (phyPct < 20f && totalSubSecs > 0) "⚠️ Increase Physics daily numerical practice to avoid score lag." else "✅ Physics study time is on track."}
        • ${if (chemPct < 20f && totalSubSecs > 0) "⚠️ Chemistry requires balanced attention between Organic mechanisms and Physical numericals." else "✅ Chemistry allocation is balanced."}
        """.trimIndent()
      }

      // 12. MOCK TESTS & MARKS
      cleanQ.contains("mock") || cleanQ.contains("test") || cleanQ.contains("score") || cleanQ.contains("marks") -> {
        if (scores.isEmpty()) {
          """
          📊 **Mock Test Performance**:

          No test scores logged yet. Add your recent test marks in the **NEET -> Test Marks** tab to view automated diagnostics and subject-wise percentiles.

          🛠️ **Mock Test Action Plan**:
          • Maintain an **Error Notebook** for every test question missed.
          • Target: 1 Full Mock test every Sunday under strict 2:00 PM to 5:20 PM exam conditions.
          """.trimIndent()
        } else {
          val latest = scores.maxByOrNull { it.date }!!
          val phyAvg = scores.map { it.physicsScore }.average().toInt()
          val chemAvg = scores.map { it.chemistryScore }.average().toInt()
          val bioAvg = scores.map { it.botanyScore + it.zoologyScore }.average().toInt()

          """
          📈 **Mock Test Score Diagnostic**:

          • **Tests Logged**: ${scores.size}
          • **Latest Test**: '${latest.testName}' on ${latest.date} -> **${latest.totalScore}/720**
          • **Score Averages**: Total: **$avgScore/720** | Physics: $phyAvg/180 | Chem: $chemAvg/180 | Bio: $bioAvg/360

          🛠️ **Mistake Recovery**:
          • Maintain an **Error Notebook**. Write the exact concept error for every question missed.
          • Stop random guessing: leaving an uncertain question blank saves 1 negative mark.
          """.trimIndent()
        }
      }

      // 13. DEFAULT DIRECT RESPONSE
      else -> {
        """
        🎯 **NEET Preparation & Analytics Brief**:

        • **Chapters Status**: $completedChapters/$totalChapters Completed | $ncertChapters NCERT Read | $pyqChapters PYQ | $exerciseChapters Exercise | $arChapters A&R
        • **Weekly Study**: ${String.format(Locale.getDefault(), "%.1f", totalHours7)} hours logged in past 7 days across $completedTasksCount7 task sessions
        • **Average Mock Test**: ${if (avgScore > 0) "$avgScore/720" else "No tests logged"}

        💡 **Ask specifically for**:
        • *"Active recall and error notebook system for mock tests"*
        • *"Weekly summary of my tasks and hours"*
        • *"What am I missing or neglecting?"*
        • *"NEET deleted topics and syllabus update"*
        • *"Assertion & Reason (A&R) question solving trick"*
        • *"How to study Organic Chemistry / Modern Physics"*
        """.trimIndent()
      }
    }
  }
}
