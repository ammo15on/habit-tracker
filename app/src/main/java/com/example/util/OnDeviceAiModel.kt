package com.example.util

import com.example.data.model.NeetChapter
import com.example.data.model.NeetTestScore
import com.example.ui.DaySummary
import com.example.ui.SubjectTimeBreakdown
import java.util.Locale

/**
 * Lightweight On-Device AI reasoning and computation model for NEET & study analytics.
 * Runs 100% locally on the device with zero network overhead, immediate response,
 * and deterministic mathematical diagnostics on real student logs.
 */
object OnDeviceAiModel {

  fun generateResponse(
    query: String,
    chapters: List<NeetChapter>,
    scores: List<NeetTestScore>,
    subjectTimes: List<SubjectTimeBreakdown>,
    days: List<DaySummary>
  ): String {
    val q = query.trim().lowercase(Locale.getDefault())

    // 1. Compute on-device statistical metrics
    val totalSeconds = subjectTimes.sumOf { it.seconds }
    val bioTimes = subjectTimes.filter { it.name.equals("Botany", true) || it.name.equals("Zoology", true) }
    val bioSecs = bioTimes.sumOf { it.seconds }
    val phySecs = subjectTimes.firstOrNull { it.name.equals("Physics", true) }?.seconds ?: 0L
    val chemSecs = subjectTimes.firstOrNull { it.name.equals("Chemistry", true) }?.seconds ?: 0L

    val bioPct = if (totalSeconds > 0) (bioSecs * 100f) / totalSeconds else 0f
    val phyPct = if (totalSeconds > 0) (phySecs * 100f) / totalSeconds else 0f
    val chemPct = if (totalSeconds > 0) (chemSecs * 100f) / totalSeconds else 0f

    val totalChapters = chapters.size
    val completedChapters = chapters.count { it.isCompleted }
    val pyqDoneChapters = chapters.count { it.isPyqDone }
    val revisionDoneChapters = chapters.count { it.isRevisionDone }
    val chapterPct = if (totalChapters > 0) (completedChapters * 100) / totalChapters else 0

    val latestScore = scores.maxByOrNull { it.date }
    val avgScore = if (scores.isNotEmpty()) scores.map { it.totalScore }.average().toInt() else 0
    val maxScore = if (scores.isNotEmpty()) scores.maxOf { it.totalScore } else 0

    val bestDays = days.count { it.isBest }
    val avgDays = days.count { it.isAverage }
    val worstDays = days.count { it.isWorst }

    val sb = StringBuilder()
    sb.append("⚡ **On-Device AI Engine (Local Computation)**\n\n")

    when {
      // Time / Hours / Percentage Dedication Analysis
      q.contains("time") || q.contains("hour") || q.contains("percentage") || q.contains("dedicat") || q.contains("split") || q.contains("ratio") -> {
        sb.append("📊 **Computed Subject Allocation & Balance Audit**:\n")
        sb.append("• **Total Logged Study Time**: ${DateUtils.formatTime(totalSeconds)}\n")
        sb.append("• **Biology (Botany + Zoology)**: ${String.format(Locale.getDefault(), "%.1f", bioPct)}% (${DateUtils.formatTime(bioSecs)}) [Target: 40%]\n")
        sb.append("• **Physics**: ${String.format(Locale.getDefault(), "%.1f", phyPct)}% (${DateUtils.formatTime(phySecs)}) [Target: 30%]\n")
        sb.append("• **Chemistry**: ${String.format(Locale.getDefault(), "%.1f", chemPct)}% (${DateUtils.formatTime(chemSecs)}) [Target: 30%]\n\n")

        sb.append("🎯 **On-Device Diagnostic Verdict**:\n")
        if (bioPct < 35f && totalSeconds > 0) {
          sb.append("⚠️ **Bio Deficit**: Biology carries 50% of the entire NEET paper (360/720 marks). Your current allocation (${String.format(Locale.getDefault(), "%.0f", bioPct)}%) is below the optimal 40% threshold. Boost NCERT active recall by 45 mins daily.\n")
        } else if (phyPct < 25f && totalSeconds > 0) {
          sb.append("⚠️ **Physics Risk**: Physics requires regular numeric muscle memory. At ${String.format(Locale.getDefault(), "%.0f", phyPct)}%, calculation inertia may set in. Dedicate at least 1 hour daily to 30 timed numericals.\n")
        } else {
          sb.append("✅ **Solid Distribution**: Your time dedication closely adheres to the NEET golden ratio (40:30:30). Maintain this rhythm across week days.\n")
        }
      }

      // Mock Test Scores / Marks / Weak Areas
      q.contains("score") || q.contains("mark") || q.contains("test") || q.contains("mock") || q.contains("struggl") || q.contains("weak") -> {
        sb.append("📈 **Mock Test Diagnostics & Gap Analysis**:\n")
        if (scores.isEmpty()) {
          sb.append("• No mock tests logged in your tracker yet. Log your first test in the **NEET** tab to enable granular scoring analysis!\n\n")
          sb.append("💡 **Immediate Baseline Goal**: Aim for **550+** on your first baseline full syllabus mock test (Phy: 120+, Chem: 130+, Bio: 300+).\n")
        } else {
          val latest = latestScore!!
          sb.append("• **Latest Mock Score**: ${latest.totalScore}/720 (Physics: ${latest.physicsScore}/180, Chemistry: ${latest.chemistryScore}/180, Botany: ${latest.botanyScore}/180, Zoology: ${latest.zoologyScore}/180)\n")
          sb.append("• **Average Score**: $avgScore/720 | **High Score**: $maxScore/720\n\n")

          val target = 650
          val deficit = (target - latest.totalScore).coerceAtLeast(0)
          sb.append("🎯 **Path to 650+ Target** (Deficit: **+$deficit marks**):\n")
          val subScores = listOf(
            "Physics" to (latest.physicsScore to 150),
            "Chemistry" to (latest.chemistryScore to 160),
            "Biology" to ((latest.botanyScore + latest.zoologyScore) to 340)
          )
          for ((subName, scorePair) in subScores) {
            val (current, goal) = scorePair
            val gap = goal - current
            if (gap > 0) {
              sb.append("• **$subName**: Current $current -> Goal $goal (+$gap marks needed, approx. ${(gap + 3) / 4} more correct MCQs)\n")
            } else {
              sb.append("• **$subName**: Strong zone ($current marks)! Focus on error-free accuracy.\n")
            }
          }
          sb.append("\n💡 **Action Plan**: Every incorrect question in your latest test cost you 5 marks (+4 lost and -1 penalty). Create a dedicated **Mistake Notebook** and review all incorrect questions within 24 hours of each test.")
        }
      }

      // NCERT / Chapters / PYQ
      q.contains("chapter") || q.contains("ncert") || q.contains("pyq") || q.contains("finish") || q.contains("syllabus") -> {
        sb.append("📚 **NEET Syllabus & Mastery Telemetry**:\n")
        sb.append("• **Total Chapters Tracked**: $totalChapters\n")
        sb.append("• **Completed Chapters**: $completedChapters / $totalChapters ($chapterPct%)\n")
        sb.append("• **PYQs Solved Chapters**: $pyqDoneChapters / $totalChapters (${if (totalChapters > 0) (pyqDoneChapters * 100) / totalChapters else 0}%)\n")
        sb.append("• **NCERT Revisions Done**: $revisionDoneChapters / $totalChapters (${if (totalChapters > 0) (revisionDoneChapters * 100) / totalChapters else 0}%)\n\n")

        val unrevised = chapters.filter { it.isCompleted && !it.isRevisionDone }
        val pendingPyq = chapters.filter { it.isCompleted && !it.isPyqDone }
        val incomplete = chapters.filter { !it.isCompleted }

        if (pendingPyq.isNotEmpty()) {
          sb.append("⚠️ **PYQ Gap Alert**: ${pendingPyq.size} completed chapters still lack 15-year past questions (e.g. \"${pendingPyq.first().name}\"). Concepts repeat in 70%+ of NEET questions. Make PYQ solving your top priority!\n\n")
        }
        if (incomplete.isNotEmpty()) {
          val nextUp = incomplete.take(3).map { "${it.name} (${it.subject})" }
          sb.append("🚀 **Next High-Priority Chapters to Complete**:\n")
          nextUp.forEach { sb.append("• $it\n") }
        }
      }

      // Forgetting Curve / Memory / Active Recall / Spaced Repetition
      q.contains("forget") || q.contains("memor") || q.contains("recall") || q.contains("revision") || q.contains("spaced") || q.contains("curve") -> {
        sb.append("🧠 **Scientific Forgetting Curve Mastery (Ebbinghaus Protocol)**:\n")
        sb.append("Without structured recall, retention plunges to ~33% within 6 days.\n\n")
        sb.append("🔁 **The 1-3-7-30 Spaced Repetition Formula**:\n")
        sb.append("1. **Day 1 (Immediate Blurting)**: Right after finishing a chapter, close notes and blurt key formulas and diagrams on blank paper (15 mins).\n")
        sb.append("2. **Day 3 (Targeted PYQs)**: Solve 20 questions without notes. Tests retrieval cues.\n")
        sb.append("3. **Day 7 (Cross-Topic Test)**: Combine with 2 other chapters for interleaved practice.\n")
        sb.append("4. **Day 30 (Full Recall)**: Rapid scan of your Mistake Copy & NCERT summary tables.\n\n")
        sb.append("💡 **NEET Pro Tip**: Convert all NCERT exceptions (Inorganic Chemistry & Plant Morphology) into spaced flashcards for night-time review.")
      }

      // Physics Specific
      q.contains("physic") || q.contains("formula") || q.contains("numerical") || q.contains("mechanic") || q.contains("optic") -> {
        sb.append("⚡ **Physics Acceleration Strategy**:\n")
        sb.append("Physics logged: **${DateUtils.formatTime(phySecs)}** (${String.format(Locale.getDefault(), "%.1f", phyPct)}% of total study time).\n\n")
        sb.append("📌 **Core 3-Step Problem Solving Rule**:\n")
        sb.append("1. **Extract Givens & Dimensions**: Always write SI units first to immediately spot potential unit conversion traps.\n")
        sb.append("2. **High-Yield Priority Chapters**:\n")
        sb.append("   • Modern Physics (Photoelectric, Dual Nature, Atoms, Nuclei, Semiconductors) -> ~12-16 easy marks.\n")
        sb.append("   • Current Electricity & Optics -> High concept repetition in NEET.\n")
        sb.append("   • Thermodynamics & Thermal Properties -> Directly overlaps with Physical Chemistry!\n")
        sb.append("3. **Speed Drill**: Solve 25 MCQs with a stopwatch set to 25 minutes. Do not spend >1.5 minutes on a single question in the first pass.")
      }

      // Chemistry Specific
      q.contains("chem") || q.contains("organic") || q.contains("inorganic") || q.contains("reaction") || q.contains("mechanism") -> {
        sb.append("🧪 **Chemistry 160+ Roadmap**:\n")
        sb.append("Chemistry logged: **${DateUtils.formatTime(chemSecs)}** (${String.format(Locale.getDefault(), "%.1f", chemPct)}% of total study time).\n\n")
        sb.append("📌 **Tri-Section Approach**:\n")
        sb.append("• **Organic Chemistry**: Focus on electrophilic/nucleophilic attack mechanisms, GOC stability (carbocations, resonance), and named reactions.\n")
        sb.append("• **Inorganic Chemistry**: 100% NCERT line-by-line. Coordination compounds, d & f-block, and p-block trends.\n")
        sb.append("• **Physical Chemistry**: Keep a one-page formula sheet for Chemical Equilibrium, Electrochemistry, and Solutions. Practice calculating square roots and logarithms mentally.")
      }

      // Biology Specific
      q.contains("bio") || q.contains("botany") || q.contains("zoology") || q.contains("plant") || q.contains("human") || q.contains("genetics") -> {
        sb.append("🌿 **Biology 360/360 Perfection Strategy**:\n")
        sb.append("Biology logged: **${DateUtils.formatTime(bioSecs)}** (${String.format(Locale.getDefault(), "%.1f", bioPct)}% of total study time).\n\n")
        sb.append("📌 **Key Directives for Full Marks**:\n")
        sb.append("1. **Diagram Subtitles**: NEET often tests the tiny text written *under* NCERT diagrams and scientist intro pages (e.g. Ernst Mayr, Ramachandran).\n")
        sb.append("2. **Assertion-Reason Mastery**: Look for words like *because*, *due to*, *hence* in NCERT paragraphs.\n")
        sb.append("3. **Genetics & Human Physiology**: These two units alone constitute 30-35% of all Biology questions. Revise pedigrees and hormonal pathways weekly.")
      }

      // Default Comprehensive Diagnosis
      else -> {
        sb.append("📋 **Holistic NEET Readiness Summary**:\n\n")
        sb.append("• **Study Consistency**: $bestDays Best days, $avgDays Average days, $worstDays Hard days recorded.\n")
        sb.append("• **Total Active Hours**: ${DateUtils.formatTimeWords(totalSeconds)}\n")
        sb.append("• **Chapter Completion**: $completedChapters/$totalChapters completed ($chapterPct%)\n")
        if (latestScore != null) {
          sb.append("• **Current Mock Score**: ${latestScore.totalScore}/720\n")
        }
        sb.append("\n💡 **Top On-Device Recommended Next Action**:\n")
        val lowSubject = subjectTimes.filter { it.name != "Habits & Tasks" }.minByOrNull { it.seconds }
        if (lowSubject != null && lowSubject.seconds < 3600L) {
          sb.append("• Spend at least 90 minutes on **${lowSubject.name}** today to balance subject coverage.\n")
        } else {
          sb.append("• Review 30 PYQs from your most recent completed chapter and update your tally in the NEET tab!\n")
        }
        sb.append("• Maintain your evening study streak to lock in another 'Best' day rating 😊.")
      }
    }

    return sb.toString()
  }
}
