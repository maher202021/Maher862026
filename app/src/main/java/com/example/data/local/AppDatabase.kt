package com.example.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppDatabase(context: Context) : SQLiteOpenHelper(context, "yemen_services_direct_db", null, 1) {

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
                isPending INTEGER
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE admin_config (
                id INTEGER PRIMARY KEY,
                registrationConditions TEXT,
                welcomeMessage TEXT,
                baseAppRateHourYER INTEGER,
                fontSizeModifier REAL,
                secretKey TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE chat_messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sender TEXT,
                message TEXT,
                timestamp INTEGER
            )
        """.trimIndent())

        // Insert initial configuration
        db.execSQL("""
            INSERT INTO admin_config (id, registrationConditions, welcomeMessage, baseAppRateHourYER, fontSizeModifier, secretKey)
            VALUES (1, 'اللائحة التنظيمية المفتوحة: 1. الأمانة والمصداقية المطلقة مع العملاء. 2. الالتزام بالمواعيد ومستوى جودة الخدمة. 3. كرت المهنة والبطاقة الشخصية اختيارية للتوثيق لكن تزيد فرصة ظهورك كمهني موثوق. 4. يحق للإدارة حظر أي حساب في حال وجود شكاوي متكررة.', 'أهلاً بك يا غالي! أنا أبو يمن مساعدك الذكي للتنظيف والكهرباء والسباكة وكل الحرف. كيف أقدر أساعدك اليوم بخصوص أعطال البيت أو تسعير الخدمات؟ 🛠️⚡', 4500, 1.0, '9999')
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
        val pCursor = db.rawQuery("SELECT * FROM providers ORDER BY rating DESC, name ASC", null)
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
                    isPending = pCursor.getInt(pCursor.getColumnIndexOrThrow("isPending")) == 1
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
                secretKey = cCursor.getString(cCursor.getColumnIndexOrThrow("secretKey"))
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
                        message = mCursor.getString(mCursor.getColumnIndexOrThrow("message")),
                        timestamp = mCursor.getLong(mCursor.getColumnIndexOrThrow("timestamp"))
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
        }
        db.insertWithOnConflict("admin_config", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        refreshData()
    }

    @Synchronized
    fun insertChatMessage(sender: String, message: String) {
        val db = writableDatabase ?: return
        val cv = ContentValues().apply {
            put("sender", sender)
            put("message", message)
            put("timestamp", System.currentTimeMillis())
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
}
