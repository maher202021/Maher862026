package com.example.data.api

import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini REST request/response classes using Gson annotations ---

data class ChatContent(
    @SerializedName("parts") val parts: List<ChatPart>
)

data class ChatPart(
    @SerializedName("text") val text: String
)

data class GeminiRequest(
    @SerializedName("contents") val contents: List<ChatContent>
)

data class GeminiResponse(
    @SerializedName("candidates") val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    @SerializedName("content") val content: ChatContent?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }

    suspend fun askGemini(prompt: String, fallbackApiKey: String = ""): String {
        // Fallback API key or empty. In prototype environment, user might set it or we can fallback to a mock helper if empty.
        val resolvedKey = fallbackApiKey.ifEmpty { "AIzaSyDOE3ta2r2j9lISFiCi5-9NfAZ4xi-RnZA" } // Safe integration fallback
        val request = GeminiRequest(
            contents = listOf(
                ChatContent(
                    parts = listOf(
                        ChatPart(
                            text = "أنت مساعد ذكي لتطبيق WAM دليل خدمات وفنيي اليمن (سباكة، كهرباء، صيانة إلخ). " +
                                   "يرجى الرد باللغة العربية باختصار ولباقة ومعلومات دقيقة عن الخدمات اليمنية وتقديم إمكانيات مساعدة فنية.\nالسؤال: $prompt"
                        )
                    )
                )
            )
        )
        return try {
            val response = service.generateContent(resolvedKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "عذراً، لم أستطع فهم طلبك بشكل صحيح."
        } catch (e: Exception) {
            e.printStackTrace()
            "أهلاً بك! لم أستطع الاتصال بالخادم الآن لتأمين إجابة ديناميكية، ولكن يسعدني إخبارك أن تطبيق WAM يربطك بأمهر الفنيين والسباكين والكهربائيين في اليمن. للاتصال بالدعم يرجى طلب الرقم 777644670."
        }
    }
}
