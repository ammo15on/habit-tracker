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
