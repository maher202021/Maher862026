package com.example.data

import com.example.BuildConfig
import com.example.data.api.*
import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.*

class AppRepository(private val db: AppDatabase) {

    val approvedProviders: Flow<List<Provider>> = db.approvedProvidersFlow
    val pendingProviders: Flow<List<Provider>> = db.pendingProvidersFlow
    val bookmarkedProviders: Flow<List<Provider>> = db.bookmarkedProvidersFlow
    val adminConfig: Flow<AdminConfig?> = db.adminConfigFlow
    val chatMessages: Flow<List<ChatMessage>> = db.chatMessagesFlow

    suspend fun insertProvider(provider: Provider) {
        db.insertProvider(provider)
    }

    suspend fun updateProvider(provider: Provider) {
        db.insertProvider(provider)
    }

    suspend fun deleteProvider(provider: Provider) {
        db.deleteProvider(provider)
    }

    suspend fun toggleBookmark(provider: Provider) {
        db.insertProvider(provider.copy(isBookmarked = !provider.isBookmarked))
    }

    suspend fun getAdminConfigSingle(): AdminConfig {
        return db.adminConfigFlow.firstOrNull() ?: AdminConfig()
    }

    suspend fun updateAdminConfig(config: AdminConfig) {
        db.insertAdminConfig(config)
    }

    suspend fun insertChatMessage(sender: String, text: String) {
        db.insertChatMessage(sender, text)
    }

    suspend fun clearChat() {
        db.clearChatHistory()
    }

    // Prepopulate if DB doesn't have any registered technicians on first run
    fun prepopulateDatabaseIfEmpty() {
        val readable = db.readableDatabase ?: return
        val cursor = readable.rawQuery("SELECT COUNT(*) FROM providers", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()

        if (count == 0) {
            val initialTechnicians = listOf(
                Provider(
                    name = "م. ياسر الحميري",
                    mainCategory = "كهرباء وإلكترونيات",
                    subCategory = "تصليح أجهزة وشبكات منزلية",
                    city = "صنعاء",
                    phone = "775432109",
                    whatsapp = "775432109",
                    description = "خبرة 10 سنوات في صيانة وتوصيل أنظمة الطاقة الشمسية الذكية، تمديد خطوط وإصلاح لوحات التوزيع المنزلية.",
                    rating = 4.9f,
                    votes = 42,
                    isVerified = true,
                    photoUri = "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=400",
                    isPending = false
                ),
                Provider(
                    name = "الأسطى ناصر العدني",
                    mainCategory = "سباكة وصحي",
                    subCategory = "تأسيس وصيانة شبكات مياه",
                    city = "عدن",
                    phone = "733987654",
                    whatsapp = "733987654",
                    description = "متخصص في كشف تسربات المياه وتأسيس شبكات الصرف الصحي للمنازل بمواد أصلية وضمانة للعمل المتين.",
                    rating = 4.8f,
                    votes = 28,
                    isVerified = true,
                    photoUri = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400",
                    isPending = false
                ),
                Provider(
                    name = "فارس الشرعبي",
                    mainCategory = "نجارة وديكور",
                    subCategory = "صيانة وتفصيل غرف وأبواب",
                    city = "تعز",
                    phone = "711554433",
                    whatsapp = "711554433",
                    description = "تفصيل وصيانة الأثاث الخشبي، أبواب غرف بجودة عالية، معالجة تلف الأخشاب والترميم الفوري السريع.",
                    rating = 4.7f,
                    votes = 19,
                    isVerified = false,
                    photoUri = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400",
                    isPending = false
                ),
                Provider(
                    name = "صالح باوزير",
                    mainCategory = "تكييف وتبريد",
                    subCategory = "صيانة مكيفات اسبليت وشباك",
                    city = "حضرموت",
                    phone = "774900112",
                    whatsapp = "774900112",
                    description = "شحن فريون، فك وتركيب مكيفات، معالجة ضعف التبريد وتوفير استهلاك الكهرباء لجميع أنواع المكيفات.",
                    rating = 4.9f,
                    votes = 31,
                    isVerified = true,
                    photoUri = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
                    isPending = false
                ),
                Provider(
                    name = "الحداد عبده عياش",
                    mainCategory = "حدادة وألومنيوم",
                    subCategory = "حماية نوافذ ومظلات وسواتر",
                    city = "الحديدة",
                    phone = "735882211",
                    whatsapp = "",
                    description = "تفصيل بوابات حديدية قوية ونوافذ ألومنيوم وشبك حماية مقاوم للرطوبة والصدأ بأسعار مناسبة.",
                    rating = 4.6f,
                    votes = 15,
                    isVerified = false,
                    photoUri = null,
                    isPending = false
                ),
                Provider(
                    name = "أروى الصنعاني",
                    mainCategory = "خياطة وتفصيل",
                    subCategory = "تفصيل جلابيات وفساتين تراثية",
                    city = "صنعاء",
                    phone = "777123456",
                    whatsapp = "777123456",
                    description = "خياطة الملابس النسائية التراثية والفساتين الراقية، دقة عالية وموديلات حديثة لتلبي كل تطلعاتكم.",
                    gender = "أنثى",
                    rating = 4.9f,
                    votes = 55,
                    isVerified = true,
                    photoUri = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400",
                    isPending = false
                )
            )
            for (tech in initialTechnicians) {
                db.insertProvider(tech)
            }
        }
    }

    // Direct Gemini rest chat API background query loop
    suspend fun getGeminiResponse(userPrompt: String, history: List<ChatMessage>, baseRate: Int): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty()) {
            return "يا غالي، لم يتم العثور على مفتاح الذكاء الاصطناعي (Gemini API Key). رجاءً قم بتكوينه في مفاتيح النظام للدردشة مع المساعد."
        }

