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

    fun exportDatabaseBackup(): String {
        return db.exportBackupJson()
    }

    fun importDatabaseBackup(json: String): Boolean {
        return db.importBackupJson(json)
    }

    suspend fun toggleBookmark(provider: Provider) {
        db.toggleLocalBookmark(provider)
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
        db.prepopulateFirestoreIfEmpty()
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
