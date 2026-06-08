package com.example.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

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

    // Local Bookmarks mapping
    private val _bookmarkedIds = MutableStateFlow<Set<Int>>(emptySet())
    private var cachedRemoteProviders: List<Provider> = emptyList()
    private var providersListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var configListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var chatsListener: com.google.firebase.firestore.ListenerRegistration? = null

    init {
        // Initialize Firebase manually
        try {
            val options = FirebaseOptions.Builder()
                .setApplicationId("1:363038603529:android:1f845fcf442c2e6693fbd8")
                .setProjectId("yemendate")
                .setApiKey("AIzaSyDOE3ta2r2j9lISFiCi5-9NfAZ4xi-RnZA")
                .setStorageBucket("yemendate.firebasestorage.app")
                .build()
            FirebaseApp.initializeApp(context, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Initialize Bookmarks Table and load them
        try {
            val db = writableDatabase
            db?.execSQL("CREATE TABLE IF NOT EXISTS provider_bookmarks (provider_id INTEGER PRIMARY KEY)")
            loadLocalBookmarks()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Setup real-time Firestore listener
        setupFirestoreSnapshotListener()

        // Start tracking connection for force reconnection
        monitorInternetConnection(context)

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

    // Refresh memory cache in streams (only config and chat, providers are real-time through Firestore)
    @Synchronized
    fun refreshData() {
        val db = readableDatabase ?: return

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

    // --- Firestore Sync Helper Methods ---

    private fun loadLocalBookmarks() {
        val db = readableDatabase ?: return
        val set = mutableSetOf<Int>()
        try {
            val cursor = db.rawQuery("SELECT provider_id FROM provider_bookmarks", null)
            if (cursor.moveToFirst()) {
                do {
                    set.add(cursor.getInt(0))
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _bookmarkedIds.value = set
    }

    fun toggleLocalBookmark(p: Provider) {
        val db = writableDatabase ?: return
        val current = _bookmarkedIds.value
        try {
            if (current.contains(p.id)) {
                db.delete("provider_bookmarks", "provider_id = ?", arrayOf(p.id.toString()))
                _bookmarkedIds.value = current - p.id
            } else {
                val cv = ContentValues().apply { put("provider_id", p.id) }
                db.insert("provider_bookmarks", null, cv)
                _bookmarkedIds.value = current + p.id
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        updateProviderFlows(cachedRemoteProviders)
    }

    fun refreshSnapshotListeners() {
        try {
            providersListener?.remove()
            configListener?.remove()
            chatsListener?.remove()

            providersListener = null
            configListener = null
            chatsListener = null
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        setupFirestoreSnapshotListener()
    }

    private fun monitorInternetConnection(context: Context) {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            if (connectivityManager != null) {
                val networkRequest = android.net.NetworkRequest.Builder()
                    .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
                
                connectivityManager.registerNetworkCallback(networkRequest, object : android.net.ConnectivityManager.NetworkCallback() {
                    private var wasDisconnected = false

                    override fun onAvailable(network: android.net.Network) {
                        super.onAvailable(network)
                        if (wasDisconnected) {
                            // Internet returned back, force refresh listeners
                            refreshSnapshotListeners()
                            wasDisconnected = false
                        }
                    }

                    override fun onLost(network: android.net.Network) {
                        super.onLost(network)
                        wasDisconnected = true
                    }
                })
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun mapToAdminConfig(map: Map<String, Any>?): AdminConfig {
        if (map == null) return AdminConfig()
        return AdminConfig(
            id = 1,
            registrationConditions = map["registrationConditions"] as? String ?: "",
            welcomeMessage = map["welcomeMessage"] as? String ?: "",
            baseAppRateHourYER = (map["baseAppRateHourYER"] as? Long ?: 4500L).toInt(),
            fontSizeModifier = (map["fontSizeModifier"] as? Double ?: 1.0).toFloat(),
            secretKey = map["secretKey"] as? String ?: "maher--736462",
            appName = map["appName"] as? String ?: "كل خدمات اليمن",
            themeIndex = (map["themeIndex"] as? Long ?: 2L).toInt(),
            themePrimaryColor = map["themePrimaryColor"] as? String ?: "0xFF0E6F4B",
            themeSecondaryColor = map["themeSecondaryColor"] as? String ?: "0xFFD4AF37",
            sponsorFooter = map["sponsorFooter"] as? String ?: "MAW 777644670",
            supportPhone = map["supportPhone"] as? String ?: "777644670",
            supportEmail = map["supportEmail"] as? String ?: "support@yemenservices.com",
            supportWhatsapp = map["supportWhatsapp"] as? String ?: "777644670",
            adminPassword = map["adminPassword"] as? String ?: "maher736462",
            adminUsername = map["adminUsername"] as? String ?: "WAM2026",
            footerFontSize = (map["footerFontSize"] as? Double ?: 10.0).toFloat(),
            footerOpacity = (map["footerOpacity"] as? Double ?: 0.5).toFloat(),
            smartAssistantSizePercent = (map["smartAssistantSizePercent"] as? Long ?: 50L).toInt(),
            smartAssistantEnabled = map["smartAssistantEnabled"] as? Boolean ?: true,
            smartAssistantIcon = map["smartAssistantIcon"] as? String ?: "🤖 المساعد",
            chatEnabled = map["chatEnabled"] as? Boolean ?: true,
            chatDisabledMessage = map["chatDisabledMessage"] as? String ?: "تنبيه: تم تعطيل الميزة مؤقتاً للتحديث ومراجعة الاتصالات 🛠️",
            chatIcon = map["chatIcon"] as? String ?: "💬",
            chatColor = map["chatColor"] as? String ?: "0xFFD4AF37",
            chatBubbleSizePercent = (map["chatBubbleSizePercent"] as? Long ?: 50L).toInt(),
            maxRadiusSearch = (map["maxRadiusSearch"] as? Long ?: 50L).toInt(),
            voiceSearchEnabled = map["voiceSearchEnabled"] as? Boolean ?: true,
            loyaltyPointsEnabled = map["loyaltyPointsEnabled"] as? Boolean ?: true,
            maintenanceMode = map["maintenanceMode"] as? Boolean ?: false,
            maintenanceMessage = map["maintenanceMessage"] as? String ?: "التطبيق قيد التطوير والصيانة الدورية حالياً...",
            twoFactorAuthEnabled = map["twoFactorAuthEnabled"] as? Boolean ?: false,
            monthlySubscriptionEnabled = map["monthlySubscriptionEnabled"] as? Boolean ?: true,
            topBarLayout = map["topBarLayout"] as? String ?: "home,login,register,lang,refresh"
        )
    }

    private fun setupFirestoreSnapshotListener() {
        try {
            val firestore = FirebaseFirestore.getInstance()

            // 1. Providers Listener (Unsubscribe old first explicitly)
            providersListener?.remove()
            providersListener = firestore.collection("providers")
                .addSnapshotListener { snapshots, e ->
                    if (snapshotErrorGate(e)) return@addSnapshotListener
                    if (snapshots != null) {
                        val providersList = mutableListOf<Provider>()
                        for (doc in snapshots.documents) {
                            try {
                                val p = documentToProvider(doc)
                                providersList.add(p)
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        }
                        cachedRemoteProviders = providersList
                        updateProviderFlows(providersList)
                    }
                }

            // 2. Identity & Configuration Sync Listener
            configListener?.remove()
            configListener = firestore.collection("settings").document("admin_config")
                .addSnapshotListener { snapshot, e ->
                    if (snapshot != null && snapshot.exists()) {
                        try {
                            val remoteConfig = mapToAdminConfig(snapshot.data)
                            insertAdminConfigLocalOnly(remoteConfig)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }

            // 3. Real-Time Chat Sync Listener (Ordering Messages by timestamp)
            chatsListener?.remove()
            chatsListener = firestore.collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener { snapshot, e ->
                    if (snapshot != null) {
                        val list = mutableListOf<ChatMessage>()
                        for (doc in snapshot.documents) {
                            try {
                                val idVal = (doc.getLong("id") ?: 0L).toInt()
                                val sender = doc.getString("sender") ?: "user"
                                val senderName = doc.getString("senderName") ?: ""
                                val receiverIdValue = (doc.getLong("receiverId") ?: 0L).toInt()
                                val message = doc.getString("message") ?: ""
                                val tempTimestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                list.add(ChatMessage(idVal, sender, senderName, receiverIdValue, message, tempTimestamp, false))
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        }
                        _chatMessagesFlow.value = list
                    }
                }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun snapshotErrorGate(e: Exception?): Boolean {
        return e != null
    }

    private fun updateProviderFlows(remoteProviders: List<Provider>) {
        val bookmarkedSet = _bookmarkedIds.value
        val updatedList = remoteProviders.map { p ->
            p.copy(isBookmarked = bookmarkedSet.contains(p.id))
        }

        // Sort them neatly: Pinned first, then sorted by rating descending, then by name alphabetically.
        val sortedList = updatedList.sortedWith(
            compareByDescending<Provider> { it.isPinned }
                .thenByDescending { it.rating }
                .thenBy { it.name }
        )

        _approvedProvidersFlow.value = sortedList.filter { !it.isPending }
        _pendingProvidersFlow.value = sortedList.filter { it.isPending }
        _bookmarkedProvidersFlow.value = sortedList.filter { it.isBookmarked && !it.isPending }
    }

    fun prepopulateFirestoreIfEmpty() {
        try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("providers").limit(1).get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
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

                        val batch = firestore.batch()
                        for (tech in initialTechnicians) {
                            val phoneFiltered = tech.phone.filter { it.isDigit() }
                            val techId = Math.abs(phoneFiltered.hashCode())
                            val docId = "provider_${phoneFiltered}"
                            val docRef = firestore.collection("providers").document(docId)
                            val data = providerToMap(tech.copy(id = techId))
                            batch.set(docRef, data)
                        }
                        batch.commit()
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun documentToProvider(doc: com.google.firebase.firestore.DocumentSnapshot): Provider {
        val phone = doc.getString("phone") ?: ""
        val id = (doc.getLong("id") ?: Math.abs(phone.filter { it.isDigit() }.hashCode()).toLong()).toInt()
        return Provider(
            id = id,
            name = doc.getString("name") ?: "",
            mainCategory = doc.getString("mainCategory") ?: "",
            subCategory = doc.getString("subCategory") ?: "",
            city = doc.getString("city") ?: "",
            phone = phone,
            whatsapp = doc.getString("whatsapp") ?: "",
            description = doc.getString("description") ?: "",
            rating = (doc.getDouble("rating") ?: doc.getLong("rating")?.toDouble() ?: 4.5).toFloat(),
            votes = (doc.getLong("votes") ?: 0).toInt(),
            isVerified = doc.getBoolean("isVerified") ?: false,
            photoUri = doc.getString("photoUri"),
            idPhotoUri = doc.getString("idPhotoUri"),
            gender = doc.getString("gender") ?: "ذكر",
            registerDate = doc.getLong("registerDate") ?: System.currentTimeMillis(),
            isBookmarked = false,
            isPending = doc.getBoolean("isPending") ?: false,
            isPinned = doc.getBoolean("isPinned") ?: false,
            isRecommended = doc.getBoolean("isRecommended") ?: false,
            isSubscribed = doc.getBoolean("isSubscribed") ?: false,
            points = (doc.getLong("points") ?: 0).toInt(),
            latitude = doc.getDouble("latitude") ?: 0.0,
            longitude = doc.getDouble("longitude") ?: 0.0
        )
    }

    private fun providerToMap(p: Provider): Map<String, Any?> {
        return mapOf(
            "id" to p.id,
            "name" to p.name,
            "mainCategory" to p.mainCategory,
            "subCategory" to p.subCategory,
            "city" to p.city,
            "phone" to p.phone,
            "whatsapp" to p.whatsapp,
            "description" to p.description,
            "rating" to p.rating.toDouble(),
            "votes" to p.votes,
            "isVerified" to p.isVerified,
            "photoUri" to p.photoUri,
            "idPhotoUri" to p.idPhotoUri,
            "gender" to p.gender,
            "registerDate" to p.registerDate,
            "isPending" to p.isPending,
            "isPinned" to p.isPinned,
            "isRecommended" to p.isRecommended,
            "isSubscribed" to p.isSubscribed,
            "points" to p.points,
            "latitude" to p.latitude,
            "longitude" to p.longitude
        )
    }

    // --- Mutations ---

    fun insertProvider(p: Provider) {
        try {
            val phoneFiltered = p.phone.filter { it.isDigit() }
            val finalId = if (p.id != 0) p.id else Math.abs(phoneFiltered.hashCode())
            val updatedProvider = p.copy(id = finalId)

            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val docId = "provider_${phoneFiltered.ifEmpty { finalId.toString() }}"
            firestore.collection("providers").document(docId)
                .set(providerToMap(updatedProvider))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun deleteProvider(p: Provider) {
        try {
            val phoneFiltered = p.phone.filter { it.isDigit() }
            val docId = "provider_${phoneFiltered.ifEmpty { p.id.toString() }}"
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            firestore.collection("providers").document(docId).delete()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    @Synchronized
    fun insertAdminConfigLocalOnly(config: AdminConfig) {
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

    private fun insertAdminConfigFirestore(config: AdminConfig) {
        try {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val map = mapOf(
                "registrationConditions" to config.registrationConditions,
                "welcomeMessage" to config.welcomeMessage,
                "baseAppRateHourYER" to config.baseAppRateHourYER.toLong(),
                "fontSizeModifier" to config.fontSizeModifier.toDouble(),
                "secretKey" to config.secretKey,
                "appName" to config.appName,
                "themeIndex" to config.themeIndex.toLong(),
                "themePrimaryColor" to config.themePrimaryColor,
                "themeSecondaryColor" to config.themeSecondaryColor,
                "sponsorFooter" to config.sponsorFooter,
                "supportPhone" to config.supportPhone,
                "supportEmail" to config.supportEmail,
                "supportWhatsapp" to config.supportWhatsapp,
                "adminPassword" to config.adminPassword,
                "adminUsername" to config.adminUsername,
                "footerFontSize" to config.footerFontSize.toDouble(),
                "footerOpacity" to config.footerOpacity.toDouble(),
                "smartAssistantSizePercent" to config.smartAssistantSizePercent.toLong(),
                "smartAssistantEnabled" to config.smartAssistantEnabled,
                "smartAssistantIcon" to config.smartAssistantIcon,
                "chatEnabled" to config.chatEnabled,
                "chatDisabledMessage" to config.chatDisabledMessage,
                "chatIcon" to config.chatIcon,
                "chatColor" to config.chatColor,
                "chatBubbleSizePercent" to config.chatBubbleSizePercent.toLong(),
                "maxRadiusSearch" to config.maxRadiusSearch.toLong(),
                "voiceSearchEnabled" to config.voiceSearchEnabled,
                "loyaltyPointsEnabled" to config.loyaltyPointsEnabled,
                "maintenanceMode" to config.maintenanceMode,
                "maintenanceMessage" to config.maintenanceMessage,
                "twoFactorAuthEnabled" to config.twoFactorAuthEnabled,
                "monthlySubscriptionEnabled" to config.monthlySubscriptionEnabled,
                "topBarLayout" to config.topBarLayout
            )
            firestore.collection("settings").document("admin_config").set(map)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    @Synchronized
    fun insertAdminConfig(config: AdminConfig) {
        insertAdminConfigLocalOnly(config)
        insertAdminConfigFirestore(config)
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

        try {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val mId = java.util.UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis()
            val docData = mapOf(
                "id" to Math.abs(mId.hashCode()).toLong(),
                "sender" to sender,
                "senderName" to senderName,
                "receiverId" to receiverId.toLong(),
                "message" to message,
                "timestamp" to timestamp
            )
            firestore.collection("messages").document(mId).set(docData)

            // Track active chat room for the Admin dashboard
            val chatId = "chat_user_${receiverId}"
            val chatData = mapOf(
                "id" to chatId,
                "participants" to listOf("الزائر", if (senderName.isNotEmpty()) senderName else "الحرفي $receiverId"),
                "lastMessage" to message,
                "timestamp" to timestamp,
                "isHelpRequested" to false,
                "isBlocked" to false
            )
            firestore.collection("chats").document(chatId).set(chatData)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
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
