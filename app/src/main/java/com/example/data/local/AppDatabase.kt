package com.example.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppDatabase(context: Context) : SQLiteOpenHelper(context, "yemen_services_direct_db", null, 2) {

    // --- Dynamic Reactive Streams ---
    private val _approvedProvidersFlow = MutableStateFlow<List<Provider>>(emptyList())
    val approvedProvidersFlow: Flow<List<Provider>> = _approvedProvidersFlow.asStateFlow()

    private val _pendingProvidersFlow = MutableStateFlow<List<Provider>>(emptyList())
    val pendingProvidersFlow: Flow<List<Provider>> = _pendingProvidersFlow.asStateFlow()

    private val _bookmarkedProvidersFlow = MutableStateFlow<List<Provider>>(emptyList())
    val bookmarkedProvidersFlow: Flow<List<Provider>> = _bookmarkedProvidersFlow.asStateFlow()

    private val _adminConfigFlow = MutableStateFlow<AdminConfig?>(null)
    val adminConfigFlow: Flow<AdminConfig?> = _adminConfigFlow.asStateFlow()

    private val _chatMessagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessagesFlow: Flow<List<ChatMessage>> = _chatMessagesFlow.asStateFlow()

    init {
        // Trigger initial data load to populate streams
        refreshData()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE providers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                mainCategory TEXT NOT NULL,
                subCategory TEXT NOT NULL,
                city TEXT NOT NULL,
                phone TEXT NOT NULL,
                whatsapp TEXT,
                description TEXT,
                rating REAL,
                votes INTEGER,
                isVerified INTEGER,
                photoUri TEXT,
                idPhotoUri TEXT,
                gender TEXT,
                registerDate INTEGER,
                isBookmarked INTEGER,
                isPending INTEGER,
                isPinned INTEGER,
                isRecommended INTEGER,
                isSubscribed INTEGER,
                points INTEGER,
                latitude REAL,
                longitude REAL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE admin_config (
                id INTEGER PRIMARY KEY,
                registrationConditions TEXT,
                welcomeMessage TEXT,
                baseAppRateHourYER INTEGER,
                fontSizeModifier REAL,
                secretKey TEXT,
                appName TEXT,
                themeIndex INTEGER,
                themePrimaryColor TEXT,
                themeSecondaryColor TEXT,
                sponsorFooter TEXT,
                supportPhone TEXT,
                supportEmail TEXT,
                supportWhatsapp TEXT,
                adminPassword TEXT,
                adminUsername TEXT,
                footerFontSize REAL,
                footerOpacity REAL,
                smartAssistantSizePercent INTEGER,
                smartAssistantEnabled INTEGER,
                smartAssistantIcon TEXT,
                chatEnabled INTEGER,
                chatDisabledMessage TEXT,
                chatIcon TEXT,
                chatColor TEXT,
                chatBubbleSizePercent INTEGER,
                maxRadiusSearch INTEGER,
                voiceSearchEnabled INTEGER,
                loyaltyPointsEnabled INTEGER,
                maintenanceMode INTEGER,
                maintenanceMessage TEXT,
                twoFactorAuthEnabled INTEGER,
                monthlySubscriptionEnabled INTEGER,
                topBarLayout TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE chat_messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sender TEXT,
                senderName TEXT,
                receiverId INTEGER,
                message TEXT,
                timestamp INTEGER,
                isRead INTEGER
            )
        """.trimIndent())

        // Insert initial configuration
        db.execSQL("""
            INSERT INTO admin_config (
                id, registrationConditions, welcomeMessage, baseAppRateHourYER, fontSizeModifier, secretKey,
                appName, themeIndex, themePrimaryColor, themeSecondaryColor, sponsorFooter, supportPhone,
                supportEmail, supportWhatsapp, adminPassword, adminUsername, footerFontSize, footerOpacity,
                smartAssistantSizePercent, smartAssistantEnabled, smartAssistantIcon, chatEnabled, chatDisabledMessage,
                chatIcon, chatColor, chatBubbleSizePercent, maxRadiusSearch, voiceSearchEnabled, loyaltyPointsEnabled,
                maintenanceMode, maintenanceMessage, twoFactorAuthEnabled, monthlySubscriptionEnabled, topBarLayout
            ) VALUES (
                1, 
                'اللائحة التنظيمية المفتوحة:\n1. الأمانة والمصداقية المطلقة مع العملاء.\n2. الالتزام بالمواعيد ومستوى جودة الخدمة.\n3. كرت المهنة والبطاقة الشخصية اختيارية للتوثيق لكن تزيد فرصة ظهورك كمهني موثوق.\n4. يحق للإدارة حظر أي حساب في حال وجود شكاوي متكررة.', 
                'أهلاً بك يا غالي! أنا أبو يمن مساعدك الذكي للتنظيف والكهرباء والسباكة وكل الحرف. كيف أقدر أساعدك اليوم بخصوص أعطال البيت أو تسعير الخدمات؟ 🛠️⚡', 
                4500, 1.0, 'maher--736462',
                'كل خدمات اليمن', 2, '0xFF0E6F4B', '0xFFD4AF37', 'MAW 777644670', '777644670',
                'support@yemenservices.com', '777644670', 'maher736462', 'WAM2026', 10.0, 0.5,
                50, 1, '🤖 المساعد', 1, 'تنبيه: تم تعطيل الميزة مؤقتاً للتحديث ومراجعة الاتصالات 🛠️',
                '💬', '0xFFD4AF37', 50, 50, 1, 1,
                0, 'التطبيق قيد التطوير والصيانة الدورية حالياً. سنعود بشكل أفضل قريباً جداً 🛠️', 0, 1, 'home,login,register,lang,refresh'
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS providers")
        db.execSQL("DROP TABLE IF EXISTS admin_config")
        db.execSQL("DROP TABLE IF EXISTS chat_messages")
        onCreate(db)
    }

    // Refresh memory cache in streams
    @Synchronized
    fun refreshData() {
        val db = readableDatabase ?: return

        // Load Approved
        val approved = mutableListOf<Provider>()
        val pCursor = db.rawQuery("SELECT * FROM providers ORDER BY isPinned DESC, rating DESC, name ASC", null)
        if (pCursor.moveToFirst()) {
            do {
                val prov = Provider(
                    id = pCursor.getInt(pCursor.getColumnIndexOrThrow("id")),
                    name = pCursor.getString(pCursor.getColumnIndexOrThrow("name")),
                    mainCategory = pCursor.getString(pCursor.getColumnIndexOrThrow("mainCategory")),
                    subCategory = pCursor.getString(pCursor.getColumnIndexOrThrow("subCategory")),
                    city = pCursor.getString(pCursor.getColumnIndexOrThrow("city")),
                    phone = pCursor.getString(pCursor.getColumnIndexOrThrow("phone")),
                    whatsapp = pCursor.getString(pCursor.getColumnIndexOrThrow("whatsapp")) ?: "",
                    description = pCursor.getString(pCursor.getColumnIndexOrThrow("description")) ?: "",
                    rating = pCursor.getFloat(pCursor.getColumnIndexOrThrow("rating")),
                    votes = pCursor.getInt(pCursor.getColumnIndexOrThrow("votes")),
                    isVerified = pCursor.getInt(pCursor.getColumnIndexOrThrow("isVerified")) == 1,
                    photoUri = pCursor.getString(pCursor.getColumnIndexOrThrow("photoUri")),
                    idPhotoUri = pCursor.getString(pCursor.getColumnIndexOrThrow("idPhotoUri")),
                    gender = pCursor.getString(pCursor.getColumnIndexOrThrow("gender")) ?: "ذكر",
                    registerDate = pCursor.getLong(pCursor.getColumnIndexOrThrow("registerDate")),
                    isBookmarked = pCursor.getInt(pCursor.getColumnIndexOrThrow("isBookmarked")) == 1,
                    isPending = pCursor.getInt(pCursor.getColumnIndexOrThrow("isPending")) == 1,
                    isPinned = pCursor.getInt(pCursor.getColumnIndexOrThrow("isPinned")) == 1,
                    isRecommended = pCursor.getInt(pCursor.getColumnIndexOrThrow("isRecommended")) == 1,
                    isSubscribed = pCursor.getInt(pCursor.getColumnIndexOrThrow("isSubscribed")) == 1,
                    points = pCursor.getInt(pCursor.getColumnIndexOrThrow("points")),
                    latitude = pCursor.getDouble(pCursor.getColumnIndexOrThrow("latitude")),
                    longitude = pCursor.getDouble(pCursor.getColumnIndexOrThrow("longitude"))
                )
                approved.add(prov)
            } while (pCursor.moveToNext())
        }
        pCursor.close()

        _approvedProvidersFlow.value = approved.filter { !it.isPending }
        _pendingProvidersFlow.value = approved.filter { it.isPending }
        _bookmarkedProvidersFlow.value = approved.filter { it.isBookmarked && !it.isPending }

        // Load Config
        var config: AdminConfig? = null
        val cCursor = db.rawQuery("SELECT * FROM admin_config WHERE id = 1", null)
        if (cCursor.moveToFirst()) {
            config = AdminConfig(
                id = cCursor.getInt(cCursor.getColumnIndexOrThrow("id")),
                registrationConditions = cCursor.getString(cCursor.getColumnIndexOrThrow("registrationConditions")),
                welcomeMessage = cCursor.getString(cCursor.getColumnIndexOrThrow("welcomeMessage")),
                baseAppRateHourYER = cCursor.getInt(cCursor.getColumnIndexOrThrow("baseAppRateHourYER")),
                fontSizeModifier = cCursor.getFloat(cCursor.getColumnIndexOrThrow("fontSizeModifier")),
                secretKey = cCursor.getString(cCursor.getColumnIndexOrThrow("secretKey")),
                appName = cCursor.getString(cCursor.getColumnIndexOrThrow("appName")) ?: "كل خدمات اليمن",
                themeIndex = cCursor.getInt(cCursor.getColumnIndexOrThrow("themeIndex")),
                themePrimaryColor = cCursor.getString(cCursor.getColumnIndexOrThrow("themePrimaryColor")) ?: "0xFF0E6F4B",
                themeSecondaryColor = cCursor.getString(cCursor.getColumnIndexOrThrow("themeSecondaryColor")) ?: "0xFFD4AF37",
                sponsorFooter = cCursor.getString(cCursor.getColumnIndexOrThrow("sponsorFooter")) ?: "MAW 777644670",
                supportPhone = cCursor.getString(cCursor.getColumnIndexOrThrow("supportPhone")) ?: "777644670",
                supportEmail = cCursor.getString(cCursor.getColumnIndexOrThrow("supportEmail")) ?: "support@yemenservices.com",
                supportWhatsapp = cCursor.getString(cCursor.getColumnIndexOrThrow("supportWhatsapp")) ?: "777644670",
                adminPassword = cCursor.getString(cCursor.getColumnIndexOrThrow("adminPassword")) ?: "maher736462",
                adminUsername = cCursor.getString(cCursor.getColumnIndexOrThrow("adminUsername")) ?: "WAM2026",
                footerFontSize = cCursor.getFloat(cCursor.getColumnIndexOrThrow("footerFontSize")),
                footerOpacity = cCursor.getFloat(cCursor.getColumnIndexOrThrow("footerOpacity")),
                smartAssistantSizePercent = cCursor.getInt(cCursor.getColumnIndexOrThrow("smartAssistantSizePercent")),
                smartAssistantEnabled = cCursor.getInt(cCursor.getColumnIndexOrThrow("smartAssistantEnabled")) == 1,
                smartAssistantIcon = cCursor.getString(cCursor.getColumnIndexOrThrow("smartAssistantIcon")) ?: "🤖 المساعد",
                chatEnabled = cCursor.getInt(cCursor.getColumnIndexOrThrow("chatEnabled")) == 1,
                chatDisabledMessage = cCursor.getString(cCursor.getColumnIndexOrThrow("chatDisabledMessage")) ?: "",
                chatIcon = cCursor.getString(cCursor.getColumnIndexOrThrow("chatIcon")) ?: "💬",
                chatColor = cCursor.getString(cCursor.getColumnIndexOrThrow("chatColor")) ?: "0xFFD4AF37",
                chatBubbleSizePercent = cCursor.getInt(cCursor.getColumnIndexOrThrow("chatBubbleSizePercent")),
                maxRadiusSearch = cCursor.getInt(cCursor.getColumnIndexOrThrow("maxRadiusSearch")),
                voiceSearchEnabled = cCursor.getInt(cCursor.getColumnIndexOrThrow("voiceSearchEnabled")) == 1,
                loyaltyPointsEnabled = cCursor.getInt(cCursor.getColumnIndexOrThrow("loyaltyPointsEnabled")) == 1,
                maintenanceMode = cCursor.getInt(cCursor.getColumnIndexOrThrow("maintenanceMode")) == 1,
                maintenanceMessage = cCursor.getString(cCursor.getColumnIndexOrThrow("maintenanceMessage")) ?: "",
                twoFactorAuthEnabled = cCursor.getInt(cCursor.getColumnIndexOrThrow("twoFactorAuthEnabled")) == 1,
                monthlySubscriptionEnabled = cCursor.getInt(cCursor.getColumnIndexOrThrow("monthlySubscriptionEnabled")) == 1,
                topBarLayout = cCursor.getString(cCursor.getColumnIndexOrThrow("topBarLayout")) ?: "home,login,register,lang,refresh"
            )
        }
        cCursor.close()
        _adminConfigFlow.value = config

        // Load Chat History
        val messages = mutableListOf<ChatMessage>()
        val mCursor = db.rawQuery("SELECT * FROM chat_messages ORDER BY timestamp ASC", null)
        if (mCursor.moveToFirst()) {
            do {
                messages.add(
                    ChatMessage(
                        id = mCursor.getInt(mCursor.getColumnIndexOrThrow("id")),
                        sender = mCursor.getString(mCursor.getColumnIndexOrThrow("sender")),
                        senderName = mCursor.getString(mCursor.getColumnIndexOrThrow("senderName")) ?: "",
                        receiverId = mCursor.getInt(mCursor.getColumnIndexOrThrow("receiverId")),
                        message = mCursor.getString(mCursor.getColumnIndexOrThrow("message")),
                        timestamp = mCursor.getLong(mCursor.getColumnIndexOrThrow("timestamp")),
                        isRead = mCursor.getInt(mCursor.getColumnIndexOrThrow("isRead")) == 1
                    )
                )
            } while (mCursor.moveToNext())
        }
        mCursor.close()
        _chatMessagesFlow.value = messages
    }

    // --- Mutations ---
    @Synchronized
    fun insertProvider(p: Provider) {
        val db = writableDatabase ?: return
        val cv = ContentValues().apply {
            if (p.id != 0) {
                put("id", p.id)
            }
            put("name", p.name)
            put("mainCategory", p.mainCategory)
            put("subCategory", p.subCategory)
            put("city", p.city)
            put("phone", p.phone)
            put("whatsapp", p.whatsapp)
            put("description", p.description)
            put("rating", p.rating)
            put("votes", p.votes)
            put("isVerified", if (p.isVerified) 1 else 0)
            put("photoUri", p.photoUri)
            put("idPhotoUri", p.idPhotoUri)
            put("gender", p.gender)
            put("registerDate", p.registerDate)
            put("isBookmarked", if (p.isBookmarked) 1 else 0)
            put("isPending", if (p.isPending) 1 else 0)
            put("isPinned", if (p.isPinned) 1 else 0)
            put("isRecommended", if (p.isRecommended) 1 else 0)
            put("isSubscribed", if (p.isSubscribed) 1 else 0)
            put("points", p.points)
            put("latitude", p.latitude)
            put("longitude", p.longitude)
        }
        db.insertWithOnConflict("providers", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        refreshData()
    }

    @Synchronized
    fun deleteProvider(p: Provider) {
        val db = writableDatabase ?: return
        db.delete("providers", "id = ?", arrayOf(p.id.toString()))
        refreshData()
    }

    @Synchronized
    fun insertAdminConfig(config: AdminConfig) {
        val db = writableDatabase ?: return
        val cv = ContentValues().apply {
            put("id", 1)
            put("registrationConditions", config.registrationConditions)
            put("welcomeMessage", config.welcomeMessage)
            put("baseAppRateHourYER", config.baseAppRateHourYER)
            put("fontSizeModifier", config.fontSizeModifier)
            put("secretKey", config.secretKey)
            put("appName", config.appName)
            put("themeIndex", config.themeIndex)
            put("themePrimaryColor", config.themePrimaryColor)
            put("themeSecondaryColor", config.themeSecondaryColor)
            put("sponsorFooter", config.sponsorFooter)
            put("supportPhone", config.supportPhone)
            put("supportEmail", config.supportEmail)
            put("supportWhatsapp", config.supportWhatsapp)
            put("adminPassword", config.adminPassword)
            put("adminUsername", config.adminUsername)
            put("footerFontSize", config.footerFontSize)
            put("footerOpacity", config.footerOpacity)
            put("smartAssistantSizePercent", config.smartAssistantSizePercent)
            put("smartAssistantEnabled", if (config.smartAssistantEnabled) 1 else 0)
            put("smartAssistantIcon", config.smartAssistantIcon)
            put("chatEnabled", if (config.chatEnabled) 1 else 0)
            put("chatDisabledMessage", config.chatDisabledMessage)
            put("chatIcon", config.chatIcon)
            put("chatColor", config.chatColor)
            put("chatBubbleSizePercent", config.chatBubbleSizePercent)
            put("maxRadiusSearch", config.maxRadiusSearch)
            put("voiceSearchEnabled", if (config.voiceSearchEnabled) 1 else 0)
            put("loyaltyPointsEnabled", if (config.loyaltyPointsEnabled) 1 else 0)
            put("maintenanceMode", if (config.maintenanceMode) 1 else 0)
            put("maintenanceMessage", config.maintenanceMessage)
            put("twoFactorAuthEnabled", if (config.twoFactorAuthEnabled) 1 else 0)
            put("monthlySubscriptionEnabled", if (config.monthlySubscriptionEnabled) 1 else 0)
            put("topBarLayout", config.topBarLayout)
        }
        db.insertWithOnConflict("admin_config", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        refreshData()
    }

    @Synchronized
    fun insertChatMessage(sender: String, message: String, senderName: String = "", receiverId: Int = 0) {
        val db = writableDatabase ?: return
        val cv = ContentValues().apply {
            put("sender", sender)
            put("senderName", senderName)
            put("receiverId", receiverId)
            put("message", message)
            put("timestamp", System.currentTimeMillis())
            put("isRead", 0)
        }
        db.insert("chat_messages", null, cv)
        refreshData()
    }

    @Synchronized
    fun clearChatHistory() {
        val db = writableDatabase ?: return
        db.execSQL("DELETE FROM chat_messages")
        refreshData()
    }

    // Backup & Restore Utilities
    @Synchronized
    fun exportBackupJson(): String {
        val db = readableDatabase ?: return ""
        val sb = StringBuilder()
        sb.append("{\n  \"providers\": [\n")
        val pCursor = db.rawQuery("SELECT * FROM providers", null)
        var first = true
        if (pCursor.moveToFirst()) {
            do {
                if (!first) sb.append(",\n")
                first = false
                val id = pCursor.getInt(pCursor.getColumnIndexOrThrow("id"))
                val name = pCursor.getString(pCursor.getColumnIndexOrThrow("name"))
                val main = pCursor.getString(pCursor.getColumnIndexOrThrow("mainCategory"))
                val sub = pCursor.getString(pCursor.getColumnIndexOrThrow("subCategory"))
                val city = pCursor.getString(pCursor.getColumnIndexOrThrow("city"))
                val phone = pCursor.getString(pCursor.getColumnIndexOrThrow("phone"))
                val wa = pCursor.getString(pCursor.getColumnIndexOrThrow("whatsapp")) ?: ""
                val desc = pCursor.getString(pCursor.getColumnIndexOrThrow("description")) ?: ""
                val rating = pCursor.getFloat(pCursor.getColumnIndexOrThrow("rating"))
                val isP = pCursor.getInt(pCursor.getColumnIndexOrThrow("isPending"))
                val isPin = pCursor.getInt(pCursor.getColumnIndexOrThrow("isPinned"))
                val isRec = pCursor.getInt(pCursor.getColumnIndexOrThrow("isRecommended"))
                val sbsc = pCursor.getInt(pCursor.getColumnIndexOrThrow("isSubscribed"))
                val pts = pCursor.getInt(pCursor.getColumnIndexOrThrow("points"))
                val lat = pCursor.getDouble(pCursor.getColumnIndexOrThrow("latitude"))
                val lon = pCursor.getDouble(pCursor.getColumnIndexOrThrow("longitude"))

                sb.append("    {")
                sb.append("\"id\": $id, ")
                sb.append("\"name\": \"$name\", ")
                sb.append("\"mainCategory\": \"$main\", ")
                sb.append("\"subCategory\": \"$sub\", ")
                sb.append("\"city\": \"$city\", ")
                sb.append("\"phone\": \"$phone\", ")
                sb.append("\"whatsapp\": \"$wa\", ")
                sb.append("\"description\": \"${desc.replace("\n", "\\n")}\", ")
                sb.append("\"rating\": $rating, ")
                sb.append("\"isPending\": $isP, ")
                sb.append("\"isPinned\": $isPin, ")
                sb.append("\"isRecommended\": $isRec, ")
                sb.append("\"isSubscribed\": $sbsc, ")
                sb.append("\"points\": $pts, ")
                sb.append("\"latitude\": $lat, ")
                sb.append("\"longitude\": $lon")
                sb.append("}")
            } while (pCursor.moveToNext())
        }
        pCursor.close()
        sb.append("\n  ]\n}")
        return sb.toString()
    }

    @Synchronized
    fun importBackupJson(json: String): Boolean {
        // Simple robust custom parser for safe import directly into SQLite table
        val db = writableDatabase ?: return false
        return try {
            db.beginTransaction()
            db.delete("providers", null, null)

            // Extremely reliable token parser for simple flat lists
            val providersRegex = Regex("\\{([^}]+)\\}")
            val matches = providersRegex.findAll(json)

            for (m in matches) {
                val groupVal = m.groupValues[1]
                val keysRegex = Regex("\"(\\w+)\"\\s*:\\s*(?:\"([^\"]*)\"|([\\d.]+))")
                val contents = keysRegex.findAll(groupVal)

                val cv = ContentValues()
                for (con in contents) {
                    val key = con.groupValues[1]
                    val strValue = con.groupValues[2]
                    val numValue = con.groupValues[3]

                    if (key == "id") continue // reset to autogenerate to avoid overlap

                    if (strValue.isNotEmpty()) {
                        cv.put(key, strValue.replace("\\n", "\n"))
                    } else if (numValue.isNotEmpty()) {
                        if (numValue.contains(".")) {
                            cv.put(key, numValue.toDouble())
                        } else {
                            cv.put(key, numValue.toInt())
                        }
                    }
                }
                db.insert("providers", null, cv)
            }
            db.setTransactionSuccessful()
            db.endTransaction()
            refreshData()
            true
        } catch (e: Exception) {
            if (db.inTransaction()) db.endTransaction()
            false
        }
    }
}
