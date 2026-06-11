package com.example.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class AppDatabase(private val context: Context) : SQLiteOpenHelper(context, "wam_services_core_db", null, 5) {

    // --- Dynamic Streams (StateFlows for UI) ---
    private val _approvedProvidersFlow = MutableStateFlow<List<Provider>>(emptyList())
    val approvedProvidersFlow: StateFlow<List<Provider>> = _approvedProvidersFlow.asStateFlow()

    private val _pendingProvidersFlow = MutableStateFlow<List<Provider>>(emptyList())
    val pendingProvidersFlow: StateFlow<List<Provider>> = _pendingProvidersFlow.asStateFlow()

    private val _chatsFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatsFlow: StateFlow<List<ChatMessage>> = _chatsFlow.asStateFlow()

    private val _categoriesFlow = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categoriesFlow: StateFlow<List<CategoryItem>> = _categoriesFlow.asStateFlow()

    private val _citiesFlow = MutableStateFlow<List<CityItem>>(emptyList())
    val citiesFlow: StateFlow<List<CityItem>> = _citiesFlow.asStateFlow()

    private val _adminConfigFlow = MutableStateFlow<AdminConfig>(AdminConfig())
    val adminConfigFlow: StateFlow<AdminConfig> = _adminConfigFlow.asStateFlow()

    private val _bannersFlow = MutableStateFlow<List<BannerAd>>(emptyList())
    val bannersFlow: StateFlow<List<BannerAd>> = _bannersFlow.asStateFlow()

    private val _reportsFlow = MutableStateFlow<List<ReportItem>>(emptyList())
    val reportsFlow: StateFlow<List<ReportItem>> = _reportsFlow.asStateFlow()

    private val _supervisorsFlow = MutableStateFlow<List<AppSupervisor>>(emptyList())
    val supervisorsFlow: StateFlow<List<AppSupervisor>> = _supervisorsFlow.asStateFlow()

    private val _bookmarkedIds = MutableStateFlow<Set<String>>(emptySet())
    val bookmarkedIds: StateFlow<Set<String>> = _bookmarkedIds.asStateFlow()

    // Active Snapshot Listeners mapping
    private val activeRegistrations = mutableListOf<ListenerRegistration>()

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS bookmarks (provider_id TEXT PRIMARY KEY)")
        db?.execSQL("""
            CREATE TABLE IF NOT EXISTS cached_providers (
                id TEXT PRIMARY KEY,
                name TEXT,
                phone TEXT,
                city TEXT,
                region TEXT,
                category TEXT,
                sub_category TEXT,
                rate REAL,
                is_vip INTEGER,
                is_pinned INTEGER,
                is_recommended INTEGER,
                is_verified INTEGER,
                photo_url TEXT,
                gps TEXT,
                id_card TEXT,
                prev_works TEXT,
                status TEXT,
                rejection_reason TEXT,
                points INTEGER
            )
        """)
        db?.execSQL("""
            CREATE TABLE IF NOT EXISTS cached_categories (
                id TEXT PRIMARY KEY,
                name_ar TEXT,
                name_en TEXT,
                description TEXT,
                icon TEXT,
                sort_order INTEGER
            )
        """)
        db?.execSQL("""
            CREATE TABLE IF NOT EXISTS cached_cities (
                id TEXT PRIMARY KEY,
                name_ar TEXT,
                name_en TEXT
            )
        """)
        db?.execSQL("""
            CREATE TABLE IF NOT EXISTS cached_config (
                id TEXT PRIMARY KEY,
                config_json TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS bookmarks")
        db?.execSQL("DROP TABLE IF EXISTS cached_providers")
        db?.execSQL("DROP TABLE IF EXISTS cached_categories")
        db?.execSQL("DROP TABLE IF EXISTS cached_cities")
        db?.execSQL("DROP TABLE IF EXISTS cached_config")
        onCreate(db)
    }

    init {
        // Load offline cache on initialization
        loadLocalCache()
        // Start real-time sync listeners
        startRealtimeSync()
    }

    private fun loadLocalCache() {
        val db = readableDatabase ?: return

        // 1. Bookmarks
        val bookmarks = mutableSetOf<String>()
        val c1 = db.rawQuery("SELECT provider_id FROM bookmarks", null)
        if (c1.moveToFirst()) {
            do {
                bookmarks.add(c1.getString(0))
            } while (c1.moveToNext())
        }
        c1.close()
        _bookmarkedIds.value = bookmarks

        // 2. Providers
        val cachedProvList = mutableListOf<Provider>()
        val c2 = db.rawQuery("SELECT * FROM cached_providers", null)
        if (c2.moveToFirst()) {
            do {
                cachedProvList.add(
                    Provider(
                        id = c2.getString(0) ?: "",
                        name = c2.getString(1) ?: "",
                        phone = c2.getString(2) ?: "",
                        locationCity = c2.getString(3) ?: "",
                        locationRegion = c2.getString(4) ?: "",
                        serviceCategory = c2.getString(5) ?: "",
                        subCategory = c2.getString(6) ?: "",
                        rate = c2.getDouble(7),
                        isVip = c2.getInt(8) == 1,
                        isPinned = c2.getInt(9) == 1,
                        isRecommended = c2.getInt(10) == 1,
                        isVerified = c2.getInt(11) == 1,
                        photoUrl = c2.getString(12) ?: "",
                        gpsCoordinates = c2.getString(13) ?: "",
                        idCardUrl = c2.getString(14) ?: "",
                        previousWorksJson = c2.getString(15) ?: "",
                        status = c2.getString(16) ?: "approved",
                        rejectionReason = c2.getString(17) ?: "",
                        points = c2.getInt(18)
                    )
                )
            } while (c2.moveToNext())
        }
        c2.close()
        _approvedProvidersFlow.value = cachedProvList.filter { it.status == "approved" }
        _pendingProvidersFlow.value = cachedProvList.filter { it.status == "pending" }

        // 3. Categories
        val cachedCats = mutableListOf<CategoryItem>()
        val c3 = db.rawQuery("SELECT * FROM cached_categories ORDER BY sort_order ASC", null)
        if (c3.moveToFirst()) {
            do {
                cachedCats.add(
                    CategoryItem(
                        id = c3.getString(0) ?: "",
                        nameAr = c3.getString(1) ?: "",
                        nameEn = c3.getString(2) ?: "",
                        description = c3.getString(3) ?: "",
                        iconName = c3.getString(4) ?: "",
                        sortOrder = c3.getInt(5)
                    )
                )
            } while (c3.moveToNext())
        }
        c3.close()
        if (cachedCats.isNotEmpty()) {
            _categoriesFlow.value = cachedCats
        } else {
            // Setup default placeholder categories
            _categoriesFlow.value = listOf(
                CategoryItem("1", "كهربائي", "Electrician", "الكهرباء المنزلية والإنارة والديكور", "bolt", 0),
                CategoryItem("2", "سباكة وصيانة", "Plumber", "صيانة الأنابيب والخزانات والمضخات", "plumbing", 1),
                CategoryItem("3", "نجار وبناء", "Carpenter", "الموبيليا وتصميم الأخشاب والديكور المطور", "carpenter", 2),
                CategoryItem("4", "تكييف وتبريد", "Ac Technician", "تركيب شاشات وصيانة أجهزة التبريد والتحكيم", "ac_unit", 3)
            )
        }

        // 4. Cities
        val cachedCits = mutableListOf<CityItem>()
        val c4 = db.rawQuery("SELECT * FROM cached_cities", null)
        if (c4.moveToFirst()) {
            do {
                cachedCits.add(
                    CityItem(
                        id = c4.getString(0) ?: "",
                        nameAr = c4.getString(1) ?: "",
                        nameEn = c4.getString(2) ?: ""
                    )
                )
            } while (c4.moveToNext())
        }
        c4.close()
        if (cachedCits.isNotEmpty()) {
            _citiesFlow.value = cachedCits
        } else {
            _citiesFlow.value = listOf(
                CityItem("1", "صنعاء", "Sana'a"),
                CityItem("2", "عدن", "Aden"),
                CityItem("3", "تعز", "Taiz"),
                CityItem("4", "الحديدة", "Hodeidah"),
                CityItem("5", "حضرموت", "Hadramout")
            )
        }

        // 5. App Setting Config
        val c5 = db.rawQuery("SELECT config_json FROM cached_config LIMIT 1", null)
        if (c5.moveToFirst()) {
            try {
                val jsonStr = c5.getString(0)
                if (!jsonStr.isNullOrEmpty()) {
                    val jo = JSONObject(jsonStr)
                    _adminConfigFlow.value = AdminConfig(
                        adminUsername = jo.optString("adminUsername", "WAM2026"),
                        adminPassword = jo.optString("adminPassword", "maher736462"),
                        appName = jo.optString("appName", "كل خدمات اليمن"),
                        slogan = jo.optString("slogan", "أكبر دليل خدمي وتنسيقي متكامل للكهرباء والسباكة والمهن الحرة"),
                        primaryColorHex = jo.optString("primaryColorHex", "#0F172A"),
                        secondaryColorHex = jo.optString("secondaryColorHex", "#3B82F6"),
                        footerText = jo.optString("footerText", "MAW 777644670"),
                        infoHtmlText = jo.optString("infoHtmlText", "مؤسسة الـ WAM للتنمية والخدمات - ريادة الأعمال التقنية في الجمهورية اليمنية."),
                        supportPhone = jo.optString("supportPhone", "777644670"),
                        supportEmail = jo.optString("supportEmail", "support@yemenservices.com"),
                        supportWhatsapp = jo.optString("supportWhatsapp", "777644670"),
                        isMaintenanceMode = jo.optBoolean("isMaintenanceMode", false),
                        footerHeightDp = jo.optInt("footerHeightDp", 56),
                        footerAlpha = jo.optDouble("footerAlpha", 1.0).toFloat(),
                        footerCustomImage = jo.optString("footerCustomImage", ""),
                        assistantActive = jo.optBoolean("assistantActive", true),
                        assistantIcon = jo.optString("assistantIcon", "🤖"),
                        assistantScale = jo.optDouble("assistantScale", 0.5).toFloat(),
                        assistantX = jo.optDouble("assistantX", 0.9).toFloat(),
                        assistantY = jo.optDouble("assistantY", 0.85).toFloat(),
                        secretKey = jo.optString("secretKey", "maher--736462")
                    )
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        c5.close()
    }

    private fun startRealtimeSync() {
        try {
            cleanupListeners()
            val firestore = FirebaseFirestore.getInstance()

            // 1. Sync Config
            val l1 = firestore.collection("app_settings").document("global_config")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Sync", "Config listen error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val config = snapshot.toObject(AdminConfig::class.java)
                        if (config != null) {
                            _adminConfigFlow.value = config
                            saveConfigLocal(config)
                        }
                    } else {
                        // Create default config remote if missing
                        val def = AdminConfig()
                        firestore.collection("app_settings").document("global_config").set(def)
                    }
                }
            activeRegistrations.add(l1)

            // 2. Sync Providers
            val l2 = firestore.collection("service_providers")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = snapshot.toObjects(Provider::class.java)
                        _approvedProvidersFlow.value = list
                        saveProvidersLocal(list, "approved")
                    }
                }
            activeRegistrations.add(l2)

            // 3. Sync Pending Requests
            val l3 = firestore.collection("pending_providers")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = snapshot.toObjects(Provider::class.java)
                        _pendingProvidersFlow.value = list
                        saveProvidersLocal(list, "pending")
                    }
                }
            activeRegistrations.add(l3)

            // 4. Sync Categories
            val l4 = firestore.collection("categories")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = snapshot.toObjects(CategoryItem::class.java)
                        _categoriesFlow.value = list.sortedBy { it.sortOrder }
                        saveCategoriesLocal(list)
                    }
                }
            activeRegistrations.add(l4)

            // 5. Sync Cities
            val l5 = firestore.collection("cities")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = snapshot.toObjects(CityItem::class.java)
                        _citiesFlow.value = list
                        saveCitiesLocal(list)
                    }
                }
            activeRegistrations.add(l5)

            // 6. Sync Banner Ads
            val l6 = firestore.collection("banners")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = snapshot.toObjects(BannerAd::class.java)
                        _bannersFlow.value = list
                    }
                }
            activeRegistrations.add(l6)

            // 7. Sync Chat Messages
            val l7 = firestore.collection("chat_messages")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = snapshot.toObjects(ChatMessage::class.java)
                        _chatsFlow.value = list.sortedBy { it.timestamp }
                    }
                }
            activeRegistrations.add(l7)

            // 8. Sync Reports
            val l8 = firestore.collection("reports")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = snapshot.toObjects(ReportItem::class.java)
                        _reportsFlow.value = list
                    }
                }
            activeRegistrations.add(l8)

            // 9. Sync Supervisors
            val l9 = firestore.collection("supervisors")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = snapshot.toObjects(AppSupervisor::class.java)
                        _supervisorsFlow.value = list
                    }
                }
            activeRegistrations.add(l9)

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun cleanupListeners() {
        for (reg in activeRegistrations) {
            reg.remove()
        }
        activeRegistrations.clear()
    }

    // --- SQLite Caching Local Operations ---

    private fun saveConfigLocal(config: AdminConfig) {
        try {
            val db = writableDatabase ?: return
            val jo = JSONObject().apply {
                put("adminUsername", config.adminUsername)
                put("adminPassword", config.adminPassword)
                put("appName", config.appName)
                put("slogan", config.slogan)
                put("primaryColorHex", config.primaryColorHex)
                put("secondaryColorHex", config.secondaryColorHex)
                put("footerText", config.footerText)
                put("infoHtmlText", config.infoHtmlText)
                put("supportPhone", config.supportPhone)
                put("supportEmail", config.supportEmail)
                put("supportWhatsapp", config.supportWhatsapp)
                put("isMaintenanceMode", config.isMaintenanceMode)
                put("footerHeightDp", config.footerHeightDp)
                put("footerAlpha", config.footerAlpha.toDouble())
                put("footerCustomImage", config.footerCustomImage)
                put("assistantActive", config.assistantActive)
                put("assistantIcon", config.assistantIcon)
                put("assistantScale", config.assistantScale.toDouble())
                put("assistantX", config.assistantX.toDouble())
                put("assistantY", config.assistantY.toDouble())
                put("secretKey", config.secretKey)
            }
            db.execSQL("INSERT OR REPLACE INTO cached_config (id, config_json) VALUES ('main', ?)", arrayOf(jo.toString()))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun saveProvidersLocal(list: List<Provider>, forceStatus: String) {
        try {
            val db = writableDatabase ?: return
            // delete existing with that status
            db.execSQL("DELETE FROM cached_providers WHERE status = ?", arrayOf(forceStatus))
            for (p in list) {
                val cv = ContentValues().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("phone", p.phone)
                    put("city", p.locationCity)
                    put("region", p.locationRegion)
                    put("category", p.serviceCategory)
                    put("sub_category", p.subCategory)
                    put("rate", p.rate)
                    put("is_vip", if (p.isVip) 1 else 0)
                    put("is_pinned", if (p.isPinned) 1 else 0)
                    put("is_recommended", if (p.isRecommended) 1 else 0)
                    put("is_verified", if (p.isVerified) 1 else 0)
                    put("photo_url", p.photoUrl)
                    put("gps", p.gpsCoordinates)
                    put("id_card", p.idCardUrl)
                    put("prev_works", p.previousWorksJson)
                    put("status", forceStatus)
                    put("rejection_reason", p.rejectionReason)
                    put("points", p.points)
                }
                db.insertWithOnConflict("cached_providers", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun saveCategoriesLocal(list: List<CategoryItem>) {
        try {
            val db = writableDatabase ?: return
            db.execSQL("DELETE FROM cached_categories")
            for (item in list) {
                val cv = ContentValues().apply {
                    put("id", item.id)
                    put("name_ar", item.nameAr)
                    put("name_en", item.nameEn)
                    put("description", item.description)
                    put("icon", item.iconName)
                    put("sort_order", item.sortOrder)
                }
                db.insert("cached_categories", null, cv)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun saveCitiesLocal(list: List<CityItem>) {
        try {
            val db = writableDatabase ?: return
            db.execSQL("DELETE FROM cached_cities")
            for (item in list) {
                val cv = ContentValues().apply {
                    put("id", item.id)
                    put("name_ar", item.nameAr)
                    put("name_en", item.nameEn)
                }
                db.insert("cached_cities", null, cv)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    // --- Bookmarks Accessors ---

    fun toggleBookmark(id: String): Boolean {
        val db = writableDatabase ?: return false
        val current = _bookmarkedIds.value.toMutableSet()
        val toggled: Boolean
        if (current.contains(id)) {
            db.delete("bookmarks", "provider_id = ?", arrayOf(id))
            current.remove(id)
            toggled = false
        } else {
            val cv = ContentValues().apply { put("provider_id", id) }
            db.insert("bookmarks", null, cv)
            current.add(id)
            toggled = true
        }
        _bookmarkedIds.value = current
        return toggled
    }

    // --- Cloud Mutator Helpers (Firestore backplane) ---

    fun updateConfigInFirestore(config: AdminConfig, onComplete: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("app_settings").document("global_config")
            .set(config)
            .addOnSuccessListener {
                _adminConfigFlow.value = config
                saveConfigLocal(config)
                onComplete(true)
            }
            .addOnFailureListener { onComplete(false) }
    }

    fun submitProviderRequest(provider: Provider, onComplete: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val targetCol = if (provider.status == "approved") "service_providers" else "pending_providers"
        val docId = if (provider.id.isEmpty()) firestore.collection(targetCol).document().id else provider.id
        val finalized = provider.copy(id = docId)

        firestore.collection(targetCol).document(docId)
            .set(finalized)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun approveRequest(p: Provider, onComplete: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val approved = p.copy(status = "approved")
        
        // 1. Delete from pending_providers
        firestore.collection("pending_providers").document(p.id).delete()
            .addOnSuccessListener {
                // 2. Add to service_providers
                firestore.collection("service_providers").document(p.id).set(approved)
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    fun rejectRequest(p: Provider, reason: String, onComplete: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val rejected = p.copy(status = "rejected", rejectionReason = reason)
        firestore.collection("pending_providers").document(p.id).set(rejected)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun deleteProvider(pId: String, onComplete: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("service_providers").document(pId).delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun sendChatMessage(msg: ChatMessage, onComplete: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val docId = firestore.collection("chat_messages").document().id
        val finalized = msg.copy(id = docId)
        firestore.collection("chat_messages").document(docId)
            .set(finalized)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun clearChatHistory(onComplete: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("chat_messages").get()
            .addOnSuccessListener { qs ->
                val batch = firestore.batch()
                for (doc in qs.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit()
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    fun saveBanner(banner: BannerAd, onComplete: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val id = if (banner.id.isEmpty()) firestore.collection("banners").document().id else banner.id
        val finalized = banner.copy(id = id)
        firestore.collection("banners").document(id).set(finalized)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun deleteBanner(id: String, onComplete: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("banners").document(id).delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun saveCategory(cat: CategoryItem, onComplete: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val id = if (cat.id.isEmpty()) firestore.collection("categories").document().id else cat.id
        val finalized = cat.copy(id = id)
        firestore.collection("categories").document(id).set(finalized)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun deleteCategory(id: String, onComplete: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("categories").document(id).delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun saveCity(city: CityItem, onComplete: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val id = if (city.id.isEmpty()) firestore.collection("cities").document().id else city.id
        val finalized = city.copy(id = id)
        firestore.collection("cities").document(id).set(finalized)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun deleteCity(id: String, onComplete: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("cities").document(id).delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun submitReport(report: ReportItem, onComplete: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val id = firestore.collection("reports").document().id
        val finalized = report.copy(id = id)
        firestore.collection("reports").document(id).set(finalized)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun saveSupervisor(sv: AppSupervisor, onComplete: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("supervisors").document(sv.username).set(sv)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun deleteSupervisor(username: String, onComplete: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("supervisors").document(username).delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // --- PURGE AND SANITIZE ALL (The Owner Emergency Options) ---

    fun nukeAllAppFilesAndLocalData(onComplete: (Boolean) -> Unit) {
        try {
            cleanupListeners()
            val db = writableDatabase
            db?.execSQL("DELETE FROM bookmarks")
            db?.execSQL("DELETE FROM cached_providers")
            db?.execSQL("DELETE FROM cached_categories")
            db?.execSQL("DELETE FROM cached_cities")
            db?.execSQL("DELETE FROM cached_config")

            // Empty live flows
            _approvedProvidersFlow.value = emptyList()
            _pendingProvidersFlow.value = emptyList()
            _chatsFlow.value = emptyList()
            _bannersFlow.value = emptyList()
            _reportsFlow.value = emptyList()
            _supervisorsFlow.value = emptyList()
            _bookmarkedIds.value = emptySet()

            // Delete SharedPreferences
            val prefs = context.getSharedPreferences("WAM_SERVICES_PREFS", Context.MODE_PRIVATE)
            prefs.edit().clear().commit()

            // Delete local cache files and temporary directories
            context.cacheDir?.deleteRecursively()
            context.filesDir?.deleteRecursively()

            // Nuke Firestore tables
            val firestore = FirebaseFirestore.getInstance()
            val collections = listOf(
                "service_providers", "pending_providers", "chat_messages", 
                "banners", "reports", "supervisors", "categories", "cities"
            )

            var finishedCount = 0
            for (col in collections) {
                firestore.collection(col).get().addOnSuccessListener { qs ->
                    val b = firestore.batch()
                    for (doc in qs.documents) {
                        b.delete(doc.reference)
                    }
                    b.commit().addOnCompleteListener {
                        finishedCount++
                        if (finishedCount == collections.size) {
                            // Restart listeners to bind correctly
                            startRealtimeSync()
                            onComplete(true)
                        }
                    }
                }.addOnFailureListener {
                    finishedCount++
                    if (finishedCount == collections.size) {
                        startRealtimeSync()
                        onComplete(true)
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            onComplete(false)
        }
    }
}