        val apiContents = mutableListOf<Content>()
        
        for (msg in history.takeLast(10)) {
            apiContents.add(
                Content(parts = listOf(Part(text = "${if (msg.sender == "user") "المستخدم" else "أبو يمن المساعد"}: ${msg.message}")))
            )
        }

        apiContents.add(Content(parts = listOf(Part(text = "المستخدم: $userPrompt"))))

        val promptInstructions = """
            أنت "أبو يمن" - مستشار فني وحرفي يمني ذكي، ودود، شهم، وبسيط. 
            تتحدث بلهجة يمنية أصيلة، وتقدم النصح بخصوص أعطال البيوت (السباكة، الكهرباء، تكييف، نجارة، حدادة، أنظمة طاقة شمسية) ومعلومات تسعير تقريبية للبلد بالريال اليمني.
            بناءً على إعدادات الإدارة، متوسط السعر الاسترشادي ساعة العمل للحرفيين حالياً هو $baseRate ريال يمني.
            أجب باختصار وبشكل مفيد وعملي جداً، ركّز على تعليمات الأمان لتجنب الصعق الكهربائي أو الغرق، ودائماً انصحهم بالاستعانة بفني من الدليل إذا كان العطل معقداً.
            احرص على استخدام تعبيرات يمنية مثل (يا غالي، أبشر بعزك، سهل السلا، من عيوني، يا رجال، سابر، إلخ).
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = apiContents,
            generationConfig = GenerationConfig(temperature = 0.7f, maxOutputTokens = 1200),
            systemInstruction = Content(parts = listOf(Part(text = promptInstructions)))
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "يا غالي، ما وصلتلي كلمتك زين، جرب تسألني مرة ثانية وسأكون جاهز للإجابة أبشر بعزك ⚡."
        } catch (e: java.lang.Exception) {
            "يا سيدي العفو منك، واجهت مشكلة في الاتصال بالشبكة حالياً للدردشة الذكية، طمّن بالك تقدر تصفح دليل الفنيين وأرقامهم بالكامل بدون إنترنت 🛠️. الخطأ: ${e.message}"
        }
    }
}
