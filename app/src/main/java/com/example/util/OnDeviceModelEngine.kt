package com.example.util

import com.example.data.model.NeetChapter
import com.example.data.model.NeetTestScore
import com.example.ui.SubjectTimeBreakdown
import java.util.Locale

object OnDeviceModelEngine {

  fun generateResponse(
    question: String,
    chapters: List<NeetChapter>,
    scores: List<NeetTestScore>,
    subjectTimes: List<SubjectTimeBreakdown>
  ): String {
    val cleanQ = question.trim().lowercase(Locale.ROOT)

    // Analyze data locally for personalized response
    val totalChapters = chapters.size
    val completedChapters = chapters.count { it.isCompleted }
    val pyqChapters = chapters.count { it.isPyqDone }
    val revisedChapters = chapters.count { it.isRevisionDone }
    val completionPercent = if (totalChapters > 0) (completedChapters * 100) / totalChapters else 0

    val avgScore = if (scores.isNotEmpty()) scores.map { it.totalScore }.average().toInt() else 0
    val latestScore = if (scores.isNotEmpty()) scores.maxByOrNull { it.date }?.totalScore ?: 0 else 0

    val physicsTime = subjectTimes.find { it.name == "Physics" }?.seconds ?: 0L
    val chemistryTime = subjectTimes.find { it.name == "Chemistry" }?.seconds ?: 0L
    val botanyTime = subjectTimes.find { it.name == "Botany" }?.seconds ?: 0L
    val zoologyTime = subjectTimes.find { it.name == "Zoology" }?.seconds ?: 0L
    val totalStudySecs = physicsTime + chemistryTime + botanyTime + zoologyTime

    val bioPercent = if (totalStudySecs > 0) ((botanyTime + zoologyTime) * 100f / totalStudySecs) else 0f
    val phyPercent = if (totalStudySecs > 0) (physicsTime * 100f / totalStudySecs) else 0f
    val chemPercent = if (totalStudySecs > 0) (chemistryTime * 100f / totalStudySecs) else 0f

    return when {
      // 1. GREETING
      cleanQ.contains("hi") || cleanQ.contains("hello") || cleanQ.contains("hey") || cleanQ.contains("greet") -> {
        """
        ⚡ **On-Device AI NEET Mentor v1.0** (Offline Mode)
        
        Hello! I am your locally-running AI Coach. I process all your study records, NCERT progress, and test marks directly on your device with zero latency!
        
        Here is a quick snapshot of your local diagnostics:
        • **NCERT Chapter Completion**: $completedChapters/$totalChapters ($completionPercent%)
        • **Average Mock Test Score**: ${if (avgScore > 0) "$avgScore/720" else "No tests logged"}
        • **Spaced Revision Progress**: $revisedChapters chapters revised
        
        Ask me anything about your weak subjects, study schedule, or NCERT memory tips!
        """.trimIndent()
      }

      // 2. TIMING & HOURS DEDICATION
      cleanQ.contains("percentage") || cleanQ.contains("hour") || cleanQ.contains("time") || cleanQ.contains("dedicat") || cleanQ.contains("balance") -> {
        val phyHrs = String.format(Locale.getDefault(), "%.1fh", physicsTime / 3600.0)
        val chemHrs = String.format(Locale.getDefault(), "%.1fh", chemistryTime / 3600.0)
        val bioHrs = String.format(Locale.getDefault(), "%.1fh", (botanyTime + zoologyTime) / 3600.0)

        """
        💻 **Local On-Device Study Time Audit**:
        
        Based on your local tracker logs, here is your exact subject-wise dedication:
        • 🌿 **Biology (Botany + Zoology)**: $bioHrs (${String.format(Locale.getDefault(), "%.1f", bioPercent)}% of total study)
        • ⚡ **Physics**: $phyHrs (${String.format(Locale.getDefault(), "%.1f", phyPercent)}% of total study)
        • 🧪 **Chemistry**: $chemHrs (${String.format(Locale.getDefault(), "%.1f", chemPercent)}% of total study)
        
        🎯 **Local AI Optimization Rules**:
        1. **The 50-25-25 Target**: Ideally, aim for 40-50% Biology and 25-30% each for Physics and Chemistry.
        2. ${if (phyPercent < 20f) "⚠️ **Physics Alert**: Your Physics dedication is low (${String.format(Locale.getDefault(), "%.1f", phyPercent)}%). Physics requires daily numerical practice. Set a recurring tracker task for 45 minutes of Physics formulas." else "✅ **Physics Dedication**: Your Physics time is solid! Keep practicing 30 formulas daily."}
        3. ${if (bioPercent > 60f) "⚠️ **Biology Bias**: You are spending a high percentage of time on Biology (${String.format(Locale.getDefault(), "%.1f", bioPercent)}%). While Biology is 50% of the NEET exam weight, don't neglect Physics/Chemistry." else "✅ **Biology Dedication**: Excellent balance on Biology. Ensure NCERT line-by-line reading."}
        """.trimIndent()
      }

      // 3. MOCK TEST MARKS & SCORES
      cleanQ.contains("struggl") || cleanQ.contains("weak") || cleanQ.contains("marks") || cleanQ.contains("test") || cleanQ.contains("score") -> {
        if (scores.isEmpty()) {
          """
          📊 **Local Performance Diagnostic**:
          
          You haven't logged any Mock Test scores in the **NEET tab** yet!
          
          📝 **How to get personalized diagnostics**:
          1. Go to the **NEET** tab in this screen.
          2. Scroll to the **Mock Tests** section.
          3. Tap **Add Test Score** and enter your Physics, Chemistry, and Biology scores.
          
          Once logged, I will instantly analyze your score trends and pinpoint your weakest sections right here!
          """.trimIndent()
        } else {
          val latest = scores.maxByOrNull { it.date }!!
          val phyAvg = scores.map { it.physicsScore }.average().toInt()
          val chemAvg = scores.map { it.chemistryScore }.average().toInt()
          val botanyAvg = scores.map { it.botanyScore }.average().toInt()
          val zoologyAvg = scores.map { it.zoologyScore }.average().toInt()
          val bioAvg = botanyAvg + zoologyAvg

          val subjects = listOf("Physics" to phyAvg, "Chemistry" to chemAvg, "Biology" to bioAvg)
          val weakest = subjects.minByOrNull { it.second }!!

          """
          📈 **Local On-Device Mock Test Diagnostic**:
          
          I've analyzed your mock test history (${scores.size} tests logged).
          • **Latest Test**: '${latest.testName}' -> **${latest.totalScore}/720**
          • **Average Score**: **$avgScore/720** (Phy: $phyAvg/180, Chem: $chemAvg/180, Bio: $bioAvg/360)
          
          🔍 **Struggling Focus Area**:
          Your lowest average scoring section is **${weakest.first}** at **${weakest.second} marks**.
          
          🛠️ **Actionable Recovery Plan**:
          • **For ${weakest.first}**: Dedicate the first 90 minutes of your study session to this subject tomorrow. Focus on writing out wrong answers from your last mock test in an *Error Notebook*.
          • **Active Recall**: For incorrect Biology/Chemistry questions, immediately open NCERT and highlight the exact sentence. For Physics, re-derive the formula from basic principles.
          • **Avoid Negative Marking**: In your next test, do not guess questions where you cannot eliminate at least 2 options. Leaving a question blank is +0, but guessing incorrectly is -1!
          """.trimIndent()
        }
      }

      // 4. MEMORIZATION, FORGETTING, RECALL
      cleanQ.contains("forget") || cleanQ.contains("memor") || cleanQ.contains("recall") || cleanQ.contains("revision") || cleanQ.contains("spaced") || cleanQ.contains("ebbinghaus") -> {
        """
        🧠 **Local Brain Science & Active Recall Module**:
        
        To prevent forgetting NCERT chapters like *Plant Kingdom* or *GOC Mechanisms*, we must tackle the Ebbinghaus Forgetting Curve!
        
        📅 **On-Device Spaced Repetition Protocol**:
        • **Immediate (Day 1)**: Close your notes and write a 5-line summary of what you studied.
        • **Active (Day 3)**: Solve 15-20 random MCQs/PYQs on this topic without opening the textbook.
        • **Consolidation (Day 7)**: Revise your *Error Notebook* containing only your past mistakes.
        • **Retention (Day 30)**: Attempt a timed 45-minute subject mock test.
        
        💡 **Active Recall Techniques to try today**:
        1. **Blurting**: Read an NCERT section for 10 minutes. Close the book, and write down every single point, key term, and exception you can remember in red pen.
        2. **Feynman Method**: Explain *Kinematics Equations* or *Cardiac Cycle* aloud to an imaginary student. If you stutter or get stuck, that is your exact weak area!
        """.trimIndent()
      }

      // 5. NCERT & PYQ MASTER PLAN
      cleanQ.contains("ncert") || cleanQ.contains("pyq") || cleanQ.contains("chapter") -> {
        val incompleteList = chapters.filter { !it.isCompleted }.take(3).map { "• ${it.name} (${it.subject})" }

        """
        📚 **NCERT & PYQ Completion Diagnostics**:
        
        Your current syllabus standing:
        • **Completed chapters**: $completedChapters / $totalChapters
        • **Past Year Questions (PYQs) done**: $pyqChapters chapters
        • **NCERT revisions done**: $revisedChapters chapters
        
        ${if (incompleteList.isNotEmpty()) "🔍 **Next chapters to prioritize immediately**:\n" + incompleteList.joinToString("\n") else "🎉 **Amazing work! You have completed all high-weightage NCERT chapters!**"}
        
        🎯 **PYQ Golden Rule**:
        • Make sure to solve every single PYQ from **2012 to 2025**. NEET frequently repeats direct concepts (and sometimes exact question formats) from the last 10 years.
        • When doing Biology chapters, treat NCERT diagrams, labels, and table values as highly examinable.
        """.trimIndent()
      }

      // DEFAULT NEET SUCCESS Roadmap
      else -> {
        """
        ✨ **Personalized NEET Prep Roadmap (Local Engine)**:
        
        Your on-device logs show:
        • **Study Syllabus Completion**: $completedChapters chapters done out of $totalChapters.
        • **Revision Index**: Spaced revision completed for $revisedChapters chapters.
        
        🚀 **Top 3 Actions for Your Next Study Session**:
        1. **Prioritize Weak Topics First**: Spend 45 minutes on any pending chapter revisions (like Spaced Repetition).
        2. **Practice 35 Numericals**: Set a timer for 40 minutes and practice Physics/Physical Chemistry calculations with no external help.
        3. **Analyze Test Mistakes**: Review your mistake log to ensure negative marks are minimized.
        
        *Tip: You can toggle "Cloud Model" above to send this query to the cloud for advanced reasoning responses!*
        """.trimIndent()
      }
    }
  }
}
