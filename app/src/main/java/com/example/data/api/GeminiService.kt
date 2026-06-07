package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

interface GeminiApi {
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

    val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }
}

class GeminiService {

    // Offline FAQ DB
    private val localFaq = mapOf(
        "مرحبا" to "أهلاً بك! أنا مساعدك الذكي في تطبيق كل خدمات اليمن. كيف يمكنني مساعدتك في العثور على الفنيين المهرة أو الميزات اليوم؟",
        "السلام عليكم" to "وعليكم السلام ورحمة الله وبركاته! أهلاً بك في الدليل والخدمات الأول في اليمن. كيف أساعدك اليوم؟",
        "التسجيل" to "لتسجيل حساب فني أو مقدم خدمة، يرجى الضغط على الأيقونة 👤 (تسجيل مهني) في القائمة العلوية وتعبئة الاستمارة المطلوبة.",
        "تسجيل" to "لتسجيل حساب فني أو مقدم خدمة، يرجى الضغط على الأيقونة 👤 (تسجيل مهني) في القائمة العلوية وتعبئة الاستمارة المطلوبة.",
        "الأسعار" to "تختلف الأسعار باختلاف مقدمي الخدمة وحسب طبيعة العمل المطلوب. يمكنك التواصل مع الفني مباشرة لمناقشة السعر والاتفاق عليه.",
        "أسعار" to "تختلف الأسعار باختلاف مقدمي الخدمة وحسب طبيعة العمل المطلوب. يمكنك التواصل مع الفني مباشرة لمناقشة السعر والاتفاق عليه.",
        "الدعم" to "يمكنك التواصل مع فريق الدعم الفني MAW مباشرة على الرقم 777644670 عبر الاتصال الهاتفي أو واتساب لأي شكاوى أو استفسارات.",
        "اتصال" to "يمكنك التواصل مع فريق الدعم الفني MAW مباشرة على الرقم 777644670 عبر الاتصال الهاتفي أو واتساب لأي شكاوى أو استفسارات.",
        "عن التطبيق" to "تطبيق كل خدمات اليمن هو تطبيق مجاني يهدف لتسهيل الوصول للفنيين والمحترفين لجميع المجالات والمهن في اليمن بطريقة سهلة ومبتكرة مع دعم كامل بدون إنترنت.",
        "من مطور التطبيق" to "تم تطوير هذا التطبيق بواسطة المهندس ماهر علوان MAW لدعم قطاع الحرفيين والمواهب وتنمية التجارة والخدمات المحلية في اليمن."
    )

    suspend fun getResponse(message: String): String = withContext(Dispatchers.IO) {
        // 1. Process with local FAQ first (offline capability)
        val normalizedMsg = message.trim().lowercase()
        for ((key, value) in localFaq) {
            if (normalizedMsg.contains(key)) {
                return@withContext value
            }
        }

        // 2. Call Gemini API if not found locally
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            return@withContext "عذراً، مساعد اليمن الذكي يعمل بوضعية محدودة لعدم توفر مفتاح Gemini API صالح في السيرفر حالياً. يمكنك تصفح دليل الفنيين الرائع والاتصال بهم مباشرة دون إنترنت!"
        }

        try {
            val systemRule = "أن تتقمص دور مساعد فني وخدمي متميز لمستخدمي اليمن في تطبيق 'كل خدمات اليمن'. رد كأخصائي محلي ودود ومتفانٍ ومحب لليمن باللغة العربية الفصحى أو اللهجة الصنعانية الرصينة."
            val fullPrompt = "$systemRule\n\nالسؤال: $message"

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = fullPrompt)
                        )
                    )
                )
            )

            val reply = GeminiClient.api.generateContent(apiKey, request)
            return@withContext reply.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "عذراً، لم أستطع فهم الإجابة. هل يمكن تفصيل سؤالك أكثر؟"
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "عذراً، يبدو أن هناك صعوبة مؤقتة في الاتصال بخدمات المساعد الذكي عبر الإنترنت. سأحاول خدمتك بكل ود. للاتصال المباشر بالدعم يرجى طلب: 777644670"
        }
    }
}
